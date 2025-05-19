package com.rits.customdataservice.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.AuditLogService;
import com.rits.auditlogservice.service.ProducerEvent;
import com.rits.customdataservice.Exception.CustomDataException;
import com.rits.customdataservice.dto.CategoryList;
import com.rits.customdataservice.dto.Extension;

import com.rits.customdataservice.model.CustomData;
import com.rits.customdataservice.model.MessageModel;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.rits.customdataservice.dto.CustomDataRequest;
import com.rits.customdataservice.model.CustomDataList;
import com.rits.customdataservice.service.CustomDataServiceImpl;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/customdata-service")
public class CustomDataController {

    private final CustomDataServiceImpl customDataServiceImpl;
    private final ObjectMapper objectMapper;

    private final AuditLogService auditlogservice;
    private final ApplicationEventPublisher eventPublisher;


//    "required": ["site","category"]

//    {
//        "site":"RITS",
//            "category":"c3",
//            "customDataList":[
//        {
//            "sequence":"df",
//             "customData":"arg",
//             "fieldLabel":"aegr",
//             "required":true
//        }]
//    }

//    =====================================Code to create new record===================================
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?>  createCustomData(@RequestBody CustomDataRequest customDataRequest) throws JsonProcessingException {
        MessageModel createCustomData;
//        ResponseEntity<?> validationResponse = validationController.validateCustomDataSchema( payload);
//        if (validationResponse.getStatusCode() != HttpStatus.OK) {
//            return ResponseEntity.badRequest().body(validationResponse.getBody());
//        }
//        CustomDataRequest customDataRequest = new ObjectMapper().convertValue(payload, CustomDataRequest.class);


            try {
                createCustomData = customDataServiceImpl.createCustomData(customDataRequest);

                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(customDataRequest.getSite())
                        .action_code("CUSTOMDATA-CREATED")
                        .action_detail("Customdata Created "+ customDataRequest.getUserId())
                        .action_detail_handle("ActionDetailBO:"+customDataRequest.getSite()+","+"CUSTOMDATA-CREATED"+customDataRequest.getUserId()+":"+"com.rits.customdataservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(customDataRequest.getUserId())
                        .txnId("CUSTOMDATA-CREATED"+String.valueOf(LocalDateTime.now())+customDataRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Create")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                return ResponseEntity.ok(createCustomData);

//        objectMapper.registerModule(new JavaTimeModule());
//        Extension preExtension = Extension.builder().site(customDataRequest.getSite()).hookPoint("PRE").activity("customdata-service").hookableMethod("create").request(objectMapper.writeValueAsString(customDataRequest)).build();
//        String preExtensionResponse = customDataServiceImpl.callExtension(preExtension);
//        CustomDataRequest preExtensionCustomData = objectMapper.readValue(preExtensionResponse, CustomDataRequest.class);
//
//        try {
//            createCustomData = customDataServiceImpl.createCustomData(preExtensionCustomData);
//            Extension postExtension = Extension.builder().site(customDataRequest.getSite()).hookPoint("POST").activity("customdata-service").hookableMethod("create").request(objectMapper.writeValueAsString(createCustomData.getResponse())).build();
//            String postExtensionResponse = customDataServiceImpl.callExtension(postExtension);
//            CustomData postExtensionCustomData = objectMapper.readValue(postExtensionResponse, CustomData.class);
//            return ResponseEntity.ok( MessageModel.builder().message_details(createCustomData.getMessage_details()).response(postExtensionCustomData).build())


            } catch (CustomDataException e) {
               throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }
//    =================================================================================================

//    {
//        "site":"RITS",
//        "category":"c1"
//    }


    @PostMapping("/retrieveByCategory")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<CustomDataList>> retrieveCustomDataList(@RequestBody CustomDataRequest customDataRequest) {
        if(customDataRequest.getSite() != null && !customDataRequest.getSite().isEmpty())
        {
            try {
                List<CustomDataList> retrievedCustomDataList = customDataServiceImpl.retrieveCustomDataListByCategory(customDataRequest);
                return ResponseEntity.ok(retrievedCustomDataList);
            } catch (CustomDataException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CustomDataException(2202,customDataRequest.getSite());
    }

//    {
//        "site":"RITS",
//        "category":"catagory2"
//    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> updateCustomData(@RequestBody CustomDataRequest customDataRequest) throws JsonProcessingException {
        MessageModel updateCustomData;

//            try {
//                CustomData updatedCustomData = customDataServiceImpl.updateCustomData(customDataRequest);
//                return ResponseEntity.ok(updatedCustomData);
        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(customDataRequest.getSite()).hookPoint("PRE").activity("customdata-service").hookableMethod("update").request(objectMapper.writeValueAsString(customDataRequest)).build();
        String preExtensionResponse = customDataServiceImpl.callExtension(preExtension);
        CustomDataRequest preExtensionCustomData = objectMapper.readValue(preExtensionResponse, CustomDataRequest.class);

        try {
            updateCustomData = customDataServiceImpl.updateCustomData(preExtensionCustomData);
            Extension postExtension = Extension.builder().site(customDataRequest.getSite()).hookPoint("POST").activity("customdata-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateCustomData.getResponse())).build();
            String postExtensionResponse = customDataServiceImpl.callExtension(postExtension);
            CustomData postExtensionCustomData = objectMapper.readValue(postExtensionResponse, CustomData.class);

            AuditLogRequest auditlog = AuditLogRequest.builder()
                    .site(customDataRequest.getSite())
                    .action_code("CUSTOMDATA-UPDATED "+ customDataRequest.getUserId())
                    .action_detail("Customdata Updated")
                    .action_detail_handle("ActionDetailBO:"+customDataRequest.getSite()+","+"CUSTOMDATA-UPDATED"+customDataRequest.getUserId()+":"+"com.rits.customdataservice.controller")
                    .activity("From Service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(customDataRequest.getUserId())
                    .txnId("CUSTOMDATA-UPDATED"+String.valueOf(LocalDateTime.now())+customDataRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Update")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(auditlog));

            return ResponseEntity.ok( MessageModel.builder().message_details(updateCustomData.getMessage_details()).response(postExtensionCustomData).build());

            } catch (CustomDataException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

//    {
//        "site":"RITS",
//        "category":"catagory2"
//    }

//    =================================Code to delete a particular record=====================================
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> deleteCustomData(@RequestBody CustomDataRequest customDataRequest) throws JsonProcessingException {
        MessageModel deleteResponse;
//            try {
//                Response deletedCustomData = customDataServiceImpl.deleteCustomData(customDataRequest);
//                return ResponseEntity.ok(deletedCustomData);
        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(customDataRequest.getSite()).hookPoint("PRE").activity("customdata-service").hookableMethod("delete").request(objectMapper.writeValueAsString(customDataRequest)).build();
        String preExtensionResponse = customDataServiceImpl.callExtension(preExtension);
        CustomDataRequest preExtensionCustomData = objectMapper.readValue(preExtensionResponse, CustomDataRequest.class);

        try {
            deleteResponse = customDataServiceImpl.deleteCustomData(preExtensionCustomData);
            Extension postExtension = Extension.builder().site(customDataRequest.getSite()).hookPoint("POST").activity("customdata-service").hookableMethod("delete").request(objectMapper.writeValueAsString(deleteResponse.getResponse())).build();
            String postExtensionResponse = customDataServiceImpl.callExtension(postExtension);
            CustomData postExtensionCustomData = objectMapper.readValue(postExtensionResponse, CustomData.class);

            AuditLogRequest auditlog = AuditLogRequest.builder()
                    .site(customDataRequest.getSite())
                    .action_code("CUSTOMDATA DELETED")
                    .action_detail("Customdata Deleted ")
                    .action_detail_handle("ActionDetailBO:"+customDataRequest.getSite()+","+"CUSTOMDATA-DELETED"+","+customDataRequest.getUserId()+":"+"com.rits.customdataservice.controller")
                    .activity("From Service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(customDataRequest.getUserId())
                    .txnId("CUSTOMDATA-DELETED"+String.valueOf(LocalDateTime.now())+customDataRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Delete")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(auditlog));

//            return ResponseEntity.ok( MessageModel.builder().message_details(updateCustomData.getMessage_details()).response(postExtensionCustomData).build());
            return ResponseEntity.ok(deleteResponse);
            } catch (CustomDataException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CategoryList> retrieveTop50(@RequestBody CustomDataRequest customDataRequest)
    {
        try {
            CategoryList retrievedCategoryList = customDataServiceImpl.retrieveTop50(customDataRequest);
            return ResponseEntity.ok(retrievedCategoryList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
