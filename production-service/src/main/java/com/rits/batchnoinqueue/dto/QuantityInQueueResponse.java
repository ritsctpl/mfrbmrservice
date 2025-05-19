package com.rits.batchnoinqueue.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class QuantityInQueueResponse {
    private BigDecimal qtyInQueue;
    private LocalDateTime createdDateTime;
    private boolean qualityApproval;
}
