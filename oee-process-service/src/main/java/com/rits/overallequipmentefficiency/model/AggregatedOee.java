package com.rits.overallequipmentefficiency.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "r_aggregated_oee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedOee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String site;
    private String shiftId;
    private String workcenterId;
    private String resourceId;
    private LocalDate logDate;
    private String category;

    private String item;
    private String itemVersion;
    private String operation;
    private String operationVersion;
    private String shopOrderId;
    private String batchNumber;

    // Foreign key references to aggregated components
    private Long aggregatedAvailabilityId;
    private Long aggregatedPerformanceId;
    private Long aggregatedQualityId;


    private Double availability;   // from aggregated availability
    private Double performance;    // from aggregated performance
    private Double quality;        // from aggregated quality
    private Double oee;            // computed overall OEE

    private Double productionTime; // not used.
    private Integer plan;   // not used.

    private Double actualProductionTime;
    private Double breakTime;
    private Double plannedProductionTime;
    private Double actualTime;
    private Double totalDowntime;

    private Double cycleTime;
    private Double actualCycleTime;
    private Double plannedQuantity;
    private Double totalGoodQuantity;
    private Double totalBadQuantity;
    private Double totalQuantity;

    // Used for retrieval (for example, by date)
    private int month;
    private int year;
    private LocalDateTime createdDatetime;
    private LocalDateTime intervalStartDateTime;
    private LocalDateTime intervalEndDateTime;

    private String eventSource; // "MACHINE" or "MANUAL"

    private Boolean active;
    private double targetQuantity;

}
