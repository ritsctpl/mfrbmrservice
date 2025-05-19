package com.rits.performance.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "r_performance")
public class OeePerformanceEntity {

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
    private Long availabilityId;
    private LocalDateTime intervalStartDateTime;
    private LocalDateTime intervalEndDateTime;
    private Integer active;
    private double targetQuantity;

}
