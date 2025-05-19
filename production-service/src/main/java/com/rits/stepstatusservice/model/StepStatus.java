package com.rits.stepstatusservice.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StepStatus {

    private String batchNo;
    private String orderNo;
    List<StepStatusList> stepStatusList;

}
