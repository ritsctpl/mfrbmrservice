package com.rits.batchnohold.service;

import com.rits.batchnodoneservice.model.BatchNoDone;
import com.rits.batchnohold.dto.BatchNoHoldRequest;
import com.rits.batchnohold.exception.BatchNoHoldException;
import com.rits.batchnohold.model.BatchNoHold;
import com.rits.batchnohold.model.BatchNoHoldMessageModel;
import com.rits.batchnohold.repository.BatchNoHoldRepository;
import com.rits.batchnoinwork.dto.BatchNoInWorkRequest;
import com.rits.batchnoinwork.exception.BatchNoInWorkException;
import com.rits.batchnoinwork.model.BatchNoInWork;
import com.rits.batchnoinwork.service.BatchNoInWorkService;
import com.rits.scrapservice.model.MessageDetails;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchNoHoldServiceImpl implements BatchNoHoldService{
    private final BatchNoHoldRepository batchNoHoldRepository;
    @Autowired
    private BatchNoInWorkService batchNoInWorkService;

    @Override
    public BatchNoHoldMessageModel hold(BatchNoHoldRequest batchNoHoldRequest) throws Exception{

        BatchNoHold existingBatchNoHold = batchNoHoldRepository.findBySiteAndBatchNoAndResourceAndOperationAndPhaseIdAndActive(
                batchNoHoldRequest.getSite(),
                batchNoHoldRequest.getBatchNo(),
                batchNoHoldRequest.getResource(),
                batchNoHoldRequest.getOperation(),
                batchNoHoldRequest.getPhaseId(),
                1 // Active status
        );
        if(existingBatchNoHold != null){
            throw new BatchNoHoldException(7039, batchNoHoldRequest.getBatchNo());
        }

        BatchNoInWorkRequest batchNoInWorkRequest = BatchNoInWorkRequest.builder()
                .site(batchNoHoldRequest.getSite())
                .batchNo(batchNoHoldRequest.getBatchNo())
                .orderNumber(batchNoHoldRequest.getOrderNumber())
                .recipe(batchNoHoldRequest.getRecipe())
                .recipeVersion(batchNoHoldRequest.getRecipeVersion())
                .phaseId(batchNoHoldRequest.getPhaseId())
                .operation(batchNoHoldRequest.getOperation())
                .build();

        batchNoInWorkService.deleteBatchNoInWork(batchNoInWorkRequest);

        String batchNoBO = "BatchNoBO:" + batchNoHoldRequest.getSite() + "," + batchNoHoldRequest.getBatchNo();
        String recipeBO = "RecipeBO:" + batchNoHoldRequest.getSite() + "," + batchNoHoldRequest.getRecipe() + "," + batchNoHoldRequest.getRecipeVersion();
        String batchNoRecipeHeaderBO = "BatchNoRecipeHeaderBO:" + batchNoHoldRequest.getSite() + "," + recipeBO + "," + batchNoBO;

        BatchNoHold response = BatchNoHold.builder()
                .site(batchNoHoldRequest.getSite())
                .handle("Hold:" + batchNoHoldRequest.getSite() + "," + (StringUtils.isEmpty(batchNoHoldRequest.getBatchNo()) ? null : batchNoHoldRequest.getBatchNo()))
                .batchNo(StringUtils.isEmpty(batchNoHoldRequest.getBatchNo()) ? null : batchNoHoldRequest.getBatchNo())
                .status("Hold")
                .phaseId(StringUtils.isEmpty(batchNoHoldRequest.getPhaseId()) ? null : batchNoHoldRequest.getPhaseId())
                .operation(StringUtils.isEmpty(batchNoHoldRequest.getOperation()) ? null : batchNoHoldRequest.getOperation())
                .resource(StringUtils.isEmpty(batchNoHoldRequest.getResource()) ? null : batchNoHoldRequest.getResource())
                .orderNumber(StringUtils.isEmpty(batchNoHoldRequest.getOrderNumber()) ? null : batchNoHoldRequest.getOrderNumber())
                .material(StringUtils.isEmpty(batchNoHoldRequest.getMaterial()) ? null : batchNoHoldRequest.getMaterial())
                .materialVersion(StringUtils.isEmpty(batchNoHoldRequest.getMaterialVersion()) ? null : batchNoHoldRequest.getMaterialVersion())
                .recipe(StringUtils.isEmpty(batchNoHoldRequest.getRecipe()) ? null : batchNoHoldRequest.getRecipe())
                .recipeVersion(StringUtils.isEmpty(batchNoHoldRequest.getRecipeVersion()) ? null : batchNoHoldRequest.getRecipeVersion())
                .scrapQuantity(StringUtils.isEmpty(batchNoHoldRequest.getScrapQuantity()) ? null : batchNoHoldRequest.getScrapQuantity())
                .holdQty(batchNoHoldRequest.getQty())
                .user(batchNoHoldRequest.getUser())
                .reasonCode(batchNoHoldRequest.getReasonCode())
                .comment(batchNoHoldRequest.getComment())
                .createdDateTime(LocalDateTime.now())
                .createdBy(batchNoHoldRequest.getUser())
                .batchNoHeaderHandle(batchNoBO)
                .batchNoRecipeHeaderHandle(batchNoRecipeHeaderBO)
                .baseUom(batchNoHoldRequest.getBaseUom())
                .qualityApproval(true)
                .type("hold")
                .active(1)
                .build();
        batchNoHoldRepository.save(response);

        return BatchNoHoldMessageModel.builder()
                .message_details(new MessageDetails(batchNoHoldRequest.getBatchNo() + " Hold ", "S"))
                .build();
    }

    @Override
    public BatchNoHoldMessageModel unhold(BatchNoHoldRequest batchNoHoldRequest) throws Exception {
        // Fetch records based on the given parameters (query for active records)
        BatchNoHold existingBatchNoHold = batchNoHoldRepository.findBySiteAndBatchNoAndResourceAndOperationAndPhaseIdAndActive(
                batchNoHoldRequest.getSite(),
                batchNoHoldRequest.getBatchNo(),
                batchNoHoldRequest.getResource(),
                batchNoHoldRequest.getOperation(),
                batchNoHoldRequest.getPhaseId(),
                1 // Active status
        );

        // If no records are found, throw an exception with a specific error code
        if (existingBatchNoHold == null) {
            throw new BatchNoHoldException(340); // No record found to unhold
        }

        existingBatchNoHold.setActive(0);
        existingBatchNoHold.setModifiedDateTime(LocalDateTime.now());
        batchNoHoldRepository.save(existingBatchNoHold);

        BatchNoInWorkRequest batchNoInWorkRequest = BatchNoInWorkRequest.builder()
                .site(batchNoHoldRequest.getSite())
                .batchNo(batchNoHoldRequest.getBatchNo())
                .orderNumber(batchNoHoldRequest.getOrderNumber())
                .recipe(batchNoHoldRequest.getRecipe())
                .recipeVersion(batchNoHoldRequest.getRecipeVersion())
                .phaseId(batchNoHoldRequest.getPhaseId())
                .operation(batchNoHoldRequest.getOperation())
                .build();

        batchNoInWorkService.unDeleteBatchNoInWork(batchNoInWorkRequest);

        // Return a success message model
        return BatchNoHoldMessageModel.builder()
                .message_details(new MessageDetails(batchNoHoldRequest.getBatchNo() + " Unhold successfully", "S"))
                .build();
    }

    @Override
    public boolean isBatchOnHold(String site, String batchNo) throws Exception{
        Optional<BatchNoHold> batchStatus = batchNoHoldRepository.findBySiteAndBatchNoAndActive(site, batchNo,1);
        return batchStatus.isPresent() && "HOLD".equalsIgnoreCase(batchStatus.get().getStatus());
    }

    public List<BatchNoHold> getBatchHoldList(BatchNoInWorkRequest request) throws Exception {

        try {
            List<BatchNoHold> batchNoHoldList = new ArrayList<>();
            if(!StringUtils.isEmpty(request.getOperation()) && StringUtils.isEmpty(request.getResource()) && StringUtils.isEmpty(request.getPhaseId())) {
                batchNoHoldList = batchNoHoldRepository.findByOperationAndSiteAndActive(request.getOperation(), request.getSite(), 1, getPagable(request.getMaxRecord()));
            } else if(!StringUtils.isEmpty(request.getOperation()) && !StringUtils.isEmpty(request.getResource()) && StringUtils.isEmpty(request.getPhaseId())){
                batchNoHoldList = batchNoHoldRepository.findBySiteAndOperationAndResourceAndActive(request.getSite(), request.getOperation(), request.getResource(), 1, getPagable(request.getMaxRecord()));
            } else if(!StringUtils.isEmpty(request.getOperation()) && !StringUtils.isEmpty(request.getResource()) && !StringUtils.isEmpty(request.getPhaseId())){
                batchNoHoldList = batchNoHoldRepository.findByOperationAndResourceAndPhaseIdAndSiteAndActive(request.getOperation(), request.getResource(), request.getPhaseId(), request.getSite(), 1, getPagable(request.getMaxRecord()));
            }

            batchNoHoldList = batchNoHoldList.stream()
                    .filter(BatchNoHold::isQualityApproval)
                    .collect(Collectors.toList());

            return batchNoHoldList;
        } catch (Exception e) {
            throw new BatchNoInWorkException(173, e.getMessage());
        }
    }

    private Pageable getPagable(int maxRecords){
        if (maxRecords > 0) {
            return PageRequest.of(0, maxRecords, Sort.by(Sort.Direction.DESC, "createdDateTime"));
        } else {
            return Pageable.unpaged(); // Retrieve all records
        }
    }

}
