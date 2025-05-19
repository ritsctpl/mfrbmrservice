package com.rits.mfrservice.model;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ManufacturingProcedure {
    private String phase;
    private String procedureDescription;
    private String observation;
}
