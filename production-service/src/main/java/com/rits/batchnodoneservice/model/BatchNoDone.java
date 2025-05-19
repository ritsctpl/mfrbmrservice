package com.rits.batchnodoneservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document(collection = "R_BATCH_NO_DONE")
public class BatchNoDone {

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
    private String workcenter;
    private String phaseId;
    private String operation;
    private BigDecimal quantityBaseUom;
    private BigDecimal quantityMeasuredUom;
    private String baseUom;
    private String measuredUom;
    private LocalDateTime queuedTimestamp;
    private String resource;
    private String user;
    private BigDecimal qtyDone;
    private BigDecimal doneQuantityBaseUom;
    private BigDecimal doneQuantityMeasuredUom;
    private BigDecimal scrapQuantityBaseUom;
    private BigDecimal scrapQuantityMeasuredUom;
    private String orderNumber;
    private boolean qualityApproval;
    private String productionVersion;
    private String type;
    private Integer active;

    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
}
