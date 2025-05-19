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
public class OeeByProductionLineResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<OeeProductionLineData> oeeByProductionLine;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OeeProductionLineData {
        private String workcenterId;
        private double percentage;
    }
}
