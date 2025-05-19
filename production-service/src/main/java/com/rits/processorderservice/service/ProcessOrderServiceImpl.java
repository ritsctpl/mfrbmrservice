package com.rits.processorderservice.service;


import com.rits.Utility.ProcessOrderUtility;
import com.rits.assemblyservice.dto.AuditLogRequest;
import com.rits.batchnorecipeheaderservice.dto.BatchNoRecipeHeaderReq;
import com.rits.batchnorecipeheaderservice.model.BatchNoRecipeHeader;
import com.rits.batchnorecipeheaderservice.service.BatchNoRecipeHeaderService;
import com.rits.nextnumbergeneratorservice.dto.GeneratePrefixAndSuffixRequest;
import com.rits.nextnumbergeneratorservice.model.NextNumberMessageModel;
import com.rits.nextnumbergeneratorservice.service.NextNumberGeneratorService;
import com.rits.processorderservice.dto.*;

import com.rits.processorderservice.exception.ProcessOrderException;
import com.rits.processorderservice.model.ProcessOrder;
import com.rits.processorderservice.model.ProcessOrderMessageModel;
import com.rits.processorderservice.model.BatchNumber;
import com.rits.processorderservice.repository.ProcessOrderRepository;

import com.rits.processorderservice.model.MessageDetails;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.data.mongodb.core.query.Query;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class ProcessOrderServiceImpl implements ProcessOrderService {
    private final ProcessOrderRepository processOrderRepository;
    private final WebClient.Builder webClientBuilder;
    private final MongoTemplate mongoTemplate;
    private final MessageSource localMessageSource;
    private final NextNumberGeneratorService nextNumberGeneratorService;



    @Value("${bom-service.url}/isExist")
    private String bomUrl;
    @Value("${item-service.url}/isExist")
    private String itemUrl;
    @Value("${routing-service.url}/isExist")
    private String routingUrl;
    @Value("${workcenter-service.url}/isExist")
    private String workCenterUrl;
    @Value("${recipe-service.url}/isExist")
    private String recipeUrl;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${nextnumbergenerator-service.url}/retrieveSampleNextNumber")
    private String nextNumberUrl;
    @Value("${batchnoheader-service.url}/isExist")
    private String readBatchNoUrl;

    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    @Autowired
    private BatchNoRecipeHeaderService batchNoRecipeHeaderService;


    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public ProcessOrderMessageModel createProcessOrder(ProcessOrderRequest processOrderRequest) throws Exception {
        if (processOrderRepository.existsBySiteAndOrderNumberAndActive(processOrderRequest.getSite(), processOrderRequest.getOrderNumber(), 1)) {
            throw new ProcessOrderException(4201, processOrderRequest.getOrderNumber());
        } else {

            GeneratePrefixAndSuffixRequest generateRequest = GeneratePrefixAndSuffixRequest.builder()
                    .site(processOrderRequest.getSite())
                    .numberType("Process Order")
                    .object(processOrderRequest.getMaterial())
                    .objectVersion(processOrderRequest.getMaterialVersion())
                    .userBO("")
                    .itemBO("")
                    .itemGroupBO("")
                    .pcuBO("")
                    .nextNumberActivity("")
                    .shopOrderBO("")
                    .priority(1)
                    .nonStartObject("")
                    .nonStartVersion("")
                    .build();



            validations(processOrderRequest);

            ProcessOrder processOrder = processOrderBuilder(processOrderRequest);

            if(!StringUtils.hasText(processOrderRequest.getOrderNumber())) {
                NextNumberMessageModel nextNumberMessage = nextNumberGeneratorService.generateNextNumber(generateRequest);

                if ("E".equals(nextNumberMessage.getMessage_details().getMsg_type())) {
                    throw new ProcessOrderException(4202, nextNumberMessage.getMessage_details().getMsg());
                }
                String generatedOrderNumber = nextNumberMessage.getGeneratedNextNumberResponse().getNextNum();
                processOrder.setOrderNumber(generatedOrderNumber);
            }

            processOrder.setCreatedDateTime(LocalDateTime.now());
            processOrder.setCreatedBy(processOrderRequest.getUserId());
            ProcessOrder processOrderCreate = processOrderRepository.save(processOrder);

            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(processOrderRequest.getSite())
                    .change_stamp("Create")
                    .action_code("PROCESSORDER-CREATE")
                    .action_detail("PocessOrder Created " + processOrderRequest.getOrderNumber())
                    .action_detail_handle("ActionDetailBO:" + processOrderRequest.getSite() + "," + "PROCESSORDER-CREATE" + "," + processOrderRequest.getUserId() + ":" + "com.rits.processorderservice.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(processOrderRequest.getUserId())
                    .router(processOrderRequest.getRecipe())
                    .router_revision(processOrderRequest.getRecipeVersion())
                    .item(processOrderRequest.getMaterial())
                    .item_revision(processOrderRequest.getMaterialVersion())
                    .txnId("PROCESSORDER-CREATE" + String.valueOf(LocalDateTime.now()) + processOrderRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("PROCESS_ORDER")
                    .build();
            webClientBuilder.build()
                    .post()
                    .uri(auditlogUrl)
                    .bodyValue(activityLog)
                    .retrieve()
                    .bodyToMono(AuditLogRequest.class)
                    .block();

            BigDecimal availableQty = processOrderRequest.getAvailableQtyToRelease();
            int qty = (availableQty != null) ? availableQty.intValue() : 0;

            ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                    .site(processOrderRequest.getSite())
                    .eventType("PROCESSORDER-CREATE")
                    .userId(processOrderRequest.getUserId())
                    .batchNo(processOrderRequest.getBatchNumber().isEmpty() ? null : processOrderRequest.getBatchNumber().get(0).getBatchNumber())
                    .orderNumber(processOrderRequest.getOrderNumber())
                    .material(processOrderRequest.getMaterial())
                    .workcenterId(processOrderRequest.getWorkCenter())
                    .materialVersion(processOrderRequest.getMaterialVersion())
                    .qty(qty)
                    .topic("production-log")
                    .status("Active")
                    .eventData(processOrderRequest.getOrderNumber() + " Created successfully")
                    .build();

            boolean productionLog = ProcessOrderUtility.productionLog(productionLogRequest);
            if(!productionLog){
                throw new ProcessOrderException(7024);
            }
            
            String createdMessage = getFormattedMessage(1, processOrderRequest.getOrderNumber());
            return ProcessOrderMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).processOrderResponse(processOrderCreate).build();
        }
    }

    @Override
    public ProcessOrderMessageModel updateProcessOrder(ProcessOrderRequest processOrderRequest) throws Exception {

        if (processOrderRepository.existsBySiteAndActiveAndOrderNumber(processOrderRequest.getSite(), 1, processOrderRequest.getOrderNumber())) {
            ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndOrderNumber(processOrderRequest.getSite(), 1, processOrderRequest.getOrderNumber());
            if (existingProcessOrder.isInUse()) {
                throw new ProcessOrderException(4223);
            }
            validations(processOrderRequest);
            ProcessOrder updatedProcessOrder = processOrderBuilder(processOrderRequest);

            updatedProcessOrder.setHandle(existingProcessOrder.getHandle());
            updatedProcessOrder.setSite(existingProcessOrder.getSite());
            updatedProcessOrder.setOrderNumber(existingProcessOrder.getOrderNumber());
            updatedProcessOrder.setCreatedBy(existingProcessOrder.getCreatedBy());
            updatedProcessOrder.setCreatedDateTime(existingProcessOrder.getCreatedDateTime());
            updatedProcessOrder.setModifiedDateTime(LocalDateTime.now());
            updatedProcessOrder.setModifiedBy(processOrderRequest.getUserId());

            ProcessOrder processOrderUpdate = processOrderRepository.save(updatedProcessOrder);

            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(processOrderRequest.getSite())
                    .change_stamp("Update")
                    .action_code("PROCESSORDER-UPDATE")
                    .action_detail("ProcessOrder Updated " + processOrderRequest.getOrderNumber())
                    .action_detail_handle("ActionDetailBO:" + processOrderRequest.getSite() + "," + "PROCESSORDER-UPDATE" + "," + processOrderRequest.getUserId() + ":" + "com.rits.processorderservice.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(processOrderRequest.getUserId())
                    .router(processOrderRequest.getRecipe())
                    .router_revision(processOrderRequest.getRecipeVersion())
                    .item(processOrderRequest.getMaterial())
                    .item_revision(processOrderRequest.getMaterialVersion())
                    .txnId("PROCESSORDER-UPDATE" + String.valueOf(LocalDateTime.now()) + processOrderRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("PROCESS_ORDER")
                    .build();

            webClientBuilder.build()
                    .post()
                    .uri(auditlogUrl)
                    .bodyValue(activityLog)
                    .retrieve()
                    .bodyToMono(AuditLogRequest.class)
                    .block();

            BigDecimal availableQty = processOrderRequest.getAvailableQtyToRelease();
            int qty = (availableQty != null) ? availableQty.intValue() : 0;
            ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                    .site(processOrderRequest.getSite())
                    .eventType("PROCESSORDER-UPDATE")
                    .userId(processOrderRequest.getUserId())
                    .batchNo(processOrderRequest.getBatchNumber().isEmpty() ? null : processOrderRequest.getBatchNumber().get(0).getBatchNumber())
                    .orderNumber(processOrderRequest.getOrderNumber())
                    .material(processOrderRequest.getMaterial())
                    .workcenterId(processOrderRequest.getWorkCenter())
                    .materialVersion(processOrderRequest.getMaterialVersion())
                    .qty(qty)
                    .topic("production-log")
                    .status("Active")
                    .eventData(processOrderRequest.getOrderNumber() + " Updated successfully")
                    .build();

            boolean productionLog = ProcessOrderUtility.productionLog(productionLogRequest);
            if(!productionLog){
                throw new ProcessOrderException(7024);
            }

            String updateMessage = getFormattedMessage(2, processOrderRequest.getOrderNumber());
            return ProcessOrderMessageModel.builder()
                    .message_details(new MessageDetails(updateMessage, "S"))
                    .processOrderResponse(processOrderUpdate)
                    .build();

        } else {
            throw new ProcessOrderException(4203, processOrderRequest.getOrderNumber());
        }
    }

    @Override
    public ProcessOrderMessageModel deleteProcessOrder(String site, String orderNumber, String userId) throws Exception {
        if (processOrderRepository.existsBySiteAndActiveAndOrderNumber(site, 1, orderNumber)) {
            ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndOrderNumber(site, 1, orderNumber);
            if (!existingProcessOrder.isInUse()) {
                existingProcessOrder.setActive(0);
                existingProcessOrder.setModifiedDateTime(LocalDateTime.now());
                existingProcessOrder.setModifiedBy(userId);
                processOrderRepository.save(existingProcessOrder);

                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(existingProcessOrder.getSite())
                        .change_stamp("Delete")
                        .action_code("PROCESSORDER-DELETE")
                        .action_detail("ProcessOrder Deleted " + orderNumber)
                        .action_detail_handle("ActionDetailBO:" + existingProcessOrder.getSite() + "," + "PROCESSORDER-DELETE" + "," + userId + ":" + "com.rits.processorderservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(userId)
                        .txnId("PROCESSORDER-DELETE" + String.valueOf(LocalDateTime.now()) + userId)
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("PROCESS_ORDER")
                        .build();

                webClientBuilder.build()
                        .post()
                        .uri(auditlogUrl)
                        .bodyValue(activityLog)
                        .retrieve()
                        .bodyToMono(AuditLogRequest.class)
                        .block();

                BigDecimal availableQty = existingProcessOrder.getAvailableQtyToRelease();
                int qty = (availableQty != null) ? availableQty.intValue() : 0;
                ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                        .site(existingProcessOrder.getSite())
                        .eventType("PROCESSORDER-CREATE")
                        .batchNo(existingProcessOrder.getBatchNumber().isEmpty() ? null : existingProcessOrder.getBatchNumber().get(0).getBatchNumber())
                        .orderNumber(existingProcessOrder.getOrderNumber())
                        .material(existingProcessOrder.getMaterial())
                        .workcenterId(existingProcessOrder.getWorkCenter())
                        .materialVersion(existingProcessOrder.getMaterialVersion())
                        .qty(qty)
                        .topic("production-log")
                        .status("Active")
                        .eventData(existingProcessOrder.getOrderNumber() + " Created successfully")
                        .build();

                boolean productionLog = ProcessOrderUtility.productionLog(productionLogRequest);
                if(!productionLog){
                    throw new ProcessOrderException(7024);
                }

                String deleteMessage = getFormattedMessage(3, orderNumber);
                return ProcessOrderMessageModel.builder().message_details(new MessageDetails(deleteMessage, "S")).build();
            }

            throw new ProcessOrderException(4234);
        }
        throw new ProcessOrderException(4203, orderNumber);
    }


    @Override
    public ProcessOrder retrieveProcessOrder(String site, String orderNumber) throws Exception {
        ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndOrderNumber(site, 1, orderNumber);
        if (existingProcessOrder == null) {
            throw new ProcessOrderException(4203, orderNumber);
        }
        return existingProcessOrder;
    }

//    public ProcessOrderResponseList getAllProcessOrder(String site, String orderNumber) throws Exception {
//        if(orderNumber != null && !orderNumber.isEmpty()){
//            ProcessOrder processOrderResponse = processOrderRepository.findBySiteAndActiveAndOrderNumber(site, 1, orderNumber);
//            if (processOrderResponse == null) {
//                return ProcessOrderResponseList.builder().processOrderResponseList(Collections.emptyList()).build();
//            }
//            String batchNumberString = processOrderResponse.getBatchNumber().stream()
//                    .map(BatchNumber::getBatchNumber) // Extract the batchNumber field from each BatchNumber object
//                    .filter(batchNumber -> batchNumber != null && !batchNumber.isEmpty()) // Remove any null/empty values
//                    .distinct() // Remove duplicates if needed
//                    .collect(Collectors.joining(", ")); // Join them into a single string with a comma separator
//
//            // Now, build the ProcessOrderResponse with the processed batchNumber string
//            ProcessOrderResponse processOrderResponseResult = ProcessOrderResponse.builder()
//                    .orderNumber(processOrderResponse.getOrderNumber())
//                    .material(processOrderResponse.getMaterial())
//                    .materialDescription(processOrderResponse.getMaterialDescription())
//                    .availableQtyToRelease(processOrderResponse.getAvailableQtyToRelease().toString())
//                    .orderType(processOrderResponse.getOrderType())
//                    .uom(processOrderResponse.getUom())
//                    .batchNumber(batchNumberString) // Set the batch numbers as a single string
//                    .productionVersion(processOrderResponse.getProductionVersion())
//                    .build();
//
//            // Return the ProcessOrderResponse wrapped inside a list (singleton list)
//            return ProcessOrderResponseList.builder()
//                    .processOrderResponseList(Collections.singletonList(processOrderResponseResult))
//                    .build();
//        }
//        return getAllProcessOrderByCreatedDate(site);
//
//    }

    public ProcessOrderResponseList getAllProcessOrder(String site, String orderNumber) throws Exception {
        if (orderNumber != null && !orderNumber.isEmpty()) {
            List<ProcessOrder> processOrderList = processOrderRepository.findBySiteAndActiveAndOrderNumberContainingIgnoreCase(site, 1, orderNumber);

            if (processOrderList == null || processOrderList.isEmpty()) {
                return ProcessOrderResponseList.builder()
                        .processOrderResponseList(Collections.emptyList())
                        .build();
            }

            processOrderList = processOrderList.stream()
                    .filter(order -> {
                        try {
                            return Double.parseDouble(String.valueOf(order.getAvailableQtyToRelease())) > 0;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            List<ProcessOrderResponse> processOrderResponses = processOrderList.stream().map(processOrder -> {
                String batchNumberString = processOrder.getBatchNumber().stream()
                        .map(BatchNumber::getBatchNumber)
                        .filter(batchNumber -> batchNumber != null && !batchNumber.isEmpty())
                        .distinct()
                        .collect(Collectors.joining(", "));

                return ProcessOrderResponse.builder()
                        .orderNumber(processOrder.getOrderNumber())
                        .material(processOrder.getMaterial())
                        .materialDescription(processOrder.getMaterialDescription())
                        .availableQtyToRelease(processOrder.getAvailableQtyToRelease().toString())
                        .orderType(processOrder.getOrderType())
                        .uom(processOrder.getUom())
                        .batchNumber(batchNumberString)
                        .productionVersion(processOrder.getProductionVersion())
                        .createdDateTime(processOrder.getCreatedDateTime())
                        .build();
            }).collect(Collectors.toList());

            return ProcessOrderResponseList.builder()
                    .processOrderResponseList(processOrderResponses)
                    .build();
        }
        return getAllProcessOrderByCreatedDate(site);
    }

    public ProcessOrderResponseList getAllProessOrders(String site, String orderNumber) throws Exception {
        List<ProcessOrder> processOrderList = StringUtils.hasText(orderNumber)
                ? processOrderRepository.findTop50BySiteAndOrderNumberContainingIgnoreCaseAndActive(site, orderNumber,1)
                : processOrderRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1,site);

        if (processOrderList == null || processOrderList.isEmpty()) {
            return ProcessOrderResponseList.builder()
                    .processOrderResponseList(Collections.emptyList())
                    .build();
        }

        List<ProcessOrderResponse> processOrderResponses = processOrderList.stream().map(processOrder -> {
            BatchNoRecipeHeaderReq batchNoRecipeHeaderReq = new BatchNoRecipeHeaderReq();
            batchNoRecipeHeaderReq.setSite(processOrder.getSite());
            batchNoRecipeHeaderReq.setOrderNo(processOrder.getOrderNumber());

            // Extracting batch numbers safely
            String batchNumberString = Optional.ofNullable(processOrder.getBatchNumber())
                    .filter(batchList -> !batchList.isEmpty())
                    .map(batchList -> batchList.stream()
                            .map(BatchNumber::getBatchNumber)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.joining(", ")))
                    .orElse("");

            batchNoRecipeHeaderReq.setBatchNo(batchNumberString);

            List<BatchNoRecipeHeader> batchNoRecipeHeaderList = Collections.emptyList();
            try {
                batchNoRecipeHeaderList = batchNoRecipeHeaderService.getBatchRecipeBySiteAndBatchAndOrder(batchNoRecipeHeaderReq);
            } catch (Exception e) {
                throw new RuntimeException("Error fetching batch recipe headers", e);
            }

            // Fetching orderReleasedTime safely
            LocalDateTime orderReleasedTime = batchNoRecipeHeaderList.stream()
                    .findFirst()
                    .map(BatchNoRecipeHeader::getCreatedDatetime)
                    .orElse(null);

            return ProcessOrderResponse.builder()
                    .orderNumber(processOrder.getOrderNumber())
                    .material(processOrder.getMaterial())
                    .materialDescription(processOrder.getMaterialDescription())
                    .orderReleasedTime(orderReleasedTime)
                    .availableQtyToRelease(Optional.ofNullable(processOrder.getAvailableQtyToRelease())
                            .map(Object::toString)
                            .orElse("0"))
                    .orderType(processOrder.getOrderType())
                    .uom(processOrder.getUom())
                    .batchNumber(batchNumberString)
                    .productionVersion(processOrder.getProductionVersion())
                    .build();
        }).collect(Collectors.toList());

        return ProcessOrderResponseList.builder()
                .processOrderResponseList(processOrderResponses)
                .build();
    }

    @Override
    public ProcessOrderResponseList getAllProcessOrderByCreatedDate(String site) throws Exception {
        // Fetch ProcessOrder objects from the database
        List<ProcessOrder> processOrders = processOrderRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1,site);
        processOrders= processOrders.stream()
                // Filter out orders where availableQtyToRelease is not greater than 0
                .filter(order -> {
                    try {
                        return Double.parseDouble(String.valueOf(order.getAvailableQtyToRelease())) > 0;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                // Sort by createdDateTime in descending order
                .sorted((o1, o2) -> o2.getCreatedDateTime().compareTo(o1.getCreatedDateTime()))
                // Limit to top 50 orders
                .limit(50)
                .collect(Collectors.toList());

        // Transform the data to ProcessOrderResponse
        List<ProcessOrderResponse> processOrderResponses = processOrders.stream()
                .map(processOrder -> {
                    // Extract only the batchNumber values
                    String batchNumberString = processOrder.getBatchNumber().stream()
                            .map(BatchNumber::getBatchNumber) // Extract the batchNumber field
                            .filter(batchNumber -> batchNumber != null && !batchNumber.isEmpty()) // Remove null/empty values
                            .distinct() // Remove duplicates if needed
                            .collect(Collectors.joining(", ")); // Join them with a comma

                    // Build the ProcessOrderResponse
                    return ProcessOrderResponse.builder()
                            .orderNumber(processOrder.getOrderNumber())
                            .material(processOrder.getMaterial())
                            .materialDescription(processOrder.getMaterialDescription())
                            .availableQtyToRelease(processOrder.getAvailableQtyToRelease().toString())
                            .orderType(processOrder.getOrderType())
                            .uom(processOrder.getUom())
                            .batchNumber(batchNumberString) // Set the batch numbers as a single string
                            .productionVersion(processOrder.getProductionVersion())
                            .createdDateTime(processOrder.getCreatedDateTime())
                            .build();
                })
                .collect(Collectors.toList()); // Use Collectors.toList() instead of .toList()

        // Return the response list
        return ProcessOrderResponseList.builder()
                .processOrderResponseList(processOrderResponses)
                .build();
    }

    @Override
    public ProcessOrderResponseList retrieveTop50OrderNos(String site) throws Exception {
        List<ProcessOrder> processOrders = processOrderRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);

        List<ProcessOrderResponse> processOrderResponses = processOrders.stream()
                .map(processOrder -> {

                    BatchNoRecipeHeaderReq batchNoRecipeHeaderReq = new BatchNoRecipeHeaderReq();
                    batchNoRecipeHeaderReq.setSite(processOrder.getSite());
                    batchNoRecipeHeaderReq.setOrderNo(processOrder.getOrderNumber());

                    // Avoid index errors by checking if batchNumber list is present
                    String batchNumberString = Optional.ofNullable(processOrder.getBatchNumber())
                            .filter(batchList -> !batchList.isEmpty())
                            .map(batchList -> batchList.stream()
                                    .map(BatchNumber::getBatchNumber)
                                    .filter(Objects::nonNull)
                                    .distinct()
                                    .collect(Collectors.joining(", ")))
                            .orElse(""); // Default empty string if no batch numbers

                    batchNoRecipeHeaderReq.setBatchNo(batchNumberString);

                    List<BatchNoRecipeHeader> batchNoRecipeHeaderList = Collections.emptyList();
                    try {
                        batchNoRecipeHeaderList = batchNoRecipeHeaderService.getBatchRecipeBySiteAndBatchAndOrder(batchNoRecipeHeaderReq);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    // Ensure releasedTime is safely retrieved as LocalDateTime
                    LocalDateTime orderReleasedTime = batchNoRecipeHeaderList.stream()
                            .findFirst()
                            .map(BatchNoRecipeHeader::getCreatedDatetime)
                            .orElse(null); // Returns null if no value is found

                    return ProcessOrderResponse.builder()
                            .orderNumber(processOrder.getOrderNumber())
                            .material(processOrder.getMaterial())
                            .materialDescription(processOrder.getMaterialDescription())
                            .orderReleasedTime(orderReleasedTime) // LocalDateTime handled properly
                            .availableQtyToRelease(Optional.ofNullable(processOrder.getAvailableQtyToRelease())
                                    .map(Object::toString)
                                    .orElse("0")) // Default to "0" if null
                            .orderType(processOrder.getOrderType())
                            .uom(processOrder.getUom())
                            .batchNumber(batchNumberString)
                            .productionVersion(processOrder.getProductionVersion())
                            .build();
                })
                .collect(Collectors.toList());

        return ProcessOrderResponseList.builder()
                .processOrderResponseList(processOrderResponses)
                .build();
    }

    @Override
    public ProcessOrderMessageModel saveBatchNumber(ProcessOrderRequest processOrderRequest) throws Exception {

        ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndOrderNumber(processOrderRequest.getSite(), 1, processOrderRequest.getOrderNumber());

        List<BatchNumber> bno = existingProcessOrder.getBatchNumber();
        List<ProcessOrder> processOrders = new ArrayList<>();
        Map<String, Integer> bnoNumberList = processOrderRequest.getBnoNumberList();
        for(String p: processOrderRequest.getNewBnos()) {
            for (BatchNumber serialBatchNumber : bno) {
                String s= "BnoBO:" + processOrderRequest.getSite() + "," +p;
                if (serialBatchNumber.getBatchNumber().equals(p)) {
                    serialBatchNumber.setBatchNumberQuantity(String.valueOf(bnoNumberList.get(s)));
                    serialBatchNumber.setState("Released");
                    serialBatchNumber.setEnabled(true);
                    bnoNumberList.remove(s);
                    break;
                }
            }
        }
        List<BatchNumber> existingSerialBnos = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : bnoNumberList.entrySet()) {
            Integer value = entry.getValue();
            String key = entry.getKey();
            String[] bnoArray = key.split(",");

            BatchNumber newSerialBatchNumber = new BatchNumber();
            newSerialBatchNumber.setBatchNumber(bnoArray[1]);
            newSerialBatchNumber.setBatchNumberQuantity(String.valueOf(value));
            newSerialBatchNumber.setState("Released");
            newSerialBatchNumber.setEnabled(true);

            existingSerialBnos.add(newSerialBatchNumber);
        }
        existingProcessOrder.setInUse(true);
        existingProcessOrder.getBatchNumber().addAll(existingSerialBnos);
        processOrders.add(existingProcessOrder);

        String updateMessage = getFormattedMessage(2,processOrderRequest.getOrderNumber());
        return ProcessOrderMessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).processOrderResponses(processOrderRepository.saveAll(processOrders)).build();
    }

    @Override
    public List<ProcessOrderList> findActiveProcessOrdersByDate(String site, LocalDateTime productionStartDate, LocalDateTime productionFinishDate) throws Exception {
        int active = 1;
        String status = "active";
        List<ProcessOrderList> activeProcessOrders;
        if (productionStartDate != null) {
            activeProcessOrders = processOrderRepository.findByActiveAndSiteAndStatusIgnoreCaseAndProductionStartDateAfter(active, site, status, productionStartDate);
            if(activeProcessOrders==null || activeProcessOrders.isEmpty()){
                throw new ProcessOrderException(4205);
            }
        } else if (productionFinishDate != null) {
            activeProcessOrders = processOrderRepository.findByActiveAndSiteAndStatusIgnoreCaseAndProductionFinishDateAfter(active, site, status, productionFinishDate);
            if(activeProcessOrders==null || activeProcessOrders.isEmpty()){
                throw new ProcessOrderException(4205);
            }
        }
        else{
            throw new ProcessOrderException(4206);
        }
        return activeProcessOrders;
    }

    @Override
    public ProcessOrder retrieveProcessOrderListUsingBno(String site, String sfcNumber) throws Exception {
        ProcessOrder processOrderList=processOrderRepository.findByActiveAndSiteAndBatchNumber_BatchNumber(1,site,sfcNumber);
        return processOrderList;
    }


    @Override
    public List<BatchNumber> getBnoList(String site, String orderNumber) throws Exception {
        ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndOrderNumber(site, 1,orderNumber);
        if(existingProcessOrder==null){
            throw new ProcessOrderException(4203,orderNumber);
        }
        return existingProcessOrder.getBatchNumber();
    }

    @Override
    public ProcessOrderResponse getBySiteAndOrderNumber(String site, String orderNumber) {
        return processOrderRepository.findBySiteAndOrderNumberAndActive(site, orderNumber, 1);
    }

    @Override
    public Boolean isProcessOrderExist(String site,String orderNumber) throws Exception
    {
        return processOrderRepository.existsBySiteAndActiveAndOrderNumber(site,1,orderNumber);
    }

    @Override
    public List<BatchNumber> updateBnoList(String site, String orderNumber, List<String> bnoList) throws Exception {
        ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndOrderNumber(site, 1, orderNumber);

        if (existingProcessOrder == null) {
            throw new ProcessOrderException(4203, orderNumber);
        }
        if (existingProcessOrder.getBatchNumber().isEmpty()) {
            throw new ProcessOrderException(4207);
        }
        List<BatchNumber> serialBnoToRemove = new ArrayList<>();
        for (BatchNumber serialBatchNumber : existingProcessOrder.getBatchNumber()) {
            if (bnoList.contains(serialBatchNumber.getSerialNumber())) {
                if (!serialBatchNumber.getState().equalsIgnoreCase("created")) {
                    throw new ProcessOrderException(3207, serialBatchNumber.getSerialNumber(), serialBatchNumber.getState());
                } else {
                    serialBnoToRemove.add(serialBatchNumber);
                }
            }
        }
        existingProcessOrder.getBatchNumber().removeAll(serialBnoToRemove);
        processOrderRepository.save(existingProcessOrder);
        return existingProcessOrder.getBatchNumber();
    }

    @Override
    public List<ProcessOrder> getProcessOrdersByMaterial(ProcessOrderRequest processOrderRequest) throws Exception {
        List<ProcessOrder> processOrderList = processOrderRepository.findByActiveAndSiteAndMaterialAndMaterialVersion(1, processOrderRequest.getSite(),processOrderRequest.getMaterial(), processOrderRequest.getMaterialVersion());
        if(processOrderList != null || !processOrderList.isEmpty()){
            return processOrderList;
        }
        throw new ProcessOrderException(6);
    }
    @Override
    public List<ProcessOrder> findProcessOrderBnoInWork(String site) throws Exception {
        List<ProcessOrder> processOrderList = processOrderRepository.findByActiveAndSiteAndInUse(1, site,true);
        if(processOrderList==null ||processOrderList.isEmpty()){
            throw new ProcessOrderException(4209);
        }
        return processOrderList;
    }

    @Override
    public List<ProcessOrder> getProcessOrdersByCriteria(
            String site,
            String orderNumber,
            String orderType,
            String recipe,
            String recipeVersion,
            String material,
            String materialVersion,
            LocalDateTime productionStartDate,
            LocalDateTime productionFinishDate,
            String workCenter) throws Exception {

        Query query = new Query();
        if (site != null && !site.isEmpty()) {
            query.addCriteria(Criteria.where("site").is(site));
        }

        if (orderNumber != null && !orderNumber.isEmpty()) {
            query.addCriteria(Criteria.where("orderNumber").is(orderNumber));
        }

        if (orderType != null && !orderType.isEmpty()) {
            query.addCriteria(Criteria.where("orderType").is(orderType));
        }

        if (material != null && !material.isEmpty()) {
            query.addCriteria(Criteria.where("material").is(material));

            if (materialVersion != null && !materialVersion.isEmpty()) {
                query.addCriteria(Criteria.where("materialVersion").is(materialVersion));
            }
        }
        if (recipe != null && !recipe.isEmpty()) {
            query.addCriteria(Criteria.where("recipe").is(recipe));

            if (recipeVersion != null && !recipeVersion.isEmpty()) {
                query.addCriteria(Criteria.where("recipeVersion").is(recipeVersion));
            }
        }

        if (productionStartDate != null) {
            query.addCriteria(Criteria.where("productionStartDate").gte(productionStartDate));
        }

        if (productionFinishDate != null) {
            query.addCriteria(Criteria.where("productionFinishDate").lte(productionFinishDate));
        }

        if (workCenter != null && !workCenter.isEmpty()) {
            query.addCriteria(Criteria.where("workCenter").is(workCenter));
        }


        List<ProcessOrder> processOrderList = mongoTemplate.find(query, ProcessOrder.class);
        return processOrderList;
    }


    public void validations(ProcessOrderRequest processOrderRequest) throws Exception
    {
        if (StringUtils.hasText(processOrderRequest.getMaterial()) && StringUtils.hasText(processOrderRequest.getMaterialVersion())) {
            Boolean itemExist = isItemExist(processOrderRequest.getSite(),processOrderRequest.getMaterial(),processOrderRequest.getMaterialVersion());
            if (!itemExist) {
                throw new ProcessOrderException(300, processOrderRequest.getMaterial(),processOrderRequest.getMaterialVersion());
            }
        }
        if(StringUtils.hasText(processOrderRequest.getMaterial()) && !StringUtils.hasText(processOrderRequest.getMaterialVersion())){
            throw new ProcessOrderException(300,processOrderRequest.getMaterial(),processOrderRequest.getMaterialVersion());
        }

        if (StringUtils.hasText(processOrderRequest.getRecipe())) {
            Boolean recipeExist = isRecipeExist(processOrderRequest.getSite(),processOrderRequest.getRecipe(),processOrderRequest.getRecipeVersion());
            if (!recipeExist) {
                throw new ProcessOrderException(4213, processOrderRequest.getRecipe(), processOrderRequest.getRecipeVersion());
            }
        }
        if(StringUtils.hasText(processOrderRequest.getRecipe()) && !StringUtils.hasText(processOrderRequest.getRecipeVersion())){
            throw new ProcessOrderException(4213, processOrderRequest.getRecipe());
        }

        if (StringUtils.hasText(processOrderRequest.getWorkCenter())) {
            Boolean workCenterExist = isWorkCenterExist(processOrderRequest.getSite(),processOrderRequest.getWorkCenter());
            if (!workCenterExist) {
                throw new ProcessOrderException(600, processOrderRequest.getWorkCenter());
            }
        }

        if(processOrderRequest.getBatchNumber()!=null&& !processOrderRequest.getBatchNumber().isEmpty()){
            for(BatchNumber serialBatchNumber: processOrderRequest.getBatchNumber())
            {
                if(serialBatchNumber.getState().equals("New")) {
//                    String bnoBO = "BnoBO:" + processOrderRequest.getSite() + "," + serialBatchNumber.getBatchNumber();
                    Boolean batchNoHeaderExist = isBatchNoHeaderExist(processOrderRequest.getSite(),serialBatchNumber.getBatchNumber());
                    if (batchNoHeaderExist != null && batchNoHeaderExist) {
                        throw new ProcessOrderException(4212, serialBatchNumber.getBatchNumber());
                    }
                }
            }
        }
        if(processOrderRequest.getProductionStartDate()!=null && processOrderRequest.getProductionFinishDate()!=null)
        {
            if (!processOrderRequest.getProductionStartDate().isBefore(processOrderRequest.getProductionFinishDate())) {
                throw new ProcessOrderException(4210);
            }
        }
        if(processOrderRequest.getSchedStartTime()!=null && processOrderRequest.getSchedFinTime()!=null)
        {
            if (!processOrderRequest.getSchedStartTime().isBefore(processOrderRequest.getSchedFinTime())) {
                throw new ProcessOrderException(4211);
            }
        }
    }

//        @Override
//    public ProcessOrderMessageModel saveBatchNumber(ProcessOrderRequest processOrderRequest) throws Exception {
//
//        ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndOrderNumber(processOrderRequest.getSite(), 1, processOrderRequest.getOrderNumber());
//
//        List<BatchNumber> pcu = existingProcessOrder.getBatchNumber();
//        List<ProcessOrder> processOrders = new ArrayList<>();
//        Map<String, Integer> pcuNumberList = processOrderRequest.getPcuNumberList();
//        for(String p: processOrderRequest.getNewPcus()) {
//            for (BatchNumber serialBatchNumber : pcu) {
//                String s= "PcuBO:" + processOrderRequest.getSite() + "," +p;
//                if (serialBatchNumber.getBatchNumber().equals(p)) {
//                    serialBatchNumber.setBatchNumberQuantity(String.valueOf(pcuNumberList.get(s)));
//                    serialBatchNumber.setState("Released");
//                    serialBatchNumber.setEnabled(true);
//                    pcuNumberList.remove(s);
//                    break;
//                }
//            }
//        }
//        List<BatchNumber> existingSerialPcus = new ArrayList<>();
//        for (Map.Entry<String, Integer> entry : pcuNumberList.entrySet()) {
//            Integer value = entry.getValue();
//            String key = entry.getKey();
//            String[] pcuArray = key.split(",");
//
//            BatchNumber newSerialBatchNumber = new BatchNumber();
//            newSerialBatchNumber.setBatchNumber(pcuArray[1]);
//            newSerialBatchNumber.setBatchNumberQuantity(String.valueOf(value));
//            newSerialBatchNumber.setState("Released");
//            newSerialBatchNumber.setEnabled(true);
//
//            existingSerialPcus.add(newSerialBatchNumber);
//        }
//        //existingProcessOrder.setAvailableQtyToRelease(String.valueOf(Integer.parseInt(existingProcessOrder.getBuildQty()) - Double.parseDouble(processOrderRequest.getProductionQuantities().get(0).getReleasedQty())));
//       // existingProcessOrder.setProductionQuantities(processOrderRequest.getProductionQuantities());
//        existingProcessOrder.setInUse(true);
//        existingProcessOrder.getBatchNumber().addAll(existingSerialPcus);
//        processOrders.add(existingProcessOrder);
//
//        String updateMessage = getFormattedMessage(2,processOrderRequest.getOrderNumber());
//       return ProcessOrderMessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).processOrderResponses(processOrderRepository.saveAll(processOrders)).build();
//    }
//

//    @Override
//    public List<ProcessOrderList> findActiveProcessOrdersByDate(String site, LocalDateTime plannedStart, LocalDateTime plannedCompletion) throws Exception {
//        int active = 1;
//        String status = "active";
//        List<ProcessOrderList> activeProcessOrders;
//        if (plannedStart != null) {
//            activeProcessOrders = processOrderRepository.findByActiveAndSiteAndStatusIgnoreCaseAndPlannedStartAfter(active, site, status, plannedStart);
//            if(activeProcessOrders==null || activeProcessOrders.isEmpty()){
//                throw new ProcessOrderException(4205);
//            }
//        } else if (plannedCompletion != null) {
//            activeProcessOrders = processOrderRepository.findByActiveAndSiteAndStatusIgnoreCaseAndPlannedCompletionAfter(active, site, status, plannedCompletion);
//            if(activeProcessOrders==null || activeProcessOrders.isEmpty()){
//                throw new ProcessOrderException(4205);
//            }
//        }
//        else{
//            throw new ProcessOrderException(3203);
//        }
//        return activeProcessOrders;
//    }

//    @Override
//    public ProcessOrder retrieveProcessOrderListUsingSFCNumber(String site, String sfcNumber) throws Exception {
//        ProcessOrder processOrderList=processOrderRepository.findByActiveAndSiteAndBatchNumber(1,site,sfcNumber);
//        return processOrderList;
//    }

//    @Override
//    public ProcessOrderMessageModel createProcessOrder(ProcessOrderRequest processOrderRequest) throws Exception {
//        if (processOrderRepository.existsBySiteAndActiveAndProcessOrder(processOrderRequest.getSite(), 1, processOrderRequest.getProcessOrder())) {
//            throw new ProcessOrderException(4201, processOrderRequest.getProcessOrder());
//        } else {
//            if(processOrderRequest.getProcessOrder()==null|| processOrderRequest.getProcessOrder().isEmpty()){
//                String nextNumberResponse = sampleNextNumber(processOrderRequest.getSite(),processOrderRequest.getParentOrder());
//                if (nextNumberResponse==null || nextNumberResponse.isEmpty()) {
//                    throw new ProcessOrderException(5100, processOrderRequest.getParentOrder());
//                }
//                processOrderRequest.setProcessOrder(nextNumberResponse);
//            }
//            validations(processOrderRequest);
//            ProcessOrder processOrder = processOrderBuilder(processOrderRequest);
//            processOrder.setCreatedDateTime(LocalDateTime.now());
//            processOrder.setParentOrderBO(processOrderRequest.getParentOrderBO());
//            processOrder.setParentPcuBO(processOrderRequest.getParentPcuBO());
//            processOrder.setAvailableQtyToRelease(processOrderRequest.getBuildQty());
//            if(processOrder.getProductionQuantities().size()!=0) {
//                processOrder.getProductionQuantities().get(0).setReleasedQty("0");
//            }
//            processOrder.setCreatedBy(processOrderRequest.getUserId());
//            processOrder.setProcessOrderBO("ProcessOrderBO:"+processOrderRequest.getSite()+","+processOrderRequest.getProcessOrder());
//            ProcessOrder processOrderCreate = processOrderRepository.save(processOrder);
//
//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(processOrderRequest.getSite())
//                    .change_stamp("Create")
//                    .action_code("PROCESSORDER-CREATE")
//                    .action_detail("PocessOrder Created "+processOrderRequest.getProcessOrder())
//                    .action_detail_handle("ActionDetailBO:"+processOrderRequest.getSite()+","+"PROCESSORDER-CREATE"+","+processOrderRequest.getUserId()+":"+"com.rits.processorderservice.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(processOrderRequest.getUserId())
//                    .router(processOrderRequest.getPlannedRouting())
//                    .router_revision(processOrderRequest.getRoutingVersion())
//                    .item(processOrderRequest.getPlannedMaterial())
//                    .item_revision(processOrderRequest.getMaterialVersion())
//                    .txnId("PROCESSORDER-CREATE"+String.valueOf(LocalDateTime.now())+processOrderRequest.getUserId())
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .category("PROCESS_ORDER")
//                    .build();
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();
//            String createdMessage = getFormattedMessage(1, processOrderRequest.getProcessOrder());
//            return ProcessOrderMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).processOrderResponse(processOrderCreate).build();
//        }
//    }
//
//
//
//    @Override
//    public ProcessOrderMessageModel updateProcessOrder(ProcessOrderRequest processOrderRequest) throws Exception {
//        if (processOrderRepository.existsBySiteAndActiveAndProcessOrder(processOrderRequest.getSite(), 1, processOrderRequest.getProcessOrder())) {
//            ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndProcessOrder(processOrderRequest.getSite(), 1, processOrderRequest.getProcessOrder());
//            if (existingProcessOrder.isInUse()) {
//                throw new ProcessOrderException(4223);
//            }
//                validations(processOrderRequest);
//            ProcessOrder processOrder = processOrderBuilder(processOrderRequest);
//            processOrder.setSite(existingProcessOrder.getSite());
//            processOrder.setHandle(existingProcessOrder.getHandle());
//            processOrder.setProcessOrder(existingProcessOrder.getProcessOrder());
//            processOrder.setParentOrderBO(existingProcessOrder.getParentOrderBO());
//            processOrder.setParentPcuBO(existingProcessOrder.getParentPcuBO());
//            if(processOrder.getProductionQuantities().size()!=0) {
//                processOrder.setAvailableQtyToRelease(String.valueOf(Double.parseDouble(processOrderRequest.getBuildQty()) - Double.parseDouble(processOrderRequest.getProductionQuantities().get(0).getReleasedQty())));
//            }processOrder.setCreatedBy(existingProcessOrder.getCreatedBy());
//            processOrder.setCreatedDateTime(existingProcessOrder.getCreatedDateTime());
//            processOrder.setModifiedDateTime(LocalDateTime.now());
//            processOrder.setModifiedBy(processOrderRequest.getUserId());
//
//            ProcessOrder processOrderUpdate = processOrderRepository.save(processOrder);
//
//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(processOrderRequest.getSite())
//                    .change_stamp("Update")
//                    .action_code("PROCESSORDER-UPDATE")
//                    .action_detail("ProcessOrder Updated "+processOrderRequest.getProcessOrder())
//                    .action_detail_handle("ActionDetailBO:"+processOrderRequest.getSite()+","+"PROCESSORDER-UPDATE"+","+processOrderRequest.getUserId()+":"+"com.rits.processorderservice.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(processOrderRequest.getUserId())
//                    .router(processOrderRequest.getPlannedRouting())
//                    .router_revision(processOrderRequest.getRoutingVersion())
//                    .item(processOrderRequest.getPlannedMaterial())
//                    .item_revision(processOrderRequest.getMaterialVersion())
//                    .txnId("PROCESSORDER-UPDATE"+String.valueOf(LocalDateTime.now())+processOrderRequest.getUserId())
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .category("PROCESS_ORDER")
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();
//                String updateMessage = getFormattedMessage(2,processOrderRequest.getProcessOrder());
//                return ProcessOrderMessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).processOrderResponse(processOrderUpdate).build();
//
//            } else {
//                throw new ProcessOrderException(4202, processOrderRequest.getProcessOrder());
//            }
//    }

//    public void validations(ProcessOrderRequest processOrderRequest) throws Exception
//    {
//        if (processOrderRequest.getPlannedBom() != null && !processOrderRequest.getPlannedBom().isEmpty() && processOrderRequest.getBomVersion()!=null && !processOrderRequest.getBomVersion().isEmpty()) {
//            Boolean bomExist = isBomExist(processOrderRequest.getSite(),processOrderRequest.getPlannedBom(),processOrderRequest.getBomVersion());
//            if (!bomExist) {
//                throw new ProcessOrderException(200, processOrderRequest.getPlannedBom());
//            }
//        }
//        if(processOrderRequest.getPlannedBom() != null && !processOrderRequest.getPlannedBom().isEmpty() && (processOrderRequest.getBomVersion()==null || processOrderRequest.getBomVersion().isEmpty())){
//            throw new ProcessOrderException(200,processOrderRequest.getPlannedBom(),processOrderRequest.getBomVersion());
//        }
//        if (processOrderRequest.getPlannedMaterial() != null && !processOrderRequest.getPlannedMaterial().isEmpty() && processOrderRequest.getMaterialVersion()!=null && !processOrderRequest.getMaterialVersion().isEmpty()) {
//            Boolean itemExist = isItemExist(processOrderRequest.getSite(),processOrderRequest.getPlannedMaterial(),processOrderRequest.getMaterialVersion());
//            if (!itemExist) {
//                throw new ProcessOrderException(100, processOrderRequest.getPlannedMaterial(),processOrderRequest.getMaterialVersion());
//            }
//        }
//        if(processOrderRequest.getPlannedMaterial() != null && !processOrderRequest.getPlannedMaterial().isEmpty() && (processOrderRequest.getMaterialVersion()==null || processOrderRequest.getMaterialVersion().isEmpty())){
//            throw new ProcessOrderException(100,processOrderRequest.getPlannedMaterial(),processOrderRequest.getMaterialVersion());
//        }
//        if (processOrderRequest.getPlannedRouting() != null && !processOrderRequest.getPlannedRouting().isEmpty() && processOrderRequest.getPlannedRouting()!=null && !processOrderRequest.getPlannedRouting().isEmpty()) {
//            Boolean routingExist = isRoutingExist(processOrderRequest.getSite(),processOrderRequest.getPlannedRouting(),processOrderRequest.getRoutingVersion());
//            if (!routingExist) {
//                throw new ProcessOrderException(500, processOrderRequest.getPlannedRouting());
//            }
//        }
//        if(processOrderRequest.getPlannedRouting() != null && !processOrderRequest.getPlannedRouting().isEmpty() && (processOrderRequest.getRoutingVersion()==null || processOrderRequest.getRoutingVersion().isEmpty())){
//            throw new ProcessOrderException(500, processOrderRequest.getPlannedRouting(),processOrderRequest.getRoutingVersion());
//        }
//        if (processOrderRequest.getPlannedWorkCenter() != null && !processOrderRequest.getPlannedWorkCenter().isEmpty()) {
//            Boolean workCenterExist = isWorkCenterExist(processOrderRequest.getSite(),processOrderRequest.getPlannedWorkCenter());
//            if (!workCenterExist) {
//                throw new ProcessOrderException(600, processOrderRequest.getPlannedWorkCenter());
//            }
//        }
//        if(processOrderRequest.getProductionQuantities().size()!=0) {
//            if (processOrderRequest.getProductionQuantities().get(0).getReleasedQty() == null || processOrderRequest.getProductionQuantities().get(0).getReleasedQty().isEmpty()) {
//                processOrderRequest.getProductionQuantities().get(0).setReleasedQty("0");
//            }
//        }
//        if(processOrderRequest.getSerialBatchNumber()!=null&& !processOrderRequest.getSerialBatchNumber().isEmpty()){
//            for(BatchNumber serialBatchNumber: processOrderRequest.getSerialBatchNumber())
//            {
//                if(serialBatchNumber.getState().equals("New")) {
//                    String pcuBO = "PcuBO:" + processOrderRequest.getSite() + "," + serialBatchNumber.getBno();
//                    Boolean pcuHeaderResponse = isPcuHeaderExist(processOrderRequest.getSite(),pcuBO);
//                    if (pcuHeaderResponse != null && pcuHeaderResponse) {
//                        throw new ProcessOrderException(8, serialBatchNumber.getBno());
//                    }
//                }
//            }
//        }
//        if(processOrderRequest.getPlannedStart()!=null && processOrderRequest.getPlannedCompletion()!=null)
//        {
//            if (!processOrderRequest.getPlannedStart().isBefore(processOrderRequest.getPlannedCompletion())) {
//                throw new ProcessOrderException(3226);
//            }
//        }
//        if(processOrderRequest.getScheduledStart()!=null && processOrderRequest.getScheduledEnd()!=null)
//        {
//            if (!processOrderRequest.getScheduledStart().isBefore(processOrderRequest.getScheduledEnd())) {
//                throw new ProcessOrderException(3227);
//            }
//        }
//    }

    public String sampleNextNumber(String site,String parentOrder)throws Exception
    {
        NextNumber nextNumberRequest= NextNumber.builder().site(site).numberType("Process Order").parentOrder(parentOrder).build();
        String nextNumberResponse = webClientBuilder.build()
                .post()
                .uri(nextNumberUrl)
                .bodyValue(nextNumberRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return nextNumberResponse;
    }
    public Boolean isBomExist(String site,String bom,String revision) throws Exception
    {
        IsExist isExist = IsExist.builder().site(site).bom(bom).revision(revision).build();
        Boolean bomExist = webClientBuilder.build()
                .post()
                .uri(bomUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return bomExist;
    }
    public Boolean isItemExist(String site,String item,String revision) throws Exception
    {
        IsExist isExist = IsExist.builder().site(site).item(item).revision(revision).build();
        Boolean itemExist = webClientBuilder.build()
                .post()
                .uri(itemUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return itemExist;
    }
    public Boolean isRoutingExist(String site,String routing,String version) throws Exception
    {
        IsExist isExist = IsExist.builder().site(site).routing(routing).version(version).build();
        Boolean routingExist = webClientBuilder.build()
                .post()
                .uri(routingUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return routingExist;
    }

    public Boolean isRecipeExist(String site,String recipeName,String version) throws Exception
    {
        IsExist isExist = IsExist.builder().site(site).recipeId(recipeName).version(version).build();
        Boolean routingExist = webClientBuilder.build()
                .post()
                .uri(recipeUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return routingExist;
    }
    public Boolean isWorkCenterExist(String site,String workCenter) throws Exception
    {
        IsExist isExist = IsExist.builder().site(site).workCenter(workCenter).build();
        Boolean workCenterExist = webClientBuilder.build()
                .post()
                .uri(workCenterUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return workCenterExist;
    }
    public Boolean isBatchNoHeaderExist(String site,String batchNo) throws Exception
    {
        PcuHeaderRequest batchNoRequest = PcuHeaderRequest.builder().site(site).batchNumber(batchNo).build();
        Boolean pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(readBatchNoUrl)
                .bodyValue(batchNoRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return pcuHeaderResponse;
    }

    public ProcessOrder processOrderBuilder(ProcessOrderRequest processOrderRequest)
    {
        Integer targetQuantity = processOrderRequest.getTargetQuantity();

        BigDecimal availableQty = targetQuantity != null
                ? BigDecimal.valueOf(targetQuantity)
                : BigDecimal.ZERO;

        BigDecimal qty = (availableQty != null) ? availableQty : BigDecimal.valueOf(0);

        ProcessOrder processOrder = ProcessOrder.builder()
                .site(processOrderRequest.getSite())
                .handle("OrderNo:" + processOrderRequest.getSite() + "," + processOrderRequest.getMaterial() + "," + processOrderRequest.getOrderNumber())
                .orderNumber(processOrderRequest.getOrderNumber())
                .status(processOrderRequest.getStatus())
                .orderType(processOrderRequest.getOrderType())
                .productionVersion(processOrderRequest.getProductionVersion())
                .material(processOrderRequest.getMaterial())
                .materialDescription(processOrderRequest.getMaterialDescription())
                .targetQuantity(processOrderRequest.getTargetQuantity())
                .uom(processOrderRequest.getUom())
                .finishDate(processOrderRequest.getFinishDate())
                .startDate(processOrderRequest.getStartDate())
                .materialVersion(processOrderRequest.getMaterialVersion())
                .recipe(processOrderRequest.getRecipe())
                .recipeVersion(processOrderRequest.getRecipeVersion())
                .priority(processOrderRequest.getPriority())
                .schedStartTime(processOrderRequest.getSchedStartTime())
                .schedFinTime(processOrderRequest.getSchedFinTime())
                .batchNumber(processOrderRequest.getBatchNumber())
                .availableQtyToRelease(qty)
                .conversionFactor(processOrderRequest.getConversionFactor())
                .active(1)
                .productionVersion(processOrderRequest.getProductionVersion())
                .inUse(processOrderRequest.isInUse())
                .build();
        return processOrder;
    }

//    @Override
//    public ProcessOrderMessageModel saveSerialBatchNumber(ProcessOrderRequest processOrderRequest) throws Exception {
//
//        ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndProcessOrder(processOrderRequest.getSite(), 1, processOrderRequest.getProcessOrder());
//
//        List<BatchNumber> pcu = existingProcessOrder.getSerialBatchNumber();
//        List<ProcessOrder> processOrders = new ArrayList<>();
//        Map<String, Integer> pcuNumberList = processOrderRequest.getPcuNumberList();
//        for(String p: processOrderRequest.getNewPcus()) {
//            for (BatchNumber serialBatchNumber : pcu) {
//                String s= "PcuBO:" + processOrderRequest.getSite() + "," +p;
//                if (serialBatchNumber.getBno().equals(p)) {
//                    serialBatchNumber.setBnoQuantity(String.valueOf(pcuNumberList.get(s)));
//                    serialBatchNumber.setState("Released");
//                    serialBatchNumber.setEnabled(true);
//                    pcuNumberList.remove(s);
//                    break;
//                }
//            }
//        }
//        List<BatchNumber> existingSerialPcus = new ArrayList<>();
//        for (Map.Entry<String, Integer> entry : pcuNumberList.entrySet()) {
//            Integer value = entry.getValue();
//            String key = entry.getKey();
//            String[] pcuArray = key.split(",");
//
//            BatchNumber newSerialBatchNumber = new BatchNumber();
//            newSerialBatchNumber.setBno(pcuArray[1]);
//            newSerialBatchNumber.setBnoQuantity(String.valueOf(value));
//            newSerialBatchNumber.setState("Released");
//            newSerialBatchNumber.setEnabled(true);
//
//            existingSerialPcus.add(newSerialBatchNumber);
//        }
//        existingProcessOrder.setAvailableQtyToRelease(String.valueOf(Integer.parseInt(existingProcessOrder.getBuildQty()) - Double.parseDouble(processOrderRequest.getProductionQuantities().get(0).getReleasedQty())));
//        existingProcessOrder.setProductionQuantities(processOrderRequest.getProductionQuantities());
//        existingProcessOrder.setInUse(true);
//        existingProcessOrder.getSerialBatchNumber().addAll(existingSerialPcus);
//        processOrders.add(existingProcessOrder);
//
//        String updateMessage = getFormattedMessage(2,processOrderRequest.getProcessOrder());
//       return ProcessOrderMessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).processOrderResponses(processOrderRepository.saveAll(processOrders)).build();
//    }
//


//   @Override
//   public Boolean isProcessOrderExist(String site,String processOrder) throws Exception
//   {
//      return processOrderRepository.existsBySiteAndActiveAndProcessOrder(site,1,processOrder);
//   }
//
//
//    @Override
//    public ProcessOrder retrieveProcessOrder(String site, String processOrder) throws Exception {
//        ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndProcessOrder(site, 1, processOrder);
//        if (existingProcessOrder == null) {
//            throw new ProcessOrderException(4202, processOrder);
//        }
//        return existingProcessOrder;
//    }


//    @Override
//    public ProcessOrderResponseList getAllProcessOrder(String site, String processOrder) throws Exception {
//        if (processOrder != null && !processOrder.isEmpty()) {
//            List<ProcessOrderResponse> processOrderResponses = processOrderRepository.findByActiveAndSiteAndProcessOrderContainingIgnoreCase(1, site, processOrder);
//            if (processOrderResponses.isEmpty()) {
//                throw new ProcessOrderException(4202, processOrder);
//            }
//            return ProcessOrderResponseList.builder().processOrderResponseList(processOrderResponses).build();
//        }
//        return getAllProcessOrderByCreatedDate(site);
//    }
//


//    @Override
//    public ProcessOrderResponseList getAllProcessOrderByCreatedDate(String site) throws Exception {
//        List<ProcessOrderResponse> processOrderResponses = processOrderRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
//        return ProcessOrderResponseList.builder().processOrderResponseList(processOrderResponses).build();
//    }
//
//
//
//    @Override
//    public ProcessOrderMessageModel deleteProcessOrder(String site, String processOrder, String userId) throws Exception {
//        if (processOrderRepository.existsBySiteAndActiveAndProcessOrder(site, 1, processOrder)) {
//            ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndProcessOrder(site, 1, processOrder);
//            if(!existingProcessOrder.isInUse()) {
//                existingProcessOrder.setActive(0);
//                existingProcessOrder.setModifiedDateTime(LocalDateTime.now());
//                existingProcessOrder.setModifiedBy(userId);
//                processOrderRepository.save(existingProcessOrder);
//                AuditLogRequest activityLog = AuditLogRequest.builder()
//                        .site(site)
//                        .change_stamp("Update")
//                        .action_code("PROCESSODER-UPDATE")
//                        .action_detail("ProcessOrder Updated " + processOrder)
//                        .action_detail_handle("ActionDetailBO:" + site + "," + "PROCESSORDER-UPDATE" + "," + userId + ":" + "com.rits.processorderservice.service")
//                        .date_time(String.valueOf(LocalDateTime.now()))
//                        .userId(userId)
//                        .txnId("PROCESSORDER-UPDATE" + String.valueOf(LocalDateTime.now()) + userId)
//                        .created_date_time(String.valueOf(LocalDateTime.now()))
//                        .category("PROCESS_ORDER")
//                        .build();
//
//                webClientBuilder.build()
//                        .post()
//                        .uri(auditlogUrl)
//                        .bodyValue(activityLog)
//                        .retrieve()
//                        .bodyToMono(AuditLogRequest.class)
//                        .block();
//                String deleteMessage = getFormattedMessage(3, processOrder);
//                return ProcessOrderMessageModel.builder().message_details(new MessageDetails(deleteMessage, "S")).build();
//            }
//            throw new ProcessOrderException(4234);
//        }
//        throw new ProcessOrderException(4202,processOrder);
//    }
//
//
//
////    @Override
////    public String callExtension(Extension extension) throws Exception {
////        String extensionResponse = webClientBuilder.build()
////                .post()
////                .uri(extensionUrl)
////                .bodyValue(extension)
////                .retrieve()
////                .bodyToMono(String.class)
////                .block();
////        if (extensionResponse == null) {
////            throw new ProcessOrderException(800);
////        }
////        return extensionResponse;
////    }
//
//
//
//
//    @Override
//    public List<ProcessOrderList> findActiveProcessOrdersByDate(String site, LocalDateTime plannedStart, LocalDateTime plannedCompletion) throws Exception {
//        int active = 1;
//        String status = "active";
//        List<ProcessOrderList> activeProcessOrders;
//        if (plannedStart != null) {
//            activeProcessOrders = processOrderRepository.findByActiveAndSiteAndStatusIgnoreCaseAndPlannedStartAfter(active, site, status, plannedStart);
//            if(activeProcessOrders==null || activeProcessOrders.isEmpty()){
//                throw new ProcessOrderException(4205);
//            }
//        } else if (plannedCompletion != null) {
//            activeProcessOrders = processOrderRepository.findByActiveAndSiteAndStatusIgnoreCaseAndPlannedCompletionAfter(active, site, status, plannedCompletion);
//            if(activeProcessOrders==null || activeProcessOrders.isEmpty()){
//                throw new ProcessOrderException(4205);
//            }
//        }
//        else{
//            throw new ProcessOrderException(3203);
//        }
//        return activeProcessOrders;
//    }
//
//
//    @Override
//    public List<ProcessOrder> findProcessOrderPcuInWork(String site) throws Exception {
//        List<ProcessOrder> processOrderList = processOrderRepository.findByActiveAndSiteAndInUse(1, site,true);
//        if(processOrderList==null ||processOrderList.isEmpty()){
//            throw new ProcessOrderException(4208);
//        }
//        return processOrderList;
//    }
//
//    @Override
//    public List<ProcessOrder> getProcessOrdersByMaterial(ProcessOrderRequest processOrderRequest) throws Exception {
//        List<ProcessOrder> processOrderList = processOrderRepository.findByActiveAndSiteAndPlannedMaterialAndMaterialVersion(1, processOrderRequest.getSite(),processOrderRequest.getPlannedMaterial(), processOrderRequest.getMaterialVersion());
//        if(processOrderList != null || !processOrderList.isEmpty()){
//            return processOrderList;
//        }
//        throw new ProcessOrderException(6);
//    }
//
//
//    @Override
//    public List<BatchNumber> getSerialNumberList(String site, String processOrder) throws Exception {
//        ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndProcessOrder(site, 1,processOrder);
//        if(existingProcessOrder==null){
//            throw new ProcessOrderException(4202,processOrder);
//        }
//        return existingProcessOrder.getSerialBatchNumber();
//    }
//
//
//
//    @Override
//    public List<BatchNumber> updateSerialNumberList(String site, String processOrder, List<String> serialNumberList) throws Exception {
//        ProcessOrder existingProcessOrder = processOrderRepository.findBySiteAndActiveAndProcessOrder(site, 1, processOrder);
//
//
//
//        if (existingProcessOrder == null) {
//            throw new ProcessOrderException(4202, processOrder);
//        }
//        if (existingProcessOrder.getSerialBatchNumber().isEmpty()) {
//            throw new ProcessOrderException(3206);
//        }
//        List<BatchNumber> serialBnoToRemove = new ArrayList<>();
//        for (BatchNumber serialBatchNumber : existingProcessOrder.getSerialBatchNumber()) {
//            if (serialNumberList.contains(serialBatchNumber.getSerialBatchNumber())) {
//                if (!serialBatchNumber.getState().equalsIgnoreCase("created")) {
//                    throw new ProcessOrderException(3207, serialBatchNumber.getSerialBatchNumber(), serialBatchNumber.getState());
//                } else {
//                    serialBnoToRemove.add(serialBatchNumber);
//                }
//            }
//        }
//        existingProcessOrder.getSerialBatchNumber().removeAll(serialBnoToRemove);
//        processOrderRepository.save(existingProcessOrder);
//        return existingProcessOrder.getSerialBatchNumber();
//    }
//
//
//
//    @Override
//    public ProcessOrder retrieveProcessOrderListUsingSFCNumber(String site, String sfcNumber) throws Exception {
//        ProcessOrder processOrderList=processOrderRepository.findByActiveAndSiteAndSerialBatchNumber_bno(1,site,sfcNumber);
//        return processOrderList;
//    }
//    @Override
//    public List<ProcessOrder> getProcessOrdersByCriteria(
//            String site,
//            String processOrder,
//            String orderType,
//            String routing ,
//            String routingVersion,
//            String material,
//            String materialVersion ,
//            LocalDateTime plannedStart,
//            LocalDateTime plannedCompletion,
//            String workCenter) throws Exception {
//
//
//
//        Query query = new Query();
//        if (site != null && !site.isEmpty()) {
//            query.addCriteria(Criteria.where("site").is(site));
//        }
//
//
//
//        if (processOrder != null && !processOrder.isEmpty()) {
//            query.addCriteria(Criteria.where("processOrder").is(processOrder));
//        }
//
//
//
//        if (orderType != null && !orderType.isEmpty()) {
//            query.addCriteria(Criteria.where("orderType").is(orderType));
//        }
//
//
//
//        if (material != null && !material.isEmpty()) {
//            query.addCriteria(Criteria.where("plannedMaterial").is(material));
//
//
//
//            if (materialVersion != null && !materialVersion.isEmpty()) {
//                query.addCriteria(Criteria.where("materialVersion").is(materialVersion));
//            }
//        }
//        if (routing != null && !routing.isEmpty()) {
//            query.addCriteria(Criteria.where("plannedRouting").is(routing));
//
//
//
//            if (routingVersion != null && !routingVersion.isEmpty()) {
//                query.addCriteria(Criteria.where("routingVersion").is(routingVersion));
//            }
//        }
//
//
//
//        if (plannedStart != null) {
//            query.addCriteria(Criteria.where("plannedStart").gte(plannedStart));
//        }
//
//
//
//        if (plannedCompletion != null) {
//            query.addCriteria(Criteria.where("plannedCompletion").lte(plannedCompletion));
//        }
//
//
//
//        if (workCenter != null && !workCenter.isEmpty()) {
//            query.addCriteria(Criteria.where("plannedWorkCenter").is(workCenter));
//        }
//
//
//
//        List<ProcessOrder> processOrderList = mongoTemplate.find(query, ProcessOrder.class);
//        return processOrderList;
//    }
//



}