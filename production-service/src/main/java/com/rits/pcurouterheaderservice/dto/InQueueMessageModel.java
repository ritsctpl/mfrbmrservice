package com.rits.pcurouterheaderservice.dto;

import com.rits.pcurouterheaderservice.model.MessageDetails;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InQueueMessageModel {
     private PcuInQueue response;
    private MessageDetails message_details;
}
