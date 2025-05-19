package com.rits.shoporderservice.dto;

import com.rits.shoporderservice.model.CustomData;
import com.rits.shoporderservice.model.ProductionQuantity;
import com.rits.shoporderservice.model.SerialPcu;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderRequest {
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
    private String pcu;
    private List<ProductionQuantity> productionQuantities;
    private String availableQtyToRelease;
    private List<SerialPcu> serialPcu;
    private List<CustomData> customDataList;
    private String parentOrder;
    private boolean inUse;
    private String userId;
    private String parentOrderBO;
    private String parentPcuBO;
    private List<String> newPcus;
    private Map<String,Integer> pcuNumberList;
}
