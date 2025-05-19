package com.rits.shiftservice.repository;

import com.rits.shiftservice.model.CalendarRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CalendarRulePostgresRepository extends JpaRepository<CalendarRules, Integer> {
    Optional<CalendarRules> findByShiftRefAndProductionDay(String shiftRef, String productionDay);

    @Query("SELECT cr FROM CalendarRules cr WHERE cr.shiftRef = :shiftRef AND cr.productionDay = :productionDay AND cr.day = :day and cr.active='1'")
    Optional<CalendarRules> findByShiftRefAndProductionDayAndDay(
            @Param("shiftRef") String shiftRef,
            @Param("productionDay") String productionDay,
            @Param("day") String day);

}
