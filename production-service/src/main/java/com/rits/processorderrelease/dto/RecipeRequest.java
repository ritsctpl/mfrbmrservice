package com.rits.processorderrelease.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class RecipeRequest {
    private String site;
    private String recipeId;
    private String version;
}
