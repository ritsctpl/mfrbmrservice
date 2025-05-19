package com.rits.downtimeservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentShiftDetails {
    private String shiftId;
    private LocalDateTime shiftCreatedDatetime;
    private LocalTime breakStartTime;
    private Long plannedOperatingTime;
    private Long breaktime;
    private Long nonproduction;
    private String shiftRef;
}
