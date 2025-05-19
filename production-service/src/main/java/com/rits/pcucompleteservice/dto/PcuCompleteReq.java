package com.rits.pcucompleteservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
//@RequiredArgsConstructor
public class PcuCompleteReq implements Cloneable{
    private String site;
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
    private String qtyCompleted;
    private String qtyInWork;
    private String shopOrder;
    private String childRouter;
    private String childRouterVersion;
    private String parentStepID;
    private String nextStepId;
    private Boolean parallel;
    private String workCenter;
    private LocalDateTime dateTime;
    private String handle;
    private int active;
    private String qtyInQueue;
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
//    public PcuCompleteReq(PcuCompleteReqWithBO PcuCompleteReq) {
//        this.site = PcuCompleteReq.getSite();
//        this.pcu = PcuCompleteReq.getPcuBO();
//        this.item = PcuCompleteReq.getItemBO();
//        this.router = PcuCompleteReq.getRouterBO();
//        this.operation = PcuCompleteReq.getOperationBO();
//        this.resource = PcuCompleteReq.getResourceBO();
//        this.stepID = PcuCompleteReq.getStepID();
//        this.user = PcuCompleteReq.getUserBO();
//        this.qtyToComplete = PcuCompleteReq.getQtyToComplete();
//        this.qtyCompleted = PcuCompleteReq.getQtyCompleted();
//        this.shopOrder = PcuCompleteReq.getShopOrderBO();
//        this.childRouter = PcuCompleteReq.getChildRouterBO();
//        this.parentStepID = PcuCompleteReq.getParentStepID();
//        this.nextStepId= PcuCompleteReq.getNextStepId();
//        this.workCenter= PcuCompleteReq.getWorkCenter();
//    }

}