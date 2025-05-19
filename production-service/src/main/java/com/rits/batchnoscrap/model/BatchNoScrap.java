package com.rits.batchnoscrap.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_BATCH_NO_SCRAP")
public class BatchNoScrap {
    @Id
    private String handle;
    private String scrapBO;
    private String site;
    private String batchNo;
    private String status;
    private String phaseId;
    private String operation;
    private String resource;
    private String orderNumber;
    private String workcenter;
    //private String processLot;
    private String material;
    private String materialVersion;
    private String recipe;
    private String recipeVersion;
    private BigDecimal scrapQuantity;
    private String user;
    private String reasonCode;
    private String comment;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int active;
    private String batchNoHeaderHandle;
    private String batchNoRecipeHeaderHandle;

}
