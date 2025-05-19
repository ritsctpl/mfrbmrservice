package com.rits.machinestatusservice.dto;

import lombok.*;

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
