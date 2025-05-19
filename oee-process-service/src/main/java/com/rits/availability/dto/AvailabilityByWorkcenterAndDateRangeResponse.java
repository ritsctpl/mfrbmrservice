package com.rits.availability.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityByWorkcenterAndDateRangeResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<AvailabilityByWorkcenterAndDateRange> availabilityByWorkcenterAndDateRanges;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailabilityByWorkcenterAndDateRange {
        private String workcenterId;
        private LocalDateTime createdDatetime;
        private double availabilityPercentage;
    }

}
