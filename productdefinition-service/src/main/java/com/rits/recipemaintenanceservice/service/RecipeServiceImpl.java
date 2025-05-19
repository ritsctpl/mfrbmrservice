package com.rits.recipemaintenanceservice.service;

import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.operationservice.service.OperationServiceImpl;
import com.rits.recipemaintenanceservice.dto.*;
import com.rits.recipemaintenanceservice.exception.RecipeException;
import com.rits.recipemaintenanceservice.model.Recipes;
import com.rits.recipemaintenanceservice.repository.RecipeServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {
    private final RecipeServiceRepository recipeServiceRepository;
    private final OperationServiceImpl operationService;
    @Autowired
    private RecipeServiceImpl recipeService;
    @Autowired
    private MongoTemplate mongoTemplate;

    private Recipes getExistingRecipe(RecipeRequests recipeRequest){
        Recipes existingRecipe = null;
        if(StringUtils.hasText(recipeRequest.getVersion()))
            existingRecipe = recipeServiceRepository.findBySiteAndRecipeIdAndVersionAndActive(recipeRequest.getSite(), recipeRequest.getRecipeId(),  recipeRequest.getVersion(),1);
        else
            existingRecipe = recipeServiceRepository.findBySiteAndRecipeIdAndCurrentVersionAndActive(recipeRequest.getSite(), recipeRequest.getRecipeId(),  true,1);

        return existingRecipe;
    }
    @Override
    public RecipeMessageModel createOrUpdateRecipe(RecipeRequests recipeRequest) throws Exception {
        Recipes recipe = null;
        boolean statusCheck = false;
        String status = "created";

        if(!StringUtils.hasText(recipeRequest.getBatchSize()))
            throw new RecipeException(147);

        if(!StringUtils.hasText(recipeRequest.getBatchUom()))
            throw new RecipeException(102);
        
        Recipes existingRecipe = getExistingRecipe(recipeRequest);
        
        if (existingRecipe != null) {

            if(existingRecipe.getStatus().equals("IN_USE")){
                // create
                existingRecipe.setCurrentVersion(false);
                existingRecipe.setModifiedBy(recipeRequest.getUser());
                existingRecipe.setModifiedDate(LocalDateTime.now());
                recipeServiceRepository.save(existingRecipe);

                String[] oldVersion = existingRecipe.getVersion().split("-");
                int versionNo = Integer.parseInt(oldVersion[1]) + 1;

                String newVersion = oldVersion[0] +"-"+ versionNo;

                recipeRequest.setCurrentVersion(true);
                recipeRequest.setVersion(newVersion);
                recipe = recipeService.recipeBuilder(recipeRequest);

            } else {
                // update
                statusCheck = true;

                updateRecipe(existingRecipe, recipeRequest);

                existingRecipe.setModifiedDate(LocalDateTime.now());
                existingRecipe.setModifiedBy(recipeRequest.getUser());

                recipe = existingRecipe;
            }

        } else {

//            if(!recipeRequest.getStatus().equalsIgnoreCase("NEW"))
            recipeRequest.setCurrentVersion(true);
            recipeRequest.setVersion(recipeRequest.getVersion() + "-1");
            recipe = recipeService.recipeBuilder(recipeRequest);
        }

        if(statusCheck)
            status = "updated";

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails(recipeRequest.getRecipeId() + " Recipe " + status, "S"))
                .response(recipeServiceRepository.save(recipe))
                .build();
    }

    public Recipes recipeBuilder(RecipeRequests recipeRequest){

        Recipes recipe = Recipes.builder()
                .site(recipeRequest.getSite())
                .handle("RecipeBO:" + recipeRequest.getSite() + "," + recipeRequest.getRecipeId() + "," + recipeRequest.getVersion())
                .recipeId(recipeRequest.getRecipeId())
                .version(recipeRequest.getVersion())
                .currentVersion(recipeRequest.isCurrentVersion())
                .recipeDescription(StringUtils.hasText(recipeRequest.getRecipeDescription()) ? recipeRequest.getRecipeDescription() : recipeRequest.getRecipeId())
                .status(recipeRequest.getStatus())
                .batchSize(recipeRequest.getBatchSize())
                .batchUom(recipeRequest.getBatchUom())
                .scaling(recipeRequest.getScaling())
                .ingredients(recipeRequest.getIngredients())
                .phases(recipeRequest.getPhases())
                .totalActualCycleTime(recipeRequest.getTotalActualCycleTime())
                .totalExpectedCycleTime(recipeRequest.getTotalExpectedCycleTime())
                .workCenters(recipeRequest.getWorkCenters())
                .triggerPoints(recipeRequest.getTriggerPoints())
                .yieldTracking(recipeRequest.getYieldTracking())
                .packagingAndLabeling(recipeRequest.getPackagingAndLabeling())
                .adjustments(recipeRequest.getAdjustments())
                .safetyProcedures(recipeRequest.getSafetyProcedures())
                .operatorActions(recipeRequest.getOperatorActions())
                .compliance(recipeRequest.getCompliance())
                .createdBy(recipeRequest.getUser())
                .createdDate(LocalDateTime.now())
                .active(1)
                .build();

        return recipe;
    }

    private void updateRecipe(Recipes existingRecipe, RecipeRequests recipeRequest){

//        existingRecipe.setRecipeName(StringUtils.isEmpty(recipeRequest.getRecipeId()) ? existingRecipe.getRecipeId() : recipeRequest.getRecipeId());

        if(!StringUtils.hasText(recipeRequest.getBatchSize()))
            throw new RecipeException(147);

        existingRecipe.setBatchSize(recipeRequest.getBatchSize());
        existingRecipe.setBatchUom(recipeRequest.getBatchUom());
        existingRecipe.setStatus(recipeRequest.getStatus());
        existingRecipe.setRecipeDescription(StringUtils.hasText(recipeRequest.getRecipeDescription()) ? recipeRequest.getRecipeDescription() : recipeRequest.getRecipeId());
        existingRecipe.setTotalExpectedCycleTime(recipeRequest.getTotalExpectedCycleTime());
        existingRecipe.setTotalActualCycleTime(recipeRequest.getTotalActualCycleTime());

        updateScaling(existingRecipe, existingRecipe.getScaling(), recipeRequest.getScaling());
//        updateIngredients(existingRecipe, existingRecipe.getIngredients(), recipeRequest.getIngredients());
//        updatePhases(existingRecipe, existingRecipe.getPhases(), recipeRequest.getPhases());
        updateWorkCenters(existingRecipe, existingRecipe.getWorkCenters(), recipeRequest.getWorkCenters());
        updateTriggerPoints(existingRecipe, existingRecipe.getTriggerPoints(), recipeRequest.getTriggerPoints());

        updateYieldTracking(existingRecipe, existingRecipe.getYieldTracking(), recipeRequest.getYieldTracking());
        updatePackagingAndLabeling(existingRecipe, existingRecipe.getPackagingAndLabeling(), recipeRequest.getPackagingAndLabeling());
        updateParentAdjustments(existingRecipe, existingRecipe.getAdjustments(), recipeRequest.getAdjustments());
        updateSafetyProcedures(existingRecipe, existingRecipe.getSafetyProcedures(), recipeRequest.getSafetyProcedures());
        updateOperatorActions(existingRecipe, existingRecipe.getOperatorActions(), recipeRequest.getOperatorActions());
        updateCompliance(existingRecipe, existingRecipe.getCompliance(), recipeRequest.getCompliance());

        if(existingRecipe.getPhases() != null) {
            for (Phase existingPhase : existingRecipe.getPhases()) {
                Phase requestPhase = findMatchingPhase(recipeRequest.getPhases(), existingPhase);

                if (requestPhase != null && existingPhase.getOperations() != null && !existingPhase.getOperations().isEmpty()){
                    for (Operation existingop : existingPhase.getOperations()) {
                        Operation requestop = findMatchingop(requestPhase.getOperations(), existingop);

                        if (requestop != null) {
                            updateAdjustments(existingop, existingop.getAdjustments(), requestop.getAdjustments());
                        }
                    }
                }
            }
        }
    }

    private Phase findMatchingPhase(List<Phase> requestPhases, Phase existingPhase) {
        if (requestPhases == null || existingPhase == null || existingPhase.getSequence() == null) {
            return null;
        }
        return requestPhases.stream()
                .filter(phase -> phase != null && phase.getSequence() != null && phase.getSequence().equals(existingPhase.getSequence()))
                .findFirst()
                .orElse(null);
    }

    private Operation findMatchingop(List<Operation> requestops, Operation existingop) {
        if (requestops == null || existingop == null || existingop.getSequence() == null) {
            return null;
        }
        return requestops.stream()
                .filter(op -> op != null && op.getSequence() != null && op.getSequence().equals(existingop.getSequence()))
                .findFirst()
                .orElse(null);
    }

    private static void updatePackagingAndLabeling(Recipes recipes, PackagingAndLabeling existingPackaging, PackagingAndLabeling packagingRequest) {

        if (packagingRequest == null){
            recipes.setPackagingAndLabeling(null);
            return;
        }

        if (existingPackaging == null) {
            existingPackaging = new PackagingAndLabeling();
        }

        existingPackaging.setPackagingType(packagingRequest.getPackagingType());
        existingPackaging.setPrimaryPackagingMaterial(packagingRequest.getPrimaryPackagingMaterial());
        existingPackaging.setSecondaryPackagingType(packagingRequest.getSecondaryPackagingType());
        existingPackaging.setContainerSize(packagingRequest.getContainerSize());
        existingPackaging.setLabelFormat(packagingRequest.getLabelFormat());
        existingPackaging.setInstructions(packagingRequest.getInstructions());

        existingPackaging.setLabelingRequirements(packagingRequest.getLabelingRequirements());
        existingPackaging.setLabelLanguage(packagingRequest.getLabelLanguage());
        existingPackaging.setComplianceStandards(packagingRequest.getComplianceStandards());

        if (existingPackaging.getEnvironmentalRequirements() == null) {
            existingPackaging.setEnvironmentalRequirements(new EnvironmentalRequirements());
        }
        updateEnvironmentalRequirements(recipes, existingPackaging.getEnvironmentalRequirements(), packagingRequest.getEnvironmentalRequirements());
        recipes.setPackagingAndLabeling(existingPackaging);
    }

    private static void updateEnvironmentalRequirements(Recipes recipes, EnvironmentalRequirements existingRequirements, EnvironmentalRequirements requestRequirements) {
        if (recipes.getPackagingAndLabeling() == null) {
            recipes.setPackagingAndLabeling(new PackagingAndLabeling());
        }

        if (requestRequirements == null){
            recipes.getPackagingAndLabeling().setEnvironmentalRequirements(null);
            return;
        }

        if (existingRequirements == null) {
            existingRequirements = new EnvironmentalRequirements();
        }

        existingRequirements.setStorageTemperature(requestRequirements.getStorageTemperature());
        existingRequirements.setHumidityRange(requestRequirements.getHumidityRange());
        existingRequirements.setProtectionFromLight(requestRequirements.isProtectionFromLight());
    }


    private static void updateYieldTracking(Recipes existingRecipe, YieldTracking existingYieldTracking, YieldTracking yieldTrackingRequest) {
        if (yieldTrackingRequest == null){
            existingRecipe.setYieldTracking(null);
            return;
        }

        if (existingYieldTracking == null) {

            existingYieldTracking = new YieldTracking();
            existingYieldTracking.setExpectedYield(yieldTrackingRequest.getExpectedYield());
            existingYieldTracking.setAllowedVariance(yieldTrackingRequest.getAllowedVariance());
            existingYieldTracking.setActualYield(yieldTrackingRequest.getActualYield());

            existingYieldTracking.setByProducts(new ArrayList<>(yieldTrackingRequest.getByProducts()));
            existingYieldTracking.setWaste(new ArrayList<>(yieldTrackingRequest.getWaste()));
            existingYieldTracking.setCorrections(new ArrayList<>(yieldTrackingRequest.getCorrections()));
            existingYieldTracking.setQualityDeviations(new ArrayList<>(yieldTrackingRequest.getQualityDeviations()));
        } else {

            existingYieldTracking.setExpectedYield(yieldTrackingRequest.getExpectedYield());
            existingYieldTracking.setAllowedVariance(yieldTrackingRequest.getAllowedVariance());
            existingYieldTracking.setActualYield(yieldTrackingRequest.getActualYield());

            updateByProduct(existingRecipe, existingYieldTracking.getByProducts(), yieldTrackingRequest.getByProducts());
            updateWaste(existingRecipe, existingYieldTracking.getWaste(), yieldTrackingRequest.getWaste());
            updateCorrections(existingRecipe, existingYieldTracking.getCorrections(), yieldTrackingRequest.getCorrections());
            updateQualityDeviations(existingRecipe, existingYieldTracking.getQualityDeviations(), yieldTrackingRequest.getQualityDeviations());
        }
        existingRecipe.setYieldTracking(existingYieldTracking);

    }

    private static void updateByProduct(Recipes existingRecipe, List<ByProduct> existingByProducts, List<ByProduct> byProductRequests) {

        if (existingRecipe.getYieldTracking() == null) {
            existingRecipe.setYieldTracking(new YieldTracking());
        }

        if (byProductRequests == null){
            existingRecipe.getYieldTracking().setByProducts(null);
            return;
        }

        if (existingByProducts == null) {
            existingByProducts = new ArrayList<>();
        }

        Map<String, ByProduct> existingByProductMap = existingByProducts.stream()
                .collect(Collectors.toMap(ByProduct::getSequence, byProduct -> byProduct));

        for (ByProduct request : byProductRequests) {
            ByProduct existing = existingByProductMap.get(request.getSequence());

            if (existing != null) {
                existing.setByProductId(request.getByProductId());
                existing.setDescription(request.getDescription());
                existing.setExpectedQuantity(request.getExpectedQuantity());
                existing.setUom(request.getUom());
                existing.setHandlingProcedure(request.getHandlingProcedure());
            } else {
                existingByProducts.add(request);
            }
        }
        existingRecipe.getYieldTracking().setByProducts(existingByProducts);
    }

    private static void updateIngredientByProduct(ParentIngredient existingIngredient, ByProduct existingByProducts, ByProduct byProductRequests) {

        if (byProductRequests == null) {
            existingIngredient.setByProduct(null);
            return;
        }

        if (existingByProducts == null) {
            existingByProducts = new ByProduct();
        }

        existingByProducts.setByProductId(byProductRequests.getByProductId());
        existingByProducts.setDescription(byProductRequests.getDescription());
        existingByProducts.setByProductQuantity(byProductRequests.getByProductQuantity());
        existingByProducts.setUom(byProductRequests.getUom());

        existingIngredient.setByProduct(existingByProducts);
    }


    private static void updateWaste(Recipes existingRecipe, List<Waste> existingWaste, List<Waste> wasteRequests) {
        if (existingRecipe.getYieldTracking() == null) {
            existingRecipe.setYieldTracking(new YieldTracking());
        }

        if (wasteRequests == null){
            existingRecipe.getYieldTracking().setWaste(null);
            return;
        }

        if (existingWaste == null) {
            existingWaste = new ArrayList<>();
        }

        for (Waste wasteRequest : wasteRequests) {
            Waste existingWasteItem = findWasteById(existingWaste, wasteRequest.getSequence());

            if (existingWasteItem != null) {
                existingWasteItem.setWasteId(wasteRequest.getWasteId());
                existingWasteItem.setDescription(wasteRequest.getDescription());
                existingWasteItem.setQuantity(wasteRequest.getQuantity());
                existingWasteItem.setUom(wasteRequest.getUom());
                existingWasteItem.setHandlingProcedure(wasteRequest.getHandlingProcedure());
                existingWasteItem.setCostOfDisposal(wasteRequest.getCostOfDisposal());
            } else {
                existingWaste.add(wasteRequest);
            }
        }
        existingRecipe.getYieldTracking().setWaste(existingWaste);
    }

    private static Waste findWasteById(List<Waste> wastes, String wasteId) {
        if (wastes == null || wasteId == null) {
            return null;
        }
        for (Waste waste : wastes) {
            if (waste.getSequence().equals(wasteId)) {
                return waste;
            }
        }
        return null;
    }

    private static void updateCorrections(Recipes existingRecipe, List<Correction> existingCorrections, List<Correction> correctionRequests) {

        if (existingRecipe.getYieldTracking() == null) {
            existingRecipe.setYieldTracking(new YieldTracking());
        }

        if (correctionRequests == null){
            existingRecipe.getYieldTracking().setCorrections(null);
            return;
        }

        if (existingCorrections == null) {
            existingCorrections = new ArrayList<>();
        }

        for (Correction correctionRequest : correctionRequests) {
            Correction existingCorrection = findCorrectionById(existingCorrections, correctionRequest.getSequence());

            if (existingCorrection != null) {
                existingCorrection.setCorrectionId(correctionRequest.getCorrectionId());
                existingCorrection.setCorrDescription(correctionRequest.getCorrDescription());
                existingCorrection.setCondition(correctionRequest.getCondition());
                existingCorrection.setAction(correctionRequest.getAction());
                existingCorrection.setImpact(correctionRequest.getImpact());
            } else {
                existingCorrections.add(correctionRequest);
            }
        }
        existingRecipe.getYieldTracking().setCorrections(existingCorrections);
    }

    private static Correction findCorrectionById(List<Correction> corrections, String correctionId) {
        for (Correction correction : corrections) {
            if (correction.getSequence().equals(correctionId)) {
                return correction;
            }
        }
        return null;
    }


    private static void updateQualityDeviations(Recipes existingRecipe, List<QualityDeviation> existingDeviations, List<QualityDeviation> deviationRequests) {

        if (existingRecipe.getYieldTracking() == null) {
            existingRecipe.setYieldTracking(new YieldTracking());
        }

        if (deviationRequests == null){
            existingRecipe.getYieldTracking().setQualityDeviations(null);
            return;
        }

        if (existingDeviations == null) {
            existingDeviations = new ArrayList<>();
        }

        for (QualityDeviation deviationRequest : deviationRequests) {
            QualityDeviation existingDeviation = findQualityDeviationById(existingDeviations, deviationRequest.getSequence());

            if (existingDeviation != null) {
                existingDeviation.setDeviationId(deviationRequest.getDeviationId());
                existingDeviation.setCondition(deviationRequest.getCondition());
                existingDeviation.setDevDescription(deviationRequest.getDevDescription());
                existingDeviation.setImpact(deviationRequest.getImpact());
                existingDeviation.setRequiredAction(deviationRequest.getRequiredAction());
            } else {
                existingDeviations.add(deviationRequest);
            }
        }
        existingRecipe.getYieldTracking().setQualityDeviations(existingDeviations);
    }

    private static QualityDeviation findQualityDeviationById(List<QualityDeviation> deviations, String deviationId) {
        for (QualityDeviation deviation : deviations) {
            if (deviation.getSequence().equals(deviationId)) {
                return deviation;
            }
        }
        return null;
    }



    private static void updateTriggerPoints(Recipes existingRecipe, List<TriggerPoint> existingTriggerPoints, List<TriggerPoint> triggerPointRequests) {
        if (triggerPointRequests == null){
            existingRecipe.setTriggerPoints(null);
            return;
        }

        if (existingTriggerPoints == null) {
            existingTriggerPoints = new ArrayList<>();
        }

        for (TriggerPoint triggerPointRequest : triggerPointRequests) {
            TriggerPoint existingTriggerPoint = findTriggerPointById(existingTriggerPoints, triggerPointRequest.getTriggerPointId());

            if (existingTriggerPoint != null) {
                existingTriggerPoint.setTriggerPointId(triggerPointRequest.getTriggerPointId());
                existingTriggerPoint.setDescription(triggerPointRequest.getDescription());
                existingTriggerPoint.setCondition(triggerPointRequest.getCondition());
                existingTriggerPoint.setAction(triggerPointRequest.getAction());
            } else {
                existingTriggerPoints.add(triggerPointRequest);
            }
        }
        existingRecipe.setTriggerPoints(existingTriggerPoints);
    }

    private static TriggerPoint findTriggerPointById(List<TriggerPoint> triggerPoints, String triggerPointId) {
        for (TriggerPoint triggerPoint : triggerPoints) {
            if (triggerPoint.getTriggerPointId().equals(triggerPointId)) {
                return triggerPoint;
            }
        }
        return null;
    }

    private static void updateWorkCenters(Recipes existingRecipe, List<WorkCenter> existingWorkCenters, List<WorkCenter> workCenterRequests) {
        if (workCenterRequests == null){
            existingRecipe.setWorkCenters(null);
            return;
        }

        if (existingWorkCenters == null) {
            existingWorkCenters = new ArrayList<>();
        }

        for (WorkCenter workCenterRequest : workCenterRequests) {
            WorkCenter existingWorkCenter = findWorkCenterById(existingWorkCenters, workCenterRequest.getWorkCenterId());

            if (existingWorkCenter != null) {
                existingWorkCenter.setWorkCenterId(workCenterRequest.getWorkCenterId());
                existingWorkCenter.setDescription(workCenterRequest.getDescription());
                existingWorkCenter.setResource(workCenterRequest.getResource());
                existingWorkCenter.setSystemStatus(workCenterRequest.getSystemStatus());
                existingWorkCenter.setCapacity(workCenterRequest.getCapacity());
                existingWorkCenter.setShiftDetails(workCenterRequest.getShiftDetails());
                existingWorkCenter.setOperatorId(workCenterRequest.getOperatorId());
                existingWorkCenter.setMaintenanceSchedule(workCenterRequest.getMaintenanceSchedule());
                existingWorkCenter.setTooling(workCenterRequest.getTooling());
                existingWorkCenter.setCalibrationStatus(workCenterRequest.getCalibrationStatus());
                existingWorkCenter.setTargetCycleTime(workCenterRequest.getTargetCycleTime());
                existingWorkCenter.setLocationId(workCenterRequest.getLocationId());
                existingWorkCenter.setZone(workCenterRequest.getZone());
                existingWorkCenter.setPhasesHandled(workCenterRequest.getPhasesHandled());
            } else {
                existingWorkCenters.add(workCenterRequest);
            }
        }
        existingRecipe.setWorkCenters(existingWorkCenters);
    }

    private static WorkCenter findWorkCenterById(List<WorkCenter> workCenters, String workCenterId) {
        for (WorkCenter workCenter : workCenters) {
            if (workCenter.getSequence().equals(workCenterId)) {
                return workCenter;
            }
        }
        return null;
    }


    private static void updateScaling(Recipes existingRecipe, Scaling existingScaling, Scaling scalingRequest) {
        if (scalingRequest == null){
            existingRecipe.setScaling(null);
            return;
        }

        if(existingScaling == null)
            existingScaling = new Scaling();

        existingScaling.setScalingFactor(scalingRequest.getScalingFactor());
        existingScaling.setScalable(scalingRequest.getScalable());
        existingScaling.setMaxBatchSize(scalingRequest.getMaxBatchSize());
        existingScaling.setMinBatchSize(scalingRequest.getMinBatchSize());

        existingRecipe.setScaling(existingScaling);
    }

    private static void updateIngredients(Recipes existingRecipe, Ingredients existingIngredients, Ingredients ingredientsRequest) {
        if (ingredientsRequest == null){
            existingRecipe.setIngredients(null);
            return;
        }

        if (existingIngredients == null) {
            existingIngredients = new Ingredients();
            existingRecipe.setIngredients(existingIngredients);
        }

        if (existingIngredients.getActive() == null) {
            existingIngredients.setActive(new ArrayList<>());
        }

        if(ingredientsRequest.getActive() != null && !ingredientsRequest.getActive().isEmpty()) {
            for (ParentIngredient ingredientRequest : ingredientsRequest.getActive()) {
                ParentIngredient existingIngredient = findParentIngredientById(existingIngredients.getActive(), ingredientRequest.getSequence());

                if (existingIngredient != null) {
                    existingParentIngredientUpdate(existingIngredient, ingredientRequest);
//                    existingRecipe.getIngredients().setActive(existingIngredients.getActive());
                } else {
//                    existingIngredients.getActive().add(ingredientRequest);
                    throw new RecipeException(128, ingredientRequest.getIngredientId());
                }
            }

        } else {
            existingIngredients.setActive(null);
        }

        if (existingIngredients.getInactive() == null) {
            existingIngredients.setInactive(new ArrayList<>());
        }

        if(ingredientsRequest.getInactive() != null && !ingredientsRequest.getInactive().isEmpty()) {
            for (ParentIngredient ingredientRequest : ingredientsRequest.getInactive()) {
                ParentIngredient existingIngredient = findParentIngredientById(existingIngredients.getInactive(), ingredientRequest.getSequence());

                if (existingIngredient != null) {
                    existingParentIngredientUpdate(existingIngredient, ingredientRequest);
//                    existingRecipe.getIngredients().setInactive(existingIngredients.getInactive());

                } else {
//                    existingIngredients.getInactive().add(ingredientRequest);
                    throw new RecipeException(128, ingredientRequest.getIngredientId());
                }
            }

        }else {
            existingIngredients.setInactive(null);
        }
    }

    private static ParentIngredient findParentIngredientById(List<ParentIngredient> ingredients, String ingreId) {
        for (ParentIngredient ingredient : ingredients) {// check nulls
            if (ingredient.getSequence().equals(ingreId)) {
                return ingredient;
            }
        }
        return null;
    }

    private static void existingParentIngredientUpdate(ParentIngredient existingIngredient, ParentIngredient ingredientRequest){
        existingIngredient.setIngredientId(ingredientRequest.getIngredientId());
        existingIngredient.setIngredientVersion(ingredientRequest.getIngredientVersion());
        existingIngredient.setIngreDescription(ingredientRequest.getIngreDescription());
        existingIngredient.setQuantity(ingredientRequest.getQuantity());
        existingIngredient.setUom(ingredientRequest.getUom());
        existingIngredient.setSequence(ingredientRequest.getSequence());
        existingIngredient.setTolerance(ingredientRequest.getTolerance());
        existingIngredient.setMaterialType(ingredientRequest.getMaterialType());
        existingIngredient.setSupplierId(ingredientRequest.getSupplierId());
        existingIngredient.setStorageLocation(ingredientRequest.getStorageLocation());
        existingIngredient.setHandlingInstructions(ingredientRequest.getHandlingInstructions());
        existingIngredient.setStorageInstructions(ingredientRequest.getStorageInstructions());
        existingIngredient.setUnitCost(ingredientRequest.getUnitCost());
        existingIngredient.setCurrency(ingredientRequest.getCurrency());
        existingIngredient.setTotalCost(ingredientRequest.getTotalCost());
        existingIngredient.setWasteQuantity(ingredientRequest.getWasteQuantity());
        existingIngredient.setWasteUoM(ingredientRequest.getWasteUoM());
        existingIngredient.setBatchNumber(ingredientRequest.getBatchNumber());
        existingIngredient.setHazardous(ingredientRequest.getHazardous());
        existingIngredient.setOperationId(ingredientRequest.getOperationId());
        existingIngredient.setOperationVersion(ingredientRequest.getOperationVersion());
//        existingIngredient.setMaterialDescription(ingredientRequest.getMaterialDescription());
        updateQualityControlParameters(existingIngredient.getQcParameters(), ingredientRequest.getQcParameters());

        updateIngredientByProduct(existingIngredient, existingIngredient.getByProduct(), ingredientRequest.getByProduct());

        updateAlternateIngredients(existingIngredient, existingIngredient.getAlternateIngredients(), ingredientRequest.getAlternateIngredients());
    }

    private static void updateQualityControlParameters(List<QualityControlParameter> existingQCs, List<QualityControlParameter> qcRequests) {

        if (existingQCs == null) {
            existingQCs = new ArrayList<>();
        }
        if (qcRequests == null || qcRequests.isEmpty()){
            existingQCs.clear();
            return;
        }

        for (QualityControlParameter qcRequest : qcRequests) {
            QualityControlParameter existingQC = findQCParameterById(existingQCs, qcRequest.getSequence());

            if (existingQC != null) {
                existingQC.setQcId(qcRequest.getQcId());
                existingQC.setQcDescription(qcRequest.getQcDescription());
                existingQC.setParameter(qcRequest.getParameter());
                existingQC.setActualValue(qcRequest.getActualValue());
                existingQC.setExpectedValue(qcRequest.getExpectedValue());
                existingQC.setMonitoringFrequency(qcRequest.getMonitoringFrequency());
                existingQC.setActionsOnFailure(qcRequest.getActionsOnFailure());
                existingQC.setTolerance(qcRequest.getTolerance());
                existingQC.setMin(qcRequest.getMin());
                existingQC.setMax(qcRequest.getMax());
                existingQC.setToolsRequired(qcRequest.getToolsRequired());
            } else {
                existingQCs.add(qcRequest);
//                throw new RecipeException(148,qcRequest.getQcId());
            }
        }
    }

    private static QualityControlParameter findQCParameterById(List<QualityControlParameter> qcs, String qcId) {
        for (QualityControlParameter qc : qcs) {
            if (qc.getSequence().equals(qcId)) {
                return qc;
            }
        }
        return null;
    }

    private static void updateByProduct(ByProduct existingByProduct, ByProduct requestByProduct) {
        if (requestByProduct == null){
            existingByProduct = new ByProduct();
            return;
        }

        if (existingByProduct == null) {
            existingByProduct = new ByProduct();
        }

        existingByProduct.setByProductId(requestByProduct.getByProductId());
        existingByProduct.setDescription(requestByProduct.getDescription());
        existingByProduct.setExpectedQuantity(requestByProduct.getExpectedQuantity());
        existingByProduct.setUom(requestByProduct.getUom());
        existingByProduct.setHandlingProcedure(requestByProduct.getHandlingProcedure());
        existingByProduct.setByProductQuantity(requestByProduct.getByProductQuantity());
        existingByProduct.setReusable(requestByProduct.getReusable());
        existingByProduct.setDisposalCost(requestByProduct.getDisposalCost());
        existingByProduct.setCurrency(requestByProduct.getCurrency());
        existingByProduct.setQuantityProduced(requestByProduct.getQuantityProduced());
    }

    private static void updateAlternateIngredients(ParentIngredient existingIngredient, List<AlternateIngredient> existingAlternates, List<AlternateIngredient> alternateRequests) {
        if (alternateRequests == null){
            existingIngredient.setAlternateIngredients(null);
            return;
        }

        if (existingAlternates == null) {
            existingAlternates = new ArrayList<>();
        }

        for (AlternateIngredient alternateRequest : alternateRequests) {
            AlternateIngredient existingAlternate = findAlternateIngredientById(existingAlternates, alternateRequest.getSequence());

            if (existingAlternate != null) {
                existingAlternate.setIngredientId(alternateRequest.getIngredientId());
                existingAlternate.setIngredientVersion(alternateRequest.getIngredientVersion());
                existingAlternate.setIngreDescription(alternateRequest.getIngreDescription());
                existingAlternate.setQuantity(alternateRequest.getQuantity());
                existingAlternate.setUom(alternateRequest.getUom());
                existingAlternate.setTolerance(alternateRequest.getTolerance());
                existingAlternate.setMaterialDescription(alternateRequest.getMaterialDescription());
                existingAlternate.setStorageLocation(alternateRequest.getStorageLocation());
                existingAlternate.setMaterialType(alternateRequest.getMaterialType());
                existingAlternate.setBatchNumber(alternateRequest.getBatchNumber());
                existingAlternate.setExpiryDate(alternateRequest.getExpiryDate());
                existingAlternate.setManufactureDate(alternateRequest.getManufactureDate());
                existingAlternate.setUnitCost(alternateRequest.getUnitCost());
                existingAlternate.setTotalCost(alternateRequest.getTotalCost());

                updateQualityControlParameters(existingAlternate.getQcParameters(), alternateRequest.getQcParameters());

            } else {
                existingAlternates.add(alternateRequest);
//                throw new RecipeException(149, alternateRequest.getIngredientId());
            }
        }
        existingIngredient.setAlternateIngredients(existingAlternates);
    }

    private static AlternateIngredient findAlternateIngredientById(List<AlternateIngredient> alternates, String ingredientId) {
        for (AlternateIngredient alternate : alternates) {
            if (alternate.getSequence().equals(ingredientId)) {
                return alternate;
            }
        }
        return null;
    }

    private void updatePhases(Recipes existingRecipe, List<Phase> existingPhases, List<Phase> phasesRequest) {
        if (phasesRequest == null || phasesRequest.isEmpty()){
            existingRecipe.setPhases(null);
            return;
        }

        if (existingPhases == null || existingPhases.isEmpty()) {
            existingPhases = new ArrayList<>();
        }

        for (Phase phaseRequest : phasesRequest) {
            Phase existingPhase = findPhaseById(existingPhases, phaseRequest.getSequence());

            if (existingPhase != null) {

                existingPhase.setPhaseId(phaseRequest.getPhaseId());
                existingPhase.setPhaseDescription(phaseRequest.getPhaseDescription());
                existingPhase.setExpectedCycleTime(phaseRequest.getExpectedCycleTime());
                existingPhase.setConditional(phaseRequest.getConditional() != null && phaseRequest.getConditional());
                existingPhase.setParallel(phaseRequest.getParallel() != null && phaseRequest.getParallel());
                existingPhase.setAnyOrder(phaseRequest.getAnyOrder() != null && phaseRequest.getAnyOrder());
                existingPhase.setTriggeredPhase(phaseRequest.getTriggeredPhase());
                existingPhase.setEntryPhase(phaseRequest.isEntryPhase());
                existingPhase.setExitPhase(phaseRequest.isExitPhase());
                existingPhase.setNextPhase(phaseRequest.getNextPhase());
                if ((phaseRequest.getConditional() == null || !phaseRequest.getConditional()) &&
                        (phaseRequest.getParallel() == null || !phaseRequest.getParallel()) && (phaseRequest.getAnyOrder() == null || !phaseRequest.getAnyOrder())) {
                    existingPhase.setSequential(true);
                }

                updatePhaseIngredients(existingPhase.getIngredients(), existingPhase.getIngredients());
                updatePhaseOperations(existingPhase.getOperations(), existingPhase.getOperations());

            } else {

                Phase newPhase = new Phase();
                newPhase.setPhaseId(phaseRequest.getPhaseId());
                newPhase.setPhaseDescription(phaseRequest.getPhaseDescription());
                newPhase.setSequence(phaseRequest.getSequence());
                newPhase.setExpectedCycleTime(phaseRequest.getExpectedCycleTime());
                newPhase.setIngredients(phaseRequest.getIngredients());
                newPhase.setOperations(phaseRequest.getOperations());
                newPhase.setConditional(phaseRequest.getConditional() != null && phaseRequest.getConditional());
                newPhase.setParallel(phaseRequest.getParallel() != null && phaseRequest.getParallel());
                newPhase.setAnyOrder(phaseRequest.getAnyOrder() != null && phaseRequest.getAnyOrder());
                newPhase.setTriggeredPhase(phaseRequest.getTriggeredPhase());
                newPhase.setEntryPhase(phaseRequest.isEntryPhase());
                newPhase.setExitPhase(phaseRequest.isExitPhase());
                newPhase.setNextPhase(phaseRequest.getNextPhase());
                if ((phaseRequest.getConditional() == null || !phaseRequest.getConditional()) &&
                        (phaseRequest.getParallel() == null || !phaseRequest.getParallel()) && (phaseRequest.getAnyOrder() == null || !phaseRequest.getAnyOrder())) {
                    newPhase.setSequential(true);
                }

                existingPhases.add(newPhase);
//                throw new RecipeException(150, phaseRequest.getPhaseId());
            }
        }

        existingRecipe.setPhases(existingPhases);
    }

    private static Phase findPhaseById(List<Phase> phases, String phaseId) {
        for (Phase phase : phases) {
            if (phase.getSequence().equals(phaseId)) {
                return phase;
            }
        }
        return null;
    }

    public void updatePhaseIngredients(Ingredients existingIngredients, Ingredients requestIngredients) {
        if (existingIngredients == null) {
            existingIngredients = new Ingredients();
        }

        // Handle Active Ingredients
        List<ParentIngredient> existingActive = existingIngredients.getActive();
        List<ParentIngredient> requestActive = requestIngredients != null ? requestIngredients.getActive() : null;

        if (requestActive == null || requestActive.isEmpty()) {
            if (existingActive != null) {
                existingActive.clear(); // Clear active list if no active ingredients in request
            }
        } else {
            if (existingActive == null) {
                existingActive = new ArrayList<>();
                existingIngredients.setActive(existingActive);
            }
            mergeIngredients(existingActive, requestActive);
        }

        // Handle Inactive Ingredients
        List<ParentIngredient> existingInactive = existingIngredients.getInactive();
        List<ParentIngredient> requestInactive = requestIngredients != null ? requestIngredients.getInactive() : null;

        if (requestInactive == null || requestInactive.isEmpty()) {
            if (existingInactive != null) {
                existingInactive.clear(); // Clear inactive list if no inactive ingredients in request
            }
        } else {
            if (existingInactive == null) {
                existingInactive = new ArrayList<>();
                existingIngredients.setInactive(existingInactive);
            }
            mergeIngredients(existingInactive, requestInactive);
        }
    }

