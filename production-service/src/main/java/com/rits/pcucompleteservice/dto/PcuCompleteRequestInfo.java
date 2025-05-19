package com.rits.pcucompleteservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuCompleteRequestInfo implements Cloneable{
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
    private String workCenter;
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    public PcuCompleteRequestInfo(PcuCompleteRequestInfo pcuCompleteReqWithBO) {
        this.site = pcuCompleteReqWithBO.site;
        this.pcuBO = pcuCompleteReqWithBO.pcuBO;
        this.itemBO = pcuCompleteReqWithBO.itemBO;
        this.routerBO = pcuCompleteReqWithBO.routerBO;
        this.operationBO = pcuCompleteReqWithBO.operationBO;
        this.resourceBO = pcuCompleteReqWithBO.resourceBO;
        this.stepID = pcuCompleteReqWithBO.stepID;
        this.userBO = pcuCompleteReqWithBO.userBO;
        this.qtyToComplete = pcuCompleteReqWithBO.qtyToComplete;
        this.qtyCompleted = pcuCompleteReqWithBO.qtyCompleted;
        this.shopOrderBO = pcuCompleteReqWithBO.shopOrderBO;
        this.childRouterBO = pcuCompleteReqWithBO.childRouterBO;
        this.parentStepID = pcuCompleteReqWithBO.parentStepID;
        this.nextStepId= pcuCompleteReqWithBO.nextStepId;
        this.workCenter= pcuCompleteReqWithBO.workCenter;
    }


}
