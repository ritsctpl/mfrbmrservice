package com.rits.overallequipmentefficiency.repository;

import com.rits.overallequipmentefficiency.model.OeeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OverallEquipmentEfficiencyRepository extends JpaRepository<OeeModel, Long> {
}

