package com.rits.batchnophaseprogressservice.model;

import com.rits.batchnophaseprogressservice.dto.BatchNoPhaseProgressRequest;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private BatchNoPhaseProgress response;
    private MessageDetails message_details;
}
