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
public class PerformanceByOperatorResponse {
    private String startTime;
    private String endTime;
    private List<OperatorPerformance> performanceByOperator;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatorPerformance {
        private String operator;
        private int performancePercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
