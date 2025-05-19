package com.rits.overallequipmentefficiency.dto;

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
public class DowntimeRequest {
//    private String site;
//    private String resource;
//    private String workcenter;
//    private String start_time;
//    private String end_time;
//    private String shift;
//    private String event;
//    private String logMessage;
    private String resourceId;
    private String workcenterId;
    private String site;
    private String shiftId;
    private LocalDateTime shiftCreatedDateTime;
    private LocalDateTime shiftBreakStartDatetime;
    private LocalDateTime downtimeStart;
    private LocalDateTime downtimeEnd;
    private Integer downtEvent;
    private String reason;
    private String rootCause;
    private String commentUsr;
    private String downtimeType;
    private LocalDateTime dateTime;

    private List<String> resourceList;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private List<String> workcenterList;
    private List<String> resourceIds;
    private LocalDateTime intervalStart;
    private LocalDateTime intervalEnd;
}
