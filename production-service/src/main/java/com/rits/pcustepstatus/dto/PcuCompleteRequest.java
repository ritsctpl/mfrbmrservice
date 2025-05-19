package com.rits.pcustepstatus.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuCompleteRequest {
    private String site;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String resourceBO;
    private String stepID;
    private String userBO;
    private String qtyToComplete;
    private String qtyCompleted;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
    private String nextStepId;
    private Boolean parallel;
    public PcuCompleteRequest(PcuCompleteRequest pcuCompleteRequest) {
        this.site = pcuCompleteRequest.site;
        this.pcuBO = pcuCompleteRequest.pcuBO;
        this.itemBO = pcuCompleteRequest.itemBO;
        this.routerBO = pcuCompleteRequest.routerBO;
        this.operationBO = pcuCompleteRequest.operationBO;
        this.resourceBO = pcuCompleteRequest.resourceBO;
        this.stepID = pcuCompleteRequest.stepID;
        this.userBO = pcuCompleteRequest.userBO;
        this.qtyToComplete = pcuCompleteRequest.qtyToComplete;
        this.qtyCompleted = pcuCompleteRequest.qtyCompleted;
        this.shopOrderBO = pcuCompleteRequest.shopOrderBO;
        this.childRouterBO = pcuCompleteRequest.childRouterBO;
        this.parentStepID = pcuCompleteRequest.parentStepID;
        this.nextStepId=pcuCompleteRequest.nextStepId;
        this.parallel=pcuCompleteRequest.parallel;
    }


}
