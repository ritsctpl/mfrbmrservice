package com.rits.nonconformanceservice.model;

import com.rits.dispositionlogservice.model.DispositionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private NcData response;
    private MessageDetails message_details;
}
