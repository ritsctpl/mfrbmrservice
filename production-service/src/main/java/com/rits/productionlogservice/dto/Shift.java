package com.rits.productionlogservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Shift {
    @Id
    private String handle;
    private String site;
    private String shiftName;
    private String description;
    private String shiftType;
    private String workCenter;
    private String resource;
    private List<ShiftIntervals> shiftIntervals;
    private List<Break> calendarList;
    private int active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
