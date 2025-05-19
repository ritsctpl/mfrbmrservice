package com.rits.downtimeservice.service;

import com.rits.Utility.BOConverter;
import com.rits.availability.dto.AvailabilityRequestForDowntime;
import com.rits.common.dto.OeeFilterRequest;
import com.rits.downtimeservice.dto.*;
import com.rits.downtimeservice.exception.DowntimeException;
import com.rits.downtimeservice.model.Downtime;
import com.rits.downtimeservice.repository.DowntimeRepository;
import com.rits.downtimeservice.repository.MachineLogRepository;
//import com.rits.downtimeservice.repository.MachineLogRepository;
//import com.rits.shiftservice.model.Break;
import com.rits.oeeservice.dto.ShiftRequest;
import com.rits.oeeservice.service.OeeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DowntimeServiceImpl implements DowntimeService {
    private final MessageSource localMessageSource;
    private final JdbcTemplate jdbcTemplate;
    private final MachineLogRepository machineLogRepository;
    private final DowntimeRepository downtimeRepository;
    @PersistenceContext
    private EntityManager entityManager;
    //private final MachineLogRepository machineLogRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${shift-service.url}/shiftPlannedOperatingTime")
    private String shiftPlannedOperatingTimeUrl;

    @Value("${shift-service.url}/nonProduction")
    private String nonProductionQueryUrl;

    @Value("${shift-service.url}/breakDurationList")
    private String breakDurationQueryUrl;

    @Value("${shift-service.url}/getCurrentShiftAndBreak")
    private String getCurrentShiftAndBreakUrl;
    @Value("${downtime-service.url}/dynamicBreakDuration")
    private String dynamicBreakDurationQueryUrl;

    @Value("${shift-service.url}/CurrentCompleteShiftDetails")
    private String currentCompleteShiftDetails;

    @Autowired
    private OeeService oeeService;

    public String getFormattedMessage(int code, Object...args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    //    @Override
    //    public DowntimeMessageModel calculateTrigger(DowntimeRequest downtimeRequest) throws Exception{
    //        MachineLogEntity machineLog = null;
    //        try{
    //            String sql = "SELECT * FROM get_shift_details(:shiftSite, :shiftCurrentDateTime, :p_shiftName, :shiftResource)";
    //
    //            Query query = entityManager.createNativeQuery(sql);
    //            query.setParameter("shiftSite", downtimeRequest.getSite());
    //            query.setParameter("shiftCurrentDateTime", Timestamp.valueOf(LocalDateTime.now()));
    //            query.setParameter("p_shiftName", downtimeRequest.getShift());
    //            query.setParameter("shiftResource", downtimeRequest.getResource());
    //
    //            List<Object[]> results = query.getResultList();
    //
    //            if (!results.isEmpty()) {
    //                Object[] row = results.get(0);
    //                ShiftDTO shiftDetail = ShiftDTO.builder()
    //                        .shiftName((String) row[0])
    //                        .shiftCreatedDateTime(((Timestamp) row[1]).toLocalDateTime())
    //                        .breakCreatedDateTime(((Timestamp) row[2]).toLocalDateTime())
    //                        .build();
    //
    //                Timestamp shiftCreatedTimestamp = shiftDetail.getShiftCreatedDateTime() != null
    //                        ? Timestamp.valueOf(shiftDetail.getShiftCreatedDateTime())
    //                        : null;
    //                Timestamp shiftBreakCreatedTimestamp = shiftDetail.getBreakCreatedDateTime() != null
    //                        ? Timestamp.valueOf(shiftDetail.getBreakCreatedDateTime())
    //                        : null;
    //
    //                 machineLog = MachineLogEntity.builder()
    //                        .siteId(downtimeRequest.getSite())
    //                        .logEvent(downtimeRequest.getEvent())
    //                        .shiftId(shiftDetail.getShiftName())
    //                        .resourceId(downtimeRequest.getResource())
    //                        .shiftCreatedDateTime(shiftCreatedTimestamp.toLocalDateTime())
    //                        .shiftBreakCreatedDateTime(shiftBreakCreatedTimestamp.toLocalDateTime())
    //                        .logMessage(downtimeRequest.getLogMessage())
    //                        .createdDateTime(LocalDateTime.now())
    //                        .active(1)
    //                        .build();
    //
    //                String createdMessage = getFormattedMessage(1, shiftDetail.getShiftName());
    //                return DowntimeMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(machineLogRepository.save(machineLog)).build();
    //            }
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //        return DowntimeMessageModel.builder().message_details(new MessageDetails("no result found", "E")).build();
    //    }
    //
    //
    //    @Override
    //    public DowntimeMessageModel calculateLive(DowntimeRequest downtimeRequest) throws Exception{
    //
    //        List<DowntimeLiveRecord> liveRecords = new ArrayList<>();
    //
    //        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
    //            String sql = "SELECT * FROM liveDowntimeCalc(?, ?, ?, ?, ?, ?)";
    //            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
    //                Timestamp startTime = downtimeRequest.getStart_time() != null
    //                        ? Timestamp.valueOf(downtimeRequest.getStart_time())
    //                        : null;
    //                Timestamp endTime = downtimeRequest.getEnd_time() != null
    //                        ? Timestamp.valueOf(downtimeRequest.getEnd_time())
    //                        : null;
    //
    //                stmt.setString(1, downtimeRequest.getSite());
    //                stmt.setString(2, downtimeRequest.getResource());
    //                stmt.setString(3, downtimeRequest.getWorkcenter());
    //                stmt.setString(4, downtimeRequest.getShift());
    //                stmt.setTimestamp(5, startTime);
    //                stmt.setTimestamp(6, endTime);
    //
    //                try (ResultSet rs = stmt.executeQuery()) {
    //                    while (rs.next()) {
    //                        DowntimeLiveRecord record = new DowntimeLiveRecord();
    //                        record.setSiteId(rs.getString("site_id"));
    //                        record.setShiftId(rs.getString("shift_id"));
    //                        record.setWorkcenterId(rs.getString("workcenter_id"));
    //                        record.setResourceId(rs.getString("resource_id"));
    //                        record.setItemId(rs.getString("item_id"));
    //                        record.setOperationId(rs.getString("operation_id"));
    //                        record.setTotalDowntime(rs.getLong("totalDowntime"));
    //
    //                        liveRecords.add(record);
    //                    }
    //                }
    //            }
    //        } catch (SQLException e) {
    //            e.printStackTrace();
    //        }
    //
    //        String createdMessage = getFormattedMessage(4, downtimeRequest.getResource());
    //        return DowntimeMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).downtimeRecordList(liveRecords).build();
    //
    //    }


    //   DowntimeMessageModel calculateLive(DowntimeRequest downtimeRequest) throws Exception;
    //   DowntimeMessageModel calculateLive(DowntimeRequest downtimeRequest) throws Exception;

    @Override
    public DowntimeResponse updateDowntime(Long id, DowntimeRequest downtimeRequest) {
        Optional < Downtime > existingDowntime = downtimeRepository.findById(id);

        if (!existingDowntime.isPresent()) {
            return new DowntimeResponse("Downtime record not found", false);
        }

        Downtime downtime = existingDowntime.get();

        if (downtimeRequest.getDowntimeEnd() != null &&
                downtimeRequest.getDowntimeEnd().isBefore(downtime.getDowntimeStart())) {
            return new DowntimeResponse("Downtime end cannot be before downtime start", false);
        }

        downtime.setDowntimeEnd(downtimeRequest.getDowntimeEnd());
        downtime.setDowntimeDuration(calculateDowntimeDuration(downtime.getDowntimeStart(), downtimeRequest.getDowntimeEnd()));
        downtime.setDowntimeType(downtimeRequest.getDowntimeType());
        downtime.setReason(downtimeRequest.getReason());
        downtime.setRootCause(downtimeRequest.getRootCause());
        downtime.setCommentUsr(downtimeRequest.getCommentUsr());
        downtime.setIsOeeImpact(downtimeRequest.getDowntimeType().equalsIgnoreCase("unplanned") ? 1 : 0);
        downtime.setUpdatedDatetime(LocalDateTime.now());

        downtimeRepository.save(downtime);

        return new DowntimeResponse("Downtime updated successfully", true);
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


    private Boolean savedowntimeRec(DowntimeRequest downtimeRequest, CurrentShiftDetails shiftResponse,long downtimeDuration,int Active ) {

        Downtime newDowntime = new Downtime();

        newDowntime.setResourceId(downtimeRequest.getResourceId() != null ? downtimeRequest.getResourceId() : null);
        newDowntime.setWorkcenterId(downtimeRequest.getWorkcenterId() != null ? downtimeRequest.getWorkcenterId() : null);
        newDowntime.setSite(downtimeRequest.getSite() != null ? downtimeRequest.getSite() : null);
        newDowntime.setShiftId(shiftResponse == null ? null : StringUtils.isEmpty(shiftResponse.getShiftId()) ? null : shiftResponse.getShiftId());
        newDowntime.setShiftCreatedDateTime(shiftResponse == null ? null : shiftResponse.getShiftCreatedDatetime() == null ? null : shiftResponse.getShiftCreatedDatetime());
        if (downtimeRequest.getDowntEvent()==0) {

                newDowntime.setDowntimeDuration(downtimeDuration);

            if(downtimeRequest.getDowntimeStart()!=null)
            newDowntime.setDowntimeStart(downtimeRequest.getDowntimeStart());
            else
                newDowntime.setDowntimeStart(LocalDateTime.now());
        }
        if (downtimeRequest.getDowntEvent()==1) {
            if(downtimeRequest.getDowntimeEnd()!=null) {
                newDowntime.setDowntimeEnd(downtimeRequest.getDowntimeEnd());
            }
            else {
                newDowntime.setDowntimeEnd(LocalDateTime.now());
            }
            if(downtimeDuration!=0) {
                newDowntime.setDowntimeDuration(downtimeDuration);
            }
            else{
                newDowntime.setDowntimeDuration(0L);
            }
            newDowntime.setDowntimeStart(downtimeRequest.getDowntimeStart());

        }
        newDowntime.setDowntEvent(downtimeRequest.getDowntEvent() != null ? downtimeRequest.getDowntEvent() : null);
        newDowntime.setReason(downtimeRequest.getReason() != null ? downtimeRequest.getReason() : null);
        newDowntime.setRootCause(downtimeRequest.getRootCause() != null ? downtimeRequest.getRootCause() : null);
        newDowntime.setCommentUsr(downtimeRequest.getCommentUsr() != null ? downtimeRequest.getCommentUsr() : null);
        newDowntime.setDowntimeType(downtimeRequest.getDowntimeType() != null ? downtimeRequest.getDowntimeType() : null);
        newDowntime.setActive(Active);
        newDowntime.setIsOeeImpact(downtimeRequest.getDowntimeType() != null ? (downtimeRequest.getDowntimeType().equalsIgnoreCase("unplanned") ? 1 : 0) : null);
        newDowntime.setCreatedDatetime(LocalDateTime.now());
        newDowntime.setUpdatedDatetime(LocalDateTime.now());
        newDowntime.setPlannedOperatingTime(shiftResponse.getPlannedOperatingTime());
        newDowntime.setShiftRef(shiftResponse.getShiftRef());
        try {
            downtimeRepository.save(newDowntime);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    @Transactional
    public Boolean logDowntime(DowntimeRequest downtimeRequest) {
         Boolean Created=false;
        LocalDateTime downtimeStartDate = null;
        long totalDowntimeDuration = 0;
        if (!StringUtils.isEmpty(downtimeRequest.getResourceId())) {
            CurrentShiftDetails shiftResponse = new CurrentShiftDetails();
            Pageable pageable = PageRequest.of(0, 1);
            Page < Downtime > firstDownPage = downtimeRepository.findFirstDownEvent(
                    downtimeRequest.getResourceId(), downtimeRequest.getWorkcenterId(), pageable);

            Optional < Downtime > firstDown = firstDownPage.stream().findFirst();
            if (!firstDown.isEmpty()) {
                downtimeStartDate = firstDown.get().getDowntimeStart();
            }
            else{
            downtimeStartDate=LocalDateTime.now();
            }


            if (downtimeRequest.getDowntEvent() == 0) {

             updateDowntimeStatusService(downtimeRequest.getWorkcenterId(), downtimeRequest.getResourceId(), downtimeRequest.getShiftId(), downtimeRequest.getSite(), downtimeStartDate);
                if (firstDown.isPresent()) {
                    // Calculate downtime between the first recorded downtime start and now
                    long newDowntime = java.time.Duration.between(
                            firstDown.get().getCreatedDatetime(),
                            LocalDateTime.now()
                    ).toSeconds();

                    // Add previously recorded downtime duration, if any
                    long previousDowntime = (firstDown.get().getDowntimeDuration() != null && firstDown.get().getDowntimeDuration() != 0)
                            ? firstDown.get().getDowntimeDuration()
                            : 0;

                    // Subtract break time (if any)
                    long breakTimeInSeconds = (shiftResponse.getBreaktime() != null ? shiftResponse.getBreaktime() : 0) * 60;

                    totalDowntimeDuration = newDowntime + previousDowntime - breakTimeInSeconds;
                }

                downtimeRequest.setDowntimeStart(downtimeStartDate);
                shiftResponse = getCurruntShiftDetails(downtimeRequest, downtimeStartDate);
                savedowntimeRec(downtimeRequest, shiftResponse,totalDowntimeDuration,1);


            } else {
                downtimeRequest.setDowntimeEnd(downtimeRequest.getDateTime());
                downtimeRequest.setDowntimeStart(downtimeStartDate);
                shiftResponse = getCurruntShiftDetails(downtimeRequest, downtimeStartDate);
                if (firstDown.isPresent() && downtimeRequest.getDowntEvent() == 1) {

                    if (shiftResponse.getNonproduction() != null && shiftResponse.getNonproduction() == 1) {
                        downtimeRepository.updateDowntimeAndDeactivateRecords(downtimeRequest.getResourceId(), downtimeRequest.getWorkcenterId(), downtimeRequest.getDowntimeEnd(), 0L, shiftResponse.getPlannedOperatingTime());
                        return true;
                    }

                    if (firstDown.isPresent()) {
                        // Calculate downtime between the first recorded downtime start and now
                        long newDowntime = java.time.Duration.between(
                                firstDown.get().getCreatedDatetime(),
                                LocalDateTime.now()
                        ).toSeconds();

                        // Add previously recorded downtime duration, if any
                        long previousDowntime = (firstDown.get().getDowntimeDuration() != null && firstDown.get().getDowntimeDuration() != 0)
                                ? firstDown.get().getDowntimeDuration()
                                : 0;

                        // Subtract break time (if any)
                        long breakTimeInSeconds = (shiftResponse.getBreaktime() != null ? shiftResponse.getBreaktime() : 0) * 60;

                        totalDowntimeDuration = newDowntime + previousDowntime - breakTimeInSeconds;
                    }
                    savedowntimeRec(downtimeRequest, shiftResponse , totalDowntimeDuration,1);
                    downtimeRepository.deactivateRecords(downtimeRequest.getResourceId(), downtimeRequest.getWorkcenterId());


                } else {
                    savedowntimeRec(downtimeRequest, shiftResponse , 0,0);
                }

            }
        } else {
            List<Downtime> downtime = downtimeRepository.findGroupedDowntimesBySiteAndDowntEvent(downtimeRequest.getSite(), 0);
            if(downtime.size()!=0){
                for (Downtime oldDowntime : downtime) {
                    // Calculate downtimeDuration (time difference in minutes)
                    CurrentShiftDetails shiftResponse = getCurruntShiftDetails(downtimeRequest, oldDowntime.getDowntimeStart());

                    // Calculate downtime duration
                    if (downtime.size()!=0) {
                        // Calculate downtime between the first recorded downtime start and now
                        long newDowntime = java.time.Duration.between(
                                oldDowntime.getCreatedDatetime(),
                                LocalDateTime.now()
                        ).toSeconds();

                        // Add previously recorded downtime duration, if any
                        long previousDowntime = oldDowntime.getDowntimeDuration() != 0
                                ? oldDowntime.getDowntimeDuration()
                                : 0;

                        // Subtract break time (if any)
                        long breakTimeInSeconds = (shiftResponse.getBreaktime() != null ? shiftResponse.getBreaktime() : 0) * 60;

                        totalDowntimeDuration = newDowntime + previousDowntime - breakTimeInSeconds;
                    }
                    // Create a new downtime record with updated data
                    Downtime newDowntime = Downtime.builder()
                            .resourceId(oldDowntime.getResourceId())
                            .workcenterId(oldDowntime.getWorkcenterId())
                            .site(oldDowntime.getSite())
                            .shiftId(oldDowntime.getShiftId())
                            .shiftCreatedDateTime(oldDowntime.getShiftCreatedDateTime())
                            .shiftBreakStartDatetime(String.valueOf(shiftResponse.getBreakStartTime()))
                            .downtimeStart(oldDowntime.getDowntimeStart())
                            .plannedOperatingTime(shiftResponse.getPlannedOperatingTime())
                            .downtimeDuration(totalDowntimeDuration)
                            .downtEvent(oldDowntime.getDowntEvent())
                            .reason(oldDowntime.getReason())
                            .rootCause(oldDowntime.getRootCause())
                            .commentUsr(oldDowntime.getCommentUsr())
                            .downtimeType(oldDowntime.getDowntimeType())
                            .active(1) // Mark new record as active
                            .createdDatetime(LocalDateTime.now()) // New createdDatetime
                            .updatedDatetime(LocalDateTime.now()) // New updatedDatetime
                            .build();

                    // Save the new downtime record
                    downtimeRepository.save(newDowntime);

                    // Update the old downtime record to inactive
                    oldDowntime.setActive(0);
                    oldDowntime.setUpdatedDatetime(LocalDateTime.now());
                    downtimeRepository.save(oldDowntime);
                }
            }
            else{

            }
        }

        return true;
    }
    public int updateDowntimeStatusService(String workcenterId, String resourceId, String shiftId, String site, LocalDateTime startDatetime) {
        // Construct the query string with parameters
        String query = "UPDATE r_downtime d SET active = 0 " +
                "WHERE (" + (workcenterId == null ? "NULL" : "'" + workcenterId + "'") + " IS NULL OR d.workcenter_id = '" + workcenterId + "') " +
                "AND (" + (resourceId == null ? "NULL" : "'" + resourceId + "'") + " IS NULL OR d.resource_id = '" + resourceId + "') " +
                "AND (" + (shiftId == null ? "NULL" : "'" + shiftId + "'") + " IS NULL OR d.shift_id = '" + shiftId + "') " +
                "AND (" + (site == null ? "NULL" : "'" + site + "'") + " IS NULL OR d.site = '" + site + "') " +
                "AND d.downt_event = 0 " +
                "AND d.created_datetime BETWEEN '" + startDatetime + "' AND CURRENT_TIMESTAMP";

        // Print the constructed query
        System.out.println("Executing query: " + query);

        // Execute the repository method
        int updatedRows = downtimeRepository.updateDowntimeStatus(workcenterId, resourceId, shiftId, site, startDatetime);

        // Log the result
        System.out.println("Number of rows updated: " + updatedRows);
        return updatedRows;
    }
    /* @Override
    @Transactional
    public DowntimeResponse logDowntime(DowntimeRequest downtimeRequest) {

        if (StringUtils.isEmpty(downtimeRequest.getResourceId()))
            throw new DowntimeException(1007);

        DowntimeRequestForShift shift = DowntimeRequestForShift.builder().site(downtimeRequest.getSite()).build();
        ShiftDetailsDTO shiftResponse = webClientBuilder
                .build()
                .post()
                .uri(getCurrentShiftAndBreakUrl)
                .body(BodyInserters.fromValue(shift))
                .retrieve()
                .bodyToMono(ShiftDetailsDTO.class)
                .block();

        if(downtimeRequest.getDowntEvent()==0){

            Downtime newDowntime = new Downtime();

            newDowntime.setResourceId(downtimeRequest.getResourceId() != null ? downtimeRequest.getResourceId() : null);
            newDowntime.setWorkcenterId(downtimeRequest.getWorkcenterId() != null ? downtimeRequest.getWorkcenterId() : null);
            newDowntime.setSite(downtimeRequest.getSite() != null ? downtimeRequest.getSite() : null);
            newDowntime.setShiftId(shiftResponse == null ? null : StringUtils.isEmpty(shiftResponse.getShiftId()) ? null : shiftResponse.getShiftId());
            newDowntime.setShiftCreatedDateTime(shiftResponse == null ? null : shiftResponse.getShiftCreatedDatetime() == null ? null : shiftResponse.getShiftCreatedDatetime());
            newDowntime.setShiftBreakStartDatetime(shiftResponse == null ? null : shiftResponse.getBreakStartTime() == null ? null : String.valueOf(shiftResponse.getBreakStartTime()));
            newDowntime.setDowntimeStart(downtimeRequest.getDowntimeStart() != null ? downtimeRequest.getDowntimeStart() : null); // downtime is calculated, why harcoded
            newDowntime.setDowntimeEnd(downtimeRequest.getDowntimeEnd() != null ? downtimeRequest.getDowntimeEnd() : null);
            newDowntime.setDowntEvent(downtimeRequest.getDowntEvent() != null ? downtimeRequest.getDowntEvent() : null);
            newDowntime.setReason(downtimeRequest.getReason() != null ? downtimeRequest.getReason() : null);
            newDowntime.setRootCause(downtimeRequest.getRootCause() != null ? downtimeRequest.getRootCause() : null);
            newDowntime.setCommentUsr(downtimeRequest.getCommentUsr() != null ? downtimeRequest.getCommentUsr() : null);
            newDowntime.setDowntimeType(downtimeRequest.getDowntimeType() != null ? downtimeRequest.getDowntimeType() : null);
            newDowntime.setIsActive(1);
            newDowntime.setIsOeeImpact(downtimeRequest.getDowntimeType() != null ? (downtimeRequest.getDowntimeType().equalsIgnoreCase("unplanned") ? 1 : 0 ) : null);
            newDowntime.setCreatedDatetime(LocalDateTime.now());
            newDowntime.setUpdatedDatetime(LocalDateTime.now());

            downtimeRepository.save(newDowntime);
        }else {

            if (StringUtils.isEmpty(downtimeRequest.getWorkcenterId()))
                throw new DowntimeException(1006);

//            if (StringUtils.isEmpty(downtimeRequest.getShiftId()))
//                throw new DowntimeException(1008);

            Optional<Downtime> firstDown = downtimeRepository.findFirstDownEvent(downtimeRequest.getResourceId(), downtimeRequest.getWorkcenterId());

            if (!firstDown.isPresent() && downtimeRequest.getDowntEvent() == 1) {
                return new DowntimeResponse("No active down event found", false);
            }

            DowntimeRequestForShift shiftPlannedOperatingTimeRequest = DowntimeRequestForShift.builder()
                    .site(downtimeRequest.getSite())
                    .shiftId(shiftResponse.getShiftId())
                    .build();

            Long shiftPlannedOperatingTime = webClientBuilder.build()
                    .post()
                    .uri(shiftPlannedOperatingTimeUrl)
                    .bodyValue(shiftPlannedOperatingTimeRequest)
                    .retrieve()
                    .bodyToMono(Long.class)
                    .block();

            DowntimeRequestForShift nonProductionQueryRequest = DowntimeRequestForShift.builder()
                    .date(firstDown.get().getDowntimeStart())
                    .build();

            Long nonProduction = webClientBuilder.build()
                    .post()
                    .uri(nonProductionQueryUrl)
                    .bodyValue(nonProductionQueryRequest)
                    .retrieve()
                    .bodyToMono(Long.class)
                    .block();

            if (nonProduction != null && nonProduction == 1) {
                downtimeRepository.updateDowntimeAndDeactivateRecords(downtimeRequest.getResourceId(), downtimeRequest.getWorkcenterId(), downtimeRequest.getDowntimeEnd(), 0L, shiftPlannedOperatingTime);
                return new DowntimeResponse("Downtime logged successfully ", true);
            }

            DowntimeRequestForShift breakDurationQueryRequest = DowntimeRequestForShift.builder()
                    .site(downtimeRequest.getSite())
                    .downtimeEnd(downtimeRequest.getDowntimeEnd())
                    .downtimeStart(firstDown.get().getDowntimeStart())
                    .shiftIds(List.of(shiftResponse.getShiftId()))
                    .build();

            Long breakDuration = webClientBuilder.build()
                    .post()
                    .uri(breakDurationQueryUrl)
                    .bodyValue(breakDurationQueryRequest)
                    .retrieve()
                    .bodyToMono(Long.class)
                    .block();

            long downtimeDuration = Duration.between(firstDown.get().getDowntimeStart().toLocalTime(), downtimeRequest.getDowntimeEnd().toLocalTime()).toMinutes();
            downtimeDuration -= (breakDuration != null ? breakDuration : 0);

            downtimeRepository.updateDowntimeAndDeactivateRecords(downtimeRequest.getResourceId(), downtimeRequest.getWorkcenterId(), downtimeRequest.getDowntimeEnd(), downtimeDuration, shiftPlannedOperatingTime);

            Downtime newDowntime = new Downtime();

            newDowntime.setResourceId(downtimeRequest.getResourceId() != null ? downtimeRequest.getResourceId() : null);
            newDowntime.setWorkcenterId(downtimeRequest.getWorkcenterId() != null ? downtimeRequest.getWorkcenterId() : null);
            newDowntime.setSite(downtimeRequest.getSite() != null ? downtimeRequest.getSite() : null);
            newDowntime.setShiftId(shiftResponse == null ? null : StringUtils.isEmpty(shiftResponse.getShiftId()) ? null : shiftResponse.getShiftId());
            newDowntime.setShiftCreatedDateTime(shiftResponse == null ? null : shiftResponse.getShiftCreatedDatetime() == null ? null : shiftResponse.getShiftCreatedDatetime());
            newDowntime.setShiftBreakStartDatetime(shiftResponse == null ? null : shiftResponse.getBreakStartTime() == null ? null : String.valueOf(shiftResponse.getBreakStartTime()));
            newDowntime.setDowntimeDuration(downtimeDuration);
            newDowntime.setPlannedOperatingTime(shiftPlannedOperatingTime);
            newDowntime.setDowntimeStart(downtimeRequest.getDowntimeStart());
            newDowntime.setDowntimeEnd(downtimeRequest.getDowntimeEnd());
            newDowntime.setDowntEvent(downtimeRequest.getDowntEvent());
            newDowntime.setReason(downtimeRequest.getReason() != null ? downtimeRequest.getReason() : null);
            newDowntime.setRootCause(downtimeRequest.getRootCause() != null ? downtimeRequest.getRootCause() : null);
            newDowntime.setCommentUsr(downtimeRequest.getCommentUsr() != null ? downtimeRequest.getCommentUsr() : null);
            newDowntime.setDowntimeType(downtimeRequest.getDowntimeType());
            newDowntime.setIsActive(1);
            newDowntime.setIsOeeImpact(downtimeRequest.getDowntimeType().equalsIgnoreCase("unplanned") ? 1 : 0);
            newDowntime.setCreatedDatetime(LocalDateTime.now());
            newDowntime.setUpdatedDatetime(LocalDateTime.now());

            downtimeRepository.save(newDowntime);
        }

        return new DowntimeResponse("Downtime logged successfully", true);
    }*/
    @Override
    @Transactional
    public DowntimeCloseResponse closeActiveDowntime(String resourceId, String workcenterId) {
        if (resourceId == null || resourceId.isEmpty()) {
            throw new DowntimeException(1007);
        }
        if (workcenterId == null || workcenterId.isEmpty()) {
            throw new DowntimeException(1006);
        }
        List < Downtime > activeDowntimes = downtimeRepository.findActiveDowntimes(resourceId, workcenterId);

        if (activeDowntimes.isEmpty()) {
            return new DowntimeCloseResponse("No active downtime records found for the specified resource and workcenter", false, 0);
        }
        int updatedCount = downtimeRepository.closeActiveDowntimes(resourceId, workcenterId);

        return new DowntimeCloseResponse("Successfully closed active downtime records", true, updatedCount);

    }

    @Override
    public DowntimeResponse reopenDowntime(Long id) {
        Optional < Downtime > downtimeOpt = downtimeRepository.findById(id);

        if (!downtimeOpt.isPresent()) {
            return new DowntimeResponse("Downtime record not found", false);
        }

        Downtime downtime = downtimeOpt.get();

        if (downtime.getActive() == 1) {
            return new DowntimeResponse("Downtime is already active", false);
        }

        downtime.setActive(1);
        //        downtime.setUpdatedDatetime(new Timestamp(System.currentTimeMillis()));
        downtime.setUpdatedDatetime(LocalDateTime.now());

        downtimeRepository.save(downtime);

        return new DowntimeResponse("Downtime reopened successfully", true);
    }
    @Override
    public List < DowntimeResponseList > getDowntimeHistoryByRootCause(String rootCause) {
        if (rootCause == null || rootCause.isEmpty()) {
            throw new DowntimeException(1102);
        }
        List < Downtime > downtimes = downtimeRepository.findByRootCauseOrderByDowntimeStartDesc(rootCause);

        List < DowntimeResponseList > responses = new ArrayList < > ();
        for (Downtime downtime: downtimes) {
            responses.add(new DowntimeResponseList(
                    downtime.getId(),
                    downtime.getResourceId(),
                    downtime.getWorkcenterId(),
                    downtime.getShiftId(),
                    downtime.getDowntimeStart(),
                    downtime.getDowntimeEnd(),
                    downtime.getDowntimeDuration(),
                    downtime.getDowntimeType(),
                    downtime.getReason(),
                    downtime.getRootCause(),
                    downtime.getActive()));
        }
        return responses;
    }

    @Override
    public DowntimeBulkResponse bulkLogDowntime(List < DowntimeRequest > downtimeRequests) {

        for (DowntimeRequest request: downtimeRequests) {
            if (!validateRequest(request)) {
                return new DowntimeBulkResponse("Invalid request data", false, 0);
            }
        }
        int loggedCount = 0;
        for (DowntimeRequest request: downtimeRequests) {
            try {
                Downtime downtime = new Downtime();
                downtime.setResourceId(request.getResourceId());
                downtime.setWorkcenterId(request.getWorkcenterId());
                downtime.setDowntimeStart(request.getDowntimeStart());
                downtime.setDowntimeEnd(request.getDowntimeEnd());
                downtime.setDowntimeType(request.getDowntimeType());
                downtime.setReason(request.getReason());
                downtime.setRootCause(request.getRootCause());
                downtime.setCommentUsr(request.getCommentUsr());
                downtime.setDowntEvent(request.getDowntEvent());
                downtime.setActive(1);
                downtime.setSite(request.getSite());
                downtime.setIsOeeImpact(request.getDowntimeType().equalsIgnoreCase("unplanned") ? 1 : 0);
                downtime.setCreatedDatetime(LocalDateTime.now());

                if (request.getDowntEvent() != null && request.getDowntEvent().equals(1)) {
                    Pageable pageable = (Pageable) PageRequest.of(0, 1);
                    Page < Downtime > firstDownPage = downtimeRepository.findFirstDownEvent(
                            request.getResourceId(), request.getWorkcenterId(), pageable);

                    Optional < Downtime > firstDown = firstDownPage.stream().findFirst();

                    if (firstDown.isPresent()) {
                        downtime.setDowntimeDuration(calculateDowntimeDuration(
                                firstDown.get().getDowntimeStart(), request.getDowntimeEnd()));
                    }
                }
                downtimeRepository.save(downtime);
                loggedCount++;
            } catch (Exception e) {
                return new DowntimeBulkResponse("Error logging downtime: " + e.getMessage(), false, loggedCount);
            }
        }
        return new DowntimeBulkResponse("Bulk downtime logging successful", true, loggedCount);
    }

    private boolean validateRequest(DowntimeRequest request) {
        return request.getResourceId() != null && request.getWorkcenterId() != null &&
                request.getDowntEvent() != null;
    }

    private long calculateDowntimeDuration(LocalDateTime downtimeStart, LocalDateTime downtimeEnd) {
        if (downtimeStart == null || downtimeEnd == null) {
            return 0;
        }
        return Duration.between(downtimeEnd.toLocalTime(), downtimeStart.toLocalTime()).toMinutes();
    }

    @Override
    public List < DowntimeResponseList > getDowntimeHistoryByResource(String resourceId) {
        if (resourceId == null || resourceId.isEmpty()) {
            throw new DowntimeException(1007);
        }
        List < Downtime > downtimes = downtimeRepository.findByResourceIdOrderByDowntimeStartDesc(resourceId);
        List < DowntimeResponseList > responses = new ArrayList < > ();
        for (Downtime downtime: downtimes) {
            responses.add(new DowntimeResponseList(
                    downtime.getId(),
                    downtime.getResourceId(),
                    downtime.getWorkcenterId(),
                    downtime.getShiftId(),
                    downtime.getDowntimeStart(),
                    downtime.getDowntimeEnd(),
                    downtime.getDowntimeDuration(),
                    downtime.getDowntimeType(),
                    downtime.getReason(),
                    downtime.getRootCause(),
                    downtime.getActive()
            ));
        }
        return responses;
    }



    @Override
    public List < DowntimeResponseList > getDowntimeHistoryByWorkcenter(String workcenterId) {
        if (workcenterId == null || workcenterId.isEmpty()) {
            throw new DowntimeException(1006);
        }
        List < Downtime > downtimes = downtimeRepository.findByWorkcenterIdOrderByDowntimeStartDesc(workcenterId);
        List < DowntimeResponseList > responses = new ArrayList < > ();
        for (Downtime downtime: downtimes) {
            responses.add(new DowntimeResponseList(
                    downtime.getId(),
                    downtime.getResourceId(),
                    downtime.getWorkcenterId(),
                    downtime.getShiftId(),
                    downtime.getDowntimeStart(),
                    downtime.getDowntimeEnd(),
                    downtime.getDowntimeDuration(),
                    downtime.getDowntimeType(),
                    downtime.getReason(),
                    downtime.getRootCause(),
                    downtime.getActive()
            ));
        }

        return responses;
    }

    @Override
    public List < DowntimeResponseList > getDowntimeHistoryByShift(String shiftId) {
        if (shiftId == null || shiftId.isEmpty()) {
            throw new DowntimeException(1008);
        }
        List < Downtime > downtimes = downtimeRepository.findByShiftIdOrderByDowntimeStartDesc(shiftId);
        List < DowntimeResponseList > responses = new ArrayList < > ();
        for (Downtime downtime: downtimes) {
            responses.add(new DowntimeResponseList(
                    downtime.getId(),
                    downtime.getResourceId(),
                    downtime.getWorkcenterId(),
                    downtime.getShiftId(),
                    downtime.getDowntimeStart(),
                    downtime.getDowntimeEnd(),
                    downtime.getDowntimeDuration(),
                    downtime.getDowntimeType(),
                    downtime.getReason(),
                    downtime.getRootCause(),
                    downtime.getActive()
            ));
        }

        return responses;
    }

    @Override
    public List < DowntimeResponseList > getDowntimeHistoryByReason(String reason) {
        if (reason == null || reason.isEmpty()) {
            throw new DowntimeException(1012);
        }
        List < Downtime > downtimes = downtimeRepository.findByReasonOrderByDowntimeStartDesc(reason);
        List < DowntimeResponseList > responses = new ArrayList < > ();
        for (Downtime downtime: downtimes) {
            responses.add(new DowntimeResponseList(
                    downtime.getId(),
                    downtime.getResourceId(),
                    downtime.getWorkcenterId(),
                    downtime.getShiftId(),
                    downtime.getDowntimeStart(),
                    downtime.getDowntimeEnd(),
                    downtime.getDowntimeDuration(),
                    downtime.getDowntimeType(),
                    downtime.getReason(),
                    downtime.getRootCause(),
                    downtime.getActive()
            ));
        }
        return responses;
    }

    @Override
    public List < DowntimeResponseList > getDowntimeHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate.isBefore(startDate)) {
            throw new DowntimeException(1103);
        }
        List < Downtime > downtimes = downtimeRepository.findByDowntimeStartBetweenOrderByDowntimeStartDesc(startDate, endDate);

        List < DowntimeResponseList > responses = new ArrayList < > ();
        for (Downtime downtime: downtimes) {
            responses.add(new DowntimeResponseList(
                    downtime.getId(),
                    downtime.getResourceId(),
                    downtime.getWorkcenterId(),
                    downtime.getShiftId(),
                    downtime.getDowntimeStart(),
                    downtime.getDowntimeEnd(),
                    downtime.getDowntimeDuration(),
                    downtime.getDowntimeType(),
                    downtime.getReason(),
                    downtime.getRootCause(),
                    downtime.getActive()));
        }
        return responses;
    }


    @Override
    public List < DowntimeResponseList > getDowntimeHistoryByResourceAndDateRange(String resourceId, LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate.isBefore(startDate)) {
            throw new DowntimeException(1103);
        }
        List < Downtime > downtimes = downtimeRepository.findByResourceIdAndDowntimeStartBetweenOrderByDowntimeStartDesc(resourceId, startDate, endDate);

        List < DowntimeResponseList > responses = new ArrayList < > ();
        for (Downtime downtime: downtimes) {
            responses.add(new DowntimeResponseList(
                    downtime.getId(),
                    downtime.getResourceId(),
                    downtime.getWorkcenterId(),
                    downtime.getShiftId(),
                    downtime.getDowntimeStart(),
                    downtime.getDowntimeEnd(),
                    downtime.getDowntimeDuration(),
                    downtime.getDowntimeType(),
                    downtime.getReason(),
                    downtime.getRootCause(),
                    downtime.getActive()
            ));
        }
        return responses;
    }
    @Override
    public List<Downtime> getTotalDowntimeList(AvailabilityRequestForDowntime request) {
        List<Downtime> downTimeList=new ArrayList<Downtime>();
        List<String> shiftIds = new ArrayList<>();
// Build the base query for total downtime
        StringBuilder totalDowntimeQuery = new StringBuilder(
                "SELECT resource_id, workcenter_id, site, shift_id, "
                        + "COALESCE(CAST(SUM(EXTRACT(EPOCH FROM LEAST(downtime_end, ?) - GREATEST(downtime_start, ?)) / 60) AS DECIMAL(10,2)), 0.00) AS total_downtime "
                        + "FROM r_downtime "
                        + "WHERE downtime_type = 'unplanned' "
                        + "AND downtime_end >= ? "
                        + "AND downtime_start <= ? "
                        + "AND active = 1 ");

        // Default to current time if start and end time are not provided
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : now.minusHours(1);
        LocalDateTime endTime = request.getEndTime() != null ? request.getEndTime() : now;

        // Convert LocalDateTime to SQL Timestamp
        Timestamp startTimeStr = Timestamp.valueOf(startTime);
        Timestamp endTimeStr = Timestamp.valueOf(endTime);

        // Extract other parameters
        String resourceId = request.getResourceId();
        String workcenterId = request.getWorkcenterId();
        String site = request.getSite();

        // Add conditions dynamically based on whether values are provided
        List<Object> queryParams = new ArrayList<>();
        queryParams.add(endTimeStr);  // downtime_end upper bound
        queryParams.add(startTimeStr); // downtime_start lower bound
        queryParams.add(startTimeStr); // downtime_start lower bound
        queryParams.add(endTimeStr);   // downtime_end upper bound

        if (resourceId != null) {
            totalDowntimeQuery.append("AND resource_id = ? ");
            queryParams.add(resourceId);
        }

        if (workcenterId != null) {
            totalDowntimeQuery.append("AND workcenter_id = ? ");
            queryParams.add(workcenterId);
        }

        if (site != null) {
            totalDowntimeQuery.append("AND site = ? ");
            queryParams.add(site);
        }
        totalDowntimeQuery.append("GROUP BY resource_id, workcenter_id, site, shift_id;");
        String finalQuery = totalDowntimeQuery.toString();
        List<Map<String, Object>> downtimeList = null;
        try {
            downtimeList = jdbcTemplate.queryForList(finalQuery, queryParams.toArray());

            for (Map<String, Object> downtimeRecord : downtimeList) {
                 resourceId = (String) downtimeRecord.get("resource_id");
                 workcenterId = (String) downtimeRecord.get("workcenter_id");
                 site = (String) downtimeRecord.get("site");
                String shiftId = (String) downtimeRecord.get("shift_id");
                BigDecimal totalDowntime = (BigDecimal) downtimeRecord.get("total_downtime");
                Downtime downtime = new Downtime();
                downtime.setResourceId(resourceId);
                downtime.setWorkcenterId(workcenterId);
                downtime.setSite(site);
                downtime.setShiftId(shiftId);
                downtime.setDowntimeDuration(totalDowntime.longValue());
                downTimeList.add(downtime);
                 shiftId = (String) downtimeRecord.get("shift_id");
                shiftIds.add(shiftId);
            }

        } catch (DataAccessException e) {
            // Catch DataAccessException and log it
            System.err.println("Data access error: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other general exceptions
            System.err.println("An error occurred: " + e.getMessage());
        }

        // Calculate total downtime
        Double totalDowntime =null;

        if (downtimeList != null && !downtimeList.isEmpty()) {
            // Extract the total downtime from the query result
            for (Map<String, Object> row : downtimeList) {
           /*     totalDowntime = (Double) row.get("total_downtime");*/
            }
            if (totalDowntime != null) {

            } else {
                // Handle dynamic break if provided
                Long breakDuration = 0L;
                if (request.getDynamicBreak() != 0) {
                    // Make API call for dynamic break duration
                    AvailabilityRequestForDowntime breakDurationRequest = AvailabilityRequestForDowntime.builder()
                            .dynamicBreak(request.getDynamicBreak())
                            .build();

                    List<Break> breakDurations = webClientBuilder.build()
                            .post()
                            .uri(breakDurationQueryUrl)
                            .bodyValue(breakDurationRequest)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<Break>>() {})  // Maps the response to List<Break>
                            .block();
                } else {
                    //

                    // Make API call for break duration based on shift IDs
                    DowntimeRequestForShift breakDurationRequest = DowntimeRequestForShift.builder()
                            .site(request.getSite())
                            .downtimeEnd(request.getEndTime())
                            .downtimeStart(request.getStartTime())
                            .shiftIds(shiftIds)
                            .build();

                    List<Break> breakDurations = webClientBuilder.build()
                            .post()
                            .uri(breakDurationQueryUrl)
                            .bodyValue(breakDurationRequest)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<Break>>() {})  // Maps the response to List<Break>
                            .block();

                    for (Break aBreak : breakDurations) {
                        for (Downtime downtime : downTimeList) {}/*{
                            String[] parts = aBreak.getShiftRef.split(",");
                            String shiftid = parts[parts.length - 1];
                            // Check if the downtime and break match based on shiftId, resourceId, workcenterId, etc.
                            if (downtime.getShiftId().equals(shiftid)) {

                                // Get the break duration in minutes
                                int breakMinutes = breakDuration.getMeanTime();

                                // Get the total downtime (ensure it's in minutes)
                                Long totalDowntime = downtime.getDowntimeDuration();

                                // Subtract break duration from total downtime
                                Long adjustedDowntime = totalDowntime - breakMinutes;

                                // Set the adjusted downtime in the Downtime object
                                downtime.setDowntimeDuration(adjustedDowntime);

                                // Optionally, you can log or perform other operations here
                                System.out.println("Adjusted Downtime for " + downtime.getResourceId() + " on shift " + downtime.getShiftId() + " is: " + adjustedDowntime);
                            }
                        }*/
                    }
                }
            }
        }
        return downTimeList;
    }

    @Override
    public Double getTotalDowntime(AvailabilityRequestForDowntime request) {

        String totalDowntimeQuery = "SELECT COALESCE(" +
                "    CAST(SUM(" +
                "        EXTRACT(EPOCH FROM LEAST(downtime_end, ?) - GREATEST(downtime_start, ?)) / 60" +
                "    ) AS DECIMAL(10,2)), " +
                "    0.00" +
                ") AS total_downtime " +
                "FROM r_downtime " +
                "WHERE resource_id = ? " +
                "  AND workcenter_id = ? " +
                "  AND site = ? " +
                "  AND downtime_type = 'unplanned' " +
                "  AND downtime_end >= ? " +
                "  AND downtime_start <= ? " +
                "  AND is_active = 0";

        Double totalDowntime = jdbcTemplate.queryForObject(
                totalDowntimeQuery,
                new Object[]{request.getEndTime(), request.getStartTime(), request.getResourceId(), request.getWorkcenterId(), request.getSite(), request.getStartTime(), request.getEndTime()},
                Double.class);

        Long breakDuration;
        if(totalDowntime == 0.0) {
            return totalDowntime;
        }else {
            if (request.getDynamicBreak() != 0) {

                AvailabilityRequestForDowntime breakDurationRequest = AvailabilityRequestForDowntime.builder()
                        .dynamicBreak(request.getDynamicBreak())
                        .build();

                breakDuration = webClientBuilder.build()
                        .post()
                        .uri(dynamicBreakDurationQueryUrl)
                        .bodyValue(breakDurationRequest)
                        .retrieve()
                        .bodyToMono(Long.class)
                        .block();

            } else {

                List<String> shiftIds = (request.getShiftId() != null)
                        ? List.of(request.getShiftId())
                        : request.getShiftIds();

                DowntimeRequestForShift breakDurationRequest = DowntimeRequestForShift.builder()
                        .site(request.getSite())
                        .downtimeEnd(request.getEndTime())
                        .downtimeStart(request.getStartTime())
                        .shiftIds(shiftIds)
                        .build();

                breakDuration = webClientBuilder.build()
                        .post()
                        .uri(breakDurationQueryUrl)
                        .bodyValue(breakDurationRequest)
                        .retrieve()
                        .bodyToMono(Long.class)
                        .block();
            }
        }
        return totalDowntime - (double)breakDuration;
    }


    @Override
    public Long getDynamicBreakDuration(int dynamicBreak) {

        final int constantDynamicBreak = 30;
        Long breakDuration = 0L;

        if (dynamicBreak != 0) {

            breakDuration = (long) Math.min(dynamicBreak, constantDynamicBreak);
        }
        return breakDuration;
    }
    @Override
    public List<Downtime> getDowntimeSummary(String site, List<String> resourceList,
                                             LocalDateTime startDateTime, LocalDateTime endDateTime,
                                             String workcenter, String shift) {
        return downtimeRepository.findDowntimeSummary(site, resourceList, workcenter, shift);
    }

    @Override
    public List<Downtime> getDowntime(String site, List<String> resourceList,
                                             LocalDateTime downtimeStart, LocalDateTime downtimeEnd) {
        return downtimeRepository.findDowntime(site, resourceList,downtimeStart, downtimeEnd);
    }
//    @Override
//    public OverallDowntimeResponse getOverallDowntime(OeeFilterRequest request) {
//        // Custom logic to retrieve overall downtime
//        List<Downtime> downtimeList = downtimeRepository.findOverallDowntime(request);
//        return OverallDowntimeResponse.builder().startTime(request.getStartTime()).endTime(request.getEndTime()).resourceId(downtimeList.get(0).getResourceId()).downtimeDuration(downtimeList.get(0).getDowntimeDuration()).build();
//    }

    @Override
    public OverallDowntimeResponse getOverallDowntime(OeeFilterRequest request) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());
        queryBuilder.append("SELECT SUM(d.downtime_duration) AS totalDowntime ")
                .append("FROM r_downtime d ")
                .append("WHERE d.site = :site ");
        applyCommonFilters(queryBuilder, queryParameters, request);
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.minusHours(24);
            request.setStartTime(startTime);
            request.setEndTime(now);
        }

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);
            BigDecimal totalDowntime = (BigDecimal) query.getSingleResult();
            long downtimeDuration = (totalDowntime != null) ? totalDowntime.longValue() : 0L;

            List<OverallDowntimeResponse.OverallDowntime> downtimeDurations = new ArrayList<>();

