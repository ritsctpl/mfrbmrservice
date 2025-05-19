package com.rits.shiftservice.repository;

import com.rits.shiftservice.model.Shift;
import com.rits.shiftservice.model.ShiftIntervals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface ShiftIntervalsRepository extends JpaRepository<ShiftIntervals, Long> {

    @Query("SELECT si FROM ShiftIntervals si WHERE si.shiftRef = :shiftRef AND si.active = 1 AND si.validFrom >= :startDate AND si.validEnd <= :endDate")
    List<ShiftIntervals> findByShiftRefAndValidDateRange(String shiftRef, LocalDateTime startDate, LocalDateTime endDate);

}
