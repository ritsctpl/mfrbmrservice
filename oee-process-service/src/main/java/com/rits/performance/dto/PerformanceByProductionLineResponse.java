package com.rits.performance.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceByProductionLineResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<ProductionLinePerformance> performanceByProductionLine;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductionLinePerformance {
        private String workcenterId;
        private String resourceId;
        private double performancePercentage;
        //private String interval;

        // Getters and Setters
    }

    // Getters and Setters
}
