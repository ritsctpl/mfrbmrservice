package com.rits.mfrrecipesservice.repository;

import com.rits.mfrrecipesservice.dto.CriticalControlPoints;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CriticalControlsRepository extends MongoRepository<CriticalControlPoints,String> {
    CriticalControlPoints findByHandleAndActiveAndSite(String handle, int active, String site);
    boolean existsByHandleAndSiteAndActive(String handle, String site, int active);
}
