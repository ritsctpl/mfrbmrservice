package com.rits.oeeservice.dto;

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
public class OeeByShiftResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<OeeShiftData> oeeByShift;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OeeShiftData {
        private String shiftId;
        private double availability;
        private double performance;
        private double quality;
    }

}
