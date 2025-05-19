package com.rits.overallequipmentefficiency.dto;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakDetails {
    public LocalTime breakStartTime;
    public LocalTime breakEndTime;
    public int meanTime;
}
