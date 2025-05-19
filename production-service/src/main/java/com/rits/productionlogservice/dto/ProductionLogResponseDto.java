package com.rits.productionlogservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionLogResponseDto {
    private Double actualCycleTime;
    private Double actualQuantity;
    private Double manufacturedTime;

}