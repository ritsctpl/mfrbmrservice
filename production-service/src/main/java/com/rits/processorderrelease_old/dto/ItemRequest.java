package com.rits.processorderrelease_old.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemRequest {
    private String item;
    private String revision;
    private  String site;
    private String lotSize;
}
