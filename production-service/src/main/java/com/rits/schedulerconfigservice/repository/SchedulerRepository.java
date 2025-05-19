package com.rits.schedulerconfigservice.repository;

import com.rits.schedulerconfigservice.model.SchedulerConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface SchedulerRepository extends MongoRepository<SchedulerConfig, String> {

    SchedulerConfig findBySiteAndEntityIdAndEntityName(String site, Integer entityId, String entityName);

    List<SchedulerConfig> findByEntityType(String entityType);

    List<SchedulerConfig> findByEnabled(Boolean enabled);

    List<SchedulerConfig> findByEntityName(String entityName);

    List<SchedulerConfig> findByNextRunTimeAfter(Date date);

    List<SchedulerConfig> findByEventTypeAndEntityType(String eventType, String entityType);

    List<SchedulerConfig> findByApiEndpoint(String apiEndpoint);

    void deleteByEntityId(Integer entityId);

    long countByEventType(String eventType);

    SchedulerConfig findBySiteAndEntityId(String site, Integer entityId);
}
