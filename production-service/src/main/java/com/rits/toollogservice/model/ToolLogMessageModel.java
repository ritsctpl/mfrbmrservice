package com.rits.toollogservice.model;

import com.rits.startservice.model.Start;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToolLogMessageModel {
    private ToolLog response;
    private MessageDetails message_details;
}
