package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PackagingAndLabeling {
    private String packagingType;
    private String primaryPackagingMaterial;
    private String secondaryPackagingType;
    private String containerSize;
    private String labelFormat;
    private List<String> labelingRequirements;
    private List<String> labelLanguage;
    private List<String> complianceStandards;
    private String instructions;
    private EnvironmentalRequirements environmentalRequirements;
}
