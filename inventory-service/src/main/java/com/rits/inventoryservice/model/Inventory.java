package com.rits.inventoryservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "R_INVENTORY")
public class Inventory {

    private String site;
    @Id
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
    private List<String> tags;
    private List<InventoryDataDetails> inventoryIdDataDetails;
    private List<InventoryLocation> inventoryIdLocation;
    private int usageCount ;
    private int maximumUsageCount ;
    private boolean  inUsage;
    private boolean splittedInventory;
    private String  parentInventoryId;
    private LocalDateTime  partitionDate;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;


}
