package com.rits.quality.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectsByReasonResponse {
    /*private LocalDateTime startTime;
    private LocalDateTime endTime;*/
    private List<ReasonDefects> defectsByReason;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReasonDefects {
        private String reason;
        private double occurance;
        private double qualityPercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
