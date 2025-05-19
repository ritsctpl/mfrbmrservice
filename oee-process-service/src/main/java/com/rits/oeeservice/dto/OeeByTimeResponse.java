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
public class OeeByTimeResponse {
    /*private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resourceId;*/
    private List<OeeTimeData> oeeOverTime;
    private List<OeeByTimeResponse.OeeResourceData> resources;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OeeTimeData {
        private String date;
        private double percentage;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OeeResourceData {
        private String resource;
        private List<OeeTimeData> points;
    }
}
