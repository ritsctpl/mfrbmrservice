package com.rits.barcodeservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.AuditLogService;
import com.rits.auditlogservice.service.ProducerEvent;
import com.rits.barcodeservice.dto.*;
import com.rits.barcodeservice.exception.BarCodeException;
import com.rits.barcodeservice.model.Barcode;
import com.rits.barcodeservice.model.ListDetails;
import com.rits.barcodeservice.model.MessageModel;
import com.rits.barcodeservice.service.BarcodeService;
import com.rits.dataFieldService.dto.DataFieldRequest;
import com.rits.dataFieldService.dto.DataFieldResponseList;
import com.rits.dataFieldService.exception.DataFieldException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("app/v1/barcode-service")
@RequiredArgsConstructor
public class BarcodeController {
    private final ObjectMapper objectMapper;
    private final BarcodeService barcodeservice;

    private final AuditLogService auditlogservice;
    private final ApplicationEventPublisher eventPublisher;
    @PostMapping("create")
    public ResponseEntity<?> createDataField(@RequestBody BarcodeRequest barcodeRequest) throws Exception {
        MessageModel createDataField;
        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(barcodeRequest.getSite()).hookPoint("PRE").activity("barcode-service").hookableMethod("create").request(objectMapper.writeValueAsString(barcodeRequest)).build();
        String preExtensionResponse = barcodeservice.callExtension(preExtension);
        BarcodeRequest preExtensionDataField = objectMapper.readValue(preExtensionResponse, BarcodeRequest.class);

        try {
            createDataField = barcodeservice.createBarcode(preExtensionDataField);
            Extension postExtension = Extension.builder().site(barcodeRequest.getSite()).hookPoint("POST").activity("barcode-service").hookableMethod("create").request(objectMapper.writeValueAsString(createDataField.getResponse())).build();
            String postExtensionResponse = barcodeservice.callExtension(postExtension);
            Barcode postExtensionDataField = objectMapper.readValue(postExtensionResponse, Barcode.class);

            AuditLogRequest auditlog = AuditLogRequest.builder()
                    .site(barcodeRequest.getSite())
                    .action_code("BARCODE-CREATED")
                    .action_detail("Barcode Created "+ barcodeRequest.getCode())
                    .action_detail_handle("ActionDetailBO:"+barcodeRequest.getSite()+","+"BARCODE-CREATED"+barcodeRequest.getCreatedBy()+":"+"com.rits.barcodeservice.controller")
                    .activity("From Service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(barcodeRequest.getCreatedBy())
                    .txnId("BARCODE-CREATED"+String.valueOf(LocalDateTime.now())+barcodeRequest.getCreatedBy())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Create")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(auditlog));

            return ResponseEntity.ok( MessageModel.builder().message_details(createDataField.getMessage_details()).response(postExtensionDataField).build());

        } catch (BarCodeException barcodeException) {
            throw barcodeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("update")
    public ResponseEntity<MessageModel> updateBarcode(@RequestBody BarcodeRequest barcodeRequest) throws Exception {
        MessageModel updateBarcode;


        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(barcodeRequest.getSite()).hookPoint("PRE").activity("barcode-service").hookableMethod("update").request(objectMapper.writeValueAsString(barcodeRequest)).build();
        String preExtensionResponse = barcodeservice.callExtension(preExtension);
        BarcodeRequest preExtensionBarcode = objectMapper.readValue(preExtensionResponse, BarcodeRequest.class);

        try {
            updateBarcode = barcodeservice.updateBarcode(preExtensionBarcode);
            Extension postExtension = Extension.builder().site(barcodeRequest.getSite()).hookPoint("POST").activity("barcode-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateBarcode.getResponse())).build();
            String postExtensionResponse = barcodeservice.callExtension(postExtension);
            Barcode postExtensionDataField = objectMapper.readValue(postExtensionResponse, Barcode.class);


            AuditLogRequest auditlog = AuditLogRequest.builder()
                    .site(barcodeRequest.getSite())
                    .action_code("BARCODE-UPDATED "+ barcodeRequest.getUserId())
                    .action_detail("Barcode Updated")
                    .action_detail_handle("ActionDetailBO:"+barcodeRequest.getSite()+","+"BARCODE-UPDATED"+barcodeRequest.getUserId()+":"+"com.rits.barcodeservice.controller")
                    .activity("From Service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(barcodeRequest.getUserId())
                    .txnId("BARCODE-UPDATED"+String.valueOf(LocalDateTime.now())+barcodeRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Update")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(auditlog));


            return ResponseEntity.ok( MessageModel.builder().message_details(updateBarcode.getMessage_details()).response(postExtensionDataField).build());


        } catch (BarCodeException barcodeException) {
            throw barcodeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("delete")
    public ResponseEntity<MessageModel> deleteCode(@RequestBody BarcodeRequest barcodeRequest) throws Exception {
        MessageModel deleteResponse;
        if (barcodeRequest.getSite() != null && !barcodeRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(barcodeRequest.getSite()).hookPoint("PRE").activity("barcode-service").hookableMethod("delete").request(objectMapper.writeValueAsString(barcodeRequest)).build();
            String preExtensionResponse = barcodeservice.callExtension(preExtension);
            BarcodeRequest preExtensionDataField = objectMapper.readValue(preExtensionResponse, BarcodeRequest.class);

            try {
                deleteResponse = barcodeservice.deleteCode(preExtensionDataField.getCode(), preExtensionDataField.getSite(),preExtensionDataField.getUserId());
                Extension postExtension = Extension.builder().site(barcodeRequest.getSite()).hookPoint("POST").activity("barcode-service").hookableMethod("delete").request(objectMapper.writeValueAsString(deleteResponse.getResponse())).build();
                String postExtensionResponse = barcodeservice.callExtension(postExtension);
                Barcode postExtensionDataField = objectMapper.readValue(postExtensionResponse, Barcode.class);
                // return ResponseEntity.ok(postExtensionDataField);

                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(barcodeRequest.getSite())
                        .action_code("BARCODE-DELETED")
                        .action_detail("Barcode Deleted")
                        .action_detail_handle("ActionDetailBO:"+barcodeRequest.getSite()+","+"BARCODE-DELETED"+barcodeRequest.getUserId()+":"+"com.rits.barcodeservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(barcodeRequest.getUserId())
                        .txnId("BARCODE-DELETED"+String.valueOf(LocalDateTime.now())+barcodeRequest.getCreatedBy())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Delete")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                return ResponseEntity.ok(deleteResponse);

            } catch (BarCodeException barcodeException) {
                throw barcodeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BarCodeException(1);

    }

    @PostMapping("retrieveAll")
    public ResponseEntity<?> getCodeList(@RequestBody BarcodeRequest barcodeRequest) {
        BarcodeResponse retrieveAllCode;
        if (barcodeRequest.getSite() != null && !barcodeRequest.getSite().isEmpty()) {
            try {
                retrieveAllCode = barcodeservice.getCodeList(barcodeRequest.getCode(), barcodeRequest.getSite());
                return ResponseEntity.ok(retrieveAllCode);
            } catch (BarCodeException barcodeException) {
                throw barcodeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BarCodeException(1);

    }
    @PostMapping("getAllCode")
    public ResponseEntity<?> getAllCode(@RequestBody BarcodeRequest barcodeRequest) {
        BarcodeAllCodeList getAllCode;
        if (barcodeRequest.getSite() != null && !barcodeRequest.getSite().isEmpty()) {
            try {
                getAllCode = barcodeservice.getAllCode(barcodeRequest.getSite());
                return ResponseEntity.ok(getAllCode);
            } catch (BarCodeException barcodeException) {
                throw barcodeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BarCodeException(1);

    }
    
    
}
