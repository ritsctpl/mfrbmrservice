package com.rits.workflowstatesmasterservice.repository;

import com.rits.workflowstatesmasterservice.model.WorkFlowStatesMaster;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkFlowStatesMasterRepository extends MongoRepository<WorkFlowStatesMaster, String> {

Boolean existsBySiteAndHandleAndActiveEquals(String site, String name, int active);
WorkFlowStatesMaster findBySiteAndHandleAndActiveEquals(String site, String handle, int active);

List<WorkFlowStatesMaster> findBySiteAndNameContainsIgnoreCaseAndActiveEquals(String site, String name, int active);

List<WorkFlowStatesMaster> findTop50BySiteAndActiveEquals(String site, int active);

List<WorkFlowStatesMaster> findBySiteAndIsEndAndActiveEquals(String site, Boolean isEnd, int active);

}
