package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CriticalControlPoints {
    private String ccpId;
//    private String ccpName;
    private String description;
    private String criticalLimits;
    private String monitoringFrequency;
    private String correctiveAction;
}
