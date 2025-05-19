package com.rits.overallequipmentefficiency.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftOutput {
    public String shiftId;
    public LocalDateTime shiftCreatedDatetime;
    public LocalTime shiftStartTime;
    public LocalTime shiftEndTime;
    public LocalDateTime intervalStartDatetime;
    public LocalDateTime intervalEndDatetime;
    public List<BreakDetails> breaks;
    public int plannedOperatingTime;
    public Integer breaktime;
    public Integer nonproduction;
    public Integer totalShiftTime;
    public String shiftRef;
    public boolean nonProductiveDay;
}
