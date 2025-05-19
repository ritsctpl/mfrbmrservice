package com.rits.batchnoyieldreportingservice.model;

import com.rits.batchnoyieldreportingservice.dto.BatchNoYieldReportingRequest;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private BatchNoYieldReporting response;
    private MessageDetails message_details;
}
