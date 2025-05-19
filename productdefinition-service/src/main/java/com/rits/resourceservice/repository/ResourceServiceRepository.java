package com.rits.resourceservice.repository;

import com.rits.resourceservice.dto.ResourceListResponse;
import com.rits.resourceservice.model.Resource;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ResourceServiceRepository extends MongoRepository<Resource, String> {

    List<ResourceListResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    Resource findByResourceAndSiteAndActiveEquals(String resource, String site, int active);

    List<ResourceListResponse> findByActiveAndSiteAndResourceContainingIgnoreCase(int active, String site, String resource);

    boolean existsByResourceAndSiteAndActiveEquals(String resource, String site, int active);

    List<ResourceListResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeAsc(int active, String site);

    boolean existsBySiteAndHandleAndActive(String site, String resource, int i);

    List<ResourceListResponse> findBySiteAndActive(String site, int i);

    Resource findBySiteAndActiveAndResource(String site, int active, String resource);

    Resource findByErpEquipmentNumberAndSiteAndActiveEquals(String erpEquipmentNumber, String site, int active);
    List<ResourceListResponse> findByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

}
