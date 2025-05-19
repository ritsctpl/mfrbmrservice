package com.rits.bomservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ComponentUsageRequest {
    private String component;
    private String site;
    private String componentVersion;
}
