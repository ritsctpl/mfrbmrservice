package com.rits.startservice.model;

import com.rits.startservice.dto.PcuInQueue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PcuInQueueMessageModel {
    private PcuInQueue response;
    private MessageDetails message_details;
}
