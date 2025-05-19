package com.rits.oeeservice.repository;

import com.rits.oeeservice.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface OeeShiftRepository extends JpaRepository<OeeShift, Integer> {

    // Query to find the shift by site, workcenterId, resourceId
    @Query("SELECT s FROM OeeShift s WHERE s.site = :site AND s.workcenterId = :workcenterId AND s.resourceId = :resourceId AND s.active = 1")
    Optional<OeeShift> findActiveShift(String site, String workcenterId, String resourceId);

    @Query("SELECT si FROM OeeShiftInterval si WHERE si.shift.id = :shiftId AND si.startTime >= :shiftStartTime AND si.endTime <= :endTime")
    List<OeeShiftInterval> findShiftIntervalsByShiftIdAndTimeRange(Integer shiftId, LocalDateTime shiftStartTime, LocalDateTime endTime);
}