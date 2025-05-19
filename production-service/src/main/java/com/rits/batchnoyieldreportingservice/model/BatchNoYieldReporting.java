package com.rits.batchnoyieldreportingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document(collection = "R_BATCH_NO_YIELD_REPORTING")
public class BatchNoYieldReporting {

    @Id
    private String handle;
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
