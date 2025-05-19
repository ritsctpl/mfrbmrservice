package com.rits.overallequipmentefficiency.service;

import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.AvailabilityEntity;
import com.rits.overallequipmentefficiency.model.PerformanceModel;
import com.rits.overallequipmentefficiency.repository.OeePerformanceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;


@Service("oeeperformanceService")
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final WebClient.Builder webClientBuilder;



    @Value("${productionlog-service.url}/totalProducedQuantity")
    private String totalProducedQuantity;

    @Value("${integration-service.uri}/logGenericMessage")
    private String logGenericMessage;

    @Value("${cycletime-service.url}/calculateCycleTime")
    private String calculateCycleTime;

    private static final Logger log = LoggerFactory.getLogger(PerformanceServiceImpl.class);
    @Autowired
    private OeePerformanceRepository performanceRepository;


    /*@Override
    public List<PerformanceOutput> calculatePerformance(PerformanceInput input) {
        List<PerformanceModel> performanceEntities = new ArrayList<>();
        List<PerformanceOutput> perfomanceOutputs = new ArrayList<>();

        AvailabilityEntity availability = input.getAvailabilityEntity();
        CycleTime cycleTime = input.getCycleTime();

        // Map OeeProductionLogRequest
        OeeProductionLogRequest logRequest = buildProductionLogRequest(availability, cycleTime);

        // Call ProductionLog API for Complete and Done events
        List<OeeProductionLogResponse> responses = fetchProductionLogs(logRequest);

        // Calculate parts to be produced
        double partsToBeProduced = availability.getActualAvaialbleTime() / (cycleTime != null ? cycleTime.getCycleTime() : 1);

        for (OeeProductionLogResponse response : responses) {
            // Calculate performance for grandTotalQty
            double performancePercentage = (partsToBeProduced != 0 ? response.getGrandTotalQty() / partsToBeProduced : 0.0)*100;
            String responseEventType = response.getEventType();
            // Build OeePerformanceEntity for grandTotalQty
            PerformanceModel overallPerformanceEntity = buildPerformanceEntity(
                    availability, cycleTime, response.getGrandTotalQty(),
                    performancePercentage, partsToBeProduced, null,
                    response.getShiftId(), availability.getResourceId(),responseEventType);
            performanceEntities.add(overallPerformanceEntity);

            // Calculate and build for each shop order
            for (OeeProductionLogResponse.ShopOrderBreakdown shopOrder : response.getShopOrderBreakdowns()) {
                double shopOrderPerformance = (partsToBeProduced != 0 ? shopOrder.getTotalQty() / partsToBeProduced : 0.0)*100;

                PerformanceModel shopOrderEntity = buildPerformanceEntity(
                        availability, cycleTime, shopOrder.getTotalQty(),
                        shopOrderPerformance, partsToBeProduced,
                        shopOrder.getShopOrder(), response.getShiftId(),
                        availability.getResourceId(),responseEventType);
                performanceEntities.add(shopOrderEntity);
            }
        }

        // Save all OeePerformanceEntity records
        List<PerformanceModel> savedEntities = performanceRepository.saveAll(performanceEntities);

        // Publish QualityInput and build PerformanceOutput after saving
        for (PerformanceModel savedEntity : savedEntities) {
            PerformanceOutput performanceOutput = buildPerfomanceOutput(savedEntity, availability, cycleTime);
            publishQualityInput(performanceOutput); // Publish the message
            perfomanceOutputs.add(performanceOutput); // Add to output list
        }

        return perfomanceOutputs;
    }*/

 /*   @Override
    public List<PerformanceOutput> calculatePerformance(PerformanceInput input) {
        List<PerformanceModel> performanceEntities = new ArrayList<>();
        List<PerformanceOutput> perfomanceOutputs = new ArrayList<>();

        AvailabilityEntity availability = input.getAvailabilityEntity();
        CycleTime cycleTime = input.getCycleTime();
        String eventBy = input.getEventBy();

        // Map OeeProductionLogRequest
        OeeProductionLogRequest logRequest = buildProductionLogRequest(availability, cycleTime, eventBy);

        // Call ProductionLog API for specific eventType(s)
        List<OeeProductionLogResponse> responses = fetchProductionLogs(logRequest, eventBy);

        // Calculate parts to be produced based on eventBy
        double partsToBeProduced = calculatePartsToBeProduced(availability, cycleTime, eventBy);

        for (OeeProductionLogResponse response : responses) {
            // Calculate performance for grandTotalQty
            if(partsToBeProduced==0){

            }
            double performancePercentage = (partsToBeProduced != 0 ? response.getGrandTotalQty() / partsToBeProduced : 0.0) * 100;

            String responseEventType = response.getEventType();
            double perfromanceEfficiency = ((double) response.getGrandTotalQty() / partsToBeProduced) * 100;
            if(partsToBeProduced==0){
                performancePercentage=100;
                perfromanceEfficiency=100;
            }

            // Build OeePerformanceEntity for grandTotalQty
            PerformanceModel overallPerformanceEntity = buildPerformanceEntity(
                    availability, cycleTime, response.getGrandTotalQty(),
                    performancePercentage, partsToBeProduced, null,
                    response.getShiftId(), availability.getResourceId(), responseEventType,perfromanceEfficiency,"");
            performanceEntities.add(overallPerformanceEntity);

            PerformanceOutput headerPerformanceOutput = buildPerfomanceOutput(overallPerformanceEntity, availability, cycleTime);
            publishQualityInput(headerPerformanceOutput);
            perfomanceOutputs.add(headerPerformanceOutput);

            // Calculate and build for each shop order
            for (OeeProductionLogResponse.ShopOrderBreakdown shopOrder : response.getShopOrderBreakdowns()) {
                double shopOrderPerformance = (partsToBeProduced != 0 ? shopOrder.getTotalQty() / partsToBeProduced : 0.0) * 100;
                 perfromanceEfficiency = ((double) shopOrder.getTotalQty() / partsToBeProduced) * 100;
                PerformanceModel shopOrderEntity = buildPerformanceEntity(
                        availability, cycleTime, shopOrder.getTotalQty(),
                        shopOrderPerformance, partsToBeProduced,
                        shopOrder.getShopOrder(), response.getShiftId(),
                        availability.getResourceId(), responseEventType,perfromanceEfficiency,shopOrder.getBatchNumber());
                performanceEntities.add(shopOrderEntity);

                PerformanceOutput performanceOutput = buildPerfomanceOutput(shopOrderEntity, availability, cycleTime);
                publishQualityInput(performanceOutput);
                perfomanceOutputs.add(performanceOutput);
            }
        }

        // Save all OeePerformanceEntity records
        List<PerformanceModel> savedEntities = performanceRepository.saveAll(performanceEntities);

        return perfomanceOutputs;
    }*/
 @Override
 public List<PerformanceOutput> calculatePerformance(PerformanceInput input) {
     List<PerformanceModel> performanceEntities = new ArrayList<>();
     List<PerformanceOutput> performanceOutputs = new ArrayList<>();

     AvailabilityEntity availability = input.getAvailabilityEntity();
     CycleTime cycleTime = input.getCycleTime();

     if (isCycleTimeInvalid(cycleTime)) {
         return calculatePerformanceByItemAndItemVersion(input);
     }

     String eventBy = input.getEventBy();

     // Map OeeProductionLogRequest
     OeeProductionLogRequest logRequest = buildProductionLogRequest(availability, cycleTime, eventBy);

     // Call ProductionLog API for specific eventType(s)
     List<OeeProductionLogResponse> responses = fetchProductionLogs(logRequest, eventBy);

     // Calculate parts to be produced based on eventBy
     double partsToBeProduced = calculatePartsToBeProduced(availability, cycleTime, eventBy);

     for (OeeProductionLogResponse response : responses) {
         double performancePercentage = (partsToBeProduced >0 ? response.getGrandTotalQty() / partsToBeProduced : 0.0) * 100;
         double performanceEfficiency;
         if (partsToBeProduced > 0) {
             performanceEfficiency = ((double) response.getGrandTotalQty() / partsToBeProduced) * 100;
         } else {
             // Handle the zero case: if there's no planned production, you might decide performance is 0%
             performanceEfficiency = 0;
         }


         if (partsToBeProduced == 0) {
             performancePercentage = 100;
             performanceEfficiency = 100;
         }


         // Build OeePerformanceEntity for grandTotalQty
         PerformanceModel overallPerformanceEntity = buildPerformanceEntity(
                 availability, cycleTime, response.getGrandTotalQty(),
                 performancePercentage, partsToBeProduced, null,
                 availability.getShiftId(), availability.getResourceId(), response.getEventType(),
                 performanceEfficiency, "","",input.getEventBy());

         performanceEntities.add(overallPerformanceEntity);

         // Calculate and build for each shop order
         for (OeeProductionLogResponse.ShopOrderBreakdown shopOrder : response.getShopOrderBreakdowns()) {
             double shopOrderPerformance = (partsToBeProduced > 0 ? shopOrder.getTotalQty() / partsToBeProduced : 0.0) * 100;
             performanceEfficiency = ((double) shopOrder.getTotalQty() / partsToBeProduced) * 100;

             PerformanceModel shopOrderEntity = buildPerformanceEntity(
                     availability, cycleTime, shopOrder.getTotalQty(),
                     shopOrderPerformance, partsToBeProduced,
                     shopOrder.getShopOrder(), availability.getShiftId(),
                     availability.getResourceId(), response.getEventType(),
                     performanceEfficiency, shopOrder.getBatchNumber(),shopOrder.getBatchSize(),input.getEventBy());

             performanceEntities.add(shopOrderEntity);
         }
     }

     // **Step 1: Save all PerformanceModel records**
     List<PerformanceModel> savedEntities = performanceRepository.saveAll(performanceEntities);

     // **Step 2: Build and publish PerformanceOutput after saving**
     for (PerformanceModel savedEntity : savedEntities) {
         PerformanceOutput performanceOutput = buildPerfomanceOutput(savedEntity, availability, cycleTime);
         publishQualityInput(performanceOutput);
         performanceOutputs.add(performanceOutput);
     }

     return performanceOutputs;
 }

    private double calculatePartsToBeProduced(AvailabilityEntity availability, CycleTime cycleTime, String eventBy) {
        if (availability == null || cycleTime == null || eventBy == null) {
            return 0; // return 0 or handle invalid input case
        }

        double actualAvailableTime = availability.getActualAvailableTime();
        double quantityProduced = 0;

        // Retrieve the target quantity for the specific record from cycleTime
        Double targetQuantity = cycleTime.getTargetQuantity();  // Assuming target quantity is stored here


            if ("RESOURCE".equalsIgnoreCase(eventBy)) {
                double cycleTimeValue = cycleTime.getCycleTime();
                if (cycleTimeValue > 0) {
                    quantityProduced = availability.getActualAvailableTime()/(cycleTime!=null? cycleTime.getCycleTime() : 1);
                }
            } else if ("WORKCENTER".equalsIgnoreCase(eventBy)) {
                double manufacturedTime = cycleTime.getManufacturedTime();
                if (manufacturedTime > 0) {
                    quantityProduced=availability.getActualAvailableTime()/(cycleTime!=null? cycleTime.getManufacturedTime() : 1);
                }
            }




        return quantityProduced; // Return the calculated quantity produced
    }

    private double calculateTargetQuantity(AvailabilityEntity availability, CycleTime cycleTime, String eventBy) {
        if (availability == null || cycleTime == null || eventBy == null) {
            return 0;
        }
        double actualAvailableTime = availability.getActualAvailableTime();
        double downtime = availability.getDowntime();
        double targetQuantity=0.0;
        if ("RESOURCE".equalsIgnoreCase(eventBy)) {
            double cycleTimeValue = cycleTime.getCycleTime();
            if (cycleTimeValue > 0) {
                targetQuantity = (actualAvailableTime + downtime) / cycleTimeValue;
            }
        } else if ("WORKCENTER".equalsIgnoreCase(eventBy)) {
            double manufacturedTime = cycleTime.getManufacturedTime();
            if (manufacturedTime > 0) {
                targetQuantity = (actualAvailableTime + downtime) / manufacturedTime;
            }
        }
        return targetQuantity;
    }



    private PerformanceModel buildPerformanceEntity(
            AvailabilityEntity availability, CycleTime cycleTime,
            double actualOutput, double performancePercentage,
            double plannedOutput, String shopOrder, String shiftId,
            String resourceId, String eventType,Double perfromanceEfficiency,String batchnumber,String batchSize,String eventBy) {

        // Determine the planned cycle time based on eventType
        double plannedCycleTime;
        if ("Complete".equalsIgnoreCase(eventType) || "completeSfcBatch".equalsIgnoreCase(eventType)) {
            plannedCycleTime = cycleTime != null ? cycleTime.getCycleTime() : 0;
        } else if ("Done".equalsIgnoreCase(eventType) || "doneSfcBatch".equalsIgnoreCase(eventType)) {
            plannedCycleTime = cycleTime != null ? cycleTime.getManufacturedTime() : 0;
        } else {
            plannedCycleTime = 0; // Default value if eventType does not match any condition
        }

        return PerformanceModel.builder()
                .site(availability.getSite())
                .shiftId(availability.getShiftId() != null ? availability.getShiftId() : null)
                .resourceId(resourceId != null ? resourceId : availability.getResourceId())
                .workcenterId(cycleTime != null ? cycleTime.getWorkCenterId() : null)
                .item(cycleTime != null ? cycleTime.getItem() : null)
                .itemVersion(cycleTime != null ? cycleTime.getItemVersion() : null)
                .operation(cycleTime != null ? cycleTime.getOperationId() : null)
                .operationVersion(cycleTime != null ? cycleTime.getOperationVersion() : null)
                .eventType(eventType)
                .downtimeDuration(availability.getDowntime())
                .availabilityId(availability.getId())
                .plannedOutput(plannedOutput)
                .plannedCycleTime(plannedCycleTime)
                .actualOutput(actualOutput)
                .performancePercentage(performancePercentage)
                .shopOrderBO(shopOrder)
                .intervalStartDateTime(availability.getIntervalStartDateTime())
                .batchNumber(batchnumber)
                .batchSize(batchSize)
                .intervalEndDateTime(availability.getIntervalEndDateTime())
                .createdDatetime(LocalDateTime.now())
                .performanceEfficiency(perfromanceEfficiency)
                .category(availability.getCategory())
                .targetQuantity(calculateTargetQuantity(availability, cycleTime, eventBy))
                .active(1)
                .build();
    }


   /* private OeeProductionLogRequest buildProductionLogRequest(AvailabilityEntity availability, CycleTime cycleTime) {
        OeeProductionLogRequest logRequest = new OeeProductionLogRequest();
        logRequest.setSite(availability.getSite());
        logRequest.setResourceId(availability.getResourceId());
        logRequest.setIntervalStartDateTime(availability.getIntervalStartDateTime());
        logRequest.setIntervalEndDateTime(availability.getIntervalEndDateTime());

        if (cycleTime != null) {
            if (isValid(cycleTime.getItemId())) {
                logRequest.setItemId(cycleTime.getItemId());
            }
            if (isValid(cycleTime.getItemVersion())) {
                logRequest.setItemVersion(cycleTime.getItemVersion());
            }
            if (isValid(cycleTime.getWorkCenterId())) {
                logRequest.setWorkcenterId(cycleTime.getWorkCenterId());
            }
            if (isValid(cycleTime.getOperationId())) {
                logRequest.setOperationId(cycleTime.getOperationId());
            }
            if (isValid(cycleTime.getOperationVersion())) {
                logRequest.setOperationVersion(cycleTime.getOperationVersion());
            }
        }

        return logRequest;
    }*/
   private OeeProductionLogRequest buildProductionLogRequest(AvailabilityEntity availability, CycleTime cycleTime, String eventBy) {
       OeeProductionLogRequest logRequest = new OeeProductionLogRequest();

       logRequest.setSite(availability.getSite());
       logRequest.setIntervalStartDateTime(availability.getIntervalStartDateTime());
       logRequest.setIntervalEndDateTime(availability.getIntervalEndDateTime());

       if ("RESOURCE".equalsIgnoreCase(eventBy)) {
           logRequest.setResourceId(availability.getResourceId());

           if (cycleTime != null) {
               if (isValid(cycleTime.getItem())) {
                   logRequest.setItemId(cycleTime.getItem());
               }
               if (isValid(cycleTime.getItemVersion())) {
                   logRequest.setItemVersion(cycleTime.getItemVersion());
               }
               if (isValid(cycleTime.getWorkCenterId())) {
                   logRequest.setWorkcenterId(cycleTime.getWorkCenterId());
               }
               if (isValid(cycleTime.getOperationId())) {
                   logRequest.setOperationId(cycleTime.getOperationId());
               }
               if (isValid(cycleTime.getOperationVersion())) {
                   logRequest.setOperationVersion(cycleTime.getOperationVersion());
               }
               /*if (isValid(availability.getShiftId())) {
                   logRequest.setShiftId(availability.getShiftId());
               }*/

           }

       } else if ("WORKCENTER".equalsIgnoreCase(eventBy)) {
           logRequest.setWorkcenterId(availability.getWorkcenterId());
           if (isValid(cycleTime.getItem())) {
               logRequest.setItemId(cycleTime.getItem());
           }
           if (isValid(cycleTime.getItemVersion())) {
               logRequest.setItemVersion(cycleTime.getItemVersion());
           }
           if (isValid(cycleTime.getOperationId())) {
               logRequest.setOperationId(cycleTime.getOperationId());
           }
           if (isValid(cycleTime.getOperationVersion())) {
               logRequest.setOperationVersion(cycleTime.getOperationVersion());
           }
           /*if (isValid(availability.getShiftId())) {
               logRequest.setShiftId(availability.getShiftId());
           }*/
       }

       return logRequest;
   }
    private boolean isValid(String value) {
        return value != null && !value.trim().isEmpty() && !value.equals("");
    }


  /*  private List<OeeProductionLogResponse> fetchProductionLogs(OeeProductionLogRequest logRequest) {
        List<OeeProductionLogResponse> responses = new ArrayList<>();

        for (String eventType : List.of("Complete", "Done")) {
            logRequest.setEventType(eventType);

            List<OeeProductionLogResponse> responseList = webClientBuilder.build()
                    .post()
                    .uri(totalProducedQuantity)
                    .bodyValue(logRequest)
                    .retrieve()
                    .bodyToFlux(OeeProductionLogResponse.class) // Changed to handle List response
                    .collectList() // Collect the Flux into a List
                    .block(); // Block to wait for the result

            if (responseList != null && !responseList.isEmpty()) {
                responses.addAll(responseList); // Add all responses to the main list
            }
        }

        return responses;
    }*/

    private List<OeeProductionLogResponse> fetchProductionLogs(OeeProductionLogRequest logRequest, String eventBy) {
        List<OeeProductionLogResponse> responses = new ArrayList<>();

       // List<String> eventTypes = "RESOURCE".equalsIgnoreCase(eventBy) ? List.of("Complete") : List.of("Done");
        List<String> eventTypes;
        if ("RESOURCE".equalsIgnoreCase(eventBy)) {
            eventTypes = List.of("completeSfcBatch", "doneSfcBatch","signOffCmp", "machineCompleteSfcBatch", "machineDoneSfcBatch");
        } else {
            eventTypes = List.of("doneSfcBatch", "machineDoneSfcBatch");
        }

        for (String eventType : eventTypes) {
            logRequest.setEventType(eventType);

            List<OeeProductionLogResponse> responseList = webClientBuilder.build()
                    .post()
                    .uri(totalProducedQuantity)
                    .bodyValue(logRequest)
                    .retrieve()
                    .bodyToFlux(OeeProductionLogResponse.class) // Changed to handle List response
                    .collectList() // Collect the Flux into a List
                    .block(); // Block to wait for the result

            // Add a default response if the API call returns null or empty
            if (responseList == null || responseList.isEmpty()) {
               // if ((("RESOURCE".equalsIgnoreCase(eventBy)) && (eventType.equalsIgnoreCase("completeSfcBatch"))) ||
               //         !("RESOURCE".equalsIgnoreCase(eventBy)) || (("RESOURCE".equalsIgnoreCase(eventBy)) && (eventType.equalsIgnoreCase("machineCompleteSfcBatch"))))
              //  {
                OeeProductionLogResponse defaultResponse = new OeeProductionLogResponse();
                defaultResponse.setShiftId(null); // Or "N/A"
                defaultResponse.setGrandTotalQty(0);
                defaultResponse.setEventType(eventType);
                defaultResponse.setShopOrderBreakdowns(Collections.emptyList());
                responses.add(defaultResponse);
               // }
            } else {
                responses.addAll(responseList);
            }

            /*if (responseList != null && !responseList.isEmpty()) {
                responses.addAll(responseList);
            }*/
        }

        return responses;
    }
