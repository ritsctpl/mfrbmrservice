package com.rits.worklistservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuDone {
    private String site;
    private String handle;
    private String pcuBO;
    private LocalDateTime dateTime;
    private String itemBO;
    private String routerBO;
    private String userBO;
    private String qtyDone;
    private String shopOrderBO;
    private int active;
    public java.util.List<String> getFieldNames() {
        java.util.List<String> fieldNames = new ArrayList<>();

        Field[] fields = PcuInQueue.class.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }

    public PcuDone(PcuInQueue pcuInQueue) {
        this.site = pcuInQueue.getSite();
        this.handle = pcuInQueue.getHandle();
        this.pcuBO = pcuInQueue.getPcuBO();
        this.dateTime = pcuInQueue.getDateTime();
        this.itemBO = pcuInQueue.getItemBO();
        this.routerBO = pcuInQueue.getRouterBO();
        this.userBO = pcuInQueue.getUserBO();
        this.qtyDone = pcuInQueue.getQtyInQueue();
        this.shopOrderBO = pcuInQueue.getShopOrderBO();
        this.active = pcuInQueue.getActive();
    }
}
