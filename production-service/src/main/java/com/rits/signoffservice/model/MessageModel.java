package com.rits.signoffservice.model;

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
    private SignOff response;
//    private List<MessageDetails> message_details;
    private MessageDetails message_details;
}
