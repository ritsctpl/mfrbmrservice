package com.rits.oeeservice.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsResponse {
    private List<MetricData> metricData;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricData {
        private String workcenter;
        private List<DayData> days;
        private List<MonthData> month;
        private List<YearData> year;

        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DayData {
            private String name;
            private double oee;
            private double performance;
            private double quality;
            private double availability;
        }

        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MonthData {
            private String name;
            private double oee;
            private double performance;
            private double quality;
            private double availability;
        }

        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class YearData {
            private String name;
            private double oee;
            private double performance;
            private double quality;
            private double availability;
        }
    }
}
