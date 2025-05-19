package com.rits.overallequipmentefficiency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineLogResponse {
    private Long machineLogId;
    private String siteId;
    private String shiftId;
    private LocalDateTime shiftCreatedDateTime;
    private LocalDateTime createdDateTime;
    private String logMessage;
    private String logEvent;
    private Integer active;
}
