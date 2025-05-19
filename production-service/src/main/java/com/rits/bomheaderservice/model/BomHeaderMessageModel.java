package com.rits.bomheaderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BomHeaderMessageModel {
    private BomHeader response;
    private MessageDetails message_details;
}
