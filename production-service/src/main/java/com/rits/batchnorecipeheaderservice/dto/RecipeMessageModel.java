package com.rits.batchnorecipeheaderservice.dto;

import com.rits.batchnorecipeheaderservice.model.Recipes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecipeMessageModel {
    private Recipes response;

}