//    private static void mergeIngredients(List<ParentIngredient> existingIngredients, List<ParentIngredient> requestIngredients) {
//        Map<String, ParentIngredient> existingMap = existingIngredients.stream()
//                .filter(ingredient -> ingredient.getIngredientId() != null)
//                .collect(Collectors.toMap(ParentIngredient::getIngredientId, ingredient -> ingredient));
//
//        for (ParentIngredient requestIngredient : requestIngredients) {
//            if (requestIngredient.getIngredientId() == null) {
//                continue;
//            }
//
//            ParentIngredient existingIngredient = existingMap.get(requestIngredient.getSequence());
//
//            if (existingIngredient != null) {
//                // Update existing ingredient fields
//                existingIngredient.setIngreDescription(requestIngredient.getIngreDescription());
//                existingIngredient.setQuantity(requestIngredient.getQuantity());
//                existingIngredient.setUom(requestIngredient.getUom());
//                existingIngredient.setSequence(requestIngredient.getSequence());
//                existingIngredient.setMaterialDescription(requestIngredient.getMaterialDescription());
//                existingIngredient.setStorageLocation(requestIngredient.getStorageLocation());
//                existingIngredient.setTolerance(requestIngredient.getTolerance());
//                existingIngredient.setMaterialType(requestIngredient.getMaterialType());
//                existingIngredient.setSupplierId(requestIngredient.getSupplierId());
//                existingIngredient.setSourceLocation(requestIngredient.getSourceLocation());
//                existingIngredient.setHandlingInstructions(requestIngredient.getHandlingInstructions());
//                existingIngredient.setStorageInstructions(requestIngredient.getStorageInstructions());
//                existingIngredient.setUnitCost(requestIngredient.getUnitCost());
//                existingIngredient.setCurrency(requestIngredient.getCurrency());
//                existingIngredient.setTotalCost(requestIngredient.getTotalCost());
//                existingIngredient.setWasteQuantity(requestIngredient.getWasteQuantity());
//                existingIngredient.setWasteUoM(requestIngredient.getWasteUoM());
//                existingIngredient.setBatchNumber(requestIngredient.getBatchNumber());
//                existingIngredient.setHazardous(requestIngredient.getHazardous());
//
//                // Update byProduct and alternateIngredients if present
//                existingIngredient.setByProduct(requestIngredient.getByProduct());
//                existingIngredient.setAlternateIngredients(requestIngredient.getAlternateIngredients());
//
//
//                // Update QC parameters
//                updateQualityControlParameters(existingIngredient.getQcParameters(), requestIngredient.getQcParameters());
//            } else {
//                // Add new ingredient
////                existingIngredients.add(requestIngredient);
//                throw new RecipeException(128, requestIngredient.getIngredientId());
//            }
//        }
//    }

    private void mergeIngredients(List<ParentIngredient> existingIngredients, List<ParentIngredient> requestIngredients) {
        if (existingIngredients == null) {
            existingIngredients = new ArrayList<>();
        }

        if (requestIngredients == null || requestIngredients.isEmpty()) {
            return;
        }

        for (ParentIngredient requestIngredient : requestIngredients) {
            ParentIngredient existingIngredient = findIngredientByIdAndSequence(existingIngredients, requestIngredient.getSequence());

            if (existingIngredient != null) {
                updateParentIngredient(existingIngredient, requestIngredient);
            } else {
                existingIngredients.add(requestIngredient);
//                throw new RecipeException(128, requestIngredient.getIngredientId());
            }
        }
    }



    private static ParentIngredient findIngredientByIdAndSequence(List<ParentIngredient> ingredients, String sequence) {
        for (ParentIngredient ingredient : ingredients) {
            if (ingredient != null && ingredient.getSequence() != null && ingredient.getSequence().equals(sequence)) {
                return ingredient; // Return the matching ingredient
            }
        }
        return null;
    }

