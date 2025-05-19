package com.rits.batchnodoneservice.service;

import com.rits.batchnodoneservice.dto.BatchNoDoneRequest;
import com.rits.batchnodoneservice.model.MessageModel;
import com.rits.batchnodoneservice.model.BatchNoDone;

import java.util.List;

public interface BatchNoDoneService {

    MessageModel create(BatchNoDoneRequest request) throws Exception;

    MessageModel update(BatchNoDoneRequest request);

    MessageModel delete(BatchNoDoneRequest request);

    BatchNoDone retrieve(BatchNoDoneRequest request);

    List<BatchNoDone> retrieveAll(String site);

    List<BatchNoDone> retrieveTop50(String site);

    boolean isBatchNoDoneExist(String site, String batchNo);

    BatchNoDone getBySiteAndBatchNoHeaderBOAndOrderNoAndPhaseIdAndOperationAndResource(String site, String batchNoHeaderBO, String orderNumber, String phaseId, String operation, String resource);


}
