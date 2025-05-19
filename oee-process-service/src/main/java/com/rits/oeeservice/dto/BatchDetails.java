package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

// Batch Level
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public
class BatchDetails extends OeeDetails {
    private String batchNo;
    private String item;
    private int actual;
    private int plan;
    private int rejection;
    private int productionTime;
    private int actualTime;
    private int downtimeDuration;
    private int plannedDowntime;
    private int unplannedDowntime;
    private Map<String, Integer> downtimeReasons;
    private int goodQualityCount;
    private int badQualityCount;
    private double energyUsage;
    private double actualCycleTime;
    private double plannedCycletime;

    public BatchDetails(String batchNumber, String item, int i, int totalPlannedQty, int totalBadQty, int totalDowntime, int totalGoodQty, int totalBadQty1, double v, int productionTimeSeconds, int totalTimeSeconds, double availability, double performance, double quality, double oee) {
    }


}
