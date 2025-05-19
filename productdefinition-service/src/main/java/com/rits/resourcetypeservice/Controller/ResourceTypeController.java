package com.rits.resourcetypeservice.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.kafkaservice.ProducerEvent;
import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.resourceservice.service.ResourceServiceImpl;
import com.rits.resourcetypeservice.Exception.ResourceTypeException;
import com.rits.resourcetypeservice.Model.MessageModel;
import com.rits.resourcetypeservice.dto.*;
import com.rits.resourcetypeservice.Model.ResourceType;
import com.rits.resourcetypeservice.Service.ResourceTypeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/app/v1/resourcetype-service")
public class ResourceTypeController {

    private final ResourceTypeServiceImpl resourceTypeServiceImpl;
    private final ResourceServiceImpl resourceServiceImpl;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

//     "required": [
//    "site",
//    "resourceType"
//  ]

//    {
//        "site" : "RITS",
//            "resourceType" : "GROUP10",
//            "resourceTypeDescription" : "Group4",
//            "resourceMemberList" : [
//        {
//            "resource" : ""
//        }
//    ]
//    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public  ResponseEntity<?> createResourceType(@RequestBody ResourceTypeRequest resourceTypeRequest) throws JsonProcessingException {
        MessageModel createResourceType;
//       MessageModel validationResponse = resourceTypeServiceImpl.validation( payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        ResourceTypeRequest resourceTypeRequest = new ObjectMapper().convertValue(payload, ResourceTypeRequest.class);

//            try {
//                ResourceType createdResourceType = resourceTypeServiceImpl.createResourceType(resourceTypeRequest);
//                return ResponseEntity.ok(createdResourceType);
        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(resourceTypeRequest.getSite()).hookPoint("PRE").activity("resourcetype-service").hookableMethod("create").request(objectMapper.writeValueAsString(resourceTypeRequest)).build();
        String preExtensionResponse = resourceTypeServiceImpl.callExtension(preExtension);
        ResourceTypeRequest preExtensionResourceType = objectMapper.readValue(preExtensionResponse, ResourceTypeRequest.class);

