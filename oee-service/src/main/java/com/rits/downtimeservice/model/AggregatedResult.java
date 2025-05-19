package com.rits.downtimeservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AggregatedResult {
    private int totalPlannedProductionTime;
    private int totalDowntime;
    private int totalOperatingTime;
    private int totalMcBreakDownHours;
    private String resourceId;

}
