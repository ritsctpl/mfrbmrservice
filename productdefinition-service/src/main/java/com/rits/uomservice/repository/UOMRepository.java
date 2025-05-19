package com.rits.uomservice.repository;

import com.rits.uomservice.dto.UOMResponse;
import com.rits.uomservice.model.UOMEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UOMRepository extends JpaRepository<UOMEntity, Integer> {


    boolean existsByActiveAndSiteAndUomCode(int active,String site, String uomCode);

    UOMEntity findBySiteAndId(String site, Integer id);

    List<UOMResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(Integer active, String site);

    List<UOMResponse> findByActiveAndSiteOrderByCreatedDateTimeDesc(Integer active, String site);


    UOMEntity findByIdAndSiteAndActiveEquals(Integer id, String site, Integer active);

    boolean existsBySiteAndIdAndActive(String site,Integer id,Integer active);
}