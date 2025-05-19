package com.rits.mfrrecipesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComponentList {
    private String materialCode;
    private String calculatedValue;
    private String operation;
    private String dataCollection;
}