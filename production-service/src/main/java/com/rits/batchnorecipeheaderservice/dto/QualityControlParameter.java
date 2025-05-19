package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class QualityControlParameter {
    private String sequence;
    private String qcId;
//    private String qcName;
    private String qcDescription;
    private String parameter;
    private String actualValue;
    private String expectedValue;
    private String monitoringFrequency;
    private List<String> toolsRequired;
    private String actionsOnFailure;
    private String tolerance;
    private Double min;
    private Double max;
}