//    private static void updateParentIngredient1(ParentIngredient existingIngredient, ParentIngredient requestIngredient) {
//        // Update the fields from the request ingredient to the existing ingredient
//        existingIngredient.setIngreDescription(requestIngredient.getIngreDescription());
//        existingIngredient.setQuantity(requestIngredient.getQuantity());
//        existingIngredient.setUom(requestIngredient.getUom());
//        existingIngredient.setMaterialDescription(requestIngredient.getMaterialDescription());
//        existingIngredient.setStorageLocation(requestIngredient.getStorageLocation());
//        existingIngredient.setTolerance(requestIngredient.getTolerance());
//        existingIngredient.setMaterialType(requestIngredient.getMaterialType());
//        existingIngredient.setSupplierId(requestIngredient.getSupplierId());
//        existingIngredient.setSourceLocation(requestIngredient.getSourceLocation());
//        existingIngredient.setHandlingInstructions(requestIngredient.getHandlingInstructions());
//        existingIngredient.setStorageInstructions(requestIngredient.getStorageInstructions());
//        existingIngredient.setUnitCost(requestIngredient.getUnitCost());
//        existingIngredient.setCurrency(requestIngredient.getCurrency());
//        existingIngredient.setTotalCost(requestIngredient.getTotalCost());
//        existingIngredient.setWasteQuantity(requestIngredient.getWasteQuantity());
//        existingIngredient.setWasteUoM(requestIngredient.getWasteUoM());
//        existingIngredient.setBatchNumber(requestIngredient.getBatchNumber());
//        existingIngredient.setHazardous(requestIngredient.getHazardous());
//
//        updateByProduct(existingIngredient.getByProduct(), requestIngredient.getByProduct());
//        updateAlternateIngredients(existingIngredient, existingIngredient.getAlternateIngredients(), requestIngredient.getAlternateIngredients());
//        updateQualityControlParameters(existingIngredient.getQcParameters(), requestIngredient.getQcParameters());
//
//    }

    private void updatePhaseOperations(List<Operation> existingops, List<Operation> opRequests) {

        if (existingops == null) {
            existingops = new ArrayList<>();
        }

        if (opRequests == null){
            existingops.clear();
            return;
        }

        for (Operation opRequest : opRequests) {
            Operation existingop = findOperationById(existingops, opRequest.getSequence());

            if (existingop != null) {
                existingop.setOperationId(opRequest.getOperationId());
                existingop.setOperationVersion(opRequest.getOperationVersion());
                existingop.setOperationDescription(opRequest.getOperationDescription());
                existingop.setInstruction(opRequest.getInstruction());
                existingop.setTools(opRequest.getTools());
                existingop.setExpectedCycleTime(opRequest.getExpectedCycleTime());
                existingop.setType(opRequest.getType());
                existingop.setSequence(opRequest.getSequence());
                existingop.setCcp(opRequest.getCcp());
                existingop.setEntryOperation(opRequest.isEntryOperation());
                existingop.setLastOperationAtPhase(opRequest.isLastOperationAtPhase());
                existingop.setNextOperations(opRequest.getNextOperations());

                updateResources(existingop, existingop.getResources(), opRequest.getResources());
                updateDataCollections(existingop, existingop.getDataCollection(), opRequest.getDataCollection());
                updateQualityControlParameter(existingop, existingop.getQcParameters(), opRequest.getQcParameters());
                updateAdjustments(existingop, existingop.getAdjustments(), opRequest.getAdjustments());
                updateCriticalControlPoints(existingop, existingop.getCriticalControlPoints(), opRequest.getCriticalControlPoints());
                updateByProducts(existingop, existingop.getByProducts(), opRequest.getByProducts());
//                updatePhaseIngredients(existingop.getOpIngredients(), opRequest.getOpIngredients());

            } else {
                existingops.add(opRequest);
//                throw new RecipeException(151, opRequest.getOperationId());
            }
        }
    }

//    private static void updateOperationIngre(Operation existingOpIngre, List<Ingredients> opIngredients, List<Ingredients> opIngredientsRequests) {
//        if (opIngredientsRequests == null){
//            existingOpIngre.setOpIngredients(null);
//            return;
//        }
//
//        if (opIngredients == null) {
//            opIngredients = new ArrayList<>();
//        }
//
//        for (PhaseIngredient opIngredient : opIngredientsRequests) {
//            PhaseIngredient existingIngredient = findIngredientById(opIngredients, opIngredient.getIngredientId());
//
//            if (existingIngredient != null) {
//                existingIngredient.setIngredientId(opIngredient.getIngredientId());
//                existingIngredient.setIngreDescription(opIngredient.getIngreDescription());
//                existingIngredient.setQuantity(opIngredient.getQuantity());
//                existingIngredient.setSequence(opIngredient.getSequence());
//                existingIngredient.setAssociatedOp(opIngredient.getAssociatedOp());
//                existingIngredient.setUom(opIngredient.getUom());
//
//                updateQualityControlParameters(existingIngredient.getQcParameters(), opIngredient.getQcParameters());
//
//            } else {
//                opIngredients.add(opIngredient);
//            }
//        }
//        existingOpIngre.setOpIngredients(opIngredients);
//    }

    private static void updateByProducts(Operation existingop, List<ByProduct> existingByProducts, List<ByProduct> byProductRequests) {
        if (byProductRequests == null){
            existingop.setByProducts(null);
            return;
        }

        if (existingByProducts == null) {
            existingByProducts = new ArrayList<>();
        }

        for (ByProduct byProductRequest : byProductRequests) {
            ByProduct existingByProduct = findByProductById(existingByProducts, byProductRequest.getByProductId());

            if (existingByProduct != null) {
                updateByProduct(existingByProduct, byProductRequest);
            } else {
                existingByProducts.add(byProductRequest);
            }
        }
        existingop.setByProducts(existingByProducts);
    }

    private static ByProduct findByProductById(List<ByProduct> byProducts, String byProductId) {
        for (ByProduct byProduct : byProducts) {
            if (byProduct.getByProductId().equals(byProductId)) {
                return byProduct;
            }
        }
        return null;
    }



    private static void updateAdjustments(Operation existingop, List<Adjustment> existingAdjustments, List<Adjustment> adjustmentRequests) {
        if (adjustmentRequests == null){
            existingop.setAdjustments(null);
            return;
        }

        if (existingAdjustments == null) {
            existingAdjustments = new ArrayList<>();
        }

        for (Adjustment adjustmentRequest : adjustmentRequests) {
            Adjustment existingAdjustment = findAdjustmentById(existingAdjustments, adjustmentRequest.getAdjustmentId());

            if (existingAdjustment != null) {
                existingAdjustment.setReason(adjustmentRequest.getReason());
                existingAdjustment.setImpactOnProcess(adjustmentRequest.getImpactOnProcess());
            } else {
                existingAdjustments.add(adjustmentRequest);
            }
        }
        existingop.setAdjustments(existingAdjustments);
    }

    private static void updateParentAdjustments(Recipes existingRecipe, List<Adjustment> existingAdjustments, List<Adjustment> adjustmentRequests) {

        if (adjustmentRequests == null) {
            existingRecipe.setAdjustments(null);
            return;
        }

        if (existingAdjustments == null) {
            existingAdjustments = new ArrayList<>();
        }

        for (Adjustment adjustmentRequest : adjustmentRequests) {
            Adjustment existingAdjustment = findAdjustmentById(existingAdjustments, adjustmentRequest.getAdjustmentId());

            if (existingAdjustment != null) {
                existingAdjustment.setReason(adjustmentRequest.getReason());
                existingAdjustment.setImpactOnProcess(adjustmentRequest.getImpactOnProcess());
                existingAdjustment.setImpactOnYield(adjustmentRequest.getImpactOnYield());
                existingAdjustment.setEffectOnCycleTime(adjustmentRequest.getEffectOnCycleTime());
                existingAdjustment.setEffectOnQuality(adjustmentRequest.getEffectOnQuality());
                existingAdjustment.setReason(adjustmentRequest.getReason());
                existingAdjustment.setImpactOnProcess(adjustmentRequest.getImpactOnProcess());
            } else {
                existingAdjustments.add(adjustmentRequest);
            }
        }
        existingRecipe.setAdjustments(existingAdjustments);
    }

    private static Adjustment findAdjustmentById(List<Adjustment> adjustments, String adjustmentId) {
        for (Adjustment adjustment : adjustments) {
            if (adjustment.getAdjustmentId().equals(adjustmentId)) {
                return adjustment;
            }
        }
        return null;
    }

    private static void updateCriticalControlPoints(Operation existingop, CriticalControlPoints existingCCP, CriticalControlPoints ccpRequest) {
        if (ccpRequest == null){
            existingop.setCriticalControlPoints(null);
            return;
        }

        if (existingCCP == null) {
            existingCCP = new CriticalControlPoints();
        }

        existingCCP.setCcpId(ccpRequest.getCcpId());
        existingCCP.setDescription(ccpRequest.getDescription());
        existingCCP.setCriticalLimits(ccpRequest.getCriticalLimits());
        existingCCP.setMonitoringFrequency(ccpRequest.getMonitoringFrequency());
        existingCCP.setCorrectiveAction(ccpRequest.getCorrectiveAction());

        existingop.setCriticalControlPoints(existingCCP);
    }


    private static CriticalControlPoints findCCPById(List<CriticalControlPoints> ccps, String ccpId) {
        for (CriticalControlPoints ccp : ccps) {
            if (ccp.getSequence().equals(ccpId)) {
                return ccp;
            }
        }
        return null;
    }


    private static void updateQualityControlParameter(Operation existingop, QualityControlParameter existingQcParameters, QualityControlParameter qcParameterRequest) {
        if (qcParameterRequest == null){
            existingop.setQcParameters(null);
            return;
        }

        if(existingQcParameters == null){
            existingQcParameters = new QualityControlParameter();
        }

        existingQcParameters.setQcId(qcParameterRequest.getQcId());
        existingQcParameters.setQcDescription(qcParameterRequest.getQcDescription());
        existingQcParameters.setParameter(qcParameterRequest.getParameter());
        existingQcParameters.setActualValue(qcParameterRequest.getActualValue());
        existingQcParameters.setExpectedValue(qcParameterRequest.getExpectedValue());
        existingQcParameters.setMonitoringFrequency(qcParameterRequest.getMonitoringFrequency());
        existingQcParameters.setToolsRequired(qcParameterRequest.getToolsRequired());
        existingQcParameters.setActionsOnFailure(qcParameterRequest.getActionsOnFailure());
        existingQcParameters.setTolerance(qcParameterRequest.getTolerance());
        existingQcParameters.setMin(qcParameterRequest.getMin());
        existingQcParameters.setMax(qcParameterRequest.getMax());

        existingop.setQcParameters(existingQcParameters);
    }


    private static Operation findOperationById(List<Operation> ops, String opId) {
        for (Operation op : ops) {
            if (op.getSequence().equals(opId)) {
                return op;
            }
        }
        return null;
    }

    private static void updateDataCollections(Operation existingop, List<DataCollection> existingDataCollections, List<DataCollection> dataCollectionRequests) {
        if (dataCollectionRequests == null){
            existingop.setDataCollection(null);
            return;
        }

        if (existingDataCollections == null) {
            existingDataCollections = new ArrayList<>();
        }

        for (DataCollection dataCollectionRequest : dataCollectionRequests) {
            DataCollection existingDataCollection = findDataCollectionById(existingDataCollections, dataCollectionRequest.getDataPointId());

            if (existingDataCollection != null) {
                existingDataCollection.setDataPointId(dataCollectionRequest.getDataPointId());
                existingDataCollection.setDescription(dataCollectionRequest.getDescription());
                existingDataCollection.setFrequency(dataCollectionRequest.getFrequency());
                existingDataCollection.setExpectedValueRange(dataCollectionRequest.getExpectedValueRange());
                existingDataCollection.setParameter(dataCollectionRequest.getParameter());
                existingDataCollection.setExpectedValue(dataCollectionRequest.getExpectedValue());
                existingDataCollection.setMonitoringFrequency(dataCollectionRequest.getMonitoringFrequency());
                existingDataCollection.setAllowedVariance(dataCollectionRequest.getAllowedVariance());
            } else {
                existingDataCollections.add(dataCollectionRequest);
//                throw new RecipeException(152, dataCollectionRequest.getDataPointId());
            }
        }
        existingop.setDataCollection(existingDataCollections);
    }

    private static DataCollection findDataCollectionById(List<DataCollection> dataCollections, String dataPointId) {
        for (DataCollection dataCollection : dataCollections) {
            if (dataCollection.getDataPointId().equals(dataPointId)) {
                return dataCollection;
            }
        }
        return null;
    }

    private static void updateResources(Operation existingop, List<Resource> existingResources, List<Resource> resourceRequests) {
        if (resourceRequests == null){
            existingop.setResources(null);
            return;
        }

        if (existingResources == null) {
            existingResources = new ArrayList<>();
        }

        for (Resource resourceRequest : resourceRequests) {
            Resource existingResource = findResourceById(existingResources, resourceRequest.getResourceId());

            if (existingResource != null) {
                existingResource.setResourceId(resourceRequest.getResourceId());
                existingResource.setDescription(resourceRequest.getDescription());
                existingResource.setWorkCenterId(resourceRequest.getWorkCenterId());

                updateResourceParameters(existingResource, existingResource.getParameters(), resourceRequest.getParameters());

            } else {
                existingResources.add(resourceRequest);
            }
        }
        existingop.setResources(existingResources);
    }

    private static void updateResourceParameters(Resource existingResource, ResourceParameters existingParameters, ResourceParameters parameterRequest) {
        if (parameterRequest == null){
            existingResource.setParameters(null);
            return;
        }

        if (existingParameters == null) {
            existingParameters = new ResourceParameters();
//            existingResource.setParameters(existingParameters);
        }

        existingParameters.setRpm(parameterRequest.getRpm());
        existingParameters.setDuration(parameterRequest.getDuration());
        existingParameters.setPressure(parameterRequest.getPressure());
        existingResource.setParameters(existingParameters);

    }

    private static Resource findResourceById(List<Resource> resources, String resourceId) {
        for (Resource resource : resources) {
            if (resource.getResourceId().equals(resourceId)) {
                return resource;
            }
        }
        return null;
    }

    private static void updateCompliance(Recipes recipes, Compliance existingCompliance, Compliance complianceRequest) {
        if (complianceRequest == null) {
            recipes.setCompliance(null);
            return;
        }

        if (existingCompliance == null) {
            existingCompliance = new Compliance();
        }

        existingCompliance.setRegulatoryAgencies(complianceRequest.getRegulatoryAgencies());
        existingCompliance.setAuditRequired(complianceRequest.isAuditRequired());

        recipes.setCompliance(existingCompliance);
    }


    private static void updateOperatorActions(Recipes recipes, List<OperatorAction> existingActions, List<OperatorAction> actionRequests) {
        if (actionRequests == null) {
            recipes.setOperatorActions(null);
            return;
        }

        if (existingActions == null) {
            existingActions = new ArrayList<>();
        }


        for (OperatorAction actionRequest : actionRequests) {
            OperatorAction existingAction = findOperatorActionByopId(existingActions, actionRequest.getSequence());

            if (existingAction != null) {
                existingAction.setOpId(actionRequest.getOpId());
                existingAction.setAction(actionRequest.getAction());
                existingAction.setReason(actionRequest.getReason());
                existingAction.setApprovalRequired(actionRequest.isApprovalRequired());
            } else {
//                existingActions.addAll(actionRequests);
                throw new RecipeException(153, actionRequest.getOpId());
            }
        }
        recipes.setOperatorActions(existingActions);
    }

    private static OperatorAction findOperatorActionByopId(List<OperatorAction> actions, String opId) {
        for (OperatorAction action : actions) {
            if (action.getSequence().equals(opId)) {
                return action;
            }
        }
        return null;
    }


    private static void updateSafetyProcedures(Recipes recipes, List<SafetyProcedure> existingProcedures, List<SafetyProcedure> procedureRequests) {

        if (procedureRequests == null){
            recipes.setSafetyProcedures(null);
            return;
        }

        if (existingProcedures == null) {
            existingProcedures = new ArrayList<>();
        }

        for (SafetyProcedure procedureRequest : procedureRequests) {
            SafetyProcedure existingProcedure = findSafetyProcedureById(existingProcedures, procedureRequest.getSequence());

            if (existingProcedure != null) {
                existingProcedure.setOpId(procedureRequest.getOpId());
                existingProcedure.setRiskFactor(procedureRequest.getRiskFactor());

                updateMitigations(existingProcedure.getMitigation(), procedureRequest.getMitigation());
            } else {
                existingProcedures.addAll(procedureRequests);
//                throw new RecipeException(154, procedureRequest.getSequence());
            }
        }
        recipes.setSafetyProcedures(existingProcedures);
    }

    private static SafetyProcedure findSafetyProcedureById(List<SafetyProcedure> procedures, String opId) {
        for (SafetyProcedure procedure : procedures) {
            if (procedure.getSequence().equals(opId)) {
                return procedure;
            }
        }
        return null;
    }

    private static void updateMitigations(List<Mitigation> existingMitigations, List<Mitigation> mitigationRequests) {
        if (existingMitigations == null) {
            existingMitigations = new ArrayList<>();
        }

        if (mitigationRequests == null){
            existingMitigations.clear();
            return;
        }


        for (Mitigation mitigationRequest : mitigationRequests) {
            Mitigation existingMitigation = findMitigationByOp(existingMitigations, mitigationRequest.getSequence());

            if (existingMitigation != null) {
                existingMitigation.setMitigationOp(mitigationRequest.getMitigationOp());
            } else {
                existingMitigations.add(mitigationRequest);
            }
        }
    }

    private static Mitigation findMitigationByOp(List<Mitigation> mitigations, String mitigationOp) {
        for (Mitigation mitigation : mitigations) {
            if (mitigation.getSequence().equals(mitigationOp)) {
                return mitigation;
            }
        }
        return null;
    }


    @Override
    public RecipeMessageModel deleteRecipe(RecipeRequests recipeRequest) throws Exception {
        Recipes existingRecipe = getExistingRecipe(recipeRequest);
        if (existingRecipe != null) {

            if(existingRecipe.getStatus().equals("inuse"))
                throw new RecipeException(129,recipeRequest.getRecipeId());

            existingRecipe.setActive(0);
            existingRecipe.setModifiedDate(LocalDateTime.now());
            existingRecipe.setModifiedBy(recipeRequest.getUser());

            recipeServiceRepository.save(existingRecipe);
            existingRecipe = null;

            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails(recipeRequest.getRecipeId() + " Recipe deleted", "S"))
                    .build();
        }
        throw new RecipeException(114,recipeRequest.getRecipeId());
    }

    @Override
    public RecipeMessageModel getRecipe(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = null;

        if(recipeRequest.getVersion() == null || recipeRequest.getVersion().equals("")){
            existingRecipe = recipeServiceRepository.findBySiteAndRecipeIdAndCurrentVersionAndActive(recipeRequest.getSite(), recipeRequest.getRecipeId(), true, 1);

            if (existingRecipe == null) {
                throw new RecipeException(118, recipeRequest.getRecipeId());
            }
        } else {
            existingRecipe = getExistingRecipe(recipeRequest);
            if (existingRecipe == null) {
                throw new RecipeException(118, recipeRequest.getRecipeId());
            }
        }

        return RecipeMessageModel.builder().response(existingRecipe).build();
    }

    @Override
    public RecipeMessageModel recipesList(String site, String recipeId) throws Exception {
        List<RecipeRequest> recipeResponse;
        if (recipeId == null || recipeId.isEmpty()) {
            recipeResponse = recipeServiceRepository.findBySiteAndActiveOrderByCreatedDateDesc(site, 1);
        } else {
            recipeResponse = recipeServiceRepository.findBySiteAndRecipeIdAndActive(site, recipeId, 1);
        }
        if (recipeResponse == null || recipeResponse.isEmpty()) {
            throw new RecipeException(124);
        }
        return RecipeMessageModel.builder().responseList(recipeResponse).build();
    }

    @Override
    public boolean isExist(String site, String recipeId, String version) throws Exception {
        if (version != null && !version.isEmpty()) {
            return recipeServiceRepository.existsByRecipeIdAndVersionAndSiteAndActiveEquals(recipeId, version, site, 1);
        } else {
            return recipeServiceRepository.existsByRecipeIdAndCurrentVersionAndSiteAndActive(recipeId, true, site, 1);
        }
    }

    @Override
    public RecipeMessageModel addIngredients(RecipeRequests recipeRequest) throws Exception {
        if(StringUtils.hasText(recipeRequest.getParentIngredient().getOperationId())) {
            if (!StringUtils.hasText(recipeRequest.getOperationId()) || !StringUtils.hasText(recipeRequest.getOpVersion()))
                return RecipeMessageModel.builder().message_details(new MessageDetails("operation or operation version is empty", "E")).build();

            if (!validateOperation(recipeRequest.getSite(), recipeRequest.getParentIngredient().getOperationId(), recipeRequest.getParentIngredient().getOperationVersion()))
                return RecipeMessageModel.builder().message_details(new MessageDetails("operation is not valid", "E")).build();
        }
        List<AlternateIngredient> alternateIngredients = recipeRequest.getParentIngredient().getAlternateIngredients();

        if (alternateIngredients != null && !alternateIngredients.isEmpty()) {
            boolean dateAccepted = alternateIngredients.stream()
                    .allMatch(alternateIngredient -> alternateIngredient.getExpiryDate() != null
                            && alternateIngredient.getManufactureDate() != null
                            && alternateIngredient.getExpiryDate().isBefore(alternateIngredient.getManufactureDate()));

            if (dateAccepted) {
                throw new RecipeException(155);
            }
        }

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();


        if ("active".equalsIgnoreCase(recipeRequest.getIngredientType())) {
            if (existingRecipe.getIngredients() == null) {
                Ingredients ingredients = new Ingredients();
                ingredients.setActive(new ArrayList<>());
                ingredients.getActive().add(recipeRequest.getParentIngredient());
                existingRecipe.setIngredients(ingredients);
            } else if (existingRecipe.getIngredients().getActive() == null) {
                existingRecipe.getIngredients().setActive(new ArrayList<>());
                existingRecipe.getIngredients().getActive().add(recipeRequest.getParentIngredient());
            } else {
                RecipeRequests finalRecipeRequest = recipeRequest;
                boolean ingredientExists = existingRecipe.getIngredients().getActive().stream()
                        .anyMatch(ingredient -> ingredient.getSequence().equals(finalRecipeRequest.getParentIngredient().getSequence()));

                if (ingredientExists) {
                    return RecipeMessageModel.builder()
                            .message_details(new MessageDetails("Ingredient ID already exists in active ingredients", "E"))
                            .build();
                } else {
                    existingRecipe.getIngredients().getActive().add(recipeRequest.getParentIngredient());
                }
            }
        }

        if ("inActive".equalsIgnoreCase(recipeRequest.getIngredientType())) {
            if (existingRecipe.getIngredients() == null) {
                Ingredients ingredients = new Ingredients();
                ingredients.setInactive(new ArrayList<>());
                ingredients.getInactive().add(recipeRequest.getParentIngredient());
                existingRecipe.setIngredients(ingredients);
            } else if (existingRecipe.getIngredients().getInactive() == null) {
                existingRecipe.getIngredients().setInactive(new ArrayList<>());
                existingRecipe.getIngredients().getInactive().add(recipeRequest.getParentIngredient());
            } else {
                RecipeRequests finalRecipeRequest = recipeRequest;
                boolean ingredientExists = existingRecipe.getIngredients().getInactive().stream()
                        .anyMatch(ingredient -> ingredient.getSequence().equals(finalRecipeRequest.getParentIngredient().getSequence()));

                if (ingredientExists) {
                    return RecipeMessageModel.builder()
                            .message_details(new MessageDetails("Ingredient ID already exists in inActive ingredients", "E"))
                            .build();
                } else {
                    existingRecipe.getIngredients().getInactive().add(recipeRequest.getParentIngredient());
                }
            }
        }
        existingRecipe.setModifiedBy(recipeRequest.getUser());
        existingRecipe.setModifiedDate(LocalDateTime.now());
        recipeServiceRepository.save(existingRecipe);
        return RecipeMessageModel.builder()
                .message_details(new MessageDetails("Ingredient added successfully to active ingredients", "S"))
                .build();
    }

    @Override
    public RecipeMessageModel updateIngredient(RecipeRequests recipeRequest) throws Exception {
        if(StringUtils.hasText(recipeRequest.getParentIngredient().getOperationId())) {
            if (!StringUtils.hasText(recipeRequest.getOperationId()) || !StringUtils.hasText(recipeRequest.getOpVersion()))
                return RecipeMessageModel.builder().message_details(new MessageDetails("operation or operation version is empty", "E")).build();

            if (!validateOperation(recipeRequest.getSite(), recipeRequest.getParentIngredient().getOperationId(), recipeRequest.getParentIngredient().getOperationVersion()))
                return RecipeMessageModel.builder().message_details(new MessageDetails("operation is not valid", "E")).build();
        }

        List<AlternateIngredient> alternateIngredients = recipeRequest.getParentIngredient().getAlternateIngredients();

        if (alternateIngredients != null && !alternateIngredients.isEmpty()) {
            boolean dateAccepted = alternateIngredients.stream()
                    .allMatch(alternateIngredient -> alternateIngredient.getExpiryDate() != null
                            && alternateIngredient.getManufactureDate() != null
                            && alternateIngredient.getExpiryDate().isBefore(alternateIngredient.getManufactureDate()));

            if (dateAccepted) {
                throw new RecipeException(155);
            }
        }

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getIngredients() == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("ingredients not present", "E")).build();

        String ingredientIdToUpdate = recipeRequest.getIngreSequence();
        boolean ingredientFound = false;

        if (recipeRequest.getIngredientType().equals("active") && existingRecipe.getIngredients().getActive() != null) {
            ingredientFound = updateIngredientIfExists(existingRecipe.getIngredients().getActive(), ingredientIdToUpdate, recipeRequest);
        }

        if (!ingredientFound && recipeRequest.getIngredientType().equals("inactive") && existingRecipe.getIngredients().getInactive() != null) {
            ingredientFound = updateIngredientIfExists(existingRecipe.getIngredients().getInactive(), ingredientIdToUpdate, recipeRequest);
        }

        if (ingredientFound) {
            existingRecipe.setModifiedBy(recipeRequest.getUser());
            existingRecipe.setModifiedDate(LocalDateTime.now());
            recipeServiceRepository.save(existingRecipe);
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Ingredient updated successfully", "S"))
                    .build();
        } else {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Ingredient ID not found for update", "E"))
                    .build();
        }
    }

    private boolean updateIngredientIfExists(List<ParentIngredient> ingredients, String ingredientIdToUpdate, RecipeRequests recipeRequest) {
        return ingredients.stream()
                .filter(ingredient -> ingredient.getSequence().equals(ingredientIdToUpdate))
                .findFirst()
                .map(ingredient -> {
                    updateParentIngredient(ingredient, recipeRequest.getParentIngredient());
                    return true;
                })
                .orElse(false);
    }


    private void updateParentIngredient(ParentIngredient ingredientToUpdate, ParentIngredient recipeRequest) {

        existingParentIngredientUpdate(ingredientToUpdate, recipeRequest);
        ingredientToUpdate.setStorageLocation(recipeRequest.getStorageLocation());
        ingredientToUpdate.setTolerance(recipeRequest.getTolerance());
        ingredientToUpdate.setMaterialType(recipeRequest.getMaterialType());
        ingredientToUpdate.setSupplierId(recipeRequest.getSupplierId());
        ingredientToUpdate.setSourceLocation(recipeRequest.getSourceLocation());
        ingredientToUpdate.setHandlingInstructions(recipeRequest.getHandlingInstructions());
        ingredientToUpdate.setStorageInstructions(recipeRequest.getStorageInstructions());
        ingredientToUpdate.setUnitCost(recipeRequest.getUnitCost());
        ingredientToUpdate.setCurrency(recipeRequest.getCurrency());
        ingredientToUpdate.setTotalCost(recipeRequest.getTotalCost());
        ingredientToUpdate.setWasteQuantity(recipeRequest.getWasteQuantity());
        ingredientToUpdate.setWasteUoM(recipeRequest.getWasteUoM());
        ingredientToUpdate.setBatchNumber(recipeRequest.getBatchNumber());
        ingredientToUpdate.setHazardous(recipeRequest.getHazardous());
        ingredientToUpdate.setExpiryDate(recipeRequest.getExpiryDate());
        ingredientToUpdate.setManufactureDate(recipeRequest.getManufactureDate());
    }


    @Override
    public RecipeMessageModel deleteIngredient(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getIngredients() == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("ingredients not present", "E")).build();

        List<ParentIngredient> activeIngredients = existingRecipe.getIngredients().getActive();
        List<ParentIngredient> inactiveIngredients = existingRecipe.getIngredients().getInactive();

        String ingredientIdToRemove = recipeRequest.getIngreSequence();
        boolean deleteIngredient = false;

        for (Iterator<ParentIngredient> iterator = activeIngredients.iterator(); iterator.hasNext();) {
            ParentIngredient activeIngredient = iterator.next();
            if (activeIngredient.getSequence().equals(ingredientIdToRemove)) {
                iterator.remove();
                deleteIngredient = true;
                break;
            }
        }

        if(!deleteIngredient) {
            for (Iterator<ParentIngredient> iterator = inactiveIngredients.iterator(); iterator.hasNext(); ) {
                ParentIngredient inactiveIngredient = iterator.next();
                if (inactiveIngredient.getSequence().equals(ingredientIdToRemove)) {
                    iterator.remove();
                    deleteIngredient = true;
                    break;
                }
            }
        }

        if(deleteIngredient) {
            existingRecipe.setModifiedBy(recipeRequest.getUser());
            existingRecipe.setModifiedDate(LocalDateTime.now());
            recipeServiceRepository.save(existingRecipe);
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Ingredients deleted", "S"))
                    .build();
        }
        existingRecipe = null;

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails("Ingredients not present to delete", "E"))
                .build();

    }

    @Override
    public RecipeMessageModel addPhases(RecipeRequests recipeRequest) throws RecipeException {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if ((recipeRequest.getPhase().getConditional() == null || !recipeRequest.getPhase().getConditional()) &&
                (recipeRequest.getPhase().getParallel() == null || !recipeRequest.getPhase().getParallel()) && (recipeRequest.getPhase().getAnyOrder() == null || !recipeRequest.getPhase().getAnyOrder())) {
            recipeRequest.getPhase().setSequential(true);
        }

        Phase userPhase = recipeRequest.getPhase();

        if(existingRecipe.getPhases() != null && !existingRecipe.getPhases().isEmpty()) {

            boolean phaseExists = existingRecipe.getPhases().stream()
                    .anyMatch(phase -> phase.getSequence().equals(recipeRequest.getPhaseSequence()));

            if (phaseExists) {
                return RecipeMessageModel.builder()
                        .message_details(new MessageDetails("phaseId already exists, so not added", "E"))
                        .build();
            } else {
                existingRecipe.getPhases().add(userPhase);
            }

        } else {
            existingRecipe.setPhases(new ArrayList<>());
            existingRecipe.getPhases().add(userPhase);
        }
        existingRecipe.setModifiedBy(recipeRequest.getUser());
        existingRecipe.setModifiedDate(LocalDateTime.now());
        recipeServiceRepository.save(existingRecipe);
        existingRecipe = null;

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails("Phases added", "S"))
                .build();
    }

    @Override
    public RecipeMessageModel updatePhase(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not available" , "E")).build();

        boolean isUpdated = false;
        for (Phase incomingPhase : recipeRequest.getPhases()) {

            for (Phase existingPhase : existingRecipe.getPhases()) {
                if (existingPhase.getSequence().equals(incomingPhase.getSequence())) {
                    updatePhaseFields(existingPhase, incomingPhase);
                    isUpdated = true;
                    existingRecipe.setModifiedBy(recipeRequest.getUser());
                    existingRecipe.setModifiedDate(LocalDateTime.now());
                    recipeServiceRepository.save(existingRecipe);
                    break;
                }
            }
        }
        existingRecipe = null;

        if(isUpdated)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Phase updated", "S"))
                    .build();
        else
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("phase dosent exist to update", "E"))
                    .build();

    }

    private void updatePhaseFields(Phase existingPhase, Phase incomingPhase) {
        existingPhase.setPhaseId(incomingPhase.getPhaseId());
        existingPhase.setPhaseDescription(incomingPhase.getPhaseDescription());

        existingPhase.setSequence(incomingPhase.getSequence());
        existingPhase.setExpectedCycleTime(incomingPhase.getExpectedCycleTime());
        existingPhase.setConditional(incomingPhase.getConditional() != null && incomingPhase.getConditional());
        existingPhase.setParallel(incomingPhase.getParallel() != null && incomingPhase.getParallel());
        existingPhase.setAnyOrder(incomingPhase.getAnyOrder() != null && incomingPhase.getAnyOrder());
        existingPhase.setTriggeredPhase(incomingPhase.getTriggeredPhase());
        existingPhase.setEntryPhase(incomingPhase.isEntryPhase());
        existingPhase.setExitPhase(incomingPhase.isExitPhase());
        existingPhase.setNextPhase(incomingPhase.getNextPhase());

        if ((incomingPhase.getConditional() == null || !incomingPhase.getConditional()) &&
                (incomingPhase.getParallel() == null || !incomingPhase.getParallel()) && (incomingPhase.getAnyOrder() == null || !incomingPhase.getAnyOrder())) {
            existingPhase.setSequential(true);
        }

        existingPhase.setTriggeredPhase(incomingPhase.getTriggeredPhase());

        updatePhaseIngredients(existingPhase.getIngredients(), existingPhase.getIngredients());
        updatePhaseOperations(existingPhase.getOperations(), existingPhase.getOperations());

    }


    @Override
    public RecipeMessageModel deletePhase(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not available" , "E")).build();

        boolean removed = existingRecipe.getPhases().removeIf(phase -> recipeRequest.getPhaseSequence().equals(phase.getSequence()));

        if (!removed)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("phase not present to delete in " + recipeRequest.getRecipeId(), "E"))
                    .build();
        existingRecipe.setModifiedBy(recipeRequest.getUser());
        existingRecipe.setModifiedDate(LocalDateTime.now());
        recipeServiceRepository.save(existingRecipe);
        existingRecipe = null;

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails("Phase deleted from recipe " + recipeRequest.getRecipeId(), "S"))
                .build();
    }

    @Override
    public RecipeMessageModel getRecipePhases(RecipeRequests recipeRequest) throws Exception {
        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if(existingRecipe == null)
            throw new RecipeException(114, recipeRequest.getRecipeId());

        return RecipeMessageModel.builder().phases(existingRecipe.getPhases()).message_details(new MessageDetails("Phase list","S")).build();

    }

    @Override
    public RecipeMessageModel getPhaseOperations(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not available" , "E")).build();

        List<Operation> ops = existingRecipe.getPhases().stream()
                .filter(phase -> phase.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .map(Phase::getOperations)
                .orElse(Collections.emptyList())
                .stream()
                .filter(op ->
                        "sequential".equalsIgnoreCase(op.getType()) ||
                        "parallel".equalsIgnoreCase(op.getType()) ||
                        "conditional".equalsIgnoreCase(op.getType())
                )
                .collect(Collectors.toList());

        existingRecipe = null;

        if(ops.isEmpty())
            return RecipeMessageModel.builder().message_details(new MessageDetails("Operation not found","S")).build();

        return RecipeMessageModel.builder().ops(ops).build();

    }

    @Override
    public RecipeMessageModel getPhaseOperationById(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not available" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return RecipeMessageModel.builder().message_details(new MessageDetails("phase Id not found" , "E")).build();
        }

        Operation op = phase.getOperations().stream()
                .filter(s -> s.getSequence().equals(recipeRequest.getOpSequence()))
                .findFirst()
                .orElse(null);

        existingRecipe = null;

        if(op == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("Operation not found","S")).build();

        List<ParentIngredient> relevantActiveIngredients = new ArrayList<>();
        List<ParentIngredient> relevantInactiveIngredients = new ArrayList<>();

        Ingredients ingredients = phase.getIngredients();
        if (ingredients != null) {
            // Process active ingredients
            if (ingredients.getActive() != null) {
                for (ParentIngredient activeIngredient : ingredients.getActive()) {
                    if (isIngredientRelevantToOperation(activeIngredient, op)) {
                        relevantActiveIngredients.add(activeIngredient);
                    }
                }
            }

            // Process inactive ingredients
            if (ingredients.getInactive() != null) {
                for (ParentIngredient inactiveIngredient : ingredients.getInactive()) {
                    if (isIngredientRelevantToOperation(inactiveIngredient, op)) {
                        relevantInactiveIngredients.add(inactiveIngredient);
                    }
                }
            }
        }

        Ingredients filteredIngredients = new Ingredients();
        filteredIngredients.setActive(relevantActiveIngredients);
        filteredIngredients.setInactive(relevantInactiveIngredients);

        return RecipeMessageModel.builder()
                .op(op)
                .phasesIngredients(filteredIngredients)
                .build();

    }

    private boolean isIngredientRelevantToOperation(ParentIngredient ingredient, Operation op) {
        return ingredient.getOperationId() == null || ingredient.getOperationId().isEmpty() ||
                ingredient.getOperationId().equals(op.getOperationId());
    }

    @Override
    public RecipeMessageModel trackYield(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (recipeRequest.getYieldTracking() == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("yield tracking not found", "E")).build();

        String actualYield = recipeRequest.getYieldTracking().getActualYield();

        if (actualYield == null || actualYield.isEmpty()) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("actualYield value is empty", "E"))
                    .build();
        }

        if(existingRecipe.getYieldTracking() == null)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("yieldTracking is empty", "E"))
                    .build();

        YieldTracking yieldTracking = existingRecipe.getYieldTracking();

        yieldTracking.setActualYield(actualYield);

        // Update ByProducts
        for (ByProduct byProductUpdate : recipeRequest.getYieldTracking().getByProducts()) {
            boolean found = existingRecipe.getYieldTracking().getByProducts().stream()
                    .filter(existingByProduct -> existingByProduct.getSequence().equals(byProductUpdate.getSequence()))
                    .peek(existingByProduct -> {
                        existingByProduct.setDescription(byProductUpdate.getDescription() );
                        existingByProduct.setExpectedQuantity(byProductUpdate.getExpectedQuantity());
                        existingByProduct.setUom(byProductUpdate.getUom());
                        existingByProduct.setHandlingProcedure(byProductUpdate.getHandlingProcedure());
                    })
                    .findFirst()
                    .isPresent();

            if (!found) {
                throw new RecipeException(126 ,byProductUpdate.getByProductId());
            }
        }

        // Update Wastes
        for (Waste wasteUpdate : recipeRequest.getYieldTracking().getWaste()) {
            boolean found = existingRecipe.getYieldTracking().getWaste().stream()
                    .filter(existingWaste -> existingWaste.getSequence().equals(wasteUpdate.getSequence()))
                    .peek(existingWaste -> {
                        existingWaste.setDescription(wasteUpdate.getDescription());
                        existingWaste.setQuantity(wasteUpdate.getQuantity() );
                        existingWaste.setUom(wasteUpdate.getUom());
                        existingWaste.setHandlingProcedure(wasteUpdate.getHandlingProcedure());
                        existingWaste.setCostOfDisposal(wasteUpdate.getCostOfDisposal());
                    })
                    .findFirst()
                    .isPresent();

            if (!found) {
                throw new RecipeException(127 ,wasteUpdate.getWasteId());
            }
        }

        String expectedYield = yieldTracking.getExpectedYield();
        if (Double.parseDouble(actualYield) < Double.parseDouble(expectedYield)) {
            // triggerCorrections(recipeId)
        }

