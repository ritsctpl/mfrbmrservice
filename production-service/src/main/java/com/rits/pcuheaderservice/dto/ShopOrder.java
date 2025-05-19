package com.rits.pcuheaderservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<ProductionQuantity> productionQuantities;
    private List<SerialPcu> serialPcu;
    private List<CustomData> customDataList;
    private String parentOrder;
    private String releasedQty;
    private String availableQtyToRelease;
    private boolean inUse;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

}
