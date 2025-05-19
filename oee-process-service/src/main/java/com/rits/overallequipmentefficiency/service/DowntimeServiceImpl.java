package com.rits.overallequipmentefficiency.service;


import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.DowntimeModel;
import com.rits.overallequipmentefficiency.model.ResourceStatus;
import com.rits.overallequipmentefficiency.repository.OeeDowntimeRepository;
import com.rits.overallequipmentefficiency.repository.OeeMachineLogRepository;
import com.rits.overallequipmentefficiency.service.DowntimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("oeeDowntimeService")
@RequiredArgsConstructor
public class DowntimeServiceImpl implements DowntimeService {

    private final OeeMachineLogRepository machineLogRepository;
    private final OeeDowntimeRepository downtimeRepository;

    @Value("${shift-service.url}/getShiftDetailBetweenTime")
    private String getShiftDetailBetweenTime;

    private final WebClient.Builder webClientBuilder;

    private static final List<String> DOWN_EVENTS = List.of("UNSCHEDULED_DOWN", "SCHEDULED_DOWN");
    private static final List<String> UP_EVENTS = List.of("RELEASABLE", "PRODUCTION");





    /*@Override
    public void logDownTime(DowntimeRequest request) {

             // Requirement 1: Find active record with downtimeType in DOWN_EVENTS
        Optional<DowntimeModel> activeRecord = downtimeRepository.findDowntimeByEvent(
                request.getResourceId(), DOWN_EVENTS, 1);

        if (activeRecord.isPresent()) {
            // Requirement 2: Process active record for update
            DowntimeModel downtime = activeRecord.get();

            // Ensure that downtime spans multiple days
            if (!downtime.getDowntimeStart().toLocalDate().isEqual(request.getDowntimeEnd().toLocalDate())) {
                // Handle multiday downtimes using updateUpdatedDateTime
                updateUpdatedDateTime(downtime.getSite(), downtime.getResourceId(), downtime.getDowntimeStart(), request.getDowntimeEnd());
            }

            processDownTimeUpdate(request, downtime);
        } else {
            throw new IllegalStateException("No active MACHINE_DOWN record found for this resource.");
        }
    }*/

    @Override
    public void logDownTime(DowntimeRequest request) {
        // Requirement 1: Find active record with downtimeType in DOWN_EVENTS
        Optional<DowntimeModel> activeRecord = downtimeRepository.findDowntimeByEvent(
                request.getResourceId(), DOWN_EVENTS, 1);

        if (activeRecord.isPresent()) {
            // Process active record for update
            DowntimeModel downtime = activeRecord.get();

            //LocalDateTime startDateTime = downtime.getDowntimeStart();
            LocalDateTime startDateTime = downtime.getUpdatedDatetime();
            LocalDateTime endDateTime = request.getDowntimeEnd();

            // Ensure that downtime spans multiple days
            while (!startDateTime.toLocalDate().isAfter(endDateTime.toLocalDate())) {
                LocalDateTime dayEndTime = startDateTime.toLocalDate().atTime(23, 59, 59);

                // Cap the dayEndTime to ensure it doesn't exceed the actual downtimeEnd
                if (dayEndTime.isAfter(endDateTime)) {
                    dayEndTime = endDateTime;
                }

                if (!startDateTime.toLocalDate().isEqual(endDateTime.toLocalDate())) {
                    // Handle intermediate days using updateUpdatedDateTime
                    updateUpdatedDateTime(
                            downtime.getSite(),
                            downtime.getResourceId(),
                            startDateTime,
                            dayEndTime
                    );

                    // Re-fetch the active record to get the updated downtime
                    Optional<DowntimeModel> updatedRecord = downtimeRepository.findDowntimeByEvent(
                            request.getResourceId(), DOWN_EVENTS, 1);

                    if (updatedRecord.isEmpty()) {
                        throw new IllegalStateException("Active record not found after update. Data inconsistency detected.");
                    }
                    downtime = updatedRecord.get();

                } else {
                    // Handle the final day using processDownTimeUpdate
                    request.setDowntimeStart(startDateTime); // Ensure the request has the start time
                 //   downtime.setUpdatedDatetime(startDateTime); // Update downtime with the correct start time
                    processDownTimeUpdate(request, downtime);
                }

                // Move to the next day
                startDateTime = startDateTime.toLocalDate().plusDays(1).atStartOfDay();
            }
        } else {
            throw new IllegalStateException("No active MACHINE_DOWN record found for this resource.");
        }
    }


