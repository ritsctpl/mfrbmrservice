package com.rits.machinestatusservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MachineStatusRequest {
    private String event;
    private String resource;
    private String workcenter;
    private String shift;
    private String downtimeStart;
    private String downtimeEnd;
    private String meantime;
    private String shiftStartTime;
    private String shiftEndTime;
    private String shiftAvailableTime;
    private String breakHours;
    private String createdDateTime;
    private String createdDate;
    private String site;
    private String reasonCode;
    private int processed;
    private int active;
    private String shiftId;
    private String siteId;
    private String itemId;
    private String logMessage;
    private String operationId;
    private LocalDateTime modifiedDateTime;
    private LocalDateTime shiftBreakCreatedDateTime;
    private LocalDateTime shiftCreatedDateTime;
}
