package com.rits.pcustepstatus.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuStepStatusMain {
    private String pcu;
    private String pcuStatus;
    private String material;
    private String materialVersion;
    private String orderType;
    private String routing;
    private String routingVersion;
    private String processLot;
    private String operation;
    private String workCenterCategory;
    private String shopOrder;
    private String resource;
    private String materialGroup;
    private String workCenter;
}
