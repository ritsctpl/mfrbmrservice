package com.rits.pco.repository;
import com.rits.pco.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiTransactionRepository extends MongoRepository<ApiTransaction, String> {}