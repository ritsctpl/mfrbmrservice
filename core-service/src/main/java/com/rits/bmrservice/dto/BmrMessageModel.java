package com.rits.bmrservice.dto;

import com.rits.mfrrecipesservice.dto.BomPhaseSeperation;
import com.rits.mfrrecipesservice.dto.MfrRecipes;
import com.rits.mfrscreenconfigurationservice.model.MFRRefList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BmrMessageModel {
    private RawMaterialIndentData rawMaterialIndent;
    private MessageDetails message_details;
    private MfrRecipes mfrFullRecord;
    private List<MFRRefList> mrfRefList;
    private BomPhaseSeperation bomPhaseSeperation;
    private BmrRecipes bmrFullRecord;
    private BmrRecipes response;
    private Map<String, List<String>> referenceId;
}
