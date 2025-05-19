package com.rits.overallequipmentefficiency.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityRequest {

    private String site;
    private String resourceId;
    private String workcenterId;
    private int interval;
    private String shiftId;
    private List<String> shiftIds;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int dynamicBreak;
    private int eventIntervalSeconds;
    private String shiftRef;

}
