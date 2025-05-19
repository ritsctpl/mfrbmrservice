package com.rits.worklistservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderRequest {
    private String site;
    private String shopOrder;

}
