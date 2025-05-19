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
public class PerformanceLossReasonsResponse {
    /*private String startTime;
    private String endTime;*/
    private List<PerformanceLossReason> performanceLossReason;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceLossReason {
        private String reason;
        private int occurrence;
        private double performancePercentage;
        //private String interval;
        // Getters and Setters
    }

    // Getters and Setters
}
