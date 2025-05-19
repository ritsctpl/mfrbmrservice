package com.rits.pcurouterheaderservice.model;

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
    private String nextStepId;
    private List<RoutingStep> routingStep;
    private MessageDetails messagedetails;
}
