package com.rits.overallequipmentefficiency.service;


import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.DowntimeModel;
import com.rits.overallequipmentefficiency.model.MachineLogModel;
import com.rits.overallequipmentefficiency.repository.OeeDowntimeRepository;
import com.rits.overallequipmentefficiency.repository.OeeMachineLogRepository;
import com.rits.quality.dto.ProductionLogRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("oeeMachineLogService")
@RequiredArgsConstructor
public class MachineLogServiceImpl implements MachineLogService {

    @Autowired
    private OeeMachineLogRepository machineLogRepository;

    @Autowired
    private OeeDowntimeRepository downtimeRepository;


    @Autowired
    private DowntimeService downtimeService;


    private final WebClient.Builder webClientBuilder;

    @Value("${shift-service.url}/getShiftDetailBetweenTime")
    private String getShiftDetailBetweenTime;

    @Value("${productionlog-service.url}/save")
    private String insertProductionlog;

    // Requirement 1: Define final event type variables
    private static final List<String> DOWN_EVENTS = List.of("UNSCHEDULED_DOWN", "SCHEDULED_DOWN");
    private static final List<String> UP_EVENTS = List.of("RELEASABLE", "PRODUCTION");

    @Override
    public void logMachineLog(MachineLogRequest request) {

        if(request.getCreatedDateTime() == null){
            request.setCreatedDateTime(LocalDateTime.now());
        }
        ShiftInput shiftRequest = new ShiftInput(
                request.getSiteId(), request.getResourceId(), request.getWorkcenterId(),
                request.getCreatedDateTime(), request.getCreatedDateTime().plusSeconds(2)
        );

        List<ShiftOutput> shiftDetails = webClientBuilder.build()
                .post()
                .uri(getShiftDetailBetweenTime)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToFlux(ShiftOutput.class)
                .collectList()
                .block();

        ShiftOutput shiftDetail = shiftDetails != null && !shiftDetails.isEmpty() ? shiftDetails.get(0) : null;
        if (shiftDetail != null) {
            request.setShiftId(shiftDetail.getShiftId());
            request.setShiftCreatedDateTime(shiftDetail.getShiftCreatedDatetime());
        }

        // Requirement 2: Map logEvent to eventType
        String eventType = determineEventType(request.getLogEvent());

        if (eventType == null) {
            throw new IllegalArgumentException("Invalid event type");
        }

        if (DOWN_EVENTS.contains(request.getLogEvent())) {
            // Handle down events
            handleDownEvent(request);
        } else if (UP_EVENTS.contains(request.getLogEvent())) {
            // Handle up events
            handleUpEvent(request);
        } else {
            // Requirement 9: Log only in machine log for unknown events
            insertMachineLog(request);
            insertProductionLog(request);
        }
    }

    @Transactional // Ensures transactional context for update/delete queries
    public void deactivateOldMachineLogs(String resourceId) {
        machineLogRepository.deactivateOldRecords(resourceId);
    }


    private void handleDownEvent(MachineLogRequest request) {
        // Requirement 3: Check for active MACHINE_DOWN records
        Optional<DowntimeModel> activeRecord = downtimeRepository.findDowntimeByEvent(
                request.getResourceId(), DOWN_EVENTS, 1);

        if (activeRecord.isEmpty()) {
            // Requirement 4: First-time record, insert in downtime and machine log
            DowntimeModel downtime = buildDowntimeFromRequest(request);
            downtimeRepository.save(downtime);
            insertMachineLog(request);
            insertProductionLog(request);
        } else {
            // Requirement 6: Repeated message, log in machine log only
            deactivateOldMachineLogs(request.getResourceId());
            insertMachineLog(request);
            insertProductionLog(request);
        }
    }

    private void handleUpEvent(MachineLogRequest request) {
        // Requirement 7: Query for active MACHINE_DOWN record
        Optional<DowntimeModel> activeRecord = downtimeRepository.findDowntimeByEvent(
                request.getResourceId(), DOWN_EVENTS, 1);

        if (activeRecord.isPresent()) {
            // Requirement 8: Call logDownTime with the active record

            DowntimeModel downtime = activeRecord.get();
            DowntimeRequest downtimeRequest = DowntimeRequest.builder()
                    .resourceId(downtime.getResourceId())
                    .workcenterId(downtime.getWorkcenterId())
                    .site(downtime.getSite())
                    .downtimeEnd(request.getCreatedDateTime())
                    .reason(request.getReason())
                    .rootCause(request.getRootCause())
                    .commentUsr(request.getCommentUsr())
                    .build();
            downtimeService.logDownTime(downtimeRequest);

          //  Downtime downtime = activeRecord.get();
           /* downtime.setDowntimeEnd(request.getCreatedDateTime());
            downtime.setUpdatedDatetime(LocalDateTime.now());
            downtime.setActive(0); // Requirement 10: Deactivate old records
            downtimeRepository.save(downtime);*/

            deactivateOldMachineLogs(request.getResourceId());
            insertMachineDeactiveLog(request);
            insertProductionLog(request);
        } else {
            // Requirement 9: Log only in machine log if no active MACHINE_DOWN record
            deactivateOldMachineLogs(request.getResourceId());
            //insertMachineLog(request);
            insertMachineDeactiveLog(request);
            insertProductionLog(request);
        }
    }

