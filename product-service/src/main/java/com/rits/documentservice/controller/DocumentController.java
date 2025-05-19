package com.rits.documentservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.documentservice.dto.*;
import com.rits.documentservice.exception.DocumentException;
import com.rits.documentservice.model.Document;
import com.rits.documentservice.model.MessageModel;
import com.rits.documentservice.service.DocumentService;
import org.springframework.context.ApplicationEventPublisher;
import com.rits.kafkaservice.ProducerEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("app/v1/document-service")
@RequiredArgsConstructor
public class  DocumentController {
    private final DocumentService documentService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;


    //{
    //    "site": "rits",
    //    "document": "Example Document1",
    //    "version": "1.0"
    //
    //}


//    {
//        "site": "rits",
//        "document": "Example Document",
//        "version": "1.0",
//        "description": "This is an example document",
//        "printQty": "1",
//        "documentType": "PDF",
//        "printBy": "John Doe",
//        "printMethods": "Email",
//        "status": "Approved",
//        "currentVersion": true,
//        "template": "example_template.html",
//        "documentOptions": [
//            {
//                "bomAssemblyMetrics": true,
//                "bomComponentData": true,
//                "bomHeaderData": true,
//                "containerAssemblyMetrics": true,
//                "containerCustomData": false,
//                "containerHeaderData": true,
//                "documentCustomData": true,
//                "floorStockHeaderData": false,
//                "floorStockReceiptData": true,
//                "materialCustomData": false,
//                "ncCodeCustomData": false,
//                "ncData": true,
//                "operationCustomData": true,
//                "parametricData": false,
//                "routingData": true,
//                "pcuData": true,
//                "pcuHeader": false,
//                "pcuPackData": true,
//                "shopOrderCustomData": true,
//                "shopOrderHeaderData": false,
//                "workInstructionData": true
//            }
//        ],
//        "printIntegration": [
//            {
//                "dataAcquisition": "Example Data Acquisition",
//                "formatting": "PDF",
//                "transport": "Email",
//                "writeErrorLog": true
//            }
//        ],
//        "customDataList": [
//            {
//                "customData": "Example Custom Data 1",
//                "value": "Example Value 1"
//            },
//            {
//                "customData": "Example Custom Data 2",
//                "value": "Example Value 2"
//            }
//        ],
//        "inUse": true
//    }


