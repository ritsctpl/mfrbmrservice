package com.rits.performance.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceByTimeResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resourceId;
    private List<PerformanceData> performanceOverTime;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceData {
        private String date;
        private double performancePercentage;
    }
}
