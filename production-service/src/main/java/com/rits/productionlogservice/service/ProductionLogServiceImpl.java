package com.rits.productionlogservice.service;

import com.rits.Utility.BOConverter;
import com.rits.batchnoheader.dto.BatchNoHeaderRequest;
import com.rits.batchnoheader.model.BatchNoHeader;
import com.rits.batchnoheader.service.BatchNoHeaderService;
import com.rits.batchnorecipeheaderservice.exception.BatchNoRecipeHeaderException;
import com.rits.batchnorecipeheaderservice.service.BatchNoRecipeHeaderService;
import com.rits.pcuinqueueservice.exception.PcuInQueueException;
import com.rits.productionlogservice.dto.*;

import com.rits.productionlogservice.event.Message;
import com.rits.productionlogservice.exception.ProductionLogException;
import com.rits.productionlogservice.model.ProductionLog;
import com.rits.productionlogservice.model.ProductionLogMongo;
import com.rits.productionlogservice.producer.ProductionLogProducer;
import com.rits.productionlogservice.repository.ProductionLogMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import org.springframework.data.mongodb.core.query.Criteria;
import com.rits.productionlogservice.repository.ProductionLogRepository;
import com.rits.startservice.dto.ShiftRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;



@Service
@RequiredArgsConstructor
public class ProductionLogServiceImpl implements ProductionLogService{
    private final ProductionLogRepository productionLogRepository;
    private final ProductionLogMongoRepository productionLogMongoRepository;
    private final MongoTemplate mongoTemplate;
    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher applicationEventPublisher;
    @Value("${operation-service.url}/retrieveOperationByCurrentVersion")
    private String retrieveOperationByCurrentVersionUrl;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Value("${shift-service.url}/getShiftDetailsByShiftType")
    private String getShiftDetailsByShiftTypeUrl;

    @Value("${performance-service.url}/performanceUniqueComb")
    private String performanceUniqueCombUrl;
    @Value("${shift-service.url}/getShiftDetailBetweenTime")
    private String getShiftDetailBetweenTime;
    @Value("${shift-service.url}/getShiftBetweenDates")
    private String getShiftBetweenDatesUrl;

    @Value("${cycletime-service.url}/getPlannedCycleTime")
    private String getPlannedCycleTimeUrl;
    @Value("${availability.url}/getAvailabilityRec")
    private String getAvailabilityRecUrl;
    @Value("${cycletime-service.url}/getCycleTimeRecs")
    private String getCycleTimeRecsUrl;
    @Value("${workcenter-service.url}/retrieve")
    private String retrieveWorkcenter;
    @Value("${batchnoheader-service.url}/getBatchHeader")
    private String getBatchHeader;
    @Value("${cycletime-service.url}/getCycletimesByResourceAndItem")
    private String getCycletimesByResourceAndItem;

    @Value("${workcenter-service.url}/getWorkCenterByResource")
    private String getWorkcenterbyResource;

    private final BatchNoHeaderService batchNoHeaderService;
    @Override
    public Boolean create(ProductionLogRequest productionLogRequest) throws Exception
    {
        String resource = productionLogRequest.getResourceId();
        BreakMinutes shiftRecordList = null;
        // If workcenterId is null or empty, fetch it using resourceId
        if (productionLogRequest.getWorkcenterId() == null || productionLogRequest.getWorkcenterId().trim().isEmpty()) {
            String resolvedWorkCenter = getWorkCenterFromService(productionLogRequest.getSite(), resource);
            productionLogRequest.setWorkcenterId(resolvedWorkCenter);
        }


//        if(productionLogRequest.getResourceId() != null && !productionLogRequest.getResourceId().isEmpty())
//        {
//            String [] resourceId = productionLogRequest.getResourceId().split(",");
//
//            resource = resourceId[1];
//
//        }


        ShiftRequest retrieveShiftRequest = ShiftRequest.builder()
                .site(productionLogRequest.getSite())
                .resourceId(resource)
                .workCenterId(productionLogRequest.getWorkcenterId())
                .build();
        shiftRecordList = getShiftDetailsByShiftType(retrieveShiftRequest);
        Double plannedCycleTime = 0.0;
        Double actualCycleTime = 0.0;
        Double manufactureTime =0.0;

        if(productionLogRequest.getEventType().equalsIgnoreCase("BATCH_COMPLETED")) {
            ProductionLogDto retrievePlannedCycleTime = ProductionLogDto.builder()
                    .site(productionLogRequest.getSite() != null ? productionLogRequest.getSite() : null)
                    .workcenter_id(productionLogRequest.getWorkcenterId() != null ? productionLogRequest.getWorkcenterId() : null)
                    .operation(productionLogRequest.getOperation_bo() != null ? BOConverter.getOperation(productionLogRequest.getOperation_bo()) : null)
                    .operation_version(productionLogRequest.getOperation_bo() != null ? BOConverter.getOperationVersion(productionLogRequest.getOperation_bo()) : null)
                    .resource_id(productionLogRequest.getResourceId() != null ? productionLogRequest.getResourceId() : null)
                    .item(productionLogRequest.getItemBO() != null ? BOConverter.getItem(productionLogRequest.getItemBO()) : null)
                    .item_version(productionLogRequest.getItemBO() != null ? BOConverter.getItemVersion(productionLogRequest.getItemBO()) : null)
                    .shift_id(shiftRecordList != null && shiftRecordList.getShiftId() != null ? shiftRecordList.getShiftId() : null)
                    .pcu(productionLogRequest.getPcu() != null ? productionLogRequest.getPcu() : null)
                    .build();
            plannedCycleTime = getPlannedCycleTime(retrievePlannedCycleTime);

            actualCycleTime = productionLogRepository.calculateActualCycleTime(
                    productionLogRequest.getSite() != null ? productionLogRequest.getSite() : null,
                    shiftRecordList != null && shiftRecordList.getShiftId() != null ? shiftRecordList.getShiftId() : null,
                    productionLogRequest.getWorkcenterId() != null ? productionLogRequest.getWorkcenterId() : null,
                    productionLogRequest.getResourceId() != null ? productionLogRequest.getResourceId() : null,
                    productionLogRequest.getPcu() != null ? productionLogRequest.getPcu() : null,
                    productionLogRequest.getOperation_bo() != null ? BOConverter.getOperation(productionLogRequest.getOperation_bo()) : null,
                    productionLogRequest.getOperation_bo() != null ? BOConverter.getOperationVersion(productionLogRequest.getOperation_bo()) : null,
                    productionLogRequest.getItemBO() != null ? BOConverter.getItem(productionLogRequest.getItemBO()) : null,
                    productionLogRequest.getItemBO() != null ? BOConverter.getItemVersion(productionLogRequest.getItemBO()) : null
            );

            manufactureTime = productionLogRepository.calculateManufacturedTime(
                    productionLogRequest.getSite() != null ? productionLogRequest.getSite() : null,
                    shiftRecordList != null && shiftRecordList.getShiftId() != null ? shiftRecordList.getShiftId() : null,
                    productionLogRequest.getWorkcenterId() != null ? productionLogRequest.getWorkcenterId() : null,
                    productionLogRequest.getResourceId() != null ? productionLogRequest.getResourceId() : null,
                    productionLogRequest.getPcu() != null ? productionLogRequest.getPcu() : null,
                    productionLogRequest.getOperation_bo() != null ? BOConverter.getOperation(productionLogRequest.getOperation_bo()) : null,
                    productionLogRequest.getOperation_bo() != null ? BOConverter.getOperationVersion(productionLogRequest.getOperation_bo()) : null,
                    productionLogRequest.getItemBO() != null ? BOConverter.getItem(productionLogRequest.getItemBO()) : null,
                    productionLogRequest.getItemBO() != null ? BOConverter.getItemVersion(productionLogRequest.getItemBO()) : null
            );
        }

        if(productionLogRequest.getEventType().equalsIgnoreCase("MC_UP"))
        {
            Boolean isRecordLogged = getShiftAndLogForMacUpMacDown(productionLogRequest.getSite(),resource);
        }

        //mongodb
        int secondsSinceEpoch = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        ProductionLogMongo createProductionlogMongo = ProductionLogMongo.builder()
                .eventId(String.valueOf(secondsSinceEpoch))
                .eventType(productionLogRequest.getEventType())
                .eventData(productionLogRequest.getEventData())
                .eventDatetime(productionLogRequest.getEventDatetime() != null ? productionLogRequest.getEventDatetime() : LocalDateTime.now())
                .userId(productionLogRequest.getUserId())
                .pcu(productionLogRequest.getPcu())
                .shoporderBo(productionLogRequest.getShopOrderBO())
                .workcenterId(productionLogRequest.getWorkcenterId())
                .resourceId(productionLogRequest.getResourceId())
                .operation(productionLogRequest.getOperation_bo() != null ? BOConverter.getOperation(productionLogRequest.getOperation_bo()) : productionLogRequest.getOperation())
                .operationVersion(productionLogRequest.getOperation_bo() != null ? BOConverter.getOperationVersion(productionLogRequest.getOperation_bo()) : "")
                .routerBo(productionLogRequest.getRouterBO() != null ? BOConverter.getRouter(productionLogRequest.getRouterBO()) : null)
                .routerVersion(productionLogRequest.getRouterBO() != null ? BOConverter.getRouterVersion(productionLogRequest.getRouterBO()) : null)
                .item(productionLogRequest.getItemBO() != null ? BOConverter.getItem(productionLogRequest.getItemBO()) : productionLogRequest.getMaterial())
                .itemVersion(productionLogRequest.getItemBO() != null ? BOConverter.getItemVersion(productionLogRequest.getItemBO()) : productionLogRequest.getMaterialVersion())
                .dcGrp(productionLogRequest.getDcGrp())
                .dataField(productionLogRequest.getDataField())
                .dataValue(productionLogRequest.getDataValue())
                .component(productionLogRequest.getComponent())
                .nc(productionLogRequest.getNc())
                .metaData(productionLogRequest.getMetaData())
                .qty(productionLogRequest.getQty())
                .workInstructionBo(productionLogRequest.getWorkInstructionBo())
                .comments(productionLogRequest.getComments())
                .reasonCode(productionLogRequest.getReasonCode())
                .site(productionLogRequest.getSite())
                .shiftId(productionLogRequest.getShiftId())
                .shiftCreatedDatetime(productionLogRequest.getShiftCreatedDatetime())
                .shiftStartTime(productionLogRequest.getShiftStartTime())
                .shiftEndTime(productionLogRequest.getShiftEndTime())
                .shiftAvailableTime(productionLogRequest.getShiftAvailableTime())
                .totalBreakHours(productionLogRequest.getTotalBreakHours())
                .quantityStarted(productionLogRequest.getQuantityStarted())
                .quantityCompleted(productionLogRequest.getQuantityCompleted())
                .quantityRework(productionLogRequest.getQuantityRework())
                .quantityScrapped(productionLogRequest.getQuantityScrapped())
                .isQualityImpact(productionLogRequest.getIsQualityImpact())
                .isPerformanceImpact(productionLogRequest.getIsPerformanceImpact())
                .plannedCycleTime(plannedCycleTime)
                .actualCycleTime(actualCycleTime)
                .manufactureTime(manufactureTime)
                .batchNo(productionLogRequest.getBatchNo())
                .orderNumber(productionLogRequest.getOrderNumber())
                .operation(productionLogRequest.getOperation())
                .phaseId(productionLogRequest.getPhaseId())
                .material(productionLogRequest.getMaterial())
                .materialVersion(productionLogRequest.getMaterialVersion())
                .status(productionLogRequest.getStatus())
                .instructionType(productionLogRequest.getInstructionType())
                .signoffUser(productionLogRequest.getSignoffUser())
                .createdDatetime(productionLogRequest.getEventDatetime() != null ? productionLogRequest.getEventDatetime() : LocalDateTime.now())
                .updatedDatetime(LocalDateTime.now())
                .active(1)
                .build();

        if(shiftRecordList !=null && shiftRecordList.getShiftId()!=null && !shiftRecordList.getShiftId().isEmpty() && shiftRecordList.getStartTime()!=null)
        {
            createProductionlogMongo.setShiftId(shiftRecordList.getShiftId());
            createProductionlogMongo.setShiftCreatedDatetime(shiftRecordList.getShiftCreatedDatetime());
            createProductionlogMongo.setShiftStartTime(shiftRecordList.getStartTime());
            createProductionlogMongo.setShiftEndTime(shiftRecordList.getEndTime());
            createProductionlogMongo.setTotalBreakHours(shiftRecordList.getBreakTime());
            createProductionlogMongo.setShiftAvailableTime(shiftRecordList.getPlannedTime());

            productionLogRequest.setShiftId(shiftRecordList.getShiftId());
            productionLogRequest.setShiftCreatedDatetime(shiftRecordList.getShiftCreatedDatetime());
            productionLogRequest.setShiftStartTime(shiftRecordList.getStartTime());
            productionLogRequest.setShiftEndTime(shiftRecordList.getEndTime());
            productionLogRequest.setTotalBreakHours(shiftRecordList.getBreakTime());
            productionLogRequest.setShiftAvailableTime(shiftRecordList.getPlannedTime());
        }
        productionLogMongoRepository.save(createProductionlogMongo);

        if(productionLogRequest.getQty() == null || productionLogRequest.getQty()==0)
        {
            productionLogRequest.setQty(0);
        }
        if(productionLogRequest.getQuantityCompleted() == null || productionLogRequest.getQuantityCompleted()==0)
        {
            productionLogRequest.setQuantityCompleted(0);
        }
        if(productionLogRequest.getQuantityScrapped() == null || productionLogRequest.getQuantityScrapped()==0)
        {
            productionLogRequest.setQuantityScrapped(0);
        }

        //postgresql
        ProductionLog createProductionLog = ProductionLog.builder()
                .event_id(productionLogRequest.getEventId())
                .event_type(productionLogRequest.getEventType())
                .event_data(productionLogRequest.getEventData())
                .event_datetime(productionLogRequest.getEventDatetime() != null ? productionLogRequest.getEventDatetime() : LocalDateTime.now())
                .user_id(productionLogRequest.getUserId())
                .pcu(productionLogRequest.getPcu())
                .shop_order_bo(productionLogRequest.getShopOrderBO())
                .workcenter_id(productionLogRequest.getWorkcenterId())
                .resource_id(productionLogRequest.getResourceId() != null && productionLogRequest.getResourceId().startsWith("ResourceBO")
                        ? BOConverter.getResource(productionLogRequest.getResourceId())
                        : productionLogRequest.getResourceId())
                .operation(productionLogRequest.getOperation_bo() != null ? BOConverter.getOperation(productionLogRequest.getOperation_bo()) : productionLogRequest.getOperation())
                .operation_version(productionLogRequest.getOperation_bo() != null ? BOConverter.getOperationVersion(productionLogRequest.getOperation_bo()) : "")
                .router_bo(productionLogRequest.getRouterBO() != null ? BOConverter.getRouter(productionLogRequest.getRouterBO()) : null)
                .router_version(productionLogRequest.getRouterBO() != null ? BOConverter.getRouterVersion(productionLogRequest.getRouterBO()) : null)
                .item(productionLogRequest.getItemBO() != null
                        ? BOConverter.getItem(productionLogRequest.getItemBO())
                        : (productionLogRequest.getItem() != null ? productionLogRequest.getItem() : productionLogRequest.getMaterial())
                )
                .item_version(productionLogRequest.getItemBO() != null
                        ? BOConverter.getItemVersion(productionLogRequest.getItemBO())
                        : (productionLogRequest.getItemVersion() != null ? productionLogRequest.getItemVersion() : productionLogRequest.getMaterialVersion())
                )
                .dc_grp(productionLogRequest.getDcGrp())
                .data_field(productionLogRequest.getDataField())
                .data_value(productionLogRequest.getDataValue())
                .component(productionLogRequest.getComponent())
                .nc(productionLogRequest.getNc())
                .meta_data(productionLogRequest.getMetaData())
                .qty(productionLogRequest.getQty())
                .work_instruction_bo(productionLogRequest.getWorkInstructionBo())
                .comments(productionLogRequest.getComments())
                .reason_code(productionLogRequest.getReasonCode())
                .site(productionLogRequest.getSite())
                .shift_id(productionLogRequest.getShiftId())
                .shift_created_datetime(productionLogRequest.getShiftCreatedDatetime())
                .shift_start_time(productionLogRequest.getShiftStartTime())
                .shift_end_time(productionLogRequest.getShiftEndTime())
                .shift_available_time(productionLogRequest.getShiftAvailableTime())
                .total_break_hours(productionLogRequest.getTotalBreakHours())
                .quantity_started(productionLogRequest.getQuantityStarted())
                .quantity_completed(productionLogRequest.getQuantityCompleted())
                .quantity_rework(productionLogRequest.getQuantityRework())
                .quantity_scrapped(productionLogRequest.getQuantityScrapped())
                .is_quality_impact(productionLogRequest.getIsQualityImpact())
                .is_performance_impact(productionLogRequest.getIsPerformanceImpact())
                .planned_cycle_time(plannedCycleTime)
                .actual_cycle_time(actualCycleTime)
                .manufacture_time(manufactureTime)
                .batchNo(productionLogRequest.getBatchNo())
                .orderNumber(productionLogRequest.getOrderNumber())
                .phaseId(productionLogRequest.getPhaseId())
                .material(productionLogRequest.getMaterial())
                .materialVersion(productionLogRequest.getMaterialVersion())
                .status(productionLogRequest.getStatus())
                .instruction_type(productionLogRequest.getInstructionType())
                .signoff_user(productionLogRequest.getSignoffUser())
                .created_datetime(productionLogRequest.getEventDatetime() != null ? productionLogRequest.getEventDatetime() : LocalDateTime.now())
                .updated_datetime(LocalDateTime.now())
                .active(1)
                .build();
        if(shiftRecordList !=null && shiftRecordList.getShiftId()!=null && !shiftRecordList.getShiftId().isEmpty() && shiftRecordList.getStartTime()!=null)
        {
            createProductionLog.setShift_id(shiftRecordList.getShiftId());
            createProductionLog.setShift_created_datetime(shiftRecordList.getShiftCreatedDatetime());
            createProductionLog.setShift_start_time(shiftRecordList.getStartTime());
            createProductionLog.setShift_end_time(shiftRecordList.getEndTime());
            createProductionLog.setTotal_break_hours(shiftRecordList.getBreakTime());
            createProductionLog.setShift_available_time(shiftRecordList.getPlannedTime());
        }
        productionLogRepository.save(createProductionLog);

        String applicationEvent = null;
        if(productionLogRequest.getEventType().equalsIgnoreCase("MC_UP")||productionLogRequest.getEventType().equalsIgnoreCase("MC_DOWN")){
            applicationEvent=productionLogRequest.getEventType();
        }else{
            applicationEvent = "ProductionLogEvent";
        }
        productionLogRequest.setCreatedDatetime(LocalDateTime.now());
        Message<ProductionLogRequest> message = new Message<>(applicationEvent, productionLogRequest);
        applicationEventPublisher.publishEvent(message);

        // applicationEventPublisher.publishEvent(new ProductionLogPlacedEvent(productionLogRequest));

        return true;
    }

