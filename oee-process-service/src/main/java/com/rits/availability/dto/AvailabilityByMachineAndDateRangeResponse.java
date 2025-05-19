package com.rits.availability.dto;

import lombok.*;


import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityByMachineAndDateRangeResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<AvailabilityByMachineAndDateRange> availabilityByMachineAndDateRanges;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailabilityByMachineAndDateRange {
        private String resourceId;
        private LocalDateTime createdDatetime;
        private double availabilityPercentage;
    }

}
