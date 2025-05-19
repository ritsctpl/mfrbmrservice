package com.rits.downtimeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DowntimeEventDTO {
    private String resourceId;
    private LocalDateTime downtimeStart;
    private LocalDateTime downtimeEnd;
    private long downTimeDuration;
}
