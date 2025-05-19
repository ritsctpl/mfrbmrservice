package com.rits.assemblyservice.dto;

import com.rits.assemblyservice.model.MessageDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BomHeaderMessageModel {

    private MessageDetails message_details;
}
