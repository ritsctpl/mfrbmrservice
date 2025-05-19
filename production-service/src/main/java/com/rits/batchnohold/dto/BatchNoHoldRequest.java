package com.rits.batchnohold.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchNoHoldRequest {
    private String site;
    private String batchNo;
    private String status;
    private String phaseId;
    private String operation;
    private String resource;
    private String orderNumber;
    private String workcenter;
    private String material;
    private String materialVersion;
    private String recipe;
    private String recipeVersion;
    private BigDecimal qty;
    private String scrapQuantity;
    private String user;
    private String reasonCode;
    private String comment;
    private String batchNoHeaderHandle;
    private String batchNoRecipeHeaderHandle;
    private String baseUom;
    private int active;
}
