package com.rits.workflowstatesmasterservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private WorkFlowStatesMaster response;
    private MessageDetails message_details;
}
