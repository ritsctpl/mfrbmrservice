package com.rits.oeeservice.service;

import com.rits.downtimeservice.dto.PlannedMinutes;
import com.rits.downtimeservice.dto.ShiftRequest;
import com.rits.oeeservice.dto.*;
import com.rits.oeeservice.exception.OeeException;
import com.rits.oeeservice.model.Oee;
import com.rits.oeeservice.repository.OeeRepository;
import com.rits.performanceservice.dto.ProductionLogRequest;
import com.rits.qualityservice.dto.QualityRequestList;
import com.rits.qualityservice.model.Quality;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service

@RequiredArgsConstructor
public class OeeServiceImpl implements OeeService{
    private final OeeRepository oeeRepository;
    private final MongoTemplate mongoTemplate;
    private final WebClient.Builder webClientBuilder;
    @Value("${shift-service.url}/getBreakHoursTillNowByType")
    private String getPlannedTimeUrl;
    @Value("${downtime-service.url}/getAvailabilityForLive")
    private String getAvailablityForLiveUrl;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]");

    @Override
    public Boolean calculateOee(OeeRequestList oeeRequestList)throws Exception
    {
        Boolean oeeCalculated = false;
        for(OeeRequest oeeRequest : oeeRequestList.getOeeRequestList())
        {
            Double availability = oeeRequest.getAvailability()/100;
            Double perFormance = oeeRequest.getPerformance()/100;
            Double quality = oeeRequest.getQuality()/100;

            Double oee = availability*perFormance*quality;
            oee = oee*100;
            DecimalFormat df = new DecimalFormat("#.###");
            double trimmedOee = Double.parseDouble(df.format(oee));


            Oee createOeeRecord = Oee.builder()
                    .site(oeeRequest.getSite())
                    .uniqueId(oeeRequest.getUniqueId())
                    .resourceId(oeeRequest.getResourceId())
                    .createdDateTime(LocalDateTime.now())
                    .shift(oeeRequest.getShift())
                    .entryTime(oeeRequest.getEntryTime())
                    .plannedProductionTime(oeeRequest.getPlannedProductionTime())
                    .totalDowntime(oeeRequest.getTotalDowntime())
                    .operatingTime(oeeRequest.getOperatingTime())
                    .breakHours(oeeRequest.getBreakHours())
                    .availability(oeeRequest.getAvailability())
                    .active(1)
                    .tags(oeeRequest.getTags())
                    .event(oeeRequest.getEvent())
                    .itemBO(oeeRequest.getItemBO())
                    .operationBO(oeeRequest.getOperationBO())
                    .routingBO(oeeRequest.getRoutingBO())
                    .calculatedCycleTime(oeeRequest.getCalculatedCycleTime())
                    .count(oeeRequest.getCount())
                    .reasonCode(oeeRequest.getReasonCode())
                    .scrapQuantity(oeeRequest.getScrapQuantity())
                    .quality(oeeRequest.getQuality())
                    .shoporderBO(oeeRequest.getShoporderBO())
                    .workcenterBO(oeeRequest.getWorkcenterBO())
                    .performance(oeeRequest.getPerformance())
                    .idealTime(oeeRequest.getIdealTime())
                    .speedLoss(oeeRequest.getSpeedLoss())
                    .eventPerformance(oeeRequest.getEventPerformance())
                    .scrapQuantity(oeeRequest.getScrapQuantity())
                    .shiftStartTime(oeeRequest.getShiftStartTime())
                    .quality(oeeRequest.getQuality())
                    .oee(trimmedOee)
                    .actualValue(oeeRequest.getActualValue())
                    .targetValue(oeeRequest.getTargetValue())
                    .done(oeeRequest.isDone())
                    .build();
            oeeRepository.save(createOeeRecord);
            oeeCalculated = true;
        }
        return oeeCalculated;
    }

    @Override
    public List<Oee> calculateOeeForLive(OeeRequestList oeeRequestList)throws Exception
    {
        List<Oee> oeeCalculated=new ArrayList<>();
        for(OeeRequest oeeRequest : oeeRequestList.getOeeRequestList())
        {
            Double availability = oeeRequest.getAvailability()/100;
            Double perFormance = oeeRequest.getPerformance()/100;
            Double quality = oeeRequest.getQuality()/100;

            Double oee = availability*perFormance*quality;
            oee = oee*100;
            DecimalFormat df = new DecimalFormat("#.###");
            double trimmedOee = Double.parseDouble(df.format(oee));

            Oee createOeeRecord = Oee.builder()
                    .site(oeeRequest.getSite())
                    .uniqueId(oeeRequest.getUniqueId())
                    .resourceId(oeeRequest.getResourceId())
                    .createdDateTime(LocalDateTime.now())
                    .shift(oeeRequest.getShift())
                    .entryTime(oeeRequest.getEntryTime())
                    .plannedProductionTime(oeeRequest.getPlannedProductionTime())
                    .totalDowntime(oeeRequest.getTotalDowntime())
                    .operatingTime(oeeRequest.getOperatingTime())
                    .breakHours(oeeRequest.getBreakHours())
                    .availability(oeeRequest.getAvailability())
                    .active(1)
                    .tags(oeeRequest.getTags())
                    .event(oeeRequest.getEvent())
                    .itemBO(oeeRequest.getItemBO())
                    .operationBO(oeeRequest.getOperationBO())
                    .routingBO(oeeRequest.getRoutingBO())
                    .calculatedCycleTime(oeeRequest.getCalculatedCycleTime())
                    .count(oeeRequest.getCount())
                    .reasonCode(oeeRequest.getReasonCode())
                    .scrapQuantity(oeeRequest.getScrapQuantity())
                    .quality(oeeRequest.getQuality())
                    .shoporderBO(oeeRequest.getShoporderBO())
                    .workcenterBO(oeeRequest.getWorkcenterBO())
                    .performance(oeeRequest.getPerformance())
                    .idealTime(oeeRequest.getIdealTime())
                    .speedLoss(oeeRequest.getSpeedLoss())
                    .eventPerformance(oeeRequest.getEventPerformance())
                    .scrapQuantity(oeeRequest.getScrapQuantity())
                    .shiftStartTime(oeeRequest.getShiftStartTime())
                    .quality(oeeRequest.getQuality())
                    .oee(trimmedOee)
                    .actualValue(oeeRequest.getActualValue())
                    .targetValue(oeeRequest.getTargetValue())
                    .done(oeeRequest.isDone())
                    .build();
            oeeCalculated.add(createOeeRecord);
        }
        return oeeCalculated;
    }


    @Override
    public List<AvailabilityByResource> getAvailabilityByResource(List<Oee> oeeData) {
        return oeeData.stream()
                .collect(Collectors.groupingBy(oee -> oee.getResourceId() + "-" + oee.getCreatedDateTime().toLocalDate(),
                        Collectors.averagingDouble(Oee::getAvailability)))
                .entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    String resourceId = parts[0];
                    LocalDate date = LocalDate.parse(parts[1] + "-" + parts[2]+"-"+parts[3]);
                    double averageAvailability = entry.getValue();
                    return new AvailabilityByResource(averageAvailability, resourceId, date);
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<DownTimeByResource> getDownTimeByResource(List<Oee> oeeData) {
        return oeeData.stream()
                .collect(Collectors.groupingBy(
                        Oee::getResourceId,
                        Collectors.summingDouble(Oee::getTotalDowntime)))
                .entrySet().stream()
                .map(entry -> new DownTimeByResource(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }
    @Override
    public List<DownTimeByReason> getDownTimeByReason(List<Oee> oeeData) {
        List<Oee> filteredOeeData = oeeData.stream()
                .filter(oee -> oee.getReasonCode() != null && !oee.getReasonCode().isEmpty())
                .collect(Collectors.toList());

        // Group downtime by reason code and calculate the average downtime for each reason
        return filteredOeeData.stream()
                .collect(Collectors.groupingBy(Oee::getReasonCode,
                        Collectors.averagingInt(Oee::getTotalDowntime)))
                .entrySet().stream()
                .map(entry -> new DownTimeByReason(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }


    @Override
    public List<Oee> getOEE(FilterObjects filterObjects) {
        Criteria criteria = buildFilterCriteria(filterObjects);

        MatchOperation matchOperation = Aggregation.match(criteria);

        // Add your OEE calculation stages here
        // ...

        Aggregation aggregation = Aggregation.newAggregation(matchOperation /* other stages */);

        return mongoTemplate.aggregate(aggregation, "R_OEE", Oee.class).getMappedResults();
    }

    private Criteria buildFilterCriteria(FilterObjects filterObjects) {
        List<Criteria> criteriaList = new ArrayList<>();

        addToCriteriaListIfNotNull(criteriaList, "site", filterObjects.getSite());
        addToCriteriaListIfNotNull(criteriaList, "shoporderBO", filterObjects.getShopOrderBO());
        addToCriteriaListIfNotNull(criteriaList, "itemBO", filterObjects.getItemBO());
        addToCriteriaListIfNotNull(criteriaList, "shift", filterObjects.getShift());
        addToCriteriaListIfNotNull(criteriaList, "resourceId", filterObjects.getResourceId());
        addToCriteriaListIfNotNull(criteriaList, "workCenterBO", filterObjects.getWorkCenterBO());
        addToCriteriaListIfNotNull(criteriaList, "operationBO", filterObjects.getOperationBO());
        addToCriteriaListIfNotNull(criteriaList, "tags", filterObjects.getTags());

        // ... Repeat for other parameters

        if (filterObjects.getStartDate() != null && filterObjects.getEndDate() != null) {
            criteriaList.add(Criteria.where("createdDateTime").gte(filterObjects.getStartDate()).lte(filterObjects.getEndDate()));
        }
        if(filterObjects.getStartDate()==null|| filterObjects.getStartDate().equals("")){
            filterObjects.setStartDate(LocalDateTime.now());
            filterObjects.setStartDate(filterObjects.getStartDate().withHour(0).withMinute(0).withSecond(1));
            criteriaList.add(Criteria.where("createdDateTime").gte(filterObjects.getStartDate()));
        }
        if(filterObjects.getItemBO()!=null&& !filterObjects.getItemBO().isEmpty()){
            criteriaList.add(Criteria.where("done").is(true));
        }

        return new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
    }

    private void addToCriteriaListIfNotNull(List<Criteria> criteriaList, String field, Object value) {
        if (value != null && value!= "") {
            criteriaList.add(Criteria.where(field).is(value));
        }
    }



    @Override
    public List<DownTimeByResourcePerDay> getDownTimePerDay(List<Oee> oeeData) {
        return oeeData.stream()
                .collect(Collectors.groupingBy(
                        oee -> oee.getResourceId() + "-" + oee.getCreatedDateTime().toLocalDate(),
                        Collectors.summingDouble(Oee::getTotalDowntime)))
                .entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    LocalDate date = LocalDate.parse(parts[1] + "-" + parts[2]+"-"+parts[3]);

                    return new DownTimeByResourcePerDay(parts[0], entry.getValue(), date);
                })
                .collect(Collectors.toList());
    }



    public List<SpeedLossByResource> getSpeedLossByResource(List<Oee> oeeData) {
        Map<String, Map<LocalDate, Double>> speedLossMap = oeeData.stream()
                .collect(Collectors.groupingBy(Oee::getResourceId,
                        Collectors.groupingBy(oee -> oee.getCreatedDateTime().toLocalDate(),
                                Collectors.summingDouble(Oee::getSpeedLoss))));

        List<SpeedLossByResource> speedLossList = new ArrayList<>();
        speedLossMap.forEach((resource, dateMap) ->
                dateMap.forEach((date, totalSpeedLoss) ->
                        speedLossList.add(new SpeedLossByResource(totalSpeedLoss, resource, date))));

        return speedLossList;
    }

    public List<PerformanceByResourcePerDay> getPerformanceByResourcePerDay(List<Oee> oeeData) {
        return oeeData.stream()
                .collect(Collectors.groupingBy(oee -> oee.getResourceId() + "-" + oee.getCreatedDateTime().toLocalDate(),
                        Collectors.averagingDouble(Oee::getPerformance)))
                .entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    LocalDate date = LocalDate.parse(parts[1] + "-" + parts[2]+"-"+parts[3]);

                    return new PerformanceByResourcePerDay(entry.getValue(), parts[0], date);
                })
                .collect(Collectors.toList());
    }

    public List<QualityByResourcePerDay> getQualityByResourcePerDay(List<Oee> oeeData) {
        return oeeData.stream()
                .collect(Collectors.groupingBy(oee -> oee.getResourceId() + "-" + oee.getCreatedDateTime().toLocalDate(),
                        Collectors.averagingDouble(Oee::getQuality)))
                .entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    LocalDate date = LocalDate.parse(parts[1] + "-" + parts[2]+"-"+parts[3]);
                    return new QualityByResourcePerDay(entry.getValue(), parts[0],date);
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<OEEByItem> getOEEByItem(List<Oee> oeeList) {
        return oeeList.stream()
                .filter(oee -> oee.getItemBO() != null && !oee.getItemBO().isEmpty())
                .collect(Collectors.groupingBy(
                        oee -> oee.getItemBO().toLowerCase(), // Convert to lowercase
                        Collectors.averagingDouble(Oee::getOee)))
                .entrySet().stream()
                .map(entry -> new OEEByItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TrendAnalysis> getTrendAnalysis(List<Oee> oeeData) {
        return new ArrayList<>(oeeData.stream()
                .collect(Collectors.groupingBy(
                        oee -> oee.getCreatedDateTime().toLocalDate(),
                        Collectors.reducing(
                                new TrendAnalysis(0, 0, null),
                                oee -> new TrendAnalysis(
                                        oee.getCount(),
                                        oee.getTargetValue(),
                                        oee.getCreatedDateTime().toLocalDate()),
                                (t1, t2) -> new TrendAnalysis(
                                        t1.getActualValue() + t2.getActualValue(),
                                        t1.getTargetValue() + t2.getTargetValue(),
                                        t1.getDate() != null ? t1.getDate() : t2.getDate()))))
                .values());
    }




    @Override
    public List<ProductionComparison> getComparison(List<Oee> oeeData) {
        return new ArrayList<>(oeeData.stream()
                .collect(Collectors.groupingBy(
                        Oee::getResourceId,
                        Collectors.reducing(new ProductionComparison(0, 0, null),
                                oee -> new ProductionComparison(oee.getCount(),
                                        oee.getTargetValue(),
                                        oee.getResourceId()),
                                (p1, p2) -> new ProductionComparison(p1.getActualValue() + p2.getActualValue(),
                                        p1.getTargetValue() + p2.getTargetValue(),
                                        p1.getResource()!=null ? p1.getResource():p2.getResource()))
                ))
                .values());
    }

    @Override
    public List<ScrapRateByResource> getScrap(List<Oee> oeeData) {
        return oeeData.stream()
                .collect(Collectors.groupingBy(
                        Oee::getResourceId,
                        Collectors.summingInt(Oee::getScrapQuantity)))
                .entrySet().stream()
                .map(entry -> new ScrapRateByResource(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TotalCountByResource> getTotalCountByResource(List<Oee> oeeData) {
        return oeeData.stream()
                .collect(Collectors.groupingBy(
                        Oee::getResourceId,
                        Collectors.reducing(
                                new TotalCountByResource(0, 0, 0, null),
                                oee -> new TotalCountByResource(
                                        oee.getCount() - oee.getScrapQuantity(),
                                        oee.getScrapQuantity(),
                                        oee.getCount(),
                                        oee.getResourceId()), // Ensure resource ID is set here
                                (t1, t2) -> new TotalCountByResource(
                                        t1.getGoodCount() + t2.getGoodCount(),
                                        t1.getBadCount() + t2.getBadCount(),
                                        t1.getTotalCount() + t2.getTotalCount(),
                                        t1.getResource() !=null ? t1.getResource():t2.getResource())
                        )))
                .values()
                .stream()
                .filter(totalCountByResource -> totalCountByResource.getResource() != null && !totalCountByResource.getResource().isEmpty()) // Filter out null or empty resourceIds
                .map(totalCountByResource -> new TotalCountByResource(
                        totalCountByResource.getGoodCount(),
                        totalCountByResource.getBadCount(),
                        totalCountByResource.getTotalCount(),
                        totalCountByResource.getResource())) // Use getResource() directly
                .collect(Collectors.toList());
    }






    public List<OEEByResource> getOEEByResource(List<Oee> oeeList) {

        return oeeList.stream()
                .collect(Collectors.groupingBy(oee -> oee.getResourceId() ,
                        Collectors.averagingDouble(Oee::getOee)))
                .entrySet().stream()
                .map(entry -> {

                    return new OEEByResource(entry.getKey(), entry.getValue());
                })
                .collect(Collectors.toList());
    }

    @Override
    public FinalOEE getOEECompleteResult(FilterObjects filterObjects) {
        List<Oee> getOeeData = getOEE(filterObjects);//done
        List<Oee> oeeData = filterOperationCompleteWhenItsDone(getOeeData);
        List<Oee> lastEntryTimeRecord=getLastEntryRecord(getOeeData);
        FinalOEE getOeeValues= getOeeValue(lastEntryTimeRecord);
        List<OEEByItem> oeeByItemData = getOEEByItem(oeeData);//done
        List<OEEByResource> oeeByResourceData = getOEEByResource(oeeData); //done

        // Additional methods
        List<AvailabilityByResource> availabilityByResource = getAvailabilityByResource(oeeData);//done
        List<DownTimeByResource> downTimeByResourceData = getDownTimeByResource(oeeData);
        List<DownTimeByReason> downTimeByReasonData = getDownTimeByReason(oeeData);//done
        List<DownTimeByResourcePerDay> downTimePerDayData =  getDownTimePerDay(oeeData);
        List<SpeedLossByResource> speedLossByResourceData = getSpeedLossByResource(oeeData); //done
        List<PerformanceByResourcePerDay> performanceByResourcePerDayData = getPerformanceByResourcePerDay(oeeData);//done
        List<QualityByResourcePerDay> qualityByResourcePerDayData = getQualityByResourcePerDay(oeeData);//done
        List<TrendAnalysis> trendAnalysisData = getTrendAnalysis(oeeData);
        List<ProductionComparison> productionComparison = getComparison(oeeData);
        List<ScrapRateByResource> scrapRateData = getScrap(oeeData);
        List<TotalCountByResource> totalCountByResourceData = getTotalCountByResource(lastEntryTimeRecord);

        return new FinalOEE(oeeData, oeeByItemData, oeeByResourceData,
                availabilityByResource, downTimeByReasonData,downTimeByResourceData, downTimePerDayData,
                speedLossByResourceData, performanceByResourcePerDayData, qualityByResourcePerDayData, trendAnalysisData, productionComparison,scrapRateData, totalCountByResourceData , getOeeValues.getAvailability(), getOeeValues.getPerformance(), getOeeValues.getQuality(), getOeeValues.getOee());
    }

    private List<Oee> getLastEntryRecord(List<Oee> getOeeData) {
        Map<LocalDate, List<Oee>> groupedByDate = groupByDate(getOeeData);
        List<Oee> finalOeeList = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Oee>> dateEntry : groupedByDate.entrySet()) {
            Map<String, List<Oee>> groupedByShift = groupByShift(dateEntry.getValue());

            for (Map.Entry<String, List<Oee>> shiftEntry : groupedByShift.entrySet()) {
                List<Oee> sortedOeeList = shiftEntry.getValue().stream()
                        .sorted(Comparator.comparing(Oee::getEntryTime).reversed())
                        .collect(Collectors.toList());

                if (!sortedOeeList.isEmpty()) {
                    LocalDateTime maxEntryTime = LocalDateTime.parse(sortedOeeList.get(0).getEntryTime(), formatter);
                    LocalDateTime oneMinuteBeforeMaxEntryTime = maxEntryTime.minusMinutes(1);

                    List<Oee> filteredOeeList = sortedOeeList.stream()
                            .filter(oee -> {
                                LocalDateTime entryTime = LocalDateTime.parse(oee.getEntryTime(), formatter);
                                return !entryTime.isBefore(oneMinuteBeforeMaxEntryTime) && !entryTime.isAfter(maxEntryTime);
                            })
                            .collect(Collectors.toList());

                    finalOeeList.addAll(filteredOeeList);
                }
            }
        }

        return finalOeeList;
    }
    private Map<LocalDate, List<Oee>> groupByDate(List<Oee> getOeeData) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return getOeeData.stream()
                .collect(Collectors.groupingBy(oee -> LocalDate.parse(oee.getEntryTime().substring(0, 10), dateFormatter)));
    }
    private Map<String, List<Oee>> groupByShift(List<Oee> oeeList) {
        return oeeList.stream()
                .collect(Collectors.groupingBy(Oee::getShift));
    }

    private FinalOEE getOeeValue(List<Oee> getOeeData) {
        double totalPerformance = 0;
        double totalAvailability = 0;
        double totalQuality = 0;
int count =0;
        if(getOeeData!=null) {
            for (Oee oee : getOeeData) {
                totalPerformance += oee.getPerformance();
                totalAvailability += oee.getAvailability();
                totalQuality += oee.getQuality();
                count++;
            }
        }
        totalAvailability=totalAvailability/count;
        totalPerformance=totalPerformance/count;
        totalQuality=totalQuality/count;
        double oee=(totalAvailability/100) * (totalPerformance/100) * (totalQuality/100) * 100;

        FinalOEE finalOEE=FinalOEE.builder().oee(oee).availability(totalAvailability).performance(totalPerformance).quality(totalQuality).build();
       return finalOEE;
    }

    private List<Oee> filterOperationCompleteWhenItsDone(List<Oee> oeeData) {
        Map<String, List<Oee>> groupedOee = oeeData.stream()
                .collect(Collectors.groupingBy(oee -> oee.getOperationBO() + "_" + oee.getItemBO() + "_" +
                        oee.getShoporderBO() + "_" + oee.getRoutingBO() + "_" +
                        oee.getResourceId()));

        List<Oee> filteredOee = new ArrayList<>();
        for (List<Oee> oeeList : groupedOee.values()) {
            Optional<Oee> doneRecord = oeeList.stream().filter(Oee::isDone).findFirst();
            if (doneRecord.isPresent()) {
                filteredOee.add(doneRecord.get());
            } else {
                filteredOee.addAll(oeeList);
            }
        }

        return filteredOee;
    }


    @Override
    public FinalOEE getOEECompleteResultForLiveData(FilterObjects filterObject) {
        String site= filterObject.getSite();
        ShiftRequest shiftRequest;
        boolean resourcePresent=false;
        if(filterObject.getResourceId()!=null&& !filterObject.getResourceId().isEmpty()) {
            resourcePresent=true;






        shiftRequest=ShiftRequest.builder().site(site).shiftType("Resource").resource(filterObject.getResourceId()).build();
        }else {
            shiftRequest = ShiftRequest.builder().site(site).shiftType("General").build();
        }
        PlannedMinutes getPlannedMinutes=getPlannedMinutes(shiftRequest);
        if(filterObject.getShift()!=null && !filterObject.getShift().isEmpty()){
            if(getPlannedMinutes.getShiftName()!=null && !getPlannedMinutes.getShiftName().isEmpty()) {
                if (!getPlannedMinutes.getShiftName().equalsIgnoreCase(filterObject.getShift())) {
                    throw new OeeException(4);
                }
            }
        }
        String shiftStartTime= getPlannedMinutes.getStartTime();
        LocalDateTime currentTime=LocalDateTime.now();
        LocalDate localDate= currentTime.toLocalDate();
        LocalDateTime shiftStartDateTime=parseStringToLocalDateTime(localDate,shiftStartTime);
        //call Availibility. from availability to perforamnce downTimeAvailabilityToDownTimeBuilder then call calculatePerformanceForLiveData,
        // i have made call my performance to quality to oee.. so find a way to get the oee object and create a new method to get data from repository till now and add the object which we made to that list
        // then create a clone method of getOEECompleteResult. instead of  List<Oee> oeeData = getOEE(filterObjects); this line... assign the list<Oee> which we collected . done
        filterObject.setStartDate(shiftStartDateTime);
        filterObject.setEndDate(currentTime);
        filterObject.setShift(getPlannedMinutes.getShiftName());
        List<Oee> getOeeData = new ArrayList<>();


        List<Oee> liveData=getAvailabilityForLive(site,filterObject.getResourceId(),resourcePresent);
        if(liveData!=null && !liveData.isEmpty()){
            List<Oee> templist = filterOeeList(liveData,filterObject);
            getOeeData.addAll(templist);
        }
        List<Oee> oeeData = filterOperationCompleteWhenItsDone(getOeeData);
        List<Oee> lastEntryRecord=getLastEntryRecord(getOeeData);
        FinalOEE getOeeValues= getOeeValue(lastEntryRecord);
        List<OEEByItem> oeeByItemData = getOEEByItem(oeeData);//done
        List<OEEByResource> oeeByResourceData = getOEEByResource(oeeData); //done

        // Additional methods
        List<AvailabilityByResource> availabilityByResource = getAvailabilityByResource(oeeData);//done
        List<DownTimeByResource> downTimeByResourceData = getDownTimeByResource(oeeData);
        List<DownTimeByReason> downTimeByReasonData = getDownTimeByReason(oeeData);//done
        List<DownTimeByResourcePerDay> downTimePerDayData =  getDownTimePerDay(oeeData);
        List<SpeedLossByResource> speedLossByResourceData = getSpeedLossByResource(oeeData); //done
        List<PerformanceByResourcePerDay> performanceByResourcePerDayData = getPerformanceByResourcePerDay(oeeData);//done
        List<QualityByResourcePerDay> qualityByResourcePerDayData = getQualityByResourcePerDay(oeeData);//done
        List<TrendAnalysis> trendAnalysisData = getTrendAnalysis(oeeData);
        List<ProductionComparison> productionComparison = getComparison(oeeData);
        List<ScrapRateByResource> scrapRateData = getScrap(oeeData);
        List<TotalCountByResource> totalCountByResourceData = getTotalCountByResource(lastEntryRecord);

        return new FinalOEE(oeeData, oeeByItemData, oeeByResourceData,
                availabilityByResource, downTimeByReasonData,downTimeByResourceData, downTimePerDayData,
                speedLossByResourceData, performanceByResourcePerDayData, qualityByResourcePerDayData, trendAnalysisData, productionComparison,scrapRateData, totalCountByResourceData, getOeeValues.getAvailability(), getOeeValues.getPerformance(), getOeeValues.getQuality(), getOeeValues.getOee());

    }
public List<Oee> filterOeeList(List<Oee> oeeList, FilterObjects filterObjects) {
    if (oeeList == null) {
        return Collections.emptyList();
    }
    return oeeList.stream()
            .filter(Objects::nonNull)
            .filter(oee -> filterObjects.getShopOrderBO() == null || filterObjects.getShopOrderBO().isEmpty() || (oee.getShoporderBO() != null && oee.getShoporderBO().equals(filterObjects.getShopOrderBO())))
            .filter(oee -> filterObjects.getItemBO() == null || filterObjects.getItemBO().isEmpty() || (oee.getItemBO() != null && oee.isDone() && oee.getItemBO().equals(filterObjects.getItemBO())))
            .filter(oee -> filterObjects.getRoutingBO() == null || filterObjects.getRoutingBO().isEmpty() || (oee.getRoutingBO() != null && oee.getRoutingBO().equals(filterObjects.getRoutingBO())))
            .filter(oee -> filterObjects.getWorkCenterBO() == null || filterObjects.getWorkCenterBO().isEmpty() || (oee.getWorkcenterBO() != null && oee.getWorkcenterBO().equals(filterObjects.getWorkCenterBO())))
            .filter(oee -> filterObjects.getOperationBO() == null || filterObjects.getOperationBO().isEmpty() || (oee.getOperationBO() != null && oee.getOperationBO().equals(filterObjects.getOperationBO())))
            .filter(oee -> filterObjects.getResourceId() == null || filterObjects.getResourceId().isEmpty() || (oee.getResourceId() != null && oee.getResourceId().equals(filterObjects.getResourceId())))
            .collect(Collectors.toList());
}
    private List<Oee> getAvailabilityForLive(String site, String resourceId,boolean resourcePresent) {
        OeeRequest oeeRequest;
        if(resourcePresent) {
           oeeRequest= OeeRequest.builder().site(site).resourceId(resourceId).build();
        }else{
             oeeRequest = OeeRequest.builder().site(site).build();
        }
        List<Oee> oeeList=webClientBuilder.build()
                .post()
                .uri(getAvailablityForLiveUrl)
                .bodyValue(oeeRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Oee>>() {
                })
                .block();
        return oeeList;
    }

    @Override
    public OeeRequestList getQualityToOeeRequest(QualityRequestList qualityRequestList) {
        List<OeeRequest> oeeRequests = new ArrayList<>();
       List<Quality> qualityList= qualityRequestList.getQualityResponseList();
      for(Quality quality: qualityList){
          OeeRequest oeeRequest =  OeeRequest.builder()
                  .uniqueId(quality.getUniqueId())
                  .site(quality.getSite())
                  .resourceId(quality.getResourceId())
                  .createdDateTime(quality.getCreatedDateTime())
                  .shiftStartTime(quality.getShiftStartTime())
                  .shift(quality.getShift())
                  .tags(quality.getTags())
                  .entryTime(quality.getEntryTime())
                  .plannedProductionTime(quality.getPlannedProductionTime())
                  .totalDowntime(quality.getTotalDowntime())
                  .operatingTime(quality.getOperatingTime())
                  .breakHours(quality.getBreakHours())
                  .availability(quality.getAvailability())
                  .performance(quality.getPerformance())
                  .count(quality.getCount())
                  .calculatedCycleTime(quality.getCalculatedCycleTime())
                  .reasonCode(quality.getReasonCode())
                  .active(quality.getActive())
                  .event(quality.getEvent())
                  .itemBO(quality.getItemBO())
                  .routingBO(quality.getRoutingBO())
                  .operationBO(quality.getOperationBO())
                  .shoporderBO(quality.getShoporderBO())
                  .workcenterBO(quality.getWorkcenterBO())
                  .speedLoss(quality.getSpeedLoss())
                  .idealTime(quality.getIdealTime())
                  .scrapQuantity(quality.getScrapQuantity())
                  .quality(quality.getQuality())
                  .eventPerformance(quality.getEventPerformance())
                  .actualValue(quality.getActualValue())
                  .targetValue(quality.getTargetValue())
                  .done(quality.isDone())
                  .build();

          oeeRequests.add(oeeRequest);
      }

        OeeRequestList oeeRequestList = new OeeRequestList();
        oeeRequestList.setOeeRequestList(oeeRequests);
        return oeeRequestList;
    }

    @Override
    public FinalOEE retrieveOee(FilterObjects filterObjects) throws Exception {
        List<Oee> getOeeData = getOEEByTags(filterObjects); // done
        if(getOeeData.isEmpty()){
            filterObjects.setTags(null);
            getOeeData=getOEE(filterObjects);
        }
        List<Oee> oeeData = filterOperationCompleteWhenItsDone(getOeeData);

        // Create a map with shiftName as key and list of Oee as value
      List<Oee> lastEntryTimeRecord=getLastEntryRecord(oeeData);

        FinalOEE getOeeValues = getOeeValue(lastEntryTimeRecord);
        List<OEEByItem> oeeByItemData = getOEEByItem(oeeData); // done
        List<OEEByResource> oeeByResourceData = getOEEByResource(oeeData); // done

        // Additional methods
        List<AvailabilityByResource> availabilityByResource = getAvailabilityByResource(oeeData); // done
        List<DownTimeByResource> downTimeByResourceData = getDownTimeByResource(oeeData);
        List<DownTimeByReason> downTimeByReasonData = getDownTimeByReason(oeeData); // done
        List<DownTimeByResourcePerDay> downTimePerDayData = getDownTimePerDay(oeeData);
        List<SpeedLossByResource> speedLossByResourceData = getSpeedLossByResource(oeeData); // done
        List<PerformanceByResourcePerDay> performanceByResourcePerDayData = getPerformanceByResourcePerDay(oeeData); // done
        List<QualityByResourcePerDay> qualityByResourcePerDayData = getQualityByResourcePerDay(oeeData); // done
        List<TrendAnalysis> trendAnalysisData = getTrendAnalysis(oeeData);
        List<ProductionComparison> productionComparison = getComparison(oeeData);
        List<ScrapRateByResource> scrapRateData = getScrap(oeeData);
        List<TotalCountByResource> totalCountByResourceData = getTotalCountByResource(lastEntryTimeRecord);

        return new FinalOEE(oeeData, oeeByItemData, oeeByResourceData,
                availabilityByResource, downTimeByReasonData, downTimeByResourceData, downTimePerDayData,
                speedLossByResourceData, performanceByResourcePerDayData, qualityByResourcePerDayData, trendAnalysisData, productionComparison, scrapRateData, totalCountByResourceData,
                getOeeValues.getAvailability(), getOeeValues.getPerformance(), getOeeValues.getQuality(), getOeeValues.getOee());
    }


    private String getTags(FilterObjects filterObjects) {
        String tags=null;
        if(filterObjects.getResourceId()!=null&&!filterObjects.getResourceId().isEmpty()) {
             tags = "Resource:" + filterObjects.getResourceId();
        }
        if(filterObjects.getItemBO()!=null&&!filterObjects.getItemBO().isEmpty()){
            tags+="-Material:"+filterObjects.getItemBO();
        }
        if(filterObjects.getShopOrderBO()!=null&&!filterObjects.getShopOrderBO().isEmpty()){
            tags+="-ShopOrder:"+filterObjects.getShopOrderBO();
        } if(filterObjects.getOperationBO()!=null&&!filterObjects.getOperationBO().isEmpty()){
            tags+="-Operation:"+filterObjects.getOperationBO();
        } if(filterObjects.getRoutingBO()!=null&&!filterObjects.getRoutingBO().isEmpty()){
            tags+="-Routing:"+filterObjects.getRoutingBO();
        }
        return tags;
    }
    private List<Oee> getOEEByTags(FilterObjects filterObjects) {
        String tags= getTags(filterObjects);
        FilterObjects objects= new FilterObjects();
        objects.setSite(filterObjects.getSite());
        objects.setTags(tags);
        objects.setShift(filterObjects.getShift());
        objects.setStartDate(filterObjects.getStartDate());
        objects.setEndDate(filterObjects.getEndDate());


        Criteria criteria= buildFilterCriteria(objects);
        MatchOperation matchOperation = Aggregation.match(criteria);

        // Add your OEE calculation stages here
        // ...

        Aggregation aggregation = Aggregation.newAggregation(matchOperation /* other stages */);

        return mongoTemplate.aggregate(aggregation, "R_OEE", Oee.class).getMappedResults();
    }

    private LocalDateTime parseStringToLocalDateTime(LocalDate localDate, String shiftStartTime) {
        LocalTime time = LocalTime.parse(shiftStartTime, TIME_FORMATTER);
        return LocalDateTime.of(localDate, time);
    }

    private PlannedMinutes getPlannedMinutes(ShiftRequest shiftRequest) {
        PlannedMinutes plannedMinutes = webClientBuilder.build()
                .post()
                .uri(getPlannedTimeUrl)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(PlannedMinutes.class)
                .block();
        if(plannedMinutes == null || plannedMinutes.getShiftName() == null || plannedMinutes.getShiftName().isEmpty()){
            throw new OeeException(3);
        }
        return plannedMinutes;
    }


}
