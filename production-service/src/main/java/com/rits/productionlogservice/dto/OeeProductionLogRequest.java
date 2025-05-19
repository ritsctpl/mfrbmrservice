package com.rits.productionlogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
