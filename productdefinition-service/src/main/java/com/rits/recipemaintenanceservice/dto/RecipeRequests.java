package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RecipeRequests {

    private String site;
    private String handle;
    private String recipeId;
    private String recipeDescription;
    private String version;
    private String batchSize;
    private String batchUom;
    private String totalExpectedCycleTime;
    private String totalActualCycleTime;
    private String user;
    private String operationType;
    private String status;
    private boolean currentVersion;
    private int active;

    private Scaling scaling;
    private Ingredients ingredients;
    private List<Phase> phases;
    private List<WorkCenter> workCenters;
    private List<TriggerPoint> triggerPoints;
    private YieldTracking yieldTracking;
    private PackagingAndLabeling packagingAndLabeling;
    private List<Adjustment> adjustments;
    private List<SafetyProcedure> safetyProcedures;
    private List<OperatorAction> operatorActions;
    private Compliance compliance;
    private String phaseId;
    private String operationId;
    private String opVersion;
    private String ingredientId;
    private String actualYield;
    private boolean condition;
    private List<Operation> operations;
    private Phase phase;
    private ParentIngredient parentIngredient;
    private String ingredientType;
    private String ingreSequence;
    private String phaseSequence;
    private String opSequence;

    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String modifiedBy;

}
