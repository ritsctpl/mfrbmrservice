package com.rits.datacollectionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.datacollectionservice.dto.*;
import com.rits.datacollectionservice.exception.DataCollectionException;
import com.rits.datacollectionservice.model.Attachment;
import com.rits.datacollectionservice.model.DataCollection;
import com.rits.datacollectionservice.model.DataCollectionMessageModel;
import com.rits.datacollectionservice.model.Parameter;
import com.rits.datacollectionservice.service.DataCollectionServiceImpl;
import com.rits.kafkaservice.ProducerEvent;
import com.rits.operationservice.dto.AuditLogRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/datacollection-service")
public class DataCollectionController {
    private final DataCollectionServiceImpl dataCollectionService;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private final ApplicationEventPublisher eventPublisher;


//{
//    "site": "rits",
//    "dataCollection": "DataCollection1",
//    "version": "B",
//    "status": "Active",
//    "collectionType": "sfc"
//}


    //input 2


    //{
    //    "site": "rits",
    //    "dataCollection": "DataCollection1",
    //    "version": "B",
    //    "status": "Active",
    //    "collectionType": "sfc",
    //    "description": "Description A",
    //    "currentVersion": true,
    //    "collectDataAt": "Work Center A",
    //    "erpGroup": "ERP Group A",
    //    "qmInspectionGroup": "QM Inspection Group A",
    //    "passOrFailGroup": "Pass/Fail Group A",
    //    "failOrRejectNumber": "1001",
    //    "userAuthenticationRequired": "true",
    //    "certification": "Certification A",
    //    "frequency": "30",
    //    "parameterList": [
    //        {
    //            "sequence": "1",
    //            "parameterName": "Parameter 1",
    //            "description": "Description of Parameter 1",
    //            "type": "numeric",
    //            "prompt": "Prompt for Parameter 1",
    //            "status": "Active",
    //            "allowMissingValues": true,
    //            "displayDataValues": true,
    //            "falseValue": "0",
    //            "trueValue": "1",
    //            "dataField": "Data Field A",
    //            "formula": "",
    //            "minValue": "0",
    //            "maxValue": "100",
    //            "targetValue": "50",
    //            "softLimitCheckOnMinOrMaxValue": true,
    //            "overrideMinOrMax": true,
    //            "autoLogNconMinOrMaxOverride": true,
    //            "certification": "Certification A",
    //            "ncCode": "NC Code A",
    //            "mask": "",
    //            "unitOfMeasure": "mm",
    //            "requiredDataEntries": 1,
    //            "optionalDataEntries": 11
    //        },
    //        {
    //            "sequence": "2",
    //            "parameterName": "Parameter 2",
    //            "description": "Description of Parameter 2",
    //            "type": "text",
    //            "prompt": "Prompt for Parameter 2",
    //            "status": "Active",
    //            "allowMissingValues": false,
    //            "displayDataValues": true,
    //            "falseValue": "",
    //            "trueValue": "",
    //            "dataField": "Data Field B",
    //            "formula": "",
    //            "minValue": "",
    //            "maxValue": "",
    //            "targetValue": "",
    //            "softLimitCheckOnMinOrMaxValue": false,
    //            "overrideMinOrMax": false,
    //            "autoLogNconMinOrMaxOverride": false,
    //            "certification": "Certification B",
    //            "ncCode": "NC Code B",
    //            "mask": "mask",
    //            "unitOfMeasure": "mm",
    //            "requiredDataEntries": 1,
    //            "optionalDataEntries": 11
    //        }
    //    ],
    //    "attachmentList": [
    //        {
    //            "sequence": "1",
    //            "itemGroup": "Item Group A",
    //            "item": "Item2",
    //            "itemVersion": "1.0",
    //            "routing": "Routing2",
    //            "routingVersion": "1.0",
    //            "operation": "Operation2",
    //            "workCenter": "workCenter2",
    //            "resource": "Resource2",
    //            "shopOrder": "Shop Order2",
    //            "pcu": "PCU2"
    //        }
    //    ],
    //    "customDataList": [
    //        {
    //            "customData": "data",
    //            "value": "value"
    //        }
    //    ],
    //    "active": 1,
    //    "createdDateTime": "2023-04-06T14:41:13.365",
    //    "modifiedDateTime": null
    //}
    @PostMapping("create")
    public ResponseEntity<?> createDataCollection(@RequestBody DataCollectionRequest dataCollectionRequest) throws Exception {
//        MessageModel validationResponse = dataCollectionService.validation( payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        DataCollectionRequest dataCollectionRequest = new ObjectMapper().convertValue(payload, DataCollectionRequest.class);
        DataCollectionMessageModel createDataCollection;
        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(dataCollectionRequest.getSite()).hookPoint("PRE").activity("datacollection-service").hookableMethod("create").request(objectMapper.writeValueAsString(dataCollectionRequest)).build();
        String preExtensionResponse = dataCollectionService.callExtension(preExtension);
        DataCollectionRequest preExtensionDataCollection = objectMapper.readValue(preExtensionResponse, DataCollectionRequest.class);

        try {
            createDataCollection = dataCollectionService.createDataCollection(preExtensionDataCollection);

            Extension postExtension = Extension.builder().site(dataCollectionRequest.getSite()).hookPoint("POST").activity("datacollection-service").hookableMethod("create").request(objectMapper.writeValueAsString(createDataCollection.getResponse())).build();
            String postExtensionResponse = dataCollectionService.callExtension(postExtension);
            DataCollection postExtensionDataCollection = objectMapper.readValue(postExtensionResponse, DataCollection.class);

            AuditLogRequest activityLog = dataCollectionService.createAUditLog(dataCollectionRequest);
            eventPublisher.publishEvent(new ProducerEvent(activityLog));

            return ResponseEntity.ok(DataCollectionMessageModel.builder().message_details(createDataCollection.getMessage_details()).response(postExtensionDataCollection).build());


        } catch (DataCollectionException dataCollectionException) {
            throw dataCollectionException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //{
    //    "site": "rits",
    //    "dataCollection": "DataCollection1",
    //    "version": "B",
    //    "description": "Description A",
    //    "status": "Active",
    //    "currentVersion": true,
    //    "collectionType": "sfc",
    //    "collectDataAt": "Work Center A",
    //    "erpGroup": "ERP Group A",
    //    "qmInspectionGroup": "QM Inspection Group A",
    //    "passOrFailGroup": "Pass/Fail Group A",
    //    "failOrRejectNumber": "1001",
    //    "userAuthenticationRequired": "true",
    //    "certification": "Certification A",
    //    "frequency": "30",
    //    "parameterList": [
    //        {
    //            "sequence": "1",
    //            "parameterName": "Parameter 1",
    //            "description": "Description of Parameter 1",
    //            "type": "numeric",
    //            "prompt": "Prompt for Parameter 1",
    //            "status": "Active",
    //            "allowMissingValues": true,
    //            "displayDataValues": true,
    //            "falseValue": "0",
    //            "trueValue": "1",
    //            "dataField": "Data Field A",
    //            "formula": "",
    //            "minValue": "0",
    //            "maxValue": "100",
    //            "targetValue": "50",
    //            "softLimitCheckOnMinOrMaxValue": true,
    //            "overrideMinOrMax": true,
    //            "autoLogNconMinOrMaxOverride": true,
    //            "certification": "Certification A",
    //            "ncCode": "NC Code A",
    //            "mask": "",
    //            "unitOfMeasure": "mm",
    //            "requiredDataEntries": 1,
    //            "optionalDataEntries": 11
    //        },
    //        {
    //            "sequence": "2",
    //            "parameterName": "Parameter 2",
    //            "description": "Description of Parameter 2",
    //            "type": "text",
    //            "prompt": "Prompt for Parameter 2",
    //            "status": "Active",
    //            "allowMissingValues": false,
    //            "displayDataValues": true,
    //            "falseValue": "",
    //            "trueValue": "",
    //            "dataField": "Data Field B",
    //            "formula": "",
    //            "minValue": "",
    //            "maxValue": "",
    //            "targetValue": "",
    //            "softLimitCheckOnMinOrMaxValue": false,
    //            "overrideMinOrMax": false,
    //            "autoLogNconMinOrMaxOverride": false,
    //            "certification": "Certification B",
    //            "ncCode": "NC Code B",
    //            "mask": "mask",
    //            "unitOfMeasure": "mm",
    //            "requiredDataEntries": 1,
    //            "optionalDataEntries": 11
    //        }
    //    ],
    //    "attachmentList": [
    //        {
    //            "sequence": "1",
    //            "itemGroup": "Item Group A",
    //            "item": "Item A",
    //            "itemVersion": "1.0",
    //            "routing": "Routing A",
    //            "routingVersion": "1.0",
    //            "operation": "Operation A",
    //            "workCenter": "workCenter1",
    //            "resource": "Resource A",
    //            "shopOrder": "Shop Order A",
    //            "pcu": "PCU A"
    //        },
    //        {
    //            "sequence": "2",
    //            "itemGroup": "Item Group B",
    //            "item": "Item B",
    //            "itemVersion": "1.0",
    //            "routing": "Routing B",
    //            "routingVersion": "1.0",
    //            "operation": "Operation A",
    //            "workCenter": "workCenter1",
    //            "resource": "Resource A",
    //            "shopOrder": "Shop Order A",
    //            "pcu": "PCU A"
    //        }
    //    ],
    //    "customDataList": [
    //        {
    //            "customData": "data",
    //            "value": "value"
    //        }
    //    ],
    //    "active": 1,
    //    "createdDateTime": "2023-04-06T14:41:13.365",
    //    "modifiedDateTime": null
    //}
    @PostMapping("update")
    public ResponseEntity<DataCollectionMessageModel> updateDataCollection(@RequestBody DataCollectionRequest dataCollectionRequest) throws Exception {
        DataCollectionMessageModel updateDataCollection;


        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(dataCollectionRequest.getSite()).hookPoint("PRE").activity("datacollection-service").hookableMethod("update").request(objectMapper.writeValueAsString(dataCollectionRequest)).build();
        String preExtensionResponse = dataCollectionService.callExtension(preExtension);
        DataCollectionRequest preExtensionDataCollection = objectMapper.readValue(preExtensionResponse, DataCollectionRequest.class);

        try {
            updateDataCollection = dataCollectionService.updateDataCollection(preExtensionDataCollection);
            Extension postExtension = Extension.builder().site(dataCollectionRequest.getSite()).hookPoint("POST").activity("datacollection-service").hookableMethod("create").request(objectMapper.writeValueAsString(updateDataCollection.getResponse())).build();
            String postExtensionResponse = dataCollectionService.callExtension(postExtension);
            DataCollection postExtensionDataCollection = objectMapper.readValue(postExtensionResponse, DataCollection.class);

            AuditLogRequest activityLog = dataCollectionService.updateAuditLog(dataCollectionRequest);
            eventPublisher.publishEvent(new ProducerEvent(activityLog));

            return ResponseEntity.ok(DataCollectionMessageModel.builder().message_details(updateDataCollection.getMessage_details()).response(postExtensionDataCollection).build());

        } catch (DataCollectionException dataCollectionException) {
            throw dataCollectionException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //{
    //    "site": "rits",
    //    "dataCollection": "DataCollection1"
    //}

    //{
    //    "site": "rits",
    //    "dataCollection": "DataCollection1",
    //    "version": "B"
    //}
    @PostMapping("retrieve")
    public ResponseEntity<DataCollection> retrieveDataCollection(@RequestBody DataCollectionRequest dataCollectionRequest) throws Exception {
        DataCollection retrieveDataCollection;
        if (dataCollectionRequest.getSite() != null && !dataCollectionRequest.getSite().isEmpty()) {

            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(dataCollectionRequest.getSite()).hookPoint("PRE").activity("datacollection-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(dataCollectionRequest)).build();
            String preExtensionResponse = dataCollectionService.callExtension(preExtension);
            DataCollectionRequest preExtensionDataCollection = objectMapper.readValue(preExtensionResponse, DataCollectionRequest.class);

            try {
                retrieveDataCollection = dataCollectionService.retrieveDataCollection(preExtensionDataCollection.getDataCollection(), preExtensionDataCollection.getVersion(), preExtensionDataCollection.getSite());
                Extension postExtension = Extension.builder().site(dataCollectionRequest.getSite()).hookPoint("POST").activity("datacollection-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveDataCollection)).build();
                String postExtensionResponse = dataCollectionService.callExtension(postExtension);
                DataCollection postExtensionDataCollection = objectMapper.readValue(postExtensionResponse, DataCollection.class);
                return ResponseEntity.ok(postExtensionDataCollection);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }
    //{
    //    "site":"rits",
    //    "dataCollection":"data"
    //}

    //{
    //    "site":"rits"
    //}
    @PostMapping("retrieveAll")
    public ResponseEntity<DataCollectionResponseList> getDataCollectionList(@RequestBody DataCollectionRequest dataCollectionRequest) {
        DataCollectionResponseList getDataCollectionList;
        if (dataCollectionRequest.getSite() != null && !dataCollectionRequest.getSite().isEmpty()) {
            try {
                getDataCollectionList = dataCollectionService.getDataCollectionList(dataCollectionRequest.getDataCollection(), dataCollectionRequest.getSite());
                return ResponseEntity.ok(getDataCollectionList);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"rits"
    //}
    @PostMapping("retrieveTop50")
    public ResponseEntity<DataCollectionResponseList> getDataCollectionListByCreationDate(@RequestBody DataCollectionRequest dataCollectionRequest) {
        DataCollectionResponseList retrieveTop50DataCollections;
        if (dataCollectionRequest.getSite() != null && !dataCollectionRequest.getSite().isEmpty()) {
            try {
                retrieveTop50DataCollections = dataCollectionService.getDataCollectionListByCreationDate(dataCollectionRequest.getSite());
                return ResponseEntity.ok(retrieveTop50DataCollections);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"rits",
    //    "dataCollection":"DataCollection1",
    //    "version":"B"
    //}
    @PostMapping("delete")
    public ResponseEntity<DataCollectionMessageModel> deleteDataCollection(@RequestBody DataCollectionRequest dataCollectionRequest) throws Exception {
        DataCollectionMessageModel deleteResponse;
        if (dataCollectionRequest.getSite() != null && !dataCollectionRequest.getSite().isEmpty()) {

            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(dataCollectionRequest.getSite()).hookPoint("PRE").activity("datacollection-service").hookableMethod("delete").request(objectMapper.writeValueAsString(dataCollectionRequest)).build();
            String preExtensionResponse = dataCollectionService.callExtension(preExtension);
            DataCollectionRequest preExtensionDataCollection = objectMapper.readValue(preExtensionResponse, DataCollectionRequest.class);

            try {
                deleteResponse = dataCollectionService.deleteDataCollection(dataCollectionRequest.getDataCollection(), dataCollectionRequest.getVersion(), dataCollectionRequest.getSite(), dataCollectionRequest.getUserId());
                Extension postExtension = Extension.builder().site(dataCollectionRequest.getSite()).hookPoint("POST").activity("datacollection-service").hookableMethod("delete").request(objectMapper.writeValueAsString(deleteResponse.getResponse())).build();
                String postExtensionResponse = dataCollectionService.callExtension(postExtension);
                DataCollection postExtensionDataCollection = objectMapper.readValue(postExtensionResponse, DataCollection.class);
                // return ResponseEntity.ok(postExtensionDataCollection);
                AuditLogRequest activityLog = dataCollectionService.deleteAuditLog(dataCollectionRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(deleteResponse);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"rits",
    //    "item":"Item2"
    //}
    @PostMapping("retrieveByItem")
    public ResponseEntity<List<String>> getDcGroupNameListByItem(@RequestBody RetrieveRequest retrieveRequest) {
        List<String> retrieveByItem;
        if (retrieveRequest.getSite() != null && !retrieveRequest.getSite().isEmpty()) {
            try {
                retrieveByItem = dataCollectionService.getDcGroupNameListByItem(retrieveRequest.getItem(), retrieveRequest.getSite());
                return ResponseEntity.ok(retrieveByItem);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"rits",
    //    "item":"Item2",
    //    "pcu":"PCU2"
    //}
    @PostMapping("retrieveByPcuAndItem")
    public ResponseEntity<List<String>> getDcGroupNameListByPcu(@RequestBody RetrieveRequest retrieveRequest) {
        List<String> retrieveByPcuAndItem;
        if (retrieveRequest.getSite() != null && !retrieveRequest.getSite().isEmpty()) {
            try {
                retrieveByPcuAndItem = dataCollectionService.getDcGroupNameListByPCU(retrieveRequest.getPcu(), retrieveRequest.getItem(), retrieveRequest.getSite());
                return ResponseEntity.ok(retrieveByPcuAndItem);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"rits",
    //    "item":"Item2",
    //    "operation":"Operation2"
    //}
    @PostMapping("retrieveByItemAndOperation")
    public ResponseEntity<List<String>> getDcGroupNameListByItemOperation(@RequestBody RetrieveRequest retrieveRequest) {
        List<String> retrieveByItemAndOperation;
        if (retrieveRequest.getSite() != null && !retrieveRequest.getSite().isEmpty()) {
            try {
                retrieveByItemAndOperation = dataCollectionService.getDcGroupNameListByItemOperation(retrieveRequest.getItem(), retrieveRequest.getOperation(), retrieveRequest.getSite());
                return ResponseEntity.ok(retrieveByItemAndOperation);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"rits",
    //    "pcu":"PCU2",
    //    "operation":"Operation2"
    //}
    @PostMapping("retrieveByPcuAndOperation")
    public ResponseEntity<List<String>> getDcGroupNameListByPcuOperation(@RequestBody RetrieveRequest retrieveRequest) {
        List<String> retrieveByPcuAndOperation;
        if (retrieveRequest.getSite() != null && !retrieveRequest.getSite().isEmpty()) {
            try {
                retrieveByPcuAndOperation = dataCollectionService.getDcGroupNameListByPcuOperation(retrieveRequest.getPcu(), retrieveRequest.getOperation(), retrieveRequest.getSite());
                return ResponseEntity.ok(retrieveByPcuAndOperation);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"rits",
    //    "item":"Item2",
    //    "operation":"Operation2",
    //    "routing":"Routing2"
    //}
    @PostMapping("retrieveByItemAndOperationAndRouting")
    public ResponseEntity<List<String>> getDcGroupNameListByItemOperationRouting(@RequestBody RetrieveRequest retrieveRequest) {
        List<String> retrieveByItemAndOperationAndRouting;
        if (retrieveRequest.getSite() != null && !retrieveRequest.getSite().isEmpty()) {
            try {
                retrieveByItemAndOperationAndRouting = dataCollectionService.getDcGroupNameListByItemOperationRouting(retrieveRequest.getItem(), retrieveRequest.getOperation(), retrieveRequest.getRouting(), retrieveRequest.getSite());
                return ResponseEntity.ok(retrieveByItemAndOperationAndRouting);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"rits",
    //    "item":"Item2",
    //    "pcu":"PCU2",
    //    "operation":"Operation2",
    //    "routing":"Routing2"
    //}
    @PostMapping("retrieveByOperationAndRoutingAndPcu")
    public ResponseEntity<List<String>> getDcGroupNameListByOperationRoutingPcu(@RequestBody RetrieveRequest retrieveRequest) {
        List<String> retrieveByOperationAndRoutingAndPcu;
        if (retrieveRequest.getSite() != null && !retrieveRequest.getSite().isEmpty()) {
            try {
                retrieveByOperationAndRoutingAndPcu = dataCollectionService.getDcGroupNameListByOperationRoutingPcu(retrieveRequest.getOperation(), retrieveRequest.getRouting(), retrieveRequest.getPcu(), retrieveRequest.getSite(), retrieveRequest.getItem());
                return ResponseEntity.ok(retrieveByOperationAndRoutingAndPcu);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"rits",
    //    "resource":"Resource2"
    //}
    @PostMapping("retrieveByResource")
    public ResponseEntity<List<String>> getDcGroupNameListByResource(@RequestBody RetrieveRequest retrieveRequest) {
        List<String> retrieveByResource;
        if (retrieveRequest.getSite() != null && !retrieveRequest.getSite().isEmpty()) {
            try {
                retrieveByResource = dataCollectionService.getDcGroupNameListByResource(retrieveRequest.getResource(), retrieveRequest.getSite());
                return ResponseEntity.ok(retrieveByResource);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }
    //{
    //    "site":"rits",
    //    "pcu":"PCU2",
    //    "resource":"Resource2"
    //}

    @PostMapping("retrieveByResourceAndPcu")
    public ResponseEntity<List<String>> getDcGroupNameListByResourcePcu(@RequestBody RetrieveRequest retrieveRequest) {
        List<String> retrieveByResourceAndPcu;
        if (retrieveRequest.getSite() != null && !retrieveRequest.getSite().isEmpty()) {
            try {
                retrieveByResourceAndPcu = dataCollectionService.getDcGroupNameListByResourcePcu(retrieveRequest.getResource(), retrieveRequest.getPcu(), retrieveRequest.getSite());
                return ResponseEntity.ok(retrieveByResourceAndPcu);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"rits",
    //    "resource":"Resource2",
    //    "workCenter":"workCenter2"
    //}
    @PostMapping("retrieveByWorkCenter")
    public ResponseEntity<List<String>> getDcGroupNameListByWorkCenter(@RequestBody RetrieveRequest retrieveRequest) {
        List<String> retrieveByWorkCenter;
        if (retrieveRequest.getSite() != null && !retrieveRequest.getSite().isEmpty()) {
            try {
                retrieveByWorkCenter = dataCollectionService.getDcGroupNameListByWorkCenter(retrieveRequest.getWorkCenter(), retrieveRequest.getSite(), retrieveRequest.getResource());
                return ResponseEntity.ok(retrieveByWorkCenter);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"rits",
    //    "item":"Item1",
    //    "shopOrder":"Shop Order2"
    //}
    @PostMapping("retrieveByShopOrder")
    public ResponseEntity<List<String>> getDcGroupNameListByShopOrder(@RequestBody RetrieveRequest retrieveRequest) {
        List<String> retrieveByShopOrder;
        if (retrieveRequest.getSite() != null && !retrieveRequest.getSite().isEmpty()) {
            try {
                retrieveByShopOrder = dataCollectionService.getDcGroupNameListByShopOrder(retrieveRequest.getShopOrder(), retrieveRequest.getSite(), retrieveRequest.getItem());
                return ResponseEntity.ok(retrieveByShopOrder);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }

    //{
    //    "site":"RITS",
    //    "operation":"Operation2",
    //    "shopOrder":"Shop Order2"
    //}
    @PostMapping("retrieveByShopOrderAndOperation")
    public ResponseEntity<List<String>> getDcGroupNameListByShopOrderOperation(@RequestBody RetrieveRequest retrieveRequest) {
        List<String> retrieveByShopOrderAndOperation;
        if (retrieveRequest.getSite() != null && !retrieveRequest.getSite().isEmpty()) {
            try {
                retrieveByShopOrderAndOperation = dataCollectionService.getDcGroupNameListByShopOrderOperation(retrieveRequest.getShopOrder(), retrieveRequest.getOperation(), retrieveRequest.getSite());
                return ResponseEntity.ok(retrieveByShopOrderAndOperation);
            } catch (DataCollectionException dataCollectionException) {
                throw dataCollectionException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new DataCollectionException(1);
        }
    }
//    @PostMapping("retrieveByPcuAndOperationAndResource")
//    public DataCollectionList findByOperationPcuAndResource(@RequestBody RetrieveRequest retrieveRequest)
//    {
//        try {
//            return dataCollectionService.findByOperationPcuAndResource(retrieveRequest.getSite(),retrieveRequest.getOperation(),retrieveRequest.getResource(),retrieveRequest.getPcu());
//        } catch (DataCollectionException dataCollectionException) {
//            throw dataCollectionException;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @PostMapping("retrieveByPcuAndOperationAndResource")
    public DataCollectionList findByOperationPcuAndResource(@RequestBody RetrieveRequest retrieveRequest) {
        try {
//            return dataCollectionService.findByOperationPcuAndResource(retrieveRequest.getSite(),retrieveRequest.getOperation(),retrieveRequest.getResource(),retrieveRequest.getPcu());
            return dataCollectionService.retrieveByAttachment(retrieveRequest.getAttachmentList(), retrieveRequest.getPcu(), retrieveRequest.getOperation(), retrieveRequest.getResource(), retrieveRequest.getSite());
        } catch (DataCollectionException dataCollectionException) {
            throw dataCollectionException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveDCForBatchRecipe")
    public DataCollectionList retrieveDCForBatchRecipe(@RequestBody RetrieveRequest retrieveRequest) {
        try {
            return dataCollectionService.retrieveDCForBatchRecipe(retrieveRequest);
        } catch (DataCollectionException dataCollectionException) {
            throw dataCollectionException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    REQUEST BODY

//    [
//    {
//        "routing": "r1",
//            "routingVersion": "A",
//            "operation": "",
//            "workCenter": "wc1",
//            "resource": "",
//            "shopOrder": ""
//    },
//    {
//        "routing": "",
//            "routingVersion": "",
//            "operation": "CONDENSER_ASSY",
//            "workCenter": "",
//            "resource": "",
//            "shopOrder": ""
//    }
//]

    @PostMapping("/retrieveByAttachment")
    public List<DataCollection> getDataCollections(@RequestBody List<Attachment> attachmentList) {
        List<AttachmentPoint> attachmentPoints = new ArrayList<>();
        for (Attachment attachments : attachmentList) {
            AttachmentPoint attachmentPoint = AttachmentPoint.builder().attachmentList(DataCollectionServiceImpl.createFields(attachments)).build();
            attachmentPoints.add(attachmentPoint);
        }
        List<String> selectedFields = attachmentPoints.stream()
                .flatMap(attachmentPoint -> attachmentPoint.getAttachmentList().entrySet().stream()
                        .filter(entry -> !entry.getValue().isEmpty())
                        .map(entry -> entry.getKey() + "_" + entry.getValue()))
                .collect(Collectors.toList());

        List<String> combinations = dataCollectionService.generateCombinations(selectedFields);
        combinations.remove(0);

        Query query = new Query();
        query.addCriteria(Criteria.where("tags").in(combinations));
        List<DataCollection> dataCollectionList = mongoTemplate.find(query, DataCollection.class);
        return dataCollectionList;
    }

    @PostMapping("retrieveAllParameterNames")
    public List<Parameter> getAllParametersName(@RequestBody RetrieveRequest retrieveRequest) {
        try {
            return dataCollectionService.retrieveAllParameterNames(retrieveRequest.getSite());
        } catch (DataCollectionException dataCollectionException) {
            throw dataCollectionException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

