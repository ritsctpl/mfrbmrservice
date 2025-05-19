package com.rits.processorderstateservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessOrderCompleteRequest {
    private List<BatchDetails> completeBatches;
    private Boolean sync;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BatchDetails {
        private String site;
        private String batchNumber;
        private String phase;
        private String operation;
        private String resource;
        private String workcenter;
        private String user;
        private String orderNumber;
        private String material;
        private String materialVersion;
        private BigDecimal quantity;
        private BigDecimal yieldQuantity;
        private BigDecimal scrapQuantity;
        private Boolean finalReport;
        private String uom;
        private String reasonCode;
        private BigDecimal finalQtyAfterScrap;
    }
}
