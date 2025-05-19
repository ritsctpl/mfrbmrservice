package com.rits.recipemaintenanceservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ParameterList {

    private String parameter;
    private String description;
    private String value;
    private String unitOfMeasure;
}
