package com.rits.batchnocomplete.model;

import com.rits.batchnocomplete.dto.BatchNoCompleteDTO;
import com.rits.nextnumbergeneratorservice.model.MessageDetails;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BatchNoCompleteMsgModel {
    private BatchNoComplete response;
    private MessageDetails message_details;
}
