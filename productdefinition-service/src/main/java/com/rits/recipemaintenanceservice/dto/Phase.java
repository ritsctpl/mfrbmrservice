package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Phase {
    private String phaseId;
    private String phaseDescription;
    private String sequence;
    private boolean entryPhase;
    private boolean exitPhase;
    private String nextPhase;
    private String expectedCycleTime;
    private Ingredients ingredients;// optional
    private List<Operation> operations;
    private Boolean conditional;
    private Boolean parallel;
    private Boolean anyOrder;
    private Boolean sequential;
    private String triggeredPhase;

    public Phase(String sequence, String phaseId){
        this.sequence = sequence;
        this.phaseId = phaseId;
    }
}
