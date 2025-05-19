package com.rits.bmrservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterialIndentRecord {
    private String phase;
    private String materialCode;
    private String materialDescription;
    private String uom;
    private double standardQty;
}
