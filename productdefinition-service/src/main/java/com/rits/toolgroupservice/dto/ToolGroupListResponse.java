package com.rits.toolgroupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolGroupListResponse {

    private String toolGroup;

    private String description;
    private String status;
    private String trackingControl;
    private String location;

    private String timeBased;
    private String erpGroup;
    private int toolQty;
}
