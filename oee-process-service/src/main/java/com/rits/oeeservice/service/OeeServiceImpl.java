package com.rits.oeeservice.service;

import com.rits.availability.dto.AvailabilityByDownTimeResponse;
import com.rits.availability.dto.AvailabilityRequest;
import com.rits.availability.dto.OverallAvailabilityResponse;
import com.rits.availability.repository.OeeAvailabilityRepository;
import com.rits.common.dto.OeeFilterRequest;
import com.rits.downtimeservice.dto.DowntimeRequest;
import com.rits.oeeservice.dto.*;
import com.rits.oeeservice.dto.WorkCenter;
import com.rits.oeeservice.exception.OeeException;
import com.rits.oeeservice.model.*;
import com.rits.oeeservice.repository.*;
import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.AggregatedOee;
import com.rits.overallequipmentefficiency.repository.AggregatedOeeRepository;
import com.rits.overallequipmentefficiency.repository.AggregatedTimePeriodRepository;
import com.rits.overallequipmentefficiency.service.AvailabilityService;
import com.rits.performance.dto.PerformanceByDowntimeResponse;
import com.rits.performance.dto.PerformanceComparisonResponse;
import com.rits.performance.dto.PerformanceRequest;
import com.rits.performance.model.OeePerformanceEntity;
import com.rits.oeeservice.event.CalculationEvent;
import com.rits.performance.repository.PerformanceRepository;
import com.rits.quality.dto.ScrapAndReworkTrendResponse;
import com.rits.quality.model.ProductionQuality;
import com.rits.quality.repository.QualityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OeeServiceImpl implements OeeService {
    private static final Set<String> VALID_EVENT_TYPES = Set.of("completesfcbatch", "scrapsfc", "donesfcbatch","machinecompletesfcbatch", "machinescrapsfc", "machinedonesfcbatch");
    private static final double DEFAULT_ENERGY_USAGE = 0.0;
    private List<ShiftResponse> allShifts = new ArrayList<>();
    private final AggregatedTimePeriodRepository aggregatedTimePeriodRepository;

    private OeeShiftRepository shiftRepository;
    private final JdbcTemplate jdbcTemplate;
    //    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;
    String location = this.getClass().getName();

    // For application name (assuming you're using @Value injection)
    @Value("${spring.application.name:defaultAppName}")
    private String applicationName;
    @Value("${cycletime-service.url}/getCycletimesByWorkcenter")
    private String getCycletimesByWorkcenter;
    @Value("${cycletime-service.url}/getCycletimesByResource")
    private String getCycletimesByResource;
    @Value("${cycletime-service.url}/getCycleTimeRecs")
    private String getCycletimesByitem;
    @Value("${shift-service.url}/plannedProductionTimes")
    private String getPlannedProductionTimes;
    @Value("${shift-service.url}/CurrentCompleteShiftDetails")
    private String getCurrentCompleteShiftDetails;
    @Value("${shift-service.url}/retrieveAll")
    private String getAllShifts;
    @Value("${shift-service.url}/getBreakHours")
    private String getBreakHours;

    @Value("${shift-service.url}/retrieve")
    private String getretretrieveShifts;

    @Value("${workcenter-service.url}/retrieveTrackOeeWorkcenters")
    private String retrieveTrackOeeWorkcenters;

    @Value("${resource-service.url}/retrieveByResource")
    private String retrieveByResource;
    @Value("${resource-service.url}/retrieveBySiteAndErpEquipmentNumber")
    private String retrieveBySiteAndErpEquipmentNumber;
    @PersistenceContext
    private EntityManager entityManager;
    @Value("${availability.url}/getAvailabilityRec")
    private String getAvailabilityUrl;
    @Value("${availability.url}/logAvailability")
    private String logAvailabilityUrl;
    @Value("${performance-service.url}/getPerfromanceByDateRange")
    private String getPerfromanceByDateRangeUrl;
    @Value("${performance-service.url}/calculatePerformance")
    private String calculatePerformanceUrl;

    @Value("${quality-service.url}/getQualityByDateTime")
    private String getQualityByDateTimeUrl;
    @Value("${quality-service.url}/calculateQuality")
    private String calculateQualityUrl;

    @Value("${downtime-service.url}/log")
    private String logDowntimeUrl;
    @Value("${oee-calculation--service.url}/getCurruntStatus")
    private String getDowntimeStatusByresourceUrl;
    @Value("${oee-calculation--service.url}/getBreakHoursBetweenTimeWithDetails")
    private String getBreakHoursBetweenTimeWithDetails;
    @Value("${productionlog-service.url}/getBatchActualCyleTime")
    private String getBatchActualCyleTime;

    @Value("${productionlog-service.url}/save")
    private String createProductionlog;

    @Value("${workcenter-service.url}/getWorkCenterByResource")
    private String workcenterUrl;

    @Value("${workcenter-service.url}/retrieve")
    private String workcenterRetrieveUrl;

    private final ApplicationEventPublisher eventPublisher;
    private final WebClient.Builder webClientBuilder;
    private final OeeRepository oeeRepository;
    @Autowired
    private AggregatedOeeRepository aggregatedOeeRepository;

    @Autowired
    private OeeAvailabilityRepository availabilityRepository;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private QualityRepository qualityRepository;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private OeeService oeeService;

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public OeeCalculationResponse calculateOee(OeeRequestList requestList) {
        List<OeeResponse> oeeResponses = new ArrayList<>();
        List<ShiftMessage> messages = new ArrayList<>();

        for (OeeRequest request : requestList.getRequests()) {
            List<String> resourceId = request.getResourceId();

            Optional<OeeShift> activeShiftOpt = shiftRepository.findActiveShift(
                    request.getSite(), request.getWorkcenterId().toString(), String.valueOf(request.getResourceId()));

            if (!activeShiftOpt.isPresent()) {
                messages.add(new ShiftMessage(resourceId.toString(), "No active shift found for resource: " + resourceId));
                continue;
            }

            Optional<Oee> oeeDataOpt = oeeRepository.findOeeData(
                    request.getSite(), request.getWorkcenterId().toString(), request.getResourceId().toString(), request.getItemBo().toString()
            );

            if (!oeeDataOpt.isPresent()) {
                messages.add(new ShiftMessage(resourceId.toString(), "No OEE data found for resource: " + resourceId));
                continue;
            }

            Oee oeeData = oeeDataOpt.get();

            ItemDetails itemDetails = new ItemDetails();
            itemDetails.setItemBo(oeeData.getItemBo());
            itemDetails.setActual(oeeData.getGoodQty());
            itemDetails.setPlan(oeeData.getTotalQty());
            itemDetails.setRejection(oeeData.getBadQty());
            itemDetails.setProductionTime((int) oeeData.getProductionTime());
            itemDetails.setActualTime((int) oeeData.getActualTime());
            itemDetails.setDowntime(oeeData.getTotalDowntime());
            itemDetails.setGoodQualityCount(oeeData.getGoodQty());
            itemDetails.setBadQualityCount(oeeData.getBadQty());

            double availability = oeeData.getAvailability();
            double performance = oeeData.getPerformance();
            double quality = oeeData.getQuality();

            OeeResponse response = new OeeResponse();
            response.setResource(resourceId.toString());
            response.setOee(Math.round(availability * performance * quality * 100.0) / 100.0);
            response.setAvailability(availability);
            response.setPerformance(performance);
            response.setQuality(quality);
            response.setDowntime(oeeData.getTotalDowntime() > 0);
            response.setItemDetails(itemDetails);

            messages.add(new ShiftMessage(resourceId.toString(), "Details found for resource: " + resourceId));

            oeeResponses.add(response);
        }

        if (oeeResponses.isEmpty()) {
            messages.add(new ShiftMessage(null, "No valid OEE data found for the given criteria."));
        }

        return new OeeCalculationResponse(oeeResponses, messages);
    }

    @Override
    public Boolean calculateOEE(OeeFilterRequest request) {
        Boolean created = false;

        // Fetch current time for endDateTime
        LocalDateTime currentTime = LocalDateTime.now();
        setStartAndEndTimes(request);

        // Fetch Availability, Performance, and Quality data
        List<OeePerformanceEntity> performanceRecs = getPerfromanceByDateRange(request);
        List<ProductionQuality> qualityRecs = getQualityByDateTime(request);
        List<OverallAvailabilityResponse> availabiltiyRec = getAvailabilityRec(request);

        // List to accumulate OEE records
        List<Oee> oeeRecords = new ArrayList<>();

        // Ensure the lists are not null before looping
        if (performanceRecs == null || qualityRecs == null || availabiltiyRec == null) {
            return created; // Return false if any of the lists are null
        }

        // Loop through the performance records
        for (OeePerformanceEntity performance : performanceRecs) {
            // Define the combination key for quality and availability records
            String resourceId = performance.getResourceId();
            String workcenterId = performance.getWorkcenterId();
            String site = performance.getSite();
            String shiftId = performance.getShiftId();
            String item = performance.getItem();
            String itemVersion = performance.getItemVersion();
            String operation = performance.getOperation();
            String operationVersion = performance.getOperationVersion();
            String pcuOrBatchNumber = performance.getBatchNumber(); // Use batch number or pcu
            String shopOrder = performance.getShopOrderBO(); // Use shop order

            // Filter out matching quality records
            ProductionQuality matchingQuality = findMatchingQuality(qualityRecs, resourceId, workcenterId, site, shiftId, item, itemVersion, operation, operationVersion, pcuOrBatchNumber, shopOrder);

            // Filter out matching availability records
            double availability = 0;
            OverallAvailabilityResponse matchingAvailability = findMatchingAvailability(availabiltiyRec, resourceId);

            if (matchingAvailability != null && matchingAvailability.getTotalAvailableTimeSeconds() > 0) {
                availability = matchingAvailability.getTotalAvailableTimeSeconds();
            }

            // Check if we found matching quality and availability data
            if (matchingQuality != null) {
                // Calculate OEE
                double totalQuantity = matchingQuality.getTotalQuantity();
                double goodQuantity = matchingQuality.getGoodQuantity();
                double qualityPercentage = totalQuantity > 0 ? (goodQuantity / totalQuantity) * 100 : 0;

                double plannedOutput = performance.getPlannedOutput();
                double actualOutput = performance.getActualOutput();
                double performancePercentage = plannedOutput > 0 ? (actualOutput / plannedOutput) * 100 : 0;

                double downtimeDuration = performance.getDowntimeDuration();
                double totalDowntime = downtimeDuration > 0 ? downtimeDuration : 0;

// Availability calculation
                double plannedTime = performance.getPlannedCycleTime();
                double availabilityPercentage = plannedTime > 0 ? (plannedTime - totalDowntime) / plannedTime * 100 : 0;

// Calculate OEE: OEE = Availability * Performance * Quality
                double oee = (availabilityPercentage * performancePercentage * qualityPercentage) / 10000; // OEE as a percentage

// Now oee should be a value between 0 and 100
// OEE as a percentage

                // Create OEE entity
                Oee oeeEntity = Oee.builder()
                        .site(site != null ? site : "")
                        .shiftId(shiftId != null ? shiftId : "")
                        .pcuId(pcuOrBatchNumber != null ? pcuOrBatchNumber : "")
                        .workcenterId(workcenterId != null ? workcenterId : "")
                        .resourceId(resourceId != null ? resourceId : "")
                        .operationBo(operation != null ? operation : "")
                        .routingBo("") // You can add routingBo if necessary
                        .itemBo(item != null ? item : "")
                        .item(item != null ? item : "")
                        .itemVersion(itemVersion != null ? itemVersion : "")
                        .shoporderId(shopOrder != null ? shopOrder : "")
                        .totalDowntime((int) totalDowntime)
                        .availability(availabilityPercentage)
                        .performance(performancePercentage)
                        .quality(qualityPercentage)
                        .goodQty(goodQuantity)
                        .badQty(totalQuantity - goodQuantity)
                        .totalQty(totalQuantity)
                        .oee(oee)
                        .plan(1) // You can set plan field based on your requirements
                        .productionTime((int) plannedTime)
                        .actualTime((int) actualOutput)
                        .createdDatetime(currentTime)
                        .active(1) // Active field is set to 1
                        .build();

                // Add the OEE entity to the list
                oeeRecords.add(oeeEntity);
            }
        }

        // Save all OEE records in bulk
        if (!oeeRecords.isEmpty()) {
            oeeRepository.saveAll(oeeRecords);  // Save all OEE records in one batch
            created = true;
        }

        return created;
    }

    private ProductionQuality findMatchingQuality(List<ProductionQuality> qualityRecs, String resourceId, String workcenterId, String site, String shiftId, String item, String itemVersion, String operation, String operationVersion, String pcuOrBatchNumber, String shopOrder) {
        if (qualityRecs == null || qualityRecs.isEmpty()) {
            return null; // Return null if the list is empty
        }

        for (ProductionQuality q : qualityRecs) {
            if (q != null
                    && safeEquals(q.getResourceId(), resourceId)
                    && safeEquals(q.getSite(), site)
                    && safeEquals(q.getShiftId(), shiftId)
                    && safeEquals(q.getItem(), item)
                    && safeEquals(q.getItemVersion(), itemVersion)
                    && safeEquals(q.getOperation(), operation)
                    && safeEquals(q.getOperationVersion(), operationVersion)
                    && (safeEquals(q.getPcu(), pcuOrBatchNumber) || safeEquals(q.getBatchNumber(), pcuOrBatchNumber)))
            //&& safeEquals(q.getShop_order(), shopOrder))
            {
                System.out.println("Matching record found: " + q); // Debugging log
                return q;  // Return the matching record
            }
        }

        return null; // If no match found
    }


    // Helper method to safely compare strings, handling null values
    private boolean safeEquals(String str1, String str2) {
        return (str1 == null && str2 == null) || (str1 != null && str1.equals(str2));
    }


    private OverallAvailabilityResponse findMatchingAvailability(List<OverallAvailabilityResponse> availabilityRecs, String resourceId) {
        return availabilityRecs.stream()
                .filter(a -> a.getResourceId() != null && a.getResourceId().equals(resourceId))
                .findFirst()
                .orElse(null);
    }


    @Override
    public List<Oee> executeQuery() throws Exception {
        List<Oee> oeeRecords = new ArrayList<>();

        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            String sql = "CALL calculate_oee_procedure()";
            try (CallableStatement stmt = connection.prepareCall(sql)) {
                stmt.execute();

                ResultSet rs = stmt.getResultSet();
                if (rs != null) {
                    while (rs.next()) {
                        Oee record = new Oee();
                        record.setSite(rs.getString("site"));
                        record.setShiftId(rs.getString("shift_id"));
                        record.setPcuId(rs.getString("pcu_id"));
                        record.setWorkcenterId(rs.getString("workcenter_id"));
                        record.setResourceId(rs.getString("resource_id"));
                        record.setOperationBo(rs.getString("operation_bo"));
                        record.setRoutingBo(rs.getString("routing_bo"));
                        record.setItemBo(rs.getString("item_bo"));
                        record.setShoporderId(rs.getString("shoporder_id"));
                        record.setTotalDowntime(rs.getInt("total_downtime"));
                        record.setAvailability(rs.getDouble("availability"));
                        record.setPerformance(rs.getDouble("performance"));
                        record.setQuality(rs.getDouble("quality"));
                        record.setGoodQty(rs.getDouble("good_qty"));
                        record.setBadQty(rs.getDouble("bad_qty"));
                        record.setTotalQty(rs.getDouble("total_qty"));
                        record.setOee(rs.getDouble("oee"));
                        record.setActive(rs.getInt("active"));
                        oeeRecords.add(record);
                    }
                }
            }
        }

        return oeeRecords;
    }

    public List<OverallAvailabilityResponse> getAvailabilityRec(OeeFilterRequest request) {
        AvailabilityRequest availabilityRequest = new AvailabilityRequest();
        availabilityRequest.setSite(request.getSite());
        availabilityRequest.setStartDateTime(request.getStartDateTime());
        availabilityRequest.setEndDateTime(request.getEndDateTime());
        return webClientBuilder.build()
                .post()
                .uri(getAvailabilityUrl)
                .bodyValue(availabilityRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<OverallAvailabilityResponse>>() {
                })
                .block();
    }

    public List<OeePerformanceEntity> getPerfromanceByDateRange(OeeFilterRequest request) {

        return webClientBuilder.build()
                .post()
                .uri(getPerfromanceByDateRangeUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<OeePerformanceEntity>>() {
                })
                .block();
    }

    public List<ProductionQuality> getQualityByDateTime(OeeFilterRequest request) {

        return webClientBuilder.build()
                .post()
                .uri(getQualityByDateTimeUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductionQuality>>() {
                })
                .block();
    }
    /*@Override
    public OverallOeeResponse getOverallOee(OeeFilterRequest request) throws Exception {

        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();

        queryParameters.put("site", request.getSite());
        queryBuilder.append("SELECT ROUND(CAST(AVG(o.oee) AS NUMERIC), 2) AS avgOee ")
                .append("FROM R_OEE o ")
                .append("WHERE o.site = :site ");
        applyCommonFilters(queryBuilder, queryParameters, request);

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());

            queryParameters.forEach(query::setParameter);

            BigDecimal avgOee = (BigDecimal) query.getSingleResult();
            double oeePercentage = (avgOee != null) ? avgOee.doubleValue() : 0.0;

            OverallOeeResponse response = new OverallOeeResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());
            response.setResourceId(request.getResourceId() != null ? String.join(", ", request.getResourceId()) : null);
            response.setShiftId(request.getShiftId() != null ? String.join(", ", request.getShiftId()) : null);
            response.setOeePercentage(oeePercentage);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OeeByTimeResponse getOeeByTime(OeeFilterRequest request) throws Exception {

        StringBuilder sql = new StringBuilder(
                "SELECT o.created_datetime AS date, ROUND(CAST(AVG(o.oee) AS NUMERIC), 2) AS averageOee " +
                        "FROM R_OEE o " +
                        "WHERE o.site = :site ");

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        applyCommonFilters(sql, queryParameters, request);

        sql.append("GROUP BY o.created_datetime ORDER BY o.created_datetime");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            OeeByTimeResponse response = new OeeByTimeResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());
            response.setResourceId(request.getResourceId() != null ? String.join(", ", request.getResourceId()) : null);

            List<OeeByTimeResponse.OeeTimeData> oeeByTimeList = results.stream()
                    .map(result -> {
//                        java.sql.Timestamp timestamp = (java.sql.Timestamp) result[0];
//                        LocalDateTime dateTime = timestamp.toLocalDateTime();
//
//                        OeeByTimeResponse.OeeTimeData oeeOverTime = new OeeByTimeResponse.OeeTimeData();
//                        oeeOverTime.setDate(dateTime != null ? dateTime.format(dateFormatter) : "");

                        OeeByTimeResponse.OeeTimeData oeeOverTime = new OeeByTimeResponse.OeeTimeData();
                        if (result[0] != null) {
                            java.sql.Timestamp timestamp = (java.sql.Timestamp) result[0];
                            LocalDateTime dateTime = timestamp.toLocalDateTime();
                            oeeOverTime.setDate(dateTime.format(dateFormatter));
                        } else {
                            oeeOverTime.setDate("");
                        }

                        BigDecimal averageOee = (BigDecimal) result[1];
                        oeeOverTime.setOeePercentage(averageOee != null ? averageOee.doubleValue() : 0.0);

                        return oeeOverTime;
                    })
                    .collect(Collectors.toList());

            response.setOeeOverTime(oeeByTimeList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OeeByMachineResponse getOeeByMachine(OeeFilterRequest request) throws Exception {
        StringBuilder sql = new StringBuilder(
                "SELECT o.resource_id, ROUND(CAST(AVG(o.oee) AS NUMERIC), 2) AS averageOee " +
                        "FROM R_OEE o " +
                        "WHERE o.site = :site "
        );

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        applyCommonFilters(sql, queryParameters, request);
        sql.append("GROUP BY o.resource_id ");
        sql.append("ORDER BY o.resource_id");
        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            OeeByMachineResponse response = new OeeByMachineResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<OeeByMachineResponse.OeeMachineData> oeeDataList = results.stream()
                    .map(result -> {
                        OeeByMachineResponse.OeeMachineData data = new OeeByMachineResponse.OeeMachineData();
                        data.setResourceId(result[0] != null ? result[0].toString() : "");

                        BigDecimal oeePercentage = (BigDecimal) result[1];
                        data.setOeePercentage(oeePercentage != null ? oeePercentage.doubleValue() : 0.0);

                        return data;
                    })
                    .collect(Collectors.toList());

            response.setOeeByMachine(oeeDataList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    public OeeByShiftResponse getOeeByShift(OeeFilterRequest request) throws Exception {
        StringBuilder sql = new StringBuilder(
                "SELECT o.shift_id, " +
                        "ROUND(CAST(AVG(o.availability) AS NUMERIC), 2) AS availability, " +
                        "ROUND(CAST(AVG(o.performance) AS NUMERIC), 2) AS performance, " +
                        "ROUND(CAST(AVG(o.quality) AS NUMERIC), 2) AS quality " +
                        "FROM R_OEE o " +
                        "WHERE o.site = :site "
        );
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        applyCommonFilters(sql, queryParameters, request);

        sql.append("GROUP BY o.shift_id ");
        sql.append("ORDER BY o.shift_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            OeeByShiftResponse response = new OeeByShiftResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<OeeByShiftResponse.OeeShiftData> oeeDataList = results.stream()
                    .map(row -> {
                        OeeByShiftResponse.OeeShiftData data = new OeeByShiftResponse.OeeShiftData();
                        data.setShiftId(row[0] != null ? row[0].toString().split(",")[2] : "");
                        data.setAvailability(row[1] != null ? new BigDecimal(row[1].toString()).doubleValue() : 0.0);
                        data.setPerformance(row[2] != null ? new BigDecimal(row[2].toString()).doubleValue() : 0.0);
                        data.setQuality(row[3] != null ? new BigDecimal(row[3].toString()).doubleValue() : 0.0);
                        return data;
                    })
                    .collect(Collectors.toList());

            response.setOeeByShift(oeeDataList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public OeeByBreakdownResponse getOeeByBreakdown(OeeFilterRequest request) throws Exception {

        String eventTypeOfPerformance = null;
        String eventSource = null;
        if((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null) {
            eventTypeOfPerformance = "WORKCENTER";
            eventSource = request.getEventSource() + "_DONE";
        } else {
            eventTypeOfPerformance = "RESOURCE";
            eventSource = request.getEventSource() + "_COMPLETE";
        }

        Map<String, Object> queryParameters = new HashMap<>();
        String sql = buildOEEBreakDownQuery(queryParameters, request, eventTypeOfPerformance, eventSource);

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            OeeByBreakdownResponse response = new OeeByBreakdownResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<OeeByBreakdownResponse.OeeBreakdownData> oeeBreakdownList = results.stream()
                    .map(row -> {
                        OeeByBreakdownResponse.OeeBreakdownData data = new OeeByBreakdownResponse.OeeBreakdownData();
                        data.setResourceId(row[0] != null ? row[0].toString() : "");

//                        double totalGoodQty = (row[1] != null) ? ((Number) row[1]).doubleValue() : 0.0;
//                        double totalBadQty = (row[2] != null) ? ((Number) row[2]).doubleValue() : 0.0;
//                        double totalPlannedQty = (row[3] != null) ? ((Number) row[3]).doubleValue() : 0.0;
//                        double totalDowntime = (row[4] != null) ? ((Number) row[4]).doubleValue() : 0.0;
//
//                        LocalDateTime intervalStartTime = (row[5] != null) ? ((java.sql.Timestamp) row[5]).toLocalDateTime() : null;
//                        LocalDateTime intervalEndTime = (row[6] != null) ? ((java.sql.Timestamp) row[6]).toLocalDateTime() : null;
//
//                        // Calculate total time in seconds
//                        long totalTimeSeconds = (intervalStartTime != null && intervalEndTime != null) ?
//                                Duration.between(intervalStartTime, intervalEndTime).getSeconds() : 0L;
//
//                        long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
//                        long actualTimeSeconds = totalTimeSeconds;

                        // Calculate availability, performance, quality, and OEE
//                        double availability = (productionTimeSeconds > 0) ? ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;
//                        double performance = (totalPlannedQty > 0) ? ((totalGoodQty + totalBadQty) / totalPlannedQty) * 100 : 0.0;
//                        double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;
//                        double oee = (availability * performance * quality) / 10000;

                        double availability = (row[1] != null) ? ((Number) row[1]).doubleValue() : 0.0;
                        double performance = (row[2] != null) ? ((Number) row[2]).doubleValue() : 0.0;
                        double quality = (row[3] != null) ? ((Number) row[3]).doubleValue() : 0.0;

                        data.setAvailability(Math.round(availability * 100.0) / 100.0);
                        data.setPerformance(Math.round(performance * 100.0) / 100.0);
                        data.setQuality(Math.round(quality * 100.0) / 100.0);

                        // Check if all values are zero, and exclude the entry if they are
                        if (availability == 0.0 && performance == 0.0 && quality == 0.0) {
                            return null;
                        }

                        return data;
                    })
                    .filter(Objects::nonNull) // Filter out null entries
                    .collect(Collectors.toList());

            response.setOeeBreakdown(oeeBreakdownList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String buildOEEBreakDownQuery(Map<String, Object> queryParameters, OeeFilterRequest request, String eventTypeOfPerformance, String eventSource) {

        StringBuilder sql = new StringBuilder(
                "SELECT o.resource_id, " +
                        "o.availability, " +
                        "o.performance, " +
                        "o.quality " +
                        "FROM r_aggregated_oee o " +
                        "WHERE o.active = TRUE AND (o.planned_quantity > 0 OR o.total_quantity > 0) "
        );

        // Append the common WHERE clause
        sql.append(buildWhereClause(queryParameters, request, eventTypeOfPerformance, eventSource));

        sql.append(" GROUP BY o.resource_id, o.availability, o.performance, o.quality ")
                .append(" ORDER BY o.resource_id");

        return sql.toString();
    }

    public String buildOEEByComponentQuery(Map<String, Object> queryParameters, OeeFilterRequest request, String eventTypeOfPerformance, String eventSource) {

        StringBuilder sql = new StringBuilder(
                "SELECT DATE_TRUNC('second', o.created_datetime) AS date, " +
                        "o.availability, " +
                        "o.performance, " +
                        "o.quality " +
                        "FROM r_aggregated_oee o " +
                        "WHERE o.active = TRUE AND (o.planned_quantity > 0 OR o.total_quantity > 0) "
        );

        // Append the common WHERE clause
        sql.append(buildWhereClause(queryParameters, request, eventTypeOfPerformance, eventSource));

        sql.append(" GROUP BY DATE_TRUNC('second', o.created_datetime), o.availability, o.performance, o.quality " +
                " ORDER BY DATE_TRUNC('second', o.created_datetime) ");

        return sql.toString();
    }


    public String buildOEEByProductQuery(Map<String, Object> queryParameters, OeeFilterRequest request, String eventTypeOfPerformance, String eventSource) {

        StringBuilder sql = new StringBuilder(
                "SELECT o.item, " +
                        "o.availability, " +
                        "o.performance, " +
                        "o.quality, " +
                        "o.oee, " +
                        "SUM(o.total_good_quantity) AS goodqty, " +
                        "SUM(o.total_bad_quantity) AS badqty " +
                        "FROM r_aggregated_oee o " +
                        "WHERE o.active = TRUE " +
                        "AND (o.planned_quantity > 0 OR o.total_quantity > 0) "
        );
        if ("MANUAL".equalsIgnoreCase(request.getEventSource())) {
            sql.append("AND COALESCE(o.item, '') <> '' ");
        }

        // Append the common WHERE clause
        sql.append(buildWhereClause(queryParameters, request, eventTypeOfPerformance, eventSource));

        sql.append(" GROUP BY o.item, o.availability, o.performance, o.quality, o.oee ORDER BY o.item ");

        return sql.toString();
    }

    public String buildOEEByAvailabilityDowntimeQuery(Map<String, Object> queryParameters, OeeFilterRequest request) {

        StringBuilder sql = new StringBuilder(
                "SELECT d.resource_id, d.reason, d.totalDowntime, " +
                        "o.availability " +
                        "FROM ( " +
                        "    SELECT resource_id, reason, SUM(downtime_duration) AS totalDowntime " +
                        "    FROM R_DOWNTIME " +
                        "    WHERE site = :site " +
                        "    AND reason IS NOT NULL " +
                        "    AND downtime_start >= :startTime " +
                        "    AND downtime_end <= :endTime " +
                        "    GROUP BY resource_id, reason " +
                        ") d " +
                        "LEFT JOIN r_aggregated_oee o ON d.resource_id = o.resource_id AND o.site = :site " +
                        "WHERE o.active = TRUE "
//                        "AND COALESCE(o.batch_number, '') <> '' "
        );


        // Append the common WHERE clause
        sql.append(buildWhereClause(queryParameters, request, "RESOURCE", request.getEventSource()+"_COMPLETE"));

        sql.append("GROUP BY d.resource_id, d.reason, d.totalDowntime, o.availability ")
                .append("ORDER BY d.resource_id, d.reason");

        return sql.toString();
    }

    public String buildOEEPerformanceComparisonQuery(Map<String, Object> queryParameters, OeeFilterRequest request) {

        StringBuilder sql = new StringBuilder(
                "SELECT o.resource_id, " +
                        "o.performance, " +
                        "o.quality, " +
                        "COALESCE(d.totalDowntime, 0) AS totalDowntime " +
                        "FROM r_aggregated_oee o " +
                        "LEFT JOIN ( " +
                        "    SELECT resource_id, site, SUM(downtime_duration) AS totalDowntime " +
                        "    FROM r_downtime " +
                        "    WHERE site = :site " +
                        "    GROUP BY resource_id, site " +
                        ") d ON o.resource_id = d.resource_id AND o.site = d.site " +
                        "WHERE o.active = TRUE "
        );


        // Append the common WHERE clause
        sql.append(buildWhereClause(queryParameters, request, "RESOURCE", (request + "_COMPLETE")));

        sql.append("GROUP BY o.resource_id, d.totalDowntime, o.performance, o.quality ")
                .append("ORDER BY o.resource_id");

        return sql.toString();
    }

    public String buildOEEPerformanceDowntimeQuery(Map<String, Object> queryParameters, OeeFilterRequest request) {

        StringBuilder sql = new StringBuilder(
                "SELECT o.item, d.reason, " +
                        "d.total_downtime AS totalDowntime, " +
                        "o.performance " +
                        "FROM ( " +
                        "    SELECT resource_id, reason, SUM(downtime_duration) AS total_downtime " +
                        "    FROM R_DOWNTIME " +
                        "    WHERE site = :site " +
                        "    AND reason IS NOT NULL " +
                        "    GROUP BY resource_id, reason " +
                        ") d " +
                        "LEFT JOIN r_aggregated_oee o " +
                        "ON d.resource_id = o.resource_id " +
                        "AND o.active = TRUE "
        );

        // Append the common WHERE clause
        String whereClause = buildWhereClause(queryParameters, request, "RESOURCE", request.getEventSource() + "_COMPLETE");
        sql.append(whereClause);

        sql.append("GROUP BY o.item, d.reason, d.total_downtime, o.performance ")
                .append("ORDER BY o.item, d.reason");

        return sql.toString();
    }

    public String buildOEEScrapAnReworkTrendQuery(Map<String, Object> queryParameters, OeeFilterRequest request, String eventTypeOfPerformance, String eventSource) {

        StringBuilder sql = new StringBuilder(
                " SELECT DATE_TRUNC('second', o.created_datetime) AS date, " +
                        "o.total_bad_quantity AS scrapValue, o.quality " +
                        "FROM r_aggregated_oee o " +
                        "WHERE o.active = TRUE "
        );

        // Append the common WHERE clause
        sql.append(buildWhereClause(queryParameters, request, eventTypeOfPerformance, eventSource));

        sql.append("GROUP BY DATE_TRUNC('second', o.created_datetime), o.total_bad_quantity, o.quality ")
                .append("ORDER BY DATE_TRUNC('second', o.created_datetime) ");

        return sql.toString();
    }

//    @Override
//    public OeeByProductionLineResponse getOeeByProductionLine(OeeFilterRequest request) throws Exception {
//
//        StringBuilder sql = new StringBuilder(
//                "SELECT o.workcenter_id, ROUND(CAST(AVG(o.oee) AS NUMERIC), 2) AS oeePercentage " +
//                        "FROM R_OEE o " +
//                        "WHERE o.site = :site ");
//
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("site", request.getSite());
//
//        applyCommonFilters(sql, queryParameters, request);
//        sql.append("GROUP BY o.workcenter_id ");
//        sql.append("ORDER BY o.workcenter_id");
//
//        try {
//            Query query = entityManager.createNativeQuery(sql.toString());
//            queryParameters.forEach(query::setParameter);
//
//            List<Object[]> results = query.getResultList();
//
//            OeeByProductionLineResponse response = new OeeByProductionLineResponse();
//            response.setStartTime(request.getStartTime());
//            response.setEndTime(request.getEndTime());
//
//            List<OeeByProductionLineResponse.OeeProductionLineData> oeeDataList = results.stream()
//                    .map(row -> {
//                        OeeByProductionLineResponse.OeeProductionLineData data = new OeeByProductionLineResponse.OeeProductionLineData();
//                        data.setWorkcenterId(row[0]!=null ? row[0].toString() : "");
//                        BigDecimal oeePercentage = (BigDecimal) row[1];
//                        data.setOeePercentage(oeePercentage != null ? oeePercentage.doubleValue() : 0.0);
//                        return data;
//                    })
//                    .collect(Collectors.toList());
//
//            response.setOeeByProductionLine(oeeDataList);
//            return response;
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//    @Override
//    public OeeByProductResponse getOeeByProduct(OeeFilterRequest request) {
//        StringBuilder queryBuilder = new StringBuilder();
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("site", request.getSite());
//
//        // Build the query
//        queryBuilder.append("SELECT o.item, o.item_version, ")
//                .append("AVG(o.availability) as avg_availability, ")
//                .append("AVG(o.performance) as avg_performance, ")
//                .append("AVG(o.quality) as avg_quality ")
//                .append("FROM R_OEE o ")
//                .append("WHERE o.site = :site ");
//
//        applyCommonFilters(queryBuilder, queryParameters, request);
//        queryBuilder.append("GROUP BY o.item, o.item_version ")
//                .append("ORDER BY o.item");
//
//
//        try {
//            Query query = entityManager.createNativeQuery(queryBuilder.toString());
//            queryParameters.forEach(query::setParameter);
//            List<Object[]> results = query.getResultList();
//
//            List<OeeByProductResponse.OeeProductData> oeeProductList = results.stream()
//                    .map(result -> {
//                        String itemVal = result[0] != null ? result[0].toString() : "";
//                        String itemVersionVal = result[1] != null ? result[1].toString() : "";
//
//                        Double availability = result[2] != null ?
//                                (result[2] instanceof BigDecimal ?
//                                        ((BigDecimal) result[2]).doubleValue() :
//                                        Double.parseDouble(result[2].toString())) :
//                                0.0;
//
//                        Double performance = result[3] != null ?
//                                (result[3] instanceof BigDecimal ?
//                                        ((BigDecimal) result[3]).doubleValue() :
//                                        Double.parseDouble(result[3].toString())) :
//                                0.0;
//
//                        Double quality = result[4] != null ?
//                                (result[4] instanceof BigDecimal ?
//                                        ((BigDecimal) result[4]).doubleValue() :
//                                        Double.parseDouble(result[4].toString())) :
//                                0.0;
//
//                        return OeeByProductResponse.OeeProductData.builder()
//                                .product(itemVal + "/" + itemVersionVal)
//                                .availability(availability.intValue())
//                                .performance(performance.intValue())
//                                .quality(quality.intValue())
//                                .build();
//                    })
//                    .collect(Collectors.toList());
//
//            return OeeByProductResponse.builder()
//                    .oeeByProduct(oeeProductList)
//                    .startTime(request.getStartTime() != null ? request.getStartTime().toString() : "")
//                    .endTime(request.getEndTime() != null ? request.getEndTime().toString() : "")
//                    .build();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public OeeByComponentResponse getOeeByComponent(OeeFilterRequest request) throws Exception {

        String eventTypeOfPerformance = null;
        String eventSource = null;
        if(request.getResourceId() != null || request.getOperation() != null) {
            eventTypeOfPerformance = "RESOURCE";
            eventSource = request.getEventSource() + "_COMPLETE";
        } else {
            eventTypeOfPerformance = "WORKCENTER";
            eventSource = request.getEventSource() + "_DONE";
        }

        Map<String, Object> queryParameters = new HashMap<>();
        String sql = buildOEEByComponentQuery(queryParameters, request, eventTypeOfPerformance, eventSource);

        try {
            Query query = entityManager.createNativeQuery(sql);
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            OeeByComponentResponse response = new OeeByComponentResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            Map<LocalDateTime, List<double[]>> hourBuckets = new TreeMap<>();
            LocalDateTime minTime = null;

            for (Object[] result : results) {
                if (result[0] == null) continue;

                LocalDateTime dateTime = ((Timestamp) result[0]).toLocalDateTime();
                if (minTime == null || dateTime.isBefore(minTime)) minTime = dateTime;

                double availability = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
                double performance = result[2] != null ? ((Number) result[2]).doubleValue() : 0.0;
                double quality = result[3] != null ? ((Number) result[3]).doubleValue() : 0.0;

                if (availability == 0 && performance == 0 && quality == 0) continue;

                long hoursDiff = Duration.between(minTime, dateTime).toHours();
                LocalDateTime bucketStart = minTime.plusHours(hoursDiff);

                hourBuckets.computeIfAbsent(bucketStart, k -> new ArrayList<>())
                        .add(new double[]{availability, performance, quality});
            }

            List<OeeByComponentResponse.OeeComponentData> oeeComponentList = hourBuckets.entrySet().stream()
                    .map(entry -> {
                        List<double[]> entries = entry.getValue();

                        double avgAvailability = entries.stream().mapToDouble(e -> e[0]).average().orElse(0.0);
                        double avgPerformance = entries.stream().mapToDouble(e -> e[1]).average().orElse(0.0);
                        double avgQuality = entries.stream().mapToDouble(e -> e[2]).average().orElse(0.0);

                        OeeByComponentResponse.OeeComponentData data = new OeeByComponentResponse.OeeComponentData();
                        data.setDate(entry.getKey().format(dateFormatter));
                        data.setAvailability(Math.round(avgAvailability * 100.0) / 100.0);
                        data.setPerformance(Math.round(avgPerformance * 100.0) / 100.0);
                        data.setQuality(Math.round(avgQuality * 100.0) / 100.0);
                        return data;
                    })
                    .collect(Collectors.toList());

            response.setOeeByComponent(oeeComponentList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public OeeByOrderResponse getOeeByOrder(OeeFilterRequest request) throws Exception {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT o.shoporder_id, ")
                .append("ROUND(CAST(AVG(o.availability) AS NUMERIC), 2) AS availability, ")
                .append("ROUND(CAST(AVG(o.performance) AS NUMERIC), 2) AS performance, ")
                .append("ROUND(CAST(AVG(o.quality) AS NUMERIC), 2) AS quality ")
                .append("FROM R_OEE o ")
                .append("WHERE o.site = :site ");

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        // Time range filtering logic using interval overlap
        if (request.getStartTime() == null && request.getEndTime() == null) {
            // Default to the last 24 hours if no start or end time is provided
            sql.append("AND o.created_datetime BETWEEN CURRENT_TIMESTAMP - INTERVAL '24 HOURS' AND CURRENT_TIMESTAMP ");
        } else if (request.getStartTime() != null && request.getEndTime() != null) {
            queryParameters.put("startTime", request.getStartTime());
            queryParameters.put("endTime", request.getEndTime());
            sql.append("AND o.created_datetime BETWEEN :startTime AND :endTime ");
        }

        // Additional filters
        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            sql.append(" AND o.resource_id IN (:resourceId) ");
            queryParameters.put("resourceId", request.getResourceId());
        }
        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            sql.append(" AND o.workcenter_id IN (:workcenterId) ");
            queryParameters.put("workcenterId", request.getWorkcenterId());
        }
        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            sql.append(" AND o.shift_id IN (:shiftId) ");
            queryParameters.put("shiftId", request.getShiftId());
        }
        if (request.getShoporderId() != null && !request.getShoporderId().isEmpty()) {
            sql.append(" AND o.shoporder_id IN (:shoporderId) ");
            queryParameters.put("shoporderId", request.getShoporderId());
        }

        // Group and order by shop order ID
        sql.append(" GROUP BY o.shoporder_id");
        sql.append(" ORDER BY o.shoporder_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();


            OeeByOrderResponse response = new OeeByOrderResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());
            response.setShoporderId(request.getShoporderId() != null ? String.join(", ", request.getShoporderId()) : null);

            List<OeeByOrderResponse.OeeOrderData> oeeOrderList = results.stream()
                    .map(row -> {
                        OeeByOrderResponse.OeeOrderData data = new OeeByOrderResponse.OeeOrderData();
                        data.setAvailability(row[1] != null ? new BigDecimal(row[1].toString()).doubleValue() : 0.0);
                        data.setPerformance(row[2] != null ? new BigDecimal(row[2].toString()).doubleValue() : 0.0);
                        data.setQuality(row[3] != null ? new BigDecimal(row[3].toString()).doubleValue() : 0.0);
                        return data;
                    })
                    .collect(Collectors.toList());

            response.setOeeByOrder(oeeOrderList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setStartAndEndTimes(OeeFilterRequest request) {
        LocalDateTime currentTime = LocalDateTime.now();

        // Check if startDateTime and endDateTime are already set
        if (request.getStartDateTime() != null && request.getEndDateTime() != null) {
            // Use the provided start and end times
            return;
        }

        // If not set, compute them based on interval
        request.setEndDateTime(currentTime);

        int intervalSeconds = request.getEventIntervalSeconds();
        LocalDateTime startDateTime = intervalSeconds > 0
                ? currentTime.minusSeconds(intervalSeconds)
                : currentTime.minusMinutes(60);

        request.setStartDateTime(startDateTime);
    }

    @Override
    public Boolean calculate(OeeFilterRequest request) {
        setStartAndEndTimes(request);

        try {
            // Start the chain by publishing the first event
            eventPublisher.publishEvent(new CalculationEvent(this, request, "DOWN_TIME"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    public List<Map<String,Object>> getCurrentStatus(String site) {
        DowntimeRequest downtimeRequest=new DowntimeRequest();
        downtimeRequest.setSite(site);
        return webClientBuilder.build()
                .post()
                .uri(getDowntimeStatusByresourceUrl)
                .bodyValue(downtimeRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String,Object>>>() {})
                .block();
    }

    public DowntimeSummary getDownTimeSummary(DowntimeRequest downtimeRequest) {
        return webClientBuilder.build()
                .post()
                .uri(getBreakHoursBetweenTimeWithDetails)
                .bodyValue(downtimeRequest)
                .retrieve()
                .bodyToMono( DowntimeSummary.class)
                .block();
    }

    private Boolean calculateDownTime(OeeFilterRequest request){
        System.out.println("Starting calculateDownTime...");
        DowntimeRequest downtimeRequest=new DowntimeRequest();
        downtimeRequest.setSite(request.getSite());
        downtimeRequest.setDowntimeStart(request.getStartDateTime());
        downtimeRequest.setDowntimeEnd(request.getEndDateTime());
        Boolean calculated=false;
        calculated= webClientBuilder.build()
                .post()
                .uri(logDowntimeUrl)
                .bodyValue(downtimeRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return calculated;
    }
    private Boolean calculateAvailability(OeeFilterRequest request){
        System.out.println("Starting calculateAvailability...");
        AvailabilityRequest availabilityRequest=new AvailabilityRequest();
        availabilityRequest.setSite(request.getSite());
        availabilityRequest.setStartDateTime(request.getStartDateTime());
        availabilityRequest.setEndDateTime(request.getEndDateTime());
        Boolean calculated=false;
        calculated= webClientBuilder.build()
                .post()
                .uri(logAvailabilityUrl)
                .bodyValue(availabilityRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return calculated;
    }
    private Boolean calculatePerformance(OeeFilterRequest request){
        System.out.println("Starting calculatePerformance...");
        Boolean calculated=false;
        PerformanceRequest performanceRequest=new PerformanceRequest();
        performanceRequest.setSite(request.getSite());
        performanceRequest.setStartDateTime(request.getStartDateTime());
        performanceRequest.setEndDateTime(request.getEndDateTime());
        calculated= webClientBuilder.build()
                .post()
                .uri(calculatePerformanceUrl)
                .bodyValue(performanceRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return calculated;
    }
    private Boolean calculateQuality(OeeFilterRequest request){
        System.out.println("Starting calculateQuality...");
        Boolean calculated=false;
        calculated= webClientBuilder.build()
                .post()
                .uri(calculateQualityUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return calculated;
    }

    private Boolean calculateOee(OeeFilterRequest request) {
        System.out.println("Starting calculateOee...");
        Boolean calculated = false;
        calculated = calculateOEE(request);
        return calculated;
    }

    private void applyCommonFilters(StringBuilder queryBuilder, Map<String, Object> queryParameters, OeeFilterRequest request) {

        if (request.getStartTime() != null && request.getEndTime() != null) {

            queryParameters.put("startTime", request.getStartTime());
            queryParameters.put("endTime", request.getEndTime());
            queryBuilder.append("AND o.interval_start_date_time <= :endTime ")
                    .append("AND o.interval_end_date_time >= :startTime ");
        } else {
            LocalDateTime now = LocalDateTime.now();
            //LocalDateTime startTime = now.minusHours(24);

            ShiftRequest shiftreq = new ShiftRequest();
            shiftreq.setSite(request.getSite());
            LocalDateTime startTime = getEarliestValidShiftStartDateTime(shiftreq);

            queryParameters.put("startTime", startTime);
            queryParameters.put("endTime", now);

            request.setStartTime(startTime);
            request.setEndTime(now);

            queryBuilder.append("AND o.interval_start_date_time <= :endTime ")
                    .append("AND o.interval_end_date_time >= :startTime ");
        }

        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            queryBuilder.append("AND o.resource_id IN :resourceId ");
            queryParameters.put("resourceId", request.getResourceId());
        }

        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            queryBuilder.append("AND o.shift_id IN :shiftId ");
            queryParameters.put("shiftId", request.getShiftId());
        }

        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            queryBuilder.append("AND o.workcenter_id IN :workcenterId ");
            queryParameters.put("workcenterId", request.getWorkcenterId());
        }

        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
            queryBuilder.append("AND o.batch_number IN :batchNumber ");
            queryParameters.put("batchNumber", request.getBatchNumber());
        }


        /*// Handle multiple items filtering
        if (request.getItem() != null && !request.getItem().isEmpty()) {
            List<String> itemBOList = new ArrayList<>();
            List<String> itemVersionList = new ArrayList<>();

            for (String item : request.getItem()) {
                if (item.contains("/")) {
                    String[] itemParts = item.split("/");
                    if (itemParts.length == 2) {
                        itemBOList.add(itemParts[0]);   // Extract itemBO
                        itemVersionList.add(itemParts[1]); // Extract itemVersion
                    }
                }
            }

            if (!itemBOList.isEmpty()) {
                queryBuilder.append("AND o.item IN (:itemBO) ");
                queryParameters.put("itemBO", itemBOList);
            }

            if (!itemVersionList.isEmpty()) {
                queryBuilder.append("AND o.item_version IN (:itemVersion) ");
                queryParameters.put("itemVersion", itemVersionList);
            }
        }*/
    }

    @Override
    public OeeByProductResponse retrieveByFilter(OeeFilterRequest request) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();

        queryBuilder.append("SELECT * FROM R_OEE ")
                .append("WHERE site = :site ");
        queryParameters.put("site", request.getSite());

        if (StringUtils.isNotBlank(request.getResource())) {
            queryBuilder.append("AND resource_id = :resource ");
            queryParameters.put("resource", request.getResource());
        }
        if (StringUtils.isNotBlank(request.getWorkcenter())) {
            queryBuilder.append("AND workcenter_id = :workcenter ");
            queryParameters.put("workcenter", request.getWorkcenter());
        }

        List<Map<String, Object>> queryResult;
        try {
            System.out.println("Generated Query: " + queryBuilder.toString());
            queryResult = namedJdbcTemplate.queryForList(queryBuilder.toString(), queryParameters);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching OEE data", e);
        }

        List<OeeResponse> responseList = new ArrayList<>();
        for (Map<String, Object> row : queryResult) {
            double downtime = ((Number) row.get("production_time")).intValue() - ((Number) row.get("actual_time")).intValue();
            OeeResponse response = new OeeResponse();
            response.setResource((String) row.get("resource_id"));
            response.setOee(((Number) row.get("oee")).doubleValue());
            response.setAvailability(((Number) row.get("availability")).doubleValue());
            response.setPerformance(((Number) row.get("performance")).doubleValue());
            response.setQuality(((Number) row.get("quality")).doubleValue());
            response.setDowntime(downtime > 0);
            // Populate item details
            ItemDetails itemDetails = new ItemDetails();
            itemDetails.setItemBo((row.get("item") + " " + row.get("item_version")));
            itemDetails.setPlan(((Number) row.get("plan")).intValue());
            itemDetails.setRejection(response.getAvailability() - response.getPerformance()); // Availability - Performance
            itemDetails.setProductionTime(((Number) row.get("production_time")).intValue());
            itemDetails.setActualTime(((Number) row.get("actual_time")).intValue());
            itemDetails.setDowntime(downtime); // Production - Actual time
            itemDetails.setGoodQualityCount(((Number) row.get("good_qty")).intValue());
            itemDetails.setBadQualityCount(((Number) row.get("bad_qty")).intValue());

            response.setItemDetails(itemDetails);

            responseList.add(response);
        }

        return OeeByProductResponse.builder().oeeResponses(responseList).build();
    }


   /* private List<ShiftDetails> fetchShiftDetailsForSite(List<Oee> oeeDetails) {
        List<ShiftDetails> shiftDetailsList = new ArrayList<>();
        // Group the data by shift, workcenter, resource, and batch
        Map<String, List<Oee>> groupedData = oeeDetails.stream()
                .filter(Objects::nonNull) // Ensure the element itself is not null
                .filter(oee -> oee.getShiftId() != null)
                .filter(oee -> oee.getWorkcenterId() != null)
                .filter(oee -> oee.getResourceId() != null)
                .filter(oee -> oee.getBatchNumber() != null)
                .filter(oee -> oee.getEventTypeOfPerformance().equalsIgnoreCase("completeSfcBatch") ||
                        oee.getEventTypeOfPerformance().equalsIgnoreCase("ScrapSFC") ||
                        oee.getEventTypeOfPerformance().equalsIgnoreCase("doneSfcBatch"))
                .collect(Collectors.groupingBy(oee ->
                        oee.getSite() + "-" + oee.getShiftId() + "-" + oee.getWorkcenterId() + "-" + oee.getResourceId() + "-" + oee.getBatchNumber()));

        // Iterate through each group

        // Iterate through each group (site)
        for (List<Oee> group : groupedData.values()) {
            if (group.get(0).getSite() != null && group.get(0).getSite() != "") {
                ShiftDetails shiftDetails = calculateShiftDetails(group, null);
                shiftDetailsList.add(shiftDetails);
            }
        }

        return shiftDetailsList;
    }
*/

    @Override
    public List<SiteDetails> getOeeDetailsBySite(OeeRequest request) {
        return null;
    }


    private List<SiteDetails> calculateSiteDetails(List<Oee> group, String eventSource) {
        Map<String, List<Oee>> groupedBySite = group.stream()
                .filter(Objects::nonNull) // Ensure the element itself is not null
                .filter(oee -> oee.getSite() != null)
                .filter(oee -> {
                    if (eventSource.equals("MANUAL")) {
                        return oee.getEventTypeOfPerformance().equalsIgnoreCase("completeSfcBatch");
                    } else {
                        return oee.getEventTypeOfPerformance().equalsIgnoreCase("machineCompleteSfcBatch");
                    }
                })
                .collect(Collectors.groupingBy(Oee::getSite));

        List<SiteDetails> siteDetailsList = new ArrayList<>();
        for (Map.Entry<String, List<Oee>> entry : groupedBySite.entrySet()) {
            if (group.get(0).getSite() != null && !group.get(0).getSite().isEmpty()) {
                List<Oee> siteGroup = entry.getValue();
                siteGroup.sort((oee1, oee2) -> oee1.getIntervalStartDateTime().compareTo(oee2.getIntervalStartDateTime()));

                ShiftRequest shiftreq= new ShiftRequest();
                shiftreq.setSite(group.get(0).getSite());
                LocalDateTime intervalStartDateTime=getEarliestValidShiftStartDateTime(shiftreq);

                // Convert Instant.now() to LocalDateTime using the system default time zone
                Instant nowInstant = Instant.now();
                LocalDateTime now = LocalDateTime.ofInstant(nowInstant, ZoneId.systemDefault());

                // Calculate duration in seconds
                long totalTimeSeconds = Duration.between(intervalStartDateTime, now).getSeconds();

                // Calculate cumulative values
                double totalGoodQty = siteGroup.stream().mapToDouble(Oee::getGoodQty).sum();
                double totalBadQty = siteGroup.stream().mapToDouble(Oee::getBadQty).sum();
                double totalPlannedQty = siteGroup.stream().mapToDouble(Oee::getPlan).sum();
                double totalDowntime = siteGroup.stream().mapToDouble(Oee::getTotalDowntime).sum();
                long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
                long actualTimeSeconds = totalTimeSeconds;

                // Calculate performance, availability, and quality from quantities
                double availability = (productionTimeSeconds > 0) ? ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;
                double performance = (totalPlannedQty > 0) ? (totalGoodQty / totalPlannedQty) * 100 : 0.0;
                double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;
                double oee = (availability * performance * quality) / 10000;

                // Create and populate SiteDetails for each site
                SiteDetails siteDetails = new SiteDetails();
                siteDetails.setSite(entry.getKey());
                siteDetails.setActual((int) (totalGoodQty + totalBadQty));
                siteDetails.setPlan((int) totalPlannedQty);
                siteDetails.setRejection((int) totalBadQty);
                siteDetails.setDowntimeDuration(totalDowntime);
                siteDetails.setGoodQualityCount((int) totalGoodQty);
                siteDetails.setBadQualityCount((int) totalBadQty);
                siteDetails.setEnergyUsage(0.0); // Placeholder for energy usage
                siteDetails.setProductionTime((int) productionTimeSeconds);
                siteDetails.setActualTime((int) actualTimeSeconds);

                // Set calculated values
                siteDetails.setAvailability(availability);
                siteDetails.setPerformance(performance);
                siteDetails.setQuality(quality);
                siteDetails.setOee(oee);

                siteDetailsList.add(siteDetails);
            }
        }

        return siteDetailsList;
    }


  /*  @Override
    public List<ShiftDetails> getOeeDetailsByShiftAndSite(OeeRequest request) {
        List<ShiftDetails> shiftDetailsList = new ArrayList<>();
        List<Oee> oeeDetails = oeeRepository.findBySiteAndShiftIdInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
                request.getSite(),
                request.getShiftId(),
                LocalDateTime.parse(request.getStartTime()),
                LocalDateTime.parse(request.getEndTime())
        );

        ShiftDetails shiftDetails = calculateShiftDetails(oeeDetails,null);
        shiftDetailsList.add(shiftDetails);
        return shiftDetailsList;
    }*/

    @Override
    public List<WorkcenterDetails> getOeeDetailsByWorkcenterAndShiftAndSite(OeeRequest request) {
        List<WorkcenterDetails> workcenterDetailsList = new ArrayList<>();
        List<Oee> oeeDetails = oeeRepository.findBySiteAndShiftIdInAndWorkcenterIdInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
                request.getSite(),
                request.getShiftId(),
                request.getWorkcenterId(),
                LocalDateTime.parse(request.getStartTime()),
                LocalDateTime.parse(request.getEndTime())
        );
        Map<String, Map<String, Double>> quantities = calculateTotalQuantitiesByWorkCenter(oeeDetails);
        WorkcenterDetails workcenterDetails = calculateWorkcenterDetails(oeeDetails,quantities);
        workcenterDetailsList.add(workcenterDetails);
        return workcenterDetailsList;
    }

    @Override
    public List<ResourceDetails> getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(OeeRequest request) {
        List<ResourceDetails> resourceDetailsList = new ArrayList<>();
        List<Oee> oeeDetails = oeeRepository.findBySiteAndShiftIdInAndWorkcenterIdInAndResourceIdInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
                request.getSite(),
                request.getShiftId(),
                request.getWorkcenterId(),
                request.getResourceId(),
                LocalDateTime.parse(request.getStartTime()),
                LocalDateTime.parse(request.getEndTime())
        );

        ResourceDetails resourceDetails = calculateResourceDetails(oeeDetails);
        resourceDetailsList.add(resourceDetails);
        return resourceDetailsList;
    }

    @Override
    public List<BatchDetails> getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(OeeRequest request) {
        List<BatchDetails> batchDetailsList = new ArrayList<>();
        List<Oee> oeeDetails = oeeRepository.findBySiteAndShiftIdInAndWorkcenterIdInAndResourceIdInAndBatchNumberInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
                request.getSite(),
                request.getShiftId(),
                request.getWorkcenterId(),
                request.getResourceId(),
                request.getBatchno(),
                LocalDateTime.parse(request.getStartTime()),
                LocalDateTime.parse(request.getEndTime())
        );
        oeeDetails.sort(Comparator.comparing(Oee::getIntervalStartDateTime));
        Map<String, Map<String, Double>> scrapqty = calculateTotalQuantitiesByBatch(oeeDetails);
        LocalDateTime intervalStartTime = oeeDetails.get(0).getIntervalStartDateTime();
        LocalDateTime intervalEndTime = oeeDetails.get(oeeDetails.size() - 1).getIntervalEndDateTime();
        long totalTimeSeconds = Duration.between(intervalStartTime, intervalEndTime).getSeconds();
        BatchDetails batchDetails = calculateBatchDetails(oeeDetails,totalTimeSeconds,scrapqty);
        batchDetailsList.add(batchDetails);
        return batchDetailsList;
    }


@Override
public List<ShiftDetails> getOeeDetailsByShiftId(OeeRequest oeeRequest){
    List<ShiftDetails> shiftDetailsList=new ArrayList<>();
    ShiftRequest shiftreq= new ShiftRequest();
    shiftreq.setSite(oeeRequest.getSite());
    LocalDateTime intervalStartDateTime=getEarliestValidShiftStartDateTime(shiftreq);

    LocalDateTime now = LocalDateTime.now();
    List<String> eventType = Arrays.asList("doneSfcBatch", "completeSfcBatch");
    List<Oee> oeeDetails = oeeRepository.findByIntervalAndSiteForBatch(
            intervalStartDateTime,
            now,
            oeeRequest.getSite(),
            oeeRequest.getEventSource().equals("MANUAL")
                    ? eventType
                    : List.of("machineDoneSfcBatch", "machineCompleteSfcBatch"),
            oeeRequest.getEventSource().equals("MANUAL")
    );
    Map<String, List<Oee>> groupedData = oeeDetails.stream()
            .filter(Objects::nonNull) // Ensure the element itself is not null
            .filter(oee -> {
                if (oeeRequest.getEventSource().equalsIgnoreCase("MANUAL")) {
                    return "ScrapSFC".equalsIgnoreCase(oee.getEventTypeOfPerformance())
                            || "doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                } else {
                    return "machineScrapSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())
                            || "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                }
            })
            .filter(oee -> !oeeRequest.getEventSource().equalsIgnoreCase("MANUAL") || (oee.getWorkcenterId() != null && !oee.getWorkcenterId().isEmpty()))
            .filter(oee -> oee.getShiftId() != null)
            .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty())
            .filter(oee -> !oeeRequest.getEventSource().equalsIgnoreCase("MANUAL") || (oee.getOperation() != null && !oee.getOperation().isEmpty()))
 // Ensure resourceId is not null/empty
            .collect(Collectors.groupingBy(oee ->
                    oee.getShiftId() ));
    System.out.println(groupedData);
    List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(oeeRequest.getSite());
    Map<String, List<Oee>> groupedDataWithComplete = oeeDetails.stream()
            .filter(Objects::nonNull)


            .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty())
            // Ensure resourceId is not null/empty
            .filter(oee -> {
                if (oeeRequest.getEventSource().equalsIgnoreCase("MANUAL")) {
                    return "completeSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())
                            || "doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                } else {
                    return "machineCompleteSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())
                            || "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                }
            })
            .filter(oee -> !oeeRequest.getEventSource().equalsIgnoreCase("MANUAL") || (oee.getWorkcenterId() != null && !oee.getWorkcenterId().isEmpty()))
            .filter(oee -> oee.getShiftId() != null)
            .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty())
            .filter(oee -> !oeeRequest.getEventSource().equalsIgnoreCase("MANUAL") || (oee.getOperation() != null && !oee.getOperation().isEmpty()))
            .collect(Collectors.groupingBy(Oee::getShiftId));

    // Get the set of resource IDs associated with each tracked WorkCenter, filtered by completeSfcBatch event type
    Set<String> workCenterResourceIdSet = oeeDetails.stream()
            .filter(oee -> {
                if (oeeRequest.getEventSource().equalsIgnoreCase("MANUAL")) {
                    return "completeSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())
                            || "doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                } else {
                    return "machineCompleteSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())
                            || "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                }
            })
            .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty()) // Ensure resourceId is not null or empty
            .map(Oee::getResourceId) // Extract the resourceId
            .collect(Collectors.toSet());

// Filter groupedDataWithComplete to only include matching workcenterId and resourceId
    Map<String, List<Oee>> groupedDataWithCompleteFiltered = groupedDataWithComplete.entrySet().stream()
            .filter(entry ->
                    trackedWorkCenters.stream()
                            .anyMatch(workCenter -> workCenter.getWorkCenter().equals(entry.getKey()) &&
                                    workCenter.getAssociationList().stream()
                                            .anyMatch(association -> workCenterResourceIdSet.contains(association.getAssociateId())))
            )
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


    Map<String, Map<String, Double>> scrapqty = calculateTotalQuantitiesForShiftId(groupedDataWithComplete);


// Extract shift IDs from groupedData into a HashSet for O(1) lookup
    Set<String> groupedShiftIds = groupedData.values().stream()
            .map(group -> group.get(0).getShiftId())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    ShiftRequest shiftreq1 = new ShiftRequest();
    shiftreq1.setSite(oeeRequest.getSite());
    List<ShiftResponse> allShifts = getAllShifts(shiftreq1);
// Iterate through all shifts
    // Iterate through all shifts
    Set<String> addedShiftIds = new HashSet<>();

// First process groupedData without looping allShifts
    for (List<Oee> group : groupedData.values()) {
        if (group.isEmpty()) continue; // Avoid IndexOutOfBoundsException

        if (group.get(0).getShiftId() != null && !group.get(0).getShiftId().isEmpty()) {
            ShiftResponse shift = getShiftByHandle(group.get(0).getShiftId(), allShifts); // Fetch shift
            if (shift != null) {
                ShiftDetails shiftDetails = calculateShiftDetails(group, scrapqty, shift);
                shiftDetailsList.add(shiftDetails);
                addedShiftIds.add(shift.getHandle()); // Mark this shift as processed
            }
        }
    }

// Now loop through allShifts **only for missing shifts**
    for (ShiftResponse shift : allShifts) {
        if (!addedShiftIds.contains(shift.getHandle())) {  // Process only missing shifts
            Oee oee = Oee.builder()
                    .site(oeeRequest.getSite())
                    .shiftId(shift.getHandle()) // Set shift ID from request
                    .totalDowntime(0)
                    .availability(0)
                    .performance(0)
                    .quality(0)
                    .goodQty(0)
                    .badQty(0)
                    .totalQty(0)
                    .oee(0)
                    .plan(0)
                    .productionTime(0)
                    .actualTime(0)
                    .intervalStartDateTime(intervalStartDateTime)
                    .availabilityId(0L)
                    .performanceId(0L)
                    .qualityId(0L)
                    .active(1) // Assuming active status as default
                    .eventTypeOfPerformance("DEFAULT")
                    .batchNumber("N/A")
                    .build();

            // Create a new group for this OEE
            List<Oee> newGroup = new ArrayList<>();
            newGroup.add(oee);

            // Calculate shift details for this newly created OEE entry
            ShiftDetails shiftDetails = calculateShiftDetails(newGroup, scrapqty, shift);
            shiftDetailsList.add(shiftDetails);
        }
    }

// Remove any null shift entries
    shiftDetailsList.removeIf(shiftDetails -> shiftDetails.getShift() == null);



    return shiftDetailsList;


}
    private ShiftResponse getShiftByHandle(String handle, List<ShiftResponse> allShifts) {
        for (ShiftResponse shift : allShifts) {
            if (shift.getHandle().equals(handle)) {
                return shift;
            }
        }
        return null; // Return null if not found
    }

    @Override
    public List<WorkcenterDetails> getOeeDetailsByWorkCenterId(OeeRequest oeeRequest) {
        List<WorkcenterDetails> workcenterDetailsList = new ArrayList<>();
        List<String> eventType = Arrays.asList("doneSfcBatch", "completeSfcBatch");
        List<ShiftDetails> shiftDetailsList=new ArrayList<>();
        ShiftRequest shiftreq= new ShiftRequest();
        shiftreq.setSite(oeeRequest.getSite());
        LocalDateTime intervalStartDateTime=getEarliestValidShiftStartDateTime(shiftreq);

        LocalDateTime now = LocalDateTime.now();
        // Fetch OEE records based on site and time interval

        List<Oee> oeeDetails = oeeRepository.findByIntervalAndSiteForBatch(
                intervalStartDateTime,
                now,
                oeeRequest.getSite(),
                oeeRequest.getEventSource().equals("MANUAL")
                        ? eventType
                        : List.of("machineDoneSfcBatch", "machineCompleteSfcBatch"),
                oeeRequest.getEventSource().equals("MANUAL")
        );

        // Group records by workcenterId after filtering out unwanted events
        Map<String, List<Oee>> groupedData = oeeDetails.stream()
                .filter(Objects::nonNull)


                .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty())
                // Ensure resourceId is not null/empty
                .filter(oee -> {
                    if (oeeRequest.getEventSource().equalsIgnoreCase("MANUAL")) {
                        return "ScrapSFC".equalsIgnoreCase(oee.getEventTypeOfPerformance()) ||
                                "doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                    } else {
                        return "machineScrapSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) ||
                                "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                    }
                })
                .filter(oee -> oee.getWorkcenterId() != null && !oee.getWorkcenterId().isEmpty())
                .collect(Collectors.groupingBy(Oee::getWorkcenterId));
        List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(oeeRequest.getSite());
        Map<String, List<Oee>> groupedDataWithComplete = oeeDetails.stream()
                .filter(Objects::nonNull)


                .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty())
                // Ensure resourceId is not null/empty
                .filter(oee -> {
                    if (oeeRequest.getEventSource().equalsIgnoreCase("MANUAL")) {
                        return "completeSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) ||
                                "doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                    } else {
                        return "machineCompleteSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) ||
                                "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                    }
                })
                .filter(oee -> oee.getWorkcenterId() != null && !oee.getWorkcenterId().isEmpty())
                .collect(Collectors.groupingBy(Oee::getWorkcenterId));

        // Get the set of resource IDs associated with each tracked WorkCenter, filtered by completeSfcBatch event type
        Set<String> workCenterResourceIdSet = oeeDetails.stream()
                .filter(oee -> {
                    if (oeeRequest.getEventSource().equalsIgnoreCase("MANUAL")) {
                        return "completeSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) ||
                                "doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                    } else {
                        return "machineCompleteSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) ||
                                "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                    }
                }) // Include both event types
                .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty()) // Ensure resourceId is not null or empty
                .map(Oee::getResourceId) // Extract the resourceId
                .collect(Collectors.toSet());

// Filter groupedDataWithComplete to only include matching workcenterId and resourceId
        Map<String, List<Oee>> groupedDataWithCompleteFiltered = groupedDataWithComplete.entrySet().stream()
                .filter(entry ->
                        trackedWorkCenters.stream()
                                .anyMatch(workCenter -> workCenter.getWorkCenter().equals(entry.getKey()) &&
                                        workCenter.getAssociationList().stream()
                                                .anyMatch(association -> workCenterResourceIdSet.contains(association.getAssociateId())))
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        Map<String, Map<String, Double>> scrapqty = calculateTotalQuantitiesForWorkcenter(groupedDataWithComplete);


        // Fetch tracked WorkCenters


        // Convert WorkCenter IDs to a HashSet for O(1) lookup
        Set<String> workCenterIdSet = trackedWorkCenters.stream()
                .map(WorkCenter::getWorkCenter)
                .collect(Collectors.toSet());

        // Extract grouped WorkCenter IDs into a HashSet
        Set<String> groupedWorkCenterIdSet = groupedData.keySet();

        // Process each WorkCenter
        for (String workCenterId : workCenterIdSet) {
            WorkcenterDetails workcenterDetails;

            if (groupedWorkCenterIdSet.contains(workCenterId)) {
                // If WorkCenter exists in grouped data, execute existing logic
                List<Oee> group = groupedData.get(workCenterId);

                if (group != null) {
                    workcenterDetails = calculateWorkcenterDetails(group, scrapqty);
                } else {
                    continue; // Prevents null issues
                }
            } else {
                // If WorkCenter is NOT in groupedData, create a default Oee object
                WorkCenter workCenter = trackedWorkCenters.stream()
                        .filter(wc -> wc.getWorkCenter().equals(workCenterId))
                        .findFirst()
                        .orElse(null);

                if (workCenter == null) continue;

                // Create a default Oee object for the WorkCenter
                Oee defaultOee = Oee.builder()
                        .workcenterId(workCenterId)
                        .site(workCenter.getSite())
                        .totalDowntime(0)
                        .availability(0)
                        .performance(0)
                        .quality(0)
                        .goodQty(0)
                        .badQty(0)
                        .totalQty(0)
                        .oee(0)
                        .plan(0)
                        .productionTime(0)
                        .actualTime(0)
                        .createdDatetime(now)
                        .updatedDateTime(now)
                        .intervalStartDateTime(intervalStartDateTime)
                        .intervalEndDateTime(now)
                        .availabilityId(0L)
                        .performanceId(0L)
                        .qualityId(0L)
                        .active(1) // Assuming active status as default
                        .eventTypeOfPerformance("DEFAULT")
                        .batchNumber("N/A")
                        .build();

                // Pass the default Oee object to calculateWorkcenterDetails
                workcenterDetails = calculateWorkcenterDetails(List.of(defaultOee), scrapqty);
            }

            workcenterDetailsList.add(workcenterDetails);
        }

        // Process any grouped WorkCenter IDs that are **not** in workCenterIdSet
        for (String groupedWorkCenterId : groupedWorkCenterIdSet) {
            if (!workCenterIdSet.contains(groupedWorkCenterId)) {
                List<Oee> group = groupedData.get(groupedWorkCenterId);
                if (group != null) {
                    workcenterDetailsList.add(calculateWorkcenterDetails(group, scrapqty));
                }
            }
        }

        return workcenterDetailsList;
    }



    private static final String EVENT_TYPE = "completeSfcBatch";

    // Helper method to fetch OEE data
    private List<Oee> fetchOeeRecords(OeeRequest oeeRequest) {
        return oeeRepository.findBySiteAndIntervalBetween(
                oeeRequest.getSite(),
                LocalDateTime.parse(oeeRequest.getStartTime()),
                LocalDateTime.parse(oeeRequest.getEndTime()),
                EVENT_TYPE
        );
    }

    // Group by Batch and calculate total good, bad, and actual quantity
    public Map<String, Map<String, Double>> calculateTotalQuantitiesByBatch(List<Oee> oeeRecords) {
        return calculateTotalQuantities(oeeRecords, Oee::getBatchNumber);
    }

    public Map<String, Map<String, Double>> calculateTotalQuantitiesByWorkCenter(List<Oee> oeeRecords) {
        return calculateTotalQuantities(oeeRecords, Oee::getWorkcenterId);
    }
    public Map<String, Map<String, Double>> calculateTotalQuantitiesByWorkCenter1(List<AggregatedOee> oeeRecords) {
        return calculateTotalQuantities1(oeeRecords, AggregatedOee::getWorkcenterId);
    }

    public Map<String, Map<String, Double>> calculateTotalQuantitiesByWorkCenter1(Map<String, List<AggregatedOee>> oeeDetails) {
        return calculateTotalQuantities1(oeeDetails, AggregatedOee::getWorkcenterId);
    }

    public Map<String, Map<String, Double>> calculateTotalQuantitiesBySite(List<Oee> oeeRecords) {
        return calculateTotalQuantities(oeeRecords, Oee::getSite);
    }

    public Map<String, Map<String, Double>> calculateTotalQuantitiesByShift(List<Oee> oeeRecords) {
        return calculateTotalQuantities(oeeRecords, Oee::getShiftId);
    }
    public Map<String, Map<String, Double>> calculateTotalQuantitiesByShift1(List<AggregatedOee> oeeRecords) {
        return calculateTotalQuantities1(oeeRecords, AggregatedOee::getShiftId);
    }

    public Map<String, Map<String, Double>> calculateTotalQuantitiesByShift1(Map<String, List<AggregatedOee>> oeeDetails) {
        return calculateTotalQuantities1(oeeDetails, AggregatedOee::getShiftId);
    }

    public Map<String, Map<String, Double>> calculateTotalQuantitiesByResource(List<Oee> oeeRecords) {
        return calculateTotalQuantities(oeeRecords, Oee::getResourceId);
    }

    // Overloaded methods to support Map<String, List<Oee>>
    public Map<String, Map<String, Double>> calculateTotalQuantitiesByBatch(Map<String, List<Oee>> oeeDetails) {
        return calculateTotalQuantities(oeeDetails, Oee::getBatchNumber);
    }

    public Map<String, Map<String, Double>> calculateTotalQuantitiesByWorkCenter(Map<String, List<Oee>> oeeDetails) {
        return calculateTotalQuantities(oeeDetails, Oee::getWorkcenterId);
    }

    public Map<String, Map<String, Double>> calculateTotalQuantitiesBySite(Map<String, List<Oee>> oeeDetails) {
        return calculateTotalQuantities(oeeDetails, Oee::getSite);
    }
    public Map<String, Map<String, Double>> calculateTotalQuantitiesBySite1(Map<String, List<AggregatedOee>> oeeDetails) {
        return calculateTotalQuantities1(oeeDetails, AggregatedOee::getSite);
    }

    public Map<String, Map<String, Double>> calculateTotalQuantitiesByShift(Map<String, List<Oee>> oeeDetails) {
        return calculateTotalQuantities(oeeDetails, Oee::getShiftId);
    }

    public Map<String, Map<String, Double>> calculateTotalQuantitiesByResource(Map<String, List<Oee>> oeeDetails) {
        return calculateTotalQuantities(oeeDetails, Oee::getResourceId);
    }

    // **Reusable helper method for List<Oee> input**
    private Map<String, Map<String, Double>> calculateTotalQuantities(List<Oee> oeeRecords, Function<Oee, String> groupByFunction) {
        Map<String, Map<String, Double>> result = new HashMap<>();

        for (Oee oee : oeeRecords) {
            String key = groupByFunction.apply(oee);

            result.putIfAbsent(key, new HashMap<>(Map.of(
                    "goodQty", 0.0,
                    "badQty", 0.0,
                    "totalQty", 0.0
            )));

            Map<String, Double> quantities = result.get(key);
            quantities.put("goodQty", quantities.get("goodQty") + oee.getGoodQty());
            quantities.put("badQty", quantities.get("badQty") + oee.getBadQty());
            quantities.put("totalQty", quantities.get("totalQty") + oee.getPlan());
        }

        return result;
    }

    private Map<String, Map<String, Double>> calculateTotalQuantities1(List<AggregatedOee> oeeRecords, Function<AggregatedOee, String> groupByFunction) {
        Map<String, Map<String, Double>> result = new HashMap<>();

        for (AggregatedOee oee : oeeRecords) {
            String key = groupByFunction.apply(oee);

            result.putIfAbsent(key, new HashMap<>(Map.of(
                    "goodQty", 0.0,
                    "badQty", 0.0,
                    "totalQty", 0.0
            )));

            Map<String, Double> quantities = result.get(key);
            quantities.put("goodQty", quantities.get("goodQty") + oee.getTotalGoodQuantity());
            quantities.put("badQty", quantities.get("badQty") + oee.getTotalBadQuantity());
            quantities.put("totalQty", quantities.get("totalQty") + oee.getPlan());
        }

        return result;
    }

    private Map<String, Map<String, Double>> calculateTotalQuantities1(
            Map<String, List<AggregatedOee>> oeeDetails, Function<AggregatedOee, String> groupByFunction) {

        Map<String, Map<String, Double>> result = new HashMap<>();

        // Filter out records where resourceId is null or empty
        Map<String, List<AggregatedOee>> filteredOeeDetails = oeeDetails.entrySet().stream()
                .map(entry -> {
                    List<AggregatedOee> filteredList = entry.getValue().stream()
                            .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty())

                            .filter(oee -> oee.getWorkcenterId() != null && !oee.getWorkcenterId().isEmpty())
                            .filter(oee -> oee.getCategory().equalsIgnoreCase("WORKCENTER"))// Keep only null or empty resource IDs
                            .collect(Collectors.toList());

                    return filteredList.isEmpty() ? null : Map.entry(entry.getKey(), filteredList); // Remove empty lists
                })
                .filter(Objects::nonNull) // Remove null entries
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        // Process filtered data
        for (List<AggregatedOee> oeeRecords : filteredOeeDetails.values()) {
            result.putAll(calculateTotalQuantities1(oeeRecords, groupByFunction)); // Reuse method for List<Oee>
        }

        return result;
    }

    // **Reusable helper method for Map<String, List<Oee>> input**
    private Map<String, Map<String, Double>> calculateTotalQuantities(
            Map<String, List<Oee>> oeeDetails, Function<Oee, String> groupByFunction) {

        Map<String, Map<String, Double>> result = new HashMap<>();

        // Filter out records where resourceId is null or empty
        Map<String, List<Oee>> filteredOeeDetails = oeeDetails.entrySet().stream()
                .map(entry -> {
                    List<Oee> filteredList = entry.getValue().stream()
                            .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty())

                            .filter(oee -> oee.getWorkcenterId() != null && !oee.getWorkcenterId().isEmpty())
                            .filter(oee -> {
                                String eventType = oee.getEventTypeOfPerformance();
                                if (!eventType.toLowerCase().contains("machine")) {
                                    return eventType.equalsIgnoreCase("doneSfcBatch") ||
                                            eventType.equalsIgnoreCase("completeSfcBatch");
                                } else {
                                    return eventType.equalsIgnoreCase("machineDoneSfcBatch") ||
                                            eventType.equalsIgnoreCase("machineCompleteSfcBatch");
                                }
                            })
                            .collect(Collectors.toList());

                    return filteredList.isEmpty() ? null : Map.entry(entry.getKey(), filteredList); // Remove empty lists
                })
                .filter(Objects::nonNull) // Remove null entries
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        // Process filtered data
        for (List<Oee> oeeRecords : filteredOeeDetails.values()) {
            result.putAll(calculateTotalQuantities(oeeRecords, groupByFunction)); // Reuse method for List<Oee>
        }

        return result;
    }
    // Method to calculate total quantities for workcenter with the relevant event types
    private Map<String, Map<String, Double>> calculateTotalQuantitiesForWorkcenter(Map<String, List<Oee>> oeeDetails) {
        Map<String, Map<String, Double>> result = new HashMap<>();

        // Process each workcenter and calculate quantities
        for (Map.Entry<String, List<Oee>> entry : oeeDetails.entrySet()) {
            String workcenterId = entry.getKey();
            List<Oee> oeeRecords = entry.getValue();

            // Initialize the map for the workcenter if it doesn't exist
            result.putIfAbsent(workcenterId, new HashMap<>(Map.of(
                    "goodQty", 0.0,
                    "totalQty", 0.0,
                    "badQty", 0.0
            )));

            // Process each OEE record
            for (Oee oee : oeeRecords) {
                Map<String, Double> quantities = result.get(workcenterId);

                // Accumulate totalQty only from "doneSfcBatch" (No summing of good and bad)
                if ("doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) || "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())) {
                    quantities.put("totalQty", quantities.get("totalQty") + oee.getTotalQty());
                }

                // Accumulate scrapQty only for "completeSfcBatch"
                if ("completeSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) || "machineCompleteSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())) {
                    quantities.put("badQty", quantities.get("badQty") + oee.getBadQty());
                }

                // Accumulate goodQty only for "doneSfcBatch"
                if ("doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) || "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())) {
                    quantities.put("goodQty", quantities.get("goodQty") + oee.getGoodQty());
                }
            }
        }

        return result;
    }

    private Map<String, Map<String, Double>> calculateTotalQuantitiesForShiftId(Map<String, List<Oee>> oeeDetails) {
        Map<String, Map<String, Double>> result = new HashMap<>();

        // Process each workcenter and calculate quantities
        for (Map.Entry<String, List<Oee>> entry : oeeDetails.entrySet()) {
            String shiftId = entry.getKey();
            List<Oee> oeeRecords = entry.getValue();

            // Initialize the map for the workcenter if it doesn't exist
            result.putIfAbsent(shiftId, new HashMap<>(Map.of(
                    "goodQty", 0.0,
                    "totalQty", 0.0,
                    "badQty", 0.0
            )));

            // Process each OEE record
            for (Oee oee : oeeRecords) {
                Map<String, Double> quantities = result.get(shiftId);

                // Accumulate totalQty only from "doneSfcBatch" (No summing of good and bad)
                if ("doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) || "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())) {
                    quantities.put("totalQty", quantities.get("totalQty") + oee.getTotalQty());
                }

                // Accumulate scrapQty only for "completeSfcBatch"
                if ("completeSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) || "machineCompleteSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())) {
                    quantities.put("badQty", quantities.get("badQty") + oee.getBadQty());
                }

                // Accumulate goodQty only for "doneSfcBatch"
                if ("doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) || "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())) {
                    quantities.put("goodQty", quantities.get("goodQty") + oee.getGoodQty());
                }
            }
        }

        return result;
    }

    private Map<String, Map<String, Double>> calculateTotalQuantitiesForSite(Map<String, List<Oee>> oeeDetails) {
        Map<String, Map<String, Double>> result = new HashMap<>();

        // Process each workcenter and calculate quantities
        for (Map.Entry<String, List<Oee>> entry : oeeDetails.entrySet()) {
            String site = entry.getKey();
            List<Oee> oeeRecords = entry.getValue();

            // Initialize the map for the workcenter if it doesn't exist
            result.putIfAbsent(site, new HashMap<>(Map.of(
                    "goodQty", 0.0,
                    "totalQty", 0.0,
                    "badQty", 0.0
            )));

            // Process each OEE record
            for (Oee oee : oeeRecords) {
                Map<String, Double> quantities = result.get(site);

                // Accumulate totalQty only from "doneSfcBatch" (No summing of good and bad)
                if ("doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) || "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())) {
                    quantities.put("totalQty", quantities.get("totalQty") + oee.getTotalQty());
                }

                // Accumulate scrapQty only for "completeSfcBatch"
                if ("completeSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) || "machineCompleteSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())) {
                    quantities.put("badQty", quantities.get("badQty") + oee.getBadQty());
                }

                // Accumulate goodQty only for "doneSfcBatch"
                if ("doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) || "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance())) {
                    quantities.put("goodQty", quantities.get("goodQty") + oee.getGoodQty());
                }
            }
        }

        return result;
    }



    // Main method to calculate total quantities for all workcenters




    @Override
    public List<ResourceDetails> getOeeDetailsByResourceId(OeeRequest oeeRequest) {
        try {
            List<ResourceDetails> resourceDetailsList = new ArrayList<>();
            //String eventType = "completeSfcBatch";
            ShiftRequest shiftreq= new ShiftRequest();
            shiftreq.setSite(oeeRequest.getSite());
            LocalDateTime intervalStartDateTime=getEarliestValidShiftStartDateTime(shiftreq);

            LocalDateTime now = LocalDateTime.now();

            // Fetch OEE records based on site and time interval
            List<Oee> oeeDetails = oeeRepository.findBySiteAndIntervalBetween(
                    oeeRequest.getSite(),
                    intervalStartDateTime,
                    now,
                    oeeRequest.getEventSource().equals("MANUAL")? List.of("completeSfcBatch", "signOffCmp") : List.of("machineCompleteSfcBatch"),
                    oeeRequest.getEventSource().equals("MANUAL")
            );

            // Group records by resourceId after filtering out unwanted events
            Map<String, List<Oee>> groupedData = oeeDetails.stream()
                    .filter(Objects::nonNull)
                    .filter(oee -> {
                        if (oeeRequest.getEventSource().equals("MANUAL")) {
                            return oee.getEventTypeOfPerformance().equalsIgnoreCase("completeSfcBatch") ||
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("signOffCmp") ||
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("ScrapSFC") ||
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("doneSfcBatch");
                        } else {
                            return oee.getEventTypeOfPerformance().equalsIgnoreCase("machineCompleteSfcBatch") ||
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("machineDoneSfcBatch") ||
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("machineScrapSfcBatch");
                        }
                    })
                    .filter(oee -> oee.getResourceId() != null)
                    .collect(Collectors.groupingBy(Oee::getResourceId));

            List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(oeeRequest.getSite());
            /*groupedData = groupedData.entrySet().stream()
                    .filter(entry ->
                            trackedWorkCenters.stream()
                                    .anyMatch(workCenter -> workCenter.getWorkCenter().equals(entry.getKey()))
                    )
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));*/

            Map<String, Map<String, Double>> scrapqty = calculateTotalQuantitiesByWorkCenter(groupedData);

            // Convert WorkCenter resourceIds list to a HashSet for O(1) lookup
            Set<String> workCenterResourceIdSet = trackedWorkCenters.stream()
                    .flatMap(workCenter -> workCenter.getAssociationList().stream())
                    .filter(association -> "Resource".equalsIgnoreCase(association.getType()))
                    .map(Association::getAssociateId)
                    .collect(Collectors.toSet());  // HashSet for fast lookup

            // Extract grouped resource IDs into a HashSet
            Set<String> groupedResourceIdSet = groupedData.keySet();

            // Process each WorkCenter resource
            for (String workCenterResourceId : workCenterResourceIdSet) {
                ResourceDetails resourceDetails;

                if (groupedResourceIdSet.contains(workCenterResourceId)) {
                    // If resource exists in grouped data, execute existing logic
                    List<Oee> group = groupedData.get(workCenterResourceId);

                    if (group != null) {
                        resourceDetails = calculateResourceDetails(group);
                    } else {
                        continue; // Prevents null issues
                    }
                } else {
                    // If resourceId is NOT in groupedData, create a default Oee object
                    WorkCenter workCenter = trackedWorkCenters.stream()
                            .filter(wc -> wc.getAssociationList().stream()
                                    .anyMatch(assoc -> "Resource".equalsIgnoreCase(assoc.getType()) &&
                                            assoc.getAssociateId().equals(workCenterResourceId)))
                            .findFirst()
                            .orElse(null);

                    if (workCenter == null) continue; // Prevents null issues


                    // Create a default Oee object
                    Oee defaultOee = Oee.builder()
                            .resourceId(workCenterResourceId)
                            .site(workCenter.getSite())
                            .totalDowntime(0)
                            .availability(0)
                            .performance(0)
                            .quality(0)
                            .goodQty(0)
                            .badQty(0)
                            .totalQty(0)
                            .oee(0)
                            .plan(0)
                            .productionTime(0)
                            .actualTime(0)
                            .createdDatetime(now)
                            .updatedDateTime(now)
                            .intervalStartDateTime(intervalStartDateTime)
                            .intervalEndDateTime(now)
                            .availabilityId(0L)
                            .performanceId(0L)
                            .qualityId(0L)
                            .active(1)
                            .build();

                    // Pass the default Oee object to calculateResourceDetails
                    resourceDetails = calculateResourceDetails(List.of(defaultOee));
                }

                resourceDetailsList.add(resourceDetails);
            }

            // Process any grouped resource IDs that are **not** in workCenterResourceIdSet
            for (String groupedResourceId : groupedResourceIdSet) {
                if (!workCenterResourceIdSet.contains(groupedResourceId)) {
                    List<Oee> group = groupedData.get(groupedResourceId);
                    if (group != null) {
                        resourceDetailsList.add(calculateResourceDetails(group));
                    }
                }
            }

            return resourceDetailsList;
        } catch (Exception e) {
            throw e;
        }
    }


    @Override
    public List<ResourceDetails> getOeeDetailsByMachineDataResourceId(OeeRequest oeeRequest) {
        try {
            List<ResourceDetails> resourceDetailsList = new ArrayList<>();
            //String eventType = "completeSfcBatch";
            ShiftRequest shiftreq= new ShiftRequest();
            shiftreq.setSite(oeeRequest.getSite());
            LocalDateTime intervalStartDateTime=getEarliestValidShiftStartDateTime(shiftreq);

            LocalDateTime now = LocalDateTime.now();

            // Fetch OEE records based on site and time interval
            List<Oee> oeeDetails = oeeRepository.findBySiteAndIntervalBetween(
                    oeeRequest.getSite(),
                    intervalStartDateTime,
                    now,
                    List.of("machineCompleteSfcBatch", "machineScrapSfcBatch"),
                    true
            );

            // Group records by resourceId after filtering out unwanted events
            Map<String, List<Oee>> groupedData = oeeDetails.stream()
                    .filter(Objects::nonNull)
                    .filter(oee ->
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("machineCompleteSfcBatch") ||
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("machineScrapSfcBatch")
                    )
                    .filter(oee -> oee.getResourceId() != null)
                    .collect(Collectors.groupingBy(Oee::getResourceId));

            List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(oeeRequest.getSite());
            /*groupedData = groupedData.entrySet().stream()
                    .filter(entry ->
                            trackedWorkCenters.stream()
                                    .anyMatch(workCenter -> workCenter.getWorkCenter().equals(entry.getKey()))
                    )
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));*/

            Map<String, Map<String, Double>> scrapqty = calculateTotalQuantitiesByWorkCenter(groupedData);

            // Convert WorkCenter resourceIds list to a HashSet for O(1) lookup
            Set<String> workCenterResourceIdSet = trackedWorkCenters.stream()
                    .flatMap(workCenter -> workCenter.getAssociationList().stream())
                    .filter(association -> "Resource".equalsIgnoreCase(association.getType()))
                    .map(Association::getAssociateId)
                    .collect(Collectors.toSet());  // HashSet for fast lookup

            // Extract grouped resource IDs into a HashSet
            Set<String> groupedResourceIdSet = groupedData.keySet();

            // Process each WorkCenter resource
            for (String workCenterResourceId : workCenterResourceIdSet) {
                ResourceDetails resourceDetails;

                if (groupedResourceIdSet.contains(workCenterResourceId)) {
                    // If resource exists in grouped data, execute existing logic
                    List<Oee> group = groupedData.get(workCenterResourceId);

                    if (group != null) {
                        resourceDetails = calculateResourceDetails(group);
                    } else {
                        continue; // Prevents null issues
                    }
                } else {
                    // If resourceId is NOT in groupedData, create a default Oee object
                    WorkCenter workCenter = trackedWorkCenters.stream()
                            .filter(wc -> wc.getAssociationList().stream()
                                    .anyMatch(assoc -> "Resource".equalsIgnoreCase(assoc.getType()) &&
                                            assoc.getAssociateId().equals(workCenterResourceId)))
                            .findFirst()
                            .orElse(null);

                    if (workCenter == null) continue; // Prevents null issues


                    // Create a default Oee object
                    Oee defaultOee = Oee.builder()
                            .resourceId(workCenterResourceId)
                            .site(workCenter.getSite())
                            .totalDowntime(0)
                            .availability(0)
                            .performance(0)
                            .quality(0)
                            .goodQty(0)
                            .badQty(0)
                            .totalQty(0)
                            .oee(0)
                            .plan(0)
                            .productionTime(0)
                            .actualTime(0)
                            .createdDatetime(now)
                            .updatedDateTime(now)
                            .intervalStartDateTime(intervalStartDateTime)
                            .intervalEndDateTime(now)
                            .availabilityId(0L)
                            .performanceId(0L)
                            .qualityId(0L)
                            .active(1)
                            .build();

                    // Pass the default Oee object to calculateResourceDetails
                    resourceDetails = calculateResourceDetails(List.of(defaultOee));
                }

                resourceDetailsList.add(resourceDetails);
            }

            // Process any grouped resource IDs that are **not** in workCenterResourceIdSet
            for (String groupedResourceId : groupedResourceIdSet) {
                if (!workCenterResourceIdSet.contains(groupedResourceId)) {
                    List<Oee> group = groupedData.get(groupedResourceId);
                    if (group != null) {
                        resourceDetailsList.add(calculateResourceDetails(group));
                    }
                }
            }

            return resourceDetailsList;
        } catch (Exception e) {
            throw e;
        }
    }

//    @Override
//    public List<OperationDetails> getOeeDetailsByOperation(OeeRequest oeeRequest) {
//        try {
//            List<OperationDetails> operationDetailsList = new ArrayList<>();
//            String eventType = "completeSfcBatch";
//            List<ShiftDetails> shiftDetailsList=new ArrayList<>();
//            ShiftRequest shiftreq= new ShiftRequest();
//            shiftreq.setSite(oeeRequest.getSite());
//            LocalDateTime intervalStartDateTime=getEarliestValidShiftStartDateTime(shiftreq);
//
//            LocalDateTime now = LocalDateTime.now();
//
//            // Fetch OEE records based on site and time interval
//            List<Oee> oeeDetails = oeeRepository.findBySiteAndIntervalBetween(
//                    oeeRequest.getSite(),
//                    intervalStartDateTime,
//                    now,
//                    eventType
//            );
//
//            // Group records by resourceId after filtering out unwanted events
//            Map<String, List<Oee>> groupedData = oeeDetails.stream()
//                    .filter(Objects::nonNull)
//                    .filter(oee ->
//                            oee.getEventTypeOfPerformance().equalsIgnoreCase("completeSfcBatch") ||
//                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("ScrapSFC") ||
//                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("doneSfcBatch")
//                    )
//                    .filter(oee -> oee.getOperation() != null)
//                    .collect(Collectors.groupingBy(Oee::getOperation));
//
//            List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(oeeRequest.getSite());
//
//            // Convert WorkCenter resourceIds list to a HashSet for O(1) lookup
//            Set<String> workCenterOperationSet = trackedWorkCenters.stream()
//                    .flatMap(workCenter -> workCenter.getAssociationList().stream())
//                    .filter(association -> "Operation".equalsIgnoreCase(association.getType()))
//                    .map(Association::getAssociateId)
//                    .collect(Collectors.toSet());  // HashSet for fast lookup
//
//            // Extract grouped resource IDs into a HashSet
//            Set<String> groupedOperationSet = groupedData.keySet();
//
//            // Process each WorkCenter resource
//            for (String workCenterOperation : workCenterOperationSet) {
//                OperationDetails operationDetails;
//
//                    // If resource exists in grouped data, execute existing logic
//                    List<Oee> group = groupedData.get(workCenterOperation);
//
//                    if (group != null) {
//                        operationDetails = calculateOperationDetails(group);
//                    } else {
//                        continue; // Prevents null issues
//                    }
//
//
//                operationDetailsList.add(operationDetails);
//            }
//
//            // Process any grouped resource IDs that are **not** in workCenterResourceIdSet
//            for (String groupedOperation : groupedOperationSet) {
//                if (!workCenterOperationSet.contains(groupedOperation)){
//                    List<Oee> group = groupedData.get(groupedOperation);
//                    if (group != null) {
//                        operationDetailsList.add(calculateOperationDetails(group));
//                    }
//                }
//            }
//
//            return operationDetailsList;
//        } catch (Exception e) {
//            throw e;
//        }
//    }
    @Override
    public List<OperationDetails> getOeeDetailsByOperation(OeeRequest oeeRequest) {
        try {
            List<OperationDetails> opertionDetailsList = new ArrayList<>();
            ShiftRequest shiftreq= new ShiftRequest();
            shiftreq.setSite(oeeRequest.getSite());
            LocalDateTime intervalStartDateTime=getEarliestValidShiftStartDateTime(shiftreq);

            LocalDateTime now = LocalDateTime.now();

            List<Oee> oeeDetails = oeeRepository.findBySiteAndIntervalBetween(
                    oeeRequest.getSite(),
                    intervalStartDateTime,
                    now,
                    oeeRequest.getEventSource().equals("MANUAL")? List.of("completeSfcBatch", "signOffCmp") : List.of("machineCompleteSfcBatch"),// Include both events
                    oeeRequest.getEventSource().equals("MANUAL")
            );

            Map<String, List<Oee>> groupedData = oeeDetails.stream()
                    .filter(Objects::nonNull)
                    .filter(oee -> {
                        if (oeeRequest.getEventSource().equals("MANUAL")) {
                            return oee.getEventTypeOfPerformance().equalsIgnoreCase("completeSfcBatch") ||
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("signOffCmp") ||
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("ScrapSFC") ||
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("doneSfcBatch");
                        } else {
                            return oee.getEventTypeOfPerformance().equalsIgnoreCase("machineCompleteSfcBatch") ||
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("machineDoneSfcBatch") ||
                                    oee.getEventTypeOfPerformance().equalsIgnoreCase("machineScrapSfcBatch");
                        }
                    })
                    .filter(oee -> oee.getOperation() != null && !oee.getOperation().trim().isEmpty())
                    .collect(Collectors.groupingBy(Oee::getOperation));

            List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(oeeRequest.getSite());

            Map<String, Map<String, Double>> scrapqty = calculateTotalQuantitiesByWorkCenter(groupedData);

            Set<String> workCenterResourceSet = trackedWorkCenters.stream()
                    .flatMap(workCenter -> workCenter.getAssociationList().stream())
                    .filter(association -> "Resource".equalsIgnoreCase(association.getType()))
                    .map(Association::getAssociateId)
                    .collect(Collectors.toSet());
            Set<String> workCenterOperationSet = new HashSet<>();
            for (String workCenterResource : workCenterResourceSet) {
                Map<String, String> requestBody = Map.of(
                        "site", oeeRequest.getSite(),
                        "resource", workCenterResource
                );

                Map<String, Object> responseMap = webClientBuilder.build()
                        .post()
                        .uri(retrieveByResource)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();

                if (responseMap != null && responseMap.containsKey("defaultOperation")) {
                    String defaultOperation = (String) responseMap.get("defaultOperation");
                    if (defaultOperation != null && !defaultOperation.isEmpty()) {
                        workCenterOperationSet.add(defaultOperation);
                    }
                }
            }

            Set<String> groupedOperationSet = groupedData.keySet();
                for (String workCenterOperation : workCenterOperationSet) {

                    OperationDetails operationDetails;

                    if (groupedOperationSet.contains(workCenterOperation)) {
                        List<Oee> group = groupedData.get(workCenterOperation);

                        if (group != null) {
                            operationDetails = calculateOperationDetails(group);
                        } else {
                            continue;
                        }
                    } else{
                    Oee defaultOee = Oee.builder()
                            .operation(workCenterOperation)
                            .site(oeeRequest.getSite())
                            .totalDowntime(0)
                            .availability(0)
                            .performance(0)
                            .quality(0)
                            .goodQty(0)
                            .badQty(0)
                            .totalQty(0)
                            .oee(0)
                            .plan(0)
                            .productionTime(0)
                            .actualTime(0)
                            .createdDatetime(now)
                            .updatedDateTime(now)
                            .intervalStartDateTime(intervalStartDateTime)
                            .intervalEndDateTime(now)
                            .availabilityId(0L)
                            .performanceId(0L)
                            .qualityId(0L)
                            .active(1)
                            .build();

                    operationDetails = calculateOperationDetails(List.of(defaultOee));
                  }

                    opertionDetailsList.add(operationDetails);
                }

            for (String groupedOperation : groupedOperationSet) {
                if (!workCenterOperationSet.contains(groupedOperation)) {
                    List<Oee> group = groupedData.get(groupedOperation);
                    if (group != null) {
                        opertionDetailsList.add(calculateOperationDetails(group));
                    }
                }
            }

            return opertionDetailsList;
        } catch (Exception e) {
            throw e;
        }
    }

    private List<WorkCenter> fetchTrackedWorkCenters(String site) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("site", site);

        return webClientBuilder.build()
                .post()
                .uri(retrieveTrackOeeWorkcenters)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(WorkCenter.class)
                .collectList()
                .block();
    }


    // Helper methods to get current date and time as strings
    private String getCurrentDate() {
        return LocalDate.now().toString();
    }

    private String getCurrentTime() {
        return LocalTime.now().toString();
    }

    @Override
    public List<BatchDetails> getOeeDetailsByBatchNo(OeeRequest oeeRequest) {


        List<String> eventType = Arrays.asList("doneSfcBatch", "completeSfcBatch");
        List<ShiftDetails> shiftDetailsList=new ArrayList<>();
        ShiftRequest shiftreq= new ShiftRequest();
        shiftreq.setSite(oeeRequest.getSite());
        LocalDateTime intervalStartDateTime=getEarliestValidShiftStartDateTime(shiftreq);

        LocalDateTime now = LocalDateTime.now();

        List<Oee> oeeDetails = oeeRepository.findByIntervalAndSiteForBatch(
                intervalStartDateTime,
                now,
                oeeRequest.getSite(),
                oeeRequest.getEventSource().equals("MANUAL")
                        ? eventType
                        : List.of("machineDoneSfcBatch", "machineCompleteSfcBatch"),
                oeeRequest.getEventSource().equals("MANUAL")
        );


        if (oeeDetails.isEmpty()) {
            return Collections.emptyList();
        }

        // Sort the list once to determine the overall time range
        oeeDetails.sort(Comparator.comparing(Oee::getIntervalStartDateTime));


        // Calculate duration in seconds
        long totalTimeSeconds = Duration.between(intervalStartDateTime, now).getSeconds();

        // Filter and group by batchNumber, operation, and resourceId
        Map<String, List<Oee>> groupedData = oeeDetails.stream()
                .filter(Objects::nonNull)
                .filter(oee -> oee.getBatchNumber() != null && !oee.getBatchNumber().isEmpty()) // Ensure batchNumber is not null/empty
                .filter(oee -> {
                    if (oeeRequest.getEventSource().equalsIgnoreCase("MANUAL")) {
                        return "doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) ||
                                "ScrapSFC".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                    } else {
                        return "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) ||
                                "machineScrapSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                    }
                })
                .filter(oee -> oee.getCategory().equalsIgnoreCase("RESOURCE"))
                .collect(Collectors.groupingBy(Oee::getBatchNumber));

        Map<String, List<Oee>> groupedBySite = oeeDetails.stream()
                .filter(Objects::nonNull)
                .filter(oee -> oee.getBatchNumber() != null && !oee.getBatchNumber().isEmpty()) // Ensure batchNumber is not null/empty
                .filter(oee -> {
                    if (oeeRequest.getEventSource().equalsIgnoreCase("MANUAL")) {
                        return "doneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) ||
                                "completeSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                    } else {
                        return "machineDoneSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance()) ||
                                "machineCompleteSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                    }
                })
                .filter(oee -> oee.getCategory().equalsIgnoreCase("RESOURCE"))
                .collect(Collectors.groupingBy(Oee::getBatchNumber));

        Map<String, Map<String, Double>> scrapqty=calculateTotalQuantitiesForWorkcenter(groupedBySite);

// Convert to BatchDetails
        List<BatchDetails> batchDetailsList = calculateBatchDetails(groupedData, totalTimeSeconds, scrapqty);



        return batchDetailsList;
    }


    @Override
    public List<ResourceDetails> calculateOeeByEvent(OeeRequest oeeRequest) {
        LocalDateTime startTime;
        LocalDateTime endTime;
        List<ResourceDetails> resourceDetailsList=new ArrayList<>();
        switch (oeeRequest.getEventType().toLowerCase()) {
            case "day":
                startTime = LocalDate.now().atStartOfDay();
                endTime = startTime.plusHours(23).plusMinutes(59).plusSeconds(59);
                break;
            case "week":
                LocalDate startOfWeek = LocalDate.now()
                        .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
                startTime = startOfWeek.atStartOfDay();
                endTime = startOfWeek.plusDays(6).atTime(23, 59, 59);
                break;
            case "month":
                LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
                startTime = startOfMonth.atStartOfDay();
                endTime = startOfMonth.plusMonths(1).minusDays(1).atTime(23, 59, 59);
                break;
            default:
                throw new IllegalArgumentException("Invalid event type. Use 'day', 'week', or 'month'.");
        }

        // Fetch records in the calculated range from the database
        List<Oee> oeeDetails = oeeRepository.findByIntervalStartDateTimeAndIntervalEndDateTimeBetweenAndSite(startTime, endTime, oeeRequest.getEventType());
        Map<String, List<Oee>> groupedData = oeeDetails.stream()
                .filter(Objects::nonNull) // Ensure the element itself is not null
                .filter(oee ->
                        oee.getEventTypeOfPerformance().equalsIgnoreCase("completeSfcBatch") ||
                                oee.getEventTypeOfPerformance().equalsIgnoreCase("ScrapSFC") ||
                                oee.getEventTypeOfPerformance().equalsIgnoreCase("doneSfcBatch")
                )
                .filter(oee -> oee.getResourceId() != null) // Exclude null resourceId
                .collect(Collectors.groupingBy(Oee::getResourceId));
        List<Oee> finalOeeList= new ArrayList<>();
        for (List<Oee> group : groupedData.values()) {
            if(group.get(0).getResourceId()!=null&&group.get(0).getResourceId()!="") {
                ResourceDetails resourceDetails = calculateResourceDetails(group);
                finalOeeList=getoeeList(resourceDetails,group,startTime,endTime);
                resourceDetailsList.add(resourceDetails);
            }
        }
        if(oeeRequest.getSave()){
            oeeRepository.saveAll(finalOeeList);
        }
        return resourceDetailsList;
    }

    @Override
    public List<Map<String, Object>> getOeeByShiftByType(OeeFilterRequest oeeRequest) {
        if (oeeRequest.getStartTime() == null || oeeRequest.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);

            oeeRequest.setStartTime(startTime);
            oeeRequest.setEndTime(endTime);
        }

        String eventType = null;
        String eventSource = null;
        Boolean checkBatchCondition = false;
        if(oeeRequest.getResourceId() != null || oeeRequest.getOperation() != null) {
            eventType = "RESOURCE";
            eventSource = oeeRequest.getEventSource() + "_COMPLETE";
        } else {
            eventType = "WORKCENTER";
            eventSource = oeeRequest.getEventSource() + "_DONE";
        }

        if("MANUAL".equalsIgnoreCase(oeeRequest.getEventSource()))
            checkBatchCondition = true;

        List<AggregatedOee> oeeDetails = oeeRequest != null ?
                findAggregatedOeeByFilters(
                        oeeRequest.getSite(),
                        oeeRequest.getStartTime(),
                        oeeRequest.getEndTime(),
                        oeeRequest.getResourceId(),
                        oeeRequest.getShiftId(),
                        oeeRequest.getWorkcenterId(),
                        oeeRequest.getBatchNumber(),
                        oeeRequest.getShoporderId(),
                        oeeRequest.getItem(),
                        oeeRequest.getOperations(),
                        List.of(eventType),
                        eventSource,
                        checkBatchCondition
                ) : new ArrayList<>();
//        aggregatedOeeRepository.findByFilters1(
//                        oeeRequest.getSite(),
//                        oeeRequest.getStartTime(),
//                        oeeRequest.getEndTime(),
//                        oeeRequest.getResourceId(),
//                        oeeRequest.getShiftId(),
//                        oeeRequest.getWorkcenterId(),
//                        oeeRequest.getBatchNumber(),
//                        oeeRequest.getShoporderId(),
//                        oeeRequest.getItem(),
//                        oeeRequest.getOperations(),
//                        List.of(eventType),
//                        eventSource,
//                        checkBatchCondition
//                ) : new ArrayList<>();

        Map<String, List<AggregatedOee>> groupedData = oeeDetails.stream()
                .filter(Objects::nonNull)
                .filter(oee -> oee.getShiftId() != null)
                .collect(Collectors.groupingBy(AggregatedOee::getShiftId));

        List<Map<String, Object>> resourceDetailsList = new ArrayList<>();

        for (Map.Entry<String, List<AggregatedOee>> entry : groupedData.entrySet()) {
            String shiftId = entry.getKey();
            List<AggregatedOee> oeeList = entry.getValue();

            double totalValue = 0.0;
            String keyName = "";

            switch (oeeRequest.getType().toLowerCase()) {
                case "availability":
                    totalValue = oeeList.stream().filter(Objects::nonNull).mapToDouble(AggregatedOee::getAvailability).average().orElse(0.0);
                    keyName = "AVAILABILITY";
                    break;
                case "performance":
                    totalValue = oeeList.stream().filter(Objects::nonNull).mapToDouble(AggregatedOee::getPerformance).average().orElse(0.0);
                    keyName = "PERFORMANCE";
                    break;
                case "quality":
                    totalValue = oeeList.stream().filter(Objects::nonNull).mapToDouble(AggregatedOee::getQuality).average().orElse(0.0);
                    keyName = "QUALITY";
                    break;
                case "oee":
                    totalValue = oeeList.stream().filter(Objects::nonNull).mapToDouble(AggregatedOee::getOee).average().orElse(0.0);
                    keyName = "OEE";
                    break;
                default:
                    return new ArrayList<>();
            }

            totalValue = Math.round(totalValue * 100.0) / 100.0;

            Map<String, Object> detailsMap = new HashMap<>();
            String[] parts = shiftId.split(",");
            String shiftName = parts[parts.length - 1];
            detailsMap.put("SHIFT", shiftName);
            detailsMap.put(keyName, totalValue);

            resourceDetailsList.add(detailsMap);
        }

        return resourceDetailsList;
    }

    private Map<String, Object> createShiftMap(String key, String shift, Double value) {
        if (value == null || value == 0.0) return null;

        double roundedValue = Math.round(value * 100.0) / 100.0;

        Map<String, Object> shiftDetailsMap = new HashMap<>();
        shiftDetailsMap.put("SHIFT", shift);
        shiftDetailsMap.put(key, roundedValue);

        return shiftDetailsMap;
    }

    public List<Oee> getoeeList(ResourceDetails resourceDetails,List<Oee> group,LocalDateTime sLocalDateTime,LocalDateTime eLocalDateTime) {
        List<Oee> oeeList= new ArrayList<>();
        for(Oee oee: group) {
            Oee oeeObj = Oee.builder()
                    .site(oee.getSite())
                    .shiftId(oee.getShiftId())
                    .pcuId(oee.getPcuId())
                    .workcenterId(oee.getWorkcenterId())
                    .resourceId(resourceDetails.getResource())
                    .operation(oee.getOperation())
                    .operationVersion(oee.getOperationVersion())
                    .routingBo(oee.getRoutingBo())
                    .itemBo(oee.getItemBo())
                    .item(resourceDetails.getItem())
                    .itemVersion(oee.getItemVersion())
                    .shoporderId(oee.getShoporderId())
                    .totalDowntime(resourceDetails.getDowntimeDuration())
                    .availability(resourceDetails.getAvailability())
                    .performance(resourceDetails.getPerformance())
                    .quality(resourceDetails.getQuality())
                    .goodQty(resourceDetails.getGoodQualityCount())
                    .badQty(resourceDetails.getBadQualityCount())
                    .totalQty(resourceDetails.getGoodQualityCount() + resourceDetails.getBadQualityCount())
                    .oee(resourceDetails.getOee())
                    .plan((int) resourceDetails.getPlan())
                    .productionTime(resourceDetails.getProductionTime())
                    .actualTime(resourceDetails.getActualTime())
                    .createdDatetime(LocalDateTime.now())  // Set the current time
                    .updatedDateTime(LocalDateTime.now())
                    .intervalStartDateTime(sLocalDateTime)
                    .intervalEndDateTime(eLocalDateTime)
                    .active(1) // Assuming 1 for active
                    .eventTypeOfPerformance(oee.getEventTypeOfPerformance())
                    .batchNumber(resourceDetails.getBatchNo())
                    .build();

            oeeList.add(oeeObj);

        }      // Add to the Oee list
        return  oeeList;
    }


    //done by thiru

    @Override
    public OverallOeeResponse getOverall(OeeFilterRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {

            ShiftRequest shiftreq= new ShiftRequest();
            shiftreq.setSite(request.getSite());
            LocalDateTime startTime=  oeeService.getEarliestValidShiftStartDateTime(shiftreq);

            LocalDateTime endTime = LocalDateTime.now();
//            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(startTime);
            request.setEndTime(endTime);
        }

        String eventType = null;
        String eventSource = null;
        Boolean checkBatchCondition = false;
        if(request.getResourceId() != null || request.getOperation() != null) {
            eventType = "RESOURCE";
            eventSource = request.getEventSource() + "_COMPLETE";
        } else {
            eventType = "WORKCENTER";
            eventSource = request.getEventSource() + "_DONE";
        }

        if("MANUAL".equalsIgnoreCase(request.getEventSource()))
            checkBatchCondition = true;

//        List<AggregatedOee> oeeDetails = aggregatedOeeRepository.findByFilters(
//                request.getSite(),
//                request.getStartTime(),
//                request.getEndTime(),
//                request.getResourceId(),
//                request.getShiftId(),
//                request.getWorkcenterId(),
//                request.getBatchNumber(),
//                request.getShoporderId(),
//                request.getItem(),
//                request.getOperations(),
//                eventType,
//                eventSource,
//                checkBatchCondition
//        );

        List<AggregatedOee> oeeDetails = findAggregatedOeeByFilters(
                request.getSite(),
                request.getStartTime(),
                request.getEndTime(),
                request.getResourceId(),
                request.getShiftId(),
                request.getWorkcenterId(),
                request.getBatchNumber(),
                request.getShoporderId(),
                request.getItem(),
                request.getOperations(),
                List.of(eventType),
                eventSource,
                checkBatchCondition
        );

        Map<String, List<AggregatedOee>> groupedData = oeeDetails.stream()
                .filter(Objects::nonNull)
                .filter(oee -> oee.getSite() != null)
                .collect(Collectors.groupingBy(AggregatedOee::getSite));

        double overallPercentage = groupedData.values().stream()
                .mapToDouble(oeeList -> getMetricValueByType(oeeList, request.getType()))
                .average()
                .orElse(0.0);

//        return new OverallOeeResponse(Math.round(overallPercentage * 100.0) / 100.0);
        return new OverallOeeResponse(overallPercentage);
    }

    public List<AggregatedOee> findAggregatedOeeByFilters(
            String site,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            List<String> resourceId,
            List<String> shiftId,
            List<String> workcenterId,
            List<String> batchNumber,
            List<String> shopOrderId,
            List<String> item,
            List<String> operation,
            List<String> eventType,
            String eventSource,
            Boolean checkBatchCondition
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AggregatedOee> query = cb.createQuery(AggregatedOee.class);
        Root<AggregatedOee> root = query.from(AggregatedOee.class);

        List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();

        // Basic conditions
        predicates.add(cb.equal(root.get("site"), site));
        predicates.add(cb.between(root.get("intervalStartDateTime"), startDateTime, endDateTime));
        predicates.add(cb.between(root.get("intervalEndDateTime"), startDateTime, endDateTime));
        predicates.add(cb.isTrue(root.get("active")));
        predicates.add(cb.equal(root.get("category"), eventType));
        predicates.add(cb.equal(root.get("eventSource"), eventSource));

        // Adding filters based on lists (e.g., resourceId, shiftId, etc.)
        if (eventType != null && !eventType.isEmpty()) {
            predicates.add(root.get("category").in(eventType));
        }

        if (resourceId != null && !resourceId.isEmpty()) {
            predicates.add(root.get("resourceId").in(resourceId));
        }

        if (shiftId != null && !shiftId.isEmpty()) {
            predicates.add(root.get("shiftId").in(shiftId));
        }

        if (workcenterId != null && !workcenterId.isEmpty()) {
            predicates.add(root.get("workcenterId").in(workcenterId));
        }

        if (batchNumber != null && !batchNumber.isEmpty()) {
            predicates.add(root.get("batchNumber").in(batchNumber));
        }

        if (shopOrderId != null && !shopOrderId.isEmpty()) {
            predicates.add(root.get("shopOrderId").in(shopOrderId));
        }

        if (item != null && !item.isEmpty()) {
            predicates.add(root.get("item").in(item));
        }

        if (operation != null && !operation.isEmpty()) {
            predicates.add(root.get("operation").in(operation));
        }

        // Add complex OR conditions from the original query
        javax.persistence.criteria.Predicate orGroup = cb.or(
                cb.and(
                        isNotEmptyList(shiftId, cb),
                        isEmptyList(item, cb), isEmptyList(operation, cb), isEmptyList(batchNumber, cb),
                        isEmptyList(shopOrderId, cb), isEmptyList(workcenterId, cb),
                        cb.or(cb.isNull(root.get("item")), cb.equal(root.get("item"), "")),
                        cb.or(cb.isNull(root.get("operation")), cb.equal(root.get("operation"), "")),
                        cb.or(cb.isNull(root.get("batchNumber")), cb.equal(root.get("batchNumber"), "")),
                        cb.or(cb.isNull(root.get("shopOrderId")), cb.equal(root.get("shopOrderId"), ""))
                ),
                cb.and(
                        isNotEmptyList(item, cb), isEmptyList(operation, cb), isEmptyList(batchNumber, cb),
                        isEmptyList(shopOrderId, cb), isEmptyList(workcenterId, cb), isEmptyList(shiftId, cb),
                        cb.or(cb.isNull(root.get("operation")), cb.equal(root.get("operation"), "")),
                        cb.or(cb.isNull(root.get("batchNumber")), cb.equal(root.get("batchNumber"), "")),
                        cb.or(cb.isNull(root.get("shopOrderId")), cb.equal(root.get("shopOrderId"), ""))
                ),
                cb.and(
                        isNotEmptyList(item, cb), isNotEmptyList(operation, cb),
                        isNotEmptyList(batchNumber, cb), isNotEmptyList(shopOrderId, cb),
                        cb.isNotNull(root.get("item")), cb.notEqual(root.get("item"), ""),
                        cb.isNotNull(root.get("batchNumber")), cb.notEqual(root.get("batchNumber"), ""),
                        cb.isNotNull(root.get("shopOrderId")), cb.notEqual(root.get("shopOrderId"), "")
                ),
                cb.and(
                        isNotEmptyList(shopOrderId, cb), isEmptyList(item, cb), isEmptyList(operation, cb), isEmptyList(batchNumber, cb),
                        cb.or(cb.isNull(root.get("item")), cb.equal(root.get("item"), "")),
                        cb.or(cb.isNull(root.get("operation")), cb.equal(root.get("operation"), "")),
                        cb.or(cb.isNull(root.get("batchNumber")), cb.equal(root.get("batchNumber"), ""))
                ),
                cb.and(
                        cb.or(cb.isNotNull(root.get("site")), cb.isNotNull(root.get("workcenterId")), cb.isNotNull(root.get("batchNumber"))),
                        isEmptyList(item, cb), isEmptyList(operation, cb), isEmptyList(shopOrderId, cb), isEmptyList(shiftId, cb),
                        cb.or(
                                cb.isFalse(cb.literal(checkBatchCondition)),
                                cb.and(
                                        cb.isNotNull(root.get("batchNumber")), cb.notEqual(root.get("batchNumber"), ""),
                                        cb.isNotNull(root.get("item")), cb.notEqual(root.get("item"), ""),
                                        cb.isNotNull(root.get("operation")), cb.notEqual(root.get("operation"), ""),
                                        cb.isNotNull(root.get("shopOrderId")), cb.notEqual(root.get("shopOrderId"), "")
                                )
                        )
                )
        );

        predicates.add(orGroup);

        query.where(cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }

    // Helper methods to check list sizes
    private javax.persistence.criteria.Predicate isEmptyList(List<?> list, CriteriaBuilder cb) {
        return list == null || list.isEmpty() ? cb.isTrue(cb.literal(true)) : cb.isFalse(cb.literal(true));
    }

    private javax.persistence.criteria.Predicate isNotEmptyList(List<?> list, CriteriaBuilder cb) {
        return list != null && !list.isEmpty() ? cb.isTrue(cb.literal(true)) : cb.isFalse(cb.literal(true));
    }



    /*@Override
    public OverallOeeResponse getOverall(OeeFilterRequest request) {
        String eventTypeOfPerformance = "doneSfcBatch";
        if(request.getResourceId() != null || request.getOperation() != null) eventTypeOfPerformance = "completeSfcBatch";
        StringBuilder sql = new StringBuilder(
                "SELECT SUM(o.good_qty) AS totalGoodQty, " +
                        "SUM(o.bad_qty) AS totalBadQty, " +
                        "SUM(o.plan) AS totalPlannedQty, " +
                        "SUM(o.total_downtime) AS totalDowntime, " +
                        "MIN(o.interval_start_date_time) AS intervalStartTime, " +
                        "MAX(o.interval_end_date_time) AS intervalEndTime " +
                        "FROM R_OEE o " +
                        "WHERE o.site = :site " +
                        "AND o.event_type_of_performance = :eventTypeOfPerformance " +
                        "AND o.batch_number IS NOT NULL " +
                        "AND o.batch_number <> '' " +
                        "AND o.plan > 0 "
        );

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());
        queryParameters.put("eventTypeOfPerformance", eventTypeOfPerformance);

        applyCommonFilters(sql, queryParameters, request);

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();
            if (results.isEmpty()) {
                return new OverallOeeResponse(0.0);
            }

            Object[] result = results.get(0);

            // Extract raw data
            double totalGoodQty = (result[0] != null) ? ((Number) result[0]).doubleValue() : 0.0;
            double totalBadQty = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
            double totalPlannedQty = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;
            double totalDowntime = (result[3] != null) ? ((Number) result[3]).doubleValue() : 0.0;

            LocalDateTime intervalStartTime = (result[4] != null) ? ((java.sql.Timestamp) result[4]).toLocalDateTime() : null;
            LocalDateTime intervalEndTime = (result[5] != null) ? ((java.sql.Timestamp) result[5]).toLocalDateTime() : null;

            // Perform calculations
            double percentage = calculateOeeMetric(request.getType(), totalGoodQty, totalBadQty, totalPlannedQty, totalDowntime, intervalStartTime, intervalEndTime);

            return new OverallOeeResponse(Math.round(percentage * 100.0) / 100.0);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private double calculateOeeMetric(String type, double totalGoodQty, double totalBadQty, double totalPlannedQty,
                                      double totalDowntime, LocalDateTime intervalStartTime, LocalDateTime intervalEndTime) {
        long totalTimeSeconds = (intervalStartTime != null && intervalEndTime != null) ?
                Duration.between(intervalStartTime, intervalEndTime).getSeconds() : 0L;

        long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
        long actualTimeSeconds = totalTimeSeconds;

        // Calculate OEE metrics
        double availability = (productionTimeSeconds > 0) ? ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;
        double performance = (totalPlannedQty > 0) ? ((totalGoodQty + totalBadQty) / totalPlannedQty) * 100 : 0.0;
        double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;
        double oee = (availability * performance * quality) / 10000;

        double percentage;
        switch (type.toLowerCase()) {
            case "oee":
                percentage = oee;
                break;
            case "performance":
                percentage = performance;
                break;
            case "availability":
                percentage = availability;
                break;
            case "quality":
                percentage = quality;
                break;
            case "downtime":
                percentage = totalDowntime;
                break;
            default:
                percentage = 0.0;
        }

        if (Double.isInfinite(percentage) || Double.isNaN(percentage) || percentage > 100) {
            percentage = 100.0;
        }

        return percentage;
    }*/

    @Override
    public OeeByTimeResponse getByTime(OeeFilterRequest request) throws Exception {

        String eventTypeOfPerformance = null;
        String eventSource = null;
        if((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null) {
            eventTypeOfPerformance = "WORKCENTER";
            eventSource = request.getEventSource() + "_DONE";
        } else {
            eventTypeOfPerformance = "RESOURCE";
            eventSource = request.getEventSource() + "_COMPLETE";
        }

        Map<String, Object> queryParameters = new HashMap<>();

        String sql = buildOEEByTimeQuery(queryParameters, request, eventTypeOfPerformance, eventSource);
        try {
            Query query = entityManager.createNativeQuery(sql);
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            OeeByTimeResponse response = new OeeByTimeResponse();

            Map<String, List<OeeByTimeResponse.OeeTimeData>> groupedByResource = new LinkedHashMap<>();

            // 1. Get the minimum datetime from all results
            LocalDateTime minDateTime = results.stream()
                    .map(r -> (r[0] != null) ? ((Timestamp) r[0]).toLocalDateTime() : null)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);

            if (minDateTime == null) return new OeeByTimeResponse(); // No valid data

            Map<String, Map<LocalDateTime, List<Double>>> groupedByResourceAndHour = new LinkedHashMap<>();

            for (Object[] result : results) {
                String resourceId = result[6] != null ? result[6].toString() : "UNKNOWN";
                LocalDateTime dateTime = result[0] != null ? ((Timestamp) result[0]).toLocalDateTime() : null;
                if (dateTime == null) continue;

                double oee = (result[4] != null) ? ((Number) result[4]).doubleValue() : 0.0;
                double availability = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
                double performance = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;
                double quality = (result[3] != null) ? ((Number) result[3]).doubleValue() : 0.0;
                double totalDowntime = (result[5] != null) ? ((Number) result[5]).doubleValue() : 0.0;

                double percentage;
                switch (request.getType().toLowerCase()) {
                    case "oee": percentage = oee; break;
                    case "availability": percentage = availability; break;
                    case "performance": percentage = performance; break;
                    case "quality": percentage = quality; break;
                    case "downtime": percentage = totalDowntime; break;
                    default: percentage = 0.0;
                }

                if (percentage == 0.0) continue;

                long hoursFromStart = Duration.between(minDateTime, dateTime).toHours();
                LocalDateTime bucketStart = minDateTime.plusHours(hoursFromStart);

                groupedByResourceAndHour
                        .computeIfAbsent(resourceId, k -> new LinkedHashMap<>())
                        .computeIfAbsent(bucketStart, k -> new ArrayList<>())
                        .add(percentage);
            }

            List<OeeByTimeResponse.OeeResourceData> finalData = groupedByResourceAndHour.entrySet().stream()
                    .map(entry -> {
                        String resourceId = entry.getKey();
                        List<OeeByTimeResponse.OeeTimeData> timePoints = entry.getValue().entrySet().stream()
                                .map(e -> {
                                    double average = e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                                    return new OeeByTimeResponse.OeeTimeData(
                                            e.getKey().format(dateFormatter),
                                            Math.round(average * 100.0) / 100.0
                                    );
                                })
                                .collect(Collectors.toList());

                        return new OeeByTimeResponse.OeeResourceData(resourceId, timePoints);
                    })
                    .collect(Collectors.toList());

            response.setResources(finalData);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String buildOEEByTimeQuery(Map<String, Object> queryParameters, OeeFilterRequest request, String eventTypeOfPerformance, String eventSource) {
        StringBuilder sql = new StringBuilder(
                "SELECT DATE_TRUNC('second', o.created_datetime) AS date, " +
                        "o.availability, " +
                        "o.performance, " +
                        "o.quality, " +
                        "o.oee, " +
                        "SUM(o.total_downtime)," +
                        "o.resource_id " +
                        "FROM r_aggregated_oee o " +
                        "WHERE 1 = 1 "
//                        "WHERE o.active = TRUE "
        );

        // Append dynamically generated WHERE conditions
        sql.append(buildWhereClause(queryParameters, request, eventTypeOfPerformance, eventSource));

        sql.append(" GROUP BY DATE_TRUNC('second', o.created_datetime), o.availability, o.performance, o.quality, o.oee, o.resource_id ")
                .append(" ORDER BY DATE_TRUNC('second', o.created_datetime)");

        return sql.toString();
    }

    public String buildOEESpeedLossByResourceQuery(Map<String, Object> queryParameters, OeeFilterRequest request, String eventTypeOfPerformance, String eventSource) {

        StringBuilder sql = new StringBuilder(
                "SELECT resource_id, " +
                        "o.planned_quantity AS total_plan, " +
                        "o.total_quantity AS total_quantity " +
                        "FROM r_aggregated_oee o " +
                        "WHERE o.active = TRUE " +
                        "AND COALESCE(o.resource_id, '') <> '' " +
                        "AND (o.planned_quantity > 0 OR o.total_quantity > 0) "
        );

        // Append dynamically generated WHERE conditions
        sql.append(buildWhereClause(queryParameters, request, eventTypeOfPerformance, eventSource));

        sql.append(" GROUP BY resource_id, o.total_quantity, o.planned_quantity ");
        return sql.toString();
    }

    public String buildOEESpeedLossByWorkcenterQuery(Map<String, Object> queryParameters, OeeFilterRequest request, String eventTypeOfPerformance, String eventSource) {

        StringBuilder sql = new StringBuilder(
                "SELECT o.workcenter_id, " +
                        "o.planned_quantity AS total_plan, " +
                        "o.total_quantity AS total_quantity " +
                        "FROM r_aggregated_oee o " +
                        "WHERE o.active = TRUE " +
                        "AND (o.planned_quantity > 0 OR o.total_quantity > 0) "
        );

        // Append dynamically generated WHERE conditions
        sql.append(buildWhereClause(queryParameters, request, eventTypeOfPerformance, eventSource));

        sql.append(" GROUP BY o.workcenter_id, o.total_quantity, o.planned_quantity ORDER BY o.workcenter_id");
        return sql.toString();
    }

    private String buildWhereClause(Map<String, Object> queryParameters, OeeFilterRequest request, String eventTypeOfPerformance, String eventSource) {
        StringBuilder whereClause = new StringBuilder();

        if (request.getSite() != null) {
            whereClause.append(" AND o.site = :site ");
            queryParameters.put("site", request.getSite());
        }

        if (eventTypeOfPerformance != null) {
            whereClause.append(" AND o.category = :eventTypeOfPerformance ");
            queryParameters.put("eventTypeOfPerformance", eventTypeOfPerformance);
        }

        if (eventSource != null) {
            whereClause.append(" AND o.event_source = :eventSource ");
            queryParameters.put("eventSource", eventSource);
        }

//        whereClause.append(" AND COALESCE(o.batch_number, '') <> '' "); // Ensuring batch_number is non-empty

        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            whereClause.append(" AND o.resource_id IN (:resourceId) ");
            queryParameters.put("resourceId", request.getResourceId());
        }

        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            whereClause.append(" AND o.shift_id IN (:shiftId) ");
            queryParameters.put("shiftId", request.getShiftId());
        }

        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            whereClause.append(" AND o.workcenter_id IN (:workcenterId) ");
            queryParameters.put("workcenterId", request.getWorkcenterId());
        }

        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
            whereClause.append(" AND o.batch_number IN (:batchNumber) ");
            queryParameters.put("batchNumber", request.getBatchNumber());
        }

        if (request.getShoporderId() != null && !request.getShoporderId().isEmpty()) {
            whereClause.append(" AND o.shop_order_id IN (:shopOrderId) ");
            queryParameters.put("shopOrderId", request.getShoporderId());
        }

        if (request.getItem() != null && !request.getItem().isEmpty()) {
            whereClause.append(" AND o.item IN (:item) ");
            queryParameters.put("item", request.getItem());
        }

        if (request.getOperations() != null && !request.getOperations().isEmpty()) {
            whereClause.append(" AND o.operation IN (:operation) ");
            queryParameters.put("operation", request.getOperations());
        }

        // Interval Time Conditions
        if (request.getStartTime() != null && request.getEndTime() != null) {

            whereClause.append("AND o.interval_start_date_time <= :endTime ")
                    .append("AND o.interval_end_date_time >= :startTime ");

            queryParameters.put("startTime", request.getStartTime());
            queryParameters.put("endTime", request.getEndTime());

        } else {
            LocalDateTime now = LocalDateTime.now();

            ShiftRequest shiftreq = new ShiftRequest();
            shiftreq.setSite(request.getSite());
            LocalDateTime startTime = getEarliestValidShiftStartDateTime(shiftreq);

            whereClause.append("AND o.interval_start_date_time <= :endTime ")
                    .append("AND o.interval_end_date_time >= :startTime ");

            queryParameters.put("startTime", startTime);
            queryParameters.put("endTime", now);

            request.setStartTime(startTime);
            request.setEndTime(now);

        }

        return whereClause.toString();
    }

    @Override
    public OeeByMachineResponse getByMachine(OeeFilterRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(startTime);
            request.setEndTime(endTime);
        }

        List<String> eventTypes = new ArrayList<>();
        String eventSource = null;
        Boolean checkBatchCondition = false;
        if((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null){
            eventTypes.add("WORKCENTER");
            eventSource = request.getEventSource() + "_DONE";
        } else {
            eventTypes.add("RESOURCE");
            eventSource = request.getEventSource() + "_COMPLETE";
        }
        if("MANUAL".equalsIgnoreCase(request.getEventSource()))
            checkBatchCondition = true;

        List<AggregatedOee> oeeDetails =
                findAggregatedOeeByFilters(
                request.getSite(),
                request.getStartTime(),
                request.getEndTime(),
                request.getResourceId(),
                request.getShiftId(),
                request.getWorkcenterId(),
                request.getBatchNumber(),
                request.getShoporderId(),
                request.getItem(),
                request.getOperations(),
                eventTypes,
                eventSource, // Include both events
                checkBatchCondition
        );
//        aggregatedOeeRepository.findByFilters1(
//                request.getSite(),
//                request.getStartTime(),
//                request.getEndTime(),
//                request.getResourceId(),
//                request.getShiftId(),
//                request.getWorkcenterId(),
//                request.getBatchNumber(),
//                request.getShoporderId(),
//                request.getItem(),
//                request.getOperations(),
//                eventTypes,
//                eventSource, // Include both events
//                checkBatchCondition
//        );
        Map<String, List<AggregatedOee>> groupedData = oeeDetails.stream()
                .filter(Objects::nonNull)
                .filter(oee -> oee.getResourceId() != null)
                .collect(Collectors.groupingBy(AggregatedOee::getResourceId));

        List<OeeByMachineResponse.OeeMachineData> responseDetailsList = groupedData.entrySet().stream()
                .map(entry -> new OeeByMachineResponse.OeeMachineData(
                        entry.getKey(), // resourceId
                        getMetricValueByType(entry.getValue(), request.getType()) // List<Oee>
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        OeeByMachineResponse response = new OeeByMachineResponse();
        response.setStartTime(request.getStartTime());
        response.setEndTime(request.getEndTime());
        response.setOeeByMachine(responseDetailsList);
        return response;
    }

    private double getMetricValueForResource(ResourceDetails resource, String type) {
        if (type == null) {
            return 0.0;
        }

        double value;
        switch (type.toLowerCase()) {
            case "availability":
                value = resource.getAvailability();
                break;
            case "performance":
                value = resource.getPerformance();
                break;
            case "quality":
                value = resource.getQuality();
                break;
            case "oee":
                value = resource.getOee();
                break;
            default:
                value = 0.0;
                break;
        }

        // Round to two decimal places
        return Math.round(value * 100.0) / 100.0; // percent needed?
    }

//    private double getMetricValueForSite(SiteDetails site, String type) {
//        if (type == null) {
//            return 0.0;
//        }
//
//        double value;
//        switch (type.toLowerCase()) {
//            case "availability":
//                value = site.getAvailability();
//                break;
//            case "performance":
//                value = site.getPerformance();
//                break;
//            case "quality":
//                value = site.getQuality();
//                break;
//            case "oee":
//                value = site.getOee();
//                break;
//            default:
//                value = 0.0;
//                break;
//        }
//
//        // Round to two decimal places
//        return Math.round(value * 100.0) / 100.0;
//    }

    private double getMetricValueByType(List<AggregatedOee> oeeList, String type) {
        if (type == null || oeeList == null || oeeList.isEmpty()) {
            return 0.0;
        }

        double value;
        switch (type.toLowerCase()) {
            case "availability":
                value = oeeList.stream().mapToDouble(AggregatedOee::getAvailability).average().orElse(0.0);//remove avg
                break;
            case "performance":
                value = oeeList.stream().mapToDouble(AggregatedOee::getPerformance).average().orElse(0.0);
                break;
            case "quality":
                value = oeeList.stream().mapToDouble(AggregatedOee::getQuality).average().orElse(0.0);
                break;
            case "oee":
                value = oeeList.stream().mapToDouble(AggregatedOee::getOee).average().orElse(0.0);
                break;
            default:
                value = 0.0;
                break;
        }

        return (Math.round(value * 100.0) / 100.0);
    }

    private double getMetricValueByType1(List<Oee> oeeList, String type) {
        if (type == null || oeeList == null || oeeList.isEmpty()) {
            return 0.0;
        }

        double value;
        switch (type.toLowerCase()) {
            case "availability":
                value = oeeList.stream().mapToDouble(Oee::getAvailability).average().orElse(0.0);//remove avg
                break;
            case "performance":
                value = oeeList.stream().mapToDouble(Oee::getPerformance).average().orElse(0.0);
                break;
            case "quality":
                value = oeeList.stream().mapToDouble(Oee::getQuality).average().orElse(0.0);
                break;
            case "oee":
                value = oeeList.stream().mapToDouble(Oee::getOee).average().orElse(0.0);
                break;
            default:
                value = 0.0;
                break;
        }

        // Round to two decimal places
        return Math.round(value * 100.0) / 100.0;
    }

    /*@Override
    public OeeByMachineResponse getByMachine(OeeFilterRequest request) throws Exception {
        String eventTypeOfPerformance = "completeSfcBatch";

        if ((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null) {
            eventTypeOfPerformance = "doneSfcBatch";
        }
        StringBuilder sql = new StringBuilder(
                "SELECT o.resource_id AS date, " +
                        "SUM(o.good_qty) AS totalGoodQty, " +
                        "SUM(o.bad_qty) AS totalBadQty, " +
                        "SUM(o.plan) AS totalPlannedQty, " +
                        "SUM(o.total_downtime) AS totalDowntime, " +
                        "MIN(o.interval_start_date_time) AS intervalStartTime, " +
                        "MAX(o.interval_end_date_time) AS intervalEndTime " +
                        "FROM R_OEE o " +
                        "WHERE o.site = :site " +
                        "AND o.event_type_of_performance = :eventTypeOfPerformance " +
                        "AND o.batch_number IS NOT NULL " +  // Ensure batch_number is not null
                        "AND o.batch_number <> '' " +
                        "AND o.plan > 0 ");  // Exclude empty strings

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());
        queryParameters.put("eventTypeOfPerformance", eventTypeOfPerformance);

        applyCommonFilters(sql, queryParameters, request);

        // Group by resource_id since event_type_of_performance is already filtered
        sql.append("GROUP BY o.resource_id " +
                "ORDER BY o.resource_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            OeeByMachineResponse response = new OeeByMachineResponse();

            List<OeeByMachineResponse.OeeMachineData> OeeMachineList = new ArrayList<>();

            for (Object[] result : results) {
                OeeByMachineResponse.OeeMachineData oeeByMachine = new OeeByMachineResponse.OeeMachineData();

                oeeByMachine.setResourceId((result[0] != null) ? result[0].toString() : "");

                double totalGoodQty = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
                double totalBadQty = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;
                double totalPlannedQty = (result[3] != null) ? ((Number) result[3]).doubleValue() : 0.0;
                double totalDowntime = (result[4] != null) ? ((Number) result[4]).doubleValue() : 0.0;

                LocalDateTime intervalStartTime = (result[5] != null) ? ((java.sql.Timestamp) result[5]).toLocalDateTime() : null;
                LocalDateTime intervalEndTime = (result[6] != null) ? ((java.sql.Timestamp) result[6]).toLocalDateTime() : null;

                // Calculate total time in seconds
                long totalTimeSeconds = (intervalStartTime != null && intervalEndTime != null) ?
                        Duration.between(intervalStartTime, intervalEndTime).getSeconds() : 0L;

                long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
                long actualTimeSeconds = totalTimeSeconds;

                // Calculate availability, performance, quality, and OEE
                double availability = (productionTimeSeconds > 0) ? ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;
                double performance = (totalPlannedQty > 0) ? ((totalGoodQty + totalBadQty) / totalPlannedQty) * 100 : 0.0;
                double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;
                double oee = (availability * performance * quality) / 10000;

                // Set calculated metrics based on type from request
                String type = request.getType(); // Assuming 'type' field comes in the request (oee, performance, availability, quality, downtime)

                double percentage = 0.0;
                switch (type.toLowerCase()) {
                    case "oee":
                        percentage = oee;
                        break;
                    case "performance":
                        percentage = performance;
                        break;
                    case "availability":
                        percentage = availability;
                        break;
                    case "quality":
                        percentage = quality;
                        break;
                    case "downtime":
                        percentage = totalDowntime;
                        break;
                    default:
                        percentage = 0.0;
                        break;
                }

                oeeByMachine.setPercentage(Math.round(percentage * 100.0) / 100.0);
                OeeMachineList.add(oeeByMachine);
                *//*if (percentage != 0.0) {
                    OeeMachineList.add(oeeByMachine);
                }*//*
            }

            response.setOeeByMachine(OeeMachineList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    public OeeByProductionLineResponse getByProductionLine(OeeFilterRequest request) throws Exception {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(startTime);
            request.setEndTime(endTime);
        }

        String eventType = null;
        String eventSource = null;
        Boolean checkBatchCondition = false;
        if(request.getResourceId() != null || request.getOperation() != null) {
            eventType = "RESOURCE";
            eventSource = request.getEventSource() + "_COMPLETE";
        } else {
            eventType = "WORKCENTER";
            eventSource = request.getEventSource() + "_DONE";
        }

        if("MANUAL".equalsIgnoreCase(request.getEventSource()))
            checkBatchCondition = true;

        List<AggregatedOee> oeeDetails =
                findAggregatedOeeByFilters(
                request.getSite(),
                request.getStartTime(),
                request.getEndTime(),
                request.getResourceId(),
                request.getShiftId(),
                request.getWorkcenterId(),
                request.getBatchNumber(),
                request.getShoporderId(),
                request.getItem(),
                request.getOperations(),
                List.of(eventType),
                eventSource,
                checkBatchCondition
        );

//        aggregatedOeeRepository.findByFilters1(
//                request.getSite(),
//                request.getStartTime(),
//                request.getEndTime(),
//                request.getResourceId(),
//                request.getShiftId(),
//                request.getWorkcenterId(),
//                request.getBatchNumber(),
//                request.getShoporderId(),
//                request.getItem(),
//                request.getOperations(),
//                List.of(eventType),
//                eventSource,
//                checkBatchCondition
//        );

        Map<String, List<AggregatedOee>> groupedData = oeeDetails.stream()
                .filter(Objects::nonNull)
                .filter(oee -> oee.getWorkcenterId() != null)
                .collect(Collectors.groupingBy(AggregatedOee::getWorkcenterId));

        List<OeeByProductionLineResponse.OeeProductionLineData> responseDetailsList = groupedData.entrySet().stream()
                .map(entry -> new OeeByProductionLineResponse.OeeProductionLineData(
                        entry.getKey(), // workcenterId
                        getMetricValueByType(entry.getValue(), request.getType())
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        OeeByProductionLineResponse response = new OeeByProductionLineResponse();
        response.setOeeByProductionLine(responseDetailsList);
        return response;
    }

    private double getMetricValueForWorkcenter(List<AggregatedOee> workcenter, String type) {
        if (type == null) {
            return 0.0;
        }

        double value;
        switch (type.toLowerCase()) {
            case "availability":
                value = workcenter.stream().mapToDouble(AggregatedOee::getAvailability).average().orElse(0.0);
                break;
            case "performance":
                value = workcenter.stream().mapToDouble(AggregatedOee::getPerformance).average().orElse(0.0);
                break;
            case "quality":
                value = workcenter.stream().mapToDouble(AggregatedOee::getQuality).average().orElse(0.0);
                break;
            case "oee":
                value = workcenter.stream().mapToDouble(AggregatedOee::getOee).average().orElse(0.0);
                break;
            default:
                value = 0.0;
                break;
        }
        return Math.round(value * 100.0) / 100.0;
    }

    /*@Override
    public OeeByProductionLineResponse getByProductionLine(OeeFilterRequest request) throws Exception {
        String eventTypeOfPerformance = "doneSfcBatch";
        if(request.getResourceId() != null || request.getOperation() != null) eventTypeOfPerformance = "completeSfcBatch";
        StringBuilder sql = new StringBuilder(
                "SELECT o.workcenter_id, " +
                        "SUM(o.good_qty) AS totalGoodQty, " +
                        "SUM(o.bad_qty) AS totalBadQty, " +
                        "SUM(o.plan) AS totalPlannedQty, " +
                        "SUM(o.total_downtime) AS totalDowntime, " +
                        "MIN(o.interval_start_date_time) AS intervalStartTime, " +
                        "MAX(o.interval_end_date_time) AS intervalEndTime " +
                        "FROM R_OEE o " +
                        "WHERE o.site = :site " +
                        "AND o.event_type_of_performance = :eventTypeOfPerformance " +  // Event type filter
                        "AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
                        "AND o.plan > 0 ");
        //"AND o.workcenter_id IS NOT NULL AND o.workcenter_id <> '' ");

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());
        queryParameters.put("eventTypeOfPerformance", eventTypeOfPerformance);

        applyCommonFilters(sql, queryParameters, request);

        // Grouping by availability_id and created_datetime (or any other grouping needed)
        sql.append("GROUP BY o.workcenter_id " +
                "ORDER BY o.workcenter_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            OeeByProductionLineResponse response = new OeeByProductionLineResponse();

            List<OeeByProductionLineResponse.OeeProductionLineData> OeeProductionLineList = results.stream()
                    .map(result -> {
                        OeeByProductionLineResponse.OeeProductionLineData oeeByProductionLine = new OeeByProductionLineResponse.OeeProductionLineData();
                        // Parsing the date
                        if (result[0] != null) {
                            oeeByProductionLine.setWorkcenterId(result[0].toString());
                        } else {
                            oeeByProductionLine.setWorkcenterId("Unknown");
                        }

                        double totalGoodQty = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
                        double totalBadQty = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;
                        double totalPlannedQty = (result[3] != null) ? ((Number) result[3]).doubleValue() : 0.0;
                        double totalDowntime = (result[4] != null) ? ((Number) result[4]).doubleValue() : 0.0;

                        LocalDateTime intervalStartTime = (result[5] != null) ? ((java.sql.Timestamp) result[5]).toLocalDateTime() : null;
                        LocalDateTime intervalEndTime = (result[6] != null) ? ((java.sql.Timestamp) result[6]).toLocalDateTime() : null;

                        // Calculate total time in seconds
                        long totalTimeSeconds = (intervalStartTime != null && intervalEndTime != null) ?
                                Duration.between(intervalStartTime, intervalEndTime).getSeconds() : 0L;

                        long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
                        long actualTimeSeconds = totalTimeSeconds;

                        // Calculate availability, performance, quality, and OEE
                        double availability = (productionTimeSeconds > 0) ? ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;
                        double performance = (totalPlannedQty > 0) ? ((totalGoodQty + totalBadQty) / totalPlannedQty) * 100 : 0.0;
                        double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;
                        double oee = (availability * performance * quality) / 10000;

                        // Set calculated metrics based on type from request
                        String type = request.getType();
                        double percentage = 0.0;
                        switch (type.toLowerCase()) {
                            case "oee":
                                percentage = oee;
                                break;
                            case "performance":
                                percentage = performance;
                                break;
                            case "quality":
                                percentage = quality;
                                break;
                            default:
                                percentage = 0.0;
                                break;
                        }

                        oeeByProductionLine.setPercentage(Math.round(percentage * 100.0) / 100.0);
                        return (percentage != 0.0) ? oeeByProductionLine : null;
                    })
                    .filter(Objects::nonNull) // Remove entries where percentage is zero
                    .collect(Collectors.toList());


            response.setOeeByProductionLine(OeeProductionLineList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/
//    --------kartheek nasina--------

    @Override
    public OeeByProductResponse getByProduct(OeeFilterRequest request) throws Exception {

        String eventTypeOfPerformance = null;
        String eventSource = null;
        if(request.getResourceId() != null || request.getOperation() != null) {
            eventTypeOfPerformance = "RESOURCE";
            eventSource = request.getEventSource() + "_COMPLETE";
        } else {
            eventTypeOfPerformance = "WORKCENTER";
            eventSource = request.getEventSource() + "_DONE";
        }

        Map<String, Object> queryParameters = new HashMap<>();

        String sql = buildOEEByProductQuery(queryParameters, request, eventTypeOfPerformance, eventSource);

        try {
            Query query = entityManager.createNativeQuery(sql);
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            OeeByProductResponse response = new OeeByProductResponse();

            List<Map<String, Object>> OeeProductList = results.stream()
                    .filter(result -> result[0] != null)
                    .map(result -> {

                        double availability = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
                        double performance  = result[2] != null ? ((Number) result[2]).doubleValue() : 0.0;
                        double quality      = result[3] != null ? ((Number) result[3]).doubleValue() : 0.0;
                        double oee          = result[4] != null ? ((Number) result[4]).doubleValue() : 0.0;
                        double goodQty      = result[5] != null ? ((Number) result[5]).doubleValue() : 0.0;
                        double badQty       = result[6] != null ? ((Number) result[6]).doubleValue() : 0.0;

                        String type = request.getType() != null ? request.getType().toLowerCase() : "";

                        Map<String, Object> data = new HashMap<>();
                        data.put("product", result[0] != null ? result[0].toString() : "");

                        switch (type) {
                            case "oee":
                                data.put("availability", round(availability));
                                data.put("performance", round(performance));
                                data.put("quality", round(quality));
                                data.put("oee", round(oee));
                                break;

                            case "quality":
                                data.put("goodQty", round(goodQty));
                                data.put("badQty", round(badQty));
                                break;

                            default:
                                break;
                        }

                        return data;
                    })
                    .filter(data -> data.size() > 1)
                    .collect(Collectors.toList());

            response.setOeeByProduct(OeeProductList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }


//    --------kartheek nasina--------

    private OeeRequest buildOeeRequest(OeeFilterRequest request){

        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(startTime);
            request.setEndTime(endTime);
        }

        OeeRequest oeeRequest = OeeRequest.builder()
                .site(request.getSite())
                .workcenterId(request.getWorkcenterId())
                .resourceId(request.getResourceId())
                .shiftId(request.getShiftId())
                .itemBo(request.getItemBo())
                .startTime(String.valueOf(request.getStartTime()))
                .endTime(String.valueOf(request.getEndTime()))
                .batchno(request.getBatchNumber())
                .build();

        return oeeRequest;
    }

    @Override
    public PerformanceComparisonResponse getPerformanceComparison(OeeFilterRequest request) {

        Map<String, Object> queryParameters = new HashMap<>();

        String sql = buildOEEPerformanceComparisonQuery(queryParameters, request);

        try {
            Query query = entityManager.createNativeQuery(sql);
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceComparisonResponse response = new PerformanceComparisonResponse();
            response.setStartTime(request.getStartTime().toString());
            response.setEndTime(request.getEndTime().toString());

            List<PerformanceComparisonResponse.ComparisonData> comparisonList = results.stream()
                    .map(row -> {
                        String machine = row[0] != null ? row[0].toString() : "";

//                        double totalGoodQty = (row[1] != null) ? ((Number) row[1]).doubleValue() : 0.0;
//                        double totalBadQty = (row[2] != null) ? ((Number) row[2]).doubleValue() : 0.0;
//                        double totalPlannedQty = (row[3] != null) ? ((Number) row[3]).doubleValue() : 0.0;
                        double totalDowntime = (row[4] != null) ? ((Number) row[4]).doubleValue() : 0.0;

//                        double performancePercentage = (totalPlannedQty > 0) ? ((totalGoodQty + totalBadQty) / totalPlannedQty) * 100 : 0.0;
//                        double qualityPercentage = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;

                        double performancePercentage = (row[1] != null) ? ((Number) row[1]).doubleValue() : 0.0;
                        double qualityPercentage = (row[2] != null) ? ((Number) row[2]).doubleValue() : 0.0;

                        // Round to two decimal places
//                        performancePercentage = Math.round(performancePercentage * 100.0) / 100.0;
//                        qualityPercentage = Math.round(qualityPercentage * 100.0) / 100.0;

                        // Avoid returning 0 values
                        if (performancePercentage == 0.0 && totalDowntime == 0.0 && qualityPercentage == 0.0) return null;

                        return new PerformanceComparisonResponse.ComparisonData(
                                machine,
                                performancePercentage,
                                totalDowntime,
                                qualityPercentage
                        );
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            response.setPerformanceComparison(comparisonList);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return new PerformanceComparisonResponse(request.getStartTime().toString(), request.getEndTime().toString(), Collections.emptyList());
        }
    }

    public static long calculateSecondsFromStartToNow(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }

        // Use endTime if provided, otherwise default to current time
        LocalDateTime effectiveEndTime = (endTime != null) ? endTime : LocalDateTime.now();

        // If startTime is in the future, return 0
        if (startTime.isAfter(effectiveEndTime)) {
            return 0;
        }

        Duration duration = Duration.between(startTime, effectiveEndTime);
        return duration.getSeconds();
    }



    @Override
    public SiteDetails getOeeDetailBySite(OeeRequest request) {
        List<ShiftDetails> shiftDetailsList=new ArrayList<>();
        ShiftRequest shiftreq= new ShiftRequest();
        shiftreq.setSite(request.getSite());
        LocalDateTime intervalStartDateTime=getEarliestValidShiftStartDateTime(shiftreq);

        LocalDateTime now = LocalDateTime.now();
        List<String> eventType = Arrays.asList("doneSfcBatch", "completeSfcBatch");
        // Fetch OEE records based on site and time interval
        List<Oee> oeeDetails = oeeRepository.findByIntervalAndSiteForBatch(
                intervalStartDateTime,
                now,
                request.getSite(),
                request.getEventSource().equals("MANUAL")
                        ? eventType
                        : List.of("machineDoneSfcBatch", "machineCompleteSfcBatch"),
                request.getEventSource().equals("MANUAL")
        );

        oeeDetails.sort(Comparator.comparing(Oee::getIntervalStartDateTime));

         Map<String, List<Oee>> groupedBySite = groupOeeDetails(oeeDetails, Oee::getSite, this::isValidEventType);


        Map<String, Map<String, Double>> scrapqty=calculateTotalQuantitiesForSite(groupedBySite);
      groupedBySite = groupOeeDetails(
                oeeDetails,
                Oee::getSite,
              oee -> {
                  if (request.getEventSource().equals("MANUAL")) {
                      return !"completeSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                  } else {
                      return !"machineCompleteSfcBatch".equalsIgnoreCase(oee.getEventTypeOfPerformance());
                  }
              }
        );

        if (oeeDetails.isEmpty()) {
            // Step 1: Create a new Oee object with site from request
            Oee updatedOee = Oee.builder()
                    .site(request.getSite())
                    .totalDowntime(0)
                    .availability(0)
                    .performance(0)
                    .quality(0)
                    .goodQty(0)
                    .badQty(0)
                    .totalQty(0)
                    .oee(0)
                    .plan(0)
                    .productionTime(0)
                    .actualTime(0)
                    .intervalStartDateTime(intervalStartDateTime)
                    .availabilityId(0L)
                    .performanceId(0L)
                    .qualityId(0L)
                    .active(1)
                    .build();

// Step 2: Create a new group list with updated Oee object
            List<Oee> group = new ArrayList<>();
            group.add(updatedOee);  // Ensure it has at least one element

            List<Oee> updatedGroup = new ArrayList<>(group);
            updatedGroup.set(0, updatedOee); // Now safe to replace the first element

// Step 3: Call the calculate method with updated group
            long totalTimeSeconds = calculateSecondsFromStartToNow(intervalStartDateTime,null);
            return calculateSiteDetail(updatedGroup, totalTimeSeconds, scrapqty);

        }
        for (List<Oee> group : groupedBySite.values()) {
            if (!isNullOrEmpty(group.get(0).getSite())) {
                long totalTimeSeconds = calculateSecondsFromStartToNow(intervalStartDateTime,null);
                return calculateSiteDetail(group, totalTimeSeconds, scrapqty);
            }

        }
        return new SiteDetails();
    }

    @Override
    public List<ShiftDetails> getOeeDetailsByShift(OeeRequest request) {
        List<Oee> oeeDetails = fetchOeeDetailsByShift(request);
        Map<String, List<Oee>> groupedData = groupOeeDetails(oeeDetails, this::getShiftGroupKey, this::isValidEventType);
        Map<String, Map<String, Double>> scrapqty= calculateTotalQuantitiesByShift(groupedData);
        return groupedData.values().stream()
                .filter(group -> group.get(0).getShiftId() != null && !group.get(0).getShiftId().isEmpty())
                .map(group -> calculateShiftDetails(group, scrapqty,null)) // Ensure correct method signature
                .collect(Collectors.toList());

    }

    @Override
    public List<BatchDetails> getOeeDetailsByBatch(OeeRequest request) {
        List<Oee> oeeDetails = fetchOeeDetailsByBatch(request);
        Map<String, List<Oee>> groupedData = groupOeeDetails(oeeDetails, Oee::getBatchNumber, this::isValidEventType);
        Map<String, Map<String, Double>> scrapqty = calculateTotalQuantitiesByBatch(groupedData);
        List<BatchDetails> batchDetailsList = calculateBatchDetails(groupedData, 0, scrapqty);
        return  batchDetailsList;
    }

    @Override
    public List<ShiftDetails> getOeeDetailsByShiftAndSite(OeeRequest request) {
        return null;
    }

    @Override
    public List<ResourceDetails> getOeeDetailsByResource(OeeRequest request) {
        List<Oee> oeeDetails = fetchOeeDetailsByResource(request);
        Map<String, List<Oee>> groupedData = groupOeeDetails(oeeDetails, Oee::getResourceId, this::isValidEventType);

        return groupedData.values().stream()
                .filter(group -> !isNullOrEmpty(group.get(0).getResourceId()))
                .map(this::calculateResourceDetails)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkcenterDetails> getOeeDetailsByWorkcenter(OeeRequest request) {
        List<Oee> oeeDetails = fetchOeeDetailsByWorkcenter(request);
        Map<String, List<Oee>> groupedData = groupOeeDetails(oeeDetails, Oee::getWorkcenterId, this::isValidEventType);
        Map<String, Map<String, Double>> scrapqty=calculateTotalQuantitiesByWorkCenter(groupedData);

        return groupedData.values().stream()
                .filter(group -> group.get(0).getWorkcenterId() != null && !group.get(0).getWorkcenterId().isEmpty())
                .map(group -> calculateWorkcenterDetails(group, scrapqty)) // Ensure correct method signature
                .collect(Collectors.toList());

    }

    @Override
    public List<SiteDetails> getAllOeeDetails(OeeRequest request) {
        List<Oee> oeeDetails = fetchAllOeeDetails(request);
        return oeeDetails.isEmpty() ? Collections.emptyList() : calculateSiteDetails(oeeDetails,request.getEventSource());
    }

    // Helper Methods

    private List<Oee> fetchOeeDetailsBySite(OeeRequest request) {

        String eventType = "doneSfcBatch";
        return oeeRepository.findBySiteAndIntervalBetween(
                request.getSite(),
                LocalDateTime.parse(request.getStartTime()),
                LocalDateTime.parse(request.getEndTime()),
                eventType
        );
    }

    private List<Oee> fetchOeeDetailsByShift(OeeRequest request) {
        return oeeRepository.findBySiteAndShiftIdInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
                request.getSite(),
                request.getShiftId(),
                LocalDateTime.parse(request.getStartTime()),
                LocalDateTime.parse(request.getEndTime())
        );
    }

    private List<Oee> fetchOeeDetailsByBatch(OeeRequest request) {
        return oeeRepository.findBySiteAndBatchNumberInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
                request.getSite(),
                request.getBatchno(),
                LocalDateTime.parse(request.getStartTime()),
                LocalDateTime.parse(request.getEndTime())
        );
    }

    private List<Oee> fetchOeeDetailsByResource(OeeRequest request) {
        return oeeRepository.findBySiteAndResourceIdInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
                request.getSite(),
                request.getResourceId(),
                LocalDateTime.parse(request.getStartTime()),
                LocalDateTime.parse(request.getEndTime())
        );
    }

    private List<Oee> fetchOeeDetailsByWorkcenter(OeeRequest request) {
        return oeeRepository.findBySiteAndWorkcenterIdInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
                request.getSite(),
                request.getWorkcenterId(),
                LocalDateTime.parse(request.getStartTime()),
                LocalDateTime.parse(request.getEndTime())
        );
    }

    private List<Oee> fetchAllOeeDetails(OeeRequest request) {
        return oeeRepository.findByIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
                LocalDateTime.parse(request.getStartTime()),
                LocalDateTime.parse(request.getEndTime())
        );
    }

    private Map<String, List<Oee>> groupOeeDetails(List<Oee> oeeDetails, Function<Oee, String> groupByKey, Predicate<Oee> filterPredicate) {
        return oeeDetails.stream()
                .filter(Objects::nonNull)
                .filter(filterPredicate)
                .filter(oee -> oee.getWorkcenterId() != null)
                .filter(oee -> oee.getWorkcenterId() != "")
                .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty())


                .filter(oee -> {
                    String eventType = oee.getEventTypeOfPerformance();
                    if (!eventType.toLowerCase().contains("machine")) {
                        return eventType.equalsIgnoreCase("completeSfcBatch") ||
                                eventType.equalsIgnoreCase("doneSfcBatch");
                    } else {
                        return eventType.equalsIgnoreCase("machineCompleteSfcBatch") ||
                                eventType.equalsIgnoreCase("machineDoneSfcBatch");
                    }
                })
                .collect(Collectors.groupingBy(groupByKey));
    }

    private String getShiftGroupKey(Oee oee) {
        return oee.getSite() + "-" + oee.getShiftId() + "-" + oee.getWorkcenterId() + "-" + oee.getResourceId() + "-" + oee.getBatchNumber();
    }

    private boolean isValidEventType(Oee oee) {
        return VALID_EVENT_TYPES.contains(oee.getEventTypeOfPerformance().toLowerCase());
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private long calculateTotalTimeSeconds(List<Oee> oeeDetails) {
        LocalDateTime intervalStartTime = oeeDetails.get(0).getIntervalStartDateTime();
        LocalDateTime intervalEndTime = oeeDetails.get(oeeDetails.size() - 1).getIntervalEndDateTime();
        return Duration.between(intervalStartTime, intervalEndTime).getSeconds();
    }

    public static LocalDateTime getEarliestIntervalStartDateTime(List<Oee> oeeList) {
        Optional<LocalDateTime> earliest = oeeList.stream()
                .map(Oee::getIntervalStartDateTime)
                .min(LocalDateTime::compareTo);

        return earliest.orElse(null); // returns null if the list is empty
    }
    private LocalDateTime getLatestIntervalEndDateTime(List<Oee> group) {
        return group.stream()
                .map(Oee::getIntervalEndDateTime) // Extract interval end datetime
                .filter(Objects::nonNull) // Remove null values
                .max(LocalDateTime::compareTo) // Get the latest date-time
                .orElse(LocalDateTime.MIN); // Return minimum if no valid values exist
    }

    private SiteDetails calculateSiteDetail(List<Oee> siteGroup, long totalTimeSeconds,Map<String, Map<String, Double>> scrapqty) {
        double totalGoodQty = 0.0;
        double totalBadQty = 0.0;
        double totalActualQty = 0.0;

// Iterate over the map and find matching work center
        for (Map.Entry<String, Map<String, Double>> quantities : scrapqty.entrySet()) {
            if (quantities.getKey().equalsIgnoreCase(siteGroup.get(0).getShiftId())) {
                totalGoodQty = quantities.getValue().getOrDefault("goodQty", 0.0);
                totalBadQty = quantities.getValue().getOrDefault("badQty", 0.0);
                totalActualQty = quantities.getValue().getOrDefault("totalQty", 0.0);
            }
        }

        List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(siteGroup.get(0).getSite());

// Initialize final manufactured time accumulator
        double manufacturedTime = 0.0;

// Create a reusable CycleTimeReq and set the site
        CycleTimeReq cycleTimeReq = new CycleTimeReq();
        cycleTimeReq.setSite(siteGroup.get(0).getSite());

// Loop through each work center
        for (WorkCenter wc : trackedWorkCenters) {
            // Set the current work center ID in the request
            cycleTimeReq.setWorkCenterId(wc.getWorkCenter());

            // Fetch cycle times for this work center
            List<CycleTime> cycleTimes = fetchCycleTimesByItems(cycleTimeReq);

            // Sum the manufactured time for matching records
            double wcManufacturedTime = cycleTimes.stream()
                    .filter(ct -> ct.getWorkCenterId().equalsIgnoreCase(wc.getWorkCenter())
                            && (ct.getItem() == null || ct.getItem().isEmpty())
                            && (ct.getItemVersion() == null || ct.getItemVersion().isEmpty())
                            && (ct.getResourceId() == null || ct.getResourceId().isEmpty()))
                    .mapToDouble(CycleTime::getManufacturedTime)
                    .sum();

            // Accumulate the work center's manufactured time into the final total
            manufacturedTime += wcManufacturedTime;
        }
        ShiftRequest shiftreq= new ShiftRequest();
        shiftreq.setSite(siteGroup.get(0).getSite());
        LocalDateTime intervalStartDateTime=getEarliestValidShiftStartDateTime(shiftreq);

        DowntimRequestForShift downtimRequestForShift=new DowntimRequestForShift();
        downtimRequestForShift.setSite(siteGroup.get(0).getSite());
        downtimRequestForShift.setDowntimeStart(intervalStartDateTime);
        downtimRequestForShift.setDowntimeEnd(LocalDateTime.now());
        List<String> shiftIds = siteGroup.stream()
                .map(Oee::getShiftId)  // Extract shiftId
                .filter(Objects::nonNull) // Remove null values
                .distinct() // Ensure unique shiftIds
                .collect(Collectors.toList()); // Convert to List
        CurrentShiftDetails shiftDetails=getBreakHours(downtimRequestForShift);
        Metrics metrics = calculateMetrics(siteGroup,  calculateSecondsFromStartToNow(intervalStartDateTime,null), (int) totalGoodQty, (int) totalBadQty, (int) totalActualQty,manufacturedTime , intervalStartDateTime , LocalDateTime.now(),shiftDetails,"SITE");

        SiteDetails siteDetails = new SiteDetails();
        siteDetails.setSite(siteGroup.get(0).getSite());
        siteDetails.setActual((int) (metrics.getTotalGoodQty() + metrics.getTotalBadQty()));
        siteDetails.setPlan((int) metrics.getTotalPlannedQty());
        siteDetails.setRejection((int) metrics.getTotalBadQty());
        siteDetails.setDowntimeDuration(metrics.getTotalDowntime());
        siteDetails.setGoodQualityCount((int) metrics.getTotalGoodQty());
        siteDetails.setBadQualityCount((int) metrics.getTotalBadQty());
        siteDetails.setEnergyUsage(DEFAULT_ENERGY_USAGE);
        siteDetails.setProductionTime((int) metrics.getProductionTimeSeconds());
        siteDetails.setActualTime((int) metrics.getActualTimeSeconds());
        siteDetails.setPlannedDowntime((int) metrics.getPlannedDowntime());

        siteDetails.setActualCycleTime(metrics.getActualCycleTime());
        siteDetails.setPlannedCycletime(metrics.getPlannedCycleTime());
        siteDetails.setAvailability(metrics.getAvailability());
        siteDetails.setPerformance(metrics.getPerformance());
        siteDetails.setQuality(metrics.getQuality());
        siteDetails.setOee(metrics.getOee());

        return siteDetails;
    }

    private SiteDetails calculateSiteDetail1(List<AggregatedOee> siteGroup, long totalTimeSeconds,Map<String, Map<String, Double>> scrapqty) {
        double totalGoodQty = 0.0;
        double totalBadQty = 0.0;
        double totalActualQty = 0.0;

// Iterate over the map and find matching work center
        for (Map.Entry<String, Map<String, Double>> quantities : scrapqty.entrySet()) {
            if (quantities.getKey().equalsIgnoreCase(siteGroup.get(0).getShiftId())) {
                totalGoodQty = quantities.getValue().getOrDefault("goodQty", 0.0);
                totalBadQty = quantities.getValue().getOrDefault("badQty", 0.0);
                totalActualQty = quantities.getValue().getOrDefault("totalQty", 0.0);
            }
        }

        List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(siteGroup.get(0).getSite());

// Initialize final manufactured time accumulator
        double manufacturedTime = 0.0;

// Create a reusable CycleTimeReq and set the site
        CycleTimeReq cycleTimeReq = new CycleTimeReq();
        cycleTimeReq.setSite(siteGroup.get(0).getSite());

// Loop through each work center
        for (WorkCenter wc : trackedWorkCenters) {
            // Set the current work center ID in the request
            cycleTimeReq.setWorkCenterId(wc.getWorkCenter());

            // Fetch cycle times for this work center
            List<CycleTime> cycleTimes = fetchCycleTimesByItems(cycleTimeReq);

            // Sum the manufactured time for matching records
            double wcManufacturedTime = cycleTimes.stream()
                    .filter(ct -> ct.getWorkCenterId().equalsIgnoreCase(wc.getWorkCenter())
                            && (ct.getItem() == null || ct.getItem().isEmpty())
                            && (ct.getItemVersion() == null || ct.getItemVersion().isEmpty())
                            && (ct.getResourceId() == null || ct.getResourceId().isEmpty()))
                    .mapToDouble(CycleTime::getManufacturedTime)
                    .sum();

            // Accumulate the work center's manufactured time into the final total
            manufacturedTime += wcManufacturedTime;
        }
        ShiftRequest shiftreq= new ShiftRequest();
        shiftreq.setSite(siteGroup.get(0).getSite());
        LocalDateTime intervalStartDateTime=getEarliestValidShiftStartDateTime(shiftreq);

        DowntimRequestForShift downtimRequestForShift=new DowntimRequestForShift();
        downtimRequestForShift.setSite(siteGroup.get(0).getSite());
        downtimRequestForShift.setDowntimeStart(intervalStartDateTime);
        downtimRequestForShift.setDowntimeEnd(LocalDateTime.now());
        List<String> shiftIds = siteGroup.stream()
                .map(AggregatedOee::getShiftId)  // Extract shiftId
                .filter(Objects::nonNull) // Remove null values
                .distinct() // Ensure unique shiftIds
                .collect(Collectors.toList()); // Convert to List
        CurrentShiftDetails shiftDetails=getBreakHours(downtimRequestForShift);
        Metrics metrics = calculateMetrics1(siteGroup,  calculateSecondsFromStartToNow(intervalStartDateTime,null), (int) totalGoodQty, (int) totalBadQty, (int) totalActualQty,manufacturedTime , intervalStartDateTime , LocalDateTime.now(),shiftDetails,"SITE");

        SiteDetails siteDetails = new SiteDetails();
        siteDetails.setSite(siteGroup.get(0).getSite());
        siteDetails.setActual((int) (metrics.getTotalGoodQty() + metrics.getTotalBadQty()));
        siteDetails.setPlan((int) metrics.getTotalPlannedQty());
        siteDetails.setRejection((int) metrics.getTotalBadQty());
        siteDetails.setDowntimeDuration(metrics.getTotalDowntime());
        siteDetails.setGoodQualityCount((int) metrics.getTotalGoodQty());
        siteDetails.setBadQualityCount((int) metrics.getTotalBadQty());
        siteDetails.setEnergyUsage(DEFAULT_ENERGY_USAGE);
        siteDetails.setProductionTime((int) metrics.getProductionTimeSeconds());
        siteDetails.setActualTime((int) metrics.getActualTimeSeconds());
        siteDetails.setPlannedDowntime((int) metrics.getPlannedDowntime());

        siteDetails.setActualCycleTime(metrics.getActualCycleTime());
        siteDetails.setPlannedCycletime(metrics.getPlannedCycleTime());
        siteDetails.setAvailability(metrics.getAvailability());
        siteDetails.setPerformance(metrics.getPerformance());
        siteDetails.setQuality(metrics.getQuality());
        siteDetails.setOee(metrics.getOee());

        return siteDetails;
    }

    private ShiftDetails calculateShiftDetails(List<Oee> group,Map<String, Map<String, Double>> scrapqty,ShiftResponse shift) {
        double totalGoodQty = 0.0;
        double totalBadQty = 0.0;
        double totalActualQty = 0.0;
        Boolean remove=false;
// Iterate over the map and find matching work center
        for (Map.Entry<String, Map<String, Double>> quantities : scrapqty.entrySet()) {
            if (quantities.getKey().equalsIgnoreCase(group.get(0).getShiftId())) {
                totalGoodQty = quantities.getValue().getOrDefault("goodQty", 0.0);
                totalBadQty = quantities.getValue().getOrDefault("badQty", 0.0);
                totalActualQty = quantities.getValue().getOrDefault("totalQty", 0.0);
            }
        }


        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalDateTime intervalStartDateTime = LocalDateTime.now();
        LocalDateTime intervalEndDateTime=LocalDateTime.now();
        ShiftRequest shiftRequest= new ShiftRequest();
        shiftRequest.setShiftType("HANDLE");
        shiftRequest.setSite(group.get(0).getSite());
        shiftRequest.setShiftId(shift.getHandle());
        shift=new ShiftResponse();
        shift=getShiftDetail(shiftRequest);
// Step 1: Find the valid shift interval for today
        if (shift != null) {
            Optional<ShiftIntervals> matchingInterval = shift.getShiftIntervals().stream()
                    .filter(interval ->
                            !interval.getValidFrom().toLocalDate().isAfter(currentDate) &&  // ValidFrom <= Today
                                    !interval.getValidEnd().toLocalDate().isBefore(currentDate)    // ValidEnd >= Today
                    )
                    .findFirst();

            if (matchingInterval.isPresent()) {
                ShiftIntervals interval = matchingInterval.get();

                // Extract shift start and end times
                LocalTime shiftStart = interval.getStartTime();
                LocalTime shiftEnd = interval.getEndTime();

                // ✅ Cross-midnight shift handling
                if (shiftEnd.isBefore(shiftStart)) {
                    // Shift crosses midnight (e.g., 22:00 → 05:59)

                    if (currentTime.isAfter(LocalTime.MIDNIGHT) && currentTime.isBefore(shiftEnd)) {
                        // Current time is after midnight but before shift end → Today: 00:00 → Shift end
                        intervalStartDateTime = LocalDateTime.of(currentDate, LocalTime.MIDNIGHT);   // 00:00 AM
                        intervalEndDateTime = LocalDateTime.of(currentDate, shiftEnd);
                    } else {
                        // Current time before midnight or after shift start → Previous day shift start → Today shift end
                        intervalStartDateTime = LocalDateTime.of(currentDate.minusDays(1), shiftStart);
                        intervalEndDateTime = LocalDateTime.of(currentDate, shiftEnd);
                    }

                } else {
                    // ✅ Same-day shift handling
                    intervalStartDateTime = LocalDateTime.of(currentDate, shiftStart);
                    intervalEndDateTime = LocalDateTime.of(currentDate, shiftEnd);

                    // If current time is within the shift, set end time to now
                    if (currentTime.isAfter(shiftStart) && currentTime.isBefore(shiftEnd)) {
                        intervalEndDateTime = LocalDateTime.now();
                    }
                }

            } else {
                // ❌ No matching interval found
                ShiftRequest shiftReq = new ShiftRequest();
                shiftReq.setSite(group.get(0).getSite());

                intervalStartDateTime = getEarliestValidShiftStartDateTime(shiftReq);
                intervalEndDateTime = LocalDateTime.now();
                remove = true;
            }
        }


        // Fetch all tracked work centers for the given site
        List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(group.get(0).getSite());

// Initialize final manufactured time accumulator
        double manufacturedTime = 0.0;

// Create a reusable CycleTimeReq and set the site
        CycleTimeReq cycleTimeReq = new CycleTimeReq();
        cycleTimeReq.setSite(group.get(0).getSite());

// Loop through each work center
        for (WorkCenter wc : trackedWorkCenters) {
            // Set the current work center ID in the request
            cycleTimeReq.setWorkCenterId(wc.getWorkCenter());

            // Fetch cycle times for this work center
            List<CycleTime> cycleTimes = fetchCycleTimesByItems(cycleTimeReq);

            // Sum the manufactured time for matching records
            double wcManufacturedTime = cycleTimes.stream()
                    .filter(ct -> ct.getWorkCenterId().equalsIgnoreCase(wc.getWorkCenter())
                            && (ct.getItem() == null || ct.getItem().isEmpty())
                            && (ct.getItemVersion() == null || ct.getItemVersion().isEmpty())
                            && (ct.getResourceId() == null || ct.getResourceId().isEmpty()))
                    .mapToDouble(CycleTime::getManufacturedTime)
                    .sum();

            // Accumulate the work center's manufactured time into the final total
            manufacturedTime += wcManufacturedTime;
        }
        DowntimRequestForShift downtimRequestForShift=new DowntimRequestForShift();
        downtimRequestForShift.setSite(group.get(0).getSite());
        downtimRequestForShift.setDowntimeStart(intervalStartDateTime);
        downtimRequestForShift.setDowntimeEnd(intervalEndDateTime);
        downtimRequestForShift.setShiftId(group.get(0).getShiftId());
        CurrentShiftDetails shiftDetailsData=getBreakHours(downtimRequestForShift);
        Metrics metrics = calculateMetrics(group,  calculateSecondsFromStartToNow(intervalStartDateTime,intervalEndDateTime), (int) totalBadQty, (int) totalGoodQty, (int) totalActualQty,manufacturedTime,intervalStartDateTime , LocalDateTime.now(),shiftDetailsData,"SHIFT");

        ShiftDetails shiftDetails = new ShiftDetails();
        // Extract the shiftId and type from the shiftId string
        String shiftIdFull = group.get(0).getShiftId(); // Example: "ShiftBO:1000,General,GENERAL"
        String[] parts = shiftIdFull.split(",");

// Ensure we have enough parts before accessing indexes
        if (parts.length >= 3) {
            String shiftId = parts[parts.length - 1];  // Last value
            String shiftType = parts[parts.length - 2]; // Second last value
            shiftDetails.setShift(shiftId + "/" + shiftType);
        }
        if(remove){
            shiftDetails.setShift(null);
        }
        shiftDetails.setBatchNo(group.get(0).getBatchNumber());
        shiftDetails.setItem(group.get(0).getItem());
        shiftDetails.setActual((int) (metrics.getTotalGoodQty() + metrics.getTotalBadQty()));
        shiftDetails.setPlan((int) metrics.getTotalPlannedQty());
        shiftDetails.setRejection((int) metrics.getTotalBadQty());
        shiftDetails.setProductionTime((int) metrics.getProductionTimeSeconds());
        shiftDetails.setActualTime((int) metrics.getActualTimeSeconds());
        shiftDetails.setDowntimeDuration(metrics.getTotalDowntime());
        shiftDetails.setUnplannedDowntime(metrics.getTotalDowntime());
        shiftDetails.setPlannedDowntime((int) metrics.getPlannedDowntime());

        shiftDetails.setDowntimeReasons(null);
        shiftDetails.setGoodQualityCount((int) metrics.getTotalGoodQty());
        shiftDetails.setBadQualityCount(totalBadQty);
        shiftDetails.setEnergyUsage(DEFAULT_ENERGY_USAGE);
        shiftDetails.setPlannedCycletime(metrics.getPlannedCycleTime());
        shiftDetails.setActualCycleTime(metrics.getActualCycleTime());
        shiftDetails.setAvailability(metrics.getAvailability());
        shiftDetails.setPerformance(metrics.getPerformance());
        shiftDetails.setQuality(metrics.getQuality());
        shiftDetails.setOee(metrics.getOee());

        return shiftDetails;
    }

    private ShiftDetails calculateShiftDetails1(List<AggregatedOee> group,Map<String, Map<String, Double>> scrapqty,ShiftResponse shift) {
        double totalGoodQty = 0.0;
        double totalBadQty = 0.0;
        double totalActualQty = 0.0;
        Boolean remove=false;
// Iterate over the map and find matching work center
        for (Map.Entry<String, Map<String, Double>> quantities : scrapqty.entrySet()) {
            if (quantities.getKey().equalsIgnoreCase(group.get(0).getShiftId())) {
                totalGoodQty = quantities.getValue().getOrDefault("goodQty", 0.0);
                totalBadQty = quantities.getValue().getOrDefault("badQty", 0.0);
                totalActualQty = quantities.getValue().getOrDefault("totalQty", 0.0);
            }
        }


        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalDateTime intervalStartDateTime = LocalDateTime.now();
        LocalDateTime intervalEndDateTime=LocalDateTime.now();
        ShiftRequest shiftRequest= new ShiftRequest();
        shiftRequest.setShiftType("HANDLE");
        shiftRequest.setSite(group.get(0).getSite());
        shiftRequest.setShiftId(shift.getHandle());
        shift=new ShiftResponse();
        shift=getShiftDetail(shiftRequest);
// Step 1: Find the valid shift interval for today
        if (shift != null) {
            Optional<ShiftIntervals> matchingInterval = shift.getShiftIntervals().stream()
                    .filter(interval ->
                            !interval.getValidFrom().toLocalDate().isAfter(currentDate) &&  // ValidFrom <= Today
                                    !interval.getValidEnd().toLocalDate().isBefore(currentDate)    // ValidEnd >= Today
                    )
                    .findFirst();

            if (matchingInterval.isPresent()) {
                ShiftIntervals interval = matchingInterval.get();

                // Extract shift start and end times
                LocalTime shiftStart = interval.getStartTime();
                LocalTime shiftEnd = interval.getEndTime();

                // ✅ Cross-midnight shift handling
                if (shiftEnd.isBefore(shiftStart)) {
                    // Shift crosses midnight (e.g., 22:00 → 05:59)

                    if (currentTime.isAfter(LocalTime.MIDNIGHT) && currentTime.isBefore(shiftEnd)) {
                        // Current time is after midnight but before shift end → Today: 00:00 → Shift end
                        intervalStartDateTime = LocalDateTime.of(currentDate, LocalTime.MIDNIGHT);   // 00:00 AM
                        intervalEndDateTime = LocalDateTime.of(currentDate, shiftEnd);
                    } else {
                        // Current time before midnight or after shift start → Previous day shift start → Today shift end
                        intervalStartDateTime = LocalDateTime.of(currentDate.minusDays(1), shiftStart);
                        intervalEndDateTime = LocalDateTime.of(currentDate, shiftEnd);
                    }

                } else {
                    // ✅ Same-day shift handling
                    intervalStartDateTime = LocalDateTime.of(currentDate, shiftStart);
                    intervalEndDateTime = LocalDateTime.of(currentDate, shiftEnd);

                    // If current time is within the shift, set end time to now
                    if (currentTime.isAfter(shiftStart) && currentTime.isBefore(shiftEnd)) {
                        intervalEndDateTime = LocalDateTime.now();
                    }
                }

            } else {
                // ❌ No matching interval found
                ShiftRequest shiftReq = new ShiftRequest();
                shiftReq.setSite(group.get(0).getSite());

                intervalStartDateTime = getEarliestValidShiftStartDateTime(shiftReq);
                intervalEndDateTime = LocalDateTime.now();
                remove = true;
            }
        }


        // Fetch all tracked work centers for the given site
        List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(group.get(0).getSite());

// Initialize final manufactured time accumulator
        double manufacturedTime = 0.0;

// Create a reusable CycleTimeReq and set the site
        CycleTimeReq cycleTimeReq = new CycleTimeReq();
        cycleTimeReq.setSite(group.get(0).getSite());

// Loop through each work center
        for (WorkCenter wc : trackedWorkCenters) {
            // Set the current work center ID in the request
            cycleTimeReq.setWorkCenterId(wc.getWorkCenter());

            // Fetch cycle times for this work center
            List<CycleTime> cycleTimes = fetchCycleTimesByItems(cycleTimeReq);

            // Sum the manufactured time for matching records
            double wcManufacturedTime = cycleTimes.stream()
                    .filter(ct -> ct.getWorkCenterId().equalsIgnoreCase(wc.getWorkCenter())
                            && (ct.getItem() == null || ct.getItem().isEmpty())
                            && (ct.getItemVersion() == null || ct.getItemVersion().isEmpty())
                            && (ct.getResourceId() == null || ct.getResourceId().isEmpty()))
                    .mapToDouble(CycleTime::getManufacturedTime)
                    .sum();

            // Accumulate the work center's manufactured time into the final total
            manufacturedTime += wcManufacturedTime;
        }
        DowntimRequestForShift downtimRequestForShift=new DowntimRequestForShift();
        downtimRequestForShift.setSite(group.get(0).getSite());
        downtimRequestForShift.setDowntimeStart(intervalStartDateTime);
        downtimRequestForShift.setDowntimeEnd(intervalEndDateTime);
        downtimRequestForShift.setShiftId(group.get(0).getShiftId());
        CurrentShiftDetails shiftDetailsData=getBreakHours(downtimRequestForShift);
        Metrics metrics = calculateMetrics1(group,  calculateSecondsFromStartToNow(intervalStartDateTime,intervalEndDateTime), (int) totalBadQty, (int) totalGoodQty, (int) totalActualQty,manufacturedTime,intervalStartDateTime , LocalDateTime.now(),shiftDetailsData,"SHIFT");

        ShiftDetails shiftDetails = new ShiftDetails();
        // Extract the shiftId and type from the shiftId string
        String shiftIdFull = group.get(0).getShiftId(); // Example: "ShiftBO:1000,General,GENERAL"
        String[] parts = shiftIdFull.split(",");

// Ensure we have enough parts before accessing indexes
        if (parts.length >= 3) {
            String shiftId = parts[parts.length - 1];  // Last value
            String shiftType = parts[parts.length - 2]; // Second last value
            shiftDetails.setShift(shiftId + "/" + shiftType);
        }
        if(remove){
            shiftDetails.setShift(null);
        }
        shiftDetails.setBatchNo(group.get(0).getBatchNumber());
        shiftDetails.setItem(group.get(0).getItem());
        shiftDetails.setActual((int) (metrics.getTotalGoodQty() + metrics.getTotalBadQty()));
        shiftDetails.setPlan((int) metrics.getTotalPlannedQty());
        shiftDetails.setRejection((int) metrics.getTotalBadQty());
        shiftDetails.setProductionTime((int) metrics.getProductionTimeSeconds());
        shiftDetails.setActualTime((int) metrics.getActualTimeSeconds());
        shiftDetails.setDowntimeDuration(metrics.getTotalDowntime());
        shiftDetails.setUnplannedDowntime(metrics.getTotalDowntime());
        shiftDetails.setPlannedDowntime((int) metrics.getPlannedDowntime());

        shiftDetails.setDowntimeReasons(null);
        shiftDetails.setGoodQualityCount((int) metrics.getTotalGoodQty());
        shiftDetails.setBadQualityCount(totalBadQty);
        shiftDetails.setEnergyUsage(DEFAULT_ENERGY_USAGE);
        shiftDetails.setPlannedCycletime(metrics.getPlannedCycleTime());
        shiftDetails.setActualCycleTime(metrics.getActualCycleTime());
        shiftDetails.setAvailability(metrics.getAvailability());
        shiftDetails.setPerformance(metrics.getPerformance());
        shiftDetails.setQuality(metrics.getQuality());
        shiftDetails.setOee(metrics.getOee());

        return shiftDetails;
    }
    public LocalDateTime getEarliestValidShiftStartDateTime(ShiftRequest shiftReq) {
        List<ShiftResponse> allShifts = getAllShifts(shiftReq);
        LocalDate currentDate = LocalDate.now();
        LocalDateTime earliestStartDateTime = null;
        boolean hasShiftCoveringMidnight = false; // Flag for shifts that span 12 AM

        boolean isHandleProvided = shiftReq.getHandle() != null && !shiftReq.getHandle().isEmpty();

        for (ShiftResponse shift : allShifts) {
            // If a specific shift handle is provided, process only that shift
            if (isHandleProvided && !shiftReq.getHandle().equals(shift.getHandle())) {
                continue;
            }

            // Fetch detailed shift information
            ShiftRequest detailReq = new ShiftRequest();
            detailReq.setShiftType("HANDLE");
            detailReq.setSite(shift.getSite());
            detailReq.setShiftId(shift.getHandle());

            ShiftResponse detailedShift = getShiftDetail(detailReq);

            if (detailedShift == null || detailedShift.getShiftIntervals() == null) {
                continue; // Skip invalid shifts
            }

            for (ShiftIntervals interval : detailedShift.getShiftIntervals()) {
                LocalTime shiftStartTime = interval.getStartTime();
                LocalTime shiftEndTime = interval.getEndTime();

                // Map shift start time to today’s date
                LocalDateTime shiftStart = LocalDateTime.of(currentDate, shiftStartTime);
                LocalDateTime shiftEnd = LocalDateTime.of(currentDate, shiftEndTime);

                // Handle shifts that cross midnight
                if (shiftEndTime.isBefore(shiftStartTime) || shiftEndTime.equals(LocalTime.MIDNIGHT)) {
                    shiftEnd = shiftEnd.plusDays(1); // Shift continues into the next day
                }

                // If shift covers 12 AM, mark it
                if (shiftStartTime.isAfter(shiftEndTime) || shiftEndTime.equals(LocalTime.MIDNIGHT)) {
                    hasShiftCoveringMidnight = true;
                }

                // If a handle is provided, return immediately
                if (isHandleProvided) {
                    return shiftStart;
                }

                // Track the earliest valid shift start time (mapped to today’s date)
                if (earliestStartDateTime == null || shiftStart.isBefore(earliestStartDateTime)) {
                    earliestStartDateTime = shiftStart;
                }
            }
        }

        // 🔹 If any shift spans 12 AM, return 12:00 AM today
        if (hasShiftCoveringMidnight) {
            return LocalDateTime.of(currentDate, LocalTime.MIDNIGHT);
        }

        // Otherwise, return the earliest shift start time mapped to today
        return earliestStartDateTime;
    }








    private List<CycleTime> fetchCycleTimesByItems(CycleTimeReq cycleTimeReq) {
        return webClientBuilder.build()
                .post()
                .uri(getCycletimesByWorkcenter)
                .bodyValue(cycleTimeReq)
                .retrieve()
                .bodyToFlux(CycleTime.class)
                .collectList()
                .block();
    }

    private List<CycleTimePostgres> fetchCycleTimesByItemss(CycleTimeReq cycleTimeReq) {
        return webClientBuilder.build()
                .post()
                .uri(getCycletimesByitem)
                .bodyValue(cycleTimeReq)
                .retrieve()
                .bodyToFlux(CycleTimePostgres.class)
                .collectList()
                .block();
    }


    private WorkcenterDetails calculateWorkcenterDetails(List<Oee> group,Map<String, Map<String, Double>> scrapqty) {

        double totalGoodQty = 0.0;
        double totalBadQty = 0.0;
        double totalActualQty = 0.0;

// Iterate over the map and find matching work center
        for (Map.Entry<String, Map<String, Double>> quantities : scrapqty.entrySet()) {
            if (quantities.getKey().equalsIgnoreCase(group.get(0).getWorkcenterId())) {
                totalGoodQty = quantities.getValue().getOrDefault("goodQty", 0.0);
                totalBadQty = quantities.getValue().getOrDefault("badQty", 0.0);
                totalActualQty = quantities.getValue().getOrDefault("totalQty", 0.0);
            }
        }
        CycleTimeReq cycleTimeReq = new CycleTimeReq();
        cycleTimeReq.setSite(group.get(0).getSite());
        cycleTimeReq.setWorkCenterId(group.get(0).getWorkcenterId());
        Double ManufacturedTime = 0.0;
        List<CycleTime> cycleTimes = fetchCycleTimesByItems(cycleTimeReq);
        Double manufacturedTime = null; // Initialize to null

        for (CycleTime cycleTime : cycleTimes) {
            if (cycleTime.getWorkCenterId().equalsIgnoreCase(group.get(0).getWorkcenterId()) &&
                    (cycleTime.getItem() == null || cycleTime.getItem().isEmpty()) &&
                    (cycleTime.getItemVersion() == null || cycleTime.getItemVersion().isEmpty()) &&
                    (cycleTime.getResourceId() == null || cycleTime.getResourceId().isEmpty())) {

                manufacturedTime = cycleTime.getManufacturedTime();
                break; // Exit loop once found
            }
        }


        double quantityProduced = 0.0; // Keep type consistency with double
        ShiftRequest shiftreq= new ShiftRequest();
        shiftreq.setSite(group.get(0).getSite());
        LocalDateTime intervalStartDateTime=  getEarliestValidShiftStartDateTime(shiftreq);

        DowntimRequestForShift downtimRequestForShift=new DowntimRequestForShift();
        downtimRequestForShift.setSite(group.get(0).getSite());
        downtimRequestForShift.setDowntimeStart(intervalStartDateTime);
        downtimRequestForShift.setDowntimeEnd(LocalDateTime.now());
        List<String> shiftIds = allShifts.stream()
                .map(ShiftResponse::getShiftId)  // Extract shiftId
                .filter(Objects::nonNull) // Remove null values
                .distinct() // Ensure unique shiftIds
                .collect(Collectors.toList()); // Convert to List

        downtimRequestForShift.setShiftIds(shiftIds);
        CurrentShiftDetails shiftDetails=getBreakHours(downtimRequestForShift);
        Metrics metrics = calculateMetrics(group, calculateSecondsFromStartToNow(intervalStartDateTime,null),
                (int) totalBadQty, (int) totalGoodQty, (int) totalActualQty,manufacturedTime,intervalStartDateTime,LocalDateTime.now(),shiftDetails,"WC");


        WorkcenterDetails workcenterDetails = new WorkcenterDetails();
        workcenterDetails.setWorkcenter(group.get(0).getWorkcenterId());
        workcenterDetails.setActual((int) (metrics.getTotalGoodQty() + metrics.getTotalBadQty()));
        workcenterDetails.setPlan(metrics.getTotalPlannedQty());
        workcenterDetails.setRejection((int) metrics.getTotalBadQty());
        workcenterDetails.setDowntimeDuration(metrics.getTotalDowntime());
        workcenterDetails.setGoodQualityCount((int) metrics.getTotalGoodQty());
        workcenterDetails.setBadQualityCount(totalBadQty);
        workcenterDetails.setEnergyUsage(DEFAULT_ENERGY_USAGE);
        workcenterDetails.setProductionTime((int) metrics.getProductionTimeSeconds());
        workcenterDetails.setActualTime((int) metrics.getActualTimeSeconds());
        workcenterDetails.setPlannedDowntime((int) metrics.getPlannedDowntime());

        workcenterDetails.setPlannedCycletime(metrics.getPlannedCycleTime());
        workcenterDetails.setActualCycleTime(metrics.getActualCycleTime());
        workcenterDetails.setAvailability(metrics.getAvailability());
        workcenterDetails.setPerformance(metrics.getPerformance());
        workcenterDetails.setQuality(metrics.getQuality());
        workcenterDetails.setOee(metrics.getOee());

        return workcenterDetails;
    }

    private WorkcenterDetails calculateWorkcenterDetails1(List<AggregatedOee> group,Map<String, Map<String, Double>> scrapqty) {

        double totalGoodQty = 0.0;
        double totalBadQty = 0.0;
        double totalActualQty = 0.0;

// Iterate over the map and find matching work center
        for (Map.Entry<String, Map<String, Double>> quantities : scrapqty.entrySet()) {
            if (quantities.getKey().equalsIgnoreCase(group.get(0).getWorkcenterId())) {
                totalGoodQty = quantities.getValue().getOrDefault("goodQty", 0.0);
                totalBadQty = quantities.getValue().getOrDefault("badQty", 0.0);
                totalActualQty = quantities.getValue().getOrDefault("totalQty", 0.0);
            }
        }
        CycleTimeReq cycleTimeReq = new CycleTimeReq();
        cycleTimeReq.setSite(group.get(0).getSite());
        cycleTimeReq.setWorkCenterId(group.get(0).getWorkcenterId());
        Double ManufacturedTime = 0.0;
        List<CycleTime> cycleTimes = fetchCycleTimesByItems(cycleTimeReq);
        Double manufacturedTime = null; // Initialize to null

        for (CycleTime cycleTime : cycleTimes) {
            if (cycleTime.getWorkCenterId().equalsIgnoreCase(group.get(0).getWorkcenterId()) &&
                    (cycleTime.getItem() == null || cycleTime.getItem().isEmpty()) &&
                    (cycleTime.getItemVersion() == null || cycleTime.getItemVersion().isEmpty()) &&
                    (cycleTime.getResourceId() == null || cycleTime.getResourceId().isEmpty())) {

                manufacturedTime = cycleTime.getManufacturedTime();
                break; // Exit loop once found
            }
        }


        double quantityProduced = 0.0; // Keep type consistency with double
        ShiftRequest shiftreq= new ShiftRequest();
        shiftreq.setSite(group.get(0).getSite());
        LocalDateTime intervalStartDateTime=  getEarliestValidShiftStartDateTime(shiftreq);

        DowntimRequestForShift downtimRequestForShift=new DowntimRequestForShift();
        downtimRequestForShift.setSite(group.get(0).getSite());
        downtimRequestForShift.setDowntimeStart(intervalStartDateTime);
        downtimRequestForShift.setDowntimeEnd(LocalDateTime.now());
        List<String> shiftIds = allShifts.stream()
                .map(ShiftResponse::getShiftId)  // Extract shiftId
                .filter(Objects::nonNull) // Remove null values
                .distinct() // Ensure unique shiftIds
                .collect(Collectors.toList()); // Convert to List

        downtimRequestForShift.setShiftIds(shiftIds);
        CurrentShiftDetails shiftDetails=getBreakHours(downtimRequestForShift);
        Metrics metrics = calculateMetrics1(group, calculateSecondsFromStartToNow(intervalStartDateTime,null),
                (int) totalBadQty, (int) totalGoodQty, (int) totalActualQty,manufacturedTime,intervalStartDateTime,LocalDateTime.now(),shiftDetails,"WC");


        WorkcenterDetails workcenterDetails = new WorkcenterDetails();
        workcenterDetails.setWorkcenter(group.get(0).getWorkcenterId());
        workcenterDetails.setActual((int) (metrics.getTotalGoodQty() + metrics.getTotalBadQty()));
        workcenterDetails.setPlan(metrics.getTotalPlannedQty());
        workcenterDetails.setRejection((int) metrics.getTotalBadQty());
        workcenterDetails.setDowntimeDuration(metrics.getTotalDowntime());
        workcenterDetails.setGoodQualityCount((int) metrics.getTotalGoodQty());
        workcenterDetails.setBadQualityCount(totalBadQty);
        workcenterDetails.setEnergyUsage(DEFAULT_ENERGY_USAGE);
        workcenterDetails.setProductionTime((int) metrics.getProductionTimeSeconds());
        workcenterDetails.setActualTime((int) metrics.getActualTimeSeconds());
        workcenterDetails.setPlannedDowntime((int) metrics.getPlannedDowntime());

        workcenterDetails.setPlannedCycletime(metrics.getPlannedCycleTime());
        workcenterDetails.setActualCycleTime(metrics.getActualCycleTime());
        workcenterDetails.setAvailability(metrics.getAvailability());
        workcenterDetails.setPerformance(metrics.getPerformance());
        workcenterDetails.setQuality(metrics.getQuality());
        workcenterDetails.setOee(metrics.getOee());

        return workcenterDetails;
    }

    private ResourceDetails calculateResourceDetails(List<Oee> group) {
        ShiftRequest shiftreq= new ShiftRequest();
        shiftreq.setSite(group.get(0).getSite());
        LocalDateTime intervalStartDateTime=  getEarliestValidShiftStartDateTime(shiftreq);

        // Convert Instant.now() to LocalDateTime using the system default time zone
        Instant nowInstant = Instant.now();
        LocalDateTime now = LocalDateTime.ofInstant(nowInstant, ZoneId.systemDefault());

        // Calculate duration in seconds
        long totalTimeSeconds = Duration.between(intervalStartDateTime, now).getSeconds();
        DowntimRequestForShift downtimRequestForShift=new DowntimRequestForShift();
        downtimRequestForShift.setSite(group.get(0).getSite());
        downtimRequestForShift.setDowntimeStart(intervalStartDateTime);
        downtimRequestForShift.setDowntimeEnd(now);

        List<String> shiftIds = group.stream()
                .map(Oee::getShiftId)  // Extract shiftId
                .filter(Objects::nonNull) // Remove null values
                .distinct() // Ensure unique shiftIds
                .collect(Collectors.toList()); // Convert to List
        downtimRequestForShift.setShiftIds(shiftIds);
        CurrentShiftDetails shiftDetails=getBreakHours(downtimRequestForShift);
        Metrics metrics = calculateMetrics(group, calculateSecondsFromStartToNow(intervalStartDateTime,null),0,0,0,0.0,intervalStartDateTime,LocalDateTime.now(),shiftDetails,"RESC");
        Boolean status= false;
        List<Map<String, Object>> resourceStatuses=getCurrentStatus(group.get(0).getSite());
        for (Map<String, Object> statusMap : resourceStatuses) {
            String resourceId = (String) statusMap.get("resourceid");
            if (group.get(0).getResourceId().equals(resourceId)) {
                status = (Boolean) statusMap.get("status");
                break;  // Exit once you find a match
            }
        }
        ResourceDetails resourceDetails = new ResourceDetails();
        resourceDetails.setResource(group.get(0).getResourceId());
        resourceDetails.setActual((int) (metrics.getTotalGoodQty() + metrics.getTotalBadQty()));
        resourceDetails.setPlan((int) metrics.getTotalPlannedQty());
        resourceDetails.setRejection((int) metrics.getTotalBadQty());
        resourceDetails.setDowntimeDuration(metrics.getTotalDowntime());
        resourceDetails.setGoodQualityCount((int) metrics.getTotalGoodQty());
        resourceDetails.setBadQualityCount((int) metrics.getTotalBadQty());
        resourceDetails.setEnergyUsage(DEFAULT_ENERGY_USAGE);
        resourceDetails.setProductionTime((int) metrics.getProductionTimeSeconds());
        resourceDetails.setPlannedDowntime(shiftDetails.getBreaktime());
        resourceDetails.setActualTime((int) metrics.getActualTimeSeconds());
        resourceDetails.setDowntime(status);
        resourceDetails.setAvailability(metrics.getAvailability());
        resourceDetails.setPerformance(metrics.getPerformance());
        resourceDetails.setPlannedDowntime((int) metrics.getPlannedDowntime());


        resourceDetails.setQuality(metrics.getQuality());
        resourceDetails.setOee(metrics.getOee());
        resourceDetails.setActualCycleTime(metrics.getActualCycleTime());
        resourceDetails.setPlannedCycletime(metrics.getPlannedCycleTime());
        return resourceDetails;
    }

    private List<BatchDetails> calculateBatchDetails(Map<String, List<Oee>> groupedData,
                                                     long totalTimeSeconds,
                                                     Map<String, Map<String, Double>> scrapqty) {
        // Process each group (the key might be batchNumber or operation depending on the grouping)
        return groupedData.values().stream()
                .map(group -> calculateBatchDetails(group, totalTimeSeconds, scrapqty))
                .collect(Collectors.toList());
    }


    private BatchDetails calculateBatchDetails(List<Oee> group,
                                               long totalTimeSeconds,
                                               Map<String, Map<String, Double>> scrapqty) {
        ShiftRequest shiftreq= new ShiftRequest();
        shiftreq.setSite(group.get(0).getSite());
        // Assume that all Oee records in the group share the same batch number.
        LocalDateTime intervalStartDateTime = getEarliestIntervalStartDateTime(group)
                .isBefore(getEarliestValidShiftStartDateTime(shiftreq))
                ? getEarliestValidShiftStartDateTime(shiftreq)
                : getEarliestIntervalStartDateTime(group);

        LocalDateTime intervalEndDateTime = getLatestIntervalEndDateTime(group);
        group = group.stream()
                .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty())



                .filter(oee -> oee.getWorkcenterId() != null && !oee.getWorkcenterId().isEmpty())
                .filter(oee -> oee.getEventTypeOfPerformance().equalsIgnoreCase("ScrapSFC") ||
                        oee.getEventTypeOfPerformance().equalsIgnoreCase("doneSfcBatch"))
                .collect(Collectors.toList());


        String batchNo = group.get(0).getBatchNumber();

        // Look up scrap quantities for the batch.
        // (If keys are stored in a case-insensitive manner, you might convert batchNo to lower case or adjust as needed.)
        Map<String, Double> qtyMap = scrapqty.get(batchNo.toLowerCase());
        double totalGoodQty = (qtyMap != null) ? qtyMap.getOrDefault("goodQty", 0.0) : 0.0;
        double totalBadQty = (qtyMap != null) ? qtyMap.getOrDefault("badQty", 0.0) : 0.0;
        double totalActualQty = (qtyMap != null) ? qtyMap.getOrDefault("totalQty", 0.0) : 0.0;
        CycleTimeReq cycleTimeReq = new CycleTimeReq();
        cycleTimeReq.setSite(group.get(0).getSite());
        cycleTimeReq.setWorkCenterId(group.get(0).getWorkcenterId());
        Double ManufacturedTime = 0.0;
        List<CycleTimePostgres> cycleTimes = fetchCycleTimesByItemss(cycleTimeReq);
        Double manufacturedTime = null; // Initialize to null

        for (CycleTimePostgres cycleTime : cycleTimes) {
            if (cycleTime.getWorkcenterId().equalsIgnoreCase(group.get(0).getWorkcenterId()) &&
                    (cycleTime.getItem() != null || cycleTime.getItem().isEmpty()) &&
                    (cycleTime.getItemVersion() != null || cycleTime.getItemVersion().isEmpty()) &&
                    (cycleTime.getResourceId() != null || cycleTime.getResourceId().isEmpty())&&cycleTime.getItem().equalsIgnoreCase(group.get(0).getItem())&&cycleTime.getItemVersion().equalsIgnoreCase(group.get(0).getItemVersion())) {

                manufacturedTime = cycleTime.getManufacturedTime();
                break; // Exit loop once found
            }
        }
        DowntimRequestForShift downtimRequestForShift=new DowntimRequestForShift();
        downtimRequestForShift.setSite(group.get(0).getSite());
        downtimRequestForShift.setDowntimeStart(intervalStartDateTime);
        downtimRequestForShift.setDowntimeEnd(intervalEndDateTime);
        List<String> shiftIds = group.stream()
                .map(Oee::getShiftId)  // Extract shiftId
                .filter(Objects::nonNull) // Remove null values
                .distinct() // Ensure unique shiftIds
                .collect(Collectors.toList()); // Convert to List
        CurrentShiftDetails shiftDetails=getBreakHours(downtimRequestForShift);
        // Calculate metrics. (Assuming calculateTotalTimeSeconds(group) is a valid method call.)
        Metrics metrics = calculateMetrics(group, calculateSecondsFromStartToNow(intervalStartDateTime,null),
                (int) totalBadQty, (int) totalGoodQty, (int) totalActualQty,manufacturedTime,intervalStartDateTime,intervalEndDateTime,shiftDetails,"BATCH");

        // Create and populate BatchDetails.
        BatchDetails batchDetails = new BatchDetails();
        batchDetails.setBatchNo(batchNo);
        batchDetails.setItem(group.get(0).getItem());
        batchDetails.setActual((int) (metrics.getTotalGoodQty() + metrics.getTotalBadQty()));
        batchDetails.setPlan(Integer.parseInt(group.get(0).getBatchSize()));
        batchDetails.setRejection((int) metrics.getTotalBadQty());
        batchDetails.setDowntimeDuration((int) metrics.getTotalDowntime());
        batchDetails.setGoodQualityCount((int) metrics.getTotalGoodQty());
        batchDetails.setBadQualityCount((int) totalBadQty);
        batchDetails.setEnergyUsage(DEFAULT_ENERGY_USAGE);
        batchDetails.setProductionTime((int) metrics.getProductionTimeSeconds());
        batchDetails.setActualTime((int) metrics.getActualTimeSeconds());
        batchDetails.setUnplannedDowntime((int) metrics.getTotalDowntime());
        batchDetails.setPlannedDowntime((int) metrics.getPlannedDowntime()*60);

        batchDetails.setAvailability(metrics.getAvailability());
        batchDetails.setPerformance(metrics.getPerformance());
        batchDetails.setQuality(metrics.getQuality());
        batchDetails.setOee(metrics.getOee());

        batchDetails.setActualCycleTime(metrics.getActualCycleTime());
        batchDetails.setPlannedCycletime(metrics.getPlannedCycleTime());

        return batchDetails;
    }

    private Metrics calculateMetrics(List<Oee> group, long totalTimeSeconds, int scrap, int goodQty, int actualQty,
                                     Double manufacturedTime, LocalDateTime startDateTime, LocalDateTime endDateTime,
                                     CurrentShiftDetails shiftDetails, String type) {
        List<Oee> filteredGroup = group.stream()
                .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty())

                .collect(Collectors.toList());

        // Calculate total quantities with fallback to group values
        double totalBadQty = (scrap > 0) ? scrap : group.stream().mapToDouble(Oee::getBadQty).sum();
        double totalGoodQty = (goodQty > 0) ? goodQty : group.stream().mapToDouble(Oee::getGoodQty).sum();
        //  double totalPlannedQty = (actualQty > 0) ? actualQty : group.stream().mapToDouble(Oee::getPlan).sum();
        double totalPlannedQty=0.0;
        double totalDowntime = 0.0;
        if( "BATCH".equalsIgnoreCase(type)){
            totalTimeSeconds = Duration.between(startDateTime, endDateTime).toSeconds();
        }

        if ("WC".equalsIgnoreCase(type) || "BATCH".equalsIgnoreCase(type) ) {
            // WorkCenter: Compute total downtime for all associated resources
            String workCenterId = group.get(0).getWorkcenterId();
            List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(group.get(0).getSite());

            // Find all resources associated with this WorkCenter
            List<String> resourceIds = trackedWorkCenters.stream()
                    .filter(wc -> wc.getWorkCenter().equals(workCenterId))
                    .flatMap(wc -> wc.getAssociationList().stream())
                    .filter(assoc -> "Resource".equalsIgnoreCase(assoc.getType()))
                    .map(Association::getAssociateId)
                    .collect(Collectors.toList());
            totalTimeSeconds *= resourceIds.size();
            // Compute total downtime for all resources under this WorkCenter
            for (String resourceId : resourceIds) {
                DowntimeRequest downtimeRequest = new DowntimeRequest();
                downtimeRequest.setSite(group.get(0).getSite());
                downtimeRequest.setResourceId(resourceId);
                downtimeRequest.setStartDateTime(startDateTime);
                downtimeRequest.setEndDateTime(endDateTime);

                totalDowntime += getDownTimeSummary(downtimeRequest).getTotalDowntime();
            }

        } else if ("RESC".equalsIgnoreCase(type)) {
            // Resource: Compute downtime only for the single resource
            DowntimeRequest downtimeRequest = new DowntimeRequest();
            downtimeRequest.setSite(group.get(0).getSite());
            downtimeRequest.setResourceId(group.get(0).getResourceId());
            downtimeRequest.setStartDateTime(startDateTime);
            downtimeRequest.setEndDateTime(endDateTime);

            totalDowntime = getDownTimeSummary(downtimeRequest).getTotalDowntime();

        } else if ("SITE".equalsIgnoreCase(type) || "SHIFT".equalsIgnoreCase(type)) {
            // Site: Compute downtime for ALL WorkCenters and their resources
            List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(group.get(0).getSite());
            int totalResourceCount = 0;

            for (WorkCenter workCenter : trackedWorkCenters) {
                List<String> resourceIds = workCenter.getAssociationList().stream()
                        .filter(assoc -> "Resource".equalsIgnoreCase(assoc.getType()))
                        .map(Association::getAssociateId)
                        .collect(Collectors.toList());
                totalResourceCount += resourceIds.size(); // ✅ summing resource count

                for (String resourceId : resourceIds) {
                    DowntimeRequest downtimeRequest = new DowntimeRequest();
                    downtimeRequest.setSite(group.get(0).getSite());
                    downtimeRequest.setResourceId(resourceId);
                    downtimeRequest.setStartDateTime(startDateTime);
                    downtimeRequest.setEndDateTime(endDateTime);

                    totalDowntime += getDownTimeSummary(downtimeRequest).getTotalDowntime();
                }
            }
            totalTimeSeconds *= totalResourceCount;
        }


        // Compute planned downtime and actual planned production time
        double plannedDowntime = shiftDetails.getBreaktime()*60;
        long actualPlannedProduction = totalTimeSeconds - (long) (totalDowntime + plannedDowntime);
        if (totalPlannedQty == 0) {
            CycleTimeReq cycleTimeReq = new CycleTimeReq();
            cycleTimeReq.setSite(group.get(0).getSite());
            cycleTimeReq.setResourceId(group.get(0).getResourceId());

            List<CycleTime> cycleTimes = fetchCycleTimes(cycleTimeReq);

            if (!cycleTimes.isEmpty()) {
                double averageCycleTime = cycleTimes.stream()
                        .mapToDouble(CycleTime::getCycleTime) // Assuming CycleTime has getCycleTimeValue()
                        .average()
                        .orElse(0.0); // Default to 0 if no valid values

                if (averageCycleTime > 0) {
                    totalPlannedQty = (int) (actualPlannedProduction / averageCycleTime);
                }
            }
        }

        // Recalculate total planned quantity if manufactured time is provided
        if (manufacturedTime != null && manufacturedTime > 0) {
            totalPlannedQty = totalTimeSeconds / manufacturedTime;
        }
        double actualCycleTime=0.0;
        if("BATCH".equalsIgnoreCase(type)){
            if(group.get(0).getBatchSize()!=null&&!group.get(0).getBatchSize().isEmpty()) {
                totalPlannedQty = Double.parseDouble(group.get(0).getBatchSize());
            }
            actualCycleTime=getCycleTimeForBatch(group.get(0).getBatchNumber(),group.get(0).getSite());
        }
        else{
            actualCycleTime = (totalGoodQty + totalBadQty) > 0 ? (double) totalTimeSeconds / (totalGoodQty + totalBadQty) : 0.0;

        }
        // === Calculate Cycle Times ===
        double plannedCycleTime = (totalPlannedQty > 0) ? (double) totalTimeSeconds / totalPlannedQty : 0.0;

        // === Calculate OEE Components ===
        double availability = (totalTimeSeconds > 0) ? ((double) totalTimeSeconds / totalTimeSeconds) * 100 : 0.0;
        double performance = (totalPlannedQty > 0) ? ((totalGoodQty + totalBadQty) / totalPlannedQty) * 100 : 0.0;
        double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;
        double oee = (availability * performance * quality) / 10000;


        // Return metrics with calculated values
        return new Metrics(totalTimeSeconds, totalTimeSeconds, totalDowntime, totalGoodQty, totalBadQty, totalPlannedQty,
                availability, performance, quality, oee, actualCycleTime, plannedCycleTime,
                actualPlannedProduction, plannedDowntime);
    }

    private Metrics calculateMetrics1(List<AggregatedOee> group, long totalTimeSeconds, int scrap, int goodQty, int actualQty,
                                      Double manufacturedTime, LocalDateTime startDateTime, LocalDateTime endDateTime,
                                      CurrentShiftDetails shiftDetails, String type) {
        List<AggregatedOee> filteredGroup = group.stream()
                .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().isEmpty())

                .collect(Collectors.toList());

        // Calculate total quantities with fallback to group values
        double totalBadQty = (scrap > 0) ? scrap : group.stream().mapToDouble(AggregatedOee::getTotalBadQuantity).sum();
        double totalGoodQty = (goodQty > 0) ? goodQty : group.stream().mapToDouble(AggregatedOee::getTotalGoodQuantity).sum();
        //  double totalPlannedQty = (actualQty > 0) ? actualQty : group.stream().mapToDouble(Oee::getPlan).sum();
        double totalPlannedQty=0.0;
        double totalDowntime = 0.0;
        if( "BATCH".equalsIgnoreCase(type)){
            totalTimeSeconds = Duration.between(startDateTime, endDateTime).toSeconds();
        }

        if ("WC".equalsIgnoreCase(type) || "BATCH".equalsIgnoreCase(type) ) {
            // WorkCenter: Compute total downtime for all associated resources
            String workCenterId = group.get(0).getWorkcenterId();
            List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(group.get(0).getSite());

            // Find all resources associated with this WorkCenter
            List<String> resourceIds = trackedWorkCenters.stream()
                    .filter(wc -> wc.getWorkCenter().equals(workCenterId))
                    .flatMap(wc -> wc.getAssociationList().stream())
                    .filter(assoc -> "Resource".equalsIgnoreCase(assoc.getType()))
                    .map(Association::getAssociateId)
                    .collect(Collectors.toList());

            // Compute total downtime for all resources under this WorkCenter
            for (String resourceId : resourceIds) {
                DowntimeRequest downtimeRequest = new DowntimeRequest();
                downtimeRequest.setSite(group.get(0).getSite());
                downtimeRequest.setResourceId(resourceId);
                downtimeRequest.setStartDateTime(startDateTime);
                downtimeRequest.setEndDateTime(endDateTime);

                totalDowntime += getDownTimeSummary(downtimeRequest).getTotalDowntime();
            }

        } else if ("RESC".equalsIgnoreCase(type)) {
            // Resource: Compute downtime only for the single resource
            DowntimeRequest downtimeRequest = new DowntimeRequest();
            downtimeRequest.setSite(group.get(0).getSite());
            downtimeRequest.setResourceId(group.get(0).getResourceId());
            downtimeRequest.setStartDateTime(startDateTime);
            downtimeRequest.setEndDateTime(endDateTime);

            totalDowntime = getDownTimeSummary(downtimeRequest).getTotalDowntime();

        } else if ("SITE".equalsIgnoreCase(type) || "SHIFT".equalsIgnoreCase(type)) {
            // Site: Compute downtime for ALL WorkCenters and their resources
            List<WorkCenter> trackedWorkCenters = fetchTrackedWorkCenters(group.get(0).getSite());

            for (WorkCenter workCenter : trackedWorkCenters) {
                List<String> resourceIds = workCenter.getAssociationList().stream()
                        .filter(assoc -> "Resource".equalsIgnoreCase(assoc.getType()))
                        .map(Association::getAssociateId)
                        .collect(Collectors.toList());

                for (String resourceId : resourceIds) {
                    DowntimeRequest downtimeRequest = new DowntimeRequest();
                    downtimeRequest.setSite(group.get(0).getSite());
                    downtimeRequest.setResourceId(resourceId);
                    downtimeRequest.setStartDateTime(startDateTime);
                    downtimeRequest.setEndDateTime(endDateTime);

                    totalDowntime += getDownTimeSummary(downtimeRequest).getTotalDowntime();
                }
            }
        }


        // Compute planned downtime and actual planned production time
        double plannedDowntime = shiftDetails.getBreaktime()*60;
        long actualPlannedProduction = totalTimeSeconds - (long) (totalDowntime + plannedDowntime);
        if (totalPlannedQty == 0) {
            CycleTimeReq cycleTimeReq = new CycleTimeReq();
            cycleTimeReq.setSite(group.get(0).getSite());
            cycleTimeReq.setResourceId(group.get(0).getResourceId());

            List<CycleTime> cycleTimes = fetchCycleTimes(cycleTimeReq);

            if (!cycleTimes.isEmpty()) {
                double averageCycleTime = cycleTimes.stream()
                        .mapToDouble(CycleTime::getCycleTime) // Assuming CycleTime has getCycleTimeValue()
                        .average()
                        .orElse(0.0); // Default to 0 if no valid values

                if (averageCycleTime > 0) {
                    totalPlannedQty = (int) (actualPlannedProduction / averageCycleTime);
                }
            }
        }

        // Recalculate total planned quantity if manufactured time is provided
        if (manufacturedTime != null && manufacturedTime > 0) {
            totalPlannedQty = totalTimeSeconds / manufacturedTime;
        }
        double actualCycleTime=0.0;
        if("BATCH".equalsIgnoreCase(type)){
            if (group.get(0).getTotalQuantity() != null) {
                totalPlannedQty = group.get(0).getTotalQuantity();
            }
            actualCycleTime=getCycleTimeForBatch(group.get(0).getBatchNumber(),group.get(0).getSite());
        }
        else{
            actualCycleTime = (totalGoodQty + totalBadQty) > 0 ? (double) totalTimeSeconds / (totalGoodQty + totalBadQty) : 0.0;

        }
        // === Calculate Cycle Times ===
        double plannedCycleTime = (totalPlannedQty > 0) ? (double) totalTimeSeconds / totalPlannedQty : 0.0;

        // === Calculate OEE Components ===
        double availability = (totalTimeSeconds > 0) ? ((double) totalTimeSeconds / totalTimeSeconds) * 100 : 0.0;
        double performance = (totalPlannedQty > 0) ? ((totalGoodQty + totalBadQty) / totalPlannedQty) * 100 : 0.0;
        double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;
        double oee = (availability * performance * quality) / 10000;


        // Return metrics with calculated values
        return new Metrics(totalTimeSeconds, totalTimeSeconds, totalDowntime, totalGoodQty, totalBadQty, totalPlannedQty,
                availability, performance, quality, oee, actualCycleTime, plannedCycleTime,
                actualPlannedProduction, plannedDowntime);
    }

    @Override
    public PerformanceByDowntimeResponse getPerformanceDowntime(OeeFilterRequest request) {

        Map<String, Object> queryParameters = new HashMap<>();

        String sql = buildOEEPerformanceDowntimeQuery(queryParameters, request);

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceByDowntimeResponse response = new PerformanceByDowntimeResponse();

            List<PerformanceByDowntimeResponse.DowntimeData> downtimeList = results.stream()
                    .filter(result -> result[0] != null)
                    .map(row -> {
                        String item = row[0].toString();
                        String reason = row[1] != null ? row[1].toString() : "";

                        double totalDowntime = (row[2] != null) ? ((Number) row[2]).doubleValue() : 0.0;
                        double performancePercentage = (row[3] != null) ? ((Number) row[3]).doubleValue() : 0.0;

                        if (performancePercentage == 0.0 && totalDowntime == 0.0) return null;

                        return new PerformanceByDowntimeResponse.DowntimeData(reason, item, totalDowntime, performancePercentage);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            response.setDowntimeAnalysis(downtimeList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error calculating performance downtime", e);
        }
    }

    @Override
    public AvailabilityByDownTimeResponse getAvailabilityDowntime(OeeFilterRequest request) {

        Map<String, Object> queryParameters = new HashMap<>();

        String sql = buildOEEByAvailabilityDowntimeQuery(queryParameters, request);

        try {
            Query query = entityManager.createNativeQuery(sql);
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();
            AvailabilityByDownTimeResponse response = new AvailabilityByDownTimeResponse();
            List<AvailabilityByDownTimeResponse.DownTimeReason> availabilityList = new ArrayList<>();

            for (Object[] row : results) {
                String machine = (row[0] != null) ? row[0].toString() : "";
                String reason = (row[1] != null) ? row[1].toString() : "";

                double totalDowntime = (row[2] != null) ? ((Number) row[2]).doubleValue() : 0.0;

//                LocalDateTime intervalStartTime = (row[3] != null) ? ((java.sql.Timestamp) row[3]).toLocalDateTime() : null;
//                LocalDateTime intervalEndTime = (row[4] != null) ? ((java.sql.Timestamp) row[4]).toLocalDateTime() : null;
//
//                // Calculate total time in seconds
//                long totalTimeSeconds = (intervalStartTime != null && intervalEndTime != null) ?
//                        Duration.between(intervalStartTime, intervalEndTime).getSeconds() : 0L;
//
//                long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
//                long actualTimeSeconds = totalTimeSeconds;
//
//                // Calculate Availability
//                double availabilityPercentage = (productionTimeSeconds > 0) ?
//                        ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;

                // Round to two decimal places
                double availabilityPercentage = (row[3] != null) ? ((Number) row[3]).doubleValue() : 0.0;

                // Avoid adding 0 values
                if (availabilityPercentage != 0.0 || totalDowntime != 0.0) {
                    availabilityList.add(new AvailabilityByDownTimeResponse.DownTimeReason(reason, machine, totalDowntime, Math.round(availabilityPercentage * 100.0) / 100.0));
                }
            }

            response.setDowntimeReasons(availabilityList);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return new AvailabilityByDownTimeResponse(Collections.emptyList());
        }
    }

    public Map<String, Double> getTotalDowntimeByResource(List<Oee> oeeList) {
        if (oeeList == null || oeeList.isEmpty()) {
            return Collections.emptyMap();
        }
        return oeeList.stream()
                .filter(Objects::nonNull)
                .filter(oee -> oee.getResourceId() != null && !oee.getResourceId().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        Oee::getResourceId,
                        Collectors.summingDouble(Oee::getTotalDowntime)
                ));
    }

    @Override
    public ScrapAndReworkTrendResponse getScrapAndReworkTrend(OeeFilterRequest request) {

        String eventTypeOfPerformance = null;
        String eventSource = null;
        if((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null) {
            eventTypeOfPerformance = "WORKCENTER";
            eventSource = request.getEventSource() + "_DONE";
        } else {
            eventTypeOfPerformance = "RESOURCE";
            eventSource = request.getEventSource() + "_COMPLETE";
        }

        Map<String, Object> queryParameters = new HashMap<>();

        String queryStr = buildOEEScrapAnReworkTrendQuery(queryParameters, request, eventTypeOfPerformance, eventSource);

        Query query = entityManager.createNativeQuery(queryStr);
        queryParameters.forEach(query::setParameter);

        List<Object[]> results = query.getResultList();

        List<ScrapAndReworkTrendResponse.ScrapAndReworkTrend> trends = results.stream()
                .map(result -> {
                    LocalDateTime dateTime = result[0] != null ? ((Timestamp) result[0]).toLocalDateTime() : null;
                    double scrapValue = result[1] != null ? ((Number) result[1]).longValue() : 0.0;
                    double quality = result[2] != null ? Math.round(((Number) result[2]).doubleValue() * 100.0) / 100.0 : 0.0;

                    return new ScrapAndReworkTrendResponse.ScrapAndReworkTrend(
                            dateTime != null ? dateTime.format(dateFormatter) : "",
                            scrapValue,
                            quality
                    );
                })
                .filter(trend -> trend.getScrapValue() > 0)
                .collect(Collectors.toList());

        return ScrapAndReworkTrendResponse.builder()
                .scrapAndReworkTrends(trends)
                .build();
    }

    private long fetchShiftDetails(List<String> shiftHandles) {
        // Retrieve the list of shift handles from the request

        // Make the web call and deserialize the response as Map<String, Long>
        Map<String, Long> response = webClientBuilder.build()
                .post()
                .uri(getPlannedProductionTimes)
                .bodyValue(shiftHandles)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Long>>() {})
                .block();

        // Combine the values from the response (e.g., summing them) and return the result.
        // If the response is null or empty, return 0L.
        return (response != null)
                ? response.values().stream().mapToLong(Long::longValue).sum()
                : 0L;
    }
    private CurrentShiftDetails getBreakHours(DowntimRequestForShift downtimRequestForShift) {
        try {
            return webClientBuilder.build()
                    .post()
                    .uri(getCurrentCompleteShiftDetails)
                    .bodyValue(downtimRequestForShift)
                    .retrieve()
                    .bodyToMono(CurrentShiftDetails.class)
                    .block();
        } catch (Exception e) {
            throw new OeeException(1016);
        }
    }
    private List<ShiftResponse> getAllShifts(ShiftRequest shiftRequest) {
        ShiftResponseList responseList = webClientBuilder.build()
                .post()
                .uri(getAllShifts) // Ensure this matches "/retrieveAll"
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(ShiftResponseList.class) // Correct return type
                .block(); // Synchronously get the response

        // Return the list of shifts or an empty list if response is null
        return responseList != null && responseList.getShiftResponseList() != null
                ? responseList.getShiftResponseList()
                : new ArrayList<>();
    }
    private ShiftResponse getShiftDetail(ShiftRequest shiftRequest) {
        ShiftResponse shiftResponse = webClientBuilder.build()
                .post()
                .uri(getretretrieveShifts) // Ensure this matches "/retrieveAll"
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToMono(ShiftResponse.class) // Correct return type
                .block(); // Synchronously get the response

        // Return the list of shifts or an empty list if response is null
        return shiftResponse;
    }



//    public List<SpeedLossSummaryDTO> getSpeedLossByResource(String site, List<String> resourceIds,
//                                                            LocalDateTime intervalStart, LocalDateTime intervalEnd) {
//        LocalDateTime now = LocalDateTime.now();
//        // If no interval is provided, default to the last 24 hours.
//        if (intervalStart == null || intervalEnd == null) {
//            intervalEnd = now;
//            intervalStart = now.minusHours(24);
//        }
//        if (resourceIds != null && !resourceIds.isEmpty()) {
//            return oeeRepository.findSpeedLossBySiteAndResourceAndInterval(site, resourceIds, intervalStart, intervalEnd);
//        } else {
//            return oeeRepository.findSpeedLossBySiteAndInterval(site, intervalStart, intervalEnd);
//        }
//    }

    public List<SpeedLossSummaryDTO> getSpeedLossByResource(OeeFilterRequest request) {

        Map<String, Object> queryParameters = new HashMap<>();
        String eventTypeOfPerformance = null;
        String eventSource = null;
        eventTypeOfPerformance = "RESOURCE";
        eventSource = request.getEventSource() + "_COMPLETE";

        String sql = buildOEESpeedLossByResourceQuery(queryParameters, request, eventTypeOfPerformance, eventSource);

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();
            return results.stream()
                    .map(result -> {
                        String resourceId = (String) result[0];
                        double totalPlan = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
                        double totalQuantity = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;
                        // Calculate speed loss only if plan is greater than 0
                        double speedLoss = 0.0;
                        if (totalPlan > 0) {
                            speedLoss = ((totalPlan - totalQuantity) / totalPlan) * 100;
                            // Round to 2 decimal places
                            speedLoss = Math.round(speedLoss * 100.0) / 100.0;
                        }
                        return SpeedLossSummaryDTO.builder()
                                .resourceId(resourceId)
                                .workcenterId(null)
                                .speedLoss(speedLoss < 0 ? 0.0 : speedLoss)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error calculating speed loss by resource", e);
        }
    }

    public List<SpeedLossSummaryDTO> getSpeedLossByWorkcenter(OeeFilterRequest request) {

        String eventTypeOfPerformance = null;
        String eventSource = null;
//        if(request.getResourceId() == null) {
        eventTypeOfPerformance = "WORKCENTER";
        eventSource = request.getEventSource() + "_DONE";
//        } else {
//            eventTypeOfPerformance = "RESOURCE";
//            eventSource = request.getEventSource() + "_COMPLETE";
//        }

        Map<String, Object> queryParameters = new HashMap<>();

        String sql = buildOEESpeedLossByWorkcenterQuery(queryParameters, request, eventTypeOfPerformance, eventSource);

        try {
            Query query = entityManager.createNativeQuery(sql);
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            return results.stream()
                    .map(result -> {
                        String workcenterId = (result[0] instanceof String) ? (String) result[0] : String.valueOf(result[0]);
                        double totalPlan = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
                        double totalQuantity = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;

                        // Calculate speed loss only if plan is greater than 0
                        double speedLoss = 0.0;
                        if (totalPlan > 0) {
                            speedLoss = ((totalPlan - totalQuantity) / totalPlan) * 100;
                            // Round to 2 decimal places
                            speedLoss = Math.round(speedLoss * 100.0) / 100.0;
                        }

                        return SpeedLossSummaryDTO.builder()
                                .resourceId(null)
                                .workcenterId(workcenterId)
                                .speedLoss(speedLoss < 0 ? 0.0 : speedLoss)
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error calculating speed loss by workcenter", e);
        }
    }

//    @Override
//    public MetricsResponse getOverallHistory(OeeRequest request) throws Exception {
//        try {
//            // Fetch data for different time intervals
//            List<MetricsResponse.MetricData.DayData> dayDataList = fetchMetricsData(request, "WORKCENTER_DAY", 7);
//            List<MetricsResponse.MetricData.MonthData> monthDataList = fetchMetricsData(request, "WORKCENTER_MONTH", 3);
//            List<MetricsResponse.MetricData.YearData> yearDataList = fetchMetricsData(request, "WORKCENTER_YEAR", 3);
//
//            // workcenter list
//            List<String> workcenterList = getWorkcenterList(request);
//
//            // Build response list
//            List<MetricsResponse.MetricData> metricDataList = workcenterList.stream()
//                    .map(workcenter -> MetricsResponse.MetricData.builder()
//                            .workcenter(workcenter)
//                            .year(yearDataList)
//                            .month(monthDataList)
//                            .days(dayDataList)
//                            .build())
//                    .collect(Collectors.toList());
//
//            return buildResponse(request.getType(), metricDataList);
//        } catch (Exception e) {
//            throw new RuntimeException("Error fetching metrics data", e);
//        }
//    }
//
//    private <T> List<T> fetchMetricsData(OeeRequest request, String category, int interval) {// cat=day,month
//        List<Object> queryResult = buildQuery(category, request.getType(), StringUtils.isNotBlank(request.getWorkcenterId().toString()), request.getSite(), request.getWorkcenterId());
//        String query = (String) queryResult.get(0);
//        MapSqlParameterSource params = (MapSqlParameterSource) queryResult.get(1);
//
////        System.out.println("Original Query:\n" + query);
////
////        String resolvedQuery = query;
////        for (Map.Entry<String, Object> entry : params.getValues().entrySet()) {
////            String paramPlaceholder = ":" + entry.getKey();
////            String paramValue = (entry.getValue() instanceof List) ? entry.getValue().toString() : "'" + entry.getValue() + "'";
////            resolvedQuery = resolvedQuery.replace(paramPlaceholder, paramValue);
////        }
////
////        System.out.println("Resolved Query:\n" + resolvedQuery);
//
//        List<T> result = namedJdbcTemplate.query(query, params, (rs, rowNum) -> {
//            String key = category.equals("WORKCENTER_YEAR") ? rs.getString("year")
//                    : rs.getString(category.equals("WORKCENTER_MONTH") ? "month" : "day");
//
//            double avgValue = rs.getDouble("avg_value");
//            Object metricData = createMetricDataInstance(category, key, avgValue);
//
//            System.out.println("Created Metric Data: " + metricData.getClass().getName());
//            return (T) metricData;
////            return (T) createMetricDataInstance(category, key, avgValue);
//        });
//        return result;
//    }
//
//    private List<Object> buildQuery(String category, String type, boolean hasWorkcenterFilter, String site, List<String> workcenterIds) {
//            String periodColumn = category.equals("WORKCENTER_YEAR") ? "year" :
//                    (category.equals("WORKCENTER_MONTH") ? "month" : "day");
//
//            String interval;
//            String startDateSubtraction;
//
//            if ("day".equals(periodColumn)) {
//                interval = "INTERVAL '1 day'";
//                startDateSubtraction = "INTERVAL '6 days'"; // Last 7 days including today
//            } else if ("month".equals(periodColumn)) {
//                interval = "INTERVAL '1 month'";
//                startDateSubtraction = "INTERVAL '2 months'"; // Last 3 months
//            } else if ("year".equals(periodColumn)) {
//                interval = "INTERVAL '1 year'";
//                startDateSubtraction = "INTERVAL '2 years'"; // Last 3 years
//            } else {
//                throw new IllegalArgumentException("Invalid periodColumn: " + periodColumn);
//            }
//
//            String query = "WITH date_series AS ("
//                    + "SELECT generate_series("
//                    + "    (SELECT MAX(" + periodColumn + ") - " + startDateSubtraction + " FROM r_aggregated_time_period "
//                    + "     WHERE site = :site AND active = TRUE AND category = :category), "
//                    + "    (SELECT MAX(" + periodColumn + ") FROM r_aggregated_time_period "
//                    + "     WHERE site = :site AND active = TRUE AND category = :category), "
//                    + "    " + interval + ")::DATE AS " + periodColumn + "), aggregated_data AS ("
//                    + "SELECT " + periodColumn + ", workcenter_id, " + type + " "
//                    + "FROM r_aggregated_time_period "
//                    + "WHERE site = :site AND active = TRUE AND category = :category "
//                    + (hasWorkcenterFilter ? " AND workcenter_id IN (:workcenterIds)" : " ")
//                    + ") "
//                    + "SELECT EXTRACT("
//                    + (periodColumn.equals("day") ? "DAY" : periodColumn.equals("month") ? "MONTH" : "YEAR")
//                    + " FROM ds." + periodColumn + ") AS period_value, "
//                    + "COALESCE(ROUND(CAST(AVG(ad." + type + ") AS NUMERIC), 2), 0) AS avg_value "
//                    + "FROM date_series ds "
//                    + "LEFT JOIN aggregated_data ad ON ds." + periodColumn + " = ad." + periodColumn + " "
//                    + "GROUP BY ds." + periodColumn + (hasWorkcenterFilter ? ", ad.workcenter_id " : "")
//                    + "ORDER BY ds." + periodColumn + " ASC;";
//
//            MapSqlParameterSource params = new MapSqlParameterSource();
//            params.addValue("site", site);
//            params.addValue("category", category);
//            if (workcenterIds != null && !workcenterIds.isEmpty()) {
//                params.addValue("workcenterIds", workcenterIds);
//            }
//
//        return Arrays.asList(query, params);
//    }
//
//    private List<Object> prepareParams(OeeRequest request, String category) {
//        List<Object> params = new ArrayList<>();
//
//        // Adding site and category for all queries
//        params.add(request.getSite());
//        params.add(category);
//        params.add(request.getSite());
//        params.add(category);
//        params.add(request.getSite());
//        params.add(category);
//
//        // ✅ Correctly handle workcenter_id as an SQL Array
//        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
//            String workcenterIdString = String.join(",", request.getWorkcenterId());
//            params.add(workcenterIdString);
//        }
////        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
////            String workcenterIdString = request.getWorkcenterId().stream()
////                    .map(id -> "'" + id + "'")  // Wrap each ID in single quotes
////                    .collect(Collectors.joining(",")); // Join with commas
////            params.add(new SqlParameterValue(Types.VARCHAR, workcenterIdString)); // Pass as a SQL parameter
////        }
//
////        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
////            params.add(new SqlParameterValue(Types.ARRAY, request.getWorkcenterId().toArray(new String[0])));
////        }
//
////        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
////            String workcenterIds = String.join(",", request.getWorkcenterId()); // Join values with ','
////            params.add(new SqlParameterValue(Types.VARCHAR, workcenterIds)); // Pass as a single VARCHAR
////        }
//
//
//        return params;
//    }
//
//
//    private Object createMetricDataInstance(String category, String key, double avgValue) {
//        switch (category) {
//            case "WORKCENTER_DAY":
//                return new MetricsResponse.MetricData.DayData(key, avgValue);
//            case "WORKCENTER_MONTH":
//                return new MetricsResponse.MetricData.MonthData(key, avgValue);
//            case "WORKCENTER_YEAR":
//                return new MetricsResponse.MetricData.YearData(key, avgValue);
//            default:
//                throw new IllegalArgumentException("Unknown category: " + category);
//        }
//    }
//
//    private List<String> getWorkcenterList(OeeRequest request) {
//        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
//            return new ArrayList<>(request.getWorkcenterId());
//        }
//        return jdbcTemplate.query("SELECT DISTINCT workcenter_id FROM r_aggregated_time_period WHERE site = ? AND active = TRUE AND workcenter_id IS NOT NULL",
//                new Object[]{request.getSite()}, (rs, rowNum) -> rs.getString("workcenter_id"));
//    }
//
//    private MetricsResponse buildResponse(String type, List<MetricsResponse.MetricData> metricDataList) {
//        switch (type) {
//            case "oee":
//                return MetricsResponse.builder().oeeData(metricDataList).build();
//            case "quality":
//                return MetricsResponse.builder().qualitydata(metricDataList).build();
//            case "availability":
//                return MetricsResponse.builder().availabilitydata(metricDataList).build();
//            default:
//                return MetricsResponse.builder().performancedata(metricDataList).build();
//        }
//    }

    @Override
    public List<Map<String, Object>> getOverallResourceHistoryByType(OeeRequest request) throws Exception {
        if (request.getSite() == null || request.getDuration() == null || request.getWorkcenterId() == null) {
            throw new IllegalArgumentException("Site, duration, and workcenterId are required fields.");
        }

        StringBuilder query = new StringBuilder();
        List<Object> params = new ArrayList<>();

        query.append("SELECT resource_id AS resource, ")
                .append("COALESCE(ROUND(CAST(AVG(oee) AS numeric), 2), 0) AS oee, ")
                .append("COALESCE(ROUND(CAST(AVG(quality) AS numeric), 2), 0) AS quality, ")
                .append("COALESCE(ROUND(CAST(AVG(availability) AS numeric), 2), 0) AS availability, ")
                .append("COALESCE(ROUND(CAST(AVG(performance) AS numeric), 2), 0) AS performance, ")
                .append("COALESCE(ROUND(CAST(AVG(actual_time) AS numeric), 2), 0) AS actual, ")
                .append("COALESCE(ROUND(CAST(AVG(plan) AS numeric), 2), 0) AS plan, ")
                .append("COALESCE(ROUND(CAST(AVG(total_good_quantity) AS numeric), 2), 0) AS goodQualityCount, ")
                .append("COALESCE(ROUND(CAST(AVG(total_bad_quantity) AS numeric), 2), 0) AS badQualityCount ")
                .append("FROM r_aggregated_oee ")
                .append("WHERE site = ? AND workcenter_id IN (")
                .append(request.getWorkcenterId().stream().map(id -> "?").collect(Collectors.joining(",")))
                .append(") AND category = 'RESOURCE' "); // Ensuring only RESOURCE category is fetched

        params.add(request.getSite());
        params.addAll(request.getWorkcenterId());

        // Determine the appropriate date filter based on the format of duration
        String duration = request.getDuration();
        if (duration.matches("\\d{4}")) { // Year format (e.g., "2025")
            query.append(" AND EXTRACT(YEAR FROM log_date) = ? ");
            params.add(Integer.parseInt(duration));
        } else if (duration.matches("\\d{4}-\\d{2}")) { // Year-Month format (e.g., "2025-01")
            query.append(" AND TO_CHAR(log_date, 'YYYY-MM') = ? ");
            params.add(duration);
        } else if (duration.matches("\\d{4}-\\d{2}-\\d{2}")) { // Year-Month-Day format (e.g., "2025-01-01")
            query.append(" AND log_date = ? ");
            params.add(LocalDate.parse(duration));
        } else {
            throw new IllegalArgumentException("Invalid duration format. Use YYYY, YYYY-MM, or YYYY-MM-DD.");
        }

        query.append(" GROUP BY resource_id");

        // Execute query and transform results
        return jdbcTemplate.query(query.toString(), params.toArray(), (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("resource", rs.getString("resource"));
            result.put("oee", rs.getDouble("oee"));
            result.put("quality", rs.getDouble("quality"));
            result.put("availability", rs.getDouble("availability"));
            result.put("performance", rs.getDouble("performance"));
            result.put("actual", rs.getDouble("actual"));
            result.put("plan", rs.getDouble("plan"));
            result.put("goodQualityCount", rs.getDouble("goodQualityCount"));
            result.put("badQualityCount", rs.getDouble("badQualityCount"));
            return result;
        });
    }

    @Override
    public List<Map<String, Object>> getShiftByResource(OeeRequest request) throws Exception {
        if (request.getSite() == null || request.getDuration() == null || request.getResourceId() == null) {
            throw new IllegalArgumentException("Site, duration, and resourceId are required fields.");
        }

        StringBuilder query = new StringBuilder();
        List<Object> params = new ArrayList<>();

        query.append("SELECT shift_id AS shift, ")
                .append("COALESCE(ROUND(CAST(AVG(oee) AS numeric), 2), 0) AS oee, ")
                .append("COALESCE(ROUND(CAST(AVG(quality) AS numeric), 2), 0) AS quality, ")
                .append("COALESCE(ROUND(CAST(AVG(availability) AS numeric), 2), 0) AS availability, ")
                .append("COALESCE(ROUND(CAST(AVG(performance) AS numeric), 2), 0) AS performance ")
                .append("FROM r_aggregated_oee ")
                .append("WHERE site = ? AND resource_id IN (")
                .append(request.getResourceId().stream().map(id -> "?").collect(Collectors.joining(",")))
                .append(") ");

        params.add(request.getSite());
        params.addAll(request.getResourceId());

        String duration = request.getDuration();
        if (duration.matches("\\d{4}-\\d{2}-\\d{2}")) {
            query.append(" AND log_date = ? ");
            params.add(LocalDate.parse(duration));
        } else {
            throw new IllegalArgumentException("Invalid duration format. Use YYYY-MM-DD.");
        }

        query.append(" GROUP BY shift_id");

        // Execute query and transform results
        return jdbcTemplate.query(query.toString(), params.toArray(), (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("shift", rs.getString("shift"));
            result.put("oee", rs.getDouble("oee"));
            result.put("quality", rs.getDouble("quality"));
            result.put("availability", rs.getDouble("availability"));
            result.put("performance", rs.getDouble("performance"));
            return result;
        });
    }
    private List<CycleTime> fetchCycleTimes(CycleTimeReq cycleTimeReq) {

        List<CycleTime> cycleTimes=webClientBuilder.build()
                .post()
                .uri(getCycletimesByResource)
                .bodyValue(cycleTimeReq)
                .retrieve()
                .bodyToFlux(CycleTime.class)
                .collectList()
                .block();

        return cycleTimes;
    }
    public Long getCycleTimeForBatch(String batchNo, String site) {
        OeeProductionLogRequest logRequest = new OeeProductionLogRequest();
        logRequest.setBatchNo(batchNo);
        logRequest.setSite(site);

        try {
            // Retrieve the response as String
            String response = webClientBuilder.build()
                    .post()
                    .uri(getBatchActualCyleTime)
                    .bodyValue(logRequest)
                    .retrieve()
                    .bodyToMono(String.class)   // Always retrieve as String
                    .block();

            // Check for null/empty response
            if (response == null || response.trim().isEmpty()) {
                System.err.println("Empty response received");
                return null;
            }

            // Convert the response to Long
            return (long) Double.parseDouble(response.trim());

        } catch (WebClientResponseException e) {
            System.err.println("WebClient error: " + e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            System.err.println("Invalid format: " + e.getMessage());
            return null;  // Handle invalid number format
        }
    }

    private List<CycleTime> fetchCycleTimesByWc(CycleTimeReq cycleTimeReq) {

        List<CycleTime> cycleTimes=webClientBuilder.build()
                .post()
                .uri(getCycletimesByWorkcenter)
                .bodyValue(cycleTimeReq)
                .retrieve()
                .bodyToFlux(CycleTime.class)
                .collectList()
                .block();

        return cycleTimes;
    }
    private OperationDetails calculateOperationDetails(List<Oee> group) {
        ShiftRequest shiftreq= new ShiftRequest();
        shiftreq.setSite(group.get(0).getSite());
        LocalDateTime intervalStartDateTime=  getEarliestValidShiftStartDateTime(shiftreq);

        // Convert Instant.now() to LocalDateTime using the system default time zone
        Instant nowInstant = Instant.now();
        LocalDateTime now = LocalDateTime.ofInstant(nowInstant, ZoneId.systemDefault());

        // Calculate duration in seconds
        long totalTimeSeconds = Duration.between(intervalStartDateTime, now).getSeconds();
        DowntimRequestForShift downtimRequestForShift=new DowntimRequestForShift();
        downtimRequestForShift.setSite(group.get(0).getSite());
        downtimRequestForShift.setDowntimeStart(intervalStartDateTime);
        downtimRequestForShift.setDowntimeEnd(now);
        List<String> shiftIds = group.stream()
                .map(Oee::getShiftId)  // Extract shiftId
                .filter(Objects::nonNull) // Remove null values
                .distinct() // Ensure unique shiftIds
                .collect(Collectors.toList()); // Convert to List
        CurrentShiftDetails shiftDetails=getBreakHours(downtimRequestForShift);
        Metrics metrics = calculateMetrics(group, calculateSecondsFromStartToNow(intervalStartDateTime,null),0,0,0,0.0,intervalStartDateTime,LocalDateTime.now(),shiftDetails,"OPR");
        Boolean status= false;
        List<Map<String, Object>> operationStatuses=getCurrentStatus(group.get(0).getSite());
        for (Map<String, Object> statusMap : operationStatuses) {
            String operation = (String) statusMap.get("operation");
            if (group.get(0).getOperation().equals(operation)) {
                status = (Boolean) statusMap.get("status");
                break;  // Exit once you find a match
            }
        }
        OperationDetails operationDetails = new OperationDetails();
        operationDetails.setOperation(group.get(0).getOperation());
        operationDetails.setActual((int) (metrics.getTotalGoodQty() + metrics.getTotalBadQty()));
        operationDetails.setPlan((int) metrics.getTotalPlannedQty());
        operationDetails.setRejection((int) metrics.getTotalBadQty());
        operationDetails.setDowntimeDuration(metrics.getTotalDowntime());
        operationDetails.setGoodQualityCount((int) metrics.getTotalGoodQty());
        operationDetails.setBadQualityCount((int) metrics.getTotalBadQty());
        operationDetails.setEnergyUsage(DEFAULT_ENERGY_USAGE);
        operationDetails.setProductionTime((int) metrics.getProductionTimeSeconds());
        operationDetails.setActualTime((int) metrics.getTotalTimeSeconds());
        operationDetails.setDowntime(status);
        operationDetails.setAvailability(metrics.getAvailability());
        operationDetails.setPerformance(metrics.getPerformance());
        operationDetails.setQuality(metrics.getQuality());
        operationDetails.setOee(metrics.getOee());
        operationDetails.setActualCycleTime(metrics.getActualCycleTime());
        operationDetails.setPlannedCycletime(metrics.getPlannedCycleTime());
        operationDetails.setPlannedDowntime((int) metrics.getPlannedDowntime());
//        operationDetails.setUnplannedDowntime(metrics.getTotalDowntime());
        return operationDetails;
    }

    private OperationDetails calculateOperationDetails1(List<AggregatedOee> group) {
        ShiftRequest shiftreq= new ShiftRequest();
        shiftreq.setSite(group.get(0).getSite());
        LocalDateTime intervalStartDateTime=  getEarliestValidShiftStartDateTime(shiftreq);

        // Convert Instant.now() to LocalDateTime using the system default time zone
        Instant nowInstant = Instant.now();
        LocalDateTime now = LocalDateTime.ofInstant(nowInstant, ZoneId.systemDefault());

        // Calculate duration in seconds
        long totalTimeSeconds = Duration.between(intervalStartDateTime, now).getSeconds();
        DowntimRequestForShift downtimRequestForShift=new DowntimRequestForShift();
        downtimRequestForShift.setSite(group.get(0).getSite());
        downtimRequestForShift.setDowntimeStart(intervalStartDateTime);
        downtimRequestForShift.setDowntimeEnd(now);
        List<String> shiftIds = group.stream()
                .map(AggregatedOee::getShiftId)  // Extract shiftId
                .filter(Objects::nonNull) // Remove null values
                .distinct() // Ensure unique shiftIds
                .collect(Collectors.toList()); // Convert to List
        CurrentShiftDetails shiftDetails=getBreakHours(downtimRequestForShift);
        Metrics metrics = calculateMetrics1(group, calculateSecondsFromStartToNow(intervalStartDateTime,null),0,0,0,0.0,intervalStartDateTime,LocalDateTime.now(),shiftDetails,"OPR");
        Boolean status= false;
        List<Map<String, Object>> operationStatuses=getCurrentStatus(group.get(0).getSite());
        for (Map<String, Object> statusMap : operationStatuses) {
            String operation = (String) statusMap.get("operation");
            if (group.get(0).getOperation().equals(operation)) {
                status = (Boolean) statusMap.get("status");
                break;  // Exit once you find a match
            }
        }
        OperationDetails operationDetails = new OperationDetails();
        operationDetails.setOperation(group.get(0).getOperation());
        operationDetails.setActual((int) (metrics.getTotalGoodQty() + metrics.getTotalBadQty()));
        operationDetails.setPlan((int) metrics.getTotalPlannedQty());
        operationDetails.setRejection((int) metrics.getTotalBadQty());
        operationDetails.setDowntimeDuration(metrics.getTotalDowntime());
        operationDetails.setGoodQualityCount((int) metrics.getTotalGoodQty());
        operationDetails.setBadQualityCount((int) metrics.getTotalBadQty());
        operationDetails.setEnergyUsage(DEFAULT_ENERGY_USAGE);
        operationDetails.setProductionTime((int) metrics.getProductionTimeSeconds());
        operationDetails.setActualTime((int) metrics.getTotalTimeSeconds());
        operationDetails.setDowntime(status);
        operationDetails.setAvailability(metrics.getAvailability());
        operationDetails.setPerformance(metrics.getPerformance());
        operationDetails.setQuality(metrics.getQuality());
        operationDetails.setOee(metrics.getOee());
        operationDetails.setActualCycleTime(metrics.getActualCycleTime());
        operationDetails.setPlannedCycletime(metrics.getPlannedCycleTime());
        operationDetails.setPlannedDowntime((int) metrics.getPlannedDowntime());
        operationDetails.setUnplannedDowntime(metrics.getTotalDowntime());
        return operationDetails;
    }

    @Override
    public OeeByOperationResponse getByOperation(OeeFilterRequest request) throws Exception {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(startTime);
            request.setEndTime(endTime);
        }

        List<String> eventTypes = new ArrayList<>();

        String eventSource = null;
        Boolean checkBatchCondition = false;
        if((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null) {
            eventTypes.add("WORKCENTER");
            eventSource = request.getEventSource() + "_DONE";
        } else {
            eventTypes.add("RESOURCE");
            eventSource = request.getEventSource() + "_COMPLETE";
        }

        if("MANUAL".equalsIgnoreCase(request.getEventSource()))
            checkBatchCondition = true;

        List<AggregatedOee> oeeDetails =
                findAggregatedOeeByFilters(
                request.getSite(),
                request.getStartTime(),
                request.getEndTime(),
                request.getResourceId(),
                request.getShiftId(),
                request.getWorkcenterId(),
                request.getBatchNumber(),
                request.getShoporderId(),
                request.getItem(),
                request.getOperations(),
                eventTypes,
                eventSource,
                checkBatchCondition
        );
//        aggregatedOeeRepository.findByFilters1(
//                request.getSite(),
//                request.getStartTime(),
//                request.getEndTime(),
//                request.getResourceId(),
//                request.getShiftId(),
//                request.getWorkcenterId(),
//                request.getBatchNumber(),
//                request.getShoporderId(),
//                request.getItem(),
//                request.getOperations(),
//                eventTypes,
//                eventSource,
//                checkBatchCondition
//        );

        Map<String, List<AggregatedOee>> groupedData = oeeDetails.stream()
                .filter(Objects::nonNull)
                .filter(oee -> oee.getOperation() != null)
                .collect(Collectors.groupingBy(AggregatedOee::getOperation));

        List<OeeByOperationResponse.OeeByOperation> responseDetailsList = groupedData.entrySet().stream()
                .map(entry -> new OeeByOperationResponse.OeeByOperation(
                        entry.getKey(), // operation name/id
                        getMetricValueByType(entry.getValue(), request.getType())
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        OeeByOperationResponse response = new OeeByOperationResponse();
        response.setOeeByOperation(responseDetailsList);
        return response;
    }

    private double getMetricValueForOperation(OperationDetails operation, String type) {
        if (type == null) {
            return 0.0;
        }

        double value;
        switch (type.toLowerCase()) {
            case "availability":
                value = operation.getAvailability();
                break;
            case "performance":
                value = operation.getPerformance();
                break;
            case "quality":
                value = operation.getQuality();
                break;
            case "oee":
                value = operation.getOee();
                break;
            default:
                value = 0.0;
                break;
        }

        // Round to two decimal places
        return Math.round(value * 100.0) / 100.0;
    }

    /*@Override
    public OeeByOperationResponse getByOperation(OeeFilterRequest request) throws Exception {
        String eventTypeOfPerformance = "completeSfcBatch";

        if ((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null) {
            eventTypeOfPerformance = "doneSfcBatch";
        }

        StringBuilder sql = new StringBuilder(
                "SELECT o.operation AS operation, " +
                        "SUM(o.good_qty) AS totalGoodQty, " +
                        "SUM(o.bad_qty) AS totalBadQty, " +
                        "SUM(o.plan) AS totalPlannedQty, " +
                        "SUM(o.total_downtime) AS totalDowntime, " +
                        "MIN(o.interval_start_date_time) AS intervalStartTime, " +
                        "MAX(o.interval_end_date_time) AS intervalEndTime " +
                        "FROM R_OEE o " +
                        "WHERE o.site = :site " +
                        "AND o.event_type_of_performance = :eventTypeOfPerformance " +
                        "AND o.batch_number IS NOT NULL " +
                        "AND o.batch_number <> '' " +
                        "AND o.plan > 0 ");

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());
        queryParameters.put("eventTypeOfPerformance", eventTypeOfPerformance);

        applyCommonFilters(sql, queryParameters, request);

        sql.append("GROUP BY o.operation " +
                "ORDER BY o.operation");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();
            OeeByOperationResponse response = new OeeByOperationResponse();
            List<OeeByOperationResponse.OeeByOperation> OeeOperationList = new ArrayList<>();

            for (Object[] result : results) {
                OeeByOperationResponse.OeeByOperation oeeByOperation = new OeeByOperationResponse.OeeByOperation();

                oeeByOperation.setOperation((result[0] != null) ? result[0].toString() : "");

                double totalGoodQty = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
                double totalBadQty = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;
                double totalPlannedQty = (result[3] != null) ? ((Number) result[3]).doubleValue() : 0.0;
                double totalDowntime = (result[4] != null) ? ((Number) result[4]).doubleValue() : 0.0;

                LocalDateTime intervalStartTime = (result[5] != null) ? ((java.sql.Timestamp) result[5]).toLocalDateTime() : null;
                LocalDateTime intervalEndTime = (result[6] != null) ? ((java.sql.Timestamp) result[6]).toLocalDateTime() : null;

                long totalTimeSeconds = (intervalStartTime != null && intervalEndTime != null) ?
                        Duration.between(intervalStartTime, intervalEndTime).getSeconds() : 0L;

                long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
                long actualTimeSeconds = totalTimeSeconds;

                double availability = (productionTimeSeconds > 0) ? ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;
                double performance = (totalPlannedQty > 0) ? ((totalGoodQty + totalBadQty) / totalPlannedQty) * 100 : 0.0;
                double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;
                double oee = (availability * performance * quality) / 10000;

                String type = request.getType();
                double percentage = 0.0;
                switch (type.toLowerCase()) {
                    case "oee":
                        percentage = oee;
                        break;
                    case "performance":
                        percentage = performance;
                        break;
                    case "availability":
                        percentage = availability;
                        break;
                    case "quality":
                        percentage = quality;
                        break;
                    *//*case "downtime":
                        percentage = totalDowntime;
                        break;*//*
                    default:
                        percentage = 0.0;
                        break;
                }

                oeeByOperation.setPercentage(Math.round(percentage * 100.0) / 100.0);
                OeeOperationList.add(oeeByOperation);
            }

            response.setOeeByOperation(OeeOperationList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/
    @Override
    public MetricsResponse getOverallHistory(OeeRequest request) throws Exception {
        try {
            if (request == null || StringUtils.isBlank(request.getSite())) {
                throw new IllegalArgumentException("Invalid request: site is required");
            }

            List<String> workcenterList = getWorkcenterList(request);
            List<MetricsResponse.MetricData> metricDataList = workcenterList.stream()
                    .filter(Objects::nonNull)
                    .map(workcenter -> {
                        OeeRequest workcenterRequest = new OeeRequest();
                        workcenterRequest.setSite(request.getSite());
                        workcenterRequest.setWorkcenterId(Collections.singletonList(workcenter));

                        List<MetricsResponse.MetricData.DayData> dayDataList = fetchDayData(workcenterRequest);
                        List<MetricsResponse.MetricData.MonthData> monthDataList = fetchMonthData(workcenterRequest);
                        List<MetricsResponse.MetricData.YearData> yearDataList = fetchYearData(workcenterRequest);

                        return MetricsResponse.MetricData.builder()
                                .workcenter(workcenter)
                                .days(dayDataList)
                                .month(monthDataList)
                                .year(yearDataList)
                                .build();
                    })
                    .collect(Collectors.toList());

            return buildResponse(metricDataList);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching metrics data", e);
        }
    }

    private List<String> getWorkcenterList(OeeRequest request) {
        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            return request.getWorkcenterId().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return jdbcTemplate.query("SELECT DISTINCT workcenter_id FROM r_aggregated_time_period WHERE site = ? AND active = TRUE",
                        new Object[]{request.getSite()}, (rs, rowNum) -> rs.getString("workcenter_id"))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<MetricsResponse.MetricData.DayData> fetchDayData(OeeRequest request) {
        String query = buildDynamicQuery("WORKCENTER_DAY", request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty(), request.getSite(), request.getWorkcenterId());
        MapSqlParameterSource params = prepareParams(request, "WORKCENTER_DAY");

        return namedJdbcTemplate.query(query, params, (rs, rowNum) ->
                MetricsResponse.MetricData.DayData.builder()
                        .name(rs.getString("period_value"))
                        .oee(rs.getDouble("oee"))
                        .performance(rs.getDouble("performance"))
                        .quality(rs.getDouble("quality"))
                        .availability(rs.getDouble("availability"))
                        .build());
    }

    private List<MetricsResponse.MetricData.MonthData> fetchMonthData(OeeRequest request) {
        String query = buildDynamicQuery("WORKCENTER_MONTH", request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty(), request.getSite(), request.getWorkcenterId());
        MapSqlParameterSource params = prepareParams(request, "WORKCENTER_MONTH");

        return namedJdbcTemplate.query(query, params, (rs, rowNum) ->
                MetricsResponse.MetricData.MonthData.builder()
                        .name(rs.getString("period_value"))
                        .oee(rs.getDouble("oee"))
                        .performance(rs.getDouble("performance"))
                        .quality(rs.getDouble("quality"))
                        .availability(rs.getDouble("availability"))
                        .build());
    }

    private List<MetricsResponse.MetricData.YearData> fetchYearData(OeeRequest request) {
        String query = buildDynamicQuery("WORKCENTER_YEAR", request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty(), request.getSite(), request.getWorkcenterId());
        MapSqlParameterSource params = prepareParams(request, "WORKCENTER_YEAR");

        return namedJdbcTemplate.query(query, params, (rs, rowNum) ->
                MetricsResponse.MetricData.YearData.builder()
                        .name(rs.getString("period_value"))
                        .oee(rs.getDouble("oee"))
                        .performance(rs.getDouble("performance"))
                        .quality(rs.getDouble("quality"))
                        .availability(rs.getDouble("availability"))
                        .build());
    }

    private String buildDynamicQuery(String category, boolean hasWorkcenterFilter, String site, List<String> workcenterIds) {
        String periodColumn = category.equals("WORKCENTER_YEAR") ? "year" :
                (category.equals("WORKCENTER_MONTH") ? "month" : "day");

        String interval;
        String startDateSubtraction;

        switch (periodColumn) {
            case "day":
                interval = "INTERVAL '1 day'";
                startDateSubtraction = "INTERVAL '6 days'";
                break;
            case "month":
                interval = "INTERVAL '1 month'";
                startDateSubtraction = "INTERVAL '2 months'";
                break;
            case "year":
                interval = "INTERVAL '1 year'";
                startDateSubtraction = "INTERVAL '2 years'";
                break;
            default:
                throw new IllegalArgumentException("Invalid periodColumn: " + periodColumn);
        }

        StringBuilder query = new StringBuilder();
        query.append("WITH date_series AS (")
                .append("SELECT generate_series(")
                .append("    (SELECT MAX(").append(periodColumn).append(") - ").append(startDateSubtraction).append(" FROM r_aggregated_time_period ")
                .append("     WHERE site = :site AND active = TRUE AND category = :category), ")
                .append("    (SELECT MAX(").append(periodColumn).append(") FROM r_aggregated_time_period ")
                .append("     WHERE site = :site AND active = TRUE AND category = :category), ")
                .append("    ").append(interval).append(")::DATE AS ").append(periodColumn).append("), ")
                .append("aggregated_data AS (")
                .append("SELECT ").append(periodColumn).append(", workcenter_id, oee, performance, quality, availability ")
                .append("FROM r_aggregated_time_period ")
                .append("WHERE site = :site AND active = TRUE AND category = :category ");

        if (hasWorkcenterFilter && workcenterIds != null && !workcenterIds.isEmpty()) {
            query.append(" AND workcenter_id IN (:workcenterIds)");
        }

        query.append(") ")
                .append("SELECT EXTRACT(")
                .append(periodColumn.equals("day") ? "DAY" : periodColumn.equals("month") ? "MONTH" : "YEAR")
                .append(" FROM ds.").append(periodColumn).append(") AS period_value, ")
                .append("ad.workcenter_id, ")
                .append("COALESCE(ROUND(CAST(AVG(ad.oee) AS NUMERIC), 2), 0) AS oee, ")
                .append("COALESCE(ROUND(CAST(AVG(ad.performance) AS NUMERIC), 2), 0) AS performance, ")
                .append("COALESCE(ROUND(CAST(AVG(ad.quality) AS NUMERIC), 2), 0) AS quality, ")
                .append("COALESCE(ROUND(CAST(AVG(ad.availability) AS NUMERIC), 2), 0) AS availability ")
                .append("FROM date_series ds ")
                .append("LEFT JOIN aggregated_data ad ON ds.").append(periodColumn).append(" = ad.").append(periodColumn).append(" ")
                .append("GROUP BY ds.").append(periodColumn).append(", ad.workcenter_id")
                .append(" ORDER BY ds.").append(periodColumn).append(" ASC;");

        return query.toString();
    }

    private MapSqlParameterSource prepareParams(OeeRequest request, String category) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("site", request.getSite());
        params.addValue("category", category);
        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            params.addValue("workcenterIds", request.getWorkcenterId());
        }
        return params;
    }

    private MetricsResponse buildResponse(List<MetricsResponse.MetricData> metricDataList) {
        return MetricsResponse.builder()
                .metricData(metricDataList)
                .build();
    }

    @Override
    public List<Map<String, Object>> getByresourceTimeAndInterval(OeeRequest request) throws Exception {// handle by resource and workcenter?
        if (request.getSite() == null || request.getResourceId() == null || request.getResourceId().isEmpty()) {
            throw new IllegalArgumentException("Site and Resource ID are required fields.");
        }

        LocalDateTime endTime = request.getEndTime() != null ? LocalDateTime.parse(request.getEndTime()) : LocalDateTime.now();
        LocalDateTime startTime = request.getStartTime() != null ? LocalDateTime.parse(request.getStartTime()) : endTime.with(LocalTime.MIN);

        StringBuilder query = new StringBuilder();
        List<Object> params = new ArrayList<>();
        String eventType = "";

        query.append("SELECT ")
                .append("interval_end_date_time, ")
                .append("interval_start_date_time, ")
                .append("total_qty, good_qty, plan, bad_qty, shift_id ")
                .append("FROM ( ")
                .append("SELECT r_oee.interval_end_date_time, ")
                .append("r_oee.interval_start_date_time, ")
                .append("r_oee.total_qty, r_oee.good_qty, r_oee.plan, r_oee.bad_qty, r_oee.shift_id, ")
                .append("ROW_NUMBER() OVER (PARTITION BY r_oee.interval_end_date_time, r_oee.interval_start_date_time ")
                .append("ORDER BY r_oee.created_datetime DESC) AS rn ")
                .append("FROM r_oee ")
                .append("WHERE r_oee.site = ? ")
                .append("AND r_oee.resource_id IN (")
                .append(request.getResourceId().stream().map(id -> "?").collect(Collectors.joining(",")))
                .append(") ");

        if("MANUAL".equalsIgnoreCase(request.getEventSource()))
            query.append("AND r_oee.batch_number IS NOT NULL ");

        if ("MACHINE".equalsIgnoreCase(request.getEventSource())) {
            eventType = "machinecomplete";
        } else if ("MANUAL".equalsIgnoreCase(request.getEventSource())) {
            eventType = "completeSfcBatch";
        }
        query.append(" AND r_oee.event_type_of_performance = '")
                .append(eventType)
                .append("' ");
        query.append("AND r_oee.category = 'RESOURCE' ")
                .append("AND r_oee.interval_start_date_time <= ? ")
                .append("AND r_oee.interval_end_date_time >= ? ")
                .append(") AS filtered_data ")
                .append("WHERE rn = 1 ")
                .append("ORDER BY interval_end_date_time DESC");

        params.add(request.getSite());
        params.addAll(request.getResourceId());
        params.add(endTime);
        params.add(startTime);

        List<Map<String, Object>> rawData = jdbcTemplate.query(query.toString(), params.toArray(), (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            String shiftInfo = rs.getString("shift_id");
            String[] parts = shiftInfo != null ? shiftInfo.split(",") : new String[0];
            String lastPart = parts.length > 0 ? parts[parts.length - 1] : "";
            result.put("shift_name", lastPart);

//            result.put("shift_name", rs.getString("shift_id")); // assuming shift_id holds shift name
            result.put("interval_end_date_time", rs.getTimestamp("interval_end_date_time").toLocalDateTime());
            result.put("interval_start_date_time", rs.getTimestamp("interval_start_date_time").toLocalDateTime());
            result.put("Production_Actual_Quantity", rs.getInt("total_qty"));
            result.put("good_qty", rs.getInt("good_qty"));
            result.put("bad_qty", rs.getInt("bad_qty"));
            result.put("plan_Target_Quantity", rs.getInt("plan"));
            return result;
        });

        // Group by shift to calculate summary rows
        Map<String, List<Map<String, Object>>> groupedByShift = rawData.stream()
                .collect(Collectors.groupingBy(row -> (String) row.get("shift_name")));

        List<Map<String, Object>> finalResult = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByShift.entrySet()) {
            List<Map<String, Object>> shiftEntries = entry.getValue();

            // Calculate summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("type", "summary");
            String key = entry.getKey();
            String[] parts = key != null ? key.split(",") : new String[0];
            String lastPart = parts.length > 0 ? parts[parts.length - 1] : "";
            summary.put("shift_name", lastPart);

//            summary.put("shift_name", entry.getKey());
            summary.put("Production_Actual_Quantity", shiftEntries.stream().mapToInt(e -> (int) e.get("Production_Actual_Quantity")).sum());
            summary.put("good_qty", shiftEntries.stream().mapToInt(e -> (int) e.get("good_qty")).sum());
            summary.put("bad_qty", shiftEntries.stream().mapToInt(e -> (int) e.get("bad_qty")).sum());
            summary.put("plan_Target_Quantity", shiftEntries.stream().mapToInt(e -> (int) e.get("plan_Target_Quantity")).sum());
            summary.put("interval_start_date_time", shiftEntries.get(0).get("interval_start_date_time"));
            summary.put("interval_end_date_time", shiftEntries.get(0).get("interval_end_date_time"));

            finalResult.add(summary);
            finalResult.addAll(shiftEntries);
        }

        return finalResult;
    }

    @Override
    public CompletableFuture<OperatorReportResponse> generateReport(OperatorReportRequest request) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        OperatorReportResponse response = new OperatorReportResponse();

        LocalDateTime currentDate = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : currentDate;
        LocalDateTime endTime = request.getEndTime() != null ? request.getEndTime() : currentDate.plusDays(1).minusNanos(1);

        // Create a map of query tasks
        Map<String, Runnable> queryTasks = new HashMap<>();
        queryTasks.put("r_oee", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                oeeRepository.findOeeData(request.getSite(), request.getBatchNumber(),
                                        request.getOperation(), request.getItem(), request.getResource(),
                                        request.getWorkCenter(), request.getShoporderId(),
                                        request.getShiftId(), startTime, endTime))
                        .thenAccept(response::setR_oee)
                        .exceptionally(ex -> {
                            log.error("Error executing r_oee query", ex);
                            return null;
                        }))
        );

        queryTasks.put("r_aggregated_oee", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                aggregatedOeeRepository.findAggregatedOeeData(request.getSite(), request.getBatchNumber(),
                                        request.getOperation(), request.getItem(), request.getResource(),
                                        request.getWorkCenter(), request.getShoporderId(),
                                        request.getShiftId(), startTime, endTime))
                        .thenAccept(response::setR_aggregated_oee)
                        .exceptionally(ex -> {
                            log.error("Error executing r_aggregated_oee query", ex);
                            return null;
                        }))
        );

        // New r_overall task
        queryTasks.put("r_overall", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                aggregatedOeeRepository.findOverallData(request.getSite(), request.getBatchNumber(),
                                        request.getOperation(), request.getItem(), request.getResource(),
                                        request.getWorkCenter(), request.getShoporderId(),
                                        request.getShiftId(), startTime, endTime))
                        .thenAccept(response::setR_overall)
                        .exceptionally(ex -> {
                            log.error("Error executing r_overall query", ex);
                            return null;
                        }))
        );

        // Add r_performance task
        queryTasks.put("r_performance", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                performanceRepository.findPerformanceData(request.getSite(), request.getBatchNumber(),
                                        request.getOperation(), request.getItem(), request.getResource(),
                                        request.getWorkCenter(), request.getShoporderId(),
                                        request.getShiftId(), startTime, endTime))
                        .thenAccept(response::setR_performance)
                        .exceptionally(ex -> {
                            log.error("Error executing r_performance query", ex);
                            return null;
                        }))
        );

        // Add r_quality task
        queryTasks.put("r_quality", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                qualityRepository.findQualityData(request.getSite(), request.getBatchNumber(),
                                        request.getOperation(), request.getItem(), request.getResource(),
                                        request.getWorkCenter(), request.getShoporderId(),
                                        request.getShiftId(), startTime, endTime))
                        .thenAccept(response::setR_quality)
                        .exceptionally(ex -> {
                            log.error("Error executing r_quality query", ex);
                            return null;
                        }))
        );

        // Add r_availability task
        queryTasks.put("r_availability", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                availabilityRepository.findAvailabilityData(request.getSite(),
                                        request.getResource(),
                                        request.getWorkCenter(),
                                        request.getShiftId(),
                                        startTime,
                                        endTime
                                ))
                        .thenAccept(response::setR_availability)
                        .exceptionally(ex -> {
                            log.error("Error executing r_availability query", ex);
                            return null;
                        }))
        );

        // Add r_machine task
//        queryTasks.put("r_machine", () ->
//                futures.add(CompletableFuture.supplyAsync(() ->
//                                aggregatedOeeRepository.findMachineData(request.getSite(), request.getBatchNumber(),
//                                        request.getOperation(), request.getItem(), request.getResource(),
//                                        request.getWorkCenter(), request.getShoporderId(),
//                                        request.getShiftId(), startTime, endTime))
//                        .thenAccept(response::setR_machine)
//                        .exceptionally(ex -> {
//                            log.error("Error executing r_machine query", ex);
//                            return null;
//                        }))
//        );

        queryTasks.put("r_machine", () -> {
            CompletableFuture<List<AggregateOeeByMachineDTO>> future = executeMachineQuery(request, response, startTime, endTime);
            if (future != null) {
                futures.add(future);
            }
        });

        // Add r_workcenter task
        queryTasks.put("r_workcenter", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                oeeRepository.findWorkcenterData(request.getSite(), request.getBatchNumber(),
                                        request.getOperation(), request.getItem(), request.getResource(),
                                        request.getWorkCenter(), request.getShoporderId(),
                                        request.getShiftId(), startTime, endTime))
                        .thenAccept(response::setR_workcenter)
                        .exceptionally(ex -> {
                            log.error("Error executing r_workcenter query", ex);
                            return null;
                        }))
        );

        // Add r_time_overall task
        queryTasks.put("r_time_overall", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                aggregatedTimePeriodRepository.findTimeOverallData(
                                        request.getSite(),
                                        request.getCategory()))
                        .thenAccept(response::setR_time_overall)
                        .exceptionally(ex -> {
                            log.error("Error executing r_time_overall query", ex);
                            return null;
                        }))
        );

        // Add r_time_graph task
        queryTasks.put("r_time_graph", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                aggregatedTimePeriodRepository.findTimeGraphData(
                                        request.getSite(),
                                        request.getCategory()))
                        .thenAccept(response::setR_time_graph)
                        .exceptionally(ex -> {
                            log.error("Error executing r_time_graph query", ex);
                            return null;
                        }))
        );

        // Add r_day task
        queryTasks.put("r_day", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                aggregatedTimePeriodRepository.findDayData(request.getSite(), startTime.toLocalDate(), endTime.toLocalDate()))
                        .thenAccept(response::setR_day)
                        .exceptionally(ex -> {
                            log.error("Error executing r_day query", ex);
                            return null;
                        }))
        );

        // Add r_month task
        queryTasks.put("r_month", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                aggregatedTimePeriodRepository.findMonthData(request.getSite(),startTime.toLocalDate(), endTime.toLocalDate()))
                        .thenAccept(response::setR_month)
                        .exceptionally(ex -> {
                            log.error("Error executing r_month query", ex);
                            return null;
                        }))
        );

        // Add r_year task
        queryTasks.put("r_year", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                aggregatedTimePeriodRepository.findYearData(request.getSite(),startTime.toLocalDate(), endTime.toLocalDate()))
                        .thenAccept(response::setR_year)
                        .exceptionally(ex -> {
                            log.error("Error executing r_year query", ex);
                            return null;
                        }))
        );

        // Add r_availability_graph task
        queryTasks.put("r_availability_graph", () ->
                futures.add(CompletableFuture.supplyAsync(() ->
                                availabilityRepository.findAvailabilityGraphData(
                                        request.getSite(),
                                        request.getResource(),
                                        request.getWorkCenter(),
                                        request.getShiftId(),
                                        startTime,
                                        endTime))
                        .thenAccept(response::setR_availability_graph)
                        .exceptionally(ex -> {
                            log.error("Error executing r_availability_graph query", ex);
                            return null;
                        }))
        );

        // Add r_quality_graph task
//        queryTasks.put("r_quality_graph", () ->
//                futures.add(CompletableFuture.supplyAsync(() ->
//                                aggregatedOeeRepository.findQualityGraphData(request.getSite(), request.getBatchNumber(),
//                                        request.getOperation(), request.getItem(), request.getResource(),
//                                        request.getWorkCenter(), request.getShoporderId(),
//                                        request.getShiftId(), startTime, endTime))
//                        .thenAccept(response::setR_quality_graph)
//                        .exceptionally(ex -> {
//                            log.error("Error executing r_quality_graph query", ex);
//                            return null;
//                        }))
//        );

        queryTasks.put("r_quality_graph", () -> {
            CompletableFuture<List<AggregatedOeeQualityDTO>> future = executeQualityGraphQuery(request, response, startTime, endTime);
            if (future != null) {
                futures.add(future);
            }
        });
//        queryTasks.put("r_quality_graph", () ->
//                futures.add(CompletableFuture.supplyAsync(() -> {
//                            List<Object[]> results = aggregatedOeeRepository.findQualityGraphData(
//                                    request.getSite(), request.getBatchNumber(), request.getOperation(),
//                                    request.getItem(), request.getResource(), request.getWorkCenter(),
//                                    request.getShoporderId(), request.getShiftId(), startTime, endTime);
//
//                            // Convert List<Object[]> to List<AggregatedOeeQualityDTO>
//                            return results.stream().map(row -> new AggregatedOeeQualityDTO(
//                                    (String) row[0],   // Identifier (resourceId or workcenterId)
//                                    ((Number) row[1]).doubleValue(), // totalGoodQuantity
//                                    ((Number) row[2]).doubleValue()  // totalBadQuantity
//                            )).collect(Collectors.toList());
//                        })
//                        .thenAccept(response::setR_quality_graph)
//                        .exceptionally(ex -> {
//                            log.error("Error executing r_quality_graph query", ex);
//                            return null;
//                        }))
//        );

        // Add r_performance_graph task
//        queryTasks.put("r_performance_graph", () ->
//                futures.add(CompletableFuture.supplyAsync(() ->
//                                aggregatedOeeRepository.findPerformanceGraphData(request.getSite(), request.getBatchNumber(),
//                                        request.getOperation(), request.getItem(), request.getResource(),
//                                        request.getWorkCenter(), request.getShoporderId(),
//                                        request.getShiftId(), startTime, endTime))
//                        .thenAccept(response::setR_performance_graph)
//                        .exceptionally(ex -> {
//                            log.error("Error executing r_performance_graph query", ex);
//                            return null;
//                        }))
//        );

        queryTasks.put("r_performance_graph", () -> {
            CompletableFuture<List<PerformanceGraphAggregateOeeDTO>> future = executePerformanceGraphQuery(request, response, startTime, endTime);
            if (future != null) {
                futures.add(future);
            }
        });

        // Execute only requested queries
        request.getQueryTypes().forEach(type -> {
            if (queryTasks.containsKey(type)) {
                queryTasks.get(type).run();
            }
        });

        // Wait for all queries to complete
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> response)
                .exceptionally(throwable -> {
                    log.error("Error generating report", throwable);
                    return new OperatorReportResponse();
                });
    }

    private CompletableFuture<List<AggregatedOeeQualityDTO>> executeQualityGraphQuery(
            OperatorReportRequest request, OperatorReportResponse response, LocalDateTime startTime, LocalDateTime endTime) {

        if (request.getResource() != null && !request.getResource().isEmpty()) {
            return executeResourceQualityGraphQuery(request, response, startTime, endTime);
        } else if (request.getWorkCenter() != null && !request.getWorkCenter().isEmpty()) {
            return executeWorkcenterQualityGraphQuery(request, response, startTime, endTime);
        } else {
            log.warn("Both resource and workCenter are null or empty. Skipping query execution.");
            return null;
        }
    }

    private CompletableFuture<List<AggregatedOeeQualityDTO>> executeResourceQualityGraphQuery(
            OperatorReportRequest request, OperatorReportResponse response, LocalDateTime startTime, LocalDateTime endTime) {

        return CompletableFuture.supplyAsync(() -> {
            List<AggregatedOeeQualityDTO> result = aggregatedOeeRepository.findResourceQualityGraphData(
                    request.getSite(), request.getBatchNumber(), request.getOperation(),
                    request.getItem(), request.getResource(),request.getWorkCenter(), request.getShoporderId(),
                    request.getShiftId(), startTime, endTime);
            response.setR_quality_graph(result);
            return result;
        }).exceptionally(ex -> {
            log.error("Error executing findResourceQualityGraphData query", ex);
            return Collections.emptyList();
        });
    }

    private CompletableFuture<List<AggregatedOeeQualityDTO>> executeWorkcenterQualityGraphQuery(
            OperatorReportRequest request, OperatorReportResponse response, LocalDateTime startTime, LocalDateTime endTime) {

        return CompletableFuture.supplyAsync(() -> {
            List<AggregatedOeeQualityDTO> result = aggregatedOeeRepository.findWorkcenterQualityGraphData(
                    request.getSite(), request.getBatchNumber(), request.getOperation(),
                    request.getItem(), request.getResource(), request.getWorkCenter(), request.getShoporderId(),
                    request.getShiftId(), startTime, endTime);
            response.setR_quality_graph(result);
            return result;
        }).exceptionally(ex -> {
            log.error("Error executing findWorkcenterQualityGraphData query", ex);
            return Collections.emptyList();
        });
    }

    private CompletableFuture<List<PerformanceGraphAggregateOeeDTO>> executePerformanceGraphQuery(
            OperatorReportRequest request, OperatorReportResponse response, LocalDateTime startTime, LocalDateTime endTime) {

        if (request.getResource() != null && !request.getResource().isEmpty()) {
            return executeResourcePerformanceGraphQuery(request, response, startTime, endTime);
        } else if (request.getWorkCenter() != null && !request.getWorkCenter().isEmpty()) {
            return executeWorkcenterPerformanceGraphQuery(request, response, startTime, endTime);
        } else {
            log.warn("Both resource and workCenter are null or empty. Skipping query execution.");
            return null;
        }
    }

    private CompletableFuture<List<PerformanceGraphAggregateOeeDTO>> executeResourcePerformanceGraphQuery(
            OperatorReportRequest request, OperatorReportResponse response, LocalDateTime startTime, LocalDateTime endTime) {

        return CompletableFuture.supplyAsync(() -> {
            List<PerformanceGraphAggregateOeeDTO> result = aggregatedOeeRepository.findResourcePerformanceGraphData(
                    request.getSite(), request.getBatchNumber(), request.getOperation(),
                    request.getItem(), request.getResource(),request.getWorkCenter(), request.getShoporderId(),
                    request.getShiftId(), startTime, endTime);
            response.setR_performance_graph(result);
            return result;
        }).exceptionally(ex -> {
            log.error("Error executing findResourcePerformanceGraphData query", ex);
            return Collections.emptyList();
        });
    }

    private CompletableFuture<List<PerformanceGraphAggregateOeeDTO>> executeWorkcenterPerformanceGraphQuery(
            OperatorReportRequest request, OperatorReportResponse response, LocalDateTime startTime, LocalDateTime endTime) {

        return CompletableFuture.supplyAsync(() -> {
            List<PerformanceGraphAggregateOeeDTO> result = aggregatedOeeRepository.findWorkcenterPerformanceGraphData(
                    request.getSite(), request.getBatchNumber(), request.getOperation(),
                    request.getItem(), request.getResource(), request.getWorkCenter(), request.getShoporderId(),
                    request.getShiftId(), startTime, endTime);
            response.setR_performance_graph(result);
            return result;
        }).exceptionally(ex -> {
            log.error("Error executing findWorkcenterPerformanceGraphData query", ex);
            return Collections.emptyList();
        });
    }


    private CompletableFuture<List<AggregateOeeByMachineDTO>> executeMachineQuery(
            OperatorReportRequest request, OperatorReportResponse response, LocalDateTime startTime, LocalDateTime endTime) {

        if (request.getResource() != null && !request.getResource().isEmpty()) {
            return executeByMachineResourceQuery(request, response, startTime, endTime);
        } else if (request.getWorkCenter() != null && !request.getWorkCenter().isEmpty()) {
            return executeByMachineWorkcenterQuery(request, response, startTime, endTime);
        } else {
            log.warn("Both resource and workCenter are null or empty. Skipping query execution.");
            return null;
        }
    }

    private CompletableFuture<List<AggregateOeeByMachineDTO>> executeByMachineResourceQuery(
            OperatorReportRequest request, OperatorReportResponse response, LocalDateTime startTime, LocalDateTime endTime) {

        return CompletableFuture.supplyAsync(() -> {
            List<AggregateOeeByMachineDTO> result = aggregatedOeeRepository.findMachineDataByResource(
                    request.getSite(), request.getBatchNumber(), request.getOperation(),
                    request.getItem(), request.getResource(),request.getWorkCenter(), request.getShoporderId(),
                    request.getShiftId(), startTime, endTime);
            response.setR_machine(result);
            return result;
        }).exceptionally(ex -> {
            log.error("Error executing findResourcePerformanceGraphData query", ex);
            return Collections.emptyList();
        });
    }

    private CompletableFuture<List<AggregateOeeByMachineDTO>> executeByMachineWorkcenterQuery(
            OperatorReportRequest request, OperatorReportResponse response, LocalDateTime startTime, LocalDateTime endTime) {

        return CompletableFuture.supplyAsync(() -> {
            List<AggregateOeeByMachineDTO> result = aggregatedOeeRepository.findMachineDataByWorkcenter(
                    request.getSite(), request.getBatchNumber(), request.getOperation(),
                    request.getItem(), request.getResource(), request.getWorkCenter(), request.getShoporderId(),
                    request.getShiftId(), startTime, endTime);
            response.setR_machine(result);
            return result;
        }).exceptionally(ex -> {
            log.error("Error executing findWorkcenterPerformanceGraphData query", ex);
            return Collections.emptyList();
        });
    }

    @Override
    public List<SiteDetails> getAllOeeDetailsV1(OeeRequest request) {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new IllegalArgumentException("Site is a required field.");
        }

        StringBuilder query = new StringBuilder();
        List<Object> params = new ArrayList<>();

        query.append("SELECT ")
                .append("COALESCE(oee, 0.0) AS oee, ")
                .append("COALESCE(availability, 0.0) AS availability, ")
                .append("COALESCE(performance, 0.0) AS performance, ")
                .append("COALESCE(quality, 0.0) AS quality, ")
                .append("COALESCE(actual_time, 0.0) AS actual, ")
                .append("COALESCE(plan, 0.0) AS plan, ")
                .append("COALESCE(total_good_quantity, 0.0) AS goodQualityCount, ")
                .append("COALESCE(total_bad_quantity, 0.0) AS badQualityCount ")
                .append("FROM r_aggregated_oee ")
                .append("WHERE site = ? ")
                .append("AND category = 'WORKCENTER' ")
                .append("AND active = TRUE ")
                .append("AND shift_id IS NOT NULL AND shift_id <> '' ")
                .append("AND item IS NOT NULL AND item <> '' ")
                .append("AND operation IS NOT NULL AND operation <> '' ")
                .append("AND batch_number IS NOT NULL AND batch_number <> '' ")
                .append("AND shop_order_id IS NOT NULL AND shop_order_id <> ''");

        params.add(request.getSite());

        List<SiteDetails> siteDetailsList = jdbcTemplate.query(query.toString(), params.toArray(), (rs, rowNum) -> {
            SiteDetails siteDetails = new SiteDetails();
            siteDetails.setSite(request.getSite());
            siteDetails.setOee(rs.getDouble("oee"));
            siteDetails.setAvailability(rs.getDouble("availability"));
            siteDetails.setPerformance(rs.getDouble("performance"));
            siteDetails.setQuality(rs.getDouble("quality"));
            siteDetails.setActual(rs.getDouble("actual"));
            siteDetails.setPlan(rs.getDouble("plan"));
            siteDetails.setGoodQualityCount(rs.getDouble("goodQualityCount"));
            siteDetails.setBadQualityCount(rs.getDouble("badQualityCount"));
            return siteDetails;
        });

        return siteDetailsList;
    }

    @Override
    public List<WorkcenterDetails> getOeeDetailsByWorkCenterIdV1(OeeRequest oeeRequest) {
        if (oeeRequest.getSite() == null || oeeRequest.getSite().isEmpty()) {
            throw new IllegalArgumentException("Site is a required field.");
        }

        StringBuilder query = new StringBuilder();
        List<Object> params = new ArrayList<>();

        query.append("SELECT ")
                .append("COALESCE(oee, 0.0) AS oee, ")
                .append("COALESCE(availability, 0.0) AS availability, ")
                .append("COALESCE(performance, 0.0) AS performance, ")
                .append("COALESCE(quality, 0.0) AS quality, ")
                .append("workcenter_id, ")
                .append("COALESCE(actual_time, 0.0) AS actual, ")
                .append("COALESCE(plan, 0.0) AS plan, ")
                .append("COALESCE(total_good_quantity, 0.0) AS goodQualityCount, ")
                .append("COALESCE(total_bad_quantity, 0.0) AS badQualityCount ")
                .append("FROM r_aggregated_oee ")
                .append("WHERE site = ? ")
                .append("AND category = 'WORKCENTER' ")
                .append("AND active = TRUE ")
                .append("AND shift_id IS NOT NULL AND shift_id <> '' ")
                .append("AND item IS NOT NULL AND item <> '' ")
                .append("AND operation IS NOT NULL AND operation <> '' ")
                .append("AND batch_number IS NOT NULL AND batch_number <> '' ")
                .append("AND shop_order_id IS NOT NULL AND shop_order_id <> ''");

        params.add(oeeRequest.getSite());

        List<WorkcenterDetails> workcenterDetailsList = jdbcTemplate.query(query.toString(), params.toArray(), (rs, rowNum) -> {
            WorkcenterDetails workcenterDetails = new WorkcenterDetails();
            workcenterDetails.setWorkcenter(rs.getString("workcenter_id"));
            workcenterDetails.setOee(rs.getDouble("oee"));
            workcenterDetails.setAvailability(rs.getDouble("availability"));
            workcenterDetails.setPerformance(rs.getDouble("performance"));
            workcenterDetails.setQuality(rs.getDouble("quality"));
            workcenterDetails.setActual(rs.getDouble("actual"));
            workcenterDetails.setPlan(rs.getDouble("plan"));
            workcenterDetails.setGoodQualityCount(rs.getDouble("goodQualityCount"));
            workcenterDetails.setBadQualityCount(rs.getDouble("badQualityCount"));
            return workcenterDetails;
        });

        return workcenterDetailsList;
    }

    @Override
    public List<Map<String, Object>> getOverallResourceHistoryV1(OeeRequest request) throws Exception {
        if (request.getSite() == null || request.getDuration() == null || request.getWorkcenterId() == null) {
            throw new IllegalArgumentException("Site, duration, and workcenterId are required fields.");
        }

        StringBuilder query = new StringBuilder();
        List<Object> params = new ArrayList<>();

        query.append("SELECT ")
                .append("COALESCE(actual_time, 0.0) AS actual, ")
                .append("COALESCE(performance, 0.0) AS performance, ")
                .append("COALESCE(total_bad_quantity, 0.0) AS badQualityCount, ")
                .append("resource_id AS resource, ")
                .append("COALESCE(total_good_quantity, 0.0) AS goodQualityCount, ")
                .append("COALESCE(availability, 0.0) AS availability, ")
                .append("COALESCE(plan, 0.0) AS plan, ")
                .append("COALESCE(oee, 0.0) AS oee, ")
                .append("COALESCE(quality, 0.0) AS quality ")
                .append("FROM r_aggregated_oee ")
                .append("WHERE site = ? ")
                .append("AND category = 'WORKCENTER' ")
                .append("AND active = TRUE ")
                .append("AND workcenter_id IN (")
                .append(request.getWorkcenterId().stream().map(id -> "?").collect(Collectors.joining(",")))
                .append(") ")
                .append("AND log_date = ? ")
                .append("AND shift_id IS NOT NULL AND shift_id <> '' ")
                .append("AND item IS NOT NULL AND item <> '' ")
                .append("AND operation IS NOT NULL AND operation <> '' ")
                .append("AND batch_number IS NOT NULL AND batch_number <> '' ")
                .append("AND shop_order_id IS NOT NULL AND shop_order_id <> '' ");

        params.add(request.getSite());
        params.addAll(request.getWorkcenterId());
        params.add(LocalDate.parse(request.getDuration()));

        return jdbcTemplate.query(query.toString(), params.toArray(), (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("resource", rs.getString("resource"));
            result.put("oee", rs.getDouble("oee"));
            result.put("quality", rs.getDouble("quality"));
            result.put("availability", rs.getDouble("availability"));
            result.put("performance", rs.getDouble("performance"));
            result.put("actual", rs.getDouble("actual"));
            result.put("plan", rs.getDouble("plan"));
            result.put("goodQualityCount", rs.getDouble("goodQualityCount"));
            result.put("badQualityCount", rs.getDouble("badQualityCount"));
            return result;
        });
    }

    @Override
    public List<Map<String, Object>> getShiftByResourceV1(OeeRequest request) throws Exception {
        if (request.getSite() == null || request.getDuration() == null || request.getResourceId() == null || request.getWorkcenterId() == null) {
            throw new IllegalArgumentException("Site, duration, resourceId, and workcenterId are required fields.");
        }

        StringBuilder query = new StringBuilder();
        List<Object> params = new ArrayList<>();

        query.append("SELECT ")
                .append("COALESCE(performance, 0.0) AS performance, ")
                .append("shift_id AS shift, ")
                .append("COALESCE(availability, 0.0) AS availability, ")
                .append("COALESCE(oee, 0.0) AS oee, ")
                .append("COALESCE(quality, 0.0) AS quality ")
                .append("FROM r_aggregated_oee ")
                .append("WHERE site = ? ")
                .append("AND category = 'RESOURCE' ")
                .append("AND active = TRUE ")
                .append("AND resource_id IN (")
                .append(request.getResourceId().stream().map(id -> "?").collect(Collectors.joining(",")))
                .append(") ")
                .append("AND workcenter_id IN (")
                .append(request.getWorkcenterId().stream().map(id -> "?").collect(Collectors.joining(",")))
                .append(") ")
                .append("AND log_date = ? ")
                .append("AND shift_id IS NOT NULL AND shift_id <> '' ")
                .append("AND item IS NOT NULL AND item <> '' ")
                .append("AND operation IS NOT NULL AND operation <> '' ")
                .append("AND batch_number IS NOT NULL AND batch_number <> '' ")
                .append("AND shop_order_id IS NOT NULL AND shop_order_id <> ''");

        params.add(request.getSite());
        params.addAll(request.getResourceId());
        params.addAll(request.getWorkcenterId());
        params.add(LocalDate.parse(request.getDuration()));

        return jdbcTemplate.query(query.toString(), params.toArray(), (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("shift", rs.getString("shift"));
            result.put("oee", rs.getDouble("oee"));
            result.put("quality", rs.getDouble("quality"));
            result.put("availability", rs.getDouble("availability"));
            result.put("performance", rs.getDouble("performance"));
            return result;
        });
    }
    @Override
    public List<Map<String, Object>> getOverallHistoryV1(OeeRequest request) throws Exception {
        if (request.getSite() == null) {
            throw new IllegalArgumentException("Site is a required field.");
        }

        if (request.getWorkcenterId() == null || request.getWorkcenterId().isEmpty()) {
            throw new IllegalArgumentException("WorkcenterId is a required field.");
        }
        String workcenterId = request.getWorkcenterId().get(0);
        String query = "WITH date_series AS ( "
                + "  SELECT generate_series( "
                + "    (SELECT DATE_TRUNC('day', NOW()) - INTERVAL '6 days'), "
                + "    NOW(), "
                + "    INTERVAL '1 day' "
                + "  )::DATE AS day "
                + "), "
                + "aggregated_data AS ( "
                + "  SELECT day, workcenter_id, oee, performance, quality, availability "
                + "  FROM r_aggregated_time_period "
                + "  WHERE site = ? AND active = TRUE AND category = 'WORKCENTER_DAY' "
                + "    AND day >= DATE_TRUNC('day', NOW()) - INTERVAL '6 days' "
                + "    AND day <= NOW() "
                + "    AND workcenter_id = ? "
                + ") "
                + "SELECT "
                + "  EXTRACT(DAY FROM ds.day) AS period_value, "
                + "  COALESCE(ROUND(AVG(ad.oee)::NUMERIC, 2), 0) AS oee, "
                + "  COALESCE(ROUND(AVG(ad.performance)::NUMERIC, 2), 0) AS performance, "
                + "  COALESCE(ROUND(AVG(ad.quality)::NUMERIC, 2), 0) AS quality, "
                + "  COALESCE(ROUND(AVG(ad.availability)::NUMERIC, 2), 0) AS availability "
                + "FROM date_series ds "
                + "LEFT JOIN aggregated_data ad ON ds.day = ad.day "
                + "GROUP BY ds.day "
                + "ORDER BY ds.day ASC";

        List<Object> params = new ArrayList<>();
        params.add(request.getSite());
        params.add(workcenterId);

        return jdbcTemplate.query(query, params.toArray(), (rs, rowNum) -> {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("name", rs.getString("period_value"));
            dayData.put("oee", rs.getDouble("oee"));
            dayData.put("performance", rs.getDouble("performance"));
            dayData.put("quality", rs.getDouble("quality"));
            dayData.put("availability", rs.getDouble("availability"));
            return dayData;
        });
    }

    /**     * Retrieves the latest machine records.     */
    /*private List<Map<String, Object>> getLatestMachineRecords(LocalDateTime startTime, LocalDateTime endTime, String site) {
        if (startTime == null || endTime == null || site == null) return Collections.emptyList();

        try {
            return jdbcTemplate.queryForList(
                    "WITH LastRecords AS (" +
                            "  SELECT md.t_stamp, md.count, md.machine_name, md.reject_count, md.site, " +
                            "         ROW_NUMBER() OVER (PARTITION BY md.machine_name ORDER BY md.t_stamp DESC) AS rn " +
                            "  FROM r_machine_data md " +
                            "  WHERE md.t_stamp BETWEEN ? AND ? AND md.site = ?" +
                            ") " +
                            "SELECT * FROM LastRecords WHERE rn = 1",
                    startTime, endTime, site
            );
        } catch (Exception e) {
            System.err.println("Error fetching machine records: " + e.getMessage());
            return Collections.emptyList();
        }
    }*/

    private List<Map<String, Object>> getLatestMachineRecords(LocalDateTime startTime, LocalDateTime endTime, String site) {
        if (startTime == null || endTime == null || site == null) return Collections.emptyList();

        try {
            String sql = "WITH time_intervals AS (" +
                    "SELECT " +
                    "    machine_name, " +
                    "    t_stamp as interval_start, " +
                    "    LEAD(t_stamp, 1, ?) OVER ( " +
                    "        PARTITION BY machine_name " +
                    "        ORDER BY t_stamp " +
                    "    ) as interval_end " +
                    "FROM ( " +
                    "    SELECT DISTINCT machine_name, t_stamp " +
                    "    FROM ( " +
                    "        SELECT DISTINCT machine_name, ? as t_stamp " +
                    "        FROM r_machine_data " +
                    "        WHERE site = ? " +
                    "        UNION " +
                    "        SELECT machine_name, t_stamp " +
                    "        FROM r_machine_data " +
                    "        WHERE site = ? " +
                    "        AND t_stamp BETWEEN ? AND ? " +
                    "        AND count = 0 " +
                    "        UNION " +
                    "        SELECT DISTINCT machine_name, ? as t_stamp " +
                    "        FROM r_machine_data " +
                    "        WHERE site = ? " +
                    "    ) all_times " +
                    ") times " +
                    "), " +
                    "interval_sums AS ( " +
                    "SELECT " +
                    "    md.machine_name, " +
                    "    ti.interval_start, " +
                    "    ti.interval_end, " +
                    "    SUM(CASE WHEN md.count > 0 THEN md.count ELSE 0 END) as period_count, " +
                    "    MAX(md.t_stamp) as last_timestamp " +
                    "FROM time_intervals ti " +
                    "LEFT JOIN r_machine_data md ON " +
                    "    md.machine_name = ti.machine_name " +
                    "    AND md.t_stamp > ti.interval_start " +
                    "    AND md.t_stamp <= ti.interval_end " +
                    "    AND md.site = ? " +
                    "GROUP BY " +
                    "    md.machine_name, " +
                    "    ti.interval_start, " +
                    "    ti.interval_end " +
                    ") " +
                    "SELECT " +
                    "    machine_name, " +
                    "    period_count as count, " +
                    "    ? as site, " +
                    "    last_timestamp as t_stamp " +
                    "FROM interval_sums " +
                    "WHERE period_count > 0 " +
                    "ORDER BY machine_name, interval_start";

            return jdbcTemplate.queryForList(sql,
                    endTime,      // for LEAD function
                    startTime,    // interval start
                    site,        // first subquery
                    site,        // second subquery
                    startTime,   // between start
                    endTime,     // between end
                    endTime,     // interval end
                    site,        // third subquery
                    site,        // for main join
                    site         // for final output
            );
        } catch (Exception e) {
            System.err.println("Error fetching machine records: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> getLatestMachineRejectRecords(LocalDateTime startTime, LocalDateTime endTime, String site) {
        if (startTime == null || endTime == null || site == null) return Collections.emptyList();

        try {
            String sql = "WITH time_intervals AS (" +
                    "SELECT " +
                    "    machine_name, " +
                    "    t_stamp as interval_start, " +
                    "    LEAD(t_stamp, 1, ?) OVER ( " +
                    "        PARTITION BY machine_name " +
                    "        ORDER BY t_stamp " +
                    "    ) as interval_end " +
                    "FROM ( " +
                    "    SELECT DISTINCT machine_name, t_stamp " +
                    "    FROM ( " +
                    "        SELECT DISTINCT machine_name, ? as t_stamp " +
                    "        FROM r_machine_reject_data " +
                    "        WHERE site = ? " +
                    "        UNION " +
                    "        SELECT machine_name, t_stamp " +
                    "        FROM r_machine_reject_data " +
                    "        WHERE site = ? " +
                    "        AND t_stamp BETWEEN ? AND ? " +
                    "        AND reject_count = 0 " +
                    "        UNION " +
                    "        SELECT DISTINCT machine_name, ? as t_stamp " +
                    "        FROM r_machine_reject_data " +
                    "        WHERE site = ? " +
                    "    ) all_times " +
                    ") times " +
                    "), " +
                    "interval_sums AS ( " +
                    "SELECT " +
                    "    md.machine_name, " +
                    "    ti.interval_start, " +
                    "    ti.interval_end, " +
                    "    SUM(CASE WHEN md.reject_count > 0 THEN md.reject_count ELSE 0 END) as period_rejects, " +
                    "    MAX(md.t_stamp) as last_timestamp " +
                    "FROM time_intervals ti " +
                    "LEFT JOIN r_machine_reject_data md ON " +
                    "    md.machine_name = ti.machine_name " +
                    "    AND md.t_stamp > ti.interval_start " +
                    "    AND md.t_stamp <= ti.interval_end " +
                    "    AND md.site = ? " +
                    "GROUP BY " +
                    "    md.machine_name, " +
                    "    ti.interval_start, " +
                    "    ti.interval_end " +
                    ") " +
                    "SELECT " +
                    "    machine_name, " +
                    "    period_rejects as reject_count, " +
                    "    ? as site, " +
                    "    last_timestamp as t_stamp " +
                    "FROM interval_sums " +
                    "WHERE period_rejects > 0 " +
                    "ORDER BY machine_name, interval_start";

            return jdbcTemplate.queryForList(sql,
                    endTime,      // for LEAD function
                    startTime,    // interval start
                    site,        // first subquery
                    site,        // second subquery
                    startTime,   // between start
                    endTime,     // between end
                    endTime,     // interval end
                    site,        // third subquery
                    site,        // for main join
                    site         // for final output
            );
        } catch (Exception e) {
            System.err.println("Error fetching machine reject records: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean getAndLogMachinedataRecords(OeeRequest request) {
        String site = request.getSite();

        //count record from r_machine_data table
        List<Map<String, Object>> latestCountRecords = getLatestMachineRecords(request.getStartDateTime(), request.getEndDateTime(), site);
        if (latestCountRecords == null || latestCountRecords.isEmpty()) {
            System.out.println("No machine records found for site: " + request.getSite());
           // return false;
        }

        for (Map<String, Object> record : latestCountRecords) {
            if (record == null) continue;

            String machineName = (String) record.getOrDefault("machine_name", "");

            double currentCount = Optional.ofNullable((Number) record.get("count")).map(Number::doubleValue).orElse(0.0);

            LocalDateTime eventTime = Optional.ofNullable((Timestamp) record.get("t_stamp"))
                    .map(Timestamp::toLocalDateTime)
                    .orElse(null);

            Map<String, Object> lastProductionLogWorkcenterRecord = getLastProductionLogWorkcenterRecord(machineName, site, eventTime);

            // Log complete event
            if (currentCount > 0) {
                logProductionLog(machineName, currentCount, site, "machineCompleteSfcBatch", lastProductionLogWorkcenterRecord, eventTime);
            }

            // Log done event if FINAL_RESOURCE
            Resource resource = fetchResourceCustomDataByMachine(site, machineName);

            if (isFinalResource(resource) && currentCount > 0) {
                logProductionLog(machineName, currentCount, site, "machineDoneSfcBatch", lastProductionLogWorkcenterRecord, eventTime);
            }
        }


        //reject count record from r_machine_reject_data table
        List<Map<String, Object>> rejectRecords = getLatestMachineRejectRecords(request.getStartDateTime(), request.getEndDateTime(), site);
        if (rejectRecords == null || rejectRecords.isEmpty()) {
            System.out.println("No machine reject records found for site: " + request.getSite());
           // return false;
        }

        for (Map<String, Object> record : rejectRecords) {
            if (record == null) continue;

            String machineName = (String) record.getOrDefault("machine_name", "");

            int currentRejectCount = Optional.ofNullable((Number) record.get("reject_count"))
                    .map(Number::intValue)
                    .orElse(0);

            LocalDateTime eventTime = Optional.ofNullable((Timestamp) record.get("t_stamp"))
                    .map(Timestamp::toLocalDateTime)
                    .orElse(null);

            Map<String, Object> lastProductionLogWorkcenterRecord = getLastProductionLogWorkcenterRecord(machineName, site, eventTime);

            // Log scrap event
            if (currentRejectCount > 0) {
                logProductionLog(machineName, currentRejectCount, site, "machineScrapSfcBatch",
                        lastProductionLogWorkcenterRecord, eventTime);
            }
        }

        // calculateOee for machine data records
        com.rits.overallequipmentefficiency.dto.AvailabilityRequest availabilityRequest = com.rits.overallequipmentefficiency.dto.AvailabilityRequest.builder()
                .site(request.getSite())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .build();

        availabilityService.publishLineAvailabilityRequests(availabilityRequest);

        return true;
    }
    /*public boolean getAndLogMachinedataRecords(OeeRequest request) {
        String site = request.getSite();

        List<Map<String, Object>> latestRecords = getLatestMachineRecords(request.getStartDateTime(), request.getEndDateTime(), site);
        if (latestRecords == null || latestRecords.isEmpty()) {
            System.out.println("No machine records found for site: " + request.getSite());
            return false;
        }

        for (Map<String, Object> record : latestRecords) {
            if (record == null) continue;

            String machineName = (String) record.getOrDefault("machine_name", "UNKNOWN_MACHINE");
            Number countNum = (Number) record.get("count");
            double currentCount = (countNum != null) ? countNum.doubleValue() : 0.0;

            Number rejectCountNum = (Number) record.get("reject_count");
            int currentRejectCount = (rejectCountNum != null) ? rejectCountNum.intValue() : 0;

            LocalDateTime eventTime = Optional.ofNullable((Timestamp) record.get("t_stamp"))
                    .map(Timestamp::toLocalDateTime)
                    .orElse(LocalDateTime.now());

            //Resource resource = fetchResourceByMachine(site, machineName);
            //String finalResourceId = (resource != null && resource.getResource() != null)
            //        ? resource.getResource() : machineName;

            Map<String, Object> completeLog = getLastLogByEventType(machineName, site, "machineCompleteSfcBatch");
            Map<String, Object> doneLog = getLastLogByEventType(machineName, site, "machineDoneSfcBatch");
            Map<String, Object> scrapLog = getLastLogByEventType(machineName, site, "machineScrapSfcBatch");


            // Choose the latest of completeLog or doneLog for quantity_started
            Map<String, Object> lastStartedLog = getLatestLog(completeLog, doneLog);

            // Use scrapLog directly for quantity_scrapped

            Number lastStartedCountNum = (Number) lastStartedLog.get("quantity_started");
            double lastStartedCount = (lastStartedCountNum != null) ? lastStartedCountNum.doubleValue() : 0.0;

            Number lastRejectCountNum = (Number) scrapLog.get("quantity_scrapped");
            int lastRejectCount = (lastRejectCountNum != null) ? lastRejectCountNum.intValue() : 0;

            int countDelta = (int)(currentCount - lastStartedCount);
            int rejectDelta = currentRejectCount - lastRejectCount;

            // Log complete event
            if (countDelta > 0) {
                logProductionEvent(machineName, countDelta, currentCount, 0, 0,
                        site, "machineCompleteSfcBatch", lastStartedLog, eventTime);
            }

            // Log scrap event
            if (rejectDelta > 0) {
                logProductionEvent(machineName, rejectDelta, 0, currentRejectCount, 0,
                        site, "machineScrapSfcBatch", scrapLog, eventTime);
            }

            // Log done event if FINAL_RESOURCE
            Resource resource = fetchResourceByMachine(site, machineName);
            if (isFinalResource(resource) && countDelta > 0) {
                String workcenter = fetchWorkcenterByResource(site, machineName);
                lastStartedLog.put("work_center_id", workcenter);
                logProductionEvent(machineName, countDelta, 0, 0, currentCount,
                        site, "machineDoneSfcBatch", lastStartedLog, eventTime);
            }
        }
        return true;
    }*/

    /**     * Fetches resource details by machine.      */
    private Resource fetchResourceByMachine(String site, String machineName) {
        if (site == null || machineName == null) return null;

        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("site", site);
            requestBody.put("erpEquipmentNumber", machineName);

            return webClientBuilder.build()
                    .post()
                    .uri(retrieveBySiteAndErpEquipmentNumber)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Resource.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Error fetching resource for machine " + machineName + ": " + e.getMessage());
            return null;
        }
    }

    private Resource fetchResourceCustomDataByMachine(String site, String machineName) {
        if (site == null || machineName == null) return null;

        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("site", site);
            requestBody.put("resource", machineName);

            return webClientBuilder.build()
                    .post()
                    .uri(retrieveByResource)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Resource.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Error fetching resource for machine " + machineName + ": " + e.getMessage());
            return null;
        }
    }

    /**     * Retrieves the last production log for a given resource.       */
    private Map<String, Object> getLastProductionLog(String resourceId, String site) {
        if (resourceId == null || site == null) return createDummyProductionLog();

        try {
            return jdbcTemplate.queryForMap(
                    "SELECT * FROM r_production_log WHERE resource_id = ? AND site = ? " +
                            "ORDER BY created_datetime DESC LIMIT 1",
                    resourceId, site
            );
        } catch (EmptyResultDataAccessException e) {
            return createDummyProductionLog();
        } catch (Exception e) {
            System.err.println("Error fetching last production log: " + e.getMessage());
            return createDummyProductionLog();
        }
    }

    private Map<String, Object> getLastProductionLogWorkcenterRecord(String resourceId, String site, LocalDateTime lastTimestamp) {
        if (resourceId == null || site == null) return createDummyProductionLog();

        try {
            // Get workcenter for the resource
            String workcenter = fetchWorkcenterByResource(site, resourceId);

            // Get last 3 days production log for the workcenter
            LocalDateTime startDate = lastTimestamp.minusDays(3);
            return jdbcTemplate.queryForMap(
                    "SELECT * FROM r_production_log " +
                            "WHERE workcenter_id = ? AND site = ? " +
                            "AND created_datetime BETWEEN ? AND ? " +
                            "ORDER BY created_datetime DESC LIMIT 1",
                    workcenter, site, startDate, lastTimestamp
            );
        } catch (EmptyResultDataAccessException e) {
            return createDummyProductionLog();
        } catch (Exception e) {
            System.err.println("Error fetching last production log: " + e.getMessage());
            return createDummyProductionLog();
        }
    }

    /**     * Creates a dummy production log in case of missing data.      */
    private Map<String, Object> createDummyProductionLog() {
        Map<String, Object> dummyLog = new HashMap<>();
        dummyLog.put("work_center_id", "");
        dummyLog.put("operation", "");
        dummyLog.put("shop_order_bo", "");
        dummyLog.put("operation_version", "");
        dummyLog.put("batch_no", "");
        dummyLog.put("item", "");
        dummyLog.put("item_version", "");
        dummyLog.put("material", "");
        dummyLog.put("material_version", "");
        dummyLog.put("order_number", "");
        dummyLog.put("phase_id", "");
        dummyLog.put("updated_datetime", LocalDateTime.now());
        dummyLog.put("user_id", "");
        dummyLog.put("reason_code", "");
        return dummyLog;
    }

    /**     * Logs a production event.  */
    private void logProductionEvent(String resourceId, double qty, double quantityStarted,
                                    int quantityScrapped, double quantityCompleted,
                                    String site, String eventType,
                                    Map<String, Object> lastLog, LocalDateTime eventTime) {
        if (resourceId == null || site == null || eventType == null) {
            System.err.println("Invalid input for logging production event.");
            return;
        }

        Map<String, Object> productionLog = new HashMap<>();
        productionLog.put("createdDatetime", eventTime);
        productionLog.put("resourceId", resourceId);
        productionLog.put("qty", qty);
        productionLog.put("site", site);
        productionLog.put("eventType", eventType);
        productionLog.put("active", 1);
        productionLog.put("eventData", eventType + " logged successfully");
        productionLog.put("eventDatetime", eventTime);

        productionLog.put("operation", lastLog.getOrDefault("operation", ""));
        productionLog.put("shopOrderBO", lastLog.getOrDefault("shop_order_bo", ""));
        productionLog.put("operationVersion", lastLog.getOrDefault("operation_version", ""));
        productionLog.put("batchNo", lastLog.getOrDefault("batch_no", ""));
        productionLog.put("item", lastLog.getOrDefault("item", ""));
        productionLog.put("itemVersion", lastLog.getOrDefault("item_version", ""));
        productionLog.put("material", lastLog.getOrDefault("material", ""));
        productionLog.put("materialVersion", lastLog.getOrDefault("material_version", ""));
        productionLog.put("orderNumber", lastLog.getOrDefault("order_number", ""));
        productionLog.put("phaseId", lastLog.getOrDefault("phase_id", ""));
        productionLog.put("updatedDatetime", LocalDateTime.now());
        productionLog.put("userId", lastLog.getOrDefault("user_id", ""));
        productionLog.put("status", "active");
        productionLog.put("reasonCode", Optional.ofNullable(lastLog.get("reason_code")).orElse(""));

        if ("machineDoneSfcBatch".equals(eventType)) {
            productionLog.put("workcenterId", lastLog.getOrDefault("work_center_id", ""));
        }

        // Extra fields
        productionLog.put("quantityStarted", quantityStarted);
        productionLog.put("quantityScrapped", quantityScrapped);
        productionLog.put("quantityCompleted", quantityCompleted);

        try {
            webClientBuilder.build()
                    .post()
                    .uri(createProductionlog)
                    .bodyValue(productionLog)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Error logging production event: " + e.getMessage());
        }
    }

    private void logProductionLog(String resourceId, double qty, String site, String eventType,
                                    Map<String, Object> lastLog, LocalDateTime eventTime) {
        if (resourceId == null || site == null || eventType == null) {
            System.err.println("Invalid input for logging production event.");
            return;
        }

        Map<String, Object> productionLog = new HashMap<>();
        productionLog.put("createdDatetime", eventTime);
        productionLog.put("resourceId", resourceId);
        productionLog.put("qty", qty);
        productionLog.put("site", site);
        productionLog.put("eventType", eventType);
        productionLog.put("active", 1);
        productionLog.put("eventData", eventType + " logged successfully");
        productionLog.put("eventDatetime", eventTime);

        productionLog.put("operation", lastLog.getOrDefault("operation", ""));
        productionLog.put("shopOrderBO", lastLog.getOrDefault("shop_order_bo", ""));
        productionLog.put("operationVersion", lastLog.getOrDefault("operation_version", ""));
        productionLog.put("batchNo", lastLog.getOrDefault("batch_no", ""));
        productionLog.put("item", lastLog.getOrDefault("item", ""));
        productionLog.put("itemVersion", lastLog.getOrDefault("item_version", ""));
        productionLog.put("material", lastLog.getOrDefault("material", ""));
        productionLog.put("materialVersion", lastLog.getOrDefault("material_version", ""));
        productionLog.put("orderNumber", lastLog.getOrDefault("order_number", ""));
        productionLog.put("phaseId", lastLog.getOrDefault("phase_id", ""));
        productionLog.put("updatedDatetime", LocalDateTime.now());
        productionLog.put("userId", lastLog.getOrDefault("user_id", ""));
        productionLog.put("status", "active");
        productionLog.put("reasonCode", Optional.ofNullable(lastLog.get("reason_code")).orElse(""));

        if ("machineDoneSfcBatch".equals(eventType)) {
            productionLog.put("workcenterId", lastLog.getOrDefault("workcenter_id", ""));
        }

        try {
            webClientBuilder.build()
                    .post()
                    .uri(createProductionlog)
                    .bodyValue(productionLog)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Error logging production event: " + e.getMessage());
        }
    }

    /**     * Checks if a resource is marked as FINAL_RESOURCE.    */
    private boolean isFinalResource(Resource resource) {
        if (resource == null || resource.getResourceCustomDataList() == null) {
            return false;
        }
        return resource.getResourceCustomDataList().stream()
                .anyMatch(data -> "FINAL_RESOURCE".equalsIgnoreCase(data.getCustomData()) &&
                        "TRUE".equalsIgnoreCase(Optional.ofNullable(data.getValue()).orElse("")));
    }

    private String fetchWorkcenterByResource(String site, String resourceId) {
        try {
            RetrieveRequest retrieveRequest = RetrieveRequest.builder()
                    .site(site)
                    .resource(resourceId)
                    .build();

            String workcenter = webClientBuilder.build()
                    .post()
                    .uri(workcenterUrl)
                    .bodyValue(retrieveRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return (workcenter != null && !workcenter.isBlank()) ? workcenter : "";
        } catch (Exception e) {
            System.err.println("Error fetching workcenter for resource " + resourceId + ": " + e.getMessage());
            return "";
        }
    }

    private Map<String, Object> getLastLogByEventType(String resourceId, String site, String eventType) {
        if (resourceId == null || site == null || eventType == null) return createDummyProductionLog();

        try {
            return jdbcTemplate.queryForMap(
                    "SELECT * FROM r_production_log WHERE resource_id = ? AND site = ? AND event_type = ? " +
                            "ORDER BY created_datetime DESC LIMIT 1",
                    resourceId, site, eventType
            );
        } catch (EmptyResultDataAccessException e) {
            return createDummyProductionLog();  // Or return createDummyProductionLog() if needed
        } catch (Exception e) {
            System.err.println("Error fetching log for eventType: " + eventType + " - " + e.getMessage());
            return createDummyProductionLog();
        }
    }

    private Map<String, Object> getLatestLog(Map<String, Object> log1, Map<String, Object> log2) {
        LocalDateTime time1 = getLogTime(log1);
        LocalDateTime time2 = getLogTime(log2);

        return (time1.isAfter(time2)) ? log1 : log2;
    }

    private LocalDateTime getLogTime(Map<String, Object> log) {
        Object ts = log.get("created_datetime");
        if (ts instanceof Timestamp) {
            return ((Timestamp) ts).toLocalDateTime();
        } else if (ts instanceof LocalDateTime) {
            return (LocalDateTime) ts;
        }
        return LocalDateTime.MIN;
    }

// OeeServiceImpl.java

    public boolean getAndLogMachinedataRecordsBetween(String site, LocalDateTime intervalStart, LocalDateTime intervalEnd) {
        List<String> machineNames = getDistinctMachineNamesInInterval(site, intervalStart, intervalEnd);

        if (machineNames.isEmpty()) {
            System.out.println("No machine records found between interval for site: " + site);
            return false;
        }

        for (String machineName : machineNames) {
            Map<String, Object> latestMachineRecord = getLatestMachineRecord(site, machineName, intervalStart, intervalEnd);
            if (latestMachineRecord == null) continue;

            double currentCount = Optional.ofNullable((Double) latestMachineRecord.get("count")).orElse(0.0);
            int currentRejectCount = Optional.ofNullable((Integer) latestMachineRecord.get("reject_count")).orElse(0);
            LocalDateTime recordTime = Optional.ofNullable(latestMachineRecord.get("t_stamp"))
                    .map(val -> ((Timestamp) val).toLocalDateTime())
                    .orElse(null);


            Resource resource = fetchResourceByMachine(site, machineName);
            String finalResourceId = (resource != null && resource.getResource() != null)
                    ? resource.getResource() : machineName;

            Map<String, Object> lastCompleteLog = getLastProductionLogByEventType(finalResourceId, site, "machineCompleteSfcBatch");
            Map<String, Object> lastScrapLog = getLastProductionLogByEventType(finalResourceId, site, "machineScrapSfcBatch");

            double previousCount = getPreviousMachineValue(site, machineName, "count", lastCompleteLog);
            int previousReject = (int) getPreviousMachineValue(site, machineName, "reject_count", lastScrapLog);

            double deltaCount = (previousCount == 0 || currentCount < previousCount)
                    ? currentCount
                    : currentCount - previousCount;

            int deltaReject = (previousReject == 0 || currentRejectCount < previousReject)
                    ? currentRejectCount
                    : currentRejectCount - previousReject;

            Map<String, Object> baseLogData = getLastProductionLog(finalResourceId, site);

            if (deltaCount > 0) {

                logProductionEvent(finalResourceId, deltaCount, currentCount,0,0, site, "machineCompleteSfcBatch", baseLogData, recordTime);
            }

            if (deltaReject > 0) {

                logProductionEvent(finalResourceId, deltaReject,0,currentRejectCount,0, site,"machineScrapSfcBatch", baseLogData, recordTime);
            }

            if (isFinalResource(resource)) {
                String workcenter = fetchWorkcenterByResource(site, finalResourceId);
                baseLogData.put("work_center_id", workcenter);
                logProductionEvent(finalResourceId, deltaCount,0,0,currentCount, site, "machineDoneSfcBatch", baseLogData, recordTime);
            }
        }
        return true;
    }

    @Override
    public OverallOeeReportResponse getOverallOeeReport(String site) {
        String eventSource = "MACHINE_DONE"; // or "manual" based on business logic

        // Step 1: Get CellGroup level data from DB
        List<OeeMetrics> cellGroupMetrics = getAggregatedOeeByCategory(site, "CELL_GROUP_DAY", eventSource);

        OverallOeeReportResponse response = new OverallOeeReportResponse();
        response.setSite(site);

        OeeMetrics cellGroupMetric = cellGroupMetrics.get(0);

        OverallOeeReportResponse.CellGroupData cellGroup = new OverallOeeReportResponse.CellGroupData();
        cellGroup.setCellGroupCategory(cellGroupMetric.getWorkcenter());
        cellGroup.setOee(cellGroupMetric.getOee());
        cellGroup.setAvailability(cellGroupMetric.getAvailability());
        cellGroup.setPerformance(cellGroupMetric.getPerformance());
        cellGroup.setQuality(cellGroupMetric.getQuality());
        cellGroup.setTargetQty(cellGroupMetric.getTargetQty());
        cellGroup.setActualQty(cellGroupMetric.getActualQty());

        // Step 2: Get Cell workcenters under this CellGroup
        WorkCenter cellWorkCenters = fetchWorkCenterAssociation(site, cellGroupMetric.getWorkcenter());
        List<OverallOeeReportResponse.CellData> cellList = new ArrayList<>();

        for (Association cellWorkcenter : cellWorkCenters.getAssociationList()) {
            // Step 3: Get Cell-level OEE from DB
            List<OeeMetrics> cellMetrics = getAggregatedOeeByCategory(site, "CELL_DAY", eventSource);

            OeeMetrics cellMetric = cellMetrics.stream()
                    .filter(c -> c.getWorkcenter().equalsIgnoreCase(cellWorkcenter.getAssociateId()))
                    .findFirst()
                    .orElse(new OeeMetrics(cellWorkcenter.getAssociateId(), 0, 0, 0, 0, 0, 0));

            OverallOeeReportResponse.CellData cellData = new OverallOeeReportResponse.CellData();
            cellData.setCell(cellMetric.getWorkcenter());
            cellData.setOee(cellMetric.getOee());
            cellData.setAvailability(cellMetric.getAvailability());
            cellData.setPerformance(cellMetric.getPerformance());
            cellData.setQuality(cellMetric.getQuality());
            cellData.setTargetQty(cellMetric.getTargetQty());
            cellData.setActualQty(cellMetric.getActualQty());

            // Step 4: Get Line workcenters under this cell
            WorkCenter lineWorkCenters = fetchWorkCenterAssociation(site, cellWorkcenter.getAssociateId());
            List<OverallOeeReportResponse.LineData> lineList = new ArrayList<>();

            // Step 5: Get Line-level OEE from DB
            List<OeeMetrics> lineMetrics = getAggregatedOeeByCategory(site, "WORKCENTER_DAY", eventSource);

            for (Association lineWorkcenter : lineWorkCenters.getAssociationList()) {
                OeeMetrics lineMetric = lineMetrics.stream()
                        .filter(l -> l.getWorkcenter().equalsIgnoreCase(lineWorkcenter.getAssociateId()))
                        .findFirst()
                        .orElse(new OeeMetrics(lineWorkcenter.getAssociateId(), 0, 0, 0, 0, 0, 0));

                OverallOeeReportResponse.LineData lineData = new OverallOeeReportResponse.LineData();
                lineData.setLine(lineMetric.getWorkcenter());
                lineData.setOee(lineMetric.getOee());
                lineData.setAvailability(lineMetric.getAvailability());
                lineData.setPerformance(lineMetric.getPerformance());
                lineData.setQuality(lineMetric.getQuality());
                lineData.setTargetQty(lineMetric.getTargetQty());
                lineData.setActualQty(lineMetric.getActualQty());

                lineList.add(lineData);
            }

            cellData.setLines(lineList);
            cellList.add(cellData);
        }

        cellGroup.setCells(cellList);

        response.setCellGroups(cellGroup);
        return response;
    }

    public List<OeeMetrics> getAggregatedOeeByCategory(String site, String category, String eventSource) {
        String sql = "SELECT workcenter_id AS workCenter, oee, availability, performance, quality, " +
                "planned_quantity AS targetQty, total_quantity AS actualQty " +
                "FROM r_aggregated_time_period " +
                "WHERE site = :site AND category = :category " +
                "AND log_date = CURRENT_DATE AND active = true " +
                (eventSource != null ? "AND event_source = :eventSource " : "");

        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("category", category);
        if (eventSource != null) {
            params.put("eventSource", eventSource);
        }

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) ->
                new OeeMetrics(
                        rs.getString("workCenter"),
                        rs.getDouble("oee"),
                        rs.getDouble("availability"),
                        rs.getDouble("performance"),
                        rs.getDouble("quality"),
                        rs.getDouble("targetQty"),
                        rs.getDouble("actualQty")
                )
        );
    }

    public WorkCenter fetchWorkCenterAssociation(String site, String workCenter) {
        WorkCenterRequest request = WorkCenterRequest.builder()
                .site(site)
                .workCenter(workCenter)
                .build();

        return webClientBuilder.build()
                .post()
                .uri(workcenterRetrieveUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(WorkCenter.class)
                .block();
    }

    private List<String> getDistinctMachineNamesInInterval(String site, LocalDateTime start, LocalDateTime end) {
        try {
            return jdbcTemplate.queryForList(
                    "SELECT DISTINCT machine_name FROM r_machine_data WHERE site = ? AND t_stamp BETWEEN ? AND ?",
                    String.class, site, start, end);
        } catch (Exception e) {
            System.err.println("Error fetching machine names: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<String, Object> getLatestMachineRecord(String site, String machineName, LocalDateTime start, LocalDateTime end) {
        try {
            return jdbcTemplate.queryForMap(
                    "SELECT * FROM r_machine_data WHERE site = ? AND machine_name = ? AND t_stamp BETWEEN ? AND ? ORDER BY t_stamp DESC LIMIT 1",
                    site, machineName, start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> getLastProductionLogByEventType(String resourceId, String site, String eventType) {
        try {
            return jdbcTemplate.queryForMap(
                    "SELECT * FROM r_production_log WHERE site = ? AND resource_id = ? AND event_type = ? ORDER BY event_datetime DESC LIMIT 1",
                    site, resourceId, eventType);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private double getPreviousMachineValue(String site, String machineName, String column, Map<String, Object> lastLog) {
        if (lastLog == null || lastLog.get("event_datetime") == null) return 0.0;

        LocalDateTime lastEventTime = Optional.ofNullable(lastLog.get("event_datetime"))
                .map(val -> ((Timestamp) val).toLocalDateTime())
                .orElse(null);

        try {
            String sql = "SELECT " + column + " FROM r_machine_data WHERE site = ? AND machine_name = ? AND t_stamp <= ? ORDER BY t_stamp DESC LIMIT 1";
            return jdbcTemplate.queryForObject(sql, Double.class, site, machineName, lastEventTime);
        } catch (Exception e) {
            return 0.0;
        }
    }


}

