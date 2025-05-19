package com.rits.dispositionlogservice.model;

import com.rits.pcuinqueueservice.model.PcuInQueue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private DispositionLog response;
    private MessageDetails message_details;
}
