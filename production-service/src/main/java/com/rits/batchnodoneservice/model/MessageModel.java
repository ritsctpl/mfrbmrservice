package com.rits.batchnodoneservice.model;

import com.rits.batchnodoneservice.dto.BatchNoDoneRequest;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private BatchNoDone response;
    private MessageDetails message_details;
}
