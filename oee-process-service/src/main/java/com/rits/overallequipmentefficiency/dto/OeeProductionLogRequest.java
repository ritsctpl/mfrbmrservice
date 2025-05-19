package com.rits.overallequipmentefficiency.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OeeProductionLogRequest {
    private String site;
    private String resourceId;
    private LocalDateTime intervalStartDateTime;
    private LocalDateTime intervalEndDateTime;
    private String itemId;
    private String itemVersion;
    private String workcenterId;
    private String operationId;
    private String operationVersion;
    private String eventType; // Added eventType
    private String shiftId; // New field
    private String shopOrderBo; // New field
    private String batchNo;
    private String pcu;
    private String eventSource;

    // Getters and Setters
}
