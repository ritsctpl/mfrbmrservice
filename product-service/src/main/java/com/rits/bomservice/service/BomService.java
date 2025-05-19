package com.rits.bomservice.service;


import com.rits.bomservice.dto.BomComponentList;
import com.rits.bomservice.dto.BomRequest;
import com.rits.bomservice.dto.BomResponseList;
import com.rits.bomservice.dto.Extension;
import com.rits.bomservice.model.Bom;
import com.rits.bomservice.model.BomMessageModel;
import com.rits.containermaintenanceservice.dto.AuditLogRequest;

public interface BomService {
    BomResponseList getBomListByCreationDate(String site) throws Exception;

    BomResponseList getBomList(String bom, String site) throws Exception;

    BomMessageModel createBom(BomRequest bomRequest) throws Exception;

    BomMessageModel deleteBom(String bom, String revision, String site, String userId) throws Exception;

    boolean isBomUsed(String bom, String revision, String site) throws Exception;

    BomMessageModel updateBom(BomRequest bomRequest) throws Exception;

    BomComponentList getComponentListByOperation(String bom, String revision, String site, String operation) throws Exception;

    BomResponseList componentUsage(String component, String version, String site) throws Exception;

    Boolean isBomExist(String bom, String revision, String site) throws Exception;

    Bom retrieveBom(String bom, String revision, String site) throws Exception;

    String callExtension(Extension extension) throws Exception;

    BomResponseList retrieveByBomTypeAndSite(String site, String bomType) throws Exception;

    AuditLogRequest createAuditLog(BomRequest bomRequest);

    AuditLogRequest deleteAuditLog(BomRequest bomRequest);

    AuditLogRequest updateAuditLog(BomRequest bomRequest);
}

