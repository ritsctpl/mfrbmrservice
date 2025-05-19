package com.rits.pcucompleteservice.model;

import com.rits.pcucompleteservice.dto.PcuCompleteReq;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private PcuCompleteReq response;
    private MessageDetails message_details;
}
