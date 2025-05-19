package com.rits.mfrservice.model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ManufacturingFlowChart {
    private String procedureDescription;

    private String title;
    private List<ManufacturingFlowChartFields> data;
    private String tableId;

}
