package com.rits.repository;

import com.rits.kafkapojo.TopicConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicConfigurationRepository extends MongoRepository<TopicConfiguration, String> {
    Optional<TopicConfiguration> findByTopicNameAndActive(String topicName, boolean active);
}