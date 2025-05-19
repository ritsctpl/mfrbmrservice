package com.rits.processorderstateservice.service;

import com.rits.Utility.ProcessOrderUtility;
import com.rits.batchnocomplete.model.BatchNoComplete;
import com.rits.batchnodoneservice.exception.BatchNoDoneException;
import com.rits.batchnodoneservice.model.BatchNoDone;
import com.rits.batchnoheader.exception.BatchNoHeaderException;
import com.rits.batchnoheader.model.BatchNoHeader;
import com.rits.batchnohold.service.BatchNoHoldService;
import com.rits.batchnoinqueue.exception.BatchNoInQueueException;
import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinqueue.service.BatchNoInQueueService;
import com.rits.batchnoinwork.exception.BatchNoInWorkException;
import com.rits.batchnoinwork.model.BatchNoInWork;
import com.rits.batchnoinwork.service.BatchNoInWorkService;
import com.rits.batchnophaseprogressservice.dto.PhaseProgress;
import com.rits.batchnophaseprogressservice.exception.BatchNoPhaseProgressException;
import com.rits.batchnophaseprogressservice.model.BatchNoPhaseProgress;
import com.rits.batchnorecipeheaderservice.dto.BatchNoRecipeHeaderReq;
import com.rits.batchnorecipeheaderservice.exception.BatchNoRecipeHeaderException;
import com.rits.batchnorecipeheaderservice.model.MessageModel;
import com.rits.batchnorecipeheaderservice.service.BatchNoRecipeHeaderService;
import com.rits.batchnoscrap.dto.BatchNoScrapRequest;
import com.rits.batchnoscrap.model.BatchNoScrap;
import com.rits.batchnoscrap.model.BatchNoScrapMessageModel;
import com.rits.batchnoscrap.service.BatchNoScrapServiceImpl;
import com.rits.batchnoyieldreportingservice.dto.BatchNoYieldReportingRequest;
import com.rits.batchnoyieldreportingservice.exception.BatchNoYieldReportingException;
import com.rits.batchnoyieldreportingservice.repository.BatchNoYieldReportingRepository;
import com.rits.batchnoyieldreportingservice.service.BatchNoYieldReportingService;
import com.rits.dccollect.dto.CustomData;
import com.rits.dccollect.dto.Item;
import com.rits.hookservice.annotation.Hookable;
import com.rits.hookservice.aspect.HookAspect;
import com.rits.hookservice.service.CustomHook;
import com.rits.lineclearancelogservice.dto.LineClearanceLogRequest;
import com.rits.lineclearancelogservice.service.LineClearanceLogService;
import com.rits.lineclearanceservice.dto.RetrieveLineClearanceLogRequest;
import com.rits.lineclearanceservice.exception.LineClearanceException;
import com.rits.lineclearanceservice.model.RetrieveLineClearanceLogResponse;
import com.rits.logbuyoffservice.dto.AttachmentDetailsRequest;
import com.rits.logbuyoffservice.exception.LogBuyOffException;
import com.rits.logbuyoffservice.model.BuyoffLog;
import com.rits.logbuyoffservice.service.LogBuyOffService;
import com.rits.processorderstateservice.dto.*;
import com.rits.processorderstateservice.exception.ProcessOrderStateException;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessOrderStateServiceImpl implements ProcessOrderStateService {

    private final WebClient.Builder webClientBuilder;
    @Value("${item-service.url}/retrieve")
    private String retrieveItemUrl;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private BatchNoInQueueService batchNoInQueueService;

    @Autowired
    private LogBuyOffService logBuyOffService;

    @Autowired
    private LineClearanceLogService lineClearanceLogService;
    @Autowired
    CustomHook customHook;

    @Autowired
    private BatchNoInWorkService batchNoInWorkService;
    private final BatchNoRecipeHeaderService batchNoRecipeHeaderService;
    private final BatchNoYieldReportingRepository batchNoYieldReportingRepository;
    private final BatchNoYieldReportingService batchNoYieldReportingService;
    private final BatchNoScrapServiceImpl batchNoScrapService;

    @Autowired
    private final BatchNoHoldService batchNoHoldService;


    @Override
    @Hookable
    public ProcessOrderStartResponse startProcess(ProcessOrderStartRequest request) throws Exception {
        boolean sync = request.getSync() != null && request.getSync();

        List<CompletableFuture<ProcessOrderStartResponse.BatchStartDetails>> tasks = new ArrayList<>();
        for (ProcessOrderStartRequest.BatchDetails startBatch : request.getStartBatches()) {
            if (sync) {
                tasks.add(CompletableFuture.completedFuture(processStartBatch(startBatch)));
            } else {
                tasks.add(processStartBatchAsync(startBatch));
            }
        }

        List<ProcessOrderStartResponse.BatchStartDetails> processedBatches = tasks.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        ProcessOrderStartResponse response = new ProcessOrderStartResponse();
        boolean hasError = processedBatches.stream()
                .anyMatch(startBatch -> startBatch.getMessage() != null && startBatch.getMessage().startsWith("Error processing batch start"));

        if (hasError) {
            response.setStatus("failure");
            response.setMessage("Batch not started");
        } else {
            response.setStatus("success");
            response.setMessage("Batch started successfully");
        }
        response.setProcessedStartBatches(processedBatches);
        return response;
    }

    @Async
    private CompletableFuture<ProcessOrderStartResponse.BatchStartDetails> processStartBatchAsync(ProcessOrderStartRequest.BatchDetails startBatch) throws Exception {
        return CompletableFuture.completedFuture(processStartBatch(startBatch));
    }

    private ProcessOrderStartResponse.BatchStartDetails processStartBatch(ProcessOrderStartRequest.BatchDetails startBatch) throws Exception {
        ProcessOrderStartResponse.BatchStartDetails batchDetails = new ProcessOrderStartResponse.BatchStartDetails();
        batchDetails.setBatchNumber(startBatch.getBatchNumber());
        batchDetails.setPhase(startBatch.getPhase());
        batchDetails.setOperation(startBatch.getOperation());

        BigDecimal batchQuantity = BigDecimal.ZERO;

        LineClearanceLogRequest lineClearanceLogRequest = LineClearanceLogRequest.builder()
                .site(startBatch.getSite())
                .batchNo(startBatch.getBatchNumber())
                .resourceId(startBatch.getResource())
                .workCenterId(startBatch.getWorkcenter())
                .build();

        try {
            AttachmentDetailsRequest attachmentDetailsRequest = AttachmentDetailsRequest.builder()
                    .site(startBatch.getSite())
                    .batchNo(startBatch.getBatchNumber())
                    .operation(startBatch.getOperation())
                    .operationVersion("#")
                    .orderNumber(startBatch.getOrderNumber())
                    .quantityRequired((startBatch.getQuantity() != null) ? startBatch.getQuantity().toString() : null)
                    .item(startBatch.getMaterial())
                    .itemVersion(startBatch.getMaterialVersion())
                    .resource(startBatch.getResource())
                    .build();
//            List<BuyoffLog> buyoffLogList = logBuyOffService.getListOfBuyoff(attachmentDetailsRequest);
//            for(BuyoffLog buyoffLog : buyoffLogList){
//                if(buyoffLog.getBuyOffAction().equalsIgnoreCase("O") || buyoffLog.getBuyOffAction().equalsIgnoreCase("R") || buyoffLog.getBuyOffAction().equalsIgnoreCase("N")){
//                    throw new ProcessOrderStateException(9013);
//                }
//            }
//
//            List<RetrieveLineClearanceLogResponse> lineClearanceLogResponsesList = lineClearanceLogService.retrieveLineClearanceList(lineClearanceLogRequest);
//            for(RetrieveLineClearanceLogResponse retrieveLineClearanceLogResponse : lineClearanceLogResponsesList){
//                if(retrieveLineClearanceLogResponse.getStatus().equalsIgnoreCase("Start") || retrieveLineClearanceLogResponse.getStatus().equalsIgnoreCase("New")){
//                    throw new ProcessOrderStateException(9014);
//                }
//            }
            if(batchNoHoldService.isBatchOnHold(startBatch.getSite(),startBatch.getBatchNumber()))
                throw new ProcessOrderStateException(7038, startBatch.getBatchNumber());
            // Validate mandatory fields
            ProcessOrderUtility.validateBatchStartDetails(startBatch);

            // Retrieve batch header
            BatchNoHeader batchNoHeader = ProcessOrderUtility.getBatchNoHeaderDetails(startBatch.getSite(), startBatch.getBatchNumber(),startBatch.getOrderNumber(),startBatch.getMaterial(), startBatch.getMaterialVersion());
            if(batchNoHeader == null) {
                throw new BatchNoHeaderException(7016);
            }

            // Retrieve batch inQueue
            BatchNoInQueue batchNoInQueue = ProcessOrderUtility.getBatchNoInQueueDetails(startBatch.getSite(), batchNoHeader.getHandle(), startBatch.getPhase(), startBatch.getOperation());
            if(batchNoInQueue == null) {
                throw new BatchNoInQueueException(7017);
            }

            batchQuantity = (startBatch.getQuantity() == null || startBatch.getQuantity().toString().trim().isEmpty() || startBatch.getQuantity().compareTo(BigDecimal.ZERO) == 0)
                    ? batchNoInQueue.getQtyInQueue()
                    : startBatch.getQuantity();

            // Validate quantity
            if (batchQuantity.compareTo(batchNoInQueue.getQtyInQueue()) > 0) {
                throw new ProcessOrderStateException(7018, startBatch.getBatchNumber());
            }

            // Validate resource status
            if (!ProcessOrderUtility.isResourceStatusAcceptable(startBatch.getSite(), startBatch.getResource())) {
                throw new ProcessOrderStateException(7025);
            }
            // Validate operation status
            if (!ProcessOrderUtility.isOperationStatusAcceptable(startBatch.getSite(), startBatch.getOperation())) {
                throw new ProcessOrderStateException(7026);
            }
            // Validate batch status
            if (!ProcessOrderUtility.isBatchStatusAcceptable(startBatch.getSite(), startBatch.getBatchNumber())) {
                throw new ProcessOrderStateException(7027);
            }
            // Validate process order status (only if order number is present)
//            if (startBatch.getOrderNumber() != null && !startBatch.getOrderNumber().isEmpty() &&
//                    !ProcessOrderUtility.isProcessOrderStatusAcceptable(startBatch.getSite(), startBatch.getOrderNumber())) {
//                throw new ProcessOrderStateException(7029);
//            }
            // Validate user status
            if (startBatch.getUser() != null  && !startBatch.getUser().isEmpty() && !ProcessOrderUtility.isUserStatusAcceptable(startBatch.getUser())) {
                throw new ProcessOrderStateException(7030);
            }
            // Validate workcenter assignment to user (only if user and workcenter are not null)
            if (startBatch.getUser() != null && !startBatch.getUser().isEmpty() && startBatch.getWorkcenter() != null && !startBatch.getWorkcenter().isEmpty() &&
                    !ProcessOrderUtility.isWorkcenterAssignedToUser(startBatch.getUser(), startBatch.getWorkcenter())) {
                throw new ProcessOrderStateException(7031);
            }

            // Fetch resource details from resource service
            boolean isProcessResource = ProcessOrderUtility.getResourceDetails(startBatch.getSite(), startBatch.getResource());

            BatchNoInWork existingBatchInWork = ProcessOrderUtility.getBatchInWorkDetails(startBatch.getSite(), startBatch.getBatchNumber(), startBatch.getPhase(), startBatch.getOperation(), startBatch.getResource(), isProcessResource ? null : startBatch.getUser(), startBatch.getOrderNumber());

            // phase progress
            createOrUpdateBatchNoStartPhaseProgess(startBatch, batchNoHeader, batchNoInQueue, isProcessResource, batchQuantity);

            if (existingBatchInWork != null) {
                // BatchInWork record exists

                if (batchQuantity.compareTo(batchNoInQueue.getQtyInQueue()) == 0) {
                    // Update batchInWork with additional quantity and remove batchNoInQueue
                    existingBatchInWork.setQtyToComplete(existingBatchInWork.getQtyToComplete().add(batchQuantity));
                    ProcessOrderUtility.updateBatchInWork(existingBatchInWork);

                    batchNoInQueueService.delete(batchNoInQueue);
                } else {
                    // Reduce batchNoInQueue quantity and update batchInWork with additional quantity
                    batchNoInQueue.setQtyInQueue(batchNoInQueue.getQtyInQueue().subtract(batchQuantity));
                    ProcessOrderUtility.updateBatchInQueue(batchNoInQueue);

                    existingBatchInWork.setQtyToComplete(existingBatchInWork.getQtyToComplete().add(batchQuantity));
                    ProcessOrderUtility.updateBatchInWork(existingBatchInWork);
                }
            } else {
                // BatchInWork record does not exist
                if (batchQuantity.compareTo(batchNoInQueue.getQtyInQueue()) == 0) {
                    // Create new batchInWork and remove batchNoInQueue
                    ProcessOrderUtility.createBatchInWork(startBatch, isProcessResource, batchNoInQueue);

                    batchNoInQueueService.delete(batchNoInQueue);
                } else {
                    // Reduce batchNoInQueue quantity and create new batchInWork
                    ProcessOrderUtility.createBatchInWork(startBatch, isProcessResource, batchNoInQueue);

                    batchNoInQueue.setQtyInQueue(batchNoInQueue.getQtyInQueue().subtract(batchQuantity));
                    ProcessOrderUtility.updateBatchInQueue(batchNoInQueue);
                }
            }

            batchDetails.setQuantity(batchQuantity);
            batchDetails.setMessage("Batch " + startBatch.getBatchNumber() + " started successfully for phase " + startBatch.getPhase() + " and operation " + startBatch.getOperation());
        } catch (ProcessOrderStateException e) {
            batchDetails.setMessage("Error processing batch start 1: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (BatchNoInWorkException e) {
            batchDetails.setMessage("Error processing batch start 2: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (BatchNoInQueueException e) {
            batchDetails.setMessage("Error processing batch start 3: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (BatchNoHeaderException e) {
            batchDetails.setMessage("Error processing batch start 4: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (LogBuyOffException e){
            batchDetails.setMessage("Error processing batch start 5: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (LineClearanceException e){
            batchDetails.setMessage("Error processing batch start 6: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        }

        if (!batchDetails.getMessage().startsWith("Error")) {

            ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                    .site(startBatch.getSite())
                    .eventType("startSfcBatch")
                    .userId(startBatch.getUser())
                    .batchNo(startBatch.getOrderNumber() + "_" + startBatch.getBatchNumber())
                    .orderNumber(startBatch.getOrderNumber())
                    .operation(startBatch.getOperation())
                    .phaseId(startBatch.getPhase())
                    .workcenterId(startBatch.getWorkcenter())
                    .resourceId(startBatch.getResource())
                    .material(startBatch.getMaterial())
                    .materialVersion(startBatch.getMaterialVersion())
                    .shopOrderBO(startBatch.getOrderNumber())
                    .qty(batchQuantity.intValue())
                    .topic("production-log")
                    .status("Active")
                    .createdDatetime(LocalDateTime.now())
                    .eventData(startBatch.getBatchNumber() + " Started successfully")
                    .build();

            boolean productionLog = ProcessOrderUtility.productionLog(productionLogRequest);

            if (!productionLog) {
                throw new ProcessOrderStateException(7024);
            }
        }

        return batchDetails;
    }


    @Override
    public ProcessOrderSignoffResponse signoffProcess(ProcessOrderSignoffRequest request) throws Exception {
        boolean sync = request.getSync() != null && request.getSync();

        List<CompletableFuture<ProcessOrderSignoffResponse.BatchSignoffDetails>> tasks = new ArrayList<>();
        for (ProcessOrderSignoffRequest.BatchDetails signoffBatch : request.getSignoffBatches()) {
            if (sync) {
                tasks.add(CompletableFuture.completedFuture(processSignoffBatch(signoffBatch)));
            } else {
                tasks.add(processSignoffBatchAsync(signoffBatch));
            }
        }

        List<ProcessOrderSignoffResponse.BatchSignoffDetails> processedBatches = tasks.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        ProcessOrderSignoffResponse response = new ProcessOrderSignoffResponse();
        boolean hasError = processedBatches.stream()
                .anyMatch(signoffBatch -> signoffBatch.getMessage() != null && signoffBatch.getMessage().startsWith("Error processing batch signoff"));

        if (hasError) {
            response.setStatus("failure");
            response.setMessage("Batch not signedOff");
        } else {
            response.setStatus("success");
            response.setMessage("Batch signedOff successfully");
        }
        response.setProcessedSignoffBatches(processedBatches);
        return response;
    }

    @Async
    private CompletableFuture<ProcessOrderSignoffResponse.BatchSignoffDetails> processSignoffBatchAsync(ProcessOrderSignoffRequest.BatchDetails signoffBatch) throws Exception {
        return CompletableFuture.completedFuture(processSignoffBatch(signoffBatch));
    }

    private ProcessOrderSignoffResponse.BatchSignoffDetails processSignoffBatch(ProcessOrderSignoffRequest.BatchDetails signoffBatch) throws Exception {
        ProcessOrderSignoffResponse.BatchSignoffDetails batchDetails = new ProcessOrderSignoffResponse.BatchSignoffDetails();
        batchDetails.setBatchNumber(signoffBatch.getBatchNumber());
        batchDetails.setPhase(signoffBatch.getPhase());
        batchDetails.setOperation(signoffBatch.getOperation());

        BigDecimal batchQuantity = BigDecimal.ZERO;

        try {

            if(batchNoHoldService.isBatchOnHold(signoffBatch.getSite(),signoffBatch.getBatchNumber()))
                throw new ProcessOrderStateException(7038, signoffBatch.getBatchNumber());
            // Validate mandatory fields
            ProcessOrderUtility.validateBatchSignoffDetails(signoffBatch);

            // Retrieve batch header
            BatchNoHeader batchNoHeader = ProcessOrderUtility.getBatchNoHeaderDetails(signoffBatch.getSite(), signoffBatch.getBatchNumber(),signoffBatch.getOrderNumber(),signoffBatch.getMaterial(), signoffBatch.getMaterialVersion());
            if(batchNoHeader == null) {
                throw new BatchNoHeaderException(7016);
            }

            // Fetch resource details from resource service
            boolean isProcessResource = ProcessOrderUtility.getResourceDetails(signoffBatch.getSite(), signoffBatch.getResource());

            // Retrieve batch Inwork
            BatchNoInWork batchNoInWork = ProcessOrderUtility.getBatchNoInWorkDetails(signoffBatch.getSite(), batchNoHeader.getHandle(), signoffBatch.getPhase(), signoffBatch.getOperation(), signoffBatch.getResource(), isProcessResource ? null : signoffBatch.getUser());
            if(batchNoInWork == null){
                throw new BatchNoInWorkException(7022);
            }

            batchQuantity = (signoffBatch.getQuantity() == null || signoffBatch.getQuantity().toString().trim().isEmpty() || signoffBatch.getQuantity().compareTo(BigDecimal.ZERO) == 0)
                    ? batchNoInWork.getQtyToComplete()
                    : signoffBatch.getQuantity();

            // signoff phase progress
            signOffForPhaseProgress(signoffBatch, batchNoHeader, batchQuantity);

            // Validate quantity
            if (batchQuantity.compareTo(batchNoInWork.getQtyToComplete()) > 0) {
                throw new ProcessOrderStateException(7021, signoffBatch.getBatchNumber());
            }

            // Validate resource status
            if (!ProcessOrderUtility.isResourceStatusAcceptable(signoffBatch.getSite(), signoffBatch.getResource())) {
                throw new ProcessOrderStateException(7025);
            }
            // Validate operation status
            if (!ProcessOrderUtility.isOperationStatusAcceptable(signoffBatch.getSite(), signoffBatch.getOperation())) {
                throw new ProcessOrderStateException(7026);
            }
            // Validate batch status
            if (!ProcessOrderUtility.isBatchStatusAcceptable(signoffBatch.getSite(), signoffBatch.getBatchNumber())) {
                throw new ProcessOrderStateException(7027);
            }
            /*// Validate process order status (only if order number is present)
            if (signoffBatch.getOrderNumber() != null && !signoffBatch.getOrderNumber().isEmpty() &&
                    !ProcessOrderUtility.isProcessOrderStatusAcceptable(signoffBatch.getSite(), signoffBatch.getOrderNumber())) {
                throw new ProcessOrderStateException(7029);
            }*/
            // Validate user status
            if (signoffBatch.getUser() != null  && !signoffBatch.getUser().isEmpty() && !ProcessOrderUtility.isUserStatusAcceptable(signoffBatch.getUser())) {
                throw new ProcessOrderStateException(7030);
            }
            // Validate workcenter assignment to user (only if user and workcenter are not null)
            if (signoffBatch.getUser() != null && !signoffBatch.getUser().isEmpty() && signoffBatch.getWorkcenter() != null && !signoffBatch.getWorkcenter().isEmpty() &&
                    !ProcessOrderUtility.isWorkcenterAssignedToUser(signoffBatch.getUser(), signoffBatch.getWorkcenter())) {
                throw new ProcessOrderStateException(7031);
            }

            // Retrieve batch inQueue
            BatchNoInQueue existingBatchInQueue = ProcessOrderUtility.getBatchNoInQueueDetails(signoffBatch.getSite(), batchNoHeader.getHandle(), signoffBatch.getPhase(), signoffBatch.getOperation());

            if (existingBatchInQueue != null) {
                // BatchInQueue record exists

                if (batchQuantity.compareTo(batchNoInWork.getQtyToComplete()) == 0) {
                    // Update batchInQueue with additional quantity and remove batchWork
                    existingBatchInQueue.setQtyInQueue(existingBatchInQueue.getQtyInQueue().add(batchQuantity));
                    ProcessOrderUtility.updateBatchInQueue(existingBatchInQueue);

                    batchNoInWorkService.delete(batchNoInWork);
                } else {
                    // Reduce batchInWork quantity and update batchInQueue with additional quantity
                    batchNoInWork.setQtyToComplete(batchNoInWork.getQtyToComplete().subtract(batchQuantity));
                    ProcessOrderUtility.updateBatchInWork(batchNoInWork);

                    existingBatchInQueue.setQtyInQueue(existingBatchInQueue.getQtyInQueue().add(batchQuantity));
                    ProcessOrderUtility.updateBatchInQueue(existingBatchInQueue);
                }
            } else {
                // BatchInQueue record does not exist
                if (batchQuantity.compareTo(batchNoInWork.getQtyToComplete()) == 0) {
                    // Create new batchInQueue and remove batchInWork
                    ProcessOrderUtility.createBatchInQueueSignoff(signoffBatch, isProcessResource, batchNoInWork);

                    batchNoInWorkService.delete(batchNoInWork);
                } else {
                    // Reduce batchInWork quantity and create new batchInQueue
                    ProcessOrderUtility.createBatchInQueueSignoff(signoffBatch, isProcessResource, batchNoInWork);

                    batchNoInWork.setQtyToComplete(batchNoInWork.getQtyToComplete().subtract(batchQuantity));
                    ProcessOrderUtility.updateBatchInWork(batchNoInWork);
                }
            }

            batchDetails.setQuantity(batchQuantity);
            batchDetails.setMessage("Batch " + signoffBatch.getBatchNumber() + " signedOff successfully for phase " + signoffBatch.getPhase() + " and operation " + signoffBatch.getOperation());
        } catch (ProcessOrderStateException e) {
            batchDetails.setMessage("Error processing batch signoff: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (BatchNoInWorkException e) {
            batchDetails.setMessage("Error processing batch signoff: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (BatchNoInQueueException e) {
            batchDetails.setMessage("Error processing batch signoff: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (BatchNoHeaderException e) {
            batchDetails.setMessage("Error processing batch signoff: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (LogBuyOffException e){
            batchDetails.setMessage("Error processing batch signoff: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        }

        if (!batchDetails.getMessage().startsWith("Error")) {

            String eventType = "signoffSfcBatch"; // Default eventType
            if ("cmp_signoff".equalsIgnoreCase(signoffBatch.getReasonCode())) {
                eventType = "signOffCmp";
            }

            ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                    .site(signoffBatch.getSite())
                    .eventType(eventType) // Set dynamically based on reasonCode
                    .userId(signoffBatch.getUser())
                    .batchNo(signoffBatch.getOrderNumber() + "_" + signoffBatch.getBatchNumber())
                    .orderNumber(signoffBatch.getOrderNumber())
                    .operation(signoffBatch.getOperation())
                    .phaseId(signoffBatch.getPhase())
                    .workcenterId(signoffBatch.getWorkcenter())
                    .resourceId(signoffBatch.getResource())
                    .material(signoffBatch.getMaterial())
                    .materialVersion(signoffBatch.getMaterialVersion())
                    .shopOrderBO(signoffBatch.getOrderNumber())
                    .qty(batchQuantity.intValue())
                    .topic("production-log")
                    .status("Active")
                    .createdDatetime(LocalDateTime.now())
                    .eventData(signoffBatch.getBatchNumber() + " Signedoff successfully")
                    .build();

            boolean productionLog = ProcessOrderUtility.productionLog(productionLogRequest);
            if (!productionLog) {
                throw new ProcessOrderStateException(7024);
            }
        }

        return batchDetails;
    }

    public void signOffForPhaseProgress(ProcessOrderSignoffRequest.BatchDetails signoffBatch, BatchNoHeader batchNoHeader, BigDecimal batchQuantity) throws Exception {

        BatchNoPhaseProgress existingBatchNoPhaseRecord;
        if (signoffBatch.getOrderNumber() != null && !signoffBatch.getOrderNumber().isEmpty()) {
            existingBatchNoPhaseRecord = ProcessOrderUtility.getBatchNoPhaseProgressDetailsByOrderNumber(signoffBatch.getSite(), signoffBatch.getBatchNumber(), signoffBatch.getMaterial(), signoffBatch.getOrderNumber(), signoffBatch.getMaterialVersion());
        } else {
            existingBatchNoPhaseRecord = ProcessOrderUtility.getBatchNoPhaseProgressDetailsByBatchNoHeaderBO(signoffBatch.getSite(), signoffBatch.getBatchNumber(), signoffBatch.getMaterial(), signoffBatch.getMaterialVersion(), batchNoHeader.getHandle());
        }

        if (existingBatchNoPhaseRecord != null && existingBatchNoPhaseRecord.getPhaseProgress() != null) {
            PhaseProgress existingPhaseProgress = existingBatchNoPhaseRecord.getPhaseProgress().stream()
                    .filter(pp -> signoffBatch.getPhase().equals(pp.getPhase()) &&
                            signoffBatch.getOperation().equals(pp.getOperation()))
                    .findFirst()
                    .orElse(null);

            if (existingPhaseProgress != null) {
                BigDecimal currentQuantity = existingPhaseProgress.getStartQuantityBaseUom();
                if(currentQuantity != null) {
                    BigDecimal updatedQuantity = currentQuantity
                            .subtract(batchQuantity);
                    existingPhaseProgress.setStartQuantityBaseUom(updatedQuantity);
                    existingPhaseProgress.setStartQuantityMeasuredUom(updatedQuantity);

                    ProcessOrderUtility.updateBatchNoPhaseProgess(existingBatchNoPhaseRecord);
                }
            }
        }
    }


    @Override
    @Hookable
    public ProcessOrderCompleteResponse processOrderComplete(ProcessOrderCompleteRequest request) throws Exception {
        boolean sync = request.getSync() != null && request.getSync();

        List<CompletableFuture<ProcessOrderCompleteResponse.BatchCompleteDetails>> tasks = new ArrayList<>();
        for (ProcessOrderCompleteRequest.BatchDetails batch : request.getCompleteBatches()) {
            if (sync) {
                tasks.add(CompletableFuture.completedFuture(processBatch(batch)));
            } else {
                tasks.add(processCompleteBatchAsync(batch));
            }
        }

        List<ProcessOrderCompleteResponse.BatchCompleteDetails> processedBatches = tasks.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        ProcessOrderCompleteResponse response = new ProcessOrderCompleteResponse();
        boolean hasError = processedBatches.stream()
                .anyMatch(batch -> batch.getMessage() != null && batch.getMessage().startsWith("Error processing batch complete"));

        if (hasError) {
            response.setStatus("failure");
            response.setMessage("Batch not completed");
        } else {
            response.setStatus("success");
            response.setMessage("Batch completed successfully");
        }
        response.setBatchDetails(processedBatches);
        return response;
    }

    @Async
    private CompletableFuture<ProcessOrderCompleteResponse.BatchCompleteDetails> processCompleteBatchAsync(ProcessOrderCompleteRequest.BatchDetails batch) throws Exception {
        return CompletableFuture.completedFuture(processBatch(batch));
    }

    private ProcessOrderCompleteResponse.BatchCompleteDetails processBatch(ProcessOrderCompleteRequest.BatchDetails completebatch) throws Exception {

        BigDecimal inQueueQty = BigDecimal.ZERO;
        BigDecimal completeQtyBeforeScrap = BigDecimal.ZERO;
        BigDecimal completeQtyAfterScrap = BigDecimal.ZERO;
        BigDecimal scrapQty = BigDecimal.ZERO;
        boolean insertProductionLog = false;
        BigDecimal yieldQuantityBaseUom = null;
        BigDecimal yieldQuantityMeasuredUom = null;
        boolean scrapFound = false;

        ProcessOrderCompleteResponse.BatchCompleteDetails batchDetails = new ProcessOrderCompleteResponse.BatchCompleteDetails();
        batchDetails.setBatchNumber(completebatch.getBatchNumber());
        batchDetails.setPhase(completebatch.getPhase());
        batchDetails.setOperation(completebatch.getOperation());

        try {
            if(batchNoHoldService.isBatchOnHold(completebatch.getSite(),completebatch.getBatchNumber()))
                throw new ProcessOrderStateException(7038, completebatch.getBatchNumber());

            ProcessOrderUtility.validateBatchCompleteDetails(completebatch);
            // validations resource, operation, batchStatus, user, workCenter
            completeBasicValidations(completebatch);

            //retieve batchNoHeader details
            BatchNoHeader batchNoHeader = getBatchHeaderDetails(completebatch);
            // Retrieve batch inWork
            BatchNoInWork batchNoInWork = getBatchInWorkDetails(completebatch, batchNoHeader);

            boolean isProcessResource = ProcessOrderUtility.getResourceDetails(completebatch.getSite(), completebatch.getResource());

            BatchNoInQueue batchNoInQueue = ProcessOrderUtility.getBatchNoInQueueDetails(completebatch.getSite(), batchNoHeader.getHandle(), completebatch.getPhase(), completebatch.getOperation());
            if(batchNoInQueue != null && Boolean.TRUE.equals(completebatch.getFinalReport())) {
                throw new BatchNoInQueueException(3813);
            }

            completeQtyBeforeScrap = completebatch.getQuantity();
            completeQtyAfterScrap = completebatch.getQuantity().subtract(getProperQty(completebatch.getScrapQuantity()));
            scrapQty = completebatch.getScrapQuantity();

//            boolean isToleranceValid = isQuantityWithinTolerance(completebatch, batchNoHeader, completeQtyBeforeScrap, isProcessResource, batchNoInWork, batchNoInQueue);
//            if(!isToleranceValid)
//                throw new ProcessOrderStateException(339);
            if (customHook.isHookExecuted()) {
                if (batchNoInWork.getQtyToComplete().compareTo(completeQtyBeforeScrap) < 0)
                    throw new ProcessOrderStateException(2920, completebatch.getBatchNumber());
            }
            if(completeQtyBeforeScrap.compareTo(batchNoInWork.getQtyToComplete()) >= 0) {
                if(batchNoInQueue == null){
                    completebatch.setFinalReport(true);
                }
            }

            if (completebatch != null && scrapQty != null && scrapQty.compareTo(BigDecimal.ZERO) != 0) {
                if (completebatch.getScrapQuantity().compareTo(completeQtyBeforeScrap) > 0) {
                    throw new ProcessOrderStateException(7032);
                }
                BatchNoScrapRequest batchNoScrapRequest = buildScrapRequest(completebatch, batchNoInWork, scrapQty, completeQtyBeforeScrap, completeQtyAfterScrap);
                batchNoScrapService.scrap(batchNoScrapRequest);
                scrapFound = true;
            }

            if (completebatch.getUom().equalsIgnoreCase(batchNoHeader.getBaseUom())) {
                yieldQuantityBaseUom = batchNoHeader.getReleasedQuantityBaseUom();
            } else if (completebatch.getUom().equalsIgnoreCase(batchNoHeader.getMeasuredUom()))
                yieldQuantityMeasuredUom = batchNoHeader.getReleasedQuantityMeasuredUom();

            // nextPhase and Operation
            MessageModel nextOp = getNextPhaseAndOperation(completebatch);
            boolean isFinalOperation = nextOp.isFinalValue();

            if(!scrapFound) {
                // Handle BatchNoYield
                createOrUpdateBatchNoYield(completebatch, isProcessResource, batchNoInWork, completeQtyAfterScrap, completeQtyBeforeScrap);

                // Handle BatchNoPhaseProgress
                createOrUpdateBatchNoPhaseProgess(completebatch, batchNoHeader, completeQtyAfterScrap, batchNoInWork, isProcessResource);
            }

            // Handle BatchNoComplete
            BatchNoComplete batchNoCompleteRecord = createOrUpdateCompleteRecord(completebatch, batchNoHeader, isProcessResource, completeQtyAfterScrap, yieldQuantityBaseUom, yieldQuantityMeasuredUom, batchNoInWork);
            if(batchNoCompleteRecord != null){
                insertProductionLog = true;
            }


            if(completebatch.getFinalReport()) {

                BigDecimal balanceQtyInBatch = batchNoInWork.getQtyToComplete().subtract(completeQtyBeforeScrap);
                if (balanceQtyInBatch.compareTo(BigDecimal.ZERO) > 0) {
                    //logging scrapEvent
                    BatchNoScrapRequest batchNoScrapRequest = buildScrapRequest(completebatch, batchNoInWork, balanceQtyInBatch, BigDecimal.ZERO, BigDecimal.ZERO);
                    batchNoScrapService.scrap(batchNoScrapRequest);
                }

                // handle inQueue
                inQueueQty = batchNoCompleteRecord.getQtyToComplete();
                createOrUpdateBatchInQueue(completebatch, batchNoHeader, inQueueQty, batchNoInWork, isProcessResource, nextOp, batchDetails);
            }
            if (isFinalOperation) {
//                if(completebatch.getFinalReport())
//                    scrapQty = scrapQty.add(batchNoInWork.getQtyToComplete().subtract(completeQtyBeforeScrap));
                // handle done
                createOrUpdateDone(completebatch, batchNoHeader, completeQtyAfterScrap, batchNoInWork, isProcessResource);
//                createOrUpdateDone(completebatch, batchNoHeader, completeQtyAfterScrap, batchNoInWork, isProcessResource, scrapQty);

                // handle inventory
                createOrUpdateInventory(completebatch, completeQtyAfterScrap);
            }
            // Update or Remove BatchInWork
            Boolean inWorkDeleted = false;

            if (completeQtyBeforeScrap.compareTo(batchNoInWork.getQtyToComplete()) >= 0) {

                batchNoInWorkService.delete(batchNoInWork);
                inWorkDeleted = true;
            } else {

                batchNoInWork.setQtyToComplete(batchNoInWork.getQtyToComplete().subtract(completeQtyBeforeScrap));
                ProcessOrderUtility.updateBatchInWork(batchNoInWork);
            }

            if(completebatch.getFinalReport()){
                //delete inworkRecord
                if(!inWorkDeleted) {
                    batchNoInWorkService.delete(batchNoInWork);
                }
            }

            batchDetails.setQuantity(completeQtyBeforeScrap);

            batchDetails.setMessage("Batch " + completebatch.getBatchNumber() + " completed successfully for phase " + completebatch.getPhase() + " and operation " + completebatch.getOperation());
        }catch (ProcessOrderStateException e) {
            batchDetails.setMessage("Error processing batch complete 1: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (BatchNoInWorkException e) {
            batchDetails.setMessage("Error processing batch complete 2: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (BatchNoInQueueException e) {
            batchDetails.setMessage("Error processing batch complete 3: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (BatchNoHeaderException e) {
            batchDetails.setMessage("Error processing batch complete 4: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        } catch (BatchNoDoneException e) {
            batchDetails.setMessage("Error processing batch complete 5: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        }catch (BatchNoRecipeHeaderException e) {
            batchDetails.setMessage("Error processing batch complete 6: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        }catch (BatchNoPhaseProgressException e){
            batchDetails.setMessage("Error processing batch complete 7: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        }catch (BatchNoYieldReportingException e){
            batchDetails.setMessage("Error processing batch complete 8: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        }catch (LogBuyOffException e){
            batchDetails.setMessage("Error processing batch complete 9: " + messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), Locale.getDefault()));
        }
        catch (Exception e) {
            batchDetails.setMessage(e.getMessage());
        }

        if (insertProductionLog) {
            boolean productionLog = logProductionLogForComplete(completebatch, completeQtyAfterScrap.intValue());

            if (!productionLog)
                throw new ProcessOrderStateException(7024);
        }
        return batchDetails;
    }

    private BigDecimal getProperQty(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BatchNoHeader getBatchHeaderDetails(ProcessOrderCompleteRequest.BatchDetails completebatch) throws BatchNoHeaderException {
        BatchNoHeader batchNoHeader = ProcessOrderUtility.getBatchNoHeaderDetails(completebatch.getSite(), completebatch.getBatchNumber(), completebatch.getOrderNumber(),
                completebatch.getMaterial(), completebatch.getMaterialVersion());
        if (batchNoHeader == null) {
            throw new BatchNoHeaderException(7016);
        }
        return batchNoHeader;
    }

    private BatchNoInWork getBatchInWorkDetails(ProcessOrderCompleteRequest.BatchDetails completebatch, BatchNoHeader batchNoHeader) throws BatchNoInWorkException {
        boolean isProcessResource = ProcessOrderUtility.getResourceDetails(completebatch.getSite(), completebatch.getResource());

        BatchNoInWork batchNoInWork = ProcessOrderUtility.getBatchNoInWorkDetails(completebatch.getSite(), batchNoHeader.getHandle(), completebatch.getPhase(), completebatch.getOperation(),
                completebatch.getResource(), isProcessResource ? null : completebatch.getUser());
        if (batchNoInWork == null) {
            throw new BatchNoInWorkException(7022);
        }
        return batchNoInWork;
    }

    public boolean isQuantityWithinTolerance(ProcessOrderCompleteRequest.BatchDetails completebatch, BatchNoHeader batchNoHeader, BigDecimal completeQtyBeforeScrap,
                                             boolean isProcessResource, BatchNoInWork batchNoInWork, BatchNoInQueue batchNoInQueue) throws Exception {

        BigDecimal batchSize = batchNoInWork.getQtyToComplete();
        BigDecimal providedQty = completeQtyBeforeScrap;
        BigDecimal qty = BigDecimal.ZERO;
        String providedOperation = completebatch.getOperation();

        String minToleranceValue = null;
        String maxToleranceValue = null;
        BigDecimal minToleranceQty = BigDecimal.ZERO;
        BigDecimal maxToleranceQty = BigDecimal.ZERO;

        Item itemResponse = retrieveItem(completebatch);
        List<CustomData> customDataList = itemResponse.getCustomDataList();

        // If there is no custom data, return true
        if (customDataList == null || customDataList.isEmpty()) {
            minToleranceQty = BigDecimal.ONE;
            maxToleranceQty = batchSize;

        } else {

            Optional<CustomData> minToleranceData = customDataList.stream()
                    .filter(data -> "MIN_TOLERANCE".equals(data.getCustomData()))
                    .findFirst();

            Optional<CustomData> maxToleranceData = customDataList.stream()
                    .filter(data -> "MAX_TOLERANCE".equals(data.getCustomData()))
                    .findFirst();

            if (minToleranceData.isPresent()) {
                minToleranceValue = Arrays.stream(minToleranceData.get().getValue().split(";"))
                        .filter(val -> val.startsWith(providedOperation + ":")) // Compare with provided operation
                        .map(val -> val.split(":")[1].replace("%", "")) // Extract percentage
                        .findFirst()
                        .orElse(null);
            }

            if (maxToleranceData.isPresent()) {
                maxToleranceValue = Arrays.stream(maxToleranceData.get().getValue().split(";"))
                        .filter(val -> val.startsWith(providedOperation + ":"))
                        .map(val -> val.split(":")[1].replace("%", ""))
                        .findFirst()
                        .orElse(null);
            }

            if (minToleranceValue == null) {
                minToleranceQty = BigDecimal.ONE;
            } else {
                minToleranceQty = batchSize.subtract(batchSize.multiply(new BigDecimal(minToleranceValue))
                        .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
            }

            if (maxToleranceValue == null) {
                maxToleranceQty = batchSize;
            } else {
                maxToleranceQty = batchSize.add(batchSize.multiply(new BigDecimal(maxToleranceValue))
                        .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
            }

        }

        if(providedQty.compareTo(batchNoInWork.getQtyToComplete()) >= 0 || providedQty.compareTo(maxToleranceQty) == 0) {
            if(batchNoInQueue == null){
                completebatch.setFinalReport(true);
            }
        }

        if (maxToleranceQty != null) {
            return providedQty.compareTo(minToleranceQty) >= 0 && providedQty.compareTo(maxToleranceQty) <= 0;
        }
        return false;
    }

    private Item retrieveItem(ProcessOrderCompleteRequest.BatchDetails completebatch) {
        Item itemRequest = Item.builder().site(completebatch.getSite()).item(completebatch.getMaterial()).revision(completebatch.getMaterialVersion()).build();
        Item item = webClientBuilder.build()
                .post()
                .uri(retrieveItemUrl)
                .bodyValue(itemRequest)
                .retrieve()
                .bodyToMono(Item.class)
                .block();
        if (item == null || item.getItem() == null || item.getItem().isEmpty()) {
            throw new ProcessOrderStateException(300, completebatch.getMaterial(), completebatch.getMaterialVersion());
        }
        return item;
    }

    private void completeBasicValidations(ProcessOrderCompleteRequest.BatchDetails completebatch) throws Exception {

        if (!ProcessOrderUtility.isResourceStatusAcceptable(completebatch.getSite(), completebatch.getResource())) {
            throw new ProcessOrderStateException(7025);
        }
        if (!ProcessOrderUtility.isOperationStatusAcceptable(completebatch.getSite(), completebatch.getOperation())) {
            throw new ProcessOrderStateException(7026);
        }

        if (!ProcessOrderUtility.isBatchStatusAcceptable(completebatch.getSite(), completebatch.getBatchNumber())) {
            throw new ProcessOrderStateException(7027);
        }

        if (completebatch.getUser() != null  && !completebatch.getUser().isEmpty() && !ProcessOrderUtility.isUserStatusAcceptable(completebatch.getUser())) {
            throw new ProcessOrderStateException(7030);
        }

        if (completebatch.getUser() != null && !completebatch.getUser().isEmpty() && completebatch.getWorkcenter() != null && !completebatch.getWorkcenter().isEmpty() &&
                !ProcessOrderUtility.isWorkcenterAssignedToUser(completebatch.getUser(), completebatch.getWorkcenter())) {
            throw new ProcessOrderStateException(7031);
        }

//            if (completebatch.getOrderNumber() != null && !completebatch.getOrderNumber().isEmpty() &&
//                    !ProcessOrderUtility.isProcessOrderStatusAcceptable(completebatch.getSite(), completebatch.getOrderNumber())) {
//                throw new ProcessOrderStateException(7029);
//            }
    }

    private MessageModel getNextPhaseAndOperation(ProcessOrderCompleteRequest.BatchDetails completebatch) throws Exception {
        BatchNoRecipeHeaderReq nextOperationRequest = BatchNoRecipeHeaderReq.builder()
                .site(completebatch.getSite())
                .batchNo(completebatch.getBatchNumber())
                .orderNo(completebatch.getOrderNumber())
                .material(completebatch.getMaterial())
                .materialVersion(completebatch.getMaterialVersion())
                .phaseId(completebatch.getPhase())
                .operationId(completebatch.getOperation())
                .build();

        return batchNoRecipeHeaderService.getNextOperation(nextOperationRequest);
    }

    private void createOrUpdateBatchInQueue(ProcessOrderCompleteRequest.BatchDetails completebatch, BatchNoHeader batchNoHeader, BigDecimal quantityToProcess,
                                            BatchNoInWork batchNoInWork, boolean isProcessResource, MessageModel nextOp, ProcessOrderCompleteResponse.BatchCompleteDetails batchDetails) throws Exception {
        if (nextOp != null && nextOp.getResultBody() != null) {
            Map<String, Object> resultBody = nextOp.getResultBody();

            String nextOpId = (String) resultBody.get("operationId");
            String nextPhaseId = (String) resultBody.get("phaseId");

            BatchNoInQueue existingBatchNoInQueue = ProcessOrderUtility.getBatchNoInQueueDetails(completebatch.getSite(), batchNoHeader.getHandle(), nextPhaseId,nextOpId);

            if (existingBatchNoInQueue != null) {
                existingBatchNoInQueue.setQtyInQueue(existingBatchNoInQueue.getQtyInQueue().add(quantityToProcess));
                ProcessOrderUtility.updateBatchInQueue(existingBatchNoInQueue);
            } else {
                ProcessOrderUtility.createBatchInQueue(nextOp, completebatch, batchNoInWork, isProcessResource, quantityToProcess);
                if (nextOp.getResultBody() != null) {
                    for (Map.Entry<String, Object> entry : nextOp.getResultBody().entrySet()) {
                        if ("operationId".equals(entry.getKey())) {
                            batchDetails.setNextOperation((String) entry.getValue());
                        } else if ("phaseId".equals(entry.getKey())) {
                            batchDetails.setNextPhase((String) entry.getValue());
                        }
                    }
                }
            }
        }
    }

    private void createOrUpdateInventory(ProcessOrderCompleteRequest.BatchDetails completebatch, BigDecimal quantityToProcess) throws Exception {
        InventoryRequest inventoryRequest = createInventoryRequest(completebatch, quantityToProcess);

        InventoryRequest existingInventory = ProcessOrderUtility.getInventoryById(inventoryRequest);

        if (existingInventory != null) {
            existingInventory.setQty(existingInventory.getQty() + inventoryRequest.getQty());

            ProcessOrderUtility.updateInventory(existingInventory);
        } else {

            ProcessOrderUtility.createRecordInInventoryService(inventoryRequest);
        }
    }

    private void createOrUpdateDone(ProcessOrderCompleteRequest.BatchDetails completebatch, BatchNoHeader batchNoHeader, BigDecimal quantityToProcess,
                                    BatchNoInWork batchNoInWork, boolean isProcessResource) throws Exception {
        BigDecimal totalScrapQty = batchNoScrapService.getTotalScrapQty(completebatch.getSite(), completebatch.getOrderNumber(), completebatch.getBatchNumber());
        BatchNoDone existingBatchDone = ProcessOrderUtility.getBatchNoDoneDetails(completebatch.getSite(), batchNoHeader.getHandle(), completebatch.getOrderNumber(),
                completebatch.getPhase(), completebatch.getOperation(), completebatch.getResource());

        if (existingBatchDone != null) {
            existingBatchDone.setQtyDone(existingBatchDone.getQtyDone().add(quantityToProcess));
            existingBatchDone.setScrapQuantityBaseUom(totalScrapQty);
            existingBatchDone.setScrapQuantityMeasuredUom(totalScrapQty);
            ProcessOrderUtility.updateBatchNoDone(existingBatchDone);
        } else {
            ProcessOrderUtility.createBatchNoDone(completebatch, isProcessResource, batchNoInWork, quantityToProcess, totalScrapQty);
        }
    }

    private void createOrUpdateBatchNoPhaseProgess(ProcessOrderCompleteRequest.BatchDetails completebatch, BatchNoHeader batchNoHeader, BigDecimal quantityToProcess,
                                                   BatchNoInWork batchNoInWork, boolean isProcessResource) throws Exception {
        BatchNoPhaseProgress existingBatchNoPhaseRecord;

        if (completebatch.getOrderNumber() != null && !completebatch.getOrderNumber().isEmpty()) {
            existingBatchNoPhaseRecord = ProcessOrderUtility.getBatchNoPhaseProgressDetailsByOrderNumber(completebatch.getSite(), completebatch.getBatchNumber(), completebatch.getMaterial(), completebatch.getOrderNumber(), completebatch.getMaterialVersion());
        } else {
            existingBatchNoPhaseRecord = ProcessOrderUtility.getBatchNoPhaseProgressDetailsByBatchNoHeaderBO(completebatch.getSite(), completebatch.getBatchNumber(), completebatch.getMaterial(), completebatch.getMaterialVersion(), batchNoHeader.getHandle());
        }
        if (existingBatchNoPhaseRecord != null) {

            PhaseProgress existingPhaseProgress = existingBatchNoPhaseRecord.getPhaseProgress().stream()
                    .filter(pp -> pp.getPhase().equals(completebatch.getPhase()) && pp.getOperation().equals(completebatch.getOperation()))
                    .findFirst()
                    .orElse(null);

            if (existingPhaseProgress != null) {

                existingPhaseProgress.setScrapQuantity((existingPhaseProgress.getScrapQuantity() != null ? existingPhaseProgress.getScrapQuantity() : BigDecimal.ZERO)
                        .add(completebatch.getScrapQuantity() != null ? completebatch.getScrapQuantity() : BigDecimal.ZERO));

                existingPhaseProgress.setCompleteQuantityBaseUom(
                        (existingPhaseProgress.getCompleteQuantityBaseUom() != null ? existingPhaseProgress.getCompleteQuantityBaseUom() : BigDecimal.ZERO)
                                .add(quantityToProcess)
                );

                existingPhaseProgress.setCompleteQuantityMeasuredUom(
                        (existingPhaseProgress.getCompleteQuantityMeasuredUom() != null ? existingPhaseProgress.getCompleteQuantityMeasuredUom() : BigDecimal.ZERO)
                                .add(quantityToProcess)
                );

                existingPhaseProgress.setScrapQuantityBaseUom(
                        (existingPhaseProgress.getScrapQuantityBaseUom() != null ? existingPhaseProgress.getScrapQuantityBaseUom() : BigDecimal.ZERO)
                                .add(completebatch.getScrapQuantity() != null ? completebatch.getScrapQuantity() : BigDecimal.ZERO)
                );

                existingPhaseProgress.setScrapQuantityMeasuredUom(
                        (existingPhaseProgress.getScrapQuantityMeasuredUom() != null ? existingPhaseProgress.getScrapQuantityMeasuredUom() : BigDecimal.ZERO)
                                .add(completebatch.getScrapQuantity() != null ? completebatch.getScrapQuantity() : BigDecimal.ZERO)
                );

                ProcessOrderUtility.updateBatchNoPhaseProgess(existingBatchNoPhaseRecord);

            } else {

                PhaseProgress newPhaseProgress = buildPhaseProgress("complete", completebatch, quantityToProcess, batchNoInWork, BigDecimal.ZERO, completebatch.getPhase(), completebatch.getOperation());
                existingBatchNoPhaseRecord.getPhaseProgress().add(newPhaseProgress);
                ProcessOrderUtility.updateBatchNoPhaseProgess(existingBatchNoPhaseRecord);
            }
        } else {
            ProcessOrderUtility.createBatchNoPhaseProgress(completebatch,isProcessResource,batchNoInWork,quantityToProcess);
        }
    }

    private void createOrUpdateBatchNoStartPhaseProgess(ProcessOrderStartRequest.BatchDetails startbatch, BatchNoHeader batchNoHeader,
                                                   BatchNoInQueue batchNoInQueue, boolean isProcessResource, BigDecimal startQty) throws Exception {
        BatchNoPhaseProgress existingBatchNoPhaseRecord;

        if (startbatch.getOrderNumber() != null && !startbatch.getOrderNumber().isEmpty()) {
            existingBatchNoPhaseRecord = ProcessOrderUtility.getBatchNoPhaseProgressDetailsByOrderNumber(startbatch.getSite(), startbatch.getBatchNumber(), startbatch.getMaterial(), startbatch.getOrderNumber(), startbatch.getMaterialVersion());
        } else {
            existingBatchNoPhaseRecord = ProcessOrderUtility.getBatchNoPhaseProgressDetailsByBatchNoHeaderBO(startbatch.getSite(), startbatch.getBatchNumber(), startbatch.getMaterial(), startbatch.getMaterialVersion(), batchNoHeader.getHandle());
        }

        if (existingBatchNoPhaseRecord != null) {

            PhaseProgress existingPhaseProgress = existingBatchNoPhaseRecord.getPhaseProgress().stream()
                    .filter(pp -> pp.getPhase().equals(startbatch.getPhase()) && pp.getOperation().equals(startbatch.getOperation()))
                    .findFirst()
                    .orElse(null);

            if (existingPhaseProgress != null) {

                existingPhaseProgress.setStartQuantityBaseUom((existingPhaseProgress.getStartQuantityBaseUom() != null ? existingPhaseProgress.getStartQuantityBaseUom() : BigDecimal.ZERO)
                                .add(startQty != null ? startQty : BigDecimal.ZERO));

                existingPhaseProgress.setStartQuantityMeasuredUom((existingPhaseProgress.getStartQuantityMeasuredUom() != null ? existingPhaseProgress.getStartQuantityMeasuredUom() : BigDecimal.ZERO)
                                .add(startQty != null ? startQty : BigDecimal.ZERO));

                ProcessOrderUtility.updateBatchNoPhaseProgess(existingBatchNoPhaseRecord);

            } else {

                PhaseProgress newPhaseProgress = buildPhaseProgress("start", null, BigDecimal.ZERO, null, startQty, startbatch.getPhase(), startbatch.getOperation());
                existingBatchNoPhaseRecord.getPhaseProgress().add(newPhaseProgress);
                ProcessOrderUtility.updateBatchNoPhaseProgess(existingBatchNoPhaseRecord);
            }
        } else {
            ProcessOrderUtility.createBatchNoPhaseProgressForStart(startbatch,isProcessResource,batchNoInQueue, startQty);
        }
    }

    public PhaseProgress buildPhaseProgress(String state, ProcessOrderCompleteRequest.BatchDetails completebatch, BigDecimal quantityToProcess, BatchNoInWork batchNoInWork, BigDecimal startQty, String phase, String operation) throws Exception {

        PhaseProgress.PhaseProgressBuilder newPhaseProgress = PhaseProgress.builder()
                .phase(phase)
                .operation(operation)
                .endTimestamp(LocalDateTime.now());

        if (state.equalsIgnoreCase("start")) {
            if (startQty != null) {
                newPhaseProgress.startQuantityBaseUom(startQty)
                        .startQuantityMeasuredUom(startQty);
            }
            newPhaseProgress.status("Start");
        }

        if (state.equalsIgnoreCase("complete")) {
            if (quantityToProcess != null) {
                newPhaseProgress.completeQuantityBaseUom(quantityToProcess)
                        .completeQuantityMeasuredUom(quantityToProcess);
            }
            if (completebatch != null) {
                if (completebatch.getScrapQuantity() != null) {
                    newPhaseProgress.scrapQuantity(completebatch.getScrapQuantity())
                            .scrapQuantityBaseUom(completebatch.getScrapQuantity())
                            .scrapQuantityMeasuredUom(completebatch.getScrapQuantity());
                }
                newPhaseProgress.status(Boolean.TRUE.equals(completebatch.getFinalReport()) ? "Complete" : "Start");
            }
            if (batchNoInWork != null) {
                newPhaseProgress.baseUom(batchNoInWork.getBaseUom())
                        .measuredUom(batchNoInWork.getMeasuredUom())
                        .startTimestamp(batchNoInWork.getQueuedTimestamp());
            }
        }

        return newPhaseProgress.build();
    }

    private BatchNoComplete createOrUpdateCompleteRecord(ProcessOrderCompleteRequest.BatchDetails completebatch, BatchNoHeader batchNoHeader, boolean isProcessResource, BigDecimal completeQty,
                                                         BigDecimal yieldQuantityBaseUom, BigDecimal yieldQuantityMeasuredUom, BatchNoInWork batchNoInWork) throws Exception {
        BatchNoComplete batchNoCompleteRecord;
        BatchNoComplete existingBatchComplete = ProcessOrderUtility.getBatchNoCompleteDetails(completebatch.getSite(), batchNoHeader.getHandle(), completebatch.getPhase(), completebatch.getOperation(), completebatch.getResource(), isProcessResource ? null : completebatch.getUser(), completebatch.getOrderNumber());

        if (existingBatchComplete != null) {

            existingBatchComplete.setQtyToComplete((existingBatchComplete.getQtyToComplete() != null ? existingBatchComplete.getQtyToComplete() : BigDecimal.ZERO)
                    .add(completeQty != null ? completeQty : BigDecimal.ZERO));

            existingBatchComplete.setScrapQuantityBaseUom((existingBatchComplete.getScrapQuantityBaseUom() != null ? existingBatchComplete.getScrapQuantityBaseUom() : BigDecimal.ZERO)
                    .add(completebatch.getScrapQuantity() != null ? completebatch.getScrapQuantity() : BigDecimal.ZERO));

            existingBatchComplete.setScrapQuantityMeasuredUom((existingBatchComplete.getScrapQuantityMeasuredUom() != null ? existingBatchComplete.getScrapQuantityMeasuredUom() : BigDecimal.ZERO)
                    .add(completebatch.getScrapQuantity() != null ? completebatch.getScrapQuantity() : BigDecimal.ZERO));

            existingBatchComplete.setYieldQuantityBaseUom((existingBatchComplete.getYieldQuantityBaseUom() != null ? existingBatchComplete.getYieldQuantityBaseUom() : BigDecimal.ZERO)
                    .add(completeQty != null ? completeQty : BigDecimal.ZERO));

            existingBatchComplete.setYieldQuantityMeasuredUom((existingBatchComplete.getYieldQuantityMeasuredUom() != null ? existingBatchComplete.getYieldQuantityMeasuredUom() : BigDecimal.ZERO)
                    .add(completeQty != null ? completeQty : BigDecimal.ZERO));

            batchNoCompleteRecord = ProcessOrderUtility.updateBatchNoComplete(existingBatchComplete);

        } else {
            batchNoCompleteRecord = ProcessOrderUtility.createBatchNoComplete(completebatch, batchNoInWork, isProcessResource,yieldQuantityBaseUom,yieldQuantityMeasuredUom,completeQty);
        }
        return batchNoCompleteRecord;
    }

    private boolean logProductionLogForComplete(ProcessOrderCompleteRequest.BatchDetails completebatch, Integer qty) throws Exception {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .site(completebatch.getSite())
                .eventType("completeSfcBatch")
                .userId(completebatch.getUser())
                .batchNo(completebatch.getOrderNumber() + "_" + completebatch.getBatchNumber())
                .orderNumber(completebatch.getOrderNumber())
                .operation(completebatch.getOperation())
                .phaseId(completebatch.getPhase())
                .workcenterId(completebatch.getWorkcenter())
                .resourceId(completebatch.getResource())
                .material(completebatch.getMaterial())
                .materialVersion(completebatch.getMaterialVersion())
                .shopOrderBO(completebatch.getOrderNumber())
                .qty(qty)
                .topic("production-log")
                .status("Active")
                .createdDatetime(LocalDateTime.now())
                .eventData(completebatch.getBatchNumber() + " Completed successfully")
                .build();

        return ProcessOrderUtility.productionLog(productionLogRequest);
    }

    private BatchNoScrapRequest buildScrapRequest(ProcessOrderCompleteRequest.BatchDetails completebatch, BatchNoInWork batchNoInWork, BigDecimal scrapQty, BigDecimal completeQtyBeforeScrap, BigDecimal completeQtyAfterScrap) {
        BatchNoScrapRequest.BatchNoScrapRequestBuilder builder = BatchNoScrapRequest.builder()
                .status("SCRAP")
                .workcenter("");

        if (completebatch != null) {
            builder.site(completebatch.getSite())
                    .batchNo(completebatch.getBatchNumber())
                    .phaseId(completebatch.getPhase())
                    .operation(completebatch.getOperation())
                    .resource(completebatch.getResource())
                    .reasonCode(completebatch.getReasonCode())
                    .user(completebatch.getUser());
            if (completebatch.getScrapQuantity() != null) {
                builder.comment("scrapped " + completebatch.getScrapQuantity() + " for batchNo " + completebatch.getBatchNumber());
            }
        }

        if (batchNoInWork != null) {
            builder.recipe(batchNoInWork.getRecipe())
                    .recipeVersion(batchNoInWork.getRecipeVersion())
                    .orderNumber(batchNoInWork.getOrderNumber())
                    .material(batchNoInWork.getMaterial())
                    .materialVersion(batchNoInWork.getMaterialVersion());
        }

        if (scrapQty != null) builder.scrapQuantity(scrapQty);
        if (completeQtyBeforeScrap != null) builder.theoreticalYield(completeQtyBeforeScrap);
        if (completeQtyAfterScrap != null) builder.actualYield(completeQtyAfterScrap);

        return builder.build();
    }

    private InventoryRequest createInventoryRequest(ProcessOrderCompleteRequest.BatchDetails  request, BigDecimal quantityToProcess) {

        InventoryRequest inventory = InventoryRequest.builder()
                .site(request.getSite())
                .inventoryId(request.getOrderNumber() + "_" + request.getBatchNumber())
                .batchNumber(request.getBatchNumber())
                .description("orderNumber: " + request.getOrderNumber() + "_batchNumber: " + request.getBatchNumber())
                .item(request.getMaterial())
                .version(request.getMaterialVersion())
                .receiveQty(0)
                .qty(quantityToProcess.doubleValue())
                .originalQty(quantityToProcess.doubleValue())
                .remainingQty(quantityToProcess.doubleValue())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .build();

        return inventory;
    }

    public void createOrUpdateBatchNoYield(ProcessOrderCompleteRequest.BatchDetails completeBatch, boolean isProcessResource, BatchNoInWork batchNoInWork, BigDecimal completeQtyAfterScrap, BigDecimal completeQtyBeforeScrap) {

        Optional<BatchNoYieldReportingRequest> existingBatchNoYieldOpt = batchNoYieldReportingRepository.findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(completeBatch.getSite(),
                completeBatch.getBatchNumber(), completeBatch.getPhase(),
                completeBatch.getOperation(), completeBatch.getResource(),1);

        BigDecimal scrapQuantity = completeBatch.getScrapQuantity() != null ? completeBatch.getScrapQuantity() : BigDecimal.ZERO;
        if (existingBatchNoYieldOpt.isPresent()) {
            // update
            BatchNoYieldReportingRequest existingBatchNoYield = existingBatchNoYieldOpt.get();

            existingBatchNoYield.setActualYieldBaseUom((existingBatchNoYield.getActualYieldBaseUom() != null ? existingBatchNoYield.getActualYieldBaseUom() : BigDecimal.ZERO).add(completeQtyAfterScrap));
            existingBatchNoYield.setActualYieldMeasuredUom((existingBatchNoYield.getActualYieldMeasuredUom() != null ? existingBatchNoYield.getActualYieldMeasuredUom() : BigDecimal.ZERO).add(completeQtyAfterScrap));
            existingBatchNoYield.setTheoreticalYieldBaseUom((existingBatchNoYield.getTheoreticalYieldBaseUom() != null ? existingBatchNoYield.getTheoreticalYieldBaseUom() : BigDecimal.ZERO).add(completeQtyBeforeScrap));
            existingBatchNoYield.setTheoreticalYieldMeasuredUom((existingBatchNoYield.getTheoreticalYieldMeasuredUom() != null ? existingBatchNoYield.getTheoreticalYieldMeasuredUom() : BigDecimal.ZERO).add(completeQtyBeforeScrap));
            existingBatchNoYield.setYieldVarianceBaseUom((existingBatchNoYield.getYieldVarianceBaseUom() != null ? existingBatchNoYield.getYieldVarianceBaseUom() : BigDecimal.ZERO).add(scrapQuantity));
            existingBatchNoYield.setYieldVarianceMeasuredUom((existingBatchNoYield.getYieldVarianceMeasuredUom() != null ? existingBatchNoYield.getYieldVarianceMeasuredUom() : BigDecimal.ZERO).add(scrapQuantity));
            existingBatchNoYield.setScrapQuantityBaseUom((existingBatchNoYield.getScrapQuantityBaseUom() != null ? existingBatchNoYield.getScrapQuantityBaseUom() : BigDecimal.ZERO).add(scrapQuantity));
            existingBatchNoYield.setScrapQuantityMeasuredUom((existingBatchNoYield.getScrapQuantityMeasuredUom() != null ? existingBatchNoYield.getScrapQuantityMeasuredUom() : BigDecimal.ZERO).add(scrapQuantity));
            existingBatchNoYield.setModifiedDateTime(LocalDateTime.now());

            batchNoYieldReportingService.update(existingBatchNoYield);
        } else {
            // create
            BatchNoYieldReportingRequest batchNoYieldReportingRequest = BatchNoYieldReportingRequest.builder()
                    .site(completeBatch.getSite())
                    .batchNo(completeBatch.getBatchNumber())
                    .phaseId(completeBatch.getPhase())
                    .operation(completeBatch.getOperation())
                    .resource(completeBatch.getResource())
                    .recipe(batchNoInWork.getRecipe())
                    .recipeVersion(batchNoInWork.getRecipeVersion())
                    .theoreticalYieldBaseUom(completeQtyBeforeScrap)
                    .theoreticalYieldMeasuredUom(completeQtyBeforeScrap)
                    .baseUom(batchNoInWork.getBaseUom())
                    .measuredUom(batchNoInWork.getMeasuredUom())
                    .reportTimestamp(batchNoInWork.getQueuedTimestamp())
                    .actualYieldBaseUom(completeQtyAfterScrap)
                    .actualYieldMeasuredUom(completeQtyAfterScrap)
                    .yieldVarianceBaseUom(scrapQuantity)
                    .yieldVarianceMeasuredUom(scrapQuantity)
                    .user(isProcessResource ? null : completeBatch.getUser())
                    .scrapQuantityBaseUom(scrapQuantity)
                    .scrapQuantityMeasuredUom(scrapQuantity)
                    .orderNumber(completeBatch.getOrderNumber())
                    .active(1)
                    .build();

            batchNoYieldReportingService.create(batchNoYieldReportingRequest);
        }
    }

    @Hookable
    public String testHookableProcessStart(ProcessOrderStartRequest request) throws Exception {
        // For testing, simply return a string using an input field from the request.
        String batchNumbers = "";
        if (request != null && request.getStartBatches() != null && !request.getStartBatches().isEmpty()) {
            batchNumbers = request.getStartBatches().get(0).getBatchNumber();
        }
        return "Test method invoked. BatchNumber = " + batchNumbers;
    }


}