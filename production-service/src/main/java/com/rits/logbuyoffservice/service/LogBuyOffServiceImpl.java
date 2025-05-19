package com.rits.logbuyoffservice.service;

import com.netflix.discovery.converters.Auto;
import com.rits.Utility.BOConverter;
import com.rits.batchnoinqueue.dto.BatchNoInQueueRequest;
import com.rits.batchnoinqueue.dto.BatchNoInQueueResponse;
import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinqueue.service.BatchNoInQueueService;
import com.rits.batchnoinwork.dto.BatchNoInWorkRequest;
import com.rits.batchnoinwork.service.BatchNoInWorkService;
import com.rits.dccollect.dto.MessageDetails;
import com.rits.hookservice.annotation.Hookable;
import com.rits.lineclearancelogservice.dto.LineClearanceLogRequest;
import com.rits.lineclearancelogservice.exception.LineClearanceLogException;
import com.rits.lineclearancelogservice.model.LineClearanceLogResponse;
import com.rits.lineclearancelogservice.service.LineClearanceLogServiceImpl;
import com.rits.lineclearanceservice.dto.RetrieveLineClearanceLogRequest;
import com.rits.lineclearanceservice.model.RetrieveLineClearanceLogResponse;
import com.rits.lineclearanceservice.service.LineClearanceService;
import com.rits.logbuyoffservice.dto.AttachmentDetailsRequest;
import com.rits.logbuyoffservice.dto.BuyOff;
import com.rits.logbuyoffservice.dto.LogbuyOffRequest;
import com.rits.logbuyoffservice.dto.LogbuyOffRequestList;
import com.rits.logbuyoffservice.exception.LogBuyOffException;
import com.rits.logbuyoffservice.model.BuyoffLog;
import com.rits.logbuyoffservice.model.LogBuyOffMessageModel;
import com.rits.logbuyoffservice.repository.LogBuyOffRepository;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import com.rits.productionlogservice.producer.ProductionLogProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


import java.util.*;

@Service
@RequiredArgsConstructor
public class LogBuyOffServiceImpl implements LogBuyOffService {
    private final LogBuyOffRepository  logBuyOffRepository;
    private final MessageSource localMessageSource;
    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher eventPublisher;
    @Autowired
    private LineClearanceLogServiceImpl lineClearanceLogService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Value("${buyOff-service.url}/retrieveByAttachmentDetails")
    private String getBuyOffList;
    @Value("${buyOff-service.url}/isSkipAllowed")
    private String isSkipAllowedUrl;
    @Value("${buyOff-service.url}/isPartialAllowed")
    private String isPartialAllowedUrl;
    @Value("${buyOff-service.url}/isRejectAllowed")
    private String isRejectAllowedUrl;

