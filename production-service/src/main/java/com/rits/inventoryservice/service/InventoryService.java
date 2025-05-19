package com.rits.inventoryservice.service;

import com.rits.inventoryservice.dto.*;
import com.rits.inventoryservice.model.Inventory;
import com.rits.inventoryservice.model.InventoryMessageModel;

import java.util.List;

public interface InventoryService {
    public InventoryMessageModel createInventory(InventoryRequest inventoryRequest) throws Exception;

    public InventoryMessageModel updateInventory(InventoryRequest inventoryRequest) throws Exception;

    public InventoryMessageModel deleteInventory(InventoryRequest inventoryRequest) throws Exception;

    public Boolean isInUsage(InventoryRequest inventoryRequest) throws Exception;

    public List<Inventory> inventorySplit(InventorySplitRequest request) throws Exception;

    public  Inventory retrieveInventory(InventoryRequest inventoryRequest) throws Exception;

    Inventory retrieveInventoryReturn(InventoryRequest inventoryRequest) throws Exception;

    InventoryResponseList getTop1000InventoryListByCreationDate(InventoryRequest inventoryRequest) throws Exception;

    List<Inventory> getInventoryListByCreationDate(InventoryRequest inventoryRequest) throws Exception;

    InventoryResponseList getInventoryList(InventoryRequest inventoryRequest) throws Exception;
    public  InventoryResponseList retrieveInventoryListByItem(InventoryRequest inventoryRequest) throws Exception;
    InventoryMessageModel updateRemainingQty(String site, String inventoryId, String remainingQty) throws Exception;

    String callExtension(Extension extension);

    List<DataField>  retrievedDataFieldByDataType(String site, String item, String itemVersion);
    List<String> checkInventoryList(InventorySplitRequest inventoryListRequest);
    InventoryRequest findByInventoryId(String inventoryId);

    boolean isBatchNumberExists(String batchNumber, String site);
}
