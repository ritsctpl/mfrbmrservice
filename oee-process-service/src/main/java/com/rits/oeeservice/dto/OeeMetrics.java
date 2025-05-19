package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OeeMetrics {
    private String workcenter;
    private double oee;
    private double availability;
    private double performance;
    private double quality;
    private double targetQty;
    private double actualQty;
}
