package com.rits.quality.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectByTimeResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<DefectResourceData> defectTrendOverTime;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DefectTrend {
        private String date;
        private double defects;
    }

    @Data
    @AllArgsConstructor
    public static class DefectResourceData {
        private String resourceId;
        private List<DefectTrend> points;
    }

}