        try {
            createResourceType = resourceTypeServiceImpl.createResourceType(preExtensionResourceType);
            Extension postExtension = Extension.builder().site(resourceTypeRequest.getSite()).hookPoint("POST").activity("resourcetype-service").hookableMethod("create").request(objectMapper.writeValueAsString(createResourceType.getResponse())).build();
            String postExtensionResponse = resourceTypeServiceImpl.callExtension(postExtension);
            ResourceType postExtensionResourceType = objectMapper.readValue(postExtensionResponse, ResourceType.class);

            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(resourceTypeRequest.getSite())
                    .action_code("RESOURCETYPE-CREATED")
                    .action_detail("ResourceType Created "+resourceTypeRequest.getResourceType())
                    .action_detail_handle("ActionDetailBO:"+resourceTypeRequest.getSite()+","+"RESOURCETYPE-CREATED"+resourceTypeRequest.getResourceType()+":"+"com.rits.resourcetypeservice.Service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(resourceTypeRequest.getResourceType())
                    .operation_revision("*")
                    .txnId("RESOURCETYPE-CREATED"+String.valueOf(LocalDateTime.now())+resourceTypeRequest.getResourceType())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Create")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(activityLog));


            return ResponseEntity.ok( MessageModel.builder().message_details(createResourceType.getMessage_details()).response(postExtensionResourceType).build());
            } catch (ResourceTypeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }


    @PostMapping("/update")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MessageModel> update(@RequestBody ResourceTypeRequest resourceTypeRequest) throws Exception {
        MessageModel updateResourceType;
//            try {
//                ResourceType updatedResourceType = resourceTypeServiceImpl.update(resourceTypeRequest);
//                return ResponseEntity.ok(updatedResourceType);

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(resourceTypeRequest.getSite()).hookPoint("PRE").activity("resourcetype-service").hookableMethod("update").request(objectMapper.writeValueAsString(resourceTypeRequest)).build();
        String preExtensionResponse = resourceTypeServiceImpl.callExtension(preExtension);
        ResourceTypeRequest preExtensionResourceType = objectMapper.readValue(preExtensionResponse, ResourceTypeRequest.class);

        try {
        updateResourceType = resourceTypeServiceImpl.update(preExtensionResourceType);
        Extension postExtension = Extension.builder().site(resourceTypeRequest.getSite()).hookPoint("POST").activity("resourcetype-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateResourceType.getResponse())).build();
        String postExtensionResponse = resourceTypeServiceImpl.callExtension(postExtension);
        ResourceType postExtensionResourceType = objectMapper.readValue(postExtensionResponse, ResourceType.class);

            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(resourceTypeRequest.getSite())
                    .change_stamp("null")
                    .action_code("RESOURCETYPE-UPDATED")
                    .action_detail("ResourceType Updated " + resourceTypeRequest.getResourceType())
                    .action_detail_handle("ActionDetailBO:" + resourceTypeRequest.getSite() + "," + "RESOURCETYPE-UPDATED" + resourceTypeRequest.getResourceType() + ":" + "com.rits.resourcetypeservice.Service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(resourceTypeRequest.getResourceType())
                    .operation_revision("*")
                    .txnId("RESOURCETYPE-UPDATED" + String.valueOf(LocalDateTime.now()) + resourceTypeRequest.getResourceType())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Update")
                    .topic("audit-log")
                    .build();
            eventPublisher.publishEvent(new ProducerEvent(activityLog));

        return ResponseEntity.ok( MessageModel.builder().message_details(updateResourceType.getMessage_details()).response(postExtensionResourceType).build());

            } catch (ResourceTypeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

//    {
//        "site":"RITS"
//    }



    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ResourceTypeList>> getTop50ResourceType(@RequestBody ResourceTypeRequest resourceTypeRequest) {
        if (resourceTypeRequest.getSite() != null && !resourceTypeRequest.getSite().isEmpty()) {
            try {
                List<ResourceTypeList> top50Records = resourceTypeServiceImpl.getLast50RecordsOrderByCreatedDate(resourceTypeRequest.getSite());
                return ResponseEntity.ok(top50Records);
            } catch (ResourceTypeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceTypeException(2403,resourceTypeRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "resourceType":"GROUP1"
//    }

    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isResourceTypeExist(@RequestBody ResourceTypeRequest resourceTypeRequest) {
        if (resourceTypeRequest.getSite() != null && !resourceTypeRequest.getSite().isEmpty()) {
            try {
                Boolean  isResourceTypeExits = resourceTypeServiceImpl.isResourceTypeExist(resourceTypeRequest.getResourceType(), resourceTypeRequest.getSite());
                return ResponseEntity.ok(isResourceTypeExits);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceTypeException(2403,resourceTypeRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "resourceType":"GROUP1"
//    }

    @PostMapping("/retrieveByResourceType")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResourceType> retrieveByResourceType(@RequestBody ResourceTypeRequest resourceTypeRequest){
            if (resourceTypeRequest.getSite() != null && !resourceTypeRequest.getSite().isEmpty()) {
                try {
                    ResourceType retrieveByResourceType = resourceTypeServiceImpl.retrieveByResourceType(resourceTypeRequest.getResourceType(), resourceTypeRequest.getSite());
                    return ResponseEntity.ok(retrieveByResourceType);
                } catch (ResourceTypeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        throw new ResourceTypeException(2403,resourceTypeRequest.getSite());
    }

//    {
//    "site":"RITS",
//    "resourceType":"GROUP1"
//    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Response> deleteResourceType(@RequestBody ResourceTypeRequest resourceTypeRequest) {

                try {
                    Response deletedResourceType = resourceTypeServiceImpl.deleteResourceType(resourceTypeRequest.getResourceType(),resourceTypeRequest.getSite());
                    AuditLogRequest activityLog = AuditLogRequest.builder()
                            .site(resourceTypeRequest.getSite())
                            .change_stamp("null")
                            .action_code("RESOURCETYPE-DELETED")
                            .action_detail("ResourceType Deleted "+resourceTypeRequest.getResourceType())
                            .action_detail_handle("ActionDetailBO:"+resourceTypeRequest.getSite()+","+"RESOURCETYPE-DELETED"+resourceTypeRequest.getResourceType() +":"+"com.rits.resourcetypeservice.Service")
                            .date_time(String.valueOf(LocalDateTime.now()))
                            .userId(resourceTypeRequest.getResourceType())
                            .operation_revision("*")
                            .txnId("RESOURCETYPE-DELETED"+String.valueOf(LocalDateTime.now())+resourceTypeRequest.getResourceType())
                            .created_date_time(String.valueOf(LocalDateTime.now()))
                            .category("Delete")
                            .topic("audit-log")
                            .build();
                    eventPublisher.publishEvent(new ProducerEvent(activityLog));

                    return ResponseEntity.ok(deletedResourceType);
                } catch (ResourceTypeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
    }

//    {
//        "site":"RITS"
//    }

    @PostMapping("/retrieveAllResourceType")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<AvailableResourceType>> retrieveAllResourceType(@RequestBody ResourceTypeRequest resourceTypeRequest) {
            if (resourceTypeRequest.getSite() != null && !resourceTypeRequest.getSite().isEmpty()) {
                try {
                    List<AvailableResourceType> allResourceType = resourceTypeServiceImpl.retrieveAllResourceType(resourceTypeRequest.getSite());
                    return ResponseEntity.ok(allResourceType);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        throw new ResourceTypeException(2403,resourceTypeRequest.getSite());
    }

//    {
//        "site":"RITS",
//        "resourceType":"GROUP1"
//    }

    @PostMapping("/retrieveResourceTypeList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ResourceTypeList>> retrieveResourceTypeList(@RequestBody ResourceTypeRequest resourceTypeRequest) {
            if (resourceTypeRequest.getSite() != null && !resourceTypeRequest.getSite().isEmpty()) {
                try {
                    List<ResourceTypeList> getResourceTypeList = resourceTypeServiceImpl.retrieveResourceTypeList(resourceTypeRequest.getSite(),resourceTypeRequest.getResourceType());
                    return ResponseEntity.ok(getResourceTypeList);
                } catch (ResourceTypeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        throw new ResourceTypeException(2403,resourceTypeRequest.getSite());


    }

//    {
//        "site":"RITS",
//        "resourceType":"GROUP1",
//        "resource":[
//            "resource1","resource2"]
//    }

    @PostMapping("/remove")
    @ResponseStatus(HttpStatus.OK)
    public Boolean removeResourceFromResourceType(@RequestBody AssociateResourceToResourceType associateResourceToResourceType) {
            if (associateResourceToResourceType.getSite() != null && !associateResourceToResourceType.getSite().isEmpty()) {
                try {
                    Boolean removedResource = resourceTypeServiceImpl.removeResourceFromResourceType(associateResourceToResourceType.getResourceType(),associateResourceToResourceType.getResource(),associateResourceToResourceType.getSite());
                    return (removedResource);
                } catch (ResourceTypeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        throw new ResourceTypeException(2403,associateResourceToResourceType.getSite());

    }

//      {
//        "site":"RITS",
//        "resourceType":"GROUP1",
//        "resource":[
//            "resource1","resource2"]
//    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.OK)
    public Boolean associateResourceToResourceType(@RequestBody AssociateResourceToResourceType associate) {
        try {
            Boolean associatedResource = resourceTypeServiceImpl.associateResourceToResourceType(associate.getResourceType(), associate.getResource(),associate.getSite());
            return (associatedResource);
        } catch (ResourceTypeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    {
//        "site":"RITS"
//    }

    @PostMapping("/availableResources")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AvailableResource> availableResources(@RequestBody ResourceTypeRequest resourceTypeRequest)
    {
        if (resourceTypeRequest.getSite() != null && !resourceTypeRequest.getSite().isEmpty()) {
            try {
                AvailableResource availableResource = resourceTypeServiceImpl.availableResources(resourceTypeRequest);
                return ResponseEntity.ok(availableResource);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceTypeException(2403,resourceTypeRequest.getSite());
    }

    @PostMapping("/getAllResourcesByResourceType")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> getAllResourcesByResourceType(@RequestBody ResourceTypeRequest resourceTypeRequest)
    {
        if (StringUtils.isBlank(resourceTypeRequest.getSite()))
            throw new ResourceTypeException(2403,resourceTypeRequest.getSite());

        try {
            List<com.rits.resourceservice.dto.ResourceListResponse> resourceList = resourceServiceImpl.retrieveResourceList(resourceTypeRequest.getSite(), null);
            MessageModel availableResources = resourceTypeServiceImpl.getAllResourcesByResourceType(resourceTypeRequest, resourceList);
            return ResponseEntity.ok(availableResources);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
