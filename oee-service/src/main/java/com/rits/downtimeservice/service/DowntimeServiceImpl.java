package com.rits.downtimeservice.service;

import com.rits.downtimeservice.dto.*;
import com.rits.downtimeservice.event.MachineUpEvent;
import com.rits.downtimeservice.exception.DownTimeException;
import com.rits.downtimeservice.model.*;
import com.rits.downtimeservice.repository.DownTimeRepository;
import com.rits.downtimeservice.repository.DownTimeRepositoryImpl;
import com.rits.oeeservice.model.Oee;
import com.rits.performanceservice.dto.Combinations;
import com.rits.performanceservice.dto.DownTime;
import com.rits.performanceservice.dto.PerformanceRequestList;
import com.rits.performanceservice.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DowntimeServiceImpl implements DowntimeService {
    private final DownTimeRepository downTimeRepository;
    private final WebClient.Builder webClientBuilder;
    private final DownTimeRepositoryImpl downTimeRepositoryImpl;
    private final PerformanceService performanceService;
    private final MongoTemplate mongoTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;
    private int plannedproductionTime = 0;
    private int breakminutes = 0;
    private int totalDowntime = 0;
    private int operatingTime = 0;
    private String shiftendTime;
    private String shiftStartTime;
    @Value("${shift-service.url}/getPlannedTimeTillNowByType")
    private String getPlannedTimeUrl;
    @Value("${shift-service.url}/getBreakHoursTillNowByType")
    private String getBreakHoursTillNowUrl;
    @Value("${machinestatus-service.url}/getMcDownRecord")
    private String geActiveMachineLog;
    @Value("${shift-service.url}/getBreakHours")
    private String getBreakHoursUrl;
    @Value("${resource-service.url}/retrieveBySite")
    private String getAllResourceUrl;
    @Value("${machinestatus-service.url}/getMachineStatus")
    private String getMachineStatusUrl;
    @Value("${productionlog-service.url}/save")
    private String productionLogUrl;

    public DownTimeMessageModel logDownTimeandAvailability() throws Exception {
        getTotalBreakTillNow("RITS");
        List<AggregatedResult> aggregatedResults = downTimeRepositoryImpl.aggregateByDateRange(shiftStartTime, shiftendTime);
        DownTimeAvailability downTimeAvailability = new DownTimeAvailability();

        for (AggregatedResult result : aggregatedResults) {
            int totalPlannedProductionTime = result.getTotalPlannedProductionTime();
            int totalDowntime = result.getTotalDowntime();
            int totalOperatingTime = result.getTotalOperatingTime();
            int totalMcBreakDownHours = result.getTotalMcBreakDownHours();
            String resource = result.getResourceId();
            LocalDateTime currentDateTime = LocalDateTime.now();


          /* downTimeAvailability = DownTimeAvailability.builder().uniqueId(generateUniqueId())
                    .event(result.getEvent())
                    .resourceId(result.getResource())
                    .createdTime(result.getDowntimeEnd())
                    .createdDateTime(currentDateTime.toString())
                    .shift(result.getShift())
                    .entryTime(result.getDowntimeEnd())
                    .shiftStartDate(result.getShiftStartTime())
                    .plannedProductionTime(totalPlannedProductionTime)
                    .totalDowntime(totalDowntime)
                    .operatingTime(totalOperatingTime)
                    .availability(getAvailability())
                    .mcBreakDownHours(totalMcBreakDownHours)
                    .availability(getAvailability())
                    .shiftEndDate(result.getShiftEndTime())
                    .active("1")
                    .build();*/

            downTimeRepository.save(downTimeAvailability);
        }
        return DownTimeMessageModel.builder().response(downTimeAvailability).message_details(new DownTimeMessageDetails(downTimeAvailability.getEvent() + " Status logged Successfully", "S")).build();
    }

    private DownTimeMessageModel logDTandAVatDayEnd(DownTimeRequest downTimeRequest) throws Exception {
        List<AggregatedResult> aggregatedResults = downTimeRepositoryImpl.customAggregationForToday();
        DownTimeAvailability downTimeAvailability = new DownTimeAvailability();

        for (AggregatedResult result : aggregatedResults) {
            int totalPlannedProductionTime = result.getTotalPlannedProductionTime();
            int totalDowntime = result.getTotalDowntime();
            int totalOperatingTime = result.getTotalOperatingTime();
            int totalMcBreakDownHours = result.getTotalMcBreakDownHours();
            LocalDateTime currentDateTime = LocalDateTime.now();

            downTimeAvailability = DownTimeAvailability.builder().uniqueId(generateUniqueId())
                    .event(downTimeRequest.getEvent())
                    .resourceId(downTimeRequest.getResource())
                    .createdTime(downTimeRequest.getDowntimeEnd())
                    .createdDateTime(currentDateTime.toString())
                    .shift(downTimeRequest.getShift())
                    .entryTime(downTimeRequest.getDowntimeEnd())
                    .shiftStartDate(downTimeRequest.getShiftStartTime())
                    .plannedProductionTime(totalPlannedProductionTime)
                    .totalDowntime(totalDowntime)
                    .operatingTime(totalOperatingTime)
                    .availability(getAvailability())
                    .mcBreakDownHours(totalMcBreakDownHours)
                    .availability(getAvailability())
                    .shiftEndDate(downTimeRequest.getShiftEndTime())
                    .active(1)
                    .build();

            downTimeRepository.save(downTimeAvailability);
        }

        return DownTimeMessageModel.builder().response(downTimeAvailability).message_details(new DownTimeMessageDetails(downTimeAvailability.getEvent() + " Status logged Successfully", "S")).build();
    }

    @Override
    public DownTimeMessageModel logDownTimeandAvailability(DownTimeRequest downTimeRequest) throws Exception {
        DownTimeMessageModel resultDowntTimeMessage = null;
        // below used to test the performance link module.
/*        resultDowntTimeMessage = new DownTimeMessageModel();
        DownTimeMessageDetails downtimeDetailsMs = new DownTimeMessageDetails();
        downtimeDetailsMs.setMsg("Tested For Integration Purpose");
        downtimeDetailsMs.setMsg_type("OEE PErformance");
        resultDowntTimeMessage.setMessage_details(downtimeDetailsMs);
        DownTimeAvailability testAvail = new DownTimeAvailability();
        testAvail.setEvent(downTimeRequest.getEvent());
        testAvail.setShift(downTimeRequest.getShift());
        resultDowntTimeMessage.setResponse(testAvail);*/

        DownTimeMessageModel messageModel = new DownTimeMessageModel();
        DownTimeAvailability downTimeAvailability = new DownTimeAvailability();
        LocalDateTime currentDateTime = LocalDateTime.now();

        if (downTimeRequest.getEvent().equalsIgnoreCase("MC_UP") || downTimeRequest.getEvent().equalsIgnoreCase("TIME")) {
            int totalDownTime = getTotalDownTimeByShift(downTimeRequest.getResource(), downTimeRequest.getSite(), downTimeRequest.getShiftStartTime(), downTimeRequest.getCreatedDateTime()) + Integer.parseInt(downTimeRequest.getMeantime());
            if(totalDownTime>Integer.parseInt(downTimeRequest.getShiftAvailableTime())){
                totalDownTime=Integer.parseInt(downTimeRequest.getShiftAvailableTime());
            }
            int operatingTime = Math.max(Integer.parseInt(downTimeRequest.getShiftAvailableTime()) - totalDownTime, 0);
            MachineStatus machineStatus = getAllActiveMachineLog(downTimeRequest.getSite(),downTimeRequest.getResource());
            double availability = ((double) operatingTime / Integer.parseInt(downTimeRequest.getShiftAvailableTime())) * 100;

            downTimeAvailability = DownTimeAvailability.builder().uniqueId(generateUniqueId())
                    .event(downTimeRequest.getEvent())
                    .resourceId(downTimeRequest.getResource())
                    .site(downTimeRequest.getSite())
                    .createdTime(downTimeRequest.getDowntimeEnd())
                    .createdDateTime(downTimeRequest.getCreatedDateTime())
                    .shift(downTimeRequest.getShift())
                    .entryTime(downTimeRequest.getDowntimeEnd())
                    .shiftStartDate(downTimeRequest.getShiftStartTime())
                    .shiftEndDate(downTimeRequest.getShiftEndTime())
                    .plannedProductionTime(Integer.parseInt(downTimeRequest.getShiftAvailableTime()))
                    .totalDowntime(totalDownTime)
                    .operatingTime(operatingTime)
                    .availability((int) availability)
                    .mcBreakDownHours(totalDownTime)
                    .reasonCode(downTimeRequest.getReasonCode())
                    .active(1)
                    .processed(false)
                    .build();
            if(machineStatus != null && StringUtils.isNotEmpty(machineStatus.getReasonCode()))
            {
                downTimeAvailability.setReasonCode(machineStatus.getReasonCode());
            }

        }
        if (downTimeRequest.getEvent().equalsIgnoreCase("DAY_END")) {
            logDTandAVatDayEnd(downTimeRequest);
        }

        resultDowntTimeMessage = DownTimeMessageModel.builder().response(downTimeRepository.save(downTimeAvailability)).message_details(new DownTimeMessageDetails(downTimeAvailability.getEvent() + " Status logged Successfully", "S")).build();
        applicationEventPublisher.publishEvent(resultDowntTimeMessage);

        return resultDowntTimeMessage;
    }

    private int getTotalDownTimeByShift(String resourceId, String site, String shift, String createdDateTime) {
        LocalDateTime providedDateTime = LocalDateTime.parse(createdDateTime, DateTimeFormatter.ISO_DATE_TIME);

        Optional<DownTimeAvailability> optionalDownTimeAvailability = downTimeRepository.findTopBySiteAndResourceIdAndShiftStartDateOrderByCreatedDateTimeDesc(site, resourceId, shift);

        if (optionalDownTimeAvailability.isPresent()) {
            DownTimeAvailability downTimeAvailability = optionalDownTimeAvailability.get();

            LocalDateTime recordDateTime = LocalDateTime.parse(downTimeAvailability.getCreatedDateTime(), DateTimeFormatter.ISO_DATE_TIME);

            if (providedDateTime.toLocalDate().isEqual(recordDateTime.toLocalDate())) {
                return downTimeAvailability.getTotalDowntime();
            }
        }

        return 0;
    }

    @Override
    public DownTimeMessageModel logDownTimeandAvailabilitybyProductionLog(ProductionLogRequest productionLog) throws Exception {
        DownTimeRequest downTimeRequest = new DownTimeRequest();
        ProductionLogRequest productionLogRequest = productionLog;
        downTimeRequest.setEvent(productionLogRequest.getEventType());
        downTimeRequest.setResource(productionLogRequest.getResourceBO());  // to be changed Resource
        downTimeRequest.setWorkcenter("WC1"); // for now hard coded. Priya to include the resource to workcenter
        downTimeRequest.setShift(productionLogRequest.getShiftName());
        downTimeRequest.setShiftStartTime(productionLogRequest.getShiftStartTime());
        downTimeRequest.setShiftEndTime(productionLogRequest.getShiftEndTime());
        downTimeRequest.setShiftAvailableTime(productionLogRequest.getShiftAvailableTime());
        downTimeRequest.setBreakHours(Integer.parseInt(productionLogRequest.getTotalBreakHours()));
        downTimeRequest.setCreatedDateTime(productionLogRequest.getTimestamp());
        downTimeRequest.setSite(productionLogRequest.getSite());
        downTimeRequest.setReasonCode(productionLogRequest.getReasonCode());
        DownTimeMessageModel outputDownTmeMsgModel = logDownTimeandAvailability(downTimeRequest);
        return outputDownTmeMsgModel;
    }

    @Override
    public DownTimeMessageModel logDownTimeandAvailabilitybyProductionLog(Object productionLog) throws Exception {
        DownTimeRequest downTimeRequest = new DownTimeRequest();
        ProductionLogRequest productionLogRequest = (ProductionLogRequest) productionLog;
        downTimeRequest.setEvent(productionLogRequest.getEventType());
        downTimeRequest.setResource(productionLogRequest.getResourceBO());  // to be changed Resource
        downTimeRequest.setWorkcenter("WC1"); // for now hard coded. Priya to include the resource to workcenter
        downTimeRequest.setShift(productionLogRequest.getShiftName());
        downTimeRequest.setShiftStartTime(productionLogRequest.getShiftStartTime());
        downTimeRequest.setShiftEndTime(productionLogRequest.getShiftEndTime());
        downTimeRequest.setShiftAvailableTime(productionLogRequest.getShiftAvailableTime());
        downTimeRequest.setBreakHours(Integer.parseInt(productionLogRequest.getTotalBreakHours()));
        downTimeRequest.setCreatedDateTime(productionLogRequest.getTimestamp());
        downTimeRequest.setSite(productionLogRequest.getSite());
        downTimeRequest.setReasonCode(productionLogRequest.getReasonCode());
        DownTimeMessageModel outputDownTmeMsgModel = logDownTimeandAvailability(downTimeRequest);
        return outputDownTmeMsgModel;
    }


    @EventListener
    public DownTimeMessageModel availabilityCalculationOnMachineUp(MachineUpEvent machineUpEvent) {
        DownTimeAvailability downTimeAvailability = DownTimeAvailability.builder().event(machineUpEvent.getSendResult().getEvent()).build();
        return DownTimeMessageModel.builder().response(downTimeRepository.save(downTimeAvailability)).message_details(new DownTimeMessageDetails(downTimeAvailability.getEvent() + " Status logged Successfully", "S")).build();
    }

    private int generateUniqueId() {
        UUID uuid = UUID.randomUUID();
        // Convert UUID to a positive long value
        return (int) (uuid.getMostSignificantBits() & Integer.MAX_VALUE);
    }

    private int getPlannedProductionTime(DownTimeRequest downTimeRequest) {
        plannedproductionTime = 0;
        ShiftRequest shiftRequest = ShiftRequest.builder().site(downTimeRequest.getSite()).shiftType("Resource").resource(downTimeRequest.getResource()).build();
        PlannedMinutes plannedProduction = webClientBuilder.build()
                .post()
                .uri(getPlannedTimeUrl)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(PlannedMinutes.class)
                .block();
        plannedproductionTime = plannedProduction.getPlannedTime();
        // MinutesList shiftResponseList=plannedProduction.getResponse();
        return plannedproductionTime;
    }

    private int getTotalBreakTillNow(DownTimeRequest downTimeRequest) {
        breakminutes = 0;
        ShiftRequest shiftRequest = ShiftRequest.builder().site(downTimeRequest.getSite()).shiftType("Resource").resource(downTimeRequest.getResource()).build();
        BreakMinutes breakMinutelist = webClientBuilder.build()
                .post()
                .uri(getBreakHoursTillNowUrl)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(BreakMinutes.class)
                .block();

        breakminutes = breakMinutelist.getBreakTime();
        shiftendTime = breakMinutelist.getEndTime();
        shiftStartTime = breakMinutelist.getStartTime();

        return breakminutes;
    }

    private int getTotalBreakTillNow(String site) {
        breakminutes = 0;
        ShiftRequest shiftRequest = new ShiftRequest();
        shiftRequest.setSite("RITS");
        BreakMinutes breakMinutelist = webClientBuilder.build()
                .post()
                .uri(getBreakHoursUrl)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(BreakMinutes.class)
                .block();

        shiftendTime = breakMinutelist.getEndTime();
        shiftStartTime = breakMinutelist.getStartTime();

        return breakminutes;
    }

    private String getDownTime() {
        String downTime = "";
        return downTime;
    }

    private int getTotalDownTime(DownTimeRequest downTimeRequest) {
        totalDowntime = 0;
        int downtime = convertTimeToSeconds(downTimeRequest.getMeantime());
        int breakhours = getTotalBreakTillNow(downTimeRequest);
        totalDowntime = downtime;
        return totalDowntime;
    }

    private int convertTimeToSeconds(String timeString) {
        String[] parts = timeString.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        int totalSeconds = (hours * 3600) + (minutes * 60) + seconds;
        return totalSeconds;
    }

    private int getOperatingTime() {
        operatingTime = 0;
        operatingTime = plannedproductionTime - totalDowntime - breakminutes;
        return operatingTime;
    }

    private int getAvailability() {
        double availability = (double) operatingTime / plannedproductionTime * 100;
        return (int) availability;
    }

    private MachineStatus getAllActiveMachineLog(String site,String resource) {
        MachineStatusRequest machineStatusRequest = new MachineStatusRequest();
        machineStatusRequest.setSite(site);
        machineStatusRequest.setResource(resource);
      MachineStatus  machineStatuses =  webClientBuilder.build()
                .post()
                .uri(geActiveMachineLog)
                .bodyValue(machineStatusRequest)
                .retrieve()
                .bodyToMono(MachineStatus.class)
                .block();
        return machineStatuses;
    }

    @Override
    public List<DownTimeAvailability> getUnproccessedRec() {
        List<DownTimeAvailability> unproccessedList = downTimeRepository.findByProcessed(false);
        return unproccessedList;
    }

    @Override
    public DownTimeMessageModel updateRec(DownTimeRequest downTimeRequest) {
        DownTimeAvailability downTimeAvailability = DownTimeAvailability.builder().uniqueId(downTimeRequest.getUniqueId())
                .event(downTimeRequest.getEvent())
                .resourceId(downTimeRequest.getResource())
                .createdDateTime(downTimeRequest.getDowntimeEnd())
                .shift(downTimeRequest.getShift())
                .entryTime(downTimeRequest.getDowntimeEnd())
                .shiftStartDate(downTimeRequest.getShift())
                .plannedProductionTime(downTimeRequest.getPlannedProductionTime())
                .totalDowntime(downTimeRequest.getTotalDowntime())
                .totalDowntime(downTimeRequest.getTotalDowntime())
                .operatingTime(downTimeRequest.getOperatingTime())
                .availability(downTimeRequest.getAvailability())
                .mcBreakDownHours(downTimeRequest.getMcBreakDownHours())
                .availability(downTimeRequest.getAvailability())
                .shiftStartDate(downTimeRequest.getShiftStartTime())
                .active(downTimeRequest.getActive())
                .processed(downTimeRequest.getProcessed())
                .build();

        return DownTimeMessageModel.builder().response(downTimeRepository.save(downTimeAvailability)).message_details(new DownTimeMessageDetails(downTimeAvailability.getEvent() + " DownTime Updated Successfully", "S")).build();

    }

    @Override
    public DownTimeMessageModel getAvailabilityForScheduler(String site, List<String> resourceList, List<Combinations> combinations) throws Exception {
        DownTimeMessageModel resultDowntTimeMessage = null;
        List<String> resources =new ArrayList<>();
        if(resourceList==null || resourceList.isEmpty()){
            resources=getAllResourceFromCombination(combinations);
        }

        if (resources != null && !resources.isEmpty()) {
            for (String resource : resources) {
                BreakMinutes getShiftDetails = getBreakHours(site, resource);
                resource = "ResourceBO:"+site+","+resource;
                List<MachineStatus> machineStatuses = getMachineStatusList(site, resource);
                if (machineStatuses != null && !machineStatuses.isEmpty()) {
                    MachineStatus machineStatus = machineStatuses.get(0);
                    if (machineStatus.getShiftStartTime() != null && !machineStatus.getShiftStartTime().isEmpty() &&
                            getShiftDetails.getStartTime() != null && !getShiftDetails.getStartTime().isEmpty() &&
                            machineStatus.getShiftStartTime().equals(getShiftDetails.getStartTime()) &&
                            machineStatus.getCreatedDate() != null && !machineStatus.getCreatedDate().isEmpty() &&
                            LocalDate.parse(machineStatus.getCreatedDate()).equals(LocalDate.now())
                    ) {
                    long meanTime = Duration.between(LocalDateTime.parse(machineStatus.getDowntimeStart()), LocalDateTime.now()).toSeconds();
                    int totalDownTime = (int) (getTotalDownTimeByShift(resource, site, getShiftDetails.getStartTime(), String.valueOf(LocalDateTime.now())) + meanTime) ;
                        if(totalDownTime>getShiftDetails.getPlannedTime()){
                            totalDownTime=getShiftDetails.getPlannedTime();
                        }
                    int operatingTime = getShiftDetails.getPlannedTime() - totalDownTime;
                    double availability = ((double) operatingTime / getShiftDetails.getPlannedTime()) * 100;

                    DownTimeAvailability downTimeAvailability = DownTimeAvailability.builder().uniqueId(generateUniqueId())
                            .event("SCHEDULER")
                            .site(site)
                            .resourceId(resource)
                            .createdDateTime(String.valueOf(LocalDateTime.now()))
                            .createdTime(String.valueOf(LocalTime.now()))
                            .shift(getShiftDetails.getShiftName())
                            .shiftEndDate(getShiftDetails.getEndTime())
                            .entryTime(String.valueOf(LocalDateTime.now()))
                            .shiftStartDate(getShiftDetails.getStartTime())
                            .plannedProductionTime(getShiftDetails.getPlannedTime())
                            .totalDowntime(totalDownTime)
                            .mcBreakDownHours(totalDownTime)
                            .operatingTime(operatingTime)
                            .availability((int) availability)
                            .active(1)
                            .processed(false)
                            .build();
                    downTimeAvailability.setCombinations(combinations);
                    resultDowntTimeMessage = DownTimeMessageModel.builder().response(downTimeRepository.save(downTimeAvailability)).message_details(new DownTimeMessageDetails(downTimeAvailability.getEvent() + " Status logged Successfully", "S")).build();

                    applicationEventPublisher.publishEvent(resultDowntTimeMessage);

                    }else{
                        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                                .eventType("MC_UP")
                                .site(machineStatus.getSite())
                                .resourceBO(machineStatus.getResource())
                                .eventData("Machine Up ")
                                .workCenterBO(machineStatus.getWorkcenter())
                                .data_field("SetUpState")
                                .data_value("Productive")
                                .reasonCode(machineStatus.getReasonCode())
                                .timestamp(LocalDateTime.now().toString())
                                .build();
                        Boolean productionLogged = webClientBuilder.build()
                                .post()
                                .uri(productionLogUrl)
                                .bodyValue(productionLogRequest)
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .block();

                        if (productionLogged) {
                            ProductionLogRequest productionLogRequestDown = ProductionLogRequest.builder()
                                    .eventType("MC_DOWN")
                                    .site(machineStatus.getSite())
                                    .resourceBO(machineStatus.getResource())
                                    .eventData("Machine Down")
                                    .workCenterBO(machineStatus.getWorkcenter())
                                    .data_field("SetUpState")
                                    .data_value(machineStatus.getEvent())
                                    .reasonCode(machineStatus.getReasonCode())
                                    .timestamp(LocalDateTime.now().toString())
                                    .build();
                            Boolean productionLoggedForDown = webClientBuilder.build()
                                    .post()
                                    .uri(productionLogUrl)
                                    .bodyValue(productionLogRequestDown)
                                    .retrieve()
                                    .bodyToMono(Boolean.class)
                                    .block();
                            resultDowntTimeMessage = DownTimeMessageModel.builder().message_details(new DownTimeMessageDetails(machineStatus.getEvent() + " Status logged Successfully", "S")).build();

                        }


                    }
                } else {

                    int totalDownTime= (int)getTotalDownTimeByShift(resource,site,getShiftDetails.getStartTime(),String.valueOf(LocalDateTime.now()));
                    if(totalDownTime>getShiftDetails.getPlannedTime()){
                        totalDownTime=getShiftDetails.getPlannedTime();
                    }
                    int operatingTime = getShiftDetails.getPlannedTime()-totalDownTime;
                    double availability = ((double) operatingTime / getShiftDetails.getPlannedTime()) * 100;
                    DownTimeAvailability downTimeAvailability = DownTimeAvailability.builder().uniqueId(generateUniqueId())
                            .event("SCHEDULER")
                            .resourceId(resource)
                            .site(site)
                            .createdTime(String.valueOf(LocalTime.now()))
                            .createdDateTime(String.valueOf(LocalDateTime.now()))
                            .shift(getShiftDetails.getShiftName())
                            .entryTime(String.valueOf(LocalDateTime.now()))
                            .shiftStartDate(getShiftDetails.getStartTime())
                            .shiftEndDate(getShiftDetails.getEndTime())
                            .plannedProductionTime(getShiftDetails.getPlannedTime())
                            .totalDowntime(totalDownTime)
                            .operatingTime(operatingTime)
                            .availability((int) availability)
                            .mcBreakDownHours(totalDownTime)
                            .active(1)
                            .processed(false)
                            .build();
                    downTimeAvailability.setCombinations(combinations);
                    resultDowntTimeMessage = DownTimeMessageModel.builder().response(downTimeRepository.save(downTimeAvailability)).message_details(new DownTimeMessageDetails(downTimeAvailability.getEvent() + " Status logged Successfully", "S")).build();
                    applicationEventPublisher.publishEvent(resultDowntTimeMessage);

                }
            }
        }else{
            List<String> resourcesList=getAllResources(site);
            for (String resource : resourcesList) {
                BreakMinutes getShiftDetails = getBreakHours(site, resource);
                resource = "ResourceBO:"+site+","+resource;
                List<MachineStatus> machineStatuses = getMachineStatusList(site, resource);
                if (machineStatuses != null && !machineStatuses.isEmpty()) {
                    MachineStatus machineStatus = machineStatuses.get(0);
                    if (machineStatus.getShiftStartTime() != null && !machineStatus.getShiftStartTime().isEmpty() &&
                            getShiftDetails.getStartTime() != null && !getShiftDetails.getStartTime().isEmpty() &&
                            machineStatus.getShiftStartTime().equals(getShiftDetails.getStartTime()) &&
                            machineStatus.getCreatedDate() != null && !machineStatus.getCreatedDate().isEmpty() &&
                            LocalDate.parse(machineStatus.getCreatedDate()).equals(LocalDate.now())
                    ) {
                        long meanTime = Duration.between(LocalDateTime.parse(machineStatus.getDowntimeStart()), LocalDateTime.now()).toSeconds();
                        int totalDownTime = (int) (getTotalDownTimeByShift(resource, site, getShiftDetails.getStartTime(), String.valueOf(LocalDateTime.now())) + meanTime) ;
                        if(totalDownTime>getShiftDetails.getPlannedTime()){
                            totalDownTime=getShiftDetails.getPlannedTime();
                        }
                        int operatingTime = getShiftDetails.getPlannedTime() - totalDownTime;
                        double availability = ((double) operatingTime / getShiftDetails.getPlannedTime()) * 100;


                        DownTimeAvailability downTimeAvailability = DownTimeAvailability.builder().uniqueId(generateUniqueId())
                                .event("SCHEDULER")
                                .site(site)
                                .resourceId(resource)
                                .createdDateTime(String.valueOf(LocalDateTime.now()))
                                .createdTime(String.valueOf(LocalTime.now()))
                                .shift(getShiftDetails.getShiftName())
                                .shiftEndDate(getShiftDetails.getEndTime())
                                .entryTime(String.valueOf(LocalDateTime.now()))
                                .shiftStartDate(getShiftDetails.getStartTime())
                                .plannedProductionTime(getShiftDetails.getPlannedTime())
                                .totalDowntime(totalDownTime)
                                .mcBreakDownHours(totalDownTime)
                                .operatingTime(operatingTime)
                                .availability((int) availability)
                                .active(1)
                                .processed(false)
                                .build();
                        downTimeAvailability.setCombinations(combinations);
                        resultDowntTimeMessage = DownTimeMessageModel.builder().response(downTimeRepository.save(downTimeAvailability)).message_details(new DownTimeMessageDetails(downTimeAvailability.getEvent() + " Status logged Successfully", "S")).build();
                        applicationEventPublisher.publishEvent(resultDowntTimeMessage);

                    }else{
                        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                                .eventType("MC_UP")
                                .site(machineStatus.getSite())
                                .resourceBO(machineStatus.getResource())
                                .eventData("Machine Up ")
                                .workCenterBO(machineStatus.getWorkcenter())
                                .data_field("SetUpState")
                                .data_value("Productive")
                                .reasonCode(machineStatus.getReasonCode())
                                .timestamp(LocalDateTime.now().toString())
                                .build();
                        Boolean productionLogged = webClientBuilder.build()
                                .post()
                                .uri(productionLogUrl)
                                .bodyValue(productionLogRequest)
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .block();

                        if (productionLogged) {
                            ProductionLogRequest productionLogRequestDown = ProductionLogRequest.builder()
                                    .eventType("MC_DOWN")
                                    .site(machineStatus.getSite())
                                    .resourceBO(machineStatus.getResource())
                                    .eventData("Machine Down")
                                    .workCenterBO(machineStatus.getWorkcenter())
                                    .data_field("SetUpState")
                                    .data_value(machineStatus.getEvent())
                                    .reasonCode(machineStatus.getReasonCode())
                                    .timestamp(LocalDateTime.now().toString())
                                    .build();
                            Boolean productionLoggedForDown = webClientBuilder.build()
                                    .post()
                                    .uri(productionLogUrl)
                                    .bodyValue(productionLogRequestDown)
                                    .retrieve()
                                    .bodyToMono(Boolean.class)
                                    .block();
                            resultDowntTimeMessage = DownTimeMessageModel.builder().message_details(new DownTimeMessageDetails(machineStatus.getEvent() + " Status logged Successfully", "S")).build();

                        }


                    }
                } else {

                    int totalDownTime= (int)getTotalDownTimeByShift(resource,site,getShiftDetails.getStartTime(),String.valueOf(LocalDateTime.now()));
                    if(totalDownTime>getShiftDetails.getPlannedTime()){
                        totalDownTime=getShiftDetails.getPlannedTime();
                    }
                    int operatingTime = getShiftDetails.getPlannedTime()-totalDownTime;
                    double availability = ((double) operatingTime / getShiftDetails.getPlannedTime()) * 100;
                    DownTimeAvailability downTimeAvailability = DownTimeAvailability.builder().uniqueId(generateUniqueId())
                            .event("SCHEDULER")
                            .resourceId(resource)
                            .site(site)
                            .createdTime(String.valueOf(LocalTime.now()))
                            .createdDateTime(String.valueOf(LocalDateTime.now()))
                            .shift(getShiftDetails.getShiftName())
                            .entryTime(String.valueOf(LocalDateTime.now()))
                            .shiftStartDate(getShiftDetails.getStartTime())
                            .shiftEndDate(getShiftDetails.getEndTime())
                            .plannedProductionTime(getShiftDetails.getPlannedTime())
                            .totalDowntime(totalDownTime)
                            .operatingTime(operatingTime)
                            .availability((int) availability)
                            .mcBreakDownHours(totalDownTime)
                            .active(1)
                            .processed(false)
                            .build();
                    downTimeAvailability.setCombinations(combinations);
                    resultDowntTimeMessage = DownTimeMessageModel.builder().response(downTimeRepository.save(downTimeAvailability)).message_details(new DownTimeMessageDetails(downTimeAvailability.getEvent() + " Status logged Successfully", "S")).build();
                    applicationEventPublisher.publishEvent(resultDowntTimeMessage);

                }
            }
        }
        return resultDowntTimeMessage;
    }

    private List<String> getAllResourceFromCombination(List<Combinations> combinations) {
        List<String> resourceList = new ArrayList<>();
        for (Combinations combination : combinations) {
            String combo = combination.getCombo();
            if (combo != null && !combo.isEmpty()) {
                String parts[] = combo.split("-");
                for (String part : parts) {
                    if (part.contains("Resource:")) {
                        String resource = part.substring(part.indexOf("Resource:") + "Resource:".length());
                        if(resource.split(",").length>1){
                            String resourcepart[]=resource.split(",");
                            resource= resourcepart[1];

                        }
                        resourceList.add(resource);
                    }
                }
            }
        }
        if(resourceList.contains("*")){
            return null;
        }
        return resourceList;
    }
    @Override
    public List<Oee> getAvailabilityForLiveData(String site, String resource) throws Exception {
        List<DownTime> downTimeList = new ArrayList<>();
        if (resource != null && !resource.isEmpty()) {
            BreakMinutes getShiftDetails = getBreakHours(site, resource);

            List<MachineStatus> machineStatuses = getMachineStatusList(site, resource);
            if (machineStatuses != null && !machineStatuses.isEmpty()) {
                MachineStatus machineStatus = machineStatuses.get(0);
                long meanTime = Duration.between(LocalDateTime.parse(machineStatus.getDowntimeStart()), LocalDateTime.now()).toSeconds();
                int totalDownTime = (int) (getTotalDownTimeByShift(resource, site, getShiftDetails.getStartTime(), String.valueOf(LocalDateTime.now())) + meanTime);//-getShiftDetails.getBreakTime();
                if(totalDownTime>getShiftDetails.getPlannedTime()){
                    totalDownTime=getShiftDetails.getPlannedTime();
                }
                int operatingTime = getShiftDetails.getPlannedTime() - totalDownTime;
                double availability = ((double) operatingTime / getShiftDetails.getPlannedTime()) * 100;


                DownTimeAvailability downTimeAvailability = DownTimeAvailability.builder().uniqueId(generateUniqueId())
                        .event("SCHEDULER")
                        .site(site)
                        .resourceId(resource)
                        .createdDateTime(String.valueOf(LocalDateTime.now()))
                        .createdTime(String.valueOf(LocalTime.now()))
                        .shift(getShiftDetails.getShiftName())
                        .shiftEndDate(getShiftDetails.getEndTime())
                        .entryTime(String.valueOf(LocalDateTime.now()))
                        .shiftStartDate(getShiftDetails.getStartTime())
                        .plannedProductionTime(getShiftDetails.getPlannedTime())
                        .totalDowntime(totalDownTime)
                        .mcBreakDownHours(totalDownTime)
                        .operatingTime(operatingTime)
                        .availability((int) availability)
                        .active(1)
                        .processed(false)
                        .build();
                DownTime downTimeObject = performanceService.downTimeAvailabilityToDownTimeBuilder(downTimeAvailability);
                downTimeList.add(downTimeObject);
            } else {

                int totalDownTime= (int)getTotalDownTimeByShift(resource,site,getShiftDetails.getStartTime(),String.valueOf(LocalDateTime.now()));
                if(totalDownTime>getShiftDetails.getPlannedTime()){
                    totalDownTime=getShiftDetails.getPlannedTime();
                }
                int operatingTime = getShiftDetails.getPlannedTime()-totalDownTime;
                double availability = ((double) operatingTime / getShiftDetails.getPlannedTime()) * 100;
                DownTimeAvailability downTimeAvailability = DownTimeAvailability.builder().uniqueId(generateUniqueId())
                        .event("SCHEDULER")
                        .resourceId(resource)
                        .site(site)
                        .createdTime(String.valueOf(LocalTime.now()))
                        .createdDateTime(String.valueOf(LocalDateTime.now()))
                        .shift(getShiftDetails.getShiftName())
                        .entryTime(String.valueOf(LocalDateTime.now()))
                        .shiftStartDate(getShiftDetails.getStartTime())
                        .shiftEndDate(getShiftDetails.getEndTime())
                        .plannedProductionTime(getShiftDetails.getPlannedTime())
                        .totalDowntime(totalDownTime)
                        .operatingTime(operatingTime)
                        .availability((int) availability)
                        .mcBreakDownHours(totalDownTime)
                        .active(1)
                        .processed(false)
                        .build();
                DownTime downTimeObject = performanceService.downTimeAvailabilityToDownTimeBuilder(downTimeAvailability);
                downTimeList.add(downTimeObject);

            }
        }else{
            List<String> resources = getAllResources(site);
            for(String resourceId: resources){
                BreakMinutes getShiftDetails = getBreakHours(site, resourceId);
                resourceId="ResourceBO:"+site+","+resourceId;

                List<MachineStatus> machineStatuses = getMachineStatusList(site, resourceId);
                if (machineStatuses != null && !machineStatuses.isEmpty()) {
                    MachineStatus machineStatus = machineStatuses.get(0);
                    long meanTime = Duration.between(LocalDateTime.parse(machineStatus.getDowntimeStart()), LocalDateTime.now()).toSeconds();
                    int totalDownTime = (int) (getTotalDownTimeByShift(resourceId, site, getShiftDetails.getStartTime(), String.valueOf(LocalDateTime.now())) + meanTime);//-getShiftDetails.getBreakTime();
                    if(totalDownTime>getShiftDetails.getPlannedTime()){
                        totalDownTime=getShiftDetails.getPlannedTime();
                    }
                    int operatingTime = getShiftDetails.getPlannedTime() - totalDownTime;
                    double availability = ((double) operatingTime / getShiftDetails.getPlannedTime()) * 100;


                    DownTimeAvailability downTimeAvailability = DownTimeAvailability.builder().uniqueId(generateUniqueId())
                            .event("SCHEDULER")
                            .site(site)
                            .resourceId(resourceId)
                            .createdDateTime(String.valueOf(LocalDateTime.now()))
                            .createdTime(String.valueOf(LocalTime.now()))
                            .shift(getShiftDetails.getShiftName())
                            .shiftEndDate(getShiftDetails.getEndTime())
                            .entryTime(String.valueOf(LocalDateTime.now()))
                            .shiftStartDate(getShiftDetails.getStartTime())
                            .plannedProductionTime(getShiftDetails.getPlannedTime())
                            .totalDowntime(totalDownTime)
                            .mcBreakDownHours(totalDownTime)
                            .operatingTime(operatingTime)
                            .availability((int) availability)
                            .active(1)
                            .processed(false)
                            .build();
                    DownTime downTimeObject = performanceService.downTimeAvailabilityToDownTimeBuilder(downTimeAvailability);
                    downTimeList.add(downTimeObject);
                } else {

                    int totalDownTime= (int)getTotalDownTimeByShift(resourceId,site,getShiftDetails.getStartTime(),String.valueOf(LocalDateTime.now()));
                    if(totalDownTime>=getShiftDetails.getPlannedTime()){
                        totalDownTime=getShiftDetails.getPlannedTime();
                    }
                    int operatingTime = getShiftDetails.getPlannedTime()-totalDownTime ;
                    double availability = ((double) operatingTime / getShiftDetails.getPlannedTime()) * 100;
                    DownTimeAvailability downTimeAvailability = DownTimeAvailability.builder().uniqueId(generateUniqueId())
                            .event("SCHEDULER")
                            .resourceId(resourceId)
                            .site(site)
                            .createdTime(String.valueOf(LocalTime.now()))
                            .createdDateTime(String.valueOf(LocalDateTime.now()))
                            .shift(getShiftDetails.getShiftName())
                            .entryTime(String.valueOf(LocalDateTime.now()))
                            .shiftStartDate(getShiftDetails.getStartTime())
                            .shiftEndDate(getShiftDetails.getEndTime())
                            .plannedProductionTime(getShiftDetails.getPlannedTime())
                            .totalDowntime(totalDownTime)
                            .operatingTime(operatingTime)
                            .availability((int) availability)
                            .mcBreakDownHours(totalDownTime)
                            .active(1)
                            .processed(false)
                            .build();
                    DownTime downTimeObject = performanceService.downTimeAvailabilityToDownTimeBuilder(downTimeAvailability);
                    downTimeList.add(downTimeObject);

                }
            }
        }
        PerformanceRequestList performanceRequestList = PerformanceRequestList.builder().performanceRequestList(downTimeList).build();
        return performanceService.calculatePerformanceForLiveData(performanceRequestList);
    }

    private List<MachineStatus> getMachineStatusList(String site, String resource) {
        ShiftRequest shiftRequest = ShiftRequest.builder().site(site).resource(resource).build();
        List<MachineStatus> getMachineStatus = webClientBuilder.build()
                .post()
                .uri(getMachineStatusUrl)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<MachineStatus>>() {
                })
                .block();
        if (getMachineStatus != null && !getMachineStatus.isEmpty()) {
            return getMachineStatus;
        }
        return null;
    }

    private BreakMinutes getBreakHours(String site, String resource) {
        ShiftRequest shiftRequest = ShiftRequest.builder().site(site).shiftType("Resource").resource(resource).build();
        BreakMinutes breakMinutes = webClientBuilder.build()
                .post()
                .uri(getBreakHoursTillNowUrl)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(BreakMinutes.class)
                .block();
        if (breakMinutes == null || breakMinutes.getShiftName() == null || breakMinutes.getShiftName().isEmpty()) {
            throw new DownTimeException(3);
        }
        return breakMinutes;

    }

    private List<String> getAllResources(String site) {
        DownTimeRequest downTimeRequest = DownTimeRequest.builder().site(site).build();
        List<String> resources = new ArrayList<>();
        List<ResourceListResponse> resourceListResponses = webClientBuilder.build()
                .post()
                .uri(getAllResourceUrl)
                .bodyValue(downTimeRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ResourceListResponse>>() {
                })
                .block();
        if (resourceListResponses != null && !resourceListResponses.isEmpty()) {
            resources = resourceListResponses.stream().map(ResourceListResponse::getResource).collect(Collectors.toList());
        }
        return resources;
    }

    @Override
    public List<DowntimeResponse> getTotalDownTimeByReasonCodeInEachShift(String site, String resourceBO, String startDate, String endDate) throws Exception
    {
        Aggregation aggregation = null;
        List<DowntimeResponse> downtimeResponseList = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate tomorrow = currentDate.plusDays(1);

        if(startDate == null || startDate == "")
        {
            startDate = currentDate+"T00:00:00";
        }
        if(endDate == null || endDate == "")
        {
            endDate=tomorrow+"T00:00:00";
        }

        GroupOperation groupByReasonAndShift = Aggregation.group("reasonCode", "shift")
                .sum("totalDowntime").as("totalDowntime");

        GroupOperation groupByReason = Aggregation.group("_id.reasonCode")
                .push(new Document("shift", "$_id.shift")
                        .append("totalDowntime", "$totalDowntime")).as("downTimeByShiftList");

        ProjectionOperation projectToOutputFormat = Aggregation.project()
                .andExclude("_id")
                .and("_id").as("reasonCode")
                .and("downTimeByShiftList").as("downTimeByShiftList");

        if(StringUtils.isNotEmpty(site) && StringUtils.isNotEmpty(resourceBO))
        {
             aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("site").is(site).and("resourceId").is(resourceBO).and("createdDateTime").gte(startDate).lt(endDate)),
                    groupByReasonAndShift,
                    groupByReason,
                    projectToOutputFormat
            );

