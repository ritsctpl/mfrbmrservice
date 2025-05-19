package com.rits.startservice.service;

import com.rits.startservice.dto.*;
import com.rits.startservice.model.MessageModel;
import com.rits.startservice.model.PcuInWork;
import com.rits.startservice.model.PcuInWorkMessageModel;

import java.util.List;

public interface StartService {


    public List<PcuInWork> retrieveByOperationAndResource(StartRequest startRequest) throws Exception;

    public boolean pcuStart(StartRequestDetails startRequest) throws Exception;

   public List<RoutingStep> retrieveStepDetails(StartRequestDetails startRequest) throws Exception;

   public RoutingStep getRoutingStep(StartRequestDetails startRequest) throws Exception;

    public MessageModel start(StartRequestLists startRequestList) throws Exception;

    public PcuInWorkMessageModel createPcuInWork(StartRequestDetails startRequest)throws Exception;

    public PcuInWorkMessageModel updatePcuInWork(StartRequestDetails startRequest)throws Exception;

    List<StartRequestDetails> retrieveByPcuAndSite(StartRequestDetails startRequest) throws Exception;

    List<StartRequestDetails> retrieveDeletedPcu(StartRequestDetails startRequest) throws Exception;

    Boolean deletePcuFromAllOperations(StartRequestDetails startRequest);

    Boolean unDeletePcuFromAllOperations(StartRequestDetails startRequest);

    public StartRequestDetails retrievePcuInWorkByOperationAndItem(StartRequestDetails startRequest) throws Exception;
    public StartRequestDetails retrievePcuInWorkByOperation(StartRequestDetails startRequest) throws Exception;
    public StartRequestDetails retrievePcuInWorkByOperationAndResource(StartRequestDetails startRequest) throws Exception;

    public MessageModel deletePcuInWork(StartRequestDetails startRequest) throws Exception;

    public PcuList retrieveAll(StartRequestDetails startRequest) throws Exception;

//    public List<PcuInWork> retrieveByOperation(String site, String operation) throws Exception;

    public String callExtension(Extension extension);
    public List<StartRequestDetails> retrieveListByOperationAndResource(StartRequestDetails startRequest) throws Exception;
    public List<StartRequestDetails>  retrieveListByOperation(StartRequestDetails startRequest) throws Exception;
    public List<StartRequestDetails> getAllPcuByRoute(StartRequestDetails startRequest);
    public PcuInWorkMessageModel updateAllPcu(StartRequestDetails startRequest)throws Exception;


 List<StartRequestDetails> getAllInQueuePcuBySite(String site);
}

