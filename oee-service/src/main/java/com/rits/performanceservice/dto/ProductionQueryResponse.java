package com.rits.performanceservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionQueryResponse {
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String resourceBO;
    private String shoporderBO;
    private String workcenterBO;
    private int totalActualCycleTime;
    private int totalQtyCompleted;
    private String material;
    private String materialVersion;
    private String routing;
    private String routingVersion;
    private String operation;
    private String operationVersion;
    private String shopOrder;
    private String  resource;
    private String site;
}
