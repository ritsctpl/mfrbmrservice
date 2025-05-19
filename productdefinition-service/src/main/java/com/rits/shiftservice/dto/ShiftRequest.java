package com.rits.shiftservice.dto;

import com.rits.shiftservice.model.*;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRequest {
    private String site;
    private String handle;
    private String shiftId;
    private String description;
    private String shiftType;
    private String workCenterId;
    private String resourceId;
    private String version;
    private int shiftMeanTime;
    private int actualTime;
    private int active;
    private String modifiedBy;
    private String createdBy;
    private String userId;
    private List<ShiftIntervals> shiftIntervals;
    private List<CustomData> customDataList;
    private List<CalendarRules> calendarRules;
    private List<CalendarOverrides> calendarOverrides;
    private LocalDateTime localDateTime;
}

