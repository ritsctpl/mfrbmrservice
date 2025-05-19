package com.rits.performance.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceByShiftResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<ShiftPerformance> performanceByShift;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShiftPerformance {
        private String shiftId;
        private double performancePercentage;
        //private String interval;

        // Getters and Setters
    }

    // Getters and Setters
}
