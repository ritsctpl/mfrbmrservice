package com.rits.recipemaintenanceservice.controller;

import com.rits.kafkaservice.ProducerEvent;
import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.recipemaintenanceservice.dto.*;
import com.rits.recipemaintenanceservice.exception.RecipeException;
import com.rits.recipemaintenanceservice.model.*;
import com.rits.recipemaintenanceservice.service.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/recipe-service")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/createOrUpdate")
    public ResponseEntity<RecipeMessageModel> createRecipe(@RequestBody RecipeRequests recipeRequest) {
        RecipeMessageModel createRecipe;

        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getUser())) {
            try {
                createRecipe = recipeService.createOrUpdateRecipe(recipeRequest);

                AuditLogRequest activityLog = recipeService.createAuditLog(recipeRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(createRecipe);

            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(112);
    }

    @PostMapping("/delete")
    public ResponseEntity<RecipeMessageModel> deleteRecipe(@RequestBody RecipeRequests recipeRequest) throws Exception {
        RecipeMessageModel deleteResponse = null;
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getUser())) {

            try {
                deleteResponse = recipeService.deleteRecipe(recipeRequest);

                AuditLogRequest activityLog = recipeService.deleteAuditLog(recipeRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(deleteResponse);
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(112);
    }

    @PostMapping("/getRecipe") // retrieve
    public ResponseEntity<RecipeMessageModel> getRecipe(@RequestBody RecipeRequests recipeRequest) throws Exception {

        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId())) {
            try {
                return ResponseEntity.ok(recipeService.getRecipe(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(135);
    }


    @PostMapping("/retrieveAll")
    public ResponseEntity<RecipeMessageModel> recipesList(@RequestBody RecipeRequests recipeRequest) {

        if (StringUtils.hasText(recipeRequest.getSite())) {
            try {
                return ResponseEntity.ok(recipeService.recipesList(recipeRequest.getSite(),recipeRequest.getRecipeId()));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(1102);
    }

    @PostMapping("/addIngredients")
    public ResponseEntity<RecipeMessageModel> addIngredients(@RequestBody RecipeRequests recipeRequest) {
        RecipeMessageModel recipe;
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getIngredientType()) || StringUtils.hasText(recipeRequest.getIngredientId()) || StringUtils.hasText(recipeRequest.getUser())
                || recipeRequest.getParentIngredient() != null || StringUtils.hasText(recipeRequest.getIngreSequence())) {

            try {
                recipe = recipeService.addIngredients(recipeRequest);
                return ResponseEntity.ok(recipe);
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(125);
    }



    @PutMapping("/updateIngredient")
    public ResponseEntity<RecipeMessageModel> updateIngredient(@RequestBody RecipeRequests recipeRequest) {
        RecipeMessageModel recipe;
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getIngredientType()) || StringUtils.hasText(recipeRequest.getIngredientId()) || StringUtils.hasText(recipeRequest.getUser())
                || recipeRequest.getParentIngredient() != null || StringUtils.hasText(recipeRequest.getIngreSequence())) {

            try {
                recipe = recipeService.updateIngredient(recipeRequest);
                return ResponseEntity.ok(recipe);
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(145);
    }

    @PutMapping("/getRecipeIngreList")
    public ResponseEntity<RecipeMessageModel> getRecipeIngreList(@RequestBody RecipeRequests recipeRequest) {
        RecipeMessageModel recipe;
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())) {

            try {
                recipe = recipeService.getRecipeIngreList(recipeRequest);
                return ResponseEntity.ok(recipe);
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(145);
    }

    @PostMapping("/deleteIngredient")
    public ResponseEntity<RecipeMessageModel> deleteIngredient(@RequestBody RecipeRequests recipeRequest) {
        RecipeMessageModel recipe;
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getIngredientId()) || StringUtils.hasText(recipeRequest.getIngreSequence()) || StringUtils.hasText(recipeRequest.getUser()) ) {

            try {
                recipe = recipeService.deleteIngredient(recipeRequest);
                return ResponseEntity.ok(recipe);
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(119);
    }

    @PostMapping("/addPhases")
    public ResponseEntity<RecipeMessageModel> addPhases(@RequestBody RecipeRequests recipeRequest) {
        RecipeMessageModel recipe;
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getPhaseSequence()) || recipeRequest.getPhase() != null
                || StringUtils.hasText(recipeRequest.getUser()) ) {

            try {
                recipe = recipeService.addPhases(recipeRequest);
                return ResponseEntity.ok(recipe);
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(144);
    }

    @PutMapping("/updatePhase")
    public ResponseEntity<RecipeMessageModel> updatePhase(@RequestBody RecipeRequests recipeRequest) {
        RecipeMessageModel recipe;
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getPhaseSequence()) || recipeRequest.getPhases() != null
                || StringUtils.hasText(recipeRequest.getUser()) ) {

            try {
                recipe = recipeService.updatePhase(recipeRequest);
                return ResponseEntity.ok(recipe);
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(144);
    }

    @PostMapping("/deletePhase")
    public ResponseEntity<RecipeMessageModel> deletePhase(@RequestBody RecipeRequests recipeRequest) {
        RecipeMessageModel recipe;
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getUser()) ) {

            try {
                recipe = recipeService.deletePhase(recipeRequest);
                return ResponseEntity.ok(recipe);
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(123);
    }

    @PostMapping("/trackYield")
    public ResponseEntity<RecipeMessageModel> trackYield(@RequestBody RecipeRequests recipeRequest) {
        RecipeMessageModel recipe;
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || recipeRequest.getYieldTracking() != null || StringUtils.hasText(recipeRequest.getUser())) {

            try {
                recipe = recipeService.trackYield(recipeRequest);
                return ResponseEntity.ok(recipe);
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(143);
    }

    @PostMapping("/validateRecipe")
    public ResponseEntity<RecipeMessageModel> validateRecipe(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getBatchSize()) || StringUtils.hasText(recipeRequest.getIngredientId()) || StringUtils.hasText(recipeRequest.getIngreSequence())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getPhaseSequence()) || recipeRequest.getIngredients() != null) {

            try {
                return ResponseEntity.ok(recipeService.validateRecipeData(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(134);
    }

    @PostMapping("/getRecipePhases")//4
    public ResponseEntity<RecipeMessageModel> getRecipePhases(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId())) {

            try {
                return ResponseEntity.ok(recipeService.getRecipePhases(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(135);
    }


    @PostMapping("/getPhaseOperations") //3
    public ResponseEntity<RecipeMessageModel> getPhaseOperations(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getPhaseSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getPhaseOperations(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(141);
    }

    @PostMapping("/getPhaseOperationById")
    public ResponseEntity<RecipeMessageModel> getPhaseOperationById(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getOperationId())
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getOpSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getPhaseOperationById(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(138);
    }

    @PostMapping("/verifyIngredients")
    public ResponseEntity<RecipeMessageModel> verifyIngredients(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || recipeRequest.getIngredients() != null || StringUtils.hasText(recipeRequest.getUser())) {

            try {
                return ResponseEntity.ok(recipeService.verifyIngredients(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(119);
    }

    @PostMapping("/getAlternateRecipes")
    public ResponseEntity<RecipeMessageModel> getAlternateRecipes(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId())) {

            try {
                return ResponseEntity.ok(recipeService.getAlternateRecipes(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(135);
    }

    @PostMapping("/calculateYield")
    public ResponseEntity<RecipeMessageModel> calculateYield(@RequestBody RecipeRequests recipeRequest) {

        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || recipeRequest.getYieldTracking() != null) {

        try {
            return ResponseEntity.ok(recipeService.calculateYield(recipeRequest));
        } catch (RecipeException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        }
        throw new RecipeException(122);
    }

    @PostMapping("/getPhaseIngredients")
    public ResponseEntity<RecipeMessageModel> getPhaseIngredients(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getPhaseSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getPhaseIngredients(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(141);
    }

    @PostMapping("/getOperationInstructions")
    public ResponseEntity<RecipeMessageModel> getOperationInstructions(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getOperationId())
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getOpSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getOperationInstructions(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(138);
    }

    @PostMapping("/getNextOperation")
    public ResponseEntity<RecipeMessageModel> getNextOperation(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getOperationId())
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getOpSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getNextOperation(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(138);
    }

    @PostMapping("/getNextphase")
    public ResponseEntity<RecipeMessageModel> getNextphase(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getPhaseSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getNextphase(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        throw new RecipeException(141);
    }

    @PostMapping("/getConditionalOperation")
    public ResponseEntity<RecipeMessageModel> getConditionalOperation(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getPhaseSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getConditionalOperation(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(141);
    }

    @PostMapping("/getIngredientsWithVerification")
    public ResponseEntity<RecipeMessageModel> getIngredientsWithVerification(@RequestBody RecipeRequests recipeRequest) {

        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getPhaseSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getIngredientsWithVerification(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(141);
    }

    @PostMapping("/getPhaseOperationInstructions")
    public ResponseEntity<RecipeMessageModel> getPhaseOperationInstructions(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getVersion())
            || StringUtils.hasText(recipeRequest.getPhaseId()) || StringUtils.hasText(recipeRequest.getPhaseSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getPhaseOperationInstructions(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(141);
    }

    @PostMapping("/getParallelPhases")
    public ResponseEntity<RecipeMessageModel> getParallelPhases(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId())) {

            try {
                return ResponseEntity.ok(recipeService.getParallelPhases(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(135);
    }

    @PostMapping("/getAnyOrderOperations") //2
    public ResponseEntity<RecipeMessageModel> getAnyOrderOperations(@RequestBody RecipeRequests recipeRequest) {

        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getPhaseId())
                || StringUtils.hasText(recipeRequest.getVersion()) || StringUtils.hasText(recipeRequest.getPhaseSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getAnyOrderOperations(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(141);
    }

    @PostMapping("/getUomForIngredient")
    public ResponseEntity<RecipeMessageModel> getUomForIngredient(@RequestBody RecipeRequests recipeRequest) {

        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getIngredientId())
                || StringUtils.hasText(recipeRequest.getVersion()) || StringUtils.hasText(recipeRequest.getIngreSequence()) ) {

            try {
                return ResponseEntity.ok(recipeService.getUomForIngredient(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(146);
    }

    @PostMapping("/getIngVerificationStatus")
    public ResponseEntity<RecipeMessageModel> getIngredientVerificationStatus(@RequestBody RecipeRequests recipeRequest) {

        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getIngredientId())
                || StringUtils.hasText(recipeRequest.getVersion()) || StringUtils.hasText(recipeRequest.getIngreSequence())) {
            try {
                return ResponseEntity.ok(recipeService.getIngredientVerificationStatus(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(146);
    }


    @PostMapping("/addPhaseOperation")
    public ResponseEntity<RecipeMessageModel> addPhaseOperation(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getPhaseId())
                || StringUtils.hasText(recipeRequest.getOperationId()) || StringUtils.hasText(recipeRequest.getVersion()) || recipeRequest.getOperations() != null
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getOpSequence())) {

            try {
                return ResponseEntity.ok(recipeService.addPhaseOperation(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(136);
    }

    @PostMapping("/updatePhaseOperation")
    public ResponseEntity<RecipeMessageModel> updatePhaseOperation(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getPhaseId())
                || StringUtils.hasText(recipeRequest.getOperationId()) || StringUtils.hasText(recipeRequest.getVersion()) || recipeRequest.getOperations() != null
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getOpSequence())) {

            try {
                return ResponseEntity.ok(recipeService.updatePhaseOperation(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(136);
    }

    @PostMapping("/deletePhaseOperation")
    public ResponseEntity<RecipeMessageModel> deletePhaseOperation(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getPhaseId())
                || StringUtils.hasText(recipeRequest.getOperationId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getOpSequence())) {

            try {
                return ResponseEntity.ok(recipeService.deletePhaseOperation(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(138);
    }

    @PostMapping("/addPhaseIngredient")
    public ResponseEntity<RecipeMessageModel> addPhaseIngredient(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getPhaseId())
                || StringUtils.hasText(recipeRequest.getIngredientId()) || StringUtils.hasText(recipeRequest.getVersion()) || recipeRequest.getIngredients() != null
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getIngreSequence())) {

            try {
                return ResponseEntity.ok(recipeService.addPhaseIngredient(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(137);
    }

    @PostMapping("/updatePhaseIngredient")
    public ResponseEntity<RecipeMessageModel> updatePhaseIngredient(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getPhaseId())
                || StringUtils.hasText(recipeRequest.getIngredientId()) || StringUtils.hasText(recipeRequest.getVersion()) || recipeRequest.getIngredients() != null
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getIngreSequence())) {

            try {
                return ResponseEntity.ok(recipeService.updatePhaseIngredient(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(137);
    }

    @PostMapping("/deletePhaseIngredient")
    public ResponseEntity<RecipeMessageModel> deletePhaseIngredient(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getPhaseId())
                || StringUtils.hasText(recipeRequest.getIngredientId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getIngreSequence())) {

            try {
                return ResponseEntity.ok(recipeService.deletePhaseIngredient(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(139);
    }


    @PostMapping("/getPhaseOperationDataCollection")
    public ResponseEntity<RecipeMessageModel> getPhaseOperationDataCollection(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getPhaseId())
                || StringUtils.hasText(recipeRequest.getOperationId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getOpSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getPhaseOperationDataCollection(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(138);
    }

    @PostMapping("/getPhaseCcpDataCollection")
    public ResponseEntity<RecipeMessageModel> getPhaseCcpDataCollection(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getPhaseId())
                || StringUtils.hasText(recipeRequest.getOperationId()) || StringUtils.hasText(recipeRequest.getVersion())
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getOpSequence())) {

            try {
                return ResponseEntity.ok(recipeService.getPhaseCcpDataCollection(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(138);
    }


    @PostMapping("/getPhaseOperationsByType")//1
    public ResponseEntity<RecipeMessageModel> getPhaseOperationsByType(@RequestBody RecipeRequests recipeRequest) {

        if (StringUtils.hasText(recipeRequest.getSite()) || StringUtils.hasText(recipeRequest.getRecipeId()) || StringUtils.hasText(recipeRequest.getPhaseId())
                || StringUtils.hasText(recipeRequest.getPhaseSequence()) || StringUtils.hasText(recipeRequest.getOperationType()) || StringUtils.hasText(recipeRequest.getVersion())) {

            try {
                return ResponseEntity.ok(recipeService.getPhaseOperationsByType(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(140);
    }



    @PostMapping("/retrieveTop50")
    public ResponseEntity<RecipeMessageModel> getTop50RecipeList(@RequestBody RecipeRequests recipeRequest) {

        if (StringUtils.hasText(recipeRequest.getSite())) {

            try {
                return ResponseEntity.ok(recipeService.getTop50RecipeList(recipeRequest.getSite()));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(1102);
    }

    @PostMapping("isExist")
    public ResponseEntity<Boolean> isExist(@RequestBody RecipeRequests recipeRequest) {
        Boolean isExist;
        if (StringUtils.hasText(recipeRequest.getSite())) {

            try {
                isExist = recipeService.isExist(recipeRequest.getSite(), recipeRequest.getRecipeId(), recipeRequest.getVersion());
                return ResponseEntity.ok(isExist);
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(1102);
    }

    @PostMapping("checkReleasible")
    public ResponseEntity<Boolean> checkReleasible(@RequestBody RecipeRequests recipeRequest) {
        if (StringUtils.hasText(recipeRequest.getSite())) {

            try {
                return ResponseEntity.ok(recipeService.checkReleasible(recipeRequest));
            } catch (RecipeException recipeException) {
                throw recipeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecipeException(1102);
    }

    @PostMapping("getPhasesBySite")
    public ResponseEntity<RecipeMessageModel> getPhasesBySite(@RequestBody RecipeRequests recipeRequest) {
        if (!StringUtils.hasText(recipeRequest.getSite())) {
            throw new RecipeException(1102);
        }

        try {
            return ResponseEntity.ok(recipeService.getPhasesBySite(recipeRequest));
        } catch (RecipeException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
            }

    }

    @PostMapping("getOperationByFilter")
    public ResponseEntity<RecipeMessageModel> getOperationByFilter(@RequestBody RecipeRequests recipeRequest) {
        if (!StringUtils.hasText(recipeRequest.getSite())) {
            throw new RecipeException(1102);
        }

        try {
            return ResponseEntity.ok(recipeService.getOperationByFilter(recipeRequest));
        } catch (RecipeException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
            }

    }

    @PostMapping("setRecipeStatus")
    public void setRecipeStatus(@RequestBody RecipeRequests recipeRequest) {
        if (!StringUtils.hasText(recipeRequest.getSite()))
            throw new RecipeException(1102);

        try {
            recipeService.setRecipeStatus(recipeRequest);
        } catch (RecipeException recipeException) {
            throw recipeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

