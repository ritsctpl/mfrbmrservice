package com.rits.recipemaintenanceservice.service;

import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.recipemaintenanceservice.dto.*;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.rits.recipemaintenanceservice.service")
public interface RecipeService {

    RecipeMessageModel createOrUpdateRecipe(RecipeRequests recipeRequest) throws Exception;

    RecipeMessageModel deleteRecipe(RecipeRequests recipeRequest) throws Exception;

    RecipeMessageModel getRecipe(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel addIngredients(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel updateIngredient(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getRecipeIngreList(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel deleteIngredient(RecipeRequests recipeRequest) throws Exception;

    RecipeMessageModel addPhases(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel updatePhase(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel deletePhase(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel trackYield(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel validateRecipeData(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getRecipePhases(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getPhaseOperations(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getPhaseOperationById(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel verifyIngredients(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getAlternateRecipes(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel calculateYield(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getPhaseIngredients(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getOperationInstructions(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getNextOperation(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getNextphase(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getConditionalOperation(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getIngredientsWithVerification(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getPhaseOperationInstructions(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getParallelPhases(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getAnyOrderOperations(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getUomForIngredient(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getIngredientVerificationStatus(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel addPhaseOperation(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel updatePhaseOperation(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel deletePhaseOperation(RecipeRequests recipeRequest) throws Exception;

    RecipeMessageModel addPhaseIngredient(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel updatePhaseIngredient(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel deletePhaseIngredient(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getPhaseOperationDataCollection(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getPhaseCcpDataCollection(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getPhaseOperationsByType(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getPhasesBySite(RecipeRequests recipeRequest) throws Exception;
    RecipeMessageModel getOperationByFilter(RecipeRequests recipeRequest) throws Exception;

    RecipeMessageModel getTop50RecipeList(String site) throws Exception;
    void setRecipeStatus(RecipeRequests recipeRequest) throws Exception;

    AuditLogRequest createAuditLog(RecipeRequests recipeRequest);

    AuditLogRequest updateAuditLog(RecipeRequests recipeRequest);

    AuditLogRequest deleteAuditLog(RecipeRequests recipeRequest);

    RecipeMessageModel recipesList(String site, String recipeId) throws Exception;

    boolean isExist(String site, String recipeId, String version) throws Exception;
    Boolean checkReleasible(RecipeRequests recipeRequest) throws Exception;
}
