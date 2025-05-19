package com.rits.certificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.buyoffservice.dto.AuditLogRequest;
import com.rits.certificationservice.dto.*;
import com.rits.certificationservice.exception.CertificationException;
import com.rits.certificationservice.model.Certification;
import com.rits.certificationservice.model.MessageDetails;
import com.rits.certificationservice.model.MessageModel;
import com.rits.certificationservice.model.UserGroup;
import com.rits.certificationservice.repository.CertificationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements CertificationService {
    private final CertificationRepository certificationRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${usergroup-service.url}/getAvailableUserGroup")
    private String userGroupUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;
    @Override
    public MessageModel createCertificate(CertificationRequest certificationRequest) throws Exception {
        if (certificationRepository.existsByActiveAndSiteAndCertification(1, certificationRequest.getSite(), certificationRequest.getCertification())) {
            throw new CertificationException(701, certificationRequest.getCertification());
        }
        String duration = certificationRequest.getDuration();
        duration = duration.replaceAll(" Days","");
        duration = duration.replaceAll(" Weeks","");
        duration = duration.replaceAll(" Months","");
        duration = duration.replaceAll(" Years","");
        if(StringUtils.isEmpty(duration))
        {
            throw new CertificationException(1008);
        }
        if(duration.equals("0"))
        {
            throw new CertificationException(1009);
        }
        if (certificationRequest.getDescription() == null || certificationRequest.getDescription().isEmpty()) {
            certificationRequest.setDescription(certificationRequest.getCertification());
        }
        Certification certification = Certification.builder()
                .handle("CertificateBo:" + certificationRequest.getSite() + "," + certificationRequest.getCertification())
                .site(certificationRequest.getSite())
                .certification(certificationRequest.getCertification())
                .description(certificationRequest.getDescription())
                .status(certificationRequest.getStatus())
                .duration(certificationRequest.getDuration())
                .durationType(certificationRequest.getDurationType())
                .maxNumberOfExtensions(certificationRequest.getMaxNumberOfExtensions())
                .maxExtensionDuration(certificationRequest.getMaxExtensionDuration())
                .userGroupList(certificationRequest.getUserGroupList())
                .customDataList(certificationRequest.getCustomDataList())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .build();
        AuditLogRequest activityLog = AuditLogRequest.builder()
                .site(certificationRequest.getSite())
                .change_stamp("Create")
                .action_code("CERTIFICATION-CREATE")
                .action_detail("Certification Created "+certificationRequest.getCertification())
                .action_detail_handle("ActionDetailBO:"+certificationRequest.getSite()+","+"CERTIFICATION-CREATE"+","+certificationRequest.getUserId()+":"+"com.rits.certificationservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(certificationRequest.getUserId())
                .txnId("CERTIFICATION-CREATE"+String.valueOf(LocalDateTime.now())+certificationRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("CERTIFICATION")
                .build();

        webClientBuilder.build()
                .post()
                .uri(auditlogUrl)
                .bodyValue(activityLog)
                .retrieve()
                .bodyToMono(AuditLogRequest.class)
                .block();
        return MessageModel.builder().message_details(new MessageDetails(certificationRequest.getCertification()+" Created SuccessFully","S")).response(certificationRepository.save(certification)).build();

    }

    @Override
    public MessageModel updateCertificate(CertificationRequest certificationRequest) throws Exception {
        if (!certificationRepository.existsByActiveAndSiteAndCertification(1, certificationRequest.getSite(), certificationRequest.getCertification())) {
            throw new CertificationException(702, certificationRequest.getCertification());
        }
        if (certificationRequest.getDescription() == null || certificationRequest.getDescription().isEmpty()) {
            certificationRequest.setDescription(certificationRequest.getCertification());
        }
        String duration = certificationRequest.getDuration();
        duration = duration.replaceAll(" Days","");
        duration = duration.replaceAll(" Weeks","");
        duration = duration.replaceAll(" Months","");
        duration = duration.replaceAll(" Years","");
        if(StringUtils.isEmpty(duration))
        {
            throw new CertificationException(1008);
        }
        if(duration.equals("0"))
        {
            throw new CertificationException(1009);
        }
        Certification existingCertification = certificationRepository.findByActiveAndSiteAndCertification(1, certificationRequest.getSite(), certificationRequest.getCertification());
        Certification certification = Certification.builder()
                .handle(existingCertification.getHandle())
                .site(existingCertification.getSite())
                .certification(existingCertification.getCertification())
                .description(certificationRequest.getDescription())
                .status(certificationRequest.getStatus())
                .duration(certificationRequest.getDuration())
                .durationType(certificationRequest.getDurationType())
                .maxNumberOfExtensions(certificationRequest.getMaxNumberOfExtensions())
                .maxExtensionDuration(certificationRequest.getMaxExtensionDuration())
                .userGroupList(certificationRequest.getUserGroupList())
                .customDataList(certificationRequest.getCustomDataList())
                .active(1)
                .createdDateTime(existingCertification.getCreatedDateTime())
                .modifiedDateTime(LocalDateTime.now())
                .build();
        AuditLogRequest activityLog = AuditLogRequest.builder()
                .site(certificationRequest.getSite())
                .change_stamp("Update")
                .action_code("CERTIFICATION-UPDATE")
                .action_detail("Certification Updated "+certificationRequest.getCertification())
                .action_detail_handle("ActionDetailBO:"+certificationRequest.getSite()+","+"CERTIFICATION-UPDATE"+","+certificationRequest.getUserId()+":"+"com.rits.certificationservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(certificationRequest.getUserId())
                .txnId("CERTIFICATION-UPDATE"+String.valueOf(LocalDateTime.now())+certificationRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("CERTIFICATION")
                .build();

        webClientBuilder.build()
                .post()
                .uri(auditlogUrl)
                .bodyValue(activityLog)
                .retrieve()
                .bodyToMono(AuditLogRequest.class)
                .block();
        return MessageModel.builder().message_details(new MessageDetails(certificationRequest.getCertification()+" updated SuccessFully","S")).response(certificationRepository.save(certification)).build();
    }

    @Override
    public Certification retrieveCertificate(String site, String certification) throws Exception {
        Certification existingCertificate = certificationRepository.findByActiveAndSiteAndCertification(1, site, certification);
        if (existingCertificate == null) {
            throw new CertificationException(702, certification);
        }
        return existingCertificate;
    }

    @Override
    public MessageModel deleteCertificate(String site, String certification,String userId) throws Exception {

        Certification existingCertificate = certificationRepository.findByActiveAndSiteAndCertification(1, site, certification);
        if (existingCertificate == null) {
            throw new CertificationException(702,certification);
        }
        existingCertificate.setActive(0);
        existingCertificate.setModifiedDateTime(LocalDateTime.now());
        certificationRepository.save(existingCertificate);
        AuditLogRequest activityLog = AuditLogRequest.builder()
                .site(site)
                .change_stamp("Delete")
                .action_code("CERTIFICATION-DELETE")
                .action_detail("Certification Deleted "+certification)
                .action_detail_handle("ActionDetailBO:"+site+","+"CERTIFICATION-DELETE"+","+userId+":"+"com.rits.certificationservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(userId)
                .txnId("CERTIFICATION-DELETE"+String.valueOf(LocalDateTime.now())+userId)
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("CERTIFICATION")
                .build();

        webClientBuilder.build()
                .post()
                .uri(auditlogUrl)
                .bodyValue(activityLog)
                .retrieve()
                .bodyToMono(AuditLogRequest.class)
                .block();
        return MessageModel.builder().message_details(new MessageDetails(certification+" deleted SuccessFully","S")).build();

    }

    @Override
    public CertificationResponseList getCertificateList(String site, String certification) throws Exception {
        if (certification != null && !certification.isEmpty()) {
            List<CertificationResponse> certificationResponses = certificationRepository.findByActiveAndSiteAndCertificationContainingIgnoreCase(1, site, certification);
            if (certificationResponses.isEmpty()) {
                throw new CertificationException(702, certification);
            }
            return CertificationResponseList.builder().certificationList(certificationResponses).build();
        } else {
            return getCertificateListByCreationDate(site);
        }
    }

    @Override
    public CertificationResponseList getCertificateListByCreationDate(String site) throws Exception {
        List<CertificationResponse> certificationResponses = certificationRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return CertificationResponseList.builder().certificationList(certificationResponses).build();
    }

    @Override
    public CertificationResponseList getActiveCertificate(String site) throws Exception {
        List<CertificationResponse> certificationResponses = certificationRepository.findByActiveAndSiteAndStatus(1, site, "active");
        if (certificationResponses.isEmpty()) {
            throw new CertificationException(702, "ActiveCertificate");

        }
        return CertificationResponseList.builder().certificationList(certificationResponses).build();
    }

    @Override
    public CertificationResponseList getAllCertificates(String site) throws Exception {
        List<CertificationResponse> certificationResponse = certificationRepository.findByActiveAndSite(1, site);
        return CertificationResponseList.builder().certificationList(certificationResponse).build();
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
            throw new CertificationException(800);
        }
        return extensionResponse;
    }

    @Override
    public List<UserGroup> getAvailableUserGroup(String site, String certification) throws Exception {
        CertificationRequest request = CertificationRequest.builder().site(site).build();
        List<UserGroup> userGroupResponse = webClientBuilder.build()
                .post()
                .uri(userGroupUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UserGroup>>() {
                })
                .block();
        if (userGroupResponse == null) {
            throw new CertificationException(3000);
        }

        if (certification != null && !certification.isEmpty()) {
            Certification existingCertification = certificationRepository.findByActiveAndSiteAndCertification(1, site, certification);
            if (existingCertification == null) {
                throw new CertificationException(702, certification);
            }
            List<UserGroup> existingUserGroup = existingCertification.getUserGroupList();
            if (existingUserGroup != null && !existingUserGroup.isEmpty()) {
                userGroupResponse.removeIf(userGroup -> existingUserGroup.stream().anyMatch(userGroups -> userGroups.getUserGroup().equals(userGroup.getUserGroup())));
                return userGroupResponse;
            }

        }
        return userGroupResponse;
    }

    @Override
    public List<UserGroup> addUserGroup(String site, String certification, List<UserGroup> userGroups) throws Exception {
        Certification existingCertification = certificationRepository.findByActiveAndSiteAndCertification(1, site, certification);
        if (existingCertification == null) {
            throw new CertificationException(702, certification);
        }
        List<UserGroup> existingUserGroups = existingCertification.getUserGroupList();
        if (existingUserGroups.isEmpty()) {
            existingUserGroups.addAll(userGroups);
        } else {
            for (UserGroup userGroup : userGroups) {
                boolean alreadyExists = existingUserGroups.stream().anyMatch(addUserGroups -> Objects.equals(addUserGroups.getUserGroup(), userGroup.getUserGroup()));
                if (!alreadyExists) {
                    existingUserGroups.add(userGroup);
                }
            }
        }
        existingCertification.setUserGroupList(existingUserGroups);
        existingCertification.setModifiedDateTime(LocalDateTime.now());
        certificationRepository.save(existingCertification);
        return existingUserGroups;
    }


    @Override
    public List<UserGroup> removeUserGroup(String site, String certification, List<UserGroup> userGroupList) throws Exception {
        Certification existingCertification = certificationRepository.findByActiveAndSiteAndCertification(1, site, certification);
        if (existingCertification == null) {
            throw new CertificationException(702, certification);
        }
        List<UserGroup> existingUserGroups = existingCertification.getUserGroupList();
        if (existingUserGroups.isEmpty()) {
            return null;
        }
        for (UserGroup userGroup : userGroupList) {
            existingUserGroups.removeIf(existingUserGroup -> existingUserGroup.getUserGroup().equals(userGroup.getUserGroup()));
        }
        existingCertification.setUserGroupList(existingUserGroups);
        existingCertification.setModifiedDateTime(LocalDateTime.now());
        certificationRepository.save(existingCertification);
        return existingUserGroups;
    }

    @Override
    public boolean isExist(String site, String certification) throws Exception {
        return certificationRepository.existsByActiveAndSiteAndCertification(1,site,certification);
    }
}
