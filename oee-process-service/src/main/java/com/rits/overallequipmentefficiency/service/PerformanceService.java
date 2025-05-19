package com.rits.overallequipmentefficiency.service;

import com.rits.overallequipmentefficiency.dto.PerformanceInput;
import com.rits.overallequipmentefficiency.dto.PerformanceOutput;

import java.util.List;

public interface PerformanceService {
    List<PerformanceOutput> calculatePerformance(PerformanceInput inputs);
}
