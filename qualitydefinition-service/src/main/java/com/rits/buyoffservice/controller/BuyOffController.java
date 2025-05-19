package com.rits.buyoffservice.controller;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.QualityDefinitionServiceApplication;
import com.rits.buyoffservice.dto.*;
import com.rits.buyoffservice.exception.BuyOffException;
import com.rits.buyoffservice.model.MessageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rits.buyoffservice.model.BuyOff;
import com.rits.buyoffservice.service.BuyOffServiceImpl;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/buyoff-service")
public class BuyOffController {
    private final BuyOffServiceImpl serviceImpl;
    private final ObjectMapper objectMapper;
    private final ApplicationContext context;

//    "required": ["site", "buyOff", "version", "status"]

//    {
//        "site":"RITS",
//            "buyOff":"buyoff6787",
//            "version":"A",
//            "handle":"",
//            "description":"",
//            "status":"new",
//            "messageType":"",
//            "partialAllowed":true,
//            "rejectAllowed":true,
//            "skipAllowed":true,
//            "currentVersion":true,
//            "userGroupList" : [
//        {
//            "userGroup":""
//        },
//        {
//            "userGroup":""
//        }
//    ],
//        "attachmentList":[
//        {
//            "sequence":"sequence2",
//                "quantityRequired":"quantityRequired2",
//                "item":"item2",
//                "itemVersion":"itemVersion2",
//                "routing":"routing2",
//                "routingVersion":"routingVersion2",
//                "stepId":"stepId2",
//                "operation":"operation2",
//                "workCenter":"workcenter2",
//                "resource":"resource2",
//                "resourceType":"resourceType2",
//                "shopOrder":"shopOrder2",
//                "pcu":"pcu2"
//        }
//	],
//        "customDataList" : [
//        {
//            "customData": "",
//                "value": ""
//        },
//        {
//            "customData": "",
//                "value": ""
//        }
//    ]
//
//    }



    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createBuyOff(@RequestBody BuyOffRequest buyOffRequest) throws Exception {
        MessageModel createBuyOff;
//        MessageModel validationResponse = serviceImpl.validation( payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        BuyOffRequest buyOffRequest = new ObjectMapper().convertValue(payload, BuyOffRequest.class);

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(buyOffRequest.getSite()).hookPoint("PRE").activity("buyoff-service").hookableMethod("create").request(objectMapper.writeValueAsString(buyOffRequest)).build();
        String preExtensionResponse = serviceImpl.callExtension(preExtension);
        BuyOffRequest preExtensionBuyOff = objectMapper.readValue(preExtensionResponse, BuyOffRequest.class);

        try {
            createBuyOff = serviceImpl.createBuyOff(preExtensionBuyOff);
            Extension postExtension = Extension.builder().site(buyOffRequest.getSite()).hookPoint("POST").activity("usergroup-service").hookableMethod("create").request(objectMapper.writeValueAsString(createBuyOff.getResponse())).build();
            String postExtensionResponse = serviceImpl.callExtension(postExtension);
            BuyOff postExtensionBuyOff = objectMapper.readValue(postExtensionResponse, BuyOff.class);
            return ResponseEntity.ok( MessageModel.builder().message_details(createBuyOff.getMessage_details()).response(postExtensionBuyOff).build());
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

//    {
//        "site":"RITS"
//    }
    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BuyOffTop50Record>> retrieveTop50BuyOff(@RequestBody BuyOffRequest buyOffRequest) {
        if (buyOffRequest.getSite() != null && !buyOffRequest.getSite().isEmpty()) {
            try {
                return ResponseEntity.ok(serviceImpl.retrieveTop50BuyOff(buyOffRequest));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,buyOffRequest.getSite());
    }

    //    {
//        "site":"RITS"
//    }
    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BuyOffTop50Record>> retrieveAll(@RequestBody BuyOffRequest buyOffRequest) {
        if (buyOffRequest.getSite() != null && !buyOffRequest.getSite().isEmpty()) {
            try {
                return ResponseEntity.ok(serviceImpl.retrieveAll(buyOffRequest));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,buyOffRequest.getSite());
    }

//    {
//        "site":"RITS",
//        "buyOff":"buyoff6"
//    }

    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<BuyOff> retrieve(@RequestBody BuyOffRequest buyOffRequest) throws JsonProcessingException {

            BuyOff retrieveBuyOff;
//            try {
//                return ResponseEntity.ok(serviceImpl.retrieve(buyOffRequest));

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(buyOffRequest.getSite()).hookPoint("PRE").activity("buyoff-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(buyOffRequest)).build();
        String preExtensionResponse = serviceImpl.callExtension(preExtension);
        BuyOffRequest preExtensionBuyOff = objectMapper.readValue(preExtensionResponse, BuyOffRequest.class);

        try {
            retrieveBuyOff = serviceImpl.retrieve(preExtensionBuyOff);
            Extension postExtension = Extension.builder().site(buyOffRequest.getSite()).hookPoint("POST").activity("buyoff-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveBuyOff)).build();
            String postExtensionResponse = serviceImpl.callExtension(postExtension);
            BuyOff postExtensionBuyOff = objectMapper.readValue(postExtensionResponse, BuyOff.class);
            return ResponseEntity.ok(postExtensionBuyOff);

            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

//    {
//        "site":"RITS",
//            "buyOff":"buyoff6"
//    }

    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isCategoryExists(@RequestBody BuyOffRequest buyOffRequest) {
        if (buyOffRequest.getSite() != null && !buyOffRequest.getSite().isEmpty()) {
            try {
                return ResponseEntity.ok(serviceImpl.isBuyOffExist(buyOffRequest.getBuyOff(), buyOffRequest.getVersion(), buyOffRequest.getSite()));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,buyOffRequest.getSite());
    }

    //    {
//        "site":"RITS",
//            "buyOff":"buyoff6",
//            "version":"",
//            "handle":"",
//            "description":"",
//            "status":"",
//            "messageType":"",
//            "partialAllowed":true,
//            "rejectAllowed":true,
//            "skipAllowed":true,
//            "currentVersion":true,
//            "userGroupList" : [
//        {
//            "userGroup":""
//        },
//        {
//            "userGroup":""
//        }
//    ],
//        "attachmentList":[
//        {
//            "sequence":"sequence2",
//                "quantityRequired":"quantityRequired2",
//                "item":"item2",
//                "itemVersion":"itemVersion2",
//                "routing":"routing2",
//                "routingVersion":"routingVersion2",
//                "stepId":"stepId2",
//                "operation":"operation2",
//                "workcenter":"workcenter2",
//                "resource":"resource2",
//                "resourceType":"resourceType2",
//                "shopOrder":"shopOrder2",
//                "pcu":"pcu2"
//        }
//	],
//        "customDataList" : [
//        {
//            "customData": "",
//                "value": ""
//        },
//        {
//            "customData": "",
//                "value": ""
//        }
//    ]
//
//    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> update(@RequestBody BuyOffRequest buyOffRequest) throws JsonProcessingException {
        MessageModel updateBuyOff;

//            try {
//                return ResponseEntity.ok(serviceImpl.update(buyOffRequest));

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(buyOffRequest.getSite()).hookPoint("PRE").activity("buyoff-service").hookableMethod("update").request(objectMapper.writeValueAsString(buyOffRequest)).build();
        String preExtensionResponse = serviceImpl.callExtension(preExtension);
        BuyOffRequest preExtensionBuyOff = objectMapper.readValue(preExtensionResponse, BuyOffRequest.class);

        try {
            updateBuyOff = serviceImpl.update(preExtensionBuyOff);
            Extension postExtension = Extension.builder().site(buyOffRequest.getSite()).hookPoint("POST").activity("buyoff-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateBuyOff.getResponse())).build();
            String postExtensionResponse = serviceImpl.callExtension(postExtension);
            BuyOff postExtensionBUyOff = objectMapper.readValue(postExtensionResponse, BuyOff.class);
            return ResponseEntity.ok( MessageModel.builder().message_details(updateBuyOff.getMessage_details()).response(postExtensionBUyOff).build());

            } catch (BuyOffException e) {
                throw e;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

    }

//    {
//        "site":"RITS",
//        "buyOff":"buyoff6"
//    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> delete(@RequestBody BuyOffRequest buyOffRequest) throws JsonProcessingException {
        MessageModel deleteResponse;

//            try {
//                return ResponseEntity.ok(serviceImpl.delete(buyOffRequest));

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(buyOffRequest.getSite()).hookPoint("PRE").activity("buyoff-service").hookableMethod("delete").request(objectMapper.writeValueAsString(buyOffRequest)).build();
        String preExtensionResponse = serviceImpl.callExtension(preExtension);
        BuyOffRequest preExtensionBuyOff = objectMapper.readValue(preExtensionResponse, BuyOffRequest.class);

        try {
            deleteResponse = serviceImpl.delete(preExtensionBuyOff);
            Extension postExtension = Extension.builder().site(buyOffRequest.getSite()).hookPoint("POST").activity("buyoff-service").hookableMethod("delete").request(objectMapper.writeValueAsString(deleteResponse.getResponse())).build();
            String postExtensionResponse = serviceImpl.callExtension(postExtension);
            BuyOff postExtensionUserGroup = objectMapper.readValue(postExtensionResponse, BuyOff.class);
            return ResponseEntity.ok(deleteResponse);

            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

    }



    @PostMapping("/retrieveByAttachmentDetails")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BuyOff>> retrieveBuyOffByAttachmentDetails(@RequestBody AttachmentDetailsRequest attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {
            try {
                return ResponseEntity.ok(serviceImpl.retrieveByAttachmentDetails(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "item":"item2"
//    }

    @PostMapping("/retrieveByItem")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> retrieveBuyOffNameListByItem(@RequestBody AttachmentDetailsRequest attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {
            try {
                return ResponseEntity.ok(serviceImpl.retrieveBuyOffNameListByItem(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "item":"item2",
//            "operation":"operation2"
//    }

    @PostMapping("/retrieveByItemAndOperation")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> retrieveByAttachmentListItemAndOperation(@RequestBody AttachmentDetailsRequest attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {
            try {
                return ResponseEntity.ok(serviceImpl.retrieveBuyOffNameByItemAndOperation(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "pcu":"pcu2",
//            "operation":"operation2"
//    }

    @PostMapping("/retrieveByPcuAndOperation")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> retrieveByAttachmentListPcuAndOperation(@RequestBody AttachmentDetailsRequest attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {
            try {
                return ResponseEntity.ok(serviceImpl.retrieveBuyOffNameByPcuAndOperation(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "item":"item2",
//            "operation":"operation2",
//            "routing":"routing2"
//    }

    @PostMapping("/retrieveByItemAndOperationAndRouting")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> retrieveByAttachmentListItemAndOperationAndRouting(@RequestBody AttachmentDetailsRequest attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {
            try {
                return ResponseEntity.ok(serviceImpl.retrieveBuyOffNameByItemAndOperationAndRouting(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "resource":"resource2"
//    }

    @PostMapping("/retrieveByResource")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> retrieveBuyOffNameListByResource(@RequestBody AttachmentDetailsRequest attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {
            try {
                return ResponseEntity.ok(serviceImpl.retrieveBuyOffNameListByResource(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "resource":"resource2",
//            "pcu":"pcu2"
//    }

    @PostMapping("/retrieveByResourceAndPcu")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> retrieveBuyOffNameListByResourceAndPcu(@RequestBody AttachmentDetailsRequest attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {

            try {
                return ResponseEntity.ok(serviceImpl.retrieveBuyOffNameListByResourceAndPcu(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "shopOrder":"shopOrder2",
//            "operation":"operation2"
//    }

    @PostMapping("/retrieveByShopOrderAndOperation")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> retrieveBuyOffNameListByShopOrderAndOperation(@RequestBody AttachmentDetailsRequest attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {

            try {
                return ResponseEntity.ok(serviceImpl.retrieveBuyOffNameListByShopOrderAndOperation(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "pcu":"pcu2",
//            "item":"item2"
//    }

    @PostMapping("/retrieveByPcuAndCallItem")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> retrieveBuyOffNameListByPcu(@RequestBody AttachmentDetailsRequest attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {

            try {
                return ResponseEntity.ok(serviceImpl.retrieveBuyOffNameListByPcu(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "pcu":"pcu2",
//            "operation":"operation2",
//            "routing":"routing2",
//            "item":"item2"
//    }

    @PostMapping("/retrieveByOperationAndRoutingAndPcuAndCallItem")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> retrieveBuyOffNameByOperationAndRoutingAndPcuAndMergeItemOp
            (@RequestBody AttachmentDetailsRequest attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {

            try {
                return ResponseEntity.ok(serviceImpl.retrieveBuyOffNameByOperationAndRoutingAndPcuAndMergeItemOp(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "workCenter":"workCenter2",
//            "resource":"resource2"
//    }

    @PostMapping("/retrieveByWorkCenterAndCallResource")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> retrieveBuyOffNameByWorkCenterAndMergeResourceList(@RequestBody AttachmentDetailsRequest
                                                                                   attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {

            try {
                return ResponseEntity.ok(serviceImpl.retrieveBuyOffNameByWorkCenterAndMergeResourceList(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "shopOrder":"shopOrder2",
//            "item":"item2"
//    }

    @PostMapping("/retrieveByShopOrderAndCallItem")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> retrieveBuyOffNameListByShopOrderAndMergeItemList(@RequestBody AttachmentDetailsRequest
                                                                                  attachmentDetailsRequest) {
        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {
            try {
                return ResponseEntity.ok(serviceImpl.retrieveBuyOffNameListByShopOrderAndMergeItemList(attachmentDetailsRequest));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,attachmentDetailsRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "buyOff":"buyoff6",
//            "userGroupList":[
//        "userGroup1","userGroup2"
//    ]
//    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<BuyOff> associateResourceUserGroup(@RequestBody AssociateUserGroup associateUserGroup)
    {
        if (associateUserGroup.getSite() != null && !associateUserGroup.getSite().isEmpty()) {
            try {
                return  ResponseEntity.ok(serviceImpl.associateResourceUserGroup(associateUserGroup));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,associateUserGroup.getSite());
    }

//    {
//        "site":"RITS",
//            "buyOff":"buyoff6",
//            "userGroupList":[
//        "userGroup1","userGroup2"
//    ]
//    }

    @PostMapping("/remove")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<BuyOff> removeUserGroupType(@RequestBody AssociateUserGroup associateUserGroup)
    {
        if (associateUserGroup.getSite() != null && !associateUserGroup.getSite().isEmpty()) {
            try {
                return  ResponseEntity.ok(serviceImpl.removeUserGroupType(associateUserGroup));
            } catch (BuyOffException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BuyOffException(2702,associateUserGroup.getSite());
    }

//    @PostMapping("/retrieveAttachmentDetailsList")
//    @ResponseStatus(HttpStatus.OK)
//    public List<String> retrieveAttachmentDetailsList(@RequestBody  AttachmentDetailsRequest attachmentDetailsRequest)
//    {
//        if (attachmentDetailsRequest.getSite() != null && !attachmentDetailsRequest.getSite().isEmpty()) {
//            return serviceImpl.retrieveAttachmentDetailsList(attachmentDetailsRequest);
//        }
//        throw new EmptySiteException(attachmentDetailsRequest.getSite());
//    }

    @PostMapping("/availableUserGroup")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AvailableUserGroups> availableUserGroup(@RequestBody AssociateUserGroup associateUserGroup)
    {
        try {
            AvailableUserGroups availableUserGroups = serviceImpl.availableUserGroup(associateUserGroup);
            return ResponseEntity.ok(availableUserGroups);
        } catch (BuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/isSkipAllowed")
    @ResponseStatus(HttpStatus.OK)
    public Boolean isSkipAllowed(@RequestBody AssociateUserGroup associateUserGroup)
    {
        try {
            Boolean isSkipAllowed = serviceImpl.isSkipAllowed(associateUserGroup.getBuyOff());
            return isSkipAllowed;
        } catch (BuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/isPartialAllowed")
    @ResponseStatus(HttpStatus.OK)
    public Boolean isPartialAllowed(@RequestBody AssociateUserGroup associateUserGroup)
    {
        try {
            Boolean isPartialAllowed = serviceImpl.isPartialAllowed(associateUserGroup.getBuyOff());
            return isPartialAllowed;
        } catch (BuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/isRejectAllowed")
    @ResponseStatus(HttpStatus.OK)
    public Boolean isRejectAllowed(@RequestBody AssociateUserGroup associateUserGroup)
    {
        try {
            Boolean isRejectAllowed = serviceImpl.isRejectAllowed(associateUserGroup.getBuyOff());
            return isRejectAllowed;
        } catch (BuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/shutdown")
    public void shutdown() {
        System.out.println("Shutting down...");
        SpringApplication.exit(context, () -> 1);
    }
}
