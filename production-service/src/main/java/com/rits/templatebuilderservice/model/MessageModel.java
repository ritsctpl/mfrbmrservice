package com.rits.templatebuilderservice.model;

import com.rits.componentbuilderservice.model.Component;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private Template response;
    private MessageDetails message_details;
}
