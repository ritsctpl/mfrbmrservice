package com.rits.availability.dto;

import com.rits.performance.dto.PerformanceByProductionLineResponse;
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
public class AvailabilityByWorkcenterResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<AvailabilityWorkcenter> availabilityWorkcenters;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilityWorkcenter {
        private String workcenterId;
        private double availabilityPercentage;
    }

    }
