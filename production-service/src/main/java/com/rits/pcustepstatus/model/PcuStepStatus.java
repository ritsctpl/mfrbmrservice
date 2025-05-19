package com.rits.pcustepstatus.model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PcuStepStatus {
    private Integer stepId;
    private String operation;
    private String description;
    private String stepStatus;
    private Integer qtyInQueue;
    private Integer qtyInWork;
    private Integer qtyCompletePending;
    private List<PcuStepStatusDetails> pcuStepStatusDetailsList;
}
