package com.rits.assemblyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class OperationResponse {
    private String operation;
    private String revision;
    private String description;
    private String status;
    private String operationType;
    private boolean currentVersion;
}
