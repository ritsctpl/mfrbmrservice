package com.rits.productionlogservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShiftIntervals {
    private String startTime;
    private String endTime;
    private int shiftMeanTime;
    private int actualTime;
    private LocalDateTime validFrom;
    private LocalDateTime validEnd;
    private List<Break> breakList;
}
