package com.rits.scrapservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuCompleteRequest {
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
    private String shopOrder;
    private String childRouter;
    private String childRouterVersion;
    private String parentStepID;
    private String nextStepId;
    private Boolean parallel;
    private String workCenter;
//    public PcuCompleteRequest(PcuCompleteRequest pcuCompleteRequest) {
//        this.site = pcuCompleteRequest.site;
//        this.pcuBO = pcuCompleteRequest.pcuBO;
//        this.itemBO = pcuCompleteRequest.itemBO;
//        this.routerBO = pcuCompleteRequest.routerBO;
//        this.operationBO = pcuCompleteRequest.operationBO;
//        this.resourceBO = pcuCompleteRequest.resourceBO;
//        this.stepID = pcuCompleteRequest.stepID;
//        this.userBO = pcuCompleteRequest.userBO;
//        this.qtyToComplete = pcuCompleteRequest.qtyToComplete;
//        this.qtyCompleted = pcuCompleteRequest.qtyCompleted;
//        this.shopOrderBO = pcuCompleteRequest.shopOrderBO;
//        this.childRouterBO = pcuCompleteRequest.childRouterBO;
//        this.parentStepID = pcuCompleteRequest.parentStepID;
//        this.nextStepId=pcuCompleteRequest.nextStepId;
//        this.workCenter=pcuCompleteRequest.workCenter;
//    }
//

}
