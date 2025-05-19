package com.rits.itemgroupservice.repository;

import com.rits.itemgroupservice.dto.GroupNameResponse;
import com.rits.itemgroupservice.model.ItemGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ItemGroupRepository extends MongoRepository<ItemGroup, String> {

    long countByItemGroupAndSiteAndActive(String itemGroup, String site, int active);

    List<GroupNameResponse> findTop50BySiteAndActiveOrderByCreatedDateTimeDesc(String site, int active);

    List<GroupNameResponse> findByItemGroupContainingIgnoreCaseAndSiteAndActive(String itemGroup, String site, int active);

    ItemGroup findByItemGroupAndSiteAndActive(String itemGroup, String site, int active);

    boolean existsByItemGroupAndSiteAndActive(String itemGroup, String site, int active);

    List<ItemGroup> findBySiteAndItemGroupAndActive(String site, String itemGroup, int active);

    ItemGroup findByItemGroupAndActive(String itemGroup, int active);

    List<ItemGroup> findBySiteAndActive(String site, int active);
}
