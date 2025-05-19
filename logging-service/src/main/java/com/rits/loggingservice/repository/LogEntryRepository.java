package com.rits.loggingservice.repository;

import com.rits.loggingservice.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, String> {
    // Derived query methods for filtering (if needed)
    List<LogEntry> findByLevel(String level);
    List<LogEntry> findByMessageContaining(String message);
    List<LogEntry> findByLevelAndMessageContaining(String level, String message);
}
