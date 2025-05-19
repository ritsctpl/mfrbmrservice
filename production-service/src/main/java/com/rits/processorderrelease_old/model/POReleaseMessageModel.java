package com.rits.processorderrelease_old.model;


import com.rits.processorderrelease_old.dto.BnoHeader;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class POReleaseMessageModel {
    private List<BnoHeader> pcuHeaderResponse;
    //private MessageDetails message_details;
    private String message;
    private String nextNo;
    //private ShopOrder shopOrderResponse;
    private List<String> generatedNextNo;
    //private List<MessageDetails> pcuHeaderMessage_details;
}
