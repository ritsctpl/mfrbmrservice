package com.rits.productionlogservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionLogQueryRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resource;
    private String site;
    private String  shift;
    private String itemBO;
    private String operationBO;
    private String routingBO;
    private String shopOrderBO;
}
