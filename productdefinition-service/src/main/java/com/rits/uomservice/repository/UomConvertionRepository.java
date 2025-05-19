package com.rits.uomservice.repository;

import com.rits.uomservice.model.UomConvertionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UomConvertionRepository extends MongoRepository<UomConvertionEntity, String> {
    public UomConvertionEntity findByBaseUnitAndConversionUnitAndActiveAndSite(String baseUnit, String convertionUnit, int active, String site);
    public List<UomConvertionEntity> findByActiveAndSiteAndBaseUnitContainingIgnoreCase(int active, String site, String baseUnit);
    public List<UomConvertionEntity> findByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);
    public List<UomConvertionEntity> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);
    public UomConvertionEntity findByBaseUnitAndConversionUnitAndMaterialAndMaterialVersionAndActiveAndSite(String baseUnit, String conversionUnit, String material, String materialVersion, int active, String site);
}