    private void processDownTimeUpdate(DowntimeRequest request, DowntimeModel downtime) {

        List<ShiftOutput> shiftDetails = getShiftDetails(
                downtime.getSite(),
                downtime.getResourceId(),
                downtime.getWorkcenterId(),
                downtime.getUpdatedDatetime(),
                request.getDowntimeEnd()
        );

        if (shiftDetails == null || shiftDetails.isEmpty()) {
            throw new IllegalStateException("No shift details available for the given time range.");
        }

        // Use the helper method
        handleShiftDetailsAndUpdateDowntime(request, downtime, shiftDetails, request.getDowntimeEnd());

    }

    @Override
    public void updateUpdatedDateTime(String site, String resource, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Find active MACHINE_DOWN records for the site
        List<DowntimeModel> activeDowntimes = downtimeRepository.findActiveMachineDownBySite(site, resource, 1);

        activeDowntimes.forEach(downtime -> {
            //String shiftIdGlobal
            // Ensure the endDateTime is not earlier than updatedDatetime
            if(endDateTime.isBefore(downtime.getUpdatedDatetime())){
            // Processing old data
            return;
            }
            LocalDateTime effectiveEndDateTime = endDateTime.isBefore(downtime.getUpdatedDatetime())
                    ? downtime.getUpdatedDatetime()
                    : endDateTime;

            // Fetch shift details using helper method
            List<ShiftOutput> shiftDetails = getShiftDetails(
                    downtime.getSite(),
                    downtime.getResourceId(),
                    downtime.getWorkcenterId(),
                    downtime.getUpdatedDatetime(),
                    effectiveEndDateTime
            );
            // Default to the existing shiftId.
            String newShiftId = downtime.getShiftId();

            if (shiftDetails != null && !shiftDetails.isEmpty()) {
                // Use the helper method
                DowntimeRequest downtimeRequest = new DowntimeRequest();
                downtimeRequest.setReason(downtime.getReason());
                downtimeRequest.setCommentUsr(downtime.getCommentUsr());
                downtimeRequest.setRootCause(downtime.getRootCause());
                newShiftId = handleShiftDetailsAndUpdateDowntime(downtimeRequest, downtime, shiftDetails, endDateTime);
            }

            // Add 1 second to ensure separation from the previous record
           // LocalDateTime newDowntimeStart = endDateTime.plusSeconds(1);
            // Add 1 second and truncate to seconds to remove milliseconds
            LocalDateTime newDowntimeStart = endDateTime.plusSeconds(1).truncatedTo(ChronoUnit.SECONDS);


            DowntimeModel newDowntime = DowntimeModel.builder()
                    .resourceId(downtime.getResourceId())
                    .workcenterId(downtime.getWorkcenterId())
                    .site(downtime.getSite())
                    .shiftId(newShiftId)
                    .shiftCreatedDateTime(downtime.getShiftCreatedDateTime())
                    .downtimeStart(downtime.getDowntimeStart())
                    .shiftRef(newShiftId)
                    .reason((downtime.getReason() != null && !downtime.getReason().trim().isEmpty())
                            ? downtime.getReason()
                            : "Unknown")
                    .rootCause(downtime.getRootCause())
                    .commentUsr(downtime.getCommentUsr())
                    .downtimeType(downtime.getDowntimeType())
                    .createdDatetime(downtime.getCreatedDatetime())
                    .updatedDatetime(newDowntimeStart)// setting to 15:40
                    .downtEvent(downtime.getDowntEvent())
                    .active(1)
                    .build();
            downtimeRepository.save(newDowntime);

        });

   }

    private List<ShiftOutput> getShiftDetails(String site, String resourceId, String workcenterId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Requirement 4: Helper method to fetch shift details
        ShiftInput shiftRequest = new ShiftInput(site, resourceId, workcenterId, startDateTime, endDateTime);
        return webClientBuilder.build()
                .post()
                .uri(getShiftDetailBetweenTime)
                .bodyValue(shiftRequest)
                .retrieve()
                .bodyToFlux(ShiftOutput.class)
                .collectList()
                .block();
    }


