package com.rits.stepstatusservice.service;

import com.rits.batchnocomplete.dto.BatchNoCompleteDTO;
import com.rits.batchnocomplete.dto.BatchNoCompleteQty;
import com.rits.batchnocomplete.service.BatchNoCompleteService;
import com.rits.batchnohold.service.BatchNoHoldService;
import com.rits.batchnoinqueue.dto.BatchNoInQueueRequest;
import com.rits.batchnoinqueue.dto.QuantityInQueueResponse;
import com.rits.batchnoinqueue.service.BatchNoInQueueService;
import com.rits.batchnoinwork.dto.BatchNoInWorkRequest;
import com.rits.batchnoinwork.dto.BatchNoWorkQtyResponse;
import com.rits.batchnoinwork.service.BatchNoInWorkService;
import com.rits.batchnorecipeheaderservice.dto.BatchNoRecipeHeaderReq;
import com.rits.batchnorecipeheaderservice.dto.Operation;
import com.rits.batchnorecipeheaderservice.dto.Phase;
import com.rits.batchnorecipeheaderservice.model.BatchNoRecipeHeader;
import com.rits.batchnorecipeheaderservice.model.Recipes;
import com.rits.batchnorecipeheaderservice.service.BatchNoRecipeHeaderService;
import com.rits.batchnoscrap.dto.BatchNoScrapQtyResponse;
import com.rits.batchnoscrap.dto.BatchNoScrapRequest;
import com.rits.batchnoscrap.service.BatchNoScrapService;

