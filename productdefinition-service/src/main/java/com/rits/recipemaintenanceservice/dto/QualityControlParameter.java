package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class QualityControlParameter {
    private String sequence;
    private String qcId;
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
