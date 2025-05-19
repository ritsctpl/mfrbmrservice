package com.rits.pcurouterheaderservice.service;

import com.rits.pcurouterheaderservice.dto.*;
import com.rits.pcurouterheaderservice.model.MessageModel;
import com.rits.pcurouterheaderservice.model.PcuBo;
import com.rits.pcurouterheaderservice.model.PcuRouterHeader;
import com.rits.pcurouterheaderservice.model.RoutingStep;

import java.util.List;

public interface PcuRouterHeaderService {
    public MessageModel createPcuRouterHeader(PcuROuterHeaderCreateRequest pcuROuterHeaderCreateRequest) throws Exception;

    public MessageModel updatePcuRouterHeader(PcuROuterHeaderCreateRequest pcuROuterHeaderCreateRequest) throws Exception;

    public PcuRouterHeader retrievePcuRouterHeader(PcuRouterHeaderRequest pcuRouterHeaderRequest) throws Exception;

    public boolean isParentRoute(String site, String Router, String version, String PCU) throws Exception;

    public List<RoutingStep> getCurrentStep(String site, String PCU) throws Exception;

    public boolean isEntryStep(String site, String pcuBo, String currentRouter, String version, String operation) throws Exception;

    public String getRoutingType(String site, String router, String version) throws Exception;

    public String getRoutingSubType(String site, String router, String version) throws Exception;

    public boolean isLastReportingStep(String site, String pcuBo, String currentRouter, String version, String operation,String operationVersion) throws Exception;

    public List<String> getCurrentRouter(String site, String PCU) throws Exception;

    public PcuInQueue pcuReleaseAtEntryStep(String site, PcuBo pcuBo, String router, String version,String userBO) throws Exception;

    public String callExtension(Extension extension) throws Exception;

    public EntryStep getAllEntryStep(String site, String pcuBo, String routing, String version) throws Exception;

    public EntryStep getEntryStep(String site, String pcuBo) throws Exception;

    public PcuInQueue placePCUQueueAtFirstOperation(String site, PcuBo pcuBo,String userBO) throws Exception;

    public PcuInQueue placePCUQueueForRoutingStep(String site, PcuBo pcuBo, String routing, String version, String childRouteBO,String userBO) throws Exception;

    public PcuInQueue placePCUQueueAtSpecificOperation(String site, PcuBo pcuBo, String routing, String version, String operation,String userBO) throws Exception;

    public RoutingStep getStepDetails(String site, String routing, String version, String operation, String pcuBo) throws Exception;

    public MessageModel getOperationQueueList(String site, String routing, String version, String operation, String pcuBo,String operationVersion) throws Exception;

    public MessageModel getOperationNextStepID(String site, String routing, String version, String operation, String pcuBo) throws Exception;

    public List<RoutingStep> getStepDetailsList(String site, String routing, String version, String operation, String pcuBo) throws Exception;

    public MessageModel updateNeedsToBeCompleted(String site, String pcuBo, String routing, String version, String operation, String stepID,String operationVersion) throws Exception;

    public PcuInQueue placePCUQueueForAllStep(String site, PcuBo pcuBo, String routing, String version,String userBO) throws Exception;

    public Boolean disableRecord(PcuRouterHeaderRequest pcuRouterHeaderRequest);

    Boolean enableRecord(PcuRouterHeaderRequest pcuRouterHeaderRequest);
}