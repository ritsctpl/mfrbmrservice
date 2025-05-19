package com.rits.certificationtypeservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.certificationtypeservice.dto.CertificationTypeRequest;
import com.rits.certificationtypeservice.dto.CertificationTypeResponseList;
import com.rits.certificationtypeservice.dto.Extension;

import com.rits.certificationtypeservice.exception.CertificationTypeException;
import com.rits.certificationtypeservice.model.Certification;
import com.rits.certificationtypeservice.model.CertificationType;
import com.rits.certificationtypeservice.model.MessageModel;
import com.rits.certificationtypeservice.service.CertificationTypeService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/certificationtype-service")
public class CertificationTypeController {
    private final CertificationTypeService certificationTypeService;
    private final ObjectMapper objectMapper;

    //{
    //  "site": "rits",
    //  "certificationType": "certificationType1",
    //  "description": "Certification for skillset A",
    //  "filterCertificationId": "1234",
    //  "filterDescription": "Filtering criteria for certification",
    //  "certificationList": [
    //    {
    //      "certification": "Certification A"
    //    },
    //    {
    //      "certification": "Certification B"
    //    },
    //    {
    //      "certification": "Certification C"
    //    }
    //  ]
    //}
    @PostMapping("create")
    public ResponseEntity<?> createCertificationType(@RequestBody CertificationTypeRequest certificationTypeRequest) throws Exception {

//        MessageModel validationResponse = certificationTypeService.validation( payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        CertificationTypeRequest certificationTypeRequest = new ObjectMapper().convertValue(payload, CertificationTypeRequest.class);
            MessageModel createCertificationType;

            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(certificationTypeRequest.getSite()).hookPoint("PRE").activity("certificationtype-service").hookableMethod("create").request(objectMapper.writeValueAsString(certificationTypeRequest)).build();
            String preExtensionResponse = certificationTypeService.callExtension(preExtension);
            CertificationTypeRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, CertificationTypeRequest.class);

