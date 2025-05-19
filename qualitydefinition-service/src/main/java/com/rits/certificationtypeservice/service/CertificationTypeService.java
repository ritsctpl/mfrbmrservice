package com.rits.certificationtypeservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.certificationtypeservice.dto.CertificationTypeRequest;
import com.rits.certificationtypeservice.dto.CertificationTypeResponseList;
import com.rits.certificationtypeservice.dto.Extension;
import com.rits.certificationtypeservice.model.Certification;
import com.rits.certificationtypeservice.model.CertificationType;
import com.rits.certificationtypeservice.model.MessageModel;

import java.util.List;

public interface CertificationTypeService {
    public MessageModel createCertificationType(CertificationTypeRequest certificationTypeRequest) throws Exception;

    public MessageModel updateCertificationType(CertificationTypeRequest certificationTypeRequest) throws Exception;

    public CertificationType retrieveCertificationType(String site, String certificationType) throws Exception;

    public CertificationTypeResponseList getAllCertificationType(String site, String certificationType) throws Exception;

    public MessageModel deleteCertificationType(String site, String certificationType) throws Exception;

    public CertificationTypeResponseList getAllCertificationTypeByCreatedDate(String site) throws Exception;

    public List<Certification> getAvailableCertification(String site, String certificationType) throws Exception;

    public List<Certification> assignCertification(String site, String certificationType, List<Certification> certifications) throws Exception;

    public List<Certification> removeCertification(String site, String certificationType, List<Certification> certifications) throws Exception;

    public String callExtension(Extension extension) throws Exception;

}
