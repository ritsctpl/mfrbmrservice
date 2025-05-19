package com.rits.mfrservice.model;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BillOfMaterialsFields {
    private String siNo;
    private String materialCode;
    private String materialDescription;
    private String grade;
    private String inciName;
    private String function;
    private String casNo;
    private String uom;
    private String qtyPerBatch;
    private String ingredients;


}