    private Double getPlannedCycleTime(ProductionLogDto retrievePlannedCycleTime) {

        Double plannedCycleTime = webClientBuilder.build()
                .post()
                .uri(getPlannedCycleTimeUrl)
                .bodyValue(retrievePlannedCycleTime)
                .retrieve()
                .bodyToMono(Double.class)
                .block();
        return plannedCycleTime;
    }

    @Override
    public List<ProductionLog> retrieveByDateAndEventType(String eventType, LocalDateTime dateTime) {
        return productionLogRepository.findByEventTypeAndDateTime(eventType, dateTime);
    }

    @Override
    public List<ProductionLog> retrieveByPcuItemShopOrder(String pcu, String shopOrderBO, String item, String itemVersion) {
        if ((pcu == null || pcu.isEmpty()) &&
                (shopOrderBO == null || shopOrderBO.isEmpty()) &&
                (item == null || item.isEmpty()) &&
                (itemVersion == null || itemVersion.isEmpty())) {
            return new ArrayList<>();
        }
        return productionLogRepository.findByPcuShopOrderItemAndVersion(pcu, shopOrderBO, item, itemVersion);
    }

    @Override
    public List<ProductionLog> retrieveForProcess(ProductionLogRequest request) throws Exception {
        if(StringUtils.isNotBlank(request.getOperation()) && StringUtils.isEmpty(request.getOperationVersion())){
            request.setOperationVersion(getOperationCurrentVer(request));
        }

        LocalDateTime startTime = getDateRange(request).get(0);
        LocalDateTime endTime = getDateRange(request).get(1);

        List<ProductionLog> processProductionLogs = productionLogRepository.findByCustomCriteria(
                request.getSite(),
                request.getBatchNo(),
                request.getOrderNumber(),
                request.getMaterial(),
                request.getMaterialVersion(),
                request.getPhaseId(),
                request.getOperation(),
//                request.getOperationVersion(),
                request.getUserId(),
                startTime,
                endTime
        );
        return processProductionLogs;
    }

    private String getOperationCurrentVer(ProductionLogRequest request) throws Exception{
        ProductionLogRequest oper = ProductionLogRequest.builder().site(request.getSite()).operation(request.getOperation()).build();

        try {
            ProductionLogRequest operVersion = webClientBuilder.build()
                    .post()
                    .uri(retrieveOperationByCurrentVersionUrl)
                    .bodyValue(oper)
                    .retrieve()
                    .bodyToMono(ProductionLogRequest.class)
                    .block();

            if(operVersion == null)
                throw new PcuInQueueException(1710);

            return operVersion.getRevision();

        } catch (Exception e){
            throw e;
        }
    }

    private List<LocalDateTime> getDateRange(ProductionLogRequest request){
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if(request.getDateRange() == null) {
            LocalDateTime now = LocalDateTime.now();
            startDate = StringUtils.isBlank(request.getStartTime()) ? now.minusHours(24) : LocalDateTime.parse(request.getStartTime());
            endDate = request.getEndTime() != null ? LocalDateTime.parse(request.getEndTime()) : now;

        } else {

            switch (request.getDateRange()) {

                case "24hours":
                    LocalDateTime now = LocalDateTime.now();
                    startDate = StringUtils.isBlank(request.getStartTime()) ? now.minusHours(24) : LocalDateTime.parse(request.getStartTime());
                    endDate = StringUtils.isBlank(request.getEndTime()) ? now : LocalDateTime.parse(request.getEndTime());
                    break;

                case "today":
                    startDate = LocalDateTime.now().toLocalDate().atStartOfDay();
                    endDate = LocalDateTime.now();
                    break;

                case "yesterday":
                    startDate = LocalDateTime.now().minusDays(1).with(LocalTime.MIN);
                    endDate = LocalDateTime.now().minusDays(1).with(LocalTime.MAX);
                    break;

                case "thisWeek":
                    startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atStartOfDay();
                    endDate = LocalDateTime.now();
                    break;

                case "lastWeek":
                    startDate = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atStartOfDay();
                    endDate = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).atTime(LocalTime.MAX);
                    break;

                case "thisMonth":
                    startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                    endDate = LocalDateTime.now();
                    break;

                case "lastMonth":
                    startDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                    endDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
                    break;

                case "thisYear":
                    startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
                    endDate = LocalDateTime.now();
                    break;

                case "lastYear":
                    startDate = LocalDate.now().minusYears(1).with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
                    endDate = LocalDate.now().minusYears(1).with(TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX);
                    break;

                case "custom":
                    if (StringUtils.isBlank(request.getStartTime()) || StringUtils.isBlank(request.getEndTime())) {
                        throw new ProductionLogException(337);
                    }
                    try {
                        startDate = LocalDateTime.parse(request.getStartTime());
                        endDate = LocalDateTime.parse(request.getEndTime());
                    } catch (DateTimeParseException e) {
                        throw new ProductionLogException(338);
                    }
                    break;

                default:
                    break;
            }
        }

