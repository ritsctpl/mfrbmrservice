package com.rits.batchnorecipeheaderservice.service;

import com.rits.Utility.BOConverter;
import com.rits.batchnorecipeheaderservice.dto.*;
import com.rits.batchnorecipeheaderservice.exception.BatchNoRecipeHeaderException;
import com.rits.batchnorecipeheaderservice.model.*;
import com.rits.batchnorecipeheaderservice.dto.Phase;
import com.rits.batchnorecipeheaderservice.model.BatchNoRecipeHeader;
import com.rits.batchnorecipeheaderservice.repository.BatchNoRecipeHeaderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.aspectj.util.LangUtil.safeList;

@RequiredArgsConstructor
@Service
public class BatchNoRecipeHeaderImpl implements BatchNoRecipeHeaderService{

    private final BatchNoRecipeHeaderRepository batchNoRecRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;

    @Autowired
    private MongoTemplate mongoTemplate;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Value("${recipe-service.url}/getRecipe")
    private String getRecipeUrl;

    @Value("${datacollection-service.url}/retrieveDCForBatchRecipe")
    private String retrieveDCForBatchRecipeUrl;

    @Value("${workInstruction-service.url}/getBatchRecipeWorkInstructionList")
    private String getBatchRecipeWorkInstructionListUrl;

    private String createHandle(BatchNoRecipeHeaderReq request){
        String batchNoBO = "BatchNoBO:" + request.getSite() + "," + request.getBatchNo();
        String recipeBO = "RecipeBO:" + request.getSite() + "," + request.getRecipeId() + "," + request.getRecipeVersion();
        return "BatchNoRecipeHeaderBO:" + request.getSite() + "," + recipeBO + "," + batchNoBO + "," + request.getMaterial() + "," + request.getMaterialVersion();
    }

    private BatchNoRecipeHeader getBatchNoRecipeHeader(BatchNoRecipeHeaderReq request){
//        String handle = createHandle(request);
        BatchNoRecipeHeader batchNoRecipeHeader;
        if (request.getOrderNo() != null) {
            batchNoRecipeHeader = batchNoRecRepository.findBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersionAndActive(request.getSite(), request.getBatchNo(), request.getOrderNo(), request.getMaterial(), request.getMaterialVersion(), 1);
        } else {
            batchNoRecipeHeader = batchNoRecRepository.findBySiteAndBatchNoAndMaterialAndMaterialVersionAndActive(request.getSite(), request.getBatchNo(), request.getMaterial(), request.getMaterialVersion(), 1);
        }
        return batchNoRecipeHeader;
    }


    @Override
    public MessageModel create(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException{
        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if(batchNoRecipeHeader != null)
            throw new BatchNoRecipeHeaderException(115, request.getBatchNo());

        String batchNoBO = BOConverter.retrieveBatchNoBO(request.getSite(), request.getBatchNo());

        BatchNoRecipeHeader batchRecipe = BatchNoRecipeHeader.builder()
                .site(request.getSite())
                .handle(createHandle(request))
                .batchNo(request.getBatchNo())
                .orderNo(request.getOrderNo())
                .batchNoHeaderBO(batchNoBO)
                .batchQty(request.getBatchQty())
                .material(request.getMaterial())
                .materialVersion(request.getMaterialVersion())
                .materialDescription(request.getMaterialDescription())
                .recipeName(request.getRecipeId())
                .recipeVersion(request.getRecipeVersion())
                .createdBy(request.getUser())
                .createdDatetime(LocalDateTime.now())
                .recipe(calculatedRecipe(request))
                .active(1)
                .build();

        String createdMessage = getFormattedMessage(1, request.getBatchNo());
        return MessageModel.builder().messageDetails(new MessageDetails(createdMessage, "S")).response(batchNoRecRepository.save(batchRecipe)).build();
    }


    @Override
    public MessageModel update(BatchNoRecipeHeaderReq request)throws BatchNoRecipeHeaderException{

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if(batchNoRecipeHeader == null)
            throw new BatchNoRecipeHeaderException(119, request.getBatchNo());

        batchNoRecipeHeader.setSite(request.getSite());
        batchNoRecipeHeader.setOrderNo(request.getOrderNo());
        batchNoRecipeHeader.setBatchNo(request.getBatchNo());
        batchNoRecipeHeader.setMaterial(request.getMaterial());
        batchNoRecipeHeader.setMaterialVersion(request.getMaterialVersion());
        batchNoRecipeHeader.setMaterialDescription(request.getMaterialDescription());
        batchNoRecipeHeader.setRecipeName(request.getRecipeId());
        batchNoRecipeHeader.setRecipeVersion(request.getRecipeVersion());
        batchNoRecipeHeader.setBatchQty(request.getBatchQty());
        batchNoRecipeHeader.setModifiedBy(request.getUser());
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());

        //recipe calculations
        batchNoRecipeHeader.setRecipe(calculatedRecipe(request));

        BatchNoRecipeHeader updatedBatchRecipe = batchNoRecRepository.save(batchNoRecipeHeader);

        String updateMessage = getFormattedMessage(2, request.getBatchNo());
        return MessageModel.builder().messageDetails(new MessageDetails(updateMessage, "S")).response(updatedBatchRecipe).build();
    }

    private Recipes calculatedRecipe(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException{

        if(StringUtils.isEmpty(request.getRecipeId()) /*|| StringUtils.isEmpty(request.getRecipeVersion())*/)
            throw new BatchNoRecipeHeaderException(120);

        RecipeRequests recipeRequest = RecipeRequests.builder()
                .site(request.getSite())
                .recipeId(request.getRecipeId())
                .version(request.getRecipeVersion())
                .build();

        RecipeMessageModel recipeMessage;

        try {
            recipeMessage = webClientBuilder.build()
                    .post()
                    .uri(getRecipeUrl)
                    .bodyValue(recipeRequest)
                    .retrieve()
                    .bodyToMono(RecipeMessageModel.class)
                    .block();

            if(recipeMessage == null)
                throw new BatchNoRecipeHeaderException(116);

        } catch (BatchNoRecipeHeaderException e){
            throw new BatchNoRecipeHeaderException(155);
        }

        if(recipeMessage.getResponse() == null)
            throw new BatchNoRecipeHeaderException(116);
        request.setRecipeVersion(recipeMessage.getResponse().getVersion());
        return batchwiseRecipeCalculation(recipeMessage.getResponse(), request);
    }

    private Recipes batchwiseRecipeCalculation(Recipes recipes, BatchNoRecipeHeaderReq request){

        double batchQty = request.getBatchQty();
        if(recipes.getBatchSize() == null) {
            throw new BatchNoRecipeHeaderException(118, recipes.getRecipeId());
        }
        double recipeBatchQty = Double.parseDouble(recipes.getBatchSize());

        updateRecipeQuantities(recipes, recipeBatchQty, batchQty);
        return recipes;
    }

    public void updateRecipeQuantities(Recipes recipe, double recipeBatchQty, double batchQty) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }

        if (recipe.getIngredients() != null) {
            calculateIngredients(recipe.getIngredients(), recipeBatchQty, batchQty);
//            if (recipe.getIngredients().getActive() != null) {
//                for (ParentIngredient activeIngredient : recipe.getIngredients().getActive()) {
//                    updateIngredientQuantities(activeIngredient, recipeBatchQty, batchQty);
//                }
//            }
//
//            if (recipe.getIngredients().getInactive() != null) {
//                for (ParentIngredient inactiveIngredient : recipe.getIngredients().getInactive()) {
//                    updateIngredientQuantities(inactiveIngredient, recipeBatchQty, batchQty);
//                }
//            }
        }

        if (recipe.getPhases() != null) {
            for (Phase phase : recipe.getPhases()) {

                if (phase.getIngredients() != null) {
                    calculateIngredients(phase.getIngredients(), recipeBatchQty, batchQty);
//                        if (phaseIngredient.getActive() != null) {
//                            for (ParentIngredient activeIngredient : phaseIngredient.getActive()) {
//                                updateIngredientQuantities(activeIngredient, recipeBatchQty, batchQty);
//                            }
//                        }
//
//                        if (phaseIngredient.getInactive() != null) {
//                            for (ParentIngredient inactiveIngredient : phaseIngredient.getInactive()) {
//                                updateIngredientQuantities(inactiveIngredient, recipeBatchQty, batchQty);
//                            }
//                        }
                }

                if (phase.getOperations() != null) {
                    for (Operation op : phase.getOperations()) {
                        if (op.getByProducts() != null) {
                            for (ByProduct stepByProduct : op.getByProducts()) {
                                if(stepByProduct.getExpectedQuantity() != null) {
                                    double updatedExpectedQuantity = calcBatchQty(recipeBatchQty, Double.parseDouble(stepByProduct.getExpectedQuantity()), batchQty);
                                    stepByProduct.setExpectedQuantity(String.valueOf(updatedExpectedQuantity));
                                }
                            }
                        }

                        if (op.getOpIngredients() != null) {
//                            for (Ingredients phaseIngredient : op.getOpIngredients()) {
                            calculateIngredients(op.getOpIngredients(), recipeBatchQty, batchQty);
//                                if (phaseIngredient.getActive() != null) {
//                                    for (ParentIngredient activeIngredient : phaseIngredient.getActive()) {
//                                        updateIngredientQuantities(activeIngredient, recipeBatchQty, batchQty);
//                                    }
//                                }
//
//                                if (phaseIngredient.getInactive() != null) {
//                                    for (ParentIngredient inactiveIngredient : phaseIngredient.getInactive()) {
//                                        updateIngredientQuantities(inactiveIngredient, recipeBatchQty, batchQty);
//                                    }
//                                }
//                            }
                        }
                    }
                }
            }
        }

