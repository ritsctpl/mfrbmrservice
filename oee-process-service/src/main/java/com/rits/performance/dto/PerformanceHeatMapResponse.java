package com.rits.performance.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceHeatMapResponse {
    private String startTime;
    private String endTime;
    private List<ProductPerformance> performanceByProduct;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductPerformance {
        private String product;
        private String machine;
        private int performancePercentage;

        // Getters and Setters
    }

    // Getters and Setters
}
