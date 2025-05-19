package com.rits.processorderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessOrderMessageModel {
    private ProcessOrder processOrderResponse;
    private MessageDetails message_details;
    private List<ProcessOrder> processOrderResponses;
}
