package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Adjustment {
    private String sequence;
    private String adjustmentId;
    private String adjustmentType;
    private String reason;
    private String impactOnProcess;
    private String impactOnYield;
    private String effectOnCycleTime;
    private String effectOnQuality;
}
