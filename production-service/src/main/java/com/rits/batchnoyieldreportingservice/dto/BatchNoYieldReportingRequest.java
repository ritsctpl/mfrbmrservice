package com.rits.batchnoyieldreportingservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BatchNoYieldReportingRequest {

    private String site;
    //private String batchId;
    private String batchNo;
    private String orderNumber;
    private String recipe;
    private String recipeVersion;
    private String batchNoRecipeHeaderBO;
    private String phaseId;
    private String operation;
    private String resource;
    private BigDecimal theoreticalYieldBaseUom;
    private BigDecimal theoreticalYieldMeasuredUom;
    private BigDecimal actualYieldBaseUom;
    private BigDecimal actualYieldMeasuredUom;
    private BigDecimal yieldVarianceBaseUom;
    private BigDecimal yieldVarianceMeasuredUom;
    private BigDecimal scrapQuantityBaseUom;
    private BigDecimal scrapQuantityMeasuredUom;
    private String baseUom;
    private String measuredUom;
    private String user;
    private LocalDateTime reportTimestamp;
    private int active;

    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
}
