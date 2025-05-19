package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class YieldTracking {
    private String expectedYield;
    private String allowedVariance;
    private String actualYield;
    private List<ByProduct> byProducts;
    private List<Waste> waste;
    private List<Correction> corrections;
    private List<QualityDeviation> qualityDeviations;
}
