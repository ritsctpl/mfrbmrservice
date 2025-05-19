package com.rits.machinestatusservice.dto;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftResponse {
    private String shiftName;
    private String description;
    private String shiftType;
    private String workCenter;
    private String resource;
    private LocalTime startTime;
    private LocalTime endTime;
}
