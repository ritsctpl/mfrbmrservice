package com.rits.cycletimeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PerformancePlannedOperatingTimeResponse {

    private Double plannedOperatingTime;
    private Double shiftBreakDuration;
}
