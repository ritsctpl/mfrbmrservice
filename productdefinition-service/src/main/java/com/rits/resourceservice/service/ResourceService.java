package com.rits.resourceservice.service;

import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.resourceservice.dto.*;
import com.rits.resourceservice.model.CheckStatusRequest;
import com.rits.resourceservice.model.MessageModel;
import com.rits.resourceservice.model.Resource;

import java.util.List;

public interface ResourceService {
    MessageModel createResource(ResourceRequest resource) throws Exception;

    List<ResourceListResponse> retrieveTop50Resource(String site) throws Exception;

    Resource retrieveResource(String site, String resource) throws Exception;

    Resource retrieveBySiteAndErpEquipmentNumber(String site, String erpEquipmentNumber) throws Exception;

    boolean isResourceExist(String site, String resource) throws Exception;

    boolean isResourceExistByHandle(String site, String resource) throws Exception;

    MessageModel deleteResource(String site, String resource, String userId) throws Exception;

    MessageModel updateResource(ResourceRequest resourceRequest) throws Exception;

    List<ResourceListResponse> retrieveResourceList(String site, String resource) throws Exception;

    Boolean associateResourceTypeToResource(AssociateResourceToResourceType associate) throws Exception;

    Boolean removeResourceFromResourceType(AssociateResourceToResourceType associate) throws Exception;

    AvailableResourceTypeList availableResourceType(String site, String resource) throws Exception;

    String callExtension(Extension extension);
    MessageModel checkSetUpStatusAndCallProductionLog(CheckStatusRequest checkStatusRequest) throws Exception;

    List<ResourceListResponse> getAllResponse(String site) throws Exception;

    AuditLogRequest createAuditLog(ResourceRequest resourceRequest);

    AuditLogRequest updateAuditLog(ResourceRequest resourceRequest);

    AuditLogRequest deleteAuditLog(ResourceRequest resourceRequest);

}
