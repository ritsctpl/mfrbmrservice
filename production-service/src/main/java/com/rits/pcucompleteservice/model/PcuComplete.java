package com.rits.pcucompleteservice.model;

import com.rits.pcucompleteservice.dto.PcuCompleteRequestInfo;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Document(collection = "PCUCOMPLETE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuComplete {
    private String site;
    @Id
    private String handle;
    private LocalDateTime dateTime;
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
    private int active;
    private String workCenter;

    public PcuComplete(PcuCompleteRequestInfo req) {
        this.site = req.getSite();
        this.pcuBO = req.getPcuBO();
        this.itemBO = req.getItemBO();
        this.routerBO = req.getRouterBO();
        this.operationBO = req.getOperationBO();
        this.resourceBO = req.getResourceBO();
        this.stepID = req.getStepID();
        this.userBO = req.getUserBO();
        this.qtyToComplete = req.getQtyToComplete();
        this.qtyCompleted = req.getQtyCompleted();
        this.shopOrderBO = req.getShopOrderBO();
        this.childRouterBO = req.getChildRouterBO();
        this.parentStepID = req.getParentStepID();
        this.workCenter = req.getWorkCenter();
    }
}
