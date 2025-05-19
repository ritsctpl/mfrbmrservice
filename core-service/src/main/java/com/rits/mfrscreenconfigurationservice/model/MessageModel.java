package com.rits.mfrscreenconfigurationservice.model;

import com.rits.mfrscreenconfigurationservice.model.MessageDetails;
import com.rits.mfrscreenconfigurationservice.model.MFRScreenConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private MFRScreenConfiguration response;
    private MessageDetails message_details;
}