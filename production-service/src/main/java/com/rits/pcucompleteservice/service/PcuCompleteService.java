package com.rits.pcucompleteservice.service;

import com.rits.pcucompleteservice.dto.*;
import com.rits.pcucompleteservice.model.MessageModel;

import java.util.List;

public interface    PcuCompleteService {

    public MessageModel insert(PcuCompleteReq pcuCompleteReq) throws Exception;
    public MessageModel update(PcuCompleteReq pcuCompleteReq) throws  Exception;
    public boolean delete(PcuCompleteReq pcuCompleteReq) throws  Exception;

    Boolean deleteByPcu(PcuCompleteReq pcuCompleteReq);

    Boolean unDeleteByPcu(PcuCompleteReq pcuCompleteReq);

    public MessageModel complete(RequestList pcuComplete) throws Exception;

    public MessageModel insertOrUpdateInPcuInQueue(PcuCompleteReq pcuComplete) throws Exception;
    public MessageModel updateOrDeleteInPcuInWork(PcuCompleteReq pcuComplete) throws Exception;
    public Boolean insertInPcuDone(PcuCompleteRequestInfo pcuComplete)throws Exception;

    public RoutingStep findStepDetailsByNextStepId(RoutingRequest routingRequest) throws Exception;

    public String callExtension(Extension extension) throws Exception;
    public PcuCompleteReq retrieve(PcuCompleteReq pcuCompleteReq) throws Exception;

    public List<PcuCompleteReq> retrieveByOperation(PcuCompleteReq pcuCompleteReq) throws Exception;
    public List<PcuCompleteReq> retrieveByOperationAndShopOrder(PcuCompleteReq pcuCompleteReq) throws Exception;


}
