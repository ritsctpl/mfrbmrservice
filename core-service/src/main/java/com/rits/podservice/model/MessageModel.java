package com.rits.podservice.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {

    private Pod response;
    private MessageDetails message_details;
}
