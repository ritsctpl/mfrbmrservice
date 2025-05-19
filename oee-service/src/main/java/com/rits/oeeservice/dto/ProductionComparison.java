package com.rits.oeeservice.dto;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionComparison {
    private int actualValue;
    private int TargetValue;
    private String resource;
}
