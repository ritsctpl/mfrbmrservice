package com.rits.cycletimeservice.repository;

import com.rits.cycletimeservice.model.CycleTime;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CycleTimeRepository extends MongoRepository<CycleTime,String> {
    List<CycleTime> findBySiteAndActiveOrderByCreatedDateTimeDesc(String site, int i);

    CycleTime findBySiteAndActiveAndHandle(String site, int i, String handle);

    Boolean existsBySiteAndActiveAndHandle(String site, int i, String handle);

    List<CycleTime> findBySiteAndWorkCenterIdAndActive(String site, String workcenterId, int i);

    List<CycleTime> findBySiteAndResourceIdAndActive(String site, String resourceId, int i);
}
