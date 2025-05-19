package com.rits.shiftservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakMinutes {
    private String shiftId;
    private String shiftType;
    private LocalDateTime shiftCreatedDatetime;
    private LocalTime startTime;
    private LocalTime endTime;
    private int breakTime;
    private int plannedTime;
}
