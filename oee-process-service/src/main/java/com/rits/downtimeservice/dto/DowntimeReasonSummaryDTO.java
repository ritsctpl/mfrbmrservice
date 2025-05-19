package com.rits.downtimeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DowntimeReasonSummaryDTO {
    private String reason;
    private Long downtimeDuration; // aggregated downtime duration (in seconds)
}
