package com.rits.batchnoinqueue.service;

import com.rits.batchnoinqueue.dto.BatchInQueueResponse;
import com.rits.batchnoinqueue.dto.BatchNoInQueueRequest;
import com.rits.batchnoinqueue.dto.BatchNoInQueueResponse;
import com.rits.batchnoinqueue.dto.QuantityInQueueResponse;
import com.rits.batchnoinqueue.exception.BatchNoInQueueException;
import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinqueue.model.MessageDetails;
import com.rits.batchnoinqueue.model.MessageModel;

import com.rits.batchnoinwork.model.BatchNoInWork;
import com.rits.batchnoinwork.repository.BatchNoInWorkRepository;
import com.rits.batchnorecipeheaderservice.exception.BatchNoRecipeHeaderException;
import com.rits.lineclearancelogservice.service.LineClearanceLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.rits.batchnoinqueue.repository.BatchNoInQueueRepository;
import lombok.RequiredArgsConstructor;
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

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class BatchNoInQueueServiceImpl implements BatchNoInQueueService {
    private final BatchNoInQueueRepository batchNoInQueueRepository;
    private final BatchNoInWorkRepository batchNoInWorkRepository;
    private final MessageSource localMessageSource;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private final LineClearanceLogService lineClearanceLogService;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }


    @Override
    public MessageModel createBatchNoInQueue(BatchNoInQueueRequest batchNoInQueueRequest) throws  Exception {

        String handle = createHandle(batchNoInQueueRequest);

        BatchNoInQueue validateBatchNoInQueue = validateBatchNoInQueue(batchNoInQueueRequest.getSite(), handle);

        if(validateBatchNoInQueue != null)
            throw new BatchNoInQueueException(128, batchNoInQueueRequest.getBatchNo());

        BatchNoInQueue batchNoInQueue = batchNoInQueueBuilder(batchNoInQueueRequest);
        batchNoInQueue.setHandle(handle);
        batchNoInQueue.setCreatedBy(batchNoInQueueRequest.getUser());
        batchNoInQueue.setCreatedDateTime(LocalDateTime.now());

        batchNoInQueueRepository.save(batchNoInQueue);

        String createMessage = getFormattedMessage(1, batchNoInQueueRequest.getBatchNo());
        return MessageModel.builder().response(batchNoInQueue).message_details(MessageDetails.builder().msg(createMessage).msg_type("S").build()).build();
    }

    @Override
    public MessageModel updateBatchNoInQueue(BatchNoInQueueRequest batchNoInQueueRequest) throws Exception {

        String handle = createHandle(batchNoInQueueRequest);
        BatchNoInQueue batchNoInQueueValidation = validateBatchNoInQueue(batchNoInQueueRequest.getSite(), handle);

        if(batchNoInQueueValidation == null)
            throw new BatchNoInQueueException(127, batchNoInQueueRequest.getBatchNo());

        BatchNoInQueue batchNoInQueue = batchNoInQueueBuilder(batchNoInQueueRequest);
        batchNoInQueue.setHandle(handle);
        batchNoInQueue.setCreatedBy(batchNoInQueueValidation.getCreatedBy());
        batchNoInQueue.setCreatedDateTime(batchNoInQueueValidation.getCreatedDateTime());
        batchNoInQueue.setModifiedBy(batchNoInQueueRequest.getUser());
        batchNoInQueue.setModifiedDateTime(LocalDateTime.now());

        batchNoInQueueRepository.save(batchNoInQueue);
        String updateMessage = getFormattedMessage(2, batchNoInQueueRequest.getBatchNo());
        return MessageModel.builder().response(batchNoInQueue).message_details(MessageDetails.builder().msg(updateMessage).msg_type("S").build()).build();

    }

    @Override
    public MessageModel deleteBatchNoInQueue(BatchNoInQueueRequest batchNoInQueueRequest) throws Exception{

        String handle = createHandle(batchNoInQueueRequest);

        BatchNoInQueue batchNoInQueueValidation = validateBatchNoInQueue(batchNoInQueueRequest.getSite(),handle);
        if(batchNoInQueueValidation == null)
            throw new BatchNoInQueueException(127,batchNoInQueueRequest.getBatchNo());

        batchNoInQueueValidation.setActive(0);
        batchNoInQueueValidation.setModifiedDateTime(LocalDateTime.now());

        batchNoInQueueRepository.save(batchNoInQueueValidation);


        String deleteMessage = getFormattedMessage(3, batchNoInQueueRequest.getBatchNo());
        return MessageModel.builder().response(batchNoInQueueValidation).message_details(MessageDetails.builder().msg(deleteMessage).msg_type("S").build()).build();
    }


    @Override
    public BatchNoInQueue retrieve(BatchNoInQueueRequest batchNoInQueueRequest) throws Exception{

        String handle = createHandle(batchNoInQueueRequest);
        BatchNoInQueue batchNoInQueue = validateBatchNoInQueue(batchNoInQueueRequest.getSite(), handle);

        if(batchNoInQueue == null)
            throw new BatchNoInQueueException(7009);

        return batchNoInQueue;
    }


    @Override
    public List<BatchNoInQueue> retrieveAll(String site) {

        List<BatchNoInQueue> existingBatchNoInQueue = batchNoInQueueRepository.findBySiteAndActive(site, 1);
        return existingBatchNoInQueue;
    }


    @Override
    public List<BatchNoInQueue> retrieveTop50(String site) {
        List<BatchNoInQueue> retrieveTop50 = batchNoInQueueRepository.findTop50BySiteAndActive(site, 1);
        return retrieveTop50;
    }

    @Override
    public boolean isBatchNoInQueueExist(String site, String batchNo) throws Exception {
        if(!StringUtils.hasText(batchNo))
            throw new BatchNoInQueueException(123);

        boolean checkExistance = batchNoInQueueRepository.existsBySiteAndActiveAndBatchNo(site, 1,batchNo);
        return checkExistance;
    }

    private BatchNoInQueue batchNoInQueueBuilder(BatchNoInQueueRequest batchNoInQueueRequest){

        String batchNoBO = "BatchNoBO:" + batchNoInQueueRequest.getSite() + "," + batchNoInQueueRequest.getBatchNo();
        String recipeBO = "RecipeBO:" + batchNoInQueueRequest.getSite() + "," + batchNoInQueueRequest.getRecipe() + "," + batchNoInQueueRequest.getRecipeVersion();

        BatchNoInQueue batchNoInQueue = BatchNoInQueue.builder()
                .site(batchNoInQueueRequest.getSite())
                .dateTime(batchNoInQueueRequest.getDateTime())
                .batchNo(batchNoInQueueRequest.getBatchNo())
                .material(batchNoInQueueRequest.getMaterial())
                .materialVersion(batchNoInQueueRequest.getMaterialVersion())
                .recipe(batchNoInQueueRequest.getRecipe())
                .recipeVersion(batchNoInQueueRequest.getRecipeVersion())
                .batchNoRecipeHeaderBO("BatchNoRecipeHeaderBO:" + batchNoInQueueRequest.getSite() + "," + recipeBO + "," + batchNoBO)
                .batchNoHeaderBO("BatchNoHeaderBO:" + batchNoInQueueRequest.getSite() + "," + batchNoBO)
                .phaseId(batchNoInQueueRequest.getPhaseId())
                .phaseSequence(batchNoInQueueRequest.getPhaseSequence())
                .operation(batchNoInQueueRequest.getOperation())
                .opSequence(batchNoInQueueRequest.getOpSequence())
                .quantityBaseUom(batchNoInQueueRequest.getQuantityBaseUom())
                .quantityMeasuredUom(batchNoInQueueRequest.getQuantityMeasuredUom())
                .baseUom(batchNoInQueueRequest.getBaseUom())
                .measuredUom(batchNoInQueueRequest.getMeasuredUom())
                .queuedTimestamp(batchNoInQueueRequest.getQueuedTimestamp())
                .resource(batchNoInQueueRequest.getResource())
                .user(batchNoInQueueRequest.getUser())
                .qtyToComplete(batchNoInQueueRequest.getQtyToComplete())
                .qtyInQueue(batchNoInQueueRequest.getQtyInQueue())
                .orderNumber(batchNoInQueueRequest.getOrderNumber())
                .qualityApproval(batchNoInQueueRequest.isQualityApproval())
                .workcenter(batchNoInQueueRequest.getWorkcenter())
                .type("inqueue")
                .active(1)
                .build();
        return batchNoInQueue;
    }

    private String createHandle(BatchNoInQueueRequest batchNoInQueueRequest){
        validateRequest(batchNoInQueueRequest);
        String batchNoBO = "BatchNoBO:" + batchNoInQueueRequest.getSite() + "," + batchNoInQueueRequest.getBatchNo();
        String orderNumberBO = "OrderNumberBO:" + batchNoInQueueRequest.getSite() + "," + batchNoInQueueRequest.getOrderNumber();
        String recipeBO = "RecipeBO:" + batchNoInQueueRequest.getSite() + "," + batchNoInQueueRequest.getRecipe() + "," + batchNoInQueueRequest.getRecipeVersion();
        String batchNORecipeHeaderBO = "BatchNoRecipeHeaderBO:" + batchNoInQueueRequest.getSite() + "," + recipeBO + "," + batchNoBO;

        return "BatchNoInQueue:" + batchNoInQueueRequest.getSite() + "," + batchNoBO + "," + orderNumberBO + "," + batchNORecipeHeaderBO + "," + batchNoInQueueRequest.getPhaseId() + "," + batchNoInQueueRequest.getOperation();
    }
    private BatchNoInQueue validateBatchNoInQueue(String site, String handle){
        return batchNoInQueueRepository.findBySiteAndActiveAndHandle(site, 1, handle);
    }

    public boolean validateRequest(BatchNoInQueueRequest request){
        if(!StringUtils.hasText(request.getBatchNo())){
            throw new BatchNoInQueueException(7002);
        }
        if(!StringUtils.hasText(request.getOrderNumber())){
            throw new BatchNoInQueueException(7003);
        }
        if(!StringUtils.hasText(request.getRecipe())){
            throw new BatchNoInQueueException(7004);
        }
        if(!StringUtils.hasText(request.getRecipeVersion())){
            throw new BatchNoInQueueException(7010);
        }
        if(!StringUtils.hasText(request.getPhaseId())){
            throw new BatchNoInQueueException(7005);
        }
        if(!StringUtils.hasText(request.getOperation())){
            throw new BatchNoInQueueException(7006);
        }
        return true;
    }

    @Override
    public BatchNoInQueue getBySiteAndBatchNoHeaderAndPhaseAndOperation(String site, String batchNoHeaderBO, String phaseId, String operation) {

        BatchNoInQueue batchNoInQueueDetails = batchNoInQueueRepository.findBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndActive(site, batchNoHeaderBO, phaseId, operation, 1);
        return batchNoInQueueDetails;
    }

    @Override
    public void delete(BatchNoInQueue deleteBatchNoInQueue) {

        batchNoInQueueRepository.delete(deleteBatchNoInQueue);
    }

    @Override
    public BatchNoInQueueResponse getBatchNoRecords(BatchNoInQueueRequest request) throws Exception{
        // Fetch BatchNoInQueue records
        List<BatchNoInQueue> batchNoInQueueList = batchNoInQueueRepository.findBySiteAndOperationAndResourceAndActive(request.getSite(), request.getOperation(), request.getResource(),1, getPagable(request.getMaxRecord()));

        // Fetch BatchNoInWork records
        List<BatchNoInWork> batchNoInWorkList = batchNoInWorkRepository.findBySiteAndOperationAndResourceAndActive(request.getSite(), request.getOperation(), request.getResource(),1, getPagable(request.getMaxRecord()));

        // Map results into the response
        BatchNoInQueueResponse response = new BatchNoInQueueResponse();
        response.setBatchNoInQueueList(batchNoInQueueList);
        response.setBatchNoInWorkList(batchNoInWorkList);

        return response;
    }

    @Override
    public BatchNoInQueueResponse getInQueueForBatchRecipeByFilters(BatchNoInQueueRequest request) throws Exception{
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("site").is(request.getSite()));
            query.addCriteria(Criteria.where("active").is(1));

            // Filter by phaseId if provided
            if (request.getPhaseId() != null && !request.getPhaseId().isEmpty()) {
                query.addCriteria(Criteria.where("phaseId").is(request.getPhaseId()));
            }

            // Filter by operation if provided
            if (request.getOperation() != null && !request.getOperation().isEmpty()) {
                query.addCriteria(Criteria.where("operation").is(request.getOperation()));
            }

            // Query the database
            List<BatchNoInQueue> inQueueRecords = mongoTemplate.find(query, BatchNoInQueue.class);

            // Map the results to the response DTO
            List<BatchInQueueResponse> responseList = inQueueRecords.stream().map(record -> {
                BatchInQueueResponse response = new BatchInQueueResponse();
                response.setBatchNo(record.getBatchNo());
                response.setRecipe(record.getRecipe());
                response.setRecipeVersion(record.getRecipeVersion());
                response.setItem(record.getMaterial());
                response.setItemVersion(record.getMaterialVersion());
                response.setQuantity(record.getQtyInQueue());
                response.setProcessOrder(record.getOrderNumber());
                response.setStatus("In Queue"); // Default status
                return response;
            }).collect(Collectors.toList());

            return BatchNoInQueueResponse.builder().batchNoResponse(responseList).build();

        } catch (Exception e) {
            throw new BatchNoRecipeHeaderException(173, e.getMessage());
        }
    }

    @Override
    public List<BatchNoInQueue> getBatchInQueueList(BatchNoInQueueRequest request) throws Exception{
        try {
            List<BatchNoInQueue> batchNoInQueues = new ArrayList<>();
            if(StringUtils.hasText(request.getOperation()) && !StringUtils.hasText(request.getResource()) && !StringUtils.hasText(request.getPhaseId())) {
                batchNoInQueues = batchNoInQueueRepository.findByOperationAndSiteAndActive(request.getOperation(), request.getSite(), 1, getPagable(request.getMaxRecord()));
            } else if(StringUtils.hasText(request.getOperation()) && StringUtils.hasText(request.getResource()) && !StringUtils.hasText(request.getPhaseId())){
                batchNoInQueues = batchNoInQueueRepository.findBySiteAndOperationAndResourceAndActive(request.getSite(), request.getOperation(), request.getResource(), 1, getPagable(request.getMaxRecord()));
            } else if(StringUtils.hasText(request.getOperation()) && !StringUtils.hasText(request.getResource()) && StringUtils.hasText(request.getPhaseId())){
                batchNoInQueues = batchNoInQueueRepository.findByOperationAndPhaseIdAndSiteAndActive(request.getOperation(), request.getPhaseId(), request.getSite(), 1, getPagable(request.getMaxRecord()));
            }else if(StringUtils.hasText(request.getOperation()) && StringUtils.hasText(request.getResource()) && StringUtils.hasText(request.getPhaseId())){
                batchNoInQueues = batchNoInQueueRepository.findByOperationAndResourceAndPhaseIdAndSiteAndActive(request.getOperation(), request.getResource(), request.getPhaseId(), request.getSite(), 1, getPagable(request.getMaxRecord()));
            }
            batchNoInQueues = batchNoInQueues.stream()
                    .filter(BatchNoInQueue::isQualityApproval) // Assuming `isQualityApproval` is a boolean getter method
                    .collect(Collectors.toList());

            return batchNoInQueues;
        } catch (Exception e) {
            throw new BatchNoRecipeHeaderException(173, e.getMessage());
        }
    }

    @Override
    public List<BatchNoInQueue> getBatchInQueueListForWorkList(BatchNoInQueueRequest request) throws Exception{
        String resource = request.getResource();
        request.setResource(null);
        try {

            List<BatchNoInQueue> batchNoInQueues = new ArrayList<>();
            if(StringUtils.hasText(request.getOperation()) && !StringUtils.hasText(request.getResource()) && !StringUtils.hasText(request.getPhaseId())) {
                batchNoInQueues = batchNoInQueueRepository.findByOperationAndSiteAndActive(request.getOperation(), request.getSite(), 1, getPagable(request.getMaxRecord()));
            } else if(StringUtils.hasText(request.getOperation()) && StringUtils.hasText(request.getResource()) && !StringUtils.hasText(request.getPhaseId())){
                batchNoInQueues = batchNoInQueueRepository.findBySiteAndOperationAndResourceAndActive(request.getSite(), request.getOperation(), request.getResource(), 1, getPagable(request.getMaxRecord()));
            } else if(StringUtils.hasText(request.getOperation()) && !StringUtils.hasText(request.getResource()) && StringUtils.hasText(request.getPhaseId())){
                batchNoInQueues = batchNoInQueueRepository.findByOperationAndPhaseIdAndSiteAndActive(request.getOperation(), request.getPhaseId(), request.getSite(), 1, getPagable(request.getMaxRecord()));
            }else if(StringUtils.hasText(request.getOperation()) && StringUtils.hasText(request.getResource()) && StringUtils.hasText(request.getPhaseId())){
                batchNoInQueues = batchNoInQueueRepository.findByOperationAndResourceAndPhaseIdAndSiteAndActive(request.getOperation(), request.getResource(), request.getPhaseId(), request.getSite(), 1, getPagable(request.getMaxRecord()));
            }
//            batchNoInQueues = batchNoInQueues.stream()
//                    .filter(batchNoInQueue -> request.isQualityCheck()
//                            ? !batchNoInQueue.isQualityApproval() // If qualityCheck is true, filter records with qualityApproval = false
//                            : batchNoInQueue.isQualityApproval()) // If qualityCheck is false, filter records with qualityApproval = true
//                    .collect(Collectors.toList());

//            batchNoInQueues = batchNoInQueues.stream()
//                    .filter(batchNoInQueue -> {
//                        if (request.isQualityCheck() && !request.isOperatorCheck()) {
//                            return !batchNoInQueue.isQualityApproval(); // If qualityCheck is true, filter records with qualityApproval = false
//                        }
//                        return true;
//                    })
//                    .collect(Collectors.toList());

            batchNoInQueues = batchNoInQueues.stream()
                    .filter(batchNoInQueue -> {
                        if (request.isQualityCheck() && !request.isOperatorCheck()) { //if quality=true,operator=false show line cleared and quality not approved
                            return !batchNoInQueue.isQualityApproval() && lineClearanceLogService.checkLineClearance(batchNoInQueue.getSite(), batchNoInQueue.getBatchNo(), resource, batchNoInQueue.getOperation(), batchNoInQueue.getPhaseId());
                        } else if(!request.isQualityCheck() && request.isOperatorCheck()){ //if quality=false,operator=true dont show line cleared and quality not approved , show remaining
                            return !(!batchNoInQueue.isQualityApproval() && lineClearanceLogService.checkLineClearance(batchNoInQueue.getSite(), batchNoInQueue.getBatchNo(), resource, batchNoInQueue.getOperation(), batchNoInQueue.getPhaseId()));
                        } else{ ////if quality=false,operator=false show line not cleared and quality not approved
                            return !batchNoInQueue.isQualityApproval() && !lineClearanceLogService.checkLineClearance(batchNoInQueue.getSite(), batchNoInQueue.getBatchNo(), resource, batchNoInQueue.getOperation(), batchNoInQueue.getPhaseId());
                        }
                    })
                    .collect(Collectors.toList());

            return batchNoInQueues;
        } catch (Exception e) {
            throw new BatchNoInQueueException(173, e.getMessage());
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
    public boolean updateQualityApproval(String site, String operation, String batchNo) {
        if(batchNoInQueueRepository.existsBySiteAndOperationAndBatchNoAndActive(site, operation, batchNo, 1)){
            BatchNoInQueue existingBatchNoInQueue = batchNoInQueueRepository.findBySiteAndOperationAndBatchNoAndActive(site, operation, batchNo, 1);

            existingBatchNoInQueue.setQualityApproval(true);
            batchNoInQueueRepository.save(existingBatchNoInQueue);
            return true;
        }
        return false;
    }


//    @Override
//    public List<BatchNoInQueue> getBatchInQueueList(BatchNoInQueueRequest request) throws Exception {
//        try {
//            // Sorting and pagination setup
//            Sort sort = Sort.by(Sort.Direction.DESC, "createdDateTime");
//            Pageable pageable = PageRequest.of(0, 1000, sort);
//
//            List<BatchNoInQueue> batchNoInQueues;
//
//            if (request.getOperation() != null && request.getResource() == null && request.getPhaseId() == null) {
//                // Use paginated and sorted method
//                batchNoInQueues = batchNoInQueueRepository.findByOperationAndSiteAndActiveOrderByCreatedDateTimeDesc(
//                        request.getOperation(), request.getSite(), 1, pageable);
//            } else if (request.getOperation() != null && request.getResource() != null && request.getPhaseId() == null) {
//                // Use paginated and sorted method
//                batchNoInQueues = batchNoInQueueRepository.findBySiteAndOperationAndResourceAndActiveOrderByCreatedDateTimeDesc(
//                        request.getSite(), request.getOperation(), request.getResource(), 1, pageable);
//            } else if (request.getOperation() != null && request.getResource() != null && request.getPhaseId() != null) {
//                // Use paginated and sorted method
//                batchNoInQueues = batchNoInQueueRepository.findByOperationAndResourceAndPhaseIdAndSiteAndActiveOrderByCreatedDateTimeDesc(
//                        request.getOperation(), request.getResource(), request.getPhaseId(),
//                        request.getSite(), 1, pageable);
//            } else {
//                batchNoInQueues = List.of(); // Return empty list for invalid cases
//            }
//
//            return batchNoInQueues;
//        } catch (Exception e) {
//            throw new Exception("Error retrieving BatchNoInQueue records: " + e.getMessage(), e);
//        }
//    }

    @Override
    public QuantityInQueueResponse getBatchNoInQueueByPhaseAndOperation(BatchNoInQueueRequest request) {
        QuantityInQueueResponse batchNoInQueue = null;

        // If both batchNo and orderNumber are provided and are not blank
        if ((request.getBatchNo() != null && !request.getBatchNo().isEmpty()) &&
                (request.getOrderNumber() != null && !request.getOrderNumber().isEmpty())) {
            batchNoInQueue = batchNoInQueueRepository.findBySiteAndBatchNoAndOrderNumberAndPhaseIdAndOperation(
                    request.getSite(), request.getBatchNo(), request.getOrderNumber(), request.getPhaseId(), request.getOperation());
        }
        // If only batchNo is provided and is not blank
        else if (request.getBatchNo() != null && !request.getBatchNo().isEmpty()) {
            batchNoInQueue = batchNoInQueueRepository.findBySiteAndBatchNoAndPhaseIdAndOperation(
                    request.getSite(), request.getBatchNo(), request.getPhaseId(), request.getOperation());
        }
        // If only orderNumber is provided and is not blank
        else if (request.getOrderNumber() != null && !request.getOrderNumber().isEmpty()) {
            batchNoInQueue = batchNoInQueueRepository.findBySiteAndOrderNumberAndPhaseIdAndOperation(
                    request.getSite(), request.getOrderNumber(), request.getPhaseId(), request.getOperation());
        }
        return batchNoInQueue;

    }
}
