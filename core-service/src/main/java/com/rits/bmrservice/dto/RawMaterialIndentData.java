package com.rits.bmrservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterialIndentData {
    List<RawMaterialIndentRecord> rawMaterialIndentData;
    private String dataField;
}
