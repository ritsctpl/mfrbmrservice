package com.rits.batchnorecipeheaderservice.controller;

import com.rits.batchnorecipeheaderservice.dto.BatchNoRecipeHeaderReq;
import com.rits.batchnorecipeheaderservice.exception.BatchNoRecipeHeaderException;
import com.rits.batchnorecipeheaderservice.model.BatchNoRecipeHeader;
import com.rits.batchnorecipeheaderservice.model.MessageModel;
import com.rits.batchnorecipeheaderservice.service.BatchNoRecipeHeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/recipebatchprocess-service")
public class BatchNoRecipeHeaderController {

    @Autowired
    private final BatchNoRecipeHeaderService batchProcessService;

    @PostMapping("/create")
    public MessageModel create(@RequestBody BatchNoRecipeHeaderReq request)
    {
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoRecipeHeaderException(113);

        try {
            return batchProcessService.create(request);
        } catch (BatchNoRecipeHeaderException batchProcessException) {
            throw batchProcessException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("update")
    public MessageModel update(@RequestBody BatchNoRecipeHeaderReq request)
    {
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoRecipeHeaderException(113);

        try {
            return batchProcessService.update(request);
        } catch (BatchNoRecipeHeaderException batchProcessException) {
            throw batchProcessException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("delete")
    public MessageModel delete(@RequestBody BatchNoRecipeHeaderReq request)
    {
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoRecipeHeaderException(113);

        try {
            return batchProcessService.delete(request);
        } catch (BatchNoRecipeHeaderException batchProcessException) {
            throw batchProcessException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieve")
    public MessageModel retrieve(@RequestBody BatchNoRecipeHeaderReq request)
    {
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoRecipeHeaderException(113);

        try {
            return batchProcessService.retrieve(request);
        } catch (BatchNoRecipeHeaderException batchProcessException) {
            throw batchProcessException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveAll")
    public MessageModel retrieveAll(@RequestBody BatchNoRecipeHeaderReq request)
    {
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoRecipeHeaderException(113);

        try {
            return batchProcessService.retrieveAll(request);
        } catch (BatchNoRecipeHeaderException batchProcessException) {
            throw batchProcessException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveTop50")
    public MessageModel retrieveTop50(@RequestBody BatchNoRecipeHeaderReq request)
    {
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoRecipeHeaderException(113);

        try {
            return batchProcessService.retrieveTop50(request);
        } catch (BatchNoRecipeHeaderException batchProcessException) {
            throw batchProcessException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getRecipesPhaseByFilters")
    public MessageModel getRecipesPhaseByFilters(@RequestBody BatchNoRecipeHeaderReq request)
    {
        if(StringUtils.isEmpty(request.getSite()))
            throw new BatchNoRecipeHeaderException(113);

        try {
            return batchProcessService.getRecipesPhaseByFilters(request);
        } catch (BatchNoRecipeHeaderException batchProcessException) {
            throw batchProcessException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/addBatchRecipeIngredients")
    public ResponseEntity<MessageModel> addIngredients(@RequestBody BatchNoRecipeHeaderReq request) {
        MessageModel recipe;
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getIngredientType()) || StringUtils.isEmpty(request.getUser()) || request.getParentIngredient() == null ) {
            throw new BatchNoRecipeHeaderException(156);
        }
        try {
            recipe = batchProcessService.addIngredients(request);
            return ResponseEntity.ok(recipe);
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    @PutMapping("/updateIngredientInBatchRecipe")
    public ResponseEntity<MessageModel> updateIngredient(@RequestBody BatchNoRecipeHeaderReq request) {
        MessageModel recipe;
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getIngredientType()) || StringUtils.isEmpty(request.getIngredientId())
                || StringUtils.isEmpty(request.getUser()) || StringUtils.isEmpty(request.getIngreSequence()) || request.getParentIngredient() == null) {
            throw new BatchNoRecipeHeaderException(162);
        }
        try {
            recipe = batchProcessService.updateIngredient(request);
            return ResponseEntity.ok(recipe);
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/deleteIngredientInBatchRecipe")
    public ResponseEntity<MessageModel> deleteIngredient(@RequestBody BatchNoRecipeHeaderReq request) {
        MessageModel recipe;
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getIngredientId()) || StringUtils.isEmpty(request.getUser()) || StringUtils.isEmpty(request.getIngreSequence()) ) {
            throw new BatchNoRecipeHeaderException(157);
        }
        try {
            recipe = batchProcessService.deleteIngredient(request);
            return ResponseEntity.ok(recipe);
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/addPhasesInBatchRecipe")
    public ResponseEntity<MessageModel> addPhases(@RequestBody BatchNoRecipeHeaderReq request) {
        MessageModel recipe;
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseSequence()) || request.getPhase() == null || StringUtils.isEmpty(request.getUser()) ) {
            throw new BatchNoRecipeHeaderException(131);
        }
        try {
            recipe = batchProcessService.addPhases(request);
            return ResponseEntity.ok(recipe);
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/updatePhaseInBatchRecipe")
    public ResponseEntity<MessageModel> updatePhase(@RequestBody BatchNoRecipeHeaderReq request) {
        MessageModel recipe;
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseSequence()) || request.getPhase() == null
                || StringUtils.isEmpty(request.getUser()) ) {
            throw new BatchNoRecipeHeaderException(131);
        }
        try {
            recipe = batchProcessService.updatePhase(request);
            return ResponseEntity.ok(recipe);
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/deletePhaseInBatchRecipe")
    public ResponseEntity<MessageModel> deletePhase(@RequestBody BatchNoRecipeHeaderReq request) {
        MessageModel recipe;
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getUser()) ) {
            throw new BatchNoRecipeHeaderException(132);
        }
        try {
            recipe = batchProcessService.deletePhase(request);
            return ResponseEntity.ok(recipe);
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/trackYieldInBatchRecipe")
    public ResponseEntity<MessageModel> trackYield(@RequestBody BatchNoRecipeHeaderReq request) {
        MessageModel recipe;
        if (StringUtils.isEmpty(request.getSite()) || request.getYieldTracking() == null || StringUtils.isEmpty(request.getUser())) {
            throw new BatchNoRecipeHeaderException(133);
        }
        try {
            recipe = batchProcessService.trackYield(request);
            return ResponseEntity.ok(recipe);
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/validateRecipeInBatchRecipe")
    public ResponseEntity<MessageModel> validateRecipe(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getIngredientId()) || StringUtils.isEmpty(request.getPhaseId())
                || request.getPhaseIngredients() == null || request.getIngreSequence() == null || request.getPhaseSequence() == null) {
            throw new BatchNoRecipeHeaderException(134);
        }
        try {
            return ResponseEntity.ok(batchProcessService.validateRecipeData(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchRecipePhases")
    public ResponseEntity<MessageModel> getRecipePhases(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite())) {
            throw new BatchNoRecipeHeaderException(113);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getRecipePhases(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/getBatchRecipePhaseOps")
    public ResponseEntity<MessageModel> getPhaseOperations(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getPhaseSequence())) {
            throw new BatchNoRecipeHeaderException(140);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getPhaseOperations(request));

        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchRecipePhaseOpById")
    public ResponseEntity<MessageModel> getPhaseOperationById(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getOperationId())
                || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getOpSequence())) {
            throw new BatchNoRecipeHeaderException(141);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getPhaseOperationById(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/verifyIngredientsInBatchRecipe")
    public ResponseEntity<MessageModel> verifyIngredients(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || request.getIngredients() == null) {
            throw new BatchNoRecipeHeaderException(142);
        }
        try {
            return ResponseEntity.ok(batchProcessService.verifyIngredients(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchRecipeAlternateRecipes")
    public ResponseEntity<MessageModel> getAlternateRecipes(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite())) {
            throw new BatchNoRecipeHeaderException(113);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getAlternateRecipes(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/calculateYieldInBatchRecipe")
    public ResponseEntity<MessageModel> calculateYield(@RequestBody BatchNoRecipeHeaderReq request) {

        if (StringUtils.isEmpty(request.getSite()) || request.getYieldTracking() == null) {
            throw new BatchNoRecipeHeaderException(143);
        }
        try {
            return ResponseEntity.ok(batchProcessService.calculateYield(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @PostMapping("/getBatchRecipePhaseIngredients")
    public ResponseEntity<MessageModel> getPhaseIngredients(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getPhaseSequence())) {
            throw new BatchNoRecipeHeaderException(140);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getPhaseIngredients(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchRecipeOpInstructions")
    public ResponseEntity<MessageModel> getOperationInstructions(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getOperationId())
                || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getOpSequence())) {
            throw new BatchNoRecipeHeaderException(141);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getOperationInstructions(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/getBatchRecipeNextOperation")
    public ResponseEntity<MessageModel> getNextOperation(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getOperationId())
                || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getOpSequence())) {
            throw new BatchNoRecipeHeaderException(141);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getNextOperation(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchRecipeNextphase")
    public ResponseEntity<MessageModel> getNextphase(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getPhaseSequence())) {
            throw new BatchNoRecipeHeaderException(140);
        }

        try {
            return ResponseEntity.ok(batchProcessService.getNextphase(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/getBatchRecipeConditionalOperation")
    public ResponseEntity<MessageModel> getConditionalOperation(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getPhaseSequence())) {
            throw new BatchNoRecipeHeaderException(140);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getConditionalOperation(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/getBatchRecipePhaseIngredientsWithVerif")
    public ResponseEntity<MessageModel> getIngredientsWithVerification(@RequestBody BatchNoRecipeHeaderReq request) {

        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getPhaseSequence())) {
            throw new BatchNoRecipeHeaderException(140);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getIngredientsWithVerification(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/getBatchRecipePhaseOpInstructions")
    public ResponseEntity<MessageModel> getPhaseOperationInstructions(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getPhaseSequence())) {
            throw new BatchNoRecipeHeaderException(140);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getPhaseOperationInstructions(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/getBatchRecipeParallelPhases")
    public ResponseEntity<MessageModel> getParallelPhases(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite())) {
            throw new BatchNoRecipeHeaderException(113);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getParallelPhases(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/getBatchRecipeAnyOrderOperations")
    public ResponseEntity<MessageModel> getAnyOrderOperations(@RequestBody BatchNoRecipeHeaderReq request) {

        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getPhaseSequence())) {
            throw new BatchNoRecipeHeaderException(140);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getAnyOrderOperations(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/getBatchRecipeUomForIngredient")
    public ResponseEntity<MessageModel> getUomForIngredient(@RequestBody BatchNoRecipeHeaderReq request) {

        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getIngredientId())) {
            throw new BatchNoRecipeHeaderException(144);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getUomForIngredient(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/getBatchRecipeIngVerificationStatus")
    public ResponseEntity<MessageModel> getIngredientVerificationStatus(@RequestBody BatchNoRecipeHeaderReq request) {

        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getIngredientId())) {
            throw new BatchNoRecipeHeaderException(144);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getIngredientVerificationStatus(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }


    @PostMapping("/addBatchRecipePhaseOperation")
    public ResponseEntity<MessageModel>
    addPhaseOperation(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getUser())
                || StringUtils.isEmpty(request.getOperationId()) || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getOpSequence()) || request.getOperations() == null) {
            throw new BatchNoRecipeHeaderException(145);
        }
        try {
            return ResponseEntity.ok(batchProcessService.addPhaseOperation(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/updateBatchRecipePhaseOperation")
    public ResponseEntity<MessageModel> updatePhaseOperation(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getUser())
                || StringUtils.isEmpty(request.getOperationId()) || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getOpSequence()) || request.getOperations() == null) {
            throw new BatchNoRecipeHeaderException(145);
        }
        try {
            return ResponseEntity.ok(batchProcessService.updatePhaseOperation(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/deleteBatchRecipePhaseOperation")
    public ResponseEntity<MessageModel> deletePhaseOperation(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getOperationId())
                || StringUtils.isEmpty(request.getUser()) || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getOpSequence())) {
            throw new BatchNoRecipeHeaderException(164);
        }
        try {
            return ResponseEntity.ok(batchProcessService.deletePhaseOperation(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/addBatchRecipePhaseIngredient")
    public ResponseEntity<MessageModel> addPhaseIngredient(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getIngreSequence())
                || StringUtils.isEmpty(request.getUser()) || StringUtils.isEmpty(request.getIngredientId()) || request.getPhaseIngredients() == null) {
            throw new BatchNoRecipeHeaderException(163);
        }
        try {
            return ResponseEntity.ok(batchProcessService.addPhaseIngredient(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/updateBatchRecipePhaseIngredient")
    public ResponseEntity<MessageModel> updatePhaseIngredient(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getUser()) || StringUtils.isEmpty(request.getPhaseSequence())
                || StringUtils.isEmpty(request.getIngreSequence()) || StringUtils.isEmpty(request.getIngredientId()) || request.getPhaseIngredients() == null) {
            throw new BatchNoRecipeHeaderException(163);
        }
        try {
            return ResponseEntity.ok(batchProcessService.updatePhaseIngredient(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/deleteBatchRecipePhaseIngredient")
    public ResponseEntity<MessageModel> deletePhaseIngredient(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getIngredientId())
                || StringUtils.isEmpty(request.getUser()) || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getIngreSequence())) {
            throw new BatchNoRecipeHeaderException(146);
        }
        try {
            return ResponseEntity.ok(batchProcessService.deletePhaseIngredient(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @PostMapping("/getBatchRecipePhaseOpDataCollection")
    public ResponseEntity<MessageModel> getPhaseOperationDataCollection(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getOperationId())
                || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getOpSequence())) {
            throw new BatchNoRecipeHeaderException(147);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getPhaseOperationDataCollection(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/getBatchRecipePhaseCcpDataCollection")
    public ResponseEntity<MessageModel> getPhaseCcpDataCollection(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite())) {
            throw new BatchNoRecipeHeaderException(113);
        }
        try {
            return ResponseEntity.ok(batchProcessService.getPhaseCcpDataCollection(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }    
    
    @PostMapping("/getBatchPhaseOpByType")
    public ResponseEntity<MessageModel> getBatchPhaseOpByType(@RequestBody BatchNoRecipeHeaderReq request) {

        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getPhaseId()) || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getOperationType())) {
            throw new BatchNoRecipeHeaderException(154);
        }

        try {
            return ResponseEntity.ok(batchProcessService.getBatchPhaseOpByType(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchRecipeFirstPhaseOp")
    public ResponseEntity<Map<String, Object>> getBatchRecipeFirstPhaseFirstOp(@RequestBody BatchNoRecipeHeaderReq request) {

        if (StringUtils.isEmpty(request.getSite())) {
            throw new BatchNoRecipeHeaderException(113);
        }

        try {
            return ResponseEntity.ok(batchProcessService.getBatchRecipeFirstPhaseFirstOp(request.getBatchNo(), request.getOrderNo(), request.getMaterial(), request.getMaterialVersion()));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/getBatchRecipeOperations")
    public ResponseEntity<MessageModel> getBatchRecipeOperations(@RequestBody BatchNoRecipeHeaderReq request) {

        if (StringUtils.isEmpty(request.getSite())) {
            throw new BatchNoRecipeHeaderException(113);
        }

        try {
            return ResponseEntity.ok(batchProcessService.getBatchRecipeOperations(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchRecipeOpIngredient")
    public ResponseEntity<MessageModel> getBatchRecipeOpIngredient(@RequestBody BatchNoRecipeHeaderReq request) {

        if (StringUtils.isEmpty(request.getSite()) || StringUtils.isEmpty(request.getOperationId()) || StringUtils.isEmpty(request.getOpSequence())) {
            throw new BatchNoRecipeHeaderException(161);
        }

        try {
            return ResponseEntity.ok(batchProcessService.getBatchRecipeOpIngredient(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getByBatchRecipeByFilters")
    public ResponseEntity<MessageModel> getByBatchRecipeByFilters(@RequestBody BatchNoRecipeHeaderReq request) {
        if (StringUtils.isEmpty(request.getSite()) ) {
            throw new BatchNoRecipeHeaderException(1102);
        }

        try {
            return ResponseEntity.ok(batchProcessService.getByBatchRecipeByFilters(request));
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/getBatchRecipeBySiteAndBatchNo")
    public ResponseEntity<List<BatchNoRecipeHeader>> getBatchRecipeBySiteAndBatchAndOrder(@RequestBody BatchNoRecipeHeaderReq request){
        if (StringUtils.isEmpty(request.getSite()) ) {
            throw new BatchNoRecipeHeaderException(1102);
        }

        try {
            List<BatchNoRecipeHeader> batchNoRecipeHeader = batchProcessService.getBatchRecipeBySiteAndBatchAndOrder(request);
            return ResponseEntity.ok(batchNoRecipeHeader);
        } catch (BatchNoRecipeHeaderException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}