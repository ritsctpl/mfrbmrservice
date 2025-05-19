package com.rits.overallequipmentefficiency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DowntimeSummary {
    private long totalDowntime;
    private String majorReason;
    private String majorRootCause;

    // Getters and setters (or use Lombok @Data for brevity)

}

