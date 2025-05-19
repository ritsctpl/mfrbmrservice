package com.rits.processorderservice.dto;

import com.rits.processorderservice.model.BatchNumber;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessOrderRequest {
    private String orderNumber;
    private String site;
    private String recipe;
    private String recipeVersion;
    private String status;
    private String material;
    private String materialVersion;
    private String materialDescription;
    private String orderType;
    private Integer targetQuantity;
    private String workCenter;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;
    private LocalDateTime productionStartDate;
    private LocalDateTime productionFinishDate;
    private LocalDateTime schedFinTime;
    private LocalDateTime schedStartTime;
    private String uom;
    private String measuredUom;
    private BigDecimal conversionFactor;
    private List<BatchNumber> batchNumber;
    private String userId;
    private String batch;
    private String priority;
    private boolean inUse;
    private BigDecimal availableQtyToRelease;
    private List<String> newBnos;
    private Map<String,Integer> bnoNumberList;
    private String productionVersion;





//    private String site;
//    private String processOrder;
//    private String status;
//    private String orderType;
//    private String plannedMaterial;
//    private String materialVersion;
//    private String uom;
//    private String bomType;
//    private String plannedBom;
//    private String bomVersion;
//    private String plannedRouting;
//    private String routingVersion;
//    private String lcc;
//    private String plannedWorkCenter;
//    private int priority;
//    private int orderedQty;
//    private String buildQty;
//    private String erpUom;
//    private LocalDateTime plannedStart;
//    private LocalDateTime plannedCompletion;
//    private LocalDateTime scheduledStart;
//    private LocalDateTime scheduledEnd;
//    private String customerOrder;
//    private String customer;
//    private String autoRelease;
//    private String pcu;
//    private List<ProductionQuantity> productionQuantities;
//    private String availableQtyToRelease;
//    private List<BatchNumber> serialBatchNumber;
//    private List<CustomData> customDataList;
//    private String parentOrder;
//    private boolean inUse;
//    private String userId;
//    private String parentOrderBO;
//    private String parentPcuBO;
//    private List<String> newPcus;
//    private Map<String,Integer> pcuNumberList;
}
