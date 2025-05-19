package com.rits.oeeservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "R_OEE")
public class Oee {
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
    private double availability;
    private double performance;
    private int active;
    private String event;
    private String itemBO;
    private String routingBO;
    private String operationBO;
    private String reasonCode;
    private int scrapQuantity;
    private double quality;
    private String shoporderBO;
    private String workcenterBO;
    private int count;
    private double idealTime;
    private double speedLoss;
    private double oee;
    private double calculatedCycleTime;
    private String eventPerformance;
    private int actualValue;
    private int targetValue;
    private boolean done;
    private String tags;
}






































































