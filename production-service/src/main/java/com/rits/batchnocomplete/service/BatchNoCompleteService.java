package com.rits.batchnocomplete.service;

import com.rits.batchnocomplete.dto.BatchNoCompleteDTO;
import com.rits.batchnocomplete.dto.BatchNoCompleteQty;
import com.rits.batchnocomplete.model.BatchNoComplete;
import com.rits.batchnocomplete.model.BatchNoCompleteMsgModel;

import java.util.List;

public interface BatchNoCompleteService {
    public BatchNoCompleteMsgModel create(BatchNoCompleteDTO batchNoCompleteRequest) throws Exception;
    public BatchNoCompleteMsgModel update(BatchNoCompleteDTO batchNoCompleteRequest) throws Exception;
    public BatchNoCompleteMsgModel delete(BatchNoCompleteDTO batchNoCompleteRequest) throws Exception;
    public BatchNoComplete retrieve(BatchNoCompleteDTO batchNoCompleteRequest) throws Exception;
    public List<BatchNoComplete> retrieveAll(String site) throws Exception;
    public List<BatchNoComplete> retrieveTop50(String site) throws Exception;
    public boolean isBatchNoCompleteExist(String site, String batchNo) throws Exception;

    BatchNoComplete getBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUser(String site, String batchNoHeaderBO, String phaseId, String operation, String resource, String user, String OrderNo);

    BatchNoCompleteQty getBatchNoCompleteByPhaseAndOperation(BatchNoCompleteDTO request);

}
