package com.rits.processlotservice.repository;

import com.rits.processlotservice.model.Employee;
import com.rits.processlotservice.model.ProcessLot;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessLotRepository extends MongoRepository<ProcessLot,String> {
    boolean existsByActiveAndSiteAndProcessLot(int i, String site, String processLot);
    ProcessLot findByActiveAndSiteAndProcessLot(int i, String site, String processLot);
}
