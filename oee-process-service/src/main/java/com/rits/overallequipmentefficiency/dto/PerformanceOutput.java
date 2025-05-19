package com.rits.overallequipmentefficiency.dto;

import com.rits.overallequipmentefficiency.model.AvailabilityEntity;
import com.rits.overallequipmentefficiency.model.PerformanceModel;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PerformanceOutput {
    private AvailabilityEntity availabilityEntity;
    private CycleTime cycleTime;
    private PerformanceModel performanceEntity;

}
