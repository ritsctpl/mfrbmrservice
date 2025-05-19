package com.rits.shiftservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityRequestForDowntime {

    private String site;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resourceId;
    private String workcenterId;
    private String shiftId;
    private List<String> shiftIds;
    private int dynamicBreak;
}
