package com.rits.batchnorecipeheaderservice.model;

import com.rits.batchnorecipeheaderservice.dto.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Recipes {

    @Id
    private String handle;

    private String site;
    private String recipeId;
//    private String recipeName;
    private String version;
    private boolean currentVersion;
    private String batchSize;
    private String batchUom;
    private String totalExpectedCycleTime;
    private String totalActualCycleTime;

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

    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private int active;

    private String status;
}

