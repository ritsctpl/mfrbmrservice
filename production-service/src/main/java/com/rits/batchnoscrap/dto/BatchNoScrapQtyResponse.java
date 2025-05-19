package com.rits.batchnoscrap.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchNoScrapQtyResponse {
    private BigDecimal scrapQuantity;
}
