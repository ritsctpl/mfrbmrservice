package com.rits.quality.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRequest {
    private String site;
    private LocalDateTime localDateTime;
}
