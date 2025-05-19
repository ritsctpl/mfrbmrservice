package com.rits.overallequipmentefficiency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedAvailabilityDTO {
    private String site;
    private String shiftId;
    private String resourceId;
    private String workcenterId;
    private LocalDate shiftDate;
    private String aggregationLevel; // SHIFT, RESOURCE, WORKCENTER

    private Double totalPlannedOperatingTime;
    private Double totalActualAvailableTime;
    private Double totalRuntime;
    private Double totalDowntime;
    private Double totalShiftBreakDuration;
    private Double totalNonProductionDuration;
    private Double averageAvailabilityPercentage;
}
