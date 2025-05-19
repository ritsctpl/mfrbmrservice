package com.rits.batchnorecipeheaderservice.repository;

import com.rits.batchnorecipeheaderservice.model.BatchNoRecipeHeader;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchNoRecipeHeaderRepository extends MongoRepository<BatchNoRecipeHeader, String> {

    BatchNoRecipeHeader findByBatchNoAndMaterialAndSiteAndActive(String batchNo, String material, String site, int active);
    BatchNoRecipeHeader findByHandleAndSiteAndActive(String handle, String site, int active);
    List<BatchNoRecipeHeader> findBySiteAndActive(String site, int active);
    List<BatchNoRecipeHeader> findTop50BySiteAndActive(String site, int active);
    BatchNoRecipeHeader findByBatchNoAndOrderNoAndMaterialAndMaterialVersionAndActive(String batchNo, String orderNo, String material,String materialVersion, int active);

    BatchNoRecipeHeader findBySiteAndBatchNoAndActive(String site, String batchNo, int i);

    BatchNoRecipeHeader findBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersionAndActive(String site, String batchNo, String orderNo, String material, String materialVersion, int i);

    BatchNoRecipeHeader findBySiteAndBatchNoAndMaterialAndMaterialVersionAndActive(String site, String batchNo, String material, String materialVersion, int i);

    List<BatchNoRecipeHeader> findBySiteAndBatchNoAndOrderNo(String site, String batchNo, String orderNo);

    List<BatchNoRecipeHeader> findBySiteAndBatchNo(String site, String batchNo);

    List<BatchNoRecipeHeader> findBySiteAndOrderNo(String site, String orderNo);
    List<BatchNoRecipeHeader> findBySite(String site);
}