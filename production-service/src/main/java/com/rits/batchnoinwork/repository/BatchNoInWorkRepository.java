package com.rits.batchnoinwork.repository;


import com.rits.batchnoinwork.dto.BatchNoWorkQtyResponse;
import com.rits.batchnoinwork.model.BatchNoInWork;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BatchNoInWorkRepository extends MongoRepository<BatchNoInWork,String> {

    List<BatchNoInWork> findTop50BySiteAndActive(String site, int i);

    List<BatchNoInWork> findBySiteAndActive(String site, int i);

    BatchNoInWork findBySiteAndActiveAndHandle(String site, int i, String handle);

    boolean existsBySiteAndActiveAndHandle(String site, int i, String handle);

    boolean existsBySiteAndActiveAndBatchNo(String site, int i, String batchNo);

    boolean existsBySiteAndActiveAndResource(String site, int i, String resource);

    BatchNoInWork findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUserAndActive(String site, String batchNo, String phaseId, String operation, String resource, String user, int i);

    BatchNoInWork findBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUserAndActive(String site, String batchNoHeaderBO, String phaseId, String operation, String resource, String user, int i);

    List<BatchNoInWork> findBySiteAndOperationAndResourceAndActive(String site, String operation, String resource, int active, Pageable pageable);
    List<BatchNoInWork> findByOperationAndSiteAndActive(String operation, String site, int active, Pageable pageable);

    List<BatchNoInWork> findByOperationAndResourceAndPhaseIdAndSiteAndActive(String operation, String resource, String phaseId, String site, int active, Pageable pageable);

    List<BatchNoInWork> findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(String site, String batchNo, String phaseId, String operation, String resource, int i);

    boolean existsBySiteAndOperationAndBatchNoAndActive(String site, String operation, String batchNo, int i);

    BatchNoInWork findBySiteAndOperationAndBatchNoAndActive(String site, String operation, String batchNo, int i);

    BatchNoInWork findBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndActive(String site, String batchNoHeaderBO, String phaseId, String operation, String resource, int i);

    BatchNoInWork findByActiveAndSiteAndBatchNoAndPhaseIdAndOperationAndResource( int i, String site, String batchNo, String phaseId, String operation, String resource);

    BatchNoInWork findByActiveAndSiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUser( int i, String site, String batchNo, String phaseId, String operation, String resource, String user);


    BatchNoWorkQtyResponse findBySiteAndBatchNoAndOrderNumberAndPhaseIdAndOperation(String site, String batchNo, String orderNumber, String phaseId, String operation);

    BatchNoWorkQtyResponse findBySiteAndBatchNoAndPhaseIdAndOperation(String site, String batchNo, String phaseId, String operation);

    BatchNoWorkQtyResponse findBySiteAndOrderNumberAndPhaseIdAndOperation(String site, String orderNumber, String phaseId, String operation);

    BatchNoInWork findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUserAndActiveAndOrderNumber(String site, String batchNo, String phaseId, String operation, String resource, String user, int i, String orderNumber);

    BatchNoInWork findByBatchNoAndPhaseIdAndOperationAndResourceAndActiveAndSite(String batchNo, String phaseId, String operation, String resource, int i, String site);

    BatchNoInWork findByActiveAndSiteAndBatchNoAndPhaseIdAndOperationAndResourceAndOrderNumber( int i, String site, String batchNo, String phaseId, String operation, String resource, String orderNumber);

    BatchNoInWork findByActiveAndSiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUserAndOrderNumber( int i, String site, String batchNo, String phaseId, String operation, String resource, String user, String orderNumber);
}
