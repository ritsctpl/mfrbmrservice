package com.rits.reasoncodeservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.kafkaservice.ProducerEvent;
import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.reasoncodeservice.dto.ReasonCodeRequest;
import com.rits.reasoncodeservice.dto.ResponseList;
import com.rits.reasoncodeservice.exception.ReasonCodeException;
import com.rits.reasoncodeservice.model.ReasonCode;
import com.rits.reasoncodeservice.model.ReasonCodeMessageModel;
import com.rits.reasoncodeservice.service.ReasonCodeService;
import com.rits.reasoncodeservice.service.ReasonCodeServiceImpl;
import com.rits.resourceservice.dto.Extension;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/reasoncode-service/")
public class ReasonCodeController {
    private final ReasonCodeService reasonCodeService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/create")
    public ResponseEntity<ReasonCodeMessageModel> create(@RequestBody ReasonCodeRequest reasonCodeRequest) throws JsonProcessingException
    {
        ReasonCodeMessageModel createReasonCode;
        try {
            createReasonCode = reasonCodeService.create(reasonCodeRequest);
            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(reasonCodeRequest.getSite())
                    .change_stamp("Create")
                    .action_code("REASONCODE-CREATE")
                    .action_detail("Reason code Created " + reasonCodeRequest.getReasonCode())
                    .action_detail_handle("ActionDetailBO:" + reasonCodeRequest.getSite() + "," + "REASONCODE-CREATE" + "," + reasonCodeRequest.getUserId() + ":" + "com.rits.reasoncodeservice.controller")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(reasonCodeRequest.getUserId())
                    .txnId("REASONCODE-CREATE" + LocalDateTime.now() + reasonCodeRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Create")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(activityLog));
            return ResponseEntity.ok(ReasonCodeMessageModel.builder().message_details(createReasonCode.getMessage_details()).response(createReasonCode.getResponse()).build());
        } catch (ReasonCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ReasonCodeMessageModel> update(@RequestBody ReasonCodeRequest reasonCodeRequest) throws JsonProcessingException
    {
        ReasonCodeMessageModel updateReasonCode;
        try {
            updateReasonCode = reasonCodeService.update(reasonCodeRequest);
            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(reasonCodeRequest.getSite())
                    .change_stamp("Update")
                    .action_code("REASONCODE-UPDATE")
                    .action_detail("Reason code Updated " + reasonCodeRequest.getReasonCode())
                    .action_detail_handle("ActionDetailBO:" + reasonCodeRequest.getSite() + "," + "REASONCODE-UPDATE" + "," + reasonCodeRequest.getUserId() + ":" + "com.rits.reasoncodeservice.controller")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(reasonCodeRequest.getUserId())
                    .txnId("REASONCODE-UPDATE" + LocalDateTime.now() + reasonCodeRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Create")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(activityLog));
            return ResponseEntity.ok(ReasonCodeMessageModel.builder().message_details(updateReasonCode.getMessage_details()).response(updateReasonCode.getResponse()).build());
        } catch (ReasonCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<ReasonCodeMessageModel> delete(@RequestBody ReasonCodeRequest reasonCodeRequest) throws JsonProcessingException
    {
        ReasonCodeMessageModel deleteReasonCode;
        try {
            deleteReasonCode = reasonCodeService.delete(reasonCodeRequest.getSite(),reasonCodeRequest.getReasonCode(),reasonCodeRequest.getUserId());
            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(reasonCodeRequest.getSite())
                    .change_stamp("Delete")
                    .action_code("REASONCODE-DELETE")
                    .action_detail("Reason code Delete " + reasonCodeRequest.getReasonCode())
                    .action_detail_handle("ActionDetailBO:" + reasonCodeRequest.getSite() + "," + "REASONCODE-DELETE" + "," + reasonCodeRequest.getUserId() + ":" + "com.rits.reasoncodeservice.controller")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(reasonCodeRequest.getUserId())
                    .txnId("REASONCODE-DELETE" + LocalDateTime.now() + reasonCodeRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Delete")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(activityLog));
            return ResponseEntity.ok(deleteReasonCode);
        } catch (ReasonCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieve")
    public ResponseEntity<ReasonCode> retrieve(@RequestBody ReasonCodeRequest reasonCodeRequest) throws JsonProcessingException
    {
        ReasonCode retrieveReasonCode;
        try {
            retrieveReasonCode = reasonCodeService.retrieve(reasonCodeRequest.getSite(),reasonCodeRequest.getReasonCode());
            return ResponseEntity.ok(retrieveReasonCode);
        } catch (ReasonCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveTop50")
    public ResponseEntity<ResponseList> retrieveTop50(@RequestBody ReasonCodeRequest reasonCodeRequest)
    {
        ResponseList retrieveTop50 = null;
            try {
                retrieveTop50 = reasonCodeService.retrieveTop50(reasonCodeRequest.getSite());
                return ResponseEntity.ok(retrieveTop50);
            } catch (ReasonCodeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    @PostMapping("/retrieveAll")
    public ResponseEntity<ResponseList> retrieveAll(@RequestBody ReasonCodeRequest reasonCodeRequest)
    {
        ResponseList responseList = null;
        try {
            responseList = reasonCodeService.retrieveAll(reasonCodeRequest.getSite());
            return ResponseEntity.ok(responseList);
        } catch (ReasonCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
