package com.rits.mfrrecipesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sections {
    private List<ProductDetails> productDetails;
    private List<BillOfMaterials> billOfMaterials;
    private List<ActiveComposition> activeComposition;
    private List<BomPhaseSeperation> briefManufacturingAndPackingProcedure;
}
