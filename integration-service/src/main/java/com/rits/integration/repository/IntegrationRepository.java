package com.rits.integration.repository;

import com.rits.integration.model.IntegrationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrationRepository extends MongoRepository<IntegrationEntity, String> {
    IntegrationEntity findByIdentifier(String identifier);
    List<IntegrationEntity> findAllByOrderByCreatedDateTimeDesc();

    List<IntegrationEntity> findAllBySiteOrderByCreatedDateTimeDesc(String site);


    List<IntegrationEntity> findAllByIdentifierContainingIgnoreCase(String identifier);
    List<IntegrationEntity> findTop50ByOrderByCreatedDateTimeDesc();

    IntegrationEntity findByIdAndSite(String id, String site);

    IntegrationEntity findByIdentifierAndSite(String identifier, String site);

    void deleteByIdAndSite(String id, String site);

    List<IntegrationEntity> findAllByIdentifierContainingIgnoreCaseAndSite(String identifier, String site);

    List<IntegrationEntity> findTop50BySiteOrderByCreatedDateTimeDesc(String site);



}
