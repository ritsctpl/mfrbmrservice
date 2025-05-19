package com.rits.scheduleservice.repository;

import com.rits.scheduleservice.model.EventScheduleConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EventScheduleConfigRepository extends MongoRepository<EventScheduleConfig, String> {
    Optional<EventScheduleConfig> findByEntityId(int entityId);

}
