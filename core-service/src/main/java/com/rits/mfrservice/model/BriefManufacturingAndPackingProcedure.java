package com.rits.mfrservice.model;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BriefManufacturingAndPackingProcedure {


    private String title;
    private List<BriefManufacturingAndPackingProcedureFields> data;
    private String tableId;

}
