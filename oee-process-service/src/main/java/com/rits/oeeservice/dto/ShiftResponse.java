package com.rits.oeeservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftResponse {

    private String handle;
    private String site;
    private String shiftId;
    private String version;
    private String shiftType;
    private String workCenterId;
    private String resourceId;
    private int active;
    private String description;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int shiftMeanTime;
    private int actualTime;
    private String createdBy;
    private String modifiedBy;
    private String userId;
    private List<ShiftIntervals> shiftIntervals;
    private List<CalendarRules> calendarRules;
    private List<CalendarOverrides> calendarOverrides;
    private List<CustomData> customDataList;

    public ShiftResponse(Shift shift) {
        this.handle = shift.getHandle();
        this.site = shift.getSite();
        this.shiftId = shift.getShiftId();
        this.shiftType=shift.getShiftType();
        this.description=shift.getDescription();
        this.version = shift.getVersion();
        this.createdDateTime = shift.getCreatedDateTime();
        this.modifiedDateTime = shift.getModifiedDateTime();
        this.shiftMeanTime = shift.getShiftMeanTime();
        this.actualTime = shift.getActualTime();
        this.active=shift.getActive();
        this.resourceId=shift.getResourceId();
        this.workCenterId=shift.getWorkCenterId();

    }

    // Static method to create a list of ShiftResponse from a list of Shift
    public static List<ShiftResponse> fromShiftList(List<Shift> shifts) {
        return shifts.stream().map(ShiftResponse::new).collect(Collectors.toList());
    }

}
