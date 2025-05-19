package com.rits.startservice.dto;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRequest {
    private String site;
    private String shiftType;
    private String resourceId;
    private String workCenterId;
    private LocalDateTime localDateTime;
}
