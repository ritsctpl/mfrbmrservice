package com.rits.overallequipmentefficiency.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "r_performance")
public class PerformanceModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String site;
    private String pcu;
    private String shiftId;
    private LocalDateTime shiftCreatedDatetime;
    private String workcenterId;
    private String resourceId;
    private String item;
    private String itemVersion;
    private String operation;
    private String operationVersion;
    private String shopOrderBO;
    private String batchNumber;
    private String batchSize;
    private double plannedOutput;
    private double actualOutput;
    private Integer scrapQuantity;
    private Integer reworkQuantity;
    private double plannedCycleTime;
    private double actualCycleTime;
    private double performanceEfficiency;
    private double performancePercentage;
    private double downtimeDuration;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private LocalDateTime intervalStartDateTime;
    private LocalDateTime intervalEndDateTime;
    private Long availabilityId;
    private Integer active;
    private String eventType;
    private String category;
    private double targetQuantity;

}