        if (recipe.getYieldTracking() != null) {
            YieldTracking yieldTracking = recipe.getYieldTracking();

            if (yieldTracking.getWaste() != null) {
                for (Waste waste : yieldTracking.getWaste()) {
                    if(waste.getQuantity() != null) {
                        double updatedWasteQuantity = calcBatchQty(recipeBatchQty, Double.parseDouble(waste.getQuantity()), batchQty);
                        waste.setQuantity(String.valueOf(updatedWasteQuantity));
                    }
                }
            }

            if (yieldTracking.getByProducts() != null) {
                for (ByProduct yieldByProduct : yieldTracking.getByProducts()) {
                    if(yieldByProduct.getExpectedQuantity() != null) {
                        double updatedQuantityProduced = calcBatchQty(recipeBatchQty, Double.parseDouble(yieldByProduct.getExpectedQuantity()), batchQty);
                        yieldByProduct.setExpectedQuantity(String.valueOf(updatedQuantityProduced));
                    }
                }
            }
        }
    }

    private void calculateIngredients(Ingredients ingredients, double recipeBatchQty, double batchQty){
        if (ingredients.getActive() != null) {
            for (ParentIngredient activeIngredient : ingredients.getActive()) {
                updateIngredientQuantities(activeIngredient, recipeBatchQty, batchQty);
            }
        }

        if (ingredients.getInactive() != null) {
            for (ParentIngredient inactiveIngredient : ingredients.getInactive()) {
                updateIngredientQuantities(inactiveIngredient, recipeBatchQty, batchQty);
            }
        }
    }

    private void updateIngredientQuantities(ParentIngredient ingredient, double recipeBatchQty, double batchQty) {
        if(ingredient.getQuantity() != null) {
            double updatedQuantity = calcBatchQty(recipeBatchQty, Double.parseDouble(ingredient.getQuantity()), batchQty);
            ingredient.setQuantity(String.valueOf(updatedQuantity));
        }

        if(ingredient.getWasteQuantity() != null) {
            double updatedWasteQuantity = calcBatchQty(recipeBatchQty, Double.parseDouble(ingredient.getWasteQuantity()), batchQty);
            ingredient.setWasteQuantity(String.valueOf(updatedWasteQuantity));
        }

        if (ingredient.getAlternateIngredients() != null) {
            for (AlternateIngredient alternate : ingredient.getAlternateIngredients()) {

                double updatedAltQuantity = calcBatchQty(recipeBatchQty, alternate.getQuantity(), batchQty);
                alternate.setQuantity(updatedAltQuantity);
            }
        }

        if (ingredient.getByProduct() != null) {
            ByProduct byProduct = ingredient.getByProduct();
            if(byProduct.getByProductQuantity() != null) {
                double updatedByProductQuantity = calcBatchQty(recipeBatchQty, Double.parseDouble(byProduct.getByProductQuantity()), batchQty);
                byProduct.setByProductQuantity(String.valueOf(updatedByProductQuantity));
            }
        }
    }

    private double calcBatchQty(double recipeBatchQty, double qtyToCalc, double batchQty){
        return (qtyToCalc / recipeBatchQty) * batchQty;
    }

    @Override
    public MessageModel delete(BatchNoRecipeHeaderReq request){

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if(batchNoRecipeHeader == null)
            throw new BatchNoRecipeHeaderException(119, request.getBatchNo());

        batchNoRecipeHeader.setActive(0);
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
        batchNoRecipeHeader.setModifiedBy(request.getUser());

        batchNoRecRepository.save(batchNoRecipeHeader);
        batchNoRecipeHeader = null;

        String deleteMessage = getFormattedMessage(3, request.getBatchNo());
        return MessageModel.builder().messageDetails(new MessageDetails(deleteMessage, "S")).build();
    }

    @Override
    public MessageModel retrieve(BatchNoRecipeHeaderReq request){
        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if(batchNoRecipeHeader == null)
            throw new BatchNoRecipeHeaderException(115, request.getBatchNo());

        return MessageModel.builder().response(batchNoRecipeHeader).build();
    }

    @Override
    public MessageModel retrieveAll(BatchNoRecipeHeaderReq request){
        return MessageModel.builder().recipeHeaderList(batchNoRecRepository.findBySiteAndActive(request.getSite(), 1)).build();
    }

    @Override
    public MessageModel retrieveTop50(BatchNoRecipeHeaderReq request){
        return MessageModel.builder().recipeHeaderList(batchNoRecRepository.findTop50BySiteAndActive(request.getSite(), 1)).build();
    }

    @Override
    public MessageModel getRecipesPhaseByFilters(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        List<BatchNoRecipeHeader> batchRecords = batchNoRecipeHeaderList(request.getRecipeId(), request.getRecipeVersion(), request.getBatchNo(), request.getOrderNo(), request.getSite());
        List<Phase> phases = new ArrayList<>();

        if(batchRecords == null)
            return MessageModel.builder().messageDetails(MessageDetails.builder().msg("no record found").build()).build();

        for(BatchNoRecipeHeader batchNoRecipeHeader : batchRecords){
            Recipes recipes = batchNoRecipeHeader.getRecipe();
            if(recipes == null)
                return MessageModel.builder().messageDetails(MessageDetails.builder().msg("no recipe found").build()).build();

            if(recipes.getPhases() != null)
                phases.addAll(recipes.getPhases());
        }

        return MessageModel.builder().phaseList(phases).build();
    }

    public List<BatchNoRecipeHeader> batchNoRecipeHeaderList(String recipeName, String version, String batchNo, String orderNo, String site) {
        Criteria criteria = Criteria.where("active").is(1).and("site").is(site);

        List<Criteria> optionalCriteria = new ArrayList<>();
        if (recipeName != null) {
            optionalCriteria.add(Criteria.where("recipeName").is(recipeName));
        }
        if (version != null) {
            optionalCriteria.add(Criteria.where("recipeVersion").is(version));
        }
        if (batchNo != null) {
            optionalCriteria.add(Criteria.where("batchNo").is(batchNo));
        }
        if (orderNo != null) {
            optionalCriteria.add(Criteria.where("orderNo").is(orderNo));
        }

        if (!optionalCriteria.isEmpty()) {
            criteria = criteria.andOperator(new Criteria().orOperator(optionalCriteria.toArray(new Criteria[0])));
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, BatchNoRecipeHeader.class);
    }

    @Override
    public MessageModel getBatchPhaseOpByType(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        if (request == null || StringUtils.isEmpty(request.getOperationType())) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Invalid request: operation type is missing", "E"))
                    .build();
        }

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);
        BatchNoRecipeHeader existanceBatchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if(existanceBatchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch not found", "E")).build();

        Recipes recipe = existanceBatchNoRecipeHeader.getRecipe();
        if (recipe == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe not found in the batch header", "E"))
                    .build();
        }

        List<Operation> operationList = findOpsByType(existanceBatchNoRecipeHeader.getRecipe(), request);

        if (operationList == null || operationList.isEmpty())
            return MessageModel.builder().messageDetails(new MessageDetails("Phase operation of " + request.getOperationType() + " not found", "E")).build();
        else
            return MessageModel.builder().operationList(operationList).build();
    }

    public List<Operation> findOpsByType(Recipes existingRecipe, BatchNoRecipeHeaderReq request) {
        List<Operation> operationList = new ArrayList<>();

        if (existingRecipe.getPhases() == null) {
            throw new BatchNoRecipeHeaderException(149);
        }

        String phaseSequence = request.getPhaseSequence();
        String opType = request.getOperationType() ;

        if (StringUtils.isEmpty(phaseSequence) || StringUtils.isEmpty(opType)) {
            throw new BatchNoRecipeHeaderException(150);
        }

        for (Phase phase : existingRecipe.getPhases()) {
            if (phase == null || phase.getSequence() == null) {
                continue;
            }
            if (phase.getSequence().equals(phaseSequence)) {

                if (phase.getOperations() == null)
                    continue;

                for (Operation op : phase.getOperations()) {
                    if (op != null && op.getType() != null && op.getType().equalsIgnoreCase(opType)) {
                        operationList.add(op);
                    }
                }
                break;
            }
        }

        return operationList;
    }

    @Override
    public MessageModel addIngredients(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        if (batchNoRecipeHeader.getRecipe() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();

        if ("active".equalsIgnoreCase(request.getIngredientType())) {
            if (existingRecipe.getIngredients() == null) {
                Ingredients ingredients = new Ingredients();
                ingredients.setActive(new ArrayList<>());
                ingredients.getActive().add(request.getParentIngredient());
                existingRecipe.setIngredients(ingredients);
            } else if (existingRecipe.getIngredients().getActive() == null) {
                existingRecipe.getIngredients().setActive(new ArrayList<>());
                existingRecipe.getIngredients().getActive().add(request.getParentIngredient());
            } else {
                boolean ingredientExists = request.getParentIngredient() != null &&
                        existingRecipe.getIngredients().getActive().stream()
                                .anyMatch(ingredient -> ingredient.getSequence()
                                        .equals(request.getParentIngredient().getSequence()));


                if (ingredientExists) {
                    return MessageModel.builder()
                            .messageDetails(new MessageDetails("Ingredient ID already exists in active ingredients", "E"))
                            .build();
                } else {
                    existingRecipe.getIngredients().getActive().add(request.getParentIngredient());
                }
            }
        }

        if ("inActive".equalsIgnoreCase(request.getIngredientType())) {
            if (existingRecipe.getIngredients() == null) {
                Ingredients ingredients = new Ingredients();
                ingredients.setInactive(new ArrayList<>());
                ingredients.getInactive().add(request.getParentIngredient());
                existingRecipe.setIngredients(ingredients);
            } else if (existingRecipe.getIngredients().getInactive() == null) {
                existingRecipe.getIngredients().setInactive(new ArrayList<>());
                existingRecipe.getIngredients().getInactive().add(request.getParentIngredient());
            } else {
                boolean ingredientExists = request.getParentIngredient() != null && existingRecipe.getIngredients().getInactive().stream()
                        .anyMatch(ingredient -> ingredient.getSequence().equals(request.getParentIngredient().getSequence()));

                if (ingredientExists) {
                    return MessageModel.builder()
                            .messageDetails(new MessageDetails("Ingredient ID already exists in inActive ingredients", "E"))
                            .build();
                } else {
                    existingRecipe.getIngredients().getInactive().add(request.getParentIngredient());
                }
            }
        }
        batchNoRecipeHeader.setModifiedBy(request.getUser());
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
        batchNoRecRepository.save(batchNoRecipeHeader);
        return MessageModel.builder()
                .messageDetails(new MessageDetails("Ingredient added successfully to active ingredients", "S"))
                .build();
    }

    @Override
    public MessageModel updateIngredient(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe not found", "E")).build();

        if (existingRecipe.getIngredients() == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("ingredients not present", "E")).build();

        String ingredientIdToUpdate = request.getIngreSequence();
        boolean ingredientFound = false;

        if (request.getIngredientType().equalsIgnoreCase("active") && existingRecipe.getIngredients().getActive() != null) {
            ingredientFound = updateIngredientIfExists(existingRecipe.getIngredients().getActive(), ingredientIdToUpdate, request);
        }

        if (request.getIngredientType().equalsIgnoreCase("inactive") && existingRecipe.getIngredients().getInactive() != null) {
            ingredientFound = updateIngredientIfExists(existingRecipe.getIngredients().getInactive(), ingredientIdToUpdate, request);
        }

        if (ingredientFound) {
            batchNoRecipeHeader.setModifiedBy(request.getUser());
            batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
            batchNoRecRepository.save(batchNoRecipeHeader);
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Ingredient updated successfully", "S"))
                    .build();
        } else {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Ingredient ID not found for update", "E"))
                    .build();
        }
    }

    private boolean updateIngredientIfExists(List<ParentIngredient> ingredients, String ingredientIdToUpdate, BatchNoRecipeHeaderReq request) {
        return ingredients.stream()
                .filter(ingredient -> ingredient.getSequence().equals(ingredientIdToUpdate))
                .findFirst()
                .map(ingredient -> {
                    updateParentIngredient(ingredient, request.getParentIngredient());
                    return true;
                })
                .orElse(false);
    }
    private static void updateParentIngredient(ParentIngredient ingredientToUpdate, ParentIngredient request) {

        existingParentIngredientUpdate(ingredientToUpdate, request);
        ingredientToUpdate.setStorageLocation(request.getStorageLocation());
        ingredientToUpdate.setTolerance(request.getTolerance());
        ingredientToUpdate.setMaterialType(request.getMaterialType());
        ingredientToUpdate.setSupplierId(request.getSupplierId());
        ingredientToUpdate.setSourceLocation(request.getSourceLocation());
        ingredientToUpdate.setHandlingInstructions(request.getHandlingInstructions());
        ingredientToUpdate.setStorageInstructions(request.getStorageInstructions());
        ingredientToUpdate.setUnitCost(request.getUnitCost());
        ingredientToUpdate.setCurrency(request.getCurrency());
        ingredientToUpdate.setTotalCost(request.getTotalCost());
        ingredientToUpdate.setWasteQuantity(request.getWasteQuantity());
        ingredientToUpdate.setWasteUoM(request.getWasteUoM());
        ingredientToUpdate.setBatchNumber(request.getBatchNumber());
        ingredientToUpdate.setHazardous(request.getHazardous());
        ingredientToUpdate.setExpiryDate(request.getExpiryDate());
        ingredientToUpdate.setManufactureDate(request.getManufactureDate());
    }
    private static void existingParentIngredientUpdate(ParentIngredient existingIngredient, ParentIngredient ingredientRequest){
        existingIngredient.setIngredientId(ingredientRequest.getIngredientId());
        existingIngredient.setIngreDescription(ingredientRequest.getIngreDescription());
        existingIngredient.setQuantity(ingredientRequest.getQuantity());
        existingIngredient.setUom(ingredientRequest.getUom());
//        existingIngredient.setSequence(ingredientRequest.getSequence());
        existingIngredient.setMaterialDescription(ingredientRequest.getMaterialDescription());
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
//                existingQCs.add(qcRequest);
                throw new BatchNoRecipeHeaderException(166);
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
//                existingAlternates.add(alternateRequest);
                throw new BatchNoRecipeHeaderException(167, alternateRequest.getIngredientId());
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

    @Override
    public MessageModel deleteIngredient(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getIngredients() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("ingredients not present", "E")).build();

        List<ParentIngredient> activeIngredients = existingRecipe.getIngredients().getActive();
        List<ParentIngredient> inactiveIngredients = existingRecipe.getIngredients().getInactive();

        String ingredientIdToRemove = request.getIngreSequence();
        boolean deleteIngredient = false;

        if(activeIngredients != null){
            for (Iterator<ParentIngredient> iterator = activeIngredients.iterator(); iterator.hasNext();) {
                ParentIngredient activeIngredient = iterator.next();
                if (activeIngredient.getSequence().equals(ingredientIdToRemove)) {
                    iterator.remove();
                    deleteIngredient = true;
                    break;
                }
            }
        }

        if(!deleteIngredient) {
            if(inactiveIngredients != null) {
                for (Iterator<ParentIngredient> iterator = inactiveIngredients.iterator(); iterator.hasNext(); ) {
                    ParentIngredient inactiveIngredient = iterator.next();
                    if (inactiveIngredient.getSequence().equals(ingredientIdToRemove)) {
                        iterator.remove();
                        deleteIngredient = true;
                        break;
                    }
                }
            }
        }

        if(deleteIngredient) {
            batchNoRecipeHeader.setModifiedBy(request.getUser());
            batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
            batchNoRecRepository.save(batchNoRecipeHeader);
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Ingredients deleted", "S"))
                    .build();
        }
        existingRecipe = null;

        return MessageModel.builder()
                .messageDetails(new MessageDetails("Ingredients not present to delete", "E"))
                .build();

    }

    @Override
    public MessageModel addPhases(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        Phase userPhase = request.getPhase();

        if(existingRecipe.getPhases() != null && !existingRecipe.getPhases().isEmpty()) {
            final MessageModel[] messageModel = {null};
            boolean phaseExists = existingRecipe.getPhases().stream()
                    .anyMatch(phase -> {
                        if (phase == null) {
                            messageModel[0] = MessageModel.builder()
                                    .messageDetails(new MessageDetails("Phase is null.", "E"))
                                    .build();
                            return false;
                        } else if (phase.getSequence() == null) {
                            messageModel[0] = MessageModel.builder()
                                    .messageDetails(new MessageDetails("Phase ID is null for phase: " + phase, "E"))
                                    .build();
                            return false;
                        }
                        return phase.getSequence().equals(request.getPhaseSequence());
                    });

            if (messageModel[0] != null) {
                return messageModel[0];
            }
            if (phaseExists) {
                return MessageModel.builder()
                        .messageDetails(new MessageDetails("phaseId already exists, so not added", "E"))
                        .build();
            } else {

                for (Phase phase : existingRecipe.getPhases()) {
                    if ("00".equals(phase.getNextPhase())) {
                        phase.setNextPhase(request.getPhaseSequence());
                        phase.setExitPhase(false);
                        break;
                    }
                }
                existingRecipe.getPhases().add(request.getPhase());
            }

        } else {
            existingRecipe.setPhases(new ArrayList<>());
            existingRecipe.getPhases().add(userPhase);
        }
        batchNoRecipeHeader.setModifiedBy(request.getUser());
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
        batchNoRecRepository.save(batchNoRecipeHeader);
        existingRecipe = null;

        return MessageModel.builder()
                .messageDetails(new MessageDetails("Phases added", "S"))
                .build();
    }

    @Override
    public MessageModel updatePhase(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty())
            return MessageModel.builder().messageDetails(new MessageDetails("phases not available" , "E")).build();

        boolean isUpdated = false;
        if(request.getPhase() != null) {
//            for (Phase incomingPhase : request.getPhases()) {

            for (Phase existingPhase : existingRecipe.getPhases()) {
                if (existingPhase.getSequence().equals(request.getPhaseSequence())) {
                    updatePhaseFields(request,existingPhase, request.getPhase());
                    isUpdated = true;
                    batchNoRecipeHeader.setModifiedBy(request.getUser());
                    batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
                    batchNoRecRepository.save(batchNoRecipeHeader);
                    break;
                }
            }
//            }
        }
        existingRecipe = null;

        if(isUpdated)
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase updated", "S"))
                    .build();
        else
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("phase dosent exist to update", "E"))
                    .build();

    }

    private void updatePhaseFields(BatchNoRecipeHeaderReq request, Phase existingPhase, Phase incomingPhase) {
        existingPhase.setPhaseId(incomingPhase.getPhaseId());
        existingPhase.setPhaseDescription(incomingPhase.getPhaseDescription());

//        existingPhase.setSequence(incomingPhase.getSequence());
        existingPhase.setExpectedCycleTime(incomingPhase.getExpectedCycleTime());
        existingPhase.setConditional(incomingPhase.getConditional());
        existingPhase.setParallel(incomingPhase.getParallel());
        existingPhase.setAnyOrder(incomingPhase.getAnyOrder());
        existingPhase.setTriggeredPhase(incomingPhase.getTriggeredPhase());

        updatePhaseIngredients(request, existingPhase.getIngredients(), incomingPhase.getIngredients());
        updatePhaseOperations(request, existingPhase.getOperations(), incomingPhase.getOperations());

    }

    private void updatePhaseIngredients(BatchNoRecipeHeaderReq request, Ingredients existingIngredients, Ingredients incomingIngredients) {
        if (existingIngredients == null) {
            existingIngredients = new Ingredients();
        }

        if (existingIngredients.getActive() == null) {
            existingIngredients.setActive(new ArrayList<>());
        }
        if (existingIngredients.getInactive() == null) {
            existingIngredients.setInactive(new ArrayList<>());
        }

        if (incomingIngredients.getActive() != null) {
            existingIngredients.setActive(updateParentIngredients(existingIngredients.getActive(), incomingIngredients.getActive()));
        }

        if (incomingIngredients.getInactive() != null) {
            existingIngredients.setInactive(updateParentIngredients(existingIngredients.getInactive(), incomingIngredients.getInactive()));
        }
    }

    private List<ParentIngredient> updateParentIngredients(List<ParentIngredient> existingIngredients, List<ParentIngredient> incomingIngredients) {
        if (existingIngredients == null) {
            existingIngredients = new ArrayList<>();
        }

        Map<String, ParentIngredient> existingMap = existingIngredients.stream()
                .collect(Collectors.toMap(ParentIngredient::getSequence, ingredient -> ingredient));

        for (ParentIngredient incomingIngredient : incomingIngredients) {
            ParentIngredient existingIngredient = existingMap.get(incomingIngredient.getSequence());

            if (existingIngredient != null) {
                updateParentIngredient(existingIngredient, incomingIngredient);
            } else {
                // Add new ingredient
//                existingIngredients.add(incomingIngredient);
                throw new BatchNoRecipeHeaderException(165);
            }
        }

        return existingIngredients;
    }

    private static List<ParentIngredient> ensureActiveListExists(List<Ingredients> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return new ArrayList<>();
        }

        Ingredients firstIngredient = ingredients.get(0);
        if (firstIngredient.getActive() == null) {
            firstIngredient.setActive(new ArrayList<>());
        }

        return firstIngredient.getActive();
    }

    private static List<ParentIngredient> ensureInactiveListExists(List<Ingredients> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return new ArrayList<>();
        }

        Ingredients firstIngredient = ingredients.get(0);
        if (firstIngredient.getInactive() == null) {
            firstIngredient.setInactive(new ArrayList<>());
        }

        return firstIngredient.getInactive();
    }


    private static ParentIngredient findIngredientInList(List<Ingredients> ingredients, String ingredientId, boolean isActive) {
        if (ingredients == null || ingredientId == null) {
            return null;
        }

        for (Ingredients ingredient : ingredients) {
            List<ParentIngredient> targetList = isActive ? ingredient.getActive() : ingredient.getInactive();
            if (targetList != null) {
                for (ParentIngredient parentIngredient : targetList) {
                    if (parentIngredient != null && ingredientId.equals(parentIngredient.getSequence())) {
                        return parentIngredient;
                    }
                }
            }
        }
        return null;
    }

