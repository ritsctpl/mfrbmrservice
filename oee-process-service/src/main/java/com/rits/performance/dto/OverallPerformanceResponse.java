package com.rits.performance.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverallPerformanceResponse {
    //private LocalDateTime startTime;
    //private LocalDateTime endTime;
    private String resourceId;
    private double overallPerformancePercentage;
    //private String interval;


}
