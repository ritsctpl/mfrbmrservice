package com.rits.batchnoinwork.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BatchNoInWorkRequest {
    private String site;
    private LocalDateTime dateTime;
    private String batchNo;
    private String batchNoHeaderBO;
    private String material;
    private String materialVersion;
    private String recipe;
    private String recipeVersion;
    private String batchNoRecipeHeaderBO;
    private String phaseId;
    private String operation;
    private BigDecimal quantityBaseUom;
    private BigDecimal quantityMeasuredUom;
    private String resource;
    private String workcenter;
    private String baseUom;
    private String user;
    private BigDecimal qtyToComplete;
    private BigDecimal qtyInQueue;
    private String orderNumber;
    private boolean qualityApproval;
    private String measuredUom;
    private LocalDateTime queuedTimestamp;
    private int active;
    private int maxRecord;

}
