package com.rits.batchnoheader.dto;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BatchNoHeaderRequest {
    private String site;
    private String batchNumber;
    private String orderNo;
    private String material;
    private String materialVersion;
    private String materialDescription;
    private String status;
    private BigDecimal totalQuantity;
    private BigDecimal qtyToWorkOrder;
    private BigDecimal qtyInQueue;
    private BigDecimal qtyInHold;
    private BigDecimal qtyDone;
    private String recipeName;
    private String recipeVersion;
    private String baseUom;
    private String measuredUom;
    private BigDecimal conversionFactor;
    private BigDecimal releasedQuantityBaseUom;
    private BigDecimal releasedQuantityMeasuredUom;
}