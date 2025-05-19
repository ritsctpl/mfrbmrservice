package com.rits.nextnumbergeneratorservice.model;

import com.rits.nextnumbergeneratorservice.dto.GeneratedNextNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NextNumberMessageModel {
    private NextNumberGenerator response;
    private MessageDetails message_details;
    private GeneratedNextNumber generatedNextNumberResponse;
    private String nextNo;
    private String exception;
}
