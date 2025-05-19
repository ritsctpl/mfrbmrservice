package com.rits.overallequipmentefficiency.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DurationAggregatedTimePeriodDto {
    private Double oee;
    private Double availability;
    private Double performance;
    private Double quality;
    private Double totalGoodQuantity;
    private Double totalBadQuantity;
    private Double totalQuantity;
    private String shiftId;
}
