package com.rits.mfrservice.model;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ManufacturingStep {
    private int stepNumber;
    private String description;
}
