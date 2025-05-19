package com.rits.itemservice.service;

import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.itemservice.dto.*;
import com.rits.itemservice.model.Item;
import com.rits.itemservice.model.ItemMessageModel;
import com.rits.itemservice.model.PrintDocument;

import java.util.List;


public interface ItemService {
    ItemMessageModel createItem(ItemRequest item) throws Exception;

    ItemMessageModel deleteItem(String item, String revision, String site, String userId) throws Exception;

    ItemMessageModel updateItem(ItemRequest item) throws Exception;

    ItemResponseList getItemListByCreationDate(String site) throws Exception;

    ItemResponseList getItemList(String item, String site) throws Exception;

    Boolean isItemExist(String item, String revision, String site) throws Exception;

    Item retrieveItem(String item, String revision, String site) throws Exception;

    List<ItemResponse> retrieveAllItem(String site) throws Exception;

    String callExtension(Extension extension);

    AuditLogRequest createAuditLog(ItemRequest itemRequest);

    AuditLogRequest deleteAuditLog(ItemRequest itemRequest);

    AuditLogRequest updateAuditLog(ItemRequest itemRequest);


    PrintDocuments getAvailableDocument(String site, String item, String revision) throws Exception;

    PrintDocuments associateDocumentToPrintDocument(String site, String item, String revision, List<PrintDocument> printDocuments) throws Exception;

    PrintDocuments removeDocumentFromPrintDocument(String site, String item, String revision, List<PrintDocument> printDocuments) throws Exception;

    ItemResponseListForFilter getItemListByCreationDateItem(String site);
}
