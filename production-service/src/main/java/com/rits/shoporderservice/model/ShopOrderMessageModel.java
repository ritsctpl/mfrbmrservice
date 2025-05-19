package com.rits.shoporderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderMessageModel {
    private ShopOrder shopOrderResponse;
    private MessageDetails message_details;
    private List<ShopOrder> shopOrderResponses;
}
