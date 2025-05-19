package com.rits.batchnophaseprogressservice.service;

import com.rits.Utility.BOConverter;
import com.rits.batchnophaseprogressservice.dto.*;
import com.rits.batchnophaseprogressservice.exception.BatchNoPhaseProgressException;
import com.rits.batchnophaseprogressservice.model.*;
import com.rits.batchnophaseprogressservice.repository.BatchNoPhaseProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchNoPhaseProgressServiceImpl implements BatchNoPhaseProgressService {

    private final BatchNoPhaseProgressRepository batchNoPhaseProgressRepository;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }
    @Override
    public MessageModel create(BatchNoPhaseProgressRequest request) {
        
        String handle = createHandle(request);
        BatchNoPhaseProgress existingBatchNoPhaseProgress = batchNoPhaseProgressRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingBatchNoPhaseProgress != null) {
            throw new BatchNoPhaseProgressException(345, request.getBatchNo());
        }
        
        String batchNoBO = "BatchNoBO:" + request.getSite() + "," + request.getBatchNo();
        String recipeBO = "RecipeBO:" + request.getSite() + "," + request.getRecipe() + "," + request.getRecipeVersion();
        BatchNoPhaseProgress batchNoPhaseProgress = BatchNoPhaseProgress.builder()
                .handle(handle)
                .site(request.getSite())
                .dateTime(request.getDateTime())
                .batchNo(request.getBatchNo())
                .batchNoHeaderBO("BatchNoHeaderBO:" + request.getSite() + "," + batchNoBO)
                .material(request.getMaterial())
                .materialVersion(request.getMaterialVersion())
                .recipe(request.getRecipe())
                .recipeVersion(request.getRecipeVersion())
                .batchNoRecipeHeaderBO("BatchNoRecipeHeaderBO:" + request.getSite() + "," + recipeBO + "," + batchNoBO)
                .orderNumber(request.getOrderNumber())
                .phaseProgress(
                        request.getPhaseProgress().stream()
                                .map(phaseRequest -> PhaseProgress.builder()
                                        .phase(phaseRequest.getPhase())
                                        .operation(phaseRequest.getOperation())
                                        .startQuantityBaseUom(phaseRequest.getStartQuantityBaseUom())
                                        .startQuantityMeasuredUom(phaseRequest.getStartQuantityMeasuredUom())
                                        .completeQuantityBaseUom(phaseRequest.getCompleteQuantityBaseUom())
                                        .completeQuantityMeasuredUom(phaseRequest.getCompleteQuantityMeasuredUom())
                                        .scrapQuantity(phaseRequest.getScrapQuantity())
                                        .scrapQuantityBaseUom(phaseRequest.getScrapQuantityBaseUom())
                                        .scrapQuantityMeasuredUom(phaseRequest.getScrapQuantityMeasuredUom())
                                        .baseUom(phaseRequest.getBaseUom())
                                        .measuredUom(phaseRequest.getMeasuredUom())
                                        .startTimestamp(phaseRequest.getStartTimestamp())
                                        .endTimestamp(phaseRequest.getEndTimestamp())
                                        .status(phaseRequest.getStatus())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .user(request.getUser())
                .createdDateTime(LocalDateTime.now())
                .createdBy(request.getUser())
                .active(1)
                .build();

        batchNoPhaseProgressRepository.save(batchNoPhaseProgress);

        String createMessage = getFormattedMessage(1, request.getBatchNo());
        return MessageModel.builder().message_details(new MessageDetails(createMessage, "S")).response(batchNoPhaseProgress).build();
    }

    @Override
    public MessageModel update(BatchNoPhaseProgressRequest request) {

        String handle = createHandle(request);
        BatchNoPhaseProgress existingBatchNoPhaseProgress = batchNoPhaseProgressRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingBatchNoPhaseProgress == null) {
            throw new BatchNoPhaseProgressException(7008, request.getBatchNo());
        }
        String batchNoBO = "BatchNoBO:" + request.getSite() + "," + request.getBatchNo();

        existingBatchNoPhaseProgress.setDateTime(request.getDateTime());
        existingBatchNoPhaseProgress.setBatchNoHeaderBO(("BatchNoHeaderBO:" + request.getSite() + "," + batchNoBO));
        existingBatchNoPhaseProgress.setMaterial(request.getMaterial());
        existingBatchNoPhaseProgress.setMaterialVersion(request.getMaterialVersion());
        existingBatchNoPhaseProgress.setUser(request.getUser());
        existingBatchNoPhaseProgress.setModifiedBy(request.getUser());
        existingBatchNoPhaseProgress.setModifiedDateTime(LocalDateTime.now());

        for (PhaseProgress requestPhaseProgress : request.getPhaseProgress()) {
            boolean updated = false;

            // Check if the phase and operation exist
            for (PhaseProgress existingPhaseProgress : existingBatchNoPhaseProgress.getPhaseProgress()) {
                if (existingPhaseProgress.getPhase().equals(requestPhaseProgress.getPhase()) &&
                        existingPhaseProgress.getOperation().equals(requestPhaseProgress.getOperation())) {

                    if(requestPhaseProgress.getStartQuantityBaseUom() != null) {
                        existingPhaseProgress.setStartQuantityBaseUom(requestPhaseProgress.getStartQuantityBaseUom());
                        existingPhaseProgress.setStartQuantityMeasuredUom(requestPhaseProgress.getStartQuantityMeasuredUom());
                    }

                    existingPhaseProgress.setCompleteQuantityBaseUom(requestPhaseProgress.getCompleteQuantityBaseUom());
                    existingPhaseProgress.setCompleteQuantityMeasuredUom(requestPhaseProgress.getCompleteQuantityMeasuredUom());
                    existingPhaseProgress.setScrapQuantity(requestPhaseProgress.getScrapQuantity());
                    existingPhaseProgress.setScrapQuantityBaseUom(requestPhaseProgress.getScrapQuantityBaseUom());
                    existingPhaseProgress.setScrapQuantityMeasuredUom(requestPhaseProgress.getScrapQuantityMeasuredUom());
                    existingPhaseProgress.setBaseUom(requestPhaseProgress.getBaseUom());
                    existingPhaseProgress.setMeasuredUom(requestPhaseProgress.getMeasuredUom());
                    existingPhaseProgress.setStartTimestamp(requestPhaseProgress.getStartTimestamp());
                    existingPhaseProgress.setEndTimestamp(requestPhaseProgress.getEndTimestamp());
                    existingPhaseProgress.setStatus(requestPhaseProgress.getStatus());

                    updated = true;
                    break;
                }
            }

            // If no matching phase and operation were found, add a new entry
            if (!updated) {
                PhaseProgress.PhaseProgressBuilder builder = PhaseProgress.builder()
                        .phase(requestPhaseProgress.getPhase())
                        .operation(requestPhaseProgress.getOperation())
                        .completeQuantityBaseUom(requestPhaseProgress.getCompleteQuantityBaseUom())
                        .completeQuantityMeasuredUom(requestPhaseProgress.getCompleteQuantityMeasuredUom())
                        .scrapQuantity(requestPhaseProgress.getScrapQuantity())
                        .scrapQuantityBaseUom(requestPhaseProgress.getScrapQuantityBaseUom())
                        .scrapQuantityMeasuredUom(requestPhaseProgress.getScrapQuantityMeasuredUom())
                        .baseUom(requestPhaseProgress.getBaseUom())
                        .measuredUom(requestPhaseProgress.getMeasuredUom())
                        .startTimestamp(requestPhaseProgress.getStartTimestamp())
                        .endTimestamp(requestPhaseProgress.getEndTimestamp())
                        .status(requestPhaseProgress.getStatus());

                if (requestPhaseProgress.getStartQuantityBaseUom() != null) {
                    builder.startQuantityBaseUom(requestPhaseProgress.getStartQuantityBaseUom());
                    builder.startQuantityMeasuredUom(requestPhaseProgress.getStartQuantityMeasuredUom());
                }

                PhaseProgress newPhaseProgress = builder.build();
                existingBatchNoPhaseProgress.getPhaseProgress().add(newPhaseProgress);
            }
        }

        batchNoPhaseProgressRepository.save(existingBatchNoPhaseProgress);

        String updateMessage = getFormattedMessage(2, request.getBatchNo());
        return MessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).response(existingBatchNoPhaseProgress).build();
    }
    @Override
    public MessageModel delete(BatchNoPhaseProgressRequest request) {

        String handle = createHandle(request);
        BatchNoPhaseProgress existingBatchNoPhaseProgress = batchNoPhaseProgressRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingBatchNoPhaseProgress == null) {
            throw new BatchNoPhaseProgressException(7008, request.getBatchNo());
        }

        existingBatchNoPhaseProgress.setActive(0);
        existingBatchNoPhaseProgress.setModifiedDateTime(LocalDateTime.now());

        batchNoPhaseProgressRepository.save(existingBatchNoPhaseProgress);

        String deleteMessage = getFormattedMessage(3, request.getBatchNo());
        return MessageModel.builder().message_details(new MessageDetails(deleteMessage, "S")).build();
    }

    @Override
    public BatchNoPhaseProgress retrieve(BatchNoPhaseProgressRequest request) {

        String handle = createHandle(request);
        BatchNoPhaseProgress existingBatchNoPhaseProgress = batchNoPhaseProgressRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);
        if(existingBatchNoPhaseProgress == null){
            throw new BatchNoPhaseProgressException(7009);
        }
        return existingBatchNoPhaseProgress;
    }

    @Override
    public List<BatchNoPhaseProgress> retrieveAll(String site) {

        List<BatchNoPhaseProgress> existingBatchNoPhaseProgress = batchNoPhaseProgressRepository.findBySiteAndActive(site, 1);
        return existingBatchNoPhaseProgress;
    }

    @Override
    public List<BatchNoPhaseProgress> retrieveTop50(String site) {
        List<BatchNoPhaseProgress> existingBatchNoPhaseProgress = batchNoPhaseProgressRepository.findTop50BySiteAndActive(site, 1);
        return existingBatchNoPhaseProgress;
    }

    @Override
    public boolean isBatchNoPhaseProgressExist(String site, String batchNo) {

        boolean checkExistance = batchNoPhaseProgressRepository.existsBySiteAndActiveAndBatchNo(site, 1, batchNo);
        return checkExistance;
    }

    @Override
    public BatchNoPhaseProgress getBySiteAndBatchNoAndMaterialAndMaterialVersionAndBatchNoHeaderBO(String site, String batchNumber, String material, String materialVersion, String batchNoHeaderBO) {
        return batchNoPhaseProgressRepository.findBySiteAndBatchNoAndMaterialAndMaterialVersionAndBatchNoHeaderBOAndActive(site,batchNumber,material,materialVersion,batchNoHeaderBO,1);
    }

    @Override
    public BatchNoPhaseProgress getBySiteAndBatchNoAndMaterialAndOrderNoAndMaterialVersion(String site, String batchNumber, String material, String orderNumber, String materialVersion) {
        return batchNoPhaseProgressRepository.findBySiteAndBatchNoAndMaterialAndOrderNumberAndMaterialVersionAndActive(site,batchNumber,material,orderNumber,materialVersion,1);
    }

    private String createHandle(BatchNoPhaseProgressRequest request){
        validateRequest(request);
        String batchNoBO = "BatchNoBO:" + request.getSite() + "," + request.getBatchNo();
        String orderNumberBO = "OrderNumberBO:" + request.getSite() + "," + request.getOrderNumber();
        String recipeBO = "RecipeBO:" + request.getSite() + "," + request.getRecipe() + "," + request.getRecipeVersion();
        String batchNORecipeHeaderBO = "BatchNoRecipeHeaderBO:" + request.getSite() + "," + recipeBO + "," + batchNoBO;

        return "BatchNoPhaseProgress:" + request.getSite() + "," + batchNoBO + "," + orderNumberBO + "," + batchNORecipeHeaderBO;
    }

    public boolean validateRequest(BatchNoPhaseProgressRequest request){
        if(!StringUtils.hasText(request.getBatchNo())){
            throw new BatchNoPhaseProgressException(7002);
        }
        if(!StringUtils.hasText(request.getOrderNumber())){
            throw new BatchNoPhaseProgressException(7003);
        }
        if(!StringUtils.hasText(request.getRecipe())){
            throw new BatchNoPhaseProgressException(7004);
        }
        if(!StringUtils.hasText(request.getRecipeVersion())){
            throw new BatchNoPhaseProgressException(7010);
        }
        return true;
    }

}
