package com.rits.workflowstatesmasterservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "R_WORKFLOW_STATES_MASTER")
public class WorkFlowStatesMaster {
    @Id
    private String handle;
    private String site;
    private String name;
    private String description;
    private List appliesTo;
    private List editableFields;
    private String entityType;
    private Boolean isEnd;
    private int active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
