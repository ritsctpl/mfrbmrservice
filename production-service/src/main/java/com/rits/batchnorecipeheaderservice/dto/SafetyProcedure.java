package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SafetyProcedure {
    private String sequence;
    private String opId;
    private String riskFactor;
    private List<Mitigation> mitigation;
}
