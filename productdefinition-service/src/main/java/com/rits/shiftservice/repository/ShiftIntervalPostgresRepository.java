package com.rits.shiftservice.repository;

import com.rits.shiftservice.model.ShiftIntervals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShiftIntervalPostgresRepository extends JpaRepository<ShiftIntervals, Integer> {
    @Query(value = "SELECT * FROM r_shift_intervals WHERE shift_ref = :shiftRef AND active = 1", nativeQuery = true)
    List<ShiftIntervals> findIntervalsByShiftRef(@Param("shiftRef") String shiftRef);

}
