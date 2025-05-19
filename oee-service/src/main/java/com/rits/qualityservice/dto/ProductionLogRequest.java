package com.rits.qualityservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionLogRequest {
    private String site;
    private String itemBO;
    private String operation_bo;
    private String routerBO;
    private String shiftStartTime;
    private String entryTime;
    private String eventPerformance;
    private String resourceBO;
    private String shopOrderBO;
}
