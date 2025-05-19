package com.rits.inventoryservice.repository;

import com.rits.inventoryservice.dto.InventoryRequest;
import com.rits.inventoryservice.dto.InventoryResponse;
import com.rits.inventoryservice.model.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InventoryRepository extends MongoRepository<com.rits.inventoryservice.model.Inventory,String> {


    Inventory findByInventoryIdAndSiteAndActive(String inventoryId, String site, int active);

    long countByInventoryIdAndSiteAndActive(String inventoryId, String site, int active);

    List<InventoryResponse> findTop1000ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    List<InventoryResponse> findByInventoryIdContainingIgnoreCaseAndSiteAndActive(String inventoryId, String site, int active);

    List<InventoryResponse> findByItemAndActiveAndSiteAndRemainingQtyGreaterThanEqual(String item, int i, String site, int i1);

    List<InventoryResponse> findByItemAndVersionAndActiveAndSiteAndRemainingQtyGreaterThanEqual(String item, String version, int i, String site, float i1);

    List<InventoryResponse> findByItemAndVersionAndActiveAndSite(String item, String version, int i, String site);

    boolean existsByInventoryIdAndSite(String inventoryId, String site);

    InventoryRequest findByInventoryId(String inventoryId);
}
