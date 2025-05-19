package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

// Base Class for OEE Details
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OeeDetails {
    private double oee;
    private double availability;
    private double performance;
    private double quality;
    private boolean downtime;
}

