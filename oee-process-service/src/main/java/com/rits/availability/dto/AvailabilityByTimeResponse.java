package com.rits.availability.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityByTimeResponse {
    /*private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resourceId;*/
    private List<AvailabilityData> availabilityData;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailabilityData {
        private String date;
        private double percentage;
    }
}
