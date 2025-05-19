package com.rits.shiftservice.dto;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftInput {
    public String site;
    public String resource;
    public String workcenter;
    public LocalDateTime startDateTime;
    public LocalDateTime endDateTime;

}
