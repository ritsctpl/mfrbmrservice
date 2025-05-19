package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PhaseIngredient {
    private String ingredientId;
    private String ingreDescription;
    private String quantity;
    private int sequence;
    private String associatedOp;
    private String uom;
    private List<QualityControlParameter> qcParameters;
}
