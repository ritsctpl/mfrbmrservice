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
public class PerformanceByReasonResponse {
    private String startTime;
    private String endTime;
    private List<ReasonPerformance> performanceByReason;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReasonPerformance {
        private String product;
        private String machine;
        private int performancePercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
