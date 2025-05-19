package com.rits.resourcetypeservice.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.resourcetypeservice.Model.MessageModel;
import com.rits.resourcetypeservice.dto.*;
import com.rits.resourcetypeservice.Model.ResourceType;

import java.util.List;

public interface ResourceTypeService {
    public List<ResourceTypeList> getLast50RecordsOrderByCreatedDate(String site) throws Exception;

    public MessageModel createResourceType(ResourceTypeRequest resourceTypeRequest) throws Exception;

    public MessageModel update(ResourceTypeRequest resourceTypeRequest) throws Exception;
    public ResourceType retrieveByResourceType(String resourceType,String site) throws Exception;

    public boolean isResourceTypeExist(String resourceType,String site) throws Exception;

    public Response deleteResourceType(String resourceType, String site) throws Exception;

    public List<AvailableResourceType> retrieveAllResourceType(String site) throws Exception;

    public List<ResourceTypeList> retrieveResourceTypeList(String site,String resourceType) throws Exception;

    public Boolean removeResourceFromResourceType(String resourceType,List<String> resource,String site) throws Exception;

    public Boolean associateResourceToResourceType(String resourceType,  List<String> resource,String site) throws Exception;

    public AvailableResource availableResources(ResourceTypeRequest resourceTypeRequest) throws Exception;
    public String callExtension(Extension extension);

}
