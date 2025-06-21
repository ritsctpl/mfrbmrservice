package com.rits.overallequipmentefficiency.service;

import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.*;
import com.rits.overallequipmentefficiency.repository.AggregatedOeeRepository;
import com.rits.overallequipmentefficiency.repository.AggregatedTimePeriodRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;


@Service
@RequiredArgsConstructor
public class AggregationServiceImpl implements AggregationService {

    private static final Logger log = LoggerFactory.getLogger(AggregationServiceImpl.class);

    private final DowntimeService downtimeService;
    private final AggregatedOeeRepository aggregatedOeeRepository;
    private final AggregatedTimePeriodRepository aggregatedTimePeriodRepository;
    private final WebClient.Builder webClientBuilder;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${insights.enabled:false}")
    private boolean isInsightsEnabled;
    @Value("${performance.useLiveMaterialCycleTimes:false}")
    private boolean isLiveEnabled;


    // URLs from configuration
    @Value("${shift-service.url}/getShiftDetailBetweenTime")
    private String getShiftDetailBetweenTime;

    @Value("${productionlog-service.url}/getIntervalTimes")
    private String getIntervalTimesUrl;

    @Value("${productionlog-service.url}/totalProducedQuantity")
    private String totalProducedQuantity;


    @Value("${integration-service.uri}/logGenericMessage")
    private String logGenericMessage;

    @Value("${workcenter-service.url}/retrieve")
    private String retrieveWorkcenter;

    @Value("${workcenter-service.url}/getCell")
    private String getCell;

    @Value("${workcenter-service.url}/getCellGroup")
    private String getCellGroup;

    @Override
    @Transactional
    public AggregatedOee aggregateOee(OeeOutput oeeOutput) {

        String eventType = oeeOutput.getOeeModel().getEventTypeOfPerformance();
        /*if (!"doneSfcBatch".equalsIgnoreCase(eventType) &&
                !"completeSfcBatch".equalsIgnoreCase(eventType)) {
            return null; // or throw an exception or simply skip processing
        }*/

        // 1. Build the OeeProductionLogRequest from OeeOutput.
        OeeModel oeeModel = oeeOutput.getOeeModel();
        AvailabilityEntity availability = oeeOutput.getAvailabilityEntity();
        PerformanceModel performance = oeeOutput.getPerformanceEntity();


        // Derive the event source early from the OeeModel.
        String eventSource = oeeModel.getEventSource(); // This uses the derived getter that returns "MACHINE" or "MANUAL"
        // Process only valid event types (manual or machine)
        if (!"doneSfcBatch".equalsIgnoreCase(oeeModel.getEventTypeOfPerformance()) &&
                !"completeSfcBatch".equalsIgnoreCase(oeeModel.getEventTypeOfPerformance()) &&
                !"machineDoneSfcBatch".equalsIgnoreCase(oeeModel.getEventTypeOfPerformance()) &&
                !"machineCompleteSfcBatch".equalsIgnoreCase(oeeModel.getEventTypeOfPerformance())) {
            return null;
        }


        OeeProductionLogRequest logRequest = new OeeProductionLogRequest();
        if (isValid(oeeModel.getSite())) {
            logRequest.setSite(oeeModel.getSite());
        }
        /*if (isValid(oeeModel.getShiftId())) {
            logRequest.setShiftId(oeeModel.getShiftId());
        }*/
       if(oeeModel.getCategory().equalsIgnoreCase("WORKCENTER"))
       {
           if (isValid(oeeModel.getWorkcenterId())) {
            logRequest.setWorkcenterId(oeeModel.getWorkcenterId());
        }
       }
        if(oeeModel.getCategory().equalsIgnoreCase("RESOURCE")) {
            if (isValid(oeeModel.getResourceId())) {
                logRequest.setResourceId(oeeModel.getResourceId());
            }
        }
        if (isValid(oeeModel.getItem())) {
            logRequest.setItemId(oeeModel.getItem());
        }
        if (isValid(oeeModel.getItemVersion())) {
            logRequest.setItemVersion(oeeModel.getItemVersion());
        }
        if (isValid(oeeModel.getOperation())) {
            logRequest.setOperationId(oeeModel.getOperation());
        }
        if (isValid(oeeModel.getOperationVersion())) {
            logRequest.setOperationVersion(oeeModel.getOperationVersion());
        }
        if (isValid(oeeModel.getShoporderId())) {
            logRequest.setShopOrderBo(oeeModel.getShoporderId());
        }
        if (isValid(oeeModel.getBatchNumber())) {
            logRequest.setBatchNo(oeeModel.getBatchNumber());
        }
        // Set interval times from availability.
        logRequest.setIntervalStartDateTime(availability.getShiftStartDateTime());
        logRequest.setIntervalEndDateTime(availability.getIntervalEndDateTime());

        // Propagate the event source.
        logRequest.setEventSource(eventSource);
        logRequest.setEventType(oeeModel.getEventTypeOfPerformance());
        // 2. Adjust interval times via external service.
        IntervalTimesResponse intervalTimes = getIntervalTimes(
                logRequest,
                availability.getShiftStartDateTime(),
                availability.getIntervalEndDateTime(),
                oeeModel.getCategory()
        );
        logRequest.setIntervalStartDateTime(intervalTimes.getFirstCreatedDateTime());
        logRequest.setIntervalEndDateTime(intervalTimes.getLastCreatedDateTime());

        // 3. Compute Aggregated Availability.
        AggregatedAvailabilityResult availabilityResult = calculateAggregatedAvailability(availability, intervalTimes);

        // 4. Compute Aggregated Performance.
        String performanceEventBy = oeeModel.getCategory(); // e.g., "RESOURCE" or "WORKCENTER"
        List<OeeProductionLogResponse> performanceLogs = fetchProductionLogs(logRequest, performanceEventBy);
        double totalQty = performanceLogs.stream().mapToDouble(OeeProductionLogResponse::getGrandTotalQty).sum();
        double plannedCycleTime = performance.getPlannedCycleTime();
        double actualCycleTimeCal = (totalQty > 0) ? availabilityResult.getActualProductionTime() / totalQty : 0;
        double plannedProductionQty = plannedCycleTime > 0 ? availabilityResult.getActualProductionTime() / plannedCycleTime : 0;
        double performanceValue = plannedProductionQty > 0 ? (totalQty / plannedProductionQty) * 100 : 0;

        // 5. Compute Aggregated Quality.
        List<OeeProductionLogResponse> qualityLogs = fetchProductionLogs(logRequest, "QUALITY");
        double scrapQty = qualityLogs.stream().mapToDouble(OeeProductionLogResponse::getGrandTotalQty).sum();
        //double goodQty = totalQty - scrapQty;
        double goodQty = Math.max(0, totalQty - scrapQty);

        double qualityValue = totalQty > 0 ? (goodQty / totalQty) * 100 : 0;

        // 6. Compute overall OEE.
        double oee = (availabilityResult.getAvailability() * performanceValue * qualityValue) / 10000;

        // 7. Build new AggregatedOee record.
        AggregatedOee aggregatedOee = AggregatedOee.builder()
                .site(oeeModel.getSite())
                .shiftId(oeeModel.getShiftId())
                .workcenterId(oeeModel.getWorkcenterId())
                .resourceId(oeeModel.getResourceId())
                .logDate(availability.getAvailabilityDate() != null ? availability.getAvailabilityDate() : LocalDate.now())
                .item(oeeModel.getItem())
                .itemVersion(oeeModel.getItemVersion())
                .operation(oeeModel.getOperation())
                .operationVersion(oeeModel.getOperationVersion())
                .shopOrderId(oeeModel.getShoporderId())
                .batchNumber(oeeModel.getBatchNumber())
                .aggregatedAvailabilityId(null)
                .aggregatedPerformanceId(null)
                .aggregatedQualityId(null)
                .availability(availabilityResult.getAvailability())
                .performance(performanceValue)
                .quality(qualityValue)
                .oee(oee)
                .productionTime(0.0)
                .plan(0)
                .actualProductionTime(availabilityResult.getActualProductionTime())
                .breakTime(availabilityResult.getBreakTime())
                .plannedProductionTime(availabilityResult.getPlannedProductionTime())
                .actualTime(availabilityResult.getActualTime())
                .totalDowntime(availabilityResult.getTotalDowntime())
                .cycleTime(plannedCycleTime)
                .actualCycleTime(actualCycleTimeCal)
                .plannedQuantity(plannedProductionQty)
                .totalGoodQuantity(goodQty)
                .totalBadQuantity(scrapQty)
                .totalQuantity(totalQty)
                .category(oeeModel.getCategory())
                .eventSource(eventSource)
                .createdDatetime(LocalDateTime.now())
                .intervalStartDateTime(intervalTimes.getFirstCreatedDateTime())
                .intervalEndDateTime(intervalTimes.getLastCreatedDateTime())
                .targetQuantity(oeeModel.getTargetQuantity())
                .active(true)
                .build();

        // 7a. Deactivate any existing AggregatedOee records (by unique keys) via update.
        aggregatedOeeRepository.deactivateByUniqueKeys(
                aggregatedOee.getSite(),
                aggregatedOee.getShiftId(),
                aggregatedOee.getWorkcenterId(),
                aggregatedOee.getResourceId(),
                aggregatedOee.getItem(),
                aggregatedOee.getItemVersion(),
                aggregatedOee.getOperation(),
                aggregatedOee.getOperationVersion(),
                aggregatedOee.getShopOrderId(),
                aggregatedOee.getBatchNumber(),
                aggregatedOee.getEventSource()
        );
        aggregatedOeeRepository.save(aggregatedOee);

        // 8. Additional Aggregations for WORKCENTER-level records.
        // For WORKCENTER-level aggregation, we assume that if only site is provided (and no other dimensions),
        // then the input shiftId is used for SHIFT-level aggregation.
        /*if ("WORKCENTER".equalsIgnoreCase(oeeModel.getCategory())
                && isValid(oeeModel.getSite())
            //    && isValid(oeeModel.getShiftId())
                && isValid(oeeModel.getWorkcenterId())    // WORKCENTER not used in DAY/MONTH aggregation per new requirements.
                && !isValid(oeeModel.getResourceId())
                && !isValid(oeeModel.getItem())
                && !isValid(oeeModel.getItemVersion())
                && !isValid(oeeModel.getOperation())
                && !isValid(oeeModel.getOperationVersion())
                && !isValid(oeeModel.getShoporderId())
                && !isValid(oeeModel.getBatchNumber())) {

            AggregatedTimePeriodInput aggregatedTimePeriodInput = buildAggregatedTimePeriodInput(oeeModel, availability);
            publishAggregatedTimePeriodInput(aggregatedTimePeriodInput);
        }*/

        if ("WORKCENTER".equalsIgnoreCase(aggregatedOee.getCategory())
                && isValid(aggregatedOee.getSite())
                //    && isValid(oeeModel.getShiftId())
                && isValid(aggregatedOee.getWorkcenterId())    // WORKCENTER not used in DAY/MONTH aggregation per new requirements.
                && !isValid(aggregatedOee.getResourceId())
                && !isValid(aggregatedOee.getItem())
                && !isValid(aggregatedOee.getItemVersion())
                && !isValid(aggregatedOee.getOperation())
                && !isValid(aggregatedOee.getOperationVersion())
                && !isValid(aggregatedOee.getShopOrderId())
                && !isValid(aggregatedOee.getBatchNumber())) {

            AggregatedTimePeriodInput aggregatedTimePeriodInput = buildAggregatedTimePeriodInput(oeeModel, availability);
            publishAggregatedTimePeriodInput(aggregatedTimePeriodInput);
        }

        return aggregatedOee;
    }

