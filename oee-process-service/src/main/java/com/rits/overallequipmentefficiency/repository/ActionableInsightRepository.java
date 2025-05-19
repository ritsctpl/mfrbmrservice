package com.rits.overallequipmentefficiency.repository;

import com.rits.overallequipmentefficiency.model.ActionableInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActionableInsightRepository extends JpaRepository<ActionableInsight, Long> {

    List<ActionableInsight> findByLogDate(LocalDate date);

    List<ActionableInsight> findByWorkcenterIdAndLogDate(String workcenterId, LocalDate date);

    List<ActionableInsight> findByInsightTypeAndCategory(String insightType, String category);
}
