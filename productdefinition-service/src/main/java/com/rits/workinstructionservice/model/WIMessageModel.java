package com.rits.workinstructionservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WIMessageModel {
    private WorkInstruction response;
    private MessageDetails message_details;
}
