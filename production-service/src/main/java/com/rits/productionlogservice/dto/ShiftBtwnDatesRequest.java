package com.rits.productionlogservice.dto;

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
    private String shiftName;
    private String description;
    private String shiftType;
    private String workCenter;
    private String resource;
    private String userId;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
}
