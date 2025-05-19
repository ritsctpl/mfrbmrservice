package com.rits.performance.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceCalculationResponse {
    private String site;
    private String shiftId;
    private String workcenterId;
    private String resourceId;
    private String pcu;
    private String operation;
    private String operationVersion;
    private String item;
    private String itemVersion;
    private Double plannedCycleTime;
    private Double plannedQuantity;
    private Double actualCycleTime;
    private Double manufacturedTime;
    private Double actualQuantity;
    private Double performancePercentage;
}
