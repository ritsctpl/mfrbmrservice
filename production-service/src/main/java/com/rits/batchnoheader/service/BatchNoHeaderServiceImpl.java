package com.rits.batchnoheader.service;

import com.rits.batchnoheader.dto.BatchNoHeaderRequest;
import com.rits.batchnoheader.exception.BatchNoHeaderException;
import com.rits.batchnoheader.model.BatchNoHeader;
import com.rits.batchnoheader.model.BatchNoHeaderMsgModel;
import com.rits.batchnoheader.repository.BatchNoHeaderRepository;
import com.rits.nextnumbergeneratorservice.model.MessageDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BatchNoHeaderServiceImpl implements BatchNoHeaderService{

    private final BatchNoHeaderRepository batchNoHeaderRepository;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public BatchNoHeaderMsgModel create(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {

        if(!StringUtils.hasText(batchNoHeaderRequest.getBatchNumber()))
             throw new BatchNoHeaderException(123);

        String handle = "BatchNoHeaderBO:"+ batchNoHeaderRequest.getSite() + ",BatchNoBO:" + batchNoHeaderRequest.getSite() + "," + batchNoHeaderRequest.getBatchNumber();

        BatchNoHeader batchNoHeaderValidation = validateBatchNoHeader(batchNoHeaderRequest.getSite(), handle, batchNoHeaderRequest.getOrderNo());
        if(batchNoHeaderValidation != null)
            throw new BatchNoHeaderException(121, batchNoHeaderRequest.getBatchNumber());

        BatchNoHeader batchNoHeader = batchNoHeaderBuilder(batchNoHeaderRequest);
        batchNoHeader.setHandle(handle);
        batchNoHeader.setCreatedDateTime(LocalDateTime.now());

        batchNoHeaderRepository.save(batchNoHeader);
        String createdMessage = getFormattedMessage(1, batchNoHeaderRequest.getBatchNumber());
        return BatchNoHeaderMsgModel.builder().response(batchNoHeader).message_details(MessageDetails.builder().msg(createdMessage).msg_type("S").build()).build();

    }
    @Override
    public BatchNoHeaderMsgModel update(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception {
        if(!StringUtils.hasText(batchNoHeaderRequest.getBatchNumber()))
            throw new BatchNoHeaderException(123);

        String handle = "BatchNoHeaderBO:"+ batchNoHeaderRequest.getSite() + ",BatchNoBO:" + batchNoHeaderRequest.getSite() + "," + batchNoHeaderRequest.getBatchNumber();

        BatchNoHeader batchNoHeaderValidation = validateBatchNoHeader(batchNoHeaderRequest.getSite(), handle, batchNoHeaderRequest.getOrderNo());
        if(batchNoHeaderValidation == null)
            throw new BatchNoHeaderException(122, batchNoHeaderRequest.getBatchNumber());

        BatchNoHeader batchNoHeader = batchNoHeaderBuilder(batchNoHeaderRequest);
        batchNoHeader.setHandle(handle);
        batchNoHeader.setCreatedDateTime(batchNoHeaderValidation.getCreatedDateTime());
        batchNoHeader.setModifiedDateTime(LocalDateTime.now());

        batchNoHeaderRepository.save(batchNoHeader);
        String createdMessage = getFormattedMessage(2, batchNoHeaderRequest.getBatchNumber());
        return BatchNoHeaderMsgModel.builder().response(batchNoHeader).message_details(MessageDetails.builder().msg(createdMessage).msg_type("S").build()).build();

    }

    private BatchNoHeader validateBatchNoHeader(String site, String handle, String orderNo){
        return batchNoHeaderRepository.findBySiteAndActiveAndHandleAndOrderNo(site, 1, handle, orderNo);
    }

    private BatchNoHeader batchNoHeaderBuilder(BatchNoHeaderRequest batchNoHeaderRequest){
        BatchNoHeader batchNoHeader = BatchNoHeader.builder()
                .site(batchNoHeaderRequest.getSite())
                .batchNo(batchNoHeaderRequest.getBatchNumber())
                .orderNo(batchNoHeaderRequest.getOrderNo())
                .material(batchNoHeaderRequest.getMaterial())
                .materialVersion(batchNoHeaderRequest.getMaterialVersion())
                .status(batchNoHeaderRequest.getStatus())
                .recipeName(batchNoHeaderRequest.getRecipeName())
                .recipeVersion(batchNoHeaderRequest.getRecipeVersion())
                .totalQuantity(batchNoHeaderRequest.getTotalQuantity())
                .qtyToWorkOrder(batchNoHeaderRequest.getQtyToWorkOrder())
                .qtyInQueue(batchNoHeaderRequest.getQtyInQueue())
                .qtyInHold(batchNoHeaderRequest.getQtyInHold())
                .qtyDone(batchNoHeaderRequest.getQtyDone())
                .baseUom(batchNoHeaderRequest.getBaseUom())
                .measuredUom(batchNoHeaderRequest.getMeasuredUom())
                .conversionFactor(batchNoHeaderRequest.getConversionFactor())
                .releasedQuantityBaseUom(batchNoHeaderRequest.getReleasedQuantityBaseUom())
                .releasedQuantityMeasuredUom(batchNoHeaderRequest.getReleasedQuantityMeasuredUom())
                .active(1)
                .build();
        return batchNoHeader;
    }

    @Override
    public BatchNoHeaderMsgModel delete(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception{
        if(!StringUtils.hasText(batchNoHeaderRequest.getBatchNumber()))
            throw new BatchNoHeaderException(123);

        String handle = "BatchNoHeaderBO:"+ batchNoHeaderRequest.getSite() + ",BatchNoBO:" + batchNoHeaderRequest.getSite() + "," + batchNoHeaderRequest.getBatchNumber();

        BatchNoHeader batchNoHeaderValidation = validateBatchNoHeader(batchNoHeaderRequest.getSite(), handle, batchNoHeaderRequest.getOrderNo());
        if(batchNoHeaderValidation == null)
            throw new BatchNoHeaderException(122, batchNoHeaderRequest.getBatchNumber());

        batchNoHeaderValidation.setActive(0);
        batchNoHeaderValidation.setModifiedDateTime(LocalDateTime.now());

        batchNoHeaderRepository.save(batchNoHeaderValidation);
        String deletedMessage = getFormattedMessage(3, batchNoHeaderRequest.getBatchNumber());

        return BatchNoHeaderMsgModel.builder().response(batchNoHeaderValidation).message_details(MessageDetails.builder().msg(deletedMessage).msg_type("S").build()).build();
    }

    @Override
    public BatchNoHeaderMsgModel retrieve(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception{

        if(!StringUtils.hasText(batchNoHeaderRequest.getBatchNumber()))
            throw new BatchNoHeaderException(123);

        String handle = "BatchNoHeaderBO:"+ batchNoHeaderRequest.getSite() + ",BatchNoBO:" + batchNoHeaderRequest.getSite() + "," + batchNoHeaderRequest.getBatchNumber();

        BatchNoHeader batchNoHeaderValidation = validateBatchNoHeader(batchNoHeaderRequest.getSite(), handle, batchNoHeaderRequest.getOrderNo());
        if(batchNoHeaderValidation == null)
            throw new BatchNoHeaderException(122, batchNoHeaderRequest.getBatchNumber());

        return BatchNoHeaderMsgModel.builder().response(batchNoHeaderValidation).build();
    }

    @Override
    public BatchNoHeaderMsgModel retrieveAll(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception{
        List<BatchNoHeader> batchNoHeaders = batchNoHeaderRepository.findBySiteAndActive(batchNoHeaderRequest.getSite(), 1);
        return BatchNoHeaderMsgModel.builder().responseList(batchNoHeaders).build();
    }

    @Override
    public BatchNoHeaderMsgModel retrieveBatchNoList(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception{

        if(batchNoHeaderRequest.getBatchNumber() != null && batchNoHeaderRequest.getSite() !=null){
            List<BatchNoHeader> batchNoHeaders = batchNoHeaderRepository.findByBatchNoContainingIgnoreCaseAndSiteAndActive(batchNoHeaderRequest.getBatchNumber(), batchNoHeaderRequest.getSite(), 1);
            List<Map<String, String>> batchNos = batchNoHeaders.stream()
                    .map(batchNoHeader -> Map.of("batchNo", batchNoHeader.getOrderNo() + "_" + batchNoHeader.getBatchNo()))
                    .collect(Collectors.toList());
            return BatchNoHeaderMsgModel.builder().batchNos(batchNos).build();
        }
        List<BatchNoHeader> batchNoHeaders = batchNoHeaderRepository.findBySiteAndActive(batchNoHeaderRequest.getSite(), 1);
        List<Map<String, String>> batchNos = batchNoHeaders.stream()
                .map(batchNoHeader -> Map.of("batchNo", batchNoHeader.getOrderNo() + "_" + batchNoHeader.getBatchNo()))
                .collect(Collectors.toList());
        return BatchNoHeaderMsgModel.builder().batchNos(batchNos).build();
    }

    @Override
    public BatchNoHeaderMsgModel retrieveTop50(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception{
        List<BatchNoHeader> batchNoHeaders = batchNoHeaderRepository.findTop50BySiteAndActive(batchNoHeaderRequest.getSite(), 1);
        return BatchNoHeaderMsgModel.builder().responseList(batchNoHeaders).build();
    }

    @Override
    public boolean isBatchNoHeaderExist(String site,String batchNo) throws Exception{
        if(!StringUtils.hasText(batchNo))
            throw new BatchNoHeaderException(123);

        boolean checkExistance = batchNoHeaderRepository.existsBySiteAndActiveAndBatchNo(site, 1, batchNo);
        return checkExistance;
    }


    @Override
    public boolean isBatchNoHeader(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception{
        if(!StringUtils.hasText(batchNoHeaderRequest.getBatchNumber()))
            throw new BatchNoHeaderException(123);
        if(!StringUtils.hasText(batchNoHeaderRequest.getMaterial()))
            throw new BatchNoHeaderException(7014);

        boolean checkExistance = batchNoHeaderRepository.existsBySiteAndActiveAndBatchNoAndMaterialAndOrderNo(batchNoHeaderRequest.getSite(), 1, batchNoHeaderRequest.getBatchNumber(),
                batchNoHeaderRequest.getMaterial(), batchNoHeaderRequest.getOrderNo());
        return checkExistance;
    }

    @Override
    public BatchNoHeader getBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersion(String site, String batchNo, String orderNo, String material, String materialVersion) {

        BatchNoHeader batchNoHeaderDetails;
        if (orderNo != null) {
            batchNoHeaderDetails = batchNoHeaderRepository.findBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersionAndActive(site, batchNo, orderNo, material, materialVersion, 1);
        } else {
            batchNoHeaderDetails = batchNoHeaderRepository.findBySiteAndBatchNoAndMaterialAndMaterialVersionAndActive(site, batchNo, material,materialVersion ,1);
        }
        return batchNoHeaderDetails;
    }
    @Override
    public BatchNoHeader getBySiteAndBatchNo(String site, String batchNo) {
        return batchNoHeaderRepository.findBySiteAndBatchNoAndActive(site, batchNo, 1);
    }

    @Override
    public BatchNoHeader getBySiteAndBatchNumber(String site, String batchNo) {
        String orderNumber="";

        if (batchNo.contains("_")) {
            String[] parts = batchNo.split("_", 2);  // Split into max 2 parts
            orderNumber = parts[0];                  // First part is orderNumber
            batchNo = parts[1];                   // Second part is batchNumber
        }
        return batchNoHeaderRepository.findBySiteAndBatchNoAndOrderNoAndActive(site, batchNo, orderNumber,1);
    }

    @Override
    public BatchNoHeaderMsgModel retrieveOnlyBatchNumberList(BatchNoHeaderRequest batchNoHeaderRequest) {
        if(batchNoHeaderRequest.getBatchNumber() != null && batchNoHeaderRequest.getSite() !=null){
            List<BatchNoHeader> batchNoHeaders = batchNoHeaderRepository.findByBatchNoContainingIgnoreCaseAndSiteAndActive(batchNoHeaderRequest.getBatchNumber(), batchNoHeaderRequest.getSite(), 1);
            List<Map<String, String>> batchNos = batchNoHeaders.stream()
                    .map(batchNoHeader -> Map.of("batchNo", batchNoHeader.getBatchNo()))
                    .collect(Collectors.toList());
            return BatchNoHeaderMsgModel.builder().batchNos(batchNos).build();
        }
        List<BatchNoHeader> batchNoHeaders = batchNoHeaderRepository.findBySiteAndActive(batchNoHeaderRequest.getSite(), 1);
        List<Map<String, String>> batchNos = batchNoHeaders.stream()
                .map(batchNoHeader -> Map.of("batchNo", batchNoHeader.getBatchNo()))
                .collect(Collectors.toList());
        return BatchNoHeaderMsgModel.builder().batchNos(batchNos).build();
    }
}
