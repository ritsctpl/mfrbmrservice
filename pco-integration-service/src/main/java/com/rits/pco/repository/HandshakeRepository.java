package com.rits.pco.repository;
import com.rits.pco.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HandshakeRepository extends MongoRepository<HandshakeRecord, String> {}