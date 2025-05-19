package com.rits.processorderrelease_old.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProcessOrderRelease {
    private String shopOrder;
    private String qty;
    private String firstToLastSFC;
    private String material;
    private String routing;
    private String processLot;
    private String availableQtyToRelease;
}
