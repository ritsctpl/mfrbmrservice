package com.rits.pcuinqueueservice.model;

import com.rits.Utility.BOConverter;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInQueueDetails {
    private String site;
    private String handle;
    private LocalDateTime dateTime;
    private String pcu;
    private String item;
    private String itemVersion;
    private String router;
    private String routerVersion;
    private String operation;
    private String operationVersion;
    private String resource;
    private String stepID;
    private String user;
    private String qtyToComplete;
    private String qtyInQueue;
    private String shopOrder;
    private String childRouter;
    private String childRouterVersion;
    private String parentStepID;
    private String workCenter;
    private String type;
    private int active;

    public PcuInQueueDetails(PcuInQueue pcuInQueue) {

        if (pcuInQueue.getSite() != null && !pcuInQueue.getSite().isEmpty()) {
            this.site = pcuInQueue.getSite();
        }

        if (pcuInQueue.getHandle() != null && !pcuInQueue.getHandle().isEmpty()) {
            this.handle = pcuInQueue.getHandle();
        }

        this.dateTime = pcuInQueue.getDateTime();

        if (pcuInQueue.getPcuBO() != null && !pcuInQueue.getPcuBO().isEmpty()) {
            this.pcu = BOConverter.getPcu(pcuInQueue.getPcuBO());
        }

        if (pcuInQueue.getItemBO() != null && !pcuInQueue.getItemBO().isEmpty()) {
            this.item = BOConverter.getItem(pcuInQueue.getItemBO());
            this.itemVersion = BOConverter.getItemVersion(pcuInQueue.getItemBO());
        }

        if (pcuInQueue.getRouterBO() != null && !pcuInQueue.getRouterBO().isEmpty()) {
            this.router = BOConverter.getRouter(pcuInQueue.getRouterBO());
            this.routerVersion = BOConverter.getRouterVersion(pcuInQueue.getRouterBO());
        }

        if (pcuInQueue.getOperationBO() != null && !pcuInQueue.getOperationBO().isEmpty()) {
            this.operation = BOConverter.getOperation(pcuInQueue.getOperationBO());
            this.operationVersion = BOConverter.getOperationVersion(pcuInQueue.getOperationBO());
        }

        if (pcuInQueue.getResourceBO() != null && !pcuInQueue.getResourceBO().isEmpty()) {
            this.resource = BOConverter.getResource(pcuInQueue.getResourceBO());
        }

        if (pcuInQueue.getStepID() != null && !pcuInQueue.getStepID().isEmpty()) {
            this.stepID = pcuInQueue.getStepID();
        }

        if (pcuInQueue.getUserBO() != null && !pcuInQueue.getUserBO().isEmpty()) {
            this.user = BOConverter.getUser(pcuInQueue.getUserBO());
        }

        if (pcuInQueue.getQtyToComplete() != null && !pcuInQueue.getQtyToComplete().isEmpty()) {
            this.qtyToComplete = pcuInQueue.getQtyToComplete();
        }

        if (pcuInQueue.getQtyInQueue() != null && !pcuInQueue.getQtyInQueue().isEmpty()) {
            this.qtyInQueue = pcuInQueue.getQtyInQueue();
        }

        if (pcuInQueue.getShopOrderBO() != null && !pcuInQueue.getShopOrderBO().isEmpty()) {
            this.shopOrder = BOConverter.getShopOrder(pcuInQueue.getShopOrderBO());
        }

        if (pcuInQueue.getChildRouterBO() != null && !pcuInQueue.getChildRouterBO().isEmpty()) {
            this.childRouter = BOConverter.getChildRouter(pcuInQueue.getChildRouterBO());
            this.childRouterVersion = BOConverter.getChildRouterVersion(pcuInQueue.getChildRouterBO());        }

        if (pcuInQueue.getParentStepID() != null && !pcuInQueue.getParentStepID().isEmpty()) {
            this.parentStepID = pcuInQueue.getParentStepID();
        }

        if (pcuInQueue.getWorkCenter() != null && !pcuInQueue.getWorkCenter().isEmpty()) {
            this.workCenter = pcuInQueue.getWorkCenter();
        }

        this.active = pcuInQueue.getActive();        }
}
