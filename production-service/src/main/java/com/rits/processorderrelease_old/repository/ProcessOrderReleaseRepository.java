package com.rits.processorderrelease_old.repository;

import com.rits.processorderrelease_old.model.ProcessOrderRelease;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessOrderReleaseRepository extends MongoRepository<ProcessOrderRelease,String> {
}
