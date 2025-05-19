package com.rits.performance.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceByMachineResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<MachinePerformance> performanceByMachine;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MachinePerformance {
        private String resourceId;
        private double performancePercentage;
        //private String interval;
    }
}