//    private static PhaseIngredient findIngredientById(List<PhaseIngredient> ingredients, String ingredientId) {
//        for (PhaseIngredient ingredient : ingredients) {
//            if (ingredient.getSequence().equals(ingredientId)) {
//                return ingredient;
//            }
//        }
//        return null;
//    }

    private void updatePhaseOperations(BatchNoRecipeHeaderReq request, List<Operation> existingops, List<Operation> opRequests) {

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
                existingop.setOperationDescription(opRequest.getOperationDescription());
                existingop.setInstruction(opRequest.getInstruction());
                existingop.setTools(opRequest.getTools());
                existingop.setExpectedCycleTime(opRequest.getExpectedCycleTime());
                existingop.setType(opRequest.getType());
                existingop.setCcp(opRequest.getCcp());

                updateResources(existingop, existingop.getResources(), opRequest.getResources());
                updateDataCollections(existingop, existingop.getDataCollection(), opRequest.getDataCollection());
                updateQualityControlParameter(existingop, existingop.getQcParameters(), opRequest.getQcParameters());
                updateAdjustments(existingop, existingop.getAdjustments(), opRequest.getAdjustments());
                updateCriticalControlPoints(existingop, existingop.getCriticalControlPoints(), opRequest.getCriticalControlPoints());
                updateByProducts(existingop, existingop.getByProducts(), opRequest.getByProducts());
                updatePhaseIngredients(request, existingop.getOpIngredients(), opRequest.getOpIngredients());
            } else {
//                existingops.add(opRequest);
                throw new BatchNoRecipeHeaderException(168, opRequest.getOperationId());
            }
        }

    }

    private static Operation findOperationById(List<Operation> ops, String opId) {
        for (Operation op : ops) {
            if (op.getSequence().equals(opId)) {
                return op;
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
            Resource existingResource = findResourceById(existingResources, resourceRequest.getSequence());

            if (existingResource != null) {
                existingResource.setResourceId(resourceRequest.getResourceId());
                existingResource.setDescription(resourceRequest.getDescription());
                existingResource.setWorkCenterId(resourceRequest.getWorkCenterId());

                updateResourceParameters(existingResource, existingResource.getParameters(), resourceRequest.getParameters());

            } else {
//                existingResources.add(resourceRequest);
                throw new BatchNoRecipeHeaderException(169, resourceRequest.getResourceId());
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
            if (resource.getSequence().equals(resourceId)) {
                return resource;
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
            DataCollection existingDataCollection = findDataCollectionById(existingDataCollections, dataCollectionRequest.getSequence());

            if (existingDataCollection != null) {
                existingDataCollection.setDataPointId(dataCollectionRequest.getDataPointId());
                existingDataCollection.setDescription(dataCollectionRequest.getDescription());
                existingDataCollection.setFrequency(dataCollectionRequest.getFrequency());
                existingDataCollection.setExpectedValueRange(dataCollectionRequest.getExpectedValueRange());
                existingDataCollection.setParameterName(dataCollectionRequest.getParameterName());
                existingDataCollection.setExpectedValue(dataCollectionRequest.getExpectedValue());
                existingDataCollection.setMonitoringFrequency(dataCollectionRequest.getMonitoringFrequency());
                existingDataCollection.setAllowedVariance(dataCollectionRequest.getAllowedVariance());
            } else {
//                existingDataCollections.add(dataCollectionRequest);
                throw new BatchNoRecipeHeaderException(170, dataCollectionRequest.getDataPointId());
            }
        }
        existingop.setDataCollection(existingDataCollections);
    }

    private static DataCollection findDataCollectionById(List<DataCollection> dataCollections, String dataPointId) {
        for (DataCollection dataCollection : dataCollections) {
            if (dataCollection.getSequence().equals(dataPointId)) {
                return dataCollection;
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

    private static void updateAdjustments(Operation existingop, List<Adjustment> existingAdjustments, List<Adjustment> adjustmentRequests) {
        if (adjustmentRequests == null){
            existingop.setAdjustments(null);
            return;
        }

        if (existingAdjustments == null) {
            existingAdjustments = new ArrayList<>();
        }

        for (Adjustment adjustmentRequest : adjustmentRequests) {
            Adjustment existingAdjustment = findAdjustmentById(existingAdjustments, adjustmentRequest.getSequence());

            if (existingAdjustment != null) {
                existingAdjustment.setReason(adjustmentRequest.getReason());
                existingAdjustment.setImpactOnProcess(adjustmentRequest.getImpactOnProcess());
                existingAdjustment.setAdjustmentId(adjustmentRequest.getAdjustmentId());
                existingAdjustment.setAdjustmentType(adjustmentRequest.getAdjustmentType());
                existingAdjustment.setImpactOnYield(adjustmentRequest.getImpactOnYield());
                existingAdjustment.setEffectOnQuality(adjustmentRequest.getEffectOnQuality());
                existingAdjustment.setEffectOnCycleTime(adjustmentRequest.getEffectOnCycleTime());

            } else {
//                existingAdjustments.add(adjustmentRequest);
                throw new BatchNoRecipeHeaderException(171,adjustmentRequest.getSequence() );
            }
        }
        existingop.setAdjustments(existingAdjustments);
    }

    private static Adjustment findAdjustmentById(List<Adjustment> adjustments, String adjustmentId) {
        for (Adjustment adjustment : adjustments) {
            if (adjustment.getSequence().equals(adjustmentId)) {
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

    private static void updateByProducts(Operation existingop, List<ByProduct> existingByProducts, List<ByProduct> byProductRequests) {
        if (byProductRequests == null){
            existingop.setByProducts(null);
            return;
        }

        if (existingByProducts == null) {
            existingByProducts = new ArrayList<>();
        }

        for (ByProduct byProductRequest : byProductRequests) {
            ByProduct existingByProduct = findByProductById(existingByProducts, byProductRequest.getSequence());

            if (existingByProduct != null) {
                updateByProduct(existingByProduct, byProductRequest);
            } else {
//                existingByProducts.add(byProductRequest);
                throw new BatchNoRecipeHeaderException(172, byProductRequest.getSequence());
            }
        }
        existingop.setByProducts(existingByProducts);
    }

    private static ByProduct findByProductById(List<ByProduct> byProducts, String byProductId) {
        for (ByProduct byProduct : byProducts) {
            if (byProduct.getSequence().equals(byProductId)) {
                return byProduct;
            }
        }
        return null;
    }

    private static void updateByProduct(ByProduct existingByProduct, ByProduct byProductRequest) {
        if (byProductRequest == null){
            existingByProduct = new ByProduct();
            return;
        }

        if (existingByProduct == null) {
            existingByProduct = new ByProduct();
        }

        existingByProduct.setDescription(byProductRequest.getDescription());
        existingByProduct.setExpectedQuantity(byProductRequest.getExpectedQuantity());
        existingByProduct.setUom(byProductRequest.getUom());
        existingByProduct.setHandlingProcedure(byProductRequest.getHandlingProcedure());
    }

    @Override
    public MessageModel deletePhase(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty())
            return MessageModel.builder().messageDetails(new MessageDetails("phases not available" , "E")).build();

        boolean removed = existingRecipe.getPhases().removeIf(phase -> request.getPhaseSequence().equals(phase.getSequence()));

        if (!removed)
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("phase not present to delete in " + existingRecipe.getRecipeId(), "E"))
                    .build();

        batchNoRecipeHeader.setModifiedBy(request.getUser());
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
        batchNoRecRepository.save(batchNoRecipeHeader);
        existingRecipe = null;

        return MessageModel.builder()
                .messageDetails(new MessageDetails("Phase deleted from recipe " + existingRecipe.getRecipeId(), "S"))
                .build();
    }

    @Override
    public MessageModel trackYield(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (request.getYieldTracking() == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("yield tracking not found", "E")).build();

        String actualYield = request.getYieldTracking().getActualYield();

        if (actualYield == null || actualYield.isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("actualYield value is empty", "E"))
                    .build();
        }

        if(existingRecipe.getYieldTracking() == null)
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("yieldTracking is empty", "E"))
                    .build();

        YieldTracking yieldTracking = existingRecipe.getYieldTracking();

        yieldTracking.setActualYield(actualYield);

        if(request.getYieldTracking().getByProducts() != null) {
            // Update ByProducts
            for (ByProduct byProductUpdate : request.getYieldTracking().getByProducts()) {
                boolean found = yieldTracking.getByProducts() != null && byProductUpdate != null && byProductUpdate.getSequence() != null &&
                        yieldTracking.getByProducts().stream()
                                .filter(existingByProduct -> existingByProduct != null && existingByProduct.getSequence() != null &&
                                        existingByProduct.getSequence().equals(byProductUpdate.getSequence()))
                                .peek(existingByProduct -> {
                                    existingByProduct.setByProductId(byProductUpdate.getByProductId());
                                    existingByProduct.setDescription(byProductUpdate.getDescription());
                                    existingByProduct.setExpectedQuantity(byProductUpdate.getExpectedQuantity());
                                    existingByProduct.setUom(byProductUpdate.getUom());
                                    existingByProduct.setHandlingProcedure(byProductUpdate.getHandlingProcedure());
                                })
                                .findFirst()
                                .isPresent();


                if (!found) {
                    throw new BatchNoRecipeHeaderException(135, byProductUpdate.getByProductId());
                }
            }
        }

        // Update Wastes
        if(request.getYieldTracking().getWaste() != null) {
            for (Waste wasteUpdate : request.getYieldTracking().getWaste()) {
                boolean found = existingRecipe.getYieldTracking().getWaste() != null &&
                        wasteUpdate != null &&
                        wasteUpdate.getSequence() != null &&
                        existingRecipe.getYieldTracking().getWaste().stream()
                                .filter(existingWaste -> existingWaste != null &&
                                        existingWaste.getSequence() != null &&
                                        existingWaste.getSequence().equals(wasteUpdate.getSequence()))
                                .peek(existingWaste -> {
                                    existingWaste.setWasteId(wasteUpdate.getWasteId());
                                    existingWaste.setDescription(wasteUpdate.getDescription());
                                    existingWaste.setQuantity(wasteUpdate.getQuantity());
                                    existingWaste.setUom(wasteUpdate.getUom());
                                    existingWaste.setHandlingProcedure(wasteUpdate.getHandlingProcedure());
                                    existingWaste.setCostOfDisposal(wasteUpdate.getCostOfDisposal());
                                })
                                .findFirst()
                                .isPresent();


                if (!found) {
                    throw new BatchNoRecipeHeaderException(136, wasteUpdate.getWasteId());
                }
            }
        }

        String expectedYield = yieldTracking.getExpectedYield();
        if (Double.parseDouble(actualYield) < Double.parseDouble(expectedYield)) {
            // triggerCorrections(recipeId)
        }

        batchNoRecipeHeader.setModifiedBy(request.getUser());
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());

        batchNoRecRepository.save(batchNoRecipeHeader);

        existingRecipe = null;

        return MessageModel.builder()
                .messageDetails(new MessageDetails("Yield tracked successfully", "S"))
                .build();
    }

    @Override
    public MessageModel validateRecipeData(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        boolean validateRecipe = validateRecipe(request);
        String recipeValidator = validateRecipe ? "Recipe validated" : "Recipe validation failed";

        boolean validateIng = validateIngredients(request);
        String ingredientValidator = validateIng ? "ingredient validated" : "ingredient validation failed";

        return MessageModel.builder()
                .messageDetails(new MessageDetails(recipeValidator + " and " +  ingredientValidator, "S"))
                .build();
    }

    public boolean validateRecipe(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException{
        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null)
            throw new BatchNoRecipeHeaderException(119, request.getBatchNo());

        if(batchNoRecipeHeader.getRecipe() != null){
            Recipes recipes = batchNoRecipeHeader.getRecipe();

            if(recipes.getIngredients() == null)
                throw new BatchNoRecipeHeaderException(137);

            Ingredients ingredients = recipes.getIngredients();
            boolean activeIngredientFound = false;
            boolean inactiveIngredientFound = false;

            if(ingredients.getActive() != null) {
                activeIngredientFound = ingredients.getActive().stream()
                        .anyMatch(active -> active.getSequence().equals(request.getIngreSequence()));
            }

            if(ingredients.getInactive() != null) {
                inactiveIngredientFound = ingredients.getInactive().stream()
                        .anyMatch(inactive -> inactive.getSequence().equals(request.getIngreSequence()));
            }

            boolean ingredientFound = activeIngredientFound || inactiveIngredientFound;

            boolean phaseFound = recipes.getPhases().stream()
                    .anyMatch(phase -> phase.getSequence().equals(request.getPhaseSequence()));

            recipes = null;

            if (ingredientFound && phaseFound) {
                return true;
            }
        }
        return false;
    }

//    public boolean validateIngredients(BatchNoRecipeHeaderReq request) {
//        if(request.getPhaseIngredients() != null && !request.getPhaseIngredients().isEmpty()){
//            if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
//                throw new BatchNoRecipeHeaderException(114);
//
//            BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
//
//            if (batchNoRecipeHeader != null) {
//                if(batchNoRecipeHeader.getRecipe() == null)
//                    throw new BatchNoRecipeHeaderException(116);
//
//                Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
//                if(request.getPhaseIngredients() == null || request.getPhaseIngredients().isEmpty())
//                    throw new BatchNoRecipeHeaderException(138);
//
//                List<Ingredients> ingredients = request.getPhaseIngredients();
//
//                for (Ingredients ingredient : ingredients) {
//                    boolean isIngredientAvailable = existingRecipe.getPhases() != null &&
//                            existingRecipe.getPhases().stream()
//                                    .filter(phase -> phase.getIngredients() != null) // Filter out phases with null ingredients
//                                    .flatMap(phase -> phase.getIngredients().stream())
//                                    .anyMatch(phaseIngredient ->
//                                            phaseIngredient != null &&
//                                                    org.springframework.util.StringUtils.hasText(phaseIngredient.getIngredientName()) && phaseIngredient.getIngredientName().equals(ingredient.getIngredientName()) &&
//                                                    org.springframework.util.StringUtils.hasText(phaseIngredient.getQuantity()) && phaseIngredient.getQuantity().equals(ingredient.getQuantity()) &&
//                                                    org.springframework.util.StringUtils.hasText(phaseIngredient.getUom()) && phaseIngredient.getUom().equals(ingredient.getUom())
//                                    );
//
//                    if (!isIngredientAvailable) {
//                        return false;
//                    }
//                }
//                existingRecipe = null;
//            }
//
//        } else {
//            return false;
//
//        }
//        return true;
//    }

//    public boolean validateIngredients(BatchNoRecipeHeaderReq request) {
//        // Check for mandatory fields in the request
//        if (request.getPhaseIngredients() == null || request.getPhaseIngredients().isEmpty()) {
//            return false;
//        }
//
//        if (StringUtils.isEmpty(request.getBatchNo()) ||
//                StringUtils.isEmpty(request.getOrderNo()) ||
//                StringUtils.isEmpty(request.getMaterial()) ||
//                StringUtils.isEmpty(request.getMaterialVersion())) {
//            throw new BatchNoRecipeHeaderException(114);
//        }
//
//        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
//
//        // Validate if batchNoRecipeHeader exists and contains a recipe
//        if (batchNoRecipeHeader == null || batchNoRecipeHeader.getRecipe() == null) {
//            throw new BatchNoRecipeHeaderException(batchNoRecipeHeader == null ? 116 : 138);
//        }
//
//        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
//        List<Ingredients> ingredients = request.getPhaseIngredients();
//
//        // Iterate through each ingredient in the request
//        for (Ingredients ingredient : ingredients) {
//            boolean isIngredientAvailable = existingRecipe.getPhases() != null &&
//                    existingRecipe.getPhases().stream()
//                            .filter(phase -> phase.getIngredients() != null) // Consider phases with ingredients
//                            .flatMap(phase -> phase.getIngredients().stream()) // Flatten the Ingredients list
//                            .anyMatch(phaseIngredient ->
//                                    (phaseIngredient.getActive() != null && matchParentIngredients(phaseIngredient.getActive(), ingredient)) ||
//                                            (phaseIngredient.getInactive() != null && matchParentIngredients(phaseIngredient.getInactive(), ingredient))
//                            );
//
//            if (!isIngredientAvailable) {
//                return false; // Validation fails if the ingredient is not found
//            }
//        }
//
//        return true;
//    }

    public boolean validateIngredients(BatchNoRecipeHeaderReq request) {
        if (request.getPhaseIngredients() == null) {
            return false;
        }

        if (StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion())) {
            throw new BatchNoRecipeHeaderException(114);
        }

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null || batchNoRecipeHeader.getRecipe() == null) {
            throw new BatchNoRecipeHeaderException(batchNoRecipeHeader == null ? 116 : 138);
        }

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        Ingredients requestIngredients = request.getPhaseIngredients();

//        for (Ingredients requestIngredient : requestIngredients) {
        boolean isIngredientValid = existingRecipe.getPhases() != null &&
                existingRecipe.getPhases().stream()
                        .filter(phase -> phase.getIngredients() != null) // Only consider phases with ingredients
                        .anyMatch(phase -> matchParentIngredients(phase.getIngredients(), requestIngredients));

        if (!isIngredientValid) {
            return false;
        }
//        }

        return true;
    }

    private boolean matchParentIngredients(Ingredients phaseIngredients, Ingredients requestIngredients) {
        if (phaseIngredients == null || requestIngredients == null) {
            return false;
        }

        // Compare active ingredients
        if (requestIngredients.getActive() != null) {
            for (ParentIngredient requestIngredient : requestIngredients.getActive()) {
                boolean activeMatch = matchSingleIngredient(phaseIngredients.getActive(), requestIngredient);
                if (!activeMatch) {
                    return false;
                }
            }
        }

        // Compare inactive ingredients
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


//    private boolean matchParentIngredients(List<ParentIngredient> parentIngredients, Ingredients ingredient) {
//        return parentIngredients.stream()
//                .anyMatch(parentIngredient ->
//                        parentIngredient != null &&
//                                ingredient.getActive() != null && matchWithParentList(ingredient.getActive(), parentIngredient) ||
//                                ingredient.getInactive() != null && matchWithParentList(ingredient.getInactive(), parentIngredient)
//                );
//    }

    private boolean matchWithParentList(List<ParentIngredient> ingredientList, ParentIngredient parentIngredient) {
        return ingredientList.stream()
                .anyMatch(ingredient ->
                        hasTextAndEquals(parentIngredient.getIngreDescription(), ingredient.getIngreDescription()) &&
                                hasTextAndEquals(parentIngredient.getQuantity(), ingredient.getQuantity()) &&
                                hasTextAndEquals(parentIngredient.getUom(), ingredient.getUom()));
    }

    private boolean hasTextAndEquals(String value1, String value2) {
        return org.springframework.util.StringUtils.hasText(value1) && value1.equals(value2);
    }

    public MessageModel getRecipePhases(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if(existingRecipe == null)
            throw new BatchNoRecipeHeaderException(114, request.getRecipeId());

        return MessageModel.builder().phases(existingRecipe.getPhases()).messageDetails(new MessageDetails("Phase list","S")).build();

    }

    @Override
    public MessageModel getPhaseOperations(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("phases not available" , "E")).build();

        List<Operation> ops = existingRecipe.getPhases().stream()
                .filter(phase -> phase.getSequence().equals(request.getPhaseSequence()))
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
            return MessageModel.builder().messageDetails(new MessageDetails("Operation not found","S")).build();

        return MessageModel.builder().ops(ops).build();

    }

    @Override
    public MessageModel getBatchRecipeOperations(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("phases not available" , "E")).build();

        List<Operation> operations = safeList(existingRecipe.getPhases()).stream()
                .filter(phase -> phase != null && phase.getOperations() != null) // Ensure both phase and operations are not null
                .flatMap(phase -> phase.getOperations().stream()) // Flatten operations
                .collect(Collectors.toList());

        existingRecipe = null;

        if(operations.isEmpty())
            return MessageModel.builder().messageDetails(new MessageDetails("Operation not found","S")).build();

        return MessageModel.builder().ops(operations).build();

    }

    @Override
    public MessageModel getPhaseOperationById(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("phases not available" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("phase Id not found" , "E")).build();
        }

        if (phase.getOperations() == null || phase.getOperations().isEmpty()) {
            return MessageModel.builder().messageDetails(new MessageDetails("phase operations not found" , "E")).build();
        }

        Operation op = phase.getOperations().stream()
                .filter(s -> s.getSequence().equals(request.getOpSequence()))
                .findFirst()
                .orElse(null);


        existingRecipe = null;

        if(op == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Operation not found","S")).build();

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

        return MessageModel.builder().op(op).ingredient(filteredIngredients).build();

    }

    private boolean isIngredientRelevantToOperation(ParentIngredient ingredient, Operation op) {
        return ingredient.getOperationId() == null || ingredient.getOperationId().isEmpty() ||
                ingredient.getOperationId().equals(op.getOperationId());
    }


    @Override
    public MessageModel verifyIngredients(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        String status = " ";

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getIngredients() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("ingredients not found" , "E")).build();

        List<ParentIngredient> activeIngredients = new ArrayList<>();
        List<ParentIngredient> inActiveIngredients = new ArrayList<>();

        if (request.getIngredients().getActive() != null) {
            activeIngredients.addAll(request.getIngredients().getActive());
        }

        if (request.getIngredients().getInactive() != null) {
            inActiveIngredients.addAll(request.getIngredients().getInactive());
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
                status = status + activeIngredient.getIngredientId() + " failed ";
            } else {
                status = status + activeIngredient.getIngredientId() + " passed ";
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
                status = status + inactiveIngredient.getIngredientId() + " failed ";
            } else {
                status = status + inactiveIngredient.getIngredientId() + " passed ";
            }
        }

        return MessageModel.builder()
                .messageDetails(new MessageDetails(status, "S"))
                .build();
    }

    @Override
    public MessageModel getAlternateRecipes(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        List<AlternateIngredient> alternateIngredients = new ArrayList<>();

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getIngredients() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("ingredients not found" , "E")).build();

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

        return MessageModel.builder().alternateIngredients(alternateIngredients).build();
    }

    @Override
    public MessageModel calculateYield(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        YieldTracking yieldTracking = existingRecipe.getYieldTracking();
        if (yieldTracking == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("yieldTracking not found" , "E")).build();
        }

        double expectedYield = Double.parseDouble(yieldTracking.getExpectedYield());
        double allowedVariance = Double.parseDouble(yieldTracking.getAllowedVariance());

        double actualYield = Double.parseDouble(request.getActualYield());

        double deviation = actualYield - expectedYield;

        existingRecipe = null;
        if (Math.abs(deviation) > allowedVariance) {

            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Yield deviation beyond allowed variance with " + expectedYield + ", " + actualYield + ", " + allowedVariance , "E"))
                    .waste(request.getYieldTracking().getWaste())
                    .byProducts(request.getYieldTracking().getByProducts())
                    .build();
        }

        return MessageModel.builder()
                .messageDetails(new MessageDetails("Yield within allowed limits with " + expectedYield + ", " + actualYield + ", " + allowedVariance , "E"))
                .waste(request.getYieldTracking().getWaste())
                .byProducts(request.getYieldTracking().getByProducts())
                .build();

    }

    @Override
    public MessageModel getPhaseIngredients(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("phases not found" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("phase Id not found" , "E")).build();
        }

        existingRecipe = null;

        return MessageModel.builder().processIngredient(phase.getIngredients()).build();
    }

    @Override
    public MessageModel getOperationInstructions(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("phases not found" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("phase Id not found" , "E")).build();
        }
        if (phase.getOperations() == null || phase.getOperations().isEmpty()) {
            return MessageModel.builder().messageDetails(new MessageDetails("phase operations not found" , "E")).build();
        }

        Operation op = phase.getOperations().stream()
                .filter(s -> s.getSequence().equals(request.getOpSequence()))
                .findFirst()
                .orElse(null);

        if (op == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("OperationId not found" , "E")).build();
        }
        List<BatchRecipeInstruction> batchRecipeInstructions = new ArrayList<>();
        if(request.isNeedMasterData())
            batchRecipeInstructions = getMasterWorkInstruction(request);

        existingRecipe = null;
        return MessageModel.builder().opInstruction(op.getInstruction()).masterWorkIns(batchRecipeInstructions).build();

    }

    private List<BatchRecipeInstruction> getMasterWorkInstruction(BatchNoRecipeHeaderReq request){
        BatchNoRecipeHeaderReq recipeHeaderReq = BatchNoRecipeHeaderReq.builder()
                .site(request.getSite())
                .batchNo(request.getBatchNo())
                .orderNo(request.getOrderNo())
                .phaseId(request.getPhaseId())
                .operationId(request.getOperationId())
                .item(request.getMaterial())
                .itemVersion(request.getMaterialVersion())
                .build();

        List<BatchRecipeInstruction> batchRecipeInstructions = new ArrayList<>();
        try {
            batchRecipeInstructions = webClientBuilder.build()
                    .post()
                    .uri(getBatchRecipeWorkInstructionListUrl)
                    .bodyValue(recipeHeaderReq)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<BatchRecipeInstruction>>() {
                    })
                    .block();

        } catch (BatchNoRecipeHeaderException e){
            throw new BatchNoRecipeHeaderException(155);
        }
        return batchRecipeInstructions;
    }


    //    @Override
