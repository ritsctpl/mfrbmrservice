package com.rits.productionlogservice.dto;

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
    private int totalQtyCompleted; // Change type to int
    private int totalActualCycleTime; // Change type to int
}
