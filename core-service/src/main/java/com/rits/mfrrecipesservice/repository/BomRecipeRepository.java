package com.rits.mfrrecipesservice.repository;

import com.rits.mfrrecipesservice.dto.BomPhaseSeperation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BomRecipeRepository extends MongoRepository<BomPhaseSeperation,String> {
    BomPhaseSeperation findByHandleAndActiveAndSite(String handle, int active, String site);
    boolean existsByHandleAndSiteAndActive(String handle,  String site, int active);
}
