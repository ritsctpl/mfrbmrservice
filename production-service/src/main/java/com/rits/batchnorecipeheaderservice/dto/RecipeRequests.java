package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RecipeRequests {

    private String site;
    private String recipeId;
//    private String recipeName;
    private String version;
}
