package com.rits.quality.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityByMachineResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<MachineQuality> qualityByMachine;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MachineQuality {
        private String resourceId;
        private double qualityPercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
