package com.rits.overallequipmentefficiency.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "r_aggregated_availability_old")
@Getter
@Setter
public class AggregatedAvailabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active;  // ✅ Ensure this maps correctly to BOOLEAN

    private String site;
    private String shiftId;
    private LocalDate shiftDate;
    private String resourceId;
    private String workcenterId;
    private String aggregationLevel;

    private Double totalPlannedOperatingTime;
    private Double totalActualAvailableTime;
    private Double totalRuntime;
    private Double totalDowntime;
    private Double totalShiftBreakDuration;
    private Double totalNonProductionDuration;
    private Double averageAvailabilityPercentage;

    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private LocalDateTime intervalStartDateTime;
    private LocalDateTime intervalEndDateTime;
}
