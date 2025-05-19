package com.rits.shiftservice.service;

import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.shiftservice.dto.*;
import com.rits.shiftservice.model.*;
import com.rits.shiftservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import com.rits.shiftservice.exception.ShiftException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private ShiftMongoRepository shiftMongoRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;

    @Value("${cycletime-service.url}/receive-shift-performance")
    private String cycleTime;
    @Value("${productionlog-service.url}/getUniqueCombinations")
    private String productionlog;
    @Value("${shift-service.url}/breakDuration")
    private String breakDurationUrl;
    @Value("${downtime-service.url}/dynamicBreakDuration")
    private String dynamicBreakDurationQueryUrl;
    @Autowired
    private BreakPostgresRepository breakRepository;

    @Autowired
    private ShiftPostgresRepository shiftPostgresRepository;

    @Autowired
    private CalendarOverridePostgresRepository calendarOverrideRepository;

    @Autowired
    private CalendarRulePostgresRepository calendarRuleRepository;
    @Autowired
    private ShiftIntervalPostgresRepository shiftIntervalRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Long getShiftPlannedOperatingTime(String shiftId, String site) {

        String shiftPlannedOperatingTimeQuery = "WITH interval_durations AS ("
                + "SELECT interval.id AS interval_id, "
                + "interval.handle AS handle, "
                + "EXTRACT(EPOCH FROM (interval.end_time - interval.start_time + "
                + "CASE WHEN interval.start_time > interval.end_time THEN INTERVAL '1 day' ELSE INTERVAL '0 day' END)) / 60 AS total_duration_minutes "
                + "FROM r_shift_intervals AS interval "
                + "WHERE interval.active = 1 AND interval.shift_ref = ? AND interval.site = ?), "
                + "break_durations AS ("
                + "SELECT b.interval_ref, "
                + "SUM(CASE WHEN interval.start_time > interval.end_time THEN "
                + "GREATEST(EXTRACT(EPOCH FROM LEAST(TIME '24:00:00', b.break_time_end)) - "
                + "EXTRACT(EPOCH FROM GREATEST(interval.start_time, b.break_time_start)), 0) + "
                + "GREATEST(EXTRACT(EPOCH FROM LEAST(interval.end_time, b.break_time_end)) - "
                + "EXTRACT(EPOCH FROM GREATEST(TIME '00:00:00', b.break_time_start)), 0) "
                + "ELSE GREATEST(EXTRACT(EPOCH FROM LEAST(b.break_time_end, interval.end_time)) - "
                + "EXTRACT(EPOCH FROM GREATEST(b.break_time_start, interval.start_time)), 0) END / 60) AS total_break_duration_minutes "
                + "FROM r_break AS b "
                + "JOIN r_shift_intervals AS interval ON b.interval_ref = interval.handle "
                + "WHERE interval.active = 1 AND interval.shift_ref = ? AND interval.site = ? "
                + "GROUP BY b.interval_ref) "
                + "SELECT SUM(ROUND(COALESCE(interval_durations.total_duration_minutes, 0) - "
                + "COALESCE(break_durations.total_break_duration_minutes, 0), 0)::BIGINT) AS total_net_operating_time_minutes "
                + "FROM interval_durations "
                + "LEFT JOIN break_durations ON interval_durations.handle = break_durations.interval_ref;";

        try {
            Long shiftPlannedOperatingTime = jdbcTemplate.queryForObject(
                    shiftPlannedOperatingTimeQuery,
                    new Object[]{shiftId, site, shiftId, site},
                    Long.class);

            return shiftPlannedOperatingTime != null ? shiftPlannedOperatingTime : 0L;

        } catch (DataAccessException e) {
            // Log the exception
            System.out.println("An error occurred while executing the query: " + e.getMessage());
            e.printStackTrace();

            // Return default value in case of failure
            return 0L;
        }
    }


    @Override
    public Long getNonProduction(LocalDateTime date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String nonProductionQuery =
                "SELECT CASE " +
                        "WHEN COALESCE(" +
                        "(CASE WHEN override.production_day IS NOT NULL AND rules.day ILIKE :day THEN override.production_day END), " +
                        "rules.production_day) ILIKE 'No' THEN 1 " +
                        "ELSE 0 END AS is_non_production " +
                        "FROM r_calendar_rules AS rules " +
                        "LEFT JOIN r_calendar_overrides AS override ON override.date = DATE(:date) " +
                        "WHERE rules.day ILIKE :day";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("day", dayOfWeek.toString());
        params.addValue("date", date.toLocalDate());

        List<Integer> results = namedParameterJdbcTemplate.query(nonProductionQuery, params, (rs, rowNum) -> rs.getInt("is_non_production"));

        // Assuming you want to return the sum of all results
        return results.stream().mapToLong(Integer::longValue).sum();
    }
    @Override
    public List<Break> getBreakDurationList(LocalDateTime downtimeEnd, LocalDateTime downtimeStart, List<String> shiftIds, String site) {
        List<Break> breakList = new ArrayList<Break>();
        String breakDurationQuery = "WITH break_times AS (" +
                "SELECT " +
                "shift_ref, shift_type, break_time_start, break_time_end, " +
                "CASE WHEN break_time_start < :downtimeStart THEN " +
                "('2024-12-10 ' || :downtimeStart)::timestamp " +
                "ELSE ('2024-12-10 ' || break_time_start)::timestamp END AS adjusted_start, " +
                "CASE WHEN break_time_end > :downtimeEnd THEN " +
                "('2024-12-10 ' || :downtimeEnd)::timestamp " +
                "ELSE ('2024-12-10 ' || break_time_end)::timestamp END AS adjusted_end " +
                "FROM r_break " +
                "WHERE active = 1 " +
                "AND site = ? " +
                "AND (break_time_start >= :downtimeStart AND break_time_end <= :downtimeEnd) " +
                ") " +
                "SELECT " +
                "shift_ref, shift_type, break_time_start, break_time_end, adjusted_start, adjusted_end, " +
                "SUM(CASE " +
                "WHEN adjusted_end < adjusted_start THEN EXTRACT(EPOCH FROM (adjusted_end + INTERVAL '1 day' - adjusted_start)) / 60 " +
                "ELSE CASE " +
                "WHEN adjusted_start < ('2024-12-10 ' || :downtimeStart)::timestamp THEN " +
                "EXTRACT(EPOCH FROM (('2024-12-10 ' || :downtimeStart)::timestamp - adjusted_start)) / 60 " +
                "WHEN adjusted_end > ('2024-12-10 ' || :downtimeEnd)::timestamp THEN " +
                "EXTRACT(EPOCH FROM (adjusted_end - ('2024-12-10 ' || :downtimeEnd)::timestamp)) / 60 " +
                "ELSE EXTRACT(EPOCH FROM (adjusted_end - adjusted_start)) / 60 " +
                "END " +
                "END) AS total_break_time " +
                "FROM break_times " +
                "GROUP BY shift_ref, shift_type, break_time_start, break_time_end, adjusted_start, adjusted_end";

        // Prepare the parameters
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("downtimeStart", downtimeStart.toLocalTime());
        params.addValue("downtimeEnd", downtimeEnd.toLocalTime());
        params.addValue("site", site);
        params.addValue("shiftIds", shiftIds);

        // Manually substitute parameters into the query for printing
        String queryWithParams = breakDurationQuery
                .replace(":downtimeStart", "'" + downtimeStart.toLocalTime() + "'")
                .replace(":downtimeEnd", "'" + downtimeEnd.toLocalTime() + "'")
                .replace(":site", "'" + site + "'")
                .replace(":shiftIds", shiftIds.toString()); // You can adjust this if you need a more specific format

        // Print the final query with substituted parameters
        System.out.println("Final Query with Parameters: \n" + queryWithParams);

        // Query the database and return a list of results
        List<Map<String, Object>> breakDurations = namedParameterJdbcTemplate.queryForList(breakDurationQuery, params);
        for (Map<String, Object> row : breakDurations) {
            String shiftRef = (String) row.get("shift_ref");
            String shiftType = (String) row.get("shift_type");
            BigDecimal totalBreakTime = (BigDecimal) row.get("total_break_time");
            Break breakObj = new Break();
            breakObj.setShiftRef(shiftRef);
            breakObj.setShiftType(shiftType);
            breakObj.setMeanTime(totalBreakTime != null ? totalBreakTime.intValue() : 0);
            breakList.add(breakObj);
        }

        return breakList;
    }


    @Override
    public Long getBreakDuration(LocalDateTime downtimeEnd, LocalDateTime downtimeStart, List<String> shiftIds, String site) {
        String breakDurationQuery = "WITH overlapping_shifts AS ("
                + "SELECT si.id AS shift_interval_id, s.shift_id, "
                + "GREATEST(CAST(si.start_time AS TIME), :downtimeStart::TIME) AS overlap_start, "
                + "LEAST(CAST(si.end_time AS TIME), :downtimeEnd::TIME) AS overlap_end "
                + "FROM public.r_shift_intervals si "
                + "INNER JOIN public.r_shift s ON si.shift_fk = s.id "
                + "WHERE CAST(si.end_time AS TIME) > :downtimeStart::TIME "
                + "AND CAST(si.start_time AS TIME) < :downtimeEnd::TIME AND si.active = 1), "
                + "overlapping_breaks AS ("
                + "SELECT b.id AS break_id, b.break_time_start, b.break_time_end, "
                + "GREATEST(b.break_time_start, o.overlap_start) AS break_overlap_start, "
                + "LEAST(b.break_time_end, o.overlap_end) AS break_overlap_end "
                + "FROM public.r_break b "
                + "INNER JOIN overlapping_shifts o ON b.shift_interval_id = o.shift_interval_id "
                + "WHERE b.break_time_end > o.overlap_start "
                + "AND b.break_time_start < o.overlap_end AND b.site = :site AND b.active = 1), "
                + "break_durations AS ("
                + "SELECT break_id, "
                + "EXTRACT(EPOCH FROM (break_overlap_end - break_overlap_start)) / 60 AS break_minutes "
                + "FROM overlapping_breaks "
                + "WHERE break_overlap_end > break_overlap_start) "
                + "SELECT COALESCE(SUM(break_minutes), 0) AS total_break_minutes "
                + "FROM break_durations;";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("downtimeStart", downtimeStart.toLocalTime());
        params.addValue("downtimeEnd", downtimeEnd.toLocalTime());
        params.addValue("site", site);

        Double breakDuration = namedParameterJdbcTemplate.queryForObject(breakDurationQuery, params, Double.class);
        return (breakDuration == null) ? 0L : Math.round(breakDuration);
    }
    @Override
    public List<AvailabilityPlannedOperatingTimeResponse> getAvailabilityPlannedOperatingTime(
            String site, List<String> shiftIds, LocalDateTime startDateTime, LocalDateTime endDateTime, int dynamicBreak) {

        // Define the SQL query with placeholders for parameters
        String query = "WITH BreakDurations AS ( " +
                "    SELECT " +
                "        b.interval_ref, " +
                "        SUM(EXTRACT(EPOCH FROM (b.break_time_end::time - b.break_time_start::time))) AS total_break_duration " +
                "    FROM " +
                "        r_break b " +
                "    WHERE " +
                "        b.active = 1 " +
                "        AND b.site = :site " +
                "        AND b.created_date_time BETWEEN :startDateTime AND :endDateTime " +
                "    GROUP BY " +
                "        b.interval_ref " +
                "), " +
                "ShiftDurations AS ( " +
                "    SELECT " +
                "        si.shift_ref AS shift_ref, " +
                "        si.handle AS handle, " +
                "        EXTRACT(EPOCH FROM (si.end_time::time - si.start_time::time)) AS shift_duration " +
                "    FROM " +
                "        r_shift_intervals si " +
                "    WHERE " +
                "        si.active = 1 " +
                "        AND si.site = :site " +
                "        AND si.valid_from <= :startDateTime " +
                "        AND si.valid_end >= :endDateTime " +
                "), " +
                "PlannedOperatingTime AS ( " +
                "    SELECT " +
                "        sd.shift_ref, " +
                "        sd.handle, " +
                "        (sd.shift_duration - COALESCE(bd.total_break_duration, 0)) AS planned_operating_time, " +
                "        COALESCE(bd.total_break_duration, 0) AS break_duration " +
                "    FROM " +
                "        ShiftDurations sd " +
                "    LEFT JOIN " +
                "        BreakDurations bd ON sd.handle = bd.interval_ref " +
                ") " +
                "SELECT " +
                "    sd.shift_ref, " +
                "    planned_operating_time, " +
                "    break_duration " +
                "FROM " +
                "    PlannedOperatingTime sd " +
                "WHERE " +
                "    sd.shift_ref IN (:shiftIds) " +
                "    AND EXISTS ( " +
                "        SELECT 1 " +
                "        FROM r_shift_intervals si " +
                "        WHERE si.shift_ref = sd.shift_ref " +
                "        AND si.active = 1 " +
                "    );";

        // Create parameters map and add values
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("site", site);
        params.addValue("startDateTime", Timestamp.valueOf(startDateTime)); // Convert LocalDateTime to Timestamp
        params.addValue("endDateTime", Timestamp.valueOf(endDateTime));     // Convert LocalDateTime to Timestamp
        params.addValue("shiftIds", shiftIds); // Pass List of shiftIds as parameter

        // Execute the query and get the result (assuming multiple results expected)
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(query, params);

        // Create a list to store the response objects
        List<AvailabilityPlannedOperatingTimeResponse> responses = new ArrayList<>();

        // Iterate over the result list and build the response objects
        for (Map<String, Object> row : result) {
            // Get the BigDecimal values and convert them to Double
            BigDecimal plannedOperatingTimeBigDecimal = (BigDecimal) row.get("planned_operating_time");
            BigDecimal breakDurationBigDecimal = (BigDecimal) row.get("break_duration");
            String shiftRef = (String) row.get("shift_ref");

            // Convert the BigDecimal values to Double using the doubleValue() method
            Double plannedOperatingTime = plannedOperatingTimeBigDecimal != null ? plannedOperatingTimeBigDecimal.doubleValue() : 0.0;
            Double breakDuration = breakDurationBigDecimal != null ? breakDurationBigDecimal.doubleValue() : 0.0;

            // Create the response object with the results
            AvailabilityPlannedOperatingTimeResponse response = AvailabilityPlannedOperatingTimeResponse.builder()
                    .shiftRef(shiftRef)
                    .plannedOperatingTime(plannedOperatingTime)
                    .shiftBreakDuration(breakDuration)
                    .build();

            // Add the response object to the list
            responses.add(response);
        }

        // Return the list of response objects
        return responses;
    }




    public AvailabilityPlannedOperatingTimeResponse getAvailabilityPlannedOperatingTime1(String site, List<String> shiftIds, LocalDateTime startDateTime, LocalDateTime endDateTime, int dynamicBreak) {

        String shiftDurationQuery = "WITH daily_ranges AS ("
                + "SELECT generate_series(:startDate::DATE, :endDate::DATE, INTERVAL '1 day') AS day_date), "
                + "shift_intervals AS ("
                + "SELECT day_date, "
                + "CASE WHEN day_date = :startDate::DATE THEN :startTime::TIME ELSE TIME '00:00:00' END AS day_start, "
                + "CASE WHEN day_date = :endDate::DATE THEN :endTime::TIME ELSE TIME '24:00:00' END AS day_end "
                + "FROM daily_ranges) "
                + "SELECT CAST(ROUND(SUM(CASE WHEN interval.start_time < shift_intervals.day_end "
                + "AND interval.end_time > shift_intervals.day_start THEN "
                + "GREATEST(EXTRACT(EPOCH FROM LEAST(shift_intervals.day_end, interval.end_time)) - "
                + "EXTRACT(EPOCH FROM GREATEST(shift_intervals.day_start, interval.start_time)), 0) "
                + "ELSE 0 END / 60), 3) AS DECIMAL(10,3)) AS total_duration_minutes "
                + "FROM r_shift_intervals AS interval "
                + "JOIN r_shift AS shift ON interval.shift_id = shift.handle "
                + "JOIN shift_intervals ON DATE(interval.created_datetime) = shift_intervals.day_date "
                + "WHERE interval.active = 1 "
                + "AND interval.site = :site "
                + "AND interval.shift_id = ANY(string_to_array(:shiftIds, ',')::TEXT[]) "
                + "AND interval.created_datetime BETWEEN :startDate::DATE AND (:endDate::DATE + INTERVAL '23:59:59')::TIMESTAMP";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", startDateTime.toLocalDate());
        params.addValue("endDate", endDateTime.toLocalDate());
        params.addValue("startTime", startDateTime.toLocalTime());
        params.addValue("endTime", endDateTime.toLocalTime());
        params.addValue("site", site);

        String shiftIdsArray = String.join(",", shiftIds);
        params.addValue("shiftIds", shiftIdsArray);

        // Print query with parameters
        String queryWithParams = shiftDurationQuery
                .replace(":startDate", startDateTime.toLocalDate().toString())
                .replace(":endDate", endDateTime.toLocalDate().toString())
                .replace(":startTime", startDateTime.toLocalTime().toString())
                .replace(":endTime", endDateTime.toLocalTime().toString())
                .replace(":site", site)
                .replace(":shiftIds", shiftIdsArray);

        System.out.println("Executing Query: " + queryWithParams);

        Double shiftDuration = namedParameterJdbcTemplate.queryForObject(shiftDurationQuery, params, Double.class);

        Long breakDuration = 0L;
        if(shiftDuration == 0.0 || shiftDuration == null) {
            return AvailabilityPlannedOperatingTimeResponse.builder()
                    .plannedOperatingTime(0.0)
                    .shiftBreakDuration((double) breakDuration)
                    .build();
        } else {
            if (dynamicBreak != 0) {
                AvailabilityRequestForDowntime breakDurationRequest = AvailabilityRequestForDowntime.builder()
                        .dynamicBreak(dynamicBreak)
                        .build();

                breakDuration = webClientBuilder.build()
                        .post()
                        .uri(dynamicBreakDurationQueryUrl)
                        .bodyValue(breakDurationRequest)
                        .retrieve()
                        .bodyToMono(Long.class)
                        .block();
            } else {
                DowntimRequestForShift breakDurationRequest = DowntimRequestForShift.builder()
                        .site(site)
                        .shiftIds(shiftIds)
                        .downtimeStart(startDateTime)
                        .downtimeEnd(endDateTime)
                        .build();

                breakDuration = webClientBuilder.build()
                        .post()
                        .uri(breakDurationUrl)
                        .bodyValue(breakDurationRequest)
                        .retrieve()
                        .bodyToMono(Long.class)
                        .block();
            }
        }

        Double plannedOperatingTime = shiftDuration - (double) breakDuration;
        return AvailabilityPlannedOperatingTimeResponse.builder()
                .plannedOperatingTime(plannedOperatingTime)
                .shiftBreakDuration((double) breakDuration)
                .build();
    }

    @Override
    public Long getShiftTypeBreakDuration(LocalDateTime downtimeEnd, LocalDateTime downtimeStart, String shiftId, String site) {
        String breakDurationQuery = "WITH overlapping_shifts AS ("
                + "SELECT si.id AS shift_interval_id, "
                + "GREATEST(CAST(si.start_time AS TIME), :downtimeStart::TIME) AS overlap_start, "
                + "LEAST(CAST(si.end_time AS TIME), :downtimeEnd::TIME) AS overlap_end "
                + "FROM public.r_shift_intervals si "
                + "INNER JOIN public.r_shift s ON si.shift_fk = s.id "
                + "WHERE CAST(si.end_time AS TIME) > :downtimeStart::TIME "
                + "AND CAST(si.start_time AS TIME) < :downtimeEnd::TIME "
                + "AND s.shift_id = :shiftId), "
                + "overlapping_breaks AS ("
                + "SELECT b.id AS break_id, b.break_time_start, b.break_time_end, "
                + "GREATEST(b.break_time_start, o.overlap_start) AS break_overlap_start, "
                + "LEAST(b.break_time_end, o.overlap_end) AS break_overlap_end "
                + "FROM public.r_break b "
                + "INNER JOIN overlapping_shifts o ON b.shift_interval_id = o.shift_interval_id "
                + "WHERE b.break_time_end > o.overlap_start "
                + "AND b.break_time_start < o.overlap_end AND b.site = :site AND b.active = 1), "
                + "break_durations AS ("
                + "SELECT break_id, "
                + "EXTRACT(EPOCH FROM (break_overlap_end - break_overlap_start)) / 60 AS break_minutes "
                + "FROM overlapping_breaks "
                + "WHERE break_overlap_end > break_overlap_start) "
                + "SELECT COALESCE(SUM(break_minutes), 0) AS total_break_minutes "
                + "FROM break_durations;";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("downtimeStart", downtimeStart.toLocalTime());
        params.addValue("downtimeEnd", downtimeEnd.toLocalTime());
        params.addValue("shiftId", shiftId);
        params.addValue("site", site);

        Double breakDuration = namedParameterJdbcTemplate.queryForObject(breakDurationQuery, params, Double.class);
        return (breakDuration == null) ? 0L : Math.round(breakDuration);
    }



    @Override
    public BreakMinutes getShiftDetailsByShiftType(String site, String resourceId, String workcenterId) {
        Object[] queryResult;
        if(resourceId != null && !resourceId.isEmpty()) {
            queryResult = shiftPostgresRepository.getCurrentShiftDetailsByShiftType(site, "Resource",resourceId,workcenterId);
        }else {
            queryResult = shiftPostgresRepository.getCurrentShiftDetailsByShiftType(site, "Workcenter",resourceId,workcenterId);
        }

        if (queryResult == null || queryResult.length == 0) {
            queryResult = shiftPostgresRepository.getCurrentShiftDetailsByShiftType(site, "General",resourceId,workcenterId);
        }

        if (queryResult == null || queryResult.length == 0) {
            return null;
        }

        Object[] shiftDetails = (Object[]) queryResult[0];

        if (shiftDetails == null || shiftDetails.length < 5) {
            return null;
        }

        BreakMinutes breakMinutes = new BreakMinutes();
        breakMinutes.setShiftId(shiftDetails[5] != null ? shiftDetails[5].toString() : null);
        breakMinutes.setShiftType(shiftDetails[1] != null ? shiftDetails[1].toString() : null);
        breakMinutes.setShiftCreatedDatetime(shiftDetails[2] != null ? convertToLocalDateTime(shiftDetails[2]) : null);
        breakMinutes.setStartTime(shiftDetails[3] != null ? convertToLocalTime(shiftDetails[3]) : null);
        breakMinutes.setEndTime(shiftDetails[4] != null ? convertToLocalTime(shiftDetails[4]) : null);

        int plannedTime = getShiftPlannedOperatingTime(shiftDetails[5].toString(), site).intValue();
        breakMinutes.setPlannedTime(plannedTime);

        breakMinutes.setBreakTime((plannedTime != 0) ? getShiftTypeBreakDuration( LocalDateTime.now(), LocalDateTime.of(LocalDate.now(), convertToLocalTime(shiftDetails[3])), shiftDetails[0].toString(), site).intValue() : 0);

        return breakMinutes;
    }


    @Override
    public ShiftMessageModel createShift(ShiftRequest shiftRequest) throws Exception {
        if(StringUtils.isNotBlank(shiftRequest.getSite()) && StringUtils.isNotBlank(shiftRequest.getShiftId()))  {
            List<Shift> activeShiftsPostgres= new ArrayList<>();
            if(shiftRequest.getShiftType().equalsIgnoreCase("Resource")){
                activeShiftsPostgres= shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndResourceIdAndActive(
                        shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), shiftRequest.getResourceId(),1);
            }
            if(shiftRequest.getShiftType().equalsIgnoreCase("WorkCenter")){
                activeShiftsPostgres= shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndWorkCenterIdAndActive(
                        shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), shiftRequest.getWorkCenterId(),1);
            }
            if(shiftRequest.getShiftType().equalsIgnoreCase("General")){
                activeShiftsPostgres = shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndActive(
                        shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), 1);
            }


            if(activeShiftsPostgres.size()!=0) {

                throw new ShiftException(409 , shiftRequest.getShiftId());
                /*
                if ("General".equalsIgnoreCase(shiftRequest.getShiftType())) {
                    for (Shift existingShift : activeShiftsPostgres) {
                        if (checkDuplicateShiftIntervals(existingShift.getShiftIntervals(), shiftRequest.getShiftIntervals())) {
                            throw new ShiftException(418);
                        }
                        if (existingShift.getShiftId().equalsIgnoreCase(shiftRequest.getShiftId())) {
                            existingShift.setActive(0);
                            shiftPostgresRepository.save(existingShift);
                        }
                    }
                    Shift newShift = createBuilder(shiftRequest);
                    String[] versionParts = activeShiftsPostgres.get(0).getVersion().split("-");
                    int versionNumber = Integer.parseInt(versionParts[1]);
                    newShift.setVersion(versionParts[0] + "-" + (versionNumber + 1));
                    if(createShiftInPsql(newShift,shiftRequest)){
                        MessageDetails messageDetails = new MessageDetails("Shift created successfully", "S");
                        return new ShiftMessageModel(newShift, messageDetails);
                    }

                }
                else if ("WorkCenter".equalsIgnoreCase(shiftRequest.getShiftType())) {
                    if (shiftRequest.getWorkCenterId() == null || shiftRequest.getWorkCenterId().isEmpty()) {
                        throw new ShiftException(414);
                    }
                    else{
                        for (Shift existingShift : activeShiftsPostgres) {
                            if (checkDuplicateShiftIntervals(existingShift.getShiftIntervals(), shiftRequest.getShiftIntervals())) {
                                throw new ShiftException(418);
                            }
                            if (existingShift.getShiftId().equalsIgnoreCase(shiftRequest.getShiftId())&&existingShift.getWorkCenterId().equalsIgnoreCase(shiftRequest.getWorkCenterId())) {
                                existingShift.setActive(0);
                                shiftPostgresRepository.save(existingShift);
                            }
                        }
                        Shift newShift = createBuilder(shiftRequest);
                        String[] versionParts = activeShiftsPostgres.get(0).getVersion().split("-");
                        int versionNumber = Integer.parseInt(versionParts[1]);
                        newShift.setVersion(versionParts[0] + "-" + (versionNumber + 1));
                        if(createShiftInPsql(newShift,shiftRequest)){
                            MessageDetails messageDetails = new MessageDetails("Shift created successfully", "S");
                            return new ShiftMessageModel(newShift, messageDetails);
                        }
                    }

                }
                else if ("Resource".equalsIgnoreCase(shiftRequest.getShiftType())) {
                    if (shiftRequest.getResourceId() == null || shiftRequest.getResourceId().isEmpty()) {
                        throw new ShiftException(416);
                    }
                    else{
                        for (Shift existingShift : activeShiftsPostgres) {
                            if (checkDuplicateShiftIntervals(existingShift.getShiftIntervals(), shiftRequest.getShiftIntervals())) {
                                throw new ShiftException(418);
                            }
                            if (existingShift.getShiftId().equalsIgnoreCase(shiftRequest.getShiftId())&&existingShift.getResourceId().equalsIgnoreCase(shiftRequest.getResourceId())) {
                                existingShift.setActive(0);
                                shiftPostgresRepository.save(existingShift);
                            }
                        }

                        Shift newShift = createBuilder(shiftRequest);
                        String[] versionParts = activeShiftsPostgres.get(0).getVersion().split("-");
                        int versionNumber = Integer.parseInt(versionParts[1]);
                        newShift.setVersion(versionParts[0] + "-" + (versionNumber + 1));
                        if(createShiftInPsql(newShift,shiftRequest)){
                            MessageDetails messageDetails = new MessageDetails("Shift created successfully", "S");
                            return new ShiftMessageModel(newShift, messageDetails);
                        }
                    }

                }


            */}
            else{
                if ("General".equalsIgnoreCase(shiftRequest.getShiftType())) {

                    Shift newShift = createBuilder(shiftRequest);
                    newShift.setVersion("v-1");
                    if(createShiftInPsql(newShift,shiftRequest)){
                        MessageDetails messageDetails = new MessageDetails("Shift created successfully", "S");
                        return new ShiftMessageModel(newShift, messageDetails);
                    }
                }
                else if ("WorkCenter".equalsIgnoreCase(shiftRequest.getShiftType())) {
                    if (shiftRequest.getWorkCenterId() == null || shiftRequest.getWorkCenterId().isEmpty()) {
                        throw new ShiftException(414);
                    }
                    Shift newShift = createBuilder(shiftRequest);
                    newShift.setVersion("v-1");
                    if(createShiftInPsql(newShift,shiftRequest)){
                        MessageDetails messageDetails = new MessageDetails("Shift created successfully", "S");
                        return new ShiftMessageModel(newShift, messageDetails);
                    }

                }
                else if ("Resource".equalsIgnoreCase(shiftRequest.getShiftType())) {
                    if (shiftRequest.getResourceId() == null || shiftRequest.getResourceId().isEmpty()) {
                        throw new ShiftException(416);
                    }
                    Shift newShift = createBuilder(shiftRequest);
                    newShift.setVersion("v-1");
                    if(createShiftInPsql(newShift,shiftRequest)){
                        MessageDetails messageDetails = new MessageDetails("Shift created successfully", "S");
                        return new ShiftMessageModel(newShift, messageDetails);
                    }

                }
            }
        }
        MessageDetails messageDetails = new MessageDetails("Shift or Site should not be null or empty", "S");
        return new ShiftMessageModel(null, messageDetails);
    }

    private boolean createShiftInPsql(Shift shift, ShiftRequest shiftRequest) throws Exception {
        boolean created = false;
        int totalIntervalMeanTime = 0;

        if (shiftRequest.getShiftIntervals() != null && !shiftRequest.getShiftIntervals().isEmpty()) {
            Object[] existingIntervalsRaw = shiftPostgresRepository.findShiftIntervalsByTypeAndSite(
                    shiftRequest.getShiftType(), shiftRequest.getSite()
            );

            List<ShiftIntervals> existingIntervals = new ArrayList<>();
            for (Object obj : existingIntervalsRaw) {
                Object[] row = (Object[]) obj;
                ShiftIntervals existingInterval = new ShiftIntervals();
                existingInterval.setValidFrom(((Timestamp) row[0]).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                existingInterval.setValidEnd(((Timestamp) row[1]).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                existingInterval.setStartTime(((Time) row[2]).toLocalTime());
                existingInterval.setEndTime(((Time) row[3]).toLocalTime());
                existingIntervals.add(existingInterval);
            }

            for (ShiftIntervals newInterval : shiftRequest.getShiftIntervals()) {
                long meanTime = Duration.between(newInterval.getValidFrom(), newInterval.getValidEnd()).toMinutes();
                newInterval.setShiftMeanTime((int) meanTime);
                totalIntervalMeanTime += meanTime;
                newInterval.setSite(shift.getSite());

                for (ShiftIntervals existingInterval : existingIntervals) {
                    boolean isDateOverlapping = checkDateOverlap(newInterval.getValidFrom(), newInterval.getValidEnd(),
                            existingInterval.getValidFrom(), existingInterval.getValidEnd());
                    if (isDateOverlapping) {
                        boolean isTimeOverlapping = checkTimeOverlap(
                                newInterval.getStartTime(), newInterval.getEndTime(),
                                existingInterval.getStartTime(), existingInterval.getEndTime()
                        );

                        if (isTimeOverlapping) {
                            throw new ShiftException(5003);
                        }
                    }
                }

                if (newInterval.getBreakList() != null) {
                    for (Break breakObj : newInterval.getBreakList()) {
                        breakObj.setHandle(newInterval.getHandle());
                        breakObj.setSite(newInterval.getSite());
                    }
                }
            }

            shift.setShiftIntervals(shiftRequest.getShiftIntervals());
        } else {
            shift.setShiftIntervals(new ArrayList<>());
        }

        shift.setShiftMeanTime(totalIntervalMeanTime);
        shift.setActualTime(totalIntervalMeanTime);

        try {
            shiftPostgresRepository.save(shift);
            created = true;
        } catch (Exception e) {
            throw e;
        }
        return created;
    }


    private boolean checkTimeOverlap(LocalTime newStart, LocalTime newEnd, LocalTime existingStart, LocalTime existingEnd) {

        if (newStart.isBefore(newEnd)) {
            return !(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd));
        } else {
            return (newStart.isBefore(existingEnd) || newEnd.isAfter(existingStart));
        }
    }


    private boolean checkDateOverlap(LocalDateTime newFrom, LocalDateTime newTo,
                                     LocalDateTime existingFrom, LocalDateTime existingTo) {
        return !newTo.isBefore(existingFrom) && !newFrom.isAfter(existingTo);
    }

    private boolean intervalsOverlap(ShiftIntervals interval1, ShiftIntervals interval2) {
        return !(interval1.getValidEnd().isBefore(interval2.getValidFrom()) ||
                interval2.getValidEnd().isBefore(interval1.getValidFrom()));
    }

    private Shift createBuilder(ShiftRequest shiftRequest){
        String handleObj="";
        if(shiftRequest.getShiftType().equalsIgnoreCase("General")){
            handleObj="General";
        }
        if(shiftRequest.getShiftType().equalsIgnoreCase("Resource")){
            handleObj=shiftRequest.getResourceId();
        }
        if(shiftRequest.getShiftType().equalsIgnoreCase("Workcenter")){
            handleObj=shiftRequest.getWorkCenterId();
        }
        Shift shift = Shift.builder()
                .site(shiftRequest.getSite())
                .shiftId(shiftRequest.getShiftId())
                .handle("ShiftBO:" + shiftRequest.getSite() + "," + handleObj + "," + shiftRequest.getShiftId())
                .description(shiftRequest.getDescription())
                .version(shiftRequest.getVersion())
                .shiftType(shiftRequest.getShiftType())
                .workCenterId(shiftRequest.getWorkCenterId())
                .resourceId(shiftRequest.getResourceId())
                .createdBy(shiftRequest.getUserId())
                .modifiedBy(shiftRequest.getModifiedBy())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .build();

        if (shiftRequest.getShiftIntervals() != null) {
            for (ShiftIntervals interval : shiftRequest.getShiftIntervals()) {
                interval.setHandle("IntervalBO:" + "ShiftBO:"+ shiftRequest.getSite() + "," +handleObj +"," + shiftRequest.getShiftId());
                interval.setSite(shiftRequest.getSite());
                interval.setCreatedDateTime(LocalDateTime.now());
                interval.setCreatedBy(shiftRequest.getUserId());
                interval.setActive(1);
                interval.setShiftRef("ShiftBO:" + shiftRequest.getSite() + "," +handleObj +"," + shiftRequest.getShiftId());
                for(Break breaks:interval.getBreakList()){
                    breaks.setCreatedDateTime(LocalDateTime.now());
                    breaks.setHandle("BreakBO:"+ "IntervalBO:" + "ShiftBO:"+ shiftRequest.getSite() + "," +handleObj +"," + shiftRequest.getShiftId());
                    breaks.setSite(shiftRequest.getSite());
                    breaks.setCreatedBy(shiftRequest.getUserId());
                    breaks.setCreatedDateTime(LocalDateTime.now());
                    breaks.setIntervalRef("IntervalBO:" + "ShiftBO:"+ shiftRequest.getSite() + "," +handleObj +"," + shiftRequest.getShiftId());
                    breaks.setActive(1);
                    breaks.setShiftRef("ShiftBO:" + shiftRequest.getSite() + "," +handleObj +"," + shiftRequest.getShiftId());
                    breaks.setShiftType(shiftRequest.getShiftType());
                }
            }
        }
        else {

        }
        shift.setShiftIntervals(shiftRequest.getShiftIntervals());

        if (shiftRequest.getCalendarRules() != null) {
            for (CalendarRules rule : shiftRequest.getCalendarRules()) {
                rule.setHandle("RuleBO:"+ "IntervalBO:" + shiftRequest.getSite() + "," +handleObj +"," + shiftRequest.getShiftId());
                rule.setCreatedBy(shiftRequest.getUserId());
                rule.setCreatedDateTime(LocalDateTime.now());
                rule.setActive(1);
                rule.setShiftRef("ShiftBO:" + shiftRequest.getSite()+","+handleObj +"," + shiftRequest.getShiftId());
                rule.setSite(shiftRequest.getSite());
            }
        }
        shift.setCalendarRules(shiftRequest.getCalendarRules());

        if (shiftRequest.getCalendarOverrides() != null) {
            for (CalendarOverrides override : shiftRequest.getCalendarOverrides()) {
                override.setHandle("CalenderOvverides:" + shiftRequest.getSite() + "," + LocalDateTime.now());
                override.setCreatedBy(shiftRequest.getUserId());
                override.setCreatedDateTime(LocalDateTime.now());
                override.setActive(1);
                override.setSite(shiftRequest.getSite());
            }
        }
        shift.setCalendarOverrides(shiftRequest.getCalendarOverrides());

        if (shiftRequest.getCustomDataList() != null) {
            for (CustomData customData : shiftRequest.getCustomDataList()) {
                customData.setHandle("CustomDataBO:"+"ShiftBO:" + shiftRequest.getSite() + "," + handleObj + "," + shiftRequest.getShiftId());
                customData.setSite(shiftRequest.getSite());
                customData.setCreatedBy(shiftRequest.getUserId());
                customData.setCreatedDateTime(LocalDateTime.now());
                customData.setActive(1);
                customData.setSite(shiftRequest.getSite());
            }
        }
        shift.setCustomDataList(shiftRequest.getCustomDataList());

        return shift;
    }
    private ShiftMongo createBuilderForMongo(ShiftRequest shiftRequest){
        ShiftMongo shift = ShiftMongo.builder()
                .site(shiftRequest.getSite())
                .shiftId(shiftRequest.getShiftId())
                .handle("ShiftBO:" + shiftRequest.getSite() + "," + shiftRequest.getShiftType() + "," + shiftRequest.getShiftId())
                .description(shiftRequest.getDescription())
                .version(shiftRequest.getVersion())
                .shiftType(shiftRequest.getShiftType())
                .workCenterId(shiftRequest.getWorkCenterId())
                .resourceId(shiftRequest.getResourceId())
                .createdBy(shiftRequest.getCreatedBy())
                .modifiedBy(shiftRequest.getModifiedBy())
                .userId(shiftRequest.getUserId())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .build();

        if (shiftRequest.getShiftIntervals() != null) {
            for (ShiftIntervals interval : shiftRequest.getShiftIntervals()) {
                interval.setHandle("ShiftBO:" + shiftRequest.getSite() + "," + shiftRequest.getShiftType() + "," + shiftRequest.getShiftId());
                interval.setSite(shiftRequest.getSite());
                interval.setCreatedDateTime(LocalDateTime.now());
                for(Break breaks:interval.getBreakList()){
                    breaks.setCreatedDateTime(LocalDateTime.now());
                    breaks.setHandle("IntervalBO:"+ shiftRequest.getSite() + "," + shiftRequest.getShiftType() + "," + shiftRequest.getShiftId());
                    breaks.setShiftType(shiftRequest.getShiftType());
                }
            }
        }
        shift.setShiftIntervals(shiftRequest.getShiftIntervals());

        if (shiftRequest.getCalendarRules() != null) {
            for (CalendarRules rule : shiftRequest.getCalendarRules()) {
                rule.setHandle("ShiftBO:" + shiftRequest.getSite() + "," + shiftRequest.getShiftType() + "," + shiftRequest.getShiftId());
                rule.setSite(shiftRequest.getSite());
            }
        }
        shift.setCalendarRules(shiftRequest.getCalendarRules());

        if (shiftRequest.getCalendarOverrides() != null) {
            for (CalendarOverrides override : shiftRequest.getCalendarOverrides()) {
                override.setHandle("ShiftBO:" + shiftRequest.getSite() + "," + shiftRequest.getShiftType() + "," + shiftRequest.getShiftId());
                override.setSite(shiftRequest.getSite());
            }
        }
        shift.setCalendarOverrides(shiftRequest.getCalendarOverrides());

        if (shiftRequest.getCustomDataList() != null) {
            for (CustomData customData : shiftRequest.getCustomDataList()) {
                customData.setHandle("ShiftBO:" + shiftRequest.getSite() + "," + shiftRequest.getShiftType() + "," + shiftRequest.getShiftId());
                customData.setSite(shiftRequest.getSite());
            }
        }
        shift.setCustomDataList(shiftRequest.getCustomDataList());

        return shift;
    }
    private List<ShiftIntervals> setShiftIntervalsHandles(ShiftRequest shiftRequest) {
        return shiftRequest.getShiftIntervals().stream()
                .map(interval -> {

                    String intervalHandle = "ShiftBO:" + shiftRequest.getSite() + "," + shiftRequest.getShiftType() + "," + shiftRequest.getShiftId();
                    interval.setHandle(intervalHandle);
                    if (interval.getCreatedDateTime() == null) {
                        interval.setCreatedDateTime(LocalDateTime.now());
                    }

                    interval.setBreakList(setBreakHandle(interval.getBreakList(),  shiftRequest.getSite(), shiftRequest.getShiftType(), shiftRequest.getShiftId()));
                    return interval;
                })
                .collect(Collectors.toList());
    }

    private List<Break> setBreakHandle(List<Break> breakList, String site, String shiftType, String shiftId) {
        return breakList.stream()
                .map(breaks -> {
                    breaks.setHandle("IntervalBO:"+site+","+shiftType+","+shiftId);
                    breaks.setSite(site);
                    if (breaks.getCreatedDateTime() == null) {
                        breaks.setCreatedDateTime(LocalDateTime.now());
                    }
                    return breaks;
                })
                .collect(Collectors.toList());
    }

    private List<CalendarRules> setCalendarRulesHandles(ShiftRequest shiftRequest) {
        return shiftRequest.getCalendarRules().stream()
                .map(rule -> {
                    rule.setHandle("ShiftBO:" + shiftRequest.getSite() + "," +shiftRequest.getShiftType()+","+shiftRequest.getShiftId());
                    return rule;
                })
                .collect(Collectors.toList());
    }
    private List<CalendarOverrides> setCalendarOverridesHandles(ShiftRequest shiftRequest) {
        return shiftRequest.getCalendarOverrides().stream()
                .map(override -> {
                    override.setHandle("ShiftBO:" + shiftRequest.getSite() + "," +shiftRequest.getShiftType()+","+shiftRequest.getShiftId());
                    return override;
                })
                .collect(Collectors.toList());
    }

    private List<CustomData> setCustomDataListHandles(ShiftRequest shiftRequest) {
        return shiftRequest.getCustomDataList().stream()
                .map(customData -> {
                    customData.setHandle("ShiftBO:" + shiftRequest.getSite() + "," + shiftRequest.getShiftType() + "," + shiftRequest.getShiftId());
                    return customData;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ShiftMessageModel updateShift(ShiftRequest shiftRequest) throws Exception {

        List<Shift> existingShiftsPostgres= new ArrayList<>();
        if(shiftRequest.getShiftType().equalsIgnoreCase("Resource")){
            existingShiftsPostgres= shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndResourceIdAndActive(
                    shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), shiftRequest.getResourceId(),1);
        }
        if(shiftRequest.getShiftType().equalsIgnoreCase("WorkCenter")){
            existingShiftsPostgres= shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndWorkCenterIdAndActive(
                    shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), shiftRequest.getWorkCenterId(),1);
        }
        if(shiftRequest.getShiftType().equalsIgnoreCase("General")){
            existingShiftsPostgres = shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndActive(
                    shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), 1);
        }

        LocalDateTime currentTime = LocalDateTime.now();

        for (Shift shift : existingShiftsPostgres) {
            for (ShiftIntervals shiftIntervals : shift.getShiftIntervals()) {
                // Extract LocalDate from LocalDateTime
                LocalDate validFromDate = shiftIntervals.getValidFrom().toLocalDate();
                LocalDate validEndDate = shiftIntervals.getValidEnd().toLocalDate();

                // Get today's date
                LocalDate today = LocalDate.now();

                // Check if today is within the valid date range
                if (!today.isBefore(validFromDate) && !today.isAfter(validEndDate)) {
                    // Convert startTime and endTime to LocalDateTime
                    LocalDateTime shiftStartDateTime = LocalDateTime.of(today, shiftIntervals.getStartTime());
                    LocalDateTime shiftEndDateTime = LocalDateTime.of(today, shiftIntervals.getEndTime());

                    // Check if currentTime falls within the shift interval
                    if (!currentTime.isBefore(shiftStartDateTime) && !currentTime.isAfter(shiftEndDateTime)) {
                        throw new ShiftException(421);
                    }
                }
            }
        }

        // Step 2: Check if no existing active records are found, throw an exception
        if (existingShiftsPostgres.isEmpty()) {
            throw new ShiftException(411, shiftRequest.getShiftId());
        } else {
            // Step 3: Inactivate the existing records
            for (Shift existingShiftPostgres : existingShiftsPostgres) {
                existingShiftPostgres.setActive(0);  // Set the active flag to 0 for existing records

                // Mark all associated shift intervals as inactive
                for (ShiftIntervals shiftIntervals : existingShiftPostgres.getShiftIntervals()) {
                    shiftIntervals.setActive(0);

                    // Mark all associated breaks as inactive
                    for (Break aBreak : shiftIntervals.getBreakList()) {
                        aBreak.setActive(0);
                    }
                }

                // Mark associated calendar rules as inactive
                for (CalendarRules calendarRules : existingShiftPostgres.getCalendarRules()) {
                    calendarRules.setActive(0);
                }

                // Mark associated calendar overrides as inactive
                for (CalendarOverrides calendarOverrides : existingShiftPostgres.getCalendarOverrides()) {
                    calendarOverrides.setActive(0);
                }
                shiftPostgresRepository.save(existingShiftPostgres);  // Save the inactivated records
            }
        }

        // Step 4: Create a new Shift record from the request data
        try {
            // Create a new Shift record based on the request data
            Shift newShift = new Shift();
            updateBuilder(newShift, shiftRequest);  // Use the updateBuilder to map the new request data
            // Set the new record as active
            newShift.setActive(1);
            // Save the new Shift record
            shiftPostgresRepository.save(newShift);

            // Step 5: Return a success message
            MessageDetails messageDetails = new MessageDetails("Shift updated and new record created successfully", "S");
            return new ShiftMessageModel(null, messageDetails);
        } catch (Exception e) {
            // Step 6: Handle exceptions if the save operation fails
            throw new ShiftException(419);
        }
    }


    private Shift updateBuilder(Shift shift, ShiftRequest shiftRequest) {
        // Create a new Shift (if necessary) and set its fields based on the shiftRequest

        shift.setId(UUID.randomUUID());  // Generate a new ID for the shift
        shift.setSite(shiftRequest.getSite());  // Set the site from the request
        shift.setHandle(shiftRequest.getHandle());  // Handle, if applicable
        shift.setShiftId(shiftRequest.getShiftId());  // Set the new shift ID from the request
        shift.setDescription(shiftRequest.getDescription());  // Set description
        shift.setShiftType(shiftRequest.getShiftType());  // Set shift type
        shift.setWorkCenterId(shiftRequest.getWorkCenterId());  // Set work center ID
        shift.setResourceId(shiftRequest.getResourceId());  // Set resource ID
        shift.setShiftMeanTime(shiftRequest.getShiftMeanTime());  // Set shift mean time
        shift.setActualTime(shiftRequest.getActualTime());  // Set actual time
        shift.setVersion(shiftRequest.getVersion());  // Set version
        shift.setCreatedBy(shiftRequest.getCreatedBy());  // Set creator from request
        shift.setModifiedBy(shiftRequest.getModifiedBy());  // Set modifier from request
        shift.setCreatedDateTime(LocalDateTime.now());  // Set creation timestamp
        shift.setModifiedDateTime(LocalDateTime.now());  // Set modification timestamp
        shift.setActive(1);  // Set as active (assuming it's new)

        // Handle Shift Intervals
        if (shiftRequest.getShiftIntervals() != null) {
            List<ShiftIntervals> shiftIntervalsList = new ArrayList<>();
            for (ShiftIntervals shiftInterval : shiftRequest.getShiftIntervals()) {
                long meanTime = Duration.between(shiftInterval.getStartTime(), shiftInterval.getEndTime()).toMinutes();
                shiftInterval.setShiftMeanTime((int) meanTime);
                shiftInterval.setId(UUID.randomUUID());  // Generate a new ID for each interval
                shiftInterval.setShiftRef(shiftRequest.getHandle());
                shiftInterval.setModifiedDateTime(LocalDateTime.now());
                shiftInterval.setStartTime(shiftInterval.getStartTime());
                shiftInterval.setEndTime(shiftInterval.getEndTime());
                shiftInterval.setActive(1);// Set modified timestamp
                //shiftIntervalsList.add(shiftInterval);  // Add to the shift's intervals list
                shiftInterval.setHandle("IntervalBO:" + shiftRequest.getHandle());
                shiftInterval.setSite(shiftRequest.getSite());
                shiftInterval.setCreatedDateTime(LocalDateTime.now());
                shiftInterval.setCreatedBy(shiftRequest.getUserId());
                // Update Breaks inside Shift Intervals
                if (shiftInterval.getBreakList() != null) {
                    List<Break> breakList = new ArrayList<>();
                    for (Break breaks : shiftInterval.getBreakList()) {
                        breaks.setModifiedDateTime(LocalDateTime.now());
                        breaks.setHandle("BreakBO:IntervalBO:ShiftBO:" + shiftRequest.getHandle());
                        breaks.setId(UUID.randomUUID());
                        breaks.setSite(shiftRequest.getSite());
                        breaks.setModifiedBy(shiftRequest.getModifiedBy());
                        breaks.setIntervalRef("IntervalBO:ShiftBO:" + shiftRequest.getHandle());
                        breaks.setShiftRef("ShiftBO:" + shiftRequest.getHandle());
                        breaks.setShiftType(shiftRequest.getShiftType());
                        breaks.setActive(1);
                        breakList.add(breaks);
                    }
                    shiftInterval.setBreakList(breakList);
                }
                shiftIntervalsList.add(shiftInterval);
            }
            shift.setShiftIntervals(shiftIntervalsList);
        }

        // Handle Calendar Rules
        if (shiftRequest.getCalendarRules() != null) {
            List<CalendarRules> calendarRulesList = new ArrayList<>();
            for (CalendarRules calendarRule : shiftRequest.getCalendarRules()) {
                calendarRule.setId(UUID.randomUUID());  // Generate a new ID for each rule
                calendarRule.setSite(shiftRequest.getSite());  // Set site for each rule
                calendarRule.setModifiedDateTime(LocalDateTime.now());  // Set modified timestamp
                calendarRulesList.add(calendarRule);  // Add to the shift's calendar rules
                calendarRule.setActive(1);
            }
            shift.setCalendarRules(calendarRulesList);  // Set calendar rules in the shift
        }

        // Handle Calendar Overrides
        if (shiftRequest.getCalendarOverrides() != null) {
            List<CalendarOverrides> calendarOverridesList = new ArrayList<>();
            for (CalendarOverrides calendarOverride : shiftRequest.getCalendarOverrides()) {
                calendarOverride.setId(UUID.randomUUID());  // Generate a new ID for each override
                calendarOverride.setSite(shiftRequest.getSite());  // Set site for each override
                calendarOverride.setModifiedDateTime(LocalDateTime.now());  // Set modified timestamp
                calendarOverridesList.add(calendarOverride);  // Add to the shift's calendar overrides
                calendarOverride.setActive(1);
            }
            shift.setCalendarOverrides(calendarOverridesList);  // Set calendar overrides in the shift
        }

        // Handle Custom Data
        if (shiftRequest.getCustomDataList() != null) {
            List<CustomData> customDataList = new ArrayList<>();
            for (CustomData customData : shiftRequest.getCustomDataList()) {
                customData.setId(UUID.randomUUID());  // Generate a new ID for each custom data
                customData.setSite(shiftRequest.getSite());  // Set site for each custom data
                customData.setModifiedDateTime(LocalDateTime.now());  // Set modified timestamp
                customDataList.add(customData);  // Add to the shift's custom data list
                customData.setActive(1);
            }
            shift.setCustomDataList(customDataList);  // Set custom data list in the shift
        }

        return shift;  // Return the newly created Shift
    }




    private ShiftMongo updateBuilderForMongo(ShiftMongo shift, ShiftRequest shiftRequest){
        shift.setModifiedBy(shiftRequest.getModifiedBy());
        shift.setModifiedDateTime(LocalDateTime.now());
        shift.setDescription(shiftRequest.getDescription());
        shift.setShiftType(shiftRequest.getShiftType());
        shift.setWorkCenterId(shiftRequest.getWorkCenterId());
        shift.setResourceId(shiftRequest.getResourceId());
        shift.setActive(1);

        shift.getShiftIntervals().removeIf(interal -> !shiftRequest.getShiftIntervals().contains(interal));
        shift.getShiftIntervals().addAll(shiftRequest.getShiftIntervals());
        shift.setShiftIntervals(setShiftIntervalsHandles(shiftRequest));

        for(ShiftIntervals intervals:shift.getShiftIntervals()){
          /*  intervals.setShiftRef(shiftRequest.getShiftId());
           */ intervals.setSite(shiftRequest.getSite());
            intervals.setModifiedDateTime(LocalDateTime.now());

            for(Break breakTime:intervals.getBreakList()){
                breakTime.setModifiedDateTime(LocalDateTime.now());
                breakTime.setShiftType(shiftRequest.getShiftType());
            }
        }

        if (shiftRequest.getCalendarRules() != null) {
            for (CalendarRules rule : shiftRequest.getCalendarRules()) {
                rule.setSite(shiftRequest.getSite());
            }
        }
        shift.getCalendarRules().removeIf(interval -> !shiftRequest.getCalendarRules().contains(interval));
        shift.getCalendarRules().addAll(shiftRequest.getCalendarRules());
        shift.setCalendarRules(setCalendarRulesHandles(shiftRequest));

        if (shiftRequest.getCalendarOverrides() != null) {
            for (CalendarOverrides ride : shiftRequest.getCalendarOverrides()) {
                ride.setSite(shiftRequest.getSite());
            }
        }
        shift.getCalendarOverrides().removeIf(interval -> !shiftRequest.getCalendarOverrides().contains(interval));
        shift.getCalendarOverrides().addAll(shiftRequest.getCalendarOverrides());
        shift.setCalendarOverrides(setCalendarOverridesHandles(shiftRequest));


        if (shiftRequest.getCustomDataList()!= null) {
            for (CustomData data : shiftRequest.getCustomDataList()) {
                data.setSite(shiftRequest.getSite());
            }
        }
        shift.getCustomDataList().removeIf(interval -> !shiftRequest.getCustomDataList().contains(interval));
        shift.getCustomDataList().addAll(shiftRequest.getCustomDataList());
        shift.setCustomDataList(setCustomDataListHandles(shiftRequest));

        return shift;
    }


    @Override
    public ShiftMessageModel deleteShift(ShiftRequest shiftRequest) {

        List<Shift> existingShiftsPostgres= new ArrayList<>();
        if(shiftRequest.getShiftType().equalsIgnoreCase("Resource")){
            existingShiftsPostgres= shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndResourceIdAndActive(
                    shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), shiftRequest.getResourceId(),1);
        }
        if(shiftRequest.getShiftType().equalsIgnoreCase("WorkCenter")){
            existingShiftsPostgres= shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndWorkCenterIdAndActive(
                    shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), shiftRequest.getWorkCenterId(),1);
        }
        if(shiftRequest.getShiftType().equalsIgnoreCase("General")){
            existingShiftsPostgres = shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndActive(
                    shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), 1);
        }

        int postgresSuccess = 0;
        LocalDateTime currentTime = LocalDateTime.now();

        for (Shift shift : existingShiftsPostgres) {
            for (ShiftIntervals shiftIntervals : shift.getShiftIntervals()) {
                // Extract LocalDate from LocalDateTime
                LocalDate validFromDate = shiftIntervals.getValidFrom().toLocalDate();
                LocalDate validEndDate = shiftIntervals.getValidEnd().toLocalDate();

                // Get today's date
                LocalDate today = LocalDate.now();

                // Check if today is within the valid date range
                if (!today.isBefore(validFromDate) && !today.isAfter(validEndDate)) {
                    // Convert startTime and endTime to LocalDateTime
                    LocalDateTime shiftStartDateTime = LocalDateTime.of(today, shiftIntervals.getStartTime());
                    LocalDateTime shiftEndDateTime = LocalDateTime.of(today, shiftIntervals.getEndTime());

                    // Check if currentTime falls within the shift interval
                    if (!currentTime.isBefore(shiftStartDateTime) && !currentTime.isAfter(shiftEndDateTime)) {
                        throw new ShiftException(421);
                    }
                }
            }
        }

        /*for (Shift existingShiftPostgres : existingShiftsPostgres) {
            existingShiftPostgres.setActive(0);
            if (shiftPostgresRepository.save(existingShiftPostgres) != null) {
                postgresSuccess++;
            }
        }*/

        for (Shift existingShiftPostgres : existingShiftsPostgres) {
            existingShiftPostgres.setActive(0); // Set shift as inactive

            // Mark all associated shift intervals as inactive
            for (ShiftIntervals shiftIntervals : existingShiftPostgres.getShiftIntervals()) {
                shiftIntervals.setActive(0);

                // Mark all associated breaks as inactive
                for (Break aBreak : shiftIntervals.getBreakList()) {
                    aBreak.setActive(0);
                }
            }

            // Mark associated calendar rules as inactive
            for (CalendarRules calendarRules : existingShiftPostgres.getCalendarRules()) {
                calendarRules.setActive(0);
            }

            // Mark associated calendar overrides as inactive
            for (CalendarOverrides calendarOverrides : existingShiftPostgres.getCalendarOverrides()) {
                calendarOverrides.setActive(0);
            }

            if (shiftPostgresRepository.save(existingShiftPostgres) != null) {
                postgresSuccess++;
            }
        }

        if (postgresSuccess == 0) {
            throw new ShiftException(420);
        }
        MessageDetails messageDetails = new MessageDetails("Shift deleted successfully", "S");
        return new ShiftMessageModel(null ,messageDetails);
    }

    @Override
    public ShiftResponse retrieveShift(ShiftRequest shiftRequest) {
        List<Shift> existingShiftsPostgres= new ArrayList<>();
        Shift shift= new Shift();
        if(shiftRequest.getShiftType().equalsIgnoreCase("Resource")){
            existingShiftsPostgres= shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndResourceIdAndActive(
                    shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), shiftRequest.getResourceId(),1);
            shift= existingShiftsPostgres.get(0);
        }
        if(shiftRequest.getShiftType().equalsIgnoreCase("WorkCenter")){
            existingShiftsPostgres= shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndWorkCenterIdAndActive(
                    shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), shiftRequest.getWorkCenterId(),1);

            shift= existingShiftsPostgres.get(0); }
        if(shiftRequest.getShiftType().equalsIgnoreCase("General")){
            existingShiftsPostgres = shiftPostgresRepository.findBySiteAndShiftIdAndAndShiftTypeAndActive(
                    shiftRequest.getSite(), shiftRequest.getShiftId(),shiftRequest.getShiftType(), 1);

            shift= existingShiftsPostgres.get(0); }

        if(shiftRequest.getShiftType().equalsIgnoreCase("HANDLE")){
            existingShiftsPostgres = shiftPostgresRepository.findByHandleAndActive(shiftRequest.getShiftId(), 1);

            shift= existingShiftsPostgres.get(0); }
        if (shift == null || shift.getActive()!=1) {
            throw new ShiftException(411,shiftRequest.getShiftId());
        }
        ShiftResponse shiftResponse=createShiftResponseBuilder(shift);
        return shiftResponse;

    }
    private ShiftResponse createShiftResponseBuilder(Shift shift) {
        ShiftResponse shiftResponse = new ShiftResponse();
        shiftResponse.setHandle(shift.getHandle());
        shiftResponse.setSite(shift.getSite());
        shiftResponse.setShiftId(shift.getShiftId());
        shiftResponse.setDescription(shift.getDescription());
        shiftResponse.setShiftType(shift.getShiftType());
        shiftResponse.setCreatedBy(shift.getCreatedBy());
        shiftResponse.setModifiedBy(shift.getModifiedBy());
        shiftResponse.setCreatedDateTime(shift.getCreatedDateTime());
        shiftResponse.setModifiedDateTime(shift.getModifiedDateTime());
        shiftResponse.setActive(shift.getActive());
        shiftResponse.setShiftMeanTime(shift.getShiftMeanTime());
        shiftResponse.setActualTime(shift.getActualTime());
        shiftResponse.setVersion(shift.getVersion());

        // Filter only active shift intervals
        if (shift.getShiftIntervals() != null) {
            List<ShiftIntervals> activeIntervals = shift.getShiftIntervals().stream()
                    .filter(interval -> interval.getActive() == 1)  // Only include active intervals
                    .collect(Collectors.toList());
            shiftResponse.setShiftIntervals(activeIntervals);
        } else {
            shiftResponse.setShiftIntervals(Collections.emptyList());
        }

        shiftResponse.setCalendarRules(shift.getCalendarRules());
        shiftResponse.setCalendarOverrides(shift.getCalendarOverrides());
        shiftResponse.setCustomDataList(shift.getCustomDataList());

        if ("WorkCenter".equalsIgnoreCase(shift.getShiftType())) {
            shiftResponse.setWorkCenterId(shift.getWorkCenterId());
        } else if ("Resource".equalsIgnoreCase(shift.getShiftType())) {
            shiftResponse.setResourceId(shift.getResourceId());
        }

        return shiftResponse;
    }


    @Override
    public List<Shift> retrieveShiftByVersion(String site, String shiftId, String version) {
        if (version == null)
        {
            return shiftPostgresRepository.findTopBySiteOrderByCreatedDateTimeDesc(site);
        } else
        {
            return shiftPostgresRepository.findByVersion(version);
        }

    }


    @Override
    public ShiftResponseList retrieveTop50(String site) {

        List<ShiftResponse> shiftResponseList =shiftPostgresRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1,site);
        return new ShiftResponseList(shiftResponseList);
    }


    @Override
    public ShiftResponseList retrieveAll(ShiftRequest shiftRequest) {
        List<ShiftResponse> shiftResponseList =shiftPostgresRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1,shiftRequest.getSite());
        return new ShiftResponseList(shiftResponseList);
    }



    @Override
    public MinutesList getPlannedtime(String site) {
//        LocalDateTime now = LocalDateTime.now();
        Instant now=Instant.now();  //Instant to represent time in UTC, which is compatible with MongoDB
        Query query = new Query();
        query.addCriteria(Criteria.where("site").is(site)
                .and("active").is(1)
                .and("shiftIntervals.validFrom").lte(now)
                .and("shiftIntervals.validEnd").gte(now));

        ShiftMongo activeShift = mongoTemplate.findOne(query, ShiftMongo.class);

        if (activeShift != null && isProductionDay(activeShift)) {
            List<PlannedMinutes> plannedMinutesList = new ArrayList<>();
            for (ShiftIntervals interval : activeShift.getShiftIntervals()) {
                PlannedMinutes plannedMinutes = new PlannedMinutes();
                plannedMinutes.setShiftName(activeShift.getShiftId());
                plannedMinutes.setShiftType(activeShift.getShiftType());
                plannedMinutes.setStartTime(interval.getValidFrom().toString());
                plannedMinutes.setEndTime(interval.getValidEnd().toString());
                plannedMinutes.setPlannedTime((int) Duration.between(interval.getValidFrom(), interval.getValidEnd()).toMinutes());
                plannedMinutesList.add(plannedMinutes);
            }
            return new MinutesList(plannedMinutesList);
        }
        return new MinutesList(new ArrayList<>());
    }

    @Override
    public MinutesList getPlannedtimeTillNow(String site) {
//        LocalDateTime now = LocalDateTime.now();
        Instant now=Instant.now();  //Instant to represent time in UTC, which is compatible with MongoDB
        LocalDateTime nowDateTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
        Query query = new Query();
        query.addCriteria(Criteria.where("site").is(site)
                .and("active").is(1)
                .and("shiftIntervals.validFrom").lte(nowDateTime)
                .and("shiftIntervals.validEnd").gte(nowDateTime));
        ShiftMongo activeShift = mongoTemplate.findOne(query, ShiftMongo.class);
        if (activeShift != null && activeShift.getShiftIntervals()!=null) {
            List<PlannedMinutes> plannedMinutesList = new ArrayList<>();
            for (ShiftIntervals interval : activeShift.getShiftIntervals()) {

                LocalDateTime validFrom = interval.getValidFrom().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime validEnd = interval.getValidEnd().atZone(ZoneId.systemDefault()).toLocalDateTime();

                PlannedMinutes plannedMinutes = new PlannedMinutes();

                plannedMinutes.setShiftName(activeShift.getShiftId());
                plannedMinutes.setShiftType(activeShift.getShiftType());
                plannedMinutes.setStartTime(validFrom.toString());
                plannedMinutes.setEndTime(validEnd.isAfter(nowDateTime) ? nowDateTime.toString() : validEnd.toString());
                long plannedDuration = Duration.between(validFrom, validEnd.isAfter(nowDateTime) ? nowDateTime : validEnd).toMinutes();
                plannedMinutes.setPlannedTime((int) plannedDuration);
                plannedMinutesList.add(plannedMinutes);

            }
            return new MinutesList(plannedMinutesList);
        }
        return new MinutesList(new ArrayList<>());
    }

    @Override
    public PlannedMinutes getPlannedTimeTillNowByType(String site, String shiftType, String resourceId, String workCenterId) {
//        LocalDateTime now = LocalDateTime.now();
        Instant now=Instant.now();
        LocalDateTime nowDateTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
        Query query = new Query();
        query.addCriteria(Criteria.where("site").is(site)
                .and("shiftType").is(shiftType)
                .and("resourceId").is(resourceId)
                .and("workCenterId").is(workCenterId)
                .and("active").is(1)
                .and("shiftIntervals.validFrom").lte(nowDateTime)
                .and("shiftIntervals.validEnd").gte(nowDateTime));
        ShiftMongo activeShift = mongoTemplate.findOne(query, ShiftMongo.class);
        if (activeShift != null && activeShift.getShiftIntervals()!=null) {
            List<PlannedMinutes> plannedMinutesList = new ArrayList<>();
            for (ShiftIntervals interval : activeShift.getShiftIntervals()) {

                LocalDateTime validFrom = interval.getValidFrom().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime validEnd = interval.getValidEnd().atZone(ZoneId.systemDefault()).toLocalDateTime();

                PlannedMinutes plannedMinutes = new PlannedMinutes();

                plannedMinutes.setShiftName(activeShift.getShiftId());
                plannedMinutes.setShiftType(activeShift.getShiftType());
                plannedMinutes.setStartTime(validFrom.toString());
                plannedMinutes.setEndTime(validEnd.isAfter(nowDateTime) ? nowDateTime.toString() : validEnd.toString());
                long plannedDuration = Duration.between(validFrom, validEnd.isAfter(nowDateTime) ? nowDateTime : validEnd).toMinutes();
                plannedMinutes.setPlannedTime((int) plannedDuration);
                plannedMinutesList.add(plannedMinutes);
            }
            return plannedMinutesList.isEmpty() ? null : plannedMinutesList.get(0);
        }
        return null;
    }

    @Override
    public BreakMinutesList getBreakHours(String site) {
        //LocalDateTime now = LocalDateTime.now();
//        ZonedDateTime now=ZonedDateTime.now(ZoneOffset.UTC);

        Query query = new Query();
        Instant now=Instant.now();  //Instant to represent time in UTC, which is compatible with MongoDB
        query.addCriteria(Criteria.where("site").is(site)
                .and("active").is(1)
                .and("shiftIntervals.validFrom").lte(now)
                .and("shiftIntervals.validEnd").gte(now));

        ShiftMongo activeShift = mongoTemplate.findOne(query, ShiftMongo.class);

        if (activeShift != null && activeShift.getShiftIntervals()!=null) {
            List<BreakMinutes> breakMinutesList = new ArrayList<>();
            for (ShiftIntervals interval : activeShift.getShiftIntervals()) {
                BreakMinutes breakMinutes = new BreakMinutes();
                breakMinutes.setShiftId(activeShift.getShiftId());
                breakMinutes.setShiftType(activeShift.getShiftType());
                breakMinutes.setStartTime(interval.getValidFrom().toLocalTime());
                breakMinutes.setEndTime(interval.getValidEnd().toLocalTime());
                breakMinutes.setBreakTime((int) Duration.between(interval.getValidFrom(), interval.getValidEnd()).toMinutes());
                breakMinutes.setPlannedTime(activeShift.getShiftMeanTime());
                breakMinutesList.add(breakMinutes);
            }
            return new BreakMinutesList(breakMinutesList);
        }
        return new BreakMinutesList(new ArrayList<>());
    }

    @Override
    public BreakMinutesList getBreakHoursTillNow(String site) {
//        LocalDateTime now = LocalDateTime.now();
        // Use Instant to represent current time in UTC
        Instant now = Instant.now();
        LocalDateTime nowDateTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
        Query query = new Query();
        query.addCriteria(Criteria.where("site").is(site)
                .and("active").is(1)
                .and("shiftIntervals.validFrom").lte(nowDateTime)
                .and("shiftIntervals.validEnd").gte(nowDateTime));

        ShiftMongo activeShift = mongoTemplate.findOne(query, ShiftMongo.class);
        if (activeShift != null && activeShift.getShiftIntervals() != null) {
            List<BreakMinutes> breakMinutesList = new ArrayList<>();
            for (ShiftIntervals interval : activeShift.getShiftIntervals()) {
                LocalDateTime validFrom = interval.getValidFrom().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime validEnd = interval.getValidEnd().atZone(ZoneId.systemDefault()).toLocalDateTime();
                BreakMinutes breakMinutes = new BreakMinutes();
                breakMinutes.setShiftId(activeShift.getShiftId());
                breakMinutes.setShiftType(activeShift.getShiftType());
                breakMinutes.setStartTime(validFrom.toLocalTime());
                breakMinutes.setEndTime(validEnd.isAfter(nowDateTime) ? nowDateTime.toLocalTime(): validEnd.toLocalTime());
                LocalDateTime endTime = validEnd.isAfter(nowDateTime) ? nowDateTime : validEnd;
                breakMinutes.setBreakTime((int) Duration.between(validFrom, endTime).toMinutes());
                breakMinutes.setPlannedTime(activeShift.getShiftMeanTime());

                breakMinutesList.add(breakMinutes);
            }
            return new BreakMinutesList(breakMinutesList);
        }
        return new BreakMinutesList(new ArrayList<>());
    }
    @Override
    public BreakMinutes getBreakHoursTillNowByType(String site, String shiftType, String resourceId, String workCenterId, LocalDateTime localDateTime) {

        Instant now = Instant.now();
        LocalDateTime nowDateTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
        Query query = new Query();
        query.addCriteria(Criteria.where("site").is(site)
                .and("shiftType").is(shiftType)
                .and("resourceId").is(resourceId)
                .and("workCenterId").is(workCenterId)
                .and("active").is(1)
                .and("shiftIntervals.validFrom").lte(nowDateTime)
                .and("shiftIntervals.validEnd").gte(nowDateTime));

        ShiftMongo activeShift = mongoTemplate.findOne(query, ShiftMongo.class);
        if (activeShift != null && activeShift.getShiftIntervals() != null) {
            List<BreakMinutes> breakMinutesList = new ArrayList<>();
            for (ShiftIntervals interval : activeShift.getShiftIntervals()) {
                LocalDateTime validFrom = interval.getValidFrom().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime validEnd = interval.getValidEnd().atZone(ZoneId.systemDefault()).toLocalDateTime();
                BreakMinutes breakMinutes = new BreakMinutes();
                breakMinutes.setShiftId(activeShift.getShiftId());
                breakMinutes.setShiftType(activeShift.getShiftType());
                breakMinutes.setStartTime(validFrom.toLocalTime());
                breakMinutes.setEndTime(validEnd.isAfter(nowDateTime) ? nowDateTime.toLocalTime() : validEnd.toLocalTime());
                LocalDateTime endTime = validEnd.isAfter(nowDateTime) ? nowDateTime : validEnd;
                breakMinutes.setBreakTime((int) Duration.between(validFrom, endTime).toMinutes());
                breakMinutes.setPlannedTime(activeShift.getShiftMeanTime());
                breakMinutes.setShiftCreatedDatetime(activeShift.getCreatedDateTime());

                breakMinutesList.add(breakMinutes);
            }
            return breakMinutesList.isEmpty() ? null : breakMinutesList.get(0);
        }
        return null;
    }

    @Override
    public List<ShiftIntervalWithDate> getShiftsWithDatesInRange(String site, String shiftType, String resourceId, String workCenterId, LocalDateTime dateStart, LocalDateTime dateEnd) {
        // Convert dateStart and dateEnd to UTC ZonedDateTime to match MongoDB storage format
        ZonedDateTime utcDateStart = dateStart.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime utcDateEnd = dateEnd.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));

        Query query = new Query();
        query.addCriteria(Criteria.where("site").is(site)
                .and("shiftType").is(shiftType)
                .and("resourceId").is(resourceId)
                .and("workCenterId").is(workCenterId)
                .andOperator(
                        Criteria.where("shiftIntervals.validFrom").lte(utcDateEnd.toInstant()),
                        Criteria.where("shiftIntervals.validEnd").gte(utcDateStart.toInstant())
                ));

        List<ShiftMongo> shifts = mongoTemplate.find(query, ShiftMongo.class);
        List<ShiftIntervalWithDate> shiftIntervals = new ArrayList<>();

        for (ShiftMongo shift : shifts) {
            for (ShiftIntervals interval : shift.getShiftIntervals()) {

                if (!interval.getValidEnd().isBefore(utcDateStart.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime())
                        && !interval.getValidFrom().isAfter(utcDateEnd.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime())) {

                    ShiftIntervalWithDate intervalWithDate = new ShiftIntervalWithDate();

                    intervalWithDate.setDate(interval.getValidFrom().toLocalDate());
                    BreakMinutes breakMinutes = new BreakMinutes();

                    breakMinutes.setShiftId(shift.getShiftId());
                    breakMinutes.setShiftType(shift.getShiftType());
                    breakMinutes.setStartTime(interval.getValidFrom().toLocalTime());
                    breakMinutes.setEndTime(interval.getValidEnd().toLocalTime());
                    breakMinutes.setBreakTime((int) Duration.between(interval.getValidFrom(), interval.getValidEnd()).toMinutes());
                    breakMinutes.setPlannedTime(shift.getShiftMeanTime());
                    intervalWithDate.setBreakMinutes(breakMinutes);

                    shiftIntervals.add(intervalWithDate);
                }
            }
        }
        return shiftIntervals;
    }

    @Override
    public ShiftMongo getShiftByDates(LocalDateTime datetime) {

        Instant utcInstant = datetime.atZone(ZoneId.systemDefault()).toInstant();
        Query query = new Query();
        query.addCriteria(Criteria.where("shiftIntervals")
                .elemMatch(Criteria.where("validFrom").lte(utcInstant)
                        .and("validEnd").gte(utcInstant)));
        return mongoTemplate.findOne(query, ShiftMongo.class);
    }



    @Override
    public List<ShiftIntervalWithDate> getShiftsWithDatesInRanges(String site, String shiftType, String resource, String workCenter, LocalDateTime dateStart, LocalDateTime dateEnd) {
        return null;
    }
    @Override
    public String callExtension(Extension extension) throws Exception {
        return null;
    }
    @Override
    public AuditLogRequest createAuditLog(ShiftRequest shiftRequest) {
        return AuditLogRequest.builder()
                .site(shiftRequest.getSite())
                .action_code("SHIFT-CREATED")
                .action_detail("Shift Created " + shiftRequest.getShiftId() + "/" + shiftRequest.getShiftType())
                .action_detail_handle("ActionDetailBO:" + shiftRequest.getSite() + "," + "SHIFT-CREATED" + shiftRequest.getShiftId() + ":" + "com.rits.shiftservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(shiftRequest.getUserId())
                .txnId("SHIFT-CREATED" + LocalDateTime.now() + shiftRequest.getShiftId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Create")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest updateAuditLog(ShiftRequest shiftRequest) {
        return AuditLogRequest.builder()
                .site(shiftRequest.getSite())
                .action_code("SHIFT-UPDATED")
                .action_detail("Shift Updated " + shiftRequest.getShiftId() + "/" + shiftRequest.getShiftType())
                .action_detail_handle("ActionDetailBO:" + shiftRequest.getSite() + "," + "SHIFT-UPDATED" + shiftRequest.getShiftId() + ":" + "com.rits.shiftservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(shiftRequest.getUserId())
                .txnId("SHIFT-UPDATED" + LocalDateTime.now() + shiftRequest.getShiftId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Update")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest deleteAuditLog(ShiftRequest shiftRequest) {
        return AuditLogRequest.builder()
                .site(shiftRequest.getSite())
                .action_code("SHIFT-DELETED")
                .action_detail("Shift Deleted " + shiftRequest.getShiftId() + "/" + shiftRequest.getShiftType())
                .action_detail_handle("ActionDetailBO:" + shiftRequest.getSite() + "," + "SHIFT-DELETED" + shiftRequest.getShiftId() + ":" + "com.rits.shiftservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(shiftRequest.getUserId())
                .txnId("SHIFT-DELETED" + LocalDateTime.now() + shiftRequest.getShiftId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Delete")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }


    private int calculatePlannedTimeTillNow(Shift shift) {
        return shift.getShiftIntervals().stream()
                .mapToInt(interval -> {
                    LocalDateTime start = interval.getValidFrom();
                    LocalDateTime end = interval.getValidEnd().isAfter(LocalDateTime.now()) ? LocalDateTime.now() : interval.getValidEnd();
                    return (int) Duration.between(start, end).toMinutes();
                })
                .sum();
    }

    private int calculateBreakTime(Shift shift) {
        return shift.getShiftIntervals().stream()
                .flatMap(interval -> interval.getBreakList().stream())
                .mapToInt(breakInterval -> {
                    LocalDateTime start = LocalDateTime.parse(breakInterval.getBreakTimeStart().toString());
                    LocalDateTime end = LocalDateTime.parse(breakInterval.getBreakTimeEnd().toString());
                    return (int) Duration.between(start, end).toMinutes();
                })
                .sum();
    }

    private int calculateBreakTimeTillNow(Shift shift) {
        return shift.getShiftIntervals().stream()
                .flatMap(interval -> interval.getBreakList().stream())
                .filter(breakInterval -> breakInterval.getBreakTimeEnd().equals(LocalDateTime.now()))
                .mapToInt(breakInterval -> {
                    // LocalDateTime start = breakInterval.getBreakTimeStart();
                    //LocalDateTime end = breakInterval.getBreakTimeEnd();
                    //  return (int) Duration.between(start, end).toMinutes();
                    return 0;
                })
                .sum();
    }

    private int calculateBreakTimeTillNowByDate(Shift shift, LocalDateTime dateTime) {
        return shift.getShiftIntervals().stream()
                .flatMap(interval -> interval.getBreakList().stream())
//                .filter(breakInterval -> breakInterval.getBreakTimeEnd().)
                .mapToInt(breakInterval -> {
//                    LocalDateTime start = breakInterval.getBreakTimeStart();
//                    LocalDateTime end = breakInterval.getBreakTimeEnd();
//                    return (int) Duration.between(start, end).toMinutes();
                    return 0;
                })
                .sum();
    }

    private int calculatePlannedTime(Shift shift) {
        return shift.getShiftIntervals().stream()
                .mapToInt(interval -> (int) Duration.between(interval.getValidFrom(), interval.getValidEnd()).toMinutes())
                .sum();
    }

    private boolean isProductionDay(ShiftMongo shift) {
        // Placeholder logic for checking if it is a production day
        return true;
    }

    @Override
    public List<Shift> findShiftWithIntervalsAndBreaks(String shiftRef, String site, LocalDateTime startDate, LocalDateTime endDate) {
        // Define the SQL query
        String sql = "SELECT s.id, s.site, s.handle, s.shift_id, " +
                "si.id AS interval_id, si.start_time, si.end_time, si.valid_from, si.valid_end, " +
                "b.id AS break_id, b.break_time_start, b.break_time_end " +
                "FROM r_shift s " +
                "JOIN r_shift_intervals si ON s.id = si.shift_fk " +
                "JOIN r_break b ON si.id = b.shift_interval_id " +
                "WHERE s.active = 1 " +
                "AND si.active = 1 " +
                "AND b.active = 1 " +
                "AND s.handle = :shiftRef " +
                "AND s.site = :site " +
                "AND si.valid_from <= :endDate " +
                "AND (si.valid_end >= :startDate OR si.valid_end IS NULL)";

        // Create parameters map
        Map<String, Object> params = new HashMap<>();
        params.put("shiftRef", shiftRef);
        params.put("site", site);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        // Print the query with parameters for debugging
        System.out.println("Executing Query: " + sql);
        System.out.println("With Parameters: " + params);

        // Execute the query using NamedParameterJdbcTemplate
        List<Shift> shifts = namedParameterJdbcTemplate.query(sql, params, new RowMapper<Shift>() {
            @Override
            public Shift mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
                // Map the Shift entity
                Shift shift = new Shift();
                shift.setSite(rs.getString("site"));
                shift.setHandle(rs.getString("handle"));
                shift.setShiftId(rs.getString("shift_id"));

                // Map the ShiftInterval entity
                ShiftIntervals interval = new ShiftIntervals();
                interval.setStartTime(rs.getTime("start_time").toLocalTime());
                interval.setEndTime(rs.getTime("end_time").toLocalTime());
                interval.setValidFrom(rs.getTimestamp("valid_from").toLocalDateTime());
                interval.setValidEnd(rs.getTimestamp("valid_end") != null ? rs.getTimestamp("valid_end").toLocalDateTime() : null);

                // Map the Break entity
                Break breakObj = new Break();
                breakObj.setBreakTimeStart(rs.getTime("break_time_start").toLocalTime());
                breakObj.setBreakTimeEnd(rs.getTime("break_time_end").toLocalTime());

                // Set the break list for the interval
                interval.setBreakList(List.of(breakObj));

                // Add the interval to the shift
                shift.setShiftIntervals(List.of(interval));

                return shift;
            }
        });

        return shifts;
    }

    public List<Shift> findShiftWithIntervalsAndBreak(String shiftRef, String site, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return shiftPostgresRepository.findShiftWithIntervalsAndBreaks(shiftRef, site, startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace(); // or log the error
            throw new RuntimeException("Error executing query", e);
        }
    }

    @Override
    public List<ShiftOutput> getShifts(ShiftInput input) {
        LocalDate startDate = input.startDateTime.toLocalDate();
        LocalDate endDate = input.endDateTime.toLocalDate();
        List<ShiftOutput> allOutputs = new ArrayList<>();

        // Process shifts if productive
        List<Shift> shifts = findShiftByHierarchy(input.site, input.resource, input.workcenter);
        if (shifts.isEmpty()) {
            throw new IllegalArgumentException("No active shifts found for the given criteria.");
        }

        // Process day-by-day
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            // Determine the effective input interval for this day.
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = LocalDateTime.of(day, LocalTime.MAX); // end-of-day
            LocalDateTime effectiveStart = input.startDateTime.isAfter(dayStart) ? input.startDateTime : dayStart;
            LocalDateTime effectiveEnd = input.endDateTime.isBefore(dayEnd) ? input.endDateTime : dayEnd;
            if (!effectiveEnd.isAfter(effectiveStart)) {
                continue; // no input for this day
            }

            // Build a query Date for calendar checks.
            Date queryDate = Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
            String dayOfWeek = day.getDayOfWeek().toString();

            for (Shift shift : shifts) {
                String shiftRef = shift.getHandle();

                // Check Calendar Override and Calendar Rules for this day.
                Optional<CalendarOverrides> override = calendarOverrideRepository.findBySiteAndDate(input.site, queryDate);
                if (override.isPresent()) {
                    continue; // Skip non-productive shifts
                }
                Optional<CalendarRules> rule = calendarRuleRepository.findByShiftRefAndProductionDayAndDay(shiftRef, "Nonproduction", dayOfWeek);
                if (rule.isPresent()) {
                    continue; // Skip non-productive shifts
                }

                // Get valid intervals for this shift.
                List<ShiftIntervals> intervals = shiftIntervalRepository.findIntervalsByShiftRef(shiftRef).stream()
                        .filter(interval -> isIntervalValid(interval, effectiveStart, effectiveEnd))
                        .collect(Collectors.toList());

                for (ShiftIntervals interval : intervals) {
                    allOutputs.addAll(mapToShiftOutput(interval, effectiveStart, effectiveEnd));
                }
            }
        }

        if (allOutputs.isEmpty()) {
            return List.of(createNonProductiveOutput());
        }
        return allOutputs;
    }

    /*@Override
    public List<ShiftOutput> getShifts(ShiftInput input) {
        //LocalDate queryDate = input.startDateTime.toLocalDate();

        // Retain LocalDate for operations like getDayOfWeek()
        LocalDate localDateQuery = input.startDateTime.toLocalDate();
        // Convert LocalDate to Date
        Date queryDate = Date.from(input.startDateTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Process shifts if productive
        List<Shift> shifts = findShiftByHierarchy(input.site, input.resource, input.workcenter);

        if (shifts.isEmpty()) {
            throw new IllegalArgumentException("No active shifts found for the given criteria.");
        }
        // Initialize list to collect outputs
        List<ShiftOutput> shiftOutputs = new ArrayList<>();

        for (Shift shift : shifts) {
            String shiftRef = shift.getHandle();

            // Check Calendar Override
            Optional<CalendarOverrides> override = calendarOverrideRepository.findBySiteAndDate(input.site, queryDate);
            if (override.isPresent()) {
                continue; // Skip non-productive shifts
            }
            String dayOfWeek = localDateQuery.getDayOfWeek().toString();

// Query the Calendar Rules
            Optional<CalendarRules> rule = calendarRuleRepository.findByShiftRefAndProductionDayAndDay(shiftRef, "Nonproduction", dayOfWeek);
            // Check Calendar Rules
//           Optional<CalendarRules> rule = calendarRuleRepository.findByShiftRefAndProductionDay(shiftRef, "Nonproduction");
            if (rule.isPresent()) {
                continue; // Skip non-productive shifts
            }
            // Find valid intervals for this shift
            List<ShiftIntervals> intervals = findValidShiftIntervals(shiftRef, input.startDateTime, input.endDateTime);
            for (ShiftIntervals interval : intervals) {
                shiftOutputs.addAll(mapToShiftOutput(interval, input.startDateTime, input.endDateTime));
            }


        }

        // If no valid outputs are found, return non-productive output
        if (shiftOutputs.isEmpty()) {
            return List.of(createNonProductiveOutput());
        }

        return shiftOutputs;

    }*/

    private ShiftOutput createNonProductiveOutput() {
        ShiftOutput output = new ShiftOutput();
        output.nonProductiveDay = true;
        output.shiftId = null;
        output.shiftCreatedDatetime = null;
        output.breaks = null;
        output.plannedOperatingTime = 0;
        output.breaktime = 0;
        output.nonproduction = 0;
        output.totalShiftTime = 0;
        output.shiftRef = null;
        return output;
    }
    private List<Shift> findShiftByHierarchy(String site, String resource, String workcenter) {
        // Fetch shifts from Resource level
        List<Shift> resourceShifts = shiftPostgresRepository.findActiveShiftByResourceId(site, resource);

        // If Resource-level shifts are found, return them
        if (!resourceShifts.isEmpty()) {
            return resourceShifts;
        }

        // Fetch shifts from Workcenter level
        List<Shift> workcenterShifts = shiftPostgresRepository.findActiveShiftByWorkcenterId(site, workcenter);

        // If Workcenter-level shifts are found, return them
        if (!workcenterShifts.isEmpty()) {
            return workcenterShifts;
        }

        // Fetch shifts from General level
        return shiftPostgresRepository.findActiveShiftByGeneral(site);
    }
    private List<ShiftIntervals> findValidShiftIntervals(String shiftHandle, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return shiftIntervalRepository.findIntervalsByShiftRef(shiftHandle).stream()
                .filter(interval -> isIntervalValid(interval, startDateTime, endDateTime))
                .collect(Collectors.toList());
    }
   /*private boolean isIntervalValid(ShiftIntervals interval, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDate startDate = startDateTime.toLocalDate();
        LocalDate endDate = endDateTime.toLocalDate();
       LocalDateTime shiftStartDateTime = LocalDateTime.of(startDate, interval.getStartTime());
       LocalDateTime shiftEndDateTime = LocalDateTime.of(startDate, interval.getEndTime());

       // Adjust for shifts that cross midnight.
       if (shiftEndDateTime.isBefore(shiftStartDateTime)) {
           shiftEndDateTime = shiftEndDateTime.plusDays(1);
       }

        boolean isDateInRange = !shiftStartDateTime.toLocalDate().isAfter(endDate)
                && !shiftEndDateTime.toLocalDate().isBefore(startDate);

        // 2. Check time within shift boundaries
        boolean isTimeInRange = isTimeWithinShift(interval, startDateTime.toLocalTime(), endDateTime.toLocalTime());

        return isDateInRange && isTimeInRange;
    }*/
   private boolean isIntervalValid(ShiftIntervals interval, LocalDateTime inputStart, LocalDateTime inputEnd) {
       LocalDate day = inputStart.toLocalDate();

       // For a non-midnight shift (start <= end)
       if (!interval.getStartTime().isAfter(interval.getEndTime())) {
           LocalDateTime shiftStart = LocalDateTime.of(day, interval.getStartTime());
           LocalDateTime shiftEnd   = LocalDateTime.of(day, interval.getEndTime());
           return inputEnd.isAfter(shiftStart) && inputStart.isBefore(shiftEnd);
       } else {
           // Midnight crossing shift – split into two parts.
           // Early portion: from 00:00 to shiftEnd
           LocalDateTime earlyShiftStart = day.atStartOfDay();
           LocalDateTime earlyShiftEnd = LocalDateTime.of(day, interval.getEndTime());
           boolean validEarly = inputEnd.isAfter(earlyShiftStart) && inputStart.isBefore(earlyShiftEnd);

           // Late portion: from shiftStart to end-of-day
           LocalDateTime lateShiftStart = LocalDateTime.of(day, interval.getStartTime());
           LocalDateTime lateShiftEnd = LocalDateTime.of(day, LocalTime.MAX);
           boolean validLate = inputEnd.isAfter(lateShiftStart) && inputStart.isBefore(lateShiftEnd);

           return validEarly || validLate;
       }
   }
    private List<ShiftOutput> mapToShiftOutput(ShiftIntervals interval, LocalDateTime inputStart, LocalDateTime inputEnd) {
        List<ShiftOutput> outputs = new ArrayList<>();
        LocalDate day = inputStart.toLocalDate();

        if (!interval.getStartTime().isAfter(interval.getEndTime())) {
            // Non-midnight shift
            IntervalDateTime overlap = computeOverlap(
                    LocalDateTime.of(day, interval.getStartTime()),
                    LocalDateTime.of(day, interval.getEndTime()),
                    inputStart,
                    inputEnd
            );
            if (overlap != null) {
                outputs.add(createShiftOutput(interval, overlap.getStart(), overlap.getEnd(), inputStart, inputEnd));
            }
        } else {
            // Midnight shift – compute two parts.
            // Early part: 00:00 to shift end
            IntervalDateTime earlyOverlap = computeOverlap(
                    day.atStartOfDay(),
                    LocalDateTime.of(day, interval.getEndTime()),
                    inputStart,
                    inputEnd
            );
            if (earlyOverlap != null) {
                outputs.add(createShiftOutput(interval, earlyOverlap.getStart(), earlyOverlap.getEnd(), inputStart, inputEnd));
            }
            // Late part: shift start to end-of-day
            IntervalDateTime lateOverlap = computeOverlap(
                    LocalDateTime.of(day, interval.getStartTime()),
                    LocalDateTime.of(day, LocalTime.MAX),
                    inputStart,
                    inputEnd
            );
            if (lateOverlap != null) {
                outputs.add(createShiftOutput(interval, lateOverlap.getStart(), lateOverlap.getEnd(), inputStart, inputEnd));
            }
        }
        return outputs;
    }

    // Helper to compute the overlap between two intervals.
    private IntervalDateTime computeOverlap(LocalDateTime intervalStart, LocalDateTime intervalEnd,
                                            LocalDateTime inputStart, LocalDateTime inputEnd) {
        LocalDateTime overlapStart = inputStart.isAfter(intervalStart) ? inputStart : intervalStart;
        LocalDateTime overlapEnd = inputEnd.isBefore(intervalEnd) ? inputEnd : intervalEnd;
        if (!overlapEnd.isAfter(overlapStart)) {
            return null;
        }
        return new IntervalDateTime(overlapStart, overlapEnd);
    }

   /* private List<ShiftOutput> mapToShiftOutput(ShiftIntervals interval, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<ShiftOutput> outputs = new ArrayList<>();
// First interval calculation
        IntervalDateTime firstInterval = getIntervalDateTimes(interval, startDateTime, endDateTime);
        if (firstInterval != null) {
            outputs.add(createShiftOutput(interval, firstInterval.getStart(), firstInterval.getEnd(), startDateTime, endDateTime));
        }

        // Handle midnight crossover for the next day
        if (interval.getStartTime().isAfter(interval.getEndTime())) { // Midnight shift case

            if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
                LocalDateTime nextDayStart = startDateTime.toLocalDate().plusDays(1).atTime(interval.getStartTime());
                LocalDateTime nextDayEnd = startDateTime.toLocalDate().plusDays(1).atTime(interval.getEndTime());

                IntervalDateTime secondInterval = getIntervalDateTimes(interval, nextDayStart, endDateTime);
                if (secondInterval != null) {
                    outputs.add(createShiftOutput(interval, secondInterval.getStart(), secondInterval.getEnd(), startDateTime, endDateTime));
                }
            }
        }
        return outputs;
    }*/
    private ShiftOutput createShiftOutput(ShiftIntervals interval, LocalDateTime intervalStart, LocalDateTime intervalEnd, LocalDateTime inputStart, LocalDateTime inputEnd) {
        List<BreakDetails> breaks = findBreaks(interval.getHandle(), intervalStart.toLocalTime(), intervalEnd.toLocalTime());

        int totalBreakTime = breaks.stream().mapToInt(b -> b.meanTime).sum();
        int plannedOperatingTime = calculateOperatingTime(interval, intervalStart, intervalEnd);

        ShiftOutput output = new ShiftOutput();
        output.shiftId = interval.getShiftRef();
        output.shiftCreatedDatetime = interval.getCreatedDateTime();
        output.intervalStartDatetime = intervalStart;
        output.intervalEndDatetime = intervalEnd;
        output.shiftStartTime = interval.getStartTime();
        output.shiftEndTime = interval.getEndTime();
        output.breaks = breaks;
        output.plannedOperatingTime = plannedOperatingTime - totalBreakTime;
        output.breaktime = totalBreakTime;
        output.totalShiftTime = plannedOperatingTime;
        output.nonproduction = 0;
        output.shiftRef = interval.getShiftRef();
        output.nonProductiveDay = false;

        return output;
    }
   private boolean isTimeWithinShift(ShiftIntervals interval, LocalTime inputStartTime, LocalTime inputEndTime) {
        LocalTime shiftStart = interval.getStartTime();
        LocalTime shiftEnd = interval.getEndTime();

        // Handle shifts that cross midnight
        if (shiftEnd.isBefore(shiftStart)) {
            // Midnight crossover scenario
            boolean inputOverlapsFirstPart = !inputEndTime.isBefore(shiftStart); // Input overlaps before midnight
            boolean inputOverlapsSecondPart = !inputStartTime.isAfter(shiftEnd); // Input overlaps after midnight
            return inputOverlapsFirstPart || inputOverlapsSecondPart;
        } else {
            // Normal scenario: Check for overlap
            return !inputEndTime.isBefore(shiftStart) && !inputStartTime.isAfter(shiftEnd);
        }
    }
    private List<BreakDetails> findBreaks(String intervalHandle, LocalTime startTime, LocalTime endTime) {
        return breakRepository.findBreaksByIntervalRef(intervalHandle).stream()
                .filter(breakRecord -> isBreakInRange(breakRecord, startTime, endTime))
                .map(breakRecord -> mapToBreakDetails(breakRecord, startTime, endTime))
                //  .map(this::mapToBreakDetails)
                .collect(Collectors.toList());
    }
    private boolean isBreakInRange(Break breakRecord, LocalTime startTime, LocalTime endTime) {
        // Adjust the break start and end times to fit within the input range
        LocalTime adjustedBreakStart = breakRecord.getBreakTimeStart().isBefore(startTime) ? startTime : breakRecord.getBreakTimeStart();
        LocalTime adjustedBreakEnd = breakRecord.getBreakTimeEnd().isAfter(endTime) ? endTime : breakRecord.getBreakTimeEnd();

        // Check if the adjusted break still falls within the range
        return adjustedBreakEnd.isAfter(adjustedBreakStart);
    }

    private BreakDetails mapToBreakDetails(Break breakRecord, LocalTime startTime, LocalTime endTime) {
        BreakDetails details = new BreakDetails();

        // Adjust the break start and end times to fit within the input range
        details.breakStartTime = breakRecord.getBreakTimeStart().isBefore(startTime) ? startTime : breakRecord.getBreakTimeStart();
        details.breakEndTime = breakRecord.getBreakTimeEnd().isAfter(endTime) ? endTime : breakRecord.getBreakTimeEnd();

        // Recalculate meanTime (duration) based on the adjusted start and end times
        details.meanTime = (int) Duration.between(details.breakStartTime, details.breakEndTime).getSeconds();

        return details;
    }
    private int calculateOperatingTime(ShiftIntervals interval, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalTime shiftStart = interval.getStartTime();
        LocalTime shiftEnd = interval.getEndTime();

        // Convert input times to LocalTime
        LocalTime inputStart = startDateTime.toLocalTime();
        LocalTime inputEnd = endDateTime.toLocalTime();

        // Convert to total seconds since midnight for comparison
        int shiftStartSeconds = shiftStart.toSecondOfDay();
        int shiftEndSeconds = shiftEnd.toSecondOfDay();
        int inputStartSeconds = inputStart.toSecondOfDay();
        int inputEndSeconds = inputEnd.toSecondOfDay();

        // Handle midnight crossover for shift
        if (shiftEndSeconds < shiftStartSeconds) { // Midnight shift case
            shiftEndSeconds += 24 * 3600; // Add 24 hours to shift end
            if (inputStartSeconds < shiftStartSeconds) {
                inputStartSeconds += 24 * 3600; // Adjust input start if before midnight
            }
            if (inputEndSeconds < shiftStartSeconds) {
                inputEndSeconds += 24 * 3600; // Adjust input end if before midnight
            }
        }

        // Clip the input times to the shift boundaries
        int actualStartSeconds = Math.max(shiftStartSeconds, inputStartSeconds);
        int actualEndSeconds = Math.min(shiftEndSeconds, inputEndSeconds);

        // Ensure valid duration and return the result
        return actualEndSeconds > actualStartSeconds ? actualEndSeconds - actualStartSeconds : 0;
    }
    /**
     * Computes the overlap between the input interval and the shift interval,
     * “flattening” a midnight–crossing shift onto the same calendar day.
     * For a non–midnight shift, this returns one interval.
     * For a midnight shift (e.g. 10pm–6am), this returns up to two intervals:
     * one for the late portion (e.g. 10:00–23:59:59) and one for the early portion (00:00–6:00)
     * – both reported on the same day.
     */
    private List<IntervalDateTime> getIntervalDateTimes(ShiftIntervals interval, LocalDateTime inputStart, LocalDateTime inputEnd) {
        List<IntervalDateTime> intervals = new ArrayList<>();
        // Use the date from the input's start
        LocalDate day = inputStart.toLocalDate();

        // For a non–midnight shift (startTime <= endTime):
        if (!interval.getStartTime().isAfter(interval.getEndTime())) {
            LocalDateTime shiftStart = LocalDateTime.of(day, interval.getStartTime());
            LocalDateTime shiftEnd   = LocalDateTime.of(day, interval.getEndTime());
            LocalDateTime effectiveStart = inputStart.isAfter(shiftStart) ? inputStart : shiftStart;
            LocalDateTime effectiveEnd   = inputEnd.isBefore(shiftEnd) ? inputEnd : shiftEnd;
            if (effectiveEnd.isAfter(effectiveStart)) {
                intervals.add(new IntervalDateTime(effectiveStart, effectiveEnd));
            }
        } else {
            // For a midnight–crossing shift, split into two parts on the same day.
            // Late portion: from shiftStart to end-of-day (23:59:59)
            LocalDateTime lateShiftStart = LocalDateTime.of(day, interval.getStartTime());
            LocalDateTime lateShiftEnd = LocalDateTime.of(day, LocalTime.MAX); // e.g. 23:59:59.999
            LocalDateTime effectiveLateStart = inputStart.isAfter(lateShiftStart) ? inputStart : lateShiftStart;
            LocalDateTime effectiveLateEnd = inputEnd.isBefore(lateShiftEnd) ? inputEnd : lateShiftEnd;
            if (effectiveLateEnd.isAfter(effectiveLateStart)) {
                intervals.add(new IntervalDateTime(effectiveLateStart, effectiveLateEnd));
            }

            // Early portion: from start-of-day (00:00) to shiftEnd
            LocalDateTime earlyShiftStart = LocalDateTime.of(day, LocalTime.MIDNIGHT);
            LocalDateTime earlyShiftEnd = LocalDateTime.of(day, interval.getEndTime());
            LocalDateTime effectiveEarlyStart = inputStart.isAfter(earlyShiftStart) ? inputStart : earlyShiftStart;
            LocalDateTime effectiveEarlyEnd = inputEnd.isBefore(earlyShiftEnd) ? inputEnd : earlyShiftEnd;
            if (effectiveEarlyEnd.isAfter(effectiveEarlyStart)) {
                intervals.add(new IntervalDateTime(effectiveEarlyStart, effectiveEarlyEnd));
            }
        }

        return intervals;
    }

    /*private IntervalDateTime getIntervalDateTimes(ShiftIntervals interval, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalTime shiftStart = interval.getStartTime();
        LocalTime shiftEnd = interval.getEndTime();

        // Convert input times to LocalTime
        LocalTime inputStart = startDateTime.toLocalTime();
        LocalTime inputEnd = endDateTime.toLocalTime();

        // Convert to total seconds since midnight for comparison
        int shiftStartSeconds = shiftStart.toSecondOfDay();
        int shiftEndSeconds = shiftEnd.toSecondOfDay();
        int inputStartSeconds = inputStart.toSecondOfDay();
        int inputEndSeconds = inputEnd.toSecondOfDay();

        boolean isMidnightShift = shiftEndSeconds < shiftStartSeconds;

        // Adjust for midnight crossover
        if (isMidnightShift) {
            shiftEndSeconds += 24 * 3600; // Add 24 hours to shift end
            if (inputStartSeconds < shiftStartSeconds) {
               // inputStartSeconds += 24 * 3600; // Adjust input start if before midnight
                inputStartSeconds = shiftStartSeconds;
            }
            if (inputEndSeconds < shiftStartSeconds) {
                inputEndSeconds += 24 * 3600; // Adjust input end if before midnight
            }
        }

        // Clip the input times to the shift boundaries
        int actualStartSeconds = Math.max(shiftStartSeconds, inputStartSeconds);

        // Adjust end time to the nearest time to start within boundaries
        int actualEndSeconds;
        if (Math.abs(inputEndSeconds - actualStartSeconds) < Math.abs(shiftEndSeconds - actualStartSeconds)) {
            actualEndSeconds = inputEndSeconds;
        } else {
            actualEndSeconds = shiftEndSeconds;
        }

        // Convert the adjusted seconds back to LocalDateTime
        LocalDateTime intervalStartDateTime = startDateTime.toLocalDate().atStartOfDay().plusSeconds(actualStartSeconds);
        LocalDateTime intervalEndDateTime = startDateTime.toLocalDate().atStartOfDay().plusSeconds(actualEndSeconds);

        // Adjust if interval crosses the current day
        if (actualStartSeconds >= 24 * 3600) {
            intervalStartDateTime = intervalStartDateTime.minusDays(1); // Adjust start time to the previous day
        }
        if (actualEndSeconds >= 24 * 3600) {
            intervalEndDateTime = intervalEndDateTime.minusDays(1); // Adjust end time to the previous day
        }

        // Return null if the intervalStartDatetime is after intervalEndDatetime
        if (!intervalEndDateTime.isAfter(intervalStartDateTime)) {
            return null; // Invalid interval
        }

        return new IntervalDateTime(intervalStartDateTime, intervalEndDateTime);
    }*/

    @Override
    public ShiftDataResponse calculateShiftData(String shiftRef, String site, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        ShiftDataResponse shiftDataResponse = new ShiftDataResponse();

        // Step 1: Get the shifts based on the parameters (shiftRef, site, startDateTime, endDateTime)
        List<Shift> shifts = findShiftWithIntervalsAndBreaks(shiftRef, site, startDateTime, endDateTime);

        long totalRuntimeInSeconds = 0;         // Total runtime in seconds
        long totalBreakDurationInSeconds = 0;   // Total break duration in seconds
        long plannedProductionTimeInSeconds = 0; // Planned production time in seconds

        // Loop through shifts and intervals
        for (Shift shift : shifts) {
            for (ShiftIntervals interval : shift.getShiftIntervals()) {
                // Convert interval start and end times to LocalDateTime using startDateTime (e.g., "2024-12-26T")
                LocalDateTime intervalStart = LocalDateTime.of(startDateTime.toLocalDate(), interval.getStartTime());
                LocalDateTime intervalEnd = LocalDateTime.of(endDateTime.toLocalDate(), interval.getEndTime());

                // Check if the interval overlaps with the provided date range
                if (intervalEnd.isBefore(startDateTime) || intervalStart.isAfter(endDateTime)) {
                    continue; // Skip if the interval doesn't overlap
                }

                // Adjust interval start and end times to fit within the provided date range
                if (intervalStart.isBefore(startDateTime)) {
                    intervalStart = startDateTime;
                }
                if (intervalEnd.isAfter(endDateTime)) {
                    intervalEnd = endDateTime;
                }

                // Calculate the interval duration in seconds
                Duration intervalDuration = Duration.between(startDateTime, endDateTime);
                long intervalRuntimeInSeconds = intervalDuration.getSeconds();

                // Calculate break times within the interval
                long breakTimeInSeconds = 0;
                for (Break breakObj : interval.getBreakList()) {
                    LocalDateTime breakStartDateTime = LocalDateTime.of(startDateTime.toLocalDate(), breakObj.getBreakTimeStart());
                    LocalDateTime breakEndDateTime = LocalDateTime.of(endDateTime.toLocalDate(), breakObj.getBreakTimeEnd());

                    // Check if the break overlaps with the interval
                    if (breakEndDateTime.isBefore(intervalStart) || breakStartDateTime.isAfter(intervalEnd)) {
                        continue; // Skip if the break doesn't overlap
                    }

                    // Adjust break times to fit within the interval
                    if (breakStartDateTime.isBefore(intervalStart)) {
                        breakStartDateTime = intervalStart;
                    }
                    if (breakEndDateTime.isAfter(intervalEnd)) {
                        breakEndDateTime = intervalEnd;
                    }

                    // Calculate break duration within the interval
                    Duration breakDuration = Duration.between(breakStartDateTime, breakEndDateTime);
                    breakTimeInSeconds += breakDuration.getSeconds();
                }

                // Add runtime and break durations to totals
                totalRuntimeInSeconds += (intervalRuntimeInSeconds - breakTimeInSeconds);
                totalBreakDurationInSeconds += breakTimeInSeconds;

                // Calculate planned production time
                plannedProductionTimeInSeconds += intervalRuntimeInSeconds;
            }
        }

        // Set the results in the ShiftDataResponse
        shiftDataResponse.setShiftRef(shiftRef);
        shiftDataResponse.setSite(site);

        // Convert seconds to minutes (or keep as seconds, depending on preference)
        shiftDataResponse.setBreakDuration(totalBreakDurationInSeconds);
        shiftDataResponse.setTotalRuntime(totalRuntimeInSeconds);
        shiftDataResponse.setPlannedOperatingTime(plannedProductionTimeInSeconds);

        // Return the response object
        return shiftDataResponse;
    }

    public ShiftDetailsDTO getCurrentShiftAndBreak(String site) {
        List<Object[]> queryResult = shiftPostgresRepository.findCurrentShiftAndBreakData(site);

        // Check if the queryResult is empty or null
        if (queryResult == null || queryResult.isEmpty()) {
            System.out.println("No data found for the given site.");
            return new ShiftDetailsDTO(null, null, LocalTime.MIDNIGHT, null); // Return a default DTO
        }

        // Debugging: Print all retrieved rows
        for (Object[] row : queryResult) {
            System.out.println("Shift ID: " + row[0] + ", Break Time: " + row[3]);
        }

        System.out.println("Result length: " + queryResult.size()); // Safe .size() check

        Object[] result = queryResult.get(0);  // Access only after checking it’s not empty
        System.out.println("Row length: " + result.length);

        // Extract and convert fields safely
        String shiftId = (result[0] != null) ? result[0].toString() : null;
        LocalDateTime shiftCreatedDatetime = (result[1] != null) ? convertToLocalDateTime(result[1]) : null;
        String handle = (result[2] != null) ? result[2].toString() : null;
        LocalTime breakStartTime = (result[3] != null) ? convertToLocalTime(result[3]) : LocalTime.MIDNIGHT;

        return new ShiftDetailsDTO(shiftId, shiftCreatedDatetime, breakStartTime, handle);
    }


    private LocalDateTime convertToLocalDateTime(Object obj) {
        if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime();
        }
        throw new IllegalArgumentException("Invalid type for LocalDateTime conversion: " + obj.getClass());
    }

    // Helper method to convert Object to LocalTime
    private LocalTime convertToLocalTime(Object obj) {
        if (obj instanceof java.sql.Time) {
            return ((java.sql.Time) obj).toLocalTime();
        } else if (obj instanceof String) {
            return LocalTime.parse((String) obj);
        }
        throw new IllegalArgumentException("Invalid type for LocalTime conversion: " + obj.getClass());
    }
    @Override
    public Map<String, Long> getPlannedProductionTimes(List<String> shiftHandles) {
        List<Object[]> results = shiftPostgresRepository.getPlannedProductionTimes(shiftHandles);
        Map<String, Long> plannedProductionTimes = new HashMap<>();
        for (Object[] row : results) {
            // row[0] is the shift handle, row[1] is the calculated planned production time.
            String handle = (String) row[0];
            Long plannedProductionTime = ((Number) row[1]).longValue();
            plannedProductionTimes.put(handle, plannedProductionTime);
        }
        return plannedProductionTimes;
    }
}

/*
package com.rits.shiftservice.service;

import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.resourceservice.service.ResourceService;
import com.rits.shiftservice.dto.*;
import com.rits.shiftservice.exception.ShiftException;
import com.rits.shiftservice.model.*;
import com.rits.shiftservice.repository.ShiftRepository;
import com.rits.workcenterservice.service.WorkCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;
    private final ResourceService resourceService;
    private final WorkCenterService workCenterService;
    private final MessageSource localMessageSource;
    private final WebClient.Builder webClientBuilder;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }



    @Override
    public ShiftMessageModel createShift(ShiftRequest shiftRequest) throws Exception {
        if (isShiftExist(shiftRequest.getSite(), shiftRequest.getShiftName())) {
            throw new ShiftException(5006, shiftRequest.getShiftName());
        } else {
            try {
                getValidated(shiftRequest);
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw e;
            }
            Optional<Shift> existingShift = Optional.ofNullable(shiftRepository.findBySiteAndShiftNameAndActive(shiftRequest.getSite(), shiftRequest.getShiftName(), 1));

          //  Shift shift = createShiftBuilder(shiftRequest);
            Shift shift;
            if (existingShift.isPresent()) {
                // Update the existing shift's calendar rules
                shift = updateShiftBuilder(shiftRequest,existingShift.get());
            } else {
                // Create a new shift
                shift = createShiftBuilder(shiftRequest);
            }
            String createdMessage = getFormattedMessage(41, shiftRequest.getShiftName());

            return ShiftMessageModel.builder().response(shiftRepository.save(shift)).message_details(new MessageDetails(createdMessage, "S")).build();
        }
    }

    private Shift createShiftBuilder(ShiftRequest shiftRequest) {
        return Shift.builder()
                .site(shiftRequest.getSite())
                .shiftName(shiftRequest.getShiftName())
                .shiftType(shiftRequest.getShiftType())
              // .handle("ShiftBo:" + shiftRequest.getSite() + "," + shiftRequest.getShiftType() + "," + shiftRequest.getShiftName())
                .description(shiftRequest.getDescription())
                .workCenter(shiftRequest.getWorkCenter())
                .resource(shiftRequest.getResource())
                .shiftIntervals(shiftRequest.getShiftIntervals())
//                .calendarRules(shiftRequest.getCalendarRules())
//                .calendarOverrides(shiftRequest.getCalendarOverrides())
                .calendarRules(shiftRequest.getCalendarRules() != null ? shiftRequest.getCalendarRules() : new ArrayList<>())
                .calendarOverrides(shiftRequest.getCalendarOverrides() != null ? shiftRequest.getCalendarOverrides() : new ArrayList<>())

                .active(1)
                .customDataList(shiftRequest.getCustomDataList())
                .createdBy(shiftRequest.getUserId())
                .createdDateTime(LocalDateTime.now())
                .build();
    }

    @Override
    public ShiftMessageModel updateShift(ShiftRequest shiftRequest) throws Exception {
        if (isShiftExist(shiftRequest.getSite(), shiftRequest.getShiftName())) {
            try {
                getValidated(shiftRequest);
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw e;
            }
            Shift existingShift = shiftRepository.findBySiteAndShiftNameAndActive(shiftRequest.getSite(), shiftRequest.getShiftName(), 1);
            Shift shift = updateShiftBuilder(shiftRequest, existingShift);
            String updatedMessage = getFormattedMessage(42, shiftRequest.getShiftName());

            return ShiftMessageModel.builder().response(shiftRepository.save(shift)).message_details(new MessageDetails(updatedMessage, "S")).build();
        } else {
            throw new ShiftException(5000, shiftRequest.getShiftName());
        }


    }

    private Shift updateShiftBuilder(ShiftRequest shiftRequest, Shift existingShift) {
        // Check existing rules
        Map<String, CalendarRules> existingRulesMap = existingShift.getCalendarRules() != null ?
                existingShift.getCalendarRules().stream().collect(Collectors.toMap(CalendarRules::getDay, rule -> rule)) : new HashMap<>();

        // Update rules
        if (shiftRequest.getCalendarRules() != null) {
            for (CalendarRules updatedRule : shiftRequest.getCalendarRules()) {
                existingRulesMap.put(updatedRule.getDay(), updatedRule);
            }
        }

        // Check existing overrides
        Map<String, CalendarOverrides> existingOverridesMap = existingShift.getCalendarOverrides() != null ?
                existingShift.getCalendarOverrides().stream().collect(Collectors.toMap(CalendarOverrides::getDate, override -> override)) : new HashMap<>();

        // Update overrides
        if (shiftRequest.getCalendarOverrides() != null) {
            for (CalendarOverrides updatedOverride : shiftRequest.getCalendarOverrides()) {
                existingOverridesMap.put(updatedOverride.getDate(), updatedOverride);
            }
        }

        // Convert back to lists
        List<CalendarRules> updatedCalendarRulesList = new ArrayList<>(existingRulesMap.values());
        List<CalendarOverrides> updatedCalendarOverridesList = new ArrayList<>(existingOverridesMap.values());

        return Shift.builder()
                .site(existingShift.getSite())
                .shiftName(existingShift.getShiftName())
                .shiftType(shiftRequest.getShiftType())
                //.handle(existingShift.getHandle())
                .description(shiftRequest.getDescription())
                .workCenter(shiftRequest.getWorkCenter())
                .resource(shiftRequest.getResource())
                .shiftIntervals(shiftRequest.getShiftIntervals())
                .calendarRules(updatedCalendarRulesList)
                .calendarOverrides(updatedCalendarOverridesList)
                .active(1)
                .modifiedBy(shiftRequest.getUserId())
                .modifiedDateTime(LocalDateTime.now())
                .createdDateTime(existingShift.getCreatedDateTime())
                .createdBy(existingShift.getCreatedBy())
                .customDataList(shiftRequest.getCustomDataList())
                .build();
    }


    private void getValidated(ShiftRequest shiftRequest) throws Exception {
        isUserIdPresents(shiftRequest);
        setDescription(shiftRequest);
        isShiftIntervalValid(shiftRequest);
        if (shiftRequest.getShiftType() != null && !shiftRequest.getShiftType().isEmpty()) {
            if (shiftRequest.getShiftType().equalsIgnoreCase("General")) {
                if (!isShiftTimeValid(shiftRequest)) {

                    throw new ShiftException(5004);
                }

            } else if (shiftRequest.getShiftType().equalsIgnoreCase("Resource")) {
                if (shiftRequest.getResource() == null || shiftRequest.getResource().isEmpty()) {
                    throw new ShiftException(2009);
                }
                isResourceValid(shiftRequest);

                if (!isResourceShiftTimeValid(shiftRequest)) {
                    throw new ShiftException(5005, shiftRequest.getResource());

                }

            } else if (shiftRequest.getShiftType().equalsIgnoreCase("WorkCenter")) {
                if (shiftRequest.getWorkCenter() == null || shiftRequest.getWorkCenter().isEmpty()) {
                    throw new ShiftException(2015);
                }
                isWorkCenterValid(shiftRequest);

                if (!isWorkCenterShiftTimeValid(shiftRequest)) {
                    throw new ShiftException(5005, shiftRequest.getResource());

                }

            } else {
                throw new ShiftException(5002);
            }

        } else {
            throw new ShiftException(5001);
        }
        setActualTime(shiftRequest);
        setMeanTime(shiftRequest);
        if (shiftRequest.getResource() != null && !shiftRequest.getResource().isEmpty()) {
            isResourceValid(shiftRequest);
        }
        if (shiftRequest.getWorkCenter() != null && !shiftRequest.getWorkCenter().isEmpty()) {
            isWorkCenterValid(shiftRequest);
        }
    }

    private static void isShiftIntervalValid(ShiftRequest shiftRequest) {
        if (shiftRequest.getShiftIntervals() != null && shiftRequest.getShiftIntervals().size() > 1) {
            List<ShiftIntervals> intervals = shiftRequest.getShiftIntervals();

            for (int i = 0; i < intervals.size() - 1; i++) {
                for (int j = i + 1; j < intervals.size(); j++) {
                    ShiftIntervals interval1 = intervals.get(i);
                    ShiftIntervals interval2 = intervals.get(j);

                    if (doIntervalsOverlap(interval1, interval2) && doValidIntervalsOverlap(interval1, interval2)) {
                        throw new ShiftException(5004);
                    }
                }
            }
        }
    }

    private static boolean doIntervalsOverlap(ShiftIntervals interval1, ShiftIntervals interval2) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        LocalTime start1 = LocalTime.parse(interval1.getStartTime(), formatter);
        LocalTime end1 = LocalTime.parse(interval1.getEndTime(), formatter);

        LocalTime start2 = LocalTime.parse(interval2.getStartTime(), formatter);
        LocalTime end2 = LocalTime.parse(interval2.getEndTime(), formatter);

        // Check for interval overlap
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }

    private static boolean doValidIntervalsOverlap(ShiftIntervals interval1, ShiftIntervals interval2) {


        LocalDateTime validStart1 = interval1.getValidFrom();
        LocalDateTime validEnd1 =interval1.getValidEnd();

        LocalDateTime validStart2 = interval2.getValidFrom();
        LocalDateTime validEnd2 = interval2.getValidEnd();

        // Check for valid interval overlap
        return !(validEnd1.isBefore(validStart2) || validStart1.isAfter(validEnd2));
    }
    private void isWorkCenterValid(ShiftRequest shiftRequest) throws Exception {
        boolean flag = workCenterService.isWorkCenterExist(shiftRequest.getWorkCenter(), shiftRequest.getSite());
        if (!flag) {
            throw new ShiftException(600, shiftRequest.getWorkCenter());
        }
    }

    private void isResourceValid(ShiftRequest shiftRequest) throws Exception {
        boolean flag = resourceService.isResourceExist(shiftRequest.getResource(), shiftRequest.getSite());
        if (!flag) {
            throw new ShiftException(9301, shiftRequest.getResource());
        }

    }
    private void setMeanTime(ShiftRequest shiftRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        if (shiftRequest.getShiftIntervals() != null && !shiftRequest.getShiftIntervals().isEmpty()) {


            for (ShiftIntervals shiftIntervals : shiftRequest.getShiftIntervals()) {

                LocalDateTime startTime = parseToLocalDateTime(shiftIntervals.getStartTime(), formatter);
                LocalDateTime endTime = parseToLocalDateTime(shiftIntervals.getEndTime(), formatter);

                if (endTime.isBefore(startTime)) {
                    endTime = endTime.plusDays(1);  // Add 1 day to consider the next day
                }

                int meantime = (int) Duration.between(startTime, endTime).toSeconds();
                shiftIntervals.setShiftMeanTime(meantime);  // Ensure meantime is non-negative
            }
        }
    }


    private void setActualTime(ShiftRequest shiftRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        if (shiftRequest.getShiftIntervals() != null && !shiftRequest.getShiftIntervals().isEmpty()) {
            for (ShiftIntervals shiftIntervals : shiftRequest.getShiftIntervals()) {
                int breakTime = 0; // Reset breakTime for each shift interval
                if (shiftIntervals.getBreakList() != null && !shiftIntervals.getBreakList().isEmpty()) {
                    for (Break breakObj : shiftIntervals.getBreakList()) {
                        LocalDateTime startBreakTime = parseToLocalDateTime(breakObj.getBreakTimeStart(), formatter);
                        LocalDateTime endBreakTime = parseToLocalDateTime(breakObj.getBreakTimeEnd(), formatter);

                        if (endBreakTime.isBefore(startBreakTime)) {
                            endBreakTime = endBreakTime.plusDays(1);
                        }

                        long breakMeanTime = Duration.between(startBreakTime, endBreakTime).toSeconds();
                        breakObj.setMeanTime(String.valueOf(breakMeanTime)); // Assuming setMeanTime expects a String
                        breakTime += breakMeanTime;
                    }
                }

                LocalDateTime startTime = parseToLocalDateTime(shiftIntervals.getStartTime(), formatter);
                LocalDateTime endTime = parseToLocalDateTime(shiftIntervals.getEndTime(), formatter);

                if (endTime.isBefore(startTime)) {
                    endTime = endTime.plusDays(1);
                }

                int meantime = (int) Duration.between(startTime, endTime).toSeconds();
                int actualTime = meantime - breakTime;
                shiftIntervals.setActualTime(actualTime);
            }
        }
    }

    private LocalDateTime parseToLocalDateTime(String timeString, DateTimeFormatter formatter) {
        return LocalTime.parse(timeString, formatter).atDate(LocalDate.now());
    }




    private boolean isWorkCenterShiftTimeValid(ShiftRequest shiftRequest) {
        boolean flag = false;

        List<Shift> shifts = shiftRepository.findBySiteAndActiveAndShiftTypeAndWorkCenter(shiftRequest.getSite(), 1, shiftRequest.getShiftType(), shiftRequest.getWorkCenter());
        shifts.removeIf(existingShift -> existingShift.getShiftName().equals(shiftRequest.getShiftName()));

        if (shifts != null && !shifts.isEmpty()) {

            for (Shift shift : shifts) {
                if (doShiftsOverlap(shiftRequest, shift)) {
                    if (doDateTimeOverlap(shiftRequest, shift)) {
                        throw new ShiftException(5003);
                    } else {
                        flag = true;
                    }
                } else {
                    flag = true;
                }
            }
        } else {
            flag = true;
        }
        return flag;
    }

    private boolean isResourceShiftTimeValid(ShiftRequest shiftRequest) {
        List<Shift> shifts = shiftRepository.findBySiteAndActiveAndShiftTypeAndResource(shiftRequest.getSite(), 1, shiftRequest.getShiftType(), shiftRequest.getResource());
        shifts.removeIf(existingShift -> existingShift.getShiftName().equals(shiftRequest.getShiftName()));
        if (shifts != null && !shifts.isEmpty()) {
            for (Shift shift : shifts) {
                if (doShiftsOverlap(shiftRequest, shift) && doDateTimeOverlap(shiftRequest, shift)) {
                    throw new ShiftException(5003);
                }
            }
            return true;
        } else {
            return true;
        }
    }

    private void setDescription(ShiftRequest shiftRequest) {
        if (shiftRequest.getDescription() == null || shiftRequest.getDescription().isEmpty()) {
            shiftRequest.setDescription(shiftRequest.getShiftName());
        }
    }

    private boolean isShiftTimeValid(ShiftRequest shiftRequest) {
        boolean flag = false;
        List<Shift> shifts = shiftRepository.findBySiteAndActiveAndShiftType(shiftRequest.getSite(), 1, shiftRequest.getShiftType());
        shifts.removeIf(existingShift -> existingShift.getShiftName().equals(shiftRequest.getShiftName()));
        if (shifts != null && !shifts.isEmpty()) {
            for (Shift shift : shifts) {
                if (doShiftsOverlap(shiftRequest, shift)) {
                    if (doDateTimeOverlap(shiftRequest, shift)) {
                        throw new ShiftException(5003);
                    } else {
                        flag = true;
                    }
                } else {
                    flag = true;
                }
            }
        } else {
            flag = true;
        }
        return flag;
    }

    private boolean doDateTimeOverlap(ShiftRequest newShiftRequest, Shift existingShift) {
        if (newShiftRequest.getShiftIntervals() != null && !newShiftRequest.getShiftIntervals().isEmpty()
                && existingShift.getShiftIntervals() != null && !existingShift.getShiftIntervals().isEmpty()) {

            for (ShiftIntervals newShiftIntervals : newShiftRequest.getShiftIntervals()) {
                LocalDateTime newShiftValidFrom = newShiftIntervals.getValidFrom();
                LocalDateTime newShiftValidEnd = newShiftIntervals.getValidEnd();

                for (ShiftIntervals existingShiftIntervals : existingShift.getShiftIntervals()) {
                    LocalDateTime existingShiftValidFrom = existingShiftIntervals.getValidFrom();
                    LocalDateTime existingShiftValidEnd = existingShiftIntervals.getValidEnd();

                    // Check for overlap in each pair of intervals
                    if (!(newShiftValidEnd.isBefore(existingShiftValidFrom) || newShiftValidFrom.isAfter(existingShiftValidEnd))) {
                        return true;  // Overlapping intervals found, return true
                    }
                }
            }
        }

        // No overlapping intervals found
        return false;
    }

    private boolean doShiftsOverlap(ShiftRequest newShiftRequest, Shift existingShift) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        List<ShiftIntervals> newIntervals = newShiftRequest.getShiftIntervals();
        List<ShiftIntervals> existingIntervals = existingShift.getShiftIntervals();

        for (ShiftIntervals newInterval : newIntervals) {
            LocalDateTime newShiftStartTime =  parseToLocalDateTime(newInterval.getStartTime(), formatter);
            LocalDateTime newShiftEndTime = parseToLocalDateTime(newInterval.getEndTime(), formatter);

            // Adjust end times if they are before start times (crossing midnight)
            if (newShiftEndTime.isBefore(newShiftStartTime)) {
                newShiftEndTime = newShiftEndTime.plusDays(1);
            }

            for (ShiftIntervals existingInterval : existingIntervals) {
                LocalDateTime existingShiftStartTime = parseToLocalDateTime(existingInterval.getStartTime(), formatter);
                LocalDateTime existingShiftEndTime =parseToLocalDateTime(existingInterval.getEndTime(), formatter);

                // Adjust end times if they are before start times (crossing midnight)
                if (existingShiftEndTime.isBefore(existingShiftStartTime)) {
                    existingShiftEndTime = existingShiftEndTime.plusDays(1);
                }

                // Compare overall time ranges for the intervals
                if (!(newShiftEndTime.isBefore(existingShiftStartTime) || newShiftStartTime.isAfter(existingShiftEndTime))) {
                    return true;  // Overlapping intervals found, return true
                }
            }
        }

        // No overlapping intervals found
        return false;
    }



    private void isUserIdPresents(ShiftRequest shiftRequest) {
        if (shiftRequest.getUserId() == null || shiftRequest.getUserId().isEmpty()) {
            throw new ShiftException(108);
        }
    }

    @Override
    public ShiftMessageModel deleteShift(String site, String shiftName, String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new ShiftException(108);
        }
        if (isShiftExist(site, shiftName)) {
            Shift existingShift = shiftRepository.findBySiteAndShiftNameAndActive(site, shiftName,  1);
            existingShift.setActive(0);
            existingShift.setModifiedBy(userId);
            existingShift.setModifiedDateTime(LocalDateTime.now());
            shiftRepository.save(existingShift);
            String deletedMessage = getFormattedMessage(43, shiftName);

            return ShiftMessageModel.builder().message_details(new MessageDetails(deletedMessage, "S")).build();
        } else {
            throw new ShiftException(5000, shiftName);
        }
    }

    @Override
    public Shift retrieveShift(String site, String shiftName) {
        if (isShiftExist(site, shiftName)) {
            return shiftRepository.findBySiteAndShiftNameAndActive(site, shiftName,  1);
        } else {
            throw new ShiftException(5000, shiftName);
        }

    }

    private boolean isShiftExist(String site, String shiftName) {
        return shiftRepository.existsBySiteAndShiftNameAndActive(site, shiftName, 1);
    }


    @Override
    public ShiftResponseList retrieveTop50(String site) {
        List<ShiftResponse> shiftResponseList = shiftRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return ShiftResponseList.builder().shiftResponseList(shiftResponseList).build();
    }

    @Override
    public ShiftResponseList retrieveAll(String site, String shiftName) {
        if (shiftName != null && !shiftName.isEmpty()) {
            List<ShiftResponse> shiftResponseList = shiftRepository.findBySiteAndActiveAndShiftNameContainingIgnoreCase(site, 1, shiftName);
            return ShiftResponseList.builder().shiftResponseList(shiftResponseList).build();
        } else {
            return retrieveTop50(site);
        }
    }

    @Override
    public String callExtension(Extension extension) throws Exception {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new ShiftException(800);
        }
        return extensionResponse;
    }

    @Override
    public AuditLogRequest createAuditLog(ShiftRequest shiftRequest) {
        return AuditLogRequest.builder()
                .site(shiftRequest.getSite())
                .action_code("SHIFT-CREATED")
                .action_detail("Shift Created " + shiftRequest.getShiftName() + "/" + shiftRequest.getShiftType())
                .action_detail_handle("ActionDetailBO:" + shiftRequest.getSite() + "," + "SHIFT-CREATED" + shiftRequest.getShiftName() + ":" + "com.rits.shiftservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(shiftRequest.getUserId())
                .txnId("SHIFT-CREATED" + LocalDateTime.now() + shiftRequest.getShiftName())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Create")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest updateAuditLog(ShiftRequest shiftRequest) {
        return AuditLogRequest.builder()
                .site(shiftRequest.getSite())
                .action_code("SHIFT-UPDATED")
                .action_detail("Shift Updated " + shiftRequest.getShiftName() + "/" + shiftRequest.getShiftType())
                .action_detail_handle("ActionDetailBO:" + shiftRequest.getSite() + "," + "SHIFT-UPDATED" + shiftRequest.getShiftName() + ":" + "com.rits.shiftservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(shiftRequest.getUserId())
                .txnId("SHIFT-UPDATED" + LocalDateTime.now() + shiftRequest.getShiftName())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Update")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest deleteAuditLog(ShiftRequest shiftRequest) {
        return AuditLogRequest.builder()
                .site(shiftRequest.getSite())
                .action_code("SHIFT-DELETED")
                .action_detail("Shift Deleted " + shiftRequest.getShiftName() + "/" + shiftRequest.getShiftType())
                .action_detail_handle("ActionDetailBO:" + shiftRequest.getSite() + "," + "SHIFT-DELETED" + shiftRequest.getShiftName() + ":" + "com.rits.shiftservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(shiftRequest.getUserId())
                .txnId("SHIFT-DELETED" + LocalDateTime.now() + shiftRequest.getShiftName())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Delete")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }

    @Override
    public MinutesList getPlannedtime(String site) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        List<Shift> existingShifts = shiftRepository.findBySiteAndActive(site, 1);
        List<PlannedMinutes> plannedTimes = new ArrayList<>();

        LocalDateTime currentDateTime = LocalDateTime.now();
        int indexOfTheDay= currentDateTime.getDayOfWeek().getValue();

        for (Shift shifts : existingShifts) {
            if(shifts.getCalendarRules()!=null) {
                CalendarRules getIndex=shifts.getCalendarRules().get(indexOfTheDay);
                if (!getIndex.getProductionDay().equalsIgnoreCase("Non Production Day")) {
                    for (ShiftIntervals shift : shifts.getShiftIntervals()) {
                        LocalDateTime shiftValidFrom = shift.getValidFrom();
                        LocalDateTime shiftValidTo = shift.getValidEnd();

                        LocalDateTime shiftStartTime = parseToLocalDateTime(shift.getStartTime(), formatter);
                        LocalDateTime shiftEndTime = parseToLocalDateTime(shift.getEndTime(), formatter);

                        if (shiftEndTime.isBefore(shiftStartTime)) {
                            shiftEndTime = shiftEndTime.plusDays(1);  // Add 1 day to consider the next day
                        }

                        // Validate currentDateTime is within both time intervals)
                        if ((currentDateTime.isAfter(shiftStartTime) && currentDateTime.isBefore(shiftEndTime)) &&
                                (currentDateTime.isAfter(shiftValidFrom)) && currentDateTime.isBefore(shiftValidTo)) {
                            int minutesDifference = shift.getActualTime();
                            PlannedMinutes plannedTime = new PlannedMinutes(shifts.getShiftName(), shifts.getShiftType(), shiftStartTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")), shiftEndTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")), minutesDifference);
                            plannedTimes.add(plannedTime);
                        }
                    }
                }else{
                    throw new ShiftException(5007);
                }
            }
        }

        return MinutesList.builder().plannedMinutesList(plannedTimes).build();
    }


    @Override
    public MinutesList getPlannedtimeTillNow(String site) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        List<Shift> shifts = shiftRepository.findBySiteAndActive(site, 1);
        List<PlannedMinutes> plannedTimes = new ArrayList<>();

        LocalDateTime currentDateTime = LocalDateTime.now();
        int indexOfTheDay= currentDateTime.getDayOfWeek().getValue();

        for (Shift shift : shifts) {
            if(shift.getCalendarRules()!=null) {
                CalendarRules getIndex = shift.getCalendarRules().get(indexOfTheDay);
                if (!getIndex.getProductionDay().equalsIgnoreCase("Non Production Day")) {
                    for (ShiftIntervals shiftIntervals : shift.getShiftIntervals()) {
                        LocalDateTime shiftStartDateTime = parseToLocalDateTime(shiftIntervals.getStartTime(), formatter);
                        LocalDateTime shiftEndDateTime = parseToLocalDateTime(shiftIntervals.getEndTime(), formatter);
                        if (shiftEndDateTime.isBefore(shiftStartDateTime)) {
                            shiftEndDateTime = shiftEndDateTime.plusDays(1);  // Add 1 day to consider the next day
                        }

                        if (currentDateTime.isAfter(shiftStartDateTime) && currentDateTime.isBefore(shiftEndDateTime)) {
                            Duration plannedDuration = Duration.between(shiftStartDateTime, currentDateTime);

                            int breakMinutes = calculateBreakMinutes(shiftIntervals, LocalDateTime.from(currentDateTime));
                            int totalShiftMinutes = (int) (plannedDuration.toSeconds() - breakMinutes);

                            PlannedMinutes plannedTime = new PlannedMinutes(
                                    shift.getShiftName(),
                                    shift.getShiftType(),
                                    shiftIntervals.getStartTime(),
                                    shiftIntervals.getEndTime(),
                                    totalShiftMinutes
                            );
                            plannedTimes.add(plannedTime);
                        }
                    }
                } else {
                    throw new ShiftException(5007);
                }
            }
        }

        return MinutesList.builder().plannedMinutesList(plannedTimes).build();
    }
    private PlannedMinutes calculatePlannedTime(List<Shift> shifts){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        PlannedMinutes plannedTime=null;

        LocalDateTime currentDateTime = LocalDateTime.now();
        int indexOfTheDay= currentDateTime.getDayOfWeek().getValue();

        for (Shift shift : shifts) {
            if(shift.getCalendarRules()!=null) {
                CalendarRules getIndex = shift.getCalendarRules().get(indexOfTheDay);
                if (!getIndex.getProductionDay().equalsIgnoreCase("Non Production Day")) {
                    for (ShiftIntervals shiftIntervals : shift.getShiftIntervals()) {
                        LocalDateTime shiftStartDateTime = parseToLocalDateTime(shiftIntervals.getStartTime(), formatter);
                        LocalDateTime shiftEndDateTime = parseToLocalDateTime(shiftIntervals.getEndTime(), formatter);
                        if (shiftEndDateTime.isBefore(shiftStartDateTime)) {
                            shiftEndDateTime = shiftEndDateTime.plusDays(1);  // Add 1 day to consider the next day
                        }

                        if (currentDateTime.isAfter(shiftStartDateTime) && currentDateTime.isBefore(shiftEndDateTime)) {
                            Duration plannedDuration = Duration.between(shiftStartDateTime, currentDateTime);

                            int breakMinutes = calculateBreakMinutes(shiftIntervals, LocalDateTime.from(currentDateTime));
                            int totalShiftMinutes = (int) (plannedDuration.toSeconds() - breakMinutes);

                            plannedTime = new PlannedMinutes(
                                    shift.getShiftName(),
                                    shift.getShiftType(),
                                    shiftIntervals.getStartTime(),
                                    shiftIntervals.getEndTime(),
                                    totalShiftMinutes
                            );
                        }
                    }
                } else {
                    throw new ShiftException(5007);
                }
            }
        }

        return plannedTime;

    }

    private int calculateBreakMinutes(ShiftIntervals shift, LocalDateTime currentDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        List<Break> breakList = shift.getBreakList();
        int breakMinutes = 0;
        if(breakList!=null&& !breakList.isEmpty()) {

            for (Break breakEntry : breakList) {
                LocalDateTime breakStartDateTime = parseToLocalDateTime(breakEntry.getBreakTimeStart(), formatter);
                LocalDateTime breakEndDateTime = parseToLocalDateTime(breakEntry.getBreakTimeEnd(), formatter);
                if (breakEndDateTime.isBefore(breakStartDateTime)) {
                    breakEndDateTime = breakEndDateTime.plusDays(1);  // Add 1 day to consider the next day
                }

                if (currentDateTime.isAfter(breakStartDateTime)) {
                    breakMinutes += Duration.between(breakStartDateTime, currentDateTime.isBefore(breakEndDateTime) ? currentDateTime : breakEndDateTime).toSeconds();
                }
            }
        }

        return breakMinutes;
    }


    @Override
    public BreakMinutesList getBreakHours(String site) {
        List<Shift> shifts = shiftRepository.findBySiteAndActive(site, 1);
        List<BreakMinutes> breakTimes = new ArrayList<>();

        LocalDateTime currentDateTime = LocalDateTime.now();
        int indexOfTheDay= currentDateTime.getDayOfWeek().getValue();

        for (Shift shift : shifts) {
            if (shift.getCalendarRules() != null) {
                CalendarRules getIndex = shift.getCalendarRules().get(indexOfTheDay);
                if (!getIndex.getProductionDay().equalsIgnoreCase("Non Production Day")) {
                    for (ShiftIntervals shiftIntervals : shift.getShiftIntervals()) {
                        LocalDateTime shiftStartDateTime = shiftIntervals.getValidFrom();
                        LocalDateTime shiftEndDateTime = shiftIntervals.getValidEnd();

                        if (shiftEndDateTime.isBefore(shiftStartDateTime)) {
                            shiftEndDateTime = shiftEndDateTime.plusDays(1);  // Add 1 day to consider the next day
                        }

                        LocalDateTime intervalStart = shiftIntervals.getValidFrom();
                        LocalDateTime intervalEnd = shiftIntervals.getValidEnd();

                        if (currentDateTime.isAfter(intervalStart) && currentDateTime.isBefore(intervalEnd)
                                && currentDateTime.isAfter(shiftStartDateTime) && currentDateTime.isBefore(shiftEndDateTime)) {
                            int breakTime = 0;
                            if (shiftIntervals.getBreakList() != null && !shiftIntervals.getBreakList().isEmpty()) {
                                for (Break breakObj : shiftIntervals.getBreakList()) {
                                    LocalDateTime breakStart = currentDateTime.with(LocalTime.parse(breakObj.getBreakTimeStart()));
                                    LocalDateTime breakEnd = currentDateTime.with(LocalTime.parse(breakObj.getBreakTimeEnd()));
                                    breakTime += (int) Duration.between(breakStart, breakEnd).toSeconds();
                                }
                            }
                            Duration plannedDuration = Duration.between(shiftStartDateTime, currentDateTime);

                            int totalShiftMinutes = (int) (plannedDuration.toSeconds() - breakTime);


                            BreakMinutes plannedTime = new BreakMinutes(shift.getShiftName(), shift.getShiftType(),
                                    shiftIntervals.getStartTime(),
                                    shiftIntervals.getEndTime(), breakTime, totalShiftMinutes);
                            breakTimes.add(plannedTime);
                        }
                    }
                } else {
                    throw new ShiftException(5007);
                }
            }
        }

        return BreakMinutesList.builder().breakMinutesList(breakTimes).build();
    }



    @Override
    public BreakMinutesList getBreakHoursTillNow(String site) {
        List<Shift> shifts = shiftRepository.findBySiteAndActive(site, 1);
        List<BreakMinutes> breakTimes = new ArrayList<>();

        LocalDateTime currentDateTime = LocalDateTime.now();
        int indexOfTheDay= currentDateTime.getDayOfWeek().getValue();

        for (Shift shift : shifts) {
            if (shift.getCalendarRules() != null) {
                CalendarRules getIndex = shift.getCalendarRules().get(indexOfTheDay);
                if (!getIndex.getProductionDay().equalsIgnoreCase("Non Production Day")) {
                    for (ShiftIntervals shiftIntervals : shift.getShiftIntervals()) {
                        LocalDateTime shiftStartDateTime = shiftIntervals.getValidFrom();
                        LocalDateTime shiftEndDateTime = shiftIntervals.getValidEnd();


                        if (shiftEndDateTime.isBefore(shiftStartDateTime)) {
                            shiftEndDateTime = shiftEndDateTime.plusDays(1);  // Add 1 day to consider the next day
                        }

                        if (currentDateTime.isAfter(shiftStartDateTime) && currentDateTime.isBefore(shiftEndDateTime)
                                && currentDateTime.isAfter(shiftIntervals.getValidFrom()) && currentDateTime.isBefore(shiftIntervals.getValidEnd())) {
                            Duration plannedDuration = Duration.between(shiftStartDateTime, currentDateTime);


                            int totalBreakTime = calculateTotalBreakMinutes(shiftIntervals.getBreakList(), currentDateTime);
                            int totalShiftMinutes = (int) (plannedDuration.toSeconds() - totalBreakTime);

                            BreakMinutes breakPlannedMinutes = new BreakMinutes(
                                    shift.getShiftName(),
                                    shift.getShiftType(),
                                    shiftIntervals.getStartTime(),
                                    shiftIntervals.getEndTime(),
                                    totalBreakTime, totalShiftMinutes);
                            breakTimes.add(breakPlannedMinutes);
                        }
                    }
                } else {
                    throw new ShiftException(5007);
                }
            }
        }

        return BreakMinutesList.builder().breakMinutesList(breakTimes).build();
    }

    @Override
    public BreakMinutes getBreakHoursTillNowByType(String site, String shiftType, String resource, String workCenter, LocalDateTime localDateTime) {
        if(shiftType.equalsIgnoreCase("resource")){
            String[] resourceSplit= resource.split(",");
            if(resourceSplit.length>=2){
                resource=resourceSplit[1];
            }
            List<Shift> shifts = shiftRepository.findBySiteAndActiveAndShiftTypeAndResource(site, 1,shiftType,resource);
            BreakMinutes breakMinutes=calculateBreakMinutesByType(shifts,localDateTime);
            if(breakMinutes==null||breakMinutes.getShiftName()==null||breakMinutes.getShiftName().isEmpty()){
                List<Shift> generalShift = shiftRepository.findBySiteAndActiveAndShiftType(site, 1,"General");
                breakMinutes=calculateBreakMinutesByType(generalShift, localDateTime);
            }
            return breakMinutes;
        }else if(shiftType.equalsIgnoreCase("workCenter")){
            List<Shift> shifts = shiftRepository.findBySiteAndActiveAndShiftTypeAndWorkCenter(site, 1,shiftType,workCenter);
            BreakMinutes breakMinutes=calculateBreakMinutesByType(shifts, localDateTime);
            if(breakMinutes==null||breakMinutes.getShiftName()==null||breakMinutes.getShiftName().isEmpty()){
                List<Shift> generalShift = shiftRepository.findBySiteAndActiveAndShiftType(site, 1,"General");
                breakMinutes=calculateBreakMinutesByType(generalShift, localDateTime);
            }
            return breakMinutes;
        }else{
            List<Shift> generalShift = shiftRepository.findBySiteAndActiveAndShiftType(site, 1,"General");
            BreakMinutes breakMinutes=calculateBreakMinutesByType(generalShift, localDateTime);
            return breakMinutes;
        }
    }

    @Override
    public PlannedMinutes getPlannedTimeTillNowByType(String site, String shiftType, String resource, String workCenter) {
        if(shiftType.equalsIgnoreCase("resource")) {
            List<Shift> shifts = shiftRepository.findBySiteAndActiveAndShiftTypeAndResource(site, 1, shiftType, resource);
            PlannedMinutes plannedMinutes= calculatePlannedTime(shifts);
            if(plannedMinutes==null||plannedMinutes.getShiftName()==null||plannedMinutes.getShiftName().isEmpty()){
                List<Shift> generalShift = shiftRepository.findBySiteAndActiveAndShiftType(site, 1,"General");
                plannedMinutes=calculatePlannedTime(generalShift);
            }
            return plannedMinutes;

        }else if(shiftType.equalsIgnoreCase("workCenter")){
            List<Shift> shifts = shiftRepository.findBySiteAndActiveAndShiftTypeAndWorkCenter(site, 1,shiftType,workCenter);
            PlannedMinutes plannedMinutes= calculatePlannedTime(shifts);
            if(plannedMinutes==null||plannedMinutes.getShiftName()==null||plannedMinutes.getShiftName().isEmpty()){
                List<Shift> generalShift = shiftRepository.findBySiteAndActiveAndShiftType(site, 1,"General");
                plannedMinutes=calculatePlannedTime(generalShift);
            }
            return plannedMinutes;
        }else{
            List<Shift> generalShift = shiftRepository.findBySiteAndActiveAndShiftType(site, 1,"General");
            PlannedMinutes plannedMinutes=calculatePlannedTime(generalShift);
            return plannedMinutes;
        }
    }

    @Override
    public List<ShiftIntervalWithDate> getShiftsWithDatesInRange(String site, String shiftType, String resource, String workCenter, LocalDateTime dateStart, LocalDateTime dateEnd) {
        List<Shift> shifts;
        boolean callGeneral = false;

        if (shiftType.equalsIgnoreCase("resource")) {
            shifts = shiftRepository.findBySiteAndActiveAndShiftTypeAndResource(site, 1, shiftType, resource);
            if (shifts.isEmpty()) {
                callGeneral = true;
            }
        } else if (shiftType.equalsIgnoreCase("workCenter")) {
            shifts = shiftRepository.findBySiteAndActiveAndShiftTypeAndWorkCenter(site, 1, shiftType, workCenter);
            if (shifts.isEmpty()) {
                callGeneral = true;
            }
        } else {
            shifts = shiftRepository.findBySiteAndActiveAndShiftType(site, 1, "General");
        }

        if (callGeneral) {
            shifts = shiftRepository.findBySiteAndActiveAndShiftType(site, 1, "General");
        }

        List<ShiftIntervalWithDate> shiftsIntervalsWithDate = new ArrayList<>();
        shifts.sort(Comparator.comparing(shift -> {
            List<ShiftIntervals> intervals = shift.getShiftIntervals();
            if (intervals != null && !intervals.isEmpty()) {
                return LocalTime.parse(intervals.get(0).getStartTime());
            }
            return null; // If there are no intervals, return null to push these shifts to the end of the list
        }));

        LocalDateTime currentDate = dateStart;
        while (!currentDate.isAfter(dateEnd)) {
            int indexOfTheDay= currentDate.getDayOfWeek().getValue();

            for (Shift shift : shifts) {
                if(shift.getCalendarRules()!=null) {
                    CalendarRules getIndex = shift.getCalendarRules().get(indexOfTheDay);
                    if (!getIndex.getProductionDay().equalsIgnoreCase("Non Production Day")) {

                        for (ShiftIntervals interval : shift.getShiftIntervals()) {
                            LocalDateTime startTime = LocalDateTime.of(currentDate.toLocalDate(), LocalTime.parse(interval.getStartTime()));
                            LocalDateTime endTime = LocalDateTime.of(currentDate.toLocalDate(), LocalTime.parse(interval.getEndTime()));

                            if (endTime.isBefore(startTime)) {
                                endTime = endTime.plusDays(1);
                            }

                            // Check if the interval overlaps with the desired date range
                            if ((startTime.isBefore(dateEnd) || startTime.equals(dateEnd)) &&
                                    (endTime.isAfter(dateStart) || endTime.equals(dateStart))) {
                                BreakMinutes breakMinutes = calculateBreakMinutes(interval, currentDate, dateEnd);
                                if (breakMinutes != null && breakMinutes.getStartTime() != null && !breakMinutes.getStartTime().isEmpty()) {
                                    breakMinutes.setShiftName(shift.getShiftName());
                                    breakMinutes.setShiftType(shift.getShiftType());
                                    ShiftIntervalWithDate shiftIntervalWithDate = new ShiftIntervalWithDate();
                                    shiftIntervalWithDate.setDate(currentDate.toLocalDate());
                                    shiftIntervalWithDate.setBreakMinutes(breakMinutes);
                                    shiftsIntervalsWithDate.add(shiftIntervalWithDate);
                                }
                            }
                        }
                    } else {
                        throw new ShiftException(5007);
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return shiftsIntervalsWithDate;
    }

    private BreakMinutes calculateBreakMinutes(ShiftIntervals interval, LocalDateTime dateStart, LocalDateTime dateEnd) {
        LocalDateTime intervalStartTime = LocalDateTime.of(dateStart.toLocalDate(), LocalTime.parse(interval.getStartTime()));
        LocalDateTime intervalEndTime = LocalDateTime.of(dateStart.toLocalDate(), LocalTime.parse(interval.getEndTime()));

        // Adjust interval end time if it's before interval start time (meaning it ends on the next day)
        if (intervalEndTime.isBefore(intervalStartTime)) {
            intervalEndTime = intervalEndTime.plusDays(1);
        }

        // Check if the interval overlaps with the date range
        if ((intervalStartTime.isBefore(dateEnd) || intervalStartTime.equals(dateEnd)) &&
                (intervalEndTime.isAfter(dateStart) || intervalEndTime.equals(dateStart))) {

            // Calculate the duration until dateEnd in seconds
            LocalDateTime adjustedIntervalEndTime = intervalEndTime.isAfter(dateEnd) ? dateEnd : intervalEndTime;
            long durationUntilEndSeconds = Duration.between(intervalStartTime, adjustedIntervalEndTime).getSeconds();

            // Subtract break time within this duration in seconds
            int breakTimeSeconds = 0;
            if(interval.getBreakList() != null && ! interval.getBreakList().isEmpty()) {
                for (Break breakObj : interval.getBreakList()) {
                    LocalDateTime breakStartTime = LocalDateTime.of(dateStart.toLocalDate(), LocalTime.parse(breakObj.getBreakTimeStart()));
                    LocalDateTime breakEndTime = LocalDateTime.of(dateStart.toLocalDate(), LocalTime.parse(breakObj.getBreakTimeEnd()));

                    // Adjust break end time if it's before break start time (meaning it ends on the next day)
                    if (breakEndTime.isBefore(breakStartTime)) {
                        breakEndTime = breakEndTime.plusDays(1);
                    }

                    // If break is within the duration until dateEnd, add break time in seconds
                    if ((breakStartTime.isBefore(adjustedIntervalEndTime) || breakStartTime.equals(adjustedIntervalEndTime)) &&
                            (breakEndTime.isAfter(intervalStartTime) || breakEndTime.equals(intervalStartTime))) {
                        LocalDateTime adjustedBreakEndTime = breakEndTime.isAfter(adjustedIntervalEndTime) ? adjustedIntervalEndTime : breakEndTime;
                        breakTimeSeconds += Duration.between(breakStartTime, adjustedBreakEndTime).getSeconds();
                    }
                }
            }


            // Calculate planned time in seconds
            int plannedTimeSeconds = (int) durationUntilEndSeconds - breakTimeSeconds;

            BreakMinutes breakMinutes = new BreakMinutes();
            breakMinutes.setStartTime(interval.getStartTime());
            breakMinutes.setEndTime(interval.getEndTime());
            breakMinutes.setBreakTime(breakTimeSeconds);
            breakMinutes.setPlannedTime(plannedTimeSeconds);
            return breakMinutes;
        }else{
            int breakTimeSeconds = 0;
            if(interval.getBreakList()!= null && !interval.getBreakList().isEmpty()){

                for (Break breakObj : interval.getBreakList()) {

                    LocalDateTime breakStartTime = LocalDateTime.of(dateStart.toLocalDate(), LocalTime.parse(breakObj.getBreakTimeStart()));
                    LocalDateTime breakEndTime = LocalDateTime.of(dateStart.toLocalDate(), LocalTime.parse(breakObj.getBreakTimeEnd()));

                    // Adjust break end time if it's before break start time (meaning it ends on the next day)
                    if (breakEndTime.isBefore(breakStartTime)) {
                        breakEndTime = breakEndTime.plusDays(1);
                    }

                    // If break is within the duration until dateEnd, add break time in seconds
                    if ((breakStartTime.isBefore(intervalEndTime) || breakStartTime.equals(intervalEndTime)) &&
                            (breakEndTime.isAfter(intervalStartTime) || breakEndTime.equals(intervalStartTime))) {
                        LocalDateTime adjustedBreakEndTime = breakEndTime.isAfter(intervalEndTime) ? intervalEndTime : breakEndTime;
                        breakTimeSeconds += Duration.between(breakStartTime, adjustedBreakEndTime).getSeconds();
                    }
                }
                long durationUntilEndSeconds = Duration.between(intervalStartTime, intervalEndTime).getSeconds();
                int plannedTimeSeconds = (int) durationUntilEndSeconds - breakTimeSeconds;

                BreakMinutes breakMinutes = new BreakMinutes();
                breakMinutes.setStartTime(interval.getStartTime());
                breakMinutes.setEndTime(interval.getEndTime());
                breakMinutes.setBreakTime(breakTimeSeconds);
                breakMinutes.setPlannedTime(plannedTimeSeconds);
                return breakMinutes;
            }else {
                long durationUntilEndSeconds = Duration.between(intervalStartTime, intervalEndTime).getSeconds();
                int plannedTimeSeconds = (int) durationUntilEndSeconds - breakTimeSeconds;

                BreakMinutes breakMinutes = new BreakMinutes();
                breakMinutes.setStartTime(interval.getStartTime());
                breakMinutes.setEndTime(interval.getEndTime());
                breakMinutes.setBreakTime(breakTimeSeconds);
                breakMinutes.setPlannedTime(plannedTimeSeconds);
                return breakMinutes;

            }
        }
    }



    @Override
    public List<ShiftIntervalWithDate> getShiftsWithDatesInRanges(String site, String shiftType, String resource, String workCenter, LocalDateTime dateStart, LocalDateTime dateEnd) {
        List<Shift> shifts;
        Boolean callGeneral =false;
        if (shiftType.equalsIgnoreCase("resource")) {
            shifts = shiftRepository.findBySiteAndActiveAndShiftTypeAndResource(site, 1, shiftType, resource);
            if(shifts.isEmpty()){
                callGeneral=true;
            }
        } else if (shiftType.equalsIgnoreCase("workCenter")) {
            shifts = shiftRepository.findBySiteAndActiveAndShiftTypeAndWorkCenter(site, 1, shiftType, workCenter);
            if(shifts.isEmpty()){
                callGeneral=true;
            }
        } else {
            shifts = shiftRepository.findBySiteAndActiveAndShiftType(site, 1, "General");
        }

        if(callGeneral){
            shifts = shiftRepository.findBySiteAndActiveAndShiftType(site, 1, "General");
        }
        List<ShiftIntervalWithDate> shiftsInRange = new ArrayList<>();

        LocalDate currentDate = dateStart.toLocalDate();
        LocalTime startTime = dateStart.toLocalTime();
        LocalTime endTime = dateEnd.toLocalTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        while (!currentDate.isAfter(dateEnd.toLocalDate())) {
            int indexOfTheDay= currentDate.getDayOfWeek().getValue();

            for (Shift shift : shifts) {
                if(shift.getCalendarRules()!=null) {
                    CalendarRules getIndex = shift.getCalendarRules().get(indexOfTheDay);
                    if (!getIndex.getProductionDay().equalsIgnoreCase("Non Production Day")) {
                        int count = 0;
                        for (ShiftIntervals interval : shift.getShiftIntervals()) {
                            int totalBreaks = 0;
                            int totalShiftMinutes = 0;

                            LocalDateTime shiftStartDateTime = parseToLocalDateTime(interval.getStartTime(), formatter);
                            LocalDateTime shiftEndDateTime = parseToLocalDateTime(interval.getEndTime(), formatter);
                            if (shiftEndDateTime.isBefore(shiftStartDateTime)) {
                                shiftEndDateTime = shiftEndDateTime.plusDays(1);
                            }
                            Duration plannedDuration = Duration.between(shiftStartDateTime, shiftEndDateTime);
                            if (interval.getBreakList() != null) {
                                for (Break breaks : interval.getBreakList()) {
                                    int meanTime = Integer.parseInt(breaks.getMeanTime());
                                    totalBreaks += meanTime;
                                }
                            }
                            totalShiftMinutes = (int) (plannedDuration.toSeconds() - totalBreaks);

                            BreakMinutes breakMinutes = BreakMinutes.builder()
                                    .shiftName(shift.getShiftName())
                                    .shiftType(shift.getShiftType())
                                    .startTime(interval.getStartTime())
                                    .endTime(interval.getEndTime())
                                    .breakTime(totalBreaks)
                                    .plannedTime(totalShiftMinutes)
                                    .build();
//                    LocalDateTime intervalStartDate=parseStringToLocalDateTime(currentDate, interval.getStartTime());
//                  LocalDateTime intervalEndDate=parseStringToLocalDateTime(currentDate,interval.getEndTime());

//                    if((dateStart.isAfter(intervalStartDate) && dateStart.isBefore(intervalEndDate)) || (dateEnd.isAfter(intervalStartDate) && dateEnd.isBefore(intervalEndDate))) {
//                        ShiftIntervalWithDate shiftIntervalWithDate = ShiftIntervalWithDate.builder().date(currentDate).breakMinutes(breakMinutes).build();
//                      shiftsInRange.add(shiftIntervalWithDate);
//                  }

                            if (currentDate.isEqual(dateStart.toLocalDate())) {
                                if (startTime.isAfter(LocalTime.parse(interval.getStartTime())) && startTime.isBefore(LocalTime.parse(interval.getEndTime()))) {
                                    count++;
                                }
                                if (count > 0) {
                                    ShiftIntervalWithDate shiftIntervalWithDate = ShiftIntervalWithDate.builder().date(currentDate).breakMinutes(breakMinutes).build();
                                    shiftsInRange.add(shiftIntervalWithDate);
                                }
                            }

                            if (dateStart.isEqual(dateEnd) && !currentDate.isEqual(dateStart.toLocalDate()) && !currentDate.isEqual(dateEnd.toLocalDate())) {
                                ShiftIntervalWithDate shiftIntervalWithDate = ShiftIntervalWithDate.builder().date(currentDate).breakMinutes(breakMinutes).build();
                                shiftsInRange.add(shiftIntervalWithDate);
                            }
                            if (currentDate.isEqual(dateEnd.toLocalDate())) {
                                ShiftIntervalWithDate shiftIntervalWithDate = ShiftIntervalWithDate.builder().date(currentDate).breakMinutes(breakMinutes).build();
                                shiftsInRange.add(shiftIntervalWithDate);
                                if (endTime.isBefore(LocalTime.parse(interval.getEndTime())) && endTime.isAfter(LocalTime.parse(interval.getStartTime()))) {
                                    break;
                                }
                            }
                        }
                    } else {
                        throw new ShiftException(5007);
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return shiftsInRange;
    }
    private LocalDateTime parseStringToLocalDateTime(LocalDate localDate, String shiftStartTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        LocalTime time = LocalTime.parse(shiftStartTime,formatter );
        return LocalDateTime.of(localDate, time);
    }

    private BreakMinutes calculateBreakMinutesByType(List<Shift> shifts, LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime currentDateTime;
        if(localDateTime!=null) {
            currentDateTime=localDateTime;
        }else {

            currentDateTime = LocalDateTime.now();
        }
        BreakMinutes breakPlannedMinutes = null;

        int indexOfTheDay= currentDateTime.getDayOfWeek().getValue()%7;

        for (Shift shift : shifts) {
            if(shift.getCalendarRules()!=null) {
                CalendarRules getIndex = shift.getCalendarRules().get(indexOfTheDay);
                if (shift.getShiftType().equalsIgnoreCase("General")&&getIndex.getProductionDay().equalsIgnoreCase("Non Production Day")) {
                    throw new ShiftException(5007);
                }
                if(shift.getShiftType().equalsIgnoreCase("Resource")&&getIndex.getProductionDay().equalsIgnoreCase("Non Production Day")){
                    continue;
                }
                    for (ShiftIntervals shiftIntervals : shift.getShiftIntervals()) {
                        LocalDateTime shiftStartDateTime = parseStringToLocalDateTime(currentDateTime.toLocalDate(), shiftIntervals.getStartTime());
                        LocalDateTime shiftEndDateTime = parseStringToLocalDateTime(currentDateTime.toLocalDate(), shiftIntervals.getEndTime());

                        boolean midNightShift = false;
                        if (shiftEndDateTime.isBefore(shiftStartDateTime)) {
                            shiftEndDateTime = shiftEndDateTime.plusDays(1);
                            midNightShift = true;
                            // Add 1 day to consider the next day
                        }

                        if ((currentDateTime.isAfter(shiftStartDateTime) && currentDateTime.isBefore(shiftEndDateTime)
                                && currentDateTime.isAfter(shiftIntervals.getValidFrom()) && currentDateTime.isBefore(shiftIntervals.getValidEnd())) ){

                            Duration plannedDuration =  Duration.between(shiftStartDateTime, currentDateTime) ;


                            int totalBreakTime = calculateTotalBreakMinutes(shiftIntervals.getBreakList(), currentDateTime);
                            int totalShiftMinutes = (int) (plannedDuration.toSeconds() - totalBreakTime);


                            breakPlannedMinutes = new BreakMinutes(
                                    shift.getShiftName(),
                                    shift.getShiftType(),
                                    shiftIntervals.getStartTime(),
                                    shiftIntervals.getEndTime(),
                                    totalBreakTime,
                                    totalShiftMinutes);

                        } else {
                            if (midNightShift) {
                                currentDateTime = currentDateTime.plusDays(1);
                                if (currentDateTime.isAfter(shiftStartDateTime) && currentDateTime.isBefore(shiftEndDateTime)
                                        && currentDateTime.isAfter(shiftIntervals.getValidFrom()) && currentDateTime.isBefore(shiftIntervals.getValidEnd())) {
                                    Duration plannedDuration = Duration.between(shiftStartDateTime, currentDateTime);


                                    int totalBreakTime = calculateTotalBreakMinutes(shiftIntervals.getBreakList(), currentDateTime);
                                    int totalShiftMinutes = (int) (plannedDuration.toSeconds() - totalBreakTime);


                                    breakPlannedMinutes = new BreakMinutes(
                                            shift.getShiftName(),
                                            shift.getShiftType(),
                                            shiftIntervals.getStartTime(),
                                            shiftIntervals.getEndTime(),
                                            totalBreakTime,
                                            totalShiftMinutes);
                                }
                            }
                        }
                    }
                }

        }
        return breakPlannedMinutes;
    }

    private int calculateTotalBreakMinutes(List<Break> breakList, LocalDateTime currentDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        int totalBreakTime = 0;
        if(breakList!=null&& !breakList.isEmpty()) {

            for (Break breakEntry : breakList) {
                LocalDateTime breakStartDateTime =parseStringToLocalDateTime(currentDateTime.toLocalDate(),breakEntry.getBreakTimeStart());
                LocalDateTime breakEndDateTime = parseStringToLocalDateTime(currentDateTime.toLocalDate(),breakEntry.getBreakTimeEnd());
                if (breakEndDateTime.isBefore(breakStartDateTime)) {
                    breakEndDateTime = breakEndDateTime.plusDays(1);  // Add 1 day to consider the next day
                }

                if (currentDateTime.isAfter(breakStartDateTime)) {
                    totalBreakTime += Duration.between(breakStartDateTime, currentDateTime.isBefore(breakEndDateTime) ? currentDateTime : breakEndDateTime).toSeconds();
                }
            }
        }

        return totalBreakTime;
    }
}
*/
