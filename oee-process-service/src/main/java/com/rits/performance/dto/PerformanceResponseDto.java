package com.rits.performance.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class PerformanceResponseDto {
    private String site;
    private String workcenterId;
    private String operation;
    private String operationVersion;
    private String resourceId;
    private String item;
    private String itemVersion;
    private String shiftId;
    private String pcu;
}