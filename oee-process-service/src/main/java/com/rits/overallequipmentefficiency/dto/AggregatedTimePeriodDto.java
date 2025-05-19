package com.rits.overallequipmentefficiency.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedTimePeriodDto {
    private Double oee;
    private Double availability;
    private Double performance;
    private Double quality;
}
