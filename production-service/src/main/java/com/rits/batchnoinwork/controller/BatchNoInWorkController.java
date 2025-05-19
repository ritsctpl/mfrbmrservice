package com.rits.batchnoinwork.controller;

import com.rits.batchnoinwork.dto.BatchNoWorkQtyResponse;
import com.rits.batchnoinwork.dto.InWorkResponse;
import com.rits.batchnoinwork.model.BatchNoInWork;
import com.rits.batchnoinwork.model.MessageModel;
import com.rits.batchnoinwork.dto.BatchNoInWorkRequest;
import com.rits.batchnoinwork.exception.BatchNoInWorkException;
import com.rits.batchnoinwork.service.BatchNoInWorkService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/batchnoinwork-service")
public class BatchNoInWorkController {

    private final BatchNoInWorkService batchNoInWorkService;



    @PostMapping("/create")
    public MessageModel createBatchNoInWork(@RequestBody BatchNoInWorkRequest batchNoInWorkRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoInWorkRequest.getSite()))
            throw new BatchNoInWorkException(113);
        try {
            return batchNoInWorkService.createBatchNoInWork(batchNoInWorkRequest);
        } catch (BatchNoInWorkException batchNoInWorkException) {
            throw  batchNoInWorkException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/update")
    public MessageModel updateBatchNoInWork(@RequestBody BatchNoInWorkRequest batchNoInWorkRequest) throws Exception {
        if(StringUtils.isEmpty(batchNoInWorkRequest.getSite()))
            throw new BatchNoInWorkException(113);
        try {
            return batchNoInWorkService.updateBatchNoInWork(batchNoInWorkRequest);
        } catch (BatchNoInWorkException batchNoInWorkException) {
            throw  batchNoInWorkException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/delete")
    public MessageModel deleteBatchNoInWork(@RequestBody BatchNoInWorkRequest batchNoInWorkRequest)
    {
        if(StringUtils.isEmpty(batchNoInWorkRequest.getSite()))
            throw new BatchNoInWorkException(113);

        try {
            return batchNoInWorkService.deleteBatchNoInWork(batchNoInWorkRequest);
        } catch (BatchNoInWorkException batchNoInWorkException) {
            throw  batchNoInWorkException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/unDelete")
    public MessageModel unDeleteBatchNoInWork(@RequestBody BatchNoInWorkRequest batchNoInWorkRequest)
    {
        if(StringUtils.isEmpty(batchNoInWorkRequest.getSite()))
            throw new BatchNoInWorkException(113);

        try {
            return batchNoInWorkService.unDeleteBatchNoInWork(batchNoInWorkRequest);
        } catch (BatchNoInWorkException batchNoInWorkException) {
            throw  batchNoInWorkException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieve")
    public BatchNoInWork retrieve(@RequestBody BatchNoInWorkRequest batchNoInWorkRequest)
    {
        if(StringUtils.isEmpty(batchNoInWorkRequest.getSite()))
            throw new BatchNoInWorkException(113);

        try {
            return batchNoInWorkService.retrieve(batchNoInWorkRequest);
        } catch (BatchNoInWorkException batchNoInWorkException) {
            throw  batchNoInWorkException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveAll")
    public List<BatchNoInWork> retrieveAll(@RequestBody BatchNoInWorkRequest batchNoInWorkRequest)
    {
        if(StringUtils.isEmpty(batchNoInWorkRequest.getSite()))
            throw new BatchNoInWorkException(113);

        try {
            return batchNoInWorkService.retrieveAll(batchNoInWorkRequest.getSite());
        } catch (BatchNoInWorkException batchNoInWorkException) {
            throw  batchNoInWorkException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveTop50")
    public List<BatchNoInWork> retrieveTop50(@RequestBody BatchNoInWorkRequest batchNoInWorkRequest)
    {
        if(StringUtils.isEmpty(batchNoInWorkRequest.getSite()))
            throw new BatchNoInWorkException(113);

        try {
            return batchNoInWorkService.retrieveTop50(batchNoInWorkRequest.getSite());
        } catch (BatchNoInWorkException batchNoInWorkException) {
            throw  batchNoInWorkException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/isExist")
    public boolean isBatchNoInWorkExist(@RequestBody BatchNoInWorkRequest batchNoInWorkRequest){
    if(StringUtils.isEmpty(batchNoInWorkRequest.getSite()))
        throw new BatchNoInWorkException(113);
    try{
     return batchNoInWorkService.isBatchNoInWorkExist(batchNoInWorkRequest.getSite(), batchNoInWorkRequest.getBatchNo());
    } catch(BatchNoInWorkException batchNoInWorkException){
        throw batchNoInWorkException;
    } catch(Exception e) {
        throw new RuntimeException(e);
    }
    }

    @PostMapping("/retrieveBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUser")
    public BatchNoInWork retrieveBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUser(@RequestBody BatchNoInWorkRequest request){
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoInWorkException(113);
        try{
            return batchNoInWorkService.getBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUser(request.getSite(), request.getBatchNo(), request.getPhaseId(),request.getOperation(),
                    request.getResource(), request.getUser(), request.getOrderNumber());
        } catch(BatchNoInWorkException batchNoInWorkException){
            throw batchNoInWorkException;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUser")
    public BatchNoInWork retrieveBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUser(@RequestBody BatchNoInWorkRequest request){
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoInWorkException(113);
        try{
            return batchNoInWorkService.getBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUser(request.getSite(), request.getBatchNo(), request.getPhaseId(),request.getOperation(), request.getResource(), request.getUser());
        } catch(BatchNoInWorkException batchNoInWorkException){
            throw batchNoInWorkException;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getRecordUsingFilters")
    public List<InWorkResponse> getRecordUsingFilters(@RequestBody BatchNoInWorkRequest request){
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoInWorkException(113);

        try{
            return batchNoInWorkService.getRecordUsingFilters(request);
        } catch(BatchNoInWorkException batchNoInWorkException){
            throw batchNoInWorkException;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchInWorkList")
    public List<BatchNoInWork> getBatchInWorkList(@RequestBody BatchNoInWorkRequest request){
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoInWorkException(113);

        try{
            return batchNoInWorkService.getBatchInWorkList(request);
        } catch(BatchNoInWorkException batchNoInWorkException){
            throw batchNoInWorkException;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/updateQualityApproval")
    public boolean updateQualityApproval(@RequestBody BatchNoInWorkRequest request){
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoInWorkException(113);

        try{
            return batchNoInWorkService.updateQualityApproval(request.getSite(), request.getOperation(), request.getBatchNo());
        } catch(BatchNoInWorkException batchNoInWorkException){
            throw batchNoInWorkException;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchNoInWorkByPhaseAndOperation")
    public BatchNoWorkQtyResponse getBatchNoInWorkByPhaseAndOperation(@RequestBody BatchNoInWorkRequest request){
        try{
            return batchNoInWorkService.getBatchNoInWorkByPhaseAndOperation(request);
        } catch(BatchNoInWorkException batchNoInWorkException){
            throw batchNoInWorkException;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