import com.rits.logbuyoffservice.dto.BuyOff;
import com.rits.logbuyoffservice.dto.LogbuyOffRequest;
import com.rits.logbuyoffservice.model.BuyoffLog;
import com.rits.logbuyoffservice.service.LogBuyOffService;
import com.rits.productionlogservice.model.ProductionLog;
import com.rits.productionlogservice.service.ProductionLogService;
import com.rits.stepstatusservice.dto.BatchStepStatusRequest;
import com.rits.stepstatusservice.exception.BatchStepStatusException;
import com.rits.stepstatusservice.model.StepStatus;
import com.rits.stepstatusservice.model.StepStatusList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BatchStepStatusServiceImpl implements BatchStepStatusService{

    @Autowired
   private BatchNoRecipeHeaderService batchNoRecipeHeaderService;
    @Autowired
   private BatchNoInQueueService batchNoInQueueService;
    @Autowired
   private BatchNoInWorkService batchNoInWorkService;
    @Autowired
   private BatchNoCompleteService batchNoCompleteService;
    @Autowired
    private BatchNoHoldService batchNoHoldService;
    @Autowired
   private BatchNoScrapService batchNoScrapService;
    @Autowired
    private LogBuyOffService logBuyOffService;
    @Autowired
    private ProductionLogService productionLogService;

    @Override
    public List<StepStatus> getStepStatusByBatch(BatchStepStatusRequest batchStepStatusRequest) throws Exception {

        BatchNoRecipeHeaderReq batchNoRecipeHeaderReq = new BatchNoRecipeHeaderReq();
        batchNoRecipeHeaderReq.setSite(batchStepStatusRequest.getSite());
        batchNoRecipeHeaderReq.setBatchNo(batchStepStatusRequest.getBatchNo());
        batchNoRecipeHeaderReq.setOrderNo(batchStepStatusRequest.getOrderNumber());

        List<BatchNoRecipeHeader> batchNoRecipeHeaderList = batchNoRecipeHeaderService.getBatchRecipeBySiteAndBatchAndOrder(batchNoRecipeHeaderReq);

        if(batchNoRecipeHeaderList.size() == 0) {
            throw new BatchStepStatusException(6);
        }

        StepStatus stepStatus = null;
        List<StepStatus> stepStatusArrayList = new ArrayList<>();

        for (BatchNoRecipeHeader batchNoRecipeHeader : batchNoRecipeHeaderList) {

            Recipes recipe = batchNoRecipeHeader.getRecipe();
            List<Phase> phases = recipe.getPhases();

            List<StepStatusList> allStepStatusLists = new ArrayList<>();

            for (Phase phase : phases) {
                List<Operation> operations = phase.getOperations();

                for (Operation operation : operations) {
                    StepStatusList stepStatusList = new StepStatusList();
                    stepStatusList.setPhaseId(phase.getPhaseId());
                    stepStatusList.setOperation(operation.getOperationId());
                    stepStatusList.setMaterial(batchNoRecipeHeader.getMaterial());
                    stepStatusList.setMaterialDescription(batchNoRecipeHeader.getMaterialDescription());

                    // Fetching BatchNoInQueue
                    BatchNoInQueueRequest batchNoInQueueRequest = new BatchNoInQueueRequest();
                    batchNoInQueueRequest.setSite(batchStepStatusRequest.getSite());
                    batchNoInQueueRequest.setBatchNo(batchNoRecipeHeader.getBatchNo());
                    batchNoInQueueRequest.setOrderNumber(batchStepStatusRequest.getOrderNumber());
                    batchNoInQueueRequest.setPhaseId(phase.getPhaseId());
                    batchNoInQueueRequest.setOperation(operation.getOperationId());
                    QuantityInQueueResponse batchNoInQueue = batchNoInQueueService.getBatchNoInQueueByPhaseAndOperation(batchNoInQueueRequest);
                    stepStatusList.setQtyInQueue(batchNoInQueue != null && batchNoInQueue.getQtyInQueue() != null ? batchNoInQueue.getQtyInQueue() : BigDecimal.ZERO);
                    //stepStatusList.setBatchStartedTime((batchNoInQueue != null && batchNoInQueue.getCreatedDateTime() != null) ? batchNoInQueue.getCreatedDateTime() : null);
                    if(batchNoInQueue!=null)stepStatusList.setBatchStatus("inqueue");
                    // Fetching BatchNoInWork
                    BatchNoInWorkRequest batchNoInWorkRequest = new BatchNoInWorkRequest();
                    batchNoInWorkRequest.setSite(batchStepStatusRequest.getSite());
                    batchNoInWorkRequest.setBatchNo(batchNoRecipeHeader.getBatchNo());
                    batchNoInWorkRequest.setOrderNumber(batchStepStatusRequest.getOrderNumber());
                    batchNoInWorkRequest.setPhaseId(phase.getPhaseId());
                    batchNoInWorkRequest.setOperation(operation.getOperationId());
                    BatchNoWorkQtyResponse batchNoInWork = batchNoInWorkService.getBatchNoInWorkByPhaseAndOperation(batchNoInWorkRequest);
                    stepStatusList.setInWorkQty(batchNoInWork != null && batchNoInWork.getQtyToComplete() != null ? batchNoInWork.getQtyToComplete() : BigDecimal.ZERO);
                    //stepStatusList.setBatchStartedTime((batchNoInWork != null && batchNoInWork.getCreatedDateTime() != null) ? batchNoInWork.getCreatedDateTime() : null);
                    if(batchNoInWork!=null)stepStatusList.setBatchStatus("active");
                    // Fetching BatchNoComplete
                    BatchNoCompleteDTO batchNoCompleteDTO = new BatchNoCompleteDTO();
                    batchNoCompleteDTO.setSite(batchStepStatusRequest.getSite());
                    batchNoCompleteDTO.setBatchNo(batchNoRecipeHeader.getBatchNo());
                    batchNoCompleteDTO.setOrderNumber(batchStepStatusRequest.getOrderNumber());
                    batchNoCompleteDTO.setPhaseId(phase.getPhaseId());
                    batchNoCompleteDTO.setOperation(operation.getOperationId());
                    BatchNoCompleteQty batchNoComplete = batchNoCompleteService.getBatchNoCompleteByPhaseAndOperation(batchNoCompleteDTO);
                    stepStatusList.setQtyToComplete(batchNoComplete != null && batchNoComplete.getQtyToComplete() != null ? batchNoComplete.getQtyToComplete() : BigDecimal.ZERO);
                    stepStatusList.setBatchCompletedTime((batchNoComplete != null && batchNoComplete.getCreatedDateTime() != null) ? batchNoComplete.getCreatedDateTime() : null);
                    if(batchNoComplete!=null)stepStatusList.setBatchStatus("complete");

                    // Checking BatchNoHold
                    if(batchNoHoldService.isBatchOnHold(batchStepStatusRequest.getSite(),batchNoRecipeHeader.getBatchNo()))stepStatusList.setBatchStatus("hold");

                    // Fetching BatchNoScrap
                    BatchNoScrapRequest batchNoScrapRequest = new BatchNoScrapRequest();
                    batchNoScrapRequest.setSite(batchStepStatusRequest.getSite());
                    batchNoScrapRequest.setBatchNo(batchNoRecipeHeader.getBatchNo());
                    batchNoScrapRequest.setOrderNumber(batchStepStatusRequest.getOrderNumber());
                    batchNoScrapRequest.setPhaseId(phase.getPhaseId());
                    batchNoScrapRequest.setOperation(operation.getOperationId());
                    BatchNoScrapQtyResponse batchNoScrap = batchNoScrapService.getBatchNoScrapByPhaseAndOperation(batchNoScrapRequest);
                    stepStatusList.setScrapQuantity(batchNoScrap != null && batchNoScrap.getScrapQuantity() != null ? batchNoScrap.getScrapQuantity() : BigDecimal.ZERO);

                    List<ProductionLog> productionLog = productionLogService.retrieveBySiteAndOrderNoAndPhaseAndOperationAndEventType(batchStepStatusRequest.getSite(), batchNoRecipeHeader.getOrderNo(), phase.getPhaseId(), operation.getOperationId(), "startSfcBatch");
                    stepStatusList.setBatchStartedTime(
                        productionLog.stream()
                                .findFirst()
                                .map(ProductionLog::getCreated_datetime)
                                .orElse(null)
                    );

                    BuyoffLog buyOff = logBuyOffService.retrieveByBatchNoAndOrderNumberAndOperation(batchStepStatusRequest.getSite(), batchNoRecipeHeader.getBatchNo(), batchNoRecipeHeader.getOrderNo(), operation.getOperationId());
                    stepStatusList.setLineClearanceApproval((buyOff != null) ? true : false);

                    allStepStatusLists.add(stepStatusList);
                }
            }

            stepStatus = new StepStatus();
            stepStatus.setBatchNo(batchNoRecipeHeader.getBatchNo());
            stepStatus.setOrderNo(batchNoRecipeHeader.getOrderNo());
            stepStatus.setStepStatusList(allStepStatusLists);

            stepStatusArrayList.add(stepStatus);
        }

        return stepStatusArrayList;
    }


}