//    public MessageModel getNextOperation1(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
//
//        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
//            throw new BatchNoRecipeHeaderException(114);
//
//        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
//        if (batchNoRecipeHeader == null)
//            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();
//
//        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
//        if (existingRecipe == null)
//            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();
//
//        if (existingRecipe.getPhases() == null)
//            return MessageModel.builder().messageDetails(new MessageDetails("phases not found" , "E")).build();
//
//        Phase phase = existingRecipe.getPhases().stream()
//                .filter(p -> p.getPhaseId().equals(request.getPhaseId()))
//                .findFirst()
//                .orElse(null);
//
//        if (phase == null) {
//            return MessageModel.builder().messageDetails(new MessageDetails("phase Id not found" , "E")).build();
//        }
//        if (phase.getOperations() == null || phase.getOperations().isEmpty()) {
//            return MessageModel.builder().messageDetails(new MessageDetails("phase operations not found" , "E")).build();
//        }
//
//        Operation currentOp = phase.getOperations().stream()
//                .filter(s -> s.getOperationId().equals(request.getOperationId()))
//                .findFirst()
//                .orElse(null);
//
//        if (currentOp == null) {
//            return MessageModel.builder().messageDetails(new MessageDetails("currentOperation Id not found" , "E")).build();
//        }
//
//        Optional<Operation> nextOp = phase.getOperations().stream()
//                .filter(s -> s.getSequence() > currentOp.getSequence())
//                .min(Comparator.comparing(Operation::getSequence));
//
//        if (nextOp.isPresent()) {
//            Operation op = nextOp.get();
//            Map<String, Object> response = new HashMap<>();
//            response.put("OperationId", op.getOperationName());
//            response.put("OperationName", op.getOperationName());
//            response.put("sequence", op.getSequence());
//            response.put("instruction", op.getInstruction());
//            response.put("expectedCycleTime", op.getExpectedCycleTime());
//
//            return MessageModel.builder().resultBody(response).build();
//
//        } else {
//            return MessageModel.builder().messageDetails(new MessageDetails("No further ops in this phase" , "E")).build();
//        }
//    }

