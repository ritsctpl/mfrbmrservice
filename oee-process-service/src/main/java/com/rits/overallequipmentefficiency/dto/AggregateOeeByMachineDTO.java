package com.rits.overallequipmentefficiency.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AggregateOeeByMachineDTO {
    private String value;
    private Double availability;
    private Double oee;
    private Double performance;
    private Double quality;
}
