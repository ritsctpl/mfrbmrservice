package com.rits.checkhook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rits.checkhook.dto.MessageModel;
import com.rits.pcucompleteservice.dto.PcuCompleteReq;
import com.rits.pcucompleteservice.dto.PcuCompleteRequestInfo;
import com.rits.pcucompleteservice.dto.RequestList;
import com.rits.signoffservice.dto.SignOffRequestList;
import com.rits.startservice.dto.StartRequestList;

public interface CheckHookService {
    MessageModel isMandatoryDataCollected(PcuCompleteReq pcuCompleteReq);
    MessageModel isAllBuyOffApproved(PcuCompleteReq pcuCompleteReq);
    boolean completeCheckHooks(RequestList requestList) throws CloneNotSupportedException;
    boolean startCheckHook(StartRequestList requestList) throws Exception;
    boolean signOffCheckHook(SignOffRequestList requestList) throws Exception;

    RequestList completeCheckHook(RequestList pcuRequestList) throws CloneNotSupportedException, JsonProcessingException;

    MessageModel isAllNcClosed(PcuCompleteReq pcuCompleteReq);

    MessageModel isAllComponentAssembled(PcuCompleteReq pcuCompleteReqWithBO);
    MessageModel userCertificationHook(String user, String operation, String site);
}
