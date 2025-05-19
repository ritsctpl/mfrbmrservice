package com.rits.batchnodoneservice.repository;

import com.rits.batchnodoneservice.model.BatchNoDone;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BatchNoDoneRepository extends MongoRepository<BatchNoDone,String> {

    BatchNoDone findByHandleAndSiteAndActive(String handle, String site, int i);

    List<BatchNoDone> findBySiteAndActive(String site, int i);

    List<BatchNoDone> findTop50BySiteAndActive(String site, int active);

    boolean existsBySiteAndActiveAndBatchNo(String site, int i, String batchNo);

    BatchNoDone findBySiteAndBatchNoHeaderBOAndOrderNumberAndPhaseIdAndOperationAndResourceAndActive(String site, String batchNoHeaderBO, String orderNumber, String phaseId, String operation, String resource, int i);

    BatchNoDone findBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndActive(String site, String batchNoHeaderBO, String phaseId, String operation, String resource, int i);
}
