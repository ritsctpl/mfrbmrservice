package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PhaseIngredient {
    private String ingredientId;
    private String ingredientVersion;
    private String ingreDescription;
    private String quantity;
    private int sequence;
    private String associatedOp;
    private String uom;
    private List<QualityControlParameter> qcParameters;
}
