package com.rits.assemblyservice.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private Assembly response;
    private MessageDetails message_details;
}
