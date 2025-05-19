package com.rits.productionlogservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class PerformanceResponse {
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