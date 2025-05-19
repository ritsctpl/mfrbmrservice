package com.rits.pcustepstatus.repository;

import com.rits.pcustepstatus.model.PcuStepStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PcuStepStatusRepository extends MongoRepository<PcuStepStatus,String> {
}
