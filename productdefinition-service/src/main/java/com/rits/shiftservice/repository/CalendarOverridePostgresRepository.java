package com.rits.shiftservice.repository;

import com.rits.shiftservice.model.CalendarOverrides;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

public interface CalendarOverridePostgresRepository extends JpaRepository<CalendarOverrides, Integer> {
    Optional<CalendarOverrides> findBySiteAndDate(String site, Date date);
}
