package com.rits.overallequipmentefficiency.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "r_aggregated_time_period")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedTimePeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Key dimensions
    private String site;
    private String workcenterId;
    private LocalDate logDate;  // For day-level: the specific day; for month-level: typically the first day of the month.
    private String category;    // "DAY" or "MONTH"
    private String shiftId;
   /* private LocalDate day;
    private LocalDate month;
    private LocalDate year;
*/
   private Integer day;    // e.g., 1 to 31
    private Integer month;  // e.g., 1 to 12
    private Integer year;   // e.g., 2025

    // Aggregated fields
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
    private Double availability;
    private Double performance;
    private Double quality;
    private Double oee;
    private Boolean active;

    private String eventSource; // "MACHINE" or "MANUAL"

    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private double targetQuantity;
}