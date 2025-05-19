package com.rits.productionlogservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductionLogDto {
    private  String operationVersion;
    private String site;
    private String workcenter_id;
    private String operation;
    private String operation_version;
    private String resource_id;
    private String item;
    private String item_version;
    private String shift_id;
    private String pcu;
    private String resourceId;
    private String workcenterId;
    private String shiftId;
    private String itemVersion;
    private String batchNo;
    private String shopOrderBo;
    private Double partsToBeProduced;

    public ProductionLogDto(String site, String workcenterId, String operation, String operationVersion, String resourceId, String item, String itemVersion, String shiftId, String pcu) {
        this.site = site;
        this.workcenterId = workcenterId;
        this.operation = operation;
        this.operationVersion = operationVersion;
        this.resourceId = resourceId;
        this.item = item;
        this.itemVersion = itemVersion;
        this.shiftId = shiftId;
        this.pcu = pcu;
    }
   // private String shop_order;
}