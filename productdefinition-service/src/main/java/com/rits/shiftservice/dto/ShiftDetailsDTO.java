package com.rits.shiftservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftDetailsDTO {
    private String shiftId;
    private LocalDateTime shiftCreatedDatetime;
    private LocalTime breakStartTime;
    private String handle;

}
