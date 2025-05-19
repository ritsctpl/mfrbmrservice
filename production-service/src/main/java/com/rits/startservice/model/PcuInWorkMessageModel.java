package com.rits.startservice.model;

import com.rits.startservice.dto.StartRequestDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PcuInWorkMessageModel {
    private StartRequestDetails response;
    private MessageDetails message_details;
}
