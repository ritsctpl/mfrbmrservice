package com.rits.worklistservice.dto;

import lombok.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInWorkWithoutBO {
    private String site;
    private String handle;
    private LocalDateTime dateTime;
    private String pcu;
    private String item;
    private String router;
    private String operation;
    private String resource;
    private String stepID;
    private String user;
    private String qtyInWork;
    private String shopOrder;
    private String childRouter;
    private String parentStepID;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

    // Constructor that takes a PcuDoneWithoutBO object
    public PcuInWorkWithoutBO(PcuDoneWithoutBO done) {
        this.site = done.getSite();
        this.handle = done.getHandle();
        this.dateTime = done.getDateTime();
        this.pcu = done.getPcu();
        this.item = done.getItem();
        this.router = done.getRouter();
        this.user = done.getUser();
        this.shopOrder = done.getShopOrder();
        this.active = done.getActive();

        // Set default values or handle remaining fields as needed
        this.operation = "";
        this.resource = "";
        this.stepID = "";
        this.qtyInWork = done.getQtyDone();
        this.childRouter = "";
        this.parentStepID = "";
    }

    // Constructor that takes a PcuInQueueWithoutBO object
    public PcuInWorkWithoutBO(PcuInQueueWithoutBO queue) {
        this.site = queue.getSite();
        this.handle = queue.getHandle();
        this.dateTime = queue.getDateTime();
        this.pcu = queue.getPcu();
        this.item = queue.getItem();
        this.router = queue.getRouter();
        this.operation = queue.getOperation();
        this.resource = queue.getResource();
        this.stepID = queue.getStepID();
        this.user = queue.getUser();
        this.qtyInWork = queue.getQtyInQueue(); // Mapping qtyInQueue to qtyInWork
        this.shopOrder = queue.getShopOrder();
        this.childRouter = queue.getChildRouter();
        this.parentStepID = queue.getParentStepID();
        this.active = queue.getActive();
    }
}