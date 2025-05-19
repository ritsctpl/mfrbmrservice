package com.rits.overallequipmentefficiency.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AggregatedOeeDTO {
    private double availability;
    private double oee;
    private double performance;
    private double quality;
    private double actualCycleTime;
    private double actualProductionTime;
    private double actualTime;
    private double productionTime;
    private double totalGoodQuantity;
    private double totalBadQuantity;
    private int plan;
    private String batchNumber;
    private String resourceId;
    private String workcenterId;
    private String shiftId;
    private String operation;
    private String item;
    private String shopOrderId;
    private String category;

    public AggregatedOeeDTO(Double availability, Double oee, Double performance, Double quality,
                            Double actualCycleTime, Double actualProductionTime, Double actualTime,
                            Double productionTime, Double totalGoodQuantity, Double totalBadQuantity,
                            Integer plan, String batchNumber, String resourceId, String workcenterId,
                            String shiftId, String operation, String item, String shopOrderId,
                            String category) {
        this.availability = availability;
        this.oee = oee;
        this.performance = performance;
        this.quality = quality;
        this.actualCycleTime = actualCycleTime;
        this.actualProductionTime = actualProductionTime != null ? actualProductionTime : 0.0;
        this.actualTime = actualTime != null ? actualTime : 0.0;
        this.productionTime = productionTime != null ? productionTime : 0.0;
        this.totalGoodQuantity = totalGoodQuantity != null ? totalGoodQuantity : 0;
        this.totalBadQuantity = totalBadQuantity != null ? totalBadQuantity : 0;
        this.plan = plan != null ? plan : 0;
        this.batchNumber = batchNumber;
        this.resourceId = resourceId;
        this.workcenterId = workcenterId;
        this.shiftId = shiftId;
        this.operation = operation;
        this.item = item;
        this.shopOrderId = shopOrderId;
        this.category = category;
    }
}
