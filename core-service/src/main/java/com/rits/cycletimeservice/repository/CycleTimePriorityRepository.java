package com.rits.cycletimeservice.repository;

import com.rits.cycletimeservice.model.AttachmentPriority;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CycleTimePriorityRepository extends MongoRepository<AttachmentPriority,String> {
    AttachmentPriority findByTag(String tag);
}
