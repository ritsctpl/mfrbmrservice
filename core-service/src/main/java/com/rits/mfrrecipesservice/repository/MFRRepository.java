package com.rits.mfrrecipesservice.repository;

import com.rits.mfrrecipesservice.dto.MFRResponse;
import com.rits.mfrrecipesservice.dto.MfrRecipes;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MFRRepository extends MongoRepository<MfrRecipes,String> {
    boolean existsByMfrNoAndVersionAndActiveAndSite(String mfrNo, String version, int active, String site);
    boolean existsByMfrNoAndActiveAndSite(String mfrNo, int active, String site);
    MfrRecipes findByMfrNoAndVersionAndActive(String mfrNo, String version, int active);
    List<MFRResponse> findByActiveAndSiteOrderByCreatedDateTimeDesc(int i, String site);
    MfrRecipes findByActiveAndSiteAndMfrNoAndVersionAndType(int active, String site, String mfrNo, String version, String type);
    MfrRecipes findByMfrNoAndVersionAndSiteAndActive(String mfrNo, String version, String site,int active);
}
