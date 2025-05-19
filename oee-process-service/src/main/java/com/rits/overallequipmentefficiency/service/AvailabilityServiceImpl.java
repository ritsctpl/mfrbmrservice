package com.rits.overallequipmentefficiency.service;


import com.rits.common.dto.OeeFilterRequest;
import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.AvailabilityEntity;
import com.rits.overallequipmentefficiency.service.AvailabilityService;
import com.rits.overallequipmentefficiency.service.DowntimeService;
import com.rits.overallequipmentefficiency.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.rits.overallequipmentefficiency.dto.Association;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service("oeeavailabilityService")
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository oeeAvailabilityRepository;
    private final DowntimeService downtimeService;
    private final WebClient.Builder webClientBuilder;

    private static final Logger log = LoggerFactory.getLogger(AvailabilityServiceImpl.class);

    @Value("${shift-service.url}/getShiftDetailBetweenTime")
    private String getShiftDetailBetweenTime;

    @Value("${cycletime-service.url}/getCycletimesByResource")
    private String getCycletimesByResource;



    @Value("${cycletime-service.url}/getCycletimesByResourceAndItem")
    private String getCycletimesByResourceAndItem;


    @Value("${cycletime-service.url}/getFilteredCycleTimesByWorkCenter")
    private String getCycletimesByResourceAndItemAndWc;

    @Value("${productionlog-service.url}/uniqueItemVersions")
    private String getUniqueItemVersions;

    @Value("${productionlog-service.url}/uniqueItemVersionsByWc")
    private String getUniqueItemVersionsByWc;

    @Value("${cycletime-service.url}/getCycletimesByWorkcenter")
    private String getCycletimesByWorkcenter;

    @Value("${integration-service.uri}/logGenericMessage")
    private String logGenericMessage;


    @Value("${workcenter-service.url}/retrieve")
    private String retrieveWorkcenter;

    @Value("${workcenter-service.url}/retrieveTrackOeeWorkcenters")
    private String retrieveTrackOeeWorkcenters;

    @Override
    public List<AvailabilityEntity> logAvailabilityBetweenHours(AvailabilityRequest request) {
        List<AvailabilityEntity> OeeAvailabilityEntityList = new ArrayList<AvailabilityEntity>();
        // Step 1: Call DowntimeService to update the downtime details
        //updateDowntimeService(request);
        downtimeService.updateUpdatedDateTime(request.getSite(), request.getResourceId(), request.getStartDateTime(), request.getEndDateTime());
        DowntimeRequest dt = new DowntimeRequest();
        dt.setResourceId(request.getResourceId());
        dt.setSite(request.getSite());
        dt.setStartDateTime(request.getStartDateTime());
        dt.setEndDateTime(request.getEndDateTime());

     //   downtimeService.updateUpdatedDateTime(request.getSite(), request.getResourceId(), request.getStartDateTime(), request.getEndDateTime());
        long plannedDowntime= downtimeService.getPlannedBreakHoursBetweenTime(dt);

        // Step 2: Fetch shift details
        List<ShiftOutput> shiftOutputs = fetchShiftDetails(request);

        // Step 3: Process each shift
        for (ShiftOutput shiftOutput : shiftOutputs) {
            if(shiftOutput.getShiftId()==null){
            return OeeAvailabilityEntityList;
            }
            DowntimeSummary downtimeSummary=calculateDowntime(request, shiftOutput);

            double rawPlannedOperatingTime = shiftOutput.getPlannedOperatingTime();
            double plannedOperatingTime = Math.max(0, rawPlannedOperatingTime - plannedDowntime);

            double totalDowntime = Math.min(downtimeSummary.getTotalDowntime(), plannedOperatingTime);
            downtimeSummary.setTotalDowntime((long)totalDowntime);
            double actualAvailableTime = plannedOperatingTime - totalDowntime;

            /*double plannedOperatingTime = shiftOutput.getPlannedOperatingTime() - plannedDowntime;
            //double totalDowntime = downtimeSummary.getTotalDowntime();
            double totalDowntime = Math.min(downtimeSummary.getTotalDowntime(), plannedOperatingTime);
            double actualAvailableTime = plannedOperatingTime - totalDowntime;*/

            double availabilityPercentage = ((plannedOperatingTime > 0) ? actualAvailableTime / plannedOperatingTime : 0.0)*100;

// Construct shiftStartDateTime and shiftEndDateTime using request date and shift times
            LocalDate requestDate = request.getStartDateTime().toLocalDate();
            LocalDateTime shiftStartDateTime = LocalDateTime.of(requestDate, shiftOutput.getShiftStartTime());
            LocalDateTime shiftEndDateTime = LocalDateTime.of(requestDate, shiftOutput.getShiftEndTime());

            // If shiftEndDateTime is before shiftStartDateTime, it means the shift crosses midnight
            if (shiftEndDateTime.isBefore(shiftStartDateTime)) {
                shiftEndDateTime = shiftEndDateTime.plusDays(1);
            }
            // Now, if the request's start time is before the calculated shift start time,
// it likely means the shift actually began on the previous day.
// For example, if the logic is run at 07-02-2025 00:00:00 and the shift times are 22:00 to 05:59:59,
// then using 07-02-2025 would give shiftStartDateTime = 07-02-2025 22:00:00, which is later than the request start.
// So, subtract one day from both shift start and end.
            if (request.getStartDateTime().isBefore(shiftStartDateTime)) {
                shiftStartDateTime = shiftStartDateTime.minusDays(1);
                shiftEndDateTime = shiftEndDateTime.minusDays(1);
            }

            // Step 4: Save the calculated availability to the database
            AvailabilityEntity oeeSavedAvailability =  saveAvailabilityEntity(request, plannedOperatingTime,plannedDowntime, shiftOutput, downtimeSummary, actualAvailableTime, availabilityPercentage,shiftStartDateTime,shiftEndDateTime,"RESOURCE");

            // Map shiftStartDateTime and shiftEndDateTime
            oeeSavedAvailability.setShiftStartDateTime(shiftStartDateTime);
            oeeSavedAvailability.setShiftEndDateTime(shiftEndDateTime);

            OeeAvailabilityEntityList.add(oeeSavedAvailability);
        }

        return OeeAvailabilityEntityList;
    }

    @Override
    public List<AvailabilityEntity> getAvailabilityBetweenHours(AvailabilityRequest request) {
        List<AvailabilityEntity> OeeAvailabilityEntityList = new ArrayList<AvailabilityEntity>();
        // Step 1: Call DowntimeService to update the downtime details
        //updateDowntimeService(request);

        DowntimeRequest dt = new DowntimeRequest();
        dt.setResourceId(request.getResourceId());
        dt.setSite(request.getSite());
        dt.setStartDateTime(request.getStartDateTime());
        dt.setEndDateTime(request.getEndDateTime());

        downtimeService.updateUpdatedDateTime(request.getSite(), request.getResourceId(), request.getStartDateTime(), request.getEndDateTime());
        long plannedDowntime= downtimeService.getPlannedBreakHoursBetweenTime(dt);
        // Step 2: Fetch shift details
        List<ShiftOutput> shiftOutputs = fetchShiftDetails(request);

        // Step 3: Process each shift
        for (ShiftOutput shiftOutput : shiftOutputs) {

            DowntimeSummary downtimeSummary=calculateDowntime(request, shiftOutput);
            double plannedOperatingTime = shiftOutput.getPlannedOperatingTime() - plannedDowntime;
            double totalDowntime = downtimeSummary.getTotalDowntime();
            double actualAvailableTime = plannedOperatingTime - totalDowntime;
            double availabilityPercentage = ((plannedOperatingTime > 0) ? actualAvailableTime / plannedOperatingTime : 0.0)*100;


            LocalDate requestDate = request.getStartDateTime().toLocalDate();
            LocalDateTime shiftStartDateTime = LocalDateTime.of(requestDate, shiftOutput.getShiftStartTime());
            LocalDateTime shiftEndDateTime = LocalDateTime.of(requestDate, shiftOutput.getShiftEndTime());

            // If shiftEndDateTime is before shiftStartDateTime, it means the shift crosses midnight
            if (shiftEndDateTime.isBefore(shiftStartDateTime)) {
                shiftEndDateTime = shiftEndDateTime.plusDays(1);
            }

            // Now, if the request's start time is before the calculated shift start time,
// it likely means the shift actually began on the previous day.
// For example, if the logic is run at 07-02-2025 00:00:00 and the shift times are 22:00 to 05:59:59,
// then using 07-02-2025 would give shiftStartDateTime = 07-02-2025 22:00:00, which is later than the request start.
// So, subtract one day from both shift start and end.
            if (request.getStartDateTime().isBefore(shiftStartDateTime)) {
                shiftStartDateTime = shiftStartDateTime.minusDays(1);
                shiftEndDateTime = shiftEndDateTime.minusDays(1);
            }

            // Step 4: Save the calculated availability to the database
            AvailabilityEntity oeeAvailability =  buildOeeAvailability(request, plannedOperatingTime,plannedDowntime ,shiftOutput, downtimeSummary, actualAvailableTime, availabilityPercentage,shiftStartDateTime,shiftEndDateTime,"RESOURCE");
            OeeAvailabilityEntityList.add(oeeAvailability);
        }

        return OeeAvailabilityEntityList;
    }
    private List<ShiftOutput> fetchShiftDetails(AvailabilityRequest request) {
        ShiftInput shiftRequest = new ShiftInput(request.getSite(), request.getResourceId(),request.getWorkcenterId(),request.getStartDateTime(),request.getEndDateTime());
        return webClientBuilder.build()
                .post()
                .uri(getShiftDetailBetweenTime)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToFlux(ShiftOutput.class)
                .collectList()
                .block();
    }

    private DowntimeSummary calculateDowntime(AvailabilityRequest request, ShiftOutput shiftOutput) {
        DowntimeRequest downtimeRequest = new DowntimeRequest();
        downtimeRequest.setSite(request.getSite());
        downtimeRequest.setResourceId(request.getResourceId());
        downtimeRequest.setStartDateTime(shiftOutput.getIntervalStartDatetime());
        downtimeRequest.setEndDateTime(shiftOutput.getIntervalEndDatetime());

     //   downtimeService.getBreakHoursBetweenTime(downtimeRequest);
        return downtimeService.getBreakHoursBetweenTimeWithDetails(downtimeRequest);
    }

    private AvailabilityEntity saveAvailabilityEntity(AvailabilityRequest request, double plannedOperatingTime,long plannedDowntime,ShiftOutput shiftOutput, DowntimeSummary totalDowntime, double actualAvailableTime, double availabilityPercentage,LocalDateTime shiftStart, LocalDateTime shiftEnd,String category) {
        AvailabilityEntity entity = buildOeeAvailability(request, plannedOperatingTime, plannedDowntime,shiftOutput,totalDowntime,actualAvailableTime,availabilityPercentage,shiftStart, shiftEnd,category);
      return  oeeAvailabilityRepository.saveAndFlush(entity);
    }

    private AvailabilityEntity buildOeeAvailability(AvailabilityRequest request, double plannedOperatingTime,long plannedDowntime, ShiftOutput shiftOutput,DowntimeSummary downtimeSummary, double actualAvailableTime, double availabilityPercentage,LocalDateTime shiftStart, LocalDateTime shiftEnd,String category) {
        AvailabilityEntity entity = new AvailabilityEntity();
        entity.setSite(request.getSite());
        entity.setResourceId(request.getResourceId());
        entity.setWorkcenterId(request.getWorkcenterId());
        entity.setShiftId(shiftOutput.getShiftId());
        //entity.setAvailabilityDate(LocalDateTime.now().toLocalDate());
        entity.setAvailabilityDate(shiftOutput.getIntervalStartDatetime().toLocalDate());
        entity.setPlannedOperatingTime(plannedOperatingTime);
        entity.setRuntime(shiftOutput.getTotalShiftTime().doubleValue());
        entity.setDowntime((double) downtimeSummary.getTotalDowntime());
        entity.setActualAvailableTime(actualAvailableTime);
        entity.setReason(downtimeSummary.getMajorReason());
        entity.setRootCause(downtimeSummary.getMajorRootCause());
        entity.setShiftBreakDuration(shiftOutput.getBreaktime().doubleValue());
        entity.setNonProductionDuration((double)plannedDowntime);
        entity.setAvailabilityPercentage(availabilityPercentage);
        entity.setIsPlannedDowntimeIncluded(false);
        entity.setCreatedDatetime(LocalDateTime.now());
        entity.setReason(downtimeSummary.getMajorReason());
        entity.setRootCause(downtimeSummary.getMajorRootCause());
        entity.setIntervalStartDateTime(shiftOutput.intervalStartDatetime);
        entity.setIntervalEndDateTime(shiftOutput.intervalEndDatetime);
        entity.setUpdatedDatetime(LocalDateTime.now());
        entity.setShiftRef(shiftOutput.getShiftRef());
        entity.setShiftStartDateTime(shiftStart);
        entity.setShiftEndDateTime(shiftEnd);
        entity.setCategory(category);
        entity.setActive(1);
        return entity;
    }

    @Override
    public List<PerformanceInput> logAvailabiltyAndPublish(AvailabilityRequest request) {
        List<PerformanceInput> performanceInputs = new ArrayList<>();

        // Step 1: Call logAvailabilityBetweenHours and get the response
        List<AvailabilityEntity> availabilityEntities = logAvailabilityBetweenHours(request);

       /* // Step 2: Call the CycleTime service via WebClient
        CycleTimeReq cycleTimeReq = new CycleTimeReq();
        cycleTimeReq.setSite(request.getSite());
        cycleTimeReq.setResourceId(request.getResourceId());
        List<CycleTime> cycleTimes = fetchCycleTimes(cycleTimeReq);*/

        // Step 3: Prepare PerformanceInput for each availability and cycle time
        for (AvailabilityEntity availability : availabilityEntities) {

            // Step 2: Fetch unique items using production log service
            OeeProductionLogRequest productionLogRequest = new OeeProductionLogRequest();
            productionLogRequest.setSite(request.getSite());
            productionLogRequest.setResourceId(request.getResourceId());
            productionLogRequest.setIntervalStartDateTime(availability.getIntervalStartDateTime());
            productionLogRequest.setIntervalEndDateTime(availability.getIntervalEndDateTime());



            List<UniqueItemVersion> uniqueItemVersions = fetchUniqueItemVersions(productionLogRequest);

            /*if (uniqueItemVersions.isEmpty()) {
                continue;
            }*/
            // If no unique item versions are returned, optionally continue to the next availability.
            if (uniqueItemVersions.isEmpty()) {
                PerformanceInput performanceInput = new PerformanceInput();
                performanceInput.setAvailabilityEntity(availability);
                performanceInput.setEventBy("RESOURCE");

                // Publish the PerformanceInput.
                publishPerformanceInput(performanceInput);

                // Add the PerformanceInput to the output list.
                performanceInputs.add(performanceInput);
            }

            // For each unique item version, create a PerformanceInput and publish it.
            for (UniqueItemVersion uniqueItemVersion : uniqueItemVersions) {
                PerformanceInput performanceInput = new PerformanceInput();
                performanceInput.setAvailabilityEntity(availability);
                performanceInput.setItem(uniqueItemVersion.getItemId());
                performanceInput.setItemVersion(uniqueItemVersion.getItemVersion());
                performanceInput.setEventBy("RESOURCE");

                // Publish the PerformanceInput.
                publishPerformanceInput(performanceInput);

                // Add the PerformanceInput to the output list.
                performanceInputs.add(performanceInput);
            }
            // Step 3: Convert UniqueItemVersion to CycleTimeReq.ItemVersionReq
          /*  List<CycleTimeReq.ItemVersionReq> itemVersionReqs = uniqueItemVersions.stream()
                    .map(item -> new CycleTimeReq.ItemVersionReq(item.getItemId(), item.getItemVersion()))
                    .collect(Collectors.toList());

            // Step 4: Fetch cycle times using cycle time service
            CycleTimeReq cycleTimeReq = new CycleTimeReq();
            cycleTimeReq.setSite(request.getSite());
            cycleTimeReq.setResourceId(request.getResourceId());
            cycleTimeReq.setItemVersionReqs(itemVersionReqs);

            List<CycleTime> cycleTimes = fetchCycleTimesByItems(cycleTimeReq);


            for (CycleTime cycleTime : cycleTimes) {
                PerformanceInput performanceInput = new PerformanceInput();
                performanceInput.setAvailabilityEntity(availability);
                performanceInput.setCycleTime(cycleTime);
                performanceInput.setEventBy("RESOURCE");
                // Publish each PerformanceInput
                publishPerformanceInput(performanceInput);

                // Add to the list
                performanceInputs.add(performanceInput);
            }*/
        }

        return performanceInputs; // Step 4: Output the list of PerformanceInput
    }

    private List<CycleTime> fetchCycleTimes(CycleTimeReq cycleTimeReq) {

        List<CycleTime> cycleTimes=webClientBuilder.build()
                .post()
                .uri(getCycletimesByResource)
                .bodyValue(cycleTimeReq)
                .retrieve()
                .bodyToFlux(CycleTime.class)
                .collectList()
                .block();

        return cycleTimes;
    }

    private List<UniqueItemVersion> fetchUniqueItemVersions(OeeProductionLogRequest request) {
        return webClientBuilder.build()
                .post()
                .uri(getUniqueItemVersions)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(UniqueItemVersion.class)
                .collectList()
                .block();
    }

    private List<UniqueItemVersion> fetchUniqueItemVersionsByWc(OeeProductionLogRequest request) {
        return webClientBuilder.build()
                .post()
                .uri(getUniqueItemVersionsByWc)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(UniqueItemVersion.class)
                .collectList()
                .block();
    }

    private List<CycleTime> fetchCycleTimesByItems(CycleTimeReq cycleTimeReq) {
        return webClientBuilder.build()
                .post()
                .uri(getCycletimesByResourceAndItem)
                .bodyValue(cycleTimeReq)
                .retrieve()
                .bodyToFlux(CycleTime.class)
                .collectList()
                .block();
    }

    private List<CycleTime> fetchCycleTimesByItemsByWc(CycleTimeReq cycleTimeReq) {
        return webClientBuilder.build()
                .post()
                .uri(getCycletimesByResourceAndItemAndWc)
                .bodyValue(cycleTimeReq)
                .retrieve()
                .bodyToFlux(CycleTime.class)
                .collectList()
                .block();
    }
    private void publishPerformanceInput(PerformanceInput performanceInput) {

        try {
            // Prepare the payload for the WebClient
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("topicName", "performanceTopic");
            requestPayload.put("payload", performanceInput); // Directly map PerformanceInput to payload

            // Send the request to the integration-service using WebClientBuilder
            webClientBuilder.build()
                    .post()
                    .uri(logGenericMessage)
                    .bodyValue(requestPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Published PerformanceInput: {}, Response: {}", performanceInput, response))
                    .doOnError(e -> log.error("Failed to publish PerformanceInput: {}", performanceInput, e))
                    .block();
        } catch (Exception e) {
            log.error("Exception while publishing PerformanceInput: {}", performanceInput, e);
        }
    }

    @Override
    public List<PerformanceInput> logLineAvailability(AvailabilityRequest request) {
        List<PerformanceInput> finalPerformanceInputs = new ArrayList<>();

        // Build a WorkCenterRequest from the workcenterId provided in the request.
        WorkCenterRequest workCenterRequest = new WorkCenterRequest();
        workCenterRequest.setSite(request.getSite());
        workCenterRequest.setWorkCenter(request.getWorkcenterId());

        // Use a hierarchy method that returns a mapping:
        // key = workcenter id (could be the parent or nested workcenter),
        // value = list of resource ids directly associated with that workcenter.
        Map<String, List<String>> workCenterHierarchy = fetchWorkCenterHierarchy(workCenterRequest);

        // Iterate over each workcenter group in the hierarchy.
        for (Map.Entry<String, List<String>> entry : workCenterHierarchy.entrySet()) {
            String wcId = entry.getKey();
            List<String> resourceIds = entry.getValue();

            // For the current workcenter group, process each associated resource to calculate availability.
            List<CompletableFuture<List<PerformanceInput>>> resourceFutures = new ArrayList<>();
            for (String resourceId : resourceIds) {
                AvailabilityRequest resourceRequest = new AvailabilityRequest();
                resourceRequest.setSite(request.getSite());
                resourceRequest.setResourceId(resourceId);
                // IMPORTANT: Use the workcenter id from the group, not the original (parent) id.
                resourceRequest.setWorkcenterId(wcId);
                resourceRequest.setStartDateTime(request.getStartDateTime());
                resourceRequest.setEndDateTime(request.getEndDateTime());

                resourceFutures.add(processResourceAvailability(resourceRequest));
            }

            // Wait for all resource-level futures for this workcenter group to complete.
            List<PerformanceInput> groupPerformanceInputs = resourceFutures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            // Group the resulting AvailabilityEntity objects by shiftId.
            Map<String, List<AvailabilityEntity>> groupedByShift = groupPerformanceInputs.stream()
                    .map(PerformanceInput::getAvailabilityEntity)
                    .collect(Collectors.groupingBy(AvailabilityEntity::getShiftId));

            // For each shift group, aggregate the metrics.
            for (Map.Entry<String, List<AvailabilityEntity>> shiftEntry : groupedByShift.entrySet()) {
                String shiftId = shiftEntry.getKey();
                List<AvailabilityEntity> entities = shiftEntry.getValue();

                double avgPlannedOperatingTime = entities.stream()
                        .mapToDouble(AvailabilityEntity::getPlannedOperatingTime)
                        .average().orElse(0.0);
                double avgActualAvailableTime = entities.stream()
                        .mapToDouble(AvailabilityEntity::getActualAvailableTime)
                        .min().orElse(0.0);
                double avgRuntime = entities.stream()
                        .mapToDouble(AvailabilityEntity::getRuntime)
                        .min().orElse(0.0);
                double avgDowntime = entities.stream()
                        .mapToDouble(AvailabilityEntity::getDowntime)
                        .max().orElse(0.0);
                double avgShiftBreakDuration = entities.stream()
                        .mapToDouble(AvailabilityEntity::getShiftBreakDuration)
                        .max().orElse(0.0);
                double avgNonProductionDuration = entities.stream()
                        .mapToDouble(AvailabilityEntity::getNonProductionDuration)
                        .max().orElse(0.0);
                double avgAvailabilityPercentage = entities.stream()
                        .mapToDouble(AvailabilityEntity::getAvailabilityPercentage)
                        .min().orElse(0.0);

                // Build the aggregated availability object for this group and shift.
                AvailabilityEntity workcenterAvailability = new AvailabilityEntity();
                workcenterAvailability.setSite(request.getSite());
                // Use the specific workcenter id from the hierarchy mapping.
                workcenterAvailability.setWorkcenterId(wcId);
                workcenterAvailability.setShiftId(shiftId);
                workcenterAvailability.setPlannedOperatingTime(avgPlannedOperatingTime);
                workcenterAvailability.setActualAvailableTime(avgActualAvailableTime);
                workcenterAvailability.setRuntime(avgRuntime);
                workcenterAvailability.setDowntime(avgDowntime);
                workcenterAvailability.setAvailabilityDate(request.getStartDateTime().toLocalDate());
                workcenterAvailability.setShiftBreakDuration(avgShiftBreakDuration);
                workcenterAvailability.setNonProductionDuration(avgNonProductionDuration);
                workcenterAvailability.setAvailabilityPercentage(avgAvailabilityPercentage);
                workcenterAvailability.setCreatedDatetime(LocalDateTime.now());
                workcenterAvailability.setUpdatedDatetime(LocalDateTime.now());
                workcenterAvailability.setIntervalStartDateTime(request.getStartDateTime());
                workcenterAvailability.setIntervalEndDateTime(request.getEndDateTime());
                workcenterAvailability.setShiftRef(shiftId);
                workcenterAvailability.setActive(1);
                workcenterAvailability.setCategory("WORKCENTER");
                // Use a sample entity (if available) to set shift start and end times.
                AvailabilityEntity sampleEntity = entities.get(0);
                workcenterAvailability.setShiftStartDateTime(sampleEntity.getShiftStartDateTime());
                workcenterAvailability.setShiftEndDateTime(sampleEntity.getShiftEndDateTime());

                // Save the aggregated availability for the current workcenter group and shift.
                oeeAvailabilityRepository.saveAndFlush(workcenterAvailability);

                // --- Production Log / Unique Items Fetch ---
                // Build an OeeProductionLogRequest for this group using the aggregated availability.
                OeeProductionLogRequest productionLogRequest = new OeeProductionLogRequest();
                productionLogRequest.setSite(request.getSite());
                // Use the group workcenter id for production log lookup.
                productionLogRequest.setWorkcenterId(workcenterAvailability.getWorkcenterId());
                productionLogRequest.setIntervalStartDateTime(workcenterAvailability.getShiftStartDateTime());
                productionLogRequest.setIntervalEndDateTime(workcenterAvailability.getIntervalEndDateTime());

                /*List<UniqueItemVersion> uniqueItemVersions = fetchUniqueItemVersionsByWc(productionLogRequest);
                // If no unique item versions are found, skip publishing performance for this aggregation.
                if (uniqueItemVersions.isEmpty()) {
                    continue;
                }*/
                // --- End Production Log ---

                // Build a PerformanceInput for this workcenter group.
                PerformanceInput workcenterPerformanceInput = new PerformanceInput();
                workcenterPerformanceInput.setAvailabilityEntity(workcenterAvailability);
                workcenterPerformanceInput.setEventBy("WORKCENTER");

                // Publish the PerformanceInput.
                publishPerformanceInput(workcenterPerformanceInput);

                // Add it to the overall result list.
                finalPerformanceInputs.add(workcenterPerformanceInput);
            }
        }

        return finalPerformanceInputs;
    }


    //@Override
    public List<PerformanceInput> logLineAvailabilityPerviousVersion(AvailabilityRequest request) {
        //List<PerformanceInput> finalPerformanceInputs = new ArrayList<>();
        List<CompletableFuture<List<PerformanceInput>>> futures = new ArrayList<>();

        // Step 1: Retrieve all resources inside the workcenter
        WorkCenterRequest workCenterRequest = new WorkCenterRequest();
        workCenterRequest.setSite(request.getSite());
        workCenterRequest.setWorkCenter(request.getWorkcenterId());

        List<String> lineResourceList = fetchLineResources(workCenterRequest);

        // Step 2: Process each resource to calculate availability
        for (String resourceId : lineResourceList) {
            AvailabilityRequest resourceRequest = new AvailabilityRequest();
            resourceRequest.setSite(request.getSite());
            resourceRequest.setResourceId(resourceId);
            resourceRequest.setWorkcenterId(request.getWorkcenterId());
            resourceRequest.setStartDateTime(request.getStartDateTime());
            resourceRequest.setEndDateTime(request.getEndDateTime());


          futures.add(processResourceAvailability(resourceRequest));
         /*   List<PerformanceInput> resourcePerformanceInputs = logAvailabiltyAndPublish(resourceRequest);
            finalPerformanceInputs.addAll(resourcePerformanceInputs);*/
        }

        // Step 3: Wait for all tasks to complete and collect results
        List<PerformanceInput> finalPerformanceInputs = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Step 3: Build workcenter availability by shiftId
        Map<String, List<AvailabilityEntity>> groupedByShift = finalPerformanceInputs.stream()
                .map(PerformanceInput::getAvailabilityEntity)
                .collect(Collectors.groupingBy(AvailabilityEntity::getShiftId));

        for (Map.Entry<String, List<AvailabilityEntity>> entry : groupedByShift.entrySet()) {
            String shiftId = entry.getKey();
            List<AvailabilityEntity> entities = entry.getValue();

            double avgPlannedOperatingTime = entities.stream()
                    .mapToDouble(AvailabilityEntity::getPlannedOperatingTime)
                    .average().orElse(0.0);

            double avgActualAvailableTime = entities.stream()
                    .mapToDouble(AvailabilityEntity::getActualAvailableTime)
                    .average().orElse(0.0);

            double avgRuntime = entities.stream()
                    .mapToDouble(AvailabilityEntity::getRuntime)
                    .average().orElse(0.0);

            double avgDowntime = entities.stream()
                    .mapToDouble(AvailabilityEntity::getDowntime)
                    .average().orElse(0.0);

            double avgShiftBreakDuration = entities.stream()
                    .mapToDouble(AvailabilityEntity::getShiftBreakDuration)
                    .average().orElse(0.0);

            double avgNonProductionDuration = entities.stream()
                    .mapToDouble(AvailabilityEntity::getNonProductionDuration)
                    .average().orElse(0.0);

            double avgAvailabilityPercentage = entities.stream()
                    .mapToDouble(AvailabilityEntity::getAvailabilityPercentage)
                    .average().orElse(0.0);

            AvailabilityEntity workcenterAvailability = new AvailabilityEntity();
            workcenterAvailability.setSite(request.getSite());
            workcenterAvailability.setWorkcenterId(request.getWorkcenterId());
            workcenterAvailability.setShiftId(shiftId);
            workcenterAvailability.setPlannedOperatingTime(avgPlannedOperatingTime);
            workcenterAvailability.setActualAvailableTime(avgActualAvailableTime);
            workcenterAvailability.setRuntime(avgRuntime);
            workcenterAvailability.setDowntime(avgDowntime);
          //  workcenterAvailability.setAvailabilityDate(LocalDateTime.now().toLocalDate());
            workcenterAvailability.setAvailabilityDate(request.getStartDateTime().toLocalDate());
            workcenterAvailability.setShiftBreakDuration(avgShiftBreakDuration);
            workcenterAvailability.setNonProductionDuration(avgNonProductionDuration);
            workcenterAvailability.setAvailabilityPercentage(avgAvailabilityPercentage);
            workcenterAvailability.setCreatedDatetime(LocalDateTime.now());
            workcenterAvailability.setUpdatedDatetime(LocalDateTime.now());
            workcenterAvailability.setIntervalStartDateTime(request.getStartDateTime());
            workcenterAvailability.setIntervalEndDateTime(request.getEndDateTime());
            workcenterAvailability.setShiftRef(shiftId);
            workcenterAvailability.setActive(1);
            workcenterAvailability.setCategory("WORKCENTER");
            AvailabilityEntity sampleEntity = entities.get(0);
            workcenterAvailability.setShiftStartDateTime(sampleEntity.getShiftStartDateTime());
            workcenterAvailability.setShiftEndDateTime(sampleEntity.getShiftEndDateTime());
            // *** End of new code ***


            oeeAvailabilityRepository.saveAndFlush(workcenterAvailability);


// Step 2: Fetch unique items using production log service
            OeeProductionLogRequest productionLogRequest = new OeeProductionLogRequest();
            productionLogRequest.setSite(request.getSite());
            productionLogRequest.setWorkcenterId(workcenterAvailability.getWorkcenterId());
            productionLogRequest.setIntervalStartDateTime(workcenterAvailability.getShiftStartDateTime());
            productionLogRequest.setIntervalEndDateTime(workcenterAvailability.getIntervalEndDateTime());

            List<UniqueItemVersion> uniqueItemVersions = fetchUniqueItemVersionsByWc(productionLogRequest);


    // If no unique item versions are returned, optionally continue to the next availability.
            if (uniqueItemVersions.isEmpty()) {
                continue;
            }

            // For each unique item version, create a PerformanceInput and publish it.
            //for (UniqueItemVersion uniqueItemVersion : uniqueItemVersions) {
                PerformanceInput workcenterPerformanceInput = new PerformanceInput();
                workcenterPerformanceInput.setAvailabilityEntity(workcenterAvailability);
              //  workcenterPerformanceInput.setItem(uniqueItemVersion.getItemId());
              //  workcenterPerformanceInput.setItemVersion(uniqueItemVersion.getItemVersion());
                workcenterPerformanceInput.setEventBy("WORKCENTER");

                // Publish the PerformanceInput.
                publishPerformanceInput(workcenterPerformanceInput);

                // Add the PerformanceInput to the output list.
                finalPerformanceInputs.add(workcenterPerformanceInput);
            //}

            /*// Step 3: Convert UniqueItemVersion to CycleTimeReq.ItemVersionReq
            List<CycleTimeReq.ItemVersionReq> itemVersionReqs = uniqueItemVersions.stream()
                    .map(item -> new CycleTimeReq.ItemVersionReq(item.getItemId(), item.getItemVersion()))
                    .collect(Collectors.toList());

            // Step 4: Build performance input for workcenter
            CycleTimeReq cycleTimeReq = new CycleTimeReq();
            cycleTimeReq.setSite(request.getSite());
            cycleTimeReq.setWorkCenterId(request.getWorkcenterId());
            cycleTimeReq.setItemVersionReqs(itemVersionReqs);
            //List<CycleTime> cycleTimes = fetchCycleTimesByWorkcenter(cycleTimeReq);
            List<CycleTime> cycleTimes = fetchCycleTimesByItemsByWc(cycleTimeReq);

            for (CycleTime cycleTime : cycleTimes) {
                PerformanceInput workcenterPerformanceInput = new PerformanceInput();
                workcenterPerformanceInput.setAvailabilityEntity(workcenterAvailability);
                workcenterPerformanceInput.setCycleTime(cycleTime);
                workcenterPerformanceInput.setEventBy("WORKCENTER");

                publishPerformanceInput(workcenterPerformanceInput);
                finalPerformanceInputs.add(workcenterPerformanceInput);
            }*/
        }

        return finalPerformanceInputs;
    }

    @Async
    public CompletableFuture<List<PerformanceInput>> processResourceAvailability(AvailabilityRequest resourceRequest) {
        List<PerformanceInput> resourcePerformanceInputs = logAvailabiltyAndPublish(resourceRequest);
        return CompletableFuture.completedFuture(resourcePerformanceInputs);
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

    private Map<String, List<String>> fetchWorkCenterHierarchy(WorkCenterRequest workCenterRequest) {
        // Retrieve the current workcenter using WebClient.
        WorkCenter workCenter = webClientBuilder.build()
                .post()
                .uri(retrieveWorkcenter)
                .bodyValue(workCenterRequest)
                .retrieve()
                .bodyToMono(WorkCenter.class)
                .block();

        // Map to store the workcenter id and its directly associated resource IDs.
        Map<String, List<String>> hierarchy = new HashMap<>();

        if (workCenter == null) {
            return hierarchy;
        }

        // For the current workcenter, get its direct resources.
        List<String> directResources = new ArrayList<>();
        if (workCenter.getAssociationList() != null) {
            for (Association association : workCenter.getAssociationList()) {
                String type = association.getType();
                // If association type is "Resource", add it.
                if ("Resource".equalsIgnoreCase(type)) {
                    // Assuming the association provides an identifier via getAssociateId()
                    if (association.getAssociateId() != null && !association.getAssociateId().trim().isEmpty()) {
                        directResources.add(association.getAssociateId());
                    }
                }
            }
        }

        // Add the current workcenter to the hierarchy only if its category is "Line".
        // (You might want to change this if you always want to record the current workcenter.)
        if (workCenter.getWorkCenterCategory() != null
                && "Line".equalsIgnoreCase(workCenter.getWorkCenterCategory())) {
            hierarchy.put(workCenter.getWorkCenter(), new ArrayList<>(directResources));
        } else {
            // Otherwise, you can still add it if needed.
            hierarchy.put(workCenter.getWorkCenter(), new ArrayList<>(directResources));
        }

        // Next, process associations that represent nested work centers.
        if (workCenter.getAssociationList() != null) {
            for (Association association : workCenter.getAssociationList()) {
                String type = association.getType();
                // For nested workcenter associations, assume type "Work Center" (or "Line")
                if ("Work Center".equalsIgnoreCase(type) || "Line".equalsIgnoreCase(type)) {
                    // Retrieve nested workcenter
                    String nestedWcId = association.getAssociateId();
                    if (nestedWcId == null || nestedWcId.trim().isEmpty()) {
                        continue;
                    }
                    WorkCenterRequest nestedRequest = new WorkCenterRequest();
                    nestedRequest.setSite(workCenterRequest.getSite());
                    nestedRequest.setWorkCenter(nestedWcId);

                    // Recursively fetch the nested hierarchy mapping.
                    Map<String, List<String>> nestedHierarchy = fetchWorkCenterHierarchy(nestedRequest);
                    // Merge the mapping: each key in the nested mapping represents a workcenter already separate.
                    for (Map.Entry<String, List<String>> entry : nestedHierarchy.entrySet()) {
                        hierarchy.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        return hierarchy;
    }

    private List<CycleTime> fetchCycleTimesByWorkcenter(CycleTimeReq cycleTimeReq) {
        return webClientBuilder.build()
                .post()
                .uri(getCycletimesByWorkcenter)
                .bodyValue(cycleTimeReq)
                .retrieve()
                .bodyToFlux(CycleTime.class)
                .collectList()
                .block();
    }

    @Override
    public List<AvailabilityRequest> splitAndPublishLineAvailability(AvailabilityRequest request) {
        List<AvailabilityRequest> splitRequests = splitAvailabilityRequestByDay(request);

        int batchSize = 10;

        // Process split requests in batches
        for (int i = 0; i < splitRequests.size(); i += batchSize) {
            int end = Math.min(i + batchSize, splitRequests.size());
            List<AvailabilityRequest> batch = splitRequests.subList(i, end);

            batch.forEach(splitRequest -> CompletableFuture.runAsync(() -> publishSplitRequest(splitRequest)));
        }


        return splitRequests;
    }

    public List<AvailabilityRequest> splitAvailabilityRequestByDay(AvailabilityRequest request) {
        List<AvailabilityRequest> splitRequests = new ArrayList<>();
        LocalDateTime currentStart = request.getStartDateTime();
        LocalDateTime endDateTime = request.getEndDateTime();

        while (!currentStart.isAfter(endDateTime)) { // Use isAfter to avoid infinite loop
            LocalDateTime nextStart = currentStart.toLocalDate().atTime(23, 59, 59);
            if (nextStart.isAfter(endDateTime)) {
                nextStart = endDateTime;
            }

            AvailabilityRequest splitRequest = new AvailabilityRequest();
            splitRequest.setSite(request.getSite());
            splitRequest.setWorkcenterId(request.getWorkcenterId());
            splitRequest.setResourceId(request.getResourceId());
            splitRequest.setStartDateTime(currentStart);
            splitRequest.setEndDateTime(nextStart);
            splitRequest.setShiftRef(request.getShiftRef());

            splitRequests.add(splitRequest);

            // Increment currentStart to avoid infinite loop
            currentStart = nextStart.plusSeconds(1);
        }

        return splitRequests;
    }


    private void publishSplitRequest(AvailabilityRequest splitRequest) {
        try {
            // Prepare the payload for the WebClient
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("topicName", "availabilityTopic");
            requestPayload.put("payload", splitRequest);

            // Send the request to the integration-service using WebClientBuilder
            webClientBuilder.build()
                    .post()
                    .uri(logGenericMessage)
                    .bodyValue(requestPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Published AvailabilityRequest: {}, Response: {}", splitRequest, response))
                    .doOnError(e -> log.error("Failed to publish AvailabilityRequest: {}", splitRequest, e))
                    .block();
        } catch (Exception e) {
            log.error("Exception while publishing AvailabilityRequest: {}", splitRequest, e);
        }
    }
    private void setStartAndEndTimes(AvailabilityRequest request) {
        LocalDateTime currentTime = LocalDateTime.now();

        // Check if startDateTime and endDateTime are already set
        if (request.getStartDateTime() != null && request.getEndDateTime() != null) {
            // Use the provided start and end times
            return;
        }

        // If not set, compute them based on interval
        request.setEndDateTime(currentTime);

        int intervalSeconds = request.getEventIntervalSeconds();
        LocalDateTime startDateTime = intervalSeconds > 0
                ? currentTime.minusSeconds(intervalSeconds)
                : currentTime.minusMinutes(60);

        request.setStartDateTime(startDateTime);
    }
    @Override
    public void publishLineAvailabilityRequests(AvailabilityRequest request) {
        try {
            // Step 1: Fetch all work centers with trackOee = true for the given site
            List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(request.getSite());
          //  setStartAndEndTimes(request);
            if (trackedWorkCenters.isEmpty()) {
                log.warn("No work centers with trackOee=true found for site: {}", request.getSite());
                return;
            }

            // Step 2: Build and publish line availability requests for each work center
            for (WorkCenter workCenter : trackedWorkCenters) {
                AvailabilityRequest lineAvailabilityRequest = new AvailabilityRequest();
                lineAvailabilityRequest.setSite(request.getSite());
                lineAvailabilityRequest.setWorkcenterId(workCenter.getWorkCenter());
                lineAvailabilityRequest.setStartDateTime(request.getStartDateTime());
                lineAvailabilityRequest.setEndDateTime(request.getEndDateTime());
                // Publish to lineAvailabilityTopic
                publishLineAvailability(lineAvailabilityRequest);
            }
        } catch (Exception e) {
            log.error("Error while publishing line availability requests for site: {}", request.getSite(), e);
        }
    }

    /*private List<WorkCenter> fetchTrackedWorkCenters(String site) {
        return webClientBuilder.build()
                .get()
                .uri(retrieveWorkcenter, uriBuilder -> uriBuilder.queryParam("site", site).build())
                .retrieve()
                .bodyToFlux(WorkCenter.class)
                .collectList()
                .block();
    }*/

    private List<WorkCenter> fetchTrackedWorkCenters(String site) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("site", site);

        return webClientBuilder.build()
                .post()
                .uri(retrieveTrackOeeWorkcenters)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(WorkCenter.class)
                .collectList()
                .block();
    }

    private void publishLineAvailability(AvailabilityRequest availabilityRequest) {
        try {
            // Prepare the payload for the WebClient
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("topicName", "lineAvailabilityTopic");
            requestPayload.put("payload", availabilityRequest);

            // Send the request to the integration-service using WebClientBuilder
            webClientBuilder.build()
                    .post()
                    .uri(logGenericMessage)
                    .bodyValue(requestPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Published Line Availability Request: {}, Response: {}", availabilityRequest, response))
                    .doOnError(e -> log.error("Failed to publish Line Availability Request: {}", availabilityRequest, e))
                    .block();
        } catch (Exception e) {
            log.error("Exception while publishing Line Availability Request: {}", availabilityRequest, e);
        }
    }


}