        return Arrays.asList(startDate, endDate);
    }

    @Override
    public List<ProductionLog> retrieveForDiscrete(ProductionLogRequest request) {
        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime startTime = request.getStartDateTime() != null ? request.getStartDateTime() : now.minusHours(24);
//        LocalDateTime endTime = request.getEndDateTime() != null ? request.getEndDateTime() : now;

        LocalDateTime startTime = getDateRange(request).get(0);
        LocalDateTime endTime = getDateRange(request).get(1);

        List<ProductionLog> processProductionLogs = productionLogRepository.findByPCUCriteria(
                request.getSite(),
                request.getUserId(),
                request.getPcu(),
                request.getShopOrderBO(),
//                request.getMaterial(),
//                request.getMaterialVersion(),
                request.getItem(),
                request.getItemVersion(),
                startTime,
                endTime
        );
        return processProductionLogs;
    }

    @Override
    public List<ProductionLog> retrieveByPcuForWorkinstruction(String site, String pcu) {
        return productionLogRepository.findBySiteAndPcuAndEventType(site, pcu, "WORK_INSTRUCTION");
    }

    @Override
    public ProductionLog retrieveByPcuAndOperationAndShopOrderAndEventType(String pcu, String operation, String operationVersion, String shopOrderBO, String eventType, String site) throws Exception {
        ProductionLog response = productionLogRepository.findTopByPcuAndOperationAndShopOrderAndSiteAndActiveAndEventType(
                pcu, operation, operationVersion, shopOrderBO, eventType, site, 1);

        return response;
    }

    @Override
    public ProductionLog retrieveFirstPcuStarted(String pcu, String shopOrderBO, String eventType, String site) throws Exception {//
        ProductionLog response = productionLogRepository.findFirstByPcuShopOrderSiteActiveAndEventType(
                pcu, shopOrderBO, eventType, site, 1);
//        ProductionLogMongo response = productionLogMongoRepository.findTop1ByPcuAndShoporderBoAndEventTypeAndSiteAndActiveOrderByCreatedDatetime(pcu, shopOrderBO, eventType, site, 1);

        return response;
    }

    @Override
    public ActualCycleSum retrieveAllByOperationShopOrderAndEventType(String pcu, String shopOrderBO, String operation, String operationVersion, String eventType) throws Exception {

        Long totalActualCycleTime = productionLogRepository.getTotalActualCycleTime(pcu, shopOrderBO, operation, operationVersion, eventType);
        return new ActualCycleSum(totalActualCycleTime);
    }

    @Override
    public List<ProductionQueryResponse> getTotalQuantityWithFields(ProductionLogQueryRequest productionLogRequest) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        List<String> groupFields=groupByFieldss(productionLogRequest);
        // Building match criteria
        Criteria matchCriteria = Criteria.where("timestamp").gte(productionLogRequest.getStartTime().format(formatter))
                .andOperator(
                        Criteria.where("timestamp").lte(productionLogRequest.getEndTime().format(formatter)),
                        Criteria.where("eventType").is("PCU_COMPLETE"),
                        Criteria.where("resourceBO").is(productionLogRequest.getResource()),
                        Criteria.where("site").is(productionLogRequest.getSite()),
                        Criteria.where("shiftName").is(productionLogRequest.getShift()),
                        buildCriteriaForQuery(productionLogRequest)
                );
        // Building the aggregation pipeline
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(matchCriteria));

        GroupOperation groupOperation;
        ProjectionOperation projectionOperation;
        if (!groupFields.isEmpty()) {
            groupOperation = Aggregation.group(Fields.fields(groupFields.toArray(new String[0])))
                    .sum(ConvertOperators.ToDouble.toDouble("$qty")).as("totalQtyCompleted")
                    .sum(ConvertOperators.ToDouble.toDouble("$actualCycleTime")).as("totalActualCycleTime");

            operations.add(groupOperation);

            // Add projection to include grouped fields
            projectionOperation = Aggregation.project()
                    .and("totalQtyCompleted").as("totalQtyCompleted")
                    .and("totalActualCycleTime").as("totalActualCycleTime");

            for (String field : groupFields) {
                projectionOperation = projectionOperation.and("_id." + field).as(field);
            }
            projectionOperation = projectionOperation.andExclude("_id");
            operations.add(projectionOperation);
        }



        Aggregation aggregation = Aggregation.newAggregation(operations);

        AggregationResults<ProductionQueryResponse> results =
                mongoTemplate.aggregate(aggregation, ProductionLogMongo.class, ProductionQueryResponse.class);

        return results.getMappedResults();
    }

    private List<String> groupByFieldss(ProductionLogQueryRequest productionLogRequest) {
        List<String>  groupFields=new ArrayList<>();
        groupFields.add("itemBO");
        groupFields.add("resourceBO");
        if (productionLogRequest.getOperationBO() != null && !productionLogRequest.getOperationBO().isEmpty()) {
            if(productionLogRequest.getOperationBO().equals("*")||productionLogRequest.getOperationBO().equals("*,*")){
                productionLogRequest.setOperationBO(null);
            }
            groupFields.add("operationBO");
        }
        if (productionLogRequest.getRoutingBO() != null && !productionLogRequest.getRoutingBO().isEmpty()) {
            if(productionLogRequest.getRoutingBO().equals("*")||productionLogRequest.getRoutingBO().equals("*,*")){
                productionLogRequest.setRoutingBO(null);
            }
            groupFields.add("routerBO");
        }
        if (productionLogRequest.getShopOrderBO() != null && !productionLogRequest.getShopOrderBO().isEmpty()) {
            if(productionLogRequest.getShopOrderBO().equals("*")||productionLogRequest.getShopOrderBO().equals("*,*")){
                productionLogRequest.setShopOrderBO(null);
            }
            groupFields.add("shoporderBO");
        }
        if (productionLogRequest.getItemBO() != null && !productionLogRequest.getItemBO().isEmpty()) {
            if(productionLogRequest.getItemBO().equals("*")||productionLogRequest.getItemBO().equals("*,*")){
                productionLogRequest.setItemBO(null);
            }
        }
        return groupFields;

    }

    @Override
    public List<ProductionQueryResponse> getTotalQuantityForDoneWithFields(ProductionLogQueryRequest productionLogRequest) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        List<String> groupFields=groupByFieldss(productionLogRequest);
        // Building match criteria
        Criteria matchCriteria = Criteria.where("timestamp").gte(productionLogRequest.getStartTime().format(formatter))
                .andOperator(
                        Criteria.where("timestamp").lte(productionLogRequest.getEndTime().format(formatter)),
                        Criteria.where("eventType").is("PCU_DONE"),
                        Criteria.where("resourceBO").is(productionLogRequest.getResource()),
                        Criteria.where("site").is(productionLogRequest.getSite()),
                        Criteria.where("shiftName").is(productionLogRequest.getShift()),
                        buildCriteriaForQuery(productionLogRequest)
                );


        // Building the aggregation pipeline
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(matchCriteria));

        GroupOperation groupOperation;
        ProjectionOperation projectionOperation;
        if (!groupFields.isEmpty()&&groupFields.size()>1) {
            groupOperation = Aggregation.group(Fields.fields(groupFields.toArray(new String[0])))
                    .sum(ConvertOperators.ToDouble.toDouble("$qty")).as("totalQtyCompleted")
                    .sum(ConvertOperators.ToDouble.toDouble("$manufactureTime")).as("totalActualCycleTime");

            operations.add(groupOperation);

            // Add projection to include grouped fields
            projectionOperation = Aggregation.project()
                    .and("totalQtyCompleted").as("totalQtyCompleted")
                    .and("totalActualCycleTime").as("totalActualCycleTime");

            for (String field : groupFields) {
                projectionOperation = projectionOperation.and("_id." + field).as(field);
            }
            projectionOperation = projectionOperation.andExclude("_id");
            operations.add(projectionOperation);
        }



        Aggregation aggregation = Aggregation.newAggregation(operations);

        AggregationResults<ProductionQueryResponse> results =
                mongoTemplate.aggregate(aggregation, ProductionLogMongo.class, ProductionQueryResponse.class);

        return results.getMappedResults();
    }

    @Override
    public List<ProductionLogQualityResponse> getSumOfScrapQtyAndTotalProducedQty(ProductionLogRequest productionLogRequest)throws Exception
    {
//        String date[]= productionLogRequest.getEntryTime().split("T");
//        if(date.length>1){
//            productionLogRequest.setShiftStartTime(date[0]+"T"+productionLogRequest.getShiftStartTime());
//        } else {
//            System.out.println("No time part found in entryTime: " + productionLogRequest.getEntryTime());
//        }

        MatchOperation match1 = Aggregation.match(
                Criteria.where("timestamp").gt(productionLogRequest.getShiftStartTime()).lt(productionLogRequest.getEntryTime())
                        .andOperator(
                                Criteria.where("site").is(productionLogRequest.getSite()),
                                Criteria.where("resourceBO").is(productionLogRequest.getResourceId())));

        Criteria newQuery = buildCriteria(productionLogRequest);




        MatchOperation match2 = Aggregation.match(newQuery);

//        MatchOperation match3 = Aggregation.match(
//                Criteria.where("eventType").in(Arrays.asList("PCU_COMPLETE", "PCU_SCRAP"))
//        );

        MatchOperation match3 = Aggregation.match(
                Criteria.where("eventType").is("PCU_SCRAP")
        );
        ProjectionOperation project = Aggregation.project()
                .andExpression("{$toDouble : '$qty'}").as("qtyDouble")
                .andExpression("{'$eventType'}").as("eventType");

        GroupOperation group = Aggregation.group("eventType")
                .sum("qtyDouble").as("totalQuantity");

        ProjectionOperation projectAgain = Aggregation.project()
                .and("_id").as("eventType")
                .and("totalQuantity").as("totalQuantity");

        Aggregation aggregation = newAggregation(
                match1,match2,match3,project, group ,projectAgain
        );
        AggregationResults<ProductionLogQualityResponse> result = mongoTemplate.aggregate(aggregation, ProductionLogMongo.class, ProductionLogQualityResponse.class);
        List<ProductionLogQualityResponse> subtractedResult = new ArrayList<>();
        List<ProductionLogQualityResponse> aggregatedResult = result.getMappedResults();
        List<ProductionLogQualityResponse> unScrapRecord = getSumOfUnScrapQtyAndTotalProducedQty(productionLogRequest);
        if(aggregatedResult!=null && !aggregatedResult.isEmpty() ){
            if(unScrapRecord!= null && !unScrapRecord.isEmpty()) {
                for (int i = 0; i < aggregatedResult.size(); i++) {
                    ProductionLogQualityResponse aggregatedItem = aggregatedResult.get(i);
                    if (unScrapRecord.get(i) != null && unScrapRecord.get(i).getTotalQuantity() != 0) {
                        ProductionLogQualityResponse unScrapItem = unScrapRecord.get(i);

                        // Subtract the corresponding values
                        long subtractedQuantity = aggregatedItem.getTotalQuantity() - unScrapItem.getTotalQuantity();

                        // Create a new ProductionLogQualityResponse object with the subtracted value
                        ProductionLogQualityResponse subtractedItem = new ProductionLogQualityResponse();
                        subtractedItem.setEventType(aggregatedItem.getEventType());
                        subtractedItem.setTotalQuantity(subtractedQuantity);

                        // Add the subtracted item to the result list
                        subtractedResult.add(subtractedItem);
                    }
                }
            }else{
                subtractedResult.addAll(aggregatedResult);
            }

        }


        return subtractedResult;
    }
    public Criteria buildCriteria(ProductionLogRequest productionLogRequest) {
        Criteria criteria = new Criteria();

        if (productionLogRequest.getItemBO() != null && !productionLogRequest.getItemBO().isEmpty()) {
            criteria.and("itemBO").is(productionLogRequest.getItemBO());
        }
        if (productionLogRequest.getOperation_bo() != null && !productionLogRequest.getOperation_bo() .isEmpty()) {
            criteria.and("operationBO").is(productionLogRequest.getOperation_bo() );
        }
        if (productionLogRequest.getRouterBO() != null && !productionLogRequest.getRouterBO().isEmpty()) {
            criteria.and("routerBO").is(productionLogRequest.getRouterBO());
        }
        if (productionLogRequest.getShopOrderBO() != null && !productionLogRequest.getShopOrderBO().isEmpty()) {
            criteria.and("shoporderBO").is(productionLogRequest.getShopOrderBO());
        }
        return criteria;
    }
    public Criteria buildCriteriaForQuery(ProductionLogQueryRequest productionLogRequest) {
        Criteria criteria = new Criteria();

        if (productionLogRequest.getItemBO() != null && !productionLogRequest.getItemBO().isEmpty()) {
            criteria.and("itemBO").is(productionLogRequest.getItemBO());
        }
        if (productionLogRequest.getOperationBO() != null && !productionLogRequest.getOperationBO() .isEmpty()) {
            criteria.and("operationBO").is(productionLogRequest.getOperationBO() );
        }
        if (productionLogRequest.getRoutingBO() != null && !productionLogRequest.getRoutingBO().isEmpty()) {
            criteria.and("routerBO").is(productionLogRequest.getRoutingBO());
        }
        if (productionLogRequest.getShopOrderBO() != null && !productionLogRequest.getShopOrderBO().isEmpty()) {
            criteria.and("shoporderBO").is(productionLogRequest.getShopOrderBO());
        }
        return criteria;
    }
    public List<ProductionLogQualityResponse> getSumOfUnScrapQtyAndTotalProducedQty(ProductionLogRequest productionLogRequest)throws Exception
    {

        MatchOperation match1 = Aggregation.match(
                Criteria.where("timestamp").gt(productionLogRequest.getShiftStartTime()).lt(productionLogRequest.getEntryTime())
                        .andOperator(
                                Criteria.where("site").is(productionLogRequest.getSite()),
                                Criteria.where("resourceBO").is(productionLogRequest.getResourceId())));

        Criteria newQuery = buildCriteria(productionLogRequest);

        MatchOperation match2 = Aggregation.match(newQuery);

//        MatchOperation match3 = Aggregation.match(
//                Criteria.where("eventType").in(Arrays.asList("PCU_COMPLETE", "PCU_SCRAP"))
//        );

        MatchOperation match3 = Aggregation.match(
                Criteria.where("eventType").is("PCU_UN_SCRAP")
        );
        ProjectionOperation project = Aggregation.project()
                .andExpression("{$toDouble : '$qty'}").as("qtyDouble")
                .andExpression("{'$eventType'}").as("eventType");

        GroupOperation group = Aggregation.group("eventType")
                .sum("qtyDouble").as("totalQuantity");

        ProjectionOperation projectAgain = Aggregation.project()
                .and("_id").as("eventType")
                .and("totalQuantity").as("totalQuantity");

        Aggregation aggregation = newAggregation(
                match1,match2,match3,project, group ,projectAgain
        );
        AggregationResults<ProductionLogQualityResponse> result = mongoTemplate.aggregate(aggregation, ProductionLogMongo.class, ProductionLogQualityResponse.class);

        List<ProductionLogQualityResponse> aggregatedResult = result.getMappedResults();

        return aggregatedResult;
    }

    @EventListener
    public void producer(ProductionLogProducer producerEvent) throws Exception{
        ProductionLogRequest message = producerEvent.getSendResult();
        try {
            create(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BreakMinutes getShiftDetailsByShiftType(ShiftRequest shiftRequest)
    {
        BreakMinutes retrievedRecord = webClientBuilder.build()
                .post()
                .uri(getShiftDetailsByShiftTypeUrl)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(BreakMinutes.class)
                .block();
        return retrievedRecord;
    }
    private List<ShiftOutput> fetchShiftDetails(ShiftRequest request) {
        ShiftInput shiftRequest = new ShiftInput(request.getSite(), request.getResourceId(),request.getWorkCenterId(),request.getLocalDateTime(),request.getLocalDateTime());
        return webClientBuilder.build()
                .post()
                .uri(getShiftDetailBetweenTime)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToFlux(ShiftOutput.class)
                .collectList()
                .block();
    }

    @Override
    public List<ProductionQueryResponse> getTotalQuantity(LocalDateTime startTime, LocalDateTime endTime, String resource, String site, String shift) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("timestamp").gte(startTime.format(formatter))
                                .andOperator(
                                        Criteria.where("timestamp").lte(endTime.format(formatter)),
                                        Criteria.where("eventType").is("PCU_COMPLETE"),
                                        Criteria.where("resourceBO").is(resource),
                                        Criteria.where("site").is(site),
                                        Criteria.where("shiftName").is(shift)
                                )
                ),
                Aggregation.group(
                                Fields.fields(
                                        "itemBO", "operationBO", "resourceBO", "routerBO", "shoporderBO", "workcenterBO"
                                )
                        )
                        .sum(
                                ConvertOperators.ToDouble.toDouble("$qty")
                        ).as("totalQtyCompleted")
                        .sum(
                                ConvertOperators.ToDouble.toDouble("$actualCycleTime")
                        ).as("totalActualCycleTime"),
                Aggregation.project()
                        .and("_id.itemBO").as("itemBO")
                        .and("_id.operationBO").as("operationBO")
                        .and("_id.resourceBO").as("resourceBO")
                        .and("_id.routerBO").as("routerBO")
                        .and("_id.shoporderBO").as("shoporderBO")
                        .and("_id.workcenterBO").as("workcenterBO")
                        .and("totalQtyCompleted").as("totalQtyCompleted")
                        .and("totalActualCycleTime").as("totalActualCycleTime") // Convert string to integer
                        .andExclude("_id")
        );


        AggregationResults<ProductionQueryResponse> results =
                mongoTemplate.aggregate(aggregation, ProductionLogMongo.class, ProductionQueryResponse.class);

        List<ProductionQueryResponse> result=results.getMappedResults();

        return result;
    }
    public List<ProductionQueryResponse> getTotalQuantityScrap(LocalDateTime startTime, LocalDateTime endTime,String resource,String site) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("timestamp").gte(startTime.format(formatter))
                                .andOperator(
                                        Criteria.where("timestamp").lte(endTime.format(formatter)),
                                        Criteria.where("eventType").is("PCU_SCRAP"),
                                        Criteria.where("resourceBO").is(resource),
                                        Criteria.where("site").is(site)
                                )
                ),
                Aggregation.group(
                                "itemBO", "operationBO", "resourceBO", "routerBO", "shoporderBO", "workcenterBO"
                        )
                        .sum(
                                ConvertOperators.ToLong.toLong("$qty")
                        ).as("totalQtyCompleted")
                        .sum(
                                ConvertOperators.ToLong.toLong("$actualCycleTime")
                        ).as("totalActualCycleTime"),
                Aggregation.project()
                        .and("_id.itemBO").as("itemBO")
                        .and("_id.operationBO").as("operationBO")
                        .and("_id.resourceBO").as("resourceBO")
                        .and("_id.routerBO").as("routerBO")
                        .and("_id.shoporderBO").as("shoporderBO")
                        .and("_id.workcenterBO").as("workcenterBO")
                        .and("totalQtyCompleted").as("totalQtyCompleted")
                        .and("totalActualCycleTime").as("totalActualCycleTime") // Convert string to integer
                        .andExclude("_id")
        );


        AggregationResults<ProductionQueryResponse> results =
                mongoTemplate.aggregate(aggregation, ProductionLogMongo.class, ProductionQueryResponse.class);

        return results.getMappedResults();
    }

    @Override
    public List<ProductionQueryResponse> getTotalQuantityForDone(LocalDateTime startTime, LocalDateTime endTime, String resource, String site, String shift) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("timestamp").gte(startTime.format(formatter))
                                .andOperator(
                                        Criteria.where("timestamp").lte(endTime.format(formatter)),
                                        Criteria.where("eventType").is("PCU_DONE"),
                                        Criteria.where("resourceBO").is(resource),
                                        Criteria.where("site").is(site),
                                        Criteria.where("shiftName").is(shift)
                                )
                ),
                Aggregation.group(
                                Fields.fields(
                                        "itemBO", "operationBO", "resourceBO", "routerBO", "shoporderBO", "workcenterBO"
                                )
                        )
                        .sum(
                                ConvertOperators.ToDouble.toDouble("$qty")
                        ).as("totalQtyCompleted")
                        .sum(
                                ConvertOperators.ToDouble.toDouble("$manufactureTime")
                        ).as("totalActualCycleTime"),
                Aggregation.project()
                        .and("_id.itemBO").as("itemBO")
                        .and("_id.operationBO").as("operationBO")
                        .and("_id.resourceBO").as("resourceBO")
                        .and("_id.routerBO").as("routerBO")
                        .and("_id.shoporderBO").as("shoporderBO")
                        .and("_id.workcenterBO").as("workcenterBO")
                        .and("totalQtyCompleted").as("totalQtyCompleted")
                        .and("totalActualCycleTime").as("totalActualCycleTime") // Convert string to integer
                        .andExclude("_id")
        );

        // Execute the aggregation pipeline and retrieve the results
        AggregationResults<ProductionQueryResponse> results =
                mongoTemplate.aggregate(aggregation, ProductionLogMongo.class, ProductionQueryResponse.class);

        return results.getMappedResults();


    }

    @Override
    public List<ProductionQueryResponse> getTotalQuantityForDoneForMaterialAndResource(LocalDateTime startTime, LocalDateTime endTime, String resource, String site, String shift) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("timestamp").gte(startTime.format(formatter))
                                .andOperator(
                                        Criteria.where("timestamp").lte(endTime.format(formatter)),
                                        Criteria.where("eventType").is("PCU_DONE"),
                                        Criteria.where("resourceBO").is(resource),
                                        Criteria.where("site").is(site),
                                        Criteria.where("shiftName").is(shift)
                                )
                ),
                Aggregation.group(
                                Fields.fields(
                                        "itemBO"
                                )
                        )
                        .sum(
                                ConvertOperators.ToDouble.toDouble("$qty")
                        ).as("totalQtyCompleted")
                        .sum(
                                ConvertOperators.ToDouble.toDouble("$manufactureTime")
                        ).as("totalActualCycleTime"),
                Aggregation.project()
                        .and("_id.itemBO").as("itemBO")
                        .and("totalQtyCompleted").as("totalQtyCompleted")
                        .and("totalActualCycleTime").as("totalActualCycleTime") // Convert string to integer
                        .andExclude("_id")
        );

        // Execute the aggregation pipeline and retrieve the results
        AggregationResults<ProductionQueryResponse> results =
                mongoTemplate.aggregate(aggregation, ProductionLogMongo.class, ProductionQueryResponse.class);

        return results.getMappedResults();

    }

    public Boolean getShiftAndLogForMacUpMacDown(String site, String resource)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        //ProductionLogMongo productionLogMongo = productionLogMongoRepository.findTop1BySiteAndResourceIdAndEventTypeOrderByTimestampDesc(site,"ResourceBO:"+site+","+resource,"MC_DOWN");
        ProductionLog productionLog = productionLogRepository.findTop1BySiteAndResourceIdAndEventTypeOrderByTimestampDesc(site,resource,"MC_DOWN");
        if(productionLog != null && productionLog.getCreated_datetime()!=null) {
            List<ShiftIntervalWithDate> getShiftsWithDatesInRange = getShiftsWithDatesInRange(site, resource, productionLog.getCreated_datetime(), LocalDateTime.now());
            int count = 1;
            int secondsSinceEpoch = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            if(getShiftsWithDatesInRange.size() == 1)
            {
                return true;
            }
            for(ShiftIntervalWithDate shiftWithDate : getShiftsWithDatesInRange)
            {
                if(shiftWithDate.getBreakMinutes()!= null && shiftWithDate.getBreakMinutes().getShiftId()!=null) {
                    if (count == 1) {

//                        BreakMinutes breakMinutes = new BreakMinutes();
//                        breakMinutes.setShiftName(productionLogMongo.getShiftId());
//                        breakMinutes.setBreakTime(Integer.parseInt(productionLogMongo.getTotalBreakHours()));
//                        breakMinutes.setStartTime(productionLogMongo.getShiftStartTime());
//                        breakMinutes.setEndTime(productionLogMongo.getShiftEndTime());
//                        breakMinutes.setPlannedTime(Integer.parseInt(productionLogMongo.getShiftAvailableTime()));
                        LocalTime time = shiftWithDate.getBreakMinutes().getEndTime();
                        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                                .eventType("MC_UP")
                                .userId(productionLog.getUser_id())
                                .resourceId(productionLog.getResource_id())
                                .eventData(productionLog.getEvent_data())
                                .dataField(productionLog.getData_field())
                                .dataValue("Enabled")
                                .qty(productionLog.getQty())
                                .site(productionLog.getSite())
                                .shiftId(shiftWithDate.getBreakMinutes().getShiftId())
                                .shiftStartTime(shiftWithDate.getBreakMinutes().getStartTime())
                                .shiftEndTime(shiftWithDate.getBreakMinutes().getEndTime())
                                .shiftAvailableTime(shiftWithDate.getBreakMinutes().getPlannedTime())
                                .totalBreakHours(shiftWithDate.getBreakMinutes().getBreakTime())
//                                .timestamp(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getEndTime(),formatter,shiftWithDate.getDate()).toString())
                                .createdDatetime(LocalDateTime.of(shiftWithDate.getDate(),time))
                                .build();



                        ProductionLogMongo buildProductionLogMongo = buildProductionLogMongo(productionLog, shiftWithDate.getBreakMinutes());
                        buildProductionLogMongo.setEventId(String.valueOf(secondsSinceEpoch));
                        buildProductionLogMongo.setEventType("MC_UP");
                        buildProductionLogMongo.setCreatedDatetime(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getEndTime().toString(),formatter,shiftWithDate.getDate()));
                        productionLogMongoRepository.save(buildProductionLogMongo);

                        ProductionLog buildProductionLog = buildProductionLog(productionLog, shiftWithDate.getBreakMinutes());
                        buildProductionLog.setEvent_id(String.valueOf(secondsSinceEpoch));
                        buildProductionLog.setEvent_type("MC_UP");
                        buildProductionLog.setCreated_datetime(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getEndTime().toString(),formatter,shiftWithDate.getDate()));
                        productionLogRepository.save(buildProductionLog);

                        Message<ProductionLogRequest> message = new Message<>("MC_UP", productionLogRequest);
                        applicationEventPublisher.publishEvent(message);
                        count++;
                        continue;
                    }
                    if(count == getShiftsWithDatesInRange.size())
                    {
                        LocalTime time = shiftWithDate.getBreakMinutes().getStartTime();
                        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                                .eventType("MC_DOWN")
                                .userId(productionLog.getUser_id())
                                .resourceId(productionLog.getResource_id())
                                .eventData(productionLog.getEvent_data())
                                .dataField(productionLog.getData_field())
                                .dataValue("Disabled")
                                .qty(productionLog.getQty())
                                .site(productionLog.getSite())
                                .shiftId(shiftWithDate.getBreakMinutes().getShiftId())
                                .shiftStartTime(shiftWithDate.getBreakMinutes().getStartTime())
                                .shiftEndTime(shiftWithDate.getBreakMinutes().getEndTime())
                                .shiftAvailableTime(shiftWithDate.getBreakMinutes().getPlannedTime())
                                .totalBreakHours(shiftWithDate.getBreakMinutes().getBreakTime())
//                                .timestamp(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getStartTime(),formatter,shiftWithDate.getDate()).toString())
                                .createdDatetime(LocalDateTime.of(shiftWithDate.getDate(),time))
                                .build();

                        ProductionLogMongo buildProductionLogMongo = buildProductionLogMongo(productionLog, shiftWithDate.getBreakMinutes());
                        buildProductionLogMongo.setEventId(String.valueOf(secondsSinceEpoch));
                        buildProductionLogMongo.setEventType("MC_DOWN");
                        buildProductionLogMongo.setCreatedDatetime(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getStartTime().toString(),formatter,shiftWithDate.getDate()));
                        productionLogMongoRepository.save(buildProductionLogMongo);

                        ProductionLog buildProductionLog = buildProductionLog(productionLog, shiftWithDate.getBreakMinutes());
                        buildProductionLog.setEvent_id(String.valueOf(secondsSinceEpoch));
                        buildProductionLog.setEvent_type("MC_DOWN");
                        buildProductionLog.setCreated_datetime(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getStartTime().toString(),formatter,shiftWithDate.getDate()));
                        productionLogRepository.save(buildProductionLog);

                        Message<ProductionLogRequest> message = new Message<>("MC_DOWN", productionLogRequest);
                        applicationEventPublisher.publishEvent(message);
                        count++;
                        continue;
                    }
                    LocalTime time = shiftWithDate.getBreakMinutes().getStartTime();
                    ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                            .eventType("MC_DOWN")
                            .userId(productionLog.getUser_id())
                            .resourceId(productionLog.getResource_id())
                            .eventData(productionLog.getEvent_data())
                            .dataField(productionLog.getData_field())
                            .dataValue("Disabled")
                            .qty(productionLog.getQty())
                            .site(productionLog.getSite())
                            .shiftId(shiftWithDate.getBreakMinutes().getShiftId())
                            .shiftStartTime(shiftWithDate.getBreakMinutes().getStartTime())
                            .shiftEndTime(shiftWithDate.getBreakMinutes().getEndTime())
                            .shiftAvailableTime(shiftWithDate.getBreakMinutes().getPlannedTime())
                            .totalBreakHours(shiftWithDate.getBreakMinutes().getBreakTime())
