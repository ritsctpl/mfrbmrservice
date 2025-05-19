package com.rits.shiftservice.dto;

import com.rits.shiftservice.model.Break;
import com.rits.shiftservice.model.ShiftIntervals;
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