/*private List<OeeProductionLogResponse> fetchProductionLogs(OeeProductionLogRequest logRequest, String eventBy) {
    List<OeeProductionLogResponse> responses = new ArrayList<>();

    // Fetch both "completeSfcBatch" and "doneSfcBatch" events
    List<String> eventTypes = List.of("completeSfcBatch", "doneSfcBatch");

    for (String eventType : eventTypes) {
        logRequest.setEventType(eventType);

        List<OeeProductionLogResponse> responseList = webClientBuilder.build()
                .post()
                .uri(totalProducedQuantity)
                .bodyValue(logRequest)
                .retrieve()
                .bodyToFlux(OeeProductionLogResponse.class)
                .collectList()
                .block(); // Blocking to wait for response

        // Ensure we add a default response if API returns null or empty
        if (responseList == null || responseList.isEmpty()) {
            OeeProductionLogResponse defaultResponse = new OeeProductionLogResponse();
            defaultResponse.setShiftId(null); // Or "N/A"
            defaultResponse.setGrandTotalQty(0);
            defaultResponse.setEventType(eventType);
            defaultResponse.setShopOrderBreakdowns(Collections.emptyList());
            responses.add(defaultResponse);
        } else {
            responses.addAll(responseList);
        }
    }

    return responses;
}*/


    private PerformanceOutput buildPerfomanceOutput(PerformanceModel performanceEntity, AvailabilityEntity availability, CycleTime cycleTime) {
        PerformanceOutput output = new PerformanceOutput();
        output.setPerformanceEntity(performanceEntity);
        output.setAvailabilityEntity(availability);
        output.setCycleTime(cycleTime);
        return output;
    }

    private void publishQualityInput(PerformanceOutput performanceOuput) {

        try {
            // Prepare the payload for the WebClient
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("topicName", "qualityTopic");
            requestPayload.put("payload", performanceOuput); // Directly map PerformanceInput to payload

            // Send the request to the integration-service using WebClientBuilder
            webClientBuilder.build()
                    .post()
                    .uri(logGenericMessage)
                    .bodyValue(requestPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Published PerformanceInput: {}, Response: {}", performanceOuput, response))
                    .doOnError(e -> log.error("Failed to publish PerformanceInput: {}", performanceOuput, e))
                    .block();
        } catch (Exception e) {
            log.error("Exception while publishing PerformanceInput: {}", performanceOuput, e);
        }
    }

    /**
     * New branch when cycleTime is invalid.
     * <p>
     * This method uses the PerformanceInput’s item and itemVersion (instead of the cycleTime)
     * to call the totalProducedQuantity API and then retrieves a base cycle time via the cycle time service.
     * For each shop order breakdown, it calls the cycle time service using a DTO that is built conditionally
     * (only setting values that are non-null/non-empty) and processes both RESOURCE and WORKCENTER scenarios.
     */
    private List<PerformanceOutput> calculatePerformanceByItemAndItemVersion(PerformanceInput input) {
        List<PerformanceModel> performanceEntities = new ArrayList<>();
        List<PerformanceOutput> performanceOutputs = new ArrayList<>();

        AvailabilityEntity availability = input.getAvailabilityEntity();
        String eventBy = input.getEventBy();

        // Build production log request using item and itemVersion from PerformanceInput
        OeeProductionLogRequest logRequest = buildProductionLogRequestForItem(input);
        List<OeeProductionLogResponse> responses = fetchProductionLogs(logRequest, eventBy);

        // Determine the eventType for cycle time service calls.
        String eventTypeForCycleTime = "RESOURCE".equalsIgnoreCase(eventBy)
                ? "completeSfcBatch" : "doneSfcBatch";

        // Build header DTO using the input’s item and itemVersion
        ProductionLogDto headerDto = new ProductionLogDto();
        headerDto.setSite(availability.getSite());
        if ("RESOURCE".equalsIgnoreCase(eventBy)) {
            if (isValid(availability.getResourceId())) {
                headerDto.setResource_id(availability.getResourceId());
            }
        } else {
            if (isValid(availability.getWorkcenterId())) {
                headerDto.setWorkcenter_id(availability.getWorkcenterId());
            }
        }
        /*if (isValid(input.getItem())) {
            headerDto.setItem(input.getItem());
        }
        if (isValid(input.getItemVersion())) {
            headerDto.setItem_version(input.getItemVersion());
        }*/
        /*headerDto.setEventType(eventTypeForCycleTime);


        // Call cycle time service for header to get baseCycleTime
        double baseCycleTime = callCycleTimeService(headerDto);
        headerDto.setShift_id(availability.getShiftId()); // Only for header DTO
        // Cache for shop order cycle time lookups to avoid duplicate calls
        Map<String, Double> shopOrderCycleTimeCache = new HashMap<>();

        // Calculate header planned output using the baseCycleTime.
        double partsToBeProducedHeader = baseCycleTime > 0
                ? availability.getActualAvailableTime() / baseCycleTime : 0;*/

        // Process each production log response.
        for (OeeProductionLogResponse response : responses) {

            headerDto.setEventType(response.getEventType());


            // Call cycle time service for header to get baseCycleTime
            double baseCycleTime = callCycleTimeService(headerDto);
            headerDto.setShift_id(availability.getShiftId()); // Only for header DTO
            // Cache for shop order cycle time lookups to avoid duplicate calls
            Map<String, Double> shopOrderCycleTimeCache = new HashMap<>();

            // Calculate header planned output using the baseCycleTime.
            double partsToBeProducedHeader = baseCycleTime > 0
                    ? availability.getActualAvailableTime() / baseCycleTime : 0;

            double performancePercentageHeader = partsToBeProducedHeader > 0
                    ? (response.getGrandTotalQty() / partsToBeProducedHeader) * 100 : 0;
            double performanceEfficiencyHeader = partsToBeProducedHeader > 0
                    ? (response.getGrandTotalQty() / partsToBeProducedHeader) * 100 : 0;
            if (partsToBeProducedHeader == 0) {
                performancePercentageHeader = 100;
                performanceEfficiencyHeader = 100;
            }

            // Build header PerformanceModel record.
            String headerResource = "RESOURCE".equalsIgnoreCase(eventBy)
                    ? availability.getResourceId() : "";
            String headerWorkcenter = "WORKCENTER".equalsIgnoreCase(eventBy)
                    ? availability.getWorkcenterId() : "";
            PerformanceModel headerModel = PerformanceModel.builder()
                    .site(availability.getSite())
                    .shiftId(availability.getShiftId())
                    .resourceId(availability.getResourceId())
                    .workcenterId(availability.getWorkcenterId())
                    //.item(input.getItem())
                   // .itemVersion(input.getItemVersion())
                    .plannedCycleTime(baseCycleTime)
                    .plannedOutput(partsToBeProducedHeader)
                    .actualOutput(response.getGrandTotalQty())
                    .performancePercentage(performancePercentageHeader)
                    .performanceEfficiency(performanceEfficiencyHeader)
                    .downtimeDuration(availability.getDowntime())
                    .availabilityId(availability.getId())
                    .intervalStartDateTime(availability.getIntervalStartDateTime())
                    .intervalEndDateTime(availability.getIntervalEndDateTime())
                    .createdDatetime(LocalDateTime.now())
                    .eventType(response.getEventType())
                    .category(availability.getCategory())
                    .targetQuantity(baseCycleTime > 0 ? (availability.getActualAvailableTime() + availability.getDowntime()) / baseCycleTime : 0)
                    .active(1)
                    .build();
            performanceEntities.add(headerModel);

            // Process each shop order breakdown.
            for (OeeProductionLogResponse.ShopOrderBreakdown breakdown : response.getShopOrderBreakdowns()) {
                // Build a unique key for caching based on combination values.
                String key = (availability.getResourceId() != null ? availability.getResourceId() : "") + "_" +
                        (breakdown.getItemId() != null ? breakdown.getItemId() : "") + "_" +
                        (breakdown.getItemVersion() != null ? breakdown.getItemVersion() : "") + "_" +
                        (breakdown.getOperation() != null ? breakdown.getOperation() : "") + "_" +
                        (breakdown.getOperationVersion() != null ? breakdown.getOperationVersion() : "");
                double usedCycleTime = baseCycleTime;
                if (isValid(breakdown.getOperation()) && isValid(breakdown.getOperationVersion())
                        && isValid(breakdown.getItemId()) && isValid(breakdown.getItemVersion())) {
                    if (shopOrderCycleTimeCache.containsKey(key)) {
                        usedCycleTime = shopOrderCycleTimeCache.get(key);
                    } else {
                        ProductionLogDto shopOrderDto = new ProductionLogDto();
                        // Always set site
                        shopOrderDto.setSite(availability.getSite());
                        // For RESOURCE or WORKCENTER, set the corresponding id if valid.
                        if ("RESOURCE".equalsIgnoreCase(eventBy)) {
                            if (isValid(availability.getResourceId())) {
                                shopOrderDto.setResource_id(availability.getResourceId());
                            }
                        } else {
                            if (isValid(availability.getWorkcenterId())) {
                                shopOrderDto.setWorkcenter_id(availability.getWorkcenterId());
                            }
                        }
                        if (isValid(breakdown.getItemId())) {
                            shopOrderDto.setItem(breakdown.getItemId());
                        }
                        if (isValid(breakdown.getItemVersion())) {
                            shopOrderDto.setItem_version(breakdown.getItemVersion());
                        }
                        if (isValid(breakdown.getOperation())) {
                            shopOrderDto.setOperation(breakdown.getOperation());
                        }
                        if (isValid(breakdown.getOperationVersion())) {
                            shopOrderDto.setOperation_version(breakdown.getOperationVersion());
                        }
                        // Set eventType always.
                        shopOrderDto.setEventType(response.getEventType());
                        // Note: Do not set shift_id for shop order DTO.

                        double shopOrderCycleTime = callCycleTimeService(shopOrderDto);
                        usedCycleTime = shopOrderCycleTime > 0 ? shopOrderCycleTime : baseCycleTime;
                        shopOrderCycleTimeCache.put(key, usedCycleTime);
                    }
                }
                double partsToBeProducedShopOrder = usedCycleTime > 0
                        ? availability.getActualAvailableTime() / usedCycleTime : 0;
                double performancePercentageShopOrder = partsToBeProducedShopOrder > 0
                        ? (breakdown.getTotalQty() / partsToBeProducedShopOrder) * 100 : 0;
                double performanceEfficiencyShopOrder = partsToBeProducedShopOrder > 0
                        ? (breakdown.getTotalQty() / partsToBeProducedShopOrder) * 100 : 0;
                if (partsToBeProducedShopOrder == 0) {
                    performancePercentageShopOrder = 100;
                    performanceEfficiencyShopOrder = 100;
                }

                String headerRes = "RESOURCE".equalsIgnoreCase(eventBy)
                        ? availability.getResourceId() : "";
                String headerWc =  "WORKCENTER".equalsIgnoreCase(eventBy)
                        ? availability.getWorkcenterId() : "";
                PerformanceModel shopOrderModel = PerformanceModel.builder()
                        .site(availability.getSite())
                        .shiftId(availability.getShiftId())
                        .resourceId(availability.getResourceId())
                        .workcenterId(availability.getWorkcenterId())
                        .plannedCycleTime(usedCycleTime)
                        .plannedOutput(partsToBeProducedShopOrder)
                        .actualOutput(breakdown.getTotalQty())
                        .performancePercentage(performancePercentageShopOrder)
                        .performanceEfficiency(performanceEfficiencyShopOrder)
                        .shopOrderBO(breakdown.getShopOrder())
                        .batchNumber(breakdown.getBatchNumber())
                        .batchSize(breakdown.getBatchSize())
                        .downtimeDuration(availability.getDowntime())
                        .availabilityId(availability.getId())
                        .intervalStartDateTime(availability.getIntervalStartDateTime())
                        .intervalEndDateTime(availability.getIntervalEndDateTime())
                        .createdDatetime(LocalDateTime.now())
                        .eventType(response.getEventType())
                        .category(availability.getCategory())
                        // Also set operation and item details from the breakdown.
                        .operation(breakdown.getOperation())
                        .operationVersion(breakdown.getOperationVersion())
                        .item(breakdown.getItemId())
                        .itemVersion(breakdown.getItemVersion())
                        .targetQuantity(usedCycleTime > 0 ? (availability.getActualAvailableTime() + availability.getDowntime()) / usedCycleTime : 0)
                        .active(1)
                        .build();
                performanceEntities.add(shopOrderModel);
            }
        }
        // -------------------------------------------------------
        // New Overall Performance Calculation for resource/workcenter.
        // -------------------------------------------------------

        // Build a minimal production log request using only resource/workcenter details.
        /*OeeProductionLogRequest overallLogRequest = new OeeProductionLogRequest();
        overallLogRequest.setSite(availability.getSite());
        overallLogRequest.setIntervalStartDateTime(availability.getIntervalStartDateTime());
        overallLogRequest.setIntervalEndDateTime(availability.getIntervalEndDateTime());
        if ("RESOURCE".equalsIgnoreCase(eventBy)) {
            overallLogRequest.setResourceId(availability.getResourceId());
        } else {
            overallLogRequest.setWorkcenterId(availability.getWorkcenterId());
        }
        overallLogRequest.setEventType("RESOURCE".equalsIgnoreCase(eventBy)
                ? "completeSfcBatch" : "doneSfcBatch");

        // Call fetchProductionLogs with the minimal request to get the overall header response.
        List<OeeProductionLogResponse> overallResponses = fetchProductionLogs(overallLogRequest, eventBy);
        OeeProductionLogResponse overallHeaderResponse = overallResponses.isEmpty() ? null : overallResponses.get(0);

        // Build a minimal ProductionLogDto (only eventType, site, and resource/workcenter).
        ProductionLogDto overallDto = new ProductionLogDto();
        overallDto.setSite(availability.getSite());
        overallDto.setEventType("RESOURCE".equalsIgnoreCase(eventBy)
                ? "completeSfcBatch" : "doneSfcBatch");
        if ("RESOURCE".equalsIgnoreCase(eventBy)) {
            overallDto.setResource_id(availability.getResourceId());
        } else {
            overallDto.setWorkcenter_id(availability.getWorkcenterId());
        }

        // Call cycle time service with the minimal DTO.
        double overallCycleTime = callCycleTimeService(overallDto);
        double overallPlannedOutput = overallCycleTime > 0
                ? availability.getActualAvailableTime() / overallCycleTime : 0;
        double overallActualOutput = overallHeaderResponse != null ? overallHeaderResponse.getGrandTotalQty() : 0;
        double overallPerformancePercentage = overallPlannedOutput > 0
                ? (overallActualOutput / overallPlannedOutput) * 100 : 100;
        double overallPerformanceEfficiency = overallPerformancePercentage; // Same formula.
        String headerRes = "RESOURCE".equalsIgnoreCase(eventBy)
                ? availability.getResourceId() : "";
        String headerWc =  "WORKCENTER".equalsIgnoreCase(eventBy)
                ? availability.getWorkcenterId() : "";
        PerformanceModel overallPerformanceModel = PerformanceModel.builder()
                    .site(availability.getSite())
                    .shiftId(availability.getShiftId())
                    .resourceId(availability.getResourceId())
                    .workcenterId(availability.getWorkcenterId())
                    .plannedCycleTime(overallCycleTime)
                    .plannedOutput(overallPlannedOutput)
                    .actualOutput(overallActualOutput)
                    .performancePercentage(overallPerformancePercentage)
                    .performanceEfficiency(overallPerformanceEfficiency)
                    .downtimeDuration(availability.getDowntime())
                    .availabilityId(availability.getId())
                    .intervalStartDateTime(availability.getIntervalStartDateTime())
                    .intervalEndDateTime(availability.getIntervalEndDateTime())
                    .createdDatetime(LocalDateTime.now())
                    .eventType(overallLogRequest.getEventType())
                    .category(availability.getCategory())
                    .active(1)
                    .build();

        performanceEntities.add(overallPerformanceModel);*/
        // -------------------------------------------------------
        // Save and publish PerformanceModel records.
        List<PerformanceModel> savedEntities = performanceRepository.saveAll(performanceEntities);
        for (PerformanceModel savedEntity : savedEntities) {
            PerformanceOutput performanceOutput = buildPerfomanceOutput(savedEntity, availability, input.getCycleTime());
            publishQualityInput(performanceOutput);
            performanceOutputs.add(performanceOutput);
        }
        return performanceOutputs;
    }

    /**
     * Helper to determine if the provided cycleTime is invalid (null or missing item value).
     */
    private boolean isCycleTimeInvalid(CycleTime cycleTime) {
        return (cycleTime == null || !isValid(cycleTime.getItem()));
    }

    /**
     * Build a production log request using the item and itemVersion from PerformanceInput.
     */
    private OeeProductionLogRequest buildProductionLogRequestForItem(PerformanceInput input) {
        AvailabilityEntity availability = input.getAvailabilityEntity();
        OeeProductionLogRequest logRequest = new OeeProductionLogRequest();
        logRequest.setSite(availability.getSite());
        logRequest.setIntervalStartDateTime(availability.getIntervalStartDateTime());
        logRequest.setIntervalEndDateTime(availability.getIntervalEndDateTime());
        String eventBy = input.getEventBy();
        if ("RESOURCE".equalsIgnoreCase(eventBy)) {
            logRequest.setResourceId(availability.getResourceId());
        } else if ("WORKCENTER".equalsIgnoreCase(eventBy)) {
            logRequest.setWorkcenterId(availability.getWorkcenterId());
        }
        if (isValid(input.getItem())) {
            logRequest.setItemId(input.getItem());
        }
        if (isValid(input.getItemVersion())) {
            logRequest.setItemVersion(input.getItemVersion());
        }
        // Set the eventType based on eventBy.
        logRequest.setEventType("RESOURCE".equalsIgnoreCase(eventBy)
                ? "completeSfcBatch" : "doneSfcBatch");
        return logRequest;
    }

    /**
     * Call the cycle time service using a ProductionLogDto.
     * This method is used both for header and shop order DTOs.
     */
    private double callCycleTimeService(ProductionLogDto dto) {
        try {
            return webClientBuilder.build()
                    .post()
                    .uri(calculateCycleTime)
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(Double.class)
                    .block();
        } catch (Exception e) {
            log.error("Error calling cycle time service for dto: {}", dto, e);
            return 0;
        }
    }





}
