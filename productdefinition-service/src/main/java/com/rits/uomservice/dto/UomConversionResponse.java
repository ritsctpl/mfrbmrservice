package com.rits.uomservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UomConversionResponse {
    private String baseUnit;
    private String convertionItem;
    private String baseAmt;
    private String conversionUnit;
    private String material;
    private String materialVersion;

}
