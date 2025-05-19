package com.rits.integration.repository;

import com.rits.integration.model.JoltSpec;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface JoltSpecRepository extends MongoRepository<JoltSpec, String> {
    // Custom query methods
    JoltSpec findBySpecName(String specName);
    List<JoltSpec> findBySpecNameContainingIgnoreCase(String specName);
    List<JoltSpec> findByType(String type);
    JoltSpec findBySpecNameAndSite(String specName, String site);
    JoltSpec findByIdAndSite(String id, String site);
    List<JoltSpec> findAllBySite(String site);
    List<JoltSpec> findByTypeAndSite(String type, String site);
    List<JoltSpec> findBySpecNameContainingIgnoreCaseAndSite(String specName,String site);

}
