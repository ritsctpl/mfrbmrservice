package com.rits.downtimeservice.dto;

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
    private String shiftName;
    private String description;
    private String shiftType;
    private String workCenter;
    private String resource;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long shiftMeanTime;
    private Long actualTime;
    private LocalDateTime validFrom;
    private LocalDateTime validEnd;
    private List<Break> breakList;
    private List<Break> calendarList;
    private String userId;
}
