package com.rits.performanceservice.service;

import com.rits.downtimeservice.model.DownTimeAvailability;
import com.rits.oeeservice.model.Oee;
import com.rits.performanceservice.dto.*;
import com.rits.performanceservice.exception.PerformanceException;
import com.rits.performanceservice.model.Performance;
import com.rits.performanceservice.repository.PerformanceRepository;
import com.rits.qualityservice.dto.QualityRequestList;
import com.rits.qualityservice.service.QualityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PerformanceServiceImpl implements PerformanceService {
    private final WebClient.Builder webClientBuilder;
    private final PerformanceRepository performanceRepository;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final QualityService qualityService;
    @Value("${productionlog.url}/getTotalQuantityWithFields")
    private String getTotalQtyUrl;
    @Value("${productionlog.url}/getTotalQuantityForDoneWithFields")
    private String getTotalQtyForDoneUrl;
    @Value("${productionlog.url}/getTotalQuantityForDoneByMaterialResource")
    private String getTotalQtyForDoneBYmaterialUrl;
    @Value("${cycletime-service.url}/getIdealCycleTime")
    private String getIdealCycleTimeUrl;

    @Override
    public PerformanceRequestList calculatePerformance(PerformanceRequestList performanceRequestList) {
        List<Performance> performanceResponse = new ArrayList<>();
        PerformanceRequestList temp = performanceRequestList;
        List<DownTime> performanceResponseList = new ArrayList<>();

        if (performanceRequestList == null || performanceRequestList.getPerformanceRequestList() == null || performanceRequestList.getPerformanceRequestList().isEmpty()) {
            //call availability and get request and .
            //return calculatePerformance(); with that api output
        } else {
            for (DownTime availability : performanceRequestList.getPerformanceRequestList()) {
                List<ProductionLogRequest> productionLogRequests= getCombination(availability.getCombinations());
                if(productionLogRequests.isEmpty()){
                    ProductionLogRequest productionLogRequest=new ProductionLogRequest();
                    productionLogRequest.setResource(availability.getResourceId());
                    productionLogRequests.add(productionLogRequest);
                }
                for (ProductionLogRequest productionLogRequest : productionLogRequests) {
                    if(productionLogRequest.getResource()!=null && !productionLogRequest.getResource().isEmpty() && (productionLogRequest.getResource().equals(availability.getResourceId())|| productionLogRequest.getResource().equals("*"))) {
                       boolean isStar=false;
                        if(productionLogRequest.getResource().equals("*")){
                            isStar=true;
                        }
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                        LocalDateTime createdDateTime = availability.getCreatedDateTime();
                        LocalDateTime shiftStartTime = parseToLocalDateTime(availability.getShiftStartDate(), formatter, createdDateTime);


                        productionLogRequest.setSite(availability.getSite());
                        productionLogRequest.setShift(availability.getShift());
                        productionLogRequest.setStartTime(shiftStartTime);
                        productionLogRequest.setEndTime(createdDateTime);
                        productionLogRequest.setResource(availability.getResourceId());
//                        ProductionLogRequest productionLogRequest = new ProductionLogRequest(shiftStartTime, createdDateTime, availability.getResourceId(), availability.getSite(), availability.getShift(), null, null, null, null);
                        List<ProductionQueryResponse> totalQtyGroupByItemResRoutOp = getTotalQty(productionLogRequest);
                        //iterate thru given output andusing that item,routing,res,operation find ideal cycle time..
                        boolean hasComplete = false;
                        boolean hasDone = false;
                        for (ProductionQueryResponse productionQueryResponse : totalQtyGroupByItemResRoutOp) {
                            hasComplete = true;
                            String tags= getTags(productionLogRequest,isStar);
                            String [] tagSplitted=tags.split("-");
                            if(tagSplitted!=null&&tagSplitted.length>=2){
                                availability.setTags(tags);
                            }
                            if (productionQueryResponse.getItemBO()!=null&&!productionQueryResponse.getItemBO().isEmpty()){
                            String itemBo[] = productionQueryResponse.getItemBO().split(",");
                            productionQueryResponse.setMaterial(itemBo[1]);
                            productionQueryResponse.setMaterialVersion(itemBo[2]);}
                            if (productionQueryResponse.getRouterBO()!=null&&!productionQueryResponse.getRouterBO().isEmpty()) {

                                String routingBO[] = productionQueryResponse.getRouterBO().split(",");
                                productionQueryResponse.setRouting(routingBO[1]);
                                productionQueryResponse.setRoutingVersion(routingBO[2]);
                            }
                                if (productionQueryResponse.getResourceBO()!=null&&!productionQueryResponse.getResourceBO().isEmpty()) {

                                    String resourceBo[] = productionQueryResponse.getResourceBO().split(",");
                                    productionQueryResponse.setResource(resourceBo[1]);
                                }
                                    if (productionQueryResponse.getOperationBO()!=null&&!productionQueryResponse.getOperationBO().isEmpty()) {

                                        String operationBo[] = productionQueryResponse.getOperationBO().split(",");
                                        productionQueryResponse.setOperation(operationBo[1]);
                                        productionQueryResponse.setOperationVersion(operationBo[2]);
                                    }
                            productionQueryResponse.setSite(availability.getSite());

                            CycleTime cycleTime = getIdealCycleTime(productionQueryResponse);
                            double idealCycleTime = cycleTime.getCycleTime();
                            double actualToBeProducedTime = idealCycleTime * productionQueryResponse.getTotalQtyCompleted();
                            double operatingTime = availability.getOperatingTime();
                            double performance = (actualToBeProducedTime / operatingTime) * 100;
                            double toBeProduced = availability.getOperatingTime() / idealCycleTime;
                            double speedLoss;
                            if (toBeProduced > productionQueryResponse.getTotalActualCycleTime()) {
                                speedLoss = ((double) (toBeProduced - productionQueryResponse.getTotalActualCycleTime())) / toBeProduced;
                            } else {
                                speedLoss = ((double) (productionQueryResponse.getTotalActualCycleTime() - toBeProduced)) / toBeProduced;
                            }
                            availability.setSpeedLoss((int) speedLoss);
                            availability.setPerformance(performance);
                            availability.setItemBO(productionQueryResponse.getItemBO());
                            availability.setOperationBO(productionQueryResponse.getOperationBO());
                            availability.setRoutingBO(productionQueryResponse.getRouterBO());
                            availability.setCount(productionQueryResponse.getTotalQtyCompleted());
                            availability.setIdealTime(idealCycleTime);
                            availability.setShoporderBO(productionQueryResponse.getShoporderBO());
//                            availability.setWorkcenterBO(productionQueryResponse.getWorkcenterBO());
                            availability.setActualValue((int) actualToBeProducedTime);
                            availability.setTargetValue((int) toBeProduced);
                            availability.setEventPerformance("OPERATION_COMPLETE");
                            Performance performanceObject = saveInPerformance(availability);
                            performanceResponse.add(performanceObject);
                            performanceResponseList.add(availability);

                        }

                        List<ProductionQueryResponse> totalQtyGroupByItemResRoutOpforDone = getTotalQtyForDone(productionLogRequest);

                        for (ProductionQueryResponse productionQueryResponse : totalQtyGroupByItemResRoutOpforDone) {
                            hasDone = true;
                            String tags= getTags(productionLogRequest, isStar);
                            String [] tagSplitted=tags.split("-");
                            if(tagSplitted!=null&&tagSplitted.length>=2){
                                availability.setTags(tags);
                            }
                            if (productionQueryResponse.getItemBO()!=null&&!productionQueryResponse.getItemBO().isEmpty()){
                                String itemBo[] = productionQueryResponse.getItemBO().split(",");
                                productionQueryResponse.setMaterial(itemBo[1]);
                                productionQueryResponse.setMaterialVersion(itemBo[2]);}
                            if (productionQueryResponse.getRouterBO()!=null&&!productionQueryResponse.getRouterBO().isEmpty()) {

                                String routingBO[] = productionQueryResponse.getRouterBO().split(",");
                                productionQueryResponse.setRouting(routingBO[1]);
                                productionQueryResponse.setRoutingVersion(routingBO[2]);
                            }
                            if (productionQueryResponse.getResourceBO()!=null&&!productionQueryResponse.getResourceBO().isEmpty()) {

                                String resourceBo[] = productionQueryResponse.getResourceBO().split(",");
                                productionQueryResponse.setResource(resourceBo[1]);
                            }
                            if (productionQueryResponse.getOperationBO()!=null&&!productionQueryResponse.getOperationBO().isEmpty()) {

                                String operationBo[] = productionQueryResponse.getOperationBO().split(",");
                                productionQueryResponse.setOperation(operationBo[1]);
                                productionQueryResponse.setOperationVersion(operationBo[2]);
                            }
                            productionQueryResponse.setSite(availability.getSite());
                            CycleTime cycleTime = getIdealCycleTime(productionQueryResponse);
                            double idealCycleTime = cycleTime.getManufacturedTime();
                            double actualToBeProducedTime = idealCycleTime * productionQueryResponse.getTotalQtyCompleted();
                            double operatingTime = availability.getOperatingTime();
                            double performance = (actualToBeProducedTime / operatingTime) * 100;
                            double toBeProduced = availability.getOperatingTime() / idealCycleTime;
                            double speedLoss;
                            if (toBeProduced > productionQueryResponse.getTotalActualCycleTime()) {
                                speedLoss = ((double) (toBeProduced - productionQueryResponse.getTotalActualCycleTime())) / toBeProduced;
                            } else {
                                speedLoss = ((double) (productionQueryResponse.getTotalActualCycleTime() - toBeProduced)) / toBeProduced;
                            }
                            availability.setSpeedLoss((int) speedLoss);
                            availability.setPerformance(performance);
                            availability.setItemBO(productionQueryResponse.getItemBO());
                            availability.setOperationBO(null);
                            availability.setRoutingBO(productionQueryResponse.getRouterBO());
                            availability.setCount(productionQueryResponse.getTotalQtyCompleted());
                            availability.setIdealTime(idealCycleTime);
                            availability.setShoporderBO(productionQueryResponse.getShoporderBO());
                            //availability.setWorkcenterBO(productionQueryResponse.getWorkcenterBO());
                            availability.setActualValue((int) actualToBeProducedTime);
                            availability.setTargetValue((int) toBeProduced);
                            availability.setEventPerformance("OPERATION_DONE");
                            availability.setDone(true);
                            Performance performanceObject = saveInPerformance(availability);
                            performanceResponse.add(performanceObject);
                            performanceResponseList.add(availability);
                        }


                        if (!hasComplete && !hasDone) {
                            availability.setPerformance(0);
                            Performance performanceObject = getPerformanceObject(availability);
                            performanceRepository.save(performanceObject);
                            performanceResponse.add(performanceObject);
                            performanceResponseList.add(availability);
                        }
                    }

                }
            }
        }



        makeProcessedtrue(temp);
        PerformanceRequestList performanceRequestList1;
        performanceRequestList1 = PerformanceRequestList.builder().performanceResponseList(performanceResponse).build();
        applicationEventPublisher.publishEvent(performanceRequestList1);


        return performanceRequestList1;
    }

    private String getTags(ProductionLogRequest productionLogRequest, boolean isStar) {
        String tags=null;
        if(isStar){
            tags="Resource:*";
        }else {
            tags = "Resource:" + productionLogRequest.getResource();
        }
        if(productionLogRequest.getItemBO()!=null&&!productionLogRequest.getItemBO().isEmpty()){
            tags+="-Material:"+productionLogRequest.getItemBO();
        }
        if(productionLogRequest.getShopOrderBO()!=null&&!productionLogRequest.getShopOrderBO().isEmpty()){
            tags+="-ShopOrder:"+productionLogRequest.getShopOrderBO();
        } if(productionLogRequest.getOperationBO()!=null&&!productionLogRequest.getOperationBO().isEmpty()){
            tags+="-Operation:"+productionLogRequest.getOperationBO();
        } if(productionLogRequest.getRoutingBO()!=null&&!productionLogRequest.getRoutingBO().isEmpty()){
            tags+="-Routing:"+productionLogRequest.getRoutingBO();
        }
        return tags;
    }

    private List<ProductionLogRequest> getCombination(List<Combinations> combinations) {
        if (combinations == null || combinations.isEmpty()) {
            return Collections.emptyList();
        }

        return combinations.stream()
                .map(Combinations::getCombo)
                .filter(Objects::nonNull)
                .map(this::parseCombination)
                .collect(Collectors.toList());
    }

    private ProductionLogRequest parseCombination(String combinationRequest) {
        ProductionLogRequest productionLogRequest = new ProductionLogRequest();
        Map<String, String> comboMap = Arrays.stream(combinationRequest.split("-"))
                .map(String::trim)
                .map(combo -> combo.split(":", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));

        comboMap.forEach((key, value) -> {
            switch (key) {
                case "Material":
                    productionLogRequest.setItemBO(value);
                    break;
                case "Operation":
                    productionLogRequest.setOperationBO(value);
                    break;
                case "Routing":
                    productionLogRequest.setRoutingBO(value);
                    break;
                case "ShopOrder":
                    productionLogRequest.setShopOrderBO(value);
                    break;
                case  "Resource":
                        productionLogRequest.setResource(value);
                    break;
            }
        });

        return productionLogRequest;
    }


    private void makeProcessedtrue(PerformanceRequestList temp) {
        try {
//            webClientBuilder.build()
//                    .post()
//                    .uri(getTotalQtyForDoneUrl)
//                    .bodyValue(temp)
//                    .retrieve()
//                    .bodyToMono(new ParameterizedTypeReference<List<ProductionQueryResponse>>() {})
//                    .block();
        } catch (PerformanceException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PerformanceRequestList getUnProcessedRecord(PerformanceRequestList performanceRequestList) {
        List<Performance> performances = performanceRepository.findByProcessed(false);
        return PerformanceRequestList.builder().performanceResponseList(performances).build();
    }

    @Override
    public PerformanceRequestList makeProcessedAsTrue(List<Performance> performanceList) {
        List<Performance> list = null;
        for (Performance performance : performanceList) {
            Optional<Performance> optionalExistingPerformance = performanceRepository.findByUniqueId(performance.getUniqueId());

            if (optionalExistingPerformance.isPresent()) {
                Performance existingPerformance = optionalExistingPerformance.get();
                existingPerformance.setProcessed(true);
                performanceRepository.save(existingPerformance);
                list.add(performance);
            } else {
                throw new PerformanceException(1);
            }
        }
        return PerformanceRequestList.builder().performanceResponseList(list).build();
    }

    @Override
    public DownTime downTimeAvailabilityToDownTimeBuilder(DownTimeAvailability downTimeAvailability) {
        if (downTimeAvailability == null) {
            return null;
        }

        return DownTime.builder()
                .uniqueId(downTimeAvailability.getUniqueId())
                .resourceId(downTimeAvailability.getResourceId())
                .createdDateTime(LocalDateTime.parse(downTimeAvailability.getCreatedDateTime()))
                .shift(downTimeAvailability.getShift())
                .entryTime(downTimeAvailability.getEntryTime())
                .plannedProductionTime(downTimeAvailability.getPlannedProductionTime())
                .totalDowntime(downTimeAvailability.getTotalDowntime())
                .operatingTime(downTimeAvailability.getOperatingTime())
                .breakHours(downTimeAvailability.getBreakHours())
                .availability(downTimeAvailability.getAvailability())
                .active(downTimeAvailability.getActive())
                .event(downTimeAvailability.getEvent())
                .site(downTimeAvailability.getSite())
                .shiftStartDate(downTimeAvailability.getShiftStartDate())
                .mcBreakDownHours(downTimeAvailability.getMcBreakDownHours())
                .shiftEndDate(downTimeAvailability.getShiftEndDate())
                .processed(downTimeAvailability.getProcessed())
                .reasonCode(downTimeAvailability.getReasonCode())
                .combinations(downTimeAvailability.getCombinations())
                .build();
    }

    @Override
    public List<Oee> calculatePerformanceForLiveData(PerformanceRequestList performanceRequestList) throws Exception {
        List<Performance> performanceResponse = new ArrayList<>();
        PerformanceRequestList temp = performanceRequestList;
        List<DownTime> performanceResponseList = new ArrayList<>();
        if (performanceRequestList != null || performanceRequestList.getPerformanceRequestList() != null || !performanceRequestList.getPerformanceRequestList().isEmpty()) {
            for (DownTime availability : performanceRequestList.getPerformanceRequestList()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalDateTime createdDateTime = availability.getCreatedDateTime();
                LocalDateTime shiftStartTime = parseToLocalDateTime(availability.getShiftStartDate(), formatter, createdDateTime);


                ProductionLogRequest productionLogRequest = new ProductionLogRequest(shiftStartTime, createdDateTime, availability.getResourceId(),availability.getSite(),availability.getShift(),null,null,null,null);
                List<ProductionQueryResponse> totalQtyGroupByItemResRoutOp = getTotalQty(productionLogRequest);
                //iterate thru given output andusing that item,routing,res,operation find ideal cycle time..

                boolean hasComplete=false;
                boolean hasDone = false;
                for (ProductionQueryResponse productionQueryResponse : totalQtyGroupByItemResRoutOp) {
                    hasComplete=true;
                    String itemBo[] = productionQueryResponse.getItemBO().split(",");
                    productionQueryResponse.setMaterial(itemBo[1]);
                    productionQueryResponse.setMaterialVersion(itemBo[2]);
                        String routingBO[]=productionQueryResponse.getRouterBO().split(",");
                        productionQueryResponse.setRouting(routingBO[1]);
                        productionQueryResponse.setRoutingVersion(routingBO[2]);
                    String resourceBo[] = productionQueryResponse.getResourceBO().split(",");
                    productionQueryResponse.setResource(resourceBo[1]);
                    String operationBo[] = productionQueryResponse.getOperationBO().split(",");
                    productionQueryResponse.setOperation(operationBo[1]);
                    productionQueryResponse.setOperationVersion(operationBo[2]);
                    productionQueryResponse.setSite(availability.getSite());

                    CycleTime cycleTime = getIdealCycleTime(productionQueryResponse);
                    double idealCycleTime = cycleTime.getCycleTime();
                    double actualToBeProducedTime = idealCycleTime * productionQueryResponse.getTotalQtyCompleted();
                    double operatingTime = availability.getOperatingTime();
                    double performance = (actualToBeProducedTime / operatingTime) * 100;
                    double toBeProduced = availability.getOperatingTime() / idealCycleTime;
                    double speedLoss;
                    if (toBeProduced > productionQueryResponse.getTotalActualCycleTime()) {
                        speedLoss = ((double) (toBeProduced - productionQueryResponse.getTotalActualCycleTime())) / toBeProduced;
                    } else {
                        speedLoss = ((double) (productionQueryResponse.getTotalActualCycleTime() - toBeProduced)) / toBeProduced;
                    }

                    availability.setSpeedLoss((int)speedLoss);
                    availability.setPerformance(performance);
                    availability.setItemBO(productionQueryResponse.getItemBO());
                    availability.setOperationBO(productionQueryResponse.getOperationBO());
                    availability.setRoutingBO(productionQueryResponse.getRouterBO());
                    availability.setCount(productionQueryResponse.getTotalQtyCompleted());
                    availability.setIdealTime(idealCycleTime);
                    availability.setShoporderBO(productionQueryResponse.getShoporderBO());
                    availability.setWorkcenterBO(productionQueryResponse.getWorkcenterBO());
                    availability.setEventPerformance("OPERATION_COMPLETE");
                    availability.setTargetValue((int) toBeProduced);
                    availability.setActualValue((int)actualToBeProducedTime);
                    Performance performanceObject = getPerformanceObject(availability);
                    performanceResponse.add(performanceObject);
                    performanceResponseList.add(availability);

                }


                List<ProductionQueryResponse> totalQtyGroupByItemResRoutOpforDone = getTotalQtyForDone(productionLogRequest);

                for (ProductionQueryResponse productionQueryResponse : totalQtyGroupByItemResRoutOpforDone) {
                    hasDone=true;
                    String itemBo[] = productionQueryResponse.getItemBO().split(",");
                    productionQueryResponse.setMaterial(itemBo[1]);
                    productionQueryResponse.setMaterialVersion(itemBo[2]);
                        String routingBO[]=productionQueryResponse.getRouterBO().split(",");
                        productionQueryResponse.setRouting(routingBO[1]);
                        productionQueryResponse.setRoutingVersion(routingBO[2]);
                    String resourceBo[] = productionQueryResponse.getResourceBO().split(",");
                    productionQueryResponse.setResource(resourceBo[1]);
                    String operationBo[] = productionQueryResponse.getOperationBO().split(",");
                    productionQueryResponse.setOperation(operationBo[1]);
                    productionQueryResponse.setOperationVersion(operationBo[2]);
                    productionQueryResponse.setSite(availability.getSite());
                    CycleTime cycleTime = getIdealCycleTime(productionQueryResponse);
                    double idealCycleTime =  cycleTime.getManufacturedTime();
                    double actualToBeProducedTime = idealCycleTime * productionQueryResponse.getTotalQtyCompleted();
                    double operatingTime = availability.getOperatingTime();
                    double performance = (actualToBeProducedTime / operatingTime) * 100;
                    double toBeProduced = availability.getOperatingTime() / idealCycleTime;
                    double speedLoss;
                    if (toBeProduced > productionQueryResponse.getTotalActualCycleTime()) {
                        speedLoss = ((double) (toBeProduced - productionQueryResponse.getTotalActualCycleTime())) / toBeProduced;
                    } else {
                        speedLoss = ((double) (productionQueryResponse.getTotalActualCycleTime() - toBeProduced)) / toBeProduced;
                    }

                    availability.setSpeedLoss((int)speedLoss);
                    availability.setPerformance(performance);
                    availability.setItemBO(productionQueryResponse.getItemBO());
                    availability.setOperationBO(productionQueryResponse.getOperationBO());
                    availability.setRoutingBO(productionQueryResponse.getRouterBO());
                    availability.setCount(productionQueryResponse.getTotalQtyCompleted());
                    availability.setIdealTime(idealCycleTime);
                    availability.setShoporderBO(productionQueryResponse.getShoporderBO());
                    availability.setWorkcenterBO(productionQueryResponse.getWorkcenterBO());
                    availability.setTargetValue((int) toBeProduced);
                    availability.setActualValue((int)actualToBeProducedTime);
                    availability.setDone(true);
                    availability.setEventPerformance("OPERATION_DONE");
                    Performance performanceObject = getPerformanceObject(availability);
                    performanceResponse.add(performanceObject);
                    performanceResponseList.add(availability);
                }
                if(!hasComplete && !hasDone){
                    availability.setPerformance(0);
                    Performance performanceObject = getPerformanceObject(availability);
                    performanceResponse.add(performanceObject);
                    performanceResponseList.add(availability);
                }

            }
        }


            PerformanceRequestList performanceRequestList1 = PerformanceRequestList.builder().
                    performanceResponseList(performanceResponse).
                    build();

        QualityRequestList qualityRequestList=qualityService.getPerformanceToQualityRequest(performanceRequestList1);
        List<Oee> oeeList=qualityService.calculateQualityForLiveData(qualityRequestList);
            return oeeList;
    }

    private Performance getPerformanceObject(DownTime availability) {
        availability.setUniqueId(availability.getUniqueId()+1);
        DecimalFormat df = new DecimalFormat("#.###");
        double trimmedPerformance = Double.parseDouble(df.format(availability.getPerformance()));

        Performance performance = Performance.builder()
                .site(availability.getSite())
                .resourceId(availability.getResourceId())
                .uniqueId(availability.getUniqueId())
                .createdDateTime(availability.getCreatedDateTime())
                .shiftStartTime(availability.getShiftStartDate())
                .shift(availability.getShift())
                .entryTime(availability.getEntryTime())
                .plannedProductionTime(String.valueOf(availability.getPlannedProductionTime()))
                .totalDowntime(availability.getTotalDowntime())
                .operatingTime(availability.getOperatingTime())
                .breakHours(availability.getBreakHours())
                .availability(availability.getAvailability())
                .speedLoss(availability.getSpeedLoss())
                .shoporderBO(availability.getShoporderBO())
                .workcenterBO(availability.getWorkcenterBO())
                .reasonCode(availability.getReasonCode())
                .performance(trimmedPerformance)
                .tags(availability.getTags())
                .active(1)
                .event(availability.getEvent())
                .itemBO(availability.getItemBO())
                .routingBO(availability.getRoutingBO())
                .operationBO(availability.getOperationBO())
                .count(availability.getCount())
                .calculatedCycleTime(availability.getIdealTime())
                .eventPerformance(availability.getEventPerformance())
                .actualValue(availability.getActualValue())
                .targetValue(availability.getTargetValue())
                .done(availability.isDone())
                .processed(false)
                .build();
        return performance;
    }


    private List<ProductionQueryResponse> getTotalQtyForDone(ProductionLogRequest productionLogRequest) {
        return  webClientBuilder.build()
                .post()
                .uri(getTotalQtyForDoneUrl)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductionQueryResponse>>() {
                })
                .block();
    }

    private LocalDateTime parseToLocalDateTime(String timeString, DateTimeFormatter formatter, LocalDateTime createdDateTime) {
        LocalDate date = createdDateTime.toLocalDate();
        return LocalTime.parse(timeString, formatter).atDate(date);
    }


    private Performance saveInPerformance(DownTime availability) {
        availability.setUniqueId(availability.getUniqueId()+1);
        DecimalFormat df = new DecimalFormat("#.###");
        double trimmedPerformance = Double.parseDouble(df.format(availability.getPerformance()));

        Performance performance = Performance.builder()
                .site(availability.getSite())
                .resourceId(availability.getResourceId())
                .uniqueId(availability.getUniqueId())
                .createdDateTime(availability.getCreatedDateTime())
                .shiftStartTime(availability.getShiftStartDate())
                .shift(availability.getShift())
                .entryTime(availability.getEntryTime())
                .plannedProductionTime(String.valueOf(availability.getPlannedProductionTime()))
                .totalDowntime(availability.getTotalDowntime())
                .operatingTime(availability.getOperatingTime())
                .breakHours(availability.getBreakHours())
                .availability(availability.getAvailability())
                .speedLoss(availability.getSpeedLoss())
                .shoporderBO(availability.getShoporderBO())
                .workcenterBO(availability.getWorkcenterBO())
                .reasonCode(availability.getReasonCode())
                .performance(trimmedPerformance)
                .active(1)
                .tags(availability.getTags())
                .event(availability.getEvent())
                .itemBO(availability.getItemBO())
                .routingBO(availability.getRoutingBO())
                .operationBO(availability.getOperationBO())
                .count(availability.getCount())
                .calculatedCycleTime(availability.getIdealTime())
                .eventPerformance(availability.getEventPerformance())
                .actualValue(availability.getActualValue())
                .targetValue(availability.getTargetValue())
                .done(availability.isDone())
                .processed(false)
                .build();
        performanceRepository.save(performance);
        return performance;


    }

    private  List<ProductionQueryResponse>  getTotalQty(ProductionLogRequest productionLogRequest) {
        return  webClientBuilder.build()
                .post()
                .uri(getTotalQtyUrl)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductionQueryResponse>>() {
                })
                .block();
    }
    public CycleTime getIdealCycleTime(ProductionQueryResponse productionQueryResponse){

        CycleTime  cycleTime=  webClientBuilder.build()
                .post()
                .uri(getIdealCycleTimeUrl)
                .bodyValue(productionQueryResponse)
                .retrieve()
                .bodyToMono(CycleTime.class)
                .block();
        if(cycleTime==null||cycleTime.getSite()==null||cycleTime.getSite().isEmpty()){
            throw new PerformanceException(2);
        }
         return cycleTime;
    }




}
