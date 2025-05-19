package com.rits.workinstructionservice.controller;

import com.rits.kafkaservice.ProducerEvent;
import  com.rits.operationservice.dto.AuditLogRequest;
import com.rits.workinstructionservice.dto.*;
import com.rits.workinstructionservice.exception.WorkInstructionException;
import com.rits.workinstructionservice.model.Attachment;
import com.rits.workinstructionservice.model.WIMessageModel;
import com.rits.workinstructionservice.model.WorkInstruction;
import com.rits.workinstructionservice.service.WorkInstructionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/workinstruction-service")
public class WorkInstructionController {
    private final WorkInstructionServiceImpl workInstructionServiceImpl;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/test")
    @ResponseStatus(HttpStatus.CREATED)
    public String test()
    {
    return "ok";
    }
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createWorkInstruction(@RequestBody WorkInstructionRequest workInstructionRequest)
    {
        if (workInstructionRequest.getSite() != null && !workInstructionRequest.getSite().isEmpty() && workInstructionRequest.getWorkInstruction()!=null && !workInstructionRequest.getWorkInstruction().isEmpty()) {
            try {
                WIMessageModel createdWorkInstruction = workInstructionServiceImpl.createWorkInstruction(workInstructionRequest);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(workInstructionRequest.getSite())
                        .action_code("WORK-INSTRUCTION-CREATED")
                        .action_detail("Work Instruction Created  "+workInstructionRequest.getWorkInstruction())
                        .action_detail_handle("ActionDetailBO:"+workInstructionRequest.getSite()+","+"WORK-INSTRUCTION-CREATED"+workInstructionRequest.getUserId()+":"+"com.rits.workinstructionservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(workInstructionRequest.getUserId())
                        .operation_revision("*")
                        .txnId("WORK-INSTRUCTION-CREATED"+String.valueOf(LocalDateTime.now())+workInstructionRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Create")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return ResponseEntity.ok(createdWorkInstruction);
            }catch(WorkInstructionException e){
                throw e;

            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,workInstructionRequest.getSite());
    }

    @PostMapping("/uploadFile")
    @ResponseStatus(HttpStatus.OK)
    public WorkInstruction uploadFile(@RequestBody WorkInstructionRequest workInstructionRequest)
    {
        if (workInstructionRequest.getSite() != null && !workInstructionRequest.getSite().isEmpty() && workInstructionRequest.getWorkInstruction()!=null && !workInstructionRequest.getWorkInstruction().isEmpty()) {
            try {

                  WorkInstruction workInstruction=    workInstructionServiceImpl.uploadFile(workInstructionRequest);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(workInstructionRequest.getSite())
                        .action_code("WORK-INSTRUCTION-UPDATED")
                        .action_detail("WorkC Instruction Updated "+workInstructionRequest.getWorkInstruction())
                        .action_detail_handle("ActionDetailBO:"+workInstructionRequest.getSite()+","+"WORK-INSTRUCTION-UPDATED"+workInstructionRequest.getUserId()+":"+"com.rits.workinstructionservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(workInstructionRequest.getUserId())
                        .txnId("WORK-INSTRUCTION-UPDATED"+String.valueOf(LocalDateTime.now())+workInstructionRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Update")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                      return workInstruction;

            }catch(WorkInstructionException e){
                throw e;

            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,workInstructionRequest.getSite());
    }

    @PostMapping("/retrieveFile")
    @ResponseStatus(HttpStatus.OK)
    public String getFileContent(@RequestBody WorkInstructionRequest workInstructionRequest) throws WorkInstructionException {
        if (workInstructionRequest.getSite() != null && !workInstructionRequest.getSite().isEmpty() && workInstructionRequest.getWorkInstruction()!=null && !workInstructionRequest.getWorkInstruction().isEmpty()) {
            try {
                return  workInstructionServiceImpl.getFileContent(workInstructionRequest);
            }catch(WorkInstructionException e){
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,workInstructionRequest.getSite());
    }

    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public  ResponseEntity<Boolean> isWorkInstructionExists(@RequestBody WorkInstructionRequest workInstructionRequest)
    {
        if (workInstructionRequest.getSite() != null && !workInstructionRequest.getSite().isEmpty()) {
            try {
                Boolean isWorkExists =  workInstructionServiceImpl.isWorkInstructionExists(workInstructionRequest);
                return ResponseEntity.ok(isWorkExists);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,workInstructionRequest.getSite());
    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WIMessageModel> updateWorkInstruction(@RequestBody WorkInstructionRequest workInstructionRequest)
    {
        if (workInstructionRequest.getSite() != null && !workInstructionRequest.getSite().isEmpty() && workInstructionRequest.getWorkInstruction()!=null && !workInstructionRequest.getWorkInstruction().isEmpty()) {
            try {
                WIMessageModel updatedWorkInstructionWorkInstruction =  workInstructionServiceImpl.updateWorkInstruction(workInstructionRequest);
                WorkInstruction workInstruction=    workInstructionServiceImpl.uploadFile(workInstructionRequest);

                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(workInstructionRequest.getSite())
                        .action_code("WORK-INSTRUCTION-UPDATED")
                        .action_detail("WorkC Instruction Updated  "+workInstructionRequest.getWorkInstruction())
                        .action_detail_handle("ActionDetailBO:"+workInstructionRequest.getSite()+","+"WORK-INSTRUCTION-UPDATED"+workInstructionRequest.getUserId()+":"+"com.rits.workinstructionservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(workInstructionRequest.getUserId())
                        .txnId("WORK-INSTRUCTION-UPDATED"+String.valueOf(LocalDateTime.now())+workInstructionRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Update")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));


                return ResponseEntity.ok(updatedWorkInstructionWorkInstruction);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2001, workInstructionRequest.getWorkInstruction());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,workInstructionRequest.getSite());
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WIMessageModel> deleteWorkInstruction(@RequestBody WorkInstructionRequest workInstructionRequest)
    {
        if (workInstructionRequest.getSite() != null && !workInstructionRequest.getSite().isEmpty()){
            try {
                WIMessageModel deleteResponse = workInstructionServiceImpl.deleteWorkInstruction(workInstructionRequest);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(workInstructionRequest.getSite())
                        .action_code("WORK-CENTER-DELETED " +workInstructionRequest.getWorkInstruction() )
                        .action_detail("WorkCenter Deleted")
                        .action_detail_handle("ActionDetailBO:"+workInstructionRequest.getSite()+","+"WORK-CENTER-DELETED"+workInstructionRequest.getUserId()+":"+"com.rits.workinstructionservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(workInstructionRequest.getUserId())
                        .txnId("WORK-CENTER-DELETED"+String.valueOf(LocalDateTime.now())+workInstructionRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Delete")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return ResponseEntity.ok(deleteResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2001," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,workInstructionRequest.getSite());
    }

    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionListResponse> retrieveTop50(@RequestBody WorkInstructionRequest workInstructionRequest)
    {
        if (workInstructionRequest.getSite() != null && !workInstructionRequest.getSite().isEmpty()){
            try {
                WorkInstructionListResponse retrieveTop50Response = workInstructionServiceImpl.retrieveTop50(workInstructionRequest);
                return  ResponseEntity.ok(retrieveTop50Response);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2004," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,workInstructionRequest.getSite());
    }

    @PostMapping("/retrieveAllByWorkInstruction")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionListResponse> retrieveAllByWorkInstruction(@RequestBody WorkInstructionRequest workInstructionRequest)
    {
        if (workInstructionRequest.getSite() != null && !workInstructionRequest.getSite().isEmpty()){
            try {
                WorkInstructionListResponse retrieveAllResponse =  workInstructionServiceImpl.retrieveAllByWorkInstruction(workInstructionRequest);
                return  ResponseEntity.ok(retrieveAllResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2004," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,workInstructionRequest.getSite());
    }

    @PostMapping("/retrieveByWorkInstruction")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstruction> retrieveByWorkInstruction(@RequestBody WorkInstructionRequest workInstructionRequest)
    {
        WorkInstruction retrieveByWorkInstructionResponse = null;
            try {
                 retrieveByWorkInstructionResponse = workInstructionServiceImpl.retrieveByWorkInstruction(workInstructionRequest);
                return  ResponseEntity.ok(retrieveByWorkInstructionResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2001," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    @PostMapping("/retrieveWorkInstructionByItem")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveWorkInstructionByItem(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList retrieveWorkInstructionByItemResponse =  workInstructionServiceImpl.retrieveWorkInstructionByItem(attachmentListRequest);
                return ResponseEntity.ok(retrieveWorkInstructionByItemResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2005," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByItemOperation")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveByItemOperation(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList retrieveByItemOperationResponse = workInstructionServiceImpl.retrieveByItemOperatrion(attachmentListRequest);
                return ResponseEntity.ok(retrieveByItemOperationResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2006," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByPcuOperation")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveByPcuOperation(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList retrieveByPcuOperationResponse = workInstructionServiceImpl.retrieveByPcuOperation(attachmentListRequest);
                return ResponseEntity.ok(retrieveByPcuOperationResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2007," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }
    @PostMapping("/retrieveWorkInstructionByResourceOperation")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveByResourceOperation(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList retrieveByResourceOperationResponse = workInstructionServiceImpl.retrieveByResourceOperation(attachmentListRequest);
                return ResponseEntity.ok(retrieveByResourceOperationResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2007," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByItemOperationRouting")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveByItemOperationRouting(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList retrieveByItemOperationRoutingResponse =  workInstructionServiceImpl.retrieveByItemOperationRouting(attachmentListRequest);
                return ResponseEntity.ok(retrieveByItemOperationRoutingResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2008," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByResource")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveByResource(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList retrieveByResourceResponse = workInstructionServiceImpl.retrieveByResource(attachmentListRequest);
                return ResponseEntity.ok(retrieveByResourceResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2009," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByResourcePcu")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveByResourcePcu(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList retrieveByResourcePcuResponse = workInstructionServiceImpl.retrieveByResourcePcu(attachmentListRequest);
                return ResponseEntity.ok(retrieveByResourcePcuResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2010," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByShopOrderOperation")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveByShopOrderOperation(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList retrieveByShopOrderOperationResponse =  workInstructionServiceImpl.retrieveByShopOrderOperation(attachmentListRequest);
                return ResponseEntity.ok(retrieveByShopOrderOperationResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2011," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByPcuAndMergeItem")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveByPcuAndMergeItem(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList retrieveByPcuAndMergeItemResponse = workInstructionServiceImpl.retrieveByPcuAndMergeItem(attachmentListRequest);
                return ResponseEntity.ok(retrieveByPcuAndMergeItemResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2012," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByOperationRoutingPcuAndMergeItem")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveByOperationRoutingPcuAndMergeItem(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList retrieveByOperationRoutingPcuAndMergeItemResponse =  workInstructionServiceImpl.retrieveByOperationRoutingPcuAndMergeItem(attachmentListRequest);
                return ResponseEntity.ok(retrieveByOperationRoutingPcuAndMergeItemResponse);
            }catch(WorkInstructionException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByShopOrderAndMergeItem")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveByShopOrderAndMergeItem(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList  retrieveByShopOrderAndMergeItemResponse =  workInstructionServiceImpl.retrieveByShopOrderAndMergeItem(attachmentListRequest);
                return ResponseEntity.ok(retrieveByShopOrderAndMergeItemResponse);
            }catch(WorkInstructionException e){
                throw new WorkInstructionException(2014," ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByWorkCenterAndMergeResource")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveByWorkCenterAndMergeResource(@RequestBody AttachmentListRequest attachmentListRequest)
    {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()){
            try {
                WorkInstructionResponseList retrieveByWorkCenterAndMergeResourceResponse = workInstructionServiceImpl.retrieveByWorkCenterAndMergeResource(attachmentListRequest);
                return ResponseEntity.ok(retrieveByWorkCenterAndMergeResourceResponse);
            }catch(WorkInstructionException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003,attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveByAttachment")
    public List<WorkInstruction> getWorkInstructionByAttachment(@RequestBody List<Attachment> attachmentList) {
        try {
            return workInstructionServiceImpl.getWorkInstructionByAttachment(attachmentList);
        }catch(WorkInstructionException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getWorkInstructionList")//
    public List<WorkListResponse> getWorkInstructionList(@RequestBody WorkListRequest workListRequest) {
        List<WorkListResponse> getWorkInstructionList;
            try {
                getWorkInstructionList = workInstructionServiceImpl.getWorkInstructionList(workListRequest);
                return getWorkInstructionList;
            } catch (WorkInstructionException workInstructionException) {
                throw workInstructionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

    }

    @PostMapping("getBatchRecipeWorkInstructionList")
    public List<BatchRecipeInstruction> getBatchRecipeWorkInstructionList(@RequestBody WorkListRequest workListRequest) {
        List<BatchRecipeInstruction> getWorkInstructionList;
            try {
                getWorkInstructionList = workInstructionServiceImpl.getBatchRecipeWorkInstructionList(workListRequest);
                return getWorkInstructionList;
            } catch (WorkInstructionException workInstructionException) {
                throw workInstructionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

    }

    @PostMapping("/retrieveWorkInstructionByBom")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveWorkInstructionByBom(@RequestBody AttachmentListRequest attachmentListRequest) {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()) {
            try {
                WorkInstructionResponseList retrieveWorkInstructionByBomResponse = workInstructionServiceImpl.retrieveWorkInstructionByBom(attachmentListRequest);
                return ResponseEntity.ok(retrieveWorkInstructionByBomResponse);
            } catch (WorkInstructionException e) {
                throw new WorkInstructionException(2016, " ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003, attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByBomVersion")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveWorkInstructionByBomVersion(@RequestBody AttachmentListRequest attachmentListRequest) {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()) {
            try {
                WorkInstructionResponseList retrieveWorkInstructionByBomVersionResponse = workInstructionServiceImpl.retrieveWorkInstructionByBomVersion(attachmentListRequest);
                return ResponseEntity.ok(retrieveWorkInstructionByBomVersionResponse);
            } catch (WorkInstructionException e) {
                throw new WorkInstructionException(2016, " ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003, attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByComponent")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveWorkInstructionByComponent(@RequestBody AttachmentListRequest attachmentListRequest) {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()) {
            try {
                WorkInstructionResponseList retrieveWorkInstructionByComponentResponse = workInstructionServiceImpl.retrieveWorkInstructionByComponent(attachmentListRequest);
                return ResponseEntity.ok(retrieveWorkInstructionByComponentResponse);
            } catch (WorkInstructionException e) {
                throw new WorkInstructionException(2017, " ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003, attachmentListRequest.getSite());
    }

    @PostMapping("/retrieveWorkInstructionByComponentVersion")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkInstructionResponseList> retrieveWorkInstructionByComponentVersion(@RequestBody AttachmentListRequest attachmentListRequest) {
        if (attachmentListRequest.getSite() != null && !attachmentListRequest.getSite().isEmpty()) {
            try {
                WorkInstructionResponseList retrieveWorkInstructionByComponentVersionResponse = workInstructionServiceImpl.retrieveWorkInstructionByComponentVersion(attachmentListRequest);
                return ResponseEntity.ok(retrieveWorkInstructionByComponentVersionResponse);
            } catch (WorkInstructionException e) {
                throw new WorkInstructionException(2017, " ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkInstructionException(2003, attachmentListRequest.getSite());
    }
    @PostMapping("/logRecord")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> logWorkInstructionRecord(@RequestBody WorkInstructionRequest workInstructionRequest) {
            try {
                Boolean logWorkInstruction = workInstructionServiceImpl.logProductionRecord(workInstructionRequest);
                return ResponseEntity.ok(logWorkInstruction);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

}