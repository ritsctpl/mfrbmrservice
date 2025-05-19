package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Phase {
    private String phaseId;
//    private String phaseName;
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
    private String triggeredPhase;
}
