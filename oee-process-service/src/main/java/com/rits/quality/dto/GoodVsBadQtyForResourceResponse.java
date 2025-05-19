package com.rits.quality.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodVsBadQtyForResourceResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<GoodVsBadQtyForResource> goodVsBadQtyForResources;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoodVsBadQtyForResource {
        private String resourceId;
        private double goodQty;
        private double badQty;

        // Getters and Setters
    }
}
