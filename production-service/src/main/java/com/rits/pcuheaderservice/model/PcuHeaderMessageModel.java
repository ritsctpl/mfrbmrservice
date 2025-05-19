package com.rits.pcuheaderservice.model;

import com.rits.pcuheaderservice.dto.ShopOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PcuHeaderMessageModel {
    private  List<PcuHeader> pcuHeaderResponse;
    private List<MessageDetails> pcuHeaderMessage_details;
    private ShopOrder shopOrderResponse;
    private String message;
}
