package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.time.LocalDate;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ParentIngredient {
    private String ingredientId;
    private String ingredientVersion;
    private String ingreDescription;
    private String quantity;
    private String uom;
    private String sequence;
//    private String materialDescription;
    private String storageLocation;
    private String tolerance;
    private String materialType;
    private String supplierId;
    private String sourceLocation;
    private List<QualityControlParameter> qcParameters;
    private String handlingInstructions;
    private String storageInstructions;
    private String unitCost;
    private String currency;
    private String totalCost;
    private String wasteQuantity;
    private String wasteUoM;
    private String batchNumber;
    private ByProduct byProduct;
    private Boolean hazardous;
    private List<AlternateIngredient> alternateIngredients;
    private String operationId;
    private String operationVersion;

    private LocalDate expiryDate;
    private LocalDate manufactureDate;
}
