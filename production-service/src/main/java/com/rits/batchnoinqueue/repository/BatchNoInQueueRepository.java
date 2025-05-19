package com.rits.batchnoinqueue.repository;


import com.rits.batchnoinqueue.dto.QuantityInQueueResponse;
import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinwork.model.BatchNoInWork;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BatchNoInQueueRepository extends MongoRepository<BatchNoInQueue,String> {

    List<BatchNoInQueue> findBySiteAndActive(String site, int i);

    List<BatchNoInQueue> findTop50BySiteAndActive(String site, int i);

    BatchNoInQueue findBySiteAndActiveAndHandle(String site, int i, String handle);

    BatchNoInQueue findBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndActive(String site, String batchNoHeaderBO, String phaseId, String operation, int i);

    boolean existsBySiteAndActiveAndBatchNo(String site, int i, String batchNo);
    BatchNoInQueue findByHandleAndSiteAndActive(String handle, String site, int i);

    List<BatchNoInQueue> findBySiteAndOperationAndResourceAndActive(String site, String operation, String resource, int active, Pageable pageable);
    List<BatchNoInQueue> findByOperationAndSiteAndActive(String operation, String site, int active, Pageable pageable);
    List<BatchNoInQueue> findByOperationAndResourceAndPhaseIdAndSiteAndActive(String operation, String resource, String phaseId, String site, int active, Pageable pageable);
    List<BatchNoInQueue> findByOperationAndPhaseIdAndSiteAndActive(String operation, String phaseId, String site, int active, Pageable pageable);

    List<BatchNoInQueue> findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(String site, String batchNo, String phaseId, String operation, String resource, int i);

    // Paginated and sorted methods
    List<BatchNoInQueue> findByOperationAndSiteAndActiveOrderByCreatedDateTimeDesc(
            String operation, String site, int active, Pageable pageable);

    List<BatchNoInQueue> findBySiteAndOperationAndResourceAndActiveOrderByCreatedDateTimeDesc(
            String site, String operation, String resource, int active, Pageable pageable);

    List<BatchNoInQueue> findByOperationAndResourceAndPhaseIdAndSiteAndActiveOrderByCreatedDateTimeDesc(
            String operation, String resource, String phaseId, String site, int active, Pageable pageable);

    boolean existsBySiteAndOperationAndBatchNoAndActive(String site, String operation, String batchNo, int i);

    BatchNoInQueue findBySiteAndOperationAndBatchNoAndActive(String site, String operation, String batchNo, int i);


    QuantityInQueueResponse findBySiteAndBatchNoAndOrderNumberAndPhaseIdAndOperation(String site, String batchNo, String orderNumber, String phaseId, String operation);

    QuantityInQueueResponse findBySiteAndBatchNoAndPhaseIdAndOperation(String site, String batchNo, String phaseId, String operation);

    QuantityInQueueResponse findBySiteAndOrderNumberAndPhaseIdAndOperation(String site, String orderNumber, String phaseId, String operation);
    BatchNoInQueue findBySiteAndBatchNoAndPhaseIdAndOperationAndOrderNumberAndActive(String site, String batchNo, String phaseId, String operation, String orderNumber, int i);
}

