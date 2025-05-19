package com.rits.availability.service;

import com.rits.availability.dto.*;
import com.rits.availability.exception.AvailabilityException;
import com.rits.availability.model.OeeAvailabilityEntity;
import com.rits.availability.repository.OeeAvailabilityRepository;
import com.rits.common.dto.OeeFilterRequest;
import com.rits.availability.dto.AvailabilityRequestForDowntime;
import com.rits.availability.dto.AvailabilityRequestForShift;
import com.rits.availability.dto.AvailabilityPlannedOperatingTimeResponse;
import com.rits.downtimeservice.dto.CurrentShiftDetails;
import com.rits.downtimeservice.dto.DowntimeRequest;
import com.rits.downtimeservice.dto.DowntimeRequestForShift;
import com.rits.downtimeservice.model.Downtime;
import com.rits.oeeservice.dto.*;
import com.rits.oeeservice.service.OeeService;
//import com.rits.shiftservice.dto.ShiftBtwnDatesRequest;
//import com.rits.shiftservice.dto.ShiftDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final OeeAvailabilityRepository oeeAvailabilityRepository;
    private final WebClient.Builder webClientBuilder;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private OeeService oeeService;

    @Value("${downtime-service.url}/totalDowntime")
    private String totalDowntimeUrl;
    @Value("${downtime-service.url}/totalDownTimeList")
    private String totalDownTimeListUrl;
    @Value("${downtime-service.url}/getDowntimeSummary")
    private String getDowntimeSummaryUrl;


    @Value("${shift-service.url}/availabilityPlannedOperatingTime")
    private String availabilityPlannedOperatingTimeUrl;

    @Value("${resource-service.url}/getValidResourceList")
    private String getValidResourceListUrl;
    @Value("${shift-service.url}/CurrentCompleteShiftDetails")
    private String currentCompleteShiftDetails;
    @Value("${shift-service.url}/getRuntime")
    private String getRuntimeUrl;
    private LocalDateTime et;
    private LocalDateTime st;

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Override
    public Boolean logAvailability(AvailabilityRequest availabilityRequest) {
        Boolean logged = false;

        // Set start and end times in the request
        setStartAndEndTimes(availabilityRequest);

        // Fetch valid resources and current shift details
        List<String> resourceList = fetchValidResources(availabilityRequest);
        CurrentShiftDetails currentShiftDetails = fetchCurrentShiftDetails(availabilityRequest);

        ShiftBtwnDatesRequest shiftBtwnDatesRequest = ShiftBtwnDatesRequest.builder().site(availabilityRequest.getSite()).
                shiftRef(currentShiftDetails.getShiftRef()).
                dateEnd(availabilityRequest.getEndDateTime()).
                dateStart(availabilityRequest.getStartDateTime()).
                build();
         
        ShiftDataResponse curreuntRuntImeDetails=getRuntime(shiftBtwnDatesRequest);
        // Create and set up the downtime request
        DowntimeRequest downtimeRequest = createDowntimeRequest(availabilityRequest, resourceList);

        // Fetch downtime summary
        List<Downtime> downtimes = getDownTimeSummary(downtimeRequest);

        // Process downtimes and log availability
        Set<String> processedResources = processDowntimes(availabilityRequest, downtimes, currentShiftDetails,curreuntRuntImeDetails);

        // Handle unprocessed resources (100% availability)
        logged |= handleUnprocessedResources(availabilityRequest, resourceList, processedResources, currentShiftDetails,curreuntRuntImeDetails);

        return logged;
    }

