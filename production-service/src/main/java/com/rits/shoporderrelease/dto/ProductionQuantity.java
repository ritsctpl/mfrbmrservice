package com.rits.shoporderrelease.dto;

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
    private String underDeliveryMaximumDeliveryQty;
    private String overDeliveryTolerance;
    private String overDeliveryMaximumDeliveryQty;
    private boolean unlimitedOverDelivery;
    private String doneQty;
    private String buildQty;
    private String recommendedBuildQtyToReachOrderedQty;
    private String recommendedBuildQtyToReachMinimumDeliveryQty;
    private String releasedQty;
    private String qtyInQueueOrWork;
    private String scrappedQty;
    private String unreleasedQty;
}
