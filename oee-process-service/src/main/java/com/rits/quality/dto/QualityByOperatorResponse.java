package com.rits.quality.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualityByOperatorResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<OperatorQuality> qualityByOperator;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatorQuality {
        private String operator;
        private double qualityPercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
