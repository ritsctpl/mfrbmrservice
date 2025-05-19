package com.rits.processorderstateservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProcessOrderSignoffRequest {
    private List<BatchDetails> signoffBatches;
    private Boolean sync;

    @Data
    public static class BatchDetails {
        private String site; //added
        private String batchNumber;
        private String phase;
        private String operation;
        private String resource;
        private String workcenter;
        private String user;
        private String orderNumber;// changed to orderNumber from shopOrder
        private String material;
        private String materialVersion;
        private BigDecimal quantity;
        private String reasonCode;
    }
}
