package com.rits.inventoryservice.dto;

import com.rits.inventoryservice.model.InventoryDataDetails;
import com.rits.inventoryservice.model.InventoryLocation;
import com.rits.inventoryservice.model.NewInventoryDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {
    private String site;
    private String handle;
    private String inventoryId;
    private String batchNumber;
    private String description;
    private String status;
    private String item;
    private String version;
    private double receiveQty;
    private double qty;
    private double originalQty;
    private double remainingQty;
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

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reasonCode;
    private Map<String,Integer> inventoryMap;
    private List<NewInventoryDetails> newInventoryDetails;
    private List<InventoryRequest> inventoryRequestList;
}
