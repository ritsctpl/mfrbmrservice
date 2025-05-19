package com.rits.bmrservice.dto;

import com.rits.mfrrecipesservice.dto.BomPhaseSeperation;
import com.rits.mfrrecipesservice.dto.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RMIResponses {
    private ManufacturingProcedure manufacturing;
    private CriticalControlPoints criticalControlPoints;
    private DosAndDonts dosAndDonts;
    private RawMaterialIndentData rawMaterialIndent;
}
