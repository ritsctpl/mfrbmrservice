package com.rits.performance.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceByEfficiencyOfProduct {
    /*private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resourceId;*/
    private List<PerformanceData> performanceByEfficiencyOfProduct;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceData {
        private String item;
        private String resourceId;
        private double performancePercentage;
        //private String interval;
    }
}