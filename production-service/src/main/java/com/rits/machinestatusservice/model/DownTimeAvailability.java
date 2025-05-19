package com.rits.machinestatusservice.model;

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
    private String createdDateTime;
    private String shift;
    private String entryTime;
    private String plannedProductionTime;
    private int totalDowntime;
    private int operatingTime;
    private int breakHours;
    private int availability;
    private boolean active;
    private String event;
}
