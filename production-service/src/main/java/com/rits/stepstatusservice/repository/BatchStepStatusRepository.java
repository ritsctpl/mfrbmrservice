package com.rits.stepstatusservice.repository;


import com.rits.stepstatusservice.model.StepStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BatchStepStatusRepository extends MongoRepository<StepStatus,String> {
}