//                            .timestamp(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getStartTime(),formatter,shiftWithDate.getDate()).toString())
                            .createdDatetime(LocalDateTime.of(shiftWithDate.getDate(),time))
                            .build();

                    ProductionLogMongo buildProductionLogMongo = buildProductionLogMongo(productionLog, shiftWithDate.getBreakMinutes());
                    buildProductionLogMongo.setEventId(String.valueOf(secondsSinceEpoch));
                    buildProductionLogMongo.setEventType("MC_DOWN");
                    buildProductionLogMongo.setCreatedDatetime(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getStartTime().toString(),formatter,shiftWithDate.getDate()));
                    productionLogMongoRepository.save(buildProductionLogMongo);

                    ProductionLog buildProductionLog = buildProductionLog(productionLog, shiftWithDate.getBreakMinutes());
                    buildProductionLog.setEvent_id(String.valueOf(secondsSinceEpoch));
                    buildProductionLog.setEvent_type("MC_DOWN");
                    buildProductionLog.setCreated_datetime(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getStartTime().toString(),formatter,shiftWithDate.getDate()));
                    productionLogRepository.save(buildProductionLog);

                    Message<ProductionLogRequest> message = new Message<>("MC_DOWN", productionLogRequest);
                    applicationEventPublisher.publishEvent(message);


                    ProductionLogMongo buildProductionLogMongoMcUp = buildProductionLogMongo(productionLog, shiftWithDate.getBreakMinutes());
                    buildProductionLogMongo.setEventId(String.valueOf(secondsSinceEpoch));
                    buildProductionLogMongo.setEventType("MC_UP");
                    buildProductionLogMongo.setCreatedDatetime(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getEndTime().toString(),formatter,shiftWithDate.getDate()));
                    productionLogMongoRepository.save(buildProductionLogMongoMcUp);

                    ProductionLog buildProductionLogMcUp = buildProductionLog(productionLog, shiftWithDate.getBreakMinutes());
                    buildProductionLog.setEvent_id(String.valueOf(secondsSinceEpoch));
                    buildProductionLog.setEvent_type("MC_UP");
                    buildProductionLog.setCreated_datetime(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getEndTime().toString(),formatter,shiftWithDate.getDate()));
                    productionLogRepository.save(buildProductionLogMcUp);

                    //LocalTime times = LocalTime.parse(shiftWithDate.getBreakMinutes().getEndTime());
                    productionLogRequest.setEventType("MC_UP");
                    productionLogRequest.setCreatedDatetime(parseToLocalDateTime(shiftWithDate.getBreakMinutes().getEndTime().toString(),formatter,shiftWithDate.getDate()));
                    //productionLogRequest.setTimestamp(String.valueOf(LocalDateTime.of(shiftWithDate.getDate(),times)));
                    Message<ProductionLogRequest> messages = new Message<>("MC_UP", productionLogRequest);
                    applicationEventPublisher.publishEvent(messages);
                    count++;
                }
            }
        }
        return true;
    }
    private LocalDateTime parseToLocalDateTime(String timeString, DateTimeFormatter formatter , LocalDate date) {
        return LocalTime.parse(timeString, formatter).atDate(date);
    }


    public ProductionLogMongo buildProductionLogMongo(ProductionLog productionLog, BreakMinutes breakMinutes)
    {
        ProductionLogMongo productionLogMongo = ProductionLogMongo.builder()
                .shiftId(breakMinutes.getShiftId())
                .shiftStartTime(breakMinutes.getStartTime())
                .shiftEndTime(breakMinutes.getEndTime())
                .totalBreakHours(breakMinutes.getBreakTime())
                .shiftAvailableTime(breakMinutes.getPlannedTime())
                .build();
//        productionLogMongo.setShiftName(breakMinutes.getShiftName());
//        productionLogMongo.setShiftStartTime(breakMinutes.getStartTime());
//        productionLogMongo.setShiftEndTime(breakMinutes.getEndTime());
//        productionLogMongo.setTotalBreakHours(String.valueOf(breakMinutes.getBreakTime()));
//        productionLogMongo.setShiftAvailableTime(String.valueOf(breakMinutes.getPlannedTime()));
        return productionLogMongo;
    }
    public ProductionLog buildProductionLog(ProductionLog productionLog, BreakMinutes breakMinutes)
    {
        ProductionLog createProductionLog = ProductionLog.builder()
                .user_id(productionLog.getUser_id())
                .pcu(productionLog.getPcu())
                .resource_id(productionLog.getResource_id())
                .event_data(productionLog.getEvent_data())
                .data_field(productionLog.getData_field())
                .data_value(productionLog.getData_value())
                .site(productionLog.getSite())
                .comments(productionLog.getComments())
                .reason_code(productionLog.getReason_code())
                .shift_id(breakMinutes.getShiftId())
                .shift_start_time(breakMinutes.getStartTime())
                .shift_end_time(breakMinutes.getEndTime())
                .shift_available_time(breakMinutes.getBreakTime())
                .total_break_hours(breakMinutes.getPlannedTime())
                .active(1)
                .build();
        return createProductionLog;
    }



    public List<ShiftIntervalWithDate> getShiftsWithDatesInRange(String site, String resource,  LocalDateTime dateStart, LocalDateTime dateEnd)
    {
        ShiftBtwnDatesRequest retrieveShiftRequest = ShiftBtwnDatesRequest.builder().site(site).shiftType("resource").resource(resource).dateStart(dateStart).dateEnd(dateEnd).build();
        List<ShiftIntervalWithDate> retrievedRecord = webClientBuilder.build()
                .post()
                .uri(getShiftBetweenDatesUrl)
                .bodyValue(retrieveShiftRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ShiftIntervalWithDate>>() {
                })
                .block();
        return retrievedRecord;
    }
    @Override
    public Integer getTotalDownTimeEventForADay(String site,LocalDate startDate,LocalDate endDate,String resource) throws Exception {

        LocalDate currentDate = LocalDate.now();
        LocalDate tomorrow = currentDate.plusDays(1);

        if (startDate == null) {
            startDate = currentDate;
        }
        if (endDate == null) {
            endDate = tomorrow;
        }

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("site").is(site)
                        .and("timestamp").gte(startDate).lt(endDate)
                        .and("eventType").is("MC_DOWN")
        );
        if(StringUtils.isNotEmpty(resource))
        {
            matchOperation = Aggregation.match(
                    Criteria.where("site").is(site)
                            .and("timestamp").gte(startDate).lt(endDate)
                            .and("eventType").is("MC_DOWN")
                            .and("resourceBO").is(resource)
            );
        }
        CountOperation countOperation = Aggregation.count().as("totalDownTimeEvents");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                countOperation
        );

        AggregationResults
                <DownEvents> totalDownTimeEvents = mongoTemplate.aggregate(aggregation, ProductionLogMongo.class, DownEvents.class);
        DownEvents downTime = totalDownTimeEvents.getUniqueMappedResult();

        if(downTime != null)
        {
            return downTime.getTotalDownTimeEvents();
        }

        return 0;
    }

    public ProductionLogRequest validateStartAndEndTime(ProductionLogRequest productionLogRequest){

        if (productionLogRequest.getStartDateTime() == null) {
            productionLogRequest.setStartDateTime(LocalDateTime.now()
                    .minusHours(1)
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .toLocalDateTime());
        }
        return productionLogRequest;
    }
    @Override
    public List<ProductionQuality> getCalculatedQuality(ProductionLogRequest productionLogRequest) throws Exception {

        List<ProductionQuality> qualityResponses = new ArrayList<>();

        validateStartAndEndTime(productionLogRequest);

//        ZoneId originalZone = ZoneId.of("Asia/Kolkata");
//        LocalDateTime utcStartTime = convertToUtc(productionLogRequest.getStartDateTime(), originalZone);


        OeeFilterRequest qtyRequest = OeeFilterRequest.builder().site(productionLogRequest.getSite()).startTime(productionLogRequest.getStartDateTime()).build();

        try {
            List<PerformanceResponse> combinations = webClientBuilder.build()
                    .post()
                    .uri(performanceUniqueCombUrl)
                    .bodyValue(qtyRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<PerformanceResponse>>() {
                    })
                    .block();

            if (combinations == null) {
                throw new BatchNoRecipeHeaderException(117);
            }

            for (PerformanceResponse combination : combinations) {

                Double totalQuantity = productionLogRepository.calculateTotalQuantity(combination.getSite(), combination.getWorkcenterId(), combination.getOperation(), combination.getOperationVersion(), combination.getResourceId(), combination.getItem(), combination.getItemVersion(), combination.getShiftId(), combination.getPcu());
                Double goodQuantity = productionLogRepository.calculateGoodQuantity(combination.getSite(), combination.getWorkcenterId(), combination.getOperation(), combination.getOperationVersion(), combination.getResourceId(), combination.getItem(), combination.getItemVersion(), combination.getShiftId(), combination.getPcu());

                if (totalQuantity == null || goodQuantity == null)
                    continue;

                Double qualityPercentage = (goodQuantity / totalQuantity) * 100;

                ProductionQuality oeeQualityEntity = new ProductionQuality();
                oeeQualityEntity.setSite(combination.getSite());
                oeeQualityEntity.setWorkcenter_id(combination.getWorkcenterId());
                oeeQualityEntity.setResource_id(combination.getResourceId());
                oeeQualityEntity.setShift_id(combination.getShiftId());
                oeeQualityEntity.setPcu(combination.getPcu());
                oeeQualityEntity.setOperation(combination.getOperation());
                oeeQualityEntity.setOperation_version(combination.getOperationVersion());
                oeeQualityEntity.setItem(combination.getItem());
                oeeQualityEntity.setItem_version(combination.getItemVersion());
                oeeQualityEntity.setTotal_quantity(totalQuantity);
                oeeQualityEntity.setGood_quantity(goodQuantity);
                oeeQualityEntity.setQuality_percentage(qualityPercentage);
                oeeQualityEntity.setUser_id(productionLogRequest.getUserId());
                oeeQualityEntity.setActive(1);
                oeeQualityEntity.setCreated_date_time(LocalDateTime.now());

                qualityResponses.add(oeeQualityEntity);
            }
        } catch (ProductionLogException e){
            throw e;
        } catch(Exception e){
            throw new Exception(e);
        }

        return qualityResponses;
    }


    public static LocalDateTime convertToUtc(LocalDateTime dateString, ZoneId originalZone) {

        ZonedDateTime zonedDateTime = dateString.atZone(originalZone);

        ZonedDateTime utcZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));

        return utcZonedDateTime.toLocalDateTime();
    }

    @Override
    public List<ProductionLogDto> getUniqueCombinations(ProductionLogRequest productionLogRequest) throws Exception {

        LocalDateTime startDateTime = productionLogRequest.getStartDateTime();

        if (startDateTime == null) {

            startDateTime = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        }

        ZoneId originalZone = ZoneId.of("Asia/Kolkata");
        LocalDateTime utcStartTime = convertToUtc(startDateTime, originalZone);

        List<ProductionLogDto> combinations = productionLogRepository.findUniqueCombinations(utcStartTime, productionLogRequest.getSite());
        return combinations;
    }
    @Override
    public List<ScrapAndReworkTrendResponse.ScrapAndReworkTrend> getScrapAndReworkForResource(OeeFilterRequest qualityFilter) throws Exception {
        try{
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);

            Root<ProductionLog> root = query.from(ProductionLog.class);

            Expression<LocalDate> dateExpr = root.get("created_datetime");

            Expression<Long> scrapCount = cb.sum(
                    cb.<Long>selectCase()
                            .when(cb.equal(root.get("event_type"), "PCU_SCRAP"), 1L)
                            .otherwise(0L)
            );

            Expression<Long> reworkCount = cb.sum(
                    cb.<Long>selectCase()
                            .when(cb.equal(root.get("event_type"), "PCU_REWORK"), 1L)
                            .otherwise(0L)
            );

            query.multiselect(dateExpr, scrapCount, reworkCount);

            List<Predicate> predicates = whereConditions(qualityFilter, cb, root);
            query.where(cb.and(predicates.toArray(new Predicate[0])));

            query.groupBy(dateExpr);
            query.orderBy(cb.asc(dateExpr));

            List<Object[]> results = entityManager.createQuery(query).getResultList();

            List<ScrapAndReworkTrendResponse.ScrapAndReworkTrend> quality = new ArrayList<>();
            ScrapAndReworkTrendResponse.ScrapAndReworkTrend qualityData = null;
            for (Object[] result : results) {
                LocalDateTime dateVal = (LocalDateTime) result[0];
                Long scrapQuality = (Long) result[1];
                Long reworkQuality = (Long) result[2];

                qualityData = new ScrapAndReworkTrendResponse.ScrapAndReworkTrend();
                qualityData.setDate(dateVal !=null ? dateVal.toString() : "");
                qualityData.setScrapValue(scrapQuality !=null ? scrapQuality : 0);
                qualityData.setReworkValue(reworkQuality !=null ? reworkQuality : 0);

                quality.add(qualityData);
            }
            return quality;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Predicate> whereConditions(OeeFilterRequest request, CriteriaBuilder cb, Root<ProductionLog> root){

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.between(root.get("created_datetime"), request.getStartTime(), request.getEndTime()));

        if (StringUtils.isNotEmpty(request.getSite())) {
            predicates.add(root.get("site").in(request.getSite()));
        }
        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            predicates.add(root.get("resource_id").in(request.getResourceId()));
        }
        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            predicates.add(root.get("shift_id").in(request.getShiftId()));
        }
        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            predicates.add(root.get("workcenter_id").in(request.getWorkcenterId()));
        }
