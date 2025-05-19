package com.rits.cycletimeservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionLogResponseDto {
    private double actualCycleTime;
    private double actualQuantity;
    private double manufacturedTime;
}
