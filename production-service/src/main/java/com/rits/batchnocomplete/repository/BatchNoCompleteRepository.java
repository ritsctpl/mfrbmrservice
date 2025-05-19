package com.rits.batchnocomplete.repository;

import com.rits.batchnocomplete.dto.BatchNoCompleteQty;
import com.rits.batchnocomplete.model.BatchNoComplete;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchNoCompleteRepository extends MongoRepository<BatchNoComplete, String> {
    BatchNoComplete findBySiteAndActiveAndHandle(String site, int active, String handle);
    List<BatchNoComplete> findBySiteAndActive(String site, int active);
    List<BatchNoComplete> findTop50BySiteAndActive(String site, int active);

    boolean existsBySiteAndActiveAndBatchNo(String site, int i, String batchNo);

    List<BatchNoComplete> findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(String site, String batchNo, String phaseId, String operation, String resource, int i);

    BatchNoComplete findBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUserAndActive(String site, String batchNoHeaderBO, String phaseId, String operation, String resource, String user, int i);

    BatchNoCompleteQty findBySiteAndBatchNoAndOrderNumberAndPhaseIdAndOperation(String site, String batchNo, String orderNumber, String phaseId, String operation);

    BatchNoCompleteQty findBySiteAndBatchNoAndPhaseIdAndOperation(String site, String batchNo, String phaseId, String operation);

    BatchNoCompleteQty findBySiteAndOrderNumberAndPhaseIdAndOperation(String site, String orderNumber, String phaseId, String operation);
    BatchNoComplete findBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUserAndActiveAndOrderNumber(String site, String batchNoHeaderBO, String phaseId, String operation, String resource, String user, int i, String orderNo);
}