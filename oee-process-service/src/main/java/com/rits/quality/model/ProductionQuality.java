package com.rits.quality.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "R_QUALITY")
public class ProductionQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String site;
    private String reason;
    private String pcu;
    private String operation;
    private String operationVersion;
    private String item;
    private String itemVersion;
    private String shopOrder;
    private Double goodQuantity;
    private Double badQuantity;
    private Double plan;
    private Integer active;
    private String user_id;
    private String workcenterId;
    private String resourceId;
    private String shiftId;
    private Double totalQuantity;
    private Double qualityPercentage;
    private String batchNumber;
    private LocalDateTime calculationTimestamp;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
    private LocalDateTime intervalStartDateTime;
    private LocalDateTime intervalEndDateTime;
    private Long availabilityId;
    private Long performanceId;
    private String eventTypeOfPerformance;
    private double targetQuantity;
}
