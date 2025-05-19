package com.rits.oeeservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TrendAnalysis {
    private int actualValue;
    private int TargetValue;
    private LocalDate date;
}
