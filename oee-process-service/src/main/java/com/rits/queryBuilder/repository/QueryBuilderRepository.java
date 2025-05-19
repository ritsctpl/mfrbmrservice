package com.rits.queryBuilder.repository;

import com.rits.queryBuilder.model.QueryBuilder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QueryBuilderRepository extends MongoRepository<QueryBuilder, String> {
    Optional<QueryBuilder> findByTemplateNameAndSiteAndActive(String templateName, String site, int active);

    List<QueryBuilder> findBySite(String site);
}