    @Autowired
    private BatchNoInQueueService batchNoInQueueService;
    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

//    @Hookable
    @Override
    public LogBuyOffMessageModel accept(LogbuyOffRequestList logbuyOffRequestList) throws Exception {
        StringBuilder approvalMessage = new StringBuilder();

        List<LogbuyOffRequest> logbuyOffRequests = logbuyOffRequestList.getLogbuyOffRequestList(); // Get batch details

        if (logbuyOffRequests == null || logbuyOffRequests.isEmpty()) {
            throw new LogBuyOffException(353);
        }

        for (LogbuyOffRequest logbuyOffRequest : logbuyOffRequests) {
            String batchNo = logbuyOffRequest.getBatchNo(); // Extract batchNo

            if (!lineClearanceLogService.checkLineClearance(logbuyOffRequest.getSite(), batchNo, logbuyOffRequest.getResourceId(), logbuyOffRequest.getOperation(), logbuyOffRequest.getPhaseId())) {
                throw new LogBuyOffException(352, batchNo);
            }

            // **Step 1: Check and update existing rejected records**
            List<BuyoffLog> rejectedRecords = logBuyOffRepository.findBySiteAndBatchNoAndBuyOffActionAndActive(logbuyOffRequest.getSite(), batchNo, "R", 1);
            if (!rejectedRecords.isEmpty()) {
                for (BuyoffLog rejectedRecord : rejectedRecords) {
                    rejectedRecord.setActive(0);
                    logBuyOffRepository.save(rejectedRecord); // Update in the database
                }
            }

            int uniqueId = logbuyOffRequest.getUniqueId();

            BuyoffLog buyoffLog = BuyoffLog.builder()
                    .site(Optional.ofNullable(logbuyOffRequest.getSite()).orElse(""))
                    .handle("LogBuyOffBO:" +
                            Optional.ofNullable(logbuyOffRequest.getSite()).orElse("") + "," +
                            Optional.ofNullable(logbuyOffRequest.getBuyOffBO()).orElse("") + "," +
                            uniqueId)
                    .buyOffBO(Optional.ofNullable(logbuyOffRequest.getBuyOffBO()).orElse(""))
                    .buyOffAction("A")
                    .pcu(Optional.ofNullable(logbuyOffRequest.getPcu()).orElse(""))
                    .buyOffLogId(uniqueId)
                    .comments(Optional.ofNullable(logbuyOffRequest.getComments()).orElse(""))
                    .state("Closed")
                    .quantity(Optional.ofNullable(logbuyOffRequest.getQuantity()).orElse("")) // Get from batch detail
                    .description(Optional.ofNullable(logbuyOffRequest.getDescription()).orElse(""))
                    .operation(Optional.ofNullable(logbuyOffRequest.getOperation()).orElse(""))
                    .operationVersion(Optional.ofNullable(logbuyOffRequest.getOperationVersion()).orElse("#"))
                    .userId(Optional.ofNullable(logbuyOffRequest.getUserId()).orElse(""))
                    .item(Optional.ofNullable(logbuyOffRequest.getItem()).orElse(""))
                    .itemVersion(Optional.ofNullable(logbuyOffRequest.getItemVersion()).orElse(""))
                    .router(Optional.ofNullable(logbuyOffRequest.getRouter()).orElse(logbuyOffRequest.getRecipe()))
                    .routerVersion(Optional.ofNullable(logbuyOffRequest.getRouterVersion()).orElse(logbuyOffRequest.getRecipeVersion()))
                    .stepId(Optional.ofNullable(logbuyOffRequest.getStepId()).orElse(""))
                    .shopOrder(Optional.ofNullable(logbuyOffRequest.getShopOrder()).orElse(logbuyOffRequest.getOrderNumber()))
                    .customerOrderBO(Optional.ofNullable(logbuyOffRequest.getCustomerOrderBO()).orElse(""))
                    .processLotBO(Optional.ofNullable(logbuyOffRequest.getProcessLotBO()).orElse(""))
                    .resourceId(Optional.ofNullable(logbuyOffRequest.getResourceId()).orElse(""))
                    .dateTime(LocalDateTime.now())
                    .batchNo(batchNo) // Set the current batch number
                    .orderNumber(Optional.ofNullable(logbuyOffRequest.getOrderNumber()).orElse(""))
                    .recipe(Optional.ofNullable(logbuyOffRequest.getRecipe()).orElse(""))
                    .recipeVersion(Optional.ofNullable(logbuyOffRequest.getRecipeVersion()).orElse(""))
                    .active(1)
                    .build();

            logBuyOffRepository.save(buyoffLog);

            batchNoInQueueService.updateQualityApproval(logbuyOffRequest.getSite(), logbuyOffRequest.getOperation(), batchNo);

            // Append batch number to approval message
            approvalMessage.append(batchNo).append(", ");

            // Create and handle ProductionLogRequest inside the loop
            String approvedMessage = getFormattedMessage(20, approvalMessage.toString().trim()); // Ensure message is formatted
            ProductionLogRequest productionLogRequest = productionLog(buyoffLog, approvedMessage);
        }

        if (approvalMessage.length() > 0) {
            approvalMessage.setLength(approvalMessage.length() - 2); // Remove trailing comma and space
        }

        String finalApprovedMessage = getFormattedMessage(20, approvalMessage.toString()); // Ensure final message is formatted

        return LogBuyOffMessageModel.builder().messageDetails(new MessageDetails(finalApprovedMessage, "S")).build();
    }

