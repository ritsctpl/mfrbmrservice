package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SpeedLossSummaryDTO {
    private String resourceId;
    private String workcenterId;
    private double speedLoss;

    public SpeedLossSummaryDTO(String resourceId, double speedLoss) {
        this.resourceId = resourceId;
        this.speedLoss = speedLoss;
    }
}