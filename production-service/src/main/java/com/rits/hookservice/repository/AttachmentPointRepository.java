package com.rits.hookservice.repository;

import com.rits.hookservice.model.AttachmentPoint;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AttachmentPointRepository extends MongoRepository<AttachmentPoint, String> {
    List<AttachmentPoint> findByTargetClassAndTargetMethodAndActiveTrue(String targetClass, String targetMethod);
}
