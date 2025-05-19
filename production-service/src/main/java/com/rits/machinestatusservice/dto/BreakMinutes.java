package com.rits.machinestatusservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakMinutes {
    private String shiftName;
    private String shiftType;
    private String startTime;
    private String endTime;
    private int breakTime;
}
