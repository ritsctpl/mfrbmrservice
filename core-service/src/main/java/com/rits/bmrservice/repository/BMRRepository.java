package com.rits.bmrservice.repository;

import com.rits.bmrservice.dto.BMRResponse;
import com.rits.bmrservice.dto.BMRResponseList;
import com.rits.bmrservice.dto.BmrRecipes;
import com.rits.mfrrecipesservice.dto.MFRResponse;
import com.rits.mfrrecipesservice.dto.MfrRecipes;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BMRRepository extends MongoRepository<BmrRecipes,String> {
    boolean existsByBmrNoAndVersionAndActiveAndSite(String bmrNo, String version, int active, String site);

    BmrRecipes findByBmrNoAndVersionAndActiveAndSite(String bmrNo, String version, int active, String site);

    List<BMRResponse> findByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    boolean existsByBmrNoAndActiveAndSite(String bmrNo, int active, String site);
}
