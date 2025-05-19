package com.rits.overallequipmentefficiency.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedOeeQualityDTO {
    private String value;
    private Double goodQuantity;
    private Double badQuantity;
}
