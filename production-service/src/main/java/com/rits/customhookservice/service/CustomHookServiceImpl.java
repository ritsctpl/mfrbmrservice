package com.rits.customhookservice.service;

import com.rits.Utility.ProcessOrderUtility;
import com.rits.batchnoheader.exception.BatchNoHeaderException;
import com.rits.batchnoheader.model.BatchNoHeader;
import com.rits.batchnoinqueue.exception.BatchNoInQueueException;
import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinwork.dto.BatchNoInWorkRequest;
import com.rits.batchnoinwork.dto.BatchNoWorkQtyResponse;
import com.rits.batchnoinwork.exception.BatchNoInWorkException;
import com.rits.batchnoinwork.model.BatchNoInWork;
import com.rits.batchnoinwork.repository.BatchNoInWorkRepository;
import com.rits.batchnoinwork.service.BatchNoInWorkService;
import com.rits.customhookservice.exception.CustomHookException;
import com.rits.dccollect.dto.CustomData;
import com.rits.dccollect.dto.Item;
import com.rits.inventoryservice.service.InventoryService;
import com.rits.lineclearancelogservice.dto.LineClearanceLogRequest;
import com.rits.lineclearancelogservice.service.LineClearanceLogService;
import com.rits.lineclearanceservice.model.RetrieveLineClearanceLogResponse;
import com.rits.logbuyoffservice.dto.AttachmentDetailsRequest;
import com.rits.logbuyoffservice.model.BuyoffLog;
import com.rits.logbuyoffservice.service.LogBuyOffService;
import com.rits.processorderrelease.dto.ProcessOrderReleaseRequest;
import com.rits.processorderstateservice.dto.ProcessOrderCompleteRequest;
import com.rits.processorderstateservice.dto.ProcessOrderStartRequest;
import com.rits.processorderstateservice.exception.ProcessOrderStateException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomHookServiceImpl implements CustomHookService {
    private final WebClient.Builder webClientBuilder;
    @Value("${item-service.url}/retrieve")
    private String retrieveItemUrl;
    @Autowired
    private LogBuyOffService logBuyOffService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private LineClearanceLogService lineClearanceLogService;
    @Autowired
    private BatchNoInWorkService batchNoInWorkService;
    @Override
    public void checkLineclearance(ProcessOrderStartRequest request) {
        LineClearanceLogRequest lineClearanceLogRequest = new LineClearanceLogRequest();

        for (ProcessOrderStartRequest.BatchDetails batch : request.getStartBatches()) {
            //lineClearanceLogRequest is being set
            lineClearanceLogRequest.setSite(batch.getSite());
            lineClearanceLogRequest.setBatchNo(batch.getBatchNumber());
            lineClearanceLogRequest.setResourceId(batch.getResource());
            lineClearanceLogRequest.setWorkCenterId(batch.getWorkcenter());

            //Retrieving LineClearanceLogResponsesList
            List<RetrieveLineClearanceLogResponse> lineClearanceLogResponsesList = lineClearanceLogService.retrieveLineClearanceList(lineClearanceLogRequest);
            for(RetrieveLineClearanceLogResponse retrieveLineClearanceLogResponse : lineClearanceLogResponsesList){
                if(retrieveLineClearanceLogResponse.getStatus().equalsIgnoreCase("Start") || retrieveLineClearanceLogResponse.getStatus().equalsIgnoreCase("New")){
                    throw new ProcessOrderStateException(9014);
                }
            }
        }
    }

    @Override
    public void checkBatchInWork(ProcessOrderStartRequest request) {
        BatchNoInWorkRequest batchNoInWorkRequest = new BatchNoInWorkRequest();
        for (ProcessOrderStartRequest.BatchDetails batch : request.getStartBatches()) {
            if(batchNoInWorkService.existsBySiteAndActiveAndResource(batch.getSite(),1,batch.getResource()))throw new ProcessOrderStateException(9016);
        }
    }

    @Override
    public void checkBuyoff(ProcessOrderStartRequest request) {

        AttachmentDetailsRequest attachmentDetailsRequest = new AttachmentDetailsRequest();

        for (ProcessOrderStartRequest.BatchDetails batch : request.getStartBatches()) {

            //attachmentDetailsRequest is being set
            attachmentDetailsRequest.setSite(batch.getSite());
            attachmentDetailsRequest.setBatchNo(batch.getBatchNumber());
            attachmentDetailsRequest.setOperation(batch.getOperation());
            attachmentDetailsRequest.setOperationVersion("#");//operationVersion is not available in request
            attachmentDetailsRequest.setOrderNumber(batch.getOrderNumber());
            attachmentDetailsRequest.setItem(batch.getMaterial());
            attachmentDetailsRequest.setItemVersion(batch.getMaterialVersion());
            attachmentDetailsRequest.setResource(batch.getResource());
            attachmentDetailsRequest.setQuantityRequired(batch.getQuantity() != null ? batch.getQuantity().toString() : "0"); // Assuming quantity is required

            //Retrieving BuyoffLog List
            List<BuyoffLog> buyoffLogList = logBuyOffService.getListOfBuyoff(attachmentDetailsRequest);

            //iterating through the lists
            for(BuyoffLog buyoffLog : buyoffLogList){
                if(buyoffLog.getBuyOffAction().equalsIgnoreCase("O") || buyoffLog.getBuyOffAction().equalsIgnoreCase("R") || buyoffLog.getBuyOffAction().equalsIgnoreCase("N")){
                    throw new ProcessOrderStateException(9013);
                }
            }
        }
    }
    @Override
    public void orderRelease(ProcessOrderReleaseRequest request) {

        for (ProcessOrderReleaseRequest.OrderDetails order : request.getOrders()) {
            boolean exists = inventoryService.isBatchNumberExists(order.getBatchNumber(), order.getSite());

            // If orderType does NOT end with "PRD" and batch number does NOT exist, throw an exception
            if (!order.getOrderType().endsWith("PRD") && !exists) {
                throw new CustomHookException(9015, order.getBatchNumber());
            }
        }

    }

    @Override
    public void checkTolerance(ProcessOrderCompleteRequest request) throws Exception{
        for (ProcessOrderCompleteRequest.BatchDetails completebatch : request.getCompleteBatches()) {
            BatchNoHeader batchNoHeader = getBatchHeaderDetails(completebatch);
            BigDecimal completeQtyBeforeScrap = completebatch.getQuantity();
            boolean isProcessResource = ProcessOrderUtility.getResourceDetails(completebatch.getSite(), completebatch.getResource());
            BatchNoInWork batchNoInWork = getBatchInWorkDetails(completebatch, batchNoHeader);
            BatchNoInQueue batchNoInQueue = ProcessOrderUtility.getBatchNoInQueueDetails(completebatch.getSite(), batchNoHeader.getHandle(), completebatch.getPhase(), completebatch.getOperation());
            if(batchNoInQueue != null && Boolean.TRUE.equals(completebatch.getFinalReport())) {
                throw new BatchNoInQueueException(3813);
            }
            boolean isToleranceValid = isQuantityWithinTolerance(completebatch , batchNoHeader, completeQtyBeforeScrap , isProcessResource, batchNoInWork, batchNoInQueue, completebatch.getFinalReport());
            if (!isToleranceValid)
                throw new ProcessOrderStateException(339);

        }
    }

    private boolean isQuantityWithinTolerance(ProcessOrderCompleteRequest.BatchDetails completebatch, BatchNoHeader batchNoHeader, BigDecimal completeQtyBeforeScrap,
                                             boolean isProcessResource, BatchNoInWork batchNoInWork, BatchNoInQueue batchNoInQueue, boolean finalReport) throws Exception {

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
        if (!finalReport && batchNoInWork.getQtyToComplete().compareTo(providedQty) > 0)
            return providedQty.compareTo(maxToleranceQty) <= 0;
        if (!finalReport && batchNoInWork.getQtyToComplete().compareTo(providedQty) < 0)
            return providedQty.compareTo(minToleranceQty) >= 0 && providedQty.compareTo(maxToleranceQty) <= 0;
        if(!finalReport) return providedQty.compareTo(maxToleranceQty) <= 0;
        if (maxToleranceQty != null) {
            return providedQty.compareTo(minToleranceQty) >= 0 && providedQty.compareTo(maxToleranceQty) <= 0;
        }
        return false;
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

}
