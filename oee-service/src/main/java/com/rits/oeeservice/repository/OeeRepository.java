package com.rits.oeeservice.repository;

import com.rits.oeeservice.model.Oee;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OeeRepository extends MongoRepository<Oee,String> {
}
