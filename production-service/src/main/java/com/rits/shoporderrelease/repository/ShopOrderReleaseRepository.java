package com.rits.shoporderrelease.repository;

import com.rits.shoporderrelease.model.ShopOrderRelease;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShopOrderReleaseRepository extends MongoRepository<ShopOrderRelease,String> {
}
