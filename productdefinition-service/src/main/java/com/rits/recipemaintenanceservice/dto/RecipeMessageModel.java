package com.rits.recipemaintenanceservice.dto;

import com.rits.recipemaintenanceservice.model.Recipes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecipeMessageModel {
    private Recipes response;
    private String result;
    private List<RecipeRequest> responseList;
    private List<Recipes> recipeList;
    private MessageDetails message_details;
    private List<Phase> phases;
    private Ingredients phasesIngredients;
    private List<Operation> ops;
    private Operation op;
    private String opInstruction;
    private PhaseIngredient ingredient;
    private List<AlternateIngredient> alternateIngredients;
    private List<ByProduct> byProducts;
    private List<Waste> waste;
    private Map<String, Object> resultBody;
    private String isVerified;
    private String ingredientId;
    private List<QualityControlParameter> qcParameters;
    private List<DataCollection> dataCollection;
    private List<List<DataCollection>> dataCollections;
    private Map<String, String> phaseOps;
    private Set<String> phaseList;
    private Set<String> operationList;
    private List<Map<String, String>> ingredientDetails;
}

