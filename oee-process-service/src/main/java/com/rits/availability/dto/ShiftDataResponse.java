package com.rits.availability.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftDataResponse {
    private String shiftHandle;
    private String site;
    private long plannedOperatingTime;  // In seconds
    private long breakDuration;         // In seconds
    private List<ShiftIntervals> shiftIntervals;
    private List<Break> calendarList;
    private String shiftRef;
    private long totalRuntime;
    private long totalBreakDuration;


}
