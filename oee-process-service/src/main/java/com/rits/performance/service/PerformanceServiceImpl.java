package com.rits.performance.service;

import com.rits.availability.dto.AvailabilityRequest;
import com.rits.availability.dto.OverallAvailabilityResponse;
import com.rits.common.dto.OeeFilterRequest;
import com.rits.oeeservice.dto.*;
import com.rits.oeeservice.service.OeeService;
import com.rits.performance.dto.*;
import com.rits.performance.exception.PerformanceException;
import com.rits.performance.model.OeePerformanceEntity;
import com.rits.performance.repository.PerformanceRepository;
import com.rits.quality.dto.ProductionLogRequest;
import com.rits.quality.exception.QualityException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final WebClient.Builder webClientBuilder;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private OeeService oeeService;
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${shift-service.url}/getAvailableProductionTime")
    private String shiftlog;
    @Value("${cycletime-service.url}/getCalculatedPerformance")
    private String getCalculatedPerformance;
    @Value("${productionlog-service.url}/getByInterval")
    private String productionlog;

    @Value("${productionlog-service.url}/getProductionLogByEventType")
    private String getTotalProducedQuantityUrl;

    @Value("${availability.url}/getAvailabilityRec")
    private String getAvailabilityRecUrl;
    @Value("${cycletime-service.url}/getCycleTimeRecs")
    private String getCycleTimeRecsUrl;

    /*@Override
    public OverallPerformanceResponse getOverallPerformance(OeeFilterRequest request) throws Exception {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT ROUND(CAST(AVG(p.performance_percentage) AS NUMERIC), 2) AS avgPerformance, ")
                .append("MIN(p.interval_start_date_time) AS firstRecordTime, ")
                .append("MAX(p.interval_end_date_time) AS lastRecordTime ")
                .append("FROM R_PERFORMANCE p ")
                .append("WHERE p.site = :site ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            Object[] result = (Object[]) query.getSingleResult();

            BigDecimal avgPerformance = (BigDecimal) result[0];
            double performancePercentage = (avgPerformance != null) ? avgPerformance.doubleValue() : 0.0;
            String interval = formatInterval((Timestamp) result[1], (Timestamp) result[2], request);

            return new OverallPerformanceResponse(
                    interval,
                    request.getResourceId() != null ? String.join(", ", request.getResourceId()) : null,
                    performancePercentage
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    public OverallPerformanceResponse getOverallPerformance(OeeFilterRequest request) throws Exception {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }

        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT ")
                .append("SUM(p.actual_output) AS totalGoodQty, ")
                .append("SUM(p.scrap_quantity) AS totalBadQty, ")
                .append("SUM(p.planned_output) AS totalPlannedQty, ")
                .append("SUM(p.downtime_duration) AS totalDowntime, ")
                .append("MIN(p.interval_start_date_time) AS firstRecordTime, ")
                .append("MAX(p.interval_end_date_time) AS lastRecordTime ")
                .append("FROM R_PERFORMANCE p ")
                .append("WHERE p.site = :site ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            Object[] result = (Object[]) query.getSingleResult();

            // Extract values from query result with proper handling for BigDecimal or Double
            double totalGoodQty = (result[0] != null) ? convertToDouble(result[0]) : 0.0;
            double totalBadQty = (result[1] != null) ? convertToDouble(result[1]) : 0.0;
            double totalPlannedQty = (result[2] != null) ? convertToDouble(result[2]) : 0.0;
            double totalDowntime = (result[3] != null) ? convertToDouble(result[3]) : 0.0;
            Timestamp firstRecordTime = (Timestamp) result[4];
            Timestamp lastRecordTime = (Timestamp) result[5];

            // Compute total time in seconds only if both timestamps are not null
            long totalTimeSeconds = 0;
            if (firstRecordTime != null && lastRecordTime != null) {
                LocalDateTime intervalStartTime = firstRecordTime.toLocalDateTime();
                LocalDateTime intervalEndTime = lastRecordTime.toLocalDateTime();
                totalTimeSeconds = Duration.between(intervalStartTime, intervalEndTime).getSeconds();
            }

            // Calculate production time
            long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
            long actualTimeSeconds = totalTimeSeconds;

            // Calculate availability, performance, quality, and OEE
            double availability = (productionTimeSeconds > 0) ? ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;
            double performance = (totalPlannedQty > 0) ? (totalGoodQty / totalPlannedQty) * 100 : 0.0;
            double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;
            double oee = (availability * performance * quality) / 10000;

            // Format interval string if timestamps are available
            String interval = "";
            if (firstRecordTime != null && lastRecordTime != null) {
                interval = formatInterval(firstRecordTime, lastRecordTime, request);
            }

            // Build response
            OverallPerformanceResponse response = new OverallPerformanceResponse();
            //response.setInterval(interval);
            response.setResourceId(request.getResourceId() != null ? String.join(", ", request.getResourceId()) : null);
            response.setOverallPerformancePercentage(performance);

            return response;

        } catch (Exception e) {
            // Handle any other unexpected exceptions
            throw new Exception("Error occurred while processing the overall performance: " + e.getMessage(), e);
        }
    }

    // Utility method to safely convert result to Double, handling BigDecimal and Double
    private double convertToDouble(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        } else if (value instanceof Double) {
            return (Double) value;
        }
        return 0.0; // Default fallback value
    }

    /*@Override
    public OverallPerformanceResponse getOverallPerformance(OeeFilterRequest request) throws Exception {
        OeeRequest oeeRequest = buildOeeRequest(request);

        if (request.getSite() != null && request.getShiftId() == null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            SiteDetails oeeResponse = getOeeDetailBySite(oeeRequest);  // Only site
            return OverallPerformanceResponse.builder().overallPerformancePercentage(oeeResponse.getPerformance()).build();

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftAndSite(oeeRequest);  // Site + Shift
            return OverallPerformanceResponse.builder().overallPerformancePercentage(oeeResponse.get(0).getPerformance()).build();

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<WorkcenterDetails> oeeResponse = getOeeDetailsByWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter
            return OverallPerformanceResponse.builder().overallPerformancePercentage(oeeResponse.get(0).getPerformance()).build();

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource
            return OverallPerformanceResponse.builder().overallPerformancePercentage(oeeResponse.get(0).getPerformance()).build();

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() != null) {

            List<BatchDetails> oeeResponse = getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource + Batch
            return OverallPerformanceResponse.builder().overallPerformancePercentage(oeeResponse.get(0).getPerformance()).build();

        } else {

            List<SiteDetails> oeeResponse = getAllOeeDetails(oeeRequest);  // Default fallback case if no filters match
            return OverallPerformanceResponse.builder().overallPerformancePercentage(oeeResponse.get(0).getPerformance()).build();
        }
    }*/

    /*@Override
    public PerformanceByTimeResponse getPerformanceByTime(OeeFilterRequest request) throws Exception {

        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new QualityException(1004);
        }

        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT ")
                .append("ROUND(CAST(AVG(p.performance_percentage) AS NUMERIC), 2) AS avgPerformance, ")
                .append("MIN(p.interval_start_date_time) AS firstRecordTime, ")
                .append("MAX(p.interval_end_date_time) AS lastRecordTime ")
                .append("FROM R_PERFORMANCE p ")
                .append("WHERE p.site = :site ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("GROUP BY p.interval_start_date_time ")
                .append("ORDER BY p.interval_start_date_time");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceByTimeResponse response = new PerformanceByTimeResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());
            response.setResourceId(request.getResourceId() != null ? String.join(", ", request.getResourceId()) : null);

            List<PerformanceByTimeResponse.PerformanceData> performanceByTimeList = results.stream()
                    .map(result -> {
                        PerformanceByTimeResponse.PerformanceData performanceData = new PerformanceByTimeResponse.PerformanceData();

                        performanceData.setPerformancePercentage(result[0] != null ? ((BigDecimal) result[0]).doubleValue() : 0.0);
                        String interval = formatInterval((Timestamp) result[1], (Timestamp) result[2], request);
                        performanceData.setInterval(interval);

                        return performanceData;
                    })
                    .collect(Collectors.toList());

            response.setPerformanceOverTime(performanceByTimeList);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    public PerformanceByTimeResponse getPerformanceByTime(OeeFilterRequest request) throws Exception {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }

        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT ")
                .append("SUM(p.actual_output) AS totalGoodQty, ")
                .append("SUM(p.scrap_quantity) AS totalBadQty, ")
                .append("SUM(p.planned_output) AS totalPlannedQty, ")
                .append("SUM(p.downtime_duration) AS totalDowntime, ")
                .append("MIN(p.interval_start_date_time) AS firstRecordTime, ")
                .append("MAX(p.interval_end_date_time) AS lastRecordTime ")
                .append("FROM R_PERFORMANCE p ")
                .append("WHERE p.site = :site ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("GROUP BY p.interval_start_date_time ")
                .append("ORDER BY p.interval_start_date_time");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceByTimeResponse response = new PerformanceByTimeResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());
            response.setResourceId(request.getResourceId() != null ? String.join(", ", request.getResourceId()) : null);

            List<PerformanceByTimeResponse.PerformanceData> performanceByTimeList = results.stream()
                    .map(result -> {
                        PerformanceByTimeResponse.PerformanceData performanceData = new PerformanceByTimeResponse.PerformanceData();

                        if (result.length < 6) {
                            return performanceData; // Skip this record if there are not enough columns
                        }

                        // Extract values from query result
                        double totalGoodQty = (result[0] != null) ? convertToDouble(result[0]) : 0.0;
                        double totalBadQty = (result[1] != null) ? convertToDouble(result[1]) : 0.0;
                        double totalPlannedQty = (result[2] != null) ? convertToDouble(result[2]) : 0.0;
                        double totalDowntime = (result[3] != null) ? convertToDouble(result[3]) : 0.0;
                        Timestamp firstRecordTime = (Timestamp) result[4];
                        Timestamp lastRecordTime = (Timestamp) result[5];

                        // Initialize totalTimeSeconds to 0 if timestamps are null
                        long totalTimeSeconds = 0;

                        // Handle null timestamps
                        if (firstRecordTime != null && lastRecordTime != null) {
                            LocalDateTime intervalStartTime = firstRecordTime.toLocalDateTime();
                            LocalDateTime intervalEndTime = lastRecordTime.toLocalDateTime();
                            totalTimeSeconds = Duration.between(intervalStartTime, intervalEndTime).getSeconds();
                        }

                        // Calculate production time
                        long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
                        long actualTimeSeconds = totalTimeSeconds;

                        // Calculate availability, performance, and quality
                        double availability = (productionTimeSeconds > 0) ? ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;
                        double performance = (totalPlannedQty > 0) ? (totalGoodQty / totalPlannedQty) * 100 : 0.0;
                        double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;

                        // Calculate OEE
                        double oee = (availability * performance * quality) / 10000;

                        // Format interval
                        performanceData.setDate(firstRecordTime != null ? String.valueOf(firstRecordTime.toLocalDateTime()) : "N/A");
                        performanceData.setPerformancePercentage(performance);

                        return performanceData;
                    })
                    .collect(Collectors.toList());

            response.setPerformanceOverTime(performanceByTimeList);
            return response;
        } catch (Exception e) {
            // Log the exception and return a response with an empty list or default values
            e.printStackTrace();
            PerformanceByTimeResponse emptyResponse = new PerformanceByTimeResponse();
            emptyResponse.setPerformanceOverTime(Collections.emptyList());
            return emptyResponse;
        }
    }

    /*@Override
    public PerformanceByTimeResponse getPerformanceByTime(OeeFilterRequest request) throws Exception {
        
        OeeRequest oeeRequest = buildOeeRequest(request);

        PerformanceByTimeResponse performanceByTimeResponse = null;

        if (request.getSite() != null && request.getShiftId() == null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceId(oeeRequest);  // Only site
            performanceByTimeResponse = mapPerformanceData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftAndSite(oeeRequest);  // Site + Shift
            performanceByTimeResponse = mapPerformanceData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<WorkcenterDetails> oeeResponse = getOeeDetailsByWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter
            performanceByTimeResponse = mapPerformanceData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource
            performanceByTimeResponse = mapPerformanceData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() != null) {

            List<BatchDetails> oeeResponse = getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource + Batch
            performanceByTimeResponse = mapPerformanceData(oeeResponse);

        } else {

            List<SiteDetails> oeeResponse = getAllOeeDetails(oeeRequest);  // Default fallback case if no filters match
            performanceByTimeResponse = mapPerformanceData(oeeResponse);
        }

        return performanceByTimeResponse;    
    }

    private <T> PerformanceByTimeResponse mapPerformanceData(List<T> oeeDetails) {
        // Map the availability data into AvailabilityData list
        List<PerformanceByTimeResponse.PerformanceData> performanceData = oeeDetails.stream()
                .map(detail -> {
                    String date = "";
                    Double performancePercentage = 0.0;

                    // Check the type of object and extract the availability percentage
                    if (detail instanceof ShiftDetails) {
                        performancePercentage = ((ShiftDetails) detail).getPerformance();
                    } else if (detail instanceof WorkcenterDetails) {
                        performancePercentage = ((WorkcenterDetails) detail).getPerformance();
                    } else if (detail instanceof ResourceDetails) {
                        date = ((ResourceDetails) detail).getIntervalEndTime();
                        performancePercentage = ((ResourceDetails) detail).getPerformance();
                    } else if (detail instanceof BatchDetails) {
                        performancePercentage = ((BatchDetails) detail).getPerformance();
                    } else if (detail instanceof SiteDetails) {
                        performancePercentage = ((SiteDetails) detail).getPerformance();
                    }

                    // Return the AvailabilityData object with the availability percentage
                    return new PerformanceByTimeResponse.PerformanceData(date, performancePercentage);
                })
                .collect(Collectors.toList());

        // Return the AvailabilityByTimeResponse object with the mapped data
        return PerformanceByTimeResponse.builder()
                .performanceOverTime(performanceData)
                .build();
    }*/
    
    /*@Override
    public PerformanceByShiftResponse getPerformanceByShift(OeeFilterRequest request) throws Exception {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }

        StringBuilder sql = new StringBuilder(
                "SELECT p.shift_id, ROUND(CAST(AVG(p.performance_percentage) AS NUMERIC), 2) AS averagePerformance, " +
                        "MIN(p.interval_start_date_time) AS firstRecordTime, MAX(p.interval_end_date_time) AS lastRecordTime " +
                        "FROM R_PERFORMANCE p WHERE p.site = :site "
        );

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        applyCommonFilters(sql, queryParameters, request);
        sql.append("GROUP BY p.shift_id ");
        sql.append("ORDER BY p.shift_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceByShiftResponse response = new PerformanceByShiftResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<PerformanceByShiftResponse.ShiftPerformance> performanceByShiftList = results.stream()
                    .map(result -> {
                        String shiftId = result[0] != null ? result[0].toString().split(",")[2] : "";
                        BigDecimal averagePerformance = (BigDecimal) result[1];
                        String interval = formatInterval((Timestamp) result[2], (Timestamp) result[3], request);

                        PerformanceByShiftResponse.ShiftPerformance performanceByShift = new PerformanceByShiftResponse.ShiftPerformance();
                        performanceByShift.setShiftId(shiftId);
                        performanceByShift.setPerformancePercentage(averagePerformance != null ? averagePerformance.doubleValue() : 0.0);
                        performanceByShift.setInterval(interval);

                        return performanceByShift;
                    })
                    .collect(Collectors.toList());

            response.setPerformanceByShift(performanceByShiftList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    public PerformanceByShiftResponse getPerformanceByShift(OeeFilterRequest request) throws Exception {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }

        StringBuilder sql = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        sql.append("SELECT p.shift_id, ")
                .append("SUM(p.actual_output) AS totalGoodQty, ")
                .append("SUM(p.scrap_quantity) AS totalBadQty, ")
                .append("SUM(p.planned_output) AS totalPlannedQty, ")
                .append("SUM(p.downtime_duration) AS totalDowntime, ")
                .append("MIN(p.interval_start_date_time) AS firstRecordTime, ")
                .append("MAX(p.interval_end_date_time) AS lastRecordTime ")
                .append("FROM R_PERFORMANCE p ")
                .append("WHERE p.site = :site ");

        applyCommonFilters(sql, queryParameters, request);
        sql.append("GROUP BY p.shift_id ")
                .append("ORDER BY p.shift_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceByShiftResponse response = new PerformanceByShiftResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<PerformanceByShiftResponse.ShiftPerformance> performanceByShiftList = results.stream()
                    .map(result -> {
                        PerformanceByShiftResponse.ShiftPerformance performanceByShift = new PerformanceByShiftResponse.ShiftPerformance();

                        if (result.length < 7) {
                            return performanceByShift; // Skip this record if there are not enough columns
                        }

                        // Extract values from query result
                        String shiftId = result[0] != null ? result[0].toString() : "";
                        double totalGoodQty = (result[1] != null) ? convertToDouble(result[1]) : 0.0;
                        double totalBadQty = (result[2] != null) ? convertToDouble(result[2]) : 0.0;
                        double totalPlannedQty = (result[3] != null) ? convertToDouble(result[3]) : 0.0;
                        double totalDowntime = (result[4] != null) ? convertToDouble(result[4]) : 0.0;
                        Timestamp firstRecordTime = (Timestamp) result[5];
                        Timestamp lastRecordTime = (Timestamp) result[6];

                        // Initialize totalTimeSeconds to 0 if timestamps are null
                        long totalTimeSeconds = 0;

                        // Handle null timestamps
                        if (firstRecordTime != null && lastRecordTime != null) {
                            LocalDateTime intervalStartTime = firstRecordTime.toLocalDateTime();
                            LocalDateTime intervalEndTime = lastRecordTime.toLocalDateTime();
                            totalTimeSeconds = Duration.between(intervalStartTime, intervalEndTime).getSeconds();
                        }

                        // Calculate production time
                        long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
                        long actualTimeSeconds = totalTimeSeconds;

                        // Calculate availability, performance, and quality
                        double availability = (productionTimeSeconds > 0) ? ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;
                        double performance = (totalPlannedQty > 0) ? (totalGoodQty / totalPlannedQty) * 100 : 0.0;
                        double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;

                        // Calculate OEE
                        double oee = (availability * performance * quality) / 10000;

                        // Format interval
                        String interval = formatInterval(firstRecordTime, lastRecordTime, request);

                        // Set values in response object
                        performanceByShift.setShiftId(shiftId);
                        performanceByShift.setPerformancePercentage(performance);
                        //performanceByShift.setInterval(interval);

                        return performanceByShift;
                    })
                    .collect(Collectors.toList());

            response.setPerformanceByShift(performanceByShiftList);
            return response;
        } catch (Exception e) {
            // Log the exception and return a response with an empty list or default values
            e.printStackTrace();
            PerformanceByShiftResponse emptyResponse = new PerformanceByShiftResponse();
            emptyResponse.setPerformanceByShift(Collections.emptyList());
            return emptyResponse;
        }
    }


    /*@Override
    public PerformanceByShiftResponse getPerformanceByShift(OeeFilterRequest request) throws Exception {
        
        OeeRequest oeeRequest = buildOeeRequest(request);

        PerformanceByShiftResponse performanceByShiftResponse = null;

        if (request.getSite() != null && request.getShiftId() == null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftId(oeeRequest);
            performanceByShiftResponse = mapPerformanceByShiftData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftAndSite(oeeRequest);  // Site + Shift
            performanceByShiftResponse = mapPerformanceByShiftData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<WorkcenterDetails> oeeResponse = getOeeDetailsByWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter
            performanceByShiftResponse = mapPerformanceByShiftData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource
            performanceByShiftResponse = mapPerformanceByShiftData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() != null) {

            List<BatchDetails> oeeResponse = getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource + Batch
            performanceByShiftResponse = mapPerformanceByShiftData(oeeResponse);

        } else {

            List<SiteDetails> oeeResponse = getAllOeeDetails(oeeRequest);  // Default fallback case if no filters match
            performanceByShiftResponse = mapPerformanceByShiftData(oeeResponse);
        }

        return performanceByShiftResponse;    
    }

    private <T> PerformanceByShiftResponse mapPerformanceByShiftData(List<T> oeeDetails) {
        // Map the performance data into ShiftPerformance list
        List<PerformanceByShiftResponse.ShiftPerformance> performanceByShiftList = oeeDetails.stream()
                .map(detail -> {
                    Double performancePercentage = 0.0;
                    String shiftId = null;

                    // Extract shiftId and performancePercentage based on object type
                    if (detail instanceof ShiftDetails) {
                        ShiftDetails shiftDetails = (ShiftDetails) detail;
                        performancePercentage = shiftDetails.getPerformance();
                        shiftId = shiftDetails.getShift();
                    } *//*else if (detail instanceof WorkcenterDetails) {
                        WorkcenterDetails workcenterDetails = (WorkcenterDetails) detail;
                        performancePercentage = workcenterDetails.getPerformance();
                        shiftId = workcenterDetails.getShift();
                    } else if (detail instanceof ResourceDetails) {
                        ResourceDetails resourceDetails = (ResourceDetails) detail;
                        performancePercentage = resourceDetails.getPerformance();
                        shiftId = resourceDetails.getShift();
                    } else if (detail instanceof BatchDetails) {
                        BatchDetails batchDetails = (BatchDetails) detail;
                        performancePercentage = batchDetails.getPerformance();
                        shiftId = batchDetails.getShift();
                    } else if (detail instanceof SiteDetails) {
                        SiteDetails siteDetails = (SiteDetails) detail;
                        performancePercentage = siteDetails.getPerformance();
                        shiftId = siteDetails.getShift();
                    }*//*

                    // Return the ShiftPerformance object
                    return new PerformanceByShiftResponse.ShiftPerformance(shiftId, performancePercentage, null);
                })
                .collect(Collectors.toList());

        // Return the PerformanceByShiftResponse object with the mapped data
        return PerformanceByShiftResponse.builder()
                .performanceByShift(performanceByShiftList)
                .build();
    }*/

    /*@Override
    public PerformanceByMachineResponse getPerformanceByMachine(OeeFilterRequest request) throws Exception {

        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }

        StringBuilder sql = new StringBuilder(
                "SELECT p.resource_id, ROUND(CAST(AVG(p.performance_percentage) AS NUMERIC), 2) AS averagePerformance, " +
                        "MIN(p.interval_start_date_time) AS firstRecordTime, MAX(p.interval_end_date_time) AS lastRecordTime " +
                        "FROM R_PERFORMANCE p " +
                        "WHERE p.site = :site "
        );

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        applyCommonFilters(sql, queryParameters, request);
        sql.append("GROUP BY p.resource_id ");
        sql.append("ORDER BY p.resource_id");

        try {
            // Execute the query
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceByMachineResponse response = new PerformanceByMachineResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<PerformanceByMachineResponse.MachinePerformance> performanceByMachineList = results.stream()
                    .map(result -> {
                        PerformanceByMachineResponse.MachinePerformance performanceByMachine = new PerformanceByMachineResponse.MachinePerformance();

                        performanceByMachine.setResourceId(result[0] != null ? result[0].toString() : "");
                        BigDecimal averagePerformance = (BigDecimal) result[1];
                        performanceByMachine.setPerformancePercentage(averagePerformance != null ? averagePerformance.doubleValue() : 0.0);
                        String interval = formatInterval((Timestamp) result[2], (Timestamp) result[3], request);
                        performanceByMachine.setInterval(interval);

                        return performanceByMachine;
                    })
                    .collect(Collectors.toList());

            response.setPerformanceByMachine(performanceByMachineList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    public PerformanceByMachineResponse getPerformanceByMachine(OeeFilterRequest request) throws Exception {

        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }

        StringBuilder sql = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        sql.append("SELECT p.resource_id, ")
                .append("SUM(p.actual_output) AS totalGoodQty, ")
                .append("SUM(p.scrap_quantity) AS totalBadQty, ")
                .append("SUM(p.planned_output) AS totalPlannedQty, ")
                .append("SUM(p.downtime_duration) AS totalDowntime, ")
                .append("MIN(p.interval_start_date_time) AS firstRecordTime, ")
                .append("MAX(p.interval_end_date_time) AS lastRecordTime ")
                .append("FROM R_PERFORMANCE p ")
                .append("WHERE p.site = :site ");

        applyCommonFilters(sql, queryParameters, request);
        sql.append("GROUP BY p.resource_id ")
                .append("ORDER BY p.resource_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceByMachineResponse response = new PerformanceByMachineResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<PerformanceByMachineResponse.MachinePerformance> performanceByMachineList = results.stream()
                    .map(result -> {
                        PerformanceByMachineResponse.MachinePerformance performanceByMachine = new PerformanceByMachineResponse.MachinePerformance();

                        if (result.length < 7) {
                            return performanceByMachine; // Skip if not enough columns
                        }

                        // Extract values from query result
                        String resourceId = result[0] != null ? result[0].toString() : "";
                        double totalGoodQty = (result[1] != null) ? convertToDouble(result[1]) : 0.0;
                        double totalBadQty = (result[2] != null) ? convertToDouble(result[2]) : 0.0;
                        double totalPlannedQty = (result[3] != null) ? convertToDouble(result[3]) : 0.0;
                        double totalDowntime = (result[4] != null) ? convertToDouble(result[4]) : 0.0;
                        Timestamp firstRecordTime = (Timestamp) result[5];
                        Timestamp lastRecordTime = (Timestamp) result[6];

                        // Initialize totalTimeSeconds to 0 if timestamps are null
                        long totalTimeSeconds = 0;

                        // Handle null timestamps
                        if (firstRecordTime != null && lastRecordTime != null) {
                            LocalDateTime intervalStartTime = firstRecordTime.toLocalDateTime();
                            LocalDateTime intervalEndTime = lastRecordTime.toLocalDateTime();
                            totalTimeSeconds = Duration.between(intervalStartTime, intervalEndTime).getSeconds();
                        }

                        // Calculate production time
                        long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
                        long actualTimeSeconds = totalTimeSeconds;

                        // Calculate availability, performance, and quality
                        double availability = (productionTimeSeconds > 0) ? ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;
                        double performance = (totalPlannedQty > 0) ? (totalGoodQty / totalPlannedQty) * 100 : 0.0;
                        double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;

                        // Calculate OEE
                        double oee = (availability * performance * quality) / 10000;

                        // Format interval
                        String interval = formatInterval(firstRecordTime, lastRecordTime, request);

                        // Set values in response object
                        performanceByMachine.setResourceId(resourceId);
                        performanceByMachine.setPerformancePercentage(performance);
                        //performanceByMachine.setInterval(interval);

                        return performanceByMachine;
                    })
                    .collect(Collectors.toList());

            response.setPerformanceByMachine(performanceByMachineList);
            return response;
        } catch (Exception e) {
            // Log the exception and return a response with an empty list or default values
            e.printStackTrace();
            PerformanceByMachineResponse emptyResponse = new PerformanceByMachineResponse();
            emptyResponse.setPerformanceByMachine(Collections.emptyList());
            return emptyResponse;
        }
    }

    /*@Override
    public PerformanceByMachineResponse getPerformanceByMachine(OeeFilterRequest request) throws Exception {
        
        OeeRequest oeeRequest = buildOeeRequest(request);

        PerformanceByMachineResponse performanceByMachineResponse = null;

        if (request.getSite() != null && request.getShiftId() == null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceId(oeeRequest);
            performanceByMachineResponse = mapPerformanceMachineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftAndSite(oeeRequest);  // Site + Shift
            performanceByMachineResponse = mapPerformanceMachineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<WorkcenterDetails> oeeResponse = getOeeDetailsByWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter
            performanceByMachineResponse = mapPerformanceMachineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource
            performanceByMachineResponse = mapPerformanceMachineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() != null) {

            List<BatchDetails> oeeResponse = getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource + Batch
            performanceByMachineResponse = mapPerformanceMachineData(oeeResponse);

        } else {

            List<SiteDetails> oeeResponse = getAllOeeDetails(oeeRequest);  // Default fallback case if no filters match
            performanceByMachineResponse = mapPerformanceMachineData(oeeResponse);
        }

        return performanceByMachineResponse;    
    }

    private <T> PerformanceByMachineResponse mapPerformanceMachineData(List<T> oeeDetails) {
        // Map the availability data into AvailabilityByMachine list
        List<PerformanceByMachineResponse.MachinePerformance> performanceByMachineList = oeeDetails.stream()
                .map(detail -> {
                    Double performancePercentage = 0.0;
                    String resourceId = null;

                    // Extract resourceId and performancePercentage based on object type
                    if (detail instanceof ResourceDetails) {
                        ResourceDetails resourceDetails = (ResourceDetails) detail;
                        performancePercentage = resourceDetails.getPerformance();
                        resourceId = resourceDetails.getResource();
                    } *//*else if (detail instanceof WorkcenterDetails) {
                        WorkcenterDetails workcenterDetails = (WorkcenterDetails) detail;
                        performancePercentage = workcenterDetails.getPerformance();
                        resourceId = workcenterDetails.getResource();
                    } else if (detail instanceof BatchDetails) {
                        BatchDetails batchDetails = (BatchDetails) detail;
                        performancePercentage = batchDetails.getPerformance();
                        resourceId = batchDetails.getResource();
                    } else if (detail instanceof ShiftDetails) {
                        ShiftDetails shiftDetails = (ShiftDetails) detail;
                        performancePercentage = shiftDetails.getPerformance();
                        resourceId = shiftDetails.getResource();
                    } else if (detail instanceof SiteDetails) {
                        SiteDetails siteDetails = (SiteDetails) detail;
                        performancePercentage = siteDetails.getPerformance();
                        resourceId = siteDetails.getResource();
                    }*//*

                    // Return the AvailabilityByMachine object
                    return new PerformanceByMachineResponse.MachinePerformance(resourceId, performancePercentage);
                })
                .collect(Collectors.toList());

        // Return the AvailabilityByMachineResponse object with the mapped data
        return PerformanceByMachineResponse.builder()
                .performanceByMachine(performanceByMachineList)
                .build();
    }*/

    /*@Override
    public PerformanceByProductionLineResponse getPerformanceByProductionLine(OeeFilterRequest request) throws Exception {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }

        StringBuilder sql = new StringBuilder(
                "SELECT p.workcenter_id, p.resource_id, " +
                        "ROUND(CAST(AVG(p.performance_percentage) AS NUMERIC), 2) AS averagePerformance, " +
                        "MIN(p.interval_start_date_time) AS firstRecordTime, " +
                        "MAX(p.interval_end_date_time) AS lastRecordTime " +
                        "FROM R_PERFORMANCE p " +
                        "WHERE p.site = :site "
        );

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        applyCommonFilters(sql, queryParameters, request);

        sql.append("GROUP BY p.workcenter_id, p.resource_id ");
        sql.append("ORDER BY p.workcenter_id, p.resource_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);
            List<Object[]> results = query.getResultList();

            PerformanceByProductionLineResponse response = new PerformanceByProductionLineResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<PerformanceByProductionLineResponse.productionLinePerformance> performanceDataList = results.stream()
                    .map(row -> {
                        PerformanceByProductionLineResponse.productionLinePerformance data = new PerformanceByProductionLineResponse.productionLinePerformance();

                        data.setWorkcenterId(row[0] != null ? row[0].toString() : "");
                        data.setResourceId(row[1] != null ? row[1].toString() : "");
                        BigDecimal performancePercentage = (BigDecimal) row[2];
                        data.setPerformancePercentage(performancePercentage != null ? performancePercentage.doubleValue() : 0.0);
                        String interval = formatInterval((Timestamp) row[3], (Timestamp) row[4], request);
                        data.setInterval(interval);

                        return data;
                    })
                    .collect(Collectors.toList());

            response.setPerformanceByProductionLine(performanceDataList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    public PerformanceByProductionLineResponse getPerformanceByProductionLine(OeeFilterRequest request) throws Exception {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }

        StringBuilder sql = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        sql.append("SELECT p.workcenter_id, p.resource_id, ")
                .append("SUM(p.actual_output) AS totalGoodQty, ")
                .append("SUM(p.scrap_quantity) AS totalBadQty, ")
                .append("SUM(p.planned_output) AS totalPlannedQty, ")
                .append("SUM(p.downtime_duration) AS totalDowntime, ")
                .append("MIN(p.interval_start_date_time) AS firstRecordTime, ")
                .append("MAX(p.interval_end_date_time) AS lastRecordTime ")
                .append("FROM R_PERFORMANCE p ")
                .append("WHERE p.site = :site ");

        applyCommonFilters(sql, queryParameters, request);
        sql.append("GROUP BY p.workcenter_id, p.resource_id ")
                .append("ORDER BY p.workcenter_id, p.resource_id");

        try {
            // Execute the query
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceByProductionLineResponse response = new PerformanceByProductionLineResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<PerformanceByProductionLineResponse.ProductionLinePerformance> performanceDataList = results.stream()
                    .map(row -> {
                        PerformanceByProductionLineResponse.ProductionLinePerformance data = new PerformanceByProductionLineResponse.ProductionLinePerformance();

                        // Ensure there's enough data in the row
                        if (row.length < 8) {
                            return data; // Skip if not enough columns
                        }

                        // Extract values from query result
                        String workcenterId = row[0] != null ? row[0].toString() : "";
                        String resourceId = row[1] != null ? row[1].toString() : "";
                        double totalGoodQty = (row[2] != null) ? ((BigDecimal) row[2]).doubleValue() : 0.0;
                        double totalBadQty = (row[3] != null) ? ((BigDecimal) row[3]).doubleValue() : 0.0;
                        double totalPlannedQty = (row[4] != null) ? ((BigDecimal) row[4]).doubleValue() : 0.0;
                        double totalDowntime = (row[5] != null) ? ((BigDecimal) row[5]).doubleValue() : 0.0;
                        Timestamp firstRecordTime = (Timestamp) row[6];
                        Timestamp lastRecordTime = (Timestamp) row[7];

                        // Initialize totalTimeSeconds to 0 if timestamps are null
                        long totalTimeSeconds = 0;

                        // Handle null timestamps
                        if (firstRecordTime != null && lastRecordTime != null) {
                            LocalDateTime intervalStartTime = firstRecordTime.toLocalDateTime();
                            LocalDateTime intervalEndTime = lastRecordTime.toLocalDateTime();
                            totalTimeSeconds = Duration.between(intervalStartTime, intervalEndTime).getSeconds();
                        }

                        // Calculate production time
                        long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
                        long actualTimeSeconds = totalTimeSeconds;

                        // Calculate availability, performance, and quality
                        double availability = (productionTimeSeconds > 0) ? ((double) productionTimeSeconds / actualTimeSeconds) * 100 : 0.0;
                        double performance = (totalPlannedQty > 0) ? (totalGoodQty / totalPlannedQty) * 100 : 0.0;
                        double quality = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;

                        // Calculate OEE
                        double oee = (availability * performance * quality) / 10000;

                        // Format interval
                        String interval = formatInterval(firstRecordTime, lastRecordTime, request);

                        // Set values in response object
                        data.setWorkcenterId(workcenterId);
                        data.setResourceId(resourceId);
                        data.setPerformancePercentage(performance);
                        //data.setInterval(interval);

                        return data;
                    })
                    .collect(Collectors.toList());

            response.setPerformanceByProductionLine(performanceDataList);
            return response;

        } catch (Exception e) {
            // Log the exception and return a response with an empty list or default values
            e.printStackTrace();
            PerformanceByProductionLineResponse emptyResponse = new PerformanceByProductionLineResponse();
            emptyResponse.setPerformanceByProductionLine(Collections.emptyList());
            return emptyResponse;
        }
    }

    /*@Override
    public PerformanceByProductionLineResponse getPerformanceByProductionLine(OeeFilterRequest request) throws Exception {
        
        OeeRequest oeeRequest = buildOeeRequest(request);

        PerformanceByProductionLineResponse performanceByProductionLineResponse = null;

        if (request.getSite() != null && request.getShiftId() == null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<WorkcenterDetails> oeeResponse = getOeeDetailsByWorkCenterId(oeeRequest);
            performanceByProductionLineResponse = mapPerformanceProductionLineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftAndSite(oeeRequest);  // Site + Shift
            performanceByProductionLineResponse = mapPerformanceProductionLineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<WorkcenterDetails> oeeResponse = getOeeDetailsByWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter
            performanceByProductionLineResponse = mapPerformanceProductionLineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource
            performanceByProductionLineResponse = mapPerformanceProductionLineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() != null) {

            List<BatchDetails> oeeResponse = getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource + Batch
            performanceByProductionLineResponse = mapPerformanceProductionLineData(oeeResponse);

        } else {

            List<SiteDetails> oeeResponse = getAllOeeDetails(oeeRequest);  // Default fallback case if no filters match
            performanceByProductionLineResponse = mapPerformanceProductionLineData(oeeResponse);
        }

        return performanceByProductionLineResponse;
    }

    private <T> PerformanceByProductionLineResponse mapPerformanceProductionLineData(List<T> oeeDetails) {
        
        List<PerformanceByProductionLineResponse.ProductionLinePerformance> performanceByProductionLineList = oeeDetails.stream()
                .map(detail -> {
                    Double performancePercentage = 0.0;
                    String workcenterId = null;

                    // Extract resourceId and performancePercentage based on object type
                    if (detail instanceof ResourceDetails) {
                        ResourceDetails resourceDetails = (ResourceDetails) detail;
                        performancePercentage = resourceDetails.getPerformance();
                        //workcenterId = resourceDetails.getWorkcenter();
                    } else if (detail instanceof WorkcenterDetails) {
                        WorkcenterDetails workcenterDetails = (WorkcenterDetails) detail;
                        performancePercentage = workcenterDetails.getPerformance();
                        workcenterId = workcenterDetails.getWorkcenter();
                    } *//*else if (detail instanceof BatchDetails) {
                        BatchDetails batchDetails = (BatchDetails) detail;
                        performancePercentage = batchDetails.getPerformance();
                        workcenterId = batchDetails.getWorkcenter();
                    } else if (detail instanceof ShiftDetails) {
                        ShiftDetails shiftDetails = (ShiftDetails) detail;
                        performancePercentage = shiftDetails.getPerformance();
                        workcenterId = shiftDetails.getWorkcenter();
                    } else if (detail instanceof SiteDetails) {
                        SiteDetails siteDetails = (SiteDetails) detail;
                        performancePercentage = siteDetails.getPerformance();
                        workcenterId = siteDetails.getWorkcenter();
                    }*//*

                    // Return the AvailabilityByMachine object
                    return new PerformanceByProductionLineResponse.ProductionLinePerformance(workcenterId, performancePercentage);
                })
                .collect(Collectors.toList());
        
        return PerformanceByProductionLineResponse.builder()
                .performanceByProductionLine(performanceByProductionLineList)
                .build();
    }*/
    
    @Override
    public PerformanceByReasonResponse getPerformanceByReason(OeeFilterRequest request) {
        PerformanceByReasonResponse response = new PerformanceByReasonResponse();
        response.setStartTime(request.getStartTime().toString());
        response.setEndTime(request.getEndTime().toString());

        List<PerformanceByReasonResponse.ReasonPerformance> reasons = new ArrayList<>();
        reasons.add(new PerformanceByReasonResponse.ReasonPerformance("Product A", "Machine 1", 88));
        reasons.add(new PerformanceByReasonResponse.ReasonPerformance("Product B", "Machine 2", 85));
        reasons.add(new PerformanceByReasonResponse.ReasonPerformance("Product A", "Machine 2", 90));
        response.setPerformanceByReason(reasons);

        return response;
    }

    @Override
    public PerformanceHeatMapResponse getPerformanceHeatMap(OeeFilterRequest request) {
        PerformanceHeatMapResponse response = new PerformanceHeatMapResponse();
        response.setStartTime(request.getStartTime().toString());
        response.setEndTime(request.getEndTime().toString());

        List<PerformanceHeatMapResponse.ProductPerformance> heatMap = new ArrayList<>();
        heatMap.add(new PerformanceHeatMapResponse.ProductPerformance("Product A", "Machine 1", 88));
        heatMap.add(new PerformanceHeatMapResponse.ProductPerformance("Product B", "Machine 2", 85));
        heatMap.add(new PerformanceHeatMapResponse.ProductPerformance("Product A", "Machine 2", 90));
        response.setPerformanceByProduct(heatMap);

        return response;
    }

    @Override
    public PerformanceByDowntimeResponse getPerformanceByDowntime(OeeFilterRequest request) {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }

        StringBuilder sql = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        sql.append("SELECT p.downtime_reason, p.resource_id, SUM(p.downtime_duration) AS totalDowntime ")
                .append("FROM r_performance p ")
                .append("WHERE p.site = :site ");

        applyCommonFilters(sql, queryParameters, request);
        sql.append("GROUP BY p.downtime_reason, p.resource_id ")
                .append("ORDER BY p.downtime_reason, p.resource_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceByDowntimeResponse response = new PerformanceByDowntimeResponse();
            /*response.setStartTime(request.getStartTime().toString());
            response.setEndTime(request.getEndTime().toString());*/

            List<PerformanceByDowntimeResponse.DowntimeData> downtimeList = results.stream()
                    .map(row -> {
                        String reason = row[0] != null ? row[0].toString() : "";
                        String machine = row[1] != null ? row[1].toString() : "";
                        int totalDowntime = (row[2] != null) ? ((BigDecimal) row[2]).intValue() : 0;

                        return new PerformanceByDowntimeResponse.DowntimeData(reason, machine, totalDowntime, 0.0);
                    })
                    .collect(Collectors.toList());

            response.setDowntimeAnalysis(downtimeList);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return new PerformanceByDowntimeResponse(Collections.emptyList());
        }
    }

    @Override
    public PerformanceByOperatorResponse getPerformanceByOperator(OeeFilterRequest request) {
        PerformanceByOperatorResponse response = new PerformanceByOperatorResponse();
        response.setStartTime(request.getStartTime().toString());
        response.setEndTime(request.getEndTime().toString());

        List<PerformanceByOperatorResponse.OperatorPerformance> operators = new ArrayList<>();
        operators.add(new PerformanceByOperatorResponse.OperatorPerformance("Operator A", 90));
        operators.add(new PerformanceByOperatorResponse.OperatorPerformance("Operator B", 88));
        response.setPerformanceByOperator(operators);

        return response;
    }

    /*@Override
    public PerformanceComparisonResponse getPerformanceComparison(OeeFilterRequest request) {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }

        StringBuilder sql = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        sql.append("SELECT o.resource_id, ")
                .append("SUM(o.good_qty) AS totalGoodQty, ")
                .append("SUM(o.bad_qty) AS totalBadQty, ")
                .append("SUM(o.plan) AS totalPlannedQty, ")
                .append("SUM(o.total_downtime) AS totalDowntime ")
                .append("FROM r_oee o ")
                .append("WHERE o.site = :site ");

        applyCommonFilters(sql, queryParameters, request);
        sql.append("GROUP BY o.resource_id ")
                .append("ORDER BY o.resource_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceComparisonResponse response = new PerformanceComparisonResponse();
            response.setStartTime(request.getStartTime().toString());
            response.setEndTime(request.getEndTime().toString());

            List<PerformanceComparisonResponse.ComparisonData> comparisonList = results.stream()
                    .map(row -> {
                        String machine = row[0] != null ? row[0].toString() : "";

                        double totalGoodQty = (row[2] != null) ? ((BigDecimal) row[2]).doubleValue() : 0.0;
                        double totalBadQty = (row[3] != null) ? ((BigDecimal) row[3]).doubleValue() : 0.0;
                        double totalPlannedQty = (row[4] != null) ? ((BigDecimal) row[4]).doubleValue() : 0.0;
                        double totalDowntime = (row[5] != null) ? ((BigDecimal) row[5]).doubleValue() : 0.0;

                        double performancePercentage = (totalPlannedQty > 0) ? (totalGoodQty / totalPlannedQty) * 100 : 0.0;
                        double qualityPercentage = (totalGoodQty + totalBadQty > 0) ? (totalGoodQty / (totalGoodQty + totalBadQty)) * 100 : 0.0;

                        return new PerformanceComparisonResponse.ComparisonData(
                                machine,
                                performancePercentage,
                                totalDowntime,
                                qualityPercentage
                        );
                    })
                    .collect(Collectors.toList());

            response.setPerformanceComparison(comparisonList);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return new PerformanceComparisonResponse(request.getStartTime().toString(), request.getEndTime().toString(), Collections.emptyList());
        }
    }*/
    private void setStartAndEndTimes(PerformanceRequest request) {
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
    public Boolean calculatePerformance(PerformanceRequest performanceRequest) {
        setStartAndEndTimes(performanceRequest);

        // Step 1: Get required data
        List<ProductionLog> productionLogs = getActualPartsProduced(performanceRequest);
        List<OverallAvailabilityResponse> availabilityData = findAvailability(performanceRequest);
        List<CycleTimePostgres> cycleTimes = findCycleTimePostgres(performanceRequest.getSite());

        // Step 2: Calculate parts to be produced and performance metrics
        return calculatePartsToBeProduced(productionLogs, availabilityData, cycleTimes);
    }

    public Boolean calculatePartsToBeProduced(
            List<ProductionLog> productionLog,
            List<OverallAvailabilityResponse> availabilityData,
            List<CycleTimePostgres> cycleTimes) {

        boolean created = false;
        LocalDateTime currentTime = LocalDateTime.now();

        // Step 1: Group CycleTime data by keys
        Map<String, CycleTimePostgres> cycleTimeMap = cycleTimes.stream()
                .collect(Collectors.toMap(this::generateKey, cycleTime -> cycleTime, (existing, replacement) -> existing));

        // Step 2: Prepare list to hold all performance entities
        List<OeePerformanceEntity> performanceEntities = new ArrayList<>();

        // Step 3: Iterate through production logs and calculate metrics
        for (ProductionLog log : productionLog) {
            CycleTimePostgres cycleTime = getCycleTimeForLog(log, cycleTimeMap);
            OverallAvailabilityResponse availability = findAvailabilityForLog(log, availabilityData);

            if (availability == null) continue;

            Double availableTimeInSecs = availability.getTotalAvailableTimeSeconds();
            if (availableTimeInSecs == null || availableTimeInSecs <= 0) continue;

            Double partsToBeProduced = calculatePartsToBeProduced(availableTimeInSecs, cycleTime.getPlannedCycleTime());
            Integer actualPartsProduced = log.getQty();

            // Calculate performance metrics
            double performancePercentage = (partsToBeProduced != null && partsToBeProduced > 0)
                    ? roundToTwoDecimalPlaces((actualPartsProduced / partsToBeProduced) * 100.0)
                    : 0.0;

            double performanceEfficiency = (partsToBeProduced != null && partsToBeProduced > 0)
                    ? roundToTwoDecimalPlaces((actualPartsProduced * cycleTime.getPlannedCycleTime() / availableTimeInSecs) * 100)
                    : 0.0;

            // Build performance entity
            OeePerformanceEntity entity = OeePerformanceEntity.builder()
                    .site(safeGet(log.getSite()))
                    .pcu(safeGet(log.getPcu()))
                    .shiftId(safeGet(log.getShift_id()))
                    .shiftCreatedDatetime(Optional.ofNullable(log.getShift_created_datetime()).orElse(currentTime))
                    .workcenterId(safeGet(log.getWorkcenter_id()))
                    .resourceId(safeGet(log.getResource_id()))
                    .item(safeGet(log.getItem()))
                    .itemVersion(safeGet(log.getItem_version()))
                    .operation(safeGet(log.getOperation()))
                    .operationVersion(safeGet(log.getOperation_version()))
                    .shopOrderBO(safeGet(log.getShop_order_bo()))
                    .batchNumber(getBatchNumber(log))
                    .plannedOutput(partsToBeProduced != null ? partsToBeProduced : 0.0)
                    .actualOutput(actualPartsProduced != null ? actualPartsProduced : 0)
                    .performancePercentage(performancePercentage)
                    .plannedCycleTime(safeGet(cycleTime.getPlannedCycleTime(), 0.0))
                    .actualCycleTime(safeGet(cycleTime.getManufacturedTime(), 0.0))
                    .createdDatetime(currentTime)
                    .updatedDatetime(currentTime)
                    .downtimeDuration(safeGet(availability.getDowntime(), 0.0))
                    .performanceEfficiency(performanceEfficiency)
                    .active(1)
                    .build();

            // Add entity to list
            performanceEntities.add(entity);
        }

        // Step 4: Save all entities in batch
        if (!performanceEntities.isEmpty()) {
            performanceRepository.saveAll(performanceEntities);
            created = true; // Mark as created
        }

        return created;
    }


    // Utility methods
    private String safeGet(String value) {
        return value != null ? value : "";
    }

    private Double safeGet(Double value, Double defaultValue) {
        return value != null ? value : defaultValue;
    }

    private String getBatchNumber(ProductionLog log) {
        return (log.getBatchNo() != null && !log.getBatchNo().isEmpty()) ? log.getBatchNo() : safeGet(log.getPcu());
    }

    private double roundToTwoDecimalPlaces(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private String generateKey(CycleTimePostgres cycleTime) {
        StringBuilder keyBuilder = new StringBuilder(cycleTime.getSite());

        // Add keys with highest specificity first
        if (cycleTime.getOperation() != null && cycleTime.getOperationVersion() != null) {
            keyBuilder.append("-").append(cycleTime.getOperation()).append("-").append(cycleTime.getOperationVersion());
        } else if (cycleTime.getItem() != null && cycleTime.getItemVersion() != null) {
            keyBuilder.append("-").append(cycleTime.getItem()).append("-").append(cycleTime.getItemVersion());
        }

        if (cycleTime.getResourceId() != null) keyBuilder.append("-").append(cycleTime.getResourceId());
        if (cycleTime.getWorkcenterId() != null) keyBuilder.append("-").append(cycleTime.getWorkcenterId());

        return keyBuilder.toString().replaceAll("-{2,}", "-").replaceAll("-$", "");
    }

    private CycleTimePostgres getCycleTimeForLog(ProductionLog log, Map<String, CycleTimePostgres> cycleTimeMap) {
        // Construct keys to check in order of priority
        List<String> keysToCheck = List.of(
                log.getSite() + "-" + log.getOperation() + "-" + log.getOperation_version(),
                log.getSite() + "-" + log.getItem() + "-" + log.getItem_version() + "-" + log.getResource_id() + "-" + log.getWorkcenter_id() + "-" + log.getOperation() + "-" + log.getOperation_version(),
                log.getSite() + "-" + log.getItem() + "-" + log.getItem_version() + "-" + log.getResource_id() + "-" + log.getWorkcenter_id(),
                log.getSite() + "-" + log.getItem() + "-" + log.getItem_version() + "-" + log.getResource_id(),
                log.getSite() + "-" + log.getResource_id(),
                log.getSite() + "-" + log.getItem() + "-" + log.getItem_version()
        );

        for (String key : keysToCheck) {
            CycleTimePostgres cycleTime = cycleTimeMap.get(key);
            if (cycleTime != null) {
                return cycleTime;
            }
        }

        return new CycleTimePostgres(); // Return default empty cycle time if no match is found
    }

    private OverallAvailabilityResponse findAvailabilityForLog(ProductionLog log, List<OverallAvailabilityResponse> availabilityData) {
        return availabilityData.stream()
                .filter(availability -> availability.getSite().equals(log.getSite()) && availability.getResourceId().equals(log.getResource_id()))
                .findFirst()
                .orElse(null); // Return null if no matching availability found
    }

    private Double calculatePartsToBeProduced(Double availableTimeInSecs, Double cycleTime) {
        return cycleTime == null || cycleTime == 0 ? 0.0 : (availableTimeInSecs * 60) / cycleTime;
    }

    public List<OverallAvailabilityResponse> findAvailability(PerformanceRequest performanceRequest) {
        AvailabilityRequest availabilityRequest = new AvailabilityRequest();
        availabilityRequest.setSite(performanceRequest.getSite());
        availabilityRequest.setStartDateTime(performanceRequest.getStartDateTime());
        availabilityRequest.setEndDateTime(performanceRequest.getEndDateTime());

        return webClientBuilder.build()
                .post()
                .uri(getAvailabilityRecUrl)
                .body(BodyInserters.fromValue(availabilityRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<OverallAvailabilityResponse>>() {})
                .block();
    }

    public List<CycleTimePostgres> findCycleTimePostgres(String site) {
        CycleTimeRequest cycleTimeRequest = new CycleTimeRequest();
        cycleTimeRequest.setSite(site);
        List<CycleTimePostgres> cycleTimePostgres=webClientBuilder.build()
                .post()
                .uri(getCycleTimeRecsUrl)
                .body(BodyInserters.fromValue(cycleTimeRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<CycleTimePostgres>>() {})
                .block();
        return cycleTimePostgres;
    }

    public List<ProductionLog> getActualPartsProduced(PerformanceRequest performanceRequest) {
        ProductionLogRequest productionLogRequest = new ProductionLogRequest();
        productionLogRequest.setSite(performanceRequest.getSite());
        productionLogRequest.setStartDateTime(performanceRequest.getStartDateTime());
        productionLogRequest.setEndDateTime(performanceRequest.getEndDateTime());
        productionLogRequest.setEventType("completeSfcBatch");
        return webClientBuilder.build()
                .post()
                .uri(getTotalProducedQuantityUrl)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductionLog>>() {})
                .block();
    }

    private List<ProductionLog> getProductionLogList(ProductionLogRequest productionLogRequest){


        List<ProductionLog> combinationResponse = webClientBuilder.build()
                .post()
                .uri(productionlog)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductionLog>>() {
                })
                .block();

       return  combinationResponse;
    }


    public String calculatePerformance1(PerformanceRequest performanceRequest) {

        String performanceCalculationMessage = "empty record";

        PerformanceRequest request = PerformanceRequest.builder()
                .site(performanceRequest.getSite())
                .shiftIds(performanceRequest.getShiftIds())
                .startDateTime(performanceRequest.getStartDateTime())
                .endDateTime(performanceRequest.getEndDateTime())
                .build();


        List<OeePerformanceEntity> performanceResponse = new ArrayList<>();
        try {
            performanceResponse = webClientBuilder
                    .build()
                    .post()
                    .uri(getCalculatedPerformance)
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<OeePerformanceEntity>>() {
                    })
                    .block();

        } catch (Exception e) {
            throw e;
        }

        if(performanceResponse != null && performanceResponse.size() > 0) {
            List<OeePerformanceEntity> savedEntities = performanceRepository.saveAll(performanceResponse);

            if (savedEntities.isEmpty()) {
                performanceCalculationMessage = "Performance calculation failed";
            } else {
                performanceCalculationMessage = "Performance calculated successfully for " + savedEntities.size() + " records.";
            }
        }

        return performanceCalculationMessage;
    }


    @Override
    public List<PerformanceResponseDto> performanceUniqueComb(OeeFilterRequest request) throws Exception{
        return performanceRepository.findUniqueCombinations(request.getStartTime(), request.getSite());
    }

    @Override
    public List<OeePerformanceEntity> performanceByDateRange(OeeFilterRequest request){
       return performanceRepository.findByCreatedDatetimeBetweenAndSiteAndActive(request.getStartDateTime(),request.getEndDateTime(),request.getSite(),1);
    }

    /*@Override
    public PerformanceByEfficiencyOfProduct getPerformanceByEfficiencyOfProduct(OeeFilterRequest request) throws Exception {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new QualityException(1004);
        }

        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();

        queryParameters.put("site", request.getSite());
        queryBuilder.append("SELECT p.item AS itemVal, p.item_version AS itemVersion, ")
                .append("SUM(p.performance_efficiency) AS performanceEfficiency, ")
                .append("MIN(p.interval_start_date_time) AS firstRecordTime, ")
                .append("MAX(p.interval_end_date_time) AS lastRecordTime ")
                .append("FROM R_PERFORMANCE p ")
                .append("WHERE p.site = :site ");

        applyCommonFilters(queryBuilder, queryParameters, request);
        queryBuilder.append("GROUP BY p.item, p.item_version");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceByEfficiencyOfProduct response = new PerformanceByEfficiencyOfProduct();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());
            response.setResourceId(request.getResourceId() != null ? String.join(", ", request.getResourceId()) : null);

            List<PerformanceByEfficiencyOfProduct.PerformanceData> performanceList = results.stream()
                    .map(result -> {
                        PerformanceByEfficiencyOfProduct.PerformanceData performanceData =
                                new PerformanceByEfficiencyOfProduct.PerformanceData();

                        performanceData.setItemVal(result[0] != null ? (String) result[0] : "");
                        performanceData.setItemVersion(result[1] != null ? (String) result[1] : "");

                        performanceData.setPerformanceEfficiency(
                                result[2] != null
                                        ? (result[2] instanceof BigDecimal
                                        ? ((BigDecimal) result[2]).doubleValue()
                                        : (result[2] instanceof Double
                                        ? (Double) result[2]
                                        : Double.parseDouble(result[2].toString())))
                                        : 0.0
                        );

                        String interval = formatInterval((Timestamp) result[3], (Timestamp) result[4], request);
                        performanceData.setInterval(interval);

                        return performanceData;
                    })
                    .collect(Collectors.toList());

            response.setPerformanceByEfficiencyOfProduct(performanceList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
*/

    @Override
    public PerformanceByEfficiencyOfProduct getPerformanceByEfficiencyOfProduct(OeeFilterRequest request) {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new PerformanceException(1004);
        }

        StringBuilder sql = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        sql.append("SELECT p.item, p.resource_id, ")
                .append("SUM(p.actual_output) AS actualQty, ")
                .append("SUM(p.planned_output) AS totalPlannedQty ")
                .append("FROM r_performance p ")
                .append("WHERE p.site = :site ")
                .append("AND p.batch_number IS NOT NULL ")
                .append("AND p.batch_number <> '' ")
                .append("AND p.item IS NOT NULL ")
                .append("AND p.item <> '' ")
                .append("AND p.resource_id IS NOT NULL ")  // Ensure resource_id is not null
                .append("AND p.resource_id <> '' ")
                .append("AND (p.planned_output > 0 OR p.actual_output > 0) ");

        applyCommonFilters(sql, queryParameters, request);
        sql.append("GROUP BY p.item, p.resource_id ")
                .append("ORDER BY p.item, p.resource_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            if (results.isEmpty()) {
                return new PerformanceByEfficiencyOfProduct(Collections.emptyList());
            }

            List<PerformanceByEfficiencyOfProduct.PerformanceData> performanceDataList = new ArrayList<>();

            for (Object[] row : results) {

                String item = row[0] != null ? row[0].toString() : "";
                String resourceId = row[1] != null ? row[1].toString() : "";
                double actualQty = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
                double totalPlannedQty = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

                if (totalPlannedQty == 0.0) continue; // Avoid division by zero

                double performancePercentage = Math.round((actualQty / totalPlannedQty) * 100.0 * 100.0) / 100.0;

                if (performancePercentage > 0) { // Avoid filtering small values incorrectly
                    performanceDataList.add(new PerformanceByEfficiencyOfProduct.PerformanceData(item, resourceId, performancePercentage));
                }
            }

            return new PerformanceByEfficiencyOfProduct(performanceDataList);

        } catch (Exception e) {
            e.printStackTrace();
            return new PerformanceByEfficiencyOfProduct(Collections.emptyList());
        }
    }

    /*@Override
    public PerformanceByEfficiencyOfProduct getPerformanceByEfficiencyOfProduct(OeeFilterRequest request) throws Exception {

        OeeRequest oeeRequest = buildOeeRequest(request);

        PerformanceByEfficiencyOfProduct performanceByEfficiencyOfProduct = null;

        if (request.getSite() != null && request.getShiftId() == null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<BatchDetails> oeeResponse = getOeeDetailsByBatchNo(oeeRequest);
            performanceByEfficiencyOfProduct = mapPerformanceEfficiencyData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftAndSite(oeeRequest);  // Site + Shift
            performanceByEfficiencyOfProduct = mapPerformanceEfficiencyData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<WorkcenterDetails> oeeResponse = getOeeDetailsByWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter
            performanceByEfficiencyOfProduct = mapPerformanceEfficiencyData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource
            performanceByEfficiencyOfProduct = mapPerformanceEfficiencyData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() != null) {

            List<BatchDetails> oeeResponse = getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource + Batch
            performanceByEfficiencyOfProduct = mapPerformanceEfficiencyData(oeeResponse);

        } else {

            List<SiteDetails> oeeResponse = getAllOeeDetails(oeeRequest);  // Default fallback case if no filters match
            performanceByEfficiencyOfProduct = mapPerformanceEfficiencyData(oeeResponse);
        }

        return performanceByEfficiencyOfProduct;
    }

    private <T> PerformanceByEfficiencyOfProduct mapPerformanceEfficiencyData(List<T> efficiencyDetails) {

        List<PerformanceByEfficiencyOfProduct.PerformanceData> performanceByEfficiencyList = efficiencyDetails.stream()
                .map(detail -> {
                    double performanceEfficiency = 0.0;
                    String item = null;

                    // Extract item and performanceEfficiency based on object type
                    if (detail instanceof ResourceDetails) {
                        ResourceDetails resourceDetails = (ResourceDetails) detail;
                        performanceEfficiency = resourceDetails.getPerformance();
                        item = resourceDetails.getItem();
                    } else if (detail instanceof WorkcenterDetails) {
                        WorkcenterDetails workcenterDetails = (WorkcenterDetails) detail;
                        performanceEfficiency = workcenterDetails.getPerformance();
                        item = workcenterDetails.getItem();
                    }  else if (detail instanceof BatchDetails) {
                        BatchDetails batchDetails = (BatchDetails) detail;
                        performanceEfficiency = batchDetails.getPerformance();
                        item = batchDetails.getItem();
                    } else if (detail instanceof ShiftDetails) {
                        ShiftDetails shiftDetails = (ShiftDetails) detail;
                        performanceEfficiency = shiftDetails.getPerformance();
                        item = shiftDetails.getItem();
                    } else if (detail instanceof SiteDetails) {
                        SiteDetails siteDetails = (SiteDetails) detail;
                        performanceEfficiency = siteDetails.getPerformance();
                        item = siteDetails.getItem();
                    }

                    return new PerformanceByEfficiencyOfProduct.PerformanceData(item, performanceEfficiency);
                })
                .collect(Collectors.toList());

        return PerformanceByEfficiencyOfProduct.builder()
                .performanceByEfficiencyOfProduct(performanceByEfficiencyList)
                .build();
    }*/

    @Override
    public PerformanceLossReasonsResponse getPerformanceLossReasons(OeeFilterRequest request) throws Exception {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new QualityException(1004);
        }

        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT d.reason AS reason, ")
                .append("SUM(p.actual_output) AS totalGoodQty, ")
                .append("SUM(p.planned_output) AS totalPlannedQty, ")
                .append("COUNT(d.reason) AS occurrence ")
                .append("FROM R_DOWNTIME d ")
                .append("INNER JOIN R_PERFORMANCE p ON d.resource_id = p.resource_id ")
                .append("WHERE p.site = :site ")
                .append("AND d.reason IS NOT NULL ")
                .append("AND p.batch_number IS NOT NULL ")
                .append("AND p.batch_number <> '' ")
                .append("AND (p.planned_output > 0 OR p.actual_output > 0) ");

        applyCommonFilters(queryBuilder, queryParameters, request);
        queryBuilder.append("GROUP BY d.reason");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            PerformanceLossReasonsResponse response = new PerformanceLossReasonsResponse();

            List<PerformanceLossReasonsResponse.PerformanceLossReason> performanceLossList = results.stream()
                    .map(result -> {
                        String reason = (result[0] != null) ? (String) result[0] : "";
                        double totalGoodQty = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
                        double totalPlannedQty = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;
                        int occurrence = (result[3] instanceof Number) ? ((Number) result[3]).intValue() : 0;

                        double performancePercentage = (totalPlannedQty > 0) ? (totalGoodQty / totalPlannedQty) * 100 : 0.0;
                        performancePercentage = Math.round(performancePercentage * 100.0) / 100.0;

                        if (performancePercentage == 0.0) return null;

                        PerformanceLossReasonsResponse.PerformanceLossReason lossReason = new PerformanceLossReasonsResponse.PerformanceLossReason();
                        lossReason.setReason(reason);
                        lossReason.setPerformancePercentage(performancePercentage);
                        lossReason.setOccurrence(occurrence);

                        return lossReason;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            response.setPerformanceLossReason(performanceLossList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to format the interval string
    private String formatInterval(Timestamp firstRecordTimestamp, Timestamp lastRecordTimestamp, OeeFilterRequest request) {
        if (firstRecordTimestamp != null && lastRecordTimestamp != null) {
            return firstRecordTimestamp.toLocalDateTime() + " to " + lastRecordTimestamp.toLocalDateTime();
        } else {
            // Default to the request times if no record timestamps are found
            return request.getStartTime() + " to " + request.getEndTime();
        }
    }

    private void applyCommonFilters(StringBuilder queryBuilder, Map<String, Object> queryParameters, OeeFilterRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null) {
            queryParameters.put("startTime", request.getStartTime());
            queryParameters.put("endTime", request.getEndTime());
            queryBuilder.append("AND p.interval_start_date_time <= :endTime ")
                    .append("AND p.interval_end_date_time >= :startTime ");
        } else {
            LocalDateTime now = LocalDateTime.now();
            //LocalDateTime startTime = now.minusHours(24);
            ShiftRequest shiftreq= new ShiftRequest();
            shiftreq.setSite(request.getSite());
            LocalDateTime startTime=  oeeService.getEarliestValidShiftStartDateTime(shiftreq);

            // Set default startTime and endTime in the request object
            request.setStartTime(startTime);
            request.setEndTime(now);

            queryParameters.put("startTime", startTime);
            queryParameters.put("endTime", now);

            queryBuilder.append("AND p.interval_start_date_time <= :endTime ")
                    .append("AND p.interval_end_date_time >= :startTime ");
        }

        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            queryBuilder.append("AND p.resource_id IN :resourceId ");
            queryParameters.put("resourceId", request.getResourceId());
        }

        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            queryBuilder.append("AND p.shift_id IN :shiftId ");
            queryParameters.put("shiftId", request.getShiftId());
        }

        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            queryBuilder.append("AND p.workcenter_id IN :workcenterId ");
            queryParameters.put("workcenterId", request.getWorkcenterId());
        }

        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
            queryBuilder.append("AND p.batch_number IN :batchNumber ");
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
                queryBuilder.append("AND p.item IN (:itemBO) ");
                queryParameters.put("itemBO", itemBOList);
            }

            if (!itemVersionList.isEmpty()) {
                queryBuilder.append("AND p.item_version IN (:itemVersion) ");
                queryParameters.put("itemVersion", itemVersionList);
            }
        }*/
    }

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

    private List<BatchDetails> getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(OeeRequest oeeRequest){
        return oeeService.getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(oeeRequest);
    }

    private List<ResourceDetails> getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(OeeRequest oeeRequest){
        return oeeService.getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(oeeRequest);
    }

    private List<WorkcenterDetails> getOeeDetailsByWorkcenterAndShiftAndSite(OeeRequest oeeRequest){
        return oeeService.getOeeDetailsByWorkcenterAndShiftAndSite(oeeRequest);
    }

    private List<ShiftDetails> getOeeDetailsByShiftAndSite(OeeRequest oeeRequest){
        return oeeService.getOeeDetailsByShiftAndSite(oeeRequest);
    }

    private SiteDetails getOeeDetailBySite(OeeRequest oeeRequest){
        return oeeService.getOeeDetailBySite(oeeRequest);
    }

    private List<SiteDetails> getAllOeeDetails(OeeRequest oeeRequest){
        return oeeService.getAllOeeDetails(oeeRequest);
    }

    private List<ShiftDetails> getOeeDetailsByShiftId(OeeRequest oeeRequest) {
        return oeeService.getOeeDetailsByShiftId(oeeRequest);
    }

    private List<ResourceDetails> getOeeDetailsByResourceId(OeeRequest oeeRequest) {
        return oeeService.getOeeDetailsByResourceId(oeeRequest);
    }

    private List<WorkcenterDetails> getOeeDetailsByWorkCenterId(OeeRequest oeeRequest) {
        return oeeService.getOeeDetailsByWorkCenterId(oeeRequest);
    }

    private List<BatchDetails> getOeeDetailsByBatchNo(OeeRequest oeeRequest) {
        return oeeService.getOeeDetailsByBatchNo(oeeRequest);
    }
}
