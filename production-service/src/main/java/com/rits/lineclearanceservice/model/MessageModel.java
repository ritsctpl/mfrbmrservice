package com.rits.lineclearanceservice.model;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private LineClearance checklistTemplates;
    private MessageDetails message_details;
}
