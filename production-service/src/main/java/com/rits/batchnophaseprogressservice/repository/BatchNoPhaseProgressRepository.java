package com.rits.batchnophaseprogressservice.repository;

import com.rits.batchnophaseprogressservice.model.BatchNoPhaseProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BatchNoPhaseProgressRepository extends MongoRepository<BatchNoPhaseProgress,String> {

    BatchNoPhaseProgress findByHandleAndSiteAndActive(String handle, String site, int i);

    List<BatchNoPhaseProgress> findBySiteAndActive(String site, int i);

    List<BatchNoPhaseProgress> findTop50BySiteAndActive(String site, int active);

    boolean existsBySiteAndActiveAndBatchNo(String site, int i, String batchNo);

    BatchNoPhaseProgress findBySiteAndBatchNoAndMaterialAndMaterialVersionAndBatchNoHeaderBOAndActive(String site, String batchNumber, String material, String materialVersion, String batchNoHeaderBO, int i);

    BatchNoPhaseProgress findBySiteAndBatchNoAndMaterialAndOrderNumberAndMaterialVersionAndActive(String site, String batchNumber, String material, String orderNumber, String materialVersion, int i);
    BatchNoPhaseProgress findBySiteAndBatchNoAndMaterialAndOrderNumberAndMaterialVersionAndActiveAndRecipeAndRecipeVersion(String site, String batchNumber, String material, String orderNumber,
                                                                                                                           String materialVersion, int i, String recipe, String recipeVersion);
}
