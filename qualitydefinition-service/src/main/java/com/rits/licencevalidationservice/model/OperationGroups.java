package com.rits.licencevalidationservice.model;

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
