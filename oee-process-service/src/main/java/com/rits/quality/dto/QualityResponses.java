package com.rits.quality.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityResponses {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resourceId;
    private List<QualityOverTime> qualityOverTime;
}
