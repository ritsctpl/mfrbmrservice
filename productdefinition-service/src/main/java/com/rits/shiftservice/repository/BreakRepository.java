package com.rits.shiftservice.repository;

import com.rits.shiftservice.model.Break;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface BreakRepository extends JpaRepository<Break, Long> {

    @Query("SELECT b FROM Break b WHERE b.shiftRef = :shiftRef AND  b.active='1' AND b.createdDateTime BETWEEN :startDate AND :endDate")
    List<Break> findByShiftRefAndCreatedDateRange(String shiftRef, LocalDateTime startDate, LocalDateTime endDate);
}
