package com.rits.downtimeservice.repository;

import com.rits.downtimeservice.model.AggregatedResult;
import com.rits.downtimeservice.model.DownTimeAvailability;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DownTimeRepository extends MongoRepository<DownTimeAvailability, String> {
    // This interface should extend both MongoRepository and the custom interface
    List<DownTimeAvailability> findByProcessed(Boolean processed);

    List<DownTimeAvailability> findBySiteAndResourceIdAndShiftAndCreatedDateTimeStartingWith(String site, String resourceId, String shift, String dateString);

    Optional<DownTimeAvailability> findTopBySiteAndResourceIdAndShiftOrderByCreatedDateTimeDesc(String site, String resourceId, String shift);

    Optional<DownTimeAvailability> findTopBySiteAndResourceIdAndShiftStartDateOrderByCreatedDateTimeDesc(String site, String resourceId, String shift);
}


