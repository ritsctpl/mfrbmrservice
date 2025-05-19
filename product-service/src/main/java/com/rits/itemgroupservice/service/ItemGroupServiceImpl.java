package com.rits.itemgroupservice.service;

import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.itemgroupservice.dto.*;
import com.rits.itemgroupservice.exception.ItemGroupException;
import com.rits.itemgroupservice.model.GroupMemberList;
import com.rits.itemgroupservice.model.ItemGroup;
import com.rits.itemgroupservice.model.MessageDetails;
import com.rits.itemgroupservice.model.ItemMessageModel;
import com.rits.itemgroupservice.repository.ItemGroupRepository;
import com.rits.itemservice.service.ItemService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
@Service
public class ItemGroupServiceImpl implements ItemGroupService {
    private final ItemGroupRepository itemGroupRepository;
    private final ItemService itemService;
    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageSource localMessageSource;
    @Value("${item-service.url}/retrieveBySite")
    private String itemServiceUrl;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    @Value("${item-service.url}/retrieve")
    private String retrieveItemServiceUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public ItemMessageModel createItemGroup(ItemGroupRequest itemGroupRequest) throws Exception {
        if (itemGroupRepository.existsByItemGroupAndSiteAndActive(itemGroupRequest.getItemGroup(), itemGroupRequest.getSite(), 1)) {
            throw new ItemGroupException(113,itemGroupRequest.getItemGroup());
        }
        if (itemGroupRequest.getGroupDescription() == null || itemGroupRequest.getGroupDescription().isEmpty()) {
            itemGroupRequest.setGroupDescription(itemGroupRequest.getItemGroup());
        }
        try {
            getValidated(itemGroupRequest);
        } catch (ItemGroupException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
        if(itemGroupRequest.getGroupMemberList() != null && !itemGroupRequest.getGroupMemberList().isEmpty())
        {
            List<GroupMemberList> groupMemberLists = groupMemberListWithVersion(itemGroupRequest.getSite(),itemGroupRequest.getGroupMemberList());
            if(groupMemberLists != null && !groupMemberLists.isEmpty())
            {
                itemGroupRequest.setGroupMemberList(groupMemberLists);
            }
        }
        ItemGroup itemGroup = createBuilder(itemGroupRequest);
        String createdMessage = getFormattedMessage(10, itemGroupRequest.getItemGroup());

        return ItemMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(itemGroupRepository.save(itemGroup)).build();
    }

    private ItemGroup createBuilder(ItemGroupRequest itemGroupRequest) {
        return ItemGroup.builder()
                .handle("ItemGroupBO:" + itemGroupRequest.getSite() + "," + itemGroupRequest.getItemGroup())
                .site(itemGroupRequest.getSite())
                .itemGroup(itemGroupRequest.getItemGroup())
                .groupDescription(itemGroupRequest.getGroupDescription())
                .groupMemberList(itemGroupRequest.getGroupMemberList())
                .unitPrice(itemGroupRequest.getUnitPrice())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .createdBy(itemGroupRequest.getUserId())
                .build();
    }

    private void getValidated(ItemGroupRequest itemGroupRequest) throws Exception {
        if (itemGroupRequest.getUserId() == null || itemGroupRequest.getUserId().isEmpty()) {
            throw new ItemGroupException(108, itemGroupRequest.getItemGroup());
        }
        for (GroupMemberList item : itemGroupRequest.getGroupMemberList()) {
            Boolean isExist = itemService.isItemExist(item.getItem(), null, itemGroupRequest.getSite());
            if (!isExist) {
                throw new ItemGroupException(100, item.getItem());
            }
        }
    }

    public List<GroupMemberList> groupMemberListWithVersion(String site, List<GroupMemberList> groupMemberLists)
    {
        for(GroupMemberList groupMemberList : groupMemberLists)
        {
            Item item = retrieveItem(site,groupMemberList.getItem());
            if(item!= null && item.getItem()!=null)
            {
                groupMemberList.setItemVersion(item.getRevision());
            }
        }

        List<GroupMemberList> uniqueList = groupMemberLists.stream()
                .distinct()
                .collect(Collectors.toList());
        return uniqueList;
    }

    public Item retrieveItem(String site,String item)
    {
        ItemRequest itemRequest = ItemRequest.builder().site(site).item(item).build();
       Item retrieveditem = webClientBuilder.build()
                .post()
                .uri(retrieveItemServiceUrl)
                .bodyValue(itemRequest)
                .retrieve()
                .bodyToMono(Item.class)
                .block();
       return retrieveditem;
    }

    @Override
    public ItemMessageModel updateItemGroup(ItemGroupRequest itemGroupRequest) throws Exception {
        ItemGroup itemGroup = itemGroupRepository.findByItemGroupAndSiteAndActive(itemGroupRequest.getItemGroup(), itemGroupRequest.getSite(), 1);
        if (itemGroupRequest.getGroupDescription() == null || itemGroupRequest.getGroupDescription().isEmpty()) {
            itemGroupRequest.setGroupDescription(itemGroupRequest.getItemGroup());
        }
        if (itemGroup != null) {
            try {
                getValidated(itemGroupRequest);
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw e;
            }
            if(itemGroupRequest.getGroupMemberList() != null && !itemGroupRequest.getGroupMemberList().isEmpty())
            {
                List<GroupMemberList> groupMemberLists = groupMemberListWithVersion(itemGroupRequest.getSite(),itemGroupRequest.getGroupMemberList());
                if(groupMemberLists != null && !groupMemberLists.isEmpty())
                {
                    itemGroupRequest.setGroupMemberList(groupMemberLists);
                }
            }
            itemGroup = updateBuilder(itemGroupRequest, itemGroup);
            String updatedMessage = getFormattedMessage(12, itemGroupRequest.getItemGroup());

            return ItemMessageModel.builder().message_details(new MessageDetails(updatedMessage, "S")).response(itemGroupRepository.save(itemGroup)).build();
        } else {
            throw new ItemGroupException(112,itemGroupRequest.getItemGroup());
        }
    }

    private ItemGroup updateBuilder(ItemGroupRequest itemGroupRequest, ItemGroup itemGroup) {
        return ItemGroup.builder()
                .itemGroup(itemGroup.getItemGroup())
                .handle(itemGroup.getHandle())
                .site(itemGroupRequest.getSite())
                .active(itemGroup.getActive())
                .unitPrice(itemGroupRequest.getUnitPrice())
                .groupDescription(itemGroupRequest.getGroupDescription())
                .groupMemberList(itemGroupRequest.getGroupMemberList())
                .createdDateTime(itemGroup.getCreatedDateTime())
                .createdBy(itemGroup.getCreatedBy())
                .modifiedBy(itemGroupRequest.getUserId())
                .modifiedDateTime(LocalDateTime.now())
                .build();
    }

    @Override
    public GroupNameResponseList getGroupNameListCreationDate(String site) throws Exception {
        List<GroupNameResponse> groupNameResponses = itemGroupRepository.findTop50BySiteAndActiveOrderByCreatedDateTimeDesc(site, 1); // db.getCollection("item_groups").find().sort({"createdDateTime":-1}).limit(15)
        return GroupNameResponseList.builder().groupNameList(groupNameResponses).build();
    }

    @Override
    public GroupNameResponseList getGroupNameList(String site, String itemGroup) throws Exception {
        List<GroupNameResponse> groupNameResponses = itemGroupRepository.findByItemGroupContainingIgnoreCaseAndSiteAndActive(itemGroup, site, 1); //db.getCollection("item_groups).find({ itemGroup: { $regex: "substring", $options: "i" } }) here replace substring with any letter u want to search and i represents case insensetive
        if (groupNameResponses != null && !groupNameResponses.isEmpty()) {
            return GroupNameResponseList.builder().groupNameList(groupNameResponses).build();
        } else {
            throw new ItemGroupException(1601, itemGroup);

        }
    }

    @Override
    public ItemGroup retrieveItemGroup(String site, String itemGroup) throws Exception {
        ItemGroup sItemGroup = itemGroupRepository.findByItemGroupAndSiteAndActive(itemGroup, site, 1);
        if (sItemGroup != null) {
            return sItemGroup;
        } else {
            throw new ItemGroupException(1601, sItemGroup);
        }
    }


    @Override
    public ItemGroup associateItemBOtoItemGroup(String site, String itemGroup, List<String> items) throws Exception {
        ItemGroup aItemGroup = itemGroupRepository.findByItemGroupAndSiteAndActive(itemGroup, site, 1);
        if (aItemGroup == null) {
            throw new ItemGroupException(1601, aItemGroup);
        }
        List<GroupMemberList> groupMemberList = aItemGroup.getGroupMemberList();
        for (String item : items) {
            boolean alreadyExists = groupMemberList.stream().anyMatch(member -> Objects.equals(member.getItem(), item));
            if (!alreadyExists) {
                GroupMemberList newGroupMember = GroupMemberList.builder().item(item).build();
                groupMemberList.add(newGroupMember);
            }
        }

        aItemGroup.setGroupMemberList(groupMemberList);
        aItemGroup.setModifiedDateTime(LocalDateTime.now());
        return itemGroupRepository.save(aItemGroup);
    }


    @Override
    public ItemGroup removeItemBOFromItemGroup(String site, String itemGroup, List<String> itemList) throws Exception {
        ItemGroup oItemGroup = itemGroupRepository.findByItemGroupAndSiteAndActive(itemGroup, site, 1);
        if (oItemGroup != null) {
            for (String item : itemList) {
                if (oItemGroup.getGroupMemberList().removeIf(items -> items.getItem().equals(item))) {
                    oItemGroup.setModifiedDateTime(LocalDateTime.now());
                }
            }
        } else {
            throw new ItemGroupException(1601, oItemGroup);
        }
        return itemGroupRepository.save(oItemGroup);
    }

    @Override
    public ItemMessageModel deleteItemGroup(String site, String itemGroup, String userId) throws Exception {
        if (userId == null || userId.isEmpty()) {
            throw new ItemGroupException(108, itemGroup);
        }
        if (itemGroupRepository.existsByItemGroupAndSiteAndActive(itemGroup, site, 1)) {
            ItemGroup existingItemGroup = itemGroupRepository.findByItemGroupAndSiteAndActive(itemGroup, site, 1);
            existingItemGroup.setActive(0);
            existingItemGroup.setModifiedBy(userId);
            existingItemGroup.setModifiedDateTime(LocalDateTime.now());
            itemGroupRepository.save(existingItemGroup);
            String deletedMessage = getFormattedMessage(13, itemGroup);

            return ItemMessageModel.builder().message_details(new MessageDetails(deletedMessage, "S")).build();
        } else {
            throw new ItemGroupException(1601, itemGroup);
        }
    }

    @Override
    public Boolean isItemGroupExist(String site, String itemGroup) throws Exception {
        return itemGroupRepository.existsByItemGroupAndSiteAndActive(itemGroup, site, 1);

    }

    @Override
    public ItemList getAvailableItems(String site, String itemGroupName) throws Exception {
        if (itemGroupName != null && !itemGroupName.isEmpty()) {
            List<ItemGroup> itemGroups = itemGroupRepository.findBySiteAndItemGroupAndActive(site, itemGroupName, 1);
            if (itemGroups != null && !itemGroups.isEmpty()) {
                List<String> items = new ArrayList<>();
                for (ItemGroup itemGroup : itemGroups) {
                    List<GroupMemberList> groupMemberLists = itemGroup.getGroupMemberList();
                    if (groupMemberLists != null && !groupMemberLists.isEmpty()) {
                        items.addAll(groupMemberLists.stream()
                                .map(GroupMemberList::getItem)
                                .collect(Collectors.toList()));
                    }
                }
                ItemRequest itemRequest = new ItemRequest();

                itemRequest.setSite(site);

                List<Item> availableItems = webClientBuilder.build()
                        .post()
                        .uri(itemServiceUrl)
                        .bodyValue(itemRequest)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<Item>>() {
                        })
                        .block();
                if (availableItems != null) {
                    availableItems.removeIf(item -> items.contains(item.getItem()));

                }

                return ItemList.builder().listOfAvailableItems(availableItems).build();
            }
            throw new ItemGroupException(1601, itemGroupName);
        }
        ItemRequest itemRequest = new ItemRequest();

        itemRequest.setSite(site);

        List<Item> availableItems = webClientBuilder.build()
                .post()
                .uri(itemServiceUrl)
                .bodyValue(itemRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Item>>() {
                })
                .block();
        return ItemList.builder().listOfAvailableItems(availableItems).build();
    }

