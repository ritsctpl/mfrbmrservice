package com.rits.workflowstatesmasterservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkFlowStatesResponse {
    private String name;
    private String description;
    private List appliesTo;
    private List editableFields;
    private Boolean isEnd;
    private Boolean isActive;
}
