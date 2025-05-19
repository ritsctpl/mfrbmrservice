package com.rits.downtimeservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Minutes {
    private String shiftName;
    private String shiftType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long minutes;
}
