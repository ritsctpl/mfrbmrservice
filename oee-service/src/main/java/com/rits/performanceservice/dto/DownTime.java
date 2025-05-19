package com.rits.performanceservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DownTime {
    private int uniqueId;
    private String resourceId;
    private LocalDateTime createdDateTime;
    private String shift;
    private String entryTime;
    private int plannedProductionTime;
    private int totalDowntime;
    private int operatingTime;
    private int breakHours;
    private int availability;
    private int active;
    private String event;
    private String shiftStartDate;
    private int mcBreakDownHours;
    private String shiftEndDate;
    private Boolean processed;
    private String site;
    private String shiftStartTime;
    private double performance;
    private String eventPerformance;
    private String itemBO;
    private String routingBO;
    private String reasonCode;
    private int speedLoss;
    private String shoporderBO;
    private String workcenterBO;
    private String operationBO;
    private int count;
    private double idealTime;
    private int targetValue;
    private int actualValue;
    private boolean done;
    private String tags;
    private List<Combinations> combinations;
}
