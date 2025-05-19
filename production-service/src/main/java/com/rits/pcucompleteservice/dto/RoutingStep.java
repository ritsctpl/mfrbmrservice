package com.rits.pcucompleteservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RoutingStep {
    private String stepId;
    private String stepType;
    private String operation;
    private String operationVersion;
    private String operationDescription;
    private String routingBO;
    private String stepDescription;
    private String maximumLoopCount;
    private String erpControlKey;
    private String erpOperation;
    private String workCenter;
    private String erpWorkCenter;
    private String requiredTimeInProcess;
    private String specialInstruction;
    private String queueDecision;
    private String erpSequence;
    private boolean isLastReportingStep;
    private String reworkStep;
    private boolean blockPcusUntilInspectionFinished;
    private String nextStepId;
    private String previousStepId;
    private boolean entryStep;
    private boolean parentStep;
    private String qtyInWork;
    private String qtyInQueue;
    private String qtyInHold;
    private String qtyInPending;
    private String qtyRejected;
    private String byPassed;
    private String timeProcessed;
    private String maxLoop;
    private String useAsRework;
    private String previouslyStarted;
    private String dateQueued;
    private List<Routing> routerDetails;
    private String subType;
    private String needToBeCompleted;
}
