package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BatchNoRecipeHeaderReq {
    private String site;
    private String batchNo;
    private String batchRecipeHandle;
    private String batchNoHeaderBo;
    private String user;
    private Double batchQty;
    private String recipeId;
    private String recipeVersion;
    private String orderNo;
    private String material;
    private String materialVersion;
    private String materialDescription;
    private String status;
    private String phaseId;
    private String operationId;
    private String ingredientId;
    private String operationType;
    private String ingredientType;
    private ParentIngredient parentIngredient;
    private Phase phase;
    private List<Phase> phases;
    private YieldTracking yieldTracking;
    private Ingredients phaseIngredients;
    private String batchSize;
    private Ingredients ingredients;
    private List<Operation> operations;
    private String actualYield;
    private String phaseSequence;
    private String opSequence;
    private  String item;
    private  String itemVersion;
    private String ingreSequence;
    private boolean needMasterData;
}