    private AggregatedTimePeriodInput buildAggregatedTimePeriodInput(OeeModel oeeModel, AvailabilityEntity availability) {

        return AggregatedTimePeriodInput.builder()
                .site(oeeModel.getSite())
                .shiftId(oeeModel.getShiftId())
                .workcenterId(oeeModel.getWorkcenterId())
                .availabilityDate(availability.getAvailabilityDate())
                .eventSource(oeeModel.getEventSource())
                .build();
    }
    private void publishAggregatedTimePeriodInput(AggregatedTimePeriodInput aggregatedTimePeriodInput) {

        try {
            // Prepare the payload for the WebClient
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("topicName", "aggregatedTimePeriodTopic");
            requestPayload.put("payload", aggregatedTimePeriodInput); // Directly map PerformanceInput to payload

            // Send the request to the integration-service using WebClientBuilder
            webClientBuilder.build()
                    .post()
                    .uri(logGenericMessage)
                    .bodyValue(requestPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Published AggregatedTimePeriodInput: {}, Response: {}", aggregatedTimePeriodInput, response))
                    .doOnError(e -> log.error("Failed to publish AggregatedTimePeriodInput: {}", aggregatedTimePeriodInput, e))
                    .block();
        } catch (Exception e) {
            log.error("Exception while publishing AggregatedTimePeriodInput: {}", aggregatedTimePeriodInput, e);
        }
    }

    @Override
    @Transactional
    public AggregatedTimePeriod aggregatedTimePeriod(AggregatedTimePeriodInput input) {

        LocalDate day = input.getAvailabilityDate() != null ? input.getAvailabilityDate() : LocalDate.now();
        int aggYear = day.getYear();
        int aggMonth = day.getMonthValue();
        int aggDay = day.getDayOfMonth(); // This gets the actual day number
        String eventSource = input.getEventSource();
        // ------------------
        // LINE-LEVEL AGGREGATION (Existing)
        // ------------------
        // SHIFT-level aggregation: Use the given shift id only.
        aggregateByWorkcenterIdAndShift(input.getSite(), input.getWorkcenterId(), input.getShiftId(), day, aggMonth, aggYear, eventSource);
       // DAY-level aggregation: Aggregate from SHIFT-level AggregatedTimePeriod records for the given site and day.
        aggregateByWorkcenterIdAndDay(input.getSite(), input.getWorkcenterId(), day, aggMonth, aggYear, eventSource);
       // MONTH-level aggregation: Aggregate from DAY-level records for the given site and month.
        aggregateByWorkcenterIdAndMonth(input.getSite(), input.getWorkcenterId(), day, aggYear, aggMonth, eventSource);
       // YEAR-level aggregation: Aggregate from MONTH-level records for the given site and month.
        aggregateByWorkcenterIdAndYear(input.getSite(), input.getWorkcenterId(), day, aggYear, aggMonth, eventSource);


/*
        aggregateByWorkcenterIdAndShift(input.getSite(), input.getWorkcenterId(), input.getShiftId(), day, aggMonth, aggYear, eventSource);
        aggregateByWorkcenterIdAndDay(input.getSite(), input.getWorkcenterId(), day, aggMonth, aggYear, eventSource);
        aggregateByWorkcenterIdAndMonth(input.getSite(), input.getWorkcenterId(), day, aggYear, aggMonth, eventSource);
        aggregateByWorkcenterIdAndYear(input.getSite(), input.getWorkcenterId(), day, aggYear, aggMonth, eventSource);
*/

        // ------------------
        // NEW HIERARCHY: GET CELL AND CELL GROUP
        // ------------------
        // (Assuming input.getWorkcenterId() is the lower-level (line) workcenter id.)
        String cell = getCellForWorkcenter(input.getWorkcenterId(),input.getSite());
        String cellGroup = getCellGroupForCell(cell, input.getSite());

        // ------------------
        // NEW AGGREGATIONS FOR THE CELL LEVEL
        // ------------------
        // CELL_SHIFT: aggregate line-level AggregatedTimePeriod records for all workcenters that belong to the cell.
        aggregateByCellShift(input.getSite(), cell, input.getShiftId(), day, aggMonth, aggYear, eventSource);
        // CELL_DAY: aggregate from CELL_SHIFT records for the cell.
        aggregateByCellDay(input.getSite(), cell, day, aggMonth, aggYear, eventSource);
        // CELL_MONTH: aggregate from CELL_DAY records for the cell.
        aggregateByCellMonth(input.getSite(), cell, day, aggYear, aggMonth, eventSource);
        // CELL_YEAR: aggregate from CELL_MONTH records for the cell.
        aggregateByCellYear(input.getSite(), cell, day, aggYear, aggMonth, eventSource);

        // ------------------
        // NEW AGGREGATIONS FOR THE CELL GROUP LEVEL
        // ------------------
        // CELL_GROUP_SHIFT: For the given cell group, retrieve all cells in it (e.g. via workcenter-service), then
        // query the CELL_SHIFT records for those cells and aggregate.
        aggregateByCellGroupShift(input.getSite(), cellGroup,input.getShiftId(), day, aggMonth, aggYear, eventSource);
        // CELL_GROUP_DAY: aggregate from CELL_GROUP_SHIFT records.
        aggregateByCellGroupDay(input.getSite(), cellGroup, day, aggMonth, aggYear, eventSource);
        // CELL_GROUP_MONTH: aggregate from CELL_GROUP_DAY records.
        aggregateByCellGroupMonth(input.getSite(), cellGroup, day, aggYear, aggMonth, eventSource);
        // CELL_GROUP_YEAR: aggregate from CELL_GROUP_MONTH records.
        aggregateByCellGroupYear(input.getSite(), cellGroup, day, aggYear, aggMonth, eventSource);


    //    aggregateByShift(input.getSite(), input.getShiftId(), day, eventSource);

    //    aggregateByDay(input.getSite(), day, eventSource);

    //    aggregateByMonth(input.getSite(), aggYear, aggMonth, day, eventSource);

    //    aggregateByYear(input.getSite(), aggYear, aggMonth, day, eventSource);

        runInsightsIfEnabled(true,input);

        return null;
    }

    private void aggregateByWorkcenterIdAndShift(String site, String workcenterId, String shiftId, LocalDate day, int month, int year, String eventSource) {
        // Query active AggregatedOee records for this site, shiftId, and day
        // where workcenterId, resourceId, item, etc. are null or empty.
        List<AggregatedOee> shiftRecords = aggregatedOeeRepository.findForShiftAggregationByWorkcenterIdAndShift(site,workcenterId, shiftId, day, eventSource);
        if (shiftRecords == null || shiftRecords.isEmpty()) {
            return;
        }
        double sumActualProductionTime = shiftRecords.stream().mapToDouble(AggregatedOee::getActualProductionTime).sum();
        double sumBreakTime = shiftRecords.stream().mapToDouble(AggregatedOee::getBreakTime).sum();
        double sumPlannedProductionTime = shiftRecords.stream().mapToDouble(AggregatedOee::getPlannedProductionTime).sum();
        double sumActualTime = shiftRecords.stream().mapToDouble(AggregatedOee::getActualTime).sum();
        double sumTotalDowntime = shiftRecords.stream().mapToDouble(AggregatedOee::getTotalDowntime).sum();
        double sumCycleTime = shiftRecords.stream().mapToDouble(AggregatedOee::getCycleTime).sum();
        double sumPlannedQuantity = shiftRecords.stream().mapToDouble(AggregatedOee::getPlannedQuantity).sum();
        double sumTotalGoodQuantity = shiftRecords.stream().mapToDouble(AggregatedOee::getTotalGoodQuantity).sum();
        double sumTotalBadQuantity = shiftRecords.stream().mapToDouble(AggregatedOee::getTotalBadQuantity).sum();
        double sumTotalQuantity = shiftRecords.stream().mapToDouble(AggregatedOee::getTotalQuantity).sum();
        double sumTotalTargetQuantity = shiftRecords.stream().mapToDouble(AggregatedOee::getTargetQuantity).sum();

        double shiftAvailability = sumPlannedProductionTime > 0 ? (sumActualProductionTime / sumPlannedProductionTime) * 100 : 0;
        double shiftPerformance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double shiftQuality = sumTotalQuantity > 0 ? (sumTotalGoodQuantity / sumTotalQuantity) * 100 : 0;
        double shiftOee = (shiftAvailability * shiftPerformance * shiftQuality) / 10000;
        double actualCycleTime = (sumTotalQuantity > 0) ? sumActualProductionTime / sumTotalQuantity : 0;

        LocalDate monthDate = LocalDate.of(year, month, 1);

        AggregatedTimePeriod newShift = AggregatedTimePeriod.builder()
                .site(site)
                .shiftId(shiftId) // Note: In this level, we only use site and shiftId.
                .logDate(day)
                .category("WORKCENTER_SHIFT")
                .day(day.getDayOfMonth())
                .month(day.getMonthValue())
                .year(day.getYear())
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumTotalGoodQuantity)
                .totalBadQuantity(sumTotalBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(shiftAvailability)
                .performance(shiftPerformance)
                .quality(shiftQuality)
                .oee(shiftOee)
                .workcenterId(workcenterId)
                .eventSource(eventSource)
                .createdDatetime(LocalDateTime.now())
                .targetQuantity(sumTotalTargetQuantity)
                .active(true)
                .build();
        // Deactivate any existing SHIFT-level records for this site, shiftId, and day.
       // aggregatedTimePeriodRepository.deactivateWorkcenterIdAndShiftRecord(site,  day, workcenterId, shiftId);
        aggregatedTimePeriodRepository.deactivateWorkcenterIdAndShiftRecord(
                site,
                day,
                day.getDayOfMonth(),
                day.getMonthValue(),
                day.getYear(),
                workcenterId,
                shiftId,
                eventSource
        );

        aggregatedTimePeriodRepository.save(newShift);
    }

    private void aggregateByWorkcenterIdAndDay(String site, String workcenterId, LocalDate day, int month, int year, String eventSource) {
        List<AggregatedTimePeriod> shiftAggregates = aggregatedTimePeriodRepository.findForDayAggregationByWorkcenterIdAndShift(site, workcenterId, day, eventSource);
        if (shiftAggregates == null || shiftAggregates.isEmpty()) {
            return;
        }
        double sumActualProductionTime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualProductionTime).sum();
        double sumBreakTime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getBreakTime).sum();
        double sumPlannedProductionTime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedProductionTime).sum();
        double sumActualTime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualTime).sum();
        double sumTotalDowntime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalDowntime).sum();
        double sumCycleTime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getCycleTime).max().orElse(0.0);
        double sumPlannedQuantity = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedQuantity).sum();
        double sumTotalGoodQuantity = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalGoodQuantity).sum();
        double sumTotalBadQuantity = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalBadQuantity).sum();
        double sumTotalQuantity = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalQuantity).sum();
        double sumTotalTargetQuantity = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getTargetQuantity).sum();

        double actualCycleTime = (sumTotalQuantity > 0) ? sumActualProductionTime / sumTotalQuantity : 0;
        double dayAvailability = sumPlannedProductionTime > 0 ? (sumActualProductionTime / sumPlannedProductionTime) * 100 : 0;
        double dayPerformance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double dayQuality = sumTotalQuantity > 0 ? (sumTotalGoodQuantity / sumTotalQuantity) * 100 : 0;
        double dayOee = (dayAvailability * dayPerformance * dayQuality) / 10000;

        LocalDate monthDate = LocalDate.of(year, month, 1);

        AggregatedTimePeriod newDay = AggregatedTimePeriod.builder()
                .site(site)
                .logDate(day)
                .category("WORKCENTER_DAY")
                .day(day.getDayOfMonth())
                .month(day.getMonthValue())
                .year(day.getYear())
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumTotalGoodQuantity)
                .totalBadQuantity(sumTotalBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(dayAvailability)
                .performance(dayPerformance)
                .quality(dayQuality)
                .oee(dayOee)
                .workcenterId(workcenterId)
                .eventSource(eventSource)
                .targetQuantity(sumTotalTargetQuantity)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();
        // Deactivate any existing DAY-level record for this site and day.
      //  aggregatedTimePeriodRepository.deactivateWorkcenterIdAndDayRecord(site, workcenterId, day);
        aggregatedTimePeriodRepository.deactivateWorkcenterIdAndDayRecord(
                site,
                workcenterId,
                day,
                day.getDayOfMonth(),
                day.getMonthValue(),
                day.getYear(),
                eventSource
        );

        aggregatedTimePeriodRepository.save(newDay);
    }

    private void aggregateByWorkcenterIdAndMonth(String site, String workcenterId, LocalDate day, int year, int month, String eventSource) {
        List<AggregatedTimePeriod> dailyAggregates = aggregatedTimePeriodRepository.findForMonthAggregationByWorkcenterId(site, workcenterId, year, month, eventSource);
        if (dailyAggregates == null || dailyAggregates.isEmpty()) {
            return;
        }
        double sumActualProductionTime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualProductionTime).sum();
        double sumBreakTime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getBreakTime).sum();
        double sumPlannedProductionTime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedProductionTime).sum();
        double sumActualTime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualTime).sum();
        double sumTotalDowntime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalDowntime).sum();
        double sumCycleTime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getCycleTime).max().orElse(0.0);
        double sumPlannedQuantity = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedQuantity).sum();
        double sumTotalGoodQuantity = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalGoodQuantity).sum();
        double sumTotalBadQuantity = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalBadQuantity).sum();
        double sumTotalQuantity = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalQuantity).sum();
        double sumTotalTargetQuantity = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTargetQuantity).sum();

        double actualCycleTime = (sumTotalQuantity > 0) ? sumActualProductionTime / sumTotalQuantity : 0;
        double monthlyAvailability = sumPlannedProductionTime > 0 ? (sumActualProductionTime / sumPlannedProductionTime) * 100 : 0;
        double monthlyPerformance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double monthlyQuality = sumTotalQuantity > 0 ? (sumTotalGoodQuantity / sumTotalQuantity) * 100 : 0;
        double monthlyOee = (monthlyAvailability * monthlyPerformance * monthlyQuality) / 10000;

        LocalDate monthDate = LocalDate.of(year, month, 1);
        // Deactivate any existing MONTH-level record for this site and month.
