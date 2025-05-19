package com.rits.overallequipmentefficiency.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedOeeRequestDTO {
    private OeeOutput oeeData;
}
