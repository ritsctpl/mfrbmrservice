package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

// Resource Level
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public
class ResourceDetails extends OeeDetails {
    private String resource;
    private String batchNo;
    private String item;
    private double actual;
    private double plan;
    private double rejection;
    private double productionTime;
    private double actualTime;
    private double downtimeDuration;
    private double plannedDowntime;
    private double unplannedDowntime;
    private Map<String, Integer> downtimeReasons;
    private double goodQualityCount;
    private double badQualityCount;
    private double energyUsage;
    private String intervalEndTime;
    private boolean downtime;
    private double actualCycleTime;
    private double plannedCycletime;
    public ResourceDetails(List<String> resourceId, List<BatchDetails> batchDetailsList) {
    }
}
