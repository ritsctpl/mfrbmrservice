package com.rits.routingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.kafkaservice.ProducerEvent;
import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.routingservice.dto.*;
import com.rits.routingservice.exception.RoutingException;
import com.rits.routingservice.model.*;
import com.rits.routingservice.service.RoutingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/routing-service")
public class RoutingController {
    private final RoutingServiceImpl routingService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("create")
    public ResponseEntity<?> createRouting(@RequestBody RoutingRequest routingRequest) throws Exception {
        RoutingMessageModel createRouting;

        try {
            if (routingRequest.getRouting() == null || routingRequest.getRouting().isEmpty() || routingRequest.getRouting().equals(" ")) {
                throw new RoutingException(513);
            } else {
                createRouting = routingService.createRouting(routingRequest);

                AuditLogRequest activityLog = routingService.createAuditLog(routingRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(RoutingMessageModel.builder().message_details(createRouting.getMessage_details()).response(createRouting.getResponse()).build());
            }
        } catch (RoutingException routingException) {
            throw routingException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("update")
    public ResponseEntity<RoutingMessageModel> updateRouting(@RequestBody @NotNull RoutingRequest routingRequest) throws Exception {
        RoutingMessageModel updateRouting = null;
        if (routingRequest.getSite() != null && !routingRequest.getSite().isEmpty() &&
                routingRequest.getRouting() != null && !routingRequest.getRouting().isEmpty() &&
                routingRequest.getVersion() != null && !routingRequest.getVersion().isEmpty() &&
                routingRequest.getStatus() != null && !routingRequest.getStatus().isEmpty() &&
                routingRequest.getRoutingType() != null && !routingRequest.getRoutingType().isEmpty()
        ) {
            try {
                if (routingRequest.getRouting() == null || routingRequest.getRouting().isEmpty() || routingRequest.getRouting().equals(" ")) {
                    throw new RoutingException(513);
                }

                else {

                    updateRouting = routingService.updateRouting(routingRequest);

                    AuditLogRequest activityLog = routingService.updateAuditLog(routingRequest);
                    eventPublisher.publishEvent(new ProducerEvent(activityLog));

                    return ResponseEntity.ok(updateRouting);
                }
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(503);
    }

    @PostMapping("delete")
    public ResponseEntity<RoutingMessageModel> deleteRouting(@RequestBody RoutingRequest routingRequest) throws Exception {
        RoutingMessageModel deleteResponse;
        if (routingRequest.getSite() != null && !routingRequest.getSite().isEmpty()) {

            try {
                deleteResponse = routingService.deleteRouting(routingRequest.getSite(), routingRequest.getRouting(), routingRequest.getVersion(), routingRequest.getUserId());

                AuditLogRequest activityLog = routingService.deleteAuditLog(routingRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(deleteResponse);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }

    @PostMapping("retrieve")
    public ResponseEntity<Routing> retrieveRouting(@RequestBody RoutingRequest routingRequest) throws Exception {
        Routing retrieveRouting;
        if (routingRequest.getSite() != null && !routingRequest.getSite().isEmpty()) {

            try {
                retrieveRouting = routingService.retrieveRouting(routingRequest.getSite(), routingRequest.getRouting(), routingRequest.getVersion());

                return ResponseEntity.ok(retrieveRouting);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }

    @PostMapping("retrieveAll")
    public ResponseEntity<RoutingResponseList> getRoutingList(@RequestBody RoutingRequest routingRequest) {
        RoutingResponseList retrieveAllRouting;
        if (routingRequest.getSite() != null && !routingRequest.getSite().isEmpty()) {

            try {
                retrieveAllRouting = routingService.getRoutingList(routingRequest.getSite(), routingRequest.getRouting());
                return ResponseEntity.ok(retrieveAllRouting);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }

    @PostMapping("retrieveTop50")
    public ResponseEntity<RoutingResponseList> getRoutingListByCreationDate(@RequestBody RoutingRequest routingRequest) {
        RoutingResponseList retrieveTop50Routing;
        if (routingRequest.getSite() != null && !routingRequest.getSite().isEmpty()) {

            try {
                retrieveTop50Routing = routingService.getRoutingListByCreationDate(routingRequest.getSite());
                return ResponseEntity.ok(retrieveTop50Routing);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }

    @PostMapping("isExist")
    public ResponseEntity<Boolean> isExist(@RequestBody RoutingRequest routingRequest) {
        Boolean isExist;
        if (routingRequest.getSite() != null && !routingRequest.getSite().isEmpty()) {

            try {
                isExist = routingService.isExist(routingRequest.getSite(), routingRequest.getRouting(), routingRequest.getVersion());
                return ResponseEntity.ok(isExist);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }


    @PostMapping("retrieveBySite")
    public ResponseEntity<List<RoutingResponse>> getAvailableRouting(@RequestBody RoutingRequest routingRequest) {
        List<RoutingResponse> getAvailableRouting;
        if (routingRequest.getSite() != null && !routingRequest.getSite().isEmpty()) {

            try {
                getAvailableRouting = routingService.getAllRouting(routingRequest.getSite());
                return ResponseEntity.ok(getAvailableRouting);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }

    @PostMapping("findNextStepIDDetails")
    public ResponseEntity<RoutingMessageModel> findNextStepIDDetails(@RequestBody RoutingStepRequest routingStepRequest) {
        RoutingMessageModel findNextStepIDDetails;
        if (routingStepRequest.getSite() != null && !routingStepRequest.getSite().isEmpty()) {

            try {
                findNextStepIDDetails = routingService.findNextStepIDDetails(routingStepRequest.getSite(), routingStepRequest.getRouting(), routingStepRequest.getVersion(), routingStepRequest.getOperation(), routingStepRequest.getOperationVersion());
                return ResponseEntity.ok(findNextStepIDDetails);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }

    @PostMapping("findNextStepIDDetailsOfParentStepId")
    public ResponseEntity<RoutingMessageModel> findNextStepIDDetailsOfParentStepId(@RequestBody RoutingStepRequest routingStepRequest) {
        RoutingMessageModel findNextStepIDDetailsOfParentStepId;
        if (routingStepRequest.getSite() != null && !routingStepRequest.getSite().isEmpty()) {

            try {
                findNextStepIDDetailsOfParentStepId = routingService.findNextStepIDDetailsOfParentStepId(routingStepRequest.getSite(), routingStepRequest.getRouting(), routingStepRequest.getVersion(), routingStepRequest.getStepId());
                return ResponseEntity.ok(findNextStepIDDetailsOfParentStepId);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }

    @PostMapping("getOperationQueueList")
    public ResponseEntity<List<RoutingStep>> getOperationQueueList(@RequestBody RoutingStepRequest routingStepRequest) {
        List<RoutingStep> getOperationQueueList;
        if (routingStepRequest.getSite() != null && !routingStepRequest.getSite().isEmpty()) {

            try {
                getOperationQueueList = routingService.getOperationQueueList(routingStepRequest.getSite(), routingStepRequest.getRouting(), routingStepRequest.getVersion(), routingStepRequest.getOperation());
                return ResponseEntity.ok(getOperationQueueList);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }

    @PostMapping("getOperationNextStepID")
    public ResponseEntity<String> getOperationNextStepID(@RequestBody RoutingStepRequest routingStepRequest) {
        String getOperationNextStepID;
        if (routingStepRequest.getSite() != null && !routingStepRequest.getSite().isEmpty()) {

            try {
                getOperationNextStepID = routingService.getOperationNextStepID(routingStepRequest.getSite(), routingStepRequest.getRouting(), routingStepRequest.getVersion(), routingStepRequest.getOperation());
                return ResponseEntity.ok(getOperationNextStepID);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }

    @PostMapping("getStepDetails")
    public ResponseEntity<RoutingMessageModel> getStepDetails(@RequestBody RoutingStepRequest routingStepRequest) {
        RoutingMessageModel getStepDetails;
        if (routingStepRequest.getSite() != null && !routingStepRequest.getSite().isEmpty()) {

            try {
                getStepDetails = routingService.getStepDetails(routingStepRequest.getSite(), routingStepRequest.getRouting(), routingStepRequest.getVersion(), routingStepRequest.getStepId());
                return ResponseEntity.ok(getStepDetails);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }

    @PostMapping("findStepDetailsByNextStepId")
    public ResponseEntity<RoutingMessageModel> findStepDetailsByNextStepId(@RequestBody RoutingStepRequest routingStepRequest) {
        RoutingMessageModel findStepDetailsByNextStepId;

        try {
            findStepDetailsByNextStepId = routingService.findStepDetailsByNextStepId(routingStepRequest.getSite(), routingStepRequest.getRouting(), routingStepRequest.getVersion(), routingStepRequest.getOperation(), routingStepRequest.getStepId(), routingStepRequest.getOperationVersion());
            return ResponseEntity.ok(findStepDetailsByNextStepId);
        } catch (RoutingException routingException) {
            throw routingException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("inUse")
    public ResponseEntity<Boolean> inUseUpdate(@RequestBody RoutingStepRequest routingStepRequest) {
        Boolean inUseUpdate;
        try {
            inUseUpdate = routingService.inUseUpdate(routingStepRequest.getSite(), routingStepRequest.getRouting(), routingStepRequest.getVersion());
            return ResponseEntity.ok(inUseUpdate);
        } catch (RoutingException routingException) {
            throw routingException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("retrievelivedata")
    public ResponseEntity<Routing> retrieveRoutingwithLiveRecord(@RequestBody RoutingRequest routingRequest) throws Exception {
        Routing retrieveRouting;
        if (routingRequest.getSite() != null && !routingRequest.getSite().isEmpty()) {

            try {
                retrieveRouting = routingService.retrieveRoutingwithLiveRecord(routingRequest.getSite(), routingRequest.getRouting(), routingRequest.getVersion());

                return ResponseEntity.ok(retrieveRouting);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RoutingException(1);
    }

    @PostMapping("retrieveByType")
    public ResponseEntity<List<RoutingType>> retrieveByType(@RequestBody RoutingRequest routingRequest) throws Exception {
        List<RoutingType> retrieveRouting;
        if (routingRequest.getSite() != null && !routingRequest.getSite().isEmpty()) {

            try {
                retrieveRouting = routingService.retrieveByType(routingRequest);
                return ResponseEntity.ok(retrieveRouting);
            } catch (RoutingException routingException) {
                throw routingException;
            }

        }
        throw new RoutingException(1);
    }
}
