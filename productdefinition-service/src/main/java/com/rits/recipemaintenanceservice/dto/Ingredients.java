package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Ingredients {
    private List<ParentIngredient> active;
    private List<ParentIngredient> inactive;
}
