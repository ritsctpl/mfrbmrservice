package com.rits.oeeservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OeeByOrderResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String shoporderId;
    private List<OeeOrderData> oeeByOrder;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class OeeOrderData {
        private double availability;
        private double performance;
        private double quality;

    }


}
