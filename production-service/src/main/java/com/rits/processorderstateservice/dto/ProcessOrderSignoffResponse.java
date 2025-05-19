package com.rits.processorderstateservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data

public class ProcessOrderSignoffResponse {
    private String status;
    private String message;
    private List<BatchSignoffDetails> processedSignoffBatches;

    @Data
    public static class BatchSignoffDetails {
        private String batchNumber;
        private String phase;
        private String operation;
        private BigDecimal quantity;
        private String message;
    }
}
