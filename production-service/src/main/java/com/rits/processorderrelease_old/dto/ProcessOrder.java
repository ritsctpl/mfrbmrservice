package com.rits.processorderrelease_old.dto;


import com.rits.pcuheaderservice.dto.SerialPcu;
import com.rits.worklistservice.dto.ShopOrder;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProcessOrder {
    private String site;
    @Id
    private String handle;
    private String processOrder;
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
    //private List<CustomData> customDataList;
    private String parentOrder;
    private String releasedQty;
    private String parentOrderBO;
    private String parentPcuBO;
    private String availableQtyToRelease;
    private boolean inUse;
    private String createdBy;
    private String modifiedBy;
    private String processOrderBO;
    private String itemBO;
    private String bomBO;
    private String routingBO;
    private String workCenterBO;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private List<String> newBnos;
    private  Map<String, Integer> bnoNumberList;
    public java.util.List<String> getFieldNames() {
        java.util.List<String> fieldNames = new ArrayList<>();

        Field[] fields = ShopOrder.class.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }
}
