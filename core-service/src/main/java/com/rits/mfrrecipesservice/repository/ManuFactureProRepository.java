package com.rits.mfrrecipesservice.repository;

import com.rits.mfrrecipesservice.dto.BomPhaseSeperation;
import com.rits.mfrrecipesservice.dto.ManufacturingProcedure;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ManuFactureProRepository extends MongoRepository<ManufacturingProcedure,String> {
    ManufacturingProcedure findByHandleAndActiveAndSite(String handle, int active, String site);
    boolean existsByHandleAndSiteAndActive(String handle, String site, int active);
}
