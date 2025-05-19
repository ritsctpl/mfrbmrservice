package com.rits.overallequipmentefficiency.service;

import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.AvailabilityEntity;
import com.rits.overallequipmentefficiency.model.OeeModel;
import com.rits.overallequipmentefficiency.model.PerformanceModel;
import com.rits.overallequipmentefficiency.model.QualityModel;
import com.rits.overallequipmentefficiency.repository.OverallEquipmentEfficiencyRepository;
import com.rits.overallequipmentefficiency.repository.OeeQualityRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QualityOeeServiceImpl implements QualityOeeService {
    @Autowired
    private final OeeQualityRepository qualityRepository;
    @Autowired
    private final OverallEquipmentEfficiencyRepository oeeRepository;

    private final WebClient.Builder webClientBuilder;

    @Value("${productionlog-service.url}/totalProducedQuantity")
    private String totalProducedQuantity;

    @Value("${integration-service.uri}/logGenericMessage")
    private String logGenericMessage;

    private static final Logger log = LoggerFactory.getLogger(QualityOeeServiceImpl.class);

    @Override
    public OeeOutput calculateQualityAndOee(PerformanceOutput performanceOutput) {
        AvailabilityEntity availability = performanceOutput.getAvailabilityEntity();
        PerformanceModel performance = performanceOutput.getPerformanceEntity();

        // Build production log request for scrap
        OeeProductionLogRequest logRequest = buildProductionLogRequest(performance);
        /*logRequest.setEventType("Scrap");

        List<OeeProductionLogResponse> logResponses = fetchProductionLogs(logRequest);

        double badQty = logResponses.stream()
                .mapToDouble(OeeProductionLogResponse::getGrandTotalQty)
                .sum();*/
        // Fetch logs for Scrap and ScrapSFC
        List<String> eventTypes;
        if ("machineCompleteSfcBatch".equalsIgnoreCase(performance.getEventType()) || "machineDoneSfcBatch".equalsIgnoreCase(performance.getEventType())) {
            eventTypes = List.of("machineScrapSfcBatch");
        } else {
            eventTypes = List.of("ScrapSFC");
        }
        double badQty = 0.0;
        String reasonCode = null;
        double maxBadQty = 0.0;

        for (String eventType : eventTypes) {
            logRequest.setEventType(eventType);

            List<OeeProductionLogResponse> logResponses = fetchProductionLogs(logRequest);
            badQty += logResponses.stream()
                    .mapToDouble(OeeProductionLogResponse::getGrandTotalQty)
                    .sum();
            Optional<OeeProductionLogResponse> maxResponse = logResponses.stream()
                    .max(Comparator.comparingDouble(OeeProductionLogResponse::getGrandTotalQty));

            if (maxResponse.isPresent() && maxResponse.get().getGrandTotalQty() > maxBadQty) {
                maxBadQty = maxResponse.get().getGrandTotalQty();
                reasonCode = maxResponse.get().getReasonCode();
            }
        }
//        double goodQty = performance.getActualOutput() - badQty;
        double goodQty = Math.max(0, performance.getActualOutput() - badQty);

        double qualityPercentage = (performance.getActualOutput() > 0)
                ? (goodQty / performance.getActualOutput()) * 100 : 0.0;

        // Create QualityModel
        QualityModel qualityModel = buildQualityModel(performance, goodQty, badQty, qualityPercentage, reasonCode);
        qualityRepository.save(qualityModel);

        // Calculate OEE
        double oee = (availability.getAvailabilityPercentage()/100)
                * (performance.getPerformancePercentage()/100)
                * (qualityPercentage / 100);

        // Convert OEE to percentage for storage or display
        oee = oee * 100;

        // Create OeeModel
        OeeModel oeeModel = buildOeeModel(availability, performance, qualityModel, oee);
        oeeRepository.save(oeeModel);

        OeeOutput oeeOutput = new OeeOutput(availability, performanceOutput.getCycleTime(), performance, qualityModel, oeeModel);

        publishAggregationInput(oeeOutput);
        // Build and return output
        return oeeOutput;
    }

   /* private OeeProductionLogRequest buildProductionLogRequest(PerformanceModel performance) {
        OeeProductionLogRequest logRequest = new OeeProductionLogRequest();
        logRequest.setSite(performance.getSite());
        logRequest.setResourceId(performance.getResourceId());
        logRequest.setIntervalStartDateTime(performance.getIntervalStartDateTime());
        logRequest.setIntervalEndDateTime(performance.getIntervalEndDateTime());

        if (isValid(performance.getItem())) logRequest.setItemId(performance.getItem());
        if (isValid(performance.getItemVersion())) logRequest.setItemVersion(performance.getItemVersion());
        if (isValid(performance.getWorkcenterId())) logRequest.setWorkcenterId(performance.getWorkcenterId());
        if (isValid(performance.getOperation())) logRequest.setOperationId(performance.getOperation());
        if (isValid(performance.getOperationVersion())) logRequest.setOperationVersion(performance.getOperationVersion());
        if (isValid(performance.getShiftId())) logRequest.setShiftId(performance.getShiftId());
        if (isValid(performance.getShopOrderBO())) logRequest.setShopOrderBo(performance.getShopOrderBO());

        return logRequest;
    }
*/
   private OeeProductionLogRequest buildProductionLogRequest(PerformanceModel performance) {
       OeeProductionLogRequest logRequest = new OeeProductionLogRequest();
       String eventType = performance.getEventType();

       if ("completeSfcBatch".equals(eventType) || "machineCompleteSfcBatch".equals(eventType)) {
           logRequest.setSite(performance.getSite());
           logRequest.setIntervalStartDateTime(performance.getIntervalStartDateTime());
           logRequest.setIntervalEndDateTime(performance.getIntervalEndDateTime());

           // Include additcompleteSfcBatchnal fields for "Complete"
           if (isValid(performance.getResourceId())) logRequest.setResourceId(performance.getResourceId());
           if (isValid(performance.getItem())) logRequest.setItemId(performance.getItem());
           if (isValid(performance.getItemVersion())) logRequest.setItemVersion(performance.getItemVersion());
          // if (isValid(performance.getWorkcenterId())) logRequest.setWorkcenterId(performance.getWorkcenterId());
           if (isValid(performance.getOperation())) logRequest.setOperationId(performance.getOperation());
           if (isValid(performance.getOperationVersion())) logRequest.setOperationVersion(performance.getOperationVersion());
         //  if (isValid(performance.getShiftId())) logRequest.setShiftId(performance.getShiftId());
           if (isValid(performance.getShopOrderBO())) logRequest.setShopOrderBo(performance.getShopOrderBO());
           if (isValid(performance.getBatchNumber())) logRequest.setBatchNo(performance.getBatchNumber());
       }
       else if ("doneSfcBatch".equals(eventType) || "machineDoneSfcBatch".equals(eventType)) {
           logRequest.setSite(performance.getSite());
           logRequest.setIntervalStartDateTime(performance.getIntervalStartDateTime());
           logRequest.setIntervalEndDateTime(performance.getIntervalEndDateTime());

           // Include only minimal fields for "doneSfcBatch"
           if (isValid(performance.getItem())) logRequest.setItemId(performance.getItem());
           if (isValid(performance.getItemVersion())) logRequest.setItemVersion(performance.getItemVersion());
       //    if (isValid(performance.getShiftId())) logRequest.setShiftId(performance.getShiftId());
           if (isValid(performance.getShopOrderBO())) logRequest.setShopOrderBo(performance.getShopOrderBO());
           if (isValid(performance.getBatchNumber())) logRequest.setBatchNo(performance.getBatchNumber());
       }

       return logRequest;
   }

    private QualityModel buildQualityModel(PerformanceModel performance, double goodQty, double badQty, double qualityPercentage, String reasonCode) {
        return QualityModel.builder()
                .site(performance.getSite())
                .batchNumber(performance.getBatchNumber())
                .batchSize(performance.getBatchSize())
                .workcenterId(performance.getWorkcenterId())
                .resourceId(performance.getResourceId())
                .shiftId(performance.getShiftId())
                .operation(performance.getOperation())
                .operationVersion(performance.getOperationVersion())
                .item(performance.getItem())
                .itemVersion(performance.getItemVersion())
                .shopOrder(performance.getShopOrderBO())
                .eventTypeOfPerformance(performance.getEventType())
                .totalQuantity(performance.getActualOutput())
                .goodQuantity(goodQty)
                .badQuantity(badQty)
                .reason(reasonCode)
                .plan(performance.getPlannedOutput())
                .qualityPercentage(qualityPercentage)
                .calculationTimestamp(LocalDateTime.now())
                .createdDateTime(LocalDateTime.now())
                .updatedDateTime(LocalDateTime.now())
                .availabilityId(performance.getAvailabilityId())
                .performanceId(performance.getId())
                .intervalStartDateTime(performance.getIntervalStartDateTime())
                .intervalEndDateTime(performance.getIntervalEndDateTime())
                .category(performance.getCategory())
                .targetQuantity(performance.getTargetQuantity())
                .active(1)
                .build();
    }

    private OeeModel buildOeeModel(AvailabilityEntity availability, PerformanceModel performance, QualityModel qualityModel, double oee) {
        return OeeModel.builder()
                .site(performance.getSite())
                .shiftId(performance.getShiftId())
                .workcenterId(performance.getWorkcenterId())
                .resourceId(performance.getResourceId())
                .operation(performance.getOperation())
                .operationVersion(performance.getOperationVersion())
                .item(performance.getItem())
                .itemVersion(performance.getItemVersion())
                .shoporderId(performance.getShopOrderBO())
                .performance(performance.getPerformancePercentage())
                .performanceId(performance.getId())
                .eventTypeOfPerformance(performance.getEventType())
                .availabilityId(performance.getAvailabilityId())
                .intervalStartDateTime(performance.getIntervalStartDateTime())
                .intervalEndDateTime(performance.getIntervalEndDateTime())
                .availability(availability.getAvailabilityPercentage())
                .totalDowntime(availability.getDowntime() != null ? availability.getDowntime() : 0) // Convert Double to int
                .productionTime(availability.getPlannedOperatingTime())
                .actualTime(availability.getActualAvailableTime())
                .quality(qualityModel.getQualityPercentage())
                .goodQty(qualityModel.getGoodQuantity())
                .badQty(qualityModel.getBadQuantity())
                .reason(qualityModel.getReason())
                .totalQty(qualityModel.getTotalQuantity())
                .qualityId(qualityModel.getId())
                .plan((int) performance.getPlannedOutput())
                .oee(oee)
                .active(1)
                .batchNumber(qualityModel.getBatchNumber())
                .batchSize(qualityModel.getBatchSize())
                .createdDatetime(LocalDateTime.now())
                .updatedDateTime(LocalDateTime.now())
                .category(qualityModel.getCategory())
                .targetQuantity(qualityModel.getTargetQuantity())
                .build();
    }

    private List<OeeProductionLogResponse> fetchProductionLogs(OeeProductionLogRequest logRequest) {
        return webClientBuilder.build()
                .post()
                .uri(totalProducedQuantity)
                .bodyValue(logRequest)
                .retrieve()
                .bodyToFlux(OeeProductionLogResponse.class)
                .collectList()
                .block();
    }

    private boolean isValid(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void publishAggregationInput(OeeOutput oeeOutput) {
        try {
            // Wrap the OeeOutput into an AggregatedOeeRequestDTO
            AggregatedOeeRequestDTO requestDTO = AggregatedOeeRequestDTO.builder()
                    .oeeData(oeeOutput)
                    .build();

            // Prepare the payload for the WebClient if you want additional metadata
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("topicName", "aggreationTopic");
            // Instead of putting the OeeOutput directly, we put the wrapped DTO
            requestPayload.put("payload", requestDTO);

            // Send the request to the integration service using WebClientBuilder
            webClientBuilder.build()
                    .post()
                    .uri(logGenericMessage)
                    .bodyValue(requestPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Published AggregationInput: {}, Response: {}", requestDTO, response))
                    .doOnError(e -> log.error("Failed to publish AggregationInput: {}", requestDTO, e))
                    .block();
        } catch (Exception e) {
            log.error("Exception while publishing PerformanceInput: {}", oeeOutput, e);
        }
    }


}
