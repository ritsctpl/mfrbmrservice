package com.rits.batchnodoneservice.service;

import com.rits.Utility.ProcessOrderUtility;
import com.rits.batchnodoneservice.dto.BatchNoDoneRequest;
import com.rits.batchnodoneservice.dto.RetrieveRequest;
import com.rits.batchnodoneservice.exception.BatchNoDoneException;
import com.rits.batchnodoneservice.model.MessageDetails;
import com.rits.batchnodoneservice.model.MessageModel;
import com.rits.batchnodoneservice.model.BatchNoDone;
import com.rits.batchnodoneservice.repository.BatchNoDoneRepository;
import com.rits.processorderstateservice.exception.ProcessOrderStateException;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BatchNoDoneServiceImpl implements BatchNoDoneService {

    private final BatchNoDoneRepository batchNoDoneRepository;
    private final MessageSource localMessageSource;
    private final WebClient.Builder webClientBuilder;

    @Value("${workcenter-service.url}/getWorkCenterByResource")
    private String workcenterUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }
    @Override
    public MessageModel create(BatchNoDoneRequest request) throws Exception {

        String handle = createHandle(request);
        BatchNoDone existingBatchNoDone = batchNoDoneRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingBatchNoDone != null) {
            throw new BatchNoDoneException(7007, request.getBatchNo());
        }

        BatchNoDone batchNoDone = batchNoDoneBuilder(request);
        batchNoDone.setHandle(handle);
        batchNoDone.setCreatedBy(request.getUser());
        batchNoDone.setCreatedDateTime(LocalDateTime.now());

        batchNoDoneRepository.save(batchNoDone);

        RetrieveRequest retrieveRequest = RetrieveRequest.builder()
                .site(request.getSite())
                .resource(request.getResource())
                .build();

        String workcenter = webClientBuilder.build()
                .post()
                .uri(workcenterUrl)
                .bodyValue(retrieveRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .site(request.getSite())
                .eventType("doneSfcBatch")
                .userId(request.getUser())
                .batchNo(request.getOrderNumber() + "_" + request.getBatchNo())
                .orderNumber(request.getOrderNumber())
                .operation(request.getOperation())
                .phaseId(request.getPhaseId())
                .workcenterId(workcenter)
                .resourceId(request.getResource())
                .material(request.getMaterial())
                .materialVersion(request.getMaterialVersion())
                .shopOrderBO(request.getOrderNumber())
                .qty((request.getQtyDone() != null) ? request.getQtyDone().intValue() : 0)
//                .quantityScrapped(request.getScrapQuantityBaseUom().intValue())
                .topic("production-log")
                .status("Active")
                .createdDatetime(LocalDateTime.now())
                .eventData(request.getBatchNo() + " Done successfully")
                .build();

        boolean productionLog = ProcessOrderUtility.productionLog(productionLogRequest);
        if (!productionLog) {
            throw new BatchNoDoneException(7024);
        }
        String createMessage = getFormattedMessage(1, request.getBatchNo());
        return com.rits.batchnodoneservice.model.MessageModel.builder().message_details(new MessageDetails(createMessage, "S")).response(batchNoDone).build();
    }

    @Override
    public MessageModel update(BatchNoDoneRequest request) {

        String handle = createHandle(request);
        BatchNoDone existingBatchNoDone = batchNoDoneRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingBatchNoDone == null) {
            throw new BatchNoDoneException(7008, request.getBatchNo());
        }

        BatchNoDone batchNoDone = batchNoDoneBuilder(request);
        batchNoDone.setHandle(handle);
        batchNoDone.setCreatedBy(existingBatchNoDone.getCreatedBy());
        batchNoDone.setCreatedDateTime(existingBatchNoDone.getCreatedDateTime());
        batchNoDone.setModifiedBy(request.getUser());
        batchNoDone.setModifiedDateTime(LocalDateTime.now());

        batchNoDoneRepository.save(batchNoDone);

        String updateMessage = getFormattedMessage(2, request.getBatchNo());
        return com.rits.batchnodoneservice.model.MessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).response(batchNoDone).build();
    }

    @Override
    public MessageModel delete(BatchNoDoneRequest request) {

        String handle = createHandle(request);
        BatchNoDone existingBatchNoDone = batchNoDoneRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingBatchNoDone == null) {
            throw new BatchNoDoneException(7008, request.getBatchNo());
        }
        existingBatchNoDone.setActive(0);
        existingBatchNoDone.setModifiedDateTime(LocalDateTime.now());

        batchNoDoneRepository.save(existingBatchNoDone);

        String deleteMessage = getFormattedMessage(3, request.getBatchNo());
        return com.rits.batchnodoneservice.model.MessageModel.builder().message_details(new MessageDetails(deleteMessage, "S")).build();
    }

    @Override
    public BatchNoDone retrieve(BatchNoDoneRequest request) {

        String handle = createHandle(request);
        BatchNoDone existingBatchNoDone = batchNoDoneRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);
        if(existingBatchNoDone == null){
            throw new BatchNoDoneException(7009);
        }
        return existingBatchNoDone;
    }

    @Override
    public List<BatchNoDone> retrieveAll(String site) {

        List<BatchNoDone> existingBatchNoDoneList = batchNoDoneRepository.findBySiteAndActive(site, 1);
        return existingBatchNoDoneList;
    }

    @Override
    public List<BatchNoDone> retrieveTop50(String site) {
        List<BatchNoDone> existingBatchNoDoneList = batchNoDoneRepository.findTop50BySiteAndActive(site, 1);
        return existingBatchNoDoneList;
    }

    @Override
    public boolean isBatchNoDoneExist(String site, String batchNo) {
        if(!StringUtils.hasText(batchNo)){
            throw new BatchNoDoneException(7002);
        }
        boolean checkExistance = batchNoDoneRepository.existsBySiteAndActiveAndBatchNo(site, 1, batchNo);
        return checkExistance;
    }

    private String createHandle(BatchNoDoneRequest batchNoDoneRequest){
        validateRequest(batchNoDoneRequest);
        String batchNoBO = "BatchNoBO:" + batchNoDoneRequest.getSite() + "," + batchNoDoneRequest.getBatchNo();
        String orderNumberBO = "OrderNumberBO:" + batchNoDoneRequest.getSite() + "," + batchNoDoneRequest.getOrderNumber();
        String recipeBO = "RecipeBO:" + batchNoDoneRequest.getSite() + "," + batchNoDoneRequest.getRecipe() + "," + batchNoDoneRequest.getRecipeVersion();
        String batchNoRecipeHeaderBO = "BatchNoRecipeHeaderBO:" + batchNoDoneRequest.getSite() + "," + recipeBO + "," + batchNoBO;

        return "BatchNoDone:" + batchNoDoneRequest.getSite() + "," + batchNoBO + "," + orderNumberBO + "," + batchNoRecipeHeaderBO + "," + batchNoDoneRequest.getPhaseId() + "," + batchNoDoneRequest.getOperation();
    }

    public boolean validateRequest(BatchNoDoneRequest request){
        if(!StringUtils.hasText(request.getBatchNo())){
            throw new BatchNoDoneException(7002);
        }
        if(!StringUtils.hasText(request.getOrderNumber())){
            throw new BatchNoDoneException(7003);
        }
        if(!StringUtils.hasText(request.getRecipe())){
            throw new BatchNoDoneException(7004);
        }
        if(!StringUtils.hasText(request.getRecipeVersion())){
            throw new BatchNoDoneException(7010);
        }
        if(!StringUtils.hasText(request.getPhaseId())){
            throw new BatchNoDoneException(7005);
        }
        if(!StringUtils.hasText(request.getOperation())){
            throw new BatchNoDoneException(7006);
        }
        return true;
    }

    private BatchNoDone batchNoDoneBuilder(BatchNoDoneRequest request){
        String batchNoBO = "BatchNoBO:" + request.getSite() + "," + request.getBatchNo();
        String recipeBO = "RecipeBO:" + request.getSite() + "," + request.getRecipe() + "," + request.getRecipeVersion();

        BatchNoDone batchNoDone = BatchNoDone.builder()
                .site(request.getSite())
                .dateTime(request.getDateTime())
                .batchNo(request.getBatchNo())
                .batchNoHeaderBO("BatchNoHeaderBO:" + request.getSite() + "," + batchNoBO)
                .material(request.getMaterial())
                .materialVersion(request.getMaterialVersion())
                .recipe(request.getRecipe())
                .recipeVersion(request.getRecipeVersion())
                .batchNoRecipeHeaderBO("BatchNoRecipeHeaderBO:" + request.getSite() + "," + recipeBO + "," + batchNoBO)
                .workcenter(request.getWorkcenter())
                .phaseId(request.getPhaseId())
                .operation(request.getOperation())
                .quantityBaseUom(request.getQuantityBaseUom())
                .quantityMeasuredUom(request.getQuantityMeasuredUom())
                .baseUom(request.getBaseUom())
                .measuredUom(request.getMeasuredUom())
                .queuedTimestamp(request.getQueuedTimestamp())
                .resource(request.getResource())
                .user(request.getUser())
                .qtyDone(request.getQtyDone())
                .doneQuantityBaseUom(request.getDoneQuantityBaseUom())
                .doneQuantityMeasuredUom(request.getDoneQuantityMeasuredUom())
                .scrapQuantityBaseUom(request.getScrapQuantityBaseUom())
                .scrapQuantityMeasuredUom(request.getScrapQuantityMeasuredUom())
                .orderNumber(request.getOrderNumber())
                .qualityApproval(request.isQualityApproval())
                .type("done")
                .active(1)
                .build();
        
       return batchNoDone; 
    }

    @Override
    public BatchNoDone getBySiteAndBatchNoHeaderBOAndOrderNoAndPhaseIdAndOperationAndResource(String site, String batchNoHeaderBO, String orderNumber,String phaseId, String operation, String resource) {
        BatchNoDone batchNoDone;
        if(orderNumber!=null) {
             batchNoDone = batchNoDoneRepository.findBySiteAndBatchNoHeaderBOAndOrderNumberAndPhaseIdAndOperationAndResourceAndActive(
                    site, batchNoHeaderBO, orderNumber, phaseId, operation, resource, 1);
        }
        else {
              batchNoDone = batchNoDoneRepository.findBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndActive(site, batchNoHeaderBO, phaseId,operation,resource ,1);
        }
        return batchNoDone;
        }

}
