package com.rits.batchnoinwork.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BatchNoWorkQtyResponse {

    private BigDecimal qtyToComplete;
    private LocalDateTime createdDateTime;
}