//        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
//            predicates.add(root.get("batch_number").in(request.getBatchNumber()));
//        }

        if (request.getItem() != null && !request.getItem().isEmpty()) {
            List<Predicate> itemPredicates = new ArrayList<>();

            for (Item item : request.getItem()) {
                itemPredicates.add(
                        cb.and(
                                cb.equal(root.get("item"), item.getItem()),
                                cb.equal(root.get("item_version"), item.getItemVersion())
                        )
                );
            }
            predicates.add(cb.or(itemPredicates.toArray(new Predicate[0])));
        }

        if (request.getOperation() != null && !request.getOperation().isEmpty()) {
            List<Predicate> operationPredicates = new ArrayList<>();

            for (Operation operation : request.getOperation()) {
                operationPredicates.add(
                        cb.and(
                                cb.equal(root.get("operation"), operation.getOperation()),
                                cb.equal(root.get("operation_version"), operation.getOperationVersion())
                        )
                );
            }
            predicates.add(cb.or(operationPredicates.toArray(new Predicate[0])));
        }
        return predicates;
    }

    @Override
    public List<ProductionLogResponseDto> calculateActualCycleTimeAndActualQuantity(ProductionLogRequest productionLogRequest)throws Exception {

        List<ProductionLogResponseDto> performanceResponses = new ArrayList<>();

        List<ProductionLogDto> combinations = getUniqueCombinations(productionLogRequest);

        for (ProductionLogDto combination : combinations) {

            Double actualCycleTime = productionLogRepository.calculateActualCycleTime(
                    combination.getSite(),
                    combination.getShift_id(),
                    combination.getWorkcenter_id(),
                    combination.getResource_id(),
                    combination.getPcu(),
                    combination.getOperation(),
                    combination.getOperation_version(),
                    combination.getItem(),
                    combination.getItem_version()
            );

            Double manufacturedTime = productionLogRepository.calculateManufacturedTime(
                    combination.getSite(),
                    combination.getShift_id(),
                    combination.getWorkcenter_id(),
                    combination.getResource_id(),
                    combination.getPcu(),
                    combination.getOperation(),
                    combination.getOperation_version(),
                    combination.getItem(),
                    combination.getItem_version()
            );

            Double actualQuantity = productionLogRepository.calculateActualQuantity(
                    combination.getSite(),combination.getWorkcenter_id(),combination.getOperation(), combination.getOperation_version(),
                    combination.getResource_id(), combination.getItem(), combination.getItem_version(),combination.getShift_id(),combination.getPcu());

            ProductionLogResponseDto responseDto = new ProductionLogResponseDto();
            responseDto.setActualCycleTime(actualCycleTime);
            responseDto.setManufacturedTime(manufacturedTime);
            responseDto.setActualQuantity(actualQuantity);

            performanceResponses.add(responseDto);
        }

        return performanceResponses;
    }



    @Override
    public List<OeeProductionLogResponse> getTotalProducedQuantity(OeeProductionLogRequest request) {
        // Query the database; assume the query returns an Object[] with columns as described above.
        List<Object[]> result = productionLogRepository.getTotalProducedQuantity(
                request.getSite(),
                request.getResourceId(),
                request.getEventType(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime(),
                request.getItemId(),
                request.getItemVersion(),
                request.getWorkcenterId(),
                request.getOperationId(),
                request.getOperationVersion(),
                request.getShiftId(),
                request.getShopOrderBo(),
                request.getBatchNo()
        );

        // We'll group records by shiftId first.
        // For each shift, we'll group by shopOrder.
        Map<String, Map<String, List<OeeProductionLogResponse.ShopOrderBreakdown>>> shiftShopOrderBreakdowns = new HashMap<>();
        // Also track overall shift totals for material and for operation.
        // For material summary, key: item+itemVersion; value: totalQty.
        Map<String, Double> materialSummary = new HashMap<>();
        // For operation summary, key: operation+operationVersion; value: totalQty.
        Map<String, Double> operationSummary = new HashMap<>();
        // Also track grand total per shift and event type.
        Map<String, Double> grandTotalByShift = new HashMap<>();
        Map<String, String> eventTypeByShift = new HashMap<>();
        Map<String, String> reasonCodeByShift = new HashMap<>();

        for (Object[] record : result) {
            // Extract columns based on index (adjust if your query returns columns in different order)
            String shiftId = (String) record[0];
            String shopOrder = (String) record[1];
            Number totalQtyNumber = (Number) record[2];
            String eventType = (String) record[3];
            String batchNumber = (String) record[4];
            String operation = (String) record[5];
            String operationVersion = (String) record[6];
            String item = (String) record[7];
            String itemVersion = (String) record[8];
            String reasonCode = (String) record[9];
            double totalQty = totalQtyNumber.doubleValue();

            // Update grand total for the shift
            grandTotalByShift.put(shiftId, grandTotalByShift.getOrDefault(shiftId, 0.0) + totalQty);
            eventTypeByShift.put(shiftId, eventType);
            reasonCodeByShift.put(shiftId, reasonCode);

            // Update material summary for the shift (only if item is non-empty)
            if (item != null && !item.trim().isEmpty() && itemVersion != null) {
                String materialKey = item + "_" + itemVersion;
                materialSummary.put(materialKey, materialSummary.getOrDefault(materialKey, 0.0) + totalQty);
            }

            // Update operation summary for the shift (only if operation is non-empty)
            if (operation != null && !operation.trim().isEmpty() && operationVersion != null) {
                String opKey = operation + "_" + operationVersion;
                operationSummary.put(opKey, operationSummary.getOrDefault(opKey, 0.0) + totalQty);
            }

            // Get or create inner map for the shift
            Map<String, List<OeeProductionLogResponse.ShopOrderBreakdown>> shopOrderMap =
                    shiftShopOrderBreakdowns.computeIfAbsent(shiftId, k -> new HashMap<>());
            // Use shopOrder as key (even if empty)
            List<OeeProductionLogResponse.ShopOrderBreakdown> breakdownList =
                    shopOrderMap.computeIfAbsent(shopOrder, k -> new ArrayList<>());
            //BatchNoHeader batchNoHeader=getBatchHeader();
           /* BatchNoHeader batchNoHeader=batchNoHeaderService.getBySiteAndBatchNumber(request.getSite(),batchNumber);
            String batchSize = Optional.ofNullable(batchNoHeader)
                    .map(BatchNoHeader::getTotalQuantity)
                    .map(Object::toString)
                    .filter(size -> !size.trim().isEmpty())   // Ensure it's not empty
                    .orElse("1");  // Fallback to "1" if null or empty
*/
            String batchSize = "1"; // default fallback

            if (batchNumber != null && !batchNumber.trim().isEmpty()) {
                BatchNoHeader batchNoHeader = batchNoHeaderService.getBySiteAndBatchNumber(request.getSite(), batchNumber);
                batchSize = Optional.ofNullable(batchNoHeader)
                        .map(BatchNoHeader::getTotalQuantity)
                        .map(Object::toString)
                        .filter(size -> !size.trim().isEmpty())
                        .orElse("1");
            }

            // Create a breakdown record from this row.
            OeeProductionLogResponse.ShopOrderBreakdown breakdown =
                    new OeeProductionLogResponse.ShopOrderBreakdown(
                            shopOrder, totalQty, batchNumber, operation,
                            operationVersion, item, itemVersion, batchSize
                    );

            breakdown.setItemId(item);
            breakdown.setItemVersion(itemVersion);
            breakdownList.add(breakdown);
        }

        // Build the final response list per shift.
        List<OeeProductionLogResponse> responseList = new ArrayList<>();
        for (String shiftId : shiftShopOrderBreakdowns.keySet()) {
            Map<String, List<OeeProductionLogResponse.ShopOrderBreakdown>> shopOrderMap = shiftShopOrderBreakdowns.get(shiftId);
            List<OeeProductionLogResponse.ShopOrderBreakdown> finalBreakdownList = new ArrayList<>();

            // Process each shopOrder group within the shift.
            for (Map.Entry<String, List<OeeProductionLogResponse.ShopOrderBreakdown>> entry : shopOrderMap.entrySet()) {
                String shopOrder = entry.getKey();
                // Check the shopOrder at the start; if it's null or empty after trimming, skip this entry entirely.
                if (shopOrder == null || shopOrder.trim().isEmpty()) {
                    continue;
                }

                List<OeeProductionLogResponse.ShopOrderBreakdown> breakdownList = entry.getValue();
                // Add all individual breakdowns.
                finalBreakdownList.addAll(breakdownList);
                // Compute summary for this shop order.
                double shopOrderSum = breakdownList.stream().mapToDouble(b -> b.getTotalQty()).sum();
                // Append a summary row for this shop order (with shopOrder preserved).
               /* OeeProductionLogResponse.ShopOrderBreakdown shopOrderSummary =
                        new OeeProductionLogResponse.ShopOrderBreakdown(shopOrder, shopOrderSum, "", "", "","","","");
                // Optionally, you might want to include material or operation info if consistent across the shop order.
                finalBreakdownList.add(shopOrderSummary);*/
                if (shopOrder != null && !shopOrder.trim().isEmpty()) {
                       OeeProductionLogResponse.ShopOrderBreakdown shopOrderSummary =
                            new OeeProductionLogResponse.ShopOrderBreakdown(shopOrder, shopOrderSum, "", "", "", "", "", "");
                    finalBreakdownList.add(shopOrderSummary);
                }


            }

            // Now, add overall shift-level summaries.
            // Material summary: create one summary row per material key.
            for (String materialKey : materialSummary.keySet()) {
                double sum = materialSummary.get(materialKey);
                // Split key into item and itemVersion.
                String[] parts = materialKey.split("_", 2);
                String matItem = parts[0];
                String matItemVersion = parts.length > 1 ? parts[1] : "";
                // Use an empty shopOrder to indicate overall summary.
                finalBreakdownList.add(new OeeProductionLogResponse.ShopOrderBreakdown("", sum, "", "", "", matItem, matItemVersion,""));
            }
            // Operation summary: create one summary row per operation key.
            for (String opKey : operationSummary.keySet()) {
                double sum = operationSummary.get(opKey);
                String[] parts = opKey.split("_", 2);
                String op = parts[0];
                String opVer = parts.length > 1 ? parts[1] : "";
                finalBreakdownList.add(new OeeProductionLogResponse.ShopOrderBreakdown("", sum, "", op, opVer, "", "",""));
            }

            Double grandTotal = grandTotalByShift.get(shiftId);
            String eventType = eventTypeByShift.get(shiftId);
            String reasonCode = reasonCodeByShift.get(shiftId);
            responseList.add(new OeeProductionLogResponse(shiftId, grandTotal, finalBreakdownList, eventType, reasonCode));
        }

        return responseList;
    }


    /*@Override
    public List<OeeProductionLogResponse> getTotalProducedQuantity(OeeProductionLogRequest request) {
        // Query the database
        List<Object[]> result = productionLogRepository.getTotalProducedQuantity(
                request.getSite(),
                request.getResourceId(),
                request.getEventType(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime(),
                request.getItemId(),
                request.getItemVersion(),
                request.getWorkcenterId(),
                request.getOperationId(),
                request.getOperationVersion(),
                request.getShiftId(),
                request.getShopOrderBo(),
                request.getBatchNo()
        );

        // First, group records by shiftId and then by shopOrder.
        // Using nested maps: shiftId -> (shopOrder -> List of ShopOrderBreakdown records)
        Map<String, Map<String, List<OeeProductionLogResponse.ShopOrderBreakdown>>> shiftShopOrderBreakdowns = new HashMap<>();
        Map<String, Double> grandTotalByShift = new HashMap<>();
        Map<String, String> eventTypeByShift = new HashMap<>();

        for (Object[] record : result) {
            String shiftId = (String) record[0];
            String shopOrder = (String) record[1];
            Number totalQtyNumber = (Number) record[2];
            String eventType = (String) record[3];
            String batchNumber = (String) record[4];
            // New fields:
            String operation = (String) record[5];
            String operationVersion = (String) record[6];
            double totalQty = totalQtyNumber.doubleValue();

            // Update grand total for the shift
            grandTotalByShift.put(shiftId, grandTotalByShift.getOrDefault(shiftId, 0.0) + totalQty);
            // Store eventType for this shift
            eventTypeByShift.put(shiftId, eventType);

            // Get or create the inner map for the shift
            Map<String, List<OeeProductionLogResponse.ShopOrderBreakdown>> shopOrderMap =
                    shiftShopOrderBreakdowns.computeIfAbsent(shiftId, k -> new HashMap<>());

            // Get or create the list of breakdowns for this shopOrder
            List<OeeProductionLogResponse.ShopOrderBreakdown> breakdownList =
                    shopOrderMap.computeIfAbsent(shopOrder, k -> new ArrayList<>());

            // Add this record as a breakdown
            breakdownList.add(new OeeProductionLogResponse.ShopOrderBreakdown(shopOrder, totalQty, batchNumber,operation,operationVersion));
        }

        // Now, build the final response list
        List<OeeProductionLogResponse> responseList = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<OeeProductionLogResponse.ShopOrderBreakdown>>> shiftEntry : shiftShopOrderBreakdowns.entrySet()) {
            String shiftId = shiftEntry.getKey();
            Map<String, List<OeeProductionLogResponse.ShopOrderBreakdown>> shopOrderMap = shiftEntry.getValue();

            List<OeeProductionLogResponse.ShopOrderBreakdown> finalBreakdownList = new ArrayList<>();
            // Process each shop order group within the shift
            for (Map.Entry<String, List<OeeProductionLogResponse.ShopOrderBreakdown>> shopOrderEntry : shopOrderMap.entrySet()) {
                String shopOrder = shopOrderEntry.getKey();
                List<OeeProductionLogResponse.ShopOrderBreakdown> breakdownList = shopOrderEntry.getValue();

                // Add all individual batch breakdowns for this shop order
                finalBreakdownList.addAll(breakdownList);

                // Calculate the sum for the entire shop order
                double shopOrderSum = breakdownList.stream()
                        .mapToDouble(OeeProductionLogResponse.ShopOrderBreakdown::getTotalQty)
                        .sum();

                // Append an extra record for the shop order sum, with an empty batchNumber
                finalBreakdownList.add(new OeeProductionLogResponse.ShopOrderBreakdown(shopOrder, shopOrderSum, "","",""));
            }

            // Create the response for the shift
            Double grandTotalQty = grandTotalByShift.get(shiftId);
            String eventType = eventTypeByShift.get(shiftId);
            responseList.add(new OeeProductionLogResponse(shiftId, grandTotalQty, finalBreakdownList, eventType));
        }

        return responseList;
    }*/



    @Override
    public List<UniqueItemVersion> getUniqueItemVersions(OeeProductionLogRequest request) {
        // Query the database
        List<Object[]> result = productionLogRepository.getUniqueItemVersions(
                request.getResourceId(),
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );

        // Process the results
        Set<UniqueItemVersion> uniqueItemVersionSet = new HashSet<>();

        for (Object[] record : result) {
            String itemId = (String) record[0];
            String itemVersion = (String) record[1];

            uniqueItemVersionSet.add(new UniqueItemVersion(itemId, itemVersion));
        }

        return new ArrayList<>(uniqueItemVersionSet);
    }

    @Override
    public List<UniqueItemVersion> getUniqueItemVersionsByWorkCenter(OeeProductionLogRequest request) {

        WorkCenterRequest workCenterRequest = new WorkCenterRequest();
        workCenterRequest.setSite(request.getSite());
        workCenterRequest.setWorkCenter(request.getWorkcenterId());

        // Fetch resources associated with the work center
        List<String> resources = fetchLineResources(workCenterRequest);

        if (resources.isEmpty()) {
            return Collections.emptyList();
        }

        /*// Query the database for unique item versions using the list of resources
        List<Object[]> result = productionLogRepository.getUniqueItemVersionsByResources(
                resources,
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );*/

        // Query the database for unique item versions using the list of resources
        List<Object[]> result = productionLogRepository.getUniqueItemVersionsByResourcesEventTypeDone(
                resources,
                request.getIntervalStartDateTime(),
                request.getIntervalEndDateTime()
        );

        // Process the results
        Set<UniqueItemVersion> uniqueItemVersionSet = new HashSet<>();

        for (Object[] record : result) {
            String itemId = (String) record[0];
            String itemVersion = (String) record[1];

            uniqueItemVersionSet.add(new UniqueItemVersion(itemId, itemVersion));
        }

        return new ArrayList<>(uniqueItemVersionSet);
    }

    private List<String> fetchLineResources(WorkCenterRequest workCenterRequest) {
        WorkCenter workCenter = webClientBuilder.build()
                .post()
                .uri(retrieveWorkcenter)
                .bodyValue(workCenterRequest)
                .retrieve()
                .bodyToMono(WorkCenter.class)
                .block();

        List<String> resources = new ArrayList<>();
        for (Association association : workCenter.getAssociationList()) {
            if ("Work Center".equalsIgnoreCase(association.getType())) {
                WorkCenterRequest nestedRequest = new WorkCenterRequest();
                nestedRequest.setSite(workCenterRequest.getSite());
                nestedRequest.setWorkCenter(association.getAssociateId());
                resources.addAll(fetchLineResources(nestedRequest));
            } else if ("Resource".equalsIgnoreCase(association.getType())) {
                resources.add(association.getAssociateId());
            }
        }
        return resources;
    }


    @Override
    public List<ProductionLog> getEventsByInterval(LocalDateTime startTime, LocalDateTime endTime,String site) {
        return productionLogRepository.findByCreatedDatetimeBetweenAndSiteAndActive(startTime, endTime,site,1);
    }

    public List<ProductionLog> getTotalProducedQuantity(ProductionLogRequest productionLog) {
        // Start constructing the query with placeholders
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT resource_id, site, workcenter_id, shift_id, item, item_version, operation, operation_version, SUM(qty) AS totalProducedQty ")
                .append("FROM r_production_log WHERE qty IS NOT NULL ")
                .append("AND created_datetime BETWEEN ? AND ? ");

        // Add conditions dynamically
        if (productionLog.getResourceId() != null) {
            queryBuilder.append("AND resource_id = ? ");
        }
        if (productionLog.getSite() != null) {
            queryBuilder.append("AND site = ? ");
        }
        if (productionLog.getWorkcenterId() != null) {
            queryBuilder.append("AND workcenter_id = ? ");
        }
        if (productionLog.getShiftId() != null) {
            queryBuilder.append("AND shift_id = ? ");
        }
        if (productionLog.getMaterial() != null) {
            queryBuilder.append("AND item = ? ");
        }
        if (productionLog.getMaterialVersion() != null) {
            queryBuilder.append("AND item_version = ? ");
        }
        if (productionLog.getOperation() != null) {
            queryBuilder.append("AND operation = ? ");
        }
        if (productionLog.getOperationVersion() != null) {
            queryBuilder.append("AND operation_version = ? ");
        }

        queryBuilder.append("GROUP BY resource_id, site, workcenter_id, shift_id, item, item_version, operation, operation_version ")
                .append("ORDER BY resource_id, site, workcenter_id, shift_id, item, item_version, operation, operation_version");

        // Build parameter list
        List<Object> params = new ArrayList<>();
        params.add(productionLog.getStartDateTime());
        params.add(productionLog.getEndDateTime());
        if (productionLog.getResourceId() != null) params.add(productionLog.getResourceId());
        if (productionLog.getSite() != null) params.add(productionLog.getSite());
        if (productionLog.getWorkcenterId() != null) params.add(productionLog.getWorkcenterId());
        if (productionLog.getShiftId() != null) params.add(productionLog.getShiftId());
        if (productionLog.getMaterial() != null) params.add(productionLog.getMaterial());
        if (productionLog.getMaterialVersion() != null) params.add(productionLog.getMaterialVersion());
        if (productionLog.getOperation() != null) params.add(productionLog.getOperation());
        if (productionLog.getOperationVersion() != null) params.add(productionLog.getOperationVersion());

        // Execute query using JdbcTemplate
        String finalQuery = queryBuilder.toString();
        System.out.println("Executing Query: " + finalQuery); // Debugging purposes
        String finalQueryWithParams = replacePlaceholdersWithParams(queryBuilder.toString(), params);
        System.out.println("Executing Query with Parameters: " + finalQueryWithParams);

        return jdbcTemplate.query(
                finalQuery,
                params.toArray(),
                new BeanPropertyRowMapper<>(ProductionLog.class)
        );
    }



    private String replacePlaceholdersWithParams(String query, List<Object> params) {
        for (Object param : params) {
            String value = (param instanceof String || param instanceof LocalDateTime || param instanceof LocalDate)
                    ? "'" + param.toString() + "'"
                    : param.toString();
            query = query.replaceFirst("\\?", value);
        }
        return query;
    }
    public List<OeeAvailabilityEntity> findAvailability(AvailabilityRequest availabilityRequest) {
        // Make a POST request to the external service
        List<OeeAvailabilityEntity> shiftResponse = webClientBuilder
                .build()
                .post()
                .uri(getAvailabilityRecUrl)
                .body(BodyInserters.fromValue(availabilityRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference < List < OeeAvailabilityEntity >> () {})
                .block();
        return  shiftResponse;
    }

    public List<CycleTimePostgres> findCycleTimePostgres(String site) {
        CycleTimeRequest cycleTimeRequest= new CycleTimeRequest();
        cycleTimeRequest.setSite(site);
        // Make a POST request to the external service
        List<CycleTimePostgres> cycletime = webClientBuilder
                .build()
                .post()
                .uri(getCycleTimeRecsUrl)
                .body(BodyInserters.fromValue(cycleTimeRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference < List < CycleTimePostgres >> () {})
                .block();
        return  cycletime;
    }
    public BatchNoHeader getBatchHeader(String site, String batchNumber) {
        BatchNoHeaderRequest batchNoHeaderRequest = new BatchNoHeaderRequest();
        batchNoHeaderRequest.setSite(site);
        batchNoHeaderRequest.setBatchNumber(batchNumber);

        try {
            // Make a POST request to the external service
            return webClientBuilder
                    .build()
                    .post()
                    .uri(getBatchHeader)
                    .body(BodyInserters.fromValue(batchNoHeaderRequest))
                    .retrieve()
                    .bodyToMono(BatchNoHeader.class)
                    .block();
        } catch (WebClientResponseException e) {
            // Handle HTTP status errors (4xx, 5xx)
            System.err.println("HTTP Status Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            // Handle connection errors or timeouts
            System.err.println("Request Error: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other exceptions
            System.err.println("Unexpected Error: " + e.getMessage());
        }

        return null;  // Return null or consider using an optional or default object
    }



    @Override
    public List<ProductionLog> getProductionLogByEventType(String site,
                                                           LocalDateTime startDate,
                                                           LocalDateTime endDate,String eventType) {
        List<ProductionLog> productionLogs = productionLogRepository.findProductionLogs(startDate, endDate, site,eventType);
        AvailabilityRequest availabilityRequest= new AvailabilityRequest();
        availabilityRequest.setSite(site);
        availabilityRequest.setStartDateTime(startDate);
        availabilityRequest.setEndDateTime(endDate);




        return productionLogs;
    }




    @Override
    public LocalDateTime getFirstCreatedDateTimeByOrder(String event_type, String shop_order_bo, LocalDateTime start, LocalDateTime end) {
        Optional<ProductionLog> log = productionLogRepository
                .findFirstByEventTypeAndShopOrderBoAndCreated_datetimeBetweenOrderByCreated_datetimeAsc(event_type, shop_order_bo, start, end);
        return log.map(ProductionLog::getCreated_datetime).orElse(null);
    }

    @Override
    public LocalDateTime getFirstCreatedDateTimeByOperation(String event_type, String operation, String operation_version, LocalDateTime start, LocalDateTime end) {
        Optional<ProductionLog> log = productionLogRepository
                .findFirstByEvent_typeAndOperationAndOperation_versionAndCreated_datetimeBetweenOrderByCreated_datetimeAsc(event_type, operation, operation_version, start, end);
        return log.map(ProductionLog::getCreated_datetime).orElse(null);
    }

    @Override
    public LocalDateTime getFirstCreatedDateTimeByBatch(String event_type, String batchNo, LocalDateTime start, LocalDateTime end) {
        Optional<ProductionLog> log = productionLogRepository
                .findFirstByEvent_typeAndBatchNoAndCreated_datetimeBetweenOrderByCreated_datetimeAsc(event_type, batchNo, start, end);
        return log.map(ProductionLog::getCreated_datetime).orElse(null);
    }

    @Override
    public LocalDateTime getLastCreatedDateTimeByOrder(String event_type, String shop_order_bo, LocalDateTime start, LocalDateTime end) {
        Optional<ProductionLog> log = productionLogRepository
                .findFirstByEvent_typeAndShop_order_boAndCreated_datetimeBetweenOrderByCreated_datetimeDesc(event_type, shop_order_bo, start, end);
        return log.map(ProductionLog::getCreated_datetime).orElse(null);
    }

    @Override
    public LocalDateTime getLastCreatedDateTimeByOperation(String event_type,  String operation, String operation_version, LocalDateTime start, LocalDateTime end) {
        Optional<ProductionLog> log = productionLogRepository
                .findFirstByEvent_typeAndOperationAndOperation_versionAndCreated_datetimeBetweenOrderByCreated_datetimeDesc(event_type, operation, operation_version, start, end);
        return log.map(ProductionLog::getCreated_datetime).orElse(null);
    }

    @Override
    public LocalDateTime getLastCreatedDateTimeByBatch(String event_type, String batchNo, LocalDateTime start, LocalDateTime end) {
        Optional<ProductionLog> log = productionLogRepository
                .findFirstByEvent_typeAndBatchNoAndCreated_datetimeBetweenOrderByCreated_datetimeDesc(event_type, batchNo, start, end);
        return log.map(ProductionLog::getCreated_datetime).orElse(null);
    }

    @Override
    public LocalDateTime getFirstCreatedDateTimeByItem(String eventType, String item, String itemVersion,
                                                       LocalDateTime start, LocalDateTime end) {
        Optional<ProductionLog> log = productionLogRepository
                .findFirstByEvent_typeAndItemAndItem_versionAndCreated_datetimeBetweenOrderByCreated_datetimeAsc(
                        eventType, item, itemVersion, start, end);
        return log.map(ProductionLog::getCreated_datetime).orElse(null);
    }

    @Override
    public LocalDateTime getLastCreatedDateTimeByItem(String eventType, String item, String itemVersion,
                                                      LocalDateTime start, LocalDateTime end) {
        Optional<ProductionLog> log = productionLogRepository
                .findFirstByEvent_typeAndItemAndItem_versionAndCreated_datetimeBetweenOrderByCreated_datetimeDesc(
                        eventType, item, itemVersion, start, end);
        return log.map(ProductionLog::getCreated_datetime).orElse(null);
    }

    // 7. Get first created_datetime by event_type, item, item_version, operation, and operation_version.
    @Override
    public LocalDateTime getFirstCreatedDateTimeByItemAndOperation(String eventType, String item, String itemVersion,
                                                                   String operation, String operationVersion,
                                                                   LocalDateTime start, LocalDateTime end) {
        Optional<ProductionLog> log = productionLogRepository
                .findFirstByEvent_typeAndItemAndItem_versionAndOperationAndOperation_versionAndCreated_datetimeBetweenOrderByCreated_datetimeAsc(
                        eventType, item, itemVersion, operation, operationVersion, start, end);
        return log.map(ProductionLog::getCreated_datetime).orElse(null);
    }

    // 8. Get last created_datetime by event_type, item, item_version, operation, and operation_version.
    @Override
    public LocalDateTime getLastCreatedDateTimeByItemAndOperation(String eventType, String item, String itemVersion,
                                                                  String operation, String operationVersion,
                                                                  LocalDateTime start, LocalDateTime end) {
        Optional<ProductionLog> log = productionLogRepository
                .findFirstByEvent_typeAndItemAndItem_versionAndOperationAndOperation_versionAndCreated_datetimeBetweenOrderByCreated_datetimeDesc(
                        eventType, item, itemVersion, operation, operationVersion, start, end);
        return log.map(ProductionLog::getCreated_datetime).orElse(null);
    }


    @Override
    public LocalDateTime getFirstCreatedDateTimeByCriteria(String site, String resourceId, String eventType,
                                                           String shopOrderBo, String batchNo, String workcenterId,
                                                           String operation, String operationVersion, String item,
                                                           String itemVersion, LocalDateTime start, LocalDateTime end) {
        /*return productionLogRepository.findFirstCreatedDateTimeByCriteria(
                site, resourceId, shopOrderBo, batchNo, workcenterId,
                operation, operationVersion, item, itemVersion, start, end
        ).orElse(start);*/
        // If the eventType indicates a machine event, use the machine query.
        if (eventType != null && eventType.toLowerCase().contains("machine")) {
            return productionLogRepository.findFirstCreatedDateTimeByCriteriabyMachine(
                    site, resourceId, shopOrderBo, batchNo, workcenterId,
                    operation, operationVersion, item, itemVersion, start, end
            ).orElse(start);
        } else {
            // Otherwise, use the manual query.
            return productionLogRepository.findFirstCreatedDateTimeByCriteria(
                    site, resourceId, shopOrderBo, batchNo, workcenterId,
                    operation, operationVersion, item, itemVersion, start, end
            ).orElse(start);
        }

    }


   /* @Override
    public LocalDateTime getFirstCreatedDateTimeByCriteria(String site, String resourceId, String eventType,
                                                           String shopOrderBo, String batchNo, String workcenterId,
                                                           String operation, String operationVersion, String item,
                                                           String itemVersion, LocalDateTime start, LocalDateTime end) {
        return productionLogRepository.findFirstCreatedDateTimeByCriteria(
                site, resourceId, "startSfcBatch", shopOrderBo, batchNo, workcenterId,
                operation, operationVersion, item, itemVersion, start, end
        ).orElse(start);
    } */
    @Override
    public LocalDateTime getLastCreatedDateTimeByCriteria(String site, String resourceId, String eventType,
                                                          String shopOrderBo, String batchNo, String workcenterId,
                                                          String operation, String operationVersion, String item,
                                                          String itemVersion, LocalDateTime start, LocalDateTime end) {
       /* return productionLogRepository.findLastCreatedDateTimeByCriteria(
                site, resourceId, shopOrderBo, batchNo, workcenterId,
                operation, operationVersion, item, itemVersion, start, end
        ).orElse(end);*/
        if (eventType != null && eventType.toLowerCase().contains("machine")) {
            return productionLogRepository.findLastCreatedDateTimeByCriteriabyMachine(
                    site, resourceId, shopOrderBo, batchNo, workcenterId,
                    operation, operationVersion, item, itemVersion, start, end
            ).orElse(end);
        } else {
            return productionLogRepository.findLastCreatedDateTimeByCriteria(
                    site, resourceId, shopOrderBo, batchNo, workcenterId,
                    operation, operationVersion, item, itemVersion, start, end
            ).orElse(end);
        }
    }

    @Override
    public List<ProductionLog> retrieveBySiteAndOrderNoAndPhaseAndOperationAndEventType(String site, String orderNumber, String phaseId, String operationId, String eventType) {
        return productionLogRepository.findBySiteAndOrderNumberAndPhaseIdAndOperationAndEventType(site, orderNumber, phaseId, operationId, eventType);
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UniqueItemVersion {
        private String itemId;
        private String itemVersion;
    }
@Override

    public String getCycleTime(String batchNo, String site) {
        List<ProductionLog> logs = productionLogRepository.findBatchCycleTimes(batchNo, site);

        if (logs == null || logs.isEmpty()) {
            throw new RuntimeException("No data found for batch: " + batchNo + " at site: " + site);
        }

        // Find the first startSfcBatch and last doneSfcBatch
        LocalDateTime firstStartTime = null;
        LocalDateTime lastDoneTime = null;
        Set<String> resourceSet = new HashSet<>(); // Store unique resources
        Set<CycleTimeReq.ItemVersionReq> itemVersionSet = new HashSet<>(); // Store unique item-version pairs

        for (ProductionLog log : logs) {
            if ("startSfcBatch".equalsIgnoreCase(log.getEvent_type())) {
                if (firstStartTime == null) {
                    firstStartTime = log.getCreated_datetime();  // First occurrence
                }
            }

            if ("doneSfcBatch".equalsIgnoreCase(log.getEvent_type())) {
                lastDoneTime = log.getCreated_datetime();  // Always overwrite with the last occurrence
                if (log.getResource_id() != null) {
                    resourceSet.add(log.getResource_id()); // Add unique resources
                }
            }

            // Collect unique item + item_version combinations
            if (log.getItem() != null && log.getItem_version() != null) {
                itemVersionSet.add(new CycleTimeReq.ItemVersionReq(log.getItem(), log.getItem_version()));
            }
        }

        if (firstStartTime != null && lastDoneTime != null) {
            Duration duration = Duration.between(firstStartTime, lastDoneTime);
            long cycletimeinsec= duration.toSeconds();
             String cyccletime=String.valueOf(cycletimeinsec);
            return cyccletime;// Return cycle time in seconds
        }

        // If cycle time couldn't be determined, call fetchCycleTimesByItems
        String resource = resourceSet.isEmpty() ? null : resourceSet.iterator().next(); // Pick first unique resource

        // If cycle time couldn't be determined, call fetchCycleTimesByItems
        CycleTimeReq cycleTimeReq = CycleTimeReq.builder()
                .site(site)
                .resourceId(resource)  // Pass resource (may be null)
                .itemVersionReqs(new ArrayList<>(itemVersionSet)) // Unique item-versions
                .build();

        List<CycleTime> cycleTimes = fetchCycleTimesByResourceAndItems(cycleTimeReq);
        return !cycleTimes.isEmpty() ? String.valueOf(cycleTimes.get(0).getCycleTime()) : null;

        //throw new RuntimeException("Insufficient data: missing start or done event");
    }
    private List<CycleTime> fetchCycleTimesByResourceAndItems(CycleTimeReq cycleTimeReq) {
        return webClientBuilder.build()
                .post()
                .uri(getCycletimesByResourceAndItem)
                .bodyValue(cycleTimeReq)
                .retrieve()
                .bodyToFlux(CycleTime.class)
                .collectList()
                .block();
    }

    public String getWorkCenterFromService(String site, String resource) {
        RetrieveRequest request = RetrieveRequest.builder()
                .site(site)
                .resource(resource)
                .build();



        try {
            return webClientBuilder.build()
                    .post()
                    .uri(getWorkcenterbyResource)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // If using reactive, replace with async handling
        } catch (Exception e) {
           // log.error("Failed to retrieve WorkCenter from remote service", e);
            return null;
        }
    }

}

