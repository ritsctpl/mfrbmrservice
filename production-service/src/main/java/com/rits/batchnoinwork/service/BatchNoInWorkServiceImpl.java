package com.rits.batchnoinwork.service;


import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinwork.dto.BatchNoWorkQtyResponse;
import com.rits.batchnoinwork.dto.InWorkResponse;
import com.rits.batchnoinwork.exception.BatchNoInWorkException;
import com.rits.batchnoinwork.model.MessageDetails;
import com.rits.batchnoinwork.model.MessageModel;
import com.rits.batchnoinwork.dto.BatchNoInWorkRequest;
import com.rits.batchnoinwork.model.BatchNoInWork;
import com.rits.batchnoinwork.repository.BatchNoInWorkRepository;
import com.rits.batchnorecipeheaderservice.exception.BatchNoRecipeHeaderException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchNoInWorkServiceImpl implements BatchNoInWorkService{
    private final BatchNoInWorkRepository batchNoInWorkRepository;
    private final MessageSource localMessageSource;

    @Autowired
    private MongoTemplate mongoTemplate;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public MessageModel createBatchNoInWork(BatchNoInWorkRequest batchNoInWorkRequest) throws  Exception {

        String handle = createHandle(batchNoInWorkRequest);

        BatchNoInWork validateBatchNoInWork = validateBatchNoInWork(batchNoInWorkRequest.getSite(), handle);
        if(validateBatchNoInWork != null)
            throw new BatchNoInWorkException(129, batchNoInWorkRequest.getBatchNo());

        BatchNoInWork batchNoInWork = batchNoInWorkBuilder(batchNoInWorkRequest);
        batchNoInWork.setHandle(handle);
        batchNoInWork.setCreatedBy(batchNoInWorkRequest.getUser());
        batchNoInWork.setCreatedDateTime(LocalDateTime.now());

        batchNoInWorkRepository.save(batchNoInWork);
        String createMessage = getFormattedMessage(1, batchNoInWorkRequest.getBatchNo());
        return MessageModel.builder().response(batchNoInWork).message_details(MessageDetails.builder().msg(createMessage).msg_type("S").build()).build();
    }


    private BatchNoInWork batchNoInWorkBuilder(BatchNoInWorkRequest batchNoInWorkRequest){
        String batchNoBO = "BatchNoBO:" + batchNoInWorkRequest.getSite() + "," + batchNoInWorkRequest.getBatchNo();
        String recipeBO = "RecipeBO:" + batchNoInWorkRequest.getSite() + "," + batchNoInWorkRequest.getRecipe() + "," + batchNoInWorkRequest.getRecipeVersion();

        BatchNoInWork batchNoInWork = BatchNoInWork.builder()
                .site(batchNoInWorkRequest.getSite())
                .dateTime(batchNoInWorkRequest.getDateTime())
                .batchNo(batchNoInWorkRequest.getBatchNo())
                .material(batchNoInWorkRequest.getMaterial())
                .materialVersion(batchNoInWorkRequest.getMaterialVersion())
                .recipe(batchNoInWorkRequest.getRecipe())
                .recipeVersion(batchNoInWorkRequest.getRecipeVersion())
                .batchNoRecipeHeaderBO("BatchNoRecipeHeaderBO:" + batchNoInWorkRequest.getSite() + "," + recipeBO + "," + batchNoBO)
                .batchNoHeaderBO("BatchNoHeaderBO:" + batchNoInWorkRequest.getSite() + "," + batchNoBO)
                .phaseId(batchNoInWorkRequest.getPhaseId())
                .operation(batchNoInWorkRequest.getOperation())
                .quantityBaseUom(batchNoInWorkRequest.getQuantityBaseUom())
                .quantityMeasuredUom(batchNoInWorkRequest.getQuantityMeasuredUom())
                .baseUom(batchNoInWorkRequest.getBaseUom())
                .measuredUom(batchNoInWorkRequest.getMeasuredUom())
                .queuedTimestamp(batchNoInWorkRequest.getQueuedTimestamp())
                .resource(batchNoInWorkRequest.getResource())
                .user(batchNoInWorkRequest.getUser())
                .qtyToComplete(batchNoInWorkRequest.getQtyToComplete())
                .qtyInQueue(batchNoInWorkRequest.getQtyInQueue())
                .orderNumber(batchNoInWorkRequest.getOrderNumber())
                .qualityApproval(batchNoInWorkRequest.isQualityApproval())
                .workcenter(batchNoInWorkRequest.getWorkcenter())
                .type("inwork")
                .active(1)
                .build();
        return batchNoInWork;
    }


    @Override
    public MessageModel updateBatchNoInWork(BatchNoInWorkRequest batchNoInWorkRequest) throws Exception {

        String handle = createHandle(batchNoInWorkRequest);
        BatchNoInWork batchNoInWorkValidation = validateBatchNoInWork(batchNoInWorkRequest.getSite(), handle);
        if(batchNoInWorkValidation == null)
            throw new BatchNoInWorkException(130, batchNoInWorkRequest.getBatchNo());


        BatchNoInWork batchNoInWork = batchNoInWorkBuilder(batchNoInWorkRequest);
        batchNoInWork.setHandle(handle);
        batchNoInWork.setCreatedBy(batchNoInWorkValidation.getCreatedBy());
        batchNoInWork.setCreatedDateTime(batchNoInWorkValidation.getCreatedDateTime());
        batchNoInWork.setModifiedBy(batchNoInWorkRequest.getUser());
        batchNoInWork.setModifiedDateTime(LocalDateTime.now());

        batchNoInWorkRepository.save(batchNoInWork);
        String updateMessage = getFormattedMessage(2, batchNoInWorkRequest.getBatchNo());
        return MessageModel.builder().response(batchNoInWork).message_details(MessageDetails.builder().msg(updateMessage).msg_type("S").build()).build();

    }
    @Override
    public MessageModel deleteBatchNoInWork(BatchNoInWorkRequest batchNoInWorkRequest) throws Exception{

        String handle = createHandle(batchNoInWorkRequest);

        BatchNoInWork batchNoInWorkValidation = validateBatchNoInWork(batchNoInWorkRequest.getSite(),handle);
        if(batchNoInWorkValidation == null)
            throw new BatchNoInWorkException(130,batchNoInWorkRequest.getBatchNo());

        batchNoInWorkValidation.setActive(0);
        batchNoInWorkValidation.setModifiedDateTime(LocalDateTime.now());

        batchNoInWorkRepository.save(batchNoInWorkValidation);


        String deleteMessage = getFormattedMessage(3, batchNoInWorkRequest.getBatchNo());
        return MessageModel.builder().response(batchNoInWorkValidation).message_details(MessageDetails.builder().msg(deleteMessage).msg_type("S").build()).build();
    }

    @Override
    public MessageModel unDeleteBatchNoInWork(BatchNoInWorkRequest batchNoInWorkRequest) throws Exception{

        String handle = createHandle(batchNoInWorkRequest);

        BatchNoInWork batchNoInWorkValidation = batchNoInWorkRepository.findBySiteAndActiveAndHandle(batchNoInWorkRequest.getSite(), 0, handle);
        if(batchNoInWorkValidation == null)
            throw new BatchNoInWorkException(130,batchNoInWorkRequest.getBatchNo());

        batchNoInWorkValidation.setActive(1);
        batchNoInWorkValidation.setModifiedDateTime(LocalDateTime.now());

        batchNoInWorkRepository.save(batchNoInWorkValidation);


        String deleteMessage = getFormattedMessage(3, batchNoInWorkRequest.getBatchNo());
        return MessageModel.builder().response(batchNoInWorkValidation).message_details(MessageDetails.builder().msg(deleteMessage).msg_type("S").build()).build();
    }

    @Override
    public BatchNoInWork retrieve(BatchNoInWorkRequest batchNoInWorkRequest) throws Exception{

        String handle = createHandle(batchNoInWorkRequest);

        BatchNoInWork batchNoInWork = validateBatchNoInWork(batchNoInWorkRequest.getSite(), handle);
        if(batchNoInWork == null)
            throw new BatchNoInWorkException(7009);

        return batchNoInWork;
    }



    @Override
    public List<BatchNoInWork> retrieveAll(String site) throws Exception{

        List<BatchNoInWork> batchNoInWorks = batchNoInWorkRepository.findBySiteAndActive(site, 1);

        return batchNoInWorks;
    }

    @Override
    public List<BatchNoInWork> retrieveTop50(String site) throws Exception{
        List<BatchNoInWork> retrieveTop50 = batchNoInWorkRepository.findTop50BySiteAndActive(site, 1);
        return retrieveTop50;
    }

    @Override
    public boolean isBatchNoInWorkExist(String site, String batchNo) throws Exception{
        if(!StringUtils.hasText(batchNo))
            throw new BatchNoInWorkException(123);

        boolean checkExistance = batchNoInWorkRepository.existsBySiteAndActiveAndBatchNo(site, 1, batchNo);
        return checkExistance;
    }

    private String createHandle(BatchNoInWorkRequest batchNoInWorkRequest){
        validateRequest(batchNoInWorkRequest);
        String batchNoBO = "BatchNoBO:" + batchNoInWorkRequest.getSite() + "," + batchNoInWorkRequest.getBatchNo();
        String orderNumberBO = "OrderNumberBO:" + batchNoInWorkRequest.getSite() + "," + batchNoInWorkRequest.getOrderNumber();
        String recipeBO = "RecipeBO:" + batchNoInWorkRequest.getSite() + "," + batchNoInWorkRequest.getRecipe() + "," + batchNoInWorkRequest.getRecipeVersion();
        String batchNORecipeHeaderBO = "BatchNoRecipeHeaderBO:" + batchNoInWorkRequest.getSite() + "," + recipeBO + "," + batchNoBO;

        return "BatchNoInWork:" + batchNoInWorkRequest.getSite() + "," + batchNoBO + "," + orderNumberBO + "," + batchNORecipeHeaderBO + "," + batchNoInWorkRequest.getPhaseId() + "," + batchNoInWorkRequest.getOperation();
    }
    private BatchNoInWork validateBatchNoInWork(String site, String handle){
        return batchNoInWorkRepository.findBySiteAndActiveAndHandle(site, 1, handle);
    }
    public boolean validateRequest(BatchNoInWorkRequest request){
        if(!StringUtils.hasText(request.getBatchNo())){
            throw new BatchNoInWorkException(7002);
        }
        if(!StringUtils.hasText(request.getOrderNumber())){
            throw new BatchNoInWorkException(7003);
        }
        if(!StringUtils.hasText(request.getRecipe())){
            throw new BatchNoInWorkException(7004);
        }
        if(!StringUtils.hasText(request.getRecipeVersion())){
            throw new BatchNoInWorkException(7010);
        }
        if(!StringUtils.hasText(request.getPhaseId())){
            throw new BatchNoInWorkException(7005);
        }
        if(!StringUtils.hasText(request.getOperation())){
            throw new BatchNoInWorkException(7006);
        }
        return true;
    }

    @Override
    public BatchNoInWork getBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUser(String site, String batchNo, String phaseId, String operation, String resource, String user, String orderNo) {

        BatchNoInWork batchNoInWork;
        if(user == null || user.isEmpty()){
            batchNoInWork = batchNoInWorkRepository.findByActiveAndSiteAndBatchNoAndPhaseIdAndOperationAndResourceAndOrderNumber(
                    1, site, batchNo, phaseId, operation, resource, orderNo);
        }else {
            batchNoInWork = batchNoInWorkRepository.findByActiveAndSiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUserAndOrderNumber(
                    1, site, batchNo, phaseId, operation, resource, user, orderNo);
        }
        return batchNoInWork;
    }

    @Override
    public BatchNoInWork getBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUser(String site, String batchNoHeaderBO, String phaseId, String operation, String resource, String user) {

        BatchNoInWork batchNoInWork;
        if(user == null || user.isEmpty()){
            batchNoInWork = batchNoInWorkRepository.findBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndActive(
                    site, batchNoHeaderBO, phaseId, operation, resource, 1);
        }else {
            batchNoInWork = batchNoInWorkRepository.findBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUserAndActive(
                    site, batchNoHeaderBO, phaseId, operation, resource, user, 1);
        }
        return batchNoInWork;
    }

    @Override
    public List<InWorkResponse> getRecordUsingFilters(BatchNoInWorkRequest request) throws Exception {

        Query query = new Query();

        // Mandatory filters
        query.addCriteria(Criteria.where("site").is(request.getSite()));
        query.addCriteria(Criteria.where("active").is(1));

        // Optional filters
        if (request.getOperation() != null && !request.getOperation().isEmpty()) {
            query.addCriteria(Criteria.where("operation").is(request.getOperation()));
        }
        if (request.getPhaseId() != null && !request.getPhaseId().isEmpty()) {
            query.addCriteria(Criteria.where("phaseId").is(request.getPhaseId()));
        }
        if (request.getResource() != null && !request.getResource().isEmpty()) {
            query.addCriteria(Criteria.where("resource").is(request.getResource()));
        }

        // Execute the query
        List<BatchNoInWork> batchNoInWorks = mongoTemplate.find(query, BatchNoInWork.class);

        List<InWorkResponse> responses = batchNoInWorks.stream().map(batch -> {
            InWorkResponse response = new InWorkResponse();
            response.setBatchNo(batch.getBatchNo());
            response.setItem(batch.getMaterial());
            response.setItemVersion(batch.getMaterialVersion());
            response.setRecipe(batch.getRecipe());
            response.setRecipeVersion(batch.getRecipeVersion());
            response.setQuantity(batch.getQtyToComplete());
            response.setProcessOrder(batch.getOrderNumber());
            response.setStatus("Active");
            return response;
        }).collect(Collectors.toList());

        return responses;
    }

    @Override
    public List<BatchNoInWork> getBatchInWorkList(BatchNoInWorkRequest request) throws Exception {

        try {
            List<BatchNoInWork> batchNoInWorks = new ArrayList<>();
            if(StringUtils.hasText(request.getOperation()) && !StringUtils.hasText(request.getResource()) && !StringUtils.hasText(request.getPhaseId())) {
                batchNoInWorks = batchNoInWorkRepository.findByOperationAndSiteAndActive(request.getOperation(), request.getSite(), 1, getPagable(request.getMaxRecord()));
            } else if(StringUtils.hasText(request.getOperation()) && StringUtils.hasText(request.getResource()) && !StringUtils.hasText(request.getPhaseId())){
                batchNoInWorks = batchNoInWorkRepository.findBySiteAndOperationAndResourceAndActive(request.getSite(), request.getOperation(), request.getResource(), 1, getPagable(request.getMaxRecord()));
            } else if(StringUtils.hasText(request.getOperation()) && StringUtils.hasText(request.getResource()) && StringUtils.hasText(request.getPhaseId())){
                batchNoInWorks = batchNoInWorkRepository.findByOperationAndResourceAndPhaseIdAndSiteAndActive(request.getOperation(), request.getResource(), request.getPhaseId(), request.getSite(), 1, getPagable(request.getMaxRecord()));
            }

            batchNoInWorks = batchNoInWorks.stream()
                    .filter(BatchNoInWork::isQualityApproval)
                    .collect(Collectors.toList());

            return batchNoInWorks;
        } catch (Exception e) {
            throw new BatchNoRecipeHeaderException(173, e.getMessage());
        }
    }

    private Pageable getPagable(int maxRecords){
        if (maxRecords > 0) {
            return PageRequest.of(0, maxRecords, Sort.by(Sort.Direction.DESC, "createdDateTime"));
        } else {
            return Pageable.unpaged(); // Retrieve all records
        }
    }

    @Override
    public void delete(BatchNoInWork deleteBatchNoInWork) {

        batchNoInWorkRepository.delete(deleteBatchNoInWork);
    }

    @Override
    public boolean updateQualityApproval(String site, String operation, String batchNo) {
        if(batchNoInWorkRepository.existsBySiteAndOperationAndBatchNoAndActive(site, operation, batchNo, 1)){
            BatchNoInWork existingBatchNoInWork = batchNoInWorkRepository.findBySiteAndOperationAndBatchNoAndActive(site, operation, batchNo, 1);

            existingBatchNoInWork.setQualityApproval(true);
            batchNoInWorkRepository.save(existingBatchNoInWork);
            return true;
        }
        return false;
    }
    @Override
    public BatchNoWorkQtyResponse getBatchNoInWorkByPhaseAndOperation(BatchNoInWorkRequest request) {
        BatchNoWorkQtyResponse batchNoInWork = null;

        // If both batchNo and orderNumber are provided and are not blank
        if ((request.getBatchNo() != null && !request.getBatchNo().isEmpty()) &&
                (request.getOrderNumber() != null && !request.getOrderNumber().isEmpty())) {
            batchNoInWork = batchNoInWorkRepository.findBySiteAndBatchNoAndOrderNumberAndPhaseIdAndOperation(
                    request.getSite(), request.getBatchNo(), request.getOrderNumber(), request.getPhaseId(), request.getOperation());
        }
        // If only batchNo is provided and is not blank
        else if (request.getBatchNo() != null && !request.getBatchNo().isEmpty()) {
            batchNoInWork = batchNoInWorkRepository.findBySiteAndBatchNoAndPhaseIdAndOperation(
                    request.getSite(), request.getBatchNo(), request.getPhaseId(), request.getOperation());
        }
        // If only orderNumber is provided and is not blank
        else if (request.getOrderNumber() != null && !request.getOrderNumber().isEmpty()) {
            batchNoInWork = batchNoInWorkRepository.findBySiteAndOrderNumberAndPhaseIdAndOperation(
                    request.getSite(), request.getOrderNumber(), request.getPhaseId(), request.getOperation());
        }
        return batchNoInWork;
    }
    public boolean existsBySiteAndActiveAndResource(String site,int active, String resource) {
        return batchNoInWorkRepository.existsBySiteAndActiveAndResource(site, active, resource);
    }
}
