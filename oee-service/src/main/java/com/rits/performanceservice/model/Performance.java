package com.rits.performanceservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "Performance")
public class Performance {
    @Id
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
    private int active;
    private String event;
    private String itemBO;
    private String routingBO;
    private String operationBO;
    private int scrapQuantity;
    private double quality;
    private int count;
    private double calculatedCycleTime;
    private String eventPerformance;
    private String reasonCode;
    private String shoporderBO;
    private String workcenterBO;
    private int speedLoss;
    private double idealTime;
    private boolean processed;
    private int targetValue;
    private int actualValue;
    private boolean done;
    private String tags;
}
