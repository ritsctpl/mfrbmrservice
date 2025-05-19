package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataCollection {
    private String sequence;
    private String dataPointId;
    private String dataPointVersion;
    private String description;
    private String frequency;
    private String expectedValueRange;
    private String parameterName;
    private String expectedValue;
    private String allowedVariance;
    private String monitoringFrequency;
}