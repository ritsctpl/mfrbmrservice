package com.rits.productionlogservice.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftIntervalWithDate {
    private LocalDate date;
    private BreakMinutes breakMinutes;
}
