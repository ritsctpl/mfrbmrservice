package com.rits.shoporderservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderResponse {
    private String shopOrder;
    private String plannedMaterial;
    private String orderType;
    private LocalDateTime plannedCompletion;
    private String status;

}
