package com.rits.nonconformanceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperationGroups {
    private String validOperations;
    private String dispositionGroups;
    private boolean enabled;
}
