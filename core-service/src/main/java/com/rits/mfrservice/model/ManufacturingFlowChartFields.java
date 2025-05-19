package com.rits.mfrservice.model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ManufacturingFlowChartFields {
    private String phase;
    private String procedureDescription;
    private String observation;

}
