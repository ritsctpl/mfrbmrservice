package com.rits.recipemaintenanceservice.model;

import com.rits.recipemaintenanceservice.dto.*;
import com.rits.recipemaintenanceservice.dto.Phase;
import com.rits.routingservice.model.CustomData;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_RECIPE")
public class Recipes {

    @Id
    private String handle;

    private String recipeId;
    private String site;
    private String recipeDescription;
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

