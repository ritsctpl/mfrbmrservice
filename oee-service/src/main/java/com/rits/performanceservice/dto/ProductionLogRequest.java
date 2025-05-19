package com.rits.performanceservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionLogRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resource;
    private String site;
    private String shift;
    private String itemBO;
    private String operationBO;
    private String routingBO;
    private String shopOrderBO;
}
