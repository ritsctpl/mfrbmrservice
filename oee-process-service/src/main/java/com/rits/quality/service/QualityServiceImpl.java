package com.rits.quality.service;

import com.rits.common.dto.OeeFilterRequest;
import com.rits.oeeservice.dto.*;
import com.rits.oeeservice.service.OeeService;
import com.rits.performance.dto.PerformanceRequest;
import com.rits.performance.dto.ProductionLog;
import com.rits.quality.dto.*;
import com.rits.quality.dto.ShiftRequest;
import com.rits.quality.exception.QualityException;
import com.rits.quality.model.ProductionQuality;
import com.rits.quality.repository.QualityRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualityServiceImpl implements QualityService {

    @PersistenceContext
    private EntityManager entityManager;
    private final QualityRepository qualityRepository;
    private final WebClient.Builder webClientBuilder;

    @Autowired
    private final OeeService oeeService;
    @Autowired
    QualityServiceImpl qualityService;

    @Value("${productionlog-service.url}/getCalculatedQuality")
    private String productionlog;
    @Value("${productionlog-service.url}/getByInterval")
    private String getByIntervalUrl;
    @Value("${productionlog-service.url}/getScrapAndReworkForResource")
    private String getScrapAndReworkForResourceLog;
    @Value("${productionlog-service.url}/getProductionLogByEventType")
    private String getTotalProducedQuantityUrl;

    @Value("${shift-service.url}/getShiftStartDateTimeByDate")
    private String getShiftStartDateTimeByDateUrl;
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public OverallQualityResponse getOverallQuality(OeeFilterRequest request) {

        OeeRequest oeeRequest = createOeeRequest(request);


        SiteDetails siteDetails=oeeService.getOeeDetailBySite(oeeRequest);

        OverallQualityResponse overallQualityResponse=new OverallQualityResponse();
        overallQualityResponse.setQualityPercentage(siteDetails.getQuality());
//        overallQualityResponse.setStartTime();
//        overallQualityResponse.setEndTime();
//        overallQualityResponse.setShiftId();
//        overallQualityResponse.setResourceId();

        return overallQualityResponse;
//        StringBuilder queryBuilder = new StringBuilder();
//        Map<String, Object> queryParameters = new HashMap<>();
//
//        queryParameters.put("site", request.getSite());
//        queryBuilder.append("SELECT AVG(q.quality_percentage) as qualityPercentage ")
//                .append("FROM R_QUALITY q ")
//                .append("WHERE q.site = :site ");
//        applyCommonFilters(queryBuilder, queryParameters, request);
//
//        try {
//            Query query = entityManager.createNativeQuery(queryBuilder.toString());
//            queryParameters.forEach(query::setParameter);
//            Object result = query.getSingleResult();
//            Double avgQuality = null;
//            if (result != null) {
//                if (result instanceof BigDecimal) {
//                    avgQuality = ((BigDecimal) result).doubleValue();
//                } else if (result instanceof Double) {
//                    avgQuality = (Double) result;
//                } else {
//                    avgQuality = Double.parseDouble(result.toString());
//                }
//            }
//            Double roundedAvgQuality = avgQuality != null ? Math.round(avgQuality * 100.0) / 100.0 : 0.0;
//
//            OverallQualityResponse response = new OverallQualityResponse();
//            if(request.getResourceId() != null && !request.getResourceId().isEmpty()) {
//                response.setResourceId(String.join(", ", request.getResourceId()));
//            }
//            if(request.getShiftId() != null && !request.getShiftId().isEmpty()) {
//                response.setShiftId(String.join(", ", request.getShiftId()));
//            }
//            response.setQualityPercentage(roundedAvgQuality);
//            return response;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

//    public List<Predicate> whereConditions(OeeFilterRequest request, CriteriaBuilder cb, Root<ProductionQuality> root){
//
//        List<Predicate> predicates = new ArrayList<>();
//
//        predicates.add(cb.between(root.get("createdDateTime"), request.getStartTime(), request.getEndTime()));
//
//        if (StringUtils.isNotEmpty(request.getSite())) {
//            predicates.add(root.get("site").in(request.getSite()));
//        }
//        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
//            predicates.add(root.get("resourceId").in(request.getResourceId()));
//        }
//        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
//            predicates.add(root.get("shiftId").in(request.getShiftId()));
//        }
//        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
//            predicates.add(root.get("workcenterId").in(request.getWorkcenterId()));
//        }
//        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
//            predicates.add(root.get("batchNumber").in(request.getBatchNumber()));
//        }
//
//        if (request.getItem() != null && !request.getItem().isEmpty()) {
//            List<Predicate> itemPredicates = new ArrayList<>();
//
//            for (Item item : request.getItem()) {
//                itemPredicates.add(
//                        cb.and(
//                                cb.equal(root.get("item"), item.getItem()),
//                                cb.equal(root.get("itemVersion"), item.getItemVersion())
//                        )
//                );
//            }
//            predicates.add(cb.or(itemPredicates.toArray(new Predicate[0])));
//        }
//
//        if (request.getOperation() != null && !request.getOperation().isEmpty()) {
//            List<Predicate> operationPredicates = new ArrayList<>();
//
//            for (Operation operation : request.getOperation()) {
//                operationPredicates.add(
//                        cb.and(
//                                cb.equal(root.get("operation"), operation.getOperation()),
//                                cb.equal(root.get("operationVersion"), operation.getOperationVersion())
//                        )
//                );
//            }
//            predicates.add(cb.or(operationPredicates.toArray(new Predicate[0])));
//        }
//        return predicates;
//    }

    @Override
    public QualityByTimeResponse getQualityByTime(OeeFilterRequest request) {

        OeeRequest oeeRequest = createOeeRequest(request);

        List<ResourceDetails> resourceDetails=oeeService.getOeeDetailsByResourceId(oeeRequest);

        // Initialize the list for the response
        List<QualityByTimeResponse.QualityOverTime> qualityOverTimeList = new ArrayList<>();

        // Loop through each ResourceDetails and map to QualityByTimeResponse
        for (ResourceDetails resourceDetail : resourceDetails) {

            // Create the QualityOverTime object for each resource
            QualityByTimeResponse.QualityOverTime qualityOverTime = new QualityByTimeResponse.QualityOverTime();
            qualityOverTime.setDate(resourceDetail.getBatchNo());  // Assuming batchNo is the date or can be used as one
            qualityOverTime.setQualityPercentage(resourceDetail.getQuality());

            // Add to the list of qualityOverTime
            qualityOverTimeList.add(qualityOverTime);


        }

        // Create and return the final QualityByTimeResponse
        return QualityByTimeResponse.builder()
                .startTime(request.getStartTime()) // Start time from the filter request
                .endTime(request.getEndTime()) // End time from the filter request
                .qualityOverTime(qualityOverTimeList) // List of QualityOverTime
                .build();


//        StringBuilder queryBuilder = new StringBuilder();
//        Map<String, Object> queryParameters = new HashMap<>();
//
//        queryParameters.put("site", request.getSite());
//        queryBuilder.append("SELECT q.created_date_time, AVG(q.quality_percentage) as qualityPercentage ")
//                .append("FROM R_QUALITY q ")
//                .append("WHERE q.site = :site ");
//
//        applyCommonFilters(queryBuilder, queryParameters, request);
//        queryBuilder.append("GROUP BY q.created_date_time ORDER BY q.created_date_time");
//
//        try {
//            Query query = entityManager.createNativeQuery(queryBuilder.toString());
//            queryParameters.forEach(query::setParameter);
//            List<Object[]> results = query.getResultList();
//
//            List<QualityByTimeResponse.QualityOverTime> qualityList = results.stream()
//                    .map(result -> {
//                        QualityByTimeResponse.QualityOverTime qualityData =
//                                new QualityByTimeResponse.QualityOverTime();
//                        qualityData.setDate(result[0] != null ?
//                                (result[0] instanceof Timestamp ?
//                                        ((Timestamp) result[0]).toLocalDateTime().toString() :
//                                        result[0].toString()) :
//                                "");
//                        qualityData.setQualityPercentage(result[1] != null ?
//                                (result[1] instanceof BigDecimal ?
//                                        ((BigDecimal) result[1]).doubleValue() :
//                                        (result[1] instanceof Double ?
//                                                (Double) result[1] :
//                                                Double.parseDouble(result[1].toString()))) :
//                                0.0);
//                        return qualityData;
//                    })
//                    .collect(Collectors.toList());
//
//            return QualityByTimeResponse.builder()
//                    .startTime(request.getStartTime())
//                    .endTime(request.getEndTime())
//                    .qualityOverTime(qualityList)
//                    .build();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }


    @Override
    public QualityByShiftResponse getQualityByShift(OeeFilterRequest request) {

        OeeRequest oeeRequest = createOeeRequest(request);


        // Now call the getOeeDetailsByShiftAndSite method
        List<ShiftDetails> shiftDetails = oeeService.getOeeDetailsByShiftId(oeeRequest);

        // Convert ShiftDetails to ShiftQuality
        List<QualityByShiftResponse.ShiftQuality> shiftQualityList = shiftDetails.stream()
                .map(shiftDetail -> QualityByShiftResponse.ShiftQuality.builder()
                        .shiftId(shiftDetail.getShift()) // assuming the shift ID is stored in the 'shift' field
                        .qualityPercentage(shiftDetail.getQuality()) // A method to calculate quality percentage
                        .build())
                .collect(Collectors.toList());

        // Prepare the final response
        return QualityByShiftResponse.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .qualityByShift(shiftQualityList)
                .build();

//        StringBuilder queryBuilder = new StringBuilder();
//        Map<String, Object> queryParameters = new HashMap<>();
//
//        queryParameters.put("site", request.getSite());
//        queryBuilder.append("SELECT q.shift_id as shiftId, AVG(q.quality_percentage) as qualityPercentage ")
//                .append("FROM R_QUALITY q ")
//                .append("WHERE q.site = :site ");
//
//        applyCommonFilters(queryBuilder, queryParameters, request);
//
//        queryBuilder.append("GROUP BY q.shift_id ")
//                .append("ORDER BY q.shift_id");
//
//        try {
//            Query query = entityManager.createNativeQuery(queryBuilder.toString());
//            queryParameters.forEach(query::setParameter);
//            List<Object[]> results = query.getResultList();
//
//            List<QualityByShiftResponse.ShiftQuality> qualityList = results.stream()
//                    .map(result -> QualityByShiftResponse.ShiftQuality.builder()
//                            .shiftId(result[0] != null ? result[0].toString().split(",")[2] : "")
//                            .qualityPercentage(result[1] != null ?
//                                    (result[1] instanceof BigDecimal ?
//                                            ((BigDecimal) result[1]).doubleValue() :
//                                            (result[1] instanceof Double ?
//                                                    (Double) result[1] :
//                                                    Double.parseDouble(result[1].toString()))) :
//                                    0.0)
//                            .build())
//                    .collect(Collectors.toList());
//
//            return QualityByShiftResponse.builder()
//                    .startTime(request.getStartTime())
//                    .endTime(request.getEndTime())
//                    .qualityByShift(qualityList)
//                    .build();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public QualityByMachineResponse getQualityByMachine(OeeFilterRequest request) {

        OeeRequest oeeRequest = createOeeRequest(request);


        List<ResourceDetails> resourceDetails=oeeService.getOeeDetailsByResourceId(oeeRequest);
        // Convert ResourceDetails to MachineQuality
        List<QualityByMachineResponse.MachineQuality> machineQualityList = resourceDetails.stream()
                .map(resourceDetail -> QualityByMachineResponse.MachineQuality.builder()
                        .resourceId(resourceDetail.getResource()) // assuming 'resource' is the field that corresponds to 'resourceId'
                        .qualityPercentage(resourceDetail.getQuality())
                        .build())
                .collect(Collectors.toList());

        // Prepare the final response
        return QualityByMachineResponse.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .qualityByMachine(machineQualityList)
                .build();



//        StringBuilder queryBuilder = new StringBuilder();
//        Map<String, Object> queryParameters = new HashMap<>();
//
//        queryParameters.put("site", request.getSite());
//        queryBuilder.append("SELECT q.resource_id as resourceId, AVG(q.quality_percentage) as qualityPercentage ")
//                .append("FROM R_QUALITY q ")
//                .append("WHERE q.site = :site ");
//
//        applyCommonFilters(queryBuilder, queryParameters, request);
//
//        queryBuilder.append("GROUP BY q.resource_id ")
//                .append("ORDER BY q.resource_id");
//
//        try {
//            Query query = entityManager.createNativeQuery(queryBuilder.toString());
//            queryParameters.forEach(query::setParameter);
//            List<Object[]> results = query.getResultList();
//
//            List<QualityByMachineResponse.MachineQuality> qualityList = results.stream()
//                    .map(result -> QualityByMachineResponse.MachineQuality.builder()
//                            .resourceId(result[0] != null ? result[0].toString() : "")
//                            .qualityPercentage(result[1] != null ?
//                                    (result[1] instanceof BigDecimal ?
//                                            ((BigDecimal) result[1]).doubleValue() :
//                                            (result[1] instanceof Double ?
//                                                    (Double) result[1] :
//                                                    Double.parseDouble(result[1].toString()))) :
//                                    0.0)
//                            .build())
//                    .collect(Collectors.toList());
//
//            return QualityByMachineResponse.builder()
//                    .startTime(request.getStartTime())
//                    .endTime(request.getEndTime())
//                    .qualityByMachine(qualityList)
//                    .build();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public QualityByProductResponse getQualityByProduct(OeeFilterRequest request) {

        OeeRequest oeeRequest = createOeeRequest(request);


        List<BatchDetails> batchDetails=oeeService.getOeeDetailsByBatchNo(oeeRequest);

        List<QualityByProductResponse.ProductQuality> productQualityList = new ArrayList<>();

        // Loop through each BatchDetails and map it to ProductQuality
        for (BatchDetails batchDetail : batchDetails) {
            // Calculate quality percentage (good / total)
            double totalQualityCount = batchDetail.getGoodQualityCount();
            double qualityPercentage = totalQualityCount == 0 ? 0.0 : (double) batchDetail.getGoodQualityCount() ;

            // Create ProductQuality for each batch
            QualityByProductResponse.ProductQuality productQuality = QualityByProductResponse.ProductQuality.builder()
                    .itemBo(batchDetail.getItem()) // Assuming item maps to itemBo
                    .resourceId(request.getResource()) // Resource ID from the filter request
                    .qualityPercentage(totalQualityCount) // Calculated quality percentage
                    .build();

            // Add the ProductQuality object to the list
            productQualityList.add(productQuality);
        }

        // Create and return the final QualityByProductResponse
        return QualityByProductResponse.builder()
                .startTime(request.getStartTime()) // Start time from the filter request
                .endTime(request.getEndTime()) // End time from the filter request
                .qualityByProduct(productQualityList) // List of ProductQuality
                .build();

//        StringBuilder queryBuilder = new StringBuilder();
//        Map<String, Object> queryParameters = new HashMap<>();
//
//        queryParameters.put("site", request.getSite());
//        queryBuilder.append("SELECT q.resource_id as resourceId, AVG(q.quality_percentage) as qualityPercentage, ")
//                .append("q.item as item, q.item_version as itemVersion ")
//                .append("FROM R_QUALITY q ")
//                .append("WHERE q.site = :site ");
//
//        applyCommonFilters(queryBuilder, queryParameters, request);
//
//        queryBuilder.append("GROUP BY q.resource_id, q.item, q.item_version ")
//                .append("ORDER BY q.resource_id");
//
//        try {
//            Query query = entityManager.createNativeQuery(queryBuilder.toString());
//            queryParameters.forEach(query::setParameter);
//            List<Object[]> results = query.getResultList();
//
//            List<QualityByProductResponse.ProductQuality> qualityList = results.stream()
//                    .map(result -> {
//                        String resourceId = result[0] != null ? result[0].toString() : "";
//                        Double qualityPercentage = result[1] != null ?
//                                (result[1] instanceof BigDecimal ?
//                                        ((BigDecimal) result[1]).doubleValue() :
//                                        (result[1] instanceof Double ?
//                                                (Double) result[1] :
//                                                Double.parseDouble(result[1].toString()))) :
//                                0.0;
//                        String item = result[2] != null ? result[2].toString() : "";
//                        String itemVersion = result[3] != null ? result[3].toString() : "";
//
//                        return QualityByProductResponse.ProductQuality.builder()
//                                .itemBo(item + " " + itemVersion)
//                                .resourceId(resourceId)
//                                .qualityPercentage(qualityPercentage)
//                                .build();
//                    })
//                    .collect(Collectors.toList());
//
//            return QualityByProductResponse.builder()
//                    .startTime(request.getStartTime())
//                    .endTime(request.getEndTime())
//                    .qualityByProduct(qualityList)
//                    .build();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

//    private List<String> retrieveItems(String site, List<String> items) {
//        List<String> formattedItems = new ArrayList<>();
//
//        for (String item : items) {
//            String[] itemParts = item.split("/");
//
//            String itemName = itemParts[0];
//            String itemVersion = itemParts[1];
//
//            formattedItems.add("ItemBO:" + site + "," + itemName + "," + itemVersion);
//        }
//        return formattedItems;
//    }
//
//    private String convertToItemFormat(String formattedItem) {
//        String[] parts = formattedItem.split(",");
//
//        String itemName = parts[1];
//        String itemVersion = parts[2];
//
//        return itemName + "/" + itemVersion;
//    }

    @Override
    public DefectsByReasonResponse getDefectsByReason(OeeFilterRequest request) {
        String eventTypeOfPerformance = null;
        if("MACHINE".equalsIgnoreCase(request.getEventSource()))
            eventTypeOfPerformance = "machineCompleteSfcBatch";
        else
            eventTypeOfPerformance = "completeSfcBatch";

        if ((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null) {
            if("MACHINE".equalsIgnoreCase(request.getEventSource()))
                eventTypeOfPerformance = "machineDoneSfcBatch";
            else
                eventTypeOfPerformance = "doneSfcBatch";
        }
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();

        queryParameters.put("site", request.getSite());
        queryParameters.put("eventTypeOfPerformance", eventTypeOfPerformance);

        queryBuilder.append("SELECT COALESCE(q.reason, 'Unknown') AS reason, ")
                .append("COUNT(*) AS occurance, ")
                .append("SUM(q.good_quantity) AS totalGoodQty, ")
                .append("SUM(q.bad_quantity) AS totalBadQty ")
                .append("FROM R_QUALITY q ")
                .append("WHERE q.site = :site ")
//                .append("AND q.batch_number IS NOT NULL AND q.batch_number <> '' ") // Ensure batch_number is valid
                .append("AND q.event_type_of_performance = :eventTypeOfPerformance ")
                .append("AND (q.plan > 0 OR q.total_quantity > 0) ");

        if ("MANUAL".equalsIgnoreCase(request.getEventSource())) {
            queryBuilder.append("AND q.batch_number IS NOT NULL AND q.batch_number <> '' ");
        }

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("GROUP BY COALESCE(q.reason, 'Unknown') ")
                .append("ORDER BY reason");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);
            List<Object[]> results = query.getResultList();

            List<DefectsByReasonResponse.ReasonDefects> qualityList = new ArrayList<>();

            for (Object[] result : results) {
                String reason = result[0] != null ? result[0].toString() : "Unknown";
                double occurance = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
                double totalGoodQty = result[2] != null ? ((Number) result[2]).doubleValue() : 0.0;
                double totalBadQty = result[3] != null ? ((Number) result[3]).doubleValue() : 0.0;

                // Calculate Quality Percentage
                double qualityPercentage = (totalGoodQty + totalBadQty > 0)
                        ? (totalBadQty / (totalGoodQty + totalBadQty)) * 100
                        : 0.0;

                // Round to two decimal places
                qualityPercentage = Math.round(qualityPercentage * 100.0) / 100.0;

                // Avoid returning 0 quality percentage records
                if (qualityPercentage == 0.0) continue;

                qualityList.add(
                        DefectsByReasonResponse.ReasonDefects.builder()
                                .reason(reason)
                                .occurance(occurance)
                                .qualityPercentage(qualityPercentage)
                                .build()
                );
            }

            return DefectsByReasonResponse.builder()
                    .defectsByReason(qualityList)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public QualityLossByProductionLineResponse getQualityLossByProductionLine(OeeFilterRequest request) {
        String eventTypeOfPerformance = null;
        if("MACHINE".equalsIgnoreCase(request.getEventSource()))
            eventTypeOfPerformance = "machineCompleteSfcBatch";
        else
            eventTypeOfPerformance = "completeSfcBatch";

        if ((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null) {
            if("MACHINE".equalsIgnoreCase(request.getEventSource()))
                eventTypeOfPerformance = "machineDoneSfcBatch";
            else
                eventTypeOfPerformance = "doneSfcBatch";
        }
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());
        queryParameters.put("eventTypeOfPerformance", eventTypeOfPerformance);

        queryBuilder.append("SELECT q.workcenter_id, q.reason, ")
                .append("SUM(q.total_quantity) AS total_qty, ")
                .append("SUM(q.bad_quantity) AS bad_qty ")
                .append("FROM R_QUALITY q ")
                .append("WHERE q.site = :site ")
//                .append("AND q.batch_number IS NOT NULL AND q.batch_number <> '' ")
                .append("AND q.event_type_of_performance = :eventTypeOfPerformance ")
                .append("AND (q.plan > 0 OR q.total_quantity > 0) ");
                //.append("AND q.workcenter_id NOTNULL AND q.workcenter_id <> '' ");

        if ("MANUAL".equalsIgnoreCase(request.getEventSource())) {
            queryBuilder.append("AND q.batch_number IS NOT NULL AND q.batch_number <> '' ");
        }
        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("GROUP BY q.workcenter_id, q.reason ")
                .append("ORDER BY q.workcenter_id");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);
            List<Object[]> results = query.getResultList();

            List<QualityLossByProductionLineResponse.LineLoss> quality = results.stream()
                    .map(result -> {
                        String workcenterVal = result[0] != null ? result[0].toString() : "Unknown";
                        String reasonVal = result[1] != null ? result[1].toString() : "Unknown";

                        double totalQuality = result[2] != null ? ((Number) result[2]).doubleValue() : 0.0;
                        double badQuality = result[3] != null ? ((Number) result[3]).doubleValue() : 0.0;

                        // Avoid 0 values
                        if (badQuality <= 0) {
                            return null;
                        }

                        // Calculate loss percentage
                        double lossPercentage = (totalQuality > 0) ? (badQuality * 100.0) / totalQuality : 0.0;

                        // Round loss percentage to 2 decimal places
                        lossPercentage = Math.round(lossPercentage * 100.0) / 100.0;

                        return QualityLossByProductionLineResponse.LineLoss.builder()
                                .workcenterId(workcenterVal)
                                .reason(reasonVal)
                                .lossPercentage(lossPercentage)
                                .build();
                    })
                    .filter(Objects::nonNull)  // Remove null values (i.e., 0 badQuality cases)
                    .collect(Collectors.toList());

            return QualityLossByProductionLineResponse.builder()
                    .qualityLossByProductionLine(quality)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public GoodVsBadQtyForResourceResponse getGoodVsBadQtyForResource(OeeFilterRequest request) {
        String eventTypeOfPerformance = null;
        if("MACHINE".equalsIgnoreCase(request.getEventSource()))
            eventTypeOfPerformance = "machineCompleteSfcBatch";
        else
            eventTypeOfPerformance = "completeSfcBatch";

        if ((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null) {
            if("MACHINE".equalsIgnoreCase(request.getEventSource()))
                eventTypeOfPerformance = "machineDoneSfcBatch";
            else
                eventTypeOfPerformance = "doneSfcBatch";
        }
        StringBuilder sql = new StringBuilder(
                "SELECT q.resource_id, " +
                        "SUM(q.good_quantity) AS totalGoodQty, " +
                        "SUM(q.bad_quantity) AS totalBadQty " +
                        "FROM R_QUALITY q " +
                        "WHERE q.site = :site " +
                        "AND q.resource_id IS NOT NULL AND q.resource_id <> '' " +
//                        "AND q.batch_number IS NOT NULL AND q.batch_number <> '' " +
                        "AND q.event_type_of_performance = :eventTypeOfPerformance " +
                        "AND (q.plan > 0 OR q.total_quantity > 0) ");
        if ("MANUAL".equalsIgnoreCase(request.getEventSource())) {
            sql.append("AND q.batch_number IS NOT NULL AND q.batch_number <> '' ");
        }

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());
        queryParameters.put("eventTypeOfPerformance", eventTypeOfPerformance);

        applyCommonFilters(sql, queryParameters, request);

        sql.append("GROUP BY q.resource_id ORDER BY q.resource_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            GoodVsBadQtyForResourceResponse response = new GoodVsBadQtyForResourceResponse();

            List<GoodVsBadQtyForResourceResponse.GoodVsBadQtyForResource> goodVsBadQtyList = results.stream()
                    .map(result -> {
                        String resourceId = (result[0] != null) ? result[0].toString() : "";
                        double goodQty = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
                        double badQty = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;

                        // Skip entries where both goodQty and badQty are zero
                        if (goodQty == 0.0 && badQty == 0.0) {
                            return null;
                        }

                        GoodVsBadQtyForResourceResponse.GoodVsBadQtyForResource goodVsBadQty =
                                new GoodVsBadQtyForResourceResponse.GoodVsBadQtyForResource();

                        goodVsBadQty.setResourceId(resourceId);
                        goodVsBadQty.setGoodQty(goodQty);
                        goodVsBadQty.setBadQty(badQty);

                        return goodVsBadQty;
                    })
                    .filter(Objects::nonNull) // Remove null entries
                    .collect(Collectors.toList());

            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());
            response.setGoodVsBadQtyForResources(goodVsBadQtyList);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching good vs bad quantity per resource", e);
        }
    }
//        StringBuilder queryBuilder = new StringBuilder();
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("site", request.getSite());
//
//        // Build the query
//        queryBuilder.append("SELECT q.resource_id, ")
//                .append("SUM(q.good_quantity) as good_qty, ")
//                .append("SUM(q.bad_quantity) as bad_qty ")
//                .append("FROM R_QUALITY q ")
//                .append("WHERE q.site = :site ");
//
//        // Apply common filters
//        applyCommonFilters(queryBuilder, queryParameters, request);
//
//        // Add group by and order by
//        queryBuilder.append("GROUP BY q.resource_id ")
//                .append("ORDER BY q.resource_id");
//
//        try {
//            Query query = entityManager.createNativeQuery(queryBuilder.toString());
//            queryParameters.forEach(query::setParameter);
//            List<Object[]> results = query.getResultList();
//
//            List<GoodVsBadQtyForResourceResponse.GoodVsBadQtyForResource> quality = results.stream()
//                    .map(result -> {
//                        String resourceVal = result[0] != null ? result[0].toString() : "";
//
//                        Double goodQuality = result[1] != null ?
//                                (result[1] instanceof BigDecimal ?
//                                        ((BigDecimal) result[1]).doubleValue() :
//                                        Double.parseDouble(result[1].toString())) :
//                                0.0;
//
//                        Double badQuality = result[2] != null ?
//                                (result[2] instanceof BigDecimal ?
//                                        ((BigDecimal) result[2]).doubleValue() :
//                                        Double.parseDouble(result[2].toString())) :
//                                0.0;
//
//                        return GoodVsBadQtyForResourceResponse.GoodVsBadQtyForResource.builder()
//                                .resourceId(resourceVal)
//                                .goodQty(goodQuality)
//                                .badQty(badQuality)
//                                .build();
//                    })
//                    .collect(Collectors.toList());
//
//            return GoodVsBadQtyForResourceResponse.builder()
//                    .goodVsBadQtyForResources(quality)
//                    .startTime(request.getStartTime())
//                    .endTime(request.getEndTime())
//                    .build();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

    /*@Override
    public ScrapAndReworkTrendResponse getScrapAndReworkTrend(OeeFilterRequest request) {

        validateStartAndEndTime(request);

        try{
            List<ScrapAndReworkTrendResponse.ScrapAndReworkTrend> qualityResponses = webClientBuilder
                    .build()
                    .post()
                    .uri(getScrapAndReworkForResourceLog)
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ScrapAndReworkTrendResponse.ScrapAndReworkTrend>>() {
                    })
                    .block();

            return ScrapAndReworkTrendResponse.builder().scrapAndReworkTrends(qualityResponses).startTime(request.getStartTime()).endTime(request.getEndTime()).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/


//    @Override
//    public QualityByOperatorResponse getQualityByOperator(OeeFilterRequest request) {
//        // Mock data for response
//        QualityByOperatorResponse response = new QualityByOperatorResponse();
//        response.setStartTime(request.getStartTime().toString());
//        response.setEndTime(request.getEndTime().toString());
//        // Add quality by operator data
//        return response;
//    }
    @Override
    public DefectByProductResponse getDefectByProduct(OeeFilterRequest request) {
        String eventTypeOfPerformance = null;
        if("MACHINE".equalsIgnoreCase(request.getEventSource()))
            eventTypeOfPerformance = "machineCompleteSfcBatch";
        else
            eventTypeOfPerformance = "completeSfcBatch";

        if ((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null) {
            if("MACHINE".equalsIgnoreCase(request.getEventSource()))
                eventTypeOfPerformance = "machineDoneSfcBatch";
            else
                eventTypeOfPerformance = "doneSfcBatch";
        }
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());
        queryParameters.put("eventTypeOfPerformance", eventTypeOfPerformance);

        queryBuilder.append("SELECT q.item, SUM(q.good_quantity) AS totalGoodQty, SUM(q.bad_quantity) AS totalBadQty ")
                .append("FROM R_QUALITY q ")
                .append("WHERE q.site = :site ")
                .append("AND q.item IS NOT NULL AND q.item <> '' ")
                .append("AND q.batch_number IS NOT NULL AND q.batch_number <> '' ")
                .append("AND q.event_type_of_performance = :eventTypeOfPerformance ")
                .append("AND (q.plan > 0 OR q.total_quantity > 0) ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("GROUP BY q.item ") // Added GROUP BY clause
                .append("ORDER BY q.item");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);
            List<Object[]> results = query.getResultList();

            List<DefectByProductResponse.ProductDefects> defectsByProduct = results.stream()
                    .map(result -> {
                        String item = result[0] != null ? result[0].toString() : "";
                        double goodQty = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
                        double badQty = result[2] != null ? ((Number) result[2]).doubleValue() : 0.0;

                        if (badQty == 0.0) {
                            return null; // Skip items with zero defects
                        }

                        double defectPercentage = (goodQty + badQty) > 0 ? (badQty * 100.0) / (goodQty + badQty) : 0.0;
                        defectPercentage = Math.round(defectPercentage * 100.0) / 100.0; // Round to 2 decimal places

                        return DefectByProductResponse.ProductDefects.builder()
                                .item(item)
                                .defectPercentage(defectPercentage)
                                .build();
                    })
                    .filter(Objects::nonNull) // Remove skipped items
                    .collect(Collectors.toList());

            return DefectByProductResponse.builder()
                    .defectsByProduct(defectsByProduct)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching product defects", e);
        }
    }

//        StringBuilder queryBuilder = new StringBuilder();
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("site", request.getSite());
//
//        // Build the query
//        queryBuilder.append("SELECT q.item, q.item_version, ")
//                .append("SUM(q.total_quantity) as total_qty, ")
//                .append("SUM(q.bad_quantity) as bad_qty ")
//                .append("FROM R_QUALITY q ")
//                .append("WHERE q.site = :site ");
//
//        // Apply common filters
//        applyCommonFilters(queryBuilder, queryParameters, request);
//
//        // Add group by and order by
//        queryBuilder.append("GROUP BY q.item, q.item_version ")
//                .append("ORDER BY q.item, q.item_version");
//
//        try {
//            Query query = entityManager.createNativeQuery(queryBuilder.toString());
//            queryParameters.forEach(query::setParameter);
//            List<Object[]> results = query.getResultList();
//
//            List<DefectByProductResponse.ProductDefects> quality = results.stream()
//                    .map(result -> {
//                        String itemVal = result[0] != null ? result[0].toString() : "";
//                        String itemVersionVal = result[1] != null ? result[1].toString() : "";
//
//                        Double totalQty = result[2] != null ?
//                                (result[2] instanceof BigDecimal ?
//                                        ((BigDecimal) result[2]).doubleValue() :
//                                        Double.parseDouble(result[2].toString())) :
//                                0.0;
//
//                        Double badQty = result[3] != null ?
//                                (result[3] instanceof BigDecimal ?
//                                        ((BigDecimal) result[3]).doubleValue() :
//                                        Double.parseDouble(result[3].toString())) :
//                                0.0;
//
//                        double defectPercentage = (totalQty > 0) ?
//                                (badQty * 100.0) / totalQty : 0.0;
//
//                        defectPercentage = Math.round(defectPercentage * 100.0) / 100.0;  // Round to 2 decimal places
//
//                        return DefectByProductResponse.ProductDefects.builder()
//                                .itemBo(itemVal + " " + itemVersionVal)
//                                .defectPercentage(defectPercentage)
//                                .build();
//                    })
//                    .collect(Collectors.toList());
//
//            return DefectByProductResponse.builder()
//                    .defectsByProduct(quality)
//                    .startTime(request.getStartTime())
//                    .endTime(request.getEndTime())
//                    .build();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

    public DefectByTimeResponse getDefectByTime(OeeFilterRequest request) {
        String eventTypeOfPerformance = "MACHINE".equalsIgnoreCase(request.getEventSource()) ?
                "machineCompleteSfcBatch" : "completeSfcBatch";

        if ((request.getBatchNumber() != null || request.getShiftId() != null || request.getItem() != null || request.getWorkcenterId() != null)
                && request.getResourceId() == null && request.getOperation() == null) {
            eventTypeOfPerformance = "MACHINE".equalsIgnoreCase(request.getEventSource()) ?
                    "machineDoneSfcBatch" : "doneSfcBatch";
        }

        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());
        queryParameters.put("eventTypeOfPerformance", eventTypeOfPerformance);

        queryBuilder.append("SELECT q.created_date_time, q.bad_quantity, q.resource_id ")
                .append("FROM R_QUALITY q ")
                .append("WHERE q.site = :site ")
                .append("AND q.event_type_of_performance = :eventTypeOfPerformance ")
                .append("AND (q.plan > 0 OR q.total_quantity > 0) ");

        if ("MANUAL".equalsIgnoreCase(request.getEventSource())) {
            queryBuilder.append("AND q.batch_number IS NOT NULL AND q.batch_number <> '' ");
        }

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("ORDER BY q.created_date_time");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);
            List<Object[]> results = query.getResultList();

            // Map<resourceId, Map<bucketStartTime, List<defectQty>>>
            Map<String, Map<LocalDateTime, List<Double>>> groupedByResourceAndHour = new HashMap<>();
            LocalDateTime minTime = null;

            for (Object[] result : results) {
                LocalDateTime dateTime = result[0] != null ? ((Timestamp) result[0]).toLocalDateTime() : null;
                if (dateTime == null) continue;

                if (minTime == null || dateTime.isBefore(minTime)) {
                    minTime = dateTime;
                }

                double defects = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
                if (defects == 0.0) continue;

                String resourceId = result[2] != null ? result[2].toString() : "UNKNOWN";

                long hoursDiff = Duration.between(minTime, dateTime).toHours();
                LocalDateTime bucketStart = minTime.plusHours(hoursDiff);

                groupedByResourceAndHour
                        .computeIfAbsent(resourceId, k -> new TreeMap<>())
                        .computeIfAbsent(bucketStart, k -> new ArrayList<>())
                        .add(defects);
            }

            List<DefectByTimeResponse.DefectResourceData> finalData = groupedByResourceAndHour.entrySet().stream()
                    .map(resourceEntry -> {
                        String resourceId = resourceEntry.getKey();
                        List<DefectByTimeResponse.DefectTrend> trendList = resourceEntry.getValue().entrySet().stream()
                                .map(hourEntry -> {
                                    double avgDefects = hourEntry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                                    return new DefectByTimeResponse.DefectTrend(
                                            hourEntry.getKey().format(dateFormatter),
                                            Math.round(avgDefects * 100.0) / 100.0
                                    );
                                })
                                .collect(Collectors.toList());

                        return new DefectByTimeResponse.DefectResourceData(resourceId, trendList);
                    })
                    .collect(Collectors.toList());

            return DefectByTimeResponse.builder()
                    .defectTrendOverTime(finalData)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .build();

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
    @Transactional
    public Boolean calculateQuality(OeeFilterRequest qualityRequest) {
        Boolean created = false;

        // Fetch current time for endDateTime
        LocalDateTime currentTime = LocalDateTime.now();
        setStartAndEndTimes(qualityRequest);

        // Prepare the request for production logs
        ProductionLogRequest productionLogRequest = new ProductionLogRequest();
        productionLogRequest.setSite(qualityRequest.getSite());
        productionLogRequest.setStartDateTime(qualityRequest.getStartDateTime());
        productionLogRequest.setEndDateTime(qualityRequest.getEndDateTime());

        // Get scrap and complete logs
        productionLogRequest.setEventType("ScrapSFC");
        List<ProductionLog> scrapLogs = getActualPartsProduced(productionLogRequest);

        productionLogRequest.setEventType("completeSfcBatch");
        List<ProductionLog> sfcCompleteLogs = getActualPartsProduced(productionLogRequest);

        // Group scrap logs by unique combinations (e.g., batchNumber, workcenterId, etc.)
        Map<String, List<ProductionLog>> scrapGroupedLogs = scrapLogs.stream()
                .collect(Collectors.groupingBy(log -> createGroupKey(log, qualityRequest)));

        // Group sfcComplete logs by the same unique combinations
        Map<String, List<ProductionLog>> completeGroupedLogs = sfcCompleteLogs.stream()
                .collect(Collectors.groupingBy(log -> createGroupKey(log, qualityRequest)));

        // List to collect quality entities
        List<ProductionQuality> qualityEntities = new ArrayList<>();

        // Iterate over each group in the complete logs
        for (Map.Entry<String, List<ProductionLog>> entry : completeGroupedLogs.entrySet()) {
            List<ProductionLog> completeGroupLogs = entry.getValue();
            String groupKey = entry.getKey();

            // Calculate actual quantity produced (from complete logs)
            int totalActualQty = completeGroupLogs.stream()
                    .mapToInt(ProductionLog::getQty)
                    .sum();

            // Get the corresponding scrap logs for the same group
            List<ProductionLog> scrapGroupLogs = scrapGroupedLogs.getOrDefault(groupKey, Collections.emptyList());

            // Calculate scrap quantity (from scrap logs)
            int totalScrapQty = scrapGroupLogs.stream()
                    .mapToInt(ProductionLog::getQty)
                    .sum();

            // Calculate quality percentage
            double qualityPercentage = totalActualQty > 0
                    ? ((double) (totalActualQty - totalScrapQty) / totalActualQty) * 100
                    : 0.0;

            // Create the ProductionQuality entity
            ProductionLog sampleLog = completeGroupLogs.get(0); // Use the first log as reference
            ProductionQuality qualityEntity = ProductionQuality.builder()
                    .site(Optional.ofNullable(sampleLog.getSite()).orElse("Default Site"))  // Use default value if null
                    .resourceId(Optional.ofNullable(sampleLog.getResource_id()).orElse("Default Resource")) // Default fallback if null
                    .workcenterId(Optional.ofNullable(sampleLog.getWorkcenter_id()).orElse("Default Workcenter")) // Default fallback if null
                    .shiftId(Optional.ofNullable(sampleLog.getShift_id()).orElse("Default Shift")) // Default fallback if null
                    // If batch number is null or empty, use pcu as a fallback
                    .batchNumber(Optional.ofNullable(sampleLog.getBatchNo())
                            .filter(batchNo -> !batchNo.isEmpty())
                            .orElse(Optional.ofNullable(sampleLog.getPcu()).orElse("Default PCU"))) // Default fallback if both are null or empty
                    // If order number is null or empty, use shopOrderBO as a fallback
                    .shopOrder(Optional.ofNullable(sampleLog.getOrderNumber())
                            .filter(orderNo -> !orderNo.isEmpty())
                            .orElse(Optional.ofNullable(sampleLog.getShop_order_bo()).orElse("Default Shop Order"))) // Default fallback if both are null or empty
                    .operation(Optional.ofNullable(sampleLog.getOperation()).orElse("Default Operation"))  // Default fallback if null
                    .item(Optional.ofNullable(sampleLog.getItem()).orElse("Default Item"))  // Default fallback if null
                    .itemVersion(Optional.ofNullable(sampleLog.getItem_version()).orElse("Default Item Version"))  // Default fallback if null
                    .totalQuantity(totalActualQty > 0 ? (double) totalActualQty : 0.0)  // Ensure it's not null or negative
                    .badQuantity(totalScrapQty > 0 ? (double) totalScrapQty : 0.0)  // Ensure it's not null or negative
                    .goodQuantity((double) (totalActualQty - totalScrapQty))  // Ensure it's not null or negative
                    .operationVersion(Optional.ofNullable(sampleLog.getOperation_version()).orElse("Default Operation Version"))  // Default fallback if null
                    .qualityPercentage(qualityPercentage >= 0 ? qualityPercentage : 0.0)  // Ensure it's not null or negative
                    .calculationTimestamp(Optional.ofNullable(LocalDateTime.now()).orElse(LocalDateTime.now()))  // Use current time if null
                    .createdDateTime(Optional.ofNullable(LocalDateTime.now()).orElse(LocalDateTime.now()))  // Use current time if null
                    .updatedDateTime(Optional.ofNullable(LocalDateTime.now()).orElse(LocalDateTime.now()))  // Use current time if null
                    .active(1)  // Set active to 1
                    .build();



            qualityEntities.add(qualityEntity); // Add to list for batch save
        }

        // Save all quality records in batch if there are any
        if (!qualityEntities.isEmpty()) {
            qualityRepository.saveAll(qualityEntities); // Assuming you have a repository for ProductionQuality
            created = true;
        }

        return created;
    }

    public List<ProductionLog> getActualPartsProduced(ProductionLogRequest productionLogRequest) {

        return webClientBuilder.build()
                .post()
                .uri(getTotalProducedQuantityUrl)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductionLog>>() {})
                .block();
    }
    // Helper method to create a unique key for grouping
    private String createGroupKey(ProductionLog log, OeeFilterRequest request) {
        return String.join("|",
                log.getSite(),
                log.getResource_id() != null ? log.getResource_id() : "",
                log.getWorkcenter_id() != null ? log.getWorkcenter_id() : "",
                log.getShift_id() != null ? log.getShift_id() : "",
                log.getBatchNo() != null ? log.getBatchNo() : "",
                log.getOrderNumber() != null ? log.getOrderNumber() : "",
                log.getOperation() != null ? log.getOperation() : "",
                log.getItem() != null ? log.getItem() : "",
                log.getItem_version() != null ? log.getItem_version() : ""
        );
    }

private List<ProductionLog> getProductionLogs(ProductionLogRequest productionLogRequest){
    List<ProductionLog> logs =webClientBuilder
            .build()
            .post()
            .uri(getByIntervalUrl)
            .body(BodyInserters.fromValue(productionLogRequest))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<ProductionLog>>() {
            })
            .block();
    return logs;
}

    public String calculateQuality1(OeeFilterRequest qualityRequest) {

        String qtyCalculationMessage = "empty record";

        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .site(qualityRequest.getSite())
                .userId(qualityRequest.getUserId())
                .build();

        List<ProductionQuality> qualityResponses = new ArrayList<>();

        try {
            qualityResponses = webClientBuilder
                    .build()
                    .post()
                    .uri(productionlog)
                    .body(BodyInserters.fromValue(productionLogRequest))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ProductionQuality>>() {
                    })
                    .block();

        } catch (Exception e) {
            throw e;
        }

        if(qualityResponses != null && qualityResponses.size() > 0) {
            List<ProductionQuality> savedEntities = qualityRepository.saveAll(qualityResponses);

            if (savedEntities.isEmpty()) {
                qtyCalculationMessage = "Quality calculation failed";
            } else {
                qtyCalculationMessage = "Quality calculated successfully for " + savedEntities.size() + " records.";
            }
        }

        return qtyCalculationMessage;
    }

    @Override
    public List<ProductionQualityDTO> getQualityByWorkcenter(OeeFilterRequest request){

        validateStartAndEndTime(request);
        return qualityRepository.findQualityByWorkcenters(request.getWorkcenterId(), request.getSite(), request.getStartTime(), request.getEndTime());
    }

    @Override
    public List<ProductionQualityDTO> getQualityByDateRange(OeeFilterRequest request){

        validateStartAndEndTime(request);
        return qualityRepository.findQualityByDateRange(request.getStartTime(), request.getEndTime(), request.getSite());
    }

    public List<ProductionQuality> getQualityByDateTime1(OeeFilterRequest request) {
        try {
            List<Object[]> results = qualityRepository.findQualityPercentageAverageBySiteAndDateRange(
                    request.getSite(),
                    request.getStartDateTime(),
                    request.getEndDateTime()
            );

            List<ProductionQuality> qualityList = new ArrayList<>();

            for (Object[] row : results) {
                // Map Object[] to ProductionQuality or DTO
                ProductionQuality quality = new ProductionQuality();
                quality.setSite((String) row[0]);
                quality.setWorkcenterId((String) row[1]);
                quality.setShiftId((String) row[2]);
                quality.setItem((String) row[3]);
                quality.setItemVersion((String) row[4]);
                quality.setOperation((String) row[5]);
                quality.setOperationVersion((String) row[6]);
                quality.setBatchNumber((String) row[7]);
                quality.setShopOrder((String) row[8]);
                quality.setQualityPercentage((Double) row[9]);
                quality.setResourceId((String) row[10]);
                qualityList.add(quality);
            }

            return qualityList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    @Override
    public List<ProductionQuality> getQualityByDateTime(OeeFilterRequest request) {
        try {
            // Retrieve results directly as ProductionQuality entities
            return qualityRepository.findQualityBySiteAndDateRange(
                    request.getSite(),
                    request.getStartDateTime(),
                    request.getEndDateTime()
            );
        } catch (Exception e) {
            e.printStackTrace();  // Log the exception for debugging
            return new ArrayList<>();
        }
    }


    @Override
    public List<ProductionQuality> getQualityByCombination(OeeFilterRequest request){

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductionQuality> query = cb.createQuery(ProductionQuality.class);
        Root<ProductionQuality> quality = query.from(ProductionQuality.class);

        List<Predicate> predicates = new ArrayList<>();

        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            predicates.add(quality.get("resourceId").in(request.getResourceId()));
        }
        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            predicates.add(quality.get("workcenterId").in(request.getWorkcenterId()));
        }
        if (request.getSites() != null && !request.getSites().isEmpty()) {
            predicates.add(quality.get("site").in(request.getSites()));
        }

//        if (request.getItem() != null && !request.getItem().isEmpty()) {
//
//            for (Item item : request.getItem()) {
//                Predicate itemPredicate = cb.and(
//                        cb.equal(quality.get("item"), item.getItem()),
//                        cb.equal(quality.get("itemVersion"), item.getItemVersion())
//                );
//                predicates.add(itemPredicate);
//            }
//            predicates.add(cb.or(predicates.toArray(new Predicate[0])));
//        }

        if (request.getOperation() != null && !request.getOperation().isEmpty()) {

            for (Operation operation : request.getOperation()) {
                Predicate opPredicate = cb.and(
                        cb.equal(quality.get("operation"), operation.getOperation()),
                        cb.equal(quality.get("operationVersion"), operation.getOperationVersion())
                );
                predicates.add(opPredicate);
            }
            predicates.add(cb.or(predicates.toArray(new Predicate[0])));
        }

        if (request.getShoporderId() != null && !request.getShoporderId().isEmpty()) {
            predicates.add(quality.get("shopOrder").in(request.getShoporderId()));
        }

        validateStartAndEndTime(request);

        predicates.add(cb.greaterThanOrEqualTo(quality.get("createdDateTime"), request.getStartTime()));
        predicates.add(cb.lessThanOrEqualTo(quality.get("createdDateTime"), request.getEndTime()));

        query.select(quality).where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }

    public LocalDateTime getStartTimeFromShift(OeeFilterRequest request){

        ShiftRequest shiftRequest = ShiftRequest.builder()
                .site(request.getSite())
                .localDateTime(request.getEndTime())
                .build();

        Optional<LocalDateTime> optionalResponse = webClientBuilder
                .build()
                .post()
                .uri(getShiftStartDateTimeByDateUrl)
                .body(BodyInserters.fromValue(shiftRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Optional<LocalDateTime>>() {})
                .block();

        return optionalResponse.orElse(null);
    }

    public OeeFilterRequest validateStartAndEndTime(OeeFilterRequest qualtityRequest){

        if (qualtityRequest.getEndTime() == null) {
            qualtityRequest.setEndTime(LocalDateTime.now()
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .toLocalDateTime());
        }

        if (qualtityRequest.getStartTime() == null) {
            qualtityRequest.setStartTime(LocalDateTime.now()
                    .minusHours(24)
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .toLocalDateTime());
        }
        return qualtityRequest;
    }

    private void applyCommonFilters(StringBuilder queryBuilder, Map<String, Object> queryParameters, OeeFilterRequest request) {
        // Handle start and end time filter
        if (request.getStartTime() != null && request.getEndTime() != null) {
            // If both startTime and endTime are provided, use them
            queryParameters.put("startTime", request.getStartTime());
            queryParameters.put("endTime", request.getEndTime());
            queryBuilder.append("AND q.interval_start_date_time <= :endTime ")
                    .append("AND q.interval_end_date_time >= :startTime ");
        } else {
            // If startTime or endTime is not provided, use the default last 24 hours range
            LocalDateTime now = LocalDateTime.now();
            //LocalDateTime startTime = now.minusHours(24);
            com.rits.oeeservice.dto.ShiftRequest shiftreq= new com.rits.oeeservice.dto.ShiftRequest();
            shiftreq.setSite(request.getSite());
            LocalDateTime startTime=  oeeService.getEarliestValidShiftStartDateTime(shiftreq);

            // Set default startTime and endTime in the request object
            request.setStartTime(startTime);
            request.setEndTime(now);

            queryParameters.put("startTime", startTime);
            queryParameters.put("endTime", now);

            queryBuilder.append("AND q.interval_start_date_time <= :endTime ")
                    .append("AND q.interval_end_date_time >= :startTime ");
        }

        // Handle other filters (resource ID, shift ID, workcenter ID, batch number)
        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            queryBuilder.append("AND q.resource_id IN :resourceId ");
            queryParameters.put("resourceId", request.getResourceId());
        }

        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            queryBuilder.append("AND q.shift_id IN :shiftId ");
            queryParameters.put("shiftId", request.getShiftId());
        }

        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            queryBuilder.append("AND q.workcenter_id IN :workcenterId ");
            queryParameters.put("workcenterId", request.getWorkcenterId());
        }

        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
            queryBuilder.append("AND q.batch_number IN :batchNumber ");
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
                queryBuilder.append("AND q.item IN (:itemBO) ");
                queryParameters.put("itemBO", itemBOList);
            }

            if (!itemVersionList.isEmpty()) {
                queryBuilder.append("AND q.item_version IN (:itemVersion) ");
                queryParameters.put("itemVersion", itemVersionList);
            }
        }*/
    }
    public static OeeRequest createOeeRequest(OeeFilterRequest request) {
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


}