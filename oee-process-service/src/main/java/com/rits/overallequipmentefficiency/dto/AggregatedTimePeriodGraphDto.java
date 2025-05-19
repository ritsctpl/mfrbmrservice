package com.rits.overallequipmentefficiency.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedTimePeriodGraphDto {
    private String categoryType;
    private Double oee;
    private Double performance;
    private Double quality;
    private Double availability;
}
