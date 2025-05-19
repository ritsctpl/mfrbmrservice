package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AlternateIngredient {
    private String sequence;
    private String ingredientId;
    private String ingredientVersion;
    private String ingreDescription;
    private double quantity;
    private String uom;
    private String tolerance;
    private String materialDescription;
    private String storageLocation;
    private String materialType;
    private String batchNumber;
    private LocalDate expiryDate;
    private LocalDate manufactureDate;
    private List<QualityControlParameter> qcParameters;
    private String unitCost;
    private String totalCost;
}
