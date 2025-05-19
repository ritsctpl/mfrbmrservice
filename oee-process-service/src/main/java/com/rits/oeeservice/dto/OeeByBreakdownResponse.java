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
public class OeeByBreakdownResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<OeeBreakdownData> oeeBreakdown;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OeeBreakdownData {
        private String resourceId;
        private double availability;
        private double performance;
        private double quality;

    }
}
