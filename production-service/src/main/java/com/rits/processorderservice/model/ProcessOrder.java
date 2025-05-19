package com.rits.processorderservice.model;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "R_PROCESS_ORDER")
public class ProcessOrder{
    @Id
    private String handle;
    private String orderNumber;
    private String site;
    private String status;
    private String material;
    private String materialVersion;
    private String materialDescription;
    private String recipe;
    private String recipeVersion;
    private String orderType;
    private String mrpController;
    private String productionScheduler;
    private String reservationNumber;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;
    private LocalDateTime productionStartDate;
    private LocalDateTime productionFinishDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualFinishDate;
    private String scrap;
    private String workCenter;
    private Integer targetQuantity;
    private BigDecimal deliveredQuantity;
    private String uom;
    private String measuredUom;
    private BigDecimal conversionFactor;
    private String priority;
    private String enteredBy;
    private LocalDateTime enterDate;
    private String deletionFlag;
    private String wbsElement;
    private String confNo;
    private String confCnt;
    private String intObjNo;
    private LocalDateTime schedFinTime;
    private LocalDateTime schedStartTime;
    private String collectiveOrder;
    private String orderSeqNo;
    private String finishTime;
    private String startTime;
    private String actualStartTime;
    private String leadingOrder;
    private String salesOrder;
    private String salesOrderItem;
    private String prodSchedProfile;
    private String materialText;
    private String systemStatus;
    private BigDecimal confirmedQuantity;
    private String planSite;
    private String batch;
    private String storageLocation;
    private boolean deliveryComplete;
    private String productionVersion;
    private String mrpArea;
    private String materialExternal;
    private String materialGuid;
    private String materialLong;
    private LocalDateTime dateOfExpiry;
    private LocalDateTime dateOfManufacture;
    private List<BatchNumber> batchNumber;
    private BigDecimal availableQtyToRelease;
    private boolean inUse;
    private int active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}


































//@Document(collection = "R_PROCESS_ORDER")
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//@Builder
//public class ProcessOrder {
//    private String site;
//    @Id
//    private String handle;
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
//    private List<ProductionQuantity> productionQuantities;
//    private List<SerialBatchNumber> serialBatchNumber;
//    private List<CustomData> customDataList;
//    private String parentOrder;
//    private String releasedQty;
//    private String parentOrderBO;
//    private String parentPcuBO;
//    private String availableQtyToRelease;
//    private boolean inUse;
//    private String createdBy;
//    private String modifiedBy;
//    private String processOrderBO;
//    private String itemBO;
//    private String bomBO;
//    private String routingBO;
//    private String workCenterBO;
//    private int active;
//    private LocalDateTime createdDateTime;
//    private LocalDateTime modifiedDateTime;
//}


