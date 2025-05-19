package com.rits.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataField {
    private String sequence;
    private String dataField;
    private boolean required;
    private String description;
    private String dataType;
}
