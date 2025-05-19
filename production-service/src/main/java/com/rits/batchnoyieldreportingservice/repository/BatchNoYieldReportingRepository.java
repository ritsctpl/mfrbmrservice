package com.rits.batchnoyieldreportingservice.repository;

import com.rits.batchnoyieldreportingservice.dto.BatchNoYieldReportingRequest;
import com.rits.batchnoyieldreportingservice.model.BatchNoYieldReporting;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BatchNoYieldReportingRepository extends MongoRepository<BatchNoYieldReporting,String> {

    BatchNoYieldReporting findByHandleAndSiteAndActive(String handle, String site, int i);

    List<BatchNoYieldReporting> findBySiteAndActive(String site, int i);

    List<BatchNoYieldReporting> findTop50BySiteAndActive(String site, int active);

    boolean existsBySiteAndActiveAndBatchNo(String site, int i, String batchNo);
    Optional<BatchNoYieldReportingRequest> findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(String site, String batchNo, String phaseId, String operation, String resource, int i);

}
