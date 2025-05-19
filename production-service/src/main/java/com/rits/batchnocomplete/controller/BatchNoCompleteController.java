package com.rits.batchnocomplete.controller;

import com.rits.batchnocomplete.dto.BatchNoCompleteDTO;
import com.rits.batchnocomplete.dto.BatchNoCompleteQty;
import com.rits.batchnocomplete.exception.BatchNoCompleteException;
import com.rits.batchnocomplete.model.BatchNoComplete;
import com.rits.batchnocomplete.model.BatchNoCompleteMsgModel;
import com.rits.batchnocomplete.service.BatchNoCompleteService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("app/v1/batchnocomplete-service")
public class BatchNoCompleteController {

    @Autowired
    BatchNoCompleteService batchNoHeaderService;
    @PostMapping("/create")
    public BatchNoCompleteMsgModel create(@RequestBody BatchNoCompleteDTO batchNoCompleteRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoCompleteRequest.getSite()))
                throw new BatchNoCompleteException(5102);
        try{
            return batchNoHeaderService.create(batchNoCompleteRequest);
        }catch (BatchNoCompleteException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/update")
    public BatchNoCompleteMsgModel update(@RequestBody BatchNoCompleteDTO batchNoCompleteRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoCompleteRequest.getSite()))
                throw new BatchNoCompleteException(5102);

        try{
            return batchNoHeaderService.update(batchNoCompleteRequest);
        }catch (BatchNoCompleteException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<BatchNoCompleteMsgModel> delete(@RequestBody BatchNoCompleteDTO batchNoCompleteRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoCompleteRequest.getSite()))
                throw new BatchNoCompleteException(5102);

        try{
            return ResponseEntity.ok(batchNoHeaderService.delete(batchNoCompleteRequest));
        }catch (BatchNoCompleteException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieve")
    public BatchNoComplete retrieve(@RequestBody BatchNoCompleteDTO batchNoCompleteRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoCompleteRequest.getSite()))
                throw new BatchNoCompleteException(5102);

        try{
            return batchNoHeaderService.retrieve(batchNoCompleteRequest);
        }catch (BatchNoCompleteException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveAll")
    public List<BatchNoComplete> retrieveAll(@RequestBody BatchNoCompleteDTO batchNoCompleteRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoCompleteRequest.getSite()))
                throw new BatchNoCompleteException(5102);

        try{
            return batchNoHeaderService.retrieveAll(batchNoCompleteRequest.getSite());
        }catch (BatchNoCompleteException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveTop50")
    public List<BatchNoComplete> retrieveTop50(@RequestBody BatchNoCompleteDTO batchNoCompleteRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoCompleteRequest.getSite()))
                throw new BatchNoCompleteException(5102);

        try{
            return batchNoHeaderService.retrieveTop50(batchNoCompleteRequest.getSite());
        }catch (BatchNoCompleteException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/isExist")
    public boolean isBatchNoCompleteExist(@RequestBody BatchNoCompleteDTO batchNoCompleteRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoCompleteRequest.getSite()))
                throw new BatchNoCompleteException(5102);

        try{
            return batchNoHeaderService.isBatchNoCompleteExist(batchNoCompleteRequest.getSite(),batchNoCompleteRequest.getBatchNo());
        }catch (BatchNoCompleteException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchNoCompleteByPhaseAndOperation")
    public BatchNoCompleteQty getBatchNoCompleteByPhaseAndOperation(@RequestBody BatchNoCompleteDTO request){
        try{
            return batchNoHeaderService.getBatchNoCompleteByPhaseAndOperation(request);
        }catch (BatchNoCompleteException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
