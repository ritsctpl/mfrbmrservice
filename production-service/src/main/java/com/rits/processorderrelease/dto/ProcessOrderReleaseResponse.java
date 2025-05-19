package com.rits.processorderrelease.dto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
@Getter
@Setter
@Data
public class ProcessOrderReleaseResponse {
    private List<BatchDetails> batches;
/*    private String status;
    private String message;*/
    @Data
    public static class BatchDetails {
        private Map<String, BigDecimal> batchNumber;
        private Double quantity;
        private String material;
        private String materialVersion;
        private String processOrder;

        private String status;
        private String message;
    }

}
