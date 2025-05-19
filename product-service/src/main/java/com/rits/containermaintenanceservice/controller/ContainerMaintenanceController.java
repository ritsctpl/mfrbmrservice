package com.rits.containermaintenanceservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.containermaintenanceservice.Exception.ContainerMaintenanceException;
import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.containermaintenanceservice.dto.ContainerList;
import com.rits.containermaintenanceservice.dto.ContainerMaintenanceRequest;
import com.rits.containermaintenanceservice.dto.Extension;

import com.rits.containermaintenanceservice.model.ContainerMaintenance;
import com.rits.containermaintenanceservice.model.MessageModel;
import com.rits.containermaintenanceservice.service.ContainerMaintenanceServiceImpl;

import org.springframework.context.ApplicationEventPublisher;
import com.rits.kafkaservice.ProducerEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/containermaintenance-service")
public class ContainerMaintenanceController {

    private final ContainerMaintenanceServiceImpl containerMaintenanceServiceImpl;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

//     "required": ["site","container","status"]

//    {
//    "site": "RITS",
//    "container": "container1",
//    "description": "container",
//    "containerCategory": "",
//    "status": "true",
//    "containerDataType": "",
//    "handlingUnitManaged": true,
//    "generateHandlingUnitNumber": false,
//    "totalMinQuantity": 10,
//    "totalMaxQuantity": 20,
//    "packLevelList": [
//        {
//            "packLevel":" ",
//            "packLevelValue":" ",
//            "version":" ",
//            "shopOrder":" ",
//            "minimumQuantity":" ",
//            "maximumQuantity":" "
//        }
//    ],
//    "documentsList": [
//        {
//            "sequence":" ",
//            "document":" "
//        }
//    ],
//    "dimensionsList": [
//        {
//            "height":" ",
//            "width":" ",
//            "length":" ",
//            "maximumFileWeight":" ",
//            "containerWeight":" "
//        }
//    ],
//    "customDataList": [
//        {
//            "dataField":" ",
//            "value":" "
//        }
//    ],
//     "sfcDataType": "",
//    "sfcPackOrder": ""
//}


    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestBody ContainerMaintenanceRequest containerMaintenanceRequest) throws JsonProcessingException {
        MessageModel createContainerMaintenance;
//        MessageModel validationResponse = containerMaintenanceServiceImpl.validation( payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        ContainerMaintenanceRequest containerMaintenanceRequest = new ObjectMapper().convertValue(payload, ContainerMaintenanceRequest.class);

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(containerMaintenanceRequest.getSite()).hookPoint("PRE").activity("containermaintenance-service").hookableMethod("create").request(objectMapper.writeValueAsString(containerMaintenanceRequest)).build();
        String preExtensionResponse = containerMaintenanceServiceImpl.callExtension(preExtension);
        ContainerMaintenanceRequest preExtensionContainerMaintenance = objectMapper.readValue(preExtensionResponse, ContainerMaintenanceRequest.class);

        try {
            createContainerMaintenance = containerMaintenanceServiceImpl.create(preExtensionContainerMaintenance);
            Extension postExtension = Extension.builder().site(containerMaintenanceRequest.getSite()).hookPoint("POST").activity("containermaintenance-service").hookableMethod("create").request(objectMapper.writeValueAsString(createContainerMaintenance.getResponse())).build();
            String postExtensionResponse = containerMaintenanceServiceImpl.callExtension(postExtension);
            ContainerMaintenance postExtensionContainerMaintenance = objectMapper.readValue(postExtensionResponse, ContainerMaintenance.class);

            AuditLogRequest auditlog = AuditLogRequest.builder()
                    .site(containerMaintenanceRequest.getSite())
                    .action_code("CONTAINER-CREATED "+containerMaintenanceRequest.getDescription())
                    .action_detail("Container Created "+containerMaintenanceRequest.getDescription())
                    .action_detail_handle("ActionDetailBO:"+containerMaintenanceRequest.getSite()+","+"CONTAINER-CREATED"+","+containerMaintenanceRequest.getUserId()+":"+"com.rits.containermaintenanceservice.controller")
                    .activity("From Service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(containerMaintenanceRequest.getUserId())
                    .txnId("CONTAINER-CREATED"+String.valueOf(LocalDateTime.now())+containerMaintenanceRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Create")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(auditlog));

            return ResponseEntity.ok( MessageModel.builder().message_details(createContainerMaintenance.getMessage_details()).response(postExtensionContainerMaintenance).build());

            } catch (ContainerMaintenanceException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

    }

//    {
//        "site":"RITS",
//            "container":"container"
//    }

    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isExist(@RequestBody ContainerMaintenanceRequest containerMaintenanceRequest)
    {
        if(containerMaintenanceRequest.getSite()!=null && !containerMaintenanceRequest.getSite().isEmpty()) {
            try {
                Boolean isContainerExists = containerMaintenanceServiceImpl.isExist(containerMaintenanceRequest);
                return  ResponseEntity.ok(isContainerExists);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ContainerMaintenanceException(2602,containerMaintenanceRequest.getSite());
    }

//   {
//    "site": "RITS",
//    "container": "container1",
//    "description": "container",
//    "containerCategory": "",
//    "status": "true",
//    "containerDataType": "",
//    "handlingUnitManaged": true,
//    "generateHandlingUnitNumber": false,
//    "totalMinQuantity": 10,
//    "totalMaxQuantity": 20,
//    "packLevelList": [
//        {
//            "packLevel": " ",
//            "packLevelValue": " ",
//            "version": " ",
//            "shopOrder": " ",
//            "minimumQuantity": " ",
//            "maximumQuantity": " "
//        }
//    ],
//    "documentsList": [
//        {
//            "sequence": " ",
//            "document": " "
//        }
//    ],
//    "dimensionsList": [
//        {
//            "height": " ",
//            "width": " ",
//            "length": " ",
//            "maximumFileWeight": " ",
//            "containerWeight": " "
//        }
//    ],
//    "customDataList": [
//        {
//            "dataField": " ",
//            "value": " "
//        }
//    ],
//    "sfcDataType": "",
//    "sfcPackOrder": ""
//}


    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> update(@RequestBody ContainerMaintenanceRequest containerMaintenanceRequest) throws JsonProcessingException {
        MessageModel updateContainerMaintenance;

//            try {
//                ContainerMaintenance updatedContainerMaintenance = containerMaintenanceServiceImpl.update(containerMaintenanceRequest);
//                return ResponseEntity.ok(updatedContainerMaintenance);
        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(containerMaintenanceRequest.getSite()).hookPoint("PRE").activity("containermaintenance-service").hookableMethod("update").request(objectMapper.writeValueAsString(containerMaintenanceRequest)).build();
        String preExtensionResponse = containerMaintenanceServiceImpl.callExtension(preExtension);
        ContainerMaintenanceRequest preExtensionContainerMaintenance = objectMapper.readValue(preExtensionResponse, ContainerMaintenanceRequest.class);

        try {
            updateContainerMaintenance = containerMaintenanceServiceImpl.update(containerMaintenanceRequest);
            Extension postExtension = Extension.builder().site(containerMaintenanceRequest.getSite()).hookPoint("POST").activity("containermaintenance-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateContainerMaintenance.getResponse())).build();
            String postExtensionResponse = containerMaintenanceServiceImpl.callExtension(postExtension);
            ContainerMaintenance postExtensionContainerMaintenance = objectMapper.readValue(postExtensionResponse, ContainerMaintenance.class);

            AuditLogRequest auditlog = AuditLogRequest.builder()
                    .site(containerMaintenanceRequest.getSite())
                    .action_code("CONTAINER-UPDATED "+containerMaintenanceRequest.getDescription())
                    .action_detail("Container Updated")
                    .action_detail_handle("ActionDetailBO:"+containerMaintenanceRequest.getSite()+","+"CONTAINER-UPDATED"+","+containerMaintenanceRequest.getUserId()+":"+"com.rits.containermaintenanceservice.controller")
                    .activity("From Service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(containerMaintenanceRequest.getUserId())
                    .txnId("CONTAINER-UPDATED"+String.valueOf(LocalDateTime.now())+containerMaintenanceRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Update")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(auditlog));

            return ResponseEntity.ok( MessageModel.builder().message_details(updateContainerMaintenance.getMessage_details()).response(postExtensionContainerMaintenance).build());

            }
            catch (ContainerMaintenanceException e)
            {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

    }

//    {
//        "site":"RITS",
//            "container":"container"
//    }

    @PostMapping("/retrieveByContainer")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ContainerMaintenance> retrieveByContainerMaintenance(@RequestBody ContainerMaintenanceRequest containerMaintenanceRequest) throws JsonProcessingException {
        ContainerMaintenance retrieveContainerMaintenance;
//        try {
//            ContainerMaintenance existingContainer = containerMaintenanceServiceImpl.retrieveByContainerMaintenance(containerMaintenanceRequest);
//            return ResponseEntity.ok(existingContainer);
        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(containerMaintenanceRequest.getSite()).hookPoint("PRE").activity("containermaintenance-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(containerMaintenanceRequest)).build();
        String preExtensionResponse = containerMaintenanceServiceImpl.callExtension(preExtension);
        ContainerMaintenanceRequest preExtensionContainerMaintenance = objectMapper.readValue(preExtensionResponse, ContainerMaintenanceRequest.class);


        try {
            retrieveContainerMaintenance = containerMaintenanceServiceImpl.retrieveByContainerMaintenance(preExtensionContainerMaintenance);
            Extension postExtension = Extension.builder().site(containerMaintenanceRequest.getSite()).hookPoint("POST").activity("containermaintenance-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveContainerMaintenance)).build();
            String postExtensionResponse = containerMaintenanceServiceImpl.callExtension(postExtension);
            ContainerMaintenance postExtensionContainerMaintenance = objectMapper.readValue(postExtensionResponse, ContainerMaintenance.class);
            return ResponseEntity.ok(postExtensionContainerMaintenance);

        } catch (ContainerMaintenanceException e) {
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
    public ResponseEntity<ContainerList> retrieveTop50Container(@RequestBody ContainerMaintenanceRequest containerMaintenanceRequest)
    {
        if(containerMaintenanceRequest.getSite()!=null && !containerMaintenanceRequest.getSite().isEmpty()) {
            try {
                ContainerList retrievedContainerList = containerMaintenanceServiceImpl.retrieveTop50Container(containerMaintenanceRequest);
                return ResponseEntity.ok(retrievedContainerList);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ContainerMaintenanceException(2602,containerMaintenanceRequest.getSite());
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> delete(@RequestBody ContainerMaintenanceRequest containerMaintenanceRequest) throws JsonProcessingException {
        MessageModel deleteResponse;

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(containerMaintenanceRequest.getSite()).hookPoint("PRE").activity("usergroup-service").hookableMethod("delete").request(objectMapper.writeValueAsString(containerMaintenanceRequest)).build();
        String preExtensionResponse = containerMaintenanceServiceImpl.callExtension(preExtension);
        ContainerMaintenanceRequest preExtensionContainerMaintenance = objectMapper.readValue(preExtensionResponse, ContainerMaintenanceRequest.class);
        if (containerMaintenanceRequest.getSite() != null && !containerMaintenanceRequest.getSite().isEmpty()) {
            try {
                deleteResponse = containerMaintenanceServiceImpl.delete(preExtensionContainerMaintenance);
                Extension postExtension = Extension.builder().site(containerMaintenanceRequest.getSite()).hookPoint("POST").activity("usergroup-service").hookableMethod("delete").request(objectMapper.writeValueAsString(deleteResponse.getResponse())).build();
                String postExtensionResponse = containerMaintenanceServiceImpl.callExtension(postExtension);
                ContainerMaintenance postExtensionUserGroup = objectMapper.readValue(postExtensionResponse, ContainerMaintenance.class);

                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(containerMaintenanceRequest.getSite())
                        .action_code("CONTAINER-DELETED")
                        .action_detail("Container Deleted "+containerMaintenanceRequest.getDescription())
                        .action_detail_handle("ActionDetailBO:"+containerMaintenanceRequest.getSite()+","+"CONTAINER-DELETED"+","+containerMaintenanceRequest.getUserId()+":"+"com.rits.containermaintenanceservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(containerMaintenanceRequest.getUserId())
                        .txnId("CONTAINER-DELETED"+String.valueOf(LocalDateTime.now())+containerMaintenanceRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Delete")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                return ResponseEntity.ok(deleteResponse);

            } catch (ContainerMaintenanceException e) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ContainerMaintenanceException(2602, containerMaintenanceRequest.getSite());
    }

}
