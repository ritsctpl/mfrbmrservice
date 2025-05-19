package com.rits.dccollect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private DataCollection response;
    private MessageDetails message_details;
    private DcParametricPreSave dcParametricPreSave;
}
