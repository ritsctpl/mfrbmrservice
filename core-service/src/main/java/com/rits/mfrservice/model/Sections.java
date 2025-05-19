package com.rits.mfrservice.model;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Sections {
    private List<ProductDetails> productDetails;
    private List<BillOfMaterials> billOfMaterials;
    private List<ActiveComposition> activeComposition;
    private List<BriefManufacturingAndPackingProcedure> briefManufacturingAndPackingProcedure;
    private List<ManufacturingFlowChart> manufacturingFlowChart;

}
