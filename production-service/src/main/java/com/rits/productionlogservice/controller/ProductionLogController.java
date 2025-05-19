package com.rits.productionlogservice.controller;

import com.rits.Utility.BOConverter;
import com.rits.productionlogservice.dto.*;
import com.rits.productionlogservice.exception.ProductionLogException;
import com.rits.productionlogservice.model.ProductionLog;
import com.rits.productionlogservice.model.ProductionLogMongo;
import com.rits.productionlogservice.service.ProductionLogService;
import com.rits.productionlogservice.service.ProductionLogServiceImpl;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/productionlog-service")
public class ProductionLogController {
    private final ProductionLogService productionLogService;
    @PostMapping("/save")
    public Boolean create(@RequestBody ProductionLogRequest productionLogRequest)
    {
        try {
            return productionLogService.create(productionLogRequest);
        } catch (ProductionLogException productionLogException) {
            throw productionLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveByDateTime")
    public List<ProductionLog> retrieve(@RequestBody ProductionLogRequest productionLogRequest)
    {
        if(productionLogRequest.getSite() != null && !productionLogRequest.getSite().isEmpty()) {
            try {
                return productionLogService.retrieveByDateAndEventType(productionLogRequest.getEventType(), productionLogRequest.getCreatedDatetime());
            } catch (ProductionLogException productionLogException) {
                throw productionLogException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProductionLogException(113);
    }

    @PostMapping("retrieve")
    public List<ProductionLog> retrieveByPcuItemShopOrder(@RequestBody ProductionLogRequest productionLogRequest)
    {
        if(productionLogRequest.getSite() != null && !productionLogRequest.getSite().isEmpty()) {
            try {
                return productionLogService.retrieveByPcuItemShopOrder(productionLogRequest.getPcu(),productionLogRequest.getShopOrderBO(), BOConverter.getItem(productionLogRequest.getItemBO()), BOConverter.getItemVersion(productionLogRequest.getItemBO()));
            } catch (ProductionLogException productionLogException) {
                throw productionLogException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProductionLogException(113);
    }

    @PostMapping("retrieveByType")
    public List<ProductionLog> retrieveByType(@RequestBody ProductionLogRequest productionLogRequest)
    {
        if(StringUtils.isBlank(productionLogRequest.getSite()) || StringUtils.isBlank(productionLogRequest.getType()))
            throw new ProductionLogException(335);

        try {
            if(productionLogRequest.getType().equalsIgnoreCase("discrete"))
                return productionLogService.retrieveForDiscrete(productionLogRequest);
            else if(productionLogRequest.getType().equalsIgnoreCase("process"))
                return productionLogService.retrieveForProcess(productionLogRequest);
            else
                return new ArrayList<>();

        } catch (ProductionLogException productionLogException) {
            throw productionLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveByPcuOperationShopOrderAndEventType")
    public ProductionLog retrieveByPcuAndOperationAndShopOrderAndEventType(@RequestBody ProductionLogRequest productionLogRequest)
    {
        if(productionLogRequest.getSite() != null && !productionLogRequest.getSite().isEmpty()) {
            try {
                return productionLogService.retrieveByPcuAndOperationAndShopOrderAndEventType(productionLogRequest.getPcu(),productionLogRequest.getOperation(), productionLogRequest.getOperationVersion(), productionLogRequest.getShopOrderBO(),productionLogRequest.getEventType(),productionLogRequest.getSite());
            } catch (ProductionLogException productionLogException) {
                throw productionLogException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProductionLogException(113);
    }

    @PostMapping("retrieveFirstPcuRecord")
    public ProductionLog retrieveFirstPcuRecord(@RequestBody ProductionLogRequest productionLogRequest)
    {
        if(productionLogRequest.getSite() != null && !productionLogRequest.getSite().isEmpty()) {
            try {
                return productionLogService.retrieveFirstPcuStarted(productionLogRequest.getPcu(), productionLogRequest.getShopOrderBO(), productionLogRequest.getEventType(), productionLogRequest.getSite());
            } catch (ProductionLogException productionLogException) {
                throw productionLogException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProductionLogException(113);
    }

    @PostMapping("retrieveAllByEventTypeShopOrderOperationItem")
    public ActualCycleSum retrieveAllByEventTypeAndOperationAndShopOrder(@RequestBody ProductionLogRequest productionLogRequest)
    {
        if(productionLogRequest.getSite() != null && !productionLogRequest.getSite().isEmpty()) {
            try {
                return productionLogService.retrieveAllByOperationShopOrderAndEventType(productionLogRequest.getPcu(), productionLogRequest.getShopOrderBO(), BOConverter.getOperation(productionLogRequest.getOperation_bo()), BOConverter.getOperationVersion(productionLogRequest.getOperation_bo()), productionLogRequest.getEventType());
            } catch (ProductionLogException productionLogException) {
                throw productionLogException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProductionLogException(113);
    }

    @PostMapping("getTotalScrapAndCompletedQty")
    public List<ProductionLogQualityResponse> getSumOfScrapQtyAndTotalProducedQty(@RequestBody ProductionLogRequest productionLogRequest)
    {
        try {
            return productionLogService.getSumOfScrapQtyAndTotalProducedQty(productionLogRequest);
        } catch (ProductionLogException productionLogException) {
            throw productionLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getTotalQuantity")
    public List<ProductionQueryResponse> getTotalQuantity(@RequestBody ProductionLogQueryRequest productionLogRequest)
    {
        try {
            return productionLogService.getTotalQuantity(productionLogRequest.getStartTime(),productionLogRequest.getEndTime(),productionLogRequest.getResource(),productionLogRequest.getSite(),productionLogRequest.getShift());
        } catch (ProductionLogException productionLogException) {
            throw productionLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("getTotalQuantityWithFields")
    public List<ProductionQueryResponse> getTotalQuantityForFields(@RequestBody ProductionLogQueryRequest productionLogRequest)
    {
        try {
            return productionLogService.getTotalQuantityWithFields(productionLogRequest);
        } catch (ProductionLogException productionLogException) {
            throw productionLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("getTotalQuantityForDoneWithFields")
    public List<ProductionQueryResponse> getTotalQuantityForDoneWithFields(@RequestBody ProductionLogQueryRequest productionLogRequest)
    {
        try {
            return productionLogService.getTotalQuantityForDoneWithFields(productionLogRequest);
        } catch (ProductionLogException productionLogException) {
            throw productionLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("getTotalQuantityForDone")
    public List<ProductionQueryResponse> getTotalQuantityForDone(@RequestBody ProductionLogQueryRequest productionLogRequest)
    {
        try {
            return productionLogService.getTotalQuantityForDone(productionLogRequest.getStartTime(),productionLogRequest.getEndTime(),productionLogRequest.getResource(), productionLogRequest.getSite(),productionLogRequest.getShift());
        } catch (ProductionLogException productionLogException) {
            throw productionLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("getTotalQuantityForDoneByMaterialResource")
    public List<ProductionQueryResponse> getTotalQuantityForDoneByMaterialResource(@RequestBody ProductionLogQueryRequest productionLogRequest)
    {
        try {
            return productionLogService.getTotalQuantityForDoneForMaterialAndResource(productionLogRequest.getStartTime(),productionLogRequest.getEndTime(),productionLogRequest.getResource(), productionLogRequest.getSite(),productionLogRequest.getShift());
        } catch (ProductionLogException productionLogException) {
            throw productionLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveByPcuForWorkInstruction")
    public List<ProductionLog> getRecordsByPcuForWorkInstruction(@RequestBody ProductionLogRequest productionLogRequest)
    {
        if(productionLogRequest.getSite() != null && !productionLogRequest.getSite().isEmpty()) {
            try {
                return productionLogService.retrieveByPcuForWorkinstruction(productionLogRequest.getSite(), productionLogRequest.getPcu());
            } catch (ProductionLogException productionLogException) {
                throw productionLogException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProductionLogException(113);
    }

//    @PostMapping("getTotalDownTimeCount")
//    public Integer getTotalDownTimeEventCoubtForADay (@RequestBody ProductionLogRequest productionLogRequest)
//    {
//        try {
//            return productionLogService.getTotalDownTimeEventForADay(productionLogRequest.getSite(),productionLogRequest.getStartDate(),productionLogRequest.getEndDate(),productionLogRequest.getResourceId());
//        } catch (ProductionLogException productionLogException) {
//            throw productionLogException;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @PostMapping("getCalculatedQuality")
    public List<ProductionQuality> getCalculatedQuality(@RequestBody ProductionLogRequest productionLogRequest)
    {
        try {
            return productionLogService.getCalculatedQuality(productionLogRequest);
        } catch (ProductionLogException productionLogException) {
            throw productionLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/getUniqueCombinations")
    public ResponseEntity<List<ProductionLogDto>> getAvailableProductionTime(@RequestBody ProductionLogRequest productionLogRequest) {
        if(productionLogRequest.getSite()!=null && !productionLogRequest.getSite().isEmpty()){
            try{
                List<ProductionLogDto> response =productionLogService.getUniqueCombinations(productionLogRequest);
                return ResponseEntity.ok(response);
            }catch (ProductionLogException productionLogException){
                throw productionLogException;
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        throw new ProductionLogException(1);
    }

    @PostMapping("getActualCycleTimeAndActualQuantity")
    public List<ProductionLogResponseDto> getActualCycleTimeAndActualQuantity(@RequestBody ProductionLogRequest productionLogRequest)
    {
        try {
            return productionLogService.calculateActualCycleTimeAndActualQuantity(productionLogRequest);
        } catch (ProductionLogException productionLogException) {
            throw productionLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getScrapAndReworkForResource")
    public List<ScrapAndReworkTrendResponse.ScrapAndReworkTrend> getScrapAndReworkForResource(@RequestBody OeeFilterRequest qualityFilter)
    {
        try {
            return productionLogService.getScrapAndReworkForResource(qualityFilter);
        } catch (ProductionLogException productionLogException) {
            throw productionLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/totalProducedQuantity")
    public ResponseEntity<List<OeeProductionLogResponse>> getTotalProducedQuantity(
            @RequestBody OeeProductionLogRequest request) {
        List<OeeProductionLogResponse> response = productionLogService.getTotalProducedQuantity(request);
        if (response == null) {
            response = new ArrayList<>(); // Return an empty list if the service returns null
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/uniqueItemVersions")
    public ResponseEntity<List<ProductionLogServiceImpl.UniqueItemVersion>> getUniqueItemVersions(
            @RequestBody OeeProductionLogRequest request) {
        List<ProductionLogServiceImpl.UniqueItemVersion> uniqueItemVersions =
                productionLogService.getUniqueItemVersions(request);
        return ResponseEntity.ok(uniqueItemVersions);
    }

    @PostMapping("/uniqueItemVersionsByWc")
    public ResponseEntity<List<ProductionLogServiceImpl.UniqueItemVersion>> getUniqueItemVersionsByWc(
            @RequestBody OeeProductionLogRequest request) {
        List<ProductionLogServiceImpl.UniqueItemVersion> uniqueItemVersions =
                productionLogService.getUniqueItemVersionsByWorkCenter(request);
        return ResponseEntity.ok(uniqueItemVersions);
    }

    @PostMapping("getByInterval")
    public List<ProductionLog> getEventsByInterval(@RequestBody ProductionLogRequest productionLogRequest)
    {
        LocalDateTime start = productionLogRequest.getStartDateTime();
        LocalDateTime end = productionLogRequest.getEndDateTime();
        List<ProductionLog> events = productionLogService.getEventsByInterval(start, end,productionLogRequest.getSite());
        return events;
    }
    @PostMapping("getTotalProducedQuantity")
    public List<ProductionLog> getTotalProducedQuantity(@RequestBody ProductionLogRequest productionLogRequest)
    {

        List<ProductionLog> events = productionLogService.getTotalProducedQuantity(productionLogRequest);
        return events;
    }

    @PostMapping("/getProductionLogByEventType")
    public List<ProductionLog> getPartsToBeProduced(
            @RequestBody ProductionLogRequest productionLogRequest) {
        return productionLogService.getProductionLogByEventType(productionLogRequest.getSite() ,productionLogRequest.getStartDateTime(),productionLogRequest.getEndDateTime(),productionLogRequest.getEventType());
    }

    // 1. First created datetime by order
    @PostMapping("getFirstDtByOrder")
    public LocalDateTime getFirstCreatedDateTimeByOrder(@RequestBody OeeProductionLogRequest request) {
        return productionLogService.getFirstCreatedDateTimeByOrder(
                request.getEventType(),
                request.getShopOrderBo(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );
    }
    // 2. First created datetime by batch and operation
    @PostMapping("getFirstDtByOperation")
    public LocalDateTime getFirstCreatedDateTimeByOperation(@RequestBody OeeProductionLogRequest request) {
        return productionLogService.getFirstCreatedDateTimeByOperation(
                request.getEventType(),
                request.getOperationId(),
                request.getOperationVersion(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );
    }

    // 3. First created datetime by batch
    @PostMapping("getFirstDtByBatch")
    public LocalDateTime getFirstCreatedDateTimeByBatch(@RequestBody OeeProductionLogRequest request) {
        return productionLogService.getFirstCreatedDateTimeByBatch(
                request.getEventType(),
                request.getBatchNo(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );
    }

    // 4. Last created datetime by order
    @PostMapping("getLastDtByOrder")
    public LocalDateTime getLastCreatedDateTimeByOrder(@RequestBody OeeProductionLogRequest request) {
        return productionLogService.getLastCreatedDateTimeByOrder(
                request.getEventType(),
                request.getShopOrderBo(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );
    }

    // 5. Last created datetime by batch and operation
    @PostMapping("getLastDtByOperation")
    public LocalDateTime getLastCreatedDateTimeByOperation(@RequestBody OeeProductionLogRequest request) {
        return productionLogService.getLastCreatedDateTimeByOperation(
                request.getEventType(),
                request.getOperationId(),
                request.getOperationVersion(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );
    }



    // 6. Last created datetime by batch
    @PostMapping("getLastDtByBatch")
    public LocalDateTime getLastCreatedDateTimeByBatch(@RequestBody OeeProductionLogRequest request) {
        return productionLogService.getLastCreatedDateTimeByBatch(
                request.getEventType(),
                request.getBatchNo(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );
    }


    // 7. First created datetime by item (material-based)
    @PostMapping("getFirstDtbyItem")
    public LocalDateTime getFirstCreatedDateTimeByItem(@RequestBody OeeProductionLogRequest request) {
        return productionLogService.getFirstCreatedDateTimeByItem(
                request.getEventType(),
                request.getItemId(),         // Assuming itemId maps to ProductionLog.item
                request.getItemVersion(),    // maps to ProductionLog.item_version
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );
    }

    // 8. Last created datetime by item (material-based)
    @PostMapping("getLastDtbyItem")
    public LocalDateTime getLastCreatedDateTimeByItem(@RequestBody OeeProductionLogRequest request) {
        return productionLogService.getLastCreatedDateTimeByItem(
                request.getEventType(),
                request.getItemId(),
                request.getItemVersion(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );
    }

    // 7. First created datetime by item and operation (material-based with operation)
    @PostMapping("getFirstDtByItemOperation")
    public LocalDateTime getFirstCreatedDateTimeByItemAndOperation(@RequestBody OeeProductionLogRequest request) {
        return productionLogService.getFirstCreatedDateTimeByItemAndOperation(
                request.getEventType(),
                request.getItemId(),         // Maps to ProductionLog.item
                request.getItemVersion(),    // Maps to ProductionLog.item_version
                request.getOperationId(),    // Maps to ProductionLog.operation
                request.getOperationVersion(), // Maps to ProductionLog.operation_version
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );
    }

    // 8. Last created datetime by item and operation (material-based with operation)
    @PostMapping("getLastDtByItemOperation")
    public LocalDateTime getLastCreatedDateTimeByItemAndOperation(@RequestBody OeeProductionLogRequest request) {
        return productionLogService.getLastCreatedDateTimeByItemAndOperation(
                request.getEventType(),
                request.getItemId(),
                request.getItemVersion(),
                request.getOperationId(),
                request.getOperationVersion(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );
    }

    @PostMapping("/getIntervalTimes")
    public IntervalTimesResponse getIntervalTimes(@RequestBody OeeProductionLogRequest request) {
        // Map fields from request to the generic repository criteria.
        // For clarity:
        // - site, resourceId, eventType, shopOrderBo, batchNo, workcenterId, operation, operationVersion,
        //   item (from request.getItemId()), and itemVersion (from request.getItemVersion())
        LocalDateTime first = productionLogService.getFirstCreatedDateTimeByCriteria(
                request.getSite(),
                request.getResourceId(),
                request.getEventType(),
                request.getShopOrderBo(),
                request.getBatchNo(),
                request.getWorkcenterId(),
                request.getOperationId(),
                request.getOperationVersion(),
                request.getItemId(),
                request.getItemVersion(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );

        LocalDateTime last = productionLogService.getLastCreatedDateTimeByCriteria(
                request.getSite(),
                request.getResourceId(),
                request.getEventType(),
                request.getShopOrderBo(),
                request.getBatchNo(),
                request.getWorkcenterId(),
                request.getOperationId(),
                request.getOperationVersion(),
                request.getItemId(),
                request.getItemVersion(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );

        return new IntervalTimesResponse(first, last);
    }
    @PostMapping("/getBatchActualCyleTime")
    public String getCycleTime(@RequestBody OeeProductionLogRequest request) {

        try {
            String cycleTimeInSeconds = productionLogService.getCycleTime(request.getBatchNo(), request.getSite());
            return cycleTimeInSeconds;
        } catch (RuntimeException e) {
            return e.toString();
        }
    }
}
