package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ByProduct {
    private String sequence;
    private String byProductId;
//    private String byProductName;
    private String description;
    private String expectedQuantity;
    private String uom;
    private String handlingProcedure;
    private String byProductQuantity;
    private String reusable;
    private String disposalCost;
    private String currency;
    private String quantityProduced;
}