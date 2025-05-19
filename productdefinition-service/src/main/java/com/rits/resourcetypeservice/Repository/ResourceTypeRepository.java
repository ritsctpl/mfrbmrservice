package com.rits.resourcetypeservice.Repository;

import com.rits.resourcetypeservice.dto.AvailableResourceType;
import com.rits.resourcetypeservice.dto.ResourceTypeList;
import com.rits.resourcetypeservice.Model.ResourceType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ResourceTypeRepository extends MongoRepository<ResourceType, String> {
    List<ResourceTypeList> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active,String site);
    List<ResourceTypeList> findByActiveAndSiteOrderByCreatedDateTimeDesc(int active,String site);

    ResourceType findByResourceTypeAndSiteAndActiveEquals(String resourceType,String site,int active);

    boolean existsByResourceTypeAndSiteAndActiveEquals(String resourceType,String site,int active);

    List<AvailableResourceType> findByActiveAndSite(int active, String site);

    List<ResourceTypeList> findBySiteAndResourceTypeContainingIgnoreCaseAndActiveEquals(String site,String resourceType,int active);
    ResourceType findByResourceMemberListResourceAndSiteAndActive(String resource, String site, int active);

    List<ResourceType> findBySiteAndActiveEquals(String site, int i);
}
