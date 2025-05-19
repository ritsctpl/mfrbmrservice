package com.rits.nextnumbergeneratorservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class InventoryNextNumberRequest {
    private String site;
    private String numberType;
    private String object;
    private String objectVersion;
    private Integer receivedQty;
    private double size;
    private String userBO;
    private String nextNumberActivity;
}
