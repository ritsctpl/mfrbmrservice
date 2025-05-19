package com.rits.pcustepstatus.model;

import com.rits.startservice.model.MessageDetails;
import com.rits.startservice.model.Start;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private Start response;
//    private List<MessageDetails> message_details;
    private MessageDetails message_details;
}
