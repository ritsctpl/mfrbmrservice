package com.rits.batchnorecipeheaderservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document(collection = "R_RECIPE_BATCH_PROCESS")
public class BatchNoRecipeHeader {

    private String site;
    @Id
    private String handle;
    private String batchNo;
    private String batchNoHeaderBO;
    private String orderNo;
    private String material;
    private String materialVersion;
    private String materialDescription;
    private String status;
    private Double batchQty;
    private String recipeName;
    private String recipeVersion;
    private Recipes recipe;

    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;

    private String createdBy;
    private String modifiedBy;

    private int active;
}