package com.rits.logbuyoffservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogbuyOffRequest {
    private String site;
    private String buyOffBO;
    private String pcu;
    private String comments;
    //private String operationBO;
    private String description;
    private String userId;
    //private String itemBO;
    private String router;
    private String routerVersion;
    private String stepId;
    private String shopOrder;
    private String customerOrderBO;
    private String processLotBO;
    private String resourceId;
    private int uniqueId;
    private String quantity;
    private String qtyToComplete;

    private String batchNo;
    private String orderNumber;
    private String recipe;
    private String recipeVersion;
    private String operation;
    private String operationVersion;
    private String item;
    private String itemVersion;
    private String dateRange;
    private String phaseId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
