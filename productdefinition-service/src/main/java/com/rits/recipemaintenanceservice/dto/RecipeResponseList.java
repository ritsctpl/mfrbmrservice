package com.rits.recipemaintenanceservice.dto;

import lombok.*;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecipeResponseList {
    private List<RecipeResponse> recipeList;
}
