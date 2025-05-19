package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OperatorAction {
    private String sequence;
    private String opId;
    private String action;
    private String reason;
    private boolean approvalRequired;
}
