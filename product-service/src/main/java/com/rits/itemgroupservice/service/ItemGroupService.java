package com.rits.itemgroupservice.service;

import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.itemgroupservice.dto.Extension;
import com.rits.itemgroupservice.dto.GroupNameResponseList;
import com.rits.itemgroupservice.dto.ItemGroupRequest;
import com.rits.itemgroupservice.dto.ItemList;
import com.rits.itemgroupservice.model.ItemGroup;
import com.rits.itemgroupservice.model.ItemMessageModel;

import java.util.List;

public interface ItemGroupService {
    ItemMessageModel createItemGroup(ItemGroupRequest itemGroupRequest) throws Exception;

    ItemMessageModel updateItemGroup(ItemGroupRequest itemGroupRequest) throws Exception;

    GroupNameResponseList getGroupNameListCreationDate(String site) throws Exception;

    GroupNameResponseList getGroupNameList(String site, String itemGroup) throws Exception;

    ItemGroup retrieveItemGroup(String site, String itemGroup) throws Exception;

    ItemGroup associateItemBOtoItemGroup(String site, String itemGroup, List<String> item) throws Exception;

    ItemGroup removeItemBOFromItemGroup(String site, String itemGroup, List<String> itemList) throws Exception;

    ItemMessageModel deleteItemGroup(String site, String itemGroup, String userId) throws Exception;

    Boolean isItemGroupExist(String site, String itemGroup) throws Exception;

    ItemList getAvailableItems(String site, String itemGroup) throws Exception;

    List<ItemGroup> retrieveAllBySite(String site);

    String callExtension(Extension extension);

    AuditLogRequest createAuditLog(ItemGroupRequest itemGroupRequest);

    AuditLogRequest updateAuditLog(ItemGroupRequest itemGroupRequest);

    AuditLogRequest deleteAuditLog(ItemGroupRequest itemGroupRequest);

}
