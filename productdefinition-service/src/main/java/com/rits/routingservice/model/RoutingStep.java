package com.rits.routingservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoutingStep {
    private String stepId;
    private String stepType;
    private String operation;
    private String shopordercount;
    private String pcucount;
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
    private boolean reworkStep;
    private boolean blockPcusUntilInspectionFinished;
    private String nextStepId;
    private List<Integer> childStepId;
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
