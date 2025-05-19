package com.rits.pcucompleteservice.dto;

import com.rits.pcucompleteservice.model.MessageDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoutingMessageModel {
    private Routing response;
    private String nextStepId;
    private RoutingStep routingStep;
    private MessageDetails message_details;
}
