package com.rits.shiftservice.repository;

import com.rits.shiftservice.model.Break;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BreakPostgresRepository extends JpaRepository<Break, Integer> {
    List<Break> findBreaksByIntervalRef(String intervalRef);
}
