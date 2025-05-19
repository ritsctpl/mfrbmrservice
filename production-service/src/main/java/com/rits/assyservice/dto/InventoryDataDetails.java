package com.rits.assyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class InventoryDataDetails {
    private String  reasonCode;
    private String comment;
    private int serialNumber;

}
