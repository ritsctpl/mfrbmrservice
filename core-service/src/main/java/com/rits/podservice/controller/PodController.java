package com.rits.podservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.AuditLogService;
import com.rits.auditlogservice.service.ProducerEvent;
import com.rits.podservice.dto.*;
import com.rits.podservice.exception.PodException;
import com.rits.podservice.model.MessageModel;
import com.rits.podservice.model.Pod;
import com.rits.podservice.service.PodService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/app/v1/pod-service")
public class PodController {
    private final PodService podService;
    private final  ApplicationEventPublisher eventPublisher;


//    {
//        "site": "RITS",
//            "podName": "Z_OPERATION4_DEF",
//            "description": "RITS CUSTOM POD",
//            "status": "Enabled",
//            "panelLayout": "",
//            "kafkaIntegration": true,
//            "kafkaId": "",
//            "sessionTimeout": 30,
//            "refreshRate": 5000,
//            "defaultOperation": "OPEARTION1",
//            "defaultResource": "RESOURCE1",
//            "operationCanBeChanged": true,
//            "resourceCanBeChanged": true,
//            "showQuantity": true,
//            "pcuQueueButtonId": "BUTTONACTIVITY1",
//            "pcuInWorkButtonId": "BUTTONACTIVITY2",
//            "documentName": "Document",
//            "buttonList": [
//        {
//            "sequence": "1",
//                "buttonType": "Normal",
//                "buttonId": "startButtonId",
//                "buttonLabel": "START",
//                "buttonSize": "100",
//                "imageIcon": "",
//                "hotKey": "",
//                "buttonLocation": "middle",
//                "activityList": [
//            {
//                "activitySequence": "10",
//                    "activity": "ButtonActivity1",
//                    "type": "UI",
//                    "url": "",
//                    "pluginLocation": "1",
//                    "clearsPcu": true
//            },
//            {
//                "activitySequence": "20",
//                    "activity": "ButtonActivity2",
//                    "type": "Service",
//                    "url": "",
//                    "pluginLocation": "2",
//                    "clearsPcu": true
//            }
//      ]
//        }
//  ],
//        "listOptions":[
//        "browseWorkList":"",
//            "podWorkList":"",
//            "assembleList":"",
//            "dcCollectList":"",
//            "toolList":"",
//            "workInstructionList":"",
//            "dcEntryList":"",
//            "subStepList":""
//  ],
//        "podSelection":[
//        "mainInput":"",
//            "mainInputHotKey":"",
//            "defaultOperation":"",
//            "defaultResource":"",
//            "sfcQueueButtonID":"",
//            "sfcInWorkButtonID":"",
//            "infoLine1":"",
//            "infoLine2":"",
//            "showOperationFirst":true,
//            "showQuantity":true,
//            "operationCanBeChanged":true,
//            "resourceCanBeChanged":true
//  ],
//        "printers":[
//        "documentPrinter":"",
//            "labelPrinter":"",
//            "travelerPrinter":""
//  ],
//        "customDataList": [
//        {
//            "customData": "",
//                "value": ""
//        },
//        {
//            "customData": "",
//                "value": ""
//        }
//  ]
//    }

//    @PostMapping("/create")
//    @ResponseStatus(HttpStatus.CREATED)
//    public ResponseEntity<?> createPod(@RequestBody JsonNode payload){
//        ResponseEntity<?> validationResponse = podService.validation(payload);
//        if (validationResponse.getStatusCode() != HttpStatus.OK) {
//            return ResponseEntity.badRequest().body(validationResponse.getBody());
//        }
//        PodRequest podRequest = new ObjectMapper().convertValue(payload, PodRequest.class);
//        MessageModel createResponse;
//            try {
//                createResponse= podService.createPod(podRequest);
//                return ResponseEntity.ok(createResponse);
//            }catch(PodException e){
//                throw e;
//            }catch (Exception e) {
//                throw new RuntimeException(e);
//        }
//    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createPod(@RequestBody PodRequest podRequest){
//        MessageModel validationResponse = podService.validation( payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        PodRequest podRequest = new ObjectMapper().convertValue(payload, PodRequest.class);
        MessageModel createResponse;
        if(podRequest.getSite()!=null &&  !podRequest.getSite().isEmpty()){
            try {
                createResponse= podService.createPod(podRequest);

                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(podRequest.getSite())
                        .change_type("Create")
                        .action_code("POD-CREATE")
                        .action_detail("POD CREATED "+podRequest.getPodName())
                        .action_detail_handle("ActionDetailBO:"+podRequest.getSite()+","+"POD-CREATE"+","+podRequest.getUserId()+":"+"com.rits.podservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(podRequest.getUserId())
                        .txnId("POD-CREATE"+String.valueOf(LocalDateTime.now())+podRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Create")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                return ResponseEntity.ok(createResponse);
            }catch(PodException e){
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new PodException(1802,podRequest.getSite());
    }


//    {
//        "podName":"Z_OPERATION_DEF",
//            "site":"RITS"
//    }
    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Pod> retrievePod(@RequestBody  PodRequest podRequest){
        Pod retrieveResponse;
        if(podRequest.getSite()!=null &&  !podRequest.getSite().isEmpty()) {
            try {
                retrieveResponse= podService.retrievePod(podRequest);
                return ResponseEntity.ok(retrieveResponse);
            }catch(PodException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new PodException(1802,podRequest.getSite());
    }


    //    {
//        "site": "RITS",
//            "podName": "Z_OPERATION4_DEF",
//            "description": "RITS CUSTOM POD",
//            "status": "Enabled",
//            "panelLayout": "",
//            "kafkaIntegration": true,
//            "kafkaId": "",
//            "sessionTimeout": 30,
//            "refreshRate": 5000,
//            "defaultOperation": "OPEARTION1",
//            "defaultResource": "RESOURCE1",
//            "operationCanBeChanged": true,
//            "resourceCanBeChanged": true,
//            "showQuantity": true,
//            "pcuQueueButtonId": "BUTTONACTIVITY1",
//            "pcuInWorkButtonId": "BUTTONACTIVITY2",
//            "documentName": "Document",
//            "buttonList": [
//        {
//            "sequence": "1",
//                "buttonType": "Normal",
//                "buttonId": "startButtonId",
//                "buttonLabel": "START",
//                "buttonSize": "100",
//                "imageIcon": "",
//                "hotKey": "",
//                "buttonLocation": "middle",
//                "activityList": [
//            {
//                "activitySequence": "10",
//                    "activity": "ButtonActivity1",
//                    "type": "UI",
//                    "url": "",
//                    "pluginLocation": "1",
//                    "clearsPcu": true
//            },
//            {
//                "activitySequence": "20",
//                    "activity": "ButtonActivity2",
//                    "type": "Service",
//                    "url": "",
//                    "pluginLocation": "2",
//                    "clearsPcu": true
//            }
//      ]
//        }
//  ],
//        "listOptions":[
//        "browseWorkList":"",
//            "podWorkList":"",
//            "assembleList":"",
//            "dcCollectList":"",
//            "toolList":"",
//            "workInstructionList":"",
//            "dcEntryList":"",
//            "subStepList":""
//  ],
//        "podSelection":[
//        "mainInput":"",
//            "mainInputHotKey":"",
//            "defaultOperation":"",
//            "defaultResource":"",
//            "sfcQueueButtonID":"",
//            "sfcInWorkButtonID":"",
//            "infoLine1":"",
//            "infoLine2":"",
//            "showOperationFirst":true,
//            "showQuantity":true,
//            "operationCanBeChanged":true,
//            "resourceCanBeChanged":true
//  ],
//        "printers":[
//        "documentPrinter":"",
//            "labelPrinter":"",
//            "travelerPrinter":""
//  ],
//        "customDataList": [
//        {
//            "customData": "",
//                "value": ""
//        },
//        {
//            "customData": "",
//                "value": ""
//        }
//  ]
//    }


    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updatePod(@RequestBody PodRequest podRequest) {
        MessageModel updateResponse;
        if(podRequest.getSite()!=null &&  !podRequest.getSite().isEmpty()){
            try {
                updateResponse= podService.updatePod(podRequest);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(podRequest.getSite())
                        .change_type("Update")
                        .action_code("POD-UPDATE")
                        .action_detail("POD UPDATED "+podRequest.getPodName())
                        .action_detail_handle("ActionDetailBO:"+podRequest.getSite()+","+"POD-UPDATE"+","+podRequest.getUserId()+":"+"com.rits.podservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(podRequest.getUserId())
                        .txnId("POD-UPDATE"+String.valueOf(LocalDateTime.now())+podRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Update")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return ResponseEntity.ok(updateResponse);
            } catch(PodException e){
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new PodException(1802,podRequest.getSite());
    }
//
//    {
//        "podName":"Z_OPERATION_DEF",
//            "site":"RITS"
//    }
    @PostMapping("/delete")
    public ResponseEntity<?> deletePod(@RequestBody DeleteRequest deleteRequest){
        MessageModel deleteResponse;
        if(deleteRequest.getSite()!=null &&  !deleteRequest.getSite().isEmpty()) {
            try {
                deleteResponse= podService.deletePod(deleteRequest);

                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(deleteRequest.getSite())
                        .change_type("Delete")
                        .action_code("POD-DELETE")
                        .action_detail("POD DELETED "+deleteRequest.getPodName())
                        .action_detail_handle("ActionDetailBO:"+deleteRequest.getSite()+","+"POD-DELETE"+","+deleteRequest.getUserId()+":"+"com.rits.podservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(deleteRequest.getUserId())
                        .txnId("POD-DELETE"+String.valueOf(LocalDateTime.now())+deleteRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Delete")
                        .topic("audit-log")
                        .build();

                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return ResponseEntity.ok(deleteResponse);
            }catch(PodException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new PodException(1802,deleteRequest.getSite());
    }

//    {
//        "podName":"Z_OPERATION_DEF",
//            "site":"RITS"
//    }
    @PostMapping("retrieveButtonList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RButtonResponseList> getButtonList(@RequestBody ButtonListRequest buttonListRequest) {
        RButtonResponseList buttonListResponse;
            if(buttonListRequest.getSite()!=null &&  !buttonListRequest.getSite().isEmpty()) {
                try {
                    buttonListResponse= podService.getButtonList(buttonListRequest);
                    return ResponseEntity.ok(buttonListResponse);
                }catch(PodException e){
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        throw new PodException(1802,buttonListRequest.getSite());
    }

//    {
//            "site":"RITS"
//    }
    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PodResponseList> getPodByCreationDate(@RequestBody PodListRequest podListRequest) {
        PodResponseList retrieveTop50Response;
        if(podListRequest.getSite()!=null &&  !podListRequest.getSite().isEmpty()) {
            try {
                retrieveTop50Response= podService.getPodListByCreationDate(podListRequest);
                return ResponseEntity.ok(retrieveTop50Response);
            } catch(PodException e){
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new PodException(1802,podListRequest.getSite());
    }


    //    {
//        "podName":"Z",   // any letter conatining it retrievs all that pods that conatins that letter
//            "site":"RITS"
//    }
    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PodResponseList> getPodList(@RequestBody PodListRequest podListRequest){
        PodResponseList retrieveAllResponse;
        if(podListRequest.getSite()!=null && !podListRequest.getSite().isEmpty()){
            try {
                retrieveAllResponse= podService.getPodList(podListRequest);
                return ResponseEntity.ok(retrieveAllResponse);
            } catch(PodException e){
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new PodException(1802,podListRequest.getSite());
    }


//    {
//        "podName":"Z_OPERATION_DEF",
//            "site":"RITS"
//    }
    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isPodExist(@RequestBody PodExistRequest podExistRequest) {
        Boolean isExistResponse;
        if (podExistRequest.getSite() != null && !podExistRequest.getSite().isEmpty()) {
            try {
                isExistResponse= podService.isPodExist(podExistRequest);
                return ResponseEntity.ok(isExistResponse);
            } catch(PodException e){
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new PodException(1802,podExistRequest.getSite());
    }

}
