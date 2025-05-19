package com.rits.cycletimeservice.controller;

import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.ProducerEvent;
import com.rits.cycletimeservice.dto.*;
import com.rits.cycletimeservice.exception.CycleTimeException;
import com.rits.cycletimeservice.model.AttachmentPriority;
import com.rits.cycletimeservice.model.CycleTime;
import com.rits.cycletimeservice.model.CycleTimeMessageModel;
import com.rits.cycletimeservice.model.CycleTimePostgres;
import com.rits.cycletimeservice.service.CycleTimeService;
import com.rits.userservice.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/cycletime-service")
public class CycleTimeController {

    private final CycleTimeService cycleTimeService;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("createOrUpdate")
    public ResponseEntity<CycleTimeMessageModel> createOrUpdate(@RequestBody CycleTimeRequest cycleTimeRequest) {
            try {
                cycleTimeRequest.setItemId(cycleTimeRequest.getItemId() == null ? cycleTimeRequest.getItem() : cycleTimeRequest.getItemId());

                CycleTimeMessageModel createOrUpdateCycleTime = cycleTimeService.createOrUpdate(cycleTimeRequest);
                AuditLogRequest auditlog = null;
                if(createOrUpdateCycleTime.getMessage_details().getMsg().contains("Created")) {
                     auditlog = AuditLogRequest.builder()
                            .site(cycleTimeRequest.getSite())
                            .action_code("CYCLE-TIME-CREATED")
                            .action_detail("User Created")
                            .action_detail_handle("ActionDetailBO:" + cycleTimeRequest.getSite() + "," + "CYCLE-TIME-CREATED" + cycleTimeRequest.getUserId() + ":" + "com.rits.cycletimeservice.controller")
                            .activity("From Service")
                            .date_time(String.valueOf(LocalDateTime.now()))
                            .userId(cycleTimeRequest.getUserId())
                            .txnId("CYCLE-TIME-CREATED" + LocalDateTime.now() + cycleTimeRequest.getUserId())
                            .created_date_time(String.valueOf(LocalDateTime.now()))
                            .category("Create")
                            .topic("audit-log")
                            .build();
                }else{
                     auditlog = AuditLogRequest.builder()
                            .site(cycleTimeRequest.getSite())
                            .action_code("CYCLE-TIME-UPDATE")
                            .action_detail("Cycle Time Updated")
                            .action_detail_handle("ActionDetailBO:" + cycleTimeRequest.getSite() + "," + "CYCLE-TIME-UPDATED" + cycleTimeRequest.getUserId() + ":" + "com.rits.cycletimeservice.controller")
                            .activity("From Service")
                            .date_time(String.valueOf(LocalDateTime.now()))
                            .userId(cycleTimeRequest.getUserId())
                            .txnId("CYCLE-TIME-UPDATED" + LocalDateTime.now() + cycleTimeRequest.getUserId())
                            .created_date_time(String.valueOf(LocalDateTime.now()))
                            .category("Update")
                            .topic("audit-log")
                            .build();
                }
                eventPublisher.publishEvent(new ProducerEvent(auditlog));
                return ResponseEntity.ok(createOrUpdateCycleTime);
            } catch (CycleTimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    @PostMapping("delete")
    public ResponseEntity<CycleTimeMessageModel> deleteCycleTime(@RequestBody CycleTimeRequest cycleTimeRequest) {
            try {
                CycleTimeMessageModel deletedRecord = cycleTimeService.delete(cycleTimeRequest);
                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(cycleTimeRequest.getSite())
                        .action_code("CYCLE-TIME-DELETED")
                        .action_detail("Cycle Time Deleted")
                        .action_detail_handle("ActionDetailBO:" + cycleTimeRequest.getSite() + "," + "CYCLE-TIME-DELETED" + cycleTimeRequest.getUserId() + ":" + "com.rits.cycletimeservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(cycleTimeRequest.getUserId())
                        .txnId("CYCLE-TIME-DELETED" + String.valueOf(LocalDateTime.now()) + cycleTimeRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Delete")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));
                return ResponseEntity.ok(deletedRecord);
            } catch (CycleTimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    @PostMapping("retrieve")
    public ResponseEntity<CycleTimeResponse> retrieve(@RequestBody CycleTimeRequest cycleTimeRequest)
    {
        CycleTimeResponse retrievedRecord = null;
        try {
            retrievedRecord = cycleTimeService.retrieve(cycleTimeRequest);
            return ResponseEntity.ok(retrievedRecord);
        } catch (CycleTimeException e) {
            throw e;
        }
          catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveAll")
    public ResponseEntity<?> retrieveAll(@RequestBody CycleTimeRequest cycleTimeRequest)
    {
        CycleTimeResponseList retrievedRecord = null;
        try {
            retrievedRecord = cycleTimeService.retrieveAll(cycleTimeRequest.getSite());
            return ResponseEntity.ok(retrievedRecord);
        } catch (CycleTimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieveByCriteria")
    public ResponseEntity<?> retrieveByCriteria(@RequestBody CycleTimeRequest cycleTimeRequest)
    {
        CycleTimeResponseList retrievedRecord = null;
        try {
            retrievedRecord = cycleTimeService.retrieveByCriteria(cycleTimeRequest);
            return ResponseEntity.ok(retrievedRecord);
        } catch (CycleTimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("createPriorityCombination")
    public ResponseEntity<?> createPriorityCombination(@RequestBody AttachmentPriority attachmentPriority)
    {
        Boolean isCreated = false;
        try {
            isCreated = cycleTimeService.createPriorityCombinations(attachmentPriority);
            return ResponseEntity.ok(isCreated);
        } catch (CycleTimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

  /*  @PostMapping("getIdealCycleTime")
    public ResponseEntity<?> retrieveByAtttachment(@RequestBody CycleTimeRequest cycleTimeRequest)
    {
        CycleTime retrievedCycleTime;
        try {
            retrievedCycleTime = cycleTimeService.retrieveByAttachment(cycleTimeRequest);
            return ResponseEntity.ok(retrievedCycleTime);
        } catch (CycleTimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/



    @PostMapping("/getCalculatedPerformance")
    public List<CycleTimeDto> getCalculatedPerformance(@RequestBody PerformanceRequest request) {

        return cycleTimeService.getCalculatedPerformance(request);
    }

    @PostMapping("/getPlannedCycleTime")
    public Double getPlannedCycleTime(@RequestBody ProductionLogDto request){
        return cycleTimeService.getPlannedCycleTime(request);
    }

    @PostMapping("/getCycleTimeRecs")
    public List<CycleTimePostgres> gteCycleTimeRecs(@RequestBody CycleTimeRequest request){
        return cycleTimeService.getCycleTimeRecords(request);
    }

    @PostMapping("/getCycletimesByResource")
    public List<CycleTime> getCycleTimes(@RequestBody CycleTimeReq cycleTimeReq) {
        return cycleTimeService.getActiveCycleTimesBySiteAndResource(cycleTimeReq.getSite(), cycleTimeReq.getResourceId());
    }

    @PostMapping("/getCycletimesByWorkcenter")
    public List<CycleTime> getCycleTimesByWorkcenter(@RequestBody CycleTimeReq cycleTimeReq) {
        return cycleTimeService.getActiveCycleTimesBySiteAndWorkcenter(cycleTimeReq.getSite(), cycleTimeReq.getWorkCenterId());
    }

    @PostMapping("/getCycletimesByResourceAndItem")
    public List<CycleTime> filterCycleTimes(@RequestBody CycleTimeReq cycleTimeReq) {
        return cycleTimeService.getFilteredCycleTimes(cycleTimeReq);
    }
    @PostMapping("/getFilteredCycleTimesByWorkCenter")
    public List<CycleTime> getFilteredCycleTimesByWorkCenter(@RequestBody CycleTimeReq cycleTimeReq) {
        return cycleTimeService.getFilteredCycleTimesByWorkCenter(cycleTimeReq);
    }

    @PostMapping("/calculateCycleTime")
    public ResponseEntity<?> calculateCycleTime(@RequestBody ProductionLogDto dto) {
        Double value = cycleTimeService.getCycleTimeValue(dto);
        if (value == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No cycle time found for given parameters.");
        }
        return ResponseEntity.ok(value);
    }

}
