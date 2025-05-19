package com.rits.reasoncodeservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReasonCodeMessageModel {
    private ReasonCode response;
    private MessageDetails message_details;
}
