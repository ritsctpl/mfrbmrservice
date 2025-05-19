package com.rits.Utility;

import com.rits.batchnocomplete.dto.BatchNoCompleteDTO;
import com.rits.batchnocomplete.model.BatchNoComplete;
import com.rits.batchnocomplete.model.BatchNoCompleteMsgModel;
import com.rits.batchnocomplete.service.BatchNoCompleteService;
import com.rits.batchnodoneservice.dto.BatchNoDoneRequest;
import com.rits.batchnodoneservice.model.BatchNoDone;
import com.rits.batchnodoneservice.service.BatchNoDoneService;
import com.rits.batchnoheader.model.BatchNoHeader;
import com.rits.batchnoheader.service.BatchNoHeaderService;
import com.rits.batchnoinqueue.dto.BatchNoInQueueRequest;
import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinqueue.service.BatchNoInQueueService;
import com.rits.batchnoinwork.dto.BatchNoInWorkRequest;
import com.rits.batchnoinwork.model.BatchNoInWork;
import com.rits.batchnoinwork.service.BatchNoInWorkService;
import com.rits.batchnophaseprogressservice.dto.BatchNoPhaseProgressRequest;
import com.rits.batchnophaseprogressservice.dto.PhaseProgress;
import com.rits.batchnophaseprogressservice.model.BatchNoPhaseProgress;
import com.rits.batchnophaseprogressservice.service.BatchNoPhaseProgressService;
import com.rits.batchnorecipeheaderservice.model.BatchNoRecipeHeader;
import com.rits.batchnorecipeheaderservice.model.MessageModel;
import com.rits.batchnorecipeheaderservice.service.BatchNoRecipeHeaderService;
import com.rits.batchnoyieldreportingservice.dto.BatchNoYieldReportingRequest;
import com.rits.batchnoyieldreportingservice.service.BatchNoYieldReportingService;
import com.rits.processorderstateservice.dto.InventoryRequest;
import com.rits.processorderstateservice.model.Inventory;
import com.rits.processorderservice.dto.ProcessOrderResponse;
import com.rits.processorderservice.service.ProcessOrderService;
import com.rits.processorderstateservice.dto.*;
import com.rits.processorderstateservice.exception.ProcessOrderStateException;
import com.rits.processorderstateservice.model.Operation;
import com.rits.processorderstateservice.model.Resource;
import com.rits.processorderstateservice.model.User;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import com.rits.productionlogservice.producer.ProductionLogProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProcessOrderUtility {

    // Static fields for services
    private static BatchNoHeaderService batchNoHeaderService;
    private static ProcessOrderService processOrderService;
    private static BatchNoInWorkService batchNoInWorkService;
    private static BatchNoInQueueService batchNoInQueueService;
    private static BatchNoRecipeHeaderService batchNoRecipeHeaderService;

    private static BatchNoYieldReportingService batchNoYieldReportingService;
    private static BatchNoDoneService batchNoDoneService;
    private static BatchNoPhaseProgressService batchNoPhaseProgressService;

    private static BatchNoCompleteService batchNoCompleteService;

    private static WebClient.Builder webClientBuilder;
    private static ApplicationEventPublisher eventPublisher;

    // Static URLs
    private static String getResourceDetailsUrl;
    private static String getOperationStatusUrl;
    private static String getUserStatusUrl;
    private static String getInventoryStatusUrl;
    private static String getUpdateInventoryStatusUrl;
    private static String getInventoryByIdStatusUrl;



    // Instance fields for DI
    @Autowired
    private BatchNoHeaderService batchNoHeaderServiceInstance;

    @Autowired
    private BatchNoRecipeHeaderService batchNoRecipeHeaderServiceInstance;

    @Autowired
    private ProcessOrderService processOrderServiceInstance;

    @Autowired
    private BatchNoInWorkService batchNoInWorkServiceInstance;

    @Autowired
    private ApplicationEventPublisher eventPublisherInstance;

    private final BatchNoYieldReportingService batchNoYieldReportingServiceInstance;

    @Autowired
    private BatchNoInQueueService batchNoInQueueServiceInstance;

    private final BatchNoDoneService batchNoDoneServiceInstance;

    private final BatchNoPhaseProgressService batchNoPhaseProgressServiceInstance;

    private final BatchNoCompleteService batchNoCompleteServiceInstance;

    @Autowired
    private WebClient.Builder webClientBuilderInstance;

    @Value("${resource-service.url}/retrieveByResource")
    private String resourceDetailsUrl;

    @Value("${operation-service.url}/retrieveOperationByCurrentVersion")
    private String operationStatusUrl;

    @Value("${user-service.url}/retrieveByUser")
    private String userStatusUrl;

    @Value("${inventory-service.url}/create")
    private String inventoryStatusUrl;

    @Value("${inventory-service.url}/update")
    private String updateInventoryStatusUrl;

    @Value("${inventory-service.url}/getInventoryById")
    private String inventoryByIdStatusUrl;

    // Predefined acceptable and non-acceptable statuses
    private static final List<String> ACCEPTABLE_STATUSES = Arrays.asList("RELEASABLE", "ACTIVE", "AVAILABLE", "PRODUCTIVE", "ENABLED");
    private static final List<String> NON_ACCEPTABLE_STATUSES = Arrays.asList("HOLD", "SCHEDULED_DOWN", "UNSCHEDULED_DOWN");

    @PostConstruct
    public void init() {
        // Assign Spring-managed instances to static fields
        batchNoHeaderService = batchNoHeaderServiceInstance;
        batchNoRecipeHeaderService = batchNoRecipeHeaderServiceInstance;
        processOrderService = processOrderServiceInstance;
        batchNoInWorkService = batchNoInWorkServiceInstance;
        batchNoInQueueService = batchNoInQueueServiceInstance;
        batchNoDoneService = batchNoDoneServiceInstance;
        batchNoPhaseProgressService = batchNoPhaseProgressServiceInstance;
        batchNoCompleteService = batchNoCompleteServiceInstance;
        batchNoYieldReportingService = batchNoYieldReportingServiceInstance;
        webClientBuilder = webClientBuilderInstance;
        getResourceDetailsUrl = resourceDetailsUrl;
        getOperationStatusUrl = operationStatusUrl;
        getUserStatusUrl = userStatusUrl;
        getInventoryStatusUrl = inventoryStatusUrl;
        getUpdateInventoryStatusUrl =updateInventoryStatusUrl;
        getInventoryByIdStatusUrl = inventoryByIdStatusUrl;
        eventPublisher = eventPublisherInstance;
    }

    public static void validateBatchStartDetails(ProcessOrderStartRequest.BatchDetails startBatch) {
        if(!StringUtils.hasText(startBatch.getSite())){
            throw new ProcessOrderStateException(7001);
        }
        if(!StringUtils.hasText(startBatch.getBatchNumber())){
            throw new ProcessOrderStateException(7002);
        }
        if(!StringUtils.hasText(startBatch.getPhase())){
            throw new ProcessOrderStateException(7005);
        }
        if(!StringUtils.hasText(startBatch.getOperation())){
            throw new ProcessOrderStateException(7006);
        }
        if(!StringUtils.hasText(startBatch.getResource())){
            throw new ProcessOrderStateException(7019);
        }
        if(!StringUtils.hasText(startBatch.getUser())){
            throw new ProcessOrderStateException(7012);
        }
        if(!StringUtils.hasText(startBatch.getMaterial())){
            throw new ProcessOrderStateException(7014);
        }
        if(!StringUtils.hasText(startBatch.getMaterialVersion())){
            throw new ProcessOrderStateException(7020);
        }
    }

    public static void validateBatchSignoffDetails(ProcessOrderSignoffRequest.BatchDetails signoffBatch) {
        if(!StringUtils.hasText(signoffBatch.getSite())){
            throw new ProcessOrderStateException(7001);
        }
        if(!StringUtils.hasText(signoffBatch.getBatchNumber())){
            throw new ProcessOrderStateException(7002);
        }
        if(!StringUtils.hasText(signoffBatch.getPhase())){
            throw new ProcessOrderStateException(7005);
        }
        if(!StringUtils.hasText(signoffBatch.getOperation())){
            throw new ProcessOrderStateException(7006);
        }
        if(!StringUtils.hasText(signoffBatch.getResource())){
            throw new ProcessOrderStateException(7019);
        }
        if(!StringUtils.hasText(signoffBatch.getUser())){
            throw new ProcessOrderStateException(7012);
        }
        if(!StringUtils.hasText(signoffBatch.getMaterial())){
            throw new ProcessOrderStateException(7014);
        }
        if(!StringUtils.hasText(signoffBatch.getMaterialVersion())){
            throw new ProcessOrderStateException(7020);
        }
    }
    public static void validateBatchCompleteDetails(ProcessOrderCompleteRequest.BatchDetails completeBatch) {
        if(!StringUtils.hasText(completeBatch.getSite())){
            throw new ProcessOrderStateException(7001);
        }
        if(!StringUtils.hasText(completeBatch.getBatchNumber())){
            throw new ProcessOrderStateException(7002);
        }
        if(!StringUtils.hasText(completeBatch.getPhase())){
            throw new ProcessOrderStateException(7005);
        }
        if(!StringUtils.hasText(completeBatch.getOperation())){
            throw new ProcessOrderStateException(7006);
        }
        if(!StringUtils.hasText(completeBatch.getResource())){
            throw new ProcessOrderStateException(7019);
        }
        if(!StringUtils.hasText(completeBatch.getMaterial())){
            throw new ProcessOrderStateException(7014);
        }
        if(!StringUtils.hasText(completeBatch.getMaterialVersion())){
            throw new ProcessOrderStateException(7020);
        }
        if(!StringUtils.hasText(completeBatch.getUom())){
            throw new ProcessOrderStateException(7023);
        }
        if (completeBatch.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new ProcessOrderStateException(7033);
        }

    }
    // Static methods using the services

    public static boolean isResourceStatusAcceptable(String site, String resource) {
        ResourceRequest resourceStatusRequest = ResourceRequest.builder()
                .site(site)
                .resource(resource)
                .build();

        Resource resourceStatus = webClientBuilder.build()
                .post()
                .uri(getResourceDetailsUrl)
                .bodyValue(resourceStatusRequest)
                .retrieve()
                .bodyToMono(Resource.class)
                .block();

        return resourceStatus != null && isStatusAcceptable(resourceStatus.getStatus());
    }

    public static boolean isOperationStatusAcceptable(String site, String operation) {
        OperationRequest operationStatusRequest = OperationRequest.builder()
                .site(site)
                .operation(operation)
                .build();

        Operation operationStatus = webClientBuilder.build()
                .post()
                .uri(getOperationStatusUrl)
                .bodyValue(operationStatusRequest)
                .retrieve()
                .bodyToMono(Operation.class)
                .block();

        return operationStatus != null && isStatusAcceptable(operationStatus.getStatus());
    }

    public static boolean isBatchStatusAcceptable(String site, String batchNo) {
        BatchNoHeader batchStatus = batchNoHeaderService.getBySiteAndBatchNo(site, batchNo);
        return batchStatus != null && isStatusAcceptable(batchStatus.getStatus());
    }

    public static boolean isBatchRecipeStatusAcceptable(String site, String batchNo) {
        BatchNoRecipeHeader batchRecipeStatus = batchNoRecipeHeaderService.getBySiteAndBatchNo(site, batchNo);
        return batchRecipeStatus != null && isStatusAcceptable(batchRecipeStatus.getStatus());
    }

    public static boolean isProcessOrderStatusAcceptable(String site, String orderNumber) {
        ProcessOrderResponse processOrderStatus = processOrderService.getBySiteAndOrderNumber(site, orderNumber);
        return processOrderStatus != null && isStatusAcceptable(processOrderStatus.getStatus());
    }

    public static boolean isUserStatusAcceptable(String user) {
        UserRequest userStatusRequest = UserRequest.builder()
                .user(user)
                .build();

        User userStatus = webClientBuilder.build()
                .post()
                .uri(getUserStatusUrl)
                .bodyValue(userStatusRequest)
                .retrieve()
                .bodyToMono(User.class)
                .block();

        return userStatus != null && isStatusAcceptable(userStatus.getStatus());
    }

    //Check if the work center assignment is valid for the user.
    public static boolean isWorkcenterAssignedToUser(String user, String workCenter) {
        UserRequest workcenterAssignedToUserRequest = UserRequest.builder()
                .user(user)
                .build();

        User response = webClientBuilder.build()
                .post()
                .uri(getUserStatusUrl)
                .bodyValue(workcenterAssignedToUserRequest)
                .retrieve()
                .bodyToMono(User.class)
                .block();

        if (response == null || response.getWorkCenters() == null) {
            return false;
        }

        return response.getWorkCenters().stream()
                .anyMatch(workCenterObj -> workCenterObj.getWorkCenter().equals(workCenter));
    }

    // Get processResource from resource service
    public static boolean getResourceDetails(String site, String resource) {

        ResourceRequest resourceRequest = ResourceRequest.builder()
                .site(site)
                .resource(resource)
                .build();

        Resource resourceDetails = webClientBuilder.build()
                .post()
                .uri(getResourceDetailsUrl)
                .bodyValue(resourceRequest)
                .retrieve()
                .bodyToMono(Resource.class)
                .block();

        return resourceDetails != null && resourceDetails.isProcessResource();
    }

    public static void createRecordInInventoryService(InventoryRequest inventoryRequest) {
        Inventory inventory =  webClientBuilder.build()
                .post()
                .uri(getInventoryStatusUrl)
                .bodyValue(inventoryRequest)
                .retrieve()
                .bodyToMono(Inventory.class)
                .block();
    }

    public static void updateInventory(InventoryRequest inventoryRequest) {
        Inventory inventory =  webClientBuilder.build()
                .post()
                .uri(getUpdateInventoryStatusUrl)
                .bodyValue(inventoryRequest)
                .retrieve()
                .bodyToMono(Inventory.class)
                .block();
    }
    public static InventoryRequest getInventoryById(InventoryRequest inventoryRequest) {
        return webClientBuilder.build()
                .post()
                .uri(getInventoryByIdStatusUrl)
                .bodyValue(inventoryRequest)
                .retrieve()
                .bodyToMono(InventoryRequest.class)
                .block();
    }

    public static BatchNoHeader getBatchNoHeaderDetails(String site, String batchNumber, String orderNumber, String material, String materialVersion) {
        return batchNoHeaderService.getBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersion(site, batchNumber, orderNumber, material, materialVersion);
    }

    public static BatchNoInQueue getBatchNoInQueueDetails(String site, String batchNoHeaderBO, String phaseId, String operation) {
        return batchNoInQueueService.getBySiteAndBatchNoHeaderAndPhaseAndOperation(site, batchNoHeaderBO, phaseId, operation);
    }

    public static BatchNoInWork getBatchInWorkDetails(String site, String batchNumber, String phaseId, String operation, String resource, String user, String orderNo) {
        return batchNoInWorkService.getBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUser(site, batchNumber, phaseId, operation, resource, user, orderNo);
    }

    public static BatchNoInWork getBatchNoInWorkDetails(String site, String batchNoHeaderBO, String phaseId, String operation, String resource, String user) {

        return batchNoInWorkService.getBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUser(site, batchNoHeaderBO, phaseId, operation,resource, user);
    }

    public static BatchNoDone getBatchNoDoneDetails(String site, String batchNoHeaderBO, String orderNumber, String phaseId, String operation, String resource) {

        return batchNoDoneService.getBySiteAndBatchNoHeaderBOAndOrderNoAndPhaseIdAndOperationAndResource(site, batchNoHeaderBO,orderNumber, phaseId, operation,resource);
    }

    public static BatchNoRecipeHeader getBatchNoRecipeHeaderDetails(String site, String batchNumber, String orderNumber, String material, String materialVersion) {
        return batchNoRecipeHeaderService.getBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersion(site, batchNumber, orderNumber, material, materialVersion);
    }

    public static BatchNoPhaseProgress getBatchNoPhaseProgressDetailsByOrderNumber(String site, String batchNumber,String material, String orderNumber, String materialVersion) {
        return batchNoPhaseProgressService.getBySiteAndBatchNoAndMaterialAndOrderNoAndMaterialVersion(site, batchNumber,material, orderNumber, materialVersion);
    }

    public static BatchNoPhaseProgress getBatchNoPhaseProgressDetailsByBatchNoHeaderBO(String site, String batchNumber,String material, String materialVersion, String batchNoHeaderBO) {
        return batchNoPhaseProgressService.getBySiteAndBatchNoAndMaterialAndMaterialVersionAndBatchNoHeaderBO(site, batchNumber,material, materialVersion,batchNoHeaderBO);
    }

    public static BatchNoComplete getBatchNoCompleteDetails(String site,String batchNoHeaderBO, String phaseId, String operation,String resource, String user, String orderNo){
        return batchNoCompleteService.getBySiteAndBatchNoHeaderBOAndPhaseIdAndOperationAndResourceAndUser(site,batchNoHeaderBO,phaseId,operation,resource,user, orderNo);
    }


    public static boolean productionLog(ProductionLogRequest productionLogRequest) throws Exception{
        eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));
        return true;
    }

    public static void createBatchInWork(ProcessOrderStartRequest.BatchDetails batch, boolean isProcessResource, BatchNoInQueue batchNoInQueue) throws Exception {
        BatchNoInWorkRequest.BatchNoInWorkRequestBuilder builder = BatchNoInWorkRequest.builder()
                .active(1);

        if (batch != null) {
            builder.site(batch.getSite())
                    .batchNo(batch.getBatchNumber())
                    .material(batch.getMaterial())
                    .phaseId(batch.getPhase())
                    .operation(batch.getOperation())
                    .resource(batch.getResource())
                    .workcenter(batch.getWorkcenter())
                    .orderNumber(batch.getOrderNumber());
            if (!isProcessResource) {
                builder.user(batch.getUser());
            }
            if (batch.getQuantity() == null || batch.getQuantity().toString().trim().isEmpty() || batch.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                builder.qtyToComplete(batchNoInQueue != null ? batchNoInQueue.getQtyInQueue() : null);
            } else {
                builder.qtyToComplete(batch.getQuantity());
            }
        }

        if (batchNoInQueue != null) {
            builder.dateTime(batchNoInQueue.getDateTime())
                    .materialVersion(batchNoInQueue.getMaterialVersion())
                    .recipe(batchNoInQueue.getRecipe())
                    .recipeVersion(batchNoInQueue.getRecipeVersion())
                    .quantityBaseUom(batchNoInQueue.getQuantityBaseUom())
                    .quantityMeasuredUom(batchNoInQueue.getQuantityMeasuredUom())
                    .baseUom(batchNoInQueue.getBaseUom())
                    .measuredUom(batchNoInQueue.getMeasuredUom())
                    .queuedTimestamp(batchNoInQueue.getQueuedTimestamp())
                    .qualityApproval(batchNoInQueue.isQualityApproval());
        }

        BatchNoInWorkRequest createBatchNoInWorkRequest = builder.build();
        batchNoInWorkService.createBatchNoInWork(createBatchNoInWorkRequest);
    }

    public static void updateBatchInWork(BatchNoInWork batchNoInWork) throws Exception {

        BatchNoInWorkRequest updateBatchNoInWorkRequest = BatchNoInWorkRequest.builder()
                .site(batchNoInWork.getSite())
                .dateTime(batchNoInWork.getDateTime())
                .batchNo(batchNoInWork.getBatchNo())
                .material(batchNoInWork.getMaterial())
                .materialVersion(batchNoInWork.getMaterialVersion())
                .recipe(batchNoInWork.getRecipe())
                .recipeVersion(batchNoInWork.getRecipeVersion())
                .phaseId(batchNoInWork.getPhaseId())
                .operation(batchNoInWork.getOperation())
                .quantityBaseUom(batchNoInWork.getQuantityBaseUom())
                .quantityMeasuredUom(batchNoInWork.getQuantityMeasuredUom())
                .resource(batchNoInWork.getResource())
                .workcenter(batchNoInWork.getWorkcenter())
                .baseUom(batchNoInWork.getBaseUom())
                .user(batchNoInWork.getUser())
                .qtyToComplete(batchNoInWork.getQtyToComplete())
                .orderNumber(batchNoInWork.getOrderNumber())
                .qualityApproval(batchNoInWork.isQualityApproval())
                .measuredUom(batchNoInWork.getMeasuredUom())
                .queuedTimestamp(batchNoInWork.getQueuedTimestamp())
                .build();

        batchNoInWorkService.updateBatchNoInWork(updateBatchNoInWorkRequest);
    }

    public static void createBatchInQueueSignoff(ProcessOrderSignoffRequest.BatchDetails signoffBatch, boolean isProcessResource, BatchNoInWork batchNoInWork) throws Exception {

        BatchNoInQueueRequest.BatchNoInQueueRequestBuilder builder = BatchNoInQueueRequest.builder()
                .active(1);

        if (signoffBatch != null) {
            builder.site(signoffBatch.getSite())
                    .batchNo(signoffBatch.getBatchNumber())
                    .material(signoffBatch.getMaterial())
                    .materialVersion(signoffBatch.getMaterialVersion())
                    .phaseId(signoffBatch.getPhase())
                    .operation(signoffBatch.getOperation())
                    .resource(signoffBatch.getResource())
                    .workcenter(signoffBatch.getWorkcenter())
                    .orderNumber(signoffBatch.getOrderNumber());
            if (!isProcessResource) {
                builder.user(signoffBatch.getUser());
            }
            if (signoffBatch.getQuantity() != null && !signoffBatch.getQuantity().toString().trim().isEmpty() && signoffBatch.getQuantity().compareTo(BigDecimal.ZERO) != 0) {
                builder.qtyInQueue(signoffBatch.getQuantity());
            }
        }

        if (batchNoInWork != null) {
            builder.dateTime(batchNoInWork.getDateTime())
                    .recipe(batchNoInWork.getRecipe())
                    .recipeVersion(batchNoInWork.getRecipeVersion())
                    .quantityBaseUom(batchNoInWork.getQuantityBaseUom())
                    .quantityMeasuredUom(batchNoInWork.getQuantityMeasuredUom())
                    .baseUom(batchNoInWork.getBaseUom())
                    .measuredUom(batchNoInWork.getMeasuredUom())
                    .queuedTimestamp(batchNoInWork.getQueuedTimestamp())
                    .qualityApproval(batchNoInWork.isQualityApproval());
            if (signoffBatch == null || signoffBatch.getQuantity() == null || signoffBatch.getQuantity().toString().trim().isEmpty() || signoffBatch.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                builder.qtyInQueue(batchNoInWork.getQtyToComplete());
            }
        }

        BatchNoInQueueRequest createBatchNoInQueueRequest = builder.build();
        batchNoInQueueService.createBatchNoInQueue(createBatchNoInQueueRequest);
    }

    public static void updateBatchInQueue(BatchNoInQueue batchNoInQueue) throws Exception {

        BatchNoInQueueRequest updateBatchNoInQueueRequst = BatchNoInQueueRequest.builder()
                .site(batchNoInQueue.getSite())
                .dateTime(batchNoInQueue.getDateTime())
                .batchNo(batchNoInQueue.getBatchNo())
                .material(batchNoInQueue.getMaterial())
                .materialVersion(batchNoInQueue.getMaterialVersion())
                .recipe(batchNoInQueue.getRecipe())
                .recipeVersion(batchNoInQueue.getRecipeVersion())
                .phaseId(batchNoInQueue.getPhaseId())
                .operation(batchNoInQueue.getOperation())
                .quantityBaseUom(batchNoInQueue.getQuantityBaseUom())
                .quantityMeasuredUom(batchNoInQueue.getQuantityMeasuredUom())
                .baseUom(batchNoInQueue.getBaseUom())
                .measuredUom(batchNoInQueue.getMeasuredUom())
                .queuedTimestamp(batchNoInQueue.getQueuedTimestamp())
                .resource(batchNoInQueue.getResource())
                .workcenter(batchNoInQueue.getWorkcenter())
                .user(batchNoInQueue.getUser())
                .qtyToComplete(batchNoInQueue.getQtyToComplete())
                .qtyInQueue(batchNoInQueue.getQtyInQueue())
                .orderNumber(batchNoInQueue.getOrderNumber())
                .qualityApproval(batchNoInQueue.isQualityApproval())
                .build();

        batchNoInQueueService.updateBatchNoInQueue(updateBatchNoInQueueRequst);
    }

    public static void updateBatchNoDone(BatchNoDone batchNoDone) throws Exception {

        BatchNoDoneRequest updateBatchNoDoneRequest = BatchNoDoneRequest.builder()
                .site(batchNoDone.getSite())
                .dateTime(batchNoDone.getDateTime())
                .batchNo(batchNoDone.getBatchNo())
                .material(batchNoDone.getMaterial())
                .materialVersion(batchNoDone.getMaterialVersion())
                .recipe(batchNoDone.getRecipe())
                .recipeVersion(batchNoDone.getRecipeVersion())
                .phaseId(batchNoDone.getPhaseId())
                .operation(batchNoDone.getOperation())
                .quantityBaseUom(batchNoDone.getQuantityBaseUom())
                .quantityMeasuredUom(batchNoDone.getQuantityMeasuredUom())
                .resource(batchNoDone.getResource())
                .workcenter(batchNoDone.getWorkcenter())
                .baseUom(batchNoDone.getBaseUom())
                .user(batchNoDone.getUser())
                .qtyDone(batchNoDone.getQtyDone())
                .doneQuantityBaseUom(batchNoDone.getDoneQuantityBaseUom())
                .doneQuantityMeasuredUom(batchNoDone.getDoneQuantityMeasuredUom())
                .scrapQuantityBaseUom(batchNoDone.getScrapQuantityBaseUom())
                .scrapQuantityMeasuredUom(batchNoDone.getScrapQuantityMeasuredUom())
                .orderNumber(batchNoDone.getOrderNumber())
                .qualityApproval(batchNoDone.isQualityApproval())
                .measuredUom(batchNoDone.getMeasuredUom())
                .queuedTimestamp(batchNoDone.getQueuedTimestamp())
                .build();

        batchNoDoneService.update(updateBatchNoDoneRequest);
    }

    public static void createBatchNoDone(ProcessOrderCompleteRequest.BatchDetails completeBatch, boolean isProcessResource, BatchNoInWork batchNoInWork, BigDecimal quantityToProcess, BigDecimal totalScrapQty) throws Exception {
        BatchNoDoneRequest.BatchNoDoneRequestBuilder builder = BatchNoDoneRequest.builder()
                .active(1);

        if (completeBatch != null) {
            builder.site(completeBatch.getSite())
                    .batchNo(completeBatch.getBatchNumber())
                    .material(completeBatch.getMaterial())
                    .materialVersion(completeBatch.getMaterialVersion())
                    .phaseId(completeBatch.getPhase())
                    .operation(completeBatch.getOperation())
                    .resource(completeBatch.getResource())
                    .orderNumber(completeBatch.getOrderNumber());
            if (!isProcessResource) {
                builder.user(completeBatch.getUser());
            }
//            if (completeBatch.getScrapQuantity() != null) {
                builder.scrapQuantityBaseUom(totalScrapQty)
                        .scrapQuantityMeasuredUom(totalScrapQty);
//            }
        }

        if (batchNoInWork != null) {
            builder.dateTime(batchNoInWork.getDateTime())
                    .recipe(batchNoInWork.getRecipe())
                    .recipeVersion(batchNoInWork.getRecipeVersion())
                    .quantityBaseUom(batchNoInWork.getQuantityBaseUom())
                    .quantityMeasuredUom(batchNoInWork.getQuantityMeasuredUom())
                    .baseUom(batchNoInWork.getBaseUom())
                    .measuredUom(batchNoInWork.getMeasuredUom())
                    .queuedTimestamp(batchNoInWork.getQueuedTimestamp())
                    .workcenter(batchNoInWork.getWorkcenter())
                    .qualityApproval(batchNoInWork.isQualityApproval());
        }

        if (quantityToProcess != null) {
            builder.qtyDone(quantityToProcess);
        }

        BatchNoDoneRequest batchNoDoneRequest = builder.build();
        batchNoDoneService.create(batchNoDoneRequest);
    }


    public static BatchNoComplete updateBatchNoComplete(BatchNoComplete batchNoComplete) throws Exception {

        BatchNoCompleteDTO updateBatchNoCompleteRequest = BatchNoCompleteDTO.builder()
                .site(batchNoComplete.getSite())
                .dateTime(batchNoComplete.getDateTime())
                .batchNo(batchNoComplete.getBatchNo())
                .material(batchNoComplete.getMaterial())
                .materialVersion(batchNoComplete.getMaterialVersion())
                .recipe(batchNoComplete.getRecipe())
                .recipeVersion(batchNoComplete.getRecipeVersion())
                .phaseId(batchNoComplete.getPhaseId())
                .operation(batchNoComplete.getOperation())
                .quantityBaseUom(batchNoComplete.getQuantityBaseUom())
                .quantityMeasuredUom(batchNoComplete.getQuantityMeasuredUom())
                .resource(batchNoComplete.getResource())
                .workcenter(batchNoComplete.getWorkcenter())
                .baseUom(batchNoComplete.getBaseUom())
                .user(batchNoComplete.getUser())
                .qtyToComplete(batchNoComplete.getQtyToComplete())
                .yieldQuantityBaseUom(batchNoComplete.getYieldQuantityBaseUom())
                .yieldQuantityMeasuredUom(batchNoComplete.getYieldQuantityMeasuredUom())
                .scrapQuantityBaseUom(batchNoComplete.getScrapQuantityBaseUom())
                .scrapQuantityMeasuredUom(batchNoComplete.getScrapQuantityMeasuredUom())
                .orderNumber(batchNoComplete.getOrderNumber())
                .qualityApproval(batchNoComplete.isQualityApproval())
                .measuredUom(batchNoComplete.getMeasuredUom())
                .queuedTimestamp(batchNoComplete.getQueuedTimestamp())
                .build();

        BatchNoCompleteMsgModel response = batchNoCompleteService.update(updateBatchNoCompleteRequest);
        return response.getResponse();
    }

    public static BatchNoComplete createBatchNoComplete(ProcessOrderCompleteRequest.BatchDetails completeBatch, BatchNoInWork batchNoInWork, boolean isProcessResource, BigDecimal yieldQuantityBaseUom, BigDecimal yieldQuantityMeasuredUom, BigDecimal quantityToProcess) throws Exception {
        BatchNoCompleteDTO.BatchNoCompleteDTOBuilder builder = BatchNoCompleteDTO.builder()
                .createdDateTime(LocalDateTime.now());

        if (completeBatch != null) {
            builder.site(completeBatch.getSite())
                    .batchNo(completeBatch.getBatchNumber())
                    .material(completeBatch.getMaterial())
                    .materialVersion(completeBatch.getMaterialVersion())
                    .phaseId(completeBatch.getPhase())
                    .operation(completeBatch.getOperation())
                    .resource(completeBatch.getResource())
                    .orderNumber(completeBatch.getOrderNumber());
            if (!isProcessResource) {
                builder.user(completeBatch.getUser());
            }
            if (completeBatch.getScrapQuantity() != null) {
                builder.scrapQuantityBaseUom(completeBatch.getScrapQuantity())
                        .scrapQuantityMeasuredUom(completeBatch.getScrapQuantity());
            }
        }

        if (batchNoInWork != null) {
            builder.dateTime(batchNoInWork.getDateTime())
                    .recipe(batchNoInWork.getRecipe())
                    .recipeVersion(batchNoInWork.getRecipeVersion())
                    .quantityBaseUom(batchNoInWork.getQuantityBaseUom())
                    .quantityMeasuredUom(batchNoInWork.getQuantityMeasuredUom())
                    .workcenter(batchNoInWork.getWorkcenter())
                    .baseUom(batchNoInWork.getBaseUom())
                    .measuredUom(batchNoInWork.getMeasuredUom())
                    .queuedTimestamp(batchNoInWork.getQueuedTimestamp())
                    .qualityApproval(batchNoInWork.isQualityApproval());
        }

        if (quantityToProcess != null) {
            builder.qtyToComplete(quantityToProcess)
                    .yieldQuantityBaseUom(quantityToProcess)
                    .yieldQuantityMeasuredUom(quantityToProcess);
        }

        BatchNoCompleteDTO createBatchNoCompleteRequest = builder.build();
        BatchNoCompleteMsgModel response = batchNoCompleteService.create(createBatchNoCompleteRequest);
        return response != null ? response.getResponse() : null;
    }

    public static void createBatchNoYield(ProcessOrderCompleteRequest.BatchDetails completeBatch, boolean isProcessResource, BigDecimal yieldQuantityBaseUom, BigDecimal yieldQuantityMeasuredUom, BatchNoInWork batchNoInWork) throws Exception {
        BatchNoYieldReportingRequest.BatchNoYieldReportingRequestBuilder builder = BatchNoYieldReportingRequest.builder()
                .active(1);

        if (completeBatch != null) {
            builder.site(completeBatch.getSite())
                    .batchNo(completeBatch.getBatchNumber())
                    .phaseId(completeBatch.getPhase())
                    .operation(completeBatch.getOperation())
                    .resource(completeBatch.getResource())
                    .theoreticalYieldBaseUom(completeBatch.getQuantity())
                    .theoreticalYieldMeasuredUom(completeBatch.getQuantity())
                    .yieldVarianceBaseUom(completeBatch.getQuantity())
                    .yieldVarianceMeasuredUom(completeBatch.getQuantity())
                    .orderNumber(completeBatch.getOrderNumber());
            if (!isProcessResource) {
                builder.user(completeBatch.getUser());
            }
            if (completeBatch.getScrapQuantity() != null) {
                builder.scrapQuantityBaseUom(completeBatch.getScrapQuantity())
                        .scrapQuantityMeasuredUom(completeBatch.getScrapQuantity());
            }
        }

        if (batchNoInWork != null) {
            builder.recipe(batchNoInWork.getRecipe())
                    .recipeVersion(batchNoInWork.getRecipeVersion())
                    .baseUom(batchNoInWork.getBaseUom())
                    .measuredUom(batchNoInWork.getMeasuredUom())
                    .reportTimestamp(batchNoInWork.getQueuedTimestamp());
        }

        if (yieldQuantityBaseUom != null) {
            builder.actualYieldBaseUom(yieldQuantityBaseUom);
        }
        if (yieldQuantityMeasuredUom != null) {
            builder.actualYieldMeasuredUom(yieldQuantityMeasuredUom);
        }

        BatchNoYieldReportingRequest batchNoYieldReportingRequest = builder.build();
        batchNoYieldReportingService.create(batchNoYieldReportingRequest);
    }

    public static void createBatchInQueue(MessageModel nextOp, ProcessOrderCompleteRequest.BatchDetails completeBatch, BatchNoInWork batchNoInWork, boolean isProcessResource, BigDecimal quantityToProcess) throws Exception {

        String operationId = "";
        String phaseId = "";
        if (nextOp != null && nextOp.getResultBody() != null) {
            for (Map.Entry<String, Object> entry : nextOp.getResultBody().entrySet()) {
                if ("operationId".equals(entry.getKey())) {
                    operationId = (String) entry.getValue();
                } else if ("phaseId".equals(entry.getKey())) {
                    phaseId = (String) entry.getValue();
                }
            }
        }

        BatchNoInQueueRequest.BatchNoInQueueRequestBuilder builder = BatchNoInQueueRequest.builder()
                .qualityApproval(false)
                .active(1);

        if (completeBatch != null) {
            builder.site(completeBatch.getSite())
                    .batchNo(completeBatch.getBatchNumber())
                    .material(completeBatch.getMaterial())
                    .materialVersion(completeBatch.getMaterialVersion())
                    .workcenter(completeBatch.getWorkcenter())
                    .orderNumber(completeBatch.getOrderNumber());
            if (!isProcessResource) {
                builder.user(completeBatch.getUser());
            }
        }

        if (batchNoInWork != null) {
            builder.dateTime(batchNoInWork.getDateTime())
                    .recipe(batchNoInWork.getRecipe())
                    .recipeVersion(batchNoInWork.getRecipeVersion())
                    .quantityBaseUom(batchNoInWork.getQuantityBaseUom())
                    .quantityMeasuredUom(batchNoInWork.getQuantityMeasuredUom())
                    .baseUom(batchNoInWork.getBaseUom())
                    .measuredUom(batchNoInWork.getMeasuredUom())
                    .queuedTimestamp(batchNoInWork.getQueuedTimestamp());
        }

        if (quantityToProcess != null) {
            builder.qtyInQueue(quantityToProcess);
        }

        builder.phaseId(phaseId)
                .operation(operationId);

        BatchNoInQueueRequest createBatchNoInQueueRequest = builder.build();
        batchNoInQueueService.createBatchNoInQueue(createBatchNoInQueueRequest);
    }

    public static void createBatchNoPhaseProgress(ProcessOrderCompleteRequest.BatchDetails completeBatch, boolean isProcessResource, BatchNoInWork batchNoInWork, BigDecimal quantityToProcess) throws Exception {
        // Build the PhaseProgress object
        PhaseProgress.PhaseProgressBuilder phaseBuilder = PhaseProgress.builder()
                .status(Boolean.TRUE.equals(completeBatch.getFinalReport()) ? "Complete" : "Start")
                .endTimestamp(LocalDateTime.now());

        if (completeBatch != null) {
            phaseBuilder.phase(completeBatch.getPhase())
                    .operation(completeBatch.getOperation())
                    .scrapQuantity(completeBatch.getScrapQuantity())
                    .scrapQuantityBaseUom(completeBatch.getScrapQuantity())
                    .scrapQuantityMeasuredUom(completeBatch.getScrapQuantity());
        }

        if (batchNoInWork != null) {
            phaseBuilder.baseUom(batchNoInWork.getBaseUom())
                    .measuredUom(batchNoInWork.getMeasuredUom())
                    .startTimestamp(batchNoInWork.getQueuedTimestamp());
        }

        if (quantityToProcess != null) {
            phaseBuilder.completeQuantityBaseUom(quantityToProcess)
                    .completeQuantityMeasuredUom(quantityToProcess);
        }

        PhaseProgress newPhaseProgress = phaseBuilder.build();

        // Build the BatchNoPhaseProgress object
        BatchNoPhaseProgressRequest.BatchNoPhaseProgressRequestBuilder batchBuilder = BatchNoPhaseProgressRequest.builder()
                .dateTime(LocalDateTime.now())
                .phaseProgress(List.of(newPhaseProgress))
                .user(isProcessResource || completeBatch == null ? null : completeBatch.getUser());

        if (completeBatch != null) {
            batchBuilder.batchNo(completeBatch.getBatchNumber())
                    .material(completeBatch.getMaterial())
                    .materialVersion(completeBatch.getMaterialVersion())
                    .orderNumber(completeBatch.getOrderNumber())
                    .site(completeBatch.getSite());
        }

        if (batchNoInWork != null) {
            batchBuilder.recipe(batchNoInWork.getRecipe())
                    .recipeVersion(batchNoInWork.getRecipeVersion());
        }

        BatchNoPhaseProgressRequest newBatchNoPhaseProgress = batchBuilder.build();
        batchNoPhaseProgressService.create(newBatchNoPhaseProgress);
    }

    public static void createBatchNoPhaseProgressForStart(ProcessOrderStartRequest.BatchDetails startbatch, boolean isProcessResource, BatchNoInQueue batchNoInQueue, BigDecimal startQty) throws Exception {
        // Build the PhaseProgress object
        PhaseProgress.PhaseProgressBuilder phaseBuilder = PhaseProgress.builder()
                .endTimestamp(LocalDateTime.now())
                .status("start");

        if (startbatch != null) {
            phaseBuilder.phase(startbatch.getPhase())
                    .operation(startbatch.getOperation())
                    .startQuantityBaseUom(startQty)
                    .startQuantityMeasuredUom(startQty);
        }

        PhaseProgress newPhaseProgress = phaseBuilder.build();

        // Build the BatchNoPhaseProgress object
        BatchNoPhaseProgressRequest.BatchNoPhaseProgressRequestBuilder batchBuilder = BatchNoPhaseProgressRequest.builder()
                .dateTime(LocalDateTime.now())
                .phaseProgress(List.of(newPhaseProgress))
                .user(isProcessResource || startbatch == null ? null : startbatch.getUser());

        if (startbatch != null) {
            batchBuilder.batchNo(startbatch.getBatchNumber())
                    .material(startbatch.getMaterial())
                    .materialVersion(startbatch.getMaterialVersion())
                    .orderNumber(startbatch.getOrderNumber())
                    .site(startbatch.getSite());
        }

        if (batchNoInQueue != null) {
            batchBuilder.recipe(batchNoInQueue.getRecipe())
                    .recipeVersion(batchNoInQueue.getRecipeVersion());
        }

        BatchNoPhaseProgressRequest newBatchNoPhaseProgress = batchBuilder.build();
        batchNoPhaseProgressService.create(newBatchNoPhaseProgress);
    }

    public static void updateBatchNoPhaseProgess(BatchNoPhaseProgress batchNoPhaseProgress) throws Exception {

        BatchNoPhaseProgressRequest updateBatchNoPhaseProgressRequest = BatchNoPhaseProgressRequest.builder()
                .site(batchNoPhaseProgress.getSite())
                .dateTime(batchNoPhaseProgress.getDateTime())
                .batchNo(batchNoPhaseProgress.getBatchNo())
                .material(batchNoPhaseProgress.getMaterial())
                .materialVersion(batchNoPhaseProgress.getMaterialVersion())
                .recipe(batchNoPhaseProgress.getRecipe())
                .recipeVersion(batchNoPhaseProgress.getRecipeVersion())
                .orderNumber(batchNoPhaseProgress.getOrderNumber())
                .phaseProgress(batchNoPhaseProgress.getPhaseProgress())
                .build();

        batchNoPhaseProgressService.update(updateBatchNoPhaseProgressRequest);
    }


    //Check if the batch is completed.
    public boolean isBatchCompleted(String batchStatus) {
        return false;
    }

    // check batch is in Header
    public boolean validateBatchHeader(String batchHeaderStatus) {
        return isStatusAcceptable(batchHeaderStatus);
    }

    // -------------------- Helper Methods --------------------

    //Checks if a status is acceptable.
    private static boolean isStatusAcceptable(String status) {
        return status != null && ACCEPTABLE_STATUSES.contains(status.toUpperCase());
    }

    //Checks if a status is non-acceptable.
    private static boolean isStatusNonAcceptable(String status) {
        return status != null && NON_ACCEPTABLE_STATUSES.contains(status.toUpperCase());
    }
}
