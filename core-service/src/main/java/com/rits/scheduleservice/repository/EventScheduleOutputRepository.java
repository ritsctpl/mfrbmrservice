package com.rits.scheduleservice.repository;
import com.rits.scheduleservice.model.EventScheduleOutput;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EventScheduleOutputRepository extends MongoRepository<EventScheduleOutput, String> {
    List<EventScheduleOutput> findTop15ByScheduleIdOrderByExecutionTimeDesc(String id);
}
