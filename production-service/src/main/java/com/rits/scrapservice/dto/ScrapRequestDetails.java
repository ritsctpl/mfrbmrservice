package com.rits.scrapservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScrapRequestDetails {
    private String site;
    private String object;
    private String pcu;
    private String status;
    private String operation;
    private String operationVersion;
    private String resource;
    private String shopOrder;
    private String processLot;
    private String item;
    private String itemVersion;
    private String routing;
    private String routingVersion;
    private String bom;
    private String bomVersion;
    private String scrapQty;
    private String user;
    private String createdDateTime;
    private String pcuHeaderHandle;
    private String routerHeaderHandle;
    private String userId;
    private String eventType;
    private String eventData;
}
