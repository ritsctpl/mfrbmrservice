package com.rits.startservice.dto;

import com.rits.startservice.model.PcuInWork;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StartRequest implements Serializable, Cloneable{
    private String site;
    private String handle;
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
    private String status;
    private int recordLimit;
    private List<PcuInWork> inWorkList;
    private Boolean disable;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
@Override
public Object clone() throws CloneNotSupportedException {
    return super.clone();
}
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
        this.status=startRequest.status;
        this.handle=startRequest.handle;
        this.createdDateTime=startRequest.createdDateTime;
        this.modifiedDateTime=startRequest.modifiedDateTime;
    }
//


}
