package com.rits.toolnumberservice.repository;

import com.rits.toolnumberservice.dto.ToolNumberResponse;
import com.rits.toolnumberservice.model.ToolNumber;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ToolNumberRepository extends MongoRepository<ToolNumber,String> {
    long countByToolNumberAndSiteAndActive(String toolNumber, String site, int active);

    ToolNumber findByToolNumberAndActive(String toolNumber, int active);

    List<ToolNumberResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    List<ToolNumberResponse> findByToolNumberContainingIgnoreCaseAndSiteAndActive(String toolNumber, String site, int active);

    ToolNumber findByToolNumberAndSiteAndActive(String toolNumber, String site, int active);

    boolean existsByToolNumberAndSiteAndActive(String toolNumber, String site, int active);

    ToolNumber findByToolNumberAndActiveAndSite(String toolNumber,  int active,String site);

    List<ToolNumber> findBySiteAndActiveAndToolGroup(String site, int i, String toolGroup);

    List<ToolNumberResponse> findByActiveAndSiteAndStatus(int i, String site, String enabled);
}
