package com.rits.recipemaintenanceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class QCParameter {
    private String parameter;
    private String value;// actual
    private String tolerance;
}
