package com.rits.overallequipmentefficiency.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "r_aggregated_performance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String site;
    // Foreign key reference to aggregated availability
    private Long aggregatedAvailabilityId;

    private String pcu;
    private String shiftId;
    private String workcenterId;
    private String resourceId;


    private String item;
    private String itemVersion;
    private String operation;
    private String operationVersion;
    private String shopOrderBO;
    private String batchNumber;

    private Double totalPlannedOutput;
    private Double totalActualOutput;
    private Double performancePercentage; // e.g. (actual/planned)*100

    private LocalDateTime intervalStartDateTime;
    private LocalDateTime intervalEndDateTime;
    private LocalDateTime createdDatetime;
}
