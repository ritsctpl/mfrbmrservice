package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Site Level
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public
class SiteDetails extends OeeDetails {
    private String site;
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

    private double oee;
    private double availability;
    private double performance;
    private double quality;
    private boolean downtime;
    private double actualCycleTime;
    private double plannedCycletime;

    public SiteDetails(String site, List<ShiftDetails> shiftDetailsList) {
        this.site = site;

        this.batchNo = "";
        this.item = "";
        this.actual = 0;
        this.plan = 0;
        this.rejection = 0;
        this.productionTime = 0;
        this.actualTime = 0;
        this.downtimeDuration = 0;
        this.plannedDowntime = 0;
        this.unplannedDowntime = 0;
        this.downtimeReasons = new HashMap<>();
        this.goodQualityCount = 0;
        this.badQualityCount = 0;
        this.energyUsage = 0;
        this.actualCycleTime=0;
        this.plannedCycletime=0;

        // Process shiftDetailsList to initialize relevant fields
        if (shiftDetailsList != null) {
            for (ShiftDetails shift : shiftDetailsList) {
                this.actual += shift.getActualTime();
                this.plan += shift.getPlan();
                this.rejection += shift.getRejection();
                this.productionTime += shift.getProductionTime();
                this.actualTime += shift.getActualTime();
                this.downtimeDuration += shift.getDowntimeDuration();
                this.plannedDowntime += shift.getPlannedDowntime();
                this.unplannedDowntime += shift.getUnplannedDowntime();
                this.goodQualityCount += shift.getGoodQualityCount();
                this.badQualityCount += shift.getBadQualityCount();
                this.energyUsage += shift.getEnergyUsage();

                this.oee = shift.getOee();
                this.availability = shift.getAvailability();
                this.performance = shift.getPerformance();
                this.quality = shift.getQuality();

                // Aggregate downtime reasons
                Map<String, Integer> shiftDowntimeReasons = shift.getDowntimeReasons();
                if (shiftDowntimeReasons != null) {
                    for (Map.Entry<String, Integer> entry : shiftDowntimeReasons.entrySet()) {
                        this.downtimeReasons.merge(entry.getKey(), entry.getValue(), Integer::sum);
                    }
                }
            }
        }
    }
}