    //site,docu,version,prtQty,Template,
    @PostMapping("create")
    public ResponseEntity<?> createDocument(@RequestBody DocumentRequest documentRequest) throws Exception {
//        MessageModel validationResponse = documentService.validation( payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        DocumentRequest documentRequest = new ObjectMapper().convertValue(payload, DocumentRequest.class);

        MessageModel createDocument;
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(documentRequest.getSite()).hookPoint("PRE").activity("document-service").hookableMethod("create").request(objectMapper.writeValueAsString(documentRequest)).build();
            String preExtensionResponse = documentService.callExtension(preExtension);
            DocumentRequest preExtensionDocument = objectMapper.readValue(preExtensionResponse, DocumentRequest.class);

            try {
                createDocument = documentService.createDocument(preExtensionDocument);
                Extension postExtension = Extension.builder().site(documentRequest.getSite()).hookPoint("POST").activity("document-service").hookableMethod("create").request(objectMapper.writeValueAsString(createDocument.getResponse())).build();
                String postExtensionResponse = documentService.callExtension(postExtension);
                Document postExtensionDocument = objectMapper.readValue(postExtensionResponse, Document.class);

                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(documentRequest.getSite())
                        .action_code("DOCUMENT-CREATED "+documentRequest.getDescription())
                        .action_detail("Document Created "+documentRequest.getDescription())
                        .action_detail_handle("ActionDetailBO:"+documentRequest.getSite()+","+"DOCUMENT-CREATED"+","+documentRequest.getUserId()+":"+"com.rits.documentservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(documentRequest.getUserId())
                        .txnId("DOCUMENT-CREATED"+String.valueOf(LocalDateTime.now())+documentRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Create")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                return ResponseEntity.ok( MessageModel.builder().message_details(createDocument.getMessage_details()).response(postExtensionDocument).build());
            } catch (DocumentException documentException) {
                throw documentException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    //{
    //        "site": "rits",
    //        "document": "Example Document",
    //        "version": "1.0",
    //        "description": "This is an example document",
    //        "printQty": "1",
    //        "documentType": "PDF",
    //        "printBy": "John Doe",
    //        "printMethods": "Email",
    //        "status": "Approved",
    //        "currentVersion": true,
    //        "template": "example_template.html",
    //        "documentOptions": [
    //            {
    //                "bomAssemblyMetrics": true,
    //                "bomComponentData": true,
    //                "bomHeaderData": true,
    //                "containerAssemblyMetrics": true,
    //                "containerCustomData": false,
    //                "containerHeaderData": true,
    //                "documentCustomData": true,
    //                "floorStockHeaderData": false,
    //                "floorStockReceiptData": true,
    //                "materialCustomData": false,
    //                "ncCodeCustomData": false,
    //                "ncData": true,
    //                "operationCustomData": true,
    //                "parametricData": false,
    //                "routingData": true,
    //                "pcuData": true,
    //                "pcuHeader": false,
    //                "pcuPackData": true,
    //                "shopOrderCustomData": true,
    //                "shopOrderHeaderData": false,
    //                "workInstructionData": true
    //            }
    //        ],
    //        "printIntegration": [
    //            {
    //                "dataAcquisition": "Example Data Acquisition",
    //                "formatting": "PDF",
    //                "transport": "Email",
    //                "writeErrorLog": true
    //            }
    //        ],
    //        "customDataList": [
    //            {
    //                "customData": "Example Custom Data 1",
    //                "value": "Example Value 1"
    //            },
    //            {
    //                "customData": "Example Custom Data 2",
    //                "value": "Example Value 2"
    //            }
    //        ],
    //        "inUse": true
    //    }

    @PostMapping("update")
    public ResponseEntity<MessageModel> updateDocument(@RequestBody DocumentRequest documentRequest) throws Exception {
        MessageModel updateDataField;


            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(documentRequest.getSite()).hookPoint("PRE").activity("document-service").hookableMethod("update").request(objectMapper.writeValueAsString(documentRequest)).build();
            String preExtensionResponse = documentService.callExtension(preExtension);
            DocumentRequest preExtensionDocument = objectMapper.readValue(preExtensionResponse, DocumentRequest.class);

            try {
                updateDataField = documentService.updateDocument(preExtensionDocument);
                Extension postExtension = Extension.builder().site(documentRequest.getSite()).hookPoint("POST").activity("document-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateDataField.getResponse())).build();
                String postExtensionResponse = documentService.callExtension(postExtension);
                Document postExtensionDocument = objectMapper.readValue(postExtensionResponse, Document.class);

                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(documentRequest.getSite())
                        .action_code("DOCUMENT-UPDATED "+documentRequest.getDescription())
                        .action_detail("Document Updated")
                        .action_detail_handle("ActionDetailBO:"+documentRequest.getSite()+","+"DOCUMENT-UPDATED"+","+documentRequest.getUserId()+":"+"com.rits.documentservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(documentRequest.getUserId())
                        .txnId("DOCUMENT-UPDATED"+String.valueOf(LocalDateTime.now())+documentRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Update")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                return ResponseEntity.ok( MessageModel.builder().message_details(updateDataField.getMessage_details()).response(postExtensionDocument).build());


            } catch (DocumentException dataFieldException) {
                throw dataFieldException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    //{
    //    "site":"rits"
    //}
    @PostMapping("retrieveTop50")
    public ResponseEntity<DocumentResponseList> getDocumentListByCreationDate(@RequestBody DocumentRequest documentRequest) {
        DocumentResponseList retrieveTop50Documents;
        if (documentRequest.getSite() != null && !documentRequest.getSite().isEmpty()) {
            try {
                retrieveTop50Documents = documentService.getAllDocumentByCreatedDate(documentRequest.getSite());
                return ResponseEntity.ok(retrieveTop50Documents);
            } catch (DocumentException documentException) {
                throw documentException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DocumentException(1);

    }

    //{
    //    "site":"rits"
    //}

    //{
    //    "site":"rits",
    //    "document":"d"
    //}
    @PostMapping("retrieveAll")
    public ResponseEntity<DocumentResponseList> getDocumentList(@RequestBody DocumentRequest documentRequest) {
        DocumentResponseList retrieveAllDocuments;
        if (documentRequest.getSite() != null && !documentRequest.getSite().isEmpty()) {
            try {
                retrieveAllDocuments = documentService.getAllDocument(documentRequest.getSite(), documentRequest.getDocument());
                return ResponseEntity.ok(retrieveAllDocuments);
            } catch (DocumentException documentException) {
                throw documentException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DocumentException(1);

    }

    //{
    //    "site": "rits",
    //    "document": "Example Document",
    //    "version": "1.0"
    //}
    //


    //{
    //    "site": "rits",
    //    "document": "Example Document"}
    //
    @PostMapping("retrieve")
    public ResponseEntity<Document> retrieveDataField(@RequestBody DocumentRequest documentRequest) throws Exception {
        Document retrieveDataField;
        if (documentRequest.getSite() != null && !documentRequest.getSite().isEmpty()) {

            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(documentRequest.getSite()).hookPoint("PRE").activity("document-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(documentRequest)).build();
            String preExtensionResponse = documentService.callExtension(preExtension);
            DocumentRequest preExtensionDocument = objectMapper.readValue(preExtensionResponse, DocumentRequest.class);

            try {
                retrieveDataField = documentService.retrieveDocument(preExtensionDocument.getSite(), preExtensionDocument.getDocument(), preExtensionDocument.getVersion());
                Extension postExtension = Extension.builder().site(documentRequest.getSite()).hookPoint("POST").activity("document-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveDataField)).build();
                String postExtensionResponse = documentService.callExtension(postExtension);
                Document postExtensionDocument = objectMapper.readValue(postExtensionResponse, Document.class);
                return ResponseEntity.ok(postExtensionDocument);


            } catch (DocumentException documentException) {
                throw documentException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DocumentException(1);

    }
    //{
    //    "site": "rits",
    //    "document": "Example Document",
    //    "version": "B"
    //}

    @PostMapping("delete")
    public ResponseEntity<MessageModel> deleteDocument(@RequestBody DocumentRequest documentRequest) throws Exception {
        MessageModel deleteResponse;
        if (documentRequest.getSite() != null && !documentRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(documentRequest.getSite()).hookPoint("PRE").activity("document-service").hookableMethod("delete").request(objectMapper.writeValueAsString(documentRequest)).build();
            String preExtensionResponse = documentService.callExtension(preExtension);
            DocumentRequest preExtensionDocument = objectMapper.readValue(preExtensionResponse, DocumentRequest.class);

            try {
                deleteResponse = documentService.deleteDocument(preExtensionDocument.getSite(), preExtensionDocument.getDocument(), preExtensionDocument.getVersion(),documentRequest.getUserId());
                Extension postExtension = Extension.builder().site(documentRequest.getSite()).hookPoint("POST").activity("document-service").hookableMethod("delete").request(objectMapper.writeValueAsString(deleteResponse.getResponse())).build();
                String postExtensionResponse = documentService.callExtension(postExtension);
                Document postExtensionDocument = objectMapper.readValue(postExtensionResponse, Document.class);
                // return ResponseEntity.ok(postExtensionDataField);

                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(documentRequest.getSite())
                        .action_code("DOCUMENT-DELETED")
                        .action_detail("Document Deleted "+documentRequest.getDescription())
                        .action_detail_handle("ActionDetailBO:"+documentRequest.getSite()+","+"DOCUMENT-DELETED"+","+documentRequest.getUserId()+":"+"com.rits.documentservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(documentRequest.getUserId())
                        .txnId("DOCUMENT-DELETED"+String.valueOf(LocalDateTime.now())+documentRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Delete")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                return ResponseEntity.ok(deleteResponse);

            } catch (DocumentException documentException) {
                throw documentException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DocumentException(1);

    }

    @PostMapping("retrieveBySite")
    public ResponseEntity<List<DocumentResponse>> getAllDocuments(@RequestBody DocumentRequest documentRequest) throws Exception {
        List<DocumentResponse> getAllDocuments;
        if (documentRequest.getSite() != null && !documentRequest.getSite().isEmpty()) {
            try {
                getAllDocuments = documentService.getAllDocuments(documentRequest.getSite());
                return ResponseEntity.ok(getAllDocuments);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DocumentException(1);

    }

}
