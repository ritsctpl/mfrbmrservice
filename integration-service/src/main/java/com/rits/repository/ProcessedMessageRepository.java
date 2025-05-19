package com.rits.repository;

import com.rits.kafkapojo.ProcessedMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessedMessageRepository extends MongoRepository<ProcessedMessage, String> {
    boolean existsByMessageId(String messageId);

    List<ProcessedMessage> findTop50ByOrderByProcessedAtDesc();

}