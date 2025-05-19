package com.rits.overallequipmentefficiency.service;

import com.rits.overallequipmentefficiency.dto.OeeOutput;
import com.rits.overallequipmentefficiency.dto.PerformanceOutput;

public interface QualityOeeService {
    OeeOutput calculateQualityAndOee(PerformanceOutput performanceOutput);
}
