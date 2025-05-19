package com.rits.signoffservice.repository;

import com.rits.signoffservice.model.SignOff;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SignOffRepository extends MongoRepository<SignOff,String> {
}
