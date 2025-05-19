package com.rits.mfrrecipesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RMIResponseClass {
    private BomPhaseSeperation bomPhaseSeperation;
    private ManufacturingProcedure manufacturing;
    private CriticalControlPoints criticalControlPoints;
    private DosAndDonts dosAndDonts;
    private MfrRecipes mfrRecipes;
    private String dataFieldName;
    private Map<String,Object> trueDataFieldsRecord ;
}
