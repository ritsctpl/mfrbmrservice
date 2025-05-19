package com.rits.performanceservice.repository;

import com.rits.performanceservice.model.Performance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PerformanceRepository extends MongoRepository<Performance,String> {
    List<Performance> findByProcessed(boolean b);

    Optional<Performance> findByUniqueId(int uniqueId);
}
