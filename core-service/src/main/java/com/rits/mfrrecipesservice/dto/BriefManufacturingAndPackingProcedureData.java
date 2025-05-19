package com.rits.mfrrecipesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BriefManufacturingAndPackingProcedureData {
    private String phase;
    private String procedureDescription;
    private String observation;
    private String criticalControlPoints;
    private String dosAndDonts;
    private String materialDescription;
    private String quantity;
}
