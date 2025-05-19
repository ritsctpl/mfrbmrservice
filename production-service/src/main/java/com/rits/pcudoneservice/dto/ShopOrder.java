package com.rits.pcudoneservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ShopOrder {
    private String site;
    private String handle;
    private String shopOrder;
    private String status;
    private String orderType;
    private String plannedMaterial;
    private String materialVersion;
    private String bomType;
    private String plannedBom;
    private String bomVersion;
    private String plannedRouting;
    private String routingVersion;
    private String lcc;
    private String plannedWorkCenter;
    private int priority;
    private int orderedQty;
    private String buildQty;
    private String erpUom;
    private LocalDateTime plannedStart;
    private LocalDateTime plannedCompletion;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private String customerOrder;
    private String customer;
    private String autoRelease;
    private String parentOrder;
    private boolean inUse;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;



}
