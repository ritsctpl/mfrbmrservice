package com.rits.pcudoneservice.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private PcuDoneNoBO response;
    private MessageDetails message_details;
}
