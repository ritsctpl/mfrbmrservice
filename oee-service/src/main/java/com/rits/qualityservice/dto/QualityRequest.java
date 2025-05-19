package com.rits.qualityservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class QualityRequest {
    private int uniqueId;
    private String site;
    private String resourceId;
    private LocalDateTime createdDateTime;
    private String shiftStartTime;
    private String shift;
    private String entryTime;
    private String plannedProductionTime;
    private int totalDowntime;
    private int operatingTime;
    private int breakHours;
    private int availability;
    private double performance;
    private int count;
    private double calculatedCycleTime;
    private int active;
    private String event;
    private String itemBO;
    private String routingBO;
    private String operationBO;
    private int scrapQuantity;
    private int quality;
    private String eventPerformance;
    private String reasonCode;
    private int speedLoss;
    private String shoporderBO;
    private String workcenterBO;
    private double idealTime;
    private int actualValue;
    private int targetValue;
    private boolean done;
    private String tags;
}
