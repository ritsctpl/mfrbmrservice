package com.rits.bomservice.repository;

import com.rits.bomservice.dto.BomResponse;
import com.rits.bomservice.model.Bom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BomRepository extends MongoRepository<Bom, String> {
    List<BomResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    List<BomResponse> findByActiveAndSiteAndBomContainingIgnoreCase(int active, String site, String bom);

    Bom findByActiveAndSiteAndBomAndRevision(int active, String site, String bom, String revision);


    boolean existsByActiveAndSiteAndBomAndRevision(int active, String site, String bom, String revision);

    List<Bom> findByActiveAndSiteAndBom(int active, String site, String bom);


    Bom findByActiveAndSiteAndBomAndCurrentVersion(int active, String site, String bom, boolean version);

    List<BomResponse> findByActiveAndSiteAndBomComponentList_ComponentAndBomComponentList_ComponentVersion(int active, String site, String component, String version);

    boolean existsByActiveAndSiteAndBomComponentList_ComponentAndBomComponentList_ComponentVersion(int active, String site, String component, String version);

    List<BomResponse> findBySiteAndActiveAndBomType(String site, int i, String bomType);

    Boolean existsByActiveAndSiteAndBomAndCurrentVersion(int i, String site, String bom, boolean b);
}
