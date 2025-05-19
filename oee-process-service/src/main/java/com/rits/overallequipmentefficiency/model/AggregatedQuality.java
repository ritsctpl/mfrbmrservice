package com.rits.overallequipmentefficiency.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "r_aggregated_quality")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedQuality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String site;
    private Long aggregatedAvailabilityId;
    private Long aggregatedPerformanceId;

    private String workcenterId;
    private String resourceId;

    // NEW: store the shiftId for retrieval and grouping
    private String shiftId;


    private String item;
    private String itemVersion;
    private String operation;
    private String operationVersion;
    private String shopOrderBO;
    private String batchNumber;

    private Double totalGoodQuantity;
    private Double totalBadQuantity;
    private Double totalQuantity;
    private Double qualityPercentage; // e.g. (good/total)*100

    private LocalDateTime intervalStartDateTime;
    private LocalDateTime intervalEndDateTime;
    private LocalDateTime createdDatetime;
}