//    @Override
//    public MessageModel getNextphase1(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
//
//        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
//            throw new BatchNoRecipeHeaderException(114);
//
//        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
//        if (batchNoRecipeHeader == null)
//            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();
//
//        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
//        if (existingRecipe == null)
//            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();
//
//        if (existingRecipe.getPhases() == null)
//            return MessageModel.builder().messageDetails(new MessageDetails("phases not found" , "E")).build();
//
//        Phase phase = existingRecipe.getPhases().stream()
//                .filter(p -> p.getPhaseId().equals(request.getPhaseId()))
//                .findFirst()
//                .orElse(null);
//
//        if (phase == null) {
//            return MessageModel.builder().messageDetails(new MessageDetails("phase Id not found" , "E")).build();
//        }
//
//        Optional<Phase> nextPhase = existingRecipe.getPhases().stream()
//                .filter(p -> p.getSequence() > phase.getSequence())
//                .min(Comparator.comparing(Phase::getSequence));
//
//        if (nextPhase.isPresent()) {
//            Phase phases = nextPhase.get();
//            Map<String, Object> response = new HashMap<>();
//            response.put("phaseId", phases.getPhaseName());
//            response.put("phaseName", phases.getPhaseName());
//            response.put("sequence", phases.getSequence());
//            response.put("expectedCycleTime", phases.getExpectedCycleTime());
//
//            return MessageModel.builder().resultBody(response).build();
//        } else {
//            return MessageModel.builder().messageDetails(new MessageDetails("No further phases in the recipe" , "E")).build();
//        }
//    }

    @Override
    public MessageModel getConditionalOperation(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("phases not found" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("phase Id not found" , "E")).build();
        }

        if(phase.getConditional() != null && Boolean.TRUE.equals(phase.getConditional())){
            String triggeredPhaseId = phase.getTriggeredPhase();
            Map<String, String> nextPhaseOp = nextTriggeredPhaseId(existingRecipe, triggeredPhaseId);

            if(nextPhaseOp == null)
                return MessageModel.builder().messageDetails(new MessageDetails("No triggered phase Operation available in this recipe" , "E")).build();

            return MessageModel.builder().phaseOps(nextPhaseOp).build();
        } else {
            return MessageModel.builder().messageDetails(new MessageDetails("Condition failed, no Operation found" , "E")).build();
        }
    }

    private Map<String, String> nextTriggeredPhaseId(Recipes existingRecipe, String triggeredPhaseId) {
        if (existingRecipe == null || existingRecipe.getPhases() == null) {
            return null;
        }

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(triggeredPhaseId))
                .findFirst()
                .orElse(null);

        if (phase != null && phase.getOperations() != null) {
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


//    @Override
//    public MessageModel getIngredientsWithVerification(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
//
//        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
//            throw new BatchNoRecipeHeaderException(114);
//
//        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
//
//        if (batchNoRecipeHeader == null)
//            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();
//
//        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
//
//        if (existingRecipe == null)
//            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();
//
//        if (existingRecipe.getPhases() == null)
//            return MessageModel.builder().messageDetails(new MessageDetails("phases not found" , "E")).build();
//
//        Phase phase = existingRecipe.getPhases().stream()
//                .filter(p -> p.getPhaseId().equals(request.getPhaseId()))
//                .findFirst()
//                .orElse(null);
//
//        if (phase == null) {
//            return MessageModel.builder().messageDetails(new MessageDetails("phase Id not found" , "E")).build();
//        }
//
//        if (phase.getIngredients() == null)
//            return MessageModel.builder().messageDetails(new MessageDetails("phases ingredients not found" , "E")).build();
//
//        Recipes finalExistingRecipe = existingRecipe;
//        List<PhaseIngredient> ingredients = phase.getIngredients().stream()
//                .map(ingredient -> {
//                    PhaseIngredient ingredient1 = new PhaseIngredient();
//                    ingredient1.setIngredientName(ingredient.getIngredientName());
//                    ingredient1.setIngreDescription(ingredient.getIngreDescription());
//                    ingredient1.setQuantity(ingredient.getQuantity());
//
//                    List<QualityControlParameter> qcParameters = findQcParametersForIngredient(finalExistingRecipe, ingredient.getIngredientName());
//
//                    ingredient1.setQcParameters(qcParameters);
//
//                    return ingredient1;
//                })
//                .collect(Collectors.toList());
//
//        existingRecipe = null;
//
//        return MessageModel.builder().processIngredient(ingredients).build();
//    }

//    private List<QualityControlParameter> findQcParametersForIngredient(Recipes existingRecipe, String ingredientId) {
//        Ingredients ingredients = existingRecipe.getIngredients();
//        if (ingredients == null) {
//            return Collections.emptyList();
//        }
//
//        List<ParentIngredient> activeIngredients = ingredients.getActive();
//        List<ParentIngredient> inactiveIngredients = ingredients.getInactive();
//
//        if (activeIngredients == null) activeIngredients = Collections.emptyList();
//        if (inactiveIngredients == null) inactiveIngredients = Collections.emptyList();
//
//        return Stream.concat(activeIngredients.stream(), inactiveIngredients.stream())
//                .filter(ing -> ing.getIngredientName().equals(ingredientId))
//                .flatMap(ing -> ing.getQcParameters().stream())
//                .collect(Collectors.toList());
//    }

    @Override
    public MessageModel getIngredientsWithVerification(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if (StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion())) {
            throw new BatchNoRecipeHeaderException(114);
        }

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();
        }

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();

        if (existingRecipe == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe ID not found", "E")).build();
        }

        if (existingRecipe.getPhases() == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("Phases not found", "E")).build();
        }

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("Phase ID not found", "E")).build();
        }

        if (phase.getIngredients() == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("Phase ingredients not found", "E")).build();
        }

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
                    phaseIngredient.setIngreDescription(parentIngredient.getIngreDescription());
                    phaseIngredient.setQuantity(parentIngredient.getQuantity());

                    List<QualityControlParameter> qcParameters = findQcParametersForIngredient(finalExistingRecipe, parentIngredient.getSequence());
                    phaseIngredient.setQcParameters(qcParameters);

                    return phaseIngredient;
                })
                .collect(Collectors.toList());

        existingRecipe = null;

        return MessageModel.builder().phaseIngredientList(phaseIngredients).build();
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
    public MessageModel getPhaseOperationInstructions(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();
        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty())
            return MessageModel.builder().messageDetails(new MessageDetails("phases not found" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null)
            return MessageModel.builder().messageDetails(new MessageDetails("phase Id not found" , "E")).build();

        if (phase.getOperations() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("phase Operations not found" , "E")).build();

        List<Operation> opInstructions = phase.getOperations().stream()
                .map(op -> {
                    Operation instruction = new Operation();
                    instruction.setOperationId(op.getOperationId());
                    instruction.setInstruction(op.getInstruction());
                    return instruction;
                })
                .collect(Collectors.toList());

        existingRecipe = null;

        return MessageModel.builder().ops(opInstructions).build();
    }

    @Override
    public MessageModel getParallelPhases(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();
        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty())
            return MessageModel.builder().messageDetails(new MessageDetails("phases not found" , "E")).build();

        List<Phase> phases = existingRecipe.getPhases().stream()
                .filter(Objects::nonNull)
                .filter(phase -> Boolean.TRUE.equals(phase.getParallel()))
                .collect(Collectors.toList());

        existingRecipe = null;

        return MessageModel.builder().phases(phases).build();
    }

    @Override
    public MessageModel getAnyOrderOperations(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();
        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty())
            return MessageModel.builder().messageDetails(new MessageDetails("phases not found" , "E")).build();

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null)
            return MessageModel.builder().messageDetails(new MessageDetails("phase Id not found" , "E")).build();
        if (phase.getOperations() == null || phase.getOperations().isEmpty()) {
            return MessageModel.builder().messageDetails(new MessageDetails("Operations not found for the phase", "E")).build();
        }

        List<Operation> ops = phase.getOperations().stream()
                .filter(op -> "AnyOrder".equalsIgnoreCase(op.getType()))
                .collect(Collectors.toList());

        existingRecipe = null;

        return MessageModel.builder().ops(ops).build();
    }

    @Override
    public MessageModel getUomForIngredient(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getIngredients() == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("Ingredient not found" , "E")).build();
        }

        PhaseIngredient ingredient = findUomIngredientById(request.getIngreSequence(), existingRecipe.getIngredients());

        existingRecipe = null;

        return MessageModel.builder().phaseIngredient(ingredient).build();
    }

    public PhaseIngredient findUomIngredientById(String ingreSequence, Ingredients ingredients) {
        if (ingredients == null || ingreSequence == null) {
            return null;
        }
        PhaseIngredient ingredient = new PhaseIngredient();
        List<ParentIngredient> allIngredients = new ArrayList<>();

        if (ingredients.getActive() != null) {
            allIngredients.addAll(ingredients.getActive());
        }
        if (ingredients.getInactive() != null) {
            allIngredients.addAll(ingredients.getInactive());
        }

        for (ParentIngredient ing : allIngredients) {
            if (ing.getSequence().equals(ingreSequence)) {
                ingredient.setUom(ing.getUom());
                ingredient.setIngredientId(ing.getIngredientId());

                return ingredient;
            }
        }
        return null;
    }


    @Override
    public MessageModel getIngredientVerificationStatus(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();
        }

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe ID not found", "E")).build();
        }

        if (existingRecipe.getIngredients() == null) {
            return MessageModel.builder().messageDetails(new MessageDetails("Ingredient not found", "E")).build();
        }

        List<QualityControlParameter> qcParameters = findIngredientById(existingRecipe, request.getIngreSequence());

        if (qcParameters == null || qcParameters.isEmpty()) {
            return MessageModel.builder().messageDetails(new MessageDetails("No QC parameters found for the ingredient", "E")).build();
        }

        boolean isVerified = qcParameters.stream()
                .allMatch(param -> param.getActualValue().equals(param.getExpectedValue()));

        String status = isVerified ? "Verified" : "Failed";

        return MessageModel.builder()
                .ingredientId(request.getIngredientId())
                .isVerified(status)
                .qcParameters(qcParameters)
                .build();

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

    @Override
    public MessageModel addPhaseOperation(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe batch header not found", "E"))
                    .build();
        }

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();

        if (existingRecipe == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe ID not found", "E"))
                    .build();
        }

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("No phases present in the recipe", "S"))
                    .build();
        }

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase ID not present", "E"))
                    .build();
        }
        if (phase.getOperations() == null) {
            phase.setOperations(new ArrayList<>());
        }
        List<Operation> requestOps = request.getOperations();

        if (requestOps == null || requestOps.isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase operations are empty", "E"))
                    .build();
        }

        for (Operation opRequest : requestOps) {
            boolean opExists = phase.getOperations().stream()
                    .anyMatch(s -> s.getSequence().equals(opRequest.getSequence()));

            if (opExists) {
                return MessageModel.builder()
                        .messageDetails(new MessageDetails("Operation ID " + opRequest.getOperationId() + " already exists", "E"))
                        .build();
            }
        }
        phase.getOperations().addAll(requestOps);

        batchNoRecipeHeader.setModifiedBy(request.getUser());
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
        batchNoRecRepository.save(batchNoRecipeHeader);

        return MessageModel.builder()
                .messageDetails(new MessageDetails("Operations added", "S"))
                .build();
    }

    @Override
    public MessageModel updatePhaseOperation(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe batch header not found", "E"))
                    .build();
        }

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe ID not found", "E"))
                    .build();
        }

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("No phases present in the recipe", "S"))
                    .build();
        }

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence() != null && p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase ID not found", "E"))
                    .build();
        }

        if (phase.getOperations() == null || phase.getOperations().isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("No operations present in the phase", "S"))
                    .build();
        }

        Operation opToUpdate = phase.getOperations().stream()
                .filter(op -> op.getSequence() != null && op.getSequence().equals(request.getOpSequence()))
                .findFirst()
                .orElse(null);

        if (opToUpdate == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Operation ID not found", "E"))
                    .build();
        }

        updatePhaseOperations(request, phase.getOperations(), request.getOperations());

        batchNoRecipeHeader.setModifiedBy(request.getUser());
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
        batchNoRecRepository.save(batchNoRecipeHeader);

        return MessageModel.builder()
                .messageDetails(new MessageDetails("Operation updated successfully", "S"))
                .build();

    }



    @Override
    public MessageModel deletePhaseOperation(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe batch header not found", "E"))
                    .build();
        }

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe ID not found", "E"))
                    .build();
        }

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phases are absent", "S"))
                    .build();
        }

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence() != null && p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase ID not found", "S"))
                    .build();
        }

        if (phase.getOperations() == null || phase.getOperations().isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase operations are absent", "S"))
                    .build();
        }

        Operation opToDelete = phase.getOperations().stream()
                .filter(op -> op.getSequence() != null && op.getSequence().equals(request.getOpSequence()))
                .findFirst()
                .orElse(null);

        if (opToDelete == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Operation ID not found", "S"))
                    .build();
        }

        phase.getOperations().remove(opToDelete);
        batchNoRecipeHeader.setModifiedBy(request.getUser());
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
        batchNoRecRepository.save(batchNoRecipeHeader);

        return MessageModel.builder()
                .messageDetails(new MessageDetails("Operation deleted successfully", "S"))
                .build();
    }

    @Override
    public MessageModel addPhaseIngredient(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe batch header not found", "E"))
                    .build();
        }

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe ID not found", "E"))
                    .build();
        }

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phases are empty; please create a phase first", "E"))
                    .build();
        }

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p.getSequence() != null && p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase ID not present", "E"))
                    .build();
        }

        Ingredients phaseIngredients = phase.getIngredients();
        Ingredients requestIngredients = request.getIngredients();

        if (requestIngredients == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase ingredients are empty", "E"))
                    .build();
        }

        if (phaseIngredients == null) {
            phaseIngredients = new Ingredients();
            phase.setIngredients(phaseIngredients);
        }

        List<ParentIngredient> phaseActive = Optional.ofNullable(phaseIngredients.getActive()).orElse(new ArrayList<>());
        List<ParentIngredient> phaseInactive = Optional.ofNullable(phaseIngredients.getInactive()).orElse(new ArrayList<>());

        List<ParentIngredient> requestActive = Optional.ofNullable(requestIngredients.getActive()).orElse(new ArrayList<>());
        List<ParentIngredient> requestInactive = Optional.ofNullable(requestIngredients.getInactive()).orElse(new ArrayList<>());

        boolean ingredientExists = Stream.concat(requestActive.stream(), requestInactive.stream())
                .anyMatch(requestIngredient -> {
                    if (requestIngredient == null || requestIngredient.getSequence() == null) {
                        return false;
                    }

                    boolean existsInActive = phaseActive.stream()
                            .anyMatch(active -> active.getSequence() != null &&
                                    active.getSequence().equals(requestIngredient.getSequence()));

                    boolean existsInInactive = phaseInactive.stream()
                            .anyMatch(inactive -> inactive.getSequence() != null &&
                                    inactive.getSequence().equals(requestIngredient.getSequence()));

                    return existsInActive || existsInInactive;
                });

        if (ingredientExists) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("An ingredient with the same ID already exists in the phase (active or inactive)", "E"))
                    .build();
        }

        phaseActive.addAll(requestActive);
        phaseInactive.addAll(requestInactive);

        phaseIngredients.setActive(phaseActive);
        phaseIngredients.setInactive(phaseInactive);

        batchNoRecipeHeader.setModifiedBy(request.getUser());
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
        batchNoRecRepository.save(batchNoRecipeHeader);

        return MessageModel.builder()
                .messageDetails(new MessageDetails("Ingredients added successfully", "S"))
                .build();

    }


    @Override
    public MessageModel updatePhaseIngredient(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe batch header not found", "E"))
                    .build();
        }

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe ID not found", "E"))
                    .build();
        }

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phases not present", "E"))
                    .build();
        }

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p != null && p.getSequence() != null && p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase ID not present", "E"))
                    .build();
        }

        if (phase.getIngredients() == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Ingredients not present in the phase", "E"))
                    .build();
        }

