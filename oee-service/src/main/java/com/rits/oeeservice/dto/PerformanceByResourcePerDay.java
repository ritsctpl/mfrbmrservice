package com.rits.oeeservice.dto;

import lombok.*;

import java.time.LocalDate;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PerformanceByResourcePerDay {
    private double averagePerformance;
    private String resource;
    private LocalDate dateTime;
}
