package com.rits.certificationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.certificationservice.dto.CertificationRequest;
import com.rits.certificationservice.dto.CertificationResponseList;
import com.rits.certificationservice.dto.Extension;
import com.rits.certificationservice.exception.CertificationException;
import com.rits.certificationservice.model.Certification;
import com.rits.certificationservice.model.MessageModel;
import com.rits.certificationservice.model.UserGroup;
import com.rits.certificationservice.service.CertificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/certificate-service")
public class CertificationController {
    private final CertificationService certificationService;
    private final ObjectMapper objectMapper;


    //{
    //    "site": "rits",
    //    "certificate": "certificate1",
    //    "description": "Example certificate request",
    //    "durationType": "1 year",
    //    "status": "active",
    //    "maxNumberOfExtensions": "2",
    //    "maxExtensionDuration": "6 months",
    //    "userGroupList": [
    //        {
    //            "userGroup": "admin"
    //        },
    //        {
    //            "userGroup": "user"
    //        }
    //    ],
    //    "customDataList": [
    //        {
    //            "customData": "customField1",
    //            "value": "customValue1"
    //        },
    //        {
    //            "customData": "customField2",
    //            "value": "customValue2"
    //        }
    //    ]
    //}


    //{
    //    "site": "rits",
    //    "certificate": "certificate2",
    //    "description": "Example certificate request",
    //    "status":"active"
    //
    //}

    //site,certificate,duration
    @PostMapping("create")
    public ResponseEntity<?> createCertification(@RequestBody CertificationRequest certificationRequest) throws Exception {

//        MessageModel validationResponse = certificationService.validation( payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        CertificationRequest certificationRequest = new ObjectMapper().convertValue(payload, CertificationRequest.class);
            MessageModel createCertification;

            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(certificationRequest.getSite()).hookPoint("PRE").activity("certificate-service").hookableMethod("create").request(objectMapper.writeValueAsString(certificationRequest)).build();
            String preExtensionResponse = certificationService.callExtension(preExtension);
            CertificationRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, CertificationRequest.class);

