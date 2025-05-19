package com.rits.shiftservice.dto;

import java.time.LocalTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakDetails {
    public LocalTime breakStartTime;
    public LocalTime breakEndTime;
    public int meanTime;
}
