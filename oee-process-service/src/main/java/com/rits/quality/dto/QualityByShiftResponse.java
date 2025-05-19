package com.rits.quality.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityByShiftResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<ShiftQuality> qualityByShift;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShiftQuality {
        private String shiftId;
        private double qualityPercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
