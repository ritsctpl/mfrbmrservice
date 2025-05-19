package com.rits.performanceservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "DOWNTIME_AVAILABILITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DownTimeAvailability {
    @Id
    private int uniqueId;
    private String resourceId;
    private String createdTime;
    private String createdDateTime;
    private String shift;
    private String entryTime;
    private int plannedProductionTime;
    private int totalDowntime;
    private int operatingTime;
    private int breakHours;
    private int availability;
    private String active;
    private String event;
    private String shiftStartDate;
    private int mcBreakDownHours;
    private String shiftEndDate;
    private Boolean processed;
    private String reasonCode;
}
