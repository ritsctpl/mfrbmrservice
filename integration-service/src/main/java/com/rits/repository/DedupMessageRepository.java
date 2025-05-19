package com.rits.repository;

import com.rits.kafkapojo.DedupMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DedupMessageRepository extends MongoRepository<DedupMessage, String> {
}