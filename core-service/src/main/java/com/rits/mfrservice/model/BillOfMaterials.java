package com.rits.mfrservice.model;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BillOfMaterials {

    private String title;
    private List<BillOfMaterialsFields> data;
    private String tableId;
}
