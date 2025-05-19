package com.rits.availability.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DownTimeHeatMapResponse {
    private String startTime;
    private String endTime;
    private String machine;
    private List<HeatMapData> downtimeHeatmap;

    // Getters and Setters

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatMapData {
        private String day;
        private int hour;
        private int downtimeMinutes;

        // Getters and Setters
    }
}
