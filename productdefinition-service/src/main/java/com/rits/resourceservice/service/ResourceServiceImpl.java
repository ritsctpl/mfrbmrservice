package com.rits.resourceservice.service;

import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.resourceservice.dto.*;
import com.rits.resourceservice.exception.ResourceException;
import com.rits.resourceservice.model.*;
import com.rits.resourceservice.repository.ResourceServiceRepository;
import com.rits.resourcetypeservice.Exception.ResourceTypeException;
import com.rits.resourcetypeservice.Service.ResourceTypeService;
import com.rits.workcenterservice.dto.RetrieveRequest;
import com.rits.workcenterservice.service.WorkCenterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceServiceRepository resourceServiceRepository;
    private final WorkCenterServiceImpl workCenterService;
    private final ResourceTypeService resourceTypeService;
    private final ApplicationEventPublisher eventPublisher;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;
    @Value("${resource.type.uri}/retrieveAllResourceType")
    private String resourceTypeUrl;
    @Value("${certification.url}/isExist")
    private String isCertificationExistUrl;
    @Value("${resource.type.uri}/add")
    private String associateResourceToResourceType;
    @Value("${resource.type.uri}/remove")
    private String removeResourceFromResourceType;
    @Value("${operation.type.uri}/isExist")
    private String operationUrl;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;
    @Value("${productionlog-service.url}/save")
    private String productionLogUrl;
    @Value("${shift-service.url}/getBreakHoursTillNowByType")
    private String getBreakMinutesUrl;
    @Value("${shift-service.url}/getPlannedTimeTillNowByType")
    private String getPlannedTimeTillNowByTypeUrl;
    @Value("${workcenter-service.url}/getWorkCenterByResource")
    private String getWorkCenterByResourceUrl;

    @Value("${activity-service.url}/isExist")
    private String isActivityExistUrl;
    private final String mytopic = "audit-log";

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }
  /*  @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;*/

    @Override
    public MessageModel createResource(ResourceRequest resourceRequest) throws Exception {
        if (isResourceExist(resourceRequest.getResource(), resourceRequest.getSite())) {
            throw new ResourceException(9300, resourceRequest.getResource());
        }
        try {
            getValidated(resourceRequest);
            UpdateResourceType(null, resourceRequest);
        } catch (ResourceException resourceException) {
            throw resourceException;
        } catch (Exception e) {
            throw e;
        }
        Resource resource = createResourceBuilder(resourceRequest);
        String createdMessage = getFormattedMessage(31, resourceRequest.getResource());


        return MessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(resourceServiceRepository.save(resource)).build();

    }

    private Resource createResourceBuilder(ResourceRequest resourceRequest) {
    String setUpSate="0";
        if(resourceRequest.getStatus().equalsIgnoreCase("Enabled")){
             setUpSate="1";
        }


        return Resource.builder()
                .site(resourceRequest.getSite())
                .handle("ResourceBO:" + resourceRequest.getSite() + "," + resourceRequest.getResource())
                .resource(resourceRequest.getResource())
                .description(resourceRequest.getDescription())
                .status(resourceRequest.getStatus())
                .defaultOperation(resourceRequest.getDefaultOperation())
                .processResource(resourceRequest.isProcessResource())
                .erpEquipmentNumber(resourceRequest.getErpEquipmentNumber())
                .erpPlantMaintenanceOrder(resourceRequest.getErpPlantMaintenanceOrder())
                .validFrom(resourceRequest.getValidFrom())
                .validTo(resourceRequest.getValidTo())
                .activityHookList(resourceRequest.getActivityHookList())
                .setUpState(setUpSate)
                .createdBy(resourceRequest.getUserId())
                .utilizationPricePerHr(resourceRequest.getUtilizationPricePerHr())
                .resourceTypeList(resourceRequest.getResourceTypeList())
                .certificationList(resourceRequest.getCertificationList())
                .opcTagList(resourceRequest.getOpcTagList())
                .resourceCustomDataList(resourceRequest.getResourceCustomDataList())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .build();
    }


    private void getValidated(ResourceRequest resourceRequest) throws Exception {
        isDefaultOperationValid(resourceRequest);
        setDescriptionIfNull(resourceRequest);
        isResourceTypeValid(resourceRequest);
        isCertificationValid(resourceRequest);
        isActivityValid(resourceRequest);

    }

    private void isActivityValid(ResourceRequest resourceRequest) throws Exception {
        if (resourceRequest.getActivityHookList() != null && !resourceRequest.getActivityHookList().isEmpty()) {
            for (ActivityHook activityHook : resourceRequest.getActivityHookList()) {
                if (!activityExist(activityHook.getActivity(), resourceRequest.getSite())) {
                    throw new ResourceException(2406, activityHook.getActivity());
                }
            }
        }
    }

    private boolean activityExist(String activity, String site) {
        ActivityHookRequest isExist = ActivityHookRequest.builder().activityId(activity).site(site).build();
        Boolean isActivityExist = webClientBuilder.build()
                .post()
                .uri(isActivityExistUrl) // URL for checking activity existence
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return isActivityExist;
    }



    private void isCertificationValid(ResourceRequest resourceRequest) throws Exception {
        if (resourceRequest.getCertificationList() != null && !resourceRequest.getCertificationList().isEmpty()) {
            for (CertificationList certificationList : resourceRequest.getCertificationList()) {
                if (!(certificationExist(certificationList.getCertificationBO(), resourceRequest.getSite()))) {
                    throw new ResourceException(2405, certificationList.getCertificationBO());
                }

            }
        }
    }

    private boolean certificationExist(String certificationBO, String site) {
        IsExist isExist = IsExist.builder().certification(certificationBO).site(site).build();
        Boolean isCertificationExist = webClientBuilder.build()
                .post()
                .uri(isCertificationExistUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return isCertificationExist;
    }

    private void isResourceTypeValid(ResourceRequest resourceRequest) throws Exception {
        if (resourceRequest.getResourceTypeList() != null && !resourceRequest.getResourceTypeList().isEmpty()) {
            for (ResourceTypeList resourceTypeList : resourceRequest.getResourceTypeList()) {
                if (!(resourceTypeService.isResourceTypeExist(resourceTypeList.getResourceType(), resourceRequest.getSite()))) {
                    throw new ResourceException(2400, resourceTypeList.getResourceType());
                }

            }
        }
    }

    private void setDescriptionIfNull(ResourceRequest resourceRequest) {
        if (resourceRequest.getDescription() == null || resourceRequest.getDescription().isEmpty()) {
            resourceRequest.setDescription(resourceRequest.getResource());
        }
    }

    private void isDefaultOperationValid(ResourceRequest resourceRequest) {
        if (resourceRequest.getDefaultOperation() != null && !resourceRequest.getDefaultOperation().isEmpty()) {
            Operation operationRequest = new Operation();
            operationRequest.setOperation(resourceRequest.getDefaultOperation());
            operationRequest.setSite(resourceRequest.getSite());
            Boolean operation = webClientBuilder.build()
                    .post()
                    .uri(operationUrl)
                    .bodyValue(operationRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            if (!operation) {
                throw new ResourceException(9305, resourceRequest.getDefaultOperation());
            }
        }
    }


    @Override
    public List<ResourceListResponse> retrieveTop50Resource(String site) throws Exception {

        List<ResourceListResponse> resourceResponseList = resourceServiceRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return resourceResponseList;
    }

    @Override
    public Resource retrieveResource(String site, String resource) throws Exception {

        if (isResourceExist(resource, site)) {
            return resourceServiceRepository.findByResourceAndSiteAndActiveEquals(resource, site, 1);
        } else {
            throw new ResourceException(9301, resource);
        }
    }

    @Override
    public Resource retrieveBySiteAndErpEquipmentNumber(String site, String erpEquipmentNumber) throws Exception {

        return resourceServiceRepository.findByErpEquipmentNumberAndSiteAndActiveEquals(erpEquipmentNumber, site, 1);
    }

    @Override
    public boolean isResourceExist(String resource, String site) throws Exception {
        return resourceServiceRepository.existsByResourceAndSiteAndActiveEquals(resource, site, 1);
    }

    @Override
    public boolean isResourceExistByHandle(String site, String resource) throws Exception {
        return resourceServiceRepository.existsBySiteAndHandleAndActive(site, resource, 1);
    }

    @Override
    public MessageModel deleteResource(String site, String resource, String userId) throws Exception {
        if (userId == null || userId.isEmpty()) {
            throw new ResourceException(108);
        }
        if (isResourceExist(resource, site)) {
            Resource existingResource = resourceServiceRepository.findByResourceAndSiteAndActiveEquals(resource, site, 1);
            existingResource.setActive(0);
            existingResource.setModifiedBy(userId);
            existingResource.setModifiedDateTime(LocalDateTime.now());
            resourceServiceRepository.save(existingResource);
            String deletedMessage = getFormattedMessage(33, resource);

            return MessageModel.builder().message_details(new MessageDetails(deletedMessage, "S")).build();
        }
        throw new ResourceException(9301, resource);
    }

    @Override
    public MessageModel updateResource(ResourceRequest resourceRequest) throws Exception {
        if (isResourceExist(resourceRequest.getResource(), resourceRequest.getSite())) {
            Resource existingResource = resourceServiceRepository.findByResourceAndSiteAndActiveEquals(resourceRequest.getResource(), resourceRequest.getSite(), 1);

            try {
                getValidated(resourceRequest);
                UpdateResourceType(existingResource, resourceRequest);
                updateResourceStatusAndSetupState(resourceRequest);
            } catch (ResourceException resourceException) {
                throw resourceException;
            } catch (Exception e) {
                throw e;
            }


            Resource newResource = updateResourceBuilder(existingResource, resourceRequest);
            String updatedMessage = getFormattedMessage(32, resourceRequest.getResource());


            return MessageModel.builder().message_details(new MessageDetails(updatedMessage, "S")).response(resourceServiceRepository.save(newResource)).build();
        } else {
            throw new ResourceException(9305, resourceRequest.getDefaultOperation());
        }
    }

    private Resource updateResourceBuilder(Resource existingResource, ResourceRequest resourceRequest) {
        return Resource.builder()
                .site(existingResource.getSite())
                .handle(existingResource.getHandle())
                .resource(existingResource.getResource())
                .description(resourceRequest.getDescription())
                .status(resourceRequest.getStatus())
                .defaultOperation(resourceRequest.getDefaultOperation())
                .processResource(resourceRequest.isProcessResource())
                .erpEquipmentNumber(resourceRequest.getErpEquipmentNumber())
                .erpPlantMaintenanceOrder(resourceRequest.getErpPlantMaintenanceOrder())
                .validFrom(resourceRequest.getValidFrom())
                .validTo(resourceRequest.getValidTo())
                .utilizationPricePerHr(resourceRequest.getUtilizationPricePerHr())
                .resourceTypeList(resourceRequest.getResourceTypeList())
                .activityHookList(resourceRequest.getActivityHookList())
                .setUpState(resourceRequest.getSetUpState())
                .modifiedBy(resourceRequest.getUserId())
                .createdBy(existingResource.getCreatedBy())
                .certificationList(resourceRequest.getCertificationList())
                .opcTagList(resourceRequest.getOpcTagList())
                .resourceCustomDataList(resourceRequest.getResourceCustomDataList())
                .active(existingResource.getActive())
                .createdDateTime(existingResource.getCreatedDateTime())
                .modifiedDateTime(LocalDateTime.now())
                .build();
    }


    private void updateResourceStatusAndSetupState(ResourceRequest resourceRequest) {
        String status = resourceRequest.getStatus();
        if ("Disabled".equalsIgnoreCase(status)) {
            resourceRequest.setSetUpState("Disabled");
        } else if ("Enabled".equalsIgnoreCase(status)) {
            resourceRequest.setSetUpState("Enabled");
        }
    }

    private void UpdateResourceType(Resource existingResource, ResourceRequest resourceRequest) {

        List<ResourceTypeList> updatedResourceList = resourceRequest.getResourceTypeList();
        if (existingResource != null && existingResource.getResource() != null) {
            List<ResourceTypeList> existingResourceList = existingResource.getResourceTypeList();


            if (existingResourceList != null && !existingResourceList.isEmpty()) {

                List<String> resourceToAdd = new ArrayList<>();
                List<String> resourceToRemove = new ArrayList();

                // Compare the updated user list with the existing list and perform necessary operations
                for (ResourceTypeList updatedResourceType : updatedResourceList) {
                    boolean alreadyExists = existingResourceList.stream().anyMatch(user -> user.getResourceType().equals(updatedResourceType.getResourceType()));
                    if (!alreadyExists) {
                        resourceToAdd.add(updatedResourceType.getResourceType());
                    }
                }

                for (ResourceTypeList existResourceType : existingResourceList) {
                    boolean isRemoved = updatedResourceList.stream().noneMatch(user -> user.getResourceType().equals(existResourceType.getResourceType()));
                    if (isRemoved) {
                        resourceToRemove.add(existResourceType.getResourceType());
                    }
                }

                List<String> resourceAddList = new ArrayList<>();
                resourceAddList.add(resourceRequest.getResource());

                if (!resourceToAdd.isEmpty()) {
                    for (String resourceName : resourceToAdd) {
                        // Create a UserRequest for adding a user to the user group
                        AssociateResourceTypeToResources addResourceRequest = AssociateResourceTypeToResources.builder()
                                .site(resourceRequest.getSite())
                                .resource(resourceAddList)
                                .resourceType(resourceName)
                                .build();

                        Boolean resourceAddNam = webClientBuilder.build()
                                .post()
                                .uri(associateResourceToResourceType)
                                .bodyValue(addResourceRequest)
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .block();

                        // com.rits.userservice.model.User addUserResponse = userServiceImpl.addUserGroup(addUserRequest, userGroupList);

                        if (resourceAddNam == null || !resourceAddNam) {
                            throw new ResourceTypeException(9301, resourceRequest.getResource());
                        }
                    }
                }

                List<String> resourceRemovList = new ArrayList<>();
                resourceRemovList.add(resourceRequest.getResource());

                if (!resourceToRemove.isEmpty()) {
                    for (String resourceName : resourceToRemove) {

                        AssociateResourceTypeToResources removeResourceRequest = AssociateResourceTypeToResources.builder()
                                .site(resourceRequest.getSite())
                                .resource(resourceRemovList)
                                .resourceType(resourceName)
                                .build();

                        Boolean resourceRemovNam = webClientBuilder.build()
                                .post()
                                .uri(removeResourceFromResourceType)
                                .bodyValue(removeResourceRequest)
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .block();

                        //com.rits.userservice.model.User removeUserResponse = userServiceImpl.removeUserGroup(removeUserRequest, userGroupRemovList);

                        if (resourceRemovNam == null || !resourceRemovNam) {
                            throw new ResourceTypeException(9301, resourceRequest.getResource());
                        }
                    }
                }
            }
        } else {

            List<com.rits.resourcetypeservice.dto.Resource> resourceLists = new ArrayList<>();
            com.rits.resourcetypeservice.dto.Resource resourceReq = com.rits.resourcetypeservice.dto.Resource.builder().resource(resourceRequest.getResource()).build();
            resourceLists.add(resourceReq);

            if (!resourceRequest.getResourceTypeList().isEmpty()) {
                for (ResourceTypeList resources : resourceRequest.getResourceTypeList()) {
                    List<String> resourceType = new ArrayList<>();
                    resourceType.add(resourceReq.getResource());

                    AssociateResourceTypeToResources addResourceRequest = AssociateResourceTypeToResources.builder()
                            .site(resourceRequest.getSite())
                            .resource(resourceType)
                            .resourceType(resources.getResourceType())
                            .build();

                    Boolean resourceAddNam = webClientBuilder.build()
                            .post()
                            .uri(associateResourceToResourceType)
                            .bodyValue(addResourceRequest)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block();

                    // com.rits.userservice.model.User addUserResponse = userServiceImpl.addUserGroup(addUserRequest, userGroupList);

                    if (resourceAddNam == null || !resourceAddNam) {
                        throw new ResourceTypeException(9301, resourceRequest.getResource());
                    }
                }
            }

        }
    }


    @Override
    public List<ResourceListResponse> retrieveResourceList(String site, String resource) throws Exception {
        if (resource != null) {
            List<ResourceListResponse> resourceList = resourceServiceRepository.findByActiveAndSiteAndResourceContainingIgnoreCase(1,site, resource);
            return resourceList;
        } else {
            return resourceServiceRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        }
    }

    @Override
    public Boolean associateResourceTypeToResource(AssociateResourceToResourceType associate) throws Exception { //list of resource type
        if (isResourceExist(associate.getResource(), associate.getSite())) {
            if (associate.getResourceType() != null && !associate.getResourceType().isEmpty()) {
                Resource existingResource = resourceServiceRepository.findByResourceAndSiteAndActiveEquals(associate.getResource(), associate.getSite(), 1);
                for (int i = 0; i < associate.getResourceType().size(); i++) {
                    ResourceTypeList newResourceType = new ResourceTypeList(associate.getResourceType().get(i));
                    existingResource.getResourceTypeList().add(newResourceType);
                }
                resourceServiceRepository.save(existingResource);
                existingResource.getResourceTypeList();
                return true;
            }
        } else {
            throw new ResourceException(9304, associate.getResourceType());
        }
        throw new ResourceException(9301, associate.getResource());
    }

    @Override
    public Boolean removeResourceFromResourceType(AssociateResourceToResourceType associate) throws Exception {
        if (isResourceExist(associate.getResource(), associate.getSite())) {
            if (associate.getResourceType() != null && !associate.getResourceType().isEmpty()) {
                Resource existingResource = resourceServiceRepository.findByResourceAndSiteAndActiveEquals(associate.getResource(), associate.getSite(), 1);
                List<ResourceTypeList> existingResourceMemberList = existingResource.getResourceTypeList();
                existingResourceMemberList.removeIf(association -> associate.getResourceType().contains(association.getResourceType()));
                existingResource.setResourceTypeList(existingResourceMemberList);
                resourceServiceRepository.save(existingResource);
                return true;
            }
            throw new ResourceException(9304, associate.getResourceType());
        }
        throw new ResourceException(9301, associate.getResource());
    }

    @Override
    public AvailableResourceTypeList availableResourceType(String site, String resource) throws Exception {
        ResourceTypeRequest resourceType = new ResourceTypeRequest(site);

        List<AvailableResourceType> resourceTypeList = webClientBuilder.build()
                .post()
                .uri(resourceTypeUrl)
                .bodyValue(resourceType)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AvailableResourceType>>() {
                })
                .block();

        List<String> allResourceType = new ArrayList<>();
        List<String> associatedResourceType = new ArrayList<>();

        for (AvailableResourceType rel : resourceTypeList) {
            allResourceType.add(rel.getResourceType());
        }
        if (resource != null && !resource.isEmpty()) {
            Resource existingResource = resourceServiceRepository.findByResourceAndSiteAndActiveEquals(resource, site, 1);
            List<ResourceTypeList> associatedResourceTypeList = existingResource.getResourceTypeList();
            for (ResourceTypeList res : associatedResourceTypeList) {
                associatedResourceType.add(res.getResourceType());
            }

            for (String s : associatedResourceType) {
                allResourceType.remove(s);
            }
        }
        List<AvailableResourceType> availableResourceTypeList = new ArrayList<AvailableResourceType>();
        for (String s : allResourceType) {
            AvailableResourceType updatedResourceType = new AvailableResourceType(s);
            availableResourceTypeList.add(updatedResourceType);
        }
        AvailableResourceTypeList availableList = new AvailableResourceTypeList(availableResourceTypeList);
        return availableList;
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
            throw new ResourceException(800);
        }
        return extensionResponse;
    }

    @Override
    public MessageModel checkSetUpStatusAndCallProductionLog(CheckStatusRequest checkStatusRequest) throws Exception {
        Resource existingResource = null;
        ResourceRequest resourceRequest = ResourceRequest.builder().site(checkStatusRequest.getSite()).defaultOperation(checkStatusRequest.getDefaultOperation()).build();
        isDefaultOperationValid(resourceRequest);
//        ShiftRequest shiftRequest=ShiftRequest.builder().site(checkStatusRequest.getSite()).shiftType("Resource").resource(checkStatusRequest.getResource()).build();
//        BreakMinutes breakMinutes = webClientBuilder.build()
//                .post()
//                .uri(getBreakMinutesUrl)
//                .bodyValue(shiftRequest)
//                .retrieve()
//                .bodyToMono(BreakMinutes.class)
//                .block();
//
//        PlannedMinutes plannedMinutes = webClientBuilder.build()
//                .post()
//                .uri(getPlannedTimeTillNowByTypeUrl)
//                .bodyValue(shiftRequest)
//                .retrieve()
//                .bodyToMono(PlannedMinutes.class)
//                .block();
        RetrieveRequest retrieveRequest = RetrieveRequest.builder().site(checkStatusRequest.getSite()).resource(checkStatusRequest.getResource()).build();

//        String workCenter = webClientBuilder.build()
//                .post()
//                .uri(getWorkCenterByResourceUrl)
//                .bodyValue(retrieveRequest)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();

        String workCenter = workCenterService.getWorkCenterByResource(checkStatusRequest.getSite(), checkStatusRequest.getResource());

        if (isResourceExist(checkStatusRequest.getResource(), checkStatusRequest.getSite())) {
            existingResource = resourceServiceRepository.findByResourceAndSiteAndActiveEquals(checkStatusRequest.getResource(), checkStatusRequest.getSite(), 1);
            if (existingResource != null) {
                if (!existingResource.getSetUpState().equalsIgnoreCase(checkStatusRequest.getSetUpState())) {
                    ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                            .eventType("MC_UP")
                            .site(checkStatusRequest.getSite())
                            .userId(checkStatusRequest.getUserId())
                            .resourceBO("ResourceBO:" + checkStatusRequest.getSite() + "," + checkStatusRequest.getResource())
                            .eventData(checkStatusRequest.getResource() + "SetUpState Updated successfully")
                            .workCenterBO(workCenter)
                            .data_field("SetUpState")
                            .data_value(checkStatusRequest.getSetUpState())
                            .comments(checkStatusRequest.getComments())
                            .reasonCode(checkStatusRequest.getReasonCode())
//                            .shiftName(breakMinutes.getShiftName())
//                            .shiftStartTime(breakMinutes.getStartTime())
//                            .shiftEndTime(breakMinutes.getEndTime())
//                            .totalBreakHours(String.valueOf(breakMinutes.getBreakTime()))
//                            .shiftAvailableTime(String.valueOf(plannedMinutes.getPlannedTime()))
                            .timestamp(LocalDateTime.now().toString())
                            .build();
                    existingResource.setStatus("Enabled");
                    List<String> machineDownStates = List.of("Unknown", "StandBy", "Disabled", "Hold", "Hold Yield Rate", "Hold Consec NC", "Hold SRC Viol", "Hold SRC Warn", "Scheduled Down", "Unscheduled  Down", "Non-Scheduled");
                    if (machineDownStates.contains(checkStatusRequest.getSetUpState())) {
                        productionLogRequest.setEventType("MC_DOWN");
                        existingResource.setStatus("Disabled");
                    }
                  /*  Boolean productionLogged = webClientBuilder.build()
                            .post()
                            .uri(productionLogUrl)
                            .bodyValue(productionLogRequest)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block();*/
                }
                existingResource.setSetUpState(checkStatusRequest.getSetUpState());
                existingResource.setModifiedDateTime(LocalDateTime.now());
                existingResource.setModifiedBy(checkStatusRequest.getUserId());
                resourceServiceRepository.save(existingResource);
            }
        } else {
            throw new ResourceException(9301, checkStatusRequest.getResource());
        }
        String updatedMessage = getFormattedMessage(32, checkStatusRequest.getResource());
        return MessageModel.builder().message_details(new MessageDetails(updatedMessage, "S")).response(existingResource).build();
    }

    @Override
    public List<ResourceListResponse> getAllResponse(String site) throws Exception {
        List<ResourceListResponse> resourceListResponses = resourceServiceRepository.findBySiteAndActive(site, 1);
        return resourceListResponses;
    }