//            long totalDuration = ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime());
            long totalDuration = (request.getStartTime() != null && request.getEndTime() != null)
                    ? ChronoUnit.SECONDS.between(request.getStartTime(), request.getEndTime())
                    : 0;

            downtimeDurations.add(new OverallDowntimeResponse.OverallDowntime("TotalDuration", totalDuration));

            downtimeDurations.add(new OverallDowntimeResponse.OverallDowntime("TotalDowntime", downtimeDuration));

            return OverallDowntimeResponse.builder()
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .downtimeDurations(downtimeDurations)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public DowntimeRetResponse.DowntimeOverTime getDowntimeOverTime(OeeFilterRequest request) {
//        // Custom logic to retrieve downtime over time
//        List<Downtime> downtimeList = downtimeRepository.findDowntimeOverTime(request);
//        return DowntimeRetResponse.DowntimeOverTime.builder().date(downtimeList.get(0).getCreatedDatetime().toString()).downtimeDuration(downtimeList.get(0).getDowntimeDuration()).build();
//    }

    @Override
    public DowntimeRetResponse getDowntimeOverTime(OeeFilterRequest request) {// r_availability
        // Custom logic to retrieve downtime over time
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT " +
                        "d.resource_id AS resourceId, " +
                        "SUM(d.downtime) AS downtime, " +
                        "d.interval_start_date_time AS interval_start_date_time " +
                        "FROM r_availability d " +
                        "WHERE d.site = :site " +
                        "AND d.downtime > 0 ");

        applyCommonFilters1(queryBuilder, queryParameters, request);

        queryBuilder.append(" GROUP BY d.resource_id, d.interval_start_date_time");
        queryBuilder.append(" ORDER BY interval_start_date_time");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            DowntimeRetResponse response = new DowntimeRetResponse();
//            List<DowntimeRetResponse.DowntimeOverTime> downtimeByTimeList = results.stream()
//                    .filter(result -> result[0] != null)
//                    .map(result -> {
//                        double downtimeMinutes = result[1] != null ? ((Number) result[1]).doubleValue() / 60 : 0;
//                        if (downtimeMinutes <= 5) {
//                            return null; // skip this entry if 5 minutes or less
//                        }
//                        DowntimeRetResponse.DowntimeOverTime downtimeOverTime = new DowntimeRetResponse.DowntimeOverTime();
//                        downtimeOverTime.setResourceId((String) result[0]);  // Set resourceId
//                        BigDecimal roundedDowntime = BigDecimal.valueOf(downtimeMinutes).setScale(2, RoundingMode.HALF_UP);
//                        downtimeOverTime.setDowntimeDuration(roundedDowntime.doubleValue());
//                        Timestamp timestamp = (Timestamp) result[2];
//                        if (timestamp != null) {
//                            downtimeOverTime.setOccurredAt(timestamp.toLocalDateTime());
//                        }
//                        return downtimeOverTime;
//                    })
//                    .collect(Collectors.toList());
//
//            // Setting the downtime data to response
//            response.setDowntimeOverTime(downtimeByTimeList);
//
//            return response;

            LocalDate requestStartDate = request.getStartTime().toLocalDate();
            LocalDate requestEndDate = request.getEndTime().toLocalDate();

            boolean isSameDay = requestStartDate.equals(requestEndDate);
            String summaryKey = isSameDay
                    ? requestStartDate.atStartOfDay().toString()           // e.g., "2025-04-22T00:00"
                    : requestStartDate.toString();                         // e.g., "2025-04-22"

            Map<String, DowntimeRetResponse.DowntimeOverTime> grouped = new LinkedHashMap<>();

            for (Object[] row : results) {
                String resourceId = (String) row[0];
                double downtimeMinutes = row[1] != null ? ((Number) row[1]).doubleValue() / 60 : 0;

                if (resourceId == null || downtimeMinutes <= 5) continue;

                Timestamp ts = (Timestamp) row[2];
                if (ts == null) continue;

                LocalDateTime intervalTime = ts.toLocalDateTime();
                String timestampKey = isSameDay
                        ? intervalTime.toString()                         // "2025-04-22T08:00"
                        : summaryKey;                                     // "2025-04-22"

                double rounded = BigDecimal.valueOf(downtimeMinutes).setScale(2, RoundingMode.HALF_UP).doubleValue();

                DowntimeRetResponse.DowntimeOverTime dto = grouped.computeIfAbsent(resourceId, r -> {
                    DowntimeRetResponse.DowntimeOverTime obj = new DowntimeRetResponse.DowntimeOverTime();
                    obj.setResourceId(r);
                    obj.setDowntime(new LinkedHashMap<>());
                    return obj;
                });

                dto.getDowntime().merge(timestampKey, rounded, (oldVal, newVal) ->
                        BigDecimal.valueOf(oldVal + newVal).setScale(2, RoundingMode.HALF_UP).doubleValue()
                );
            }

// Final response
            List<DowntimeRetResponse.DowntimeOverTime> downtimeByTimeList = new ArrayList<>(grouped.values());
            response.setDowntimeOverTime(downtimeByTimeList);

            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//    @Override
//    public DowntimeRetResponse.DowntimeByReason getDowntimeByReason(OeeFilterRequest request) {
//        // Custom logic to retrieve downtime by reason
//        List<Downtime> downtimeList = downtimeRepository.findDowntimeByReason(request);
//        return DowntimeRetResponse.DowntimeByReason.builder().reasonCode(downtimeList.get(0).getReason()).downtimeDuration(downtimeList.get(0).getDowntimeDuration()).build();
//    }

    @Override
    public DowntimeRetResponse getDowntimeByReason(OeeFilterRequest request) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT d.reason AS reasonCode, SUM(d.downtime_duration) AS totalDowntimeDuration ")
                .append("FROM r_downtime d ")
                .append("WHERE d.site = :site ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("GROUP BY d.reason ORDER BY totalDowntimeDuration DESC");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            DowntimeRetResponse response = new DowntimeRetResponse();
            List<DowntimeRetResponse.DowntimeByReason> downtimeByReasonList = results.stream()
                    .map(result -> {
                        DowntimeRetResponse.DowntimeByReason downtimeByReason = new DowntimeRetResponse.DowntimeByReason();
                        downtimeByReason.setReasonCode(result[0] != null ? (String) result[0] : "Unknown");
                        long totalDowntimeDuration = (result[1] != null && result[1] instanceof BigDecimal)
                                ? ((BigDecimal) result[1]).longValue() / 60
                                : 0;

                        // Only include entries with non-zero downtime duration
                        if (totalDowntimeDuration == 0) {
                            return null;
                        }

                        downtimeByReason.setDowntimeDuration(totalDowntimeDuration);
                        return downtimeByReason;
                    })
                    .filter(Objects::nonNull) // Filter out records with zero downtime
                    .collect(Collectors.toList());

            // Assign collected data to the response object
            response.setDowntimeByReason(downtimeByReasonList);

            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public DowntimeRetResponse.DowntimeByMachine getDowntimeByMachine(OeeFilterRequest request) {
//        // Custom logic to retrieve downtime by machine
//        List<Downtime> downtimeList = downtimeRepository.findDowntimeByMachine(request);
//        return DowntimeRetResponse.DowntimeByMachine.builder().machineId(downtimeList.get(0).getResourceId()).downtimeDuration(downtimeList.get(0).getDowntimeDuration()).build();
//    }

    @Override
    public DowntimeRetResponse getDowntimeByMachine(OeeFilterRequest request) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT d.resource_id AS machineId, SUM(d.downtime_duration) AS totalDowntimeDuration ")
                .append("FROM r_downtime d ")
                .append("WHERE d.site = :site ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("GROUP BY d.resource_id ORDER BY totalDowntimeDuration DESC");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            DowntimeRetResponse response = new DowntimeRetResponse();
            List<DowntimeRetResponse.DowntimeByMachine> downtimeByMachineList = results.stream()
                    .map(result -> {
                        DowntimeRetResponse.DowntimeByMachine downtimeByMachine = new DowntimeRetResponse.DowntimeByMachine();
                        downtimeByMachine.setMachineId(result[0] != null ? (String) result[0] : "");
                        downtimeByMachine.setDowntimeDuration(
                                result[1] != null && result[1] instanceof BigDecimal
                                        ? ((BigDecimal) result[1]).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP).longValue()
                                        : 0L
                        );
                        return downtimeByMachine;
                    })
                    .collect(Collectors.toList());

            // Assign collected data to the response object
            response.setDowntimeByMachine(downtimeByMachineList);

            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DowntimeRetResponse getCumulativeDowntime(OeeFilterRequest request) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT DATE(d.created_datetime) AS date, " + // Grouping by whole day
                        "SUM(d.downtime_duration) AS totalDowntime " +
                        "FROM r_downtime d " +
                        "WHERE d.site = :site " +
                        "AND d.downtime_duration IS NOT NULL ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("GROUP BY DATE(d.created_datetime) " +  // Grouping by date only (ignoring time)
                "ORDER BY DATE(d.created_datetime)");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            DowntimeRetResponse response = new DowntimeRetResponse();
            List<DowntimeRetResponse.CumulativeDowntime> downtimeByTimeList = results.stream()
                    .map(result -> {
                        DowntimeRetResponse.CumulativeDowntime downtimeOverTime = new DowntimeRetResponse.CumulativeDowntime();
                        java.sql.Date date = (java.sql.Date) result[0];
                        downtimeOverTime.setDate(date.toString());  // Setting Date (YYYY-MM-DD)
                        downtimeOverTime.setCumulativeDowntimeDuration(
                                result[1] != null
                                        ? ((BigDecimal) result[1])
                                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                                        .longValue()
                                        : 0L
                        );
                        return downtimeOverTime;
                    })
                    .collect(Collectors.toList());

            // Setting the downtime data to response
            response.setCumulativeDowntime(downtimeByTimeList);
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public DowntimeRetResponse.DowntimeVsProductionOutput getDowntimeVsProductionOutput(OeeFilterRequest request) {
//        // Custom logic to retrieve downtime vs production output
//        List<Downtime> downtimeList = downtimeRepository.findDowntimeVsProductionOutput(request);
//        return DowntimeRetResponse.DowntimeVsProductionOutput.builder().timePeriod(downtimeList.get(0).getCreatedDatetime().toString()).downtimeDuration(downtimeList.get(0).getDowntimeDuration()).productionOutput(downtimeList.get(0).getPlannedOperatingTime()).build();
//    }

    @Override
    public DowntimeRetResponse getDowntimeVsProductionOutput(OeeFilterRequest request) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT d.resource_id AS resourceId, ")
                .append("d.totalDowntime, ")
                .append("COALESCE(ct.avg_planned_cycle_time, 0) AS plannedCycleTime ")
                .append("FROM ( ")
                .append("    SELECT resource_id, ")
                .append("           SUM(downtime_duration) AS totalDowntime ")
                .append("    FROM r_downtime d ")
                .append("    WHERE site = :site ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("    GROUP BY resource_id ")
                .append(") d ")
                .append("LEFT JOIN ( ")
                .append("    SELECT resource_id, ")
                .append("           AVG(planned_cycle_time) AS avg_planned_cycle_time ")
                .append("    FROM r_cycle_time ")
                .append("    WHERE site = :site ")
                .append("    GROUP BY resource_id ")
                .append(") ct ON d.resource_id = ct.resource_id ")
                .append("ORDER BY d.resource_id");
        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            DowntimeRetResponse response = new DowntimeRetResponse();
            List<DowntimeRetResponse.DowntimeVsProductionOutput> downtimeVsProductionOutputList = results.stream()
                    .map(result -> {
                        DowntimeRetResponse.DowntimeVsProductionOutput downtimeVsProductionOutput = new DowntimeRetResponse.DowntimeVsProductionOutput();

                        String resourceId = (result[0] != null) ? result[0].toString() : "";
                        downtimeVsProductionOutput.setResourceId(resourceId);

                        // Retrieve and round total downtime duration
                        long totalDowntimeDuration = result[1] != null && result[1] instanceof BigDecimal
                                ? ((BigDecimal) result[1])
                                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                                .longValue()
                                : 0L;
                        downtimeVsProductionOutput.setDowntime(totalDowntimeDuration);

                        double plannedCycleTime = result[2] != null ? ((double) result[2]) / 60 : 0.0;

                        double productionOutput = plannedCycleTime > 0
                                ? Math.round(totalDowntimeDuration / plannedCycleTime)
                                : 0.0;

                        downtimeVsProductionOutput.setProductionOutput(productionOutput);

                        // Only return the object if any value is non-zero
                        return (productionOutput != 0.0 || totalDowntimeDuration != 0.0) ? downtimeVsProductionOutput : null;
                    })
                    .filter(Objects::nonNull) // Remove entries where values are zero
                    .collect(Collectors.toList());

            // Assign collected data to the response object
            response.setDowntimeVsProductionOutput(downtimeVsProductionOutputList);

            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //    public DowntimeRetResponse getDowntimeImpact(OeeFilterRequest request) {
//        // Custom logic to retrieve downtime impact
//        List<Downtime> downtimeList = downtimeRepository.findDowntimeImpact(request);
//        return DowntimeRetResponse.DowntimeImpact.builder().impactType(downtimeList.get(0).getDowntimeType()).impactValue(downtimeList.get(0).getIsOeeImpact()).build();
//    }
    @Override
    public DowntimeRetResponse getDowntimeImpact(OeeFilterRequest request) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());
        queryBuilder.append("SELECT d.root_cause AS rootCause, ")
                .append("SUM(COALESCE(d.is_oee_impact, 0)) AS totalOeeImpact ")
                .append("FROM r_downtime d ")
                .append("WHERE d.site = :site ");
        applyCommonFilters(queryBuilder, queryParameters, request);
        queryBuilder.append("GROUP BY d.root_cause ORDER BY rootCause ASC");
        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);
            List<Object[]> results = query.getResultList();
            DowntimeRetResponse response = new DowntimeRetResponse();
            List<DowntimeRetResponse.DowntimeImpact> downtimeImpactList = results.stream()
                    .map(result -> {
                        DowntimeRetResponse.DowntimeImpact downtimeImpact = new DowntimeRetResponse.DowntimeImpact();
                        downtimeImpact.setImpactType(result[0] != null ? result[0].toString() : "");
                        downtimeImpact.setImpactValue(result[1] != null ?
                                (result[1] instanceof BigInteger ? ((BigInteger) result[1]).doubleValue() :
                                        result[1] instanceof BigDecimal ? ((BigDecimal) result[1]).doubleValue() :
                                                ((Number) result[1]).doubleValue()) : 0.0);
                        return downtimeImpact;
                    })
                    .collect(Collectors.toList());
            // Assign collected data to the response object
            response.setDowntimeImpact(downtimeImpactList);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DowntimeRetResponse getDowntimeDurationDistribution(OeeFilterRequest request) {
        // Custom logic to retrieve downtime duration distribution
        List<Downtime> downtimeList = downtimeRepository.findDowntimeDurationDistribution(request);
        return mapToDowntimeRetResponse(downtimeList);
    }

//    @Override
//    public DowntimeRetResponse.DowntimeAnalysis getDowntimeAnalysis(OeeFilterRequest request) {
//        // Custom logic to retrieve downtime analysis
//        List<Downtime> downtimeList = downtimeRepository.findDowntimeAnalysis(request);
//        return DowntimeRetResponse.DowntimeAnalysis.builder().reason(downtimeList.get(0).getReason()).rootCause(downtimeList.get(0).getRootCause()).downtimeDuration(downtimeList.get(0).getDowntimeDuration()).build();
//    }

    @Override
    public DowntimeRetResponse getDowntimeAnalysis(OeeFilterRequest request) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT d.reason, d.resource_id, COUNT(*) AS occurrences ")
                .append("FROM r_downtime d ")
                .append("WHERE d.site = :site ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("GROUP BY d.reason, d.resource_id ORDER BY occurrences DESC");

        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            DowntimeRetResponse response = new DowntimeRetResponse();
            List<DowntimeRetResponse.DowntimeAnalysis> downtimeAnalysisList = results.stream()
                    .map(result -> {
                        DowntimeRetResponse.DowntimeAnalysis downtimeAnalysis = new DowntimeRetResponse.DowntimeAnalysis();
                        downtimeAnalysis.setReason(result[0] != null ? (String) result[0] : "Unknown");
                        downtimeAnalysis.setMachine(result[1] != null ? (String) result[1] : "");
                        downtimeAnalysis.setOccurrences(result[2] != null ? ((Number) result[2]).intValue() : 0);
                        return downtimeAnalysis;
                    })
                    // Filter out entries where occurrences is 0
                    .filter(downtimeAnalysis -> downtimeAnalysis.getOccurrences() > 0)
                    .collect(Collectors.toList());

            // Assign collected data to the response object
            response.setDowntimeAnalysis(downtimeAnalysisList);

            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public DowntimeRetResponse getDowntimeByReasonAndShift(OeeFilterRequest request){
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("site", request.getSite());

        queryBuilder.append("SELECT d.shift_id AS shiftId, d.reason AS reasonCode, SUM(d.downtime_duration) AS totalDowntimeDuration ")
                .append("FROM r_downtime d ")
                .append("WHERE d.site = :site ");

        applyCommonFilters(queryBuilder, queryParameters, request);

        queryBuilder.append("GROUP BY d.reason, d.shift_id ORDER BY totalDowntimeDuration DESC");


        try {
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            DowntimeRetResponse response = new DowntimeRetResponse();
            List<DowntimeRetResponse.DowntimeByReasonAndShift> downtimeByReasonAndShiftList = results.stream()
                    .map(result -> {
                        DowntimeRetResponse.DowntimeByReasonAndShift downtimeByReasonAndShift = new DowntimeRetResponse.DowntimeByReasonAndShift();
                        downtimeByReasonAndShift.setShiftId(result[0] != null ? BOConverter.getShift((String) result[0]) : "");
                        downtimeByReasonAndShift.setReasonCode(result[1] != null ? (String) result[1] : "");
                        downtimeByReasonAndShift.setDowntimeDuration(
                                result[2] != null && result[2] instanceof BigDecimal
                                        ? ((BigDecimal) result[2])
                                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                                        .longValue()
                                        : 0L
                        );
                        return downtimeByReasonAndShift;
                    })
                    .collect(Collectors.toList());

            // Assign collected data to the response object
            response.setDowntimeByReasonAndShift(downtimeByReasonAndShiftList);

            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void applyCommonFilters(StringBuilder queryBuilder, Map<String, Object> queryParameters, OeeFilterRequest request) {
        // Handle start and end time filter with overlap logic using >= and <=
        if (request.getStartTime() == null || request.getEndTime() == null) {
            // Default to the last 24 hours if no time range is provided
            LocalDateTime now = LocalDateTime.now();
            //LocalDateTime startTime = now.minusHours(24);
            ShiftRequest shiftreq= new ShiftRequest();
            shiftreq.setSite(request.getSite());
            LocalDateTime startTime=  oeeService.getEarliestValidShiftStartDateTime(shiftreq);

            queryParameters.put("startTime", startTime);
            queryParameters.put("endTime", now);
            request.setStartTime(startTime);
            request.setEndTime(now);
            queryBuilder.append("AND (d.downtime_start >= :startTime AND d.downtime_start <= :endTime OR d.downtime_end >= :startTime AND d.downtime_end <= :endTime) ");
        } else {
            queryParameters.put("startTime", request.getStartTime());
            queryParameters.put("endTime", request.getEndTime());
            queryBuilder.append("AND (d.downtime_start >= :startTime AND d.downtime_start <= :endTime OR d.downtime_end >= :startTime AND d.downtime_end <= :endTime) ");
        }

        // Handle resource ID filter
        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            queryBuilder.append("AND d.resource_id IN :resourceId ");
            queryParameters.put("resourceId", request.getResourceId());
        }

        // Handle shift ID filter
        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            queryBuilder.append("AND d.shift_id IN :shiftId ");
            queryParameters.put("shiftId", request.getShiftId());
        }

        // Handle workcenter ID filter
        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            queryBuilder.append("AND d.workcenter_id IN :workcenterId ");
            queryParameters.put("workcenterId", request.getWorkcenterId());
        }


//        // Handle batch number filter
//        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
//            queryBuilder.append("AND d.batch_number IN :batchNumber ");
//            queryParameters.put("batchNumber", request.getBatchNumber());
//        }
    }

    private void applyCommonFilters1(StringBuilder queryBuilder, Map<String, Object> queryParameters, OeeFilterRequest request) {
        // Handle start and end time filter with overlap logic using >= and <=
        if (request.getStartTime() == null || request.getEndTime() == null) {
            // Default to the last 24 hours if no time range is provided
            LocalDateTime now = LocalDateTime.now();
            //LocalDateTime startTime = now.minusHours(24);
            ShiftRequest shiftreq= new ShiftRequest();
            shiftreq.setSite(request.getSite());
            LocalDateTime startTime=  oeeService.getEarliestValidShiftStartDateTime(shiftreq);

            queryParameters.put("startTime", startTime);
            queryParameters.put("endTime", now);
            request.setStartTime(startTime);
            request.setEndTime(now);
            queryBuilder.append("AND (d.interval_start_date_time >= :startTime AND d.interval_start_date_time <= :endTime OR d.interval_end_date_time >= :startTime AND d.interval_end_date_time <= :endTime) ");
        } else {
            queryParameters.put("startTime", request.getStartTime());
            queryParameters.put("endTime", request.getEndTime());
            queryBuilder.append("AND (d.interval_start_date_time >= :startTime AND d.interval_start_date_time <= :endTime OR d.interval_end_date_time >= :startTime AND d.interval_end_date_time <= :endTime) ");
        }

        // Handle resource ID filter
        if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
            queryBuilder.append("AND d.resource_id IN :resourceId ");
            queryParameters.put("resourceId", request.getResourceId());
        }

        // Handle shift ID filter
        if (request.getShiftId() != null && !request.getShiftId().isEmpty()) {
            queryBuilder.append("AND d.shift_id IN :shiftId ");
            queryParameters.put("shiftId", request.getShiftId());
        }

        // Handle workcenter ID filter
        if (request.getWorkcenterId() != null && !request.getWorkcenterId().isEmpty()) {
            queryBuilder.append("AND d.workcenter_id IN :workcenterId ");
            queryParameters.put("workcenterId", request.getWorkcenterId());
        }


//        // Handle batch number filter
//        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
//            queryBuilder.append("AND d.batch_number IN :batchNumber ");
//            queryParameters.put("batchNumber", request.getBatchNumber());
//        }
    }


    private DowntimeRetResponse mapToDowntimeRetResponse(List<Downtime> downtimeList) {
        // Map the downtimeList to DowntimeRetResponse
        DowntimeRetResponse response = new DowntimeRetResponse();

        // Set the start time and end time if needed
        // Assuming we're using the request start and end times.
        response.setStartTime(LocalDateTime.now().minusHours(24)); // Example: 24 hours before now
        response.setEndTime(LocalDateTime.now());

        // Add mapped downtime data into the response object (adjust per the needed logic)
        List<DowntimeRetResponse.DowntimeData> downtimeDataList = downtimeList.stream()
                .map(downtime -> new DowntimeRetResponse.DowntimeData(
                        downtime.getResourceId(),
                        downtime.getDowntimeDuration()
                ))
                .collect(Collectors.toList());

        response.setDowntimeData(downtimeDataList);

        return response;
    }
//    @Override
//    public List<DowntTimeByWorkcenter> getDowntimeByWorkcenter(String site, List<String> workcenterList, LocalDateTime start, LocalDateTime end) {
//        if (workcenterList != null && !workcenterList.isEmpty()) {
//            return downtimeRepository.findDowntimeSummaryBySiteAndWorkcenterAndInterval(site, workcenterList, start, end);
//        } else {
//            return downtimeRepository.findDowntimeSummaryBySiteAndInterval(site, start, end);
//        }
//    }

    @Override
    public List<DowntTimeByWorkcenter> getDowntimeByWorkcenter(String site, List<String> workcenterList, LocalDateTime start, LocalDateTime end) {
        try {
            return getDowntimeForWorkCenter(site, workcenterList, start, end);
        } catch (Exception e){
            throw new RuntimeException("Something went wrong during execution");
        }
    }

    public List<DowntTimeByWorkcenter> getDowntimeForWorkCenter(String site, List<String> workcenterList, LocalDateTime start, LocalDateTime end) {

        StringBuilder sql = new StringBuilder("SELECT d.workcenter_id, d.total_downtime " +
                "FROM r_aggregated_oee d " +
                "WHERE d.site = :site " +
                "AND d.interval_start_date_time >= :start " +
                "AND d.interval_end_date_time <= :end ");

        if (workcenterList != null && !workcenterList.isEmpty()) {
            sql.append("AND d.workcenter_id IN :workcenterList ");
        }

        sql.append("GROUP BY d.workcenter_id, d.total_downtime ");
        sql.append("ORDER BY d.total_downtime DESC");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("site", site);
        query.setParameter("start", start);
        query.setParameter("end", end);

        if (workcenterList != null && !workcenterList.isEmpty()) {
            query.setParameter("workcenterList", workcenterList);
        }

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> new DowntTimeByWorkcenter(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).longValue() / 60 : 0L
                ))
                .collect(Collectors.toList());
    }

    public List<DowntimeByResource> getDowntimeSummaryByResource(String site, List<String> workcenterList, List<String> resourceIds,
                                                                 LocalDateTime intervalStart, LocalDateTime intervalEnd) {
        // If interval is not provided, default to the last 24 hours.
        LocalDateTime now = LocalDateTime.now();
        if (intervalStart == null || intervalEnd == null) {
            intervalEnd = now;
            intervalStart = now.minusHours(24);
        }
        if (workcenterList != null && !workcenterList.isEmpty()) {
            // If a workcenter list is provided, filter by those workcenters.
            return downtimeRepository.findDowntimeBySiteAndWorkcenterAndInterval(site, workcenterList, intervalStart, intervalEnd);
        } else if(resourceIds != null && !resourceIds.isEmpty()){
            return downtimeRepository.findDowntimeBySiteAndResourceIdAndInterval(site, resourceIds,intervalStart, intervalEnd);
        } else {
            // Otherwise, just filter by site and time interval.
            return downtimeRepository.findDowntimeBySiteAndInterval(site, intervalStart, intervalEnd);
        }
    }


    @Override
    public List<DowntimeEventDTO> getDowntimeEvents(String site, List<String> resourceIds, LocalDateTime intervalStart, LocalDateTime intervalEnd) {
        LocalDateTime now = LocalDateTime.now();
        // Default to last 24 hours if interval is not provided
        if (intervalStart == null || intervalEnd == null) {
            intervalEnd = now;
            intervalStart = now.minusHours(24);
        }

        List<Downtime> downtimeRecords;
        if (resourceIds != null && !resourceIds.isEmpty()) {
            downtimeRecords = downtimeRepository.findDowntimeBySiteAndResourceAndInterval(site, resourceIds, intervalStart, intervalEnd);
        } else {
            downtimeRecords = downtimeRepository.findDowntimesBySiteAndInterval(site, intervalStart, intervalEnd);
        }

        List<DowntimeEventDTO> events = new ArrayList<>();
        for (Downtime d : downtimeRecords) {
            LocalDateTime start = d.getDowntimeStart();
            LocalDateTime end = d.getDowntimeEnd();

            /*
             * If the record is active (active == 1) and the downtime event (downtEvent) is 0,
             * assume the event is still ongoing and set the downtime end time to now.
             */
            if (d.getActive() != null && d.getActive() == 1 &&
                    d.getDowntEvent() != null && d.getDowntEvent() == 0) {
                end = now;
            }
            if(end==null){
                end = now;
            }
            // Calculate the duration in seconds
            long duration = Duration.between(start, end).getSeconds();
            events.add(new DowntimeEventDTO(d.getResourceId(), start, end, duration));
        }
        return events;
    }
    @Override
    public List<DowntimeReasonSummaryDTO> getDowntimeDurationByReason(String site, List<String> resourceIds, LocalDateTime intervalStart, LocalDateTime intervalEnd) {
        LocalDateTime now = LocalDateTime.now();

        // Default to the last 24 hours if interval is not provided.
        if (intervalStart == null || intervalEnd == null) {
            intervalEnd = now;
            intervalStart = now.minusHours(24);
        }

        List<DowntimeReasonSummaryDTO> result;

        if (resourceIds != null && !resourceIds.isEmpty()) {
            result = downtimeRepository.findDowntimeSummaryBySiteAndResourceAndInterval(
                    site, resourceIds, intervalStart, intervalEnd, now);
        } else {
            result = downtimeRepository.findDowntimeSummaryBySiteAndInterval(
                    site, intervalStart, intervalEnd, now);
        }
        for (DowntimeReasonSummaryDTO dto : result) {
            if (dto.getDowntimeDuration() != null) {
                dto.setDowntimeDuration(dto.getDowntimeDuration() / 60);
            }
        }

        return result;
    }
    @Override
    public Downtime getReasonForMachineDown(DowntimeRequest request) {
//        Downtime downtime = downtimeRepository.findFirstBySiteAndResourceIdAndWorkcenterIdAndActiveOrderByCreatedDatetimeDesc(
//                request.getSite(), request.getResourceId(), request.getWorkcenterId(), 1);
        Downtime downtime = downtimeRepository.findFirstBySiteAndResourceIdAndActiveOrderByCreatedDatetimeDesc(
                request.getSite(), request.getResourceId(), 1);

        return downtime != null ? downtime : new Downtime();
    }

    @Override
    public List<DowntimeForResource> getDowntimeWithResource(DowntimeRequest request) {
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT d.resource_id, " +
                            "SUM(d.break_time) AS breaktime, " +
                            "SUM(d.total_downtime) AS totaldowntime, " +
                            "SUM(d.planned_production_time) AS productiontime " +
                            "FROM r_aggregated_oee d " +
                            "WHERE d.site = :site "
//                            "AND d.interval_start_date_time >= :start " +
//                            "AND d.interval_end_date_time <= :end "
            );
