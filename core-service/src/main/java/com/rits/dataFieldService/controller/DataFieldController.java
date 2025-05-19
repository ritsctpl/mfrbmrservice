package com.rits.dataFieldService.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.AuditLogService;
import com.rits.auditlogservice.service.ProducerEvent;
import com.rits.dataFieldService.dto.DataFieldRequest;
import com.rits.dataFieldService.dto.DataFieldResponseList;
import com.rits.dataFieldService.dto.Extension;
import com.rits.dataFieldService.exception.DataFieldException;
import com.rits.dataFieldService.model.DataField;
import com.rits.dataFieldService.model.MessageModel;
import com.rits.dataFieldService.service.DataFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("app/v1/datafield-service")
@RequiredArgsConstructor
public class DataFieldController {

    private final DataFieldService dataFieldService;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditlogservice;
    private final ApplicationEventPublisher eventPublisher;

    //{
    //    "site":"rits"
    //}
    @PostMapping("retrieveTop50")
    public ResponseEntity<DataFieldResponseList> getDataFieldListByCreationDate(@RequestBody DataFieldRequest dataFieldRequest) {
        DataFieldResponseList retrieveTop50DataField;
        if (dataFieldRequest.getSite() != null && !dataFieldRequest.getSite().isEmpty()) {
            try {
                retrieveTop50DataField = dataFieldService.getDataFieldListByCreationDate(dataFieldRequest.getSite());
                return ResponseEntity.ok(retrieveTop50DataField);
            } catch (DataFieldException dataFieldException) {
                throw dataFieldException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataFieldException(1);

    }

    //{
    //    "site": "rits",
    //    "dataField": "data"
    //}
    @PostMapping("retrieveAll")
    public ResponseEntity<DataFieldResponseList> getDataFieldList(@RequestBody DataFieldRequest dataFieldRequest) {
        DataFieldResponseList retrieveAllDataField;
        if (dataFieldRequest.getSite() != null && !dataFieldRequest.getSite().isEmpty()) {
            try {
                retrieveAllDataField = dataFieldService.getDataFieldList(dataFieldRequest.getDataField(), dataFieldRequest.getSite());
                return ResponseEntity.ok(retrieveAllDataField);
            } catch (DataFieldException dataFieldException) {
                throw dataFieldException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataFieldException(1);

    }

    //{
    //    "site": "rits",
    //    "dataField": "dataField1"
    //}
    @PostMapping("retrieve")
    public ResponseEntity<DataField> retrieveDataField(@RequestBody DataFieldRequest dataFieldRequest) throws Exception {
        DataField retrieveDataField;
        if (dataFieldRequest.getSite() != null && !dataFieldRequest.getSite().isEmpty()) {

            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(dataFieldRequest.getSite()).hookPoint("PRE").activity("datafield-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(dataFieldRequest)).build();
            String preExtensionResponse = dataFieldService.callExtension(preExtension);
            DataFieldRequest preExtensionDataField = objectMapper.readValue(preExtensionResponse, DataFieldRequest.class);

            try {
                retrieveDataField = dataFieldService.retrieveDataField(preExtensionDataField.getDataField(), preExtensionDataField.getSite());
                Extension postExtension = Extension.builder().site(dataFieldRequest.getSite()).hookPoint("POST").activity("datafield-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveDataField)).build();
                String postExtensionResponse = dataFieldService.callExtension(postExtension);
                DataField postExtensionDataField = objectMapper.readValue(postExtensionResponse, DataField.class);
                return ResponseEntity.ok(postExtensionDataField);


            } catch (DataFieldException dataFieldException) {
                throw dataFieldException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataFieldException(1);

    }

    //{
    //    "site": "rits",
    //    "dataField": "dataField2",
    //    "type": "Text",
    //    "fieldLabel": "fieldLabel"
    //
    //}


    //    {
//        "site": "rits",
//        "dataField": "dataField1",
//        "type": "Text",
//        "qmSelectedSet": true,
//        "description": "a",
//        "fieldLabel": "fieldLabel",
//        "maskGroup": "maskGroup",
//        "preSaveActivity": "preSaveActivity",
//        "browseIcon":"browse",
//        "listDetails": [
//            {
//                "defaultLabel": true,
//                "sequence": 1,
//                "fieldValue": "value1",
//                "labelValue": "Value 1"
//            },
//            {
//                "defaultLabel": false,
//                "sequence": 2,
//                "fieldValue": "value2",
//                "labelValue": "Value 2"
//            }
//        ]
//    }
    @PostMapping("create")
    public ResponseEntity<?> createDataField(@RequestBody DataFieldRequest dataFieldRequest) throws Exception {
        MessageModel createDataField;
//        MessageModel validationResponse = dataFieldService.validation(payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        DataFieldRequest dataFieldRequest = new ObjectMapper().convertValue(payload, DataFieldRequest.class);

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(dataFieldRequest.getSite()).hookPoint("PRE").activity("datafield-service").hookableMethod("create").request(objectMapper.writeValueAsString(dataFieldRequest)).build();
        String preExtensionResponse = dataFieldService.callExtension(preExtension);
        DataFieldRequest preExtensionDataField = objectMapper.readValue(preExtensionResponse, DataFieldRequest.class);

        try {
            createDataField = dataFieldService.createDataField(preExtensionDataField);

            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(dataFieldRequest.getSite())
                    .change_type("Create")
                    .action_code("DATAFIELD-CREATED")
                    .action_detail("Data Field Created "+dataFieldRequest.getDataField())
                    .action_detail_handle("ActionDetailBO:"+dataFieldRequest.getSite()+","+"DATAFIELD-CREATED"+","+dataFieldRequest.getUserId()+":"+"com.rits.dataFieldService.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(dataFieldRequest.getUserId())
                    .txnId("DATAFIELD-CREATE"+String.valueOf(LocalDateTime.now())+dataFieldRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Create")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(activityLog));

            Extension postExtension = Extension.builder().site(dataFieldRequest.getSite()).hookPoint("POST").activity("datafield-service").hookableMethod("create").request(objectMapper.writeValueAsString(createDataField.getResponse())).build();
            String postExtensionResponse = dataFieldService.callExtension(postExtension);
            DataField postExtensionDataField = objectMapper.readValue(postExtensionResponse, DataField.class);
            return ResponseEntity.ok( MessageModel.builder().message_details(createDataField.getMessage_details()).response(postExtensionDataField).build());

        } catch (DataFieldException dataFieldException) {
            throw dataFieldException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //{
    //    "site": "rits",
    //    "dataField": "dataField"
    //}
    @PostMapping("delete")
    public ResponseEntity<MessageModel> deleteDataField(@RequestBody DataFieldRequest dataFieldRequest) throws Exception {
        MessageModel deleteResponse;
        if (dataFieldRequest.getSite() != null && !dataFieldRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(dataFieldRequest.getSite()).hookPoint("PRE").activity("datafield-service").hookableMethod("delete").request(objectMapper.writeValueAsString(dataFieldRequest)).build();
            String preExtensionResponse = dataFieldService.callExtension(preExtension);
            DataFieldRequest preExtensionDataField = objectMapper.readValue(preExtensionResponse, DataFieldRequest.class);

            try {
                deleteResponse = dataFieldService.deleteDataField(preExtensionDataField.getDataField(), preExtensionDataField.getSite(),preExtensionDataField.getUserId());

                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(dataFieldRequest.getSite())
                        .change_type("Delete")
                        .action_code("DATAFIELD-DELETED")
                        .action_detail("Data Field Deleted "+dataFieldRequest.getDataField())
                        .action_detail_handle("ActionDetailBO:"+dataFieldRequest.getSite()+","+"DATAFIELD-DELETED"+","+dataFieldRequest.getUserId()+":"+"com.rits.dataFieldService.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(dataFieldRequest.getUserId())
                        .txnId("DATAFIELD-DELETE"+String.valueOf(LocalDateTime.now())+dataFieldRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Delete")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                Extension postExtension = Extension.builder().site(dataFieldRequest.getSite()).hookPoint("POST").activity("datafield-service").hookableMethod("delete").request(objectMapper.writeValueAsString(deleteResponse.getResponse())).build();
                String postExtensionResponse = dataFieldService.callExtension(postExtension);
                DataField postExtensionDataField = objectMapper.readValue(postExtensionResponse, DataField.class);
                // return ResponseEntity.ok(postExtensionDataField);
                return ResponseEntity.ok(deleteResponse);

            } catch (DataFieldException dataFieldException) {
                throw dataFieldException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataFieldException(1);

    }


    //{
    //    "site": "rits",
    //    "dataField": "dataField2",
    //    "type": "Text",
    //    "fieldLabel": "fieldLabel"
    //
    //}


    //{
    //    "site": "rits",
    //    "dataField": "dataField1",
    //    "type": "Text",
    //    "qmSelectedSet": true,
    //    "description": "a",
    //    "fieldLabel": "fieldLabel",
    //    "maskGroup": "maskGroup",
    //    "preSaveActivity": "preSaveActivity",
    //    "browseIcon":"browse",
    //    "listDetails": [
    //        {
    //            "defaultLabel": true,
    //            "sequence": 1,
    //            "fieldValue": "value1",
    //            "labelValue": "Value 1"
    //        },
    //        {
    //            "defaultLabel": false,
    //            "sequence": 2,
    //            "fieldValue": "value2",
    //            "labelValue": "Value 2"
    //        }
    //    ]
    //}
    @PostMapping("update")
    public ResponseEntity<MessageModel> updateDataField(@RequestBody DataFieldRequest dataFieldRequest) throws Exception {
        MessageModel updateDataField;


        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(dataFieldRequest.getSite()).hookPoint("PRE").activity("datafield-service").hookableMethod("update").request(objectMapper.writeValueAsString(dataFieldRequest)).build();
        String preExtensionResponse = dataFieldService.callExtension(preExtension);
        DataFieldRequest preExtensionDataField = objectMapper.readValue(preExtensionResponse, DataFieldRequest.class);

        try {
            updateDataField = dataFieldService.updateDataField(preExtensionDataField);

            AuditLogRequest auditlog = AuditLogRequest.builder()
                    .site(dataFieldRequest.getSite())
                    .change_type("Update")
                    .action_code("DATAFIELD-UPDATED")
                    .action_detail("Data Field Updated "+dataFieldRequest.getDataField())
                    .action_detail_handle("ActionDetailBO:"+dataFieldRequest.getSite()+","+"DATAFIELD-UPDATED"+","+dataFieldRequest.getUserId()+":"+"com.rits.dataFieldService.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(dataFieldRequest.getUserId())
                    .txnId("DATAFIELD-UPDATE"+String.valueOf(LocalDateTime.now())+dataFieldRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Update")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(auditlog));

            Extension postExtension = Extension.builder().site(dataFieldRequest.getSite()).hookPoint("POST").activity("datafield-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateDataField.getResponse())).build();
            String postExtensionResponse = dataFieldService.callExtension(postExtension);
            DataField postExtensionDataField = objectMapper.readValue(postExtensionResponse, DataField.class);
            return ResponseEntity.ok( MessageModel.builder().message_details(updateDataField.getMessage_details()).response(postExtensionDataField).build());


        } catch (DataFieldException dataFieldException) {
            throw dataFieldException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("isExist")
    public ResponseEntity<Boolean> isExist(@RequestBody DataFieldRequest dataFieldRequest) {
        boolean isExist;
        if (dataFieldRequest.getSite() != null && !dataFieldRequest.getSite().isEmpty()) {
            try {
                isExist = dataFieldService.isExist(dataFieldRequest.getSite(), dataFieldRequest.getDataField());
                return ResponseEntity.ok(isExist);
            } catch (DataFieldException dataFieldException) {
                throw dataFieldException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataFieldException(1);

    }
    @PostMapping("isTrackable")
    public ResponseEntity<Boolean> isTrackable(@RequestBody DataFieldRequest dataFieldRequest) {
        Boolean isTrackable;
        if (dataFieldRequest.getSite() != null && !dataFieldRequest.getSite().isEmpty()) {
            try {
                isTrackable = dataFieldService.isTrackable(dataFieldRequest.getSite(), dataFieldRequest.getDataField());
                return ResponseEntity.ok(isTrackable);
            } catch (DataFieldException dataFieldException) {
                throw dataFieldException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataFieldException(1);

    }
}
