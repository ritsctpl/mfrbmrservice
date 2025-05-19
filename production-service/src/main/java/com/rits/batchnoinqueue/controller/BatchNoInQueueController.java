package com.rits.batchnoinqueue.controller;


import com.rits.batchnoinqueue.dto.BatchNoInQueueRequest;
import com.rits.batchnoinqueue.dto.BatchNoInQueueResponse;
import com.rits.batchnoinqueue.dto.QuantityInQueueResponse;
import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinqueue.model.MessageModel;
import com.rits.batchnoinqueue.service.BatchNoInQueueService;
import com.rits.batchnoinqueue.exception.BatchNoInQueueException;
import com.rits.batchnoinwork.dto.BatchNoInWorkRequest;
import com.rits.batchnoinwork.exception.BatchNoInWorkException;
import com.rits.batchnorecipeheaderservice.dto.BatchNoRecipeHeaderReq;
import com.rits.batchnorecipeheaderservice.exception.BatchNoRecipeHeaderException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/batchnoinqueue-service")
public class BatchNoInQueueController {
    private final BatchNoInQueueService batchNoInQueueService;

    @PostMapping("/create")
    public MessageModel createBatchNoInQueue(@RequestBody BatchNoInQueueRequest batchNoInQueueRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoInQueueRequest.getSite()))
            throw new BatchNoInQueueException(113);
        try {
            return batchNoInQueueService.createBatchNoInQueue(batchNoInQueueRequest);
         } catch (BatchNoInQueueException batchNoInQueueException) {
        throw  batchNoInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/update")
    public MessageModel updateBatchNoInQueue(@RequestBody BatchNoInQueueRequest batchNoInQueueRequest) throws Exception {
        if(StringUtils.isEmpty(batchNoInQueueRequest.getSite()))
            throw new BatchNoInQueueException(113);
        try {
             return batchNoInQueueService.updateBatchNoInQueue(batchNoInQueueRequest);
        } catch (BatchNoInQueueException batchNoInQueueException) {
            throw  batchNoInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/delete")
    public MessageModel deleteBatchNoInQueue(@RequestBody BatchNoInQueueRequest batchNoInQueueRequest)
    {
        if(StringUtils.isEmpty(batchNoInQueueRequest.getSite()))
            throw new BatchNoInQueueException(113);

        try {
            return batchNoInQueueService.deleteBatchNoInQueue(batchNoInQueueRequest);
        } catch (BatchNoInQueueException batchNoInQueueException) {
            throw  batchNoInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieve")
    public BatchNoInQueue retrieve(@RequestBody BatchNoInQueueRequest batchNoInQueueRequest)
    {
        if(StringUtils.isEmpty(batchNoInQueueRequest.getSite()))
            throw new BatchNoInQueueException(113);

        try {
            return batchNoInQueueService.retrieve(batchNoInQueueRequest);
        } catch (BatchNoInQueueException batchNoInQueueException) {
            throw  batchNoInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveAll")
    public List<BatchNoInQueue> retrieveAll(@RequestBody BatchNoInQueueRequest batchNoInQueueRequest)
    {
        if(StringUtils.isEmpty(batchNoInQueueRequest.getSite()))
            throw new BatchNoInQueueException(113);

        try {
            return batchNoInQueueService.retrieveAll(batchNoInQueueRequest.getSite());
        } catch (BatchNoInQueueException batchNoInQueueException) {
            throw  batchNoInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveTop50")
    public List<BatchNoInQueue> retrieveTop50(@RequestBody BatchNoInQueueRequest batchNoInQueueRequest)
    {
        if(StringUtils.isEmpty(batchNoInQueueRequest.getSite()))
            throw new BatchNoInQueueException(113);

        try {
            return batchNoInQueueService.retrieveTop50(batchNoInQueueRequest.getSite());
        } catch (BatchNoInQueueException batchNoInQueueException) {
            throw  batchNoInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/isExist")
    public boolean isBatchNoInQueueExist(@RequestBody BatchNoInQueueRequest batchNoInQueueRequest) throws Exception{
        if(StringUtils.isEmpty(batchNoInQueueRequest.getSite()))
            throw new BatchNoInQueueException(113);

        try {
            return batchNoInQueueService.isBatchNoInQueueExist(batchNoInQueueRequest.getSite(),batchNoInQueueRequest.getBatchNo());
        } catch (BatchNoInQueueException batchNoInQueueException) {
            throw  batchNoInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveBySiteAndBatchNoHeaderAndPhaseAndOperation")
    public BatchNoInQueue retrieveBySiteAndBatchNoHeaderAndPhaseAndOperation(@RequestBody BatchNoInQueueRequest request) throws Exception {
        if (StringUtils.isEmpty(request.getSite()))
            throw new BatchNoInQueueException(113);

        try {
            return batchNoInQueueService.getBySiteAndBatchNoHeaderAndPhaseAndOperation(request.getSite(), request.getBatchNoHeaderBO(), request.getPhaseId(), request.getOperation());
        } catch (BatchNoInQueueException batchNoInQueueException) {
            throw batchNoInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveListForBatchNoBOs")
    public ResponseEntity<BatchNoInQueueResponse> getBatchNoRecords(@RequestBody BatchNoInQueueRequest request) {
        if (StringUtils.isEmpty(request.getSite()))
            throw new BatchNoInQueueException(113);
        try {
            BatchNoInQueueResponse response = batchNoInQueueService.getBatchNoRecords(request);
            return ResponseEntity.ok(response);
        }catch (BatchNoInQueueException batchNoInQueueException) {
            throw batchNoInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getInQueueForBatchRecipeByFilters")
    public ResponseEntity<BatchNoInQueueResponse> getInQueueForBatchRecipeByFilters(@RequestBody BatchNoInQueueRequest request) {
        if (StringUtils.isEmpty(request.getSite()))
            throw new BatchNoInQueueException(113);

        try {
            return ResponseEntity.ok(batchNoInQueueService.getInQueueForBatchRecipeByFilters(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getBatchInQueueList")
    public ResponseEntity<List<BatchNoInQueue>> getBatchInQueueList(@RequestBody BatchNoInQueueRequest request) {
        if (StringUtils.isEmpty(request.getSite()))
            throw new BatchNoInQueueException(113);

        try {
            return ResponseEntity.ok(batchNoInQueueService.getBatchInQueueList(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getBatchInQueueListForWorkList")
    public ResponseEntity<List<BatchNoInQueue>> getBatchInQueueListForWorkList(@RequestBody BatchNoInQueueRequest request) {
        if (StringUtils.isEmpty(request.getSite()))
            throw new BatchNoInQueueException(113);

        try {
            return ResponseEntity.ok(batchNoInQueueService.getBatchInQueueListForWorkList(request));
        } catch (BatchNoInQueueException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/updateQualityApproval")
    public boolean updateQualityApproval(@RequestBody BatchNoInQueueRequest request){
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoInQueueException(113);

        try{
            return batchNoInQueueService.updateQualityApproval(request.getSite(), request.getOperation(), request.getBatchNo());
        } catch(BatchNoInQueueException e){
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchInQueueByPhaeAndOperation")
    public QuantityInQueueResponse getBatchNoInQueueByPhaseAndOperation(@RequestBody BatchNoInQueueRequest request){
        try{
            return batchNoInQueueService.getBatchNoInQueueByPhaseAndOperation(request);
        } catch(BatchNoInQueueException e){
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
