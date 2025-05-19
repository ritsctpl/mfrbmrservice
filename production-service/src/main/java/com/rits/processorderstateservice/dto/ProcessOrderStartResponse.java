package com.rits.processorderstateservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data

public class ProcessOrderStartResponse {
    private String status;
    private String message;
    private List<BatchStartDetails> processedStartBatches;

    @Data
    public static class BatchStartDetails {
        private String batchNumber;
        private String phase;
        private String operation;
        private BigDecimal quantity;
        private String message;
    }
}
