package com.rits.shiftservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailabilityRequestForShift {

    private String site;
    private List<String> shiftIds;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int dynamicBreak;
}
