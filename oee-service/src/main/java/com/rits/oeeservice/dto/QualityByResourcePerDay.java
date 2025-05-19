package com.rits.oeeservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class QualityByResourcePerDay {
    private double averageQuality;
    private String resource;
    private LocalDate dateTime;
}
