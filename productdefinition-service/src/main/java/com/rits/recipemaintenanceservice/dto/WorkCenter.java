package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WorkCenter {
    private String sequence;
    private String workCenterId;
    private String description;
    private String resource;
    private String systemStatus;
    private String capacity;
    private String shiftDetails;
    private String operatorId;
    private String maintenanceSchedule;
    private List<String> tooling;
    private String calibrationStatus;
    private String targetCycleTime;
    private String locationId;
    private String zone;
    private List<String> phasesHandled;
}
