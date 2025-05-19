package com.rits.performance.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PerformanceRequest {

    private String site;
    private List<String> shiftIds;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int eventIntervalSeconds;
    private String resourceId;
    private String workcenterId;
    private String shoporderNo;
    private String batchNumber;
    private String operation;
    private String operationVersion;
    private String shiftId;

}