//    public Map<String, String> retrieveResourceDiscription(ResourceTypeRequest resourceTypeRequest) throws Exception {
////        List<ResourceListResponse> resourceListResponses=resourceServiceRepository.findBySiteAndActive(site,1);
//        Map<String, String> keyValueMap = new HashMap<>();
//        for(int i=0;i<resourceTypeRequest.getResourceTypeList().size();i++) {
//            Resource resDiscription = resourceServiceRepository.findBySiteAndActiveAndResource(resourceTypeRequest.getSite(), 1, resourceTypeRequest.getResourceTypeList().get(i));
//            keyValueMap.put(resDiscription.getResource(),resDiscription.getDescription());
//        }
//        return keyValueMap;
//    }

    @Override
    public AuditLogRequest createAuditLog(ResourceRequest resourceRequest) {
        return AuditLogRequest.builder()
                .site(resourceRequest.getSite())
                .change_stamp("Create")
                .action_code("RESOURCE-CREATE")
                .action_detail("Resource Created " + resourceRequest.getResource())
                .action_detail_handle("ActionDetailBO:" + resourceRequest.getSite() + "," + "RESOURCE-CREATE" + "," + resourceRequest.getUserId() + ":" + "com.rits.resourceservice.service")
                .activity(resourceRequest.getActivity())
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(resourceRequest.getUserId())
                .operation(resourceRequest.getDefaultOperation())
                .operation_revision("*")
                .resrce(resourceRequest.getResource())
                .txnId("RESOURCE-CREATE" + LocalDateTime.now() + resourceRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Create")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest updateAuditLog(ResourceRequest resourceRequest) {
        return AuditLogRequest.builder()
                .site(resourceRequest.getSite())
                .action_code("RESOURCE-UPDATE")
                .action_detail("Resource Updated " + resourceRequest.getResource())
                .action_detail_handle("ActionDetailBO:" + resourceRequest.getSite() + "," + "RESOURCE-UPDATE" + resourceRequest.getUserId() + ":" + "com.rits.resourceservice.service")
                .activity(resourceRequest.getActivity())
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(resourceRequest.getUserId())
                .operation(resourceRequest.getDefaultOperation())
                .operation_revision("*")
                .resrce(resourceRequest.getResource())
                .txnId("RESOURCE-UPDATE" + LocalDateTime.now() + resourceRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Update")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest deleteAuditLog(ResourceRequest resourceRequest) {
    return AuditLogRequest.builder()
            .site(resourceRequest.getSite())
            .action_code("RESOURCE-DELETED")
            .action_detail("Resource Deleted " + resourceRequest.getResource())
            .action_detail_handle("ActionDetailBO:" + resourceRequest.getSite() + "," + "RESOURCE-Deleted" + resourceRequest.getUserId() + ":" + "com.rits.resourceservice.service")
            .activity(resourceRequest.getActivity())
            .date_time(String.valueOf(LocalDateTime.now()))
            .userId(resourceRequest.getUserId())
            .operation(resourceRequest.getDefaultOperation())
            .operation_revision("*")
            .resrce(resourceRequest.getResource())
            .txnId("RESOURCE-DELETE" + LocalDateTime.now() + resourceRequest.getUserId())
            .created_date_time(String.valueOf(LocalDateTime.now()))
            .category("Delete")
            .topic("audit-log")
            .build();

    }
}