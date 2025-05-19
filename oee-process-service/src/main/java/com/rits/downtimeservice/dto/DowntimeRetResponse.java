package com.rits.downtimeservice.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DowntimeRetResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private List<OverallDowntime> overallDowntime;
    private List<DowntimeOverTime> downtimeOverTime;
    private List<DowntimeByMachine> downtimeByMachine;
    private List<DowntimeByReason> downtimeByReason;
    private List<CumulativeDowntime> cumulativeDowntime;
    private List<DowntimeVsProductionOutput> downtimeVsProductionOutput;
    private List<DowntimeImpact> downtimeImpact;
    private List<DowntimeDurationDistribution> downtimeDurationDistribution;
    private List<DowntimeAnalysis> downtimeAnalysis;
    private List<DowntimeData> downtimeData;
    private List<DowntimeByReasonAndShift> downtimeByReasonAndShift;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallDowntime {
        private String resourceId;
        private long downtimeDuration;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DowntimeOverTime {
        private String resourceId;
        private String downtimeStart;
        private String downtimeEnd;
        private double downtimeDuration;
        private Map<String, Double> downtime = new LinkedHashMap<>();
        private LocalDateTime occurredAt;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DowntimeByMachine {
        private String machineId;
        private long downtimeDuration;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DowntimeByReason {
        private String reasonCode;
        private long downtimeDuration;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CumulativeDowntime {
        private String date;
        private double cumulativeDowntimeDuration;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DowntimeVsProductionOutput {
        private String resourceId;
        private double productionOutput;
        private long downtime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DowntimeImpact {
        private String impactType;  // E.g., Financial, Operational
        private double impactValue; // Impact value like cost or other
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DowntimeAnalysis {
        private String reason;
        private String machine;
        private int occurrences;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DowntimeDurationDistribution {
        private String durationRange; // E.g., 0-10 mins, 10-30 mins, etc.
        private long eventCount;      // Number of events in that range
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DowntimeData {
        private String resourceId;
        private long downtimeDuration;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DowntimeByReasonAndShift {
        private String shiftId;
        private String reasonCode;
        private long downtimeDuration;
    }
}