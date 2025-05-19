package com.rits.recipemaintenanceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EnvironmentalRequirements {
    private String storageTemperature;
    private String humidityRange;
    private boolean protectionFromLight;
}
