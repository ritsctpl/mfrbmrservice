package com.rits.uomservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UOMRequest {
    private Integer id;
    private String uomCode;
    private String description;
    private Double conversionFactor;
    private String status;
    private String site;
    private Integer active;

    private String baseUnit;
    private String convertionItem;
    private String baseAmt;
    private String conversionUnit;
    private String material;
    private String materialVersion;
    private Double givenValue;
    private String user;
}
