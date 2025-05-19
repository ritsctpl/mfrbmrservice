package com.rits.performance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceComparisonResponse {
    private String startTime;
    private String endTime;
    private List<ComparisonData> performanceComparison;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonData {
        private String machine;
        private double performancePercentage;
        private double downtimeDuration;
        private double qualityPercentage;


        // Getters and Setters
    }

    // Getters and Setters
}
