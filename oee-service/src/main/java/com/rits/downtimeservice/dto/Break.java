package com.rits.downtimeservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Break {
    private String uniqueId;
    private String breakId;
    private String shiftType;
    private LocalDateTime breakTimeStart;
    private LocalDateTime breakTimeEnd;
    private String meanTime;
}
