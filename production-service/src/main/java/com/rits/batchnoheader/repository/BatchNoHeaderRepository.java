package com.rits.batchnoheader.repository;

import com.rits.batchnoheader.model.BatchNoHeader;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchNoHeaderRepository extends MongoRepository<BatchNoHeader, String> {
    BatchNoHeader findBySiteAndActiveAndHandleAndOrderNo(String site, int active, String handle, String orderNo);
    List<BatchNoHeader> findBySiteAndActive(String site, int active);
    List<BatchNoHeader> findByBatchNoContainingIgnoreCaseAndSiteAndActive(String batchNumber, String site, int active);
    List<BatchNoHeader> findTop50BySiteAndActive(String site, int active);
    boolean existsBySiteAndActiveAndBatchNo(String site, int active, String batchNumber);
    BatchNoHeader findBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersionAndActive(String site, String batchNo, String orderNo, String material, String materialVersion, int i);
    BatchNoHeader findBySiteAndBatchNoAndMaterialAndMaterialVersionAndActive(String site, String batchNo, String material, String materialVersion,int i);

    BatchNoHeader findBySiteAndBatchNoAndActive(String site, String batchNo, int i);
    BatchNoHeader findBySiteAndBatchNoAndOrderNoAndActive(String site, String batchNo,String orderNo, int i);

    boolean existsBySiteAndActiveAndBatchNoAndMaterialAndOrderNo(String site, int i, String batchNumber, String material, String orderNo);
}