package com.rits.worklistservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInWork {
    private String site;
    private String handle;
    private LocalDateTime dateTime;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String resourceBO;
    private String stepID;
    private String userBO;
    private String qtyInWork;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    public java.util.List<String> getFieldNames() {
        java.util.List<String> fieldNames = new ArrayList<>();

        Field[] fields = PcuInWork.class.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }

    public PcuInWork(PcuInQueue pcuInQueue) {
        this.site = pcuInQueue.getSite();
        this.handle = pcuInQueue.getHandle();
        this.dateTime = pcuInQueue.getDateTime();
        this.pcuBO = pcuInQueue.getPcuBO();
        this.itemBO = pcuInQueue.getItemBO();
        this.routerBO = pcuInQueue.getRouterBO();
        this.operationBO = pcuInQueue.getOperationBO();
        this.resourceBO = pcuInQueue.getResourceBO();
        this.stepID = pcuInQueue.getStepID();
        this.userBO = pcuInQueue.getUserBO();
        this.qtyInWork = pcuInQueue.getQtyInWork();
        this.shopOrderBO = pcuInQueue.getShopOrderBO();
        this.childRouterBO = pcuInQueue.getChildRouterBO();
        this.parentStepID = pcuInQueue.getParentStepID();
        this.active = pcuInQueue.getActive();
        this.createdDateTime = pcuInQueue.getCreatedDateTime();
        this.modifiedDateTime = pcuInQueue.getModifiedDateTime();
    }
}
