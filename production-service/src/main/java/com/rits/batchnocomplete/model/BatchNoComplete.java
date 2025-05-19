package com.rits.batchnocomplete.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Document(collection = "R_BATCHNO_COMPLETE")
public class BatchNoComplete {

    @Id
    private String handle;
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
    private String baseUom;
    private String measuredUom;
    private LocalDateTime queuedTimestamp;
    private String resource;
    private String user;
    private BigDecimal qtyToComplete;
    private BigDecimal yieldQuantityBaseUom;
    private BigDecimal yieldQuantityMeasuredUom;
    private BigDecimal scrapQuantityBaseUom;
    private BigDecimal scrapQuantityMeasuredUom;
    private String orderNumber;
    private boolean qualityApproval;
    private String workcenter;

    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
}
