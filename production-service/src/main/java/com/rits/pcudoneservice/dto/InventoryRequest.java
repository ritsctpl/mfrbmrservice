package com.rits.pcudoneservice.dto;

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
public class InventoryRequest {
    private String site;
    private String handle;
    private String inventoryId;
    private String description;
    private String status;
    private String item;
    private String version;
    private int receiveQty;
    private double qty;
    private int originalQty;
    private String remainingQty;
    private String receiveDateOrTime;
    private String receiveBy;
    private List<InventoryDataDetails> inventoryIdDataDetails;
    private List<InventoryLocation> inventoryIdLocation;
    private int usageCount ;
    private int maximumUsageCount ;
    private boolean  inUsage;
    private boolean splittedInventory;
    private String  parentInventoryId;
    private String  partitionDate;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

}
