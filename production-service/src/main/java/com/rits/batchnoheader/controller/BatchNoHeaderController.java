package com.rits.batchnoheader.controller;

import com.rits.batchnoheader.dto.BatchNoHeaderRequest;
import com.rits.batchnoheader.exception.BatchNoHeaderException;
import com.rits.batchnoheader.model.BatchNoHeader;
import com.rits.batchnoheader.model.BatchNoHeaderMsgModel;
import com.rits.batchnoheader.service.BatchNoHeaderService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("app/v1/batchnoheader-service")
public class BatchNoHeaderController {

    @Autowired
    BatchNoHeaderService batchNoHeaderService;
    @PostMapping("/create")
    public BatchNoHeaderMsgModel create(@RequestBody BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoHeaderRequest.getSite()))
                throw new BatchNoHeaderException(5102);

        try{
            return batchNoHeaderService.create(batchNoHeaderRequest);
        }catch (BatchNoHeaderException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/update")
    public BatchNoHeaderMsgModel update(@RequestBody BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoHeaderRequest.getSite()))
                throw new BatchNoHeaderException(5102);

        try{
            return batchNoHeaderService.update(batchNoHeaderRequest);
        }catch (BatchNoHeaderException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<BatchNoHeaderMsgModel> delete(@RequestBody BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoHeaderRequest.getSite()))
                throw new BatchNoHeaderException(5102);

        try{
            return ResponseEntity.ok(batchNoHeaderService.delete(batchNoHeaderRequest));
        }catch (BatchNoHeaderException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieve")
    public ResponseEntity<BatchNoHeaderMsgModel> retrieve(@RequestBody BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoHeaderRequest.getSite()))
                throw new BatchNoHeaderException(5102);

        try{
            return ResponseEntity.ok(batchNoHeaderService.retrieve(batchNoHeaderRequest));
        }catch (BatchNoHeaderException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveAll")
    public ResponseEntity<BatchNoHeaderMsgModel> retrieveAll(@RequestBody BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoHeaderRequest.getSite()))
                throw new BatchNoHeaderException(5102);

        try{
            return ResponseEntity.ok(batchNoHeaderService.retrieveAll(batchNoHeaderRequest));
        }catch (BatchNoHeaderException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveBatchNoList")
    public ResponseEntity<BatchNoHeaderMsgModel> retrieveBatchNoList(@RequestBody BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoHeaderRequest.getSite()))
                throw new BatchNoHeaderException(5102);

        try{
            return ResponseEntity.ok(batchNoHeaderService.retrieveBatchNoList(batchNoHeaderRequest));
        }catch (BatchNoHeaderException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveTop50")
    public ResponseEntity<BatchNoHeaderMsgModel> retrieveTop50(@RequestBody BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoHeaderRequest.getSite()))
                throw new BatchNoHeaderException(5102);

        try{
            return ResponseEntity.ok(batchNoHeaderService.retrieveTop50(batchNoHeaderRequest));
        }catch (BatchNoHeaderException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/isExist")
    public boolean isBatchNoHeaderExist(@RequestBody BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoHeaderRequest.getSite()))
                throw new BatchNoHeaderException(5102);

        try{
            return batchNoHeaderService.isBatchNoHeaderExist(batchNoHeaderRequest.getSite(),batchNoHeaderRequest.getBatchNumber());
        }catch (BatchNoHeaderException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersion")
    public BatchNoHeader getBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersion(@RequestBody BatchNoHeaderRequest request) throws Exception {

        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoHeaderException(5102);

        try{
            return batchNoHeaderService.getBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersion(request.getSite(), request.getBatchNumber(), request.getOrderNo(), request.getMaterial(), request.getMaterialVersion());
        }catch (BatchNoHeaderException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/getBatchHeader")
    public ResponseEntity<BatchNoHeader> getBySiteAndBatchNo(@RequestBody BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoHeaderRequest.getSite()))
            throw new BatchNoHeaderException(5102);

        try{
            return ResponseEntity.ok(batchNoHeaderService.getBySiteAndBatchNo(batchNoHeaderRequest.getSite(),batchNoHeaderRequest.getBatchNumber()));
        }catch (BatchNoHeaderException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/retrieveOnlyBatchNumberList")
    public ResponseEntity<BatchNoHeaderMsgModel> retrieveOnlyBatchNumberList(@RequestBody BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {

        if(StringUtils.isEmpty(batchNoHeaderRequest.getSite()))
            throw new BatchNoHeaderException(5102);

        try{
            return ResponseEntity.ok(batchNoHeaderService.retrieveOnlyBatchNumberList(batchNoHeaderRequest));
        }catch (BatchNoHeaderException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
