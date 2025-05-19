package com.rits.overallequipmentefficiency.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "r_aggregated_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String site;
    // "resource" or "workcenter"
    private String category;
    // resourceId or workcenterId based on category
    private String identifier;

    // NEW: store the shiftId for retrieval and grouping
    private String shiftId;
    private String workcenterId;
    private String resourceId;

    private LocalDate availabilityDate;
    private LocalDateTime shiftStartDateTime;
    private LocalDateTime intervalEndDateTime;
    private LocalDateTime createdDateTime;

    private LocalDateTime updatedDatetime;
    private LocalDateTime intervalStartDateTime;


    private String item;
    private String itemVersion;
    private String operation;
    private String operationVersion;
    private String shopOrderBO;
    private String batchNumber;

    private Double totalPlannedOperatingTime;
    private Double totalActualAvailableTime;
    private Double totalRuntime;
    private Double totalDowntime;
    private Double totalShiftBreakDuration;
    private Double totalNonProductionDuration;
    private Double averageAvailability; // e.g. (actual/planned)*100
}
