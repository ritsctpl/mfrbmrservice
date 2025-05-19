package com.rits.batchnocomplete.service;

import com.rits.Utility.BOConverter;
import com.rits.batchnocomplete.dto.BatchNoCompleteDTO;
import com.rits.batchnocomplete.dto.BatchNoCompleteQty;
import com.rits.batchnocomplete.exception.BatchNoCompleteException;
import com.rits.batchnocomplete.model.BatchNoComplete;
import com.rits.batchnocomplete.model.BatchNoCompleteMsgModel;
import com.rits.batchnocomplete.repository.BatchNoCompleteRepository;
import com.rits.batchnoinqueue.exception.BatchNoInQueueException;
import com.rits.batchnoinwork.model.BatchNoInWork;
import com.rits.nextnumbergeneratorservice.model.MessageDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class BatchNoCompleteServiceImpl implements BatchNoCompleteService {

    private final BatchNoCompleteRepository batchNoCompleteRepository;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public BatchNoCompleteMsgModel create(BatchNoCompleteDTO batchNoCompleteRequest) throws Exception {

        if(!StringUtils.hasText(batchNoCompleteRequest.getBatchNo()) || !StringUtils.hasText(batchNoCompleteRequest.getOrderNumber()) || !StringUtils.hasText(batchNoCompleteRequest.getRecipe())
                && !StringUtils.hasText(batchNoCompleteRequest.getRecipeVersion()) || !StringUtils.hasText(batchNoCompleteRequest.getPhaseId()) || !StringUtils.hasText(batchNoCompleteRequest.getOperation()))
             throw new BatchNoCompleteException(124);

        String handle = createHandle(batchNoCompleteRequest);
        BatchNoComplete batchNoCompleteValidation = validateBatchNoComplete(batchNoCompleteRequest.getSite(), handle);
        if(batchNoCompleteValidation != null)
            throw new BatchNoCompleteException(125, batchNoCompleteRequest.getBatchNo());

        BatchNoComplete batchNoComplete = batchNoHeaderBuilder(batchNoCompleteRequest);
        batchNoComplete.setHandle(handle);
        batchNoComplete.setCreatedBy(batchNoCompleteRequest.getUser());
        batchNoComplete.setCreatedDateTime(LocalDateTime.now());

        batchNoCompleteRepository.save(batchNoComplete);

        String createdMessage = getFormattedMessage(1, batchNoCompleteRequest.getBatchNo());
        return BatchNoCompleteMsgModel.builder().response(batchNoComplete).message_details(MessageDetails.builder().msg(createdMessage).msg_type("S").build()).build();

    }
    @Override
    public BatchNoCompleteMsgModel update(BatchNoCompleteDTO batchNoCompleteRequest) throws Exception {
        if(!StringUtils.hasText(batchNoCompleteRequest.getBatchNo()) || !StringUtils.hasText(batchNoCompleteRequest.getOrderNumber()) || !StringUtils.hasText(batchNoCompleteRequest.getRecipe())
                && !StringUtils.hasText(batchNoCompleteRequest.getRecipeVersion()) || !StringUtils.hasText(batchNoCompleteRequest.getPhaseId()) || !StringUtils.hasText(batchNoCompleteRequest.getOperation()))
            throw new BatchNoCompleteException(124);

        String handle = createHandle(batchNoCompleteRequest);

        BatchNoComplete batchNoCompleteValidation = validateBatchNoComplete(batchNoCompleteRequest.getSite(), handle);
        if(batchNoCompleteValidation == null)
            throw new BatchNoCompleteException(126, batchNoCompleteRequest.getBatchNo());

        BatchNoComplete batchNoComplete = batchNoHeaderBuilder(batchNoCompleteRequest);
        batchNoComplete.setHandle(handle);
        batchNoComplete.setCreatedBy(batchNoCompleteValidation.getCreatedBy());
        batchNoComplete.setCreatedDateTime(batchNoCompleteValidation.getCreatedDateTime());
        batchNoComplete.setModifiedBy(batchNoCompleteRequest.getUser());
        batchNoComplete.setModifiedDateTime(LocalDateTime.now());

        batchNoCompleteRepository.save(batchNoComplete);
        String createdMessage = getFormattedMessage(2, batchNoCompleteRequest.getBatchNo());
        return BatchNoCompleteMsgModel.builder().response(batchNoComplete).message_details(MessageDetails.builder().msg(createdMessage).msg_type("S").build()).build();

    }

    private String createHandle(BatchNoCompleteDTO batchNoCompleteRequest){
        String batchNoBO = "BatchNoBO:" + batchNoCompleteRequest.getSite() + "," + batchNoCompleteRequest.getBatchNo();
        String orderNumberBO = "OrderNumberBO:" + batchNoCompleteRequest.getSite() + "," + batchNoCompleteRequest.getOrderNumber();
        String recipeBO = "RecipeBO:" + batchNoCompleteRequest.getSite() + "," + batchNoCompleteRequest.getRecipe() + "," + batchNoCompleteRequest.getRecipeVersion();
        String batchNORecipeHeaderBO = "BatchNoRecipeHeaderBO:" + batchNoCompleteRequest.getSite() + "," + recipeBO + "," + batchNoBO;

        return "BatchNoComplete:" + batchNoCompleteRequest.getSite() + "," + batchNoBO + "," + orderNumberBO + "," + batchNORecipeHeaderBO + "," + batchNoCompleteRequest.getPhaseId() + "," + batchNoCompleteRequest.getOperation();
    }    
    private BatchNoComplete validateBatchNoComplete(String site, String handle){
        return batchNoCompleteRepository.findBySiteAndActiveAndHandle(site, 1, handle);
    }

    private BatchNoComplete batchNoHeaderBuilder(BatchNoCompleteDTO batchNoCompleteRequest){
        String batchNoBO = "BatchNoBO:" + batchNoCompleteRequest.getSite() + "," + batchNoCompleteRequest.getBatchNo();
        String recipeBO = "RecipeBO:" + batchNoCompleteRequest.getSite() + "," + batchNoCompleteRequest.getRecipe() + "," + batchNoCompleteRequest.getRecipeVersion();

        BatchNoComplete batchNoComplete = BatchNoComplete.builder()
                .site(batchNoCompleteRequest.getSite())
                .dateTime(batchNoCompleteRequest.getDateTime())
                .batchNo(batchNoCompleteRequest.getBatchNo())
                .material(batchNoCompleteRequest.getMaterial())
                .materialVersion(batchNoCompleteRequest.getMaterialVersion())
                .recipe(batchNoCompleteRequest.getRecipe())
                .recipeVersion(batchNoCompleteRequest.getRecipeVersion())
                .batchNoRecipeHeaderBO("BatchNoRecipeHeaderBO:" + batchNoCompleteRequest.getSite() + "," + recipeBO + "," + batchNoBO)
                .batchNoHeaderBO("BatchNoHeaderBO:" + batchNoCompleteRequest.getSite() + "," + batchNoBO)
                .phaseId(batchNoCompleteRequest.getPhaseId())
                .operation(batchNoCompleteRequest.getOperation())
                .quantityBaseUom(batchNoCompleteRequest.getQuantityBaseUom())
                .quantityMeasuredUom(batchNoCompleteRequest.getQuantityMeasuredUom())
                .baseUom(batchNoCompleteRequest.getBaseUom())
                .measuredUom(batchNoCompleteRequest.getMeasuredUom())
                .queuedTimestamp(batchNoCompleteRequest.getQueuedTimestamp())
                .resource( batchNoCompleteRequest.getResource())
                .user(batchNoCompleteRequest.getUser())
                .qtyToComplete(batchNoCompleteRequest.getQtyToComplete())
                .yieldQuantityBaseUom(batchNoCompleteRequest.getYieldQuantityBaseUom())
                .yieldQuantityMeasuredUom(batchNoCompleteRequest.getYieldQuantityMeasuredUom())
                .scrapQuantityBaseUom(batchNoCompleteRequest.getScrapQuantityBaseUom())
                .scrapQuantityMeasuredUom(batchNoCompleteRequest.getScrapQuantityMeasuredUom())
                .orderNumber(batchNoCompleteRequest.getOrderNumber())
                .qualityApproval(batchNoCompleteRequest.isQualityApproval())
                .workcenter(batchNoCompleteRequest.getWorkcenter())
                .active(1)
                .build();
        return batchNoComplete;
    }



    @Override
    public BatchNoCompleteMsgModel delete(BatchNoCompleteDTO batchNoCompleteRequest) throws Exception{
        if(!StringUtils.hasText(batchNoCompleteRequest.getBatchNo()) && !StringUtils.hasText(batchNoCompleteRequest.getOrderNumber()) || !StringUtils.hasText(batchNoCompleteRequest.getRecipe())
                || !StringUtils.hasText(batchNoCompleteRequest.getRecipeVersion()) || !StringUtils.hasText(batchNoCompleteRequest.getPhaseId()) || !StringUtils.hasText(batchNoCompleteRequest.getOperation()))
            throw new BatchNoCompleteException(124);

        String handle = createHandle(batchNoCompleteRequest);

        BatchNoComplete batchNoCompleteValidation = validateBatchNoComplete(batchNoCompleteRequest.getSite(), handle);
        if(batchNoCompleteValidation == null)
            throw new BatchNoCompleteException(126, batchNoCompleteRequest.getBatchNo());

        batchNoCompleteValidation.setActive(0);
        batchNoCompleteValidation.setModifiedDateTime(LocalDateTime.now());

        batchNoCompleteRepository.save(batchNoCompleteValidation);
        String deletedMessage = getFormattedMessage(3, batchNoCompleteRequest.getBatchNo());

        return BatchNoCompleteMsgModel.builder().response(batchNoCompleteValidation).message_details(MessageDetails.builder().msg(deletedMessage).msg_type("S").build()).build();
    }

    @Override
    public BatchNoComplete retrieve(BatchNoCompleteDTO batchNoCompleteRequest) throws Exception{

        if(!StringUtils.hasText(batchNoCompleteRequest.getBatchNo()) || !StringUtils.hasText(batchNoCompleteRequest.getOrderNumber()) || !StringUtils.hasText(batchNoCompleteRequest.getRecipe())
                && !StringUtils.hasText(batchNoCompleteRequest.getRecipeVersion()) || !StringUtils.hasText(batchNoCompleteRequest.getPhaseId()) || !StringUtils.hasText(batchNoCompleteRequest.getOperation()))
            throw new BatchNoCompleteException(124);

        String handle = createHandle(batchNoCompleteRequest);

        BatchNoComplete batchNoCompleteValidation = validateBatchNoComplete(batchNoCompleteRequest.getSite(), handle);
        if(batchNoCompleteValidation == null)
            throw new BatchNoCompleteException(126, batchNoCompleteRequest.getBatchNo());

        return batchNoCompleteValidation;
    }

    @Override
    public List<BatchNoComplete> retrieveAll(String site) throws Exception{
        List<BatchNoComplete> batchNoHeaders = batchNoCompleteRepository.findBySiteAndActive(site, 1);

        return batchNoHeaders;
    }

    @Override
    public List<BatchNoComplete> retrieveTop50(String site) throws Exception{
        List<BatchNoComplete> batchNoHeaders = batchNoCompleteRepository.findTop50BySiteAndActive(site, 1);

        return batchNoHeaders;
    }

    @Override
    public boolean isBatchNoCompleteExist(String site, String batchNo) throws Exception{
        if(!StringUtils.hasText(batchNo))
            throw new BatchNoCompleteException(123);

        boolean checkExistance = batchNoCompleteRepository.existsBySiteAndActiveAndBatchNo(site, 1, batchNo);
        return checkExistance;
    }

    @Override
    public BatchNoComplete getBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUser(String site, String batchNoHeaderBO, String phaseId, String operation, String resource, String user, String orderNo) {

        BatchNoComplete batchNoComplete = batchNoCompleteRepository.findBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUserAndActiveAndOrderNumber(
                site, batchNoHeaderBO, phaseId, operation, resource, user, 1, orderNo);
        return batchNoComplete;
    }

    @Override
    public BatchNoCompleteQty getBatchNoCompleteByPhaseAndOperation(BatchNoCompleteDTO request) {
        BatchNoCompleteQty batchNoComplete = null;

        // Check if both batchNo and orderNumber are provided and are not blank
        if ((request.getBatchNo() != null && !request.getBatchNo().isEmpty()) &&
                (request.getOrderNumber() != null && !request.getOrderNumber().isEmpty())) {
            batchNoComplete = batchNoCompleteRepository.findBySiteAndBatchNoAndOrderNumberAndPhaseIdAndOperation(
                    request.getSite(), request.getBatchNo(), request.getOrderNumber(), request.getPhaseId(), request.getOperation());
        }
        // If only batchNo is provided and is not blank
        else if (request.getBatchNo() != null && !request.getBatchNo().isEmpty()) {
            batchNoComplete = batchNoCompleteRepository.findBySiteAndBatchNoAndPhaseIdAndOperation(
                    request.getSite(), request.getBatchNo(), request.getPhaseId(), request.getOperation());
        }
        // If only orderNumber is provided and is not blank
        else if (request.getOrderNumber() != null && !request.getOrderNumber().isEmpty()) {
            batchNoComplete = batchNoCompleteRepository.findBySiteAndOrderNumberAndPhaseIdAndOperation(
                    request.getSite(), request.getOrderNumber(), request.getPhaseId(), request.getOperation());
        }
        return batchNoComplete;
    }
}
