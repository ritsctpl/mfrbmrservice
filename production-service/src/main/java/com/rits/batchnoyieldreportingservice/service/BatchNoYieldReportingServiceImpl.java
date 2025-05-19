package com.rits.batchnoyieldreportingservice.service;

import com.rits.batchnodoneservice.exception.BatchNoDoneException;
import com.rits.batchnoyieldreportingservice.dto.BatchNoYieldReportingRequest;
import com.rits.batchnoyieldreportingservice.exception.BatchNoYieldReportingException;
import com.rits.batchnoyieldreportingservice.model.BatchNoYieldReporting;
import com.rits.batchnoyieldreportingservice.model.MessageDetails;
import com.rits.batchnoyieldreportingservice.model.MessageModel;
import com.rits.batchnoyieldreportingservice.repository.BatchNoYieldReportingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BatchNoYieldReportingServiceImpl implements BatchNoYieldReportingService {

    private final BatchNoYieldReportingRepository batchNoYieldReportingRepository;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }
    @Override
    public MessageModel create(BatchNoYieldReportingRequest request) {
        
        String handle = createHandle(request);
        BatchNoYieldReporting existingBatchNoYieldReporting = batchNoYieldReportingRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingBatchNoYieldReporting != null) {
            throw new BatchNoYieldReportingException(8002, request.getBatchNo());
        }

        BatchNoYieldReporting batchNoYieldReporting = batchNoYieldReportingBuilder(request);
        batchNoYieldReporting.setHandle(handle);
        batchNoYieldReporting.setCreatedBy(request.getUser());
        batchNoYieldReporting.setCreatedDateTime(LocalDateTime.now());
        
        batchNoYieldReportingRepository.save(batchNoYieldReporting);

        String createMessage = getFormattedMessage(1, request.getBatchNo());
        return MessageModel.builder().message_details(new MessageDetails(createMessage, "S")).response(batchNoYieldReporting).build();
    }

    @Override
    public MessageModel update(BatchNoYieldReportingRequest request) {

        String handle = createHandle(request);
        BatchNoYieldReporting existingBatchNoYieldReporting = batchNoYieldReportingRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingBatchNoYieldReporting == null) {
            throw new BatchNoYieldReportingException(8003, request.getBatchNo());
        }

        BatchNoYieldReporting batchNoYieldReporting = batchNoYieldReportingBuilder(request);
        batchNoYieldReporting.setHandle(handle);
        batchNoYieldReporting.setCreatedBy(existingBatchNoYieldReporting.getCreatedBy());
        batchNoYieldReporting.setCreatedDateTime(existingBatchNoYieldReporting.getCreatedDateTime());
        batchNoYieldReporting.setModifiedBy(request.getUser());
        batchNoYieldReporting.setModifiedDateTime(LocalDateTime.now());

        batchNoYieldReportingRepository.save(batchNoYieldReporting);

        String updateMessage = getFormattedMessage(2, request.getBatchNo());
        return MessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).response(batchNoYieldReporting).build();
    }

    @Override
    public MessageModel delete(BatchNoYieldReportingRequest request) {

        String handle = createHandle(request);
        BatchNoYieldReporting existingBatchNoYieldReporting = batchNoYieldReportingRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingBatchNoYieldReporting == null) {
            throw new BatchNoYieldReportingException(8003, request.getBatchNo());
        }

        existingBatchNoYieldReporting.setActive(0);
        existingBatchNoYieldReporting.setModifiedDateTime(LocalDateTime.now());

        batchNoYieldReportingRepository.save(existingBatchNoYieldReporting);

        String deleteMessage = getFormattedMessage(3, request.getBatchNo());
        return MessageModel.builder().message_details(new MessageDetails(deleteMessage, "S")).build();
    }

    @Override
    public BatchNoYieldReporting retrieve(BatchNoYieldReportingRequest request) {

        String handle = createHandle(request);
        BatchNoYieldReporting existingBatchNoYieldReporting = batchNoYieldReportingRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);
        if(existingBatchNoYieldReporting == null){
            throw new BatchNoYieldReportingException(7009);
        }

        return existingBatchNoYieldReporting;
    }

    @Override
    public List<BatchNoYieldReporting> retrieveAll(String site) {

        List<BatchNoYieldReporting> existingBatchNoYieldReporting = batchNoYieldReportingRepository.findBySiteAndActive(site, 1);
        return existingBatchNoYieldReporting;
    }

    @Override
    public List<BatchNoYieldReporting> retrieveTop50(String site) {
        List<BatchNoYieldReporting> existingBatchNoYieldReporting = batchNoYieldReportingRepository.findTop50BySiteAndActive(site, 1);
        return existingBatchNoYieldReporting;
    }

    @Override
    public boolean isBatchNoYieldReportingExist(String site, String batchNo) {
        if(!StringUtils.hasText(batchNo)){
            throw new BatchNoDoneException(7002);
        }

        boolean checkExistance = batchNoYieldReportingRepository.existsBySiteAndActiveAndBatchNo(site, 1, batchNo);
        return checkExistance;
    }

    public String createHandle(BatchNoYieldReportingRequest batchNoYieldReportingRequest){
        validateRequest(batchNoYieldReportingRequest);
        String batchNoBO = "BatchNoBO:" + batchNoYieldReportingRequest.getSite() + "," + batchNoYieldReportingRequest.getBatchNo();
        String orderNumberBO = "OrderNumberBO:" + batchNoYieldReportingRequest.getSite() + "," + batchNoYieldReportingRequest.getOrderNumber();
        String recipeBO = "RecipeBO:" + batchNoYieldReportingRequest.getSite() + "," + batchNoYieldReportingRequest.getRecipe() + "," + batchNoYieldReportingRequest.getRecipeVersion();
        String batchNoRecipeHeaderBO = "BatchNoRecipeHeaderBO:" + batchNoYieldReportingRequest.getSite() + "," + recipeBO + "," + batchNoBO;

        return "BatchNoYieldBO:" + batchNoYieldReportingRequest.getSite() + "," + batchNoBO + "," + orderNumberBO + "," + batchNoRecipeHeaderBO + "," + batchNoYieldReportingRequest.getPhaseId() + "," + batchNoYieldReportingRequest.getOperation();
    }

    public boolean validateRequest(BatchNoYieldReportingRequest request){
        if(!StringUtils.hasText(request.getBatchNo())){
            throw new BatchNoYieldReportingException(7002);
        }
        if(!StringUtils.hasText(request.getOrderNumber())){
            throw new BatchNoYieldReportingException(7003);
        }
        if(!StringUtils.hasText(request.getRecipe())){
            throw new BatchNoYieldReportingException(7004);
        }
        if(!StringUtils.hasText(request.getRecipeVersion())){
            throw new BatchNoYieldReportingException(7010);
        }
        if(!StringUtils.hasText(request.getPhaseId())){
            throw new BatchNoYieldReportingException(7005);
        }
        if(!StringUtils.hasText(request.getOperation())){
            throw new BatchNoYieldReportingException(7006);
        }
        return true;
    }

    private BatchNoYieldReporting batchNoYieldReportingBuilder(BatchNoYieldReportingRequest request){
        String batchNoBO = "BatchNoBO:" + request.getSite() + "," + request.getBatchNo();
        String recipeBO = "RecipeBO:" + request.getSite() + "," + request.getRecipe() + "," + request.getRecipeVersion();

        BatchNoYieldReporting batchNoYieldReporting = BatchNoYieldReporting.builder()
                .site(request.getSite())
                .batchNo(request.getBatchNo())
                .orderNumber(request.getOrderNumber())
                .recipe(request.getRecipe())
                .recipeVersion(request.getRecipeVersion())
                .batchNoRecipeHeaderBO("BatchNoRecipeHeaderBO:" + request.getSite() + "," + recipeBO + "," + batchNoBO)
                .phaseId(request.getPhaseId())
                .operation(request.getOperation())
                .resource(request.getResource())
                .theoreticalYieldBaseUom(request.getTheoreticalYieldBaseUom())
                .theoreticalYieldMeasuredUom(request.getTheoreticalYieldMeasuredUom())
                .actualYieldBaseUom(request.getActualYieldBaseUom())
                .actualYieldMeasuredUom(request.getActualYieldMeasuredUom())
                .yieldVarianceBaseUom(request.getYieldVarianceBaseUom())
                .yieldVarianceMeasuredUom(request.getYieldVarianceMeasuredUom())
                .scrapQuantityBaseUom(request.getScrapQuantityBaseUom())
                .scrapQuantityMeasuredUom(request.getScrapQuantityMeasuredUom())
                .baseUom(request.getBaseUom())
                .measuredUom(request.getMeasuredUom())
                .user(request.getUser())
                .reportTimestamp(LocalDateTime.now())
                .active(1)
                .build();

        return batchNoYieldReporting;
    }

}
