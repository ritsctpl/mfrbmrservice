package com.rits.routingservice.service;

import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.routingservice.dto.Extension;
import com.rits.routingservice.dto.RoutingRequest;
import com.rits.routingservice.dto.RoutingResponse;
import com.rits.routingservice.dto.RoutingResponseList;
import com.rits.routingservice.model.Routing;
import com.rits.routingservice.model.RoutingMessageModel;
import com.rits.routingservice.model.RoutingStep;
import com.rits.routingservice.model.RoutingType;

import java.util.List;

public interface RoutingService {
    RoutingMessageModel createRouting(RoutingRequest routingRequest) throws Exception;

    RoutingMessageModel updateRouting(RoutingRequest routingRequest) throws Exception;

    Routing retrieveRouting(String site, String routing, String version) throws Exception;

    RoutingResponseList getRoutingList(String site, String routing) throws Exception;

    RoutingResponseList getRoutingListByCreationDate(String site) throws Exception;

    RoutingMessageModel deleteRouting(String site, String routing, String version, String userId) throws Exception;

    boolean isExist(String site, String routing, String version) throws Exception;

    String callExtension(Extension extension) throws Exception;

    List<RoutingResponse> getAllRouting(String site);

    RoutingMessageModel findNextStepIDDetails(String site, String routing, String version, String operation, String operationVersion) throws Exception;

    RoutingMessageModel findNextStepIDDetailsOfParentStepId(String site, String routing, String version, String stepId) throws Exception;

    List<RoutingStep> getOperationQueueList(String site, String routing, String version, String operation) throws Exception;

    String getOperationNextStepID(String site, String routing, String version, String operation) throws Exception;

    RoutingMessageModel getStepDetails(String site, String routing, String version, String stepId) throws Exception;

    RoutingMessageModel findStepDetailsByNextStepId(String site, String routing, String version, String operation, String nextStepId, String operationVersion) throws Exception;


    boolean inUseUpdate(String site, String routing, String version) throws Exception;


    Routing retrieveRoutingwithLiveRecord(String site, String routing, String version) throws Exception;

    AuditLogRequest createAuditLog(RoutingRequest routingRequest);

    AuditLogRequest updateAuditLog(RoutingRequest routingRequest);

    AuditLogRequest deleteAuditLog(RoutingRequest routingRequest);

    List<RoutingType> retrieveByType(RoutingRequest routingRequest);
}
