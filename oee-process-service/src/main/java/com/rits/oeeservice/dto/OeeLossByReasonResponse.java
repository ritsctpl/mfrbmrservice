package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OeeLossByReasonResponse {
    private String startTime;
    private String endTime;
    private List<OeeLossReasonData> oeeLossByReason;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OeeLossReasonData {
        private String reason;
        private Integer lossPercentage;
        private Integer cumulativePercentage;
        // Getters and Setters
    }

    // Getters and Setters
}