//
//            if (request.getResourceId() != null && !request.getResourceId().isEmpty()) {
//                sql.append("AND d.resource_id IN :resourceIds ");
//            }

            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put("site", request.getSite());

            OeeFilterRequest obj = new OeeFilterRequest();
            obj.setSite(request.getSite());
            obj.setResourceId(request.getResourceIds());
            obj.setStartTime(request.getIntervalStart());
            obj.setEndTime(request.getIntervalEnd());

            applyCommonFilters1(sql, queryParameters, obj);

            sql.append("GROUP BY d.resource_id ");
            sql.append("ORDER BY d.resource_id");

            Query query = entityManager.createNativeQuery(sql.toString());
            queryParameters.forEach(query::setParameter);

            List<Object[]> results = query.getResultList();

            return results.stream()
                    .map(row -> new DowntimeForResource(
                            (String) row[0],
                            row[1] != null ? BigDecimal.valueOf(((Number) row[1]).doubleValue() / 60).setScale(2, RoundingMode.HALF_UP).doubleValue(): 0.0,
                            row[2] != null ? BigDecimal.valueOf(((Number) row[2]).doubleValue() / 60).setScale(2, RoundingMode.HALF_UP).doubleValue(): 0.0,
                            row[3] != null ? BigDecimal.valueOf(((Number) row[3]).doubleValue() / 60).setScale(2, RoundingMode.HALF_UP).doubleValue(): 0.0
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving downtime for resources: " + e.getMessage());
        }
    }
}