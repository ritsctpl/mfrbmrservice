package com.rits.toolgroupservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToolGroupMessageModel {
    private ToolGroup response;
    private MessageDetails message_details;
}
