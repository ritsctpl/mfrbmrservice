package com.rits.batchnophaseprogressservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhaseProgress {

    private String phase;
    private String operation;
    private BigDecimal startQuantityBaseUom;
    private BigDecimal startQuantityMeasuredUom;
    private BigDecimal completeQuantityBaseUom;
    private BigDecimal completeQuantityMeasuredUom;
    private BigDecimal scrapQuantity;
    private BigDecimal scrapQuantityBaseUom;
    private BigDecimal scrapQuantityMeasuredUom;
    private String baseUom;
    private String measuredUom;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private String status;
}