//        yieldTracking.setCorrections(existingRecipe.getYieldTracking().getCorrections());
//        yieldTracking.setQualityDeviations(existingRecipe.getYieldTracking().getQualityDeviations());
        existingRecipe.setModifiedBy(recipeRequest.getUser());
        existingRecipe.setModifiedDate(LocalDateTime.now());

        recipeServiceRepository.save(existingRecipe);

        existingRecipe = null;

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails("Yield tracked successfully", "S"))
                .build();
    }

    @Override
    public RecipeMessageModel validateRecipeData(RecipeRequests recipeRequest) throws Exception {
        boolean validateRecipe = validateRecipe(recipeRequest);
        String recipeValidator = validateRecipe ? "Recipe validated" : "Recipe validation failed";

        boolean validateIng = validateIngredients(recipeRequest);
        String ingredientValidator = validateIng ? "ingredient validated" : "ingredient validation failed";

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails(recipeValidator + " and " +  ingredientValidator, "S"))
                .build();
    }



    public boolean validateRecipe(RecipeRequests recipeData) throws Exception{
        Recipes recipes = getExistingRecipe(recipeData);
        if(recipes != null){

            if(recipes.getIngredients() == null)
                throw new RecipeException(132, recipes.getRecipeId());

            Ingredients ingredients = recipes.getIngredients();
            boolean activeIngredientFound = false;
            boolean inactiveIngredientFound = false;

            if(ingredients.getActive() != null) {
                activeIngredientFound = ingredients.getActive().stream()
                        .anyMatch(active -> active.getSequence().equals(recipeData.getIngreSequence()));
            }

            if(ingredients.getInactive() != null) {
                inactiveIngredientFound = ingredients.getInactive().stream()
                        .anyMatch(inactive -> inactive.getSequence().equals(recipeData.getIngreSequence()));
            }

            boolean ingredientFound = activeIngredientFound || inactiveIngredientFound;

            boolean phaseFound = recipes.getPhases().stream()
                    .anyMatch(phase -> phase.getSequence().equals(recipeData.getPhaseSequence()));

            recipes = null;

            return ingredientFound && phaseFound;
        }
        return false;
    }

    public boolean validateIngredients(RecipeRequests request) {
        if (request.getIngredients() == null) {
            return false;
        }

        Recipes existingRecipe = getExistingRecipe(request);
        if (existingRecipe == null)
            throw new RecipeException(114, request.getRecipeId());

        Ingredients requestIngredients = request.getIngredients();

//        for (Ingredients requestIngredient : requestIngredients) {
        boolean isIngredientValid = existingRecipe.getPhases() != null &&
                existingRecipe.getPhases().stream()
                        .filter(phase -> phase.getIngredients() != null) // Only consider phases with ingredients
                        .anyMatch(phase -> matchParentIngredients(phase.getIngredients(), requestIngredients));

        return isIngredientValid;
    }

    private boolean matchParentIngredients(Ingredients phaseIngredients, Ingredients requestIngredients) {
        if (phaseIngredients == null || requestIngredients == null) {
            return false;
        }

        if (requestIngredients.getActive() != null) {
            for (ParentIngredient requestIngredient : requestIngredients.getActive()) {
                boolean activeMatch = matchSingleIngredient(phaseIngredients.getActive(), requestIngredient);
                if (!activeMatch) {
                    return false;
                }
            }
        }

        if (requestIngredients.getInactive() != null) {
            for (ParentIngredient requestIngredient : requestIngredients.getInactive()) {
                boolean inactiveMatch = matchSingleIngredient(phaseIngredients.getInactive(), requestIngredient);
                if (!inactiveMatch) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean matchSingleIngredient(List<ParentIngredient> phaseIngredientList, ParentIngredient requestIngredient) {
        if (phaseIngredientList == null || requestIngredient == null) {
            return false;
        }

        return phaseIngredientList.stream().anyMatch(phaseIngredient ->
                        phaseIngredient.getIngredientId().equals(requestIngredient.getIngredientId()) && phaseIngredient.getSequence().equals(requestIngredient.getSequence()) &&
                                phaseIngredient.getUom().equals(requestIngredient.getUom())
        );
    }

    @Override
    public RecipeMessageModel verifyIngredients(RecipeRequests recipeRequest) throws Exception {
        String status = " ";

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getIngredients() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("ingredients not found" , "E")).build();

        List<ParentIngredient> activeIngredients = new ArrayList<>();
        List<ParentIngredient> inActiveIngredients = new ArrayList<>();

        if (recipeRequest.getIngredients().getActive() != null) {
            activeIngredients.addAll(recipeRequest.getIngredients().getActive());
        }

        if (recipeRequest.getIngredients().getInactive() != null) {
            inActiveIngredients.addAll(recipeRequest.getIngredients().getInactive());
        }

        for (ParentIngredient activeIngredient : activeIngredients) {
            boolean isVerified = activeIngredient.getQcParameters().stream().allMatch(requestQcParam ->
                    existingRecipe.getIngredients().getActive().stream()
                            .anyMatch(existingIngredient ->
                                    existingIngredient.getSequence().equals(activeIngredient.getSequence()) &&
                                            existingIngredient.getQcParameters().stream().anyMatch(existingQcParam ->
                                                    existingQcParam.getSequence().equals(requestQcParam.getSequence()) &&
                                                            Math.abs(Double.parseDouble(existingQcParam.getActualValue()) - Double.parseDouble(requestQcParam.getActualValue())) <= Double.parseDouble(existingQcParam.getTolerance())
                                            )
                            )
            );

            if (!isVerified) {
                status = status + activeIngredient.getIngredientId() + "failed ";
            } else {
                status = status + activeIngredient.getIngredientId() + "passed ";
            }
        }

        for (ParentIngredient inactiveIngredient : inActiveIngredients) {
            boolean isVerified = inactiveIngredient.getQcParameters().stream().allMatch(requestQcParam ->
                    existingRecipe.getIngredients().getInactive().stream()
                            .anyMatch(existingIngredient ->
                                    existingIngredient.getSequence().equals(inactiveIngredient.getSequence()) &&
                                            existingIngredient.getQcParameters().stream().anyMatch(existingQcParam ->
                                                    existingQcParam.getSequence().equals(requestQcParam.getSequence()) &&
                                                            Math.abs(Double.parseDouble(existingQcParam.getActualValue()) - Double.parseDouble(requestQcParam.getActualValue())) <= Double.parseDouble(existingQcParam.getTolerance())
                                            )
                            )
            );

            if (!isVerified) {
                status = status + inactiveIngredient.getIngredientId() + "failed ";
            } else {
                status = status + inactiveIngredient.getIngredientId() + "passed ";
            }
        }

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails(status, "S"))
                .build();
    }

    @Override
    public RecipeMessageModel getAlternateRecipes(RecipeRequests recipeRequest) throws Exception {
        List<AlternateIngredient> alternateIngredients = new ArrayList<>();

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getIngredients() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("ingredients not found" , "E")).build();

        if (existingRecipe.getIngredients().getActive() != null) {
            for (ParentIngredient ingredient : existingRecipe.getIngredients().getActive()) {
                if (ingredient.getAlternateIngredients() != null && !ingredient.getAlternateIngredients().isEmpty()) {
                    alternateIngredients.addAll(ingredient.getAlternateIngredients());
                }
            }
        }

        if (existingRecipe.getIngredients().getInactive() != null) {
            for (ParentIngredient ingredient : existingRecipe.getIngredients().getInactive()) {
                if (ingredient.getAlternateIngredients() != null && !ingredient.getAlternateIngredients().isEmpty()) {
                    alternateIngredients.addAll(ingredient.getAlternateIngredients());
                }
            }
        }
        existingRecipe = null;

        return RecipeMessageModel.builder().alternateIngredients(alternateIngredients).build();
    }


    @Override
    public RecipeMessageModel calculateYield(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        YieldTracking yieldTracking = existingRecipe.getYieldTracking();
        if (yieldTracking == null) {
            return RecipeMessageModel.builder().message_details(new MessageDetails("yieldTracking not found" , "E")).build();
        }

        double expectedYield = Double.parseDouble(yieldTracking.getExpectedYield());
        double allowedVariance = Double.parseDouble(yieldTracking.getAllowedVariance());

        double actualYield = Double.parseDouble(recipeRequest.getActualYield());

        double deviation = actualYield - expectedYield;

        existingRecipe = null;
        if (Math.abs(deviation) > allowedVariance) {

            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Yield deviation beyond allowed variance with " + expectedYield + ", " + actualYield + ", " + allowedVariance , "E"))
                    .waste(recipeRequest.getYieldTracking().getWaste())
                    .byProducts(recipeRequest.getYieldTracking().getByProducts())
                    .build();
        }

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails("Yield within allowed limits with " + expectedYield + ", " + actualYield + ", " + allowedVariance , "E"))
                .waste(recipeRequest.getYieldTracking().getWaste())
                .byProducts(recipeRequest.getYieldTracking().getByProducts())
                .build();

    }

    @Override
    public RecipeMessageModel getPhaseIngredients(RecipeRequests recipeRequest) throws Exception {
        List<PhaseIngredient> ingredients = new ArrayList<>();

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not found" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return RecipeMessageModel.builder().message_details(new MessageDetails("phase Id not found" , "E")).build();
        }

        existingRecipe = null;

        return RecipeMessageModel.builder().phasesIngredients(phase.getIngredients()).build();
    }


    @Override
    public RecipeMessageModel getOperationInstructions(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not found" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return RecipeMessageModel.builder().message_details(new MessageDetails("phase Id not found" , "E")).build();
        }

        Operation op = phase.getOperations().stream()
                .filter(s -> s.getSequence().equals(recipeRequest.getOpSequence()))
                .findFirst()
                .orElse(null);

        if (op == null) {
            return RecipeMessageModel.builder().message_details(new MessageDetails("OperationId not found" , "E")).build();
        }
        existingRecipe = null;
        return RecipeMessageModel.builder().opInstruction(op.getInstruction()).build();

    }

