package com.rits.licencevalidationservice.repository;

import com.rits.licencevalidationservice.dto.NCCodeResponse;
import com.rits.licencevalidationservice.model.NCCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NCCodeRepository extends MongoRepository<NCCode,String> {
    long countByNcCodeAndSiteAndActive(String ncCode, String site, int active);

    NCCode findByNcCodeAndActiveAndSite(String ncCode, int active, String site);

    List<NCCodeResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    List<NCCodeResponse> findByNcCodeContainingIgnoreCaseAndSiteAndActive(String ncCode, String site, int active);

    boolean existsByNcCodeAndSiteAndActive(String ncCode, String site, int active);

    NCCode findByNcCodeAndActive(String ncCode, int active);

    List<NCCode> findBySiteAndNcCodeAndActive(String site, String ncCode, int active);

    List<NCCodeResponse> findBySiteAndActive(String site, int active);

    List<NCCode> findByActiveAndSite(int i, String site);

    NCCode  findByNcCodeAndSiteAndActive(String ncCode, String site, int active);
}
