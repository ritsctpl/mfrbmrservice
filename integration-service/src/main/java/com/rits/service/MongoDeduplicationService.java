package com.rits.service;

// MongoDeduplicationService.java
import com.rits.kafkapojo.DedupMessage;
import com.rits.repository.DedupMessageRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class MongoDeduplicationService {

    private final DedupMessageRepository repository;

    public MongoDeduplicationService(DedupMessageRepository repository) {
        this.repository = repository;
    }

    public boolean isDuplicate(String messageId) {
        try {
            repository.insert(new DedupMessage(messageId));
            return false;
        } catch (DuplicateKeyException ex) {
            return true;
        }
    }
}