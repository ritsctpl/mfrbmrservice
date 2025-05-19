package com.rits.processorderstateservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessOrderCompleteResponse {
    private String status;
    private String message;
    private List<BatchCompleteDetails> batchDetails;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchCompleteDetails {
        private String batchNumber;
        private String phase;
        private String operation;
        private BigDecimal quantity;
        private String message;
        private String nextPhase;
        private String nextOperation;

    }
}
