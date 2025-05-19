package com.rits.shiftservice.dto;

import com.rits.shiftservice.model.Break;
import com.rits.shiftservice.model.CustomData;
import com.rits.shiftservice.model.ShiftIntervals;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftBtwnDatesRequest {
    private String site;
    private String shiftId;
    private String description;
    private String shiftType;
    private String workCenterId;
    private String resourceId;
    private List<ShiftIntervals> shiftIntervals;
    private List<CustomData> customDataList;
    private List<Break> calendarList;
    private String userId;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private String shiftRef;
}
