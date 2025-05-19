package com.rits.resourceservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.kafkaservice.ProducerEvent;
import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.resourceservice.dto.*;
import com.rits.resourceservice.exception.ResourceException;
import com.rits.resourceservice.model.CheckStatusRequest;
import com.rits.resourceservice.model.MessageModel;
import com.rits.resourceservice.model.Resource;
import com.rits.resourceservice.repository.ResourceServiceRepository;
import com.rits.resourceservice.service.ResourceServiceImpl;
import com.rits.resourcetypeservice.Model.ResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/resource-service")
@EnableKafka
public class ResourceController {

    private final ResourceServiceImpl resourceServiceImpl;
    private final ApplicationEventPublisher eventPublisher;
    private final ResourceServiceRepository resourceServiceRepository;


    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createResource(@RequestBody ResourceRequest resourceRequest) throws JsonProcessingException {
        MessageModel createResource;
        if (resourceRequest.getSite() != null && !resourceRequest.getSite().isEmpty()) {
        try {
            createResource = resourceServiceImpl.createResource(resourceRequest);
            AuditLogRequest activityLog = resourceServiceImpl.createAuditLog(resourceRequest);
            eventPublisher.publishEvent(new ProducerEvent(activityLog));
            return ResponseEntity.ok(MessageModel.builder().message_details(createResource.getMessage_details()).response(createResource.getResponse()).build());
        } catch (ResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
        throw new ResourceException(2303, resourceRequest.getSite());
    }


    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> updateResource(@RequestBody ResourceRequest resourceRequest) throws JsonProcessingException {
        MessageModel updateResource;
        if (resourceRequest.getSite() != null && !resourceRequest.getSite().isEmpty()) {
        try {
            updateResource = resourceServiceImpl.updateResource(resourceRequest);
            AuditLogRequest activityLog = resourceServiceImpl.updateAuditLog(resourceRequest);
            eventPublisher.publishEvent(new ProducerEvent(activityLog));
            return ResponseEntity.ok(MessageModel.builder().message_details(updateResource.getMessage_details()).response(updateResource.getResponse()).build());
        } catch (ResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
        throw new ResourceException(2303, resourceRequest.getSite());
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> deleteResource(@RequestBody ResourceRequest resourceRequest) throws JsonProcessingException {
        MessageModel deleteResource;
        if (resourceRequest.getSite() != null && !resourceRequest.getSite().isEmpty()) {
        try {
            deleteResource = resourceServiceImpl.deleteResource(resourceRequest.getSite(), resourceRequest.getResource(), resourceRequest.getUserId());
            AuditLogRequest activityLog = resourceServiceImpl.deleteAuditLog(resourceRequest);
            eventPublisher.publishEvent(new ProducerEvent(activityLog));
            return ResponseEntity.ok(deleteResource);
        } catch (ResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
        throw new ResourceException(2303, resourceRequest.getSite());
    }



    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ResourceListResponse>> retrieveTop50Resource(@RequestBody ResourceRequest resourceRequest) {
        if (resourceRequest.getSite() != null && !resourceRequest.getSite().isEmpty()) {
            try {
                List<ResourceListResponse> top50Record = resourceServiceImpl.retrieveTop50Resource(resourceRequest.getSite());
                return ResponseEntity.ok(top50Record);
            } catch (ResourceException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceException(2303, resourceRequest.getSite());
    }

    @PostMapping("/retrieveBySite")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ResourceListResponse>> getAllResource(@RequestBody ResourceTypeRequest resourceTypeRequest) {
        if (resourceTypeRequest.getSite() != null && !resourceTypeRequest.getSite().isEmpty()) {
            try {
                List<ResourceListResponse> getAllResource = resourceServiceImpl.getAllResponse(resourceTypeRequest.getSite());
                return ResponseEntity.ok(getAllResource);
            } catch (ResourceException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceException(2303, resourceTypeRequest.getSite());
    }

    @PostMapping("/retrieveByResource")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> retrieveResource(@RequestBody ResourceRequest resourceRequest) throws JsonProcessingException {
        Resource retrieveResource;
        if (resourceRequest.getSite() != null && !resourceRequest.getSite().isEmpty()) {
        try {
            retrieveResource = resourceServiceImpl.retrieveResource(resourceRequest.getSite(), resourceRequest.getResource());
            return ResponseEntity.ok(retrieveResource);
        } catch (ResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
        throw new ResourceException(2303, resourceRequest.getSite());
    }

    @PostMapping("/retrieveBySiteAndErpEquipmentNumber")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> retrieveBySiteAndErpEquipmentNumber(@RequestBody ResourceRequest resourceRequest) throws JsonProcessingException {
        Resource retrieveResource;
        if (resourceRequest.getSite() != null && !resourceRequest.getSite().isEmpty()) {
            try {
                retrieveResource = resourceServiceImpl.retrieveBySiteAndErpEquipmentNumber(resourceRequest.getSite(), resourceRequest.getErpEquipmentNumber());
                return ResponseEntity.ok(retrieveResource);
            } catch (ResourceException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceException(2303, resourceRequest.getSite());
    }


    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isResourceExist(@RequestBody ResourceRequest resourceRequest) {
        if (resourceRequest.getSite() != null && !resourceRequest.getSite().isEmpty()) {
            try {
                Boolean isResourceExist = resourceServiceImpl.isResourceExist(resourceRequest.getResource(), resourceRequest.getSite());
                return ResponseEntity.ok(isResourceExist);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceException(2303, resourceRequest.getSite());
    }

    @PostMapping("/isExistByHandle")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isExistByHandle(@RequestBody ResourceRequest resourceRequest) {
        if (resourceRequest.getSite() != null && !resourceRequest.getSite().isEmpty()) {
            try {
                Boolean isResourceExist = resourceServiceImpl.isResourceExistByHandle(resourceRequest.getSite(), resourceRequest.getResource());
                return ResponseEntity.ok(isResourceExist);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceException(2303, resourceRequest.getSite());
    }

    @PostMapping("/retrieveResourceList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ResourceListResponse>> retrieveResourceList(@RequestBody ResourceRequest resourceRequest) {
        if (resourceRequest.getSite() != null && !resourceRequest.getSite().isEmpty()) {
            try {
                List<ResourceListResponse> retrievedResourceList = resourceServiceImpl.retrieveResourceList(resourceRequest.getSite(), resourceRequest.getResource());
                return ResponseEntity.ok(retrievedResourceList);
            } catch (ResourceException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceException(2303, resourceRequest.getSite());
    }


    @PostMapping("/add")
    @ResponseStatus(HttpStatus.OK)
    public Boolean associateResourceTypeToResource(@RequestBody AssociateResourceToResourceType associate) {
        if (associate.getSite() != null && !associate.getSite().isEmpty()) {
            try {
                Boolean associatedResourceType = resourceServiceImpl.associateResourceTypeToResource(associate);
                return (associatedResourceType);
            } catch (ResourceException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceException(2303, associate.getSite());
    }


    @PostMapping("/remove")
    @ResponseStatus(HttpStatus.OK)
    public Boolean removeResourceFromResourceType(@RequestBody AssociateResourceToResourceType associate) {
        if (associate.getSite() != null && !associate.getSite().isEmpty()) {
            try {
                Boolean removedResourceType = resourceServiceImpl.removeResourceFromResourceType(associate);

                return removedResourceType;
            } catch (ResourceException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceException(2303, associate.getSite());
    }


    @PostMapping("/availableResourceType")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AvailableResourceTypeList> availableResourceType(@RequestBody ResourceTypeRequest resourceTypeRequest) {
        if (resourceTypeRequest.getSite() != null && !resourceTypeRequest.getSite().isEmpty()) {
            try {
                AvailableResourceTypeList availableResourceType = resourceServiceImpl.availableResourceType(resourceTypeRequest.getSite(), resourceTypeRequest.getResource());
                return ResponseEntity.ok(availableResourceType);
            } catch (ResourceException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResourceException(2303, resourceTypeRequest.getSite());
    }
    @PostMapping("/changeResourceStatus")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> checkSetUpStatusAndCallProductionLog(@RequestBody CheckStatusRequest checkStatusRequest) throws Exception
    {
        try {
            MessageModel changeStatusResponse = resourceServiceImpl.checkSetUpStatusAndCallProductionLog(checkStatusRequest);
            return ResponseEntity.ok(changeStatusResponse);
        } catch (ResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/getValidResourceList")
    @ResponseStatus(HttpStatus.OK)
    public List<ResourceListResponse> getActiveAndValidResources(@RequestBody ResourceRequest resourceRequest) {
        LocalDate currentDate = LocalDate.now();
        return resourceServiceRepository.findBySiteAndActive(resourceRequest.getSite(),1);
    }



//    @PostMapping("/retrieveResourceDiscription")
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<Map<String, String>> retrieveResourceDiscription(@RequestBody ResourceTypeRequest resourceTypeRequest) {//return type
//        if (resourceTypeRequest != null) {
//            try {
//                Map<String, String> getResourceDiscription = resourceServiceImpl.retrieveResourceDiscription(resourceTypeRequest);
//                return ResponseEntity.ok(getResourceDiscription);
//            } catch (ResourceException e) {
//                throw e;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//        throw new ResourceException(2303, resourceTypeRequest.getSite());
//    }
}
