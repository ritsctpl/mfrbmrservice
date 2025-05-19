package com.rits.shoporderrelease.dto;

import com.rits.nextnumbergeneratorservice.dto.GeneratedNextNumber;
import com.rits.nextnumbergeneratorservice.model.MessageDetails;
import com.rits.nextnumbergeneratorservice.model.NextNumberGenerator;
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
