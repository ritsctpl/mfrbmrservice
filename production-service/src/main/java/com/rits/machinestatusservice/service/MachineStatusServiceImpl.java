package com.rits.machinestatusservice.service;
import com.rits.machinestatusservice.dto.BreakMinutes;
import com.rits.machinestatusservice.dto.MachineStatusRequest;
import com.rits.machinestatusservice.dto.PlannedMinutes;
import com.rits.machinestatusservice.dto.ShiftRequest;
import com.rits.machinestatusservice.model.*;
import com.rits.machinestatusservice.repository.MachineLogRepository;
import com.rits.machinestatusservice.repository.MachineStatusRepository;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MachineStatusServiceImpl implements MachineStatusService {
    private final MachineStatusRepository machineStatusRepository;
    private final MachineLogRepository machineLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WebClient.Builder webClientBuilder;
    @Value("${downtime-service.url}/create")
    private String downtimeuri;
    @Value("${shift-service.url}/getBreakHoursTillNowByType")
    private String getBreakHoursTillNowUrl;
    @Value("${shift-service.url}/getPlannedTimeTillNowByType")
    private String getPlannedTimeUrl;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]");

    DateTimeFormatter formatterWithoutMilliSecond = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private int plannedproductionTime = 0;
    private int breakminutes = 0;
    private int totalDowntime = 0;
    private int operatingTime = 0;
    private String shiftendTime;
    private String shiftStartTime;
    private String shiftName;

    @Override
    public MachineStatusMessageModel logMachineStatus(MachineStatusRequest machineStatusRequest) throws Exception{
        MachineStatus machineStatus=new MachineStatus();
        MachineLog machineLog=new MachineLog();
        String[] localDate=  machineStatusRequest.getCreatedDateTime().split("T");

        if(machineStatusRequest.getEvent().equalsIgnoreCase("MC_DOWN"))
        {
            machineStatus.setCreatedDate(localDate[0]);
            machineStatus = createBuilderForMachineStatus(machineStatusRequest);
            machineLog=createBuilderForMachineLog(machineStatusRequest);
            // saving in both the databases
            machineStatusRepository.save(machineStatus);
            machineLogRepository.save(machineLog);

        }
        if(machineStatusRequest.getEvent().equalsIgnoreCase("MC_UP"))
        {
            List<MachineStatus> machineStatuses=getMachineStatuses(
                    machineStatusRequest.getSite(),machineStatusRequest.getResource(),
                    machineStatusRequest.getShiftStartTime(),localDate[0]);

            List<MachineLog> machineLogses=getMachineStatusesPostgre(
                    machineStatusRequest.getSiteId(),machineStatusRequest.getResource(),
                    machineStatusRequest.getShiftStartTime(),localDate[0]);

            if ((machineStatuses != null && !machineStatuses.isEmpty()) &&
                    (machineLogses != null && !machineLogses.isEmpty()))
            {
                    LocalTime start = parseCustomIsoDateTime(machineStatuses.get(0).getDowntimeStart());
                    LocalTime end = parseCustomTime(localDate[1]);
                    long duration = Duration.between(start, end).toSeconds();

                    machineStatus.setCreatedDate(localDate[0]);
                    machineStatus = createBuilderForMachineStatus(machineStatusRequest);
                    machineLog=createBuilderForMachineLog(machineStatusRequest);
                    // saving in both the databases
                    machineStatusRepository.save(machineStatus);
                    machineLogRepository.save(machineLog);

                    for (MachineStatus machinestatusObj : machineStatuses) {
                        machinestatusObj.setActive(0);
                        machinestatusObj.setProcessed(1);
                        machineStatusRepository.save(machinestatusObj);
                    }

                   for (MachineLog machineLogObj : machineLogses) {
                    machineLogObj.setActive(0);
                    machineLogObj.setModifiedDateTime(LocalDateTime.now());
                    machineLogRepository.save(machineLogObj);
                   }

                    String reasonCode = "";
                    if(StringUtils.isEmpty(machineStatusRequest.getReasonCode()))
                    {
                        reasonCode =  machineStatuses.get(0).getReasonCode();
                    }
                    else {
                        reasonCode = machineStatusRequest.getReasonCode();
                    }

                    DownTimeRequest downTimeRequest = new DownTimeRequest();
                    downTimeRequest.setReasonCode(reasonCode);
                    downTimeRequest.setDowntimeStart(machineStatuses.get(0).getDowntimeStart());
                    downTimeRequest.setMeantime(String.valueOf(duration));
                    downTimeRequest.setCreatedDate(localDate[0]);
                    downTimeRequest =builderForDownTimeRequest(machineStatusRequest);

                    DownTimeMessageModel generatedNextNumber = webClientBuilder.build()
                            .post()
                            .uri(downtimeuri)
                            .bodyValue(downTimeRequest)
                            .retrieve()
                            .bodyToMono(DownTimeMessageModel.class)
                            .block();
            }

            else{
                machineStatus.setCreatedDate(localDate[0]);
                machineStatus = createBuilderForMachineStatus(machineStatusRequest);
                machineLog=createBuilderForMachineLog(machineStatusRequest);
                // saving in both the databases
                machineStatusRepository.save(machineStatus);
                machineLogRepository.save(machineLog);
            }


        }
        return MachineStatusMessageModel.builder().response(machineStatus).message_details(new MachineStatusMessageDetails(machineStatus.getResource() + " Status logged SuccessFully", "S")).build();
    }

    @Override
    public List<MachineStatus> getMachineStatuses(String site, String resource, String shiftStartTime, String createdDate) {
        List<MachineStatus> machineStatuses= new ArrayList<MachineStatus>();
        machineStatuses=machineStatusRepository.findBySiteAndEventAndResourceAndActiveAndShiftStartTimeAndCreatedDate(
                site,"MC_DOWN",resource,1,shiftStartTime,createdDate);
        return machineStatuses;
    }

    @Override
    public List<MachineLog> getMachineStatusesPostgre(String siteId, String resourceId, String shiftStartTime, String createdDate) {
        List<MachineLog> machineLogs=new ArrayList<>();
        machineLogs=machineLogRepository.findBySiteIdAndLogEventAndResourceIdAndActiveAndShiftStartTimeAndCreatedDate(
                siteId,"MC_DOWN",resourceId,1,shiftStartTime,createdDate);
        return machineLogs;
    }
    private MachineLog createBuilderForMachineLog(MachineStatusRequest machineStatusRequest){
     return MachineLog.builder()
             .active(1)
             .createdDateTime(LocalDateTime.now())
             .itemId(machineStatusRequest.getItemId())
             .logEvent(machineStatusRequest.getEvent())
             .logMessage(machineStatusRequest.getLogMessage())
             .operationId(machineStatusRequest.getOperationId())
             .resourceId(machineStatusRequest.getResource())
             .shiftBreakCreatedDateTime(machineStatusRequest.getShiftBreakCreatedDateTime())
             .shiftCreatedDateTime(machineStatusRequest.getShiftCreatedDateTime())
             .shiftId(machineStatusRequest.getShiftId())
             .siteId(machineStatusRequest.getSiteId())
             .createdDate(machineStatusRequest.getCreatedDate())
             .shiftStartTime(machineStatusRequest.getShiftStartTime())
             .workcenterId(machineStatusRequest.getWorkcenter())
             .build();
    }
    private MachineStatus createBuilderForMachineStatus(MachineStatusRequest machineStatusRequest){
//        String[] localDate = new String[0];
        return MachineStatus.builder()
              .site(machineStatusRequest.getSite())
              .event(machineStatusRequest.getEvent())
              .resource(machineStatusRequest.getResource())
              .workcenter(machineStatusRequest.getWorkcenter())
              .breakTime(machineStatusRequest.getBreakHours())
              .downtimeStart(machineStatusRequest.getCreatedDateTime())
              .shift(machineStatusRequest.getShift())
              .shiftStartTime(machineStatusRequest.getShiftStartTime())
              .shiftEndTime(machineStatusRequest.getShiftEndTime())
              .shiftAvailableTime(machineStatusRequest.getShiftAvailableTime())
              .meantime("0")
                .downtimeEnd(machineStatusRequest.getDowntimeEnd())
              .uniqueId(generateUniqueId())
              .reasonCode(machineStatusRequest.getReasonCode())
              .processed(0)
              .createdDateTime(machineStatusRequest.getCreatedDateTime())
              .createdDate(machineStatusRequest.getCreatedDate())
              .active(1)
              .build();
    }
    private DownTimeRequest builderForDownTimeRequest(MachineStatusRequest machineStatusRequest){
        return DownTimeRequest.builder()
                .site(machineStatusRequest.getSite())
                .event(machineStatusRequest.getEvent())
                .resource(machineStatusRequest.getResource())
                .workcenter(machineStatusRequest.getWorkcenter())
                .breakHours(Integer.parseInt(machineStatusRequest.getBreakHours()))
                .downtimeEnd(machineStatusRequest.getCreatedDateTime())
                .shift(machineStatusRequest.getShift())
                .shiftStartTime(machineStatusRequest.getShiftStartTime())
                .shiftEndTime(machineStatusRequest.getShiftEndTime())
                .shiftAvailableTime(machineStatusRequest.getShiftAvailableTime())
                .uniqueId(generateUniqueId())
                .processed(false)
                .createdDateTime(machineStatusRequest.getCreatedDateTime())
                .active(1)
                .build();
    }

    public static LocalTime parseCustomTime(String timeString) {
        String[] parts = timeString.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds= 0;
        if (parts.length > 2 && parts[2] != null && !parts[2].isEmpty()) {
             seconds= Integer.parseInt(parts[2]);
        }

        return LocalTime.of(hours, minutes, seconds);
    }
    public static LocalTime parseCustomIsoDateTime(String isoDateTime) {
        String[] parts = isoDateTime.split("T");
        String timePart = parts[1];
        return parseCustomTime(timePart);
    }
    private int generateUniqueId() {
        UUID uuid = UUID.randomUUID();
        // Convert UUID to a positive long value
        return (int) (uuid.getMostSignificantBits() & Integer.MAX_VALUE);
    }

    @Override
    public List<MachineStatus> getMachineStatus(String site, String resource){
        List<MachineStatus> machineStatuses= new ArrayList<MachineStatus>();
        machineStatuses=machineStatusRepository.findBySiteAndEventAndResourceAndActive(site,"MC_DOWN",resource,1);
        return machineStatuses;
    }
    @Override
    public List<MachineStatus> getActiveMachineStatus(MachineStatusRequest machineStatusRequest){
        List<MachineStatus> machineStatuses= new ArrayList<MachineStatus>();
        machineStatuses=machineStatusRepository.findBySiteAndActive(machineStatusRequest.getSite(),1);
        return machineStatuses;
    }
    @Override
    public MachineStatus getActiveMachineStatusByEvent(MachineStatusRequest machineStatusRequest){
       MachineStatus machineStatuses=machineStatusRepository.findTop1BySiteAndActiveAndResourceAndEventOrderByCreatedDateTime(machineStatusRequest.getSite(),1,machineStatusRequest.getResource(),"MC_DOWN");
        return machineStatuses;
    }


    @Override
    public MachineStatusMessageModel logMachineStatus(Object productionLog) throws Exception {

        MachineStatusRequest machineStatusRequest = new MachineStatusRequest();
        ProductionLogRequest productionLogRequest = (ProductionLogRequest) productionLog;
        machineStatusRequest.setEvent(productionLogRequest.getEventType());
        machineStatusRequest.setResource(productionLogRequest.getResourceId());  // to be changed Resource
        machineStatusRequest.setWorkcenter(productionLogRequest.getWorkcenterId()); // for now hard coded. Priya to include the resource to workcenter
        machineStatusRequest.setShift(productionLogRequest.getShiftId());
        machineStatusRequest.setShiftStartTime(productionLogRequest.getShiftStartTime().toString());
        machineStatusRequest.setShiftEndTime(productionLogRequest.getShiftEndTime().toString());
        machineStatusRequest.setShiftAvailableTime(productionLogRequest.getShiftAvailableTime().toString());
        machineStatusRequest.setBreakHours(productionLogRequest.getTotalBreakHours().toString());
        machineStatusRequest.setCreatedDateTime(productionLogRequest.getCreatedDatetime().toString());
        machineStatusRequest.setSite(productionLogRequest.getSite());
        machineStatusRequest.setReasonCode(productionLogRequest.getReasonCode());

        MachineStatusMessageModel outputDownMachineStatusModel = logMachineStatus(machineStatusRequest);


        return outputDownMachineStatusModel;
    }

    private int getTotalBreakTillNow(MachineStatusRequest machineStatusRequest) {
        breakminutes = 0;
        ShiftRequest shiftRequest = ShiftRequest.builder().site(machineStatusRequest.getSite()).shiftType("resource").resource(machineStatusRequest.getResource()).build();
        BreakMinutes breakMinutelist = webClientBuilder.build()
                .post()
                .uri(getBreakHoursTillNowUrl)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(BreakMinutes.class)
                .block();
        if(breakMinutelist != null && breakMinutelist.getShiftName() != null) {
            breakminutes = breakMinutelist.getBreakTime();
            shiftendTime = breakMinutelist.getEndTime();
            shiftStartTime = breakMinutelist.getStartTime();
            shiftName = breakMinutelist.getShiftName();
        }

        return breakminutes;
    }

    private int getPlannedProductionTime(MachineStatusRequest machineStatusRequest) {
        plannedproductionTime = 0;
        ShiftRequest shiftRequest = ShiftRequest.builder().site(machineStatusRequest.getSite()).shiftType("Resource").resource(machineStatusRequest.getResource()).build();
        PlannedMinutes plannedProduction = webClientBuilder.build()
                .post()
                .uri(getPlannedTimeUrl)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(PlannedMinutes.class)
                .block();
        if(plannedProduction != null && plannedProduction.getShiftName() != null){
        plannedproductionTime=plannedProduction.getPlannedTime();
        }
        // MinutesList shiftResponseList=plannedProduction.getResponse();
        return plannedproductionTime;
    }
}