    private ProductionLogRequest productionLog(BuyoffLog buyoffLog, String message) {

        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType(buyoffLog.getBuyOffAction())
                .userId(buyoffLog.getUserId())
                .pcu(buyoffLog.getPcu())
                .batchNo(buyoffLog.getBatchNo())
                .shopOrderBO(buyoffLog.getShopOrder())
                .operation_bo((buyoffLog.getSite() != null && buyoffLog.getOperation() != null) ? BOConverter.retrieveOperationBO(buyoffLog.getSite(), buyoffLog.getOperation(), "#") : null)
                .routerBO((buyoffLog.getSite() != null && buyoffLog.getRouter() != null && buyoffLog.getRouterVersion() != null) ? BOConverter.retrieveRouterBO(buyoffLog.getSite(), buyoffLog.getRouter(), buyoffLog.getRouterVersion()) : null)
                .itemBO((buyoffLog.getSite() != null && buyoffLog.getItem() != null && buyoffLog.getItemVersion() != null) ? BOConverter.retrieveItemBO(buyoffLog.getSite(), buyoffLog.getItem(), buyoffLog.getItemVersion()) : null)
                .resourceId(buyoffLog.getResourceId())
                .qty((buyoffLog.getQuantity() != null) ? Double.valueOf(buyoffLog.getQuantity()).intValue() : 0)
                .orderNumber(buyoffLog.getOrderNumber())
                .site(buyoffLog.getSite())
                .topic("production-log")
                .status("Active")
                .eventData(message)
                .build();
        eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));
        return productionLogRequest;
    }

    @Override
    public LogBuyOffMessageModel reject(LogbuyOffRequestList logbuyOffRequestList) throws Exception {
        StringBuilder rejectionMessage = new StringBuilder();

        List<LogbuyOffRequest> logbuyOffRequests = logbuyOffRequestList.getLogbuyOffRequestList(); // Get batch details

        if (logbuyOffRequests == null || logbuyOffRequests.isEmpty()) {
            throw new LogBuyOffException(353);
        }

        for (LogbuyOffRequest logbuyOffRequest : logbuyOffRequests) {
            String batchNo = logbuyOffRequest.getBatchNo(); // Extract batchNo

            if (!lineClearanceLogService.checkLineClearance(logbuyOffRequest.getSite(), batchNo, logbuyOffRequest.getResourceId(), logbuyOffRequest.getOperation(), logbuyOffRequest.getPhaseId())) {
                throw new LogBuyOffException(352, batchNo);
            }

            lineClearanceLogService.changeLineCleranceStatusToNew(logbuyOffRequest.getSite(), batchNo, logbuyOffRequest.getResourceId());

            // Check if a rejection entry already exists for this batch
            boolean alreadyRejected = logBuyOffRepository.existsBySiteAndBatchNoAndBuyOffActionAndActive(logbuyOffRequest.getSite(), batchNo, "R", 1);
            if (alreadyRejected) {
                continue; // Skip this batch and proceed to the next one
            }

            if (checkBuyOffRejectionStatus(logbuyOffRequest)) {
                throw new LogBuyOffException(350, batchNo);
            }

            isRejectAllowed(logbuyOffRequest);
            int uniqueId = logbuyOffRequest.getUniqueId();

            BuyoffLog buyoffLog = BuyoffLog.builder()
                    .site(Optional.ofNullable(logbuyOffRequest.getSite()).orElse(""))
                    .handle("LogBuyOffBO:" +
                            Optional.ofNullable(logbuyOffRequest.getSite()).orElse("") + "," +
                            Optional.ofNullable(logbuyOffRequest.getBuyOffBO()).orElse("") + "," +
                            uniqueId)
                    .buyOffBO(Optional.ofNullable(logbuyOffRequest.getBuyOffBO()).orElse(""))
                    .buyOffAction("R")
                    .pcu(Optional.ofNullable(logbuyOffRequest.getPcu()).orElse(""))
                    .buyOffLogId(uniqueId)
                    .comments(Optional.ofNullable(logbuyOffRequest.getComments()).orElse(""))
                    .state("Open")
                    .quantity(Optional.ofNullable(logbuyOffRequest.getQuantity()).orElse(""))
                    .qtyToComplete(Optional.ofNullable(logbuyOffRequest.getQtyToComplete()).orElse(""))
                    .description(Optional.ofNullable(logbuyOffRequest.getDescription()).orElse(""))
                    .operation(Optional.ofNullable(logbuyOffRequest.getOperation()).orElse(""))
                    .operationVersion(Optional.ofNullable(logbuyOffRequest.getOperationVersion()).orElse("#"))
                    .userId(Optional.ofNullable(logbuyOffRequest.getUserId()).orElse(""))
                    .item(Optional.ofNullable(logbuyOffRequest.getItem()).orElse(""))
                    .itemVersion(Optional.ofNullable(logbuyOffRequest.getItemVersion()).orElse(""))
                    .router(Optional.ofNullable(logbuyOffRequest.getRouter()).orElse(logbuyOffRequest.getRecipe()))
                    .routerVersion(Optional.ofNullable(logbuyOffRequest.getRouterVersion()).orElse(logbuyOffRequest.getRecipeVersion()))
                    .stepId(Optional.ofNullable(logbuyOffRequest.getStepId()).orElse(""))
                    .shopOrder(Optional.ofNullable(logbuyOffRequest.getShopOrder()).orElse(logbuyOffRequest.getOrderNumber()))
                    .customerOrderBO(Optional.ofNullable(logbuyOffRequest.getCustomerOrderBO()).orElse(""))
                    .processLotBO(Optional.ofNullable(logbuyOffRequest.getProcessLotBO()).orElse(""))
                    .resourceId(Optional.ofNullable(logbuyOffRequest.getResourceId()).orElse(""))
                    .dateTime(LocalDateTime.now())
                    .batchNo(batchNo) // Set the current batch number
                    .orderNumber(Optional.ofNullable(logbuyOffRequest.getOrderNumber()).orElse(""))
                    .recipe(Optional.ofNullable(logbuyOffRequest.getRecipe()).orElse(""))
                    .recipeVersion(Optional.ofNullable(logbuyOffRequest.getRecipeVersion()).orElse(""))
                    .active(1)
                    .build();

            logBuyOffRepository.save(buyoffLog);

            rejectionMessage.append(batchNo).append(",");

            // Create and handle ProductionLogRequest inside the loop
            String rejectedMessage = getFormattedMessage(20, rejectionMessage.toString().trim()); // Ensure message is formatted
            ProductionLogRequest productionLogRequest = productionLog(buyoffLog, rejectedMessage);
        }

        if (rejectionMessage.length() > 0) {
            rejectionMessage.setLength(rejectionMessage.length() - 2); // Remove trailing comma and space
        }

        String finalRejectedMessage = getFormattedMessage(21, rejectionMessage.toString());

        return LogBuyOffMessageModel.builder().messageDetails(new MessageDetails(finalRejectedMessage.toString(), "S")).build();
    }

    public boolean checkBuyOffRejectionStatus(LogbuyOffRequest logbuyOffRequest) throws Exception {
        BatchNoInQueueRequest batchNoInQueueRequest = BatchNoInQueueRequest.builder()
                .site(logbuyOffRequest.getSite())
                .batchNo(logbuyOffRequest.getBatchNo())
                .orderNumber(logbuyOffRequest.getOrderNumber())
                .recipe(logbuyOffRequest.getRecipe())
                .recipeVersion(logbuyOffRequest.getRecipeVersion())
                .phaseId(logbuyOffRequest.getPhaseId())
                .operation(logbuyOffRequest.getOperation())
                .build();

        BatchNoInQueue response = batchNoInQueueService.retrieve(batchNoInQueueRequest);
        return response.isQualityApproval();

    }

    public boolean lineClearanceStatusCheck(LogbuyOffRequest logbuyOffRequest) throws Exception {
        LineClearanceLogRequest request = LineClearanceLogRequest.builder()
                .site(logbuyOffRequest.getSite())
                .resourceId(logbuyOffRequest.getResourceId())
                .batchNo(logbuyOffRequest.getBatchNo())
                .operation(logbuyOffRequest.getOperation())
                .phase(logbuyOffRequest.getPhaseId())
                .build();

        List<LineClearanceLogResponse> response = lineClearanceLogService.getLineClearanceLogList(request);
        if (response == null) {
            throw new IllegalStateException("No line clearance logs found.");
        }

        for (LineClearanceLogResponse log : response) {
            if (log == null) continue;

            if (Boolean.TRUE.equals(log.getIsMandatory())) {
                String status = log.getStatus();
                if (status == null || !"complete".equalsIgnoreCase(status.trim())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void isRejectAllowed(LogbuyOffRequest logbuyOffRequest) {
        BuyOff buyOff= BuyOff.builder().buyOff(logbuyOffRequest.getBuyOffBO()).build();
        Boolean isRejectAllowed= webClientBuilder.build()
                .post()
                .uri(isRejectAllowedUrl)
                .bodyValue(buyOff)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if(!isRejectAllowed){
            String[] buyOffBO=   logbuyOffRequest.getBuyOffBO().split(",");
            throw new LogBuyOffException(2002,buyOffBO[1]);
        }

    }
    private void isSkipAllowed(LogbuyOffRequest logbuyOffRequest) {
        BuyOff buyOff= BuyOff.builder().buyOff(logbuyOffRequest.getBuyOffBO()).build();
        Boolean isSkipAllowed= webClientBuilder.build()
                .post()
                .uri(isSkipAllowedUrl)
                .bodyValue(buyOff)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if(!isSkipAllowed){
            String[] buyOffBO=   logbuyOffRequest.getBuyOffBO().split(",");
            throw new LogBuyOffException(2000, buyOffBO[1]);
        }

    }
    private void isPartialAllowed(LogbuyOffRequest logbuyOffRequest) {
        BuyOff buyOff= BuyOff.builder().buyOff(logbuyOffRequest.getBuyOffBO()).build();
        Boolean isPartialAllowed= webClientBuilder.build()
                .post()
                .uri(isPartialAllowedUrl)
                .bodyValue(buyOff)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if(!isPartialAllowed){
            String[] buyOffBO=   logbuyOffRequest.getBuyOffBO().split(",");
            throw new LogBuyOffException(2001, buyOffBO[1]);
        }

    }

    @Override
    public LogBuyOffMessageModel partial(LogbuyOffRequest logbuyOffRequest) {
        isPartialAllowed(logbuyOffRequest);

        BuyoffLog buyoffLog = BuyoffLog.builder()
                .site(Optional.ofNullable(logbuyOffRequest.getSite()).orElse("")) // Fallback to empty string if null
                .handle("LogBuyOffBO:" +
                        Optional.ofNullable(logbuyOffRequest.getSite()).orElse("") + "," +
                        Optional.ofNullable(logbuyOffRequest.getBuyOffBO()).orElse("") + "," +
                        Optional.ofNullable(logbuyOffRequest.getUniqueId()).orElse(0)) // Avoid null in handle
                .buyOffBO(Optional.ofNullable(logbuyOffRequest.getBuyOffBO()).orElse("")) // Fallback to empty string
                .buyOffAction("P") // Hardcoded value
                .pcu(Optional.ofNullable(logbuyOffRequest.getPcu()).orElse("")) // Fallback to empty string
                .buyOffLogId(Optional.ofNullable(logbuyOffRequest.getUniqueId()).orElse(0)) // Fallback to 0 if null
                .comments(Optional.ofNullable(logbuyOffRequest.getComments()).orElse("")) // Fallback to empty string
                .description(Optional.ofNullable(logbuyOffRequest.getDescription()).orElse("")) // Fallback to empty string
                .state("Open") // Hardcoded value
                .quantity(Optional.ofNullable(logbuyOffRequest.getQuantity()).orElse("")) // Fallback to empty string
                .qtyToComplete(Optional.ofNullable(logbuyOffRequest.getQtyToComplete()).orElse("")) // Fallback to empty string
                .operation(Optional.ofNullable(logbuyOffRequest.getOperation()).orElse("")) // Fallback to empty string
                .operationVersion(Optional.ofNullable(logbuyOffRequest.getOperationVersion()).orElse("")) // Fallback to empty string
                .userId(Optional.ofNullable(logbuyOffRequest.getUserId()).orElse("")) // Fallback to empty string
                .item(Optional.ofNullable(logbuyOffRequest.getItem()).orElse("")) // Fallback to empty string
                .itemVersion(Optional.ofNullable(logbuyOffRequest.getItemVersion()).orElse("")) // Fallback to empty string
                .router(Optional.ofNullable(logbuyOffRequest.getRouter()).orElse("")) // Fallback to empty string
                .routerVersion(Optional.ofNullable(logbuyOffRequest.getRouterVersion()).orElse("")) // Fallback to empty string
                .stepId(Optional.ofNullable(logbuyOffRequest.getStepId()).orElse("")) // Fallback to empty string
                .shopOrder(Optional.ofNullable(logbuyOffRequest.getShopOrder()).orElse("")) // Fallback to empty string
                .customerOrderBO(Optional.ofNullable(logbuyOffRequest.getCustomerOrderBO()).orElse("")) // Fallback to empty string
                .processLotBO(Optional.ofNullable(logbuyOffRequest.getProcessLotBO()).orElse("")) // Fallback to empty string
                .resourceId(Optional.ofNullable(logbuyOffRequest.getResourceId()).orElse(""))
                .batchNo(Optional.ofNullable(logbuyOffRequest.getBatchNo()).orElse("")) // Fallback to empty string
                .orderNumber(Optional.ofNullable(logbuyOffRequest.getOrderNumber()).orElse("")) // Fallback to empty string
                .recipe(Optional.ofNullable(logbuyOffRequest.getRecipe()).orElse("")) // Fallback to empty string
                .recipeVersion(Optional.ofNullable(logbuyOffRequest.getRecipeVersion()).orElse("")) // Fallback to empty string// Fallback to empty string
                .dateTime(LocalDateTime.now()) // Current timestamp
                .active(1) // Hardcoded value
                .build();

        logBuyOffRepository.save(buyoffLog);

        logBuyOffRepository.save(buyoffLog);
        String partialMessage=getFormattedMessage(22);
        ProductionLogRequest productionLogRequest= productionLog(buyoffLog,partialMessage);
        return LogBuyOffMessageModel.builder().messageDetails(new MessageDetails(partialMessage,"S")).build();
    }

    @Override
    public LogBuyOffMessageModel skip(LogbuyOffRequest logbuyOffRequest) {
        int uniqueId= logbuyOffRequest.getUniqueId();
        isSkipAllowed(logbuyOffRequest);

        BuyoffLog buyoffLog = BuyoffLog.builder()
                .site(Optional.ofNullable(logbuyOffRequest.getSite()).orElse("")) // Fallback to empty string if null
                .handle("LogBuyOffBO:" +
                        Optional.ofNullable(logbuyOffRequest.getSite()).orElse("") + "," +
                        Optional.ofNullable(logbuyOffRequest.getBuyOffBO()).orElse("") + "," + uniqueId) // Avoid nulls in handle
                .buyOffBO(Optional.ofNullable(logbuyOffRequest.getBuyOffBO()).orElse("")) // Fallback to empty string
                .buyOffAction("S")
                .pcu(Optional.ofNullable(logbuyOffRequest.getPcu())
                        .filter(pcu -> !pcu.isEmpty())
                        .orElse(logbuyOffRequest.getBatchNo())) // Use batchNo if pcu is empty or null
                .buyOffLogId(uniqueId)
                .description(Optional.ofNullable(logbuyOffRequest.getDescription()).orElse("")) // Fallback to empty string
                .comments(Optional.ofNullable(logbuyOffRequest.getComments()).orElse("")) // Fallback to empty string
                .state("Closed")
                .quantity(Optional.ofNullable(logbuyOffRequest.getQuantity()).orElse("")) // Fallback to empty string
                .qtyToComplete(Optional.ofNullable(logbuyOffRequest.getQtyToComplete()).orElse("")) // Fallback to empty string
                .operation(Optional.ofNullable(logbuyOffRequest.getOperation())
                        .filter(op -> !op.isEmpty())
                        .orElse(logbuyOffRequest.getRouter())) // Use router if operation is empty or null
                .operationVersion(Optional.ofNullable(logbuyOffRequest.getOperationVersion())
                        .filter(opVer -> !opVer.isEmpty())
                        .orElse(logbuyOffRequest.getRouterVersion())) // Use routerVersion if operationVersion is empty or null
                .userId(Optional.ofNullable(logbuyOffRequest.getUserId()).orElse("")) // Fallback to empty string
                .item(Optional.ofNullable(logbuyOffRequest.getItem())
                        .filter(item -> !item.isEmpty())
                        .orElse(logbuyOffRequest.getRecipe())) // Use recipe if item is empty or null
                .itemVersion(Optional.ofNullable(logbuyOffRequest.getItemVersion())
                        .filter(itemVer -> !itemVer.isEmpty())
                        .orElse(logbuyOffRequest.getRecipeVersion())) // Use recipeVersion if itemVersion is empty or null
                .router(Optional.ofNullable(logbuyOffRequest.getRouter()).orElse("")) // Fallback to empty string
                .routerVersion(Optional.ofNullable(logbuyOffRequest.getRouterVersion()).orElse("")) // Fallback to empty string
                .stepId(Optional.ofNullable(logbuyOffRequest.getStepId()).orElse("")) // Fallback to empty string
                .shopOrder(Optional.ofNullable(logbuyOffRequest.getShopOrder())
                        .filter(shop -> !shop.isEmpty())
                        .orElse("")) // Use orderNumber if shopOrder is empty or null
                .customerOrderBO(Optional.ofNullable(logbuyOffRequest.getCustomerOrderBO()).orElse("")) // Fallback to empty string
                .processLotBO(Optional.ofNullable(logbuyOffRequest.getProcessLotBO()).orElse("")) // Fallback to empty string
                .resourceId(Optional.ofNullable(logbuyOffRequest.getResourceId()).orElse("")) // Fallback to empty string
                .batchNo(Optional.ofNullable(logbuyOffRequest.getBatchNo()).orElse("")) // Fallback to empty string
                .orderNumber(Optional.ofNullable(logbuyOffRequest.getOrderNumber()).orElse("")) // Fallback to empty string
                .recipe(Optional.ofNullable(logbuyOffRequest.getRecipe()).orElse("")) // Fallback to empty string
                .recipeVersion(Optional.ofNullable(logbuyOffRequest.getRecipeVersion()).orElse("")) // Fallback to empty string
                .dateTime(LocalDateTime.now())
                .active(1)
                .build();

        logBuyOffRepository.save(buyoffLog);
        String skipMessage=getFormattedMessage(23);
        ProductionLogRequest productionLogRequest= productionLog(buyoffLog,skipMessage);
        return LogBuyOffMessageModel.builder().messageDetails(new MessageDetails(skipMessage,"S")).build();
    }

    @Override
    public List<BuyoffLog> getListOfBuyoff(AttachmentDetailsRequest attachmentDetailsRequest) {
        List<BuyoffLog> responses = new ArrayList<>();

        // Check if pcu is empty, and if so, use batchNo
        String pcuOrBatchNo = (attachmentDetailsRequest.getPcu() != null && !attachmentDetailsRequest.getPcu().isEmpty())
                ? attachmentDetailsRequest.getPcu()
                : attachmentDetailsRequest.getBatchNo();  // Use batchNo if pcu is empty

        List<BuyOff> buyOffs = webClientBuilder.build()
                .post()
                .uri(getBuyOffList)
                .bodyValue(attachmentDetailsRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<BuyOff>>() { })
                .block();

        boolean hasRecord = false;
        for (BuyOff buyOff : buyOffs) {
            List<BuyoffLog> response = new ArrayList<>();
            List<BuyoffLog> list = logBuyOffRepository.findByActiveAndSiteAndBuyOffBO(1, attachmentDetailsRequest.getSite(), buyOff.getHandle());

            for (BuyoffLog buyoffLog : list) {
                // Check pcu or batchNo and continue with your existing checks
                if (buyoffLog.getBatchNo().equalsIgnoreCase(pcuOrBatchNo)&&buyoffLog.getOperation().equalsIgnoreCase(attachmentDetailsRequest.getOperation())) {
                    hasRecord = true;
                    if (buyoffLog != null) {
                        // Set default value "0" if qtyToComplete or quantity is null or empty
                        String qtyToComplete = (buyoffLog.getQtyToComplete() == null || buyoffLog.getQtyToComplete().isEmpty()) ? "0" : buyoffLog.getQtyToComplete();
                        String quantity = (buyoffLog.getQuantity() == null || buyoffLog.getQuantity().isEmpty()) ? "0" : buyoffLog.getQuantity();

                       /* if (*//*buyoffLog.getState() != null && buyoffLog.getState().equalsIgnoreCase("Open") ||*//*
                                (Double.parseDouble(qtyToComplete) > Double.parseDouble(quantity))) {*/
                            if (!quantity.equals("0")) {
                                response.add(buyoffLog);
                            }

                    }

                }
            }

            if (!hasRecord) {
                int uniqueId = generateUniqueId();

                String shopOrderValue = (attachmentDetailsRequest.getShopOrder() != null && !attachmentDetailsRequest.getShopOrder().isEmpty()) ? attachmentDetailsRequest.getShopOrder() : null;

                String recipeValue = (attachmentDetailsRequest.getRecipe() != null && !attachmentDetailsRequest.getRecipe().isEmpty()) ? attachmentDetailsRequest.getRecipe() : null;

                // Create BuyoffLog only if essential fields are not null or empty
                BuyoffLog buyoffLogReq = BuyoffLog.builder()
                        .buyOffLogId(uniqueId)
                        .description(buyOff.getDescription())
                        .site(attachmentDetailsRequest.getSite())
                        .buyOffBO(buyOff.getHandle())
                        .pcu(attachmentDetailsRequest.getPcu() != null && !attachmentDetailsRequest.getPcu().isEmpty() ? attachmentDetailsRequest.getPcu() : null)
                        .batchNo(attachmentDetailsRequest.getBatchNo() != null && !attachmentDetailsRequest.getBatchNo().isEmpty() ? attachmentDetailsRequest.getBatchNo() : null)
                        .state("Open")
                        .shopOrder(shopOrderValue) // Add only if shopOrderValue is not null or empty
                        .orderNumber(attachmentDetailsRequest.getOrderNumber()) // Always set from the request
                        .recipe(recipeValue) // Add only if recipeValue is not null or empty
                        .recipeVersion(attachmentDetailsRequest.getRecipeVersion()) // Always set from the request
                        .operation(attachmentDetailsRequest.getOperation()) // Always set from the request
                        .operationVersion(attachmentDetailsRequest.getOperationVersion()) // Always set from the request
                        .item(attachmentDetailsRequest.getItem()) // Always set from the request
                        .itemVersion(attachmentDetailsRequest.getItemVersion())
                        .buyOffAction("N")
                        .build();

                // Only add to response if BuyoffLog is not null
                if (buyoffLogReq != null) {
                    response.add(buyoffLogReq);
                }
            }

            hasRecord = false;
            responses.addAll(response);
        }

//        if (responses.isEmpty()) {
//            throw new LogBuyOffException(2003, attachmentDetailsRequest.getPcu());
//        }
        return responses;
    }

    @Override
    public List<BuyoffLog> retrieveByBatchNo(String site, String batchNo) {
        return logBuyOffRepository.findByActiveAndSiteAndBatchNo(1, site, batchNo);
    }

    private int generateUniqueId() {
        UUID uuid = UUID.randomUUID();
        // Convert UUID to a positive long value
        return (int) (uuid.getMostSignificantBits() & Integer.MAX_VALUE);
    }



    @Override
    public List<BuyoffLog> retrieveLoggedBuyOffList(String site, String pcu, String batchNo, String buyOffBO, String userId, String dateRange, LocalDateTime startDate, LocalDateTime endDate) {
        if (site == null || site.isEmpty()) {
            throw new LogBuyOffException(315);
        }

        Criteria criteria = Criteria.where("site").is(site);
        LocalDateTime now = LocalDateTime.now();

        if (pcu != null && !pcu.isEmpty()) {
            criteria.and("pcu").is(pcu);
        }

        if (batchNo != null && !batchNo.isEmpty()) {
            criteria.and("batchNo").is(batchNo);
        }

        if (buyOffBO != null && !buyOffBO.isEmpty()) {
            criteria.and("buyOffBO").is(buyOffBO);
        }

        if (userId != null && !userId.isEmpty()) {
            criteria.and("userId").is(userId);
        }

        // Apply date filters based on dateRange
        if (dateRange != null) {
            switch (dateRange) {
                case "24hours":
                    criteria.and("dateTime").gte(now.minusHours(24)).lte(now);
                    break;
                case "today":
                    LocalDate today = now.toLocalDate();
                    criteria.and("dateTime")
                            .gte(today.atStartOfDay())  // Fixed: Converting LocalDate to LocalDateTime
                            .lte(now);
                    break;
                case "yesterday":
                    LocalDate yesterday = now.toLocalDate().minusDays(1);
                    criteria.and("dateTime")
                            .gte(yesterday.atStartOfDay())
                            .lte(yesterday.atTime(23, 59, 59));
                    break;
                case "thisWeek":
                    LocalDate startOfWeek = now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    criteria.and("dateTime")
                            .gte(startOfWeek.atStartOfDay())
                            .lte(now);
                    break;
                case "lastWeek":
                    LocalDate lastWeekStart = now.toLocalDate().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    LocalDate lastWeekEnd = lastWeekStart.plusDays(6);
                    criteria.and("dateTime")
                            .gte(lastWeekStart.atStartOfDay())
                            .lte(lastWeekEnd.atTime(23, 59, 59));
                    break;
                case "thisMonth":
                    LocalDate startOfMonth = now.toLocalDate().withDayOfMonth(1);
                    criteria.and("dateTime")
                            .gte(startOfMonth.atStartOfDay())
                            .lte(now);
                    break;
                case "lastMonth":
                    LocalDate lastMonthStart = now.toLocalDate().minusMonths(1).withDayOfMonth(1);
                    LocalDate lastMonthEnd = lastMonthStart.withDayOfMonth(lastMonthStart.lengthOfMonth());
                    criteria.and("dateTime")
                            .gte(lastMonthStart.atStartOfDay())
                            .lte(lastMonthEnd.atTime(23, 59, 59));
                    break;
                case "thisYear":
                    LocalDate startOfYear = now.toLocalDate().withDayOfYear(1);
                    criteria.and("dateTime")
                            .gte(startOfYear.atStartOfDay())
                            .lte(now);
                    break;
                case "lastYear":
                    LocalDate lastYearStart = now.toLocalDate().minusYears(1).withDayOfYear(1);
                    LocalDate lastYearEnd = lastYearStart.withDayOfYear(lastYearStart.lengthOfYear());
                    criteria.and("dateTime")
                            .gte(lastYearStart.atStartOfDay())
                            .lte(lastYearEnd.atTime(23, 59, 59));
                    break;
                case "custom":
                    if (startDate != null && endDate != null) {
                        criteria.and("dateTime").gte(startDate).lte(endDate);
                    }
                    break;
                default:
                    throw new LogBuyOffException(5123, "Invalid dateRange value");
            }
        }

        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "dateTime"));

        List<BuyoffLog> buyOffLogs = mongoTemplate.find(query, BuyoffLog.class);

        // Process buyOffBO field
        for (BuyoffLog log : buyOffLogs) {
            if (log.getBuyOffBO() != null) {
                String[] parts = log.getBuyOffBO().split(",");
                if (parts.length > 1) {
                    log.setBuyOffBO(parts[1]); // Extracts "BUY_OFF1" from "BuyOffBO:RITS,BUY_OFF1,00"
                }
            }
        }

        return buyOffLogs;
    }

    @Override
    public BuyoffLog retrieveByBatchNoAndOrderNumberAndOperation(String site, String batchNo, String orderNumber, String operation) {
        return logBuyOffRepository.findByActiveAndSiteAndBatchNoAndOrderNumberAndOperationAndStateIgnoreCase(
                1, site, batchNo, orderNumber, operation, "Closed"
        );
    }


}



