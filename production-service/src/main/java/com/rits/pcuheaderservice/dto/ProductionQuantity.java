package com.rits.pcuheaderservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductionQuantity {
    private String orderedQty;
    private String defineDeliveryToleranceIn;
    private String underDeliveryTolerance;
    private String overDeliveryTolerance;
    private String maximumDeliveryQty;
    private String unlimitedOverDelivery;
    private String doneQty;
    private String releasedQty;
    private String qtyInQueueOrWork;
    private String scrappedQty;
    private String unreleasedQty;
    private String buildQty;
    private String recommendedBuildQtyToReachOrderedQty;
    private String recommendedBuildQtyToReachMinimumDeliveryQty;
}

