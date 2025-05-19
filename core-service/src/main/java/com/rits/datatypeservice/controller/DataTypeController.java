package com.rits.datatypeservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.AuditLogService;
import com.rits.auditlogservice.service.ProducerEvent;
import com.rits.datatypeservice.dto.*;
import com.rits.datatypeservice.exception.DataTypeException;
import com.rits.datatypeservice.model.DataType;
import com.rits.datatypeservice.model.MessageModel;
import com.rits.datatypeservice.service.DataTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/datatype-service")
public class DataTypeController {
    private final DataTypeService dataTypeService;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditlogservice;
    private final ApplicationEventPublisher eventPublisher;


//    {
//        "site":"RITS",
//            "category":"c2",
//            "dataType":"datatyp21",
//            "description":"a",
//            "preSaveActivity":"",
//            "dataFieldList":[
//        {
//            "sequence":"2",
//                "dataField":"2",
//                "required":true
//        },
//        {
//            "sequence":"3",
//                "dataField":"",
//                "required":true
//        }
//       ]
//    }
        @PostMapping("/create")
        @ResponseStatus(HttpStatus.CREATED)
        public ResponseEntity<?> createDataType(@RequestBody DataTypeRequest dataTypeRequest) throws JsonProcessingException {
//            MessageModel validationResponse = dataTypeService.validation(payload);
//            if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//                return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//            }
//            DataTypeRequest dataTypeRequest = new ObjectMapper().convertValue(payload,DataTypeRequest.class);
                MessageModel createResponse;
                if(dataTypeRequest.getSite()!=null && !dataTypeRequest.getSite().isEmpty()){
                    objectMapper.registerModule(new JavaTimeModule());
                    Extension preExtension = Extension.builder().site(dataTypeRequest.getSite()).hookPoint("PRE").activity("datatype-service").hookableMethod("create").request(objectMapper.writeValueAsString(dataTypeRequest)).build();
                    String preExtensionResponse = dataTypeService.callExtension(preExtension);
                    DataTypeRequest preExtensionDataType = objectMapper.readValue(preExtensionResponse, DataTypeRequest.class);

                    try {
                        createResponse = dataTypeService.createDataType(preExtensionDataType);
                        AuditLogRequest activityLog = AuditLogRequest.builder()
                                .site(dataTypeRequest.getSite())
                                .change_type("Create")
                                .action_code("DATATYPE-CREATE")
                                .action_detail("DATATYPE CREATED "+dataTypeRequest.getDataType())
                                .action_detail_handle("ActionDetailBO:"+dataTypeRequest.getSite()+","+"DATATYPE-CREATE"+","+dataTypeRequest.getUserId()+":"+"com.rits.datatypeservice.service")
                                .date_time(String.valueOf(LocalDateTime.now()))
                                .userId(dataTypeRequest.getUserId())
                                .txnId("DATATYPE-CREATE"+String.valueOf(LocalDateTime.now())+dataTypeRequest.getUserId())
                                .created_date_time(String.valueOf(LocalDateTime.now()))
                                .category("Create")
                                .topic("audit-log")
                                .build();
                        eventPublisher.publishEvent(new ProducerEvent(activityLog));

                        Extension postExtension = Extension.builder().site(dataTypeRequest.getSite()).hookPoint("POST").activity("datatype-service").hookableMethod("create").request(objectMapper.writeValueAsString(createResponse.getResponse())).build();
                        String postExtensionResponse = dataTypeService.callExtension(postExtension);
                        DataType postExtensionDataType = objectMapper.readValue(postExtensionResponse, DataType.class);
                        return ResponseEntity.ok(MessageModel.builder().message_details(createResponse.getMessage_details()).response(postExtensionDataType).build());
                    }catch(DataTypeException e){
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                throw new DataTypeException(1002, dataTypeRequest.getSite());

            }


    //        "site":"RITS",
//            "category":"c2",
//            "dataType":"datatyp21",
//            "description":"a",
//            "preSaveActivity":"",
//            "dataFieldList":[
//        {
//            "sequence":"2",
//                "dataField":"2",
//                "required":true
//        },
//        {
//            "sequence":"3",
//                "dataField":"",
//                "required":true
//        }
//       ]
//    }
    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateDataType(@RequestBody DataTypeRequest dataTypeRequest) throws JsonProcessingException {
        MessageModel updateResponse;
        if(dataTypeRequest.getSite()!=null && !dataTypeRequest.getSite().isEmpty()){
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(dataTypeRequest.getSite()).hookPoint("PRE").activity("datatype-service").hookableMethod("update").request(objectMapper.writeValueAsString(dataTypeRequest)).build();
            String preExtensionResponse = dataTypeService.callExtension(preExtension);
            DataTypeRequest preExtensionPcuRouterHeader = objectMapper.readValue(preExtensionResponse, DataTypeRequest.class);

            try {
                updateResponse = dataTypeService.updateDataType(preExtensionPcuRouterHeader);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(dataTypeRequest.getSite())
                        .change_type("Update")
                        .action_code("DATATYPE-UPDATE")
                        .action_detail("DATATYPE UPDATED "+dataTypeRequest.getDataType())
                        .action_detail_handle("ActionDetailBO:"+dataTypeRequest.getSite()+","+"DATATYPE-UPDATE"+","+dataTypeRequest.getUserId()+":"+"com.rits.datatypeservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(dataTypeRequest.getUserId())
                        .txnId("DATATYPE-UPDATE"+String.valueOf(LocalDateTime.now())+dataTypeRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Update")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                Extension postExtension = Extension.builder().site(dataTypeRequest.getSite()).hookPoint("POST").activity("datatype-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateResponse.getResponse())).build();
                String postExtensionResponse = dataTypeService.callExtension(postExtension);
                DataType postExtensionDataType = objectMapper.readValue(postExtensionResponse, DataType.class);
                return ResponseEntity.ok(MessageModel.builder().message_details(updateResponse.getMessage_details()).response(postExtensionDataType).build());
            }catch(DataTypeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataTypeException(1002, dataTypeRequest.getSite());
    }


//    {
//        "site":"RITS",
//            "dataType":"datatype2",
    //            "category":"c2",
//    }

    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DataType> retrieveDataType(@RequestBody DataTypeRequest dataTypeRequest) throws JsonProcessingException {
        DataType dataTypeResponse;
        if(dataTypeRequest.getSite()!=null && !dataTypeRequest.getSite().isEmpty()){
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(dataTypeRequest.getSite()).hookPoint("PRE").activity("datatype-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(dataTypeRequest)).build();
            String preExtensionResponse = dataTypeService.callExtension(preExtension);
            DataTypeRequest preExtensionDataType = objectMapper.readValue(preExtensionResponse, DataTypeRequest.class);

            try {
                dataTypeResponse = dataTypeService.retrieveDataType(preExtensionDataType);
                Extension postExtension = Extension.builder().site(dataTypeRequest.getSite()).hookPoint("POST").activity("datatype-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(dataTypeResponse)).build();
                String postExtensionResponse = dataTypeService.callExtension(postExtension);
                DataType postExtensionDataType = objectMapper.readValue(postExtensionResponse, DataType.class);
                return ResponseEntity.ok(postExtensionDataType);
            }catch(DataTypeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataTypeException(1002, dataTypeRequest.getSite());
    }



//    {
//        "site":"RITS",
//            "category":"c2",
//    }
    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DataTypeResponseList> getDataTypeListByCreationDate(@RequestBody DataTypeRequest dataTypeRequest) {
        DataTypeResponseList top50Response;
        if(dataTypeRequest.getSite()!=null && !dataTypeRequest.getSite().isEmpty()) {
            try {
                top50Response= dataTypeService.getDataTypeListByCreationDate(dataTypeRequest);
                return ResponseEntity.ok(top50Response);
            }catch(DataTypeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataTypeException(1002, dataTypeRequest.getSite());
    }


//    {
//        "site":"RITS",
//            "dataType":"d",   // partial value retrieves all .if its empty it retrives Top50
//            "category":"c2",
//    }
    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DataTypeResponseList> getDataTypeList(@RequestBody DataTypeRequest dataTypeRequest) {
        DataTypeResponseList dataTypeListResponse;
        if(dataTypeRequest.getSite()!=null && !dataTypeRequest.getSite().isEmpty()) {
            try {
                dataTypeListResponse= dataTypeService.getDataTypeList(dataTypeRequest);
                return ResponseEntity.ok(dataTypeListResponse);
            }catch(DataTypeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataTypeException(1002, dataTypeRequest.getSite());
    }



//    {
//        "site":"RITS",
//            "category":"c2",
//    }
    @PostMapping("/retrieveBySite")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DataTypeResponseList> retrieveAllBySite(@RequestBody DataTypeRequest dataTypeRequest) {
        DataTypeResponseList allDatatypeResponse;
        if(dataTypeRequest.getSite()!=null && !dataTypeRequest.getSite().isEmpty()) {
            try {
                allDatatypeResponse= dataTypeService.retrieveAllBySite(dataTypeRequest);
                return ResponseEntity.ok(allDatatypeResponse);
            }catch(DataTypeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataTypeException(1002, dataTypeRequest.getSite());
    }



//    {
//        "site":"RITS",
//            "dataType":"datatype1",
//            "category":"c2",
//    }
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteDataType(@RequestBody DataTypeRequest dataTypeRequest) throws JsonProcessingException {
        MessageModel deleteResponse;
        if(dataTypeRequest.getSite()!=null && !dataTypeRequest.getSite().isEmpty()) {
                objectMapper.registerModule(new JavaTimeModule());
                Extension preExtension = Extension.builder().site(dataTypeRequest.getSite()).hookPoint("PRE").activity("datatype-service").hookableMethod("delete").request(objectMapper.writeValueAsString(dataTypeRequest)).build();
                String preExtensionResponse = dataTypeService.callExtension(preExtension);
                DataTypeRequest preExtensionDataType = objectMapper.readValue(preExtensionResponse, DataTypeRequest.class);
                try {
                    deleteResponse = dataTypeService.deleteDataType(preExtensionDataType);
                    AuditLogRequest activityLog = AuditLogRequest.builder()
                            .site(dataTypeRequest.getSite())
                            .change_type("Delete")
                            .action_code("DATATYPE-DELETE")
                            .action_detail("DATATYPE DELETED "+dataTypeRequest.getDataType())
                            .action_detail_handle("ActionDetailBO:"+dataTypeRequest.getSite()+","+"DATATYPE-DELETE"+","+dataTypeRequest.getUserId()+":"+"com.rits.datatypeservice.service")
                            .date_time(String.valueOf(LocalDateTime.now()))
                            .userId(dataTypeRequest.getUserId())
                            .txnId("DATATYPE-DELETE"+String.valueOf(LocalDateTime.now())+dataTypeRequest.getUserId())
                            .created_date_time(String.valueOf(LocalDateTime.now()))
                            .category("Delete")
                            .topic("audit-log")
                            .build();
                    eventPublisher.publishEvent(new ProducerEvent(activityLog));

                    return ResponseEntity.ok(deleteResponse);
            }catch(DataTypeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataTypeException(1002, dataTypeRequest.getSite());
    }



//    {
//        "site":"RITS",
//            "dataType":"datatype2",
//            "category":"c2",
//    }
    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isDataTypeExist(@RequestBody DataTypeRequest dataTypeRequest){
        Boolean isExistResponse;
        if(dataTypeRequest.getSite()!=null && !dataTypeRequest.getSite().isEmpty()) {
            try {
                isExistResponse= dataTypeService.isDataTypeExist(dataTypeRequest);
                return ResponseEntity.ok(isExistResponse);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataTypeException(1002, dataTypeRequest.getSite());
    }


//    {
//        "site":"RITS",
//            "dataType":"datatype2",
    //            "category":"c2",
//    }

    @PostMapping("/retrieveDataFieldList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DataFieldResponseList> getDataFieldList(@RequestBody  DataTypeRequest dataTypeRequest){
        DataFieldResponseList dataFieldListResponse;
        if(dataTypeRequest.getSite()!=null && !dataTypeRequest.getSite().isEmpty()) {
            try {
                dataFieldListResponse= dataTypeService.getDataFieldList(dataTypeRequest);
                return ResponseEntity.ok(dataFieldListResponse);
            }catch (DataTypeException e) {
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DataTypeException(1002, dataTypeRequest.getSite());
    }
    @PostMapping("getDataType")
    public ResponseEntity<DataType> getDataType(@RequestBody IsExist isExist) throws Exception {
        DataType getDataType;
        try{
            getDataType=dataTypeService.retrieveDataType(isExist.getSite(), isExist.getBom(), isExist.getRevision(), isExist.getItem(),isExist.getCategory());
            return ResponseEntity.ok(getDataType);
        } catch (DataTypeException dataTypeException) {
            throw dataTypeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