//        Ingredients existingIngredient = phase.getIngredients().stream()
//                .filter(ingredient -> ingredient != null && ingredient.getIngredientId() != null &&
//                        ingredient.getIngredientId().equals(request.getIngredientId()))
//                .findFirst()
//                .orElse(null);

//        ParentIngredient existingIngredient = phase.getIngredients().stream()
//                .flatMap(ingredients -> Stream.concat(
//                        ingredients.getActive() != null ? ingredients.getActive().stream() : Stream.empty(),
//                        ingredients.getInactive() != null ? ingredients.getInactive().stream() : Stream.empty()
//                )) // Flatten active and inactive lists
//                .filter(ingredient -> ingredient != null && ingredient.getSequence() != null &&
//                        ingredient.getSequence().equals(request.getIngreSequence()))
//                .findFirst()
//                .orElse(null);

        ParentIngredient existingIngredient = null;

        if ("active".equalsIgnoreCase(request.getIngredientType())) {
            existingIngredient = Optional.ofNullable(phase.getIngredients())
                    .map(Ingredients::getActive)
                    .orElse(new ArrayList<>())
                    .stream()
                    .filter(ingredient -> ingredient != null && ingredient.getSequence() != null &&
                            ingredient.getSequence().equals(request.getIngreSequence()))
                    .findFirst()
                    .orElse(null);

        } else if ("inactive".equalsIgnoreCase(request.getIngredientType())) {
            existingIngredient = Optional.ofNullable(phase.getIngredients())
                    .map(Ingredients::getInactive)
                    .orElse(new ArrayList<>())
                    .stream()
                    .filter(ingredient -> ingredient != null && ingredient.getSequence() != null &&
                            ingredient.getSequence().equals(request.getIngreSequence()))
                    .findFirst()
                    .orElse(null);
        }

        if (existingIngredient == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Ingredient ID not present", "E"))
                    .build();
        }

        if (request.getParentIngredient() == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase ingredients not provided in the request", "E"))
                    .build();
        }

        updateParentIngredient(existingIngredient, request.getParentIngredient());

        batchNoRecipeHeader.setModifiedBy(request.getUser());
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
        batchNoRecRepository.save(batchNoRecipeHeader);

        return MessageModel.builder()
                .messageDetails(new MessageDetails("Ingredient updated successfully", "S"))
                .build();

    }


