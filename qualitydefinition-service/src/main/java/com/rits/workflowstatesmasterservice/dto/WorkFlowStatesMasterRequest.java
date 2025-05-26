package com.rits.workflowstatesmasterservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkFlowStatesMasterRequest {
    private String site;
    private String name;
    private String description;
    private List appliesTo;
    private List editableFields;
    private String entityType;
    private Boolean isEnd;
    private String userId;
}
