package com.rits.pcucompleteservice.dto;

import com.rits.pcucompleteservice.model.MessageDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PcuRouterHeaderMessageModel {
    private List<MessageDetails> message_details;
    private String nextStepId;
    private List<RoutingStep> routingStep;
    private MessageDetails messagedetails;
}
