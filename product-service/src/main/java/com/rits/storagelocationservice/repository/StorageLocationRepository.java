package com.rits.storagelocationservice.repository;

import com.rits.storagelocationservice.dto.StorageLocationResponse;
import com.rits.storagelocationservice.model.StorageLocation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StorageLocationRepository extends MongoRepository<StorageLocation,String> {
    StorageLocation findByStorageLocationAndActiveAndSite(String storageLocation, int active, String site) ;


    long countByStorageLocationAndSiteAndActive(String storageLocation, String site, int active);

    List<StorageLocationResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    List<StorageLocationResponse> findByStorageLocationContainingIgnoreCaseAndSiteAndActive(String storageLocation, String site, int active);

    Boolean existsByStorageLocationAndSiteAndActive(String storageLocation, String site, int active);

    StorageLocation findByStorageLocationAndActive(String storageLocation, int active);

    StorageLocation findByStorageLocationAndSiteAndActive(String storageLocation, String site, int active);

    List<StorageLocation> findBySiteAndStorageLocationAndActive(String site, String storageLocation, int active);
}
