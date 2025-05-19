package com.rits.recipemaintenanceservice.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Phase {
    private int phaseNumber;
    private String phaseID;
    private String description;
    private String operationActivityMaster;
    private String operationGroup;
    private String workCenter;
    private String productionProcess;
    private boolean entryActivity;
    private boolean reworkActivity;
    private boolean lastReportingActivity;
    private boolean weighingRelevant;
    private String queueDecision;
    private int baseQty;
    private String controlKey;
    private String generalInformation;
    private List<WorkInstruction> workInstructions;
    private List<DataCollection> dataCollections;
    private List<ParameterList > parameterLists;
}
