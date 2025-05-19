package com.rits.listmaintenceservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.listmaintenceservice.dto.Extension;
import com.rits.listmaintenceservice.dto.ListMaintenanceRequest;
import com.rits.listmaintenceservice.dto.ListMaintenanceResponseList;
import com.rits.listmaintenceservice.exception.ListMaintenanceException;
import com.rits.listmaintenceservice.model.ListMaintenance;
import com.rits.listmaintenceservice.model.MessageModel;
import com.rits.listmaintenceservice.service.ListMaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/listmaintenance-service")
public class ListMaintenanceController {
    private final ListMaintenanceService listMaintenanceService;
    private final ObjectMapper objectMapper;




    //{
    //  "site": "rits",
    //  "list": "example-list1"
    //}


    //{
    //  "site": "rits",
    //  "list": "example-list",
    //  "category": "example-category",
    //  "maximumNumberOfRow": "100",
    //  "type": "example-type",
    //  "allowOperatorToChangeColumnSequence": true,
    //  "allowOperatorToSortRows": true,
    //  "allowMultipleSelection": false,
    //  "showAllActiveSfcsToOperator": true,
    //  "columnList": [
    //    {
    //      "columnSequence": "1",
    //      "columnName": "dataField11",
    //      "rowSortOrder": "ascending",
    //      "width": "100",
    //      "details": {
    //        "icon": "",
    //        "status": "active"
    //      }
    //    },
    //    {
    //      "columnSequence": "2",
    //      "columnName": "dataField1",
    //      "rowSortOrder": "descending",
    //      "width": "80",
    //      "details": {
    //        "icon": "icon2",
    //        "status": "inactive"
    //      }
    //    },
    //    {
    //      "columnSequence": "3",
    //      "columnName": "dataField1",
    //      "rowSortOrder": "ascending",
    //      "width": "120",
    //      "details": {
    //        "icon": "icon3",
    //        "status": ""
    //      }
    //    }
    //  ]
    //}
    @PostMapping("create")
    public ResponseEntity<?> createListMaintenance(@RequestBody ListMaintenanceRequest listMaintenanceRequest) throws JsonProcessingException {
//        ResponseEntity<?> validationListMaintenanceResponse = validationController.validateListMaintenanceSchema( payload);
//        if (validationListMaintenanceResponse.getStatusCode() != HttpStatus.OK) {
//            return ResponseEntity.badRequest().body(validationListMaintenanceResponse.getBody());
//        }
//        ListMaintenanceRequest listMaintenanceRequest = new ObjectMapper().convertValue(payload, ListMaintenanceRequest.class);


        MessageModel createListMaintenance;
        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(listMaintenanceRequest.getSite()).hookPoint("PRE").activity("listmaintenance-service").hookableMethod("create").request(objectMapper.writeValueAsString(listMaintenanceRequest)).build();
        String preExtensionResponse = listMaintenanceService.callExtension(preExtension);
        ListMaintenanceRequest preExtensionListMaintenance = objectMapper.readValue(preExtensionResponse, ListMaintenanceRequest.class);

        try {
            createListMaintenance = listMaintenanceService.createListMaintenance(preExtensionListMaintenance);
            Extension postExtension = Extension.builder().site(preExtensionListMaintenance.getSite()).hookPoint("POST").activity("item-service").hookableMethod("create").request(objectMapper.writeValueAsString(createListMaintenance.getResponse())).build();
            String postExtensionResponse = listMaintenanceService.callExtension(postExtension);
            ListMaintenance postExtensionListMaintenance= objectMapper.readValue(postExtensionResponse, ListMaintenance.class);
            return ResponseEntity.ok( MessageModel.builder().message_details(createListMaintenance.getMessage_details()).response(postExtensionListMaintenance).build());


        } catch (ListMaintenanceException listMaintenanceException) {
            throw listMaintenanceException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //{
    //  "site": "rits",
    //  "list": "example-list1",
    //  "category": "example-category",
    //  "maximumNumberOfRow": "100",
    //  "type": "example-type",
    //  "allowOperatorToChangeColumnSequence": true,
    //  "allowOperatorToSortRows": true,
    //  "allowMultipleSelection": false,
    //  "showAllActiveSfcsToOperator": true,
    //  "columnList": [
    //    {
    //      "columnSequence": "1",
    //      "columnName": "dataField11",
    //      "rowSortOrder": "ascending",
    //      "width": "100",
    //      "details": {
    //        "icon": "",
    //        "status": "active"
    //      }
    //    },
    //    {
    //      "columnSequence": "2",
    //      "columnName": "dataField1",
    //      "rowSortOrder": "descending",
    //      "width": "80",
    //      "details": {
    //        "icon": "icon2",
    //        "status": "inactive"
    //      }
    //    },
    //    {
    //      "columnSequence": "3",
    //      "columnName": "dataField1",
    //      "rowSortOrder": "ascending",
    //      "width": "120",
    //      "details": {
    //        "icon": "icon3",
    //        "status": ""
    //      }
    //    }
    //  ]
    //}
    @PostMapping("update")
    public ResponseEntity<MessageModel> updateListMaintenance(@RequestBody ListMaintenanceRequest listMaintenanceRequest) throws JsonProcessingException {
       MessageModel updateListMaintenance;
//        objectMapper.registerModule(new JavaTimeModule());
//        Extension preExtension = Extension.builder().site(listMaintenanceRequest.getSite()).hookPoint("PRE").activity("listmaintenance-service").hookableMethod("update").request(objectMapper.writeValueAsString(listMaintenanceRequest)).build();
//        String preExtensionResponse = listMaintenanceService.callExtension(preExtension);
//        ListMaintenanceRequest preExtensionListMaintenance = objectMapper.readValue(preExtensionResponse, ListMaintenanceRequest.class);
        try {
//            updateListMaintenance= listMaintenanceService.updateListMaintenance(preExtensionListMaintenance);
//            Extension postExtension = Extension.builder().site(listMaintenanceRequest.getSite()).hookPoint("POST").activity("listmaintenance-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateListMaintenance.getResponse())).build();
//            String postExtensionResponse = listMaintenanceService.callExtension(postExtension);
//            ListMaintenance postExtensionListMaintenance= objectMapper.readValue(postExtensionResponse, ListMaintenance.class);
//
//
//
//
        updateListMaintenance=listMaintenanceService.updateListMaintenance(listMaintenanceRequest);
        return ResponseEntity.ok(updateListMaintenance);
     //   return ResponseEntity.ok( MessageModel.builder().message_details(updateListMaintenance.getMessage_details()).response(postExtensionListMaintenance).build());

        } catch (ListMaintenanceException listMaintenanceException) {
            throw listMaintenanceException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //{
    //  "site": "rits",
    //  "list": "example-list1"
    //}
    @PostMapping("retrieve")
    public ResponseEntity<ListMaintenance> retrieveListMaintenance(@RequestBody ListMaintenanceRequest listMaintenanceRequest) throws JsonProcessingException {
        ListMaintenance retrieveListMaintenance;

        if (listMaintenanceRequest.getSite() != null && !listMaintenanceRequest.getSite().isEmpty()) {

            try {
                retrieveListMaintenance= listMaintenanceService.retrieveListMaintenance(listMaintenanceRequest.getSite(),listMaintenanceRequest.getList(),listMaintenanceRequest.getCategory());
                return ResponseEntity.ok(retrieveListMaintenance);

            } catch (ListMaintenanceException listMaintenanceException) {
                throw listMaintenanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ListMaintenanceException(1);
    }
    //{
    //  "site": "rits",
    //  "list": "example-list1"
    //}
    @PostMapping("delete")
    public ResponseEntity<MessageModel> deleteListMaintenance(@RequestBody ListMaintenanceRequest listMaintenanceRequest) throws JsonProcessingException {
        MessageModel deleteListMaintenance;

        if (listMaintenanceRequest.getSite() != null && !listMaintenanceRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(listMaintenanceRequest.getSite()).hookPoint("PRE").activity("listmaintenance-service").hookableMethod("delete").request(objectMapper.writeValueAsString(listMaintenanceRequest)).build();
            String preExtensionResponse = listMaintenanceService.callExtension(preExtension);
            ListMaintenanceRequest preExtensionListMaintenance = objectMapper.readValue(preExtensionResponse, ListMaintenanceRequest.class);

            try {
                deleteListMaintenance= listMaintenanceService.deleteListMaintenance(preExtensionListMaintenance.getSite(),preExtensionListMaintenance.getList(),preExtensionListMaintenance.getCategory());
                Extension postExtension = Extension.builder().site(listMaintenanceRequest.getSite()).hookPoint("POST").activity("listmaintenance-service").hookableMethod("delete").request(objectMapper.writeValueAsString(deleteListMaintenance)).build();
                String postExtensionResponse = listMaintenanceService.callExtension(postExtension);
                ListMaintenance postExtensionListMaintenance = objectMapper.readValue(postExtensionResponse, ListMaintenance.class);
                return ResponseEntity.ok(deleteListMaintenance);

            } catch (ListMaintenanceException listMaintenanceException) {
                throw listMaintenanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ListMaintenanceException(1);
    }
    //{
    //    "site":"rits"
    //}
    @PostMapping("retrieveTop50")
    public ResponseEntity<ListMaintenanceResponseList> getAllListMaintenanceByCreatedDate(@RequestBody ListMaintenanceRequest listMaintenanceRequest) {
        ListMaintenanceResponseList retrieveTop50;
        if (listMaintenanceRequest.getSite() != null && !listMaintenanceRequest.getSite().isEmpty()) {

            try {
                retrieveTop50 = listMaintenanceService.getAllListMaintenanceByCreatedDate(listMaintenanceRequest.getSite());
                return ResponseEntity.ok(retrieveTop50);
            } catch (ListMaintenanceException listMaintenanceException) {
                throw listMaintenanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ListMaintenanceException(1);
    }
    //{
    //    "site":"rits",
    //    "list": "exa"
    //}
    @PostMapping("retrieveAll")
    public ResponseEntity<ListMaintenanceResponseList> getAllListMaintenance(@RequestBody ListMaintenanceRequest listMaintenanceRequest) {
        ListMaintenanceResponseList getAllListMaintenance;
        if (listMaintenanceRequest.getSite() != null && !listMaintenanceRequest.getSite().isEmpty()) {
            try {
                getAllListMaintenance = listMaintenanceService.getAllListMaintenance(listMaintenanceRequest.getSite(), listMaintenanceRequest.getList());
                return ResponseEntity.ok(getAllListMaintenance);
            } catch (ListMaintenanceException listMaintenanceException) {
                throw listMaintenanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ListMaintenanceException(1);
    }
    @PostMapping("getAllListByCategory")
    public ResponseEntity<ListMaintenanceResponseList> getAllListByCategory(@RequestBody ListMaintenanceRequest listMaintenanceRequest) {
        ListMaintenanceResponseList getAllListMaintenance;
        if (listMaintenanceRequest.getSite() != null && !listMaintenanceRequest.getSite().isEmpty()) {
            try {
                getAllListMaintenance = listMaintenanceService.getAllListByCategory(listMaintenanceRequest.getSite(), listMaintenanceRequest.getCategory());
                return ResponseEntity.ok(getAllListMaintenance);
            } catch (ListMaintenanceException listMaintenanceException) {
                throw listMaintenanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ListMaintenanceException(1);
    }





}
