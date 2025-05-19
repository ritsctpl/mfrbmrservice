package com.rits.nextnumbergeneratorservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ItemResponse {
    private String item;
    private String revision;
    private String description;
    private String status;
    private String procurementType;
    private String lotSize;

}

