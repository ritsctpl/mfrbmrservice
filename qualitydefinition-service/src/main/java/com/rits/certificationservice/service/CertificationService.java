package com.rits.certificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.certificationservice.dto.CertificationRequest;
import com.rits.certificationservice.dto.CertificationResponseList;
import com.rits.certificationservice.dto.Extension;
import com.rits.certificationservice.model.Certification;
import com.rits.certificationservice.model.MessageModel;
import com.rits.certificationservice.model.UserGroup;

import java.util.List;

public interface CertificationService {
    public MessageModel createCertificate(CertificationRequest certificationRequest) throws Exception;

    public MessageModel updateCertificate(CertificationRequest certificationRequest) throws Exception;

    public Certification retrieveCertificate(String site, String certification) throws Exception;

    public MessageModel deleteCertificate(String site, String certification,String userId) throws Exception;

    public CertificationResponseList getCertificateList(String site, String certification) throws Exception;

    public CertificationResponseList getCertificateListByCreationDate(String site) throws Exception;

    public CertificationResponseList getActiveCertificate(String site) throws Exception;

    public CertificationResponseList getAllCertificates(String site) throws Exception;

    public String callExtension(Extension extension) throws Exception;

    public List<UserGroup> getAvailableUserGroup(String site, String certification) throws Exception;

    public List<UserGroup> addUserGroup(String site, String certification, List<UserGroup> userGroupList) throws Exception;

    public List<UserGroup> removeUserGroup(String site, String certification, List<UserGroup> userGroupList) throws Exception;
    public boolean isExist(String site, String certification )throws Exception;

}
