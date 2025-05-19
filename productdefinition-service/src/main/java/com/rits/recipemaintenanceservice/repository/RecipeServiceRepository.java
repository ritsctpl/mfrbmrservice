package com.rits.recipemaintenanceservice.repository;

import com.rits.recipemaintenanceservice.dto.*;
import com.rits.recipemaintenanceservice.model.Recipes;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RecipeServiceRepository extends MongoRepository<Recipes, String> {
    boolean existsBySiteAndRecipeIdAndVersionAndActive(String site, String RecipeId, String version, int active);
    Recipes findBySiteAndRecipeIdAndVersionAndActive(String site, String RecipeId, String version, int active);
    Recipes findBySiteAndRecipeIdAndCurrentVersionAndActive(String site, String RecipeId, boolean currentVersion, int active);
    boolean existsBySiteAndRecipeIdAndActiveAndRecipeIdAndVersionAndBatchSize(String site, String recipeId, int active, String RecipeId, String version, String batchSize);
    Recipes findBySiteAndActiveAndRecipeIdAndVersion(String site, int active, String RecipeId, String version);
    List<RecipeRequest> findBySiteAndActiveOrderByCreatedDateDesc(String site, int active);
    List<RecipeRequest> findTop50BySiteAndActiveOrderByCreatedDateDesc(String site, int active);

    boolean existsByRecipeIdAndVersionAndSiteAndActiveEquals(String RecipeId, String version, String site, int i);

    boolean existsByRecipeIdAndCurrentVersionAndSiteAndActive(String RecipeId, boolean b, String site, int i);

    List<RecipeRequest> findBySiteAndRecipeIdAndActive(String site, String recipeId, int i);
//
//    List<RecipeResponse> findTop50BySiteOrderByCreatedOnDesc(String site);
}
