package com.rits.availability.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityByMachineResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<AvailabilityByMachine> availabilityByMachine;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailabilityByMachine {
        private String resourceId;
        private double percentage;
    }
}
