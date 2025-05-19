package com.rits.inventoryservice.dto;

import com.rits.inventoryservice.model.InventoryLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventorySplitRequest {

    private String  site;
    private String  handle;
    private String  parentInventoryId;
    private String  item;
    private String  version;
    private double  qtyonHand;
    private double  qtyToSplit;
    private int  NoOfInventoryIds ;
    private List<InventoryLocation> inventoryIdLocation;
    private List<InventoryIds>  inventoryIds  ;
    private List<String>  inventoryList  ;
    private String productionSupplyArea;
    private String   storageBin;
    private String    handlingUnitNumber;
    private String      masterHandlingUnitNumber;

}
