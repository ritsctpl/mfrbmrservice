package com.rits.shoporderrelease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class BomRequest {
    private String site;
    private String revision;
    private String bom;
}
