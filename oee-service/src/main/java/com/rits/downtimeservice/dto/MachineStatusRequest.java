package com.rits.downtimeservice.dto;

import lombok.*;

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
}
