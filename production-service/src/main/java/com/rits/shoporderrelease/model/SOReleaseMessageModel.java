package com.rits.shoporderrelease.model;


import com.rits.shoporderrelease.dto.PcuHeader;
import com.rits.shoporderrelease.dto.ShopOrder;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SOReleaseMessageModel {
    private List<PcuHeader> pcuHeaderResponse;
    private MessageDetails message_details;
    private String message;
    private String nextNo;
    private ShopOrder shopOrderResponse;
    private List<String> generatedNextNo;
    private List<MessageDetails> pcuHeaderMessage_details;
}
