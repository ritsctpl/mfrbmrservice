package com.rits.oeeservice.dto;

import lombok.*;

import javax.persistence.Entity;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OeeByProductResponse {
    private String startTime;
    private String endTime;
    private List<Map<String, Object>> oeeByProduct;
    private List<OeeResponse> oeeResponses;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OeeProductData {
        private String product;
        private double availability;
        private double performance;
        private double quality;
        private double oee;
    }

}
