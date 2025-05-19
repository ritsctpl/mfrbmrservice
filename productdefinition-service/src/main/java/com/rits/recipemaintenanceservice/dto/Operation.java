package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Operation {
    private String operationId;
    private String operationVersion;
    private String operationDescription;
    private String sequence;
    private boolean entryOperation;
    private boolean lastOperationAtPhase;
    private String nextOperations;
    private String type;
    private String instruction;
    private String expectedCycleTime;
    private List<String> tools;
    private List<Resource> resources;
    private List<DataCollection> dataCollection;
    private QualityControlParameter qcParameters;
    private List<Adjustment> adjustments;
    private Boolean ccp;
    private CriticalControlPoints criticalControlPoints;
    private List<ByProduct> byProducts;
    private Ingredients opIngredients;
}
