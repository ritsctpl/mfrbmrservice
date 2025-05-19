package com.rits.oeeservice.dto;

import com.rits.oeeservice.model.Oee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OeeByComponentResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<OeeComponentData> oeeByComponent;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OeeComponentData {
        private String date;
        private double availability;
        private double performance;
        private double quality;
    }
}
