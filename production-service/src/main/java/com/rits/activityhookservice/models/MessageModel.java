package com.rits.activityhookservice.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private ActivityHook activityHook;
    private MessageDetails message_details;
}
