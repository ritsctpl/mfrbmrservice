package com.rits.performance.dto;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class PerformanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String site;
    private String shiftId;
    private String workcenterId;
    private String resourceId;
    private String operation;
    private String operationVersion;
    private String item;
    private String itemVersion;
    private Double plannedCycleTime;
    private Double plannedQuantity;
    private Double actualCycleTime;
    private Double actualQuantity;
    private Double performancePercentage;


}