//            downtimeResponses = mongoTemplate.aggregate(aggregation, DowntimeResponse.class, DowntimeResponse.class)
//                    .getMappedResults();

        }else{

             aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("site").is(site).and("createdDateTime").gte(startDate).lt(endDate)),
                    groupByReasonAndShift,
                    groupByReason,
                    projectToOutputFormat
            );
        }
        AggregationResults
                <DowntimeResponse> downtimeResponses = mongoTemplate.aggregate(aggregation, DownTimeAvailability.class, DowntimeResponse.class);
            downtimeResponseList = downtimeResponses.getMappedResults();
        return downtimeResponseList;
    }


    private String getCurrentShiftName(String site,String resource) {
        String shiftName = "";
        ShiftRequest shiftRequest = ShiftRequest.builder().site(site).shiftType("Resource").resource(resource).build();
        BreakMinutes breakMinutelist = webClientBuilder.build()
                .post()
                .uri(getBreakHoursTillNowUrl)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(BreakMinutes.class)
                .block();

        if(breakMinutelist !=null)
        {
            shiftName = breakMinutelist.getShiftName();
        }

        return shiftName;
    }

    @Override
    public DownTimeByShift getTotalDownTimeForCurrentShift(String site, String resource)throws Exception
    {
        LocalDate currentDate = LocalDate.now();
        LocalDate tomorrow = currentDate.plusDays(1);

        String shift = getCurrentShiftName(site,resource);

//        MatchOperation matchOperation = Aggregation.match(
//                Criteria.where("site").is(site)
//                        .and("timestamp").gte(currentDate+"T00:00:00").lt(tomorrow+"T00:00:00")
//                        .and("shift").is(shift)
//        );
//        GroupOperation groupOperation = Aggregation.group("shift").sum("totalDowntime").as("totalDowntime");
//
//        ProjectionOperation projectToOutputFormat = Aggregation.project()
//                .andExclude("_id")
//                .and("_id").as("shift")
//                .and("totalDowntime").as("totalDowntime");
//        Aggregation  aggregation = Aggregation.newAggregation(
//                matchOperation,
//                groupOperation,
//                projectToOutputFormat
//        );

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("site").is(site)
                                .and("shift").is(shift)
                                .and("createdDateTime").gte(currentDate+"T00:00:00")
                                .lt(tomorrow+"T00:00:00")
                ),
                Aggregation.group().sum("totalDowntime").as("totalDownTime")
        );
        AggregationResults
                <TotalDownTime> downtimeResponses = mongoTemplate.aggregate(aggregation, DownTimeAvailability.class, TotalDownTime.class);
        TotalDownTime totalDownTime = downtimeResponses.getUniqueMappedResult();
