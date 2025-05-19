package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OeeByOperatorResponse {
    private String startTime;
    private String endTime;
    private List<OeeOperatorData> oeeByOperator;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OeeOperatorData {
        private String operator;
        private Integer oeePercentage;
        // Getters and Setters
    }

    // Getters and Setters
}
