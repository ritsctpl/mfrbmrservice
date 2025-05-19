package com.rits.batchnoinqueue.service;

import com.rits.batchnoinqueue.dto.BatchNoInQueueRequest;

import com.rits.batchnoinqueue.dto.BatchNoInQueueResponse;
import com.rits.batchnoinqueue.dto.QuantityInQueueResponse;
import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinqueue.model.MessageModel;
import com.rits.batchnorecipeheaderservice.dto.BatchNoRecipeHeaderReq;
import com.rits.batchnorecipeheaderservice.exception.BatchNoRecipeHeaderException;

import java.util.List;

public interface BatchNoInQueueService {
    public MessageModel createBatchNoInQueue(BatchNoInQueueRequest batchNoInQueueRequest)throws Exception;

    public MessageModel updateBatchNoInQueue(BatchNoInQueueRequest batchNoInQueueRequest)throws Exception;

    MessageModel deleteBatchNoInQueue(BatchNoInQueueRequest request) throws Exception;

    BatchNoInQueue retrieve(BatchNoInQueueRequest batchNoInQueueRequest) throws Exception;

    List<BatchNoInQueue> retrieveAll(String site) throws Exception;

    List<BatchNoInQueue> retrieveTop50(String site) throws Exception;

    public boolean isBatchNoInQueueExist(String site, String batchNo) throws Exception;

    BatchNoInQueue getBySiteAndBatchNoHeaderAndPhaseAndOperation(String site, String batchNoHeaderBO, String phaseId, String operation);

    void delete(BatchNoInQueue deleteBatchNoInQueue);

    BatchNoInQueueResponse getBatchNoRecords(BatchNoInQueueRequest request) throws Exception;
    BatchNoInQueueResponse getInQueueForBatchRecipeByFilters(BatchNoInQueueRequest request) throws Exception;
    List<BatchNoInQueue> getBatchInQueueList(BatchNoInQueueRequest request) throws Exception;

    boolean updateQualityApproval(String site, String operation, String batchNo);

    List<BatchNoInQueue> getBatchInQueueListForWorkList(BatchNoInQueueRequest request) throws Exception;
    QuantityInQueueResponse getBatchNoInQueueByPhaseAndOperation(BatchNoInQueueRequest request);
}
