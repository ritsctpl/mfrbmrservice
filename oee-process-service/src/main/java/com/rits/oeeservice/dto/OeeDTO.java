package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OeeDTO {
    private double oee;
    private double goodQty;
    private double badQty;
    private Integer plan;
    private double totalDowntime;
    private String item;
    private String operation;
    private String shiftId;
    private String resourceId;
    private String workcenterId;
    private String batchNumber;

    public OeeDTO(double oee, double goodQty, double badQty, Integer plan, double totalDowntime,
                  String item, String operation, String shiftId, String resourceId,
                  String workcenterId, String batchNumber) {
        this.oee = oee;
        this.goodQty = goodQty;
        this.badQty = badQty;
        this.plan = plan;
        this.totalDowntime = totalDowntime;
        this.item = item;
        this.operation = operation;
        this.shiftId = shiftId;
        this.resourceId = resourceId;
        this.workcenterId = workcenterId;
        this.batchNumber = batchNumber;
    }
}
