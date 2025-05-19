package com.rits.processorderrelease.service;

import com.rits.Utility.ProcessOrderUtility;
import com.rits.batchnoheader.dto.BatchNoHeaderRequest;
import com.rits.batchnoheader.service.BatchNoHeaderService;
import com.rits.batchnoinqueue.dto.BatchNoInQueueRequest;
import com.rits.batchnoinqueue.service.BatchNoInQueueService;
import com.rits.batchnorecipeheaderservice.dto.BatchNoRecipeHeaderReq;
import com.rits.batchnorecipeheaderservice.dto.Phase;
import com.rits.batchnorecipeheaderservice.service.BatchNoRecipeHeaderService;
import com.rits.hookservice.annotation.Hookable;
import com.rits.inventoryservice.service.InventoryService;
import com.rits.nextnumbergeneratorservice.dto.NextNumberResponse;
import com.rits.nextnumbergeneratorservice.service.NextNumberGeneratorService;
import com.rits.processorderrelease.dto.OperationRequest;
import com.rits.processorderrelease.dto.ProcessOrderReleaseRequest;
import com.rits.processorderrelease.dto.ProcessOrderReleaseResponse;
import com.rits.processorderrelease.dto.RecipeRequest;
import com.rits.processorderservice.dto.ProcessOrderRequest;
import com.rits.processorderservice.exception.ProcessOrderException;
import com.rits.processorderservice.model.BatchNumber;
import com.rits.processorderservice.model.ProcessOrder;
import com.rits.processorderservice.repository.ProcessOrderRepository;
import com.rits.processorderservice.service.ProcessOrderService;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import com.rits.shoporderrelease.dto.Item;
import com.rits.shoporderrelease.dto.ItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProcessOrderReleaseServiceImpl implements ProcessOrderReleaseService  {
    @Autowired
    ProcessOrderService procssOrderService;

    @Autowired
    NextNumberGeneratorService nextNumberService;

    @Autowired
    BatchNoHeaderService batchNoHeaderService;

    @Autowired
    BatchNoInQueueService batchNoInQueueService;

    @Autowired
    BatchNoRecipeHeaderService batchNoRecipeHeaderService;

    @Autowired
    private InventoryService inventoryService;

    @Value("${recipe-service.url}/checkReleasible")
    private String checkReleasibleUrl;

    @Value("${recipe-service.url}/setRecipeStatus")
    private String setRecipeStatusUrl;


    private final WebClient.Builder webClientBuilder;
    private final ProcessOrderRepository repository;

    @Value("${item-service.url}/retrieve")
    private String itemUrl;

    @Value("${operation-service.url}/retrieveByOperationAndSite")
    private String operationUrl;
    @Override
    @Hookable
    public ProcessOrderReleaseResponse releaseOrders(ProcessOrderReleaseRequest request) {

        // Validate the input
        validateRequest(request);

        // Create a list to hold async tasks
        List<CompletableFuture<ProcessOrderReleaseResponse.BatchDetails>> asyncTasks = new ArrayList<>();

        for (ProcessOrderReleaseRequest.OrderDetails order : request.getOrders()) {
            // Asynchronously process each order
            asyncTasks.add(processOrderAsync(order));
        }

        // Wait for all async tasks to complete
        List<ProcessOrderReleaseResponse.BatchDetails> batchDetailsList = asyncTasks.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // Build and return the response
        ProcessOrderReleaseResponse response = new ProcessOrderReleaseResponse();
        //    response.setStatus("success");
        //  response.setMessage("Order Release Summited Sucessful");
        response.setBatches(batchDetailsList);
        return response;

    }

    @Async
    public CompletableFuture<ProcessOrderReleaseResponse.BatchDetails> processOrderAsync(
            ProcessOrderReleaseRequest.OrderDetails order) {
        String processOrder = order.getProcessOrder();
        BigDecimal qtyToRelease = order.getQtyToRelease();
        String site = order.getSite();

        try {

//            validateBatchRelease(order.getSite(), order.getBatchNumber(), order.getOrderType());

            // Step 1: Validate and fetch Shop Order
            ProcessOrder processOrderDetails = procssOrderService.retrieveProcessOrder (order.getSite(),order.getProcessOrder());
            validateOrderReleasable(processOrderDetails,qtyToRelease);

            // check recipe releaseable
            validateRecipeReleasable(order.getSite(),processOrderDetails.getRecipe(), processOrderDetails.getRecipeVersion());

            //check item releasable
            validateItemReleasable(order.getSite(), processOrderDetails.getMaterial(), processOrderDetails.getMaterialVersion());

            // Step 2: Determine quantity to release
            BigDecimal releaseQuantity = (qtyToRelease != null && qtyToRelease.compareTo(BigDecimal.ZERO) > 0)
                    ? qtyToRelease
                    : processOrderDetails.getAvailableQtyToRelease();

            if (!(releaseQuantity.compareTo(new BigDecimal(0)) >= 0)) {
                throw new RuntimeException("Invalid quantity to release for order: " + processOrder);
            }

            // Step 3: Calculate number of batches
            Map<String, BigDecimal>  batchNumbers = calculateBatchNumbers(site,processOrderDetails, releaseQuantity);

            // Step 4: Create batch headers and queue entries
            List<BatchNoHeaderRequest> batchHeaders = createBatchHeaders(batchNumbers, processOrderDetails);
            List<BatchNoRecipeHeaderReq> batchNoRecipeHeaderReqs = createBatchRecipeHeaders(batchHeaders,processOrderDetails,releaseQuantity);

            processBatchHeaders(batchHeaders);
            processBatchRecipeHeaders(batchNoRecipeHeaderReqs);


            List<BatchNoInQueueRequest>  batchInQueues = buildBatchInQueueRequests(order.getUser(), batchHeaders,batchNoRecipeHeaderReqs,processOrderDetails);

            // Step 5: Persist batch headers and queue entries
            processBatchNoInQueueRequests(batchInQueues);

            // Step 6: Update Shop Order released quantity
            updateProcessOrderDetails(processOrderDetails,batchInQueues);

            repository.save(processOrderDetails);

            BigDecimal availableQty = processOrderDetails.getAvailableQtyToRelease();
            int qty = (availableQty != null) ? availableQty.intValue() : 0;

            ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                    .site(processOrderDetails.getSite())
                    .eventType("PROCESS_ORDER_RELEASE")
                    .userId(order.getUser())
                    .batchNo(processOrderDetails.getBatchNumber().get(0).getBatchNumber())
                    .orderNumber(processOrderDetails.getOrderNumber())
                    .workcenterId(processOrderDetails.getWorkCenter())
                    .material(order.getPlannedMaterial())
                    .materialVersion(order.getMaterialVersion())
                    .qty(qty)
                    .topic("production-log")
                    .status("Active")
                    .eventData(processOrderDetails.getOrderNumber() + " Released successfully")
                    .createdDatetime(LocalDateTime.now())
                    .build();

            boolean productionLog = ProcessOrderUtility.productionLog(productionLogRequest);
            if(!productionLog){
                throw new ProcessOrderException(7024);
            }

            // Step 7: Build batch details response
            ProcessOrderReleaseResponse.BatchDetails batchDetails = new ProcessOrderReleaseResponse.BatchDetails();
            batchDetails.setProcessOrder(processOrder);
            batchDetails.setBatchNumber(batchNumbers);
            batchDetails.setQuantity(releaseQuantity.doubleValue());
            batchDetails.setMaterial(order.getPlannedMaterial());
            batchDetails.setMaterialVersion(order.getMaterialVersion());
            batchDetails.setStatus("Released");
            batchDetails.setMessage(processOrder +" with qty of "+releaseQuantity.doubleValue()+ " is released" );
            setRecipeStatus(order.getSite(),processOrderDetails.getRecipe(), processOrderDetails.getRecipeVersion()); // SET RECIPE STATUS TO IN_USE
            return CompletableFuture.completedFuture(batchDetails);

        } catch (Exception e) {
            // Handle exception and build error response
            ProcessOrderReleaseResponse.BatchDetails errorDetails = new ProcessOrderReleaseResponse.BatchDetails();
            errorDetails.setProcessOrder(processOrder);
            errorDetails.setMaterial(order.getPlannedMaterial());
            errorDetails.setMaterialVersion(order.getMaterialVersion());
            errorDetails.setStatus("FAILED");
            errorDetails.setMessage("Failed to process order: " + e.getMessage()); ;
            return CompletableFuture.completedFuture(errorDetails);
        }


    }

    private void validateBatchRelease(String site, String batchNumber, String orderType) {

        boolean exists = inventoryService.isBatchNumberExists(batchNumber, site);

        // If orderType does NOT end with "PRD" and batch number does NOT exist, throw an exception
        if (!orderType.endsWith("PRD") && !exists) {
            throw new RuntimeException("First release the manufacturing order for this batch: " + batchNumber);
        }
    }


    private void validateRequest(ProcessOrderReleaseRequest request) {
        if (request.getOrders() == null || request.getOrders().isEmpty()) {
            throw new IllegalArgumentException("Order list cannot be empty.");
        }

        // Ensure unique shop orders in the request
        Set<String> uniqueOrders = request.getOrders().stream()
                .map(ProcessOrderReleaseRequest.OrderDetails::getProcessOrder)
                .collect(Collectors.toSet());

        if (uniqueOrders.size() != request.getOrders().size()) {
            throw new IllegalArgumentException("Duplicate shop orders found in the request.");
        }
    }


    private void validateOrderReleasable(ProcessOrder shopOrderDetails, BigDecimal qtyToRelease) {
        if (!shopOrderDetails.getStatus().equalsIgnoreCase("Releasable")) {
            throw new RuntimeException("Order " + shopOrderDetails.getOrderNumber() + " is not in releasable status.");
        }
        if (shopOrderDetails.getAvailableQtyToRelease().compareTo(BigDecimal.ZERO) > 0) {
            // Check if available quantity is less than the quantity to release
            if (shopOrderDetails.getAvailableQtyToRelease().compareTo(qtyToRelease) < 0) {
                throw new RuntimeException("Order " + shopOrderDetails.getOrderNumber() + " has no sufficient quantity to release.");
            }
        } else {
            throw new RuntimeException("Order " + shopOrderDetails.getOrderNumber() + " has no remaining quantity to release.");
        }
    }

    private void validateRecipeReleasable(String site, String recipe, String recipeVersion) {
        try {
            RecipeRequest recipeRequest = RecipeRequest.builder().site(site).recipeId(recipe).version(recipeVersion).build();
            Boolean validateReleasable = webClientBuilder.build()
                    .post()
                    .uri(checkReleasibleUrl)
                    .bodyValue(recipeRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if(Boolean.FALSE.equals(validateReleasable))
                throw new RuntimeException("Recipe " + recipe + " is not in releasable status.");

        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private void validateItemReleasable(String site, String material, String materialVersion) {

        try{
            ItemRequest itemRequest = ItemRequest.builder().site(site).item(material).revision(materialVersion).build();
            Item item = webClientBuilder.build()
                    .post()
                    .uri(itemUrl)
                    .bodyValue(itemRequest)
                    .retrieve()
                    .bodyToMono(Item.class)
                    .block();

            if (item == null) {
                throw new RuntimeException("Item not found");
            }

            if (!"RELEASABLE".equalsIgnoreCase(item.getStatus())) {
                throw new RuntimeException("Material " + material + " with materialVersion " + materialVersion + " is not in releasable state");
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private void setRecipeStatus(String site, String recipe, String recipeVersion) {
        try {
            RecipeRequest recipeRequest = RecipeRequest.builder().site(site).recipeId(recipe).version(recipeVersion).build();
            webClientBuilder.build()
                    .post()
                    .uri(setRecipeStatusUrl)
                    .bodyValue(recipeRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

        } catch (Exception e){
            throw new RuntimeException("unconditional error occured in recipe status change. Error " + e.getMessage());
        }
    }

    private Map<String, BigDecimal> calculateBatchNumbers(String site,ProcessOrder processOrderDetails, BigDecimal releaseQuantity) throws Exception {
        List<BatchNumber> availableBatchNumbers = processOrderDetails.getBatchNumber().stream()
                .filter(batch -> "New".equalsIgnoreCase(batch.getState())) // Filter by state
                .collect(Collectors.toList());

        // Validate batch numbers
        if (!(availableBatchNumbers.isEmpty())) {

            // Create a map of batchNumber and batchNumberQuantity
            Map<String, BigDecimal> batchMap = new LinkedHashMap<>();
            BigDecimal totalBatchQuantity = BigDecimal.ZERO;

            // Sort batches by quantity in descending order to prioritize larger batches
            availableBatchNumbers.sort((b1, b2) ->
                    new BigDecimal(b2.getBatchNumberQuantity()).compareTo(new BigDecimal(b1.getBatchNumberQuantity())));

            // Pick batches to fulfill the release quantity
            for (BatchNumber batch : availableBatchNumbers) {
                BigDecimal batchQuantity = new BigDecimal(batch.getBatchNumberQuantity());

                // Check if adding this batch will exceed the release quantity
                if (totalBatchQuantity.add(batchQuantity).compareTo(releaseQuantity) <= 0) {
                    totalBatchQuantity = totalBatchQuantity.add(batchQuantity);
                    batchMap.put(batch.getBatchNumber(), batchQuantity);
                }

                // Stop if release quantity is fulfilled
                if (totalBatchQuantity.compareTo(releaseQuantity) == 0) {
                    break; // Break the loop when the release quantity is reached
                }
            }

            // If totalBatchQuantity does not match releaseQuantity, throw an error
            if (totalBatchQuantity.compareTo(releaseQuantity) != 0) {
                throw new Exception("Insufficient batch quantities to fulfill the release quantity.");
            }

            return batchMap;

        } else {
            List<NextNumberResponse> nextNumberResponses = nextNumberService.createNextNumbers("Batch Number",site,processOrderDetails.getMaterial(), processOrderDetails.getMaterialVersion(),processOrderDetails.getOrderNumber(),"","","",releaseQuantity.doubleValue());

            Map<String, BigDecimal> newBatchMap = nextNumberResponses.stream()
                    .collect(Collectors.toMap(
                            NextNumberResponse::getNextNumber,
                            response -> BigDecimal.valueOf(response.getQty())
                    ));

            if (newBatchMap != null && !newBatchMap.isEmpty()) {
                List<BatchNumber> batchNumbers = processOrderDetails.getBatchNumber();

                if (availableBatchNumbers == null) {
                    availableBatchNumbers = new ArrayList<>();
                    processOrderDetails.setBatchNumber(availableBatchNumbers);
                }

                newBatchMap.forEach((batchNumber, quantity) -> {
                    BatchNumber newBatch = new BatchNumber();
                    newBatch.setBatchNumber(batchNumber);
                    newBatch.setBatchNumberQuantity(quantity.toPlainString());
                    newBatch.setState("IN_USE");
                    newBatch.setEnabled(true);
                    batchNumbers.add(newBatch);
                });
            }

            return newBatchMap;
        }

    }

    private double getItemLotSize(String site, String material, String materialVersion) throws WebClientResponseException {
        ItemRequest itemRequest = ItemRequest.builder().site(site).item(material).revision(materialVersion).build();
        Item itemDetail = webClientBuilder.build()
                .post()
                .uri(itemUrl)
                .bodyValue(itemRequest)
                .retrieve()
                .bodyToMono(Item.class)
                .block();
        return itemDetail != null ? itemDetail.getLotSize() : 1;
    }

    private List<BatchNoHeaderRequest> createBatchHeaders(Map<String, BigDecimal> batchNumbers, ProcessOrder processOrderDetails) throws Exception {
        List<BatchNoHeaderRequest> batchHeaderRequests = new ArrayList<>();

        // Iterate through batchNumbers map
        for (Map.Entry<String, BigDecimal> entry : batchNumbers.entrySet()) {
            String batchNumber = entry.getKey();
            BigDecimal quantity = entry.getValue();

            // Check if the batch number exists in BatchHeaderService
            BatchNoHeaderRequest batchNoHeaderRequest = new BatchNoHeaderRequest();
            batchNoHeaderRequest.setSite(processOrderDetails.getSite());
            batchNoHeaderRequest.setBatchNumber(batchNumber);
            batchNoHeaderRequest.setMaterial(processOrderDetails.getMaterial());

            boolean batchHeaderExistResponse = batchNoHeaderService.isBatchNoHeader(batchNoHeaderRequest);

            if ( !batchHeaderExistResponse) {
                // If batch number does not exist, create a new BatchNoHeaderRequest
                BatchNoHeaderRequest newBatchHeader = new BatchNoHeaderRequest();
                newBatchHeader.setSite(processOrderDetails.getSite());
                newBatchHeader.setBatchNumber(batchNumber);
                newBatchHeader.setOrderNo(processOrderDetails.getOrderNumber());
                newBatchHeader.setMaterial(processOrderDetails.getMaterial());
                newBatchHeader.setMaterialVersion(processOrderDetails.getMaterialVersion());
                newBatchHeader.setTotalQuantity(quantity);
                newBatchHeader.setQtyToWorkOrder(new BigDecimal(0)); // Default values
                newBatchHeader.setQtyInQueue(new BigDecimal(0));     // Default values
                newBatchHeader.setQtyInHold(new BigDecimal(0));     // Default values
                newBatchHeader.setQtyDone(new BigDecimal(0));       // Default values
                newBatchHeader.setRecipeName(processOrderDetails.getRecipe());
                newBatchHeader.setRecipeVersion(processOrderDetails.getRecipeVersion());
                newBatchHeader.setBaseUom(processOrderDetails.getUom());
                newBatchHeader.setMeasuredUom(processOrderDetails.getMeasuredUom()); // Assuming same UOM, adjust if different
                newBatchHeader.setConversionFactor(processOrderDetails.getConversionFactor()); // Assuming 1:1, adjust as needed
                newBatchHeader.setReleasedQuantityBaseUom(quantity);
                newBatchHeader.setStatus("releasable");
                if(processOrderDetails.getConversionFactor()!=null)
                    newBatchHeader.setReleasedQuantityMeasuredUom(quantity.multiply(processOrderDetails.getConversionFactor()));

                // Add to the list
                batchHeaderRequests.add(newBatchHeader);
            } else{
                throw new Exception("Batch numbers found for the process order in Batch Header");

            }
        }

        return batchHeaderRequests;
    }

    private List<BatchNoRecipeHeaderReq> createBatchRecipeHeaders(List<BatchNoHeaderRequest> batchHeaderRequests, ProcessOrder processOrderDetails,BigDecimal releaseQty) {
        List<BatchNoRecipeHeaderReq> batchRecipeHeaderRequests = new ArrayList<>();

        for (BatchNoHeaderRequest batchHeader : batchHeaderRequests) {
            BatchNoRecipeHeaderReq recipeHeaderReq = new BatchNoRecipeHeaderReq();

            // Populate BatchNoRecipeHeaderReq fields
            recipeHeaderReq.setSite(batchHeader.getSite());
            recipeHeaderReq.setBatchNo(batchHeader.getBatchNumber());
            recipeHeaderReq.setBatchNoHeaderBo("HeaderBO-" + batchHeader.getBatchNumber()); // Link to BatchNoHeader
            recipeHeaderReq.setUser("system"); // Default or dynamic user
            recipeHeaderReq.setBatchQty(releaseQty.doubleValue()); // Convert totalQuantity to Double
            recipeHeaderReq.setRecipeId(processOrderDetails.getRecipe());
            recipeHeaderReq.setRecipeVersion(processOrderDetails.getRecipeVersion());
            recipeHeaderReq.setOrderNo(batchHeader.getOrderNo());
            recipeHeaderReq.setMaterial(batchHeader.getMaterial());
            recipeHeaderReq.setMaterialVersion(batchHeader.getMaterialVersion());
            recipeHeaderReq.setMaterialDescription(processOrderDetails.getMaterialDescription());
            recipeHeaderReq.setRecipeVersion(processOrderDetails.getRecipeVersion());

            // Add to the list
            batchRecipeHeaderRequests.add(recipeHeaderReq);
        }

        return batchRecipeHeaderRequests;
    }

    private void processBatchHeaders(List<BatchNoHeaderRequest> batchHeaders) throws Exception {
        for (BatchNoHeaderRequest batchHeaderRequest : batchHeaders) {
            try {
                // Call the create method of BatchNoHeaderService
                batchNoHeaderService.create(batchHeaderRequest);
            } catch (Exception e) {
                // Log the error and continue with the next batch header
//                System.err.println("Failed to create batch header for BatchNumber: " + batchHeaderRequest.getBatchNumber() + ". Error: " + e.getMessage());
                throw new ProcessOrderException(160,batchHeaderRequest.getBatchNumber(), e.getMessage());
            }
        }
    }

    private void processBatchRecipeHeaders(List<BatchNoRecipeHeaderReq> batchNoRecipeHeaderReqs) throws Exception {
        for (BatchNoRecipeHeaderReq batchRecipeHeaderReq : batchNoRecipeHeaderReqs) {
            try {
                // Call the create method of BatchNoRecipeHeaderService
                batchNoRecipeHeaderService.create(batchRecipeHeaderReq);
            } catch (Exception e) {
                // Log the error and continue with the next batch recipe header
//                System.err.println("Failed to create batch recipe header for BatchNumber: " + batchRecipeHeaderReq.getBatchNo() + ". Error: " + e.getMessage());
                throw new ProcessOrderException(159,batchRecipeHeaderReq.getBatchNo(), e.getMessage());
            }
        }
    }

    private List<BatchNoInQueueRequest> buildBatchInQueueRequests(String user, List<BatchNoHeaderRequest> batchHeaders, List<BatchNoRecipeHeaderReq> batchNoRecipeHeaderReqs, ProcessOrder processOrderDetails) throws Exception {

        List<BatchNoInQueueRequest> inQueueRequests = new ArrayList<>();

        // Iterate through batch headers
        for (BatchNoHeaderRequest batchHeader : batchHeaders) {
            String batchNo = batchHeader.getBatchNumber();
            String orderNo = batchHeader.getOrderNo();
            String material = batchHeader.getMaterial();
            String materialVersion = batchHeader.getMaterialVersion();

            // Find the first phase and first operation
            Map<String, Object> phaseAndOperation = batchNoRecipeHeaderService.getBatchRecipeFirstPhaseFirstOp(batchNo, orderNo, material, materialVersion);

            Phase firstPhase = (Phase) phaseAndOperation.get("firstPhase");

            com.rits.batchnorecipeheaderservice.dto.Operation firstOperation = (com.rits.batchnorecipeheaderservice.dto.Operation) phaseAndOperation.get("firstOperation");
            OperationRequest operationRequest= OperationRequest.builder().site(processOrderDetails.getSite()).operation(firstOperation.getOperationId()).build();
            com.rits.processorderrelease.dto.Operation firstOperationResult =webClientBuilder.build()
                    .post()
                    .uri(operationUrl)
                    .bodyValue(operationRequest)
                    .retrieve()
                    .bodyToMono(com.rits.processorderrelease.dto.Operation.class)
                    .block();
            // Find the matching BatchNoRecipeHeaderReq
            BatchNoRecipeHeaderReq recipeHeaderReq = batchNoRecipeHeaderReqs.stream()
                    .filter(req -> req.getBatchNo().equals(batchNo))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Batch recipe header not found for batch: " + batchNo));

            // Build BatchNoInQueueRequest
            BatchNoInQueueRequest inQueueRequest = new BatchNoInQueueRequest();
            inQueueRequest.setSite(batchHeader.getSite());
            inQueueRequest.setDateTime(LocalDateTime.now());
            inQueueRequest.setBatchNo(batchNo); // Use batch number directly
            inQueueRequest.setBatchNoHeaderBO(batchHeader.getBatchNumber());// Example handle
            inQueueRequest.setMaterial(batchHeader.getMaterial()); // Example handle
            inQueueRequest.setMaterialVersion(batchHeader.getMaterialVersion());
            inQueueRequest.setRecipe(recipeHeaderReq.getRecipeId());
            inQueueRequest.setRecipeVersion(recipeHeaderReq.getRecipeVersion());
            inQueueRequest.setBatchNoRecipeHeaderBO(recipeHeaderReq.getBatchRecipeHandle());
            inQueueRequest.setPhaseId(firstPhase.getPhaseId());
            inQueueRequest.setOperation(firstOperation.getOperationId());
            inQueueRequest.setPhaseSequence(firstPhase.getSequence());
            inQueueRequest.setOpSequence(firstOperation.getSequence());
            inQueueRequest.setQuantityBaseUom(batchHeader.getTotalQuantity());
            inQueueRequest.setQuantityMeasuredUom(batchHeader.getTotalQuantity()); // Assuming 1:1 conversion
            inQueueRequest.setResource(""); // Populate if resource information is available
            inQueueRequest.setWorkcenter(firstOperationResult.getWorkCenter() != null ? firstOperationResult.getWorkCenter() : ""); // Populate if workcenter information is available
            inQueueRequest.setBaseUom(batchHeader.getBaseUom());
            inQueueRequest.setUser(user); // Example user
            inQueueRequest.setQtyToComplete(new BigDecimal(0)); // Default value
            inQueueRequest.setQtyInQueue(batchHeader.getTotalQuantity()); // Default value
            inQueueRequest.setOrderNumber(orderNo); // Example handle
            inQueueRequest.setQualityApproval(false);
            inQueueRequest.setMeasuredUom(batchHeader.getMeasuredUom());
            inQueueRequest.setQueuedTimestamp(LocalDateTime.now());
            inQueueRequest.setActive(1);

            // Add to the list
            inQueueRequests.add(inQueueRequest);
        }

        return inQueueRequests;
    }




    private void processBatchNoInQueueRequests(List<BatchNoInQueueRequest> inQueueRequests) {
        for (BatchNoInQueueRequest request : inQueueRequests) {
            try {
                // Call the createBatchNoInQueue method for each request
                batchNoInQueueService.createBatchNoInQueue(request);
            } catch (Exception e) {
                // Log the error and continue with the next request
//                System.err.println("Failed to create batch in queue for BatchNo: " + request.getBatchNo() + ". Error: " + e.getMessage());
                throw new ProcessOrderException(158,request.getBatchNo(), e.getMessage());
            }
        }
    }


    private void updateProcessOrderDetails(ProcessOrder processOrderDetails, List<BatchNoInQueueRequest> inQueueRequests) throws Exception {

        // Iterate over inQueueRequests to update processOrderDetails.batchNumber
        for (BatchNoInQueueRequest inQueueRequest : inQueueRequests) {
            String batchNo = inQueueRequest.getBatchNo().trim();

            processOrderDetails.getBatchNumber().stream()
                    .filter(batch -> batch.getBatchNumber().trim().equals(batchNo)) // Find the matching batch
                    .forEach(batch -> {
                        batch.setState("IN_USE"); // Update state to InUse
                        batch.setBatchNumberQuantity(String.valueOf(inQueueRequest.getQuantityBaseUom())); // Update quantity
                    });
        }

        // Update the availableQtyToRelease for processOrderDetails
        BigDecimal totalUsedQuantity = inQueueRequests.stream()
                .map(BatchNoInQueueRequest::getQuantityBaseUom) // Get used quantities
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sum up all quantities

        processOrderDetails.setAvailableQtyToRelease(
                processOrderDetails.getAvailableQtyToRelease().subtract(totalUsedQuantity));

        // Placeholder for creating ProcessOrderRequest from processOrderDetails
        ProcessOrderRequest processOrderRequest = mapProcessOrderToRequest(processOrderDetails);


        procssOrderService.updateProcessOrder(processOrderRequest);
    }

    private ProcessOrderRequest mapProcessOrderToRequest(ProcessOrder processOrderDetails) {
        ProcessOrderRequest request = new ProcessOrderRequest();
        request.setOrderNumber(processOrderDetails.getOrderNumber());
        request.setSite(processOrderDetails.getSite());
        request.setRecipe(processOrderDetails.getRecipe());
        request.setRecipeVersion(processOrderDetails.getRecipeVersion());
        request.setStatus(processOrderDetails.getStatus());
        request.setMaterial(processOrderDetails.getMaterial());
        request.setMaterialVersion(processOrderDetails.getMaterialVersion());
        request.setOrderType(processOrderDetails.getOrderType());
        request.setTargetQuantity(processOrderDetails.getTargetQuantity());
        request.setWorkCenter(processOrderDetails.getWorkCenter());
        request.setStartDate(processOrderDetails.getStartDate());
        request.setFinishDate(processOrderDetails.getFinishDate());
        request.setSchedFinTime(processOrderDetails.getSchedFinTime());
        request.setSchedStartTime(processOrderDetails.getSchedStartTime());
        request.setUom(processOrderDetails.getUom());
        request.setMeasuredUom(processOrderDetails.getMeasuredUom());
        request.setConversionFactor(processOrderDetails.getConversionFactor());
        request.setBatchNumber(processOrderDetails.getBatchNumber());
        request.setAvailableQtyToRelease(processOrderDetails.getAvailableQtyToRelease());
        request.setUserId(processOrderDetails.getCreatedBy()); // Assuming user ID is createdBy
        request.setInUse(true);
        request.setBatch(processOrderDetails.getBatch());
        request.setPriority(processOrderDetails.getPriority());
        request.setNewBnos(Collections.emptyList()); // Placeholder if not provided
        request.setBnoNumberList(new HashMap<>());   // Placeholder if not provided
        return request;
    }


}
