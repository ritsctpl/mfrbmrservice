package com.rits.quality.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityByProductResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<ProductQuality> qualityByProduct;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductQuality {
        private String itemBo;
        private String resourceId;
        private double qualityPercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