    /*private MachineLogRequest updateMachineLogReq(MachineLogRequest request){
        ShiftInput shiftRequest = new ShiftInput(
                request.getShiftId(), request.getResourceId(), request.getWorkcenterId(),
                request.getCreatedDateTime(), request.getCreatedDateTime()
        );

        List<ShiftOutput> shiftDetails = webClientBuilder.build()
                .post()
                .uri(getShiftDetailBetweenTime)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToFlux(ShiftOutput.class)
                .collectList()
                .block();

        ShiftOutput shiftDetail = shiftDetails != null && !shiftDetails.isEmpty() ? shiftDetails.get(0) : null;
        if (shiftDetail != null) {
            request.setShiftId(shiftDetail.getShiftId());
            request.setShiftCreatedDateTime(shiftDetail.getShiftCreatedDatetime());
        }

       return request;
    }*/
    private DowntimeModel buildDowntimeFromRequest(MachineLogRequest request) {
        int downtEvent = 1;
        String logEvent = request.getLogEvent();
        if ("UNSCHEDULED_DOWN".equalsIgnoreCase(logEvent)) {
            downtEvent = 1;
        } else if ("SCHEDULED_DOWN".equalsIgnoreCase(logEvent)) {
            downtEvent = 2;
        }

        // Requirement 5: Fetch shift details if shiftId and shiftCreatedDateTime are missing

       /* if (request.getShiftId() == null || request.getShiftCreatedDateTime() == null) {
            ShiftInput shiftRequest = new ShiftInput(
                    request.getShiftId(), request.getResourceId(), request.getWorkcenterId(),
                    request.getCreatedDateTime(), request.getCreatedDateTime()
            );

            List<ShiftOutput> shiftDetails = webClientBuilder.build()
                    .post()
                    .uri(getShiftDetailBetweenTime)
                    .bodyValue(shiftRequest)
                    .retrieve()
                    .bodyToFlux(ShiftOutput.class)
                    .collectList()
                    .block();
        List<ShiftOutput> shiftDetails =getShiftDetails(request);

            ShiftOutput shiftDetail = shiftDetails != null && !shiftDetails.isEmpty() ? shiftDetails.get(0) : null;
            if (shiftDetail != null) {
                request.setShiftId(shiftDetail.getShiftId());
                request.setShiftCreatedDateTime(shiftDetail.getShiftCreatedDatetime());
            }
        }*/

        return DowntimeModel.builder()
                .resourceId(request.getResourceId())
                .workcenterId(request.getWorkcenterId())
                .site(request.getSiteId())
                .shiftId(request.getShiftId())
                .shiftRef(request.getShiftId())
                .shiftCreatedDateTime(request.getShiftCreatedDateTime())
                .downtimeStart(request.getCreatedDateTime())
                .downtimeType(request.getLogEvent())
                .downtEvent(downtEvent) // set 1 or 2 accordingly
                .reason((request.getReason() != null) ? request.getReason() : "Unknown")
                .rootCause((request.getRootCause() != null) ? request.getRootCause() : "Unknown")
                .createdDatetime(LocalDateTime.now())
                .updatedDatetime(request.getCreatedDateTime())
                .commentUsr(request.getCommentUsr())
                .active(1)
                .build();
    }

    private void insertMachineLog(MachineLogRequest request) {
        // Requirement 9: Insert machine log for every event
        MachineLogModel machineLog = MachineLogModel.builder()
                .siteId(request.getSiteId())
                .shiftId(request.getShiftId())
                .shiftCreatedDateTime(request.getShiftCreatedDateTime())
                .shiftBreakCreatedDateTime(request.getShiftBreakCreatedDateTime())
                .workcenterId(request.getWorkcenterId())
                .resourceId(request.getResourceId())
                .itemId(request.getItemId())
                .operationId(request.getOperationId())
                .logMessage(request.getLogMessage())
                .logEvent(request.getLogEvent())
                .createdDateTime(LocalDateTime.now())
                .modifiedDateTime(LocalDateTime.now())
                .active(1)
                .build();

        machineLogRepository.save(machineLog);
    }

    private void insertProductionLog(MachineLogRequest request) {

        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .site(request.getSiteId())
                .shiftId(request.getShiftId())
                .shiftCreatedDatetime(request.getShiftCreatedDateTime())
                .workcenterId(request.getWorkcenterId())
                .resourceId(request.getResourceId())
                .item(request.getItemId())
                .operation(request.getOperationId())
                .eventData(request.getLogMessage())
                .eventType(request.getLogEvent())
                .createdDatetime(LocalDateTime.now())
                .updatedDatetime(LocalDateTime.now())
                .build();

        Boolean insertProductionLog = webClientBuilder.build()
                .post()
                .uri(insertProductionlog)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    private void insertMachineDeactiveLog(MachineLogRequest request) {
        // Requirement 9: Insert machine log for every event
        MachineLogModel machineLog = MachineLogModel.builder()
                .siteId(request.getSiteId())
                .shiftId(request.getShiftId())
                .shiftCreatedDateTime(request.getShiftCreatedDateTime())
                .shiftBreakCreatedDateTime(request.getShiftBreakCreatedDateTime())
                .workcenterId(request.getWorkcenterId())
                .resourceId(request.getResourceId())
                .itemId(request.getItemId())
                .operationId(request.getOperationId())
                .logMessage(request.getLogMessage())
                .logEvent(request.getLogEvent())
                .createdDateTime(LocalDateTime.now())
                .modifiedDateTime(LocalDateTime.now())
                .active(0)
                .build();

        machineLogRepository.save(machineLog);
    }

    private String determineEventType(String logEvent) {
        // Requirement 2: Map logEvent to MACHINE_DOWN or MACHINE_UP
        if (DOWN_EVENTS.contains(logEvent)) {
            return "MACHINE_DOWN";
        }
        if (UP_EVENTS.contains(logEvent)) {
            return "MACHINE_UP";
        }
        return null;
    }
}
