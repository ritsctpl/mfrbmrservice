package com.rits.mfrscreenconfigurationservice.repository;
import com.rits.mfrscreenconfigurationservice.dto.ProductResponse;
import com.rits.mfrscreenconfigurationservice.model.MFRScreenConfiguration;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MFRScreenConfigurationRepository extends MongoRepository<MFRScreenConfiguration, String> {
    int countByProductNameAndActive(String productName, int i);

    MFRScreenConfiguration findByProductNameAndSiteAndActive(String productName, String site, int i);

    MFRScreenConfiguration findByActiveAndProductName(int i, String productName);


    List<ProductResponse> findTop50ByActiveOrderByCreatedDateTimeDesc(int i);
}
