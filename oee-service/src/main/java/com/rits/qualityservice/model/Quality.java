package com.rits.qualityservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_QUALITY")
public class Quality {
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
    private String shoporderBO;
    private String workcenterBO;
    private int scrapQuantity;
    private double quality;
    private String reasonCode;
    private int speedLoss;
    private int count;
    private double calculatedCycleTime;
    private String eventPerformance;
    private boolean processed;
    private double idealTime;
    private int actualValue;
    private int targetValue;
    private boolean done;
    private String tags;
}
