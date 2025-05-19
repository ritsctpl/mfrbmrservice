package com.rits.batchnohold.repository;

import com.rits.batchnodoneservice.model.BatchNoDone;
import com.rits.batchnohold.model.BatchNoHold;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;


public interface BatchNoHoldRepository extends MongoRepository<BatchNoHold, String> {
    BatchNoHold findBySiteAndBatchNoAndResourceAndOperationAndPhaseIdAndActive(String site, String batchNo, String resource, String operation, String phaseId, int i);

    Optional<BatchNoHold> findBySiteAndBatchNoAndActive(String site, String batchNo, int i);
    List<BatchNoHold> findBySiteAndOperationAndResourceAndActive(String site, String operation, String resource, int active, Pageable pageable);
    List<BatchNoHold> findByOperationAndSiteAndActive(String operation, String site, int active, Pageable pageable);

    List<BatchNoHold> findByOperationAndResourceAndPhaseIdAndSiteAndActive(String operation, String resource, String phaseId, String site, int active, Pageable pageable);
}
