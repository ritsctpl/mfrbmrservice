package com.rits.itemservice.repository;

import com.rits.itemservice.dto.ItemResponse;
import com.rits.itemservice.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ItemRepository extends MongoRepository<Item, String> {

    List<Item> findByItemAndActiveAndSite(String item, int active, String site);

    List<ItemResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    List<ItemResponse> findByActiveAndSiteAndItemContainingIgnoreCase(int active, String site, String item);

    Item findByItemAndRevisionAndActiveAndSite(String item, String revision, int active, String Site);

    Item findByActiveAndSiteAndItemAndCurrentVersion(int active, String site, String item, boolean currentVersion);


    boolean existsByActiveAndItemAndRevisionAndSite(int active, String item, String revision, String site);


    List<ItemResponse> findByActiveAndSite(int active, String site);

    Boolean existsByActiveAndItemAndCurrentVersionAndSite(int i, String item, boolean b, String site);

    List<Item> findTop50BySiteAndActiveOrderByCreatedDateTimeDesc(String site, int i);
}
