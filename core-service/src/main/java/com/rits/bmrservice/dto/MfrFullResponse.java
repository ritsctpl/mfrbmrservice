package com.rits.bmrservice.dto;

import com.rits.mfrrecipesservice.dto.BomPhaseSeperation;
import com.rits.mfrrecipesservice.dto.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MfrFullResponse implements Serializable {
    private BomPhaseSeperation bomPhaseSeperation;
    private ManufacturingProcedure manufacturing;
    private CriticalControlPoints criticalControlPoints;
    private DosAndDonts dosAndDonts;
    private MfrRecipes mfrRecipes;
    private RawMaterialIndentData rawMaterialIndentList;
    private Map<String,Object> trueDataFieldsRecord;
}