//        int totalDownTime = downtimeResponses.getUniqueMappedResult().getTotalDowntime();
//        String shiftName = downtimeResponses.getUniqueMappedResult().getShift();
        if(totalDownTime == null || totalDownTime.getTotalDownTime() == 0)
        {
            DownTimeByShift downtime = DownTimeByShift.builder().shift(shift).totalDowntime(0).build();
            return downtime;
        }
        DownTimeByShift downtimeResponseList = DownTimeByShift.builder().shift(shift).totalDowntime(totalDownTime.getTotalDownTime()).build();
        return downtimeResponseList;

    }

    @Override
    public List<DownTimeAvailability> getAllRecordsBetweenDateTime(String site,String resource) throws Exception
    {
        String startDate = LocalDate.now()+"T06:00:00";
        String endDate = LocalDate.now()+"T18:00:00";
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("site").is(site)
                                .and("resourceId").is(resource)
                                .and("createdDateTime").gte(startDate)
                                .lt(endDate)
                )
        );
        AggregationResults
                <DownTimeAvailability> downtimeResponses = mongoTemplate.aggregate(aggregation, DownTimeAvailability.class, DownTimeAvailability.class);
        List<DownTimeAvailability> downTimeAvailabilityList = downtimeResponses.getMappedResults();
        return downTimeAvailabilityList;
    }

}
