package com.rits.quality.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityLossByProductionLineResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<LineLoss> qualityLossByProductionLine;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineLoss {
        private String workcenterId;
        private String reason;
        private double lossPercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
