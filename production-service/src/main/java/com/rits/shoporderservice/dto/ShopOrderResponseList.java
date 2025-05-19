package com.rits.shoporderservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderResponseList {
    private List<ShopOrderResponse> shopOrderResponseList;
}
