package com.rits.recipemaintenanceservice.dto;

import com.rits.recipemaintenanceservice.model.Phase;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeResponse {
    private String recipeName;
    private String version;
    private String recipeType;
    private String description;
    private String status;
    private boolean currentVersion;
}
