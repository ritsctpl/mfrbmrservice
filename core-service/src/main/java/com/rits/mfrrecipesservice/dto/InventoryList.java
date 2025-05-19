package com.rits.mfrrecipesservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class InventoryList {
    private String inventory;
    private double qty;
    private Integer currentSequence;
}
