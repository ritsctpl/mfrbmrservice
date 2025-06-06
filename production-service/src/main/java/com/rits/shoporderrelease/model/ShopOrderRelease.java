package com.rits.shoporderrelease.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ShopOrderRelease {
    private String shopOrder;
    private String qty;
    private String firstToLastSFC;
    private String material;
    private String routing;
    private String processLot;
    private String availableQtyToRelease;
}