//    @Override
//    public RecipeMessageModel getNextOperation(RecipeRequests recipeRequest) throws Exception {
//
//        Recipes existingRecipe = recipeServiceRepository.findBySiteAndRecipeNameAndVersionAndActive(recipeRequest.getSite(), recipeRequest.getRecipeId(), recipeRequest.getVersion(), 1);
//
//        if (existingRecipe == null)
//            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();
//
//        if (existingRecipe.getPhases() == null)
//            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not found" , "E")).build();
//
//        Phase phase = existingRecipe.getPhases().stream()
//                .filter(p -> p.getPhaseId().equals(recipeRequest.getPhaseId()))
//                .findFirst()
//                .orElse(null);
//
//        if (phase == null) {
//            return RecipeMessageModel.builder().message_details(new MessageDetails("phase Id not found" , "E")).build();
//        }
//
//        Operation currentOp = phase.getOperations().stream()
//                .filter(s -> s.getOperationId().equals(recipeRequest.getOperationId()))
//                .findFirst()
//                .orElse(null);
//
//        if (currentOp == null) {
//            return RecipeMessageModel.builder().message_details(new MessageDetails("currentOperation not found" , "E")).build();
//        }
//
//        if(currentOp.getNextOperations().equals("00")) {
//            if (phase.getNextPhase().equals("00")) {
//                return RecipeMessageModel.builder().message_details(new MessageDetails("This is the last Phases last operation", "E")).build();
//            } else {
//                // get next phase
//                Phase nextPhase = existingRecipe.getPhases().stream()
//                        .filter(p -> p.getSequence().equals(phase.getNextPhase()))
//                        .findFirst()
//                        .orElse(null);
//
//                if (nextPhase == null) {
//                    return RecipeMessageModel.builder()
//                            .message_details(new MessageDetails("Next phase not found", "E"))
//                            .build();
//                }
//
//                if(nextPhase.getOperations() == null || nextPhase.getOperations().isEmpty())
//                    return RecipeMessageModel.builder()
//                            .message_details(new MessageDetails("next operation not found", "E"))
//                            .build();
//
//                // Get the first operation in the next phase (entry operation)
//                Operation nextOp = nextPhase.getOperations().stream()
//                        .filter(Operation::isEntryOperation) // Assumes `isEntryOperation()` is a boolean method in Operation
//                        .findFirst()
//                        .orElse(null);
//
//                if (nextOp == null) {
//                    return RecipeMessageModel.builder()
//                            .message_details(new MessageDetails("Entry operation not found in the next phase", "E"))
//                            .build();
//                }
//
//                // Return the next operation details
//                Map<String, Object> response = new HashMap<>();
//                response.put("operationId", nextOp.getOperationId());
//                response.put("operationName", nextOp.getOperationName());
//                response.put("sequence", nextOp.getSequence());
//                response.put("instruction", nextOp.getInstruction());
//                response.put("expectedCycleTime", nextOp.getExpectedCycleTime());
//
//                return RecipeMessageModel.builder()
//                        .resultBody(response)
//                        .build();
//            }
//
//        } else {
//            Operation nextOp = phase.getOperations().stream()
//                    .filter(p -> p.getSequence().equals(currentOp.getNextOperations()))
//                    .findFirst()
//                    .orElse(null);
//
//            if (nextOp == null) {
//                return RecipeMessageModel.builder()
//                        .message_details(new MessageDetails("Next operation not found", "E"))
//                        .build();
//            }
//            // Return the next operation details
//            Map<String, Object> response = new HashMap<>();
//            response.put("operationId", nextOp.getOperationId());
//            response.put("operationName", nextOp.getOperationName());
//            response.put("sequence", nextOp.getSequence());
//            response.put("instruction", nextOp.getInstruction());
//            response.put("expectedCycleTime", nextOp.getExpectedCycleTime());
//
//            return RecipeMessageModel.builder()
//                    .resultBody(response)
//                    .build();
//        }
////        Optional<Operation> nextOp = phase.getOperations().stream()
////                .filter(s -> s.getSequence() > currentOp.getSequence())
////                .min(Comparator.comparing(Operation::getSequence));
////
////        if (nextOp.isPresent()) {
////            Operation op = nextOp.get();
////            Map<String, Object> response = new HashMap<>();
////            response.put("OperationId", op.getOperationName());
////            response.put("OperationName", op.getOperationName());
////            response.put("sequence", op.getSequence());
////            response.put("instruction", op.getInstruction());
////            response.put("expectedCycleTime", op.getExpectedCycleTime());
////
////            return RecipeMessageModel.builder().resultBody(response).build();
////
////        } else {
////            return RecipeMessageModel.builder().message_details(new MessageDetails("No further ops in this phase" , "E")).build();
////        }
//    }

    @Override
    public RecipeMessageModel getNextOperation(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getValidatedRecipe(recipeRequest);
        if (existingRecipe == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Recipe ID not found", "E"))
                    .build();
        }

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not found" , "E")).build();

        Phase phase = getValidatedPhase(existingRecipe, recipeRequest);
        if (phase == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Phase ID not found", "E"))
                    .build();
        }

        Operation currentOp = getValidatedOperation(phase, recipeRequest);
        if (currentOp == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Current operation not found", "E"))
                    .build();
        }

        // Step 4: Determine the next operation or phase
        if (currentOp.getNextOperations().equals("00") || currentOp.isLastOperationAtPhase()) {
            return handleLastOperationInPhase(existingRecipe, phase);
        } else {
            return getNextOperationInPhase(phase, currentOp);
        }
    }

    private Recipes getValidatedRecipe(RecipeRequests recipeRequest) {
        return recipeServiceRepository.findBySiteAndRecipeIdAndVersionAndActive(
                recipeRequest.getSite(),
                recipeRequest.getRecipeId(),
                recipeRequest.getVersion(),
                1
        );
    }

    private Phase getValidatedPhase(Recipes existingRecipe, RecipeRequests recipeRequest) {
        return existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .orElse(null);
    }

    private Operation getValidatedOperation(Phase phase, RecipeRequests recipeRequest) {
        return phase.getOperations().stream()
                .filter(op -> op.getSequence().equals(recipeRequest.getOpSequence()))
                .findFirst()
                .orElse(null);
    }

    private RecipeMessageModel handleLastOperationInPhase(Recipes existingRecipe, Phase phase) {
        if (phase.getNextPhase().equals("00") || phase.isExitPhase()) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("This is the last phase's last operation", "E"))
                    .build();
        } else {
            Phase nextPhase = existingRecipe.getPhases().stream()
                    .filter(p -> p.getSequence().equals(phase.getNextPhase()))
                    .findFirst()
                    .orElse(null);

            if (nextPhase == null || nextPhase.getOperations() == null || nextPhase.getOperations().isEmpty()) {
                return RecipeMessageModel.builder()
                        .message_details(new MessageDetails("Next phase or its operations not found", "E"))
                        .build();
            }

            Operation nextOp = nextPhase.getOperations().stream()
                    .filter(Operation::isEntryOperation)
                    .findFirst()
                    .orElse(null);

            if (nextOp == null) {
                return RecipeMessageModel.builder()
                        .message_details(new MessageDetails("Entry operation not found in the next phase", "E"))
                        .build();
            }

            return buildOperationResponse(nextPhase, nextOp);
        }
    }

    private RecipeMessageModel getNextOperationInPhase(Phase phase, Operation currentOp) {
        Operation nextOp = phase.getOperations().stream()
                .filter(op -> op.getSequence().equals(currentOp.getNextOperations()))
                .findFirst()
                .orElse(null);

        if (nextOp == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Next operation not found", "E"))
                    .build();
        }

        return buildOperationResponse(phase, nextOp);
    }

    private RecipeMessageModel buildOperationResponse(Phase phase, Operation operation) {
        Map<String, Object> response = new HashMap<>();
        response.put("operationId", operation.getOperationId());
        response.put("sequence", operation.getSequence());
        response.put("instruction", operation.getInstruction());
        response.put("expectedCycleTime", operation.getExpectedCycleTime());
        response.put("phaseId", phase.getPhaseId());

        return RecipeMessageModel.builder()
                .resultBody(response)
                .build();
    }


