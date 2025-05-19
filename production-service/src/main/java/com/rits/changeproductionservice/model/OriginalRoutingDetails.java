package com.rits.changeproductionservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OriginalRoutingDetails {
    private String pcu;
    private String pcuStatus;
    private String shopOrder;
    private String operation;
    private String resource;
    private String bom;
    private String bomVersion;
    private String item;
    private String itemVersion;
    private String routing;
    private String routingVersion;
    private int qty;
}
