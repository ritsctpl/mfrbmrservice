package com.rits.overallequipmentefficiency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor  // Adds a default no-args constructor
@AllArgsConstructor
public class IntervalTimesResponse {
    private LocalDateTime firstCreatedDateTime;
    private LocalDateTime lastCreatedDateTime;
}