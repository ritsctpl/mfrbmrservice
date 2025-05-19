package com.rits.groupbuilderservice.model;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private GroupBuilder groupBuilder;
    private MessageDetails message_details;
}
