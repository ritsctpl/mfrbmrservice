package com.rits.mfrservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Phase {
    private String phaseName;
    private String procedureDescription;
    private String observation;
}