//    @Override
//    public RecipeMessageModel getNextphase(RecipeRequests recipeRequest) throws Exception {
//
//        Recipes existingRecipe = recipeServiceRepository.findBySiteAndRecipeNameAndVersionAndActive(recipeRequest.getSite(), recipeRequest.getRecipeId(), recipeRequest.getVersion(),1);
//
//        if (existingRecipe == null) {
//            return RecipeMessageModel.builder().message_details(new MessageDetails("Recipe ID not found", "E")).build();
//        }
//
//        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty()) {
//            return RecipeMessageModel.builder().message_details(new MessageDetails("Phases not found", "E")).build();
//        }
//
//        Phase currentPhase = existingRecipe.getPhases().stream()
//                .filter(p -> p.getPhaseId().equals(recipeRequest.getPhaseId()))
//                .findFirst()
//                .orElse(null);
//
//        if (currentPhase == null) {
//            return RecipeMessageModel.builder().message_details(new MessageDetails("Phase ID not found", "E")).build();
//        }
//
//        // Get the next phase sequence
//        String nextPhaseSeq = currentPhase.getNextPhase(); // Assumes sequential implementation
//        if (nextPhaseSeq == null || nextPhaseSeq.equals("00")) {
//            return RecipeMessageModel.builder()
//                    .message_details(new MessageDetails("No further phases in the recipe", "E"))
//                    .build();
//        }
//
//        // Find the next phase by its sequence
//        Phase nextPhase = existingRecipe.getPhases().stream()
//                .filter(p -> p.getSequence().equals(nextPhaseSeq))
//                .findFirst()
//                .orElse(null);
//
//        if (nextPhase == null) {
//            return RecipeMessageModel.builder()
//                    .message_details(new MessageDetails("Next phase not found", "E"))
//                    .build();
//        }
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("phaseId", nextPhase.getPhaseId());
//        response.put("phaseName", nextPhase.getPhaseName());
//        response.put("sequence", nextPhase.getSequence());
//        response.put("expectedCycleTime", nextPhase.getExpectedCycleTime());
//
//        return RecipeMessageModel.builder()
//                .resultBody(response)
//                .build();
//
//
////        Optional<Phase> nextPhase = existingRecipe.getPhases().stream()
////                .filter(p -> p.getSequence() > phase.getSequence())
////                .min(Comparator.comparing(Phase::getSequence));
////
////        if (nextPhase.isPresent()) {
////            Phase phases = nextPhase.get();
////            Map<String, Object> response = new HashMap<>();
////            response.put("phaseId", phases.getPhaseName());
////            response.put("phaseName", phases.getPhaseName());
////            response.put("sequence", phases.getSequence());
////            response.put("expectedCycleTime", phases.getExpectedCycleTime());
////
////            return RecipeMessageModel.builder().resultBody(response).build();
////        } else {
////            return RecipeMessageModel.builder().message_details(new MessageDetails("No further phases in the recipe" , "E")).build();
////        }
//    }

    @Override
    public RecipeMessageModel getNextphase(RecipeRequests recipeRequest) throws Exception {
        Recipes existingRecipe = getValidatedRecipe(recipeRequest);
        if (existingRecipe == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Recipe ID not found", "E"))
                    .build();
        }

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty()) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Phases not found", "E"))
                    .build();
        }

        Phase currentPhase = getValidatedPhase(existingRecipe, recipeRequest);
        if (currentPhase == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Phase ID not found", "E"))
                    .build();
        }

        Phase nextPhase = getNextPhaseFromSequence(existingRecipe, currentPhase);
        if (nextPhase == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Next phase not found", "E"))
                    .build();
        }

        return buildPhaseResponse(nextPhase);
    }

    private Phase getNextPhaseFromSequence(Recipes existingRecipe, Phase currentPhase) {
        String nextPhaseSeq = currentPhase.getNextPhase();
        if (nextPhaseSeq == null || nextPhaseSeq.equals("00")) {
            return null;
        }

        return existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(nextPhaseSeq))
                .findFirst()
                .orElse(null);
    }

    private RecipeMessageModel buildPhaseResponse(Phase nextPhase) {
        Map<String, Object> response = new HashMap<>();
        response.put("phaseId", nextPhase.getPhaseId());
        response.put("sequence", nextPhase.getSequence());
        response.put("expectedCycleTime", nextPhase.getExpectedCycleTime());

        return RecipeMessageModel.builder()
                .resultBody(response)
                .build();
    }


    @Override
    public RecipeMessageModel getConditionalOperation(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not found" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return RecipeMessageModel.builder().message_details(new MessageDetails("phase Id not found" , "E")).build();
        }

        if(phase.getConditional() != null && Boolean.TRUE.equals(phase.getConditional())){
            String triggeredPhaseId = phase.getTriggeredPhase();
            Map<String, String> nextPhaseOp = nextTriggeredPhaseId(existingRecipe, triggeredPhaseId);

            if(nextPhaseOp == null)
                return RecipeMessageModel.builder().message_details(new MessageDetails("No triggered phase Operation available in this recipe" , "E")).build();

            return RecipeMessageModel.builder().phaseOps(nextPhaseOp).build();
        } else {
            return RecipeMessageModel.builder().message_details(new MessageDetails("Condition failed, no Operation found" , "E")).build();
        }
    }

    private Map<String, String> nextTriggeredPhaseId(Recipes existingRecipe, String triggeredPhaseId){

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(triggeredPhaseId))
                .findFirst()
                .orElse(null);

        if (phase != null) {
            return phase.getOperations().stream()
                    .findFirst()
                    .map(op -> {
                        Map<String, String> opDetails = new HashMap<>();
                        opDetails.put("operationId", op.getOperationId());
                        opDetails.put("instruction", op.getInstruction());
                        return opDetails;
                    })
                    .orElse(null);
        }
        return null;
    }

    @Override
    public RecipeMessageModel getIngredientsWithVerification(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not found" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return RecipeMessageModel.builder().message_details(new MessageDetails("phase Id not found" , "E")).build();
        }

        if (phase.getIngredients() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases ingredients not found" , "E")).build();

        Recipes finalExistingRecipe = existingRecipe;

        Ingredients ingredients = phase.getIngredients();
        List<PhaseIngredient> phaseIngredients = Stream.concat(
                        ingredients.getActive().stream(),
                        ingredients.getInactive().stream()
                )
                .filter(Objects::nonNull)
                .map(parentIngredient -> {
                    PhaseIngredient phaseIngredient = new PhaseIngredient();
                    phaseIngredient.setIngredientId(parentIngredient.getIngredientId());
                    phaseIngredient.setIngredientVersion(parentIngredient.getIngredientVersion());
                    phaseIngredient.setIngreDescription(parentIngredient.getIngreDescription());
                    phaseIngredient.setQuantity(parentIngredient.getQuantity());

                    List<QualityControlParameter> qcParameters = findQcParametersForIngredient(finalExistingRecipe, parentIngredient.getSequence());
                    phaseIngredient.setQcParameters(qcParameters);

                    return phaseIngredient;
                })
                .collect(Collectors.toList());

        existingRecipe = null;

        return RecipeMessageModel.builder().phasesIngredients(ingredients).build();
    }

    private List<QualityControlParameter> findQcParametersForIngredient(Recipes existingRecipe, String ingredientId) {
        Ingredients ingredients = existingRecipe.getIngredients();
        if (ingredients == null) {
            return Collections.emptyList();
        }

        List<ParentIngredient> activeIngredients = ingredients.getActive() != null ? ingredients.getActive() : Collections.emptyList();
        List<ParentIngredient> inactiveIngredients = ingredients.getInactive() != null ? ingredients.getInactive() : Collections.emptyList();

        return Stream.concat(activeIngredients.stream(), inactiveIngredients.stream())
                .filter(ing -> ing.getSequence().equals(ingredientId))
                .flatMap(ing -> ing.getQcParameters().stream())
                .collect(Collectors.toList());
    }

    @Override
    public RecipeMessageModel getPhaseOperationInstructions(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not found" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phase Id not found" , "E")).build();

        if (phase.getOperations() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phase Operations not found" , "E")).build();

        List<Operation> opInstructions = phase.getOperations().stream()
                .map(op -> {
                    Operation instruction = new Operation();
                    instruction.setOperationId(op.getOperationId());
                    instruction.setOperationVersion(op.getOperationVersion());
                    instruction.setInstruction(op.getInstruction());
                    return instruction;
                })
                .collect(Collectors.toList());

        existingRecipe = null;

        return RecipeMessageModel.builder().ops(opInstructions).build();
    }

    @Override
    public RecipeMessageModel getParallelPhases(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not found" , "E")).build();

        List<Phase> phases = existingRecipe.getPhases().stream()
                .filter(Objects::nonNull)
                .filter(phase -> Boolean.TRUE.equals(phase.getParallel()))
                .collect(Collectors.toList());

        existingRecipe = null;

        return RecipeMessageModel.builder().phases(phases).build();
    }

    @Override
    public RecipeMessageModel getAnyOrderOperations(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phases not found" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("phase Id not found" , "E")).build();

        List<Operation> ops = phase.getOperations().stream()
                .filter(op -> "AnyOrder".equalsIgnoreCase(op.getType()))
                .collect(Collectors.toList());

        existingRecipe = null;

        return RecipeMessageModel.builder().ops(ops).build();
    }

    @Override
    public RecipeMessageModel getUomForIngredient(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getIngredients() == null) {
            return RecipeMessageModel.builder().message_details(new MessageDetails("Ingredient not found" , "E")).build();
        }

        PhaseIngredient ingredient = findUomIngredientById(recipeRequest.getIngreSequence(), existingRecipe.getIngredients());

        existingRecipe = null;

        return RecipeMessageModel.builder().ingredient(ingredient).build();
    }

    public PhaseIngredient findUomIngredientById(String ingredientId, Ingredients ingredients) {
        PhaseIngredient ingredient = new PhaseIngredient();

        List<ParentIngredient> allIngredients = new ArrayList<>();
        allIngredients.addAll(ingredients.getActive());
        allIngredients.addAll(ingredients.getInactive());

        for (ParentIngredient ing : allIngredients) {
            if (ing.getSequence().equals(ingredientId)) {
                ingredient.setUom(ing.getUom());
                ingredient.setIngredientId(ing.getIngredientId());

                return ingredient;
            }
        }
        return null;
    }


    @Override
    public RecipeMessageModel getIngredientVerificationStatus(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getIngredients() == null) {
            return RecipeMessageModel.builder().message_details(new MessageDetails("Ingredient not found" , "E")).build();
        }

        List<QualityControlParameter> qcParameters = findIngredientById(existingRecipe, recipeRequest.getIngreSequence());

        boolean isVerified = qcParameters.stream()
                .allMatch(param -> param.getActualValue().equals(param.getExpectedValue()));

        String status = isVerified ? "Verified" : "Failed";
        existingRecipe = null;

        return RecipeMessageModel.builder().ingredientId(recipeRequest.getIngredientId()).isVerified(status).qcParameters(qcParameters).build();

    }


    private List<QualityControlParameter> findIngredientById(Recipes recipe, String ingredientId) {
        if(recipe.getIngredients().getActive() != null){
            for (ParentIngredient ingredient : recipe.getIngredients().getActive()) {
                if (ingredient.getSequence().equals(ingredientId)) {
                    return ingredient.getQcParameters();
                }
            }
        }

        if(recipe.getIngredients().getInactive() != null) {
            for (ParentIngredient ingredient : recipe.getIngredients().getInactive()) {
                if (ingredient.getSequence().equals(ingredientId)) {
                    return ingredient.getQcParameters();
                }
            }
        }

        return null;
    }

    Boolean validateOperation(String site, String operation, String operationVersion) throws Exception{
        return operationService.isOperationExist(site, operation, operationVersion);
    }

    @Override
    public RecipeMessageModel addPhaseOperation(RecipeRequests recipeRequest) throws Exception {
        if(!StringUtils.hasText(recipeRequest.getOperationId()) || !StringUtils.hasText(recipeRequest.getOpVersion()))
            return RecipeMessageModel.builder().message_details(new MessageDetails("operation or operation version is empty", "E")).build();

        if(!validateOperation(recipeRequest.getSite(), recipeRequest.getOperationId(), recipeRequest.getOpVersion()))
            return RecipeMessageModel.builder().message_details(new MessageDetails("operation is not valid", "E")).build();

        Recipes existingRecipe = getExistingRecipe(recipeRequest);
        if (existingRecipe != null) {

            if(existingRecipe.getPhases() != null) {
                Phase phase = existingRecipe.getPhases().stream()
                        .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                        .findFirst()
                        .orElse(null);

                if (phase != null) {
                    if(phase.getOperations() == null || phase.getOperations().isEmpty()) {
                        // new

                        List<Operation> requestops = recipeRequest.getOperations();
                        if (requestops != null) {
                            List<Operation> newop = new ArrayList<>(requestops);
                            phase.setOperations(newop);
                        } else
                            return RecipeMessageModel.builder()
                                    .message_details(new MessageDetails("phase operations is empty", "E"))
                                    .build();

                    } else {
                        boolean opExists = phase.getOperations().stream()
                                .anyMatch(s -> s.getSequence().equals(recipeRequest.getOpSequence()));

                        if (opExists) {
                            return RecipeMessageModel.builder()
                                    .message_details(new MessageDetails("Operation ID already exists", "E"))
                                    .build();

                        } else {
                            List<Operation> requestOps = recipeRequest.getOperations();
                            if (requestOps != null) {
                                phase.getOperations().addAll(requestOps);
                            }
                        }

                    }
                    existingRecipe.setModifiedBy(recipeRequest.getUser());
                    existingRecipe.setModifiedDate(LocalDateTime.now());
                    recipeServiceRepository.save(existingRecipe);
                } else
                    return RecipeMessageModel.builder()
                            .message_details(new MessageDetails("phase ID not present", "E"))
                            .build();

                return RecipeMessageModel.builder()
                        .message_details(new MessageDetails("Operations added", "S"))
                        .build();
            } else{
                throw new IllegalArgumentException("Phase record is empty");
            }
        } else {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Recipe ID not found", "E"))
                    .build();
        }
    }

    @Override
    public RecipeMessageModel updatePhaseOperation(RecipeRequests recipeRequest) throws Exception {
        if(!StringUtils.hasText(recipeRequest.getOperationId()) || !StringUtils.hasText(recipeRequest.getOpVersion()))
            return RecipeMessageModel.builder().message_details(new MessageDetails("operation or operation version is empty", "E")).build();

        if(!validateOperation(recipeRequest.getSite(), recipeRequest.getOperationId(), recipeRequest.getOpVersion()))
            return RecipeMessageModel.builder().message_details(new MessageDetails("operation is not valid", "E")).build();

        Recipes existingRecipe = getExistingRecipe(recipeRequest);
        if (existingRecipe != null) {

            if(existingRecipe.getPhases() != null) {

                Phase phase = existingRecipe.getPhases().stream()
                        .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                        .findFirst()
                        .orElse(null);

                if (phase != null) {
                    if(phase.getOperations() != null) {
                        Operation opToUpdate = phase.getOperations().stream()
                                .filter(s -> s.getSequence().equals(recipeRequest.getOpSequence()))
                                .findFirst()
                                .orElse(null);

                        if (opToUpdate == null)
                            return RecipeMessageModel.builder()
                                    .message_details(new MessageDetails("operation ID not present", "E"))
                                    .build();

                        updatePhaseOperations(phase.getOperations(), recipeRequest.getOperations());
                        existingRecipe.setModifiedBy(recipeRequest.getUser());
                        existingRecipe.setModifiedDate(LocalDateTime.now());
                        recipeServiceRepository.save(existingRecipe);

                        return RecipeMessageModel.builder()
                                .message_details(new MessageDetails("operation updated successfully", "S"))
                                .build();
                    } else
                        return RecipeMessageModel.builder()
                                .message_details(new MessageDetails("phase operation is empty", "S"))
                                .build();
                } else
                    return RecipeMessageModel.builder()
                            .message_details(new MessageDetails("phase ID not present", "E"))
                            .build();
            } else
                return RecipeMessageModel.builder()
                        .message_details(new MessageDetails("phase not present", "E"))
                        .build();

        } else {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Recipe ID not found", "E"))
                    .build();
        }
    }



    @Override
    public RecipeMessageModel deletePhaseOperation(RecipeRequests recipeRequest) throws Exception {
        if(!StringUtils.hasText(recipeRequest.getOperationId()) || !StringUtils.hasText(recipeRequest.getOpVersion()))
            return RecipeMessageModel.builder().message_details(new MessageDetails("operation or operation version is empty", "E")).build();

        if(!validateOperation(recipeRequest.getSite(), recipeRequest.getOperationId(), recipeRequest.getOpVersion()))
            return RecipeMessageModel.builder().message_details(new MessageDetails("operation is not valid", "E")).build();

        Recipes existingRecipe = getExistingRecipe(recipeRequest);
        if (existingRecipe != null) {

            if(existingRecipe.getPhases() == null){
                return RecipeMessageModel.builder()
                        .message_details(new MessageDetails("phases is absent", "S"))
                        .build();
            } else {
                Phase phase = existingRecipe.getPhases().stream()
                        .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                        .findFirst()
                        .orElseThrow(() -> new RecipeException(130));

                if(phase == null)
                    return RecipeMessageModel.builder()
                            .message_details(new MessageDetails("phaseId is absent", "S"))
                            .build();

                if(phase.getOperations() == null)
                    return RecipeMessageModel.builder()
                            .message_details(new MessageDetails("phaseOperation is absent", "S"))
                            .build();
                else {

                    Operation opToDelete = phase.getOperations().stream()
                            .filter(s -> s.getSequence().equals(recipeRequest.getOpSequence()))
                            .findFirst()
                            .orElseThrow(() -> new RecipeException(131));

                    phase.getOperations().remove(opToDelete);
                    existingRecipe.setModifiedBy(recipeRequest.getUser());
                    existingRecipe.setModifiedDate(LocalDateTime.now());
                    recipeServiceRepository.save(existingRecipe);

                    return RecipeMessageModel.builder()
                            .message_details(new MessageDetails("Operation deleted successfully", "S"))
                            .build();
                }
            }
        } else {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Recipe ID not found", "E"))
                    .build();
        }
    }

    @Override
    public RecipeMessageModel addPhaseIngredient(RecipeRequests recipeRequest) throws Exception {
        if(StringUtils.hasText(recipeRequest.getParentIngredient().getOperationId())) {
            if (!StringUtils.hasText(recipeRequest.getOperationId()) || !StringUtils.hasText(recipeRequest.getOpVersion()))
                return RecipeMessageModel.builder().message_details(new MessageDetails("operation or operation version is empty", "E")).build();

            if (!validateOperation(recipeRequest.getSite(), recipeRequest.getParentIngredient().getOperationId(), recipeRequest.getParentIngredient().getOperationVersion()))
                return RecipeMessageModel.builder().message_details(new MessageDetails("operation is not valid", "E")).build();
        }

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Recipe ID not found", "E"))
                    .build();
        }

        if (existingRecipe.getPhases() == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Phases are empty; please create a phase first", "E"))
                    .build();
        }

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Phase ID not present", "E"))
                    .build();
        }


        Ingredients phaseIngredients = phase.getIngredients();
        ParentIngredient requestIngredient = recipeRequest.getParentIngredient();

        if (requestIngredient == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Phase ingredients are empty", "E"))
                    .build();
        }

        if (phaseIngredients == null) {
            phaseIngredients = new Ingredients();
            phase.setIngredients(phaseIngredients);
        }

        String type = recipeRequest.getIngredientType();

        if ("active".equalsIgnoreCase(type)) {
            if (phaseIngredients.getActive() == null) {
                phaseIngredients.setActive(new ArrayList<>());
            }
            phaseIngredients.getActive().add(requestIngredient);
        } else if ("inactive".equalsIgnoreCase(type)) {
            if (phaseIngredients.getInactive() == null) {
                phaseIngredients.setInactive(new ArrayList<>());
            }
            phaseIngredients.getInactive().add(requestIngredient);
        } else {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Invalid ingredient type. Must be 'active' or 'inactive'", "E"))
                    .build();
        }
        existingRecipe.setModifiedBy(recipeRequest.getUser());
        existingRecipe.setModifiedDate(LocalDateTime.now());
        recipeServiceRepository.save(existingRecipe);

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails("Ingredients added", "S"))
                .build();
    }


    @Override
    public RecipeMessageModel updatePhaseIngredient(RecipeRequests recipeRequest) throws Exception {
        if(StringUtils.hasText(recipeRequest.getParentIngredient().getOperationId())) {
            if (!StringUtils.hasText(recipeRequest.getOperationId()) || !StringUtils.hasText(recipeRequest.getOpVersion()))
                return RecipeMessageModel.builder().message_details(new MessageDetails("operation or operation version is empty", "E")).build();

            if (!validateOperation(recipeRequest.getSite(), recipeRequest.getParentIngredient().getOperationId(), recipeRequest.getParentIngredient().getOperationVersion()))
                return RecipeMessageModel.builder().message_details(new MessageDetails("operation is not valid", "E")).build();
        }

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder() .message_details(new MessageDetails("Recipe ID not found", "E")).build();

        if(existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("phases not present", "E"))
                    .build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .orElseThrow(() -> new RecipeException(130));

        if(phase == null)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("phaseId not present", "E"))
                    .build();

        if (phase.getIngredients() == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Ingredients not present in the phase", "E"))
                    .build();
        }

        ParentIngredient existingIngredient = null;

        if ("active".equalsIgnoreCase(recipeRequest.getIngredientType())) {
            existingIngredient = Optional.of(phase.getIngredients())
                    .map(Ingredients::getActive)
                    .orElse(new ArrayList<>())
                    .stream()
                    .filter(ingredient -> ingredient != null && ingredient.getSequence() != null &&
                            ingredient.getSequence().equals(recipeRequest.getIngreSequence()))
                    .findFirst()
                    .orElse(null);

        } else if ("inactive".equalsIgnoreCase(recipeRequest.getIngredientType())) {
            existingIngredient = Optional.of(phase.getIngredients())
                    .map(Ingredients::getInactive)
                    .orElse(new ArrayList<>())
                    .stream()
                    .filter(ingredient -> ingredient != null && ingredient.getSequence() != null &&
                            ingredient.getSequence().equals(recipeRequest.getIngreSequence()))
                    .findFirst()
                    .orElse(null);
        }

        if (existingIngredient == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Ingredient ID not present", "E"))
                    .build();
        }

        if (recipeRequest.getParentIngredient() == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Phase ingredients not provided in the request", "E"))
                    .build();
        }

        updateParentIngredient(existingIngredient, recipeRequest.getParentIngredient());
        existingRecipe.setModifiedBy(recipeRequest.getUser());
        existingRecipe.setModifiedDate(LocalDateTime.now());
        recipeServiceRepository.save(existingRecipe);

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails("Ingredient updated successfully", "S"))
                .build();

    }

