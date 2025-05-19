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
public class PerformanceByDowntimeResponse {
    /*private String startTime;
    private String endTime;*/
    private List<DowntimeData> downtimeAnalysis;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DowntimeData {
        private String reason;
        private String item;
        private double downtimeDuration;
        private double performancePercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