            try {
                createCertificationType = certificationTypeService.createCertificationType(preExtensionRequest);
                Extension postExtension = Extension.builder().site(certificationTypeRequest.getSite()).hookPoint("POST").activity("certificationtype-service").hookableMethod("create").request(objectMapper.writeValueAsString(createCertificationType.getResponse())).build();
                String postExtensionResponse = certificationTypeService.callExtension(postExtension);
                CertificationType postExtensionCertificationType = objectMapper.readValue(postExtensionResponse, CertificationType.class);
                return ResponseEntity.ok( MessageModel.builder().message_details(createCertificationType.getMessage_details()).response(postExtensionCertificationType).build());
            } catch (CertificationTypeException certificationTypeException) {
                throw certificationTypeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    //{
    //  "site": "rits",
    //  "certificationType": "certificationType1",
    //  "description": "Certification for skillset A",
    //  "filterCertificationId": "1234",
    //  "filterDescription": "Filtering criteria for certification",
    //  "certificationList": [
    //    {
    //      "certification": "Certification A"
    //    },
    //    {
    //      "certification": "Certification B"
    //    },
    //    {
    //      "certification": "Certification C"
    //    }
    //  ]
    //}
    @PostMapping("update")
    public ResponseEntity<MessageModel> updateCertificationType(@RequestBody CertificationTypeRequest certificationTypeRequest) throws Exception {
        if (certificationTypeRequest.getSite() != null && !certificationTypeRequest.getSite().isEmpty() &&
                certificationTypeRequest.getCertificationType() != null && !certificationTypeRequest.getCertificationType().isEmpty()
        ) {
            MessageModel updateCertificationType;
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(certificationTypeRequest.getSite()).hookPoint("PRE").activity("CertificationType-service").hookableMethod("update").request(objectMapper.writeValueAsString(certificationTypeRequest)).build();
            String preExtensionResponse = certificationTypeService.callExtension(preExtension);
            CertificationTypeRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, CertificationTypeRequest.class);

            try {
                updateCertificationType = certificationTypeService.updateCertificationType(preExtensionRequest);
                Extension postExtension = Extension.builder().site(certificationTypeRequest.getSite()).hookPoint("POST").activity("CertificationType-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateCertificationType.getResponse())).build();
                String postExtensionResponse = certificationTypeService.callExtension(postExtension);
                CertificationType postExtensionCertificationType = objectMapper.readValue(postExtensionResponse, CertificationType.class);
                return ResponseEntity.ok( MessageModel.builder().message_details(updateCertificationType.getMessage_details()).response(postExtensionCertificationType).build());
            } catch (CertificationTypeException certificationTypeException) {
                throw certificationTypeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationTypeException(3303);
    }

    //{
    //  "site": "rits",
    //  "certificationType": "certificationType1"
    //}
    @PostMapping("retrieve")
    public ResponseEntity<CertificationType> retrieveCertificationType(@RequestBody CertificationTypeRequest certificationTypeRequest) throws Exception {
        if (certificationTypeRequest.getSite() != null && !certificationTypeRequest.getSite().isEmpty()) {
            CertificationType retrieveCertificationType;
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(certificationTypeRequest.getSite()).hookPoint("PRE").activity("CertificationType-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(certificationTypeRequest)).build();
            String preExtensionResponse = certificationTypeService.callExtension(preExtension);
            CertificationTypeRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, CertificationTypeRequest.class);

            try {
                retrieveCertificationType = certificationTypeService.retrieveCertificationType(preExtensionRequest.getSite(), certificationTypeRequest.getCertificationType());
                Extension postExtension = Extension.builder().site(certificationTypeRequest.getSite()).hookPoint("POST").activity("CertificationType-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveCertificationType)).build();
                String postExtensionResponse = certificationTypeService.callExtension(postExtension);
                CertificationType postExtensionCertificationType = objectMapper.readValue(postExtensionResponse, CertificationType.class);
                return ResponseEntity.ok(postExtensionCertificationType);
            } catch (CertificationTypeException certificationTypeException) {
                throw certificationTypeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationTypeException(1);
    }

    //{
    //  "site": "rits",
    //  "certificationType": "c"
    //}

    //{
    //    "site": "rits"
    //}
    @PostMapping("retrieveAll")
    public ResponseEntity<CertificationTypeResponseList> getCertificationTypeList(@RequestBody CertificationTypeRequest certificationTypeRequest) {
        if (certificationTypeRequest.getSite() != null && !certificationTypeRequest.getSite().isEmpty()) {
            CertificationTypeResponseList getCertificationTypeList;
            try {
                getCertificationTypeList = certificationTypeService.getAllCertificationType(certificationTypeRequest.getSite(), certificationTypeRequest.getCertificationType());
                return ResponseEntity.ok(getCertificationTypeList);
            } catch (CertificationTypeException certificationTypeException) {
                throw certificationTypeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationTypeException(1);
    }

    //{
    //    "site": "rits"
    //}
    @PostMapping("retrieveTop50")
    public ResponseEntity<CertificationTypeResponseList> getCertificationTypeListByCreationDate(@RequestBody CertificationTypeRequest certificationTypeRequest) {
        if (certificationTypeRequest.getSite() != null && !certificationTypeRequest.getSite().isEmpty()) {
            CertificationTypeResponseList retrieveTop50CertificationType;
            try {
                retrieveTop50CertificationType = certificationTypeService.getAllCertificationTypeByCreatedDate(certificationTypeRequest.getSite());
                return ResponseEntity.ok(retrieveTop50CertificationType);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationTypeException(1);
    }

    //{
    //  "site": "rits",
    //  "certificationType": "certificationType1"
    //}
    @PostMapping("delete")
    public ResponseEntity<MessageModel> deleteCertificationType(@RequestBody CertificationTypeRequest certificationTypeRequest) throws Exception {
        if (certificationTypeRequest.getSite() != null && !certificationTypeRequest.getSite().isEmpty()) {
            MessageModel deleteResponse;
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(certificationTypeRequest.getSite()).hookPoint("PRE").activity("CertificationType-service").hookableMethod("delete").request(objectMapper.writeValueAsString(certificationTypeRequest)).build();
            String preExtensionResponse = certificationTypeService.callExtension(preExtension);
            CertificationTypeRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, CertificationTypeRequest.class);

            try {
                deleteResponse = certificationTypeService.deleteCertificationType(preExtensionRequest.getSite(), preExtensionRequest.getCertificationType());
//                Extension postExtension= Extension.builder().site(certificationTypeRequest.getSite()).hookPoint("POST").activity("CertificationType-service").hookableMethod("delete").request(objectMapper.writeValueAsString(deleteResponse)).build();
//                String postExtensionResponse=certificateService.callExtension(postExtension);
//                Certificate postExtensionCertificate=objectMapper.readValue(postExtensionResponse,Certificate.class);
//                return ResponseEntity.ok(postExtensionCertificate);

                return ResponseEntity.ok(deleteResponse);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationTypeException(1);
    }

    //{
    //  "site": "rits",
    //  "certificationType": "certificationType3"
    //}
    //


    //{
    //  "site": "rits"
    //}
    @PostMapping("getAvailableCertification")
    public ResponseEntity<List<Certification>> getAvailableCertification(@RequestBody CertificationTypeRequest certificationTypeRequest) {
        if (certificationTypeRequest.getSite() != null && !certificationTypeRequest.getSite().isEmpty()) {
            List<Certification> getAllCertificationType;
            try {
                getAllCertificationType = certificationTypeService.getAvailableCertification(certificationTypeRequest.getSite(), certificationTypeRequest.getCertificationType());
                return ResponseEntity.ok(getAllCertificationType);
            } catch (CertificationTypeException certificationTypeException) {
                throw certificationTypeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationTypeException(1);
    }

    //{
    //  "site": "rits",
    //  "certificationType": "certificationType3",
    //  "certificationList": [
    //    {
    //      "certification": "certificate3"
    //    }
    //  ]
    //}
    @PostMapping("add")
    public ResponseEntity<List<Certification>> assignCertification(@RequestBody CertificationTypeRequest certificationTypeRequest) {
        if (certificationTypeRequest.getSite() != null && !certificationTypeRequest.getSite().isEmpty()) {
            List<Certification> assignCertification;
            try {
                assignCertification = certificationTypeService.assignCertification(certificationTypeRequest.getSite(), certificationTypeRequest.getCertificationType(), certificationTypeRequest.getCertificationList());
                return ResponseEntity.ok(assignCertification);
            } catch (CertificationTypeException certificationTypeException) {
                throw certificationTypeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationTypeException(1);
    }

    //{
    //    "site": "rits",
    //    "certificationType": "certificationType3",
    //    "certificationList": [
    //        {
    //            "certification": "certificate2"
    //        }
    //    ]
    //}
    @PostMapping("remove")
    public ResponseEntity<List<Certification>> removeCertification(@RequestBody CertificationTypeRequest certificationTypeRequest) {
        if (certificationTypeRequest.getSite() != null && !certificationTypeRequest.getSite().isEmpty()) {
            List<Certification> removeCertification;
            try {
                removeCertification = certificationTypeService.removeCertification(certificationTypeRequest.getSite(), certificationTypeRequest.getCertificationType(), certificationTypeRequest.getCertificationList());
                return ResponseEntity.ok(removeCertification);
            } catch (CertificationTypeException certificationTypeException) {
                throw certificationTypeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationTypeException(1);
    }


}
