package com.rits.pcucompleteservice.dto;

import com.rits.pcucompleteservice.model.MessageDetails;
import com.rits.pcucompleteservice.model.PcuComplete;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PcuDoneMessageModel {
    private PcuComplete response;
    private MessageDetails message_details;
}
