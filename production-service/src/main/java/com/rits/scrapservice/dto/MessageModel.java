package com.rits.scrapservice.dto;

import com.rits.pcurouterheaderservice.model.MessageDetails;
import com.rits.pcurouterheaderservice.model.PcuRouterHeader;
import com.rits.pcurouterheaderservice.model.RoutingStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private PcuRouterHeader response;
    private List<MessageDetails> message_details;
}
