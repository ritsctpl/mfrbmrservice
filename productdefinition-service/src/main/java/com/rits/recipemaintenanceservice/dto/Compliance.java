package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Compliance {
    private List<String> regulatoryAgencies;
    private boolean auditRequired;
}
