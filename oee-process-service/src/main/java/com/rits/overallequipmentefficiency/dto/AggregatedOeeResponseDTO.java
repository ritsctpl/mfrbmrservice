package com.rits.overallequipmentefficiency.dto;

import com.rits.overallequipmentefficiency.model.AggregatedAvailability;
import com.rits.overallequipmentefficiency.model.AggregatedPerformance;
import com.rits.overallequipmentefficiency.model.AggregatedQuality;
import com.rits.overallequipmentefficiency.model.AggregatedOee;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedOeeResponseDTO {
    private OeeOutput oeeData;
    private AggregatedAvailability aggregatedAvailability;
    private AggregatedPerformance aggregatedPerformance;
    private AggregatedQuality aggregatedQuality;
    private AggregatedOee aggregatedOee;
}
