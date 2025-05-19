package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Waste {
    private String sequence;
    private String wasteId;
//    private String wasteName;
    private String description;
    private String quantity;
    private String uom;
    private String handlingProcedure;
    private String costOfDisposal;
}