            try {
                createCertification = certificationService.createCertificate(preExtensionRequest);
                Extension postExtension = Extension.builder().site(certificationRequest.getSite()).hookPoint("POST").activity("certificate-service").hookableMethod("create").request(objectMapper.writeValueAsString(createCertification.getResponse())).build();
                String postExtensionResponse = certificationService.callExtension(postExtension);
                Certification postExtensionCertificate = objectMapper.readValue(postExtensionResponse, Certification.class);
                return ResponseEntity.ok( MessageModel.builder().message_details(createCertification.getMessage_details()).response(postExtensionCertificate).build());
            } catch (CertificationException certificationException) {
                throw certificationException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    //{
    //    "site": "rits",
    //    "certificate": "certificate2",
    //    "description": "Example certificate request",
    //    "status":"active"
    //
    //}


    //{
    //    "site": "rits",
    //    "certificate": "certificate1",
    //    "description": "Example certificate request",
    //    "durationType": "1 year",
    //    "status": "active",
    //    "maxNumberOfExtensions": "2",
    //    "maxExtensionDuration": "6 months",
    //    "userGroupList": [
    //        {
    //            "userGroup": "admin"
    //        },
    //        {
    //            "userGroup": "user"
    //        }
    //    ],
    //    "customDataList": [
    //        {
    //            "customData": "customField1",
    //            "value": "customValue1"
    //        },
    //        {
    //            "customData": "customField2",
    //            "value": "customValue2"
    //        }
    //    ]
    //}
    @PostMapping("update")
    public ResponseEntity<MessageModel> updateCertification(@RequestBody CertificationRequest certificationRequest) throws Exception {

            MessageModel updateCertification;
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(certificationRequest.getSite()).hookPoint("PRE").activity("certificate-service").hookableMethod("update").request(objectMapper.writeValueAsString(certificationRequest)).build();
            String preExtensionResponse = certificationService.callExtension(preExtension);
            CertificationRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, CertificationRequest.class);

            try {
                updateCertification = certificationService.updateCertificate(preExtensionRequest);
                Extension postExtension = Extension.builder().site(certificationRequest.getSite()).hookPoint("POST").activity("certificate-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateCertification.getResponse())).build();
                String postExtensionResponse = certificationService.callExtension(postExtension);
                Certification postExtensionCertificate = objectMapper.readValue(postExtensionResponse, Certification.class);
                return ResponseEntity.ok( MessageModel.builder().message_details(updateCertification.getMessage_details()).response(postExtensionCertificate).build());

            } catch (CertificationException certificationException) {
                throw certificationException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }



    //{
    //    "site":"rits",
    //    "certificate":"certificate1"
    //}
    @PostMapping("retrieve")
    public ResponseEntity<Certification> retrieveCertification(@RequestBody CertificationRequest certificationRequest) throws Exception {
        if (certificationRequest.getSite() != null && !certificationRequest.getSite().isEmpty()) {
            Certification retrieveCertification;
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(certificationRequest.getSite()).hookPoint("PRE").activity("certificate-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(certificationRequest)).build();
            String preExtensionResponse = certificationService.callExtension(preExtension);
            CertificationRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, CertificationRequest.class);

            try {
                retrieveCertification = certificationService.retrieveCertificate(preExtensionRequest.getSite(), preExtensionRequest.getCertification());
                Extension postExtension = Extension.builder().site(certificationRequest.getSite()).hookPoint("POST").activity("certificate-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveCertification)).build();
                String postExtensionResponse = certificationService.callExtension(postExtension);
                Certification postExtensionCertificate = objectMapper.readValue(postExtensionResponse, Certification.class);
                return ResponseEntity.ok(postExtensionCertificate);
            } catch (CertificationException certificationException) {
                throw certificationException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationException(1);
    }

    //{
    //    "site":"rits",
    //    "certificate":"c"
    //}

    //{
    //    "site":"rits",
    //    "certificate":""
    //}
    @PostMapping("retrieveAll")
    public ResponseEntity<CertificationResponseList> getCertificateList(@RequestBody CertificationRequest certificationRequest) {
        if (certificationRequest.getSite() != null && !certificationRequest.getSite().isEmpty()) {
            CertificationResponseList retrieveAllCertificates;
            try {
                retrieveAllCertificates = certificationService.getCertificateList(certificationRequest.getSite(), certificationRequest.getCertification());
                return ResponseEntity.ok(retrieveAllCertificates);
            } catch (CertificationException certificationException) {
                throw certificationException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationException(1);
    }


    //{
    //    "site":"rits"
    //}
    @PostMapping("retrieveTop50")
    public ResponseEntity<CertificationResponseList> getCertificateListByCreationDate(@RequestBody CertificationRequest certificationRequest) {
        if (certificationRequest.getSite() != null && !certificationRequest.getSite().isEmpty()) {
            CertificationResponseList retrieveTop50Certificates;
            try {
                retrieveTop50Certificates = certificationService.getCertificateListByCreationDate(certificationRequest.getSite());
                return ResponseEntity.ok(retrieveTop50Certificates);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationException(1);
    }
    //{
    //    "site":"RITS",
    //    "certificate":"certificate"
    //}

    @PostMapping("delete")
    public ResponseEntity<MessageModel> deleteCertificate(@RequestBody CertificationRequest certificationRequest) throws Exception {
        if (certificationRequest.getSite() != null && !certificationRequest.getSite().isEmpty()) {
            MessageModel deleteResponse;
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(certificationRequest.getSite()).hookPoint("PRE").activity("certificate-service").hookableMethod("delete").request(objectMapper.writeValueAsString(certificationRequest)).build();
            String preExtensionResponse = certificationService.callExtension(preExtension);
            CertificationRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, CertificationRequest.class);

            try {
                deleteResponse = certificationService.deleteCertificate(preExtensionRequest.getSite(), preExtensionRequest.getCertification(),preExtensionRequest.getUserId());
//                Extension postExtension= Extension.builder().site(certificateRequest.getSite()).hookPoint("POST").activity("certificate-service").hookableMethod("delete").request(objectMapper.writeValueAsString(deleteResponse)).build();
//                String postExtensionResponse=certificateService.callExtension(postExtension);
//                Certificate postExtensionCertificate=objectMapper.readValue(postExtensionResponse,Certificate.class);
//                return ResponseEntity.ok(postExtensionCertificate);

                return ResponseEntity.ok(deleteResponse);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationException(1);
    }


    //{
    //    "site":"rits"
    //}
    @PostMapping("activeCertificate")
    public ResponseEntity<CertificationResponseList> getActiveCertificate(@RequestBody CertificationRequest certificationRequest) {
        if (certificationRequest.getSite() != null && !certificationRequest.getSite().isEmpty()) {
            CertificationResponseList getActiveCertificate;
            try {
                getActiveCertificate = certificationService.getActiveCertificate(certificationRequest.getSite());
                return ResponseEntity.ok(getActiveCertificate);
            } catch (CertificationException e) {
                throw new CertificationException(702, certificationRequest.getCertification());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationException(1);
    }

    //{
    //    "site":"rits"
    //}
    @PostMapping("retrieveBySite")
    public ResponseEntity<CertificationResponseList> getAllCertificates(@RequestBody CertificationRequest certificationRequest) {
        if (certificationRequest.getSite() != null && !certificationRequest.getSite().isEmpty()) {
            CertificationResponseList getAllCertificates;
            try {
                getAllCertificates = certificationService.getAllCertificates(certificationRequest.getSite());
                return ResponseEntity.ok(getAllCertificates);
            } catch (CertificationException certificationException) {
                throw certificationException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationException(1);
    }
    //{
    //    "site":"rits"
    //}


    //{
    //    "site":"rits",
    //    "certification":"certificate"
    //}
    @PostMapping("getAvailableUserGroup")
    public ResponseEntity<List<UserGroup>> getAvailableUserGroup(@RequestBody CertificationRequest certificationRequest) {
        if (certificationRequest.getSite() != null && !certificationRequest.getSite().isEmpty()) {
            List<UserGroup> getAvailableUserGroup;
            try {
                getAvailableUserGroup = certificationService.getAvailableUserGroup(certificationRequest.getSite(), certificationRequest.getCertification());
                return ResponseEntity.ok(getAvailableUserGroup);
            } catch (CertificationException certificationException) {
                throw certificationException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationException(1);
    }

    //{
    //    "site": "rits",
    //    "certification": "certificate",
    //    "userGroupList": [
    //        {
    //            "userGroup": "userGroup2"
    //        }
    //    ]
    //}
    @PostMapping("add")
    public ResponseEntity<List<UserGroup>> addUserGroup(@RequestBody CertificationRequest certificationRequest) {
        if (certificationRequest.getSite() != null && !certificationRequest.getSite().isEmpty()) {
            List<UserGroup> addUserGroup;
            try {
                addUserGroup = certificationService.addUserGroup(certificationRequest.getSite(), certificationRequest.getCertification(), certificationRequest.getUserGroupList());
                return ResponseEntity.ok(addUserGroup);
            } catch (CertificationException certificationException) {
                throw certificationException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationException(1);
    }

    //{
    //    "site": "rits",
    //    "certification": "certificate",
    //    "userGroupList": [
    //        {
    //            "userGroup": "userGroup2"
    //        }
    //
    //    ]
    //}
    @PostMapping("remove")
    public ResponseEntity<List<UserGroup>> removeUserGroup(@RequestBody CertificationRequest certificationRequest) {
        if (certificationRequest.getSite() != null && !certificationRequest.getSite().isEmpty()) {
            List<UserGroup> removeUserGroup;
            try {
                removeUserGroup = certificationService.removeUserGroup(certificationRequest.getSite(), certificationRequest.getCertification(), certificationRequest.getUserGroupList());
                return ResponseEntity.ok(removeUserGroup);
            } catch (CertificationException certificationException) {
                throw certificationException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationException(1);
    }
    @PostMapping("isExist")
    public ResponseEntity<Boolean> isExist(@RequestBody CertificationRequest certificationRequest) {
        if (certificationRequest.getSite() != null && !certificationRequest.getSite().isEmpty()) {
           boolean isExist;
            try {
                isExist = certificationService.isExist(certificationRequest.getSite(), certificationRequest.getCertification());
                return ResponseEntity.ok(isExist);
            } catch (CertificationException certificationException) {
                throw certificationException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CertificationException(1);
    }
}