// Helper Methods

    private void setStartAndEndTimes(AvailabilityRequest availabilityRequest) {
        LocalDateTime currentTime = LocalDateTime.now();

        // Check if startDateTime and endDateTime are already set
        if (availabilityRequest.getStartDateTime() != null && availabilityRequest.getEndDateTime() != null) {
            // Use the provided start and end times
            return;
        }

        // If not set, compute them based on interval
        availabilityRequest.setEndDateTime(currentTime);

        int intervalSeconds = availabilityRequest.getEventIntervalSeconds();
        LocalDateTime startDateTime = intervalSeconds > 0
                ? currentTime.minusSeconds(intervalSeconds)
                : currentTime.minusMinutes(60);

        availabilityRequest.setStartDateTime(startDateTime);
    }


    private CurrentShiftDetails fetchCurrentShiftDetails(AvailabilityRequest availabilityRequest) {
        DowntimeRequest tempRequest = new DowntimeRequest();
        tempRequest.setSite(availabilityRequest.getSite());
        return getCurruntShiftDetails(tempRequest, LocalDateTime.now());
    }

    private DowntimeRequest createDowntimeRequest(AvailabilityRequest availabilityRequest, List<String> resourceList) {
        DowntimeRequest downtimeRequest = new DowntimeRequest();
        downtimeRequest.setSite(availabilityRequest.getSite());
        downtimeRequest.setStartDateTime(availabilityRequest.getStartDateTime());
        downtimeRequest.setEndDateTime(availabilityRequest.getEndDateTime());
        downtimeRequest.setResourceList(resourceList);
        downtimeRequest.setWorkcenterId(availabilityRequest.getWorkcenterId());
        downtimeRequest.setShiftId(availabilityRequest.getShiftId());
        return downtimeRequest;
    }

    private Set<String> processDowntimes(
            AvailabilityRequest availabilityRequest,
            List<Downtime> downtimes,
            CurrentShiftDetails currentShiftDetails, ShiftDataResponse curreuntRuntImeDetails
    ) {
        Set<String> processedResources = new HashSet<>();

        for (Downtime downtime : downtimes) {
            if (downtime.getDowntEvent() == 1) {
                processDowntimeEvent(availabilityRequest, downtime, currentShiftDetails, processedResources, false,curreuntRuntImeDetails);
            } else if (downtime.getDowntEvent() == 0) {
                processDowntimeEvent(availabilityRequest, downtime, currentShiftDetails, processedResources, true,curreuntRuntImeDetails);
            }
        }

        return processedResources;
    }

    private void processDowntimeEvent(
            AvailabilityRequest availabilityRequest,
            Downtime downtime,
            CurrentShiftDetails currentShiftDetails,
            Set<String> processedResources,
            boolean isFullDowntime,ShiftDataResponse curreuntRuntImeDetails
    ) {
        // Ensure downtime is not null
        if (downtime == null) {
            System.out.println("Downtime object is null. Skipping processing.");
            return;
        }

        availabilityRequest.setShiftRef(downtime.getShiftRef());
        List<AvailabilityPlannedOperatingTimeResponse> plannedResponses = getPlannedOperatingTime(availabilityRequest);

        if (plannedResponses == null || plannedResponses.isEmpty()) {
            System.out.println("Planned operating time responses are null or empty. Skipping processing.");
            return;
        }

        for (AvailabilityPlannedOperatingTimeResponse plannedResponse : plannedResponses) {
            if (plannedResponse == null) {
                System.out.println("Planned response is null. Skipping this iteration.");
                continue;
            }

            double plannedOperatingTime = curreuntRuntImeDetails.getPlannedOperatingTime();

            // Calculate downtime duration safely
            double downtimeDuration = calculateDowntimeDuration(downtime, plannedOperatingTime, isFullDowntime);

            double runtime = plannedOperatingTime - downtimeDuration;
            double availabilityPercentage = calculatePercentage(runtime, plannedOperatingTime);
            double actualAvailableTime = runtime - curreuntRuntImeDetails.getBreakDuration();

            saveAvailabilityEntity(
                    availabilityRequest,
                    downtime.getResourceId(),
                    downtime.getWorkcenterId(),
                    downtime.getShiftId(),
                    plannedResponse,
                     curreuntRuntImeDetails,
                    downtimeDuration,
                    runtime,
                    availabilityPercentage,
                    actualAvailableTime
            );

            if (downtime.getResourceId() != null) {
                processedResources.add(downtime.getResourceId());
            }
        }
    }

    // Helper method to calculate downtime duration safely
    private double calculateDowntimeDuration(Downtime downtime, double plannedOperatingTime, boolean isFullDowntime) {
        if (downtime == null) {
            System.out.println("Downtime is null. Returning zero duration.");
            return 0.0;
        }

        if (isFullDowntime) {
            // Full downtime: use the planned operating time directly
            Double recordedDowntime = Double.valueOf(downtime.getDowntimeDuration());
            LocalDateTime createdDatetime = downtime.getCreatedDatetime();

            if (createdDatetime == null) {
                System.out.println("Created datetime is null. Assuming zero elapsed time.");
                return recordedDowntime != null ? recordedDowntime : 0.0;
            }

            // Check if createdDatetime lies between start and end time
            if (createdDatetime.isAfter(st) && createdDatetime.isBefore(et)) {
                long secondsElapsed = Duration.between(createdDatetime, LocalDateTime.now()).getSeconds();
                // Use the calculated time difference between createdDatetime and now
                return (double) secondsElapsed;
            } else {
                // If createdDatetime is not between st and et, calculate downtime from st to et
                long totalDuration = Duration.between(st, et).getSeconds();
                return (double) totalDuration;
            }
        }else {
            // Partial downtime: calculate based on the record
            return downtime.getDowntimeDuration();
        }
    }
    private ShiftDataResponse getRuntime(ShiftBtwnDatesRequest shiftBtwnDatesRequest) {
       
        ShiftDataResponse shiftResponse = webClientBuilder
                .build()
                .post()
                .uri(getRuntimeUrl)
                .body(BodyInserters.fromValue(shiftBtwnDatesRequest))
                .retrieve()
                .bodyToMono(ShiftDataResponse.class)
                .block();
        return shiftResponse;
    }


    private Boolean handleUnprocessedResources(
            AvailabilityRequest availabilityRequest,
            List<String> resourceList,
            Set<String> processedResources,
            CurrentShiftDetails currentShiftDetails , ShiftDataResponse curreuntRuntImeDetails
    ) {
        Boolean logged = false;

        for (String resourceId : resourceList) {
            if (!processedResources.contains(resourceId)) {
                logged |= processFullAvailability(availabilityRequest, resourceId, currentShiftDetails,curreuntRuntImeDetails);
            }
        }

        return logged;
    }

    private Boolean processFullAvailability(
            AvailabilityRequest availabilityRequest,
            String resourceId,
            CurrentShiftDetails currentShiftDetails, ShiftDataResponse curreuntRuntImeDetails
    ) {
        availabilityRequest.setShiftRef(currentShiftDetails.getShiftRef());
        List<AvailabilityPlannedOperatingTimeResponse> plannedResponses = getPlannedOperatingTime(availabilityRequest);

        if (!plannedResponses.isEmpty()) {
            AvailabilityPlannedOperatingTimeResponse plannedResponse = plannedResponses.get(0);
            double plannedOperatingTime = curreuntRuntImeDetails.getPlannedOperatingTime();
            double runtime = curreuntRuntImeDetails.getPlannedOperatingTime();

            saveAvailabilityEntity(
                    availabilityRequest,
                    resourceId,
                    availabilityRequest.getWorkcenterId(),
                    currentShiftDetails.getShiftId(),
                    plannedResponse,
                    curreuntRuntImeDetails,
                    0.0,
                    runtime,
                    100.0,
                    runtime - curreuntRuntImeDetails.getTotalBreakDuration()
            );
            return true;
        }

        return false;
    }

    private double calculatePercentage(double part, double whole) {
        return new BigDecimal((part / whole) * 100).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private void saveAvailabilityEntity(
            AvailabilityRequest availabilityRequest,
            String resourceId,
            String workcenterId,
            String shiftId,
            AvailabilityPlannedOperatingTimeResponse plannedResponse,
            ShiftDataResponse curreuntRuntImeDetails,
            double downtime,
            double runtime,
            double availabilityPercentage,
            double actualAvailableTime
    ) {
        OeeAvailabilityEntity entity = new OeeAvailabilityEntity();
        entity.setSite(availabilityRequest.getSite());
        entity.setResourceId(resourceId);
        entity.setWorkcenterId(workcenterId);
        entity.setShiftId(shiftId);
        entity.setAvailabilityDate(LocalDate.now());
        entity.setPlannedOperatingTime((double) curreuntRuntImeDetails.getPlannedOperatingTime());
        entity.setRuntime((double) curreuntRuntImeDetails.getTotalRuntime());
        entity.setDowntime(downtime);
        entity.setAvailabilityPercentage(availabilityPercentage);
        entity.setShiftBreakDuration((double) curreuntRuntImeDetails.getBreakDuration());
        entity.setActualAvailableTime(actualAvailableTime);
        entity.setCreatedDatetime(LocalDateTime.now());
        entity.setShiftRef(plannedResponse.getShiftRef());
        entity.setActive(1);

        oeeAvailabilityRepository.save(entity);
    }


    private CurrentShiftDetails getCurruntShiftDetails(DowntimeRequest downtimeRequest, LocalDateTime date) {
        DowntimeRequestForShift shift = DowntimeRequestForShift.builder().site(downtimeRequest.getSite()).
                downtimeEnd(downtimeRequest.getDowntimeEnd()).
                downtimeStart(downtimeRequest.getDowntimeStart()).
                date(date).
                build();
        CurrentShiftDetails shiftResponse = webClientBuilder
                .build()
                .post()
                .uri(currentCompleteShiftDetails)
                .body(BodyInserters.fromValue(shift))
                .retrieve()
                .bodyToMono(CurrentShiftDetails.class)
                .block();
        return shiftResponse;
    }

    private String createKey(String site, String resourceId, String workcenterId, String shiftId) {
        return site + "_" + resourceId + "_" + workcenterId + "_" + shiftId;
    }
    private List < Downtime > getDownTimeSummary(DowntimeRequest downtimeRequest) {
        List < Downtime > totalDowntimeList = webClientBuilder.build()
                .post()
                .uri(getDowntimeSummaryUrl)
                .bodyValue(downtimeRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference < List < Downtime >> () {})
                .block();
        return totalDowntimeList;
    }
    private List < String > fetchValidResources(AvailabilityRequest availabilityRequest) {
        // WebClient to call the resource API and fetch valid resources
        ResourceRequest resourceRequest = new ResourceRequest();
        resourceRequest.setSite(availabilityRequest.getSite());
        List < ResourceListResponse > resourceListResponse = webClientBuilder.build()
                .post()
                .uri(getValidResourceListUrl)
                .bodyValue(resourceRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference < List < ResourceListResponse >> () {})
                .block();


        // Extract resource names from the response
        if (resourceListResponse != null) {
            return resourceListResponse.stream()
                    .map(ResourceListResponse::getResource) // Assuming the response has a method `getResourceName`
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    private List<AvailabilityPlannedOperatingTimeResponse> getPlannedOperatingTime(AvailabilityRequest availabilityRequest){

        List<String> shifts=new ArrayList<>();
        shifts.add(availabilityRequest.getShiftRef());
        AvailabilityRequestForShift availabilityRequestForShift = AvailabilityRequestForShift.builder()
                .site(availabilityRequest.getSite())
                .shiftIds(shifts)
                .startDateTime(availabilityRequest.getStartDateTime())
                .endDateTime(availabilityRequest.getEndDateTime())
                .dynamicBreak(availabilityRequest.getDynamicBreak() != 0 ? availabilityRequest.getDynamicBreak() : 0)
                .build();

        List<AvailabilityPlannedOperatingTimeResponse> availabilityPlannedOperatingTimeResponse = webClientBuilder.build()
                .post()
                .uri(availabilityPlannedOperatingTimeUrl)
                .bodyValue(availabilityRequestForShift)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AvailabilityPlannedOperatingTimeResponse>>() {})
                .block();

       return  availabilityPlannedOperatingTimeResponse;
    }
    @Override
    public AvailabilityResponse getLogAvailability(AvailabilityRequest request) {
        /*if(request.getResourceId()==null || request.getResourceId().isEmpty()){
            throw new AvailabilityException(1007);
        }
        if(request.getWorkcenterId() == null || request.getWorkcenterId().isEmpty()){
            throw new AvailabilityException(1006);
        }
        if(request.getShiftId() == null || request.getShiftId().isEmpty()){
            throw new AvailabilityException(1008);
        }
        if(request.getInterval() == 0){
            throw new AvailabilityException(1108);
        }*/
        // Check if startDateTime and endDateTime are null in the request
        LocalDateTime startDateTime = request.getStartDateTime() != null ? request.getStartDateTime() : LocalDateTime.now().minusMinutes(request.getInterval());
        LocalDateTime endDateTime = request.getEndDateTime() != null ? request.getEndDateTime() : LocalDateTime.now();


        AvailabilityRequestForDowntime availabilityRequestForDowntime = AvailabilityRequestForDowntime.builder()
                .site(request.getSite())
                .startTime(startDateTime)
                .endTime(endDateTime)
                .workcenterId(request.getWorkcenterId())
                .resourceId(request.getResourceId())
                .shiftId(request.getShiftId())
                .dynamicBreak(request.getDynamicBreak() != 0 ? request.getDynamicBreak() : 0)
                .build();

        List<Downtime> totalDowntimeList = webClientBuilder.build()
                .post()
                .uri(totalDownTimeListUrl)
                .bodyValue(availabilityRequestForDowntime)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Downtime>>() {})
                .block();
            Double totalDowntime= (double) (double) 0L;
        totalDowntime = totalDowntime == null ? 0.0 : totalDowntime;

        AvailabilityRequestForShift availabilityRequestForShift = AvailabilityRequestForShift.builder()
                .site(request.getSite())
                .shiftIds(List.of(request.getShiftId()))
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .dynamicBreak(request.getDynamicBreak() != 0 ? request.getDynamicBreak() : 0)
                .build();

        AvailabilityPlannedOperatingTimeResponse availabilityPlannedOperatingTimeResponse = webClientBuilder.build()
                .post()
                .uri(availabilityPlannedOperatingTimeUrl)
                .bodyValue(availabilityRequestForShift)
                .retrieve()
                .bodyToMono(AvailabilityPlannedOperatingTimeResponse.class)
                .block();

        Double plannedOperatingTime = (availabilityPlannedOperatingTimeResponse != null && availabilityPlannedOperatingTimeResponse.getPlannedOperatingTime() != null)
                ? availabilityPlannedOperatingTimeResponse.getPlannedOperatingTime()
                : 0.0;

        Double runtime = (plannedOperatingTime == 0.0) ? 0.0 :plannedOperatingTime - totalDowntime;
        Double availabilityPercentage = (plannedOperatingTime == 0.0) ? 0.0 : (runtime / plannedOperatingTime) * 100;

        OeeAvailabilityEntity oeeAvailabilityEntity = new OeeAvailabilityEntity();

        oeeAvailabilityEntity.setSite(request.getSite() != null ? request.getSite() : null);
        oeeAvailabilityEntity.setResourceId(request.getResourceId() != null ? request.getResourceId() : null);
        oeeAvailabilityEntity.setWorkcenterId(request.getWorkcenterId() != null ? request.getWorkcenterId() : null);
        //oeeAvailabilityEntity.setBatchNumber();
        oeeAvailabilityEntity.setShiftId(request.getShiftId() != null ? request.getShiftId() : null);
        oeeAvailabilityEntity.setAvailabilityDate(LocalDate.now());
        oeeAvailabilityEntity.setPlannedOperatingTime(plannedOperatingTime);
        oeeAvailabilityEntity.setRuntime(runtime);
        oeeAvailabilityEntity.setDowntime(totalDowntime);
        oeeAvailabilityEntity.setShiftBreakDuration(availabilityPlannedOperatingTimeResponse.getShiftBreakDuration());
        oeeAvailabilityEntity.setNonProductionDuration(request.getInterval() != 0 && plannedOperatingTime != null
                        ? (double) request.getInterval() - plannedOperatingTime : 0.0);
        oeeAvailabilityEntity.setAvailabilityPercentage(availabilityPercentage);
        //oeeAvailabilityEntity.setIsPlannedDowntimeIncluded();
        oeeAvailabilityEntity.setCreatedDatetime(LocalDateTime.now());
        oeeAvailabilityEntity.setUpdatedDatetime(LocalDateTime.now());
        oeeAvailabilityEntity.setActive(1);

        oeeAvailabilityRepository.save(oeeAvailabilityEntity);

        return new AvailabilityResponse("Availability logged successfully for interval: " + request.getInterval(), true);
    }

    @Override
    public CalculateAvailabilityResponse calculateAvailability(AvailabilityRequest request) {
        if(request.getResourceId()==null || request.getResourceId().isEmpty()){
            throw new AvailabilityException(1007);
        }
        if(request.getWorkcenterId() == null || request.getWorkcenterId().isEmpty()){
            throw new AvailabilityException(1006);
        }
        if(request.getShiftIds() == null || request.getShiftIds().isEmpty()){
            throw new AvailabilityException(1111);
        }
        if(request.getStartDateTime() == null || request.getEndDateTime() == null){
            throw new AvailabilityException(1109);
        }

        AvailabilityRequestForDowntime availabilityRequestForDowntime = AvailabilityRequestForDowntime.builder()
                .site(request.getSite())
                .startTime(request.getStartDateTime())
                .endTime(request.getEndDateTime())
                .workcenterId(request.getWorkcenterId())
                .resourceId(request.getResourceId())
                .shiftIds(request.getShiftIds())
                .dynamicBreak(request.getDynamicBreak() != 0 ? request.getDynamicBreak() : 0)
                .build();

        Double totalDowntime = webClientBuilder.build()
                .post()
                .uri(totalDowntimeUrl)
                .bodyValue(availabilityRequestForDowntime)
                .retrieve()
                .bodyToMono(Double.class)
                .block();

        totalDowntime = totalDowntime == null ? 0.0 : totalDowntime;

        AvailabilityRequestForShift availabilityRequestForShift = AvailabilityRequestForShift.builder()
                .site(request.getSite())
                .shiftIds(request.getShiftIds())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .dynamicBreak(request.getDynamicBreak() != 0 ? request.getDynamicBreak() : 0)
                .build();

        AvailabilityPlannedOperatingTimeResponse availabilityPlannedOperatingTimeResponse = webClientBuilder.build()
                .post()
                .uri(availabilityPlannedOperatingTimeUrl)
                .bodyValue(availabilityRequestForShift)
                .retrieve()
                .bodyToMono(AvailabilityPlannedOperatingTimeResponse.class)
                .block();

        Double plannedOperatingTime = (availabilityPlannedOperatingTimeResponse != null && availabilityPlannedOperatingTimeResponse.getPlannedOperatingTime() != null)
                ? availabilityPlannedOperatingTimeResponse.getPlannedOperatingTime()
                : 0.0;

        Double runtime = (plannedOperatingTime == 0.0) ? 0.0 :plannedOperatingTime - totalDowntime;
        Double availabilityPercentage = (plannedOperatingTime == 0.0) ? 0.0 : (runtime / plannedOperatingTime) * 100;

        return new CalculateAvailabilityResponse("Availability calculated successfully", true, Math.round(availabilityPercentage) );
    }

    @Transactional
    public AvailabilityResponse deleteAvailability(AvailabilityRequest request) {
        oeeAvailabilityRepository.deleteAvailability(request.getSite(),request.getResourceId(),request.getWorkcenterId());
        return new AvailabilityResponse("Availability deleted successfully",true);
    }

    public List<OverallAvailabilityResponse> findAvailability(AvailabilityRequest request) {
        // Construct the SQL query dynamically
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT resource_id, workcenter_id, site, shift_id, SUM(actual_available_time) AS totalAvailableTimeSeconds ")
                .append("FROM r_availability ")
                .append("WHERE availability_date BETWEEN :startDate AND :endDate ")
                .append("AND site = :site ")
                .append("AND created_datetime BETWEEN :createdStart AND :createdEnd ")
                .append("GROUP BY resource_id, workcenter_id, site, shift_id");

        try {
            // Create the native query
            Query query = entityManager.createNativeQuery(queryBuilder.toString());

            // Set query parameters dynamically
            query.setParameter("startDate", request.getStartDateTime().toLocalDate());
            query.setParameter("endDate", request.getEndDateTime().toLocalDate());
            query.setParameter("site", request.getSite());
            query.setParameter("createdStart", request.getStartDateTime());
            query.setParameter("createdEnd", request.getEndDateTime());

            // Execute the query and retrieve the results
            List<Object[]> queryResults = query.getResultList();
            List<OverallAvailabilityResponse> res = new ArrayList<>();

            for (Object[] row : queryResults) {
                OverallAvailabilityResponse response = new OverallAvailabilityResponse();

                // Map each column correctly to the response object
                response.setResourceId((String) row[0]);  // Index 0 is resource_id (String)
                response.setWorkcenterId((String) row[1]);  // Index 1 is workcenter_id (String, might be null)
                response.setSite((String) row[2]);  // Index 2 is site (String)
                response.setShiftId((String) row[3]);  // Index 3 is shift_id (String)

                // Correctly cast the total available time (SUM) to a Double
                response.setTotalAvailableTimeSeconds(((Number) row[4]).doubleValue());  // Index 4 is totalAvailableTimeSeconds (Double)

                // Add the mapped response object to the list
                res.add(response);
            }

            return res;

        } catch (Exception e) {
            // Handle any exception and log it appropriately
            throw new RuntimeException("Error executing query for availability", e);
        }
    }

    @Override
    public OverallAvailabilityResponse getOverallAvailability(OeeFilterRequest request) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT SUM(a.planned_operating_time) AS totalPlannedTime, ")
                .append("SUM(a.downtime) AS totalDowntime ")
                .append("FROM r_availability a ")
                .append("WHERE a.site = :site ")
                .append("AND a.active = 1 ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            Object[] result = (Object[]) query.getSingleResult();
            double plannedOperatingTime = (result[0] != null) ? ((Number) result[0]).doubleValue() : 0.0;
            double totalDowntime = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;

            // Calculate availability using planned operating time
            double availabilityPercentage = 0.0;
            if (plannedOperatingTime > 0) {
                long totalTimeSeconds = (long) plannedOperatingTime;
                long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
                long actualTimeSeconds = totalTimeSeconds;

                if (actualTimeSeconds > 0) {
                    availabilityPercentage = ((double) productionTimeSeconds / actualTimeSeconds) * 100;
                }
            }

            OverallAvailabilityResponse response = new OverallAvailabilityResponse();
            response.setAvailabilityPercentage(Math.round(availabilityPercentage * 100.0) / 100.0);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate overall availability", e);
        }
    }

    /*@Override
    public OverallAvailabilityResponse getOverallAvailability(OeeFilterRequest request) {
        OeeRequest oeeRequest = buildOeeRequest(request);

        if (request.getSite() != null && request.getShiftId() == null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            SiteDetails oeeResponse = getOeeDetailBySite(oeeRequest);  // Only site
            return OverallAvailabilityResponse.builder().availabilityPercentage(oeeResponse.getAvailability()).build();

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftAndSite(oeeRequest);  // Site + Shift
            return OverallAvailabilityResponse.builder().availabilityPercentage(oeeResponse.get(0).getAvailability()).build();

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<WorkcenterDetails> oeeResponse = getOeeDetailsByWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter
            return OverallAvailabilityResponse.builder().availabilityPercentage(oeeResponse.get(0).getAvailability()).build();

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource
            return OverallAvailabilityResponse.builder().availabilityPercentage(oeeResponse.get(0).getAvailability()).build();

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() != null) {

            List<BatchDetails> oeeResponse = getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource + Batch
            return OverallAvailabilityResponse.builder().availabilityPercentage(oeeResponse.get(0).getAvailability()).build();

        } else {

            List<SiteDetails> oeeResponse = getAllOeeDetails(oeeRequest);  // Default fallback case if no filters match
            return OverallAvailabilityResponse.builder().availabilityPercentage(oeeResponse.get(0).getAvailability()).build();
        }
    }*/

    @Override
    public AvailabilityByTimeResponse getAvailabilityByTime(OeeFilterRequest request) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT ")
                .append("a.created_datetime, ")
                .append("a.planned_operating_time, ")
                .append("a.downtime ")
                .append("FROM r_availability a ")
                .append("WHERE a.site = :site ")
                .append("AND a.active = 1 ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("ORDER BY a.created_datetime");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();
            AvailabilityByTimeResponse response = new AvailabilityByTimeResponse();

            // Grouping by hourly windows
            Map<LocalDateTime, List<Double>> hourlyAvailabilityMap = new TreeMap<>();
            LocalDateTime minTime = null;

            for (Object[] result : results) {
                if (result[0] == null) continue;

                LocalDateTime dateTime = ((Timestamp) result[0]).toLocalDateTime();
                if (minTime == null || dateTime.isBefore(minTime)) minTime = dateTime;

                double plannedOperatingTime = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
                double totalDowntime = result[2] != null ? ((Number) result[2]).doubleValue() : 0.0;

                if (plannedOperatingTime <= 0) continue;

                long totalTimeSeconds = (long) plannedOperatingTime;
                long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
                double availability = (totalTimeSeconds > 0)
                        ? ((double) productionTimeSeconds / totalTimeSeconds) * 100.0
                        : 0.0;

                // Bucket into hour
                long hoursDiff = Duration.between(minTime, dateTime).toHours();
                LocalDateTime bucketStart = minTime.plusHours(hoursDiff);

                hourlyAvailabilityMap.computeIfAbsent(bucketStart, k -> new ArrayList<>())
                        .add(availability);
            }

            List<AvailabilityByTimeResponse.AvailabilityData> availabilityByTimeList = hourlyAvailabilityMap.entrySet().stream()
                    .map(entry -> {
                        List<Double> availabilities = entry.getValue();
                        double avg = availabilities.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                        AvailabilityByTimeResponse.AvailabilityData data = new AvailabilityByTimeResponse.AvailabilityData();
                        data.setDate(entry.getKey().format(dateFormatter));
                        data.setPercentage(Math.round(avg * 100.0) / 100.0);
                        return data;
                    })
                    .collect(Collectors.toList());

            response.setAvailabilityData(availabilityByTimeList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate availability by time", e);
        }
    }


    /*@Override
    public AvailabilityByTimeResponse getAvailabilityByTime(OeeFilterRequest request) {

        OeeRequest oeeRequest = buildOeeRequest(request);

        AvailabilityByTimeResponse availabilityByTimeResponse = null;

        if (request.getSite() != null && request.getShiftId() == null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceId(oeeRequest);  // Only site
            availabilityByTimeResponse = mapAvailabilityData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftAndSite(oeeRequest);  // Site + Shift
            availabilityByTimeResponse = mapAvailabilityData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<WorkcenterDetails> oeeResponse = getOeeDetailsByWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter
            availabilityByTimeResponse = mapAvailabilityData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource
            availabilityByTimeResponse = mapAvailabilityData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() != null) {

            List<BatchDetails> oeeResponse = getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource + Batch
            availabilityByTimeResponse = mapAvailabilityData(oeeResponse);

        } else {

            List<SiteDetails> oeeResponse = getAllOeeDetails(oeeRequest);  // Default fallback case if no filters match
            availabilityByTimeResponse = mapAvailabilityData(oeeResponse);
        }

        return availabilityByTimeResponse;
    }

    private <T> AvailabilityByTimeResponse mapAvailabilityData(List<T> oeeDetails) {
        // Map the availability data into AvailabilityData list
        List<AvailabilityByTimeResponse.AvailabilityData> availabilityData = oeeDetails.stream()
                .map(detail -> {
                    String date = "";
                    Double availabilityPercentage = 0.0;

                    // Check the type of object and extract the availability percentage
                    if (detail instanceof ShiftDetails) {
                        availabilityPercentage = ((ShiftDetails) detail).getAvailability();
                    } else if (detail instanceof WorkcenterDetails) {
                        availabilityPercentage = ((WorkcenterDetails) detail).getAvailability();
                    } else if (detail instanceof ResourceDetails) {
                        date = ((ResourceDetails) detail).getIntervalEndTime();
                        availabilityPercentage = ((ResourceDetails) detail).getAvailability();
                    } else if (detail instanceof BatchDetails) {
                        availabilityPercentage = ((BatchDetails) detail).getAvailability();
                    } else if (detail instanceof SiteDetails) {
                        availabilityPercentage = ((SiteDetails) detail).getAvailability();
                    }

                    // Return the AvailabilityData object with the availability percentage
                    return new AvailabilityByTimeResponse.AvailabilityData(date, availabilityPercentage);
                })
                .collect(Collectors.toList());

        // Return the AvailabilityByTimeResponse object with the mapped data
        return AvailabilityByTimeResponse.builder()
                .availabilityData(availabilityData)
                .build();
    }*/

    @Override
    public AvailabilityByShiftResponse getAvailabilityByShift(OeeFilterRequest request) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.shift_id, " +
                        "SUM(a.planned_operating_time) AS totalPlannedTime, " +
                        "SUM(a.downtime) AS totalDowntime " +
                        "FROM r_availability a " +
                        "WHERE a.site = :site " +
                        "AND a.active = 1 "
        );

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        applyCommonFilters(sql, queryParameters, request);

        sql.append("GROUP BY a.shift_id ");
        sql.append("ORDER BY a.shift_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();
            AvailabilityByShiftResponse response = new AvailabilityByShiftResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<AvailabilityByShiftResponse.AvailabilityByShift> availabilityByShiftList = results.stream()
                    .map(result -> {
                        AvailabilityByShiftResponse.AvailabilityByShift availabilityByShift = new AvailabilityByShiftResponse.AvailabilityByShift();

                        // Parsing shift ID
                        availabilityByShift.setShiftId(result[0] != null ? result[0].toString().split(",")[2] : "");

                        double plannedOperatingTime = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
                        double totalDowntime = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;

                        // Calculate availability
                        double availability = 0.0;
                        if (plannedOperatingTime > 0) {
                            long totalTimeSeconds = (long) plannedOperatingTime;
                            long productionTimeSeconds = totalTimeSeconds - (long) totalDowntime;
                            long actualTimeSeconds = totalTimeSeconds;

                            if (actualTimeSeconds > 0) {
                                availability = ((double) productionTimeSeconds / actualTimeSeconds) * 100;
                            }
                        }

                        availabilityByShift.setPercentage(Math.round(availability * 100.0) / 100.0);

                        return availabilityByShift;
                    })
                    .collect(Collectors.toList());

            response.setAvailabilityByShift(availabilityByShiftList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate availability by shift", e);
        }
    }

    /*@Override
    public AvailabilityByShiftResponse getAvailabilityByShift(OeeFilterRequest request) {

        OeeRequest oeeRequest = buildOeeRequest(request);

        AvailabilityByShiftResponse availabilityByShiftResponse = null;

        if (request.getSite() != null && request.getShiftId() == null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftId(oeeRequest);
            availabilityByShiftResponse = mapAvailabilityShiftData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftAndSite(oeeRequest);  // Site + Shift
            availabilityByShiftResponse = mapAvailabilityShiftData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<WorkcenterDetails> oeeResponse = getOeeDetailsByWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter
            availabilityByShiftResponse = mapAvailabilityShiftData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource
            availabilityByShiftResponse = mapAvailabilityShiftData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() != null) {

            List<BatchDetails> oeeResponse = getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource + Batch
            availabilityByShiftResponse = mapAvailabilityShiftData(oeeResponse);

        } else {

            List<SiteDetails> oeeResponse = getAllOeeDetails(oeeRequest);  // Default fallback case if no filters match
            availabilityByShiftResponse = mapAvailabilityShiftData(oeeResponse);
        }

        return availabilityByShiftResponse;
    }

    private <T> AvailabilityByShiftResponse mapAvailabilityShiftData(List<T> oeeDetails) {
        // Map the availability data into AvailabilityByShift list
        List<AvailabilityByShiftResponse.AvailabilityByShift> availabilityByShiftList = oeeDetails.stream()
                .map(detail -> {
                    Double availabilityPercentage = 0.0;
                    String shiftId = null;

                    // Extract shiftId and availabilityPercentage based on object type
                    if (detail instanceof ShiftDetails) {
                        ShiftDetails shiftDetails = (ShiftDetails) detail;
                        availabilityPercentage = shiftDetails.getAvailability();
                        shiftId = shiftDetails.getShift();
                    } *//*else if (detail instanceof WorkcenterDetails) {
                        WorkcenterDetails workcenterDetails = (WorkcenterDetails) detail;
                        availabilityPercentage = workcenterDetails.getAvailability();
                        shiftId = workcenterDetails.getShift();
                    } else if (detail instanceof ResourceDetails) {
                        ResourceDetails resourceDetails = (ResourceDetails) detail;
                        availabilityPercentage = resourceDetails.getAvailability();
                        shiftId = resourceDetails.getShift();
                    } else if (detail instanceof BatchDetails) {
                        BatchDetails batchDetails = (BatchDetails) detail;
                        availabilityPercentage = batchDetails.getAvailability();
                        shiftId = batchDetails.getShift();
                    } else if (detail instanceof SiteDetails) {
                        SiteDetails siteDetails = (SiteDetails) detail;
                        availabilityPercentage = siteDetails.getAvailability();
                        shiftId = siteDetails.getShift();
                    }*//*

                    // Return the AvailabilityByShift object
                    return new AvailabilityByShiftResponse.AvailabilityByShift(shiftId, availabilityPercentage);
                })
                .collect(Collectors.toList());

        // Return the AvailabilityByShiftResponse object with the mapped data
        return AvailabilityByShiftResponse.builder()
                .availabilityByShift(availabilityByShiftList)
                .build();
    }*/

    @Override
    public AvailabilityByMachineResponse getAvailabilityByMachine(OeeFilterRequest request) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.resource_id, " +
                        "SUM(a.planned_operating_time) AS totalPlannedTime, " +
                        "SUM(a.downtime) AS totalDowntime " +
                        "FROM r_availability a " +
                        "WHERE a.site = :site " +
                        "AND a.active = 1 " +
                        "AND COALESCE(a.resource_id, '') <> '' ");

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        applyCommonFilters(sql, queryParameters, request);

        sql.append("GROUP BY a.resource_id ORDER BY a.resource_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();
            AvailabilityByMachineResponse response = new AvailabilityByMachineResponse();
            List<AvailabilityByMachineResponse.AvailabilityByMachine> machineList = new ArrayList<>();

            for (Object[] result : results) {
                AvailabilityByMachineResponse.AvailabilityByMachine availabilityData = new AvailabilityByMachineResponse.AvailabilityByMachine();

                String resourceId = (result[0] != null) ? result[0].toString() : "";
                double plannedOperatingTime = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
                double downtime = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;

                // Calculate availability using new logic
                double availability = 0.0;
                if (plannedOperatingTime > 0) {
                    long totalTimeSeconds = (long) plannedOperatingTime; // Using planned operating time as total time
                    long productionTimeSeconds = totalTimeSeconds - (long) downtime;
                    long actualTimeSeconds = totalTimeSeconds;

                    if (actualTimeSeconds > 0) {
                        availability = ((double) productionTimeSeconds / actualTimeSeconds) * 100;
                    }
                }

                availabilityData.setResourceId(resourceId);
                availabilityData.setPercentage(Math.round(availability * 100.0) / 100.0);
                machineList.add(availabilityData);
            }

            response.setAvailabilityByMachine(machineList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate availability", e);
        }
    }

    /*@Override
    public AvailabilityByMachineResponse getAvailabilityByMachine(OeeFilterRequest request) {

        OeeRequest oeeRequest = buildOeeRequest(request);

        AvailabilityByMachineResponse availabilityByMachineResponse = null;

        if (request.getSite() != null && request.getShiftId() == null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceId(oeeRequest);
            availabilityByMachineResponse = mapAvailabilityMachineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<ShiftDetails> oeeResponse = getOeeDetailsByShiftAndSite(oeeRequest);  // Site + Shift
            availabilityByMachineResponse = mapAvailabilityMachineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() == null && request.getBatchNumber() == null) {

            List<WorkcenterDetails> oeeResponse = getOeeDetailsByWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter
            availabilityByMachineResponse = mapAvailabilityMachineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() == null) {

            List<ResourceDetails> oeeResponse = getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource
            availabilityByMachineResponse = mapAvailabilityMachineData(oeeResponse);

        } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null && request.getResourceId() != null && request.getBatchNumber() != null) {

            List<BatchDetails> oeeResponse = getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(oeeRequest);  // Site + Shift + Workcenter + Resource + Batch
            availabilityByMachineResponse = mapAvailabilityMachineData(oeeResponse);

        } else {

            List<SiteDetails> oeeResponse = getAllOeeDetails(oeeRequest);  // Default fallback case if no filters match
            availabilityByMachineResponse = mapAvailabilityMachineData(oeeResponse);
        }

        return availabilityByMachineResponse;
    }

    private <T> AvailabilityByMachineResponse mapAvailabilityMachineData(List<T> oeeDetails) {
        // Map the availability data into AvailabilityByMachine list
        List<AvailabilityByMachineResponse.AvailabilityByMachine> availabilityByMachineList = oeeDetails.stream()
                .map(detail -> {
                    Double availabilityPercentage = 0.0;
                    String resourceId = null;

                    // Extract resourceId and availabilityPercentage based on object type
                    if (detail instanceof ResourceDetails) {
                        ResourceDetails resourceDetails = (ResourceDetails) detail;
                        availabilityPercentage = resourceDetails.getAvailability();
                        resourceId = resourceDetails.getResource();
                    } *//*else if (detail instanceof WorkcenterDetails) {
                        WorkcenterDetails workcenterDetails = (WorkcenterDetails) detail;
                        availabilityPercentage = workcenterDetails.getAvailability();
                        resourceId = workcenterDetails.getResource();
                    } else if (detail instanceof BatchDetails) {
                        BatchDetails batchDetails = (BatchDetails) detail;
                        availabilityPercentage = batchDetails.getAvailability();
                        resourceId = batchDetails.getResource();
                    } else if (detail instanceof ShiftDetails) {
                        ShiftDetails shiftDetails = (ShiftDetails) detail;
                        availabilityPercentage = shiftDetails.getAvailability();
                        resourceId = shiftDetails.getResource();
                    } else if (detail instanceof SiteDetails) {
                        SiteDetails siteDetails = (SiteDetails) detail;
                        availabilityPercentage = siteDetails.getAvailability();
                        resourceId = siteDetails.getResource();
                    }*//*

                    // Return the AvailabilityByMachine object
                    return new AvailabilityByMachineResponse.AvailabilityByMachine(resourceId, availabilityPercentage);
                })
                .collect(Collectors.toList());

        // Return the AvailabilityByMachineResponse object with the mapped data
        return AvailabilityByMachineResponse.builder()
                .availabilityByMachine(availabilityByMachineList)
                .build();
    }*/

    @Override
    public AvailabilityByWorkcenterResponse getAvailabilityByWorkcenter(OeeFilterRequest request) throws Exception {

        StringBuilder sql = new StringBuilder(
                "SELECT a.workcenter_id, ROUND(CAST(AVG(a.availability_percentage) AS NUMERIC), 2) AS averageAvailability " +
                        "FROM R_AVAILABILITY a " +
                        "WHERE a.site = :site "
        );

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        if (request.getStartTime() != null && request.getEndTime() != null) {
            sql.append("AND a.created_datetime BETWEEN :startTime AND :endTime ");
            queryParameters.put("startTime", request.getStartTime());
            queryParameters.put("endTime", request.getEndTime());
        } else if (request.getStartTime() == null && request.getEndTime() == null) {
            sql.append("AND a.created_datetime BETWEEN CURRENT_TIMESTAMP - INTERVAL '24 HOURS' AND CURRENT_TIMESTAMP ");
        }

        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            sql.append("AND a.workcenter_id IN :workcenterId ");
            queryParameters.put("workcenterId", request.getWorkcenterId());
        }

        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            sql.append(" AND a.resource_id IN :resourceId");
            queryParameters.put("resourceId", request.getResourceId());
        }

        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            sql.append(" AND a.shift_id IN :shiftId ");
            queryParameters.put("shiftId", request.getShiftId());
        }

        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
            sql.append(" AND a.batch_number IN :batchNumber ");
            queryParameters.put("batchNumber", request.getBatchNumber());
        }

        sql.append("GROUP BY a.workcenter_id ");
        sql.append("ORDER BY a.workcenter_id");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);
            List<Object[]> results = query.getResultList();

            AvailabilityByWorkcenterResponse response = new AvailabilityByWorkcenterResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<AvailabilityByWorkcenterResponse.AvailabilityWorkcenter> availabilityDataList = results.stream()
                    .map(row -> {
                        AvailabilityByWorkcenterResponse.AvailabilityWorkcenter data = new AvailabilityByWorkcenterResponse.AvailabilityWorkcenter();
                        data.setWorkcenterId(row[0].toString());
                        BigDecimal availabilityPercentage = (BigDecimal) row[1];
                        data.setAvailabilityPercentage(availabilityPercentage != null ? availabilityPercentage.doubleValue() : 0.0);
                        return data;
                    })
                    .collect(Collectors.toList());
            response.setAvailabilityWorkcenters(availabilityDataList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AvailabilityByMachineAndDateRangeResponse getAvailabilityByResourceAndDateRange(OeeFilterRequest request) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.resource_id, a.created_datetime, ROUND(CAST(AVG(a.availability_percentage) AS NUMERIC), 2) AS averageAvailability " +
                        "FROM R_AVAILABILITY a " +
                        "WHERE a.site = :site "
        );

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        if (request.getStartTime() != null && request.getEndTime() != null) {
            sql.append("AND a.created_datetime BETWEEN :startTime AND :endTime ");
            queryParameters.put("startTime", request.getStartTime());
            queryParameters.put("endTime", request.getEndTime());
        } else {

            sql.append("AND a.created_datetime BETWEEN CURRENT_TIMESTAMP - INTERVAL '24 HOURS' AND CURRENT_TIMESTAMP ");
        }

        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            sql.append("AND a.resource_id IN :resourceId ");
            queryParameters.put("resourceId", request.getResourceId());
        }

        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            sql.append("AND a.workcenter_id IN :workcenterId ");
            queryParameters.put("workcenterId", request.getWorkcenterId());
        }

        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            sql.append(" AND a.shift_id IN :shiftId ");
            queryParameters.put("shiftId", request.getShiftId());
        }

        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
            sql.append(" AND a.batch_number IN :batchNumber ");
            queryParameters.put("batchNumber", request.getBatchNumber());
        }

        sql.append("GROUP BY a.resource_id, a.created_datetime ");
        sql.append("ORDER BY a.resource_id, a.created_datetime");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            AvailabilityByMachineAndDateRangeResponse response = new AvailabilityByMachineAndDateRangeResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<AvailabilityByMachineAndDateRangeResponse.AvailabilityByMachineAndDateRange> availabilityByMachineList = results.stream()
                    .map(result -> {
                        AvailabilityByMachineAndDateRangeResponse.AvailabilityByMachineAndDateRange availabilityByMachine = new AvailabilityByMachineAndDateRangeResponse.AvailabilityByMachineAndDateRange();
                        availabilityByMachine.setResourceId(result[0].toString());
                        Timestamp createdDatetime = (Timestamp) result[1];
                        if (createdDatetime != null) {
                            LocalDateTime localDateTime = createdDatetime.toLocalDateTime();
                            availabilityByMachine.setCreatedDatetime(localDateTime);
                        }

                        BigDecimal averageAvailability = (BigDecimal) result[2];
                        availabilityByMachine.setAvailabilityPercentage(averageAvailability != null ? averageAvailability.doubleValue() : 0.0);

                        return availabilityByMachine;
                    })
                    .collect(Collectors.toList());

            response.setAvailabilityByMachineAndDateRanges(availabilityByMachineList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AvailabilityByWorkcenterAndDateRangeResponse getAvailabilityByWorkcenterAndDateRange(OeeFilterRequest request) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.workcenter_id, a.created_datetime, ROUND(CAST(AVG(a.availability_percentage) AS NUMERIC), 2) AS averageAvailability " +
                        "FROM R_AVAILABILITY a " +
                        "WHERE a.site = :site "
        );

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        if (request.getStartTime() != null && request.getEndTime() != null) {
            sql.append("AND a.created_datetime BETWEEN :startTime AND :endTime ");
            queryParameters.put("startTime", request.getStartTime());
            queryParameters.put("endTime", request.getEndTime());
        } else {

            sql.append("AND a.created_datetime BETWEEN CURRENT_TIMESTAMP - INTERVAL '24 HOURS' AND CURRENT_TIMESTAMP ");
        }

        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            sql.append("AND a.resource_id IN :resourceId ");
            queryParameters.put("resourceId", request.getResourceId());
        }

        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            sql.append("AND a.workcenter_id IN :workcenterId ");
            queryParameters.put("workcenterId", request.getWorkcenterId());
        }

        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            sql.append(" AND a.shift_id IN :shiftId ");
            queryParameters.put("shiftId", request.getShiftId());
        }

        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
            sql.append(" AND a.batch_number IN :batchNumber ");
            queryParameters.put("batchNumber", request.getBatchNumber());
        }

        sql.append("GROUP BY a.workcenter_id, a.created_datetime ");
        sql.append("ORDER BY a.workcenter_id, a.created_datetime");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            AvailabilityByWorkcenterAndDateRangeResponse response = new AvailabilityByWorkcenterAndDateRangeResponse();
            response.setStartTime(request.getStartTime());
            response.setEndTime(request.getEndTime());

            List<AvailabilityByWorkcenterAndDateRangeResponse.AvailabilityByWorkcenterAndDateRange> availabilityByWorkcenterList = results.stream()
                    .map(result -> {
                        AvailabilityByWorkcenterAndDateRangeResponse.AvailabilityByWorkcenterAndDateRange availabilityByWorkcenter = new AvailabilityByWorkcenterAndDateRangeResponse.AvailabilityByWorkcenterAndDateRange();
                        availabilityByWorkcenter.setWorkcenterId(result[0].toString());
                        Timestamp createdDatetime = (Timestamp) result[1];
                        if (createdDatetime != null) {
                            LocalDateTime localDateTime = createdDatetime.toLocalDateTime();
                            availabilityByWorkcenter.setCreatedDatetime(localDateTime);
                        }

                        BigDecimal averageAvailability = (BigDecimal) result[2];
                        availabilityByWorkcenter.setAvailabilityPercentage(averageAvailability != null ? averageAvailability.doubleValue() : 0.0);

                        return availabilityByWorkcenter;
                    })
                    .collect(Collectors.toList());

            response.setAvailabilityByWorkcenterAndDateRanges(availabilityByWorkcenterList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AvailabilityByDownTimeResponse getAvailabilityByDownTime(OeeFilterRequest request) {
        AvailabilityByDownTimeResponse response = new AvailabilityByDownTimeResponse();
        /*response.setStartTime(request.getStartTime().toString());
        response.setEndTime(request.getEndTime().toString());
        response.setMachine("Machine 1");*/

        List<AvailabilityByDownTimeResponse.DownTimeReason> downTimeReasons = new ArrayList<>();
        downTimeReasons.add(createDownTimeReason("Maintenance", 300, 40));
        downTimeReasons.add(createDownTimeReason("Setup", 200, 65));
        downTimeReasons.add(createDownTimeReason("Machine Breakdown", 150, 85));
        downTimeReasons.add(createDownTimeReason("Operator Error", 100, 100));

        response.setDowntimeReasons(downTimeReasons);
        return response;
    }

    private AvailabilityByDownTimeResponse.DownTimeReason createDownTimeReason(String reason, int minutes, double cumulativePercentage) {
        AvailabilityByDownTimeResponse.DownTimeReason data = new AvailabilityByDownTimeResponse.DownTimeReason();
        data.setReason(reason);
        /*data.setDowntimeMinutes(minutes);
        data.setCumulativePercentage(cumulativePercentage);*/
        return data;
    }

    @Override
    public DownTimeHeatMapResponse getDownTimeHeatMap(OeeFilterRequest request) {
        DownTimeHeatMapResponse response = new DownTimeHeatMapResponse();
        response.setStartTime(request.getStartTime().toString());
        response.setEndTime(request.getEndTime().toString());
        response.setMachine("Machine 1");

        List<DownTimeHeatMapResponse.HeatMapData> heatMapDataList = new ArrayList<>();
        heatMapDataList.add(createHeatMapData("Monday", 9, 20));
        heatMapDataList.add(createHeatMapData("Monday", 10, 10));
        heatMapDataList.add(createHeatMapData("Tuesday", 14, 30));

        response.setDowntimeHeatmap(heatMapDataList);
        return response;
    }

    private DownTimeHeatMapResponse.HeatMapData createHeatMapData(String day, int hour, int downtimeMinutes) {
        DownTimeHeatMapResponse.HeatMapData data = new DownTimeHeatMapResponse.HeatMapData();
        data.setDay(day);
        data.setHour(hour);
        data.setDowntimeMinutes(downtimeMinutes);
        return data;
    }

    @Override
    public List<AggregatedAvailabilityDTO> getGroupedAvailability(List<CombinationRequest> combinations) {
        return oeeAvailabilityRepository.findGroupedAvailability(combinations);
    }

    private void applyCommonFilters(StringBuilder queryBuilder, Map<String, Object> queryParameters, OeeFilterRequest request) {
        if (request.getStartTime() == null && request.getEndTime() == null) {
            LocalDateTime currentTime = LocalDateTime.now();
            //LocalDateTime startTime = currentTime.minusHours(24);
            ShiftRequest shiftreq= new ShiftRequest();
            shiftreq.setSite(request.getSite());
            LocalDateTime startTime=  oeeService.getEarliestValidShiftStartDateTime(shiftreq);

            // Set default startTime and endTime in the request object
            request.setStartTime(startTime);
            request.setEndTime(currentTime);

            queryParameters.put("startTime", startTime);
            queryParameters.put("endTime", currentTime);

            queryBuilder.append("AND a.interval_start_date_time <= :endTime ")
                    .append("AND a.interval_end_date_time >= :startTime ");
        } else if (request.getStartTime() != null && request.getEndTime() != null) {
            queryParameters.put("startTime", request.getStartTime());
            queryParameters.put("endTime", request.getEndTime());

            queryBuilder.append("AND a.interval_start_date_time <= :endTime ")
                    .append("AND a.interval_end_date_time >= :startTime ");
        }

        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            queryBuilder.append("AND a.resource_id IN :resourceId ");
            queryParameters.put("resourceId", request.getResourceId());
        }

        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            queryBuilder.append("AND a.shift_id IN :shiftId ");
            queryParameters.put("shiftId", request.getShiftId());
        }

        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            queryBuilder.append("AND a.workcenter_id IN :workcenterId ");
            queryParameters.put("workcenterId", request.getWorkcenterId());
        }

        if (request.getEventSource() != null) {
            String eventTypeOfPerformance = "RESOURCE";
            if(request.getResourceId() == null){
                eventTypeOfPerformance = "WORKCENTER";
            }
            queryBuilder.append(" AND a.category = :eventTypeOfPerformance ");
            queryParameters.put("eventTypeOfPerformance", eventTypeOfPerformance);
        }

//        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
//            queryBuilder.append("AND a.batch_number IN :batchNumber ");
//            queryParameters.put("batchNumber", request.getBatchNumber());
//        }
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
}

