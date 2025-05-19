package com.rits.recipemaintenanceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CriticalControlPoints {
    private String sequence;
    private String ccpId;
    private String description;
    private String criticalLimits;
    private String monitoringFrequency;
    private String correctiveAction;
}
