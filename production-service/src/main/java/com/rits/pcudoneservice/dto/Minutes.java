package com.rits.pcudoneservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Minutes {
    private String shiftName;
    private String shiftType;
    private LocalTime startTime;
    private LocalTime  endTime;
    private long minutes;
}
