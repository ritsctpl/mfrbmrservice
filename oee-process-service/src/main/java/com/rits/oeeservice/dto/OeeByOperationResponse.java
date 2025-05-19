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
public class OeeByOperationResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<OeeByOperation> oeeByOperation;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OeeByOperation {
        private String operation;
        private double percentage;

    }


}
