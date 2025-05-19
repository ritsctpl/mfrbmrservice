package com.rits.shoporderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NextNumber {
    private String site;
    private String numberType;
    private String parentOrder;
}
