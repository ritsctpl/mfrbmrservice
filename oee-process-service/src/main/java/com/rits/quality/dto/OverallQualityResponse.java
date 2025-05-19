package com.rits.quality.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OverallQualityResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resourceId;
    private String shiftId;
    private double qualityPercentage;

    // Getters and Setters
}