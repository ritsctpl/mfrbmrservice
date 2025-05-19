package com.rits.machinestatusservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DownTimeRequest {
    private int uniqueId;
    private String event;
    private String resource;
    private String workcenter;
    private String shift;
    private String downtimeStart;
    private String downtimeEnd;
    private String meantime;
    private String shiftStartTime;
    private String shiftEndTime;
    private String shiftAvailableTime;
    private int breakHours;
    private String createdDateTime;
    private String createdDate;
    private String site;
    private String reasonCode;
    private Boolean processed;
    private String active;
    private String resourceId;
    private String entryTime;
    private int plannedProductionTime;
    private int totalDowntime;
    private int operatingTime;
    private int availability;
    private String shiftStartDate;
    private int mcBreakDownHours;
    private String shiftEndDate;

}




