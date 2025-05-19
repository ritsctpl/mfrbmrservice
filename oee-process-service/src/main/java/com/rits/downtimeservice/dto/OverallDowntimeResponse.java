package com.rits.downtimeservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverallDowntimeResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<OverallDowntime> downtimeDurations;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallDowntime {
        private String name; //totalDuration, totalDowntimeDuration
        private long duration;
    }
}
