package com.rits.qualityservice.service;

import com.rits.oeeservice.dto.OeeRequestList;
import com.rits.oeeservice.model.Oee;
import com.rits.oeeservice.service.OeeService;
import com.rits.performanceservice.dto.PerformanceRequestList;
import com.rits.performanceservice.model.Performance;
import com.rits.qualityservice.dto.*;
import com.rits.qualityservice.model.Quality;
import com.rits.qualityservice.repository.QualityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QualityServiceImpl implements QualityService{

    private final WebClient.Builder webClientBuilder;
    private final OeeService oeeService;
    private final QualityRepository qualityRepository;

    @Value("${productionlog-service.url}/getTotalScrapAndCompletedQty")
    private String productionLogUrl;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    public List<ProductionLogQualityResponse> getTotalQuantities(ProductionLogRequest productionLogRequest)throws Exception
    {
        List<ProductionLogQualityResponse> productionLogResponse = webClientBuilder.build()
                .post()
                .uri(productionLogUrl)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductionLogQualityResponse>>() {
                })
                .block();
        return productionLogResponse;
    }

    @Override
    public QualityRequestList calculateQuality(QualityRequestList qualityRequestList)throws Exception
    {
        List<Quality> qualityResponse = new ArrayList<>();
        QualityRequestList performanceResponse = new QualityRequestList();
        for(QualityRequest qualityRequest : qualityRequestList.getQualityRequest())
        {
            int goodQuantity = 0;
            double quality = 0;
            int scrapQuantity = 0;
            int totalProducedQty = 0;

                ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                        .site(qualityRequest.getSite())
                        .itemBO(qualityRequest.getItemBO())
                        .routerBO(qualityRequest.getRoutingBO())
                        .operation_bo(qualityRequest.getOperationBO())
                        .resourceBO(qualityRequest.getResourceId())
                        .shopOrderBO(qualityRequest.getShoporderBO())
                        .eventPerformance(qualityRequest.getEventPerformance())
                        .shiftStartTime(qualityRequest.getShiftStartTime())
                        .entryTime(qualityRequest.getEntryTime())
                        .build();
                List<ProductionLogQualityResponse> totalScrapAndCompletedQtyResponse =  getTotalQuantities(productionLogRequest);
//                for(ProductionLogQualityResponse productionQuality : totalScrapAndCompletedQtyResponse)
//                {
//                    if(productionQuality.getEventType().equalsIgnoreCase("PCU_SCRAP"))
//                    {
//                        scrapQuantity = productionQuality.getTotalQuantity();
//                    }
//                    if(productionQuality.getEventType().equalsIgnoreCase("PCU_COMPLETE")){
//                        totalProducedQty = productionQuality.getTotalQuantity();
//                    }
//                }


            if(totalScrapAndCompletedQtyResponse !=null && !totalScrapAndCompletedQtyResponse.isEmpty()) {
                for (ProductionLogQualityResponse productionLogQualityResponse : totalScrapAndCompletedQtyResponse) {
                    scrapQuantity = (int) productionLogQualityResponse.getTotalQuantity();
                    int count = qualityRequest.getCount();
                    if (count != 0) {
                        goodQuantity = count - scrapQuantity;
                        quality = (double) goodQuantity / count;
                    } else {
                        quality = 0; // Set quality to zero when count is zero
                    }

                    Quality createQuality = qualityBuilder(qualityRequest,scrapQuantity,quality);
                    qualityResponse.add(createQuality);
                }
            }else{
                int count = qualityRequest.getCount();
                 scrapQuantity = qualityRequest.getScrapQuantity();

// Check if count is zero to prevent division by zero
                if (count != 0) {
                     goodQuantity = count - scrapQuantity;
                    quality = (double) goodQuantity / count;
                } else {
                    quality = 0; // Set quality to zero when count is zero
                }

                Quality createQuality = qualityBuilder(qualityRequest,scrapQuantity,quality);
                qualityResponse.add(createQuality);

            }
        }
        QualityRequestList qualityResponseList=QualityRequestList.builder().qualityResponseList(qualityResponse).build();
        applicationEventPublisher.publishEvent(qualityResponseList);
        return qualityResponseList;
    }

    private Quality qualityBuilderForLiveData(QualityRequest qualityRequest, int scrapQuantity, double quality) {
        DecimalFormat df = new DecimalFormat("#.###");
        quality=quality*100;
        double trimmedQuality = Double.parseDouble(df.format(quality));

        Quality createQuality= Quality.builder()
                .site(qualityRequest.getSite())
                .uniqueId(qualityRequest.getUniqueId())
                .resourceId(qualityRequest.getResourceId())
                .createdDateTime(LocalDateTime.now())
                .shift(qualityRequest.getShift())
                .entryTime(qualityRequest.getEntryTime())
                .plannedProductionTime(qualityRequest.getPlannedProductionTime())
                .totalDowntime(qualityRequest.getTotalDowntime())
                .operatingTime(qualityRequest.getOperatingTime())
                .breakHours(qualityRequest.getBreakHours())
                .availability(qualityRequest.getAvailability())
                .active(1)
                .event(qualityRequest.getEvent())
                .itemBO(qualityRequest.getItemBO())
                .operationBO(qualityRequest.getOperationBO())
                .routingBO(qualityRequest.getRoutingBO())
                .calculatedCycleTime(qualityRequest.getCalculatedCycleTime())
                .count(qualityRequest.getCount())
                .tags(qualityRequest.getTags())
                .performance(qualityRequest.getPerformance())
                .eventPerformance(qualityRequest.getEventPerformance())
                .scrapQuantity(scrapQuantity)
                .shiftStartTime(qualityRequest.getShiftStartTime())
                .processed(false)
                .quality(trimmedQuality)
                .shoporderBO(qualityRequest.getShoporderBO())
                .workcenterBO(qualityRequest.getWorkcenterBO())
                .reasonCode(qualityRequest.getReasonCode())
                .speedLoss(qualityRequest.getSpeedLoss())
                .idealTime(qualityRequest.getIdealTime())
                .actualValue(qualityRequest.getActualValue())
                .targetValue(qualityRequest.getTargetValue())
                .done(qualityRequest.isDone())
                .build();
        return createQuality;
    }


    private Quality qualityBuilder(QualityRequest qualityRequest, int scrapQuantity, double quality) {
        DecimalFormat df = new DecimalFormat("#.###");
        quality=quality*100;
        double trimmedQuality = Double.parseDouble(df.format(quality));

        Quality createQuality= Quality.builder()
                .site(qualityRequest.getSite())
                .uniqueId(qualityRequest.getUniqueId())
                .resourceId(qualityRequest.getResourceId())
                .createdDateTime(LocalDateTime.now())
                .shift(qualityRequest.getShift())
                .entryTime(qualityRequest.getEntryTime())
                .plannedProductionTime(qualityRequest.getPlannedProductionTime())
                .totalDowntime(qualityRequest.getTotalDowntime())
                .operatingTime(qualityRequest.getOperatingTime())
                .breakHours(qualityRequest.getBreakHours())
                .availability(qualityRequest.getAvailability())
                .tags(qualityRequest.getTags())
                .active(1)
                .event(qualityRequest.getEvent())
                .itemBO(qualityRequest.getItemBO())
                .operationBO(qualityRequest.getOperationBO())
                .routingBO(qualityRequest.getRoutingBO())
                .calculatedCycleTime(qualityRequest.getCalculatedCycleTime())
                .count(qualityRequest.getCount())
                .performance(qualityRequest.getPerformance())
                .eventPerformance(qualityRequest.getEventPerformance())
                .scrapQuantity(scrapQuantity)
                .shiftStartTime(qualityRequest.getShiftStartTime())
                .shoporderBO(qualityRequest.getShoporderBO())
                .workcenterBO(qualityRequest.getWorkcenterBO())
                .reasonCode(qualityRequest.getReasonCode())
                .speedLoss(qualityRequest.getSpeedLoss())
                .idealTime(qualityRequest.getIdealTime())
                .processed(false)
                .quality(trimmedQuality)
                .targetValue(qualityRequest.getTargetValue())
                .actualValue(qualityRequest.getActualValue())
                .done(qualityRequest.isDone())
                .build();
        return qualityRepository.save(createQuality);
    }

    @Override
    public QualityRequestList getPerformanceToQualityRequest(PerformanceRequestList performanceRequestList) {
        List<QualityRequest> qualityList = new ArrayList<>();

        for (Performance performance : performanceRequestList.getPerformanceResponseList()) {
            QualityRequest quality = QualityRequest.builder()
                    .uniqueId(performance.getUniqueId())
                    .site(performance.getSite())
                    .resourceId(performance.getResourceId())
                    .createdDateTime(performance.getCreatedDateTime())
                    .shiftStartTime(performance.getShiftStartTime())
                    .shift(performance.getShift())
                    .entryTime(performance.getEntryTime())
                    .plannedProductionTime(performance.getPlannedProductionTime())
                    .totalDowntime(performance.getTotalDowntime())
                    .operatingTime(performance.getOperatingTime())
                    .breakHours(performance.getBreakHours())
                    .availability(performance.getAvailability())
                    .performance(performance.getPerformance())
                    .active(performance.getActive())
                    .tags(performance.getTags())
                    .event(performance.getEvent())
                    .itemBO(performance.getItemBO())
                    .routingBO(performance.getRoutingBO())
                    .operationBO(performance.getOperationBO())
                    .scrapQuantity(performance.getScrapQuantity())
                    .quality((int) performance.getQuality())
                    .count(performance.getCount())
                    .calculatedCycleTime(performance.getCalculatedCycleTime())
                    .eventPerformance(performance.getEventPerformance())
                    .reasonCode(performance.getReasonCode())
                    .speedLoss(performance.getSpeedLoss())
                    .shoporderBO(performance.getShoporderBO())
                    .workcenterBO(performance.getWorkcenterBO())
                    .idealTime(performance.getIdealTime())
                    .actualValue(performance.getActualValue())
                    .targetValue(performance.getTargetValue())
                    .done(performance.isDone())
                    .build();

            qualityList.add(quality);
        }

        return QualityRequestList.builder()
                .qualityRequest(qualityList)
                .build();
    }

    @Override
    public List<Oee> calculateQualityForLiveData(QualityRequestList qualityRequestList) throws Exception {
        List<Quality> qualityResponse = new ArrayList<>();
        QualityRequestList performanceResponse = new QualityRequestList();
        for(QualityRequest qualityRequest : qualityRequestList.getQualityRequest())
        {
            int goodQuantity = 0;
            double quality = 0;
            int scrapQuantity = 0;
            int totalProducedQty = 0;

            ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                    .site(qualityRequest.getSite())
                    .itemBO(qualityRequest.getItemBO())
                    .routerBO(qualityRequest.getRoutingBO())
                    .operation_bo(qualityRequest.getOperationBO())
                    .resourceBO(qualityRequest.getResourceId())
                    .eventPerformance(qualityRequest.getEventPerformance())
                    .shiftStartTime(qualityRequest.getShiftStartTime())
                    .shopOrderBO(qualityRequest.getShoporderBO())
                    .entryTime(qualityRequest.getEntryTime())
                    .build();
            List<ProductionLogQualityResponse> totalScrapAndCompletedQtyResponse =  getTotalQuantities(productionLogRequest);
//                for(ProductionLogQualityResponse productionQuality : totalScrapAndCompletedQtyResponse)
//                {
//                    if(productionQuality.getEventType().equalsIgnoreCase("PCU_SCRAP"))
//                    {
//                        scrapQuantity = productionQuality.getTotalQuantity();
//                    }
//                    if(productionQuality.getEventType().equalsIgnoreCase("PCU_COMPLETE")){
//                        totalProducedQty = productionQuality.getTotalQuantity();
//                    }
//                }


            if(totalScrapAndCompletedQtyResponse !=null && !totalScrapAndCompletedQtyResponse.isEmpty()) {
                for (ProductionLogQualityResponse productionLogQualityResponse : totalScrapAndCompletedQtyResponse) {
                    scrapQuantity = (int) productionLogQualityResponse.getTotalQuantity();
                    int count = qualityRequest.getCount();
                    if (count != 0) {
                        goodQuantity = count - scrapQuantity;
                        quality = (double) goodQuantity / count;
                    } else {
                        quality = 0; // Set quality to zero when count is zero
                    }

                    Quality createQuality = qualityBuilderForLiveData(qualityRequest,scrapQuantity,quality);
                    qualityResponse.add(createQuality);
                }
            }else{
                int count = qualityRequest.getCount();
                scrapQuantity = qualityRequest.getScrapQuantity();

// Check if count is zero to prevent division by zero
                if (count != 0) {
                    goodQuantity = count - scrapQuantity;
                    quality = (double) goodQuantity / count;
                } else {
                    quality = 0; // Set quality to zero when count is zero
                }

                Quality createQuality = qualityBuilderForLiveData(qualityRequest,scrapQuantity,quality);
                qualityResponse.add(createQuality);

            }
        }
        QualityRequestList qualityResponseList=QualityRequestList.builder().qualityResponseList(qualityResponse).build();
        OeeRequestList  oeeRequestList=oeeService.getQualityToOeeRequest(qualityResponseList);
        List<Oee> oee=oeeService.calculateOeeForLive(oeeRequestList);
        return oee;
    }

    public List<Quality> getOeeRequestList(String site)
    {
        List<Quality> qualityList = qualityRepository.findBySiteAndActiveAndProcessed(site,1,false);
        return qualityList;
    }
}
