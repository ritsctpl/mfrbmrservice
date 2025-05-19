package com.rits.scrapservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScrapRequest {
    private String site;
    private String object;
    private String pcu;
    private String status;
    private String operation;
    private String resource;
    private String shopOrder;
    private String processLot;
    private String itemBO;
    private String routingBO;
    private String bomBO;
    private String scrapQty;
    private String userBO;
    private String createdDateTime;
    private String pcuHeaderHandle;
    private String routerHeaderHandle;
    private String userId;
    private String eventType;
    private String eventData;
}
