package com.rits.productionlogservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionLogQualityResponse {
    private String eventType;
    private long totalQuantity;
}
