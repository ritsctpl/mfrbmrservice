package com.rits.batchnoscrap.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchNoScrapRequest {
    private String site;
    private String object;
    private String batchNo;
    private String status;
    private String phaseId;
    private String operation;
   // private String operationVersion;
    private String resource;
    private String workcenter;
    private String orderNumber;
   // private String processLot;
    private String material;
    private String materialVersion;
    private String recipe;
    private String recipeVersion;
    private BigDecimal scrapQuantity;
    private BigDecimal theoreticalYield;
    private BigDecimal actualYield;
    private String user;
    private String reasonCode;
    private String comment;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String batchNoHeaderHandle;
    private String batchNoRecipeHeaderHandle;

}
