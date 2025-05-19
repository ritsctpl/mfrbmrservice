package com.rits.recipemaintenanceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Waste {
    private String wasteId;
    private String sequence;
    private String description;
    private String quantity;
    private String uom;
    private String handlingProcedure;
    private String costOfDisposal;
}
