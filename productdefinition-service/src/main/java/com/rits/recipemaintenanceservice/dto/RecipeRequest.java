package com.rits.recipemaintenanceservice.dto;

import com.rits.recipemaintenanceservice.model.Phase;
import com.rits.routingservice.model.CustomData;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RecipeRequest {

    private String site;
    private String recipeId;
    //private String recipeName;
    private String version;
    private String batchSize;
    private String batchUom;
}
