package com.rits.availability.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverallAvailabilityResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String interval;
    private String resourceId;
    private String shiftId;
    private double availabilityPercentage;
    private String site;
    private String workcenterId;
    private String batchNumber;
    private LocalDate availabilityDate;
    private Double plannedOperatingTime;
    private Double runtime;
    private Double downtime;
    private Double shiftBreakDuration;
    private Double nonProductionDuration;
    private Boolean isPlannedDowntimeIncluded;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private String shiftRef;
    private Integer active;
    private Double actualAvailableTime;
    private Double totalAvailableTimeSeconds;
}