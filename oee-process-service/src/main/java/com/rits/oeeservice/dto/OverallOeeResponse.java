package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OverallOeeResponse {
    /*private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resourceId;
    private String shiftId;*/

    private double percentage;
}
