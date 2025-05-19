package com.rits.assemblyservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComponentResponse {
    private String component;
    private int qty;
    private String inventoryId;
}