    /*private void handleShiftDetailsAndUpdateDowntime(DowntimeRequest request, DowntimeModel downtime, List<ShiftOutput> shiftDetails, LocalDateTime downtimeEnd) {
        shiftDetails.forEach(shiftDetail -> {
            if (shiftDetail.getShiftId().equals(downtime.getShiftId()) &&
                    shiftDetail.getIntervalStartDatetime().equals(downtime.getUpdatedDatetime())) {

                // Update matching downtime record
                downtime.setDowntimeEnd(shiftDetail.getIntervalEndDatetime());
                downtime.setDowntimeDuration(Long.valueOf(shiftDetail.getPlannedOperatingTime()));
                downtime.setPlannedOperatingTime(Long.valueOf(shiftDetail.getPlannedOperatingTime()));
                downtime.setShiftBreakStartDatetime(shiftDetail.getBreaktime().toString());
                downtime.setReason(request.getReason() != null ? request.getReason() : "Unknown");
                downtime.setRootCause(request.getRootCause() != null ? request.getRootCause() : "Unknown");
                downtime.setCommentUsr(request.getCommentUsr() != null ? request.getCommentUsr() : "");
            //    downtime.setUpdatedDatetime(downtimeEnd);
                downtime.setDowntEvent(1);
                downtime.setActive(0);
                downtimeRepository.save(downtime);
            } else {
                // Insert new downtime records for additional shifts
                DowntimeModel newDowntime = DowntimeModel.builder()
                        .resourceId(downtime.getResourceId())
                        .workcenterId(downtime.getWorkcenterId())
                        .site(downtime.getSite())
                        .shiftId(shiftDetail.getShiftId())
                        .shiftCreatedDateTime(shiftDetail.getShiftCreatedDatetime())
                        .downtimeStart(shiftDetail.getIntervalStartDatetime())
                        .downtimeEnd(shiftDetail.getIntervalEndDatetime())
                        .downtimeDuration(Long.valueOf(shiftDetail.getPlannedOperatingTime()))
                        .plannedOperatingTime(Long.valueOf(shiftDetail.getPlannedOperatingTime()))
                        .shiftBreakStartDatetime(shiftDetail.getBreaktime().toString())
                        .shiftRef(shiftDetail.getShiftId())
                        .reason(request.getReason() != null ? request.getReason() : "Unknown")
                        .rootCause(request.getRootCause() != null ? request.getRootCause() : "Unknown")
                        .commentUsr(request.getCommentUsr())
                        .downtimeType("UNSCHEDULED_DOWN")
                        .createdDatetime(LocalDateTime.now())
                        .updatedDatetime(downtimeEnd)
                        .downtEvent(1)
                        .active(0)
                        .build();
                downtimeRepository.save(newDowntime);
            }
        });
    }*/

    private String handleShiftDetailsAndUpdateDowntime(DowntimeRequest request,
                                                       DowntimeModel downtime,
                                                       List<ShiftOutput> shiftDetails,
                                                       LocalDateTime downtimeEnd) {
        // Default shiftId is the one already in downtime.
        String resultingShiftId = downtime.getShiftId();

        // Process each shift detail.
        for (ShiftOutput shiftDetail : shiftDetails) {
            if (shiftDetail.getShiftId().equals(downtime.getShiftId()) &&
                    shiftDetail.getIntervalStartDatetime().equals(downtime.getUpdatedDatetime())) {

                // Update the matching downtime record.
                downtime.setDowntimeEnd(shiftDetail.getIntervalEndDatetime());
                downtime.setDowntimeDuration(Long.valueOf(shiftDetail.getPlannedOperatingTime()));
                downtime.setPlannedOperatingTime(Long.valueOf(shiftDetail.getPlannedOperatingTime()));
                downtime.setShiftBreakStartDatetime(shiftDetail.getBreaktime().toString());
                downtime.setReason((downtime.getReason() != null && !downtime.getReason().trim().isEmpty())
                        ? downtime.getReason()
                        : "Unknown");
                downtime.setRootCause(downtime.getRootCause() != null ? downtime.getRootCause() : "Unknown");
                downtime.setCommentUsr(downtime.getCommentUsr() != null ? downtime.getCommentUsr() : "");
                // downtime.setUpdatedDatetime(downtimeEnd);
                downtime.setDowntEvent(downtime.getDowntEvent());
                downtime.setActive(0);
                downtimeRepository.save(downtime);
                // Keep the default shiftId.

                resultingShiftId = downtime.getShiftId();
            } else {

                downtime.setActive(0);
                downtimeRepository.save(downtime);

                // Insert a new downtime record for an additional shift.
                DowntimeModel newDowntime = DowntimeModel.builder()
                        .resourceId(downtime.getResourceId())
                        .workcenterId(downtime.getWorkcenterId())
                        .site(downtime.getSite())
                        .shiftId(shiftDetail.getShiftId())
                        .shiftCreatedDateTime(shiftDetail.getShiftCreatedDatetime())
                        .downtimeStart(downtime.getDowntimeStart())
                        .downtimeEnd(shiftDetail.getIntervalEndDatetime())
                        .downtimeDuration(Long.valueOf(shiftDetail.getPlannedOperatingTime()))
                        .plannedOperatingTime(Long.valueOf(shiftDetail.getPlannedOperatingTime()))
                        .shiftBreakStartDatetime(shiftDetail.getBreaktime().toString())
                        .shiftRef(shiftDetail.getShiftId())
                        .reason((request.getReason() != null && !request.getReason().trim().isEmpty())
                                ? request.getReason() : "Unknown")
                        .rootCause(request.getRootCause() != null ? request.getRootCause() : "Unknown")
                        .commentUsr(request.getCommentUsr())
                        .downtimeType("UNSCHEDULED_DOWN")
                        .createdDatetime(LocalDateTime.now())
                        .updatedDatetime(shiftDetail.getIntervalStartDatetime())
                        .downtEvent(downtime.getDowntEvent())
                        .active(0)
                        .build();
                downtimeRepository.save(newDowntime);
                // Use the shiftId from the new downtime (from shiftDetail).
                resultingShiftId = shiftDetail.getShiftId();
            }
        }
        return resultingShiftId;
    }


