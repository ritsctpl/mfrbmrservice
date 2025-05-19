package com.rits.productionlogservice.service;

import com.rits.productionlogservice.dto.*;
import com.rits.productionlogservice.model.ProductionLog;
import com.rits.productionlogservice.model.ProductionLogMongo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductionLogService {
    Boolean create(ProductionLogRequest productionLogRequest) throws Exception;

    List<ProductionLog> retrieveByDateAndEventType(String eventType, LocalDateTime time) throws Exception;

    List<ProductionLog> retrieveByPcuItemShopOrder(String pcu, String shopOrder, String item, String itemVersion) throws Exception;
    List<ProductionLog> retrieveForProcess(ProductionLogRequest productionLogRequest) throws Exception;
    List<ProductionLog> retrieveForDiscrete(ProductionLogRequest productionLogRequest) throws Exception;

    List<ProductionLog> retrieveByPcuForWorkinstruction(String site, String pcu);

    ProductionLog retrieveByPcuAndOperationAndShopOrderAndEventType(String pcu, String operation, String operationVersion, String shopOrder, String eventType, String site) throws Exception;

    ProductionLog retrieveFirstPcuStarted(String pcu, String shopOrderBO, String eventType, String site) throws Exception;

    ActualCycleSum retrieveAllByOperationShopOrderAndEventType(String pcu, String shopOrderBO, String operation, String operationVersion, String eventType) throws Exception;
    public List<ProductionQueryResponse> getTotalQuantityWithFields(ProductionLogQueryRequest productionLogRequest) throws Exception;
    public List<ProductionQueryResponse> getTotalQuantityForDoneWithFields(ProductionLogQueryRequest productionLogRequest) throws Exception;

    public List<ProductionQueryResponse> getTotalQuantity(LocalDateTime startTime, LocalDateTime endTime, String resource, String site, String shift);
    public List<ProductionQueryResponse> getTotalQuantityForDone(LocalDateTime startTime, LocalDateTime endTime, String resource, String site, String shift);
    public List<ProductionQueryResponse> getTotalQuantityForDoneForMaterialAndResource(LocalDateTime startTime, LocalDateTime endTime, String resource, String site, String shift);

    List<ProductionLogQualityResponse> getSumOfScrapQtyAndTotalProducedQty(ProductionLogRequest productionLogRequest)throws Exception;

    Integer getTotalDownTimeEventForADay(String site, LocalDate startDate, LocalDate endDate, String resource) throws Exception;
    List<ProductionQuality> getCalculatedQuality(ProductionLogRequest productionLogRequest) throws Exception;

    List<ProductionLogDto> getUniqueCombinations(ProductionLogRequest productionLogRequest) throws Exception;
    List<ScrapAndReworkTrendResponse.ScrapAndReworkTrend> getScrapAndReworkForResource(OeeFilterRequest qualityFilter) throws Exception;

    List<ProductionLogResponseDto> calculateActualCycleTimeAndActualQuantity(ProductionLogRequest productionLogRequest) throws Exception ;


    List<OeeProductionLogResponse>  getTotalProducedQuantity(OeeProductionLogRequest request);
    List<ProductionLog> getEventsByInterval(LocalDateTime startTime, LocalDateTime endTime,String site);
    List<ProductionLog> getTotalProducedQuantity(ProductionLogRequest ProductionLog);
    List<ProductionLog> getProductionLogByEventType(String site,
                                                    LocalDateTime startDate,
                                                    LocalDateTime endDate,String eventType);

    public List<ProductionLogServiceImpl.UniqueItemVersion> getUniqueItemVersions(OeeProductionLogRequest request);
    List<ProductionLogServiceImpl.UniqueItemVersion> getUniqueItemVersionsByWorkCenter(OeeProductionLogRequest request);

    LocalDateTime getFirstCreatedDateTimeByOrder(String event_type, String shop_order_bo, LocalDateTime start, LocalDateTime end);

    LocalDateTime getFirstCreatedDateTimeByOperation(String event_type, String operation, String operation_version, LocalDateTime start, LocalDateTime end);

    LocalDateTime getFirstCreatedDateTimeByBatch(String event_type, String batchNo, LocalDateTime start, LocalDateTime end);

    LocalDateTime getLastCreatedDateTimeByOrder(String event_type, String shop_order_bo, LocalDateTime start, LocalDateTime end);

    LocalDateTime getLastCreatedDateTimeByOperation(String event_type,  String operation, String operation_version, LocalDateTime start, LocalDateTime end);

    LocalDateTime getLastCreatedDateTimeByBatch(String event_type, String batchNo, LocalDateTime start, LocalDateTime end);

    // 7. Get first created_datetime by event_type, item, and item_version.
    LocalDateTime getFirstCreatedDateTimeByItem(String eventType, String item, String itemVersion,
                                                LocalDateTime start, LocalDateTime end);

    // 8. Get last created_datetime by event_type, item, and item_version.
    LocalDateTime getLastCreatedDateTimeByItem(String eventType, String item, String itemVersion,
                                               LocalDateTime start, LocalDateTime end);


    // 7. Get first created_datetime by event_type, item, item_version, operation, and operation_version.
    LocalDateTime getFirstCreatedDateTimeByItemAndOperation(String eventType, String item, String itemVersion,
                                                            String operation, String operationVersion,
                                                            LocalDateTime start, LocalDateTime end);

    // 8. Get last created_datetime by event_type, item, item_version, operation, and operation_version.
    LocalDateTime getLastCreatedDateTimeByItemAndOperation(String eventType, String item, String itemVersion,
                                                           String operation, String operationVersion,
                                                           LocalDateTime start, LocalDateTime end);

    LocalDateTime getFirstCreatedDateTimeByCriteria(String site,
                                                    String resourceId,
                                                    String eventType,
                                                    String shopOrderBo,
                                                    String batchNo,
                                                    String workcenterId,
                                                    String operation,
                                                    String operationVersion,
                                                    String item,
                                                    String itemVersion,
                                                    LocalDateTime start,
                                                    LocalDateTime end);

    LocalDateTime getLastCreatedDateTimeByCriteria(String site,
                                                   String resourceId,
                                                   String eventType,
                                                   String shopOrderBo,
                                                   String batchNo,
                                                   String workcenterId,
                                                   String operation,
                                                   String operationVersion,
                                                   String item,
                                                   String itemVersion,
                                                   LocalDateTime start,
                                                   LocalDateTime end);
    public String getCycleTime(String batchNo, String site);
    List<ProductionLog> retrieveBySiteAndOrderNoAndPhaseAndOperationAndEventType(String site, String orderNumber, String phaseId, String operationId, String eventType);
}

