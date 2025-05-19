package com.rits.cycletimeservice.dto;


import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CycleTimeDto {
    private String site;
    private String shiftId;
    private String workcenterId;
    private String resourceId;
    private String pcu;
    private String item;
    private String itemVersion;
    private String operation;
    private String operationVersion;
    private double plannedCycleTime;
    private double actualCycleTime;
    private double plannedOutput;
    private double actualOutput;
    private double performanceEfficiency;
    private double performancePercentage;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private Integer active;
}
