package com.rits.performanceservice.dto;

import com.rits.downtimeservice.model.DownTimeAvailability;
import com.rits.performanceservice.model.Performance;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PerformanceRequestList {
    List<DownTime> performanceRequestList;
    private List<Performance> performanceResponseList;
    private List<Combinations> combinations;
}

