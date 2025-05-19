package com.rits.oeeservice.dto;

import lombok.*;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OeeRequest {
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
    private double availability;
    private double performance;
    private int count;
    private double calculatedCycleTime;
    private String reasonCode;
    private int active;
    private String event;
    private String itemBO;
    private String routingBO;
    private String operationBO;
    private String shoporderBO;
    private String workcenterBO;
    private int speedLoss;
    private double idealTime;
    private int scrapQuantity;
    private double quality;
    private String eventPerformance;
    private int actualValue;
    private int targetValue;
    private boolean done;
    private String tags;
}
