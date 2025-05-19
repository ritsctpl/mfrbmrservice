package com.rits.shiftservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailabilityPlannedOperatingTimeResponse {

    private Double plannedOperatingTime;
    private Double shiftBreakDuration;
    private String shiftRef;
}
