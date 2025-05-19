package com.rits.productionlogservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapAndReworkTrendResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<ScrapAndReworkTrend> scrapAndReworkTrends;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScrapAndReworkTrend {
        private String date;
        private Long scrapValue;
        private Long reworkValue;

        // Getters and Setters
    }
}
