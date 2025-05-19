package com.rits.shiftservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlannedMinutes {
    private String shiftName;
    private String shiftType;
    private String  startTime;
    private String  endTime;
    private int plannedTime;
}
