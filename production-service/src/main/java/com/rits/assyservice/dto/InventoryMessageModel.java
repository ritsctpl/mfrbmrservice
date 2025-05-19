package com.rits.assyservice.dto;

import com.rits.assemblyservice.dto.Inventory;
import com.rits.assemblyservice.model.MessageDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryMessageModel {

    private Inventory response;
    private MessageDetails message_details;
}
