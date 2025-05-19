package com.rits.pcustepstatus.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StartRequest {
    private String site;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String qtyInWork;
    private String resourceBO;
    private String workCenter;
    private String quantity;
    private String qtyToComplete;
    private String stepID;
    private String userBO;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
//    private String stepId;

    public StartRequest(StartRequest startRequest) {
        this.site = startRequest.site;
        this.pcuBO = startRequest.pcuBO;
        this.itemBO = startRequest.itemBO;
        this.routerBO = startRequest.routerBO;
        this.operationBO = startRequest.operationBO;
        this.qtyInWork=startRequest.qtyInWork;
        this.resourceBO = startRequest.resourceBO;
        this.workCenter=startRequest.workCenter;
        this.stepID = startRequest.stepID;
        this.quantity=startRequest.quantity;
        this.userBO = startRequest.userBO;
        this.qtyToComplete = startRequest.qtyToComplete;
//        this.stepId=startRequest.stepId;
        this.shopOrderBO = startRequest.shopOrderBO;
        this.childRouterBO = startRequest.childRouterBO;
        this.parentStepID = startRequest.parentStepID;
    }



}