//    private void updateIngredientDetails(ParentIngredient existingIngredient, ParentIngredient userIngredient) {
//
//        existingIngredient.setIngredientName(userIngredient.getIngredientName());
//        existingIngredient.setIngreDescription(userIngredient.getIngreDescription());
//        existingIngredient.setQuantity(userIngredient.getQuantity());
//        existingIngredient.setSequence(userIngredient.getSequence());
//        existingIngredient.setAssociatedOp(userIngredient.getAssociatedOp());
//        existingIngredient.setUom(userIngredient.getUom());
//    }

//    private void updateIngredientDetails(Recipes existingRecipe, ParentIngredient existingIngredient, ParentIngredient userIngredient) {
//        if (existingIngredient == null || userIngredient == null) {
//            throw new IllegalArgumentException("Ingredients cannot be null");
//        }
//
//        // Basic information
//        existingIngredient.setSequence(userIngredient.getSequence());
//        existingIngredient.setIngredientId(userIngredient.getIngredientId());
//        existingIngredient.setIngreDescription(userIngredient.getIngreDescription());
//        existingIngredient.setQuantity(userIngredient.getQuantity());
//        existingIngredient.setUom(userIngredient.getUom());
//        existingIngredient.setMaterialDescription(userIngredient.getMaterialDescription());
//        existingIngredient.setStorageLocation(userIngredient.getStorageLocation());
//        existingIngredient.setTolerance(userIngredient.getTolerance());
//        existingIngredient.setMaterialType(userIngredient.getMaterialType());
//        existingIngredient.setSupplierId(userIngredient.getSupplierId());
//        existingIngredient.setSourceLocation(userIngredient.getSourceLocation());
//        existingIngredient.setHandlingInstructions(userIngredient.getHandlingInstructions());
//        existingIngredient.setStorageInstructions(userIngredient.getStorageInstructions());
//        existingIngredient.setUnitCost(userIngredient.getUnitCost());
//        existingIngredient.setCurrency(userIngredient.getCurrency());
//        existingIngredient.setTotalCost(userIngredient.getTotalCost());
//        existingIngredient.setWasteQuantity(userIngredient.getWasteQuantity());
//        existingIngredient.setWasteUoM(userIngredient.getWasteUoM());
//        existingIngredient.setBatchNumber(userIngredient.getBatchNumber());
//
//        // Dates
//        existingIngredient.setExpiryDate(userIngredient.getExpiryDate());
//        existingIngredient.setManufactureDate(userIngredient.getManufactureDate());
//
//        // Hazardous and ByProduct details
//        existingIngredient.setHazardous(userIngredient.getHazardous());
//        updateAdjustments(existingRecipe, existingop.getAdjustments(), opRequest.getAdjustments());
//        updateCriticalControlPoints(existingop, existingop.getCriticalControlPoints(), opRequest.getCriticalControlPoints());
//        updateByProducts(existingop, existingop.getByProducts(), opRequest.getByProducts());
//
//
//        existingIngredient.setByProduct(userIngredient.getByProduct());
//
//        // Lists
//        if (userIngredient.getQcParameters() != null) {
//            existingIngredient.setQcParameters(new ArrayList<>(userIngredient.getQcParameters()));
//        }
//        if (userIngredient.getAlternateIngredients() != null) {
//            existingIngredient.setAlternateIngredients(new ArrayList<>(userIngredient.getAlternateIngredients()));
//        }
//    }




    @Override
    public MessageModel deletePhaseIngredient(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe not found", "E"))
                    .build();

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phases not found", "E"))
                    .build();
        }

        Phase phase = existingRecipe.getPhases().stream()
                .filter(p -> p != null && p.getSequence() != null && p.getSequence().equals(request.getPhaseSequence()))
                .findFirst()
                .orElse(null);

        if (phase == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase not found", "E"))
                    .build();
        }

        if (phase.getIngredients() == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Ingredients not found", "E"))
                    .build();
        }

//        Ingredients ingredientToDelete = phase.getIngredients().stream()
//                .filter(ingredient -> ingredient != null && ingredient.getIngredientId() != null &&
//                        ingredient.getIngredientId().equals(request.getIngredientId()))
//                .findFirst()
//                .orElse(null);