//        aggregatedTimePeriodRepository.deactivateWorkcenterIdAndMonthRecord(site, workcenterId, monthDate);
        aggregatedTimePeriodRepository.deactivateWorkcenterIdAndMonthRecord(site, workcenterId, month, year, eventSource);

        AggregatedTimePeriod newMonth = AggregatedTimePeriod.builder()
                .site(site)
                .logDate(day)
                .category("WORKCENTER_MONTH")
                .day(day.getDayOfMonth())
                .month(day.getMonthValue())
                .year(day.getYear())
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumTotalGoodQuantity)
                .totalBadQuantity(sumTotalBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(monthlyAvailability)
                .performance(monthlyPerformance)
                .quality(monthlyQuality)
                .oee(monthlyOee)
                .workcenterId(workcenterId)
                .eventSource(eventSource)
                .targetQuantity(sumTotalTargetQuantity)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();
        aggregatedTimePeriodRepository.save(newMonth);
    }

    private void aggregateByYear(String site, int year, int month, LocalDate day, String eventSource) {
        List<AggregatedTimePeriod> monthlyAggregates = aggregatedTimePeriodRepository.findForYearAggregation(site, year, eventSource);

        if (monthlyAggregates == null || monthlyAggregates.isEmpty()) {
            return;
        }

        double sumActualProductionTime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualProductionTime).sum();
        double sumBreakTime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getBreakTime).sum();
        double sumPlannedProductionTime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedProductionTime).sum();
        double sumActualTime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualTime).sum();
        double sumTotalDowntime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalDowntime).sum();
        double sumCycleTime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getCycleTime).max().orElse(0.0);
        double sumPlannedQuantity = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedQuantity).sum();
        double sumTotalGoodQuantity = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalGoodQuantity).sum();
        double sumTotalBadQuantity = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalBadQuantity).sum();
        double sumTotalQuantity = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalQuantity).sum();

        double actualCycleTime = (sumTotalQuantity > 0) ? sumActualProductionTime / sumTotalQuantity : 0;
        double yearlyAvailability = sumPlannedProductionTime > 0 ? (sumActualProductionTime / sumPlannedProductionTime) * 100 : 0;
        double yearlyPerformance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double yearlyQuality = sumTotalQuantity > 0 ? (sumTotalGoodQuantity / sumTotalQuantity) * 100 : 0;
        double yearlyOee = (yearlyAvailability * yearlyPerformance * yearlyQuality) / 10000;

        LocalDate yearDate = LocalDate.of(year, month, 1);
        // Deactivate any existing YEAR-level record for this site and year.
        //aggregatedTimePeriodRepository.deactivateYearRecord(site, yearDate);
        aggregatedTimePeriodRepository.deactivateYearRecord(site, year, eventSource);

        AggregatedTimePeriod newYear = AggregatedTimePeriod.builder()
                .site(site)
                .logDate(day)
                .category("YEAR")
                .day(day.getDayOfMonth())
                .month(day.getMonthValue())
                .year(day.getYear())
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumTotalGoodQuantity)
                .totalBadQuantity(sumTotalBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(yearlyAvailability)
                .performance(yearlyPerformance)
                .quality(yearlyQuality)
                .oee(yearlyOee)
                .eventSource(eventSource)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();

        aggregatedTimePeriodRepository.save(newYear);
    }

    private void aggregateByWorkcenterIdAndYear(String site, String workcenterId, LocalDate day, int year, int month, String eventSource) {
        List<AggregatedTimePeriod> monthlyAggregates = aggregatedTimePeriodRepository.findForYearAggregationByWorkcenterId(site, workcenterId, year, eventSource);

        if (monthlyAggregates == null || monthlyAggregates.isEmpty()) {
            return;
        }

        double sumActualProductionTime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualProductionTime).sum();
        double sumBreakTime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getBreakTime).sum();
        double sumPlannedProductionTime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedProductionTime).sum();
        double sumActualTime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualTime).sum();
        double sumTotalDowntime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalDowntime).sum();
        double sumCycleTime = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getCycleTime).max().orElse(0.0);
        double sumPlannedQuantity = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedQuantity).sum();
        double sumTotalGoodQuantity = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalGoodQuantity).sum();
        double sumTotalBadQuantity = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalBadQuantity).sum();
        double sumTotalQuantity = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalQuantity).sum();
        double sumTotalTargetQuantity = monthlyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTargetQuantity).sum();

        double actualCycleTime = (sumTotalQuantity > 0) ? sumActualProductionTime / sumTotalQuantity : 0;
        double yearlyAvailability = sumPlannedProductionTime > 0 ? (sumActualProductionTime / sumPlannedProductionTime) * 100 : 0;
        double yearlyPerformance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double yearlyQuality = sumTotalQuantity > 0 ? (sumTotalGoodQuantity / sumTotalQuantity) * 100 : 0;
        double yearlyOee = (yearlyAvailability * yearlyPerformance * yearlyQuality) / 10000;

        LocalDate yearDate = LocalDate.of(year, month, 1);
        // Deactivate any existing YEAR-level record for this site and year.
        //aggregatedTimePeriodRepository.deactivateWorkcenterIdAndYearRecord(site, workcenterId, yearDate);

        aggregatedTimePeriodRepository.deactivateWorkcenterIdAndYearRecord(site, workcenterId, year, eventSource);


        AggregatedTimePeriod newYear = AggregatedTimePeriod.builder()
                .site(site)
                .logDate(day)
                .category("WORKCENTER_YEAR")
                .day(day.getDayOfMonth())
                .month(day.getMonthValue())
                .year(day.getYear())
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumTotalGoodQuantity)
                .totalBadQuantity(sumTotalBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(yearlyAvailability)
                .performance(yearlyPerformance)
                .quality(yearlyQuality)
                .oee(yearlyOee)
                .workcenterId(workcenterId)
                .eventSource(eventSource)
                .totalQuantity(sumTotalTargetQuantity)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();

        aggregatedTimePeriodRepository.save(newYear);
    }


    // -------------- Aggregation Methods --------------

    /**
     * Aggregates SHIFT-level values using the given site, shiftId, and day.
     * It queries active AggregatedOee records where optional dimensions are null.
     */
    private void aggregateByShift(String site, String shiftId, LocalDate day, String eventSource) {
        // Query active AggregatedOee records for this site, shiftId, and day
        // where workcenterId, resourceId, item, etc. are null or empty.
        List<AggregatedOee> shiftRecords = aggregatedOeeRepository.findForShiftAggregationByShift(site, shiftId, day, eventSource);
        if (shiftRecords == null || shiftRecords.isEmpty()) {
            return;
        }
        double sumActualProductionTime = shiftRecords.stream().mapToDouble(AggregatedOee::getActualProductionTime).sum();
        double sumBreakTime = shiftRecords.stream().mapToDouble(AggregatedOee::getBreakTime).sum();
        double sumPlannedProductionTime = shiftRecords.stream().mapToDouble(AggregatedOee::getPlannedProductionTime).sum();
        double sumActualTime = shiftRecords.stream().mapToDouble(AggregatedOee::getActualTime).sum();
        double sumTotalDowntime = shiftRecords.stream().mapToDouble(AggregatedOee::getTotalDowntime).sum();
        double sumCycleTime = shiftRecords.stream().mapToDouble(AggregatedOee::getCycleTime).max().orElse(0.0);
        double sumPlannedQuantity = shiftRecords.stream().mapToDouble(AggregatedOee::getPlannedQuantity).sum();
        double sumTotalGoodQuantity = shiftRecords.stream().mapToDouble(AggregatedOee::getTotalGoodQuantity).sum();
        double sumTotalBadQuantity = shiftRecords.stream().mapToDouble(AggregatedOee::getTotalBadQuantity).sum();
        double sumTotalQuantity = shiftRecords.stream().mapToDouble(AggregatedOee::getTotalQuantity).sum();

        double shiftAvailability = sumPlannedProductionTime > 0 ? (sumActualProductionTime / sumPlannedProductionTime) * 100 : 0;
        double shiftPerformance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double shiftQuality = sumTotalQuantity > 0 ? (sumTotalGoodQuantity / sumTotalQuantity) * 100 : 0;
        double shiftOee = (shiftAvailability * shiftPerformance * shiftQuality) / 10000;
        double actualCycleTime = (sumTotalQuantity > 0) ? sumActualProductionTime / sumTotalQuantity : 0;

        AggregatedTimePeriod newShift = AggregatedTimePeriod.builder()
                .site(site)
                .shiftId(shiftId) // Note: In this level, we only use site and shiftId.
                .logDate(day)
                .category("SHIFT")
                .day(day.getDayOfMonth())
                .month(day.getMonthValue())
                .year(day.getYear())
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumTotalGoodQuantity)
                .totalBadQuantity(sumTotalBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(shiftAvailability)
                .performance(shiftPerformance)
                .quality(shiftQuality)
                .oee(shiftOee)
                .eventSource(eventSource)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();
        // Deactivate any existing SHIFT-level records for this site, shiftId, and day.
        //aggregatedTimePeriodRepository.deactivateShiftRecord(site,  day, shiftId);
        aggregatedTimePeriodRepository.deactivateShiftRecord(
                site,
                day,
                day.getDayOfMonth(),
                day.getMonthValue(),
                day.getYear(),
                shiftId,
                eventSource
        );

        aggregatedTimePeriodRepository.save(newShift);
    }

    /**
     * Aggregates DAY-level values by summing SHIFT-level AggregatedTimePeriod records.
     * Here, we aggregate active SHIFT-level records for the given site and day.
     */
    private void aggregateByDay(String site, LocalDate day, String eventSource) {

/*        // Lock the active aggregated DAY records
        List<AggregatedTimePeriod> activeRecords = aggregatedTimePeriodRepository.findActiveDayRecordsForLock(
                site, day, day.getDayOfMonth(),day.getMonthValue(), day.getYear(), eventSource);*/

        List<AggregatedTimePeriod> shiftAggregates = aggregatedTimePeriodRepository.findForDayAggregationByShift(site, day, eventSource);
        if (shiftAggregates == null || shiftAggregates.isEmpty()) {
            return;
        }
        double sumActualProductionTime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualProductionTime).sum();
        double sumBreakTime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getBreakTime).sum();
        double sumPlannedProductionTime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedProductionTime).sum();
        double sumActualTime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualTime).sum();
        double sumTotalDowntime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalDowntime).sum();
        double sumCycleTime = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getCycleTime).max().orElse(0.0);
        double sumPlannedQuantity = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedQuantity).sum();
        double sumTotalGoodQuantity = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalGoodQuantity).sum();
        double sumTotalBadQuantity = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalBadQuantity).sum();
        double sumTotalQuantity = shiftAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalQuantity).sum();

        double actualCycleTime = (sumTotalQuantity > 0) ? sumActualProductionTime / sumTotalQuantity : 0;
        double dayAvailability = sumPlannedProductionTime > 0 ? (sumActualProductionTime / sumPlannedProductionTime) * 100 : 0;
        double dayPerformance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double dayQuality = sumTotalQuantity > 0 ? (sumTotalGoodQuantity / sumTotalQuantity) * 100 : 0;
        double dayOee = (dayAvailability * dayPerformance * dayQuality) / 10000;

        AggregatedTimePeriod newDay = AggregatedTimePeriod.builder()
                .site(site)
                .logDate(day)
                .category("DAY")
                .day(day.getDayOfMonth())
                .month(day.getMonthValue())
                .year(day.getYear())
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumTotalGoodQuantity)
                .totalBadQuantity(sumTotalBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(dayAvailability)
                .performance(dayPerformance)
                .quality(dayQuality)
                .oee(dayOee)
                .eventSource(eventSource)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();
        // Deactivate any existing DAY-level record for this site and day.
        //aggregatedTimePeriodRepository.deactivateDayRecord(site, day);
        aggregatedTimePeriodRepository.deactivateDayRecord(
                site,
                day,
                day.getDayOfMonth(),
                day.getMonthValue(),
                day.getYear(),
                eventSource
        );

        aggregatedTimePeriodRepository.save(newDay);
    }

    /**
     * Aggregates MONTH-level values by summing DAY-level AggregatedTimePeriod records.
     * Here, we aggregate active DAY-level records for the given site and matching year/month.
     */
    private void aggregateByMonth(String site, int year, int month, LocalDate day, String eventSource) {
        List<AggregatedTimePeriod> dailyAggregates = aggregatedTimePeriodRepository.findForMonthAggregation(site, year, month, eventSource);
        if (dailyAggregates == null || dailyAggregates.isEmpty()) {
            return;
        }
        double sumActualProductionTime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualProductionTime).sum();
        double sumBreakTime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getBreakTime).sum();
        double sumPlannedProductionTime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedProductionTime).sum();
        double sumActualTime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getActualTime).sum();
        double sumTotalDowntime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalDowntime).sum();
        double sumCycleTime = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getCycleTime).max().orElse(0.0);
        double sumPlannedQuantity = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getPlannedQuantity).sum();
        double sumTotalGoodQuantity = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalGoodQuantity).sum();
        double sumTotalBadQuantity = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalBadQuantity).sum();
        double sumTotalQuantity = dailyAggregates.stream().mapToDouble(AggregatedTimePeriod::getTotalQuantity).sum();

        double actualCycleTime = (sumTotalQuantity > 0) ? sumActualProductionTime / sumTotalQuantity : 0;
        double monthlyAvailability = sumPlannedProductionTime > 0 ? (sumActualProductionTime / sumPlannedProductionTime) * 100 : 0;
        double monthlyPerformance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double monthlyQuality = sumTotalQuantity > 0 ? (sumTotalGoodQuantity / sumTotalQuantity) * 100 : 0;
        double monthlyOee = (monthlyAvailability * monthlyPerformance * monthlyQuality) / 10000;

        LocalDate monthDate = LocalDate.of(year, month, 1);
        // Deactivate any existing MONTH-level record for this site and month.
        //aggregatedTimePeriodRepository.deactivateMonthRecord(site, monthDate);
        aggregatedTimePeriodRepository.deactivateMonthRecord(site, month, year, eventSource);

        AggregatedTimePeriod newMonth = AggregatedTimePeriod.builder()
                .site(site)
                .logDate(day)
                .category("MONTH")
                .day(day.getDayOfMonth())
                .month(day.getMonthValue())
                .year(day.getYear())
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumTotalGoodQuantity)
                .totalBadQuantity(sumTotalBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(monthlyAvailability)
                .performance(monthlyPerformance)
                .quality(monthlyQuality)
                .oee(monthlyOee)
                .eventSource(eventSource)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();
        aggregatedTimePeriodRepository.save(newMonth);
    }

    // ------------------ Helper Methods ------------------

    private IntervalTimesResponse getIntervalTimes(OeeProductionLogRequest request,
                                                   LocalDateTime intervalStartDateTime,
                                                   LocalDateTime intervalEndDateTime,
                                                   String category) {
        LocalDateTime startTime;
        LocalDateTime endTime;
        if ((request.getBatchNo() == null || request.getBatchNo().trim().isEmpty()) &&
                (request.getShopOrderBo() == null || request.getShopOrderBo().trim().isEmpty()) &&
                (request.getOperationId() == null || request.getOperationId().trim().isEmpty()) &&
                (request.getOperationVersion() == null || request.getOperationVersion().trim().isEmpty()) &&
                (request.getItemId() == null || request.getItemId().trim().isEmpty()) &&
                (request.getItemVersion() == null || request.getItemVersion().trim().isEmpty())) {
            startTime = intervalStartDateTime;
            endTime = intervalEndDateTime;
        } else {
            /*if (category != null) {
                if (category.equalsIgnoreCase("resource")) {
                    request.setEventType("completeSfcBatch");
                } else if (category.equalsIgnoreCase("workcenter")) {
                    request.setEventType("doneSfcBatch");
                }
            }*/
            IntervalTimesResponse response = webClientBuilder.build()
                    .post()
                    .uri(getIntervalTimesUrl)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(IntervalTimesResponse.class)
                    .block();
            startTime = (response != null) ? response.getFirstCreatedDateTime() : intervalStartDateTime;
            endTime = (response != null) ? response.getLastCreatedDateTime() : intervalEndDateTime;
        }
        return new IntervalTimesResponse(startTime, endTime);
    }

    private List<OeeProductionLogResponse> fetchProductionLogs(OeeProductionLogRequest logRequest, String eventBy) {
        List<OeeProductionLogResponse> responses = new ArrayList<>();
        List<String> eventTypes = new ArrayList<>();
        if ("RESOURCE".equalsIgnoreCase(eventBy) && "completeSfcBatch".equalsIgnoreCase(logRequest.getEventType())) {
            eventTypes.add("completeSfcBatch");
            logRequest.setWorkcenterId(null);
        } else if ("RESOURCE".equalsIgnoreCase(eventBy) && "machineCompleteSfcBatch".equalsIgnoreCase(logRequest.getEventType())) {
            eventTypes.add("machineCompleteSfcBatch");
            logRequest.setWorkcenterId(null);
        } else if ("WORKCENTER".equalsIgnoreCase(eventBy) && "doneSfcBatch".equalsIgnoreCase(logRequest.getEventType())) {
            eventTypes.add("doneSfcBatch");
        } else if ("WORKCENTER".equalsIgnoreCase(eventBy) && "machineDoneSfcBatch".equalsIgnoreCase(logRequest.getEventType())) {
            eventTypes.add("machineDoneSfcBatch");
        } else if ("QUALITY".equalsIgnoreCase(eventBy) && ("completeSfcBatch".equalsIgnoreCase(logRequest.getEventType()) || "doneSfcBatch".equalsIgnoreCase(logRequest.getEventType()))) {
            eventTypes.add("ScrapSFC");
        } else if ("QUALITY".equalsIgnoreCase(eventBy) && ("machineCompleteSfcBatch".equalsIgnoreCase(logRequest.getEventType()) || "machineDoneSfcBatch".equalsIgnoreCase(logRequest.getEventType()))) {
            eventTypes.add("machineScrapSfcBatch");
        }
        for (String eventType : eventTypes) {
            logRequest.setEventType(eventType);
            List<OeeProductionLogResponse> responseList = webClientBuilder.build()
                    .post()
                    .uri(totalProducedQuantity)
                    .bodyValue(logRequest)
                    .retrieve()
                    .bodyToFlux(OeeProductionLogResponse.class)
                    .collectList()
                    .block();
            if (responseList != null && !responseList.isEmpty()) {
                responses.addAll(responseList);
            }
        }
        return responses;
    }

    private List<ShiftOutput> fetchShiftDetails(AvailabilityEntity request) {
        ShiftInput shiftRequest = new ShiftInput(
                request.getSite(),
                request.getResourceId(),
                request.getWorkcenterId(),
                request.getShiftStartDateTime(),
                request.getIntervalEndDateTime()
        );
        return webClientBuilder.build()
                .post()
                .uri(getShiftDetailBetweenTime)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToFlux(ShiftOutput.class)
                .collectList()
                .block();
    }

    private double calculateDowntime(AvailabilityEntity request) {
        DowntimeRequest downtimeRequest = new DowntimeRequest();
        downtimeRequest.setSite(request.getSite());
        downtimeRequest.setResourceId(request.getResourceId());
        downtimeRequest.setStartDateTime(request.getShiftStartDateTime());
        downtimeRequest.setEndDateTime(request.getIntervalEndDateTime());
        return downtimeService.getBreakHoursBetweenTime(downtimeRequest);
    }

    private boolean isValid(String value) {
        return StringUtils.hasText(value);
    }

    /**
     * Calculate the aggregated availability.
     * Uses the computed interval times to fetch shift details, calculates planned production time,
     * break time, downtime, and then the availability percentage.
     */


    private AggregatedAvailabilityResult calculateAggregatedAvailability(AvailabilityEntity availability,
                                                                         IntervalTimesResponse intervalTimes) {
        // Update the availability entity with the computed interval times.
        availability.setShiftStartDateTime(intervalTimes.getFirstCreatedDateTime());
        availability.setIntervalEndDateTime(intervalTimes.getLastCreatedDateTime());

        // Check if the availability is for a specific resource or for a work center category.
        // (Assuming that if resourceId is null or empty, then it is a category.)
        if (availability.getResourceId() == null || availability.getResourceId().isEmpty()) {
            // This is a work center category: fetch all resources within the work center.
            WorkCenterRequest workCenterRequest = new WorkCenterRequest();
            workCenterRequest.setSite(availability.getSite());
            workCenterRequest.setWorkCenter(availability.getWorkcenterId());
            List<String> resources = fetchLineResources(workCenterRequest);

            // Initialize sums for the aggregated values.
            double totalPlannedProductionTime = Double.MAX_VALUE;
            double totalBreakTime = 0;
            double totalDowntime = 0;
            double totalActualTime = Double.MAX_VALUE;
            double totalActualProductionTime = Double.MAX_VALUE;

            // For each resource, clone the availability entity, set its resourceId, and calculate availability.
            for (String resource : resources) {
                AvailabilityEntity resourceAvailability = copyAvailability(availability);
                resourceAvailability.setResourceId(resource);
                AggregatedAvailabilityResult result = calculateSingleResourceAvailability(resourceAvailability);

                /*totalPlannedProductionTime += result.getPlannedProductionTime();
                totalBreakTime += result.getBreakTime();
                totalDowntime += result.getTotalDowntime();
                totalActualTime += result.getActualTime();
                totalActualProductionTime += result.getActualProductionTime();*/
if(isLiveEnabled){
    totalPlannedProductionTime +=result.getPlannedProductionTime();
    totalBreakTime += result.getBreakTime();
    totalDowntime += result.getTotalDowntime();
    totalActualTime +=result.getActualTime();
    totalActualProductionTime +=result.getActualProductionTime();
}
              else {
    totalPlannedProductionTime = Math.min(totalPlannedProductionTime, result.getPlannedProductionTime());
    totalBreakTime = Math.max(totalBreakTime, result.getBreakTime());
    totalDowntime = Math.max(totalDowntime, result.getTotalDowntime());
    totalActualTime = Math.min(totalActualTime, result.getActualTime());
    totalActualProductionTime = Math.min(totalActualProductionTime, result.getActualProductionTime());
}
            }

            // Compute the overall availability percentage using the total planned and actual times.
            double overallAvailabilityPercentage = totalPlannedProductionTime > 0
                    ? (totalActualTime / totalPlannedProductionTime) * 100
                    : 0;

            return new AggregatedAvailabilityResult(
                    totalPlannedProductionTime,
                    totalBreakTime,
                    totalDowntime,
                    totalActualTime,
                    totalActualProductionTime,
                    overallAvailabilityPercentage
            );
        } else {
            // Single resource: use the standard calculation.
            return calculateSingleResourceAvailability(availability);
        }
    }

    /**
     * Calculates the availability for a single resource.
     */
    private AggregatedAvailabilityResult calculateSingleResourceAvailability(AvailabilityEntity availability) {

        DowntimeRequest dt = new DowntimeRequest();
        dt.setResourceId(availability.getResourceId());
        dt.setSite(availability.getSite());
        dt.setStartDateTime(availability.getShiftStartDateTime());
        dt.setEndDateTime(availability.getIntervalEndDateTime());

        long plannedDowntime= downtimeService.getPlannedBreakHoursBetweenTime(dt);

        // Fetch shift details using the provided availability information.
        List<ShiftOutput> shifts = fetchShiftDetails(availability);
        ShiftOutput shiftOutput = (shifts != null && !shifts.isEmpty()) ? shifts.get(0) : null;

        // Determine planned production time and break time using shift details if available.
        double plannedProductionTime = shiftOutput != null
                ? shiftOutput.getPlannedOperatingTime()
                : availability.getPlannedOperatingTime();
        double breakTime = shiftOutput != null
                ? shiftOutput.getBreaktime()
                : 0;

        double plannedOperatingTime = Math.max(0, plannedProductionTime - plannedDowntime);

        // Compute downtime using your downtime service.
        double downtime = calculateDowntime(availability);
        // Cap downtime to plannedOperatingTime to avoid negative actual time.
        if (downtime > plannedOperatingTime) {
            downtime = plannedOperatingTime;
        }
        // Compute the actual production time as planned production minus downtime.
        double actualTime = plannedOperatingTime - downtime;
        // Compute the availability percentage.
        double availabilityPercentage = plannedOperatingTime > 0 ? (actualTime / plannedOperatingTime) * 100 : 0;

        return new AggregatedAvailabilityResult(
                plannedOperatingTime,
                plannedDowntime,
                downtime,
                actualTime,        // actualTime
                actualTime,        // actualProductionTime (if the same as actualTime)
                availabilityPercentage
        );
    }

    /**
     * Creates a copy of the given AvailabilityEntity.
     * (This is a simple example – adjust as needed for your class.)
     */
    private AvailabilityEntity copyAvailability(AvailabilityEntity original) {
        AvailabilityEntity copy = new AvailabilityEntity();
        copy.setSite(original.getSite());
        copy.setResourceId(original.getResourceId());
        copy.setWorkcenterId(original.getWorkcenterId());
        copy.setShiftStartDateTime(original.getShiftStartDateTime());
        copy.setIntervalEndDateTime(original.getIntervalEndDateTime());
        copy.setPlannedOperatingTime(original.getPlannedOperatingTime());
        // Copy any other necessary fields...
        return copy;
    }

    private List<String> fetchLineResources(WorkCenterRequest workCenterRequest) {
        WorkCenter workCenter = webClientBuilder.build()
                .post()
                .uri(retrieveWorkcenter)
                .bodyValue(workCenterRequest)
                .retrieve()
                .bodyToMono(WorkCenter.class)
                .block();

        List<String> resources = new ArrayList<>();
        for (Association association : workCenter.getAssociationList()) {
            if ("Work Center".equalsIgnoreCase(association.getType())) {
                WorkCenterRequest nestedRequest = new WorkCenterRequest();
                nestedRequest.setSite(workCenterRequest.getSite());
                nestedRequest.setWorkCenter(association.getAssociateId());
                resources.addAll(fetchLineResources(nestedRequest));
            } else if ("Resource".equalsIgnoreCase(association.getType())) {
                resources.add(association.getAssociateId());
            }
        }

        return resources;
    }

    private String getCellForWorkcenter(String workcenterId, String site) {
        WorkCenterRequest req = new WorkCenterRequest();
        req.setSite(site);
        req.setWorkCenter(workcenterId);
        // Call the dedicated endpoint for getting the cell.
        String  cellResponse = webClientBuilder.build()
                .post()
                .uri(getCell)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (cellResponse == null) {
            log.warn("No cell found for workcenterId: {}", workcenterId);
            return null;
        }
        // Assuming the returned WorkCenter object’s 'workCenter' field represents the parent cell.
        return cellResponse;
    }

    private String getCellGroupForCell(String cell, String site) {
        WorkCenterRequest req = new WorkCenterRequest();
        req.setSite(site);
        req.setWorkCenter(cell);
        // Call the dedicated endpoint for getting the cell group.
        String cellGroupResponse = webClientBuilder.build()
                .post()
                .uri(getCellGroup)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (cellGroupResponse == null) {
            log.warn("No cell group found for cell: {}", cell);
            return null;
        }
        // Assuming the returned WorkCenter object’s 'workCenter' field represents the cell group.
        return cellGroupResponse;
    }

    private void aggregateByCellShift(String site, String cell, String shiftId, LocalDate day, int month, int year, String eventSource) {
        // Build a request object for the remote call.
        WorkCenterRequest workCenterRequest = new WorkCenterRequest();
        workCenterRequest.setSite(site);
        workCenterRequest.setWorkCenter(cell);

        // Use WebClient to retrieve the cell record from the workcenter-service.
        WorkCenter cellRecord = webClientBuilder.build()
                .post()
                .uri(retrieveWorkcenter)
                .bodyValue(workCenterRequest)
                .retrieve()
                .bodyToMono(WorkCenter.class)
                .block();

        if (cellRecord == null) {
            log.warn("Unable to retrieve cell record for cell '{}' and site '{}'", cell, site);
            return;
        }

        // From the cell record, get all lower-level (line) workcenter IDs from the association list.
        List<String> lineWorkcenters = cellRecord.getAssociationList().stream()
                .filter(assoc -> "Work Center".equalsIgnoreCase(assoc.getType()))
                .map(assoc -> assoc.getAssociateId())
                .collect(Collectors.toList());

        if (lineWorkcenters == null || lineWorkcenters.isEmpty()) {
            log.warn("No line-level workcenters found for cell: {}", cell);
            return;
        }

        // Retrieve aggregated OEE records (from the AggregatedOeeRepository) for the given site, day, and event source,
        // for each line workcenter belonging to this cell.
        List<AggregatedOee> lineOeeRecords = aggregatedOeeRepository.findForCellShiftAggregation(site,shiftId, lineWorkcenters, day, eventSource);

        if (lineOeeRecords == null || lineOeeRecords.isEmpty()) {
            log.warn("No Aggregated OEE records found for line workcenters in cell: {}", cell);
            return;
        }

        // Sum up the various fields from the lower-level (line) OEE records.
        double totalActualProductionTime = lineOeeRecords.stream().mapToDouble(oee -> oee.getActualProductionTime()).sum();
        double totalBreakTime = lineOeeRecords.stream().mapToDouble(oee -> oee.getBreakTime()).sum();
        double totalPlannedProductionTime = lineOeeRecords.stream().mapToDouble(oee -> oee.getPlannedProductionTime()).sum();
        double totalActualTime = lineOeeRecords.stream().mapToDouble(oee -> oee.getActualTime()).sum();
        double totalDowntime = lineOeeRecords.stream().mapToDouble(oee -> oee.getTotalDowntime()).sum();
        double totalCycleTime = lineOeeRecords.stream().mapToDouble(oee -> oee.getCycleTime()).max().orElse(0.0);
        double totalPlannedQuantity = lineOeeRecords.stream().mapToDouble(oee -> oee.getPlannedQuantity()).sum();
        double totalGoodQuantity = lineOeeRecords.stream().mapToDouble(oee -> oee.getTotalGoodQuantity()).sum();
        double totalBadQuantity = lineOeeRecords.stream().mapToDouble(oee -> oee.getTotalBadQuantity()).sum();
        double totalQuantity = lineOeeRecords.stream().mapToDouble(oee -> oee.getTotalQuantity()).sum();
        double totalTargetQuantity = lineOeeRecords.stream().mapToDouble(oee -> oee.getTargetQuantity()).sum();


        double availability = totalPlannedProductionTime > 0 ? (totalActualTime / totalPlannedProductionTime) * 100 : 0;
        double performance = totalPlannedQuantity > 0 ? (totalQuantity / totalPlannedQuantity) * 100 : 0;
        double quality = totalQuantity > 0 ? (totalGoodQuantity / totalQuantity) * 100 : 0;
        double oee = (availability * performance * quality) / 10000;
        double actualCycleTime = totalQuantity > 0 ? totalActualProductionTime / totalQuantity : 0;

        // Create a new AggregatedTimePeriod record for CELL_SHIFT.
        AggregatedTimePeriod cellShiftAggregate = AggregatedTimePeriod.builder()
                .site(site)
                .workcenterId(cell)  // Use the cell id for this aggregated record.
                .shiftId(shiftId)
                .logDate(day)
                .category("CELL_SHIFT")
                .day(day.getDayOfMonth())
                .month(month)
                .year(year)
                .actualProductionTime(totalActualProductionTime)
                .breakTime(totalBreakTime)
                .plannedProductionTime(totalPlannedProductionTime)
                .actualTime(totalActualTime)
                .totalDowntime(totalDowntime)
                .cycleTime(totalCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(totalPlannedQuantity)
                .totalGoodQuantity(totalGoodQuantity)
                .totalBadQuantity(totalBadQuantity)
                .totalQuantity(totalQuantity)
                .availability(availability)
                .performance(performance)
                .quality(quality)
                .oee(oee)
                .eventSource(eventSource)
                .targetQuantity(totalTargetQuantity)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();

        // Deactivate any previously existing CELL_SHIFT record for this cell for the same day.
        aggregatedTimePeriodRepository.deactivateCellShiftRecord(site, cell, day, day.getDayOfMonth(), month, year,shiftId,eventSource);
        aggregatedTimePeriodRepository.save(cellShiftAggregate);
    }

    /**
     * Aggregates a Cell-level DAY record from all CELL_SHIFT records for the given cell and day.
     */
    private void aggregateByCellDay(String site, String cell, LocalDate day, int month, int year, String eventSource) {
        // Query CELL_SHIFT records for this cell on the given day.
        List<AggregatedTimePeriod> cellShiftRecords = aggregatedTimePeriodRepository.findForCellShiftDayAggregation(site, cell, day, eventSource);
        if (cellShiftRecords == null || cellShiftRecords.isEmpty()) {
            log.warn("No CELL_SHIFT records available for cell {} on {}", cell, day);
            return;
        }
        double sumActualProductionTime = cellShiftRecords.stream().mapToDouble(r -> r.getActualProductionTime()).sum();
        double sumBreakTime = cellShiftRecords.stream().mapToDouble(r -> r.getBreakTime()).sum();
        double sumPlannedProductionTime = cellShiftRecords.stream().mapToDouble(r -> r.getPlannedProductionTime()).sum();
        double sumActualTime = cellShiftRecords.stream().mapToDouble(r -> r.getActualTime()).sum();
        double sumTotalDowntime = cellShiftRecords.stream().mapToDouble(r -> r.getTotalDowntime()).sum();
        double sumCycleTime = cellShiftRecords.stream().mapToDouble(r -> r.getCycleTime()).max().orElse(0.0);
        double sumPlannedQuantity = cellShiftRecords.stream().mapToDouble(r -> r.getPlannedQuantity()).sum();
        double sumGoodQuantity = cellShiftRecords.stream().mapToDouble(r -> r.getTotalGoodQuantity()).sum();
        double sumBadQuantity = cellShiftRecords.stream().mapToDouble(r -> r.getTotalBadQuantity()).sum();
        double sumTotalQuantity = cellShiftRecords.stream().mapToDouble(r -> r.getTotalQuantity()).sum();
        double sumTotalTargetQuantity = cellShiftRecords.stream().mapToDouble(r -> r.getTargetQuantity()).sum();

        double availability = sumPlannedProductionTime > 0 ? (sumActualTime / sumPlannedProductionTime) * 100 : 0;
        double performance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double quality = sumTotalQuantity > 0 ? (sumGoodQuantity / sumTotalQuantity) * 100 : 0;
        double oee = (availability * performance * quality) / 10000;
        double actualCycleTime = sumTotalQuantity > 0 ? sumActualProductionTime / sumTotalQuantity : 0;

        AggregatedTimePeriod cellDay = AggregatedTimePeriod.builder()
                .site(site)
                .workcenterId(cell)
                .logDate(day)
                .category("CELL_DAY")
                .day(day.getDayOfMonth())
                .month(month)
                .year(year)
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumGoodQuantity)
                .totalBadQuantity(sumBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(availability)
                .performance(performance)
                .quality(quality)
                .oee(oee)
                .eventSource(eventSource)
                .targetQuantity(sumTotalTargetQuantity)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();

        aggregatedTimePeriodRepository.deactivateCellDayRecord(site, cell, day, day.getDayOfMonth(), month, year, eventSource);
        aggregatedTimePeriodRepository.save(cellDay);
    }

    /**
     * Aggregates a Cell-level MONTH record from all CELL_DAY records for the given cell and month.
     */
    private void aggregateByCellMonth(String site, String cell, LocalDate day, int year, int month, String eventSource) {
        List<AggregatedTimePeriod> cellDayRecords = aggregatedTimePeriodRepository.findForCellMonthAggregation(site, cell, year, month, eventSource);
        if (cellDayRecords == null || cellDayRecords.isEmpty()) {
            log.warn("No CELL_DAY records for cell {} in {}/{}", cell, month, year);
            return;
        }
        double sumActualProductionTime = cellDayRecords.stream().mapToDouble(r -> r.getActualProductionTime()).sum();
        double sumBreakTime = cellDayRecords.stream().mapToDouble(r -> r.getBreakTime()).sum();
        double sumPlannedProductionTime = cellDayRecords.stream().mapToDouble(r -> r.getPlannedProductionTime()).sum();
        double sumActualTime = cellDayRecords.stream().mapToDouble(r -> r.getActualTime()).sum();
        double sumTotalDowntime = cellDayRecords.stream().mapToDouble(r -> r.getTotalDowntime()).sum();
        double sumCycleTime = cellDayRecords.stream().mapToDouble(r -> r.getCycleTime()).max().orElse(0.0);
        double sumPlannedQuantity = cellDayRecords.stream().mapToDouble(r -> r.getPlannedQuantity()).sum();
        double sumGoodQuantity = cellDayRecords.stream().mapToDouble(r -> r.getTotalGoodQuantity()).sum();
        double sumBadQuantity = cellDayRecords.stream().mapToDouble(r -> r.getTotalBadQuantity()).sum();
        double sumTotalQuantity = cellDayRecords.stream().mapToDouble(r -> r.getTotalQuantity()).sum();
        double sumTotalTargetQuantity = cellDayRecords.stream().mapToDouble(r -> r.getTargetQuantity()).sum();

        double availability = sumPlannedProductionTime > 0 ? (sumActualTime / sumPlannedProductionTime) * 100 : 0;
        double performance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double quality = sumTotalQuantity > 0 ? (sumGoodQuantity / sumTotalQuantity) * 100 : 0;
        double oee = (availability * performance * quality) / 10000;
        double actualCycleTime = sumTotalQuantity > 0 ? sumActualProductionTime / sumTotalQuantity : 0;

        AggregatedTimePeriod cellMonth = AggregatedTimePeriod.builder()
                .site(site)
                .workcenterId(cell)
                .logDate(day) // Or set to the first day of the month as needed
                .category("CELL_MONTH")
                .day(day.getDayOfMonth())
                .month(month)
                .year(year)
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumGoodQuantity)
                .totalBadQuantity(sumBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(availability)
                .performance(performance)
                .quality(quality)
                .oee(oee)
                .eventSource(eventSource)
                .targetQuantity(sumTotalTargetQuantity)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();

        aggregatedTimePeriodRepository.deactivateCellMonthRecord(site, cell, month, year, eventSource);
        aggregatedTimePeriodRepository.save(cellMonth);
    }

    /**
     * Aggregates a Cell-level YEAR record from all CELL_MONTH records for the given cell and year.
     */
    private void aggregateByCellYear(String site, String cell, LocalDate day, int year, int month, String eventSource) {
        List<AggregatedTimePeriod> cellMonthRecords = aggregatedTimePeriodRepository.findForCellYearAggregation(site, cell, year, eventSource);
        if (cellMonthRecords == null || cellMonthRecords.isEmpty()) {
            log.warn("No CELL_MONTH records for cell {} in {}", cell, year);
            return;
        }
        double sumActualProductionTime = cellMonthRecords.stream().mapToDouble(r -> r.getActualProductionTime()).sum();
        double sumBreakTime = cellMonthRecords.stream().mapToDouble(r -> r.getBreakTime()).sum();
        double sumPlannedProductionTime = cellMonthRecords.stream().mapToDouble(r -> r.getPlannedProductionTime()).sum();
        double sumActualTime = cellMonthRecords.stream().mapToDouble(r -> r.getActualTime()).sum();
        double sumTotalDowntime = cellMonthRecords.stream().mapToDouble(r -> r.getTotalDowntime()).sum();
        double sumCycleTime = cellMonthRecords.stream().mapToDouble(r -> r.getCycleTime()).max().orElse(0.0);
        double sumPlannedQuantity = cellMonthRecords.stream().mapToDouble(r -> r.getPlannedQuantity()).sum();
        double sumGoodQuantity = cellMonthRecords.stream().mapToDouble(r -> r.getTotalGoodQuantity()).sum();
        double sumBadQuantity = cellMonthRecords.stream().mapToDouble(r -> r.getTotalBadQuantity()).sum();
        double sumTotalQuantity = cellMonthRecords.stream().mapToDouble(r -> r.getTotalQuantity()).sum();
        double sumTotalTargetQuantity = cellMonthRecords.stream().mapToDouble(r -> r.getTargetQuantity()).sum();

        double availability = sumPlannedProductionTime > 0 ? (sumActualTime / sumPlannedProductionTime) * 100 : 0;
        double performance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double quality = sumTotalQuantity > 0 ? (sumGoodQuantity / sumTotalQuantity) * 100 : 0;
        double oee = (availability * performance * quality) / 10000;
        double actualCycleTime = sumTotalQuantity > 0 ? sumActualProductionTime / sumTotalQuantity : 0;

        AggregatedTimePeriod cellYear = AggregatedTimePeriod.builder()
                .site(site)
                .workcenterId(cell)
                .logDate(day) // or an appropriate date for the year aggregate
                .category("CELL_YEAR")
                .day(day.getDayOfMonth())
                .month(month)
                .year(year)
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumGoodQuantity)
                .totalBadQuantity(sumBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(availability)
                .performance(performance)
                .quality(quality)
                .oee(oee)
                .eventSource(eventSource)
                .targetQuantity(sumTotalTargetQuantity)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();

        aggregatedTimePeriodRepository.deactivateCellYearRecords(site, year,cell, eventSource);
        aggregatedTimePeriodRepository.save(cellYear);
    }

    // === Cell Group Aggregation Methods ===

    /**
     * Aggregates a Cell Group SHIFT record.
     * For the given cell group, first retrieve all the cells (using workcenterService),
     * then query all CELL_SHIFT records for those cells, and then aggregate.
     */
    private void aggregateByCellGroupShift(String site, String cellGroup, String shiftId, LocalDate day, int month, int year, String eventSource) {
        // Retrieve all cell IDs associated with this cell group.
        WorkCenterRequest request = new WorkCenterRequest();
        request.setSite(site);
        request.setWorkCenter(cellGroup); // cellGroup is used as the input workCenter ID
        WorkCenter cellGroupRecord = webClientBuilder.build()
                .post()
                .uri(retrieveWorkcenter) // Configured endpoint for cell group lookup
                .bodyValue(request)
                .retrieve()
                .bodyToMono(WorkCenter.class)
                .block();

        if (cellGroupRecord == null) {
            log.warn("No cell group record found for cell group '{}' and site '{}'", cellGroup, site);
            return;
        }
        // From the cell group record, extract lower‑level cell IDs from associations with type "Work Center".
        List<String> cellIds = cellGroupRecord.getAssociationList().stream()
                .filter(assoc -> "Work Center".equalsIgnoreCase(assoc.getType()))
                .map(assoc -> assoc.getAssociateId())
                .collect(Collectors.toList());
        if (cellIds == null || cellIds.isEmpty()) {
            log.warn("No cells found for cell group: {}", cellGroup);
            return;
        }
        // Query existing CELL_SHIFT records for these cells.
        // (These CELL_SHIFT records are assumed to have been written into the AggregatedTimePeriod table.)
        List<AggregatedTimePeriod> cellShiftRecords = aggregatedTimePeriodRepository.findForCellGroupShiftAggregation(site,shiftId, day, cellIds, eventSource);
        if (cellShiftRecords == null || cellShiftRecords.isEmpty()) {
            log.warn("No CELL_SHIFT records found for cells in cell group: {}", cellGroup);
            return;
        }
        // Sum up the aggregated fields from the CELL_SHIFT records.
        double totalActualProductionTime = cellShiftRecords.stream().mapToDouble(r -> r.getActualProductionTime()).sum();
        double totalBreakTime = cellShiftRecords.stream().mapToDouble(r -> r.getBreakTime()).sum();
        double totalPlannedProductionTime = cellShiftRecords.stream().mapToDouble(r -> r.getPlannedProductionTime()).sum();
        double totalActualTime = cellShiftRecords.stream().mapToDouble(r -> r.getActualTime()).sum();
        double totalDowntime = cellShiftRecords.stream().mapToDouble(r -> r.getTotalDowntime()).sum();
        double totalCycleTime = cellShiftRecords.stream().mapToDouble(r -> r.getCycleTime()).max().orElse(0.0);
        double totalPlannedQuantity = cellShiftRecords.stream().mapToDouble(r -> r.getPlannedQuantity()).sum();
        double totalGoodQuantity = cellShiftRecords.stream().mapToDouble(r -> r.getTotalGoodQuantity()).sum();
        double totalBadQuantity = cellShiftRecords.stream().mapToDouble(r -> r.getTotalBadQuantity()).sum();
        double totalQuantity = cellShiftRecords.stream().mapToDouble(r -> r.getTotalQuantity()).sum();
        double sumTotalTargetQuantity = cellShiftRecords.stream().mapToDouble(r -> r.getTargetQuantity()).sum();


        double availability = totalPlannedProductionTime > 0 ? (totalActualTime / totalPlannedProductionTime) * 100 : 0;
        double performance = totalPlannedQuantity > 0 ? (totalQuantity / totalPlannedQuantity) * 100 : 0;
        double quality = totalQuantity > 0 ? (totalGoodQuantity / totalQuantity) * 100 : 0;
        double oee = (availability * performance * quality) / 10000;
        double actualCycleTime = totalQuantity > 0 ? totalActualProductionTime / totalQuantity : 0;

        // Build new aggregated record in the timeperiod table for CELL_GROUP_SHIFT.
        AggregatedTimePeriod cellGroupShift = AggregatedTimePeriod.builder()
                .site(site)
                .workcenterId(cellGroup)   // Use the cell group identifier.
                .shiftId(shiftId)
                .logDate(day)
                .category("CELL_GROUP_SHIFT")
                .day(day.getDayOfMonth())
                .month(month)
                .year(year)
                .actualProductionTime(totalActualProductionTime)
                .breakTime(totalBreakTime)
                .plannedProductionTime(totalPlannedProductionTime)
                .actualTime(totalActualTime)
                .totalDowntime(totalDowntime)
                .cycleTime(totalCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(totalPlannedQuantity)
                .totalGoodQuantity(totalGoodQuantity)
                .totalBadQuantity(totalBadQuantity)
                .totalQuantity(totalQuantity)
                .availability(availability)
                .performance(performance)
                .quality(quality)
                .oee(oee)
                .eventSource(eventSource)
                .targetQuantity(sumTotalTargetQuantity)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();

        // Deactivate any previously existing CELL_GROUP_SHIFT record for this cell group on the same day.
        aggregatedTimePeriodRepository.deactivateCellGroupShiftRecord(site, cellGroup, day, day.getDayOfMonth(), month, year, shiftId, eventSource);
        aggregatedTimePeriodRepository.save(cellGroupShift);
    }


    /**
     * Aggregates a Cell Group DAY record from CELL_GROUP_SHIFT records.
     */
    private void aggregateByCellGroupDay(String site, String cellGroup, LocalDate day, int month, int year, String eventSource) {
        List<AggregatedTimePeriod> cellGroupShiftRecords = aggregatedTimePeriodRepository.findForCellGroupDayAggregation(site, cellGroup, day, eventSource);
        if (cellGroupShiftRecords == null || cellGroupShiftRecords.isEmpty()) {
            return;
        }
        double sumActualProductionTime = cellGroupShiftRecords.stream().mapToDouble(r -> r.getActualProductionTime()).sum();
        double sumBreakTime = cellGroupShiftRecords.stream().mapToDouble(r -> r.getBreakTime()).sum();
        double sumPlannedProductionTime = cellGroupShiftRecords.stream().mapToDouble(r -> r.getPlannedProductionTime()).sum();
        double sumActualTime = cellGroupShiftRecords.stream().mapToDouble(r -> r.getActualTime()).sum();
        double sumTotalDowntime = cellGroupShiftRecords.stream().mapToDouble(r -> r.getTotalDowntime()).sum();
        double sumCycleTime = cellGroupShiftRecords.stream().mapToDouble(r -> r.getCycleTime()).max().orElse(0.0);
        double sumPlannedQuantity = cellGroupShiftRecords.stream().mapToDouble(r -> r.getPlannedQuantity()).sum();
        double sumGoodQuantity = cellGroupShiftRecords.stream().mapToDouble(r -> r.getTotalGoodQuantity()).sum();
        double sumBadQuantity = cellGroupShiftRecords.stream().mapToDouble(r -> r.getTotalBadQuantity()).sum();
        double sumTotalQuantity = cellGroupShiftRecords.stream().mapToDouble(r -> r.getTotalQuantity()).sum();
        double sumTotalTargetQuantity = cellGroupShiftRecords.stream().mapToDouble(r -> r.getTargetQuantity()).sum();


        double availability = sumPlannedProductionTime > 0 ? (sumActualTime / sumPlannedProductionTime) * 100 : 0;
        double performance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double quality = sumTotalQuantity > 0 ? (sumGoodQuantity / sumTotalQuantity) * 100 : 0;
        double oee = (availability * performance * quality) / 10000;
        double actualCycleTime = sumTotalQuantity > 0 ? sumActualProductionTime / sumTotalQuantity : 0;

        AggregatedTimePeriod cellGroupDay = AggregatedTimePeriod.builder()
                .site(site)
                .workcenterId(cellGroup)
                .logDate(day)
                .category("CELL_GROUP_DAY")
                .day(day.getDayOfMonth())
                .month(month)
                .year(year)
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumGoodQuantity)
                .totalBadQuantity(sumBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(availability)
                .performance(performance)
                .quality(quality)
                .oee(oee)
                .eventSource(eventSource)
                .targetQuantity(sumTotalTargetQuantity)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();

        aggregatedTimePeriodRepository.deactivateCellGroupDayRecords(site,  day,cellGroup, day.getDayOfMonth(), month, year, eventSource);
        aggregatedTimePeriodRepository.save(cellGroupDay);
    }

    /**
     * Aggregates a Cell Group MONTH record from CELL_GROUP_DAY records.
     */
    private void aggregateByCellGroupMonth(String site, String cellGroup, LocalDate day, int year, int month, String eventSource) {
        List<AggregatedTimePeriod> cellGroupDayRecords = aggregatedTimePeriodRepository.findForCellGroupMonthAggregation(site, cellGroup, year, month, eventSource);
        if (cellGroupDayRecords == null || cellGroupDayRecords.isEmpty()) {
            return;
        }
        double sumActualProductionTime = cellGroupDayRecords.stream().mapToDouble(r -> r.getActualProductionTime()).sum();
        double sumBreakTime = cellGroupDayRecords.stream().mapToDouble(r -> r.getBreakTime()).sum();
        double sumPlannedProductionTime = cellGroupDayRecords.stream().mapToDouble(r -> r.getPlannedProductionTime()).sum();
        double sumActualTime = cellGroupDayRecords.stream().mapToDouble(r -> r.getActualTime()).sum();
        double sumTotalDowntime = cellGroupDayRecords.stream().mapToDouble(r -> r.getTotalDowntime()).sum();
        double sumCycleTime = cellGroupDayRecords.stream().mapToDouble(r -> r.getCycleTime()).max().orElse(0.0);
        double sumPlannedQuantity = cellGroupDayRecords.stream().mapToDouble(r -> r.getPlannedQuantity()).sum();
        double sumGoodQuantity = cellGroupDayRecords.stream().mapToDouble(r -> r.getTotalGoodQuantity()).sum();
        double sumBadQuantity = cellGroupDayRecords.stream().mapToDouble(r -> r.getTotalBadQuantity()).sum();
        double sumTotalQuantity = cellGroupDayRecords.stream().mapToDouble(r -> r.getTotalQuantity()).sum();
        double sumTotalTargetQuantity = cellGroupDayRecords.stream().mapToDouble(r -> r.getTargetQuantity()).sum();


        double availability = sumPlannedProductionTime > 0 ? (sumActualTime / sumPlannedProductionTime) * 100 : 0;
        double performance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double quality = sumTotalQuantity > 0 ? (sumGoodQuantity / sumTotalQuantity) * 100 : 0;
        double oee = (availability * performance * quality) / 10000;
        double actualCycleTime = sumTotalQuantity > 0 ? sumActualProductionTime / sumTotalQuantity : 0;

        AggregatedTimePeriod cellGroupMonth = AggregatedTimePeriod.builder()
                .site(site)
                .workcenterId(cellGroup)
                .logDate(day)
                .category("CELL_GROUP_MONTH")
                .day(day.getDayOfMonth())
                .month(month)
                .year(year)
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumGoodQuantity)
                .totalBadQuantity(sumBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(availability)
                .performance(performance)
                .quality(quality)
                .oee(oee)
                .eventSource(eventSource)
                .targetQuantity(sumTotalTargetQuantity)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();

        aggregatedTimePeriodRepository.deactivateCellGroupMonthRecords(site,  month, year,cellGroup, eventSource);
        aggregatedTimePeriodRepository.save(cellGroupMonth);
    }

    /**
     * Aggregates a Cell Group YEAR record from CELL_GROUP_MONTH records.
     */
    private void aggregateByCellGroupYear(String site, String cellGroup, LocalDate day, int year, int month, String eventSource) {
        List<AggregatedTimePeriod> cellGroupMonthRecords = aggregatedTimePeriodRepository.findForCellGroupYearAggregation(site, cellGroup, year, eventSource);
        if (cellGroupMonthRecords == null || cellGroupMonthRecords.isEmpty()) {
            return;
        }
        double sumActualProductionTime = cellGroupMonthRecords.stream().mapToDouble(r -> r.getActualProductionTime()).sum();
        double sumBreakTime = cellGroupMonthRecords.stream().mapToDouble(r -> r.getBreakTime()).sum();
        double sumPlannedProductionTime = cellGroupMonthRecords.stream().mapToDouble(r -> r.getPlannedProductionTime()).sum();
        double sumActualTime = cellGroupMonthRecords.stream().mapToDouble(r -> r.getActualTime()).sum();
        double sumTotalDowntime = cellGroupMonthRecords.stream().mapToDouble(r -> r.getTotalDowntime()).sum();
        double sumCycleTime = cellGroupMonthRecords.stream().mapToDouble(r -> r.getCycleTime()).max().orElse(0.0);
        double sumPlannedQuantity = cellGroupMonthRecords.stream().mapToDouble(r -> r.getPlannedQuantity()).sum();
        double sumGoodQuantity = cellGroupMonthRecords.stream().mapToDouble(r -> r.getTotalGoodQuantity()).sum();
        double sumBadQuantity = cellGroupMonthRecords.stream().mapToDouble(r -> r.getTotalBadQuantity()).sum();
        double sumTotalQuantity = cellGroupMonthRecords.stream().mapToDouble(r -> r.getTotalQuantity()).sum();
        double sumTotalTargetQuantity = cellGroupMonthRecords.stream().mapToDouble(r -> r.getTargetQuantity()).sum();

        double availability = sumPlannedProductionTime > 0 ? (sumActualTime / sumPlannedProductionTime) * 100 : 0;
        double performance = sumPlannedQuantity > 0 ? (sumTotalQuantity / sumPlannedQuantity) * 100 : 0;
        double quality = sumTotalQuantity > 0 ? (sumGoodQuantity / sumTotalQuantity) * 100 : 0;
        double oee = (availability * performance * quality) / 10000;
        double actualCycleTime = sumTotalQuantity > 0 ? sumActualProductionTime / sumTotalQuantity : 0;

        AggregatedTimePeriod cellGroupYear = AggregatedTimePeriod.builder()
                .site(site)
                .workcenterId(cellGroup)
                .logDate(day)
                .category("CELL_GROUP_YEAR")
                .day(day.getDayOfMonth())
                .month(month)
                .year(year)
                .actualProductionTime(sumActualProductionTime)
                .breakTime(sumBreakTime)
                .plannedProductionTime(sumPlannedProductionTime)
                .actualTime(sumActualTime)
                .totalDowntime(sumTotalDowntime)
                .cycleTime(sumCycleTime)
                .actualCycleTime(actualCycleTime)
                .plannedQuantity(sumPlannedQuantity)
                .totalGoodQuantity(sumGoodQuantity)
                .totalBadQuantity(sumBadQuantity)
                .totalQuantity(sumTotalQuantity)
                .availability(availability)
                .performance(performance)
                .quality(quality)
                .oee(oee)
                .eventSource(eventSource)
                .targetQuantity(sumTotalTargetQuantity)
                .createdDatetime(LocalDateTime.now())
                .active(true)
                .build();

        aggregatedTimePeriodRepository.deactivateCellGroupYearRecords(site, year,cellGroup, eventSource);
        aggregatedTimePeriodRepository.save(cellGroupYear);
    }

    public void runInsightsIfEnabled(boolean runInsights, AggregatedTimePeriodInput input) {
        if (isInsightsEnabled && runInsights) {
            try {
                LocalDate insightDate = input.getAvailabilityDate() != null
                        ? input.getAvailabilityDate()
                        : LocalDate.now();

                entityManager
                        .createNativeQuery("CALL generate_actionable_insights(:insightDate)")
                        .setParameter("insightDate", java.sql.Date.valueOf(insightDate))
                        .executeUpdate();

                log.info("✅ Actionable insights procedure executed successfully for date: {}", insightDate);
            } catch (Exception ex) {
                log.warn("⚠️ Failed to execute actionable insights procedure: {}", ex.getMessage());
            }
        }
    }

    /*private AggregatedAvailabilityResult calculateAggregatedAvailability(AvailabilityEntity availability,
                                                                         IntervalTimesResponse intervalTimes) {
        // Update the availability entity to use the computed interval times.
        availability.setShiftStartDateTime(intervalTimes.getFirstCreatedDateTime());
        availability.setIntervalEndDateTime(intervalTimes.getLastCreatedDateTime());

        // Fetch shift details using the updated availability.
        List<ShiftOutput> shifts = fetchShiftDetails(availability);
        // For simplicity, pick the first returned shift detail.
        ShiftOutput shiftOutput = (shifts != null && !shifts.isEmpty()) ? shifts.get(0) : null;

        // Use shift details if available; otherwise, fall back to the availability entity.
        double plannedProductionTime = shiftOutput != null
                ? shiftOutput.getPlannedOperatingTime()
                : availability.getPlannedOperatingTime();
        double breakTime = shiftOutput != null
                ? shiftOutput.getBreaktime()
                : 0;
        // Use the downtimeService to get downtime for the given interval.
        double downtime = calculateDowntime(availability);
        // Compute actual production time as planned time minus break time and downtime.
        double actualTime = plannedProductionTime  - downtime;
        // Compute availability percentage
        double availabilityPercentage = plannedProductionTime > 0 ? (actualTime / plannedProductionTime) * 100 : 0;

        // Return all the computed values in a helper POJO.
        return new AggregatedAvailabilityResult(
                plannedProductionTime,
                breakTime,
                downtime,
                actualTime,        // actualTime
                actualTime,        // actualProductionTime (could be the same as actualTime)
                availabilityPercentage
        );
    }*/
    // ------------------ Helper POJO ------------------
    private static class AggregatedAvailabilityResult {
        private final double plannedProductionTime;
        private final double breakTime;
        private final double totalDowntime;
        private final double actualTime;
        private final double actualProductionTime;
        private final double availability;

        public AggregatedAvailabilityResult(double plannedProductionTime, double breakTime, double totalDowntime,
                                            double actualTime, double actualProductionTime, double availability) {
            this.plannedProductionTime = plannedProductionTime;
            this.breakTime = breakTime;
            this.totalDowntime = totalDowntime;
            this.actualTime = actualTime;
            this.actualProductionTime = actualProductionTime;
            this.availability = availability;
        }

        public double getPlannedProductionTime() {
            return plannedProductionTime;
        }

        public double getBreakTime() {
            return breakTime;
        }

        public double getTotalDowntime() {
            return totalDowntime;
        }

        public double getActualTime() {
            return actualTime;
        }

        public double getActualProductionTime() {
            return actualProductionTime;
        }

        public double getAvailability() {
            return availability;
        }
    }
}
