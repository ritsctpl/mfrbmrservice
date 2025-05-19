package com.rits.toolnumberservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToolNumberMessageModel {
    private ToolNumber response;
    private MessageDetails message_details;
}
