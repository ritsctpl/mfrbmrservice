package com.rits.pcurouterheaderservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ShopOrderRequest {
    private String site;
    private String shopOrder;
}
