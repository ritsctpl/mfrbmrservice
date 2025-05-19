package com.rits.bomservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.bomservice.Exception.BomException;
import com.rits.bomservice.dto.*;
import com.rits.bomservice.model.Bom;
import com.rits.bomservice.model.BomMessageModel;
import com.rits.bomservice.service.BomService;
import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.kafkaservice.ProducerEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/bom-service")
public class BomController {

    private final BomService bomService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;


    @PostMapping("retrieveTop50")
    public ResponseEntity<BomResponseList> getBomListByCreationDate(@RequestBody BomRequest bomRequest) {

        BomResponseList retrieveTop50Boms;
        if (bomRequest.getSite() != null && !bomRequest.getSite().isEmpty()) {
            try {
                retrieveTop50Boms = bomService.getBomListByCreationDate(bomRequest.getSite());
                return ResponseEntity.ok(retrieveTop50Boms);
            } catch (BomException bomException) {
                throw bomException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BomException(1);
    }

    @PostMapping("retrieveAll")
    public ResponseEntity<BomResponseList> getBomList(@RequestBody BomRequest bomRequest) {
        BomResponseList retrieveAllBom;
        if (bomRequest.getSite() != null && !bomRequest.getSite().isEmpty()) {
            try {
                retrieveAllBom = bomService.getBomList(bomRequest.getBom(), bomRequest.getSite());
                return ResponseEntity.ok(retrieveAllBom);
            } catch (BomException bomException) {
                throw bomException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BomException(1);

    }

    @PostMapping("retrieve")
    public ResponseEntity<Bom> retrieveBom(@RequestBody BomRequest bomRequest) throws Exception {
        Bom retrieveBom;
        if (bomRequest.getSite() != null && !bomRequest.getSite().isEmpty()) {
           
            try {
                retrieveBom = bomService.retrieveBom(bomRequest.getBom(), bomRequest.getRevision(), bomRequest.getSite());
              
                return ResponseEntity.ok(retrieveBom);

            } catch (BomException bomException) {
                throw bomException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BomException(1);
    }


    @PostMapping("create")
    public ResponseEntity<?> createBom(@RequestBody BomRequest bomRequest) throws Exception {
        BomMessageModel createBom;


        
        try {
            createBom = bomService.createBom(bomRequest);
           
            AuditLogRequest auditlog = bomService.createAuditLog(bomRequest);
            eventPublisher.publishEvent(new ProducerEvent(auditlog));

            return ResponseEntity.ok(BomMessageModel.builder().message_details(createBom.getMessage_details()).response(createBom.getResponse()).build());

        } catch (BomException bomException) {
            throw bomException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("delete")
    public ResponseEntity<BomMessageModel> deleteBom(@RequestBody BomRequest bomRequest) throws Exception {
        BomMessageModel deleteResponse;
        if (bomRequest.getSite() != null && !bomRequest.getSite().isEmpty()) {
        
            try {
                deleteResponse = bomService.deleteBom(bomRequest.getBom(), bomRequest.getRevision(), bomRequest.getSite(), bomRequest.getUserId());

                AuditLogRequest auditlog = bomService.deleteAuditLog(bomRequest);
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                return ResponseEntity.ok(deleteResponse);

            } catch (BomException bomException) {
                throw bomException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BomException(1);
    }


    @PostMapping("isBomUsed")
    public ResponseEntity<Boolean> isBomUsed(@RequestBody BomRequest bomRequest) {
        Boolean isBomUsed;
        if (bomRequest.getSite() != null && !bomRequest.getSite().isEmpty()) {

            try {
                isBomUsed = bomService.isBomUsed(bomRequest.getBom(), bomRequest.getRevision(), bomRequest.getSite());
                return ResponseEntity.ok(isBomUsed);
            } catch (BomException bomException) {
                throw bomException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BomException(1);
    }

    @PostMapping("update")
    public ResponseEntity<BomMessageModel> updateBom(@RequestBody BomRequest bomRequest) throws Exception {
        BomMessageModel updateBom;

        
        try {
            updateBom = bomService.updateBom(bomRequest);

            AuditLogRequest auditlog = bomService.updateAuditLog(bomRequest);
            eventPublisher.publishEvent(new ProducerEvent(auditlog));

            return ResponseEntity.ok(BomMessageModel.builder().message_details(updateBom.getMessage_details()).response(updateBom.getResponse()).build());
        } catch (BomException bomException) {
            throw bomException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getComponentList")
    public ResponseEntity<BomComponentList> getComponentList(@RequestBody BomRequest bomRequest) {
        BomComponentList bomComponentList;
        if (bomRequest.getSite() != null && !bomRequest.getSite().isEmpty()) {

            try {
                bomComponentList = bomService.getComponentListByOperation(bomRequest.getBom(), bomRequest.getRevision(), bomRequest.getSite(), bomRequest.getOperation());
                return ResponseEntity.ok(bomComponentList);
            } catch (BomException bomException) {
                throw bomException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BomException(1);
    }


    @PostMapping("componentUsage")
    public ResponseEntity<BomResponseList> componentUsage(@RequestBody ComponentUsageRequest componentUsageRequest) {
        BomResponseList componentUsage;
        if (componentUsageRequest.getSite() != null && !componentUsageRequest.getSite().isEmpty()) {

            try {
                componentUsage = bomService.componentUsage(componentUsageRequest.getComponent(), componentUsageRequest.getComponentVersion(), componentUsageRequest.getSite());
                return ResponseEntity.ok(componentUsage);
            } catch (BomException bomException) {
                throw bomException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BomException(1);
    }

    @PostMapping("isExist")
    public ResponseEntity<Boolean> isBomExist(@RequestBody BomRequest bomRequest) {
        Boolean isBomExist;
        if (bomRequest.getSite() != null && !bomRequest.getSite().isEmpty()) {

            try {
                isBomExist = bomService.isBomExist(bomRequest.getBom(), bomRequest.getRevision(), bomRequest.getSite());
                return ResponseEntity.ok(isBomExist);
            } catch (BomException bomException) {
                throw bomException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BomException(1);
    }

    @PostMapping("retrieveByBomAndBomType")
    public ResponseEntity<?> retrieveByBomAndBomType(@RequestBody BomRequest bomRequest) {
        BomResponseList bomList = null;
        try {
            bomList = bomService.retrieveByBomTypeAndSite(bomRequest.getSite(), bomRequest.getBomType());
            return ResponseEntity.ok(bomList);
        } catch (BomException bomException) {
            throw bomException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
 