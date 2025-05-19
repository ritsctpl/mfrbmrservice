package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BatchRecipeResponse {
    private String batchNo;
    private String item;
    private String itemVersion;
    private String recipe;
    private String recipeVersion;
    private BigDecimal quantity;
    private String processOrder;
    private String status;
}
