package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OeeWorkCenterDto {
    private String workcenterId;
    private double avgAvailability;
    private double avgOee;
    private double avgPerformance;
    private double avgQuality;
}
