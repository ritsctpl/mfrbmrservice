package com.rits.changeproductionservice.repository;

import com.rits.changeproductionservice.model.ChangeProduction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChangeProductionRepository extends MongoRepository<ChangeProduction,String> {
}
