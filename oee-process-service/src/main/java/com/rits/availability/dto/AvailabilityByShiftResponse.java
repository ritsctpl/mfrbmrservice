package com.rits.availability.dto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityByShiftResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resourceId;
    private List<AvailabilityByShift> availabilityByShift;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailabilityByShift {
        private String shiftId;
        private double percentage;
    }
}
