package com.rits.pco.repository;
import com.rits.pco.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PcoAgentRepository extends MongoRepository<PcoAgentRecord, String> {}