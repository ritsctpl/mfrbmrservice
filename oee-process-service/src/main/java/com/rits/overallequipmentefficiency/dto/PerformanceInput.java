package com.rits.overallequipmentefficiency.dto;

import com.rits.overallequipmentefficiency.model.AvailabilityEntity;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PerformanceInput {
    private AvailabilityEntity availabilityEntity;
    private CycleTime cycleTime;
    private String item;
    private String itemVersion;
    private String eventBy;
    // Getters and Setters
}

