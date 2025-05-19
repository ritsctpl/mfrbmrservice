package com.rits.inventoryservice.dto;

import com.rits.inventoryservice.model.InventoryDataDetails;
import com.rits.inventoryservice.model.InventoryLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class InventoryResponse {
    private String inventoryId;
    private String item;
    private int qty;
    private int receiveQty;
    private String version;
    private String receiveDateOrTime;
    private String remainingQty;
    private String status;
//    private String usageCount;//handling unit number
//    private String maximumUsageCount;//max handling unit number
    private String receiveBy;
    private List<String> tags;
    private List<InventoryDataDetails> inventoryIdDataDetails;
    private List<InventoryLocation> inventoryIdLocation;

}
