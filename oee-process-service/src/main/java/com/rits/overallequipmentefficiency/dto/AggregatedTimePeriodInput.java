package com.rits.overallequipmentefficiency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedTimePeriodInput {

    private String site;
    private String shiftId;
    private String workcenterId;
    private LocalDate availabilityDate;
    private String eventSource;
}
