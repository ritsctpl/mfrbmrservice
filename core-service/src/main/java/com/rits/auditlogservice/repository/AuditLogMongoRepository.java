package com.rits.auditlogservice.repository;

import com.rits.auditlogservice.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditLogMongoRepository extends MongoRepository<AuditLog,String> {
    List<AuditLog> findAllBySiteAndUserId(String site, String userId);
    List<AuditLog> findAllBySiteAndCategory(String site, String category);
    List<AuditLog> findAllByAndSiteAndUserIdAndCategory(String site, String userId,String category);

}
