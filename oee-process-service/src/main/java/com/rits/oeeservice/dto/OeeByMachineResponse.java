package com.rits.oeeservice.dto;

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
public class OeeByMachineResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<OeeMachineData> oeeByMachine;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OeeMachineData {
        private String resourceId;
        private double percentage;

    }


}