    @Override
    public List<ItemGroup> retrieveAllBySite(String site) {
        List<ItemGroup> itemGroupList = itemGroupRepository.findBySiteAndActive(site, 1);
        return itemGroupList;
    }

    @Override
    public String callExtension(Extension extension) {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new ItemGroupException(800);
        }
        return extensionResponse;
    }

    @Override
    public AuditLogRequest createAuditLog(ItemGroupRequest itemGroupRequest) {
        return AuditLogRequest.builder()
                .site(itemGroupRequest.getSite())
                .action_code("ITEMGROUP-CREATE")
                .action_detail("ItemGroup Created " + itemGroupRequest.getItemGroup())
                .action_detail_handle("ActionDetailBO:" + itemGroupRequest.getSite() + "," + "ITEMGROUP-CREATE" + "," + itemGroupRequest.getUserId() + ":" + "com.rits.itemgroupservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(itemGroupRequest.getUserId())
                .txnId("ITEMGROUP-CREATE" + LocalDateTime.now() + itemGroupRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Create")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest updateAuditLog(ItemGroupRequest itemGroupRequest) {
        return AuditLogRequest.builder()
                .site(itemGroupRequest.getSite())
                .action_code("ITEMGROUP-UPDATE")
                .action_detail("ItemGroup Updated " + itemGroupRequest.getItemGroup())
                .action_detail_handle("ActionDetailBO:" + itemGroupRequest.getSite() + "," + "ITEMGROUP-UPDATE" + "," + itemGroupRequest.getUserId() + ":" + "com.rits.itemgroupservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(itemGroupRequest.getUserId())
                .txnId("ITEMGROUP-UPDATE" + LocalDateTime.now() + itemGroupRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Update")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest deleteAuditLog(ItemGroupRequest itemGroupRequest) {
        return AuditLogRequest.builder()
                .site(itemGroupRequest.getSite())
                .action_code("ITEMGROUP-DELETE")
                .action_detail("ItemGroup Deleted " + itemGroupRequest.getItemGroup())
                .action_detail_handle("ActionDetailBO:" + itemGroupRequest.getSite() + "," + "ITEMGROUP-DELETE" + "," + itemGroupRequest.getUserId() + ":" + "com.rits.itemgroupservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(itemGroupRequest.getUserId())
                .txnId("ITEMGROUP-DELETE" + LocalDateTime.now() + itemGroupRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Delete")
                .topic("audit-log")
                .build();
    }


}




