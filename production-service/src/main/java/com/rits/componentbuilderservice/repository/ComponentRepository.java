package com.rits.componentbuilderservice.repository;

import com.rits.componentbuilderservice.dto.ComponentResponse;
import com.rits.componentbuilderservice.model.Component;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ComponentRepository extends MongoRepository<Component, String> {
    Boolean existsByHandleAndSiteAndActiveEquals(String handle, String site, int i);

    Component findByHandleAndSiteAndActiveEquals(String handle, String site, int i);

    List<ComponentResponse> findBySiteAndComponentLabelContainingIgnoreCaseAndActiveEquals(String site, String componentLabel, int active);

    List<ComponentResponse> findTop50BySiteAndActiveOrderByCreatedDateTimeDesc(String site, int active);

    // Custom query methods can be defined here if needed
    // For example, find by site or componentId
    // List<Component> findBySite(String site);
    // List<Component> findByComponentId(String componentId);
}
