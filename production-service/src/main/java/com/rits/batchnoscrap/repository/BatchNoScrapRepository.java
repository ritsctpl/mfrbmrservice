package com.rits.batchnoscrap.repository;

import com.rits.batchnoscrap.dto.BatchNoScrapQtyResponse;
import com.rits.batchnoscrap.model.BatchNoScrap;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BatchNoScrapRepository extends MongoRepository<BatchNoScrap, String> {
    List<BatchNoScrap> findByActiveAndSite(int i, String site);
    BatchNoScrap findByActiveAndSiteAndBatchNoAndOperationAndResourceAndPhaseId(int i, String site, String batchNo, String operation, String resource, String phaseId);
    List<BatchNoScrap> findByActiveAndSiteAndBatchNo(int i, String site, String batchNo);

    Optional<BatchNoScrap> findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(String site, String batchNo, String phaseId, String operation, String resource, int i);


    BatchNoScrapQtyResponse findBySiteAndBatchNoAndOrderNumberAndPhaseIdAndOperation(String site, String batchNo, String orderNumber, String phaseId, String operation);

    BatchNoScrapQtyResponse findBySiteAndBatchNoAndPhaseIdAndOperation(String site, String batchNo, String phaseId, String operation);

    BatchNoScrapQtyResponse findBySiteAndOrderNumberAndPhaseIdAndOperation(String site, String orderNumber, String phaseId, String operation);
    BatchNoScrap findByActiveAndSiteAndBatchNoAndOperationAndResourceAndPhaseIdAndOrderNumber(int i, String site, String batchNo, String operation, String resource, String phaseId, String orderNo);
    Optional<BatchNoScrap> findBySiteAndOrderNumberAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(String site, String OrderNo, String batchNo, String phaseId, String operation, String resource, int i);
    List<BatchNoScrap> findBySiteAndOrderNumberAndBatchNoAndActive(String site, String OrderNo, String batchNo, int active);
}