    @Override
    public Long getBreakHoursBetweenTime(DowntimeRequest request) {
        // Fetch downtime records with the given criteria
        List<DowntimeModel> downtimeRecords = downtimeRepository.findDowntimeWithinTimeRange(
                request.getSite(),
                request.getResourceId(),
                request.getStartDateTime(),
                request.getEndDateTime(),
                1   // downt_event = 1
        );

        // Sum the downtime durations
        long totalDowntimeDuration = downtimeRecords.stream()
                .mapToLong(DowntimeModel::getDowntimeDuration)
                .sum();

        return totalDowntimeDuration;
    }
    @Override
    public Long getPlannedBreakHoursBetweenTime(DowntimeRequest request) {
        // Fetch downtime records with the given criteria
        List<DowntimeModel> downtimeRecords = downtimeRepository.findDowntimeWithinTimeRange(
                request.getSite(),
                request.getResourceId(),
                request.getStartDateTime(),
                request.getEndDateTime(),
                2   // downt_event = 1
        );

        // Sum the downtime durations
        long totalDowntimeDuration = downtimeRecords.stream()
                .mapToLong(DowntimeModel::getDowntimeDuration)
                .sum();

        return totalDowntimeDuration;
    }
@Override
public List<Map<String, Object>> getResourceStatusList(String site) {
    return downtimeRepository.findResourceStatusBySite(site);
}
    @Override
    public DowntimeSummary getBreakHoursBetweenTimeWithDetails(DowntimeRequest request) {
        // Call the existing method for total downtime.
        Long totalDowntimeDuration = getBreakHoursBetweenTime(request);

        // Fetch downtime records (you may call a repository method here, as in your original code)
        List<DowntimeModel> downtimeRecords = downtimeRepository.findDowntimeWithinTimeRange(
                request.getSite(),
                request.getResourceId(),
                request.getStartDateTime(),
                request.getEndDateTime(),
                1   // downt_event = 1
        );

        // Group records and determine the major issue (reason and root cause).
        Map<String, Long> downtimeByIssue = downtimeRecords.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getReason() + "|" + record.getRootCause(),
                        Collectors.summingLong(DowntimeModel::getDowntimeDuration)
                ));

        Optional<Map.Entry<String, Long>> majorIssueEntry = downtimeByIssue.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        String majorReason = "";
        String majorRootCause = "";
        if (majorIssueEntry.isPresent()) {
            String[] parts = majorIssueEntry.get().getKey().split("\\|", 2);
            majorReason = parts[0];
            majorRootCause = parts.length > 1 ? parts[1] : "";
        }

        // If the major root cause is empty or null, set both to "UNKNOWN"
        if (majorRootCause == null || majorRootCause.trim().isEmpty()) {
            majorReason = "UNKNOWN";
            majorRootCause = "UNKNOWN";
        }

        return new DowntimeSummary(totalDowntimeDuration, majorReason, majorRootCause);
    }

}
