package com.rits.scrapservice.model;

import com.rits.shoporderservice.model.ShopOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScrapMessageModel {
    private Scrap shopOrderResponse;
    private MessageDetails message_details;
}
