package com.rits.batchnophaseprogressservice.service;

import com.rits.batchnophaseprogressservice.dto.BatchNoPhaseProgressRequest;
import com.rits.batchnophaseprogressservice.model.BatchNoPhaseProgress;
import com.rits.batchnophaseprogressservice.model.MessageModel;

import java.util.List;

public interface BatchNoPhaseProgressService {

    MessageModel create(BatchNoPhaseProgressRequest request);

    MessageModel update(BatchNoPhaseProgressRequest request);

    MessageModel delete(BatchNoPhaseProgressRequest request);

    BatchNoPhaseProgress retrieve(BatchNoPhaseProgressRequest request);


    List<BatchNoPhaseProgress> retrieveAll(String site);

    List<BatchNoPhaseProgress> retrieveTop50(String site);

    boolean isBatchNoPhaseProgressExist(String site, String batchNo);

    BatchNoPhaseProgress getBySiteAndBatchNoAndMaterialAndMaterialVersionAndBatchNoHeaderBO(String site, String batchNumber, String material,  String materialVersion, String batchNoHeaderBO);

    BatchNoPhaseProgress getBySiteAndBatchNoAndMaterialAndOrderNoAndMaterialVersion(String site, String batchNumber, String material, String orderNumber, String materialVersion);
}
