package com.rits.queryBuilder.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private QueryBuilder response;
    private MessageDetails message_details;
}
