package com.rits.batchnoinwork.service;

import com.rits.batchnoinwork.dto.BatchNoWorkQtyResponse;
import com.rits.batchnoinwork.dto.InWorkResponse;
import com.rits.batchnoinwork.model.BatchNoInWork;
import com.rits.batchnoinwork.model.MessageModel;
import com.rits.batchnoinwork.dto.BatchNoInWorkRequest;

import java.util.List;


public interface BatchNoInWorkService {
    public MessageModel createBatchNoInWork(BatchNoInWorkRequest batchNoInWorkRequest)throws Exception;

    MessageModel updateBatchNoInWork(BatchNoInWorkRequest batchNoInWorkRequest)throws Exception;

    MessageModel deleteBatchNoInWork(BatchNoInWorkRequest batchNoInWorkRequest)throws Exception;

    MessageModel unDeleteBatchNoInWork(BatchNoInWorkRequest batchNoInWorkRequest)throws Exception;

    BatchNoInWork retrieve(BatchNoInWorkRequest batchNoInWorkRequest)throws Exception;

    List<BatchNoInWork> retrieveAll(String site)throws Exception;

    List<BatchNoInWork> retrieveTop50(String site)throws Exception;

    boolean isBatchNoInWorkExist(String site, String batchNo) throws Exception;

    BatchNoInWork getBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUser(String site, String batchNo, String phaseId, String operation, String resource, String user, String orderNo);

    BatchNoInWork getBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUser(String site, String batchNoHeaderBO, String phaseId, String operation, String resource, String user);
    List<InWorkResponse> getRecordUsingFilters(BatchNoInWorkRequest request) throws Exception;
    List<BatchNoInWork> getBatchInWorkList(BatchNoInWorkRequest request) throws Exception;

    void delete(BatchNoInWork batchNoInWork);

    boolean updateQualityApproval(String site, String operation, String batchNo);

    BatchNoWorkQtyResponse getBatchNoInWorkByPhaseAndOperation(BatchNoInWorkRequest request);

    public boolean existsBySiteAndActiveAndResource(String site,int active, String resource);
}