//    private void updateIngredientDetails(PhaseIngredient existingIngredient, PhaseIngredient userIngredient) {
//
//        existingIngredient.setIngreDescription(userIngredient.getIngreDescription());
//        existingIngredient.setQuantity(userIngredient.getQuantity());
//        existingIngredient.setSequence(userIngredient.getSequence());
//        existingIngredient.setAssociatedOp(userIngredient.getAssociatedOp());
//        existingIngredient.setUom(userIngredient.getUom());
//    }

    @Override
    public RecipeMessageModel deletePhaseIngredient(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if (existingRecipe == null)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Recipe ID not found", "E"))
                    .build();

        if(existingRecipe.getPhases() == null)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("phases not found", "E"))
                    .build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(recipeRequest.getPhaseSequence()))
                .findFirst()
                .orElseThrow(() -> new RecipeException(130));

        if(phase == null)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("phaseId not found", "E"))
                    .build();

        if(phase.getIngredients() == null)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("ingredients not found", "E"))
                    .build();

        Ingredients ingredients = phase.getIngredients();

        if (ingredients.getActive() != null) {
            ingredients.getActive().removeIf(activeIngredient ->
                    activeIngredient != null &&
                            activeIngredient.getSequence() != null &&
                            activeIngredient.getSequence().equals(recipeRequest.getIngreSequence()));
        }

        if (ingredients.getInactive() != null) {
            ingredients.getInactive().removeIf(inactiveIngredient ->
                    inactiveIngredient != null &&
                            inactiveIngredient.getSequence() != null &&
                            inactiveIngredient.getSequence().equals(recipeRequest.getIngreSequence()));
        }
        existingRecipe.setModifiedBy(recipeRequest.getUser());
        existingRecipe.setModifiedDate(LocalDateTime.now());
        recipeServiceRepository.save(existingRecipe);

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails("Phase ingredient deleted successfully", "S"))
                .build();
    }

    @Override
    public RecipeMessageModel getPhaseOperationDataCollection(RecipeRequests recipeRequest) throws Exception {

        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if(existingRecipe == null)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Recipe ID not found", "E"))
                    .build();

        List<DataCollection> dataCollectionRecords = recipeService.getDataCollectionRecords(existingRecipe, recipeRequest);

        if (dataCollectionRecords != null) {
            return RecipeMessageModel.builder()
                    .dataCollection(dataCollectionRecords)
                    .build();

        } else {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Phase datacollection not found", "S"))
                    .build();
        }
    }

    public List<DataCollection> getDataCollectionRecords(Recipes existingRecipe, RecipeRequests recipeRequest) {

        if(existingRecipe.getPhases() == null)
            throw new RecipeException(133);

        for (Phase phase : existingRecipe.getPhases()) {
            if (phase.getSequence().equals(recipeRequest.getPhaseSequence())) {
                for (Operation op : phase.getOperations()) {
                    if (op.getSequence().equals(recipeRequest.getOpSequence())) {
                        return op.getDataCollection();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public RecipeMessageModel getPhaseCcpDataCollection(RecipeRequests recipeRequest) throws Exception {
        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if(existingRecipe == null)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Recipe ID not found", "E"))
                    .build();

        List<List<DataCollection>> ccpRecords = recipeService.findCcpDataCollections(existingRecipe, recipeRequest);

        if (!ccpRecords.isEmpty()) {
            return RecipeMessageModel.builder()
                    .dataCollections(ccpRecords)
                    .build();

        } else {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Phase dataCollection not found", "E"))
                    .build();
        }
    }

    public List<List<DataCollection>> findCcpDataCollections(Recipes existingRecipe, RecipeRequests recipeRequest) {

        List<List<DataCollection>> ccpDataCollections = new ArrayList<>();

        for (Phase phase : existingRecipe.getPhases()) {
            if (phase.getSequence().equals(recipeRequest.getPhaseSequence())) {
                for (Operation op : phase.getOperations()) {
                    if (op.getSequence().equals(recipeRequest.getOpSequence())) {
                        if(op.getCcp()){
                            ccpDataCollections.add(op.getDataCollection());
                        }
                    }
                }
            }
        }

        return ccpDataCollections;
    }

    @Override
    public RecipeMessageModel getPhaseOperationsByType(RecipeRequests recipeRequest) throws Exception {
        Recipes existingRecipe = getExistingRecipe(recipeRequest);

        if(existingRecipe == null)
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Recipe ID not found", "E"))
                    .build();

        List<Operation> ops = new ArrayList<>();

        ops = recipeService.findOpsByType(existingRecipe, recipeRequest);

        if (!ops.isEmpty()) {
            return RecipeMessageModel.builder()
                    .ops(ops)
                    .build();

        } else {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Phase operation of " + recipeRequest.getOperationType() + " not found", "E"))
                    .build();
        }
    }


    @Override
    public RecipeMessageModel getPhasesBySite(RecipeRequests recipeRequest) throws Exception {

        Query query = new Query();
        query.addCriteria(Criteria.where("site").is(recipeRequest.getSite()));
        query.fields().include("phases");
        List<Recipes> recipesList = mongoTemplate.find(query, Recipes.class);

        // Extract the phaseId values from the recipes
        Set<String> phaseIds = new HashSet<>();
        for (Recipes recipe : recipesList) {
            if (recipe.getPhases() != null) {
                for (Phase phase : recipe.getPhases()) {
                    phaseIds.add(phase.getPhaseId());
                }
            }
        }

        return RecipeMessageModel.builder()
                .phaseList(phaseIds)
                .build();
    }

    @Override
    public RecipeMessageModel getOperationByFilter(RecipeRequests recipeRequest) throws Exception {

        Query query = new Query();
        if (recipeRequest.getPhaseId() != null && !recipeRequest.getPhaseId().isEmpty()) {
            query.addCriteria(Criteria.where("site").is(recipeRequest.getSite())
                    .and("phases.phaseId").is(recipeRequest.getPhaseId()));
        } else {
            query.addCriteria(Criteria.where("site").is(recipeRequest.getSite()));
        }

        query.addCriteria(Criteria.where("active").is(1));
        List<Recipes> recipesList = mongoTemplate.find(query, Recipes.class);

        Set<String> operationIds = new HashSet<>();
        for (Recipes recipe : recipesList) {
            if (recipe.getPhases() != null) {
                for (Phase phase : recipe.getPhases()) {
                    if (recipeRequest.getPhaseId() == null || recipeRequest.getPhaseId().isEmpty()
                            || phase.getPhaseId().equals(recipeRequest.getPhaseId())) {
                        if (phase.getOperations() != null) {
                            for (Operation operation : phase.getOperations()) {
                                if (operation != null && operation.getOperationId() != null) {
                                    operationIds.add(operation.getOperationId());
                                }
                            }
                        }
                    }
                }
            }
        }

        return RecipeMessageModel.builder()
                .operationList(operationIds)
                .build();
    }

    public List<Operation> findOpsByType(Recipes existingRecipe, RecipeRequests recipeRequest) {
        List<Operation> ops = new ArrayList<>();

        if (existingRecipe.getPhases() == null) {
            throw new RecipeException(133);
        }

        String phaseSequence = recipeRequest.getPhaseSequence();
        String opType = recipeRequest.getOperationType() ;

        for (Phase phase : existingRecipe.getPhases()) {
            if (phase.getSequence().equals(phaseSequence)) {

                if (phase.getOperations() == null)
                    continue;

                for (Operation op : phase.getOperations()) {
                    if (op.getType().equalsIgnoreCase(opType)) {
                        ops.add(op);
                    }
                }
                break;
            }
        }

        return ops;
    }


    @Override
    public RecipeMessageModel getTop50RecipeList(String site) throws Exception {
        List<RecipeRequest> recipeResponses = recipeServiceRepository.findTop50BySiteAndActiveOrderByCreatedDateDesc(site, 1);
        if(recipeResponses == null)
            return RecipeMessageModel.builder().message_details(new MessageDetails("list is empty", "E")).build();

        return RecipeMessageModel.builder().responseList(recipeResponses).build();
    }

    @Override
    public Boolean checkReleasible(RecipeRequests recipeRequest) throws Exception {
        RecipeMessageModel recipe = getRecipe(recipeRequest);
        return recipe.getResponse().getStatus().equalsIgnoreCase("Releasable");
    }

     @Override
    public void setRecipeStatus(RecipeRequests recipeRequest) throws Exception {
        RecipeMessageModel recipe = getRecipe(recipeRequest);
        recipe.getResponse().setStatus("IN_USE");
    }

    @Override
    public RecipeMessageModel getRecipeIngreList(RecipeRequests recipeRequest) throws Exception {
        RecipeMessageModel recipe = getRecipe(recipeRequest);
        Recipes recipes = recipe.getResponse();
        if (recipes == null) {
            return RecipeMessageModel.builder()
                    .message_details(new MessageDetails("Recipe not found", "E"))
                    .build();
        }

        List<Map<String, String>> ingredientDetailsList = new ArrayList<>();
        if (recipes.getIngredients() != null) {
            // Process active ingredients
            if (recipes.getIngredients().getActive() != null) {
                for (ParentIngredient activeIngredient : recipes.getIngredients().getActive()) {
                    Map<String, String> ingredientDetails = new HashMap<>();
                    ingredientDetails.put("sequence", activeIngredient.getSequence());
                    ingredientDetails.put("ingredientId", activeIngredient.getIngredientId());
                    ingredientDetails.put("ingredientType", "active");
                    ingredientDetailsList.add(ingredientDetails);
                }
            }

            // Process inactive ingredients
            if (recipes.getIngredients().getInactive() != null) {
                for (ParentIngredient inactiveIngredient : recipes.getIngredients().getInactive()) {
                    Map<String, String> ingredientDetails = new HashMap<>();
                    ingredientDetails.put("sequence", inactiveIngredient.getSequence());
                    ingredientDetails.put("ingredientId", inactiveIngredient.getIngredientId());
                    ingredientDetails.put("ingredientType", "inactive");
                    ingredientDetailsList.add(ingredientDetails);
                }
            }
        }

        return RecipeMessageModel.builder()
                .message_details(new MessageDetails("Ingredients fetched successfully", "S"))
                .ingredientDetails(ingredientDetailsList)
                .build();
    }

    @Override
    public AuditLogRequest createAuditLog(RecipeRequests recipeRequest) {
        return AuditLogRequest.builder()
                .site(recipeRequest.getSite())
                .action_code("RECEIPE-CREATED")
                .action_detail("RECEIPE Created " + recipeRequest.getRecipeId() + "/" + recipeRequest.getVersion())
                .action_detail_handle("ActionDetailBO:" + recipeRequest.getSite() + "," + "RECEIPE-CREATED" + recipeRequest.getRecipeId() + ":" + "com.rits.recipemaintenanceservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(recipeRequest.getRecipeId())
                .operation_revision("*")
                .txnId("RECEIPE-CREATED" + LocalDateTime.now() + recipeRequest.getRecipeId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .router(recipeRequest.getRecipeId())
                .router_revision(recipeRequest.getRecipeId())
                .category("Create")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest updateAuditLog(RecipeRequests recipeRequest) {
        return AuditLogRequest.builder()
                .site(recipeRequest.getSite())
                .action_code("RECEIPE-UPDATED")
                .action_detail("RECEIPE Updated " + recipeRequest.getRecipeId() + "/" + recipeRequest.getVersion())
                .action_detail_handle("ActionDetailBO:" + recipeRequest.getSite() + "," + "RECEIPE-UPDATED" + recipeRequest.getRecipeId() + ":" + "com.rits.recipemaintenanceservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(recipeRequest.getUser())
                .router(recipeRequest.getRecipeId())
                .router_revision(recipeRequest.getRecipeId())
                .operation_revision("*")
                .txnId("RECEIPE-UPDATED" + LocalDateTime.now() + recipeRequest.getRecipeId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Update")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest deleteAuditLog(RecipeRequests recipeRequest) {
        return AuditLogRequest.builder()
                .site(recipeRequest.getSite())
                .action_code("RECEIPE-DELETED")
                .action_detail("RECEIPE Deleted " + recipeRequest.getRecipeId() + "/" + recipeRequest.getVersion())
                .action_detail_handle("ActionDetailBO:" + recipeRequest.getSite() + "," + "RECEIPE-DELETED" + recipeRequest.getRecipeId() + ":" + "com.rits.recipemaintenanceservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(recipeRequest.getRecipeId())
                .router(recipeRequest.getRecipeId())
                .router_revision(recipeRequest.getRecipeId())
                .operation_revision("*")
                .txnId("RECEIPE-DELETED" + LocalDateTime.now() + recipeRequest.getRecipeId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Delete")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }

}
