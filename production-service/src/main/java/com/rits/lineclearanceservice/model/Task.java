package com.rits.lineclearanceservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private String taskId;
    private String taskName;
    private String taskDescription;
    private Boolean isMandatory;
    private Boolean evidenceRequired;
    private String clearanceTimeLimit;
}