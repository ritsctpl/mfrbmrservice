package com.rits.quality.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectByProductResponse {
    /*private LocalDateTime startTime;
    private LocalDateTime endTime;*/
    private List<ProductDefects> defectsByProduct;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductDefects {
        private String item;
        private double defectPercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
