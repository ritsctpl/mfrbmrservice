package com.rits.processorderrelease_old.dto;

import com.rits.shoporderservice.model.CustomData;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessOrderRequest {
    private String site;
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
    private List<ProductionQuantity> productionQuantities;
    private String availableQtyToRelease;
    private List<SerialBno> serialBno;
    private List<CustomData> customDataList;
    private String parentOrder;
    private boolean inUse;
    private String parentOrderBO;
    private String parentPcuBO;
}
