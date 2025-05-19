package com.rits.stepstatusservice.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StepStatusList {

    private String phaseId;
    private String operation;
    private BigDecimal inWorkQty;
    private BigDecimal qtyInQueue;//qtyInQueue
    private BigDecimal qtyToComplete;
    private BigDecimal scrapQuantity;
    private String material;
    private String materialDescription;
    private LocalDateTime batchStartedTime;
    private LocalDateTime batchCompletedTime;
    private boolean lineClearanceApproval;
    private String batchStatus;
}
