package com.rits.toolgroupservice.repository;

import com.rits.toolgroupservice.dto.ToolGroupListResponse;
import com.rits.toolgroupservice.model.ToolGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ToolGroupRepository extends MongoRepository<ToolGroup,String> {
    long countByToolGroupAndSiteAndActive(String toolGroup, String site, int active);

    ToolGroup findByToolGroupAndSiteAndActive(String toolGroup, String site, int active);

    ToolGroup findByToolGroup(String toolGroup);

    boolean existsByToolGroupAndSiteAndActive(String toolGroup, String site, int active);

    ToolGroup findByToolGroupAndActive(String toolGroup, int active);

    List<ToolGroup> findBySiteAndActive(String site, int active);

    List<ToolGroupListResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    List<ToolGroupListResponse> findByToolGroupContainingIgnoreCaseAndSiteAndActive(String toolGroup, String site, int active);

    ToolGroup findByToolGroupAndActiveAndSite(String toolGroup, int active, String site);
}
