package com.rits.performance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OeePerformanceDTO {
    private Double actualOutput;
    private Double plannedOutput;
    private Double performance;
    private String item;
    private String operation;
    private String resourceId;
    private String workcenterId;
    private String batchNumber;
    private String shiftId;

    public OeePerformanceDTO(Double actualOutput, Double plannedOutput, Double performance,
                             String item, String operation, String resourceId, String workcenterId,
                             String batchNumber, String shiftId) {
        this.actualOutput = actualOutput;
        this.plannedOutput = plannedOutput;
        this.performance = performance;
        this.item = item;
        this.operation = operation;
        this.resourceId = resourceId;
        this.workcenterId = workcenterId;
        this.batchNumber = batchNumber;
        this.shiftId = shiftId;
    }
}
