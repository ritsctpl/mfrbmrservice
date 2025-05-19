package com.rits.pcurouterheaderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.pcurouterheaderservice.dto.*;
import com.rits.pcurouterheaderservice.exception.PcuRouterHeaderException;
import com.rits.pcurouterheaderservice.model.MessageDetails;
import com.rits.pcurouterheaderservice.model.MessageModel;
import com.rits.pcurouterheaderservice.model.PcuRouterHeader;
import com.rits.pcurouterheaderservice.model.RoutingStep;
import com.rits.pcurouterheaderservice.service.PcuRouterHeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/pcurouterheader-service")
public class PcuRouterHeaderController {
    private final PcuRouterHeaderService pcuRouterHeaderService;
    private final ObjectMapper objectMapper;

    @PostMapping("create")
    public ResponseEntity<MessageModel> createPcuRouterHeader(@RequestBody PcuROuterHeaderCreateRequest pcuROuterHeaderCreateRequest) throws Exception {
        MessageModel createPcuRouterHeader;

        try {
            createPcuRouterHeader = pcuRouterHeaderService.createPcuRouterHeader(pcuROuterHeaderCreateRequest);
            return ResponseEntity.ok(MessageModel.builder().message_details(createPcuRouterHeader.getMessage_details()).response(createPcuRouterHeader.getResponse()).build());

        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("update")
    public ResponseEntity<MessageModel> updatePcuRouterHeader(@RequestBody PcuROuterHeaderCreateRequest pcuROuterHeaderCreateRequest) throws Exception {
        MessageModel updatePcuRouterHeader;

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(pcuROuterHeaderCreateRequest.getSite()).hookPoint("PRE").activity("pcurouterheader-service").hookableMethod("update").request(objectMapper.writeValueAsString(pcuROuterHeaderCreateRequest)).build();
        String preExtensionResponse = pcuRouterHeaderService.callExtension(preExtension);
        PcuROuterHeaderCreateRequest preExtensionPcuRouterHeader = objectMapper.readValue(preExtensionResponse, PcuROuterHeaderCreateRequest.class);

        try {
            updatePcuRouterHeader = pcuRouterHeaderService.updatePcuRouterHeader(preExtensionPcuRouterHeader);
            Extension postExtension = Extension.builder().site(pcuROuterHeaderCreateRequest.getSite()).hookPoint("POST").activity("pcurouterheader-service").hookableMethod("update").request(objectMapper.writeValueAsString(updatePcuRouterHeader.getResponse())).build();
            String postExtensionResponse = pcuRouterHeaderService.callExtension(postExtension);
            PcuRouterHeader postExtensionRouterHeader = objectMapper.readValue(postExtensionResponse, PcuRouterHeader.class);
            return ResponseEntity.ok(MessageModel.builder().message_details(updatePcuRouterHeader.getMessage_details()).response(postExtensionRouterHeader).build());

        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieve")
    public ResponseEntity<PcuRouterHeader> retrievePcuRouterHeader(@RequestBody PcuRouterHeaderRequest pcuRouterHeaderRequest) throws Exception {
        PcuRouterHeader retrievePcuRouterHeader;
        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(pcuRouterHeaderRequest.getSite()).hookPoint("PRE").activity("pcurouterheader-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(pcuRouterHeaderRequest)).build();
        String preExtensionResponse = pcuRouterHeaderService.callExtension(preExtension);
        PcuRouterHeaderRequest preExtensionPcuRouterHeader = objectMapper.readValue(preExtensionResponse, PcuRouterHeaderRequest.class);

        try {
            retrievePcuRouterHeader = pcuRouterHeaderService.retrievePcuRouterHeader(preExtensionPcuRouterHeader);
            Extension postExtension = Extension.builder().site(pcuRouterHeaderRequest.getSite()).hookPoint("POST").activity("pcurouterheader-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrievePcuRouterHeader)).build();
            String postExtensionResponse = pcuRouterHeaderService.callExtension(postExtension);
            PcuRouterHeader postExtensionRouterHeader = objectMapper.readValue(postExtensionResponse, PcuRouterHeader.class);
            return ResponseEntity.ok(postExtensionRouterHeader);

        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("isParentRoute")
    public ResponseEntity<Boolean> isParentRoute(@RequestBody PcuRequest pcuRequest) throws Exception {
        boolean isParentRoute;
        try {
            isParentRoute = pcuRouterHeaderService.isParentRoute(pcuRequest.getSite(), pcuRequest.getRouter(), pcuRequest.getVersion(), pcuRequest.getPcuBo());
            return ResponseEntity.ok(isParentRoute);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getCurrentStep")
    public ResponseEntity<List<RoutingStep>> getCurrentStep(@RequestBody PcuRequest pcuRequest) throws Exception {
        List<RoutingStep> getCurrentStep;
        try {
            getCurrentStep = pcuRouterHeaderService.getCurrentStep(pcuRequest.getSite(), pcuRequest.getPcuBo());
            return ResponseEntity.ok(getCurrentStep);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("isEntryStep")
    public ResponseEntity<Boolean> isEntryStep(@RequestBody PcuRequest pcuRequest) throws Exception {
        boolean isEntryStep;
        try {
            isEntryStep = pcuRouterHeaderService.isEntryStep(pcuRequest.getSite(), pcuRequest.getPcuBo(), pcuRequest.getRouter(), pcuRequest.getVersion(),pcuRequest.getOperation());
            return ResponseEntity.ok(isEntryStep);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getRoutingType")
    public ResponseEntity<String> getRoutingType(@RequestBody PcuRequest pcuRequest) throws Exception {
        String getRoutingType;
        try {
            getRoutingType = pcuRouterHeaderService.getRoutingType(pcuRequest.getSite(), pcuRequest.getRouter(), pcuRequest.getVersion());
            return ResponseEntity.ok(getRoutingType);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getRoutingSubType")
    public ResponseEntity<String> getRoutingSubType(@RequestBody PcuRequest pcuRequest) throws Exception {
        String getRoutingSubType;
        try {
            getRoutingSubType = pcuRouterHeaderService.getRoutingSubType(pcuRequest.getSite(), pcuRequest.getRouter(), pcuRequest.getVersion());
            return ResponseEntity.ok(getRoutingSubType);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("isLastReportingStep")
    public ResponseEntity<Boolean> isLastReportingStep(@RequestBody PcuRequest pcuRequest) throws Exception {
        boolean isLastReportingStep;
        try {
            isLastReportingStep = pcuRouterHeaderService.isLastReportingStep(pcuRequest.getSite(), pcuRequest.getPcuBo(), pcuRequest.getRouter(), pcuRequest.getVersion(), pcuRequest.getOperation(), pcuRequest.getOperationVersion());
            return ResponseEntity.ok(isLastReportingStep);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getCurrentRouter")
    public ResponseEntity<List<String>> getCurrentRouter(@RequestBody PcuRequest pcuRequest) throws Exception {
        List<String> getCurrentRouter;
        try {
            getCurrentRouter = pcuRouterHeaderService.getCurrentRouter(pcuRequest.getSite(), pcuRequest.getPcuBo());
            return ResponseEntity.ok(getCurrentRouter);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("pcuReleaseAtEntryStep")
    public ResponseEntity<PcuInQueue> pcuReleaseAtEntryStep(@RequestBody PcuRelease pcuRouterHeader) throws Exception {
        PcuInQueue pcuReleaseAtEntryStep;
        try {
            pcuReleaseAtEntryStep = pcuRouterHeaderService.pcuReleaseAtEntryStep(pcuRouterHeader.getSite(), pcuRouterHeader.getPcuBo() ,pcuRouterHeader.getRouting(),pcuRouterHeader.getVersion(),pcuRouterHeader.getUserBO());
            return ResponseEntity.ok(pcuReleaseAtEntryStep);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getEntryStep")
    public ResponseEntity<EntryStep> getEntryStep(@RequestBody PcuRouterHeader pcuRouterHeader) throws Exception {
        EntryStep getEntryStep;
        try {
            getEntryStep = pcuRouterHeaderService.getEntryStep(pcuRouterHeader.getSite(), pcuRouterHeader.getPcuBo());
            return ResponseEntity.ok(getEntryStep);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getAllEntryStep")
    public ResponseEntity<EntryStep> getAllEntryStep(@RequestBody PcuRequest pcuRouterHeader) throws Exception {
        EntryStep getAllEntryStep;
        try {
            getAllEntryStep = pcuRouterHeaderService.getAllEntryStep(pcuRouterHeader.getSite(), pcuRouterHeader.getPcuBo() ,pcuRouterHeader.getRouter(),pcuRouterHeader.getVersion());
            return ResponseEntity.ok(getAllEntryStep);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("placePCUQueueAtFirstOperation")
    public ResponseEntity<PcuInQueue> placePCUQueueAtFirstOperation(@RequestBody PcuRelease pcuRelease) throws Exception {
        PcuInQueue placePCUQueueAtFirstOperation;
        try {
            placePCUQueueAtFirstOperation = pcuRouterHeaderService.placePCUQueueAtFirstOperation(pcuRelease.getSite(), pcuRelease.getPcuBo(),pcuRelease.getUserBO());
            return ResponseEntity.ok(placePCUQueueAtFirstOperation);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("placePCUQueueAtSpecificOperation")
    public ResponseEntity<PcuInQueue> placePCUQueueAtSpecificOperation(@RequestBody PcuRelease pcuRelease) throws Exception {
        PcuInQueue placePCUQueueAtSpecificOperation;
        try {
            placePCUQueueAtSpecificOperation = pcuRouterHeaderService.placePCUQueueAtSpecificOperation(pcuRelease.getSite(), pcuRelease.getPcuBo() ,pcuRelease.getRouting(),pcuRelease.getVersion(), pcuRelease.getOperation(),pcuRelease.getUserBO());
            return ResponseEntity.ok(placePCUQueueAtSpecificOperation);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getStepDetails")
    public ResponseEntity<RoutingStep> getStepDetails(@RequestBody PcuRequest pcuRequest) throws Exception {
        RoutingStep getStepDetails;
        try {
            getStepDetails = pcuRouterHeaderService.getStepDetails(pcuRequest.getSite() ,pcuRequest.getRouter(),pcuRequest.getVersion(), pcuRequest.getOperation(), pcuRequest.getPcuBo());
            return ResponseEntity.ok(getStepDetails);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("getStepDetailsList")
    public ResponseEntity<List<RoutingStep>> getStepDetailsList(@RequestBody PcuRequest pcuRequest) throws Exception {
        List<RoutingStep> getStepDetails;
        try {
            getStepDetails = pcuRouterHeaderService.getStepDetailsList(pcuRequest.getSite() ,pcuRequest.getRouter(),pcuRequest.getVersion(), pcuRequest.getOperation(), pcuRequest.getPcuBo());
            return ResponseEntity.ok(getStepDetails);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("getOperationQueueList")
    public ResponseEntity<MessageModel> getOperationQueueList(@RequestBody PcuRequest pcuRequest) throws Exception {
        MessageModel getOperationQueueList;
        try {
            getOperationQueueList = pcuRouterHeaderService.getOperationQueueList(pcuRequest.getSite() ,pcuRequest.getRouter(),pcuRequest.getVersion(), pcuRequest.getOperation(), pcuRequest.getPcuBo(), pcuRequest.getOperationVersion());
            return ResponseEntity.ok(getOperationQueueList);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("getOperationNextStepID")
    public ResponseEntity<MessageModel> getOperationNextStepID(@RequestBody PcuRequest pcuRequest) throws Exception {
        MessageModel getOperationNextStepID;
        try {
            getOperationNextStepID = pcuRouterHeaderService.getOperationNextStepID(pcuRequest.getSite() ,pcuRequest.getRouter(),pcuRequest.getVersion(), pcuRequest.getOperation(), pcuRequest.getPcuBo());
            return ResponseEntity.ok(getOperationNextStepID);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("updateNeedsToBeCompleted")
    public ResponseEntity<MessageModel> updateNeedsToBeCompleted(@RequestBody PcuRequest pcuRequest) throws Exception {
        MessageModel updateNeedsToBeCompleted;
        try {
            updateNeedsToBeCompleted = pcuRouterHeaderService.updateNeedsToBeCompleted(pcuRequest.getSite(), pcuRequest.getPcuBo() ,pcuRequest.getRouter(),pcuRequest.getVersion(), pcuRequest.getOperation(), pcuRequest.getStepId(),pcuRequest.getOperationVersion());
            return ResponseEntity.ok(updateNeedsToBeCompleted);
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("disableRecord")
    public ResponseEntity<MessageModel> disablePcuRouterHeader(@RequestBody PcuRouterHeaderRequest pcuRequest) throws Exception {
        MessageModel messageModel=new MessageModel();
        List<MessageDetails> message=new ArrayList<MessageDetails>();
        MessageDetails obj=new MessageDetails();
        Boolean disable = false;
        try {
         if(pcuRouterHeaderService.disableRecord(pcuRequest)){
             obj.setMsg("success");
             obj.setMsg_type("Ok");
             messageModel.setMessage_details(message);
             return  ResponseEntity.ok(messageModel);
         }
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            obj.setMsg(pcuRouterHeaderException.getLocalizedMessage());
            obj.setMsg_type("Error");
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            obj.setMsg(e.getLocalizedMessage());
            obj.setMsg_type("Error");
            throw new RuntimeException(e);
        }
        return   ResponseEntity.ok(messageModel);
    }
    @PostMapping("enableRecord")
    public ResponseEntity<MessageModel> enablePcuRouterHeader(@RequestBody PcuRouterHeaderRequest pcuRequest) throws Exception {
        MessageModel messageModel=new MessageModel();
        List<MessageDetails> message=new ArrayList<MessageDetails>();
        MessageDetails obj=new MessageDetails();
        Boolean disable = false;
        try {
            if(pcuRouterHeaderService.enableRecord(pcuRequest)){
                obj.setMsg("success");
                obj.setMsg_type("Ok");
                messageModel.setMessage_details(message);
                return  ResponseEntity.ok(messageModel);
            }
        } catch (PcuRouterHeaderException pcuRouterHeaderException) {
            obj.setMsg(pcuRouterHeaderException.getLocalizedMessage());
            obj.setMsg_type("Error");
            throw pcuRouterHeaderException;
        } catch (Exception e) {
            obj.setMsg(e.getLocalizedMessage());
            obj.setMsg_type("Error");
            throw new RuntimeException(e);
        }
        return   ResponseEntity.ok(messageModel);
    }
}
