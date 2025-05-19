package com.rits.quality.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityByTimeResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resourceId;
    private List<QualityOverTime> qualityOverTime;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityOverTime {
        private String date;
        private double qualityPercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
