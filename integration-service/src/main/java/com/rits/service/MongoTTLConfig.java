package com.rits.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

import javax.annotation.PostConstruct;

@Configuration
public class MongoTTLConfig {

    private final MongoTemplate mongoTemplate;

    @Value("${message.cleanup.ttl-seconds:86400}")
    private long ttlSeconds;

    public MongoTTLConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void createTTLIndex() {
        IndexOperations indexOps = mongoTemplate.indexOps("dedup_messages");
        Index index = new Index().on("receivedAt", Sort.Direction.ASC).expire(ttlSeconds);
        indexOps.ensureIndex(index);
    }
}
