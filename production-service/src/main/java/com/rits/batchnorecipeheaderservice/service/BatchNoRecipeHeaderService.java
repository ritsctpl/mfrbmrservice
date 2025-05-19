package com.rits.batchnorecipeheaderservice.service;

import com.rits.batchnorecipeheaderservice.dto.BatchNoRecipeHeaderReq;
import com.rits.batchnorecipeheaderservice.dto.RecipeRequests;
import com.rits.batchnorecipeheaderservice.exception.BatchNoRecipeHeaderException;
import com.rits.batchnorecipeheaderservice.model.BatchNoRecipeHeader;
import com.rits.batchnorecipeheaderservice.model.MessageModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface BatchNoRecipeHeaderService {
    MessageModel create(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException;
    MessageModel update(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException;
    MessageModel delete(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException;
    MessageModel retrieve(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException;
    MessageModel retrieveAll(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException;
    MessageModel retrieveTop50(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException;
    MessageModel getRecipesPhaseByFilters(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException;
    MessageModel getBatchPhaseOpByType(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException;
    MessageModel addIngredients(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel updateIngredient(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel deleteIngredient(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;

    MessageModel addPhases(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel updatePhase(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel deletePhase(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel trackYield(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel validateRecipeData(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getRecipePhases(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getPhaseOperations(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getPhaseOperationById(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel verifyIngredients(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getAlternateRecipes(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel calculateYield(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getPhaseIngredients(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getOperationInstructions(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getNextOperation(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getNextphase(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getConditionalOperation(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getIngredientsWithVerification(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getPhaseOperationInstructions(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getParallelPhases(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getAnyOrderOperations(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getUomForIngredient(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getIngredientVerificationStatus(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel addPhaseOperation(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel updatePhaseOperation(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel deletePhaseOperation(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;

    MessageModel addPhaseIngredient(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel updatePhaseIngredient(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel deletePhaseIngredient(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getPhaseOperationDataCollection(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getPhaseCcpDataCollection(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getBatchRecipeFirstPhaseOp(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getBatchRecipeOperations(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getBatchRecipeOpIngredient(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    MessageModel getByBatchRecipeByFilters(BatchNoRecipeHeaderReq recipeRequest) throws BatchNoRecipeHeaderException;
    Map<String, Object> getBatchRecipeFirstPhaseFirstOp(String batchNo, String orderNo, String material, String materialVersion) throws Exception;

    BatchNoRecipeHeader getBySiteAndBatchNo(String site, String batchNo);
    BatchNoRecipeHeader getBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersion(String site, String batchNumber, String orderNumber, String material, String materialVersion);

    List<BatchNoRecipeHeader> getBatchRecipeBySiteAndBatchAndOrder(BatchNoRecipeHeaderReq request) throws Exception;

}
