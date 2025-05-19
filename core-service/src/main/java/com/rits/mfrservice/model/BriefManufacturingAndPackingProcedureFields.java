package com.rits.mfrservice.model;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BriefManufacturingAndPackingProcedureFields {

    private String phase;
    private String procedureDescription;
    private String observation;

    private String criticalControlPoints;

    private String dosAndDonts;

    private String materialDescription;
    private String quantity;

}
