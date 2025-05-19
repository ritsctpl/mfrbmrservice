package com.rits.batchnocomplete.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BatchNoCompleteQty {
    private BigDecimal qtyToComplete;
    private LocalDateTime createdDateTime;
}