//        ParentIngredient ingredientToDelete = phase.getIngredients().stream()
//                .flatMap(ingredients -> Stream.concat(
//                        ingredients.getActive() != null ? ingredients.getActive().stream() : Stream.empty(),
//                        ingredients.getInactive() != null ? ingredients.getInactive().stream() : Stream.empty()
//                )) // Flatten active and inactive lists
//                .filter(ingredient -> ingredient != null && ingredient.getSequence() != null &&
//                        ingredient.getSequence().equals(request.getIngreSequence()))
//                .findFirst()
//                .orElse(null);
//
//
//        if (ingredientToDelete == null) {
//            return MessageModel.builder()
//                    .messageDetails(new MessageDetails("Ingredient ID not found", "E"))
//                    .build();
//        }

        Ingredients ingredients = phase.getIngredients();

        if (ingredients.getActive() != null) {
            ingredients.getActive().removeIf(activeIngredient ->
                    activeIngredient != null &&
                            activeIngredient.getSequence() != null &&
                            activeIngredient.getSequence().equals(request.getIngreSequence()));
        }

        if (ingredients.getInactive() != null) {
            ingredients.getInactive().removeIf(inactiveIngredient ->
                    inactiveIngredient != null &&
                            inactiveIngredient.getSequence() != null &&
                            inactiveIngredient.getSequence().equals(request.getIngreSequence()));
        }

        batchNoRecipeHeader.setModifiedBy(request.getUser());
        batchNoRecipeHeader.setModifiedDatetime(LocalDateTime.now());
        batchNoRecRepository.save(batchNoRecipeHeader);

        return MessageModel.builder()
                .messageDetails(new MessageDetails("Phase ingredient deleted successfully", "S"))
                .build();

    }

    @Override
    public MessageModel getPhaseOperationDataCollection(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {

        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if(existingRecipe == null)
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe ID not found", "E"))
                    .build();

        return getDataCollectionRecords(existingRecipe, request, request.isNeedMasterData());

    }

//    @Override
//    public MessageModel getBROpDataWithMasterCollection(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
//
//        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
//            throw new BatchNoRecipeHeaderException(114);
//
//        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
//        if (batchNoRecipeHeader == null)
//            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();
//
//        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
//        if(existingRecipe == null)
//            return MessageModel.builder()
//                    .messageDetails(new MessageDetails("Recipe ID not found", "E"))
//                    .build();
//
//        List<DataCollection> dataCollectionRecords = getDataCollectionRecords(existingRecipe, request, true);
//
//        if (dataCollectionRecords != null && !dataCollectionRecords.isEmpty()) {
//            return MessageModel.builder()
//                    .dataCollection(dataCollectionRecords)
//                    .build();
//
//        } else {
//            return MessageModel.builder()
//                    .messageDetails(new MessageDetails("Phase datacollection not found", "S"))
//                    .build();
//        }
//    }

    @Override
    public MessageModel getPhaseCcpDataCollection(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();
        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if(existingRecipe == null)
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe ID not found", "E"))
                    .build();

        List<List<DataCollection>> ccpRecords = findCcpDataCollections(existingRecipe, request);

        if (ccpRecords != null && !ccpRecords.isEmpty()) {
            return MessageModel.builder()
                    .dataCollections(ccpRecords)
                    .build();

        } else {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase dataCollection not found", "E"))
                    .build();
        }
    }

    @Override
    public MessageModel getBatchRecipeFirstPhaseOp(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);

        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();
        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if(existingRecipe == null)
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Recipe not found", "E"))
                    .build();

        List<Phase> phases = existingRecipe.getPhases();
        if (phases == null || phases.isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phases not found", "E"))
                    .build();
        }

        Phase firstPhase = phases.get(0);
        if (firstPhase == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("First phase not found", "E"))
                    .build();
        }

        // Get the operations of the first phase
        List<Operation> operations = firstPhase.getOperations();
        if (operations == null || operations.isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase operations not found", "E"))
                    .build();
        }

        // Get the first operation
        Operation operation = operations.get(0);
        if (operation == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("First phase operation not found", "E"))
                    .build();
        }

        return MessageModel.builder().op(operation).build();
    }

    @Override
    public MessageModel getBatchRecipeOpIngredient(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("phases not found" , "E")).build();

        Phase phase = getValidatedPhase(existingRecipe, request);
        if (phase == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase not found", "E"))
                    .build();
        }

        Operation currentOp = getValidatedOperation(phase, request);
        if (currentOp == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Current operation not found", "E"))
                    .build();
        }

        if (currentOp.getOpIngredients() == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("No operation ingredient found", "E"))
                    .build();
        } else {
            return MessageModel.builder().processIngredient(currentOp.getOpIngredients()).build();
        }

    }

    public List<List<DataCollection>> findCcpDataCollections(Recipes existingRecipe, BatchNoRecipeHeaderReq request) {
        if (existingRecipe == null || existingRecipe.getPhases() == null) {
            throw new BatchNoRecipeHeaderException(149);
        }
        if (request == null || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getOpSequence())) {
            throw new BatchNoRecipeHeaderException(151);
        }

        List<List<DataCollection>> ccpDataCollections = new ArrayList<>();

        for (Phase phase : existingRecipe.getPhases()) {
            if (phase == null || StringUtils.isEmpty(phase.getSequence())) {
                continue;
            }

            if (phase.getSequence().equals(request.getPhaseSequence())) {
                List<Operation> operations = phase.getOperations();

                if (operations == null || operations.isEmpty()) {
                    continue;
                }

                for (Operation op : operations) {
                    if (op == null || StringUtils.isEmpty(op.getSequence())) {
                        continue;
                    }

                    if (op.getSequence().equals(request.getOpSequence()) && Boolean.TRUE.equals(op.getCcp())) {
                        if (op.getDataCollection() != null) {
                            ccpDataCollections.add(op.getDataCollection());
                        }
                    }
                }
            }
        }

        return ccpDataCollections;
    }


    public MessageModel getDataCollectionRecords(Recipes existingRecipe, BatchNoRecipeHeaderReq request, boolean needMasterDC) {
        if (existingRecipe == null || existingRecipe.getPhases() == null) {
            throw new BatchNoRecipeHeaderException(149);
        }
        if (request == null || StringUtils.isEmpty(request.getPhaseSequence()) || StringUtils.isEmpty(request.getOpSequence())) {
            throw new BatchNoRecipeHeaderException(151);
        }
        List<DataCollection> dataCollections = new ArrayList<>();
        DataCollectionList dcList = new DataCollectionList();
        for (Phase phase : existingRecipe.getPhases()) {
            if (phase == null || StringUtils.isEmpty(phase.getSequence())) {
                continue;
            }

            if (phase.getSequence().equals(request.getPhaseSequence())) {
                List<Operation> operations = phase.getOperations();

                if (operations == null || operations.isEmpty()) {
                    continue;
                }

                for (Operation op : operations) {
                    if (op == null || StringUtils.isEmpty(op.getSequence())) {
                        continue;
                    }
                    dataCollections = op.getDataCollection();

                    if (op.getSequence().equals(request.getOpSequence())) {
                        if(needMasterDC) {
                            dcList = getMasterDataCollection(dataCollections, request);
                        }

                    }
                }
            }
        }
        return MessageModel.builder()
                .dataCollection(dataCollections)
                .masterDcList(dcList)
                .build();

    }

    private DataCollectionList getMasterDataCollection(List<DataCollection> dataCollections, BatchNoRecipeHeaderReq request){
//        Map<String,String> recipeDc = new HashMap<>();

//        for(DataCollection recipeDC : dataCollections){
//            recipeDc.put(recipeDC.getDataPointId(), recipeDC.getDataPointVersion());
//        }

        DataCollectionRequest dcRequest = DataCollectionRequest.builder()
                .site(request.getSite())
                .batchNo(request.getBatchNo())
                .orderNo(request.getOrderNo())
                .material(request.getMaterial())
                .materialVersion(request.getMaterialVersion())
                .phaseId(request.getPhaseId())
                .operationId(request.getOperationId())
//                .recipeDc(recipeDc)
                .build();


        DataCollectionList dataCollectionList = new DataCollectionList();
        try {
            dataCollectionList = webClientBuilder.build()
                    .post()
                    .uri(retrieveDCForBatchRecipeUrl)
                    .bodyValue(dcRequest)
                    .retrieve()
                    .bodyToMono(DataCollectionList.class)
                    .block();

        } catch (BatchNoRecipeHeaderException e){
            throw new BatchNoRecipeHeaderException(155);
        }
        return dataCollectionList;
    }
    @Override
    public Map<String, Object> getBatchRecipeFirstPhaseFirstOp(String batchNo, String orderNo, String material, String materialVersion) throws Exception {
        BatchNoRecipeHeader batchNoRecipeHeader = batchNoRecRepository.findByBatchNoAndOrderNoAndMaterialAndMaterialVersionAndActive(batchNo, orderNo, material, materialVersion, 1);

        if (batchNoRecipeHeader == null || batchNoRecipeHeader.getRecipe() == null) {
            throw new Exception("Recipe not found for the given batchNo, orderNo, and material.");
        }

        Recipes recipe = batchNoRecipeHeader.getRecipe();
        if (recipe.getPhases() == null || recipe.getPhases().isEmpty()) {
            throw new Exception("No phases found in the recipe.");
        }

        Phase firstPhase = recipe.getPhases().stream()
                .filter(Phase::isEntryPhase)
                .findFirst()
                .orElseThrow(() -> new Exception("No entry phase found in the recipe."));

        if (firstPhase.getOperations() == null || firstPhase.getOperations().isEmpty()) {
            throw new Exception("No operations found in the first phase of the recipe.");
        }

        Operation firstOperation = firstPhase.getOperations().stream()
                .filter(Operation::isEntryOperation)
                .findFirst()
                .orElseThrow(() -> new Exception("No entry operation found in the first phase."));

        Map<String, Object> result = new HashMap<>();
        result.put("firstPhase", firstPhase);
        result.put("firstOperation", firstOperation);

        return result;
    }

    @Override
    public MessageModel getNextOperation(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null)
            return MessageModel.builder().messageDetails(new MessageDetails("phases not found" , "E")).build();

        Phase phase = getValidatedPhase(existingRecipe, request);
        if (phase == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase ID not found", "E"))
                    .build();
        }

        Operation currentOp = getValidatedOperation(phase, request);
        if (currentOp == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Current operation not found", "E"))
                    .build();
        }

        if (currentOp.getNextOperations().equals("00") || currentOp.isLastOperationAtPhase()) {
            return handleLastOperationInPhase(existingRecipe, phase);
        } else {
            return getNextOperationInPhase(phase, currentOp);
        }
    }

    private Phase getValidatedPhase(Recipes existingRecipe, BatchNoRecipeHeaderReq recipeRequest) {
        return existingRecipe.getPhases().stream()
                .filter(p -> p.getPhaseId().equals(recipeRequest.getPhaseId()))
                .findFirst()
                .orElse(null);
    }

    private Operation getValidatedOperation(Phase phase, BatchNoRecipeHeaderReq recipeRequest) {
        return phase.getOperations().stream()
                .filter(op -> op.getOperationId().equals(recipeRequest.getOperationId()))
                .findFirst()
                .orElse(null);
    }

    private MessageModel handleLastOperationInPhase(Recipes existingRecipe, Phase phase) {
        if (phase.getNextPhase().equals("00") || phase.isExitPhase()) {
            return MessageModel.builder()
                    .finalValue(true)
                    .messageDetails(new MessageDetails("This is the last phase's last operation", "E"))
                    .build();
        } else {
            Phase nextPhase = existingRecipe.getPhases().stream()
                    .filter(p -> p.getSequence().equals(phase.getNextPhase()))
                    .findFirst()
                    .orElse(null);

            if (nextPhase == null || nextPhase.getOperations() == null || nextPhase.getOperations().isEmpty()) {
                return MessageModel.builder()
                        .messageDetails(new MessageDetails("Next phase or its operations not found", "E"))
                        .build();
            }

            Operation nextOp = nextPhase.getOperations().stream()
                    .filter(Operation::isEntryOperation)
                    .findFirst()
                    .orElse(null);

            if (nextOp == null) {
                return MessageModel.builder()
                        .messageDetails(new MessageDetails("Entry operation not found in the next phase", "E"))
                        .build();
            }

            return buildOperationResponse(nextPhase, nextOp);
        }
    }

    private MessageModel getNextOperationInPhase(Phase phase, Operation currentOp) {
        Operation nextOp = phase.getOperations().stream()
                .filter(op -> op.getSequence().equals(currentOp.getNextOperations()))
                .findFirst()
                .orElse(null);

        if (nextOp == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Next operation not found", "E"))
                    .build();
        }

        return buildOperationResponse(phase, nextOp);
    }

    private MessageModel buildOperationResponse(Phase phase, Operation operation) {
        Map<String, Object> response = new HashMap<>();
        response.put("operationId", operation.getOperationId());
        response.put("sequence", operation.getSequence());
        response.put("instruction", operation.getInstruction());
        response.put("expectedCycleTime", operation.getExpectedCycleTime());
        response.put("phaseId", phase.getPhaseId());

        return MessageModel.builder()
                .resultBody(response)
                .build();
    }


    @Override
    public MessageModel getNextphase(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        if(StringUtils.isEmpty(request.getBatchNo()) || StringUtils.isEmpty(request.getOrderNo()) || StringUtils.isEmpty(request.getMaterial()) || StringUtils.isEmpty(request.getMaterialVersion()))
            throw new BatchNoRecipeHeaderException(114);

        BatchNoRecipeHeader batchNoRecipeHeader = getBatchNoRecipeHeader(request);
        if (batchNoRecipeHeader == null)
            return MessageModel.builder().messageDetails(new MessageDetails("Recipe batch header not found", "E")).build();

        Recipes existingRecipe = batchNoRecipeHeader.getRecipe();
        if (existingRecipe == null)
            return MessageModel.builder() .messageDetails(new MessageDetails("Recipe ID not found", "E")).build();

        if (existingRecipe.getPhases() == null || existingRecipe.getPhases().isEmpty()) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phases not found", "E"))
                    .build();
        }

        Phase currentPhase = getValidatedPhase(existingRecipe, request);
        if (currentPhase == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Phase ID not found", "E"))
                    .build();
        }

        Phase nextPhase = getNextPhaseFromSequence(existingRecipe, currentPhase);
        if (nextPhase == null) {
            return MessageModel.builder()
                    .messageDetails(new MessageDetails("Next phase not found", "E"))
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

    private MessageModel buildPhaseResponse(Phase nextPhase) {
        Map<String, Object> response = new HashMap<>();
        response.put("phaseId", nextPhase.getPhaseId());
        response.put("sequence", nextPhase.getSequence());
        response.put("expectedCycleTime", nextPhase.getExpectedCycleTime());

        return MessageModel.builder()
                .resultBody(response)
                .build();
    }
    @Override
    public BatchNoRecipeHeader getBySiteAndBatchNo(String site, String batchNo) {
        return batchNoRecRepository.findBySiteAndBatchNoAndActive(site, batchNo, 1);
    }

    @Override
    public BatchNoRecipeHeader getBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersion(String site, String batchNo, String orderNo, String material, String materialVersion) {
    BatchNoRecipeHeader batchNoRecipeHeader;
        if (orderNo != null) {
            batchNoRecipeHeader = batchNoRecRepository.findBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersionAndActive(site, batchNo, orderNo, material, materialVersion, 1);
    } else {
            batchNoRecipeHeader = batchNoRecRepository.findBySiteAndBatchNoAndMaterialAndMaterialVersionAndActive(site, batchNo, material,materialVersion ,1);
    }
        return batchNoRecipeHeader;
    }

    @Override
    public MessageModel getByBatchRecipeByFilters(BatchNoRecipeHeaderReq request) throws BatchNoRecipeHeaderException {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("site").is(request.getSite()));
            query.addCriteria(Criteria.where("active").is(1));
            if (request.getPhaseId() != null && !request.getPhaseId().isEmpty()) {
                query.addCriteria(Criteria.where("recipe.phases.phaseId").is(request.getPhaseId()));
            }

            if (request.getOperationId() != null && !request.getOperationId().isEmpty()) {
                query.addCriteria(Criteria.where("recipe.phases.operations.operationId").is(request.getOperationId()));
            }

            List<BatchNoRecipeHeader> batchHeaders = mongoTemplate.find(query, BatchNoRecipeHeader.class);

            List<BatchRecipeResponse> responseList = batchHeaders.stream().map(batchHeader -> {
                BatchRecipeResponse response = new BatchRecipeResponse();
                response.setBatchNo(batchHeader.getBatchNo());
                response.setItem(batchHeader.getMaterial());
                response.setItemVersion(batchHeader.getMaterialVersion());
                response.setRecipe(batchHeader.getRecipe().getRecipeId());
                response.setRecipeVersion(batchHeader.getRecipe().getVersion());
                response.setQuantity(BigDecimal.valueOf(batchHeader.getBatchQty()));
                response.setProcessOrder(batchHeader.getOrderNo());
                response.setStatus("In Queue"); // Default status
                return response;
            }).collect(Collectors.toList());

            MessageModel response = new MessageModel();
            response.setBatchNos(responseList);
            return response;

        } catch (Exception e) {
            throw new BatchNoRecipeHeaderException(173, e.getMessage());
        }
    }
    @Override
    public List<BatchNoRecipeHeader> getBatchRecipeBySiteAndBatchAndOrder(BatchNoRecipeHeaderReq request) throws Exception {
        List<BatchNoRecipeHeader> batchNoRecipeHeader = new ArrayList<>();

        if (StringUtils.isNotBlank(request.getBatchNo()) && StringUtils.isNotBlank(request.getOrderNo())) {
            batchNoRecipeHeader = batchNoRecRepository.findBySiteAndBatchNoAndOrderNo(request.getSite(), request.getBatchNo(), request.getOrderNo());
        }
        else if(StringUtils.isNotBlank(request.getBatchNo())) {
            batchNoRecipeHeader = batchNoRecRepository.findBySiteAndBatchNo(request.getSite(), request.getBatchNo());
        }
        else if (StringUtils.isNotBlank(request.getOrderNo())) {
            batchNoRecipeHeader = batchNoRecRepository.findBySiteAndOrderNo(request.getSite(), request.getOrderNo());
        }
//        else {
//            batchNoRecipeHeader = batchNoRecRepository.findBySite(request.getSite());
//        }
        return batchNoRecipeHeader;
    }

}