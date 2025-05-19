package com.rits.certificationtypeservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.certificationtypeservice.dto.*;
import com.rits.certificationtypeservice.exception.CertificationTypeException;
import com.rits.certificationtypeservice.model.Certification;
import com.rits.certificationtypeservice.model.CertificationType;
import com.rits.certificationtypeservice.model.MessageDetails;
import com.rits.certificationtypeservice.model.MessageModel;
import com.rits.certificationtypeservice.repository.CertificationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CertificationTypeServiceImpl implements CertificationTypeService {
    private final CertificationTypeRepository certificationTypeRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${certificate-service.url}/retrieveBySite")
    private String certificateUrl;


    @Override
    public MessageModel createCertificationType(CertificationTypeRequest certificationTypeRequest) throws Exception {
        if (certificationTypeRepository.existsByActiveAndSiteAndCertificationType(1, certificationTypeRequest.getSite(), certificationTypeRequest.getCertificationType())) {
            throw new CertificationTypeException(3301, certificationTypeRequest.getCertificationType());
        }
        if (certificationTypeRequest.getDescription() == null || certificationTypeRequest.getDescription().isEmpty()) {
            certificationTypeRequest.setDescription(certificationTypeRequest.getCertificationType());
        }
        CertificationType certificationType = CertificationType.builder()
                .handle("CertificationTypeBo:" + certificationTypeRequest.getSite() + "," + certificationTypeRequest.getCertificationType())
                .site(certificationTypeRequest.getSite())
                .certificationType(certificationTypeRequest.getCertificationType())
                .description(certificationTypeRequest.getDescription())
                .filterCertificationId(certificationTypeRequest.getFilterCertificationId())
                .filterDescription(certificationTypeRequest.getFilterDescription())
                .certificationList(certificationTypeRequest.getCertificationList())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .build();
        return MessageModel.builder().message_details(new MessageDetails(certificationTypeRequest.getCertificationType()+" Created SuccessFully","S")).response(certificationTypeRepository.save(certificationType)).build();
    }

    @Override
    public MessageModel updateCertificationType(CertificationTypeRequest certificationTypeRequest) throws Exception {
        if (certificationTypeRepository.existsByActiveAndSiteAndCertificationType(1, certificationTypeRequest.getSite(), certificationTypeRequest.getCertificationType())) {
            if (certificationTypeRequest.getDescription() == null || certificationTypeRequest.getDescription().isEmpty()) {
                certificationTypeRequest.setDescription(certificationTypeRequest.getCertificationType());
            }
            CertificationType existingCertificationType = certificationTypeRepository.findByActiveAndSiteAndCertificationType(1, certificationTypeRequest.getSite(), certificationTypeRequest.getCertificationType());
            CertificationType certificationType = CertificationType.builder()
                    .handle(existingCertificationType.getHandle())
                    .site(existingCertificationType.getSite())
                    .certificationType(existingCertificationType.getCertificationType())
                    .description(certificationTypeRequest.getDescription())
                    .filterCertificationId(certificationTypeRequest.getFilterCertificationId())
                    .filterDescription(certificationTypeRequest.getFilterDescription())
                    .certificationList(certificationTypeRequest.getCertificationList())
                    .active(1)
                    .createdDateTime(existingCertificationType.getCreatedDateTime())
                    .modifiedDateTime(LocalDateTime.now())
                    .build();

            return MessageModel.builder().message_details(new MessageDetails(certificationTypeRequest.getCertificationType()+" updated SuccessFully","S")).response(certificationTypeRepository.save(certificationType)).build();
        } else {
            throw new CertificationTypeException(3302, certificationTypeRequest.getCertificationType());
        }
    }

    @Override
    public CertificationType retrieveCertificationType(String site, String certificationType) throws Exception {
        CertificationType existingCertificationType = certificationTypeRepository.findByActiveAndSiteAndCertificationType(1, site, certificationType);
        if (existingCertificationType == null) {
            throw new CertificationTypeException(3302, certificationType);
        }
        return existingCertificationType;
    }

    @Override
    public CertificationTypeResponseList getAllCertificationType(String site, String certificationType) throws Exception {
        if (certificationType != null && !certificationType.isEmpty()) {
            List<CertificationTypeResponse> certificationTypeResponses = certificationTypeRepository.findByActiveAndSiteAndCertificationTypeContainingIgnoreCase(1, site, certificationType);
            if (certificationTypeResponses.isEmpty()) {
                throw new CertificationTypeException(3302, certificationType);
            }
            return CertificationTypeResponseList.builder().certificationTypeList(certificationTypeResponses).build();
        } else {
            return getAllCertificationTypeByCreatedDate(site);
        }
    }

    @Override
    public MessageModel deleteCertificationType(String site, String certificationType) throws Exception {

        CertificationType existingCertificationType = certificationTypeRepository.findByActiveAndSiteAndCertificationType(1, site, certificationType);
        if (existingCertificationType == null) {
           throw new CertificationTypeException(3302,certificationType);
        }
        existingCertificationType.setActive(0);
        existingCertificationType.setModifiedDateTime(LocalDateTime.now());
        certificationTypeRepository.save(existingCertificationType);
        return MessageModel.builder().message_details(new MessageDetails(certificationType+" deleted SuccessFull","S")).build();

    }

    @Override
    public CertificationTypeResponseList getAllCertificationTypeByCreatedDate(String site) throws Exception {
        List<CertificationTypeResponse> certificationTypeResponses = certificationTypeRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return CertificationTypeResponseList.builder().certificationTypeList(certificationTypeResponses).build();
    }

    @Override
    public List<Certification> getAvailableCertification(String site, String certificationType) throws Exception {
        CertificationTypeRequest request = CertificationTypeRequest.builder().site(site).build();
        CertificateResponse certificateResponse = webClientBuilder.build()
                .post()
                .uri(certificateUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CertificateResponse.class)
                .block();
        if (certificateResponse == null) {
            throw new CertificationTypeException(700);
        }
        List<Certification> certifications = certificateResponse.getCertificationList();
        if (certificationType != null && !certificationType.isEmpty()) {
            CertificationType existingCertificationType = certificationTypeRepository.findByActiveAndSiteAndCertificationType(1, site, certificationType);
            if (existingCertificationType == null) {
                throw new CertificationTypeException(3302, certificationType);
            }
            List<Certification> existingCertification = existingCertificationType.getCertificationList();
            if (existingCertification != null && !existingCertification.isEmpty()) {
                certifications.removeIf(certificate -> existingCertification.stream().anyMatch(certification -> certification.getCertification().equals(certificate.getCertification())));
                return certifications;
            }

        }
        return certifications;
    }

    @Override
    public List<Certification> assignCertification(String site, String certificationType, List<Certification> certifications) throws Exception {
        CertificationType existingCertificationType = certificationTypeRepository.findByActiveAndSiteAndCertificationType(1, site, certificationType);
        if (existingCertificationType == null) {
            throw new CertificationTypeException(3302, certificationType);
        }
        List<Certification> existingCertification = existingCertificationType.getCertificationList();
        if (existingCertification.isEmpty()) {
            existingCertification.addAll(certifications);
        } else {
            for (Certification certification : certifications) {
                boolean alreadyExists = existingCertification.stream().anyMatch(addCertificate -> Objects.equals(addCertificate.getCertification(), certification.getCertification()));
                if (!alreadyExists) {
                    existingCertification.add(certification);
                }
            }
        }
        existingCertificationType.setCertificationList(existingCertification);
        existingCertificationType.setModifiedDateTime(LocalDateTime.now());
        certificationTypeRepository.save(existingCertificationType);
        return existingCertification;
    }

    @Override
    public List<Certification> removeCertification(String site, String certificationType, List<Certification> certifications) throws Exception {
        CertificationType existingCertificationType = certificationTypeRepository.findByActiveAndSiteAndCertificationType(1, site, certificationType);
        if (existingCertificationType == null) {
            throw new CertificationTypeException(3302, certificationType);
        }
        List<Certification> existingCertification = existingCertificationType.getCertificationList();
        if (existingCertification.isEmpty()) {
            return null;
        }
        for (Certification certification : certifications) {
            existingCertification.removeIf(existingCertificate -> existingCertificate.getCertification().equals(certification.getCertification()));
        }
        existingCertificationType.setCertificationList(existingCertification);
        existingCertificationType.setModifiedDateTime(LocalDateTime.now());
        certificationTypeRepository.save(existingCertificationType);
        return existingCertification;
    }

    @Override
    public String callExtension(Extension extension) throws Exception {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new CertificationTypeException(800);
        }
        return extensionResponse;
    }
}
