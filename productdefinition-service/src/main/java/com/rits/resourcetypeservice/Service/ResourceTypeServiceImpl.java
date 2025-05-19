package com.rits.resourcetypeservice.Service;

import com.rits.kafkaservice.ProducerEvent;
import  com.rits.operationservice.dto.AuditLogRequest;
import com.rits.resourcetypeservice.Exception.*;
import com.rits.resourcetypeservice.Model.MessageDetails;
import com.rits.resourcetypeservice.Model.MessageModel;
import com.rits.resourcetypeservice.dto.*;
import com.rits.resourcetypeservice.Model.ResourceMemberList;
import com.rits.resourcetypeservice.Model.ResourceType;
import com.rits.resourcetypeservice.Repository.ResourceTypeRepository;
import com.rits.workcenterservice.exception.WorkCenterException;
import com.rits.workcenterservice.model.Association;
import com.rits.workcenterservice.model.WorkCenter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ResourceTypeServiceImpl implements ResourceTypeService {

    private final ResourceTypeRepository resourceTypeRepository;

    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${resource-service.url}/retrieveResourceList")
    private String resourceServiceUrl;

    @Value("${resource-service.url}/add")
    private String associateResourceTypeToResource;

    @Value("${resource-service.url}/remove")
    private String removeResourceFromResourceType;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;
    @Override
    public List<ResourceTypeList> getLast50RecordsOrderByCreatedDate(String site) throws Exception{
        List<ResourceTypeList> resourceTypeList =  resourceTypeRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1,site);
       return resourceTypeList;
    }

    @Override
    public MessageModel createResourceType(ResourceTypeRequest resourceTypeRequest) throws Exception {
         if(isResourceTypeExist(resourceTypeRequest.getResourceType(),resourceTypeRequest.getSite()))
        {
            throw new ResourceTypeException(2401,resourceTypeRequest.getResourceType());
        }
         if (resourceTypeRequest.getResourceTypeDescription()==null || resourceTypeRequest.getResourceTypeDescription().isEmpty())
         {
             resourceTypeRequest.setResourceTypeDescription(resourceTypeRequest.getResourceType());
         }
        if (resourceTypeRequest.getResourceMemberList() != null && !resourceTypeRequest.getResourceMemberList().isEmpty()) {
            List<ResourceMemberList> resourceMemberList = resourceTypeRequest.getResourceMemberList();
            for (ResourceMemberList resourceMember : resourceMemberList) {
                ResourceType resourceType= resourceTypeRepository.findByResourceMemberListResourceAndSiteAndActive(resourceMember.getResource(),resourceTypeRequest.getSite(),1);
                if(resourceType!=null){
                    throw new ResourceTypeException(2407, resourceType.getResourceType());
                }
            }
        }
        ResourceType resourceType = ResourceType.builder()
                .handle("ResourceTypeBO:"+resourceTypeRequest.getSite()+","+resourceTypeRequest.getResourceType())
                .site(resourceTypeRequest.getSite())
                .resourceType(resourceTypeRequest.getResourceType())
                .resourceTypeDescription(resourceTypeRequest.getResourceTypeDescription())
                .resourceMemberList(resourceTypeRequest.getResourceMemberList())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .modifiedDateTime(LocalDateTime.now())
                .build();

        return MessageModel.builder().message_details(new MessageDetails(resourceTypeRequest.getResourceType()+" Created SuccessFully","S")).response(resourceTypeRepository.save(resourceType)).build();
    }
    @Override
    public MessageModel update(ResourceTypeRequest resourceTypeRequest) throws Exception {
        if (isResourceTypeExist(resourceTypeRequest.getResourceType(), resourceTypeRequest.getSite())) {
            ResourceType existingResourceType = resourceTypeRepository.findByResourceTypeAndSiteAndActiveEquals(resourceTypeRequest.getResourceType(), resourceTypeRequest.getSite(), 1);
            if (resourceTypeRequest.getResourceTypeDescription() == null || resourceTypeRequest.getResourceTypeDescription().isEmpty()) {
                resourceTypeRequest.setResourceTypeDescription(resourceTypeRequest.getResourceType());
            }
            if (resourceTypeRequest.getResourceMemberList() != null && !resourceTypeRequest.getResourceMemberList().isEmpty()) {
                List<ResourceMemberList> resourceMemberList = resourceTypeRequest.getResourceMemberList();
                for (ResourceMemberList resourceMember : resourceMemberList) {
                    ResourceType resourceType= resourceTypeRepository.findByResourceMemberListResourceAndSiteAndActive(resourceMember.getResource(),resourceTypeRequest.getSite(),1);
                    if (resourceType != null && !resourceType.getResourceType().equals(resourceTypeRequest.getResourceType())) {
                        throw new ResourceTypeException(2407, resourceType.getResourceType());
                    }
                }
            }
            List<ResourceMemberList> updatedResourceList = resourceTypeRequest.getResourceMemberList();
            List<ResourceMemberList> existingResourceList = existingResourceType.getResourceMemberList();

            if (existingResourceList != null && !existingResourceList.isEmpty()) {

                List<String> resourceToAdd = new ArrayList<>();
                List<String> resourceToRemove = new ArrayList();

                // Compare the updated user list with the existing list and perform necessary operations
                for (ResourceMemberList updatedResource : updatedResourceList) {
                    boolean alreadyExists = existingResourceList.stream().anyMatch(user -> user.getResource().equals(updatedResource.getResource()));
                    if (!alreadyExists) {
                        resourceToAdd.add(updatedResource.getResource());
                    }
                }

                for (ResourceMemberList existResource : existingResourceList) {
                    boolean isRemoved = updatedResourceList.stream().noneMatch(user -> user.getResource().equals(existResource.getResource()));
                    if (isRemoved) {
                        resourceToRemove.add(existResource.getResource());
                    }
                }

                List<String> resourceAddList = new ArrayList<>();
                resourceAddList.add(resourceTypeRequest.getResourceType());

                if (!resourceToAdd.isEmpty()) {
                    for (String resourceName : resourceToAdd) {
                        // Create a UserRequest for adding a user to the user group
                        AssociateResourceTypeToResource addResourceRequest = AssociateResourceTypeToResource.builder()
                                .site(resourceTypeRequest.getSite())
                                .resource(resourceName)
                                .resourceType(resourceAddList)
                                .build();

                       Boolean resourceAddNam = webClientBuilder.build()
                                .post()
                                .uri(associateResourceTypeToResource)
                                .bodyValue(addResourceRequest)
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .block();

                        // com.rits.userservice.model.User addUserResponse = userServiceImpl.addUserGroup(addUserRequest, userGroupList);

                        if (resourceAddNam == null || !resourceAddNam) {
                            throw new ResourceTypeException (2400, resourceTypeRequest.getResourceType());
                        }
                    }
                }

                List<String> resourceRemovList = new ArrayList<>();
                resourceRemovList.add(resourceTypeRequest.getResourceType());

                if (!resourceToRemove.isEmpty()) {
                    for (String resourceName : resourceToRemove) {

                       AssociateResourceTypeToResource addResourceRequest = AssociateResourceTypeToResource.builder()
                                .site(resourceTypeRequest.getSite())
                                .resource(resourceName)
                                .resourceType(resourceRemovList)
                                .build();

                            Boolean resourceRemovNam = webClientBuilder.build()
                                    .post()
                                    .uri(removeResourceFromResourceType)
                                    .bodyValue(addResourceRequest)
                                    .retrieve()
                                    .bodyToMono(Boolean.class)
                                    .block();
                        //com.rits.userservice.model.User removeUserResponse = userServiceImpl.removeUserGroup(removeUserRequest, userGroupRemovList);

                        if (resourceRemovNam == null || !resourceRemovNam) {
                            throw new ResourceTypeException(2400, resourceTypeRequest.getResourceType());
                        }
                    }
                }
            }

            else {

                List<com.rits.resourceservice.model.ResourceTypeList> resourceTypeLists = new ArrayList<>();
                com.rits.resourceservice.model.ResourceTypeList resourceTypeReq = com.rits.resourceservice.model.ResourceTypeList.builder().resourceType(resourceTypeRequest.getResourceType()).build();
                resourceTypeLists.add(resourceTypeReq);

                if (!resourceTypeRequest.getResourceMemberList().isEmpty()) {
                    for (ResourceMemberList resource : resourceTypeRequest.getResourceMemberList()) {
                        List<String> resourceType = new ArrayList<>();
                        resourceType.add(resourceTypeReq.getResourceType());

                        AssociateResourceTypeToResource addResourceRequest = AssociateResourceTypeToResource.builder()
                                .site(resourceTypeRequest.getSite())
                                .resource(resource.getResource())
                                .resourceType(resourceType)
                                .build();

                        Boolean resourceAddNam = webClientBuilder.build()
                                .post()
                                .uri(associateResourceTypeToResource)
                                .bodyValue(addResourceRequest)
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .block();

                        // com.rits.userservice.model.User addUserResponse = userServiceImpl.addUserGroup(addUserRequest, userGroupList);

                        if (resourceAddNam == null|| !resourceAddNam) {
                            throw new ResourceTypeException (2400, resourceTypeRequest.getResourceType());
                        }
                    }
                }

            }

                ResourceType updatedResourceType = ResourceType.builder()
                        .handle(existingResourceType.getHandle())
                        .site(existingResourceType.getSite())
                        .resourceType(existingResourceType.getResourceType())
                        .resourceTypeDescription(resourceTypeRequest.getResourceTypeDescription())
                        .resourceMemberList(resourceTypeRequest.getResourceMemberList())
                        .active(existingResourceType.getActive())
                        .createdDateTime(existingResourceType.getCreatedDateTime())
                        .modifiedDateTime(LocalDateTime.now())
                        .build();

                return MessageModel.builder()
                        .message_details(new MessageDetails(resourceTypeRequest.getResourceType() + " updated SuccessFully", "S"))
                        .response(resourceTypeRepository.save(updatedResourceType))
                        .build();
            }else {
                throw new ResourceTypeException(2400, resourceTypeRequest.getResourceType());
            }
        }

    @Override
    public ResourceType retrieveByResourceType(String resourceType,String site) throws Exception{
        if(!isResourceTypeExist(resourceType,site))
        {
            throw new ResourceTypeException(2400,resourceType);
        }
        return resourceTypeRepository.findByResourceTypeAndSiteAndActiveEquals(resourceType,site,1);
    }

    @Override
    public boolean isResourceTypeExist(String resourceType,String site) throws Exception{
        return resourceTypeRepository.existsByResourceTypeAndSiteAndActiveEquals(resourceType,site,1);
    }

    @Override
    public Response deleteResourceType(String resourceType, String site) throws Exception{
        ResourceType existingResource = resourceTypeRepository.findByResourceTypeAndSiteAndActiveEquals(resourceType,site,1);
        if (resourceTypeRepository.existsByResourceTypeAndSiteAndActiveEquals(resourceType,site,1)) {
            existingResource.setActive(0);
            resourceTypeRepository.save(existingResource);

            return Response.builder().message("Deleted "+ resourceType).build();
        } else {
            throw new ResourceTypeException(2400,resourceType);
        }
    }

    @Override
    public List<AvailableResourceType> retrieveAllResourceType(String site) throws Exception{
        List<AvailableResourceType> availableResourceTypes = resourceTypeRepository.findByActiveAndSite(1,site);
      AvailableResourceTypeList availableResourceTypeList = new AvailableResourceTypeList(availableResourceTypes);
        return availableResourceTypes;
    }

    @Override
    public List<ResourceTypeList> retrieveResourceTypeList(String site,String resourceType) throws Exception{
        List<ResourceTypeList> resourceTypes = new ArrayList<>();
        if(StringUtils.isBlank(resourceType))
            resourceTypes = resourceTypeRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1,site);
        else
            resourceTypes = resourceTypeRepository.findBySiteAndResourceTypeContainingIgnoreCaseAndActiveEquals(site,resourceType,1);
        return resourceTypes;
    }

    @Override
    public Boolean removeResourceFromResourceType(String resourceType,List<String> resource,String site) throws Exception{
        if(isResourceTypeExist(resourceType,site)) {
            if (resource != null && !resource.isEmpty()) {
                ResourceType existingResourceType = resourceTypeRepository.findByResourceTypeAndSiteAndActiveEquals(resourceType, site, 1);
                List<ResourceMemberList> existingResourceMemberList = existingResourceType.getResourceMemberList();
                existingResourceMemberList.removeIf(association -> resource.contains(association.getResource()));
                existingResourceType.setResourceMemberList(existingResourceMemberList);

                resourceTypeRepository.save(existingResourceType);
             /*   AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(existingResourceType.getSite())
                        .action_code("RESOURCETYPE-REMOVED")
                        .action_detail("Resource removed from the resourceType"+existingResourceType.getResourceType())
                        .action_detail_handle("ActionDetailBO:"+existingResourceType.getSite()+","+"RESOURCETYPE-REMOVED"+existingResourceType.getUserID()+":"+"com.rits.resourcetypeservice.Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(existingResourceType.getUserID())
                        .operation_revision("*")
                        .txnId("RESOURCETYPE-REMOVED"+String.valueOf(LocalDateTime.now())+existingResourceType.getUserID())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("RESOURCE_TYPE")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));*/

                return true;
            }
        }
        else {
            throw new ResourceTypeException(2404,resource);
        }
        throw new ResourceTypeException(2400,resourceType);
    }

    @Override
    public Boolean associateResourceToResourceType(String resourceType, List<String> resource,String site) throws Exception{
        if (resourceTypeRepository.existsByResourceTypeAndSiteAndActiveEquals(resourceType,site,1)) {
            if(resource!=null && !resource.isEmpty()) {
                ResourceType existingResourceType = resourceTypeRepository.findByResourceTypeAndSiteAndActiveEquals(resourceType, site, 1);
                for (int i = 0; i < resource.size(); i++) {
                    ResourceMemberList newResource = new ResourceMemberList(resource.get(i));
                    existingResourceType.getResourceMemberList().add(newResource);
                }
                resourceTypeRepository.save(existingResourceType);
               /* AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(existingResourceType.getSite())
                        .action_code("RESOURCETYPE-UPDATED")
                        .action_detail("Resource Associated with the resourceType"+existingResourceType.getResourceType())
                        .action_detail_handle("ActionDetailBO:"+existingResourceType.getSite()+","+"RESOURCETYPE-UPDATED"+existingResourceType.getUserID()+":"+"com.rits.resourcetypeservice.Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(existingResourceType.getUserID())
                        .operation_revision("*")
                        .txnId("RESOURCETYPE-UPDATED"+String.valueOf(LocalDateTime.now())+existingResourceType.getUserID())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("RESOURCE_TYPE")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));*/
                return true;
            }
            else {
                throw new ResourceTypeException(2404,resource);
            }
        } else {
            throw new ResourceTypeException(2400,resourceType);
        }
    }
    @Override
    public AvailableResource availableResources(ResourceTypeRequest resourceTypeRequest) throws Exception {
        ResourceRequest resourceRequest = new ResourceRequest(resourceTypeRequest.getSite());
        List<ResourceListResponse> resourceList = webClientBuilder.build()
                .post()
                .uri(resourceServiceUrl)
                .bodyValue(resourceRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ResourceListResponse>>() {
                })
                .block();
        List<String> allResource = new ArrayList<>();
        List<String> associatedResource = new ArrayList<>();
        for (ResourceListResponse rel : resourceList) {
            allResource.add(rel.getResource());
        }
        if (resourceTypeRequest.getResourceType() != null && !resourceTypeRequest.getResourceType().isEmpty()) {
            ResourceType existingResourceType = resourceTypeRepository.findByResourceTypeAndSiteAndActiveEquals(resourceTypeRequest.getResourceType(), resourceTypeRequest.getSite(), 1);
            List<ResourceMemberList> associatedResourceList = existingResourceType.getResourceMemberList();

            for (ResourceMemberList res : associatedResourceList) {
                associatedResource.add(res.getResource());
            }

            for (int i = 0; i < associatedResource.size(); i++) {
                if (allResource.contains(associatedResource.get(i))) {
                    allResource.remove(associatedResource.get(i));
                }
            }
        }
        List<Resource> availableResourceList = new ArrayList<Resource>();
        for (int i = 0; i < allResource.size(); i++) {
            Resource resource = new Resource(allResource.get(i));
            availableResourceList.add(resource);
        }
        AvailableResource availableResource = new AvailableResource(availableResourceList);
        return availableResource;
    }

    public MessageModel getAllResourcesByResourceType(ResourceTypeRequest resourceTypeRequest, List<com.rits.resourceservice.dto.ResourceListResponse> resourceList) throws Exception {

        List<com.rits.resourceservice.dto.ResourceListResponse> availableResources = new ArrayList<>();

        // If resourceType is blank, fetch all resources for the given site
        if (StringUtils.isBlank(resourceTypeRequest.getResourceType())) {
            List<ResourceType> allResourceTypes = resourceTypeRepository.findBySiteAndActiveEquals(resourceTypeRequest.getSite(), 1);
            for (ResourceType resourceType : allResourceTypes) {
                List<ResourceMemberList> associatedResourceList = resourceType.getResourceMemberList();
                if (associatedResourceList != null && resourceList != null) {
                    for (ResourceMemberList res : associatedResourceList) {
                        if (res == null || res.getResource() == null) continue;

                        for (com.rits.resourceservice.dto.ResourceListResponse resource : resourceList) {
                            if (resource != null && resource.getResource() != null && resource.getResource().equals(res.getResource())) {
                                availableResources.add(resource);
                            }
                        }
                    }
                }
            }
        } else {
            // If resourceType is provided, fetch resources for the specific resourceType and site
            ResourceType existingResourceType = resourceTypeRepository.findByResourceTypeAndSiteAndActiveEquals(resourceTypeRequest.getResourceType(), resourceTypeRequest.getSite(), 1);
            if (existingResourceType != null) {
                List<ResourceMemberList> associatedResourceList = existingResourceType.getResourceMemberList();
                if (associatedResourceList != null && resourceList != null) {
                    for (ResourceMemberList res : associatedResourceList) {
                        if (res == null || res.getResource() == null) continue;

                        for (com.rits.resourceservice.dto.ResourceListResponse resource : resourceList) {
                            if (resource != null && resource.getResource() != null && resource.getResource().equals(res.getResource())) {
                                availableResources.add(resource);
                            }
                        }
                    }
                }
            }
        }

        // Map to the desired response format
        List<ResourceListResponse> newList = availableResources.stream()
                .map(original -> new ResourceListResponse(original.getResource(), original.getDescription(), original.getStatus()))
                .collect(Collectors.toList());

        return MessageModel.builder().availableResources(newList).build();
    }

    @Override
    public String callExtension(Extension extension) {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new ResourceTypeException(800);
        }
        return extensionResponse;
    }
}
