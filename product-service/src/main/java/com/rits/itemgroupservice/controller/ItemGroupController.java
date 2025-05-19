package com.rits.itemgroupservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.itemgroupservice.dto.*;
import com.rits.itemgroupservice.exception.ItemGroupException;
import com.rits.itemgroupservice.model.ItemGroup;
import com.rits.itemgroupservice.model.ItemMessageModel;
import com.rits.itemgroupservice.service.ItemGroupService;
import com.rits.kafkaservice.ProducerEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/itemgroup-service")
public class ItemGroupController {
    private final ItemGroupService itemGroupService;

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;


    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createItemGroup(@RequestBody ItemGroupRequest itemGroupRequest) throws JsonProcessingException {
        ItemMessageModel createResponse;
        if (itemGroupRequest.getSite() != null && !itemGroupRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(itemGroupRequest.getSite()).hookPoint("PRE").activity("itemgroup-service").hookableMethod("create").request(objectMapper.writeValueAsString(itemGroupRequest)).build();
            String preExtensionResponse = itemGroupService.callExtension(preExtension);
            ItemGroupRequest preExtensionItemGroup = objectMapper.readValue(preExtensionResponse, ItemGroupRequest.class);

            try {
                createResponse = itemGroupService.createItemGroup(preExtensionItemGroup);
                Extension postExtension = Extension.builder().site(itemGroupRequest.getSite()).hookPoint("POST").activity("activitygroup-service").hookableMethod("create").request(objectMapper.writeValueAsString(createResponse.getResponse())).build();
                String postExtensionResponse = itemGroupService.callExtension(postExtension);
                ItemGroup postExtensionItemGroup = objectMapper.readValue(postExtensionResponse, ItemGroup.class);

                AuditLogRequest activityLog = itemGroupService.createAuditLog(itemGroupRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(ItemMessageModel.builder().message_details(createResponse.getMessage_details()).response(postExtensionItemGroup).build());
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemGroupException(1602, itemGroupRequest.getSite());
    }


    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateItemGroup(@RequestBody ItemGroupRequest itemGroupRequest) throws JsonProcessingException {
        ItemMessageModel updateResponse;
        if (itemGroupRequest.getSite() != null && !itemGroupRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(itemGroupRequest.getSite()).hookPoint("PRE").activity("itemgroup-service").hookableMethod("update").request(objectMapper.writeValueAsString(itemGroupRequest)).build();
            String preExtensionResponse = itemGroupService.callExtension(preExtension);
            ItemGroupRequest preExtensionItemGroup = objectMapper.readValue(preExtensionResponse, ItemGroupRequest.class);

            try {
                updateResponse = itemGroupService.updateItemGroup(preExtensionItemGroup);
                Extension postExtension = Extension.builder().site(itemGroupRequest.getSite()).hookPoint("POST").activity("itemgroup-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateResponse.getResponse())).build();
                String postExtensionResponse = itemGroupService.callExtension(postExtension);
                ItemGroup postExtensionItemGroup = objectMapper.readValue(postExtensionResponse, ItemGroup.class);
                AuditLogRequest activityLog = itemGroupService.updateAuditLog(itemGroupRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return ResponseEntity.ok(ItemMessageModel.builder().message_details(updateResponse.getMessage_details()).response(postExtensionItemGroup).build());
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemGroupException(1602, itemGroupRequest.getSite());
    }


    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GroupNameResponseList> getGroupNameListCreationDate(@RequestBody ItemGroupRequest itemGroupRequest) {
        GroupNameResponseList response;
        if (itemGroupRequest.getSite() != null && !itemGroupRequest.getSite().isEmpty()) {
            try {
                response = itemGroupService.getGroupNameListCreationDate(itemGroupRequest.getSite());
                return ResponseEntity.ok(response);
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemGroupException(1602, itemGroupRequest.getSite());
    }


    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GroupNameResponseList> getGroupNameList(@RequestBody ItemGroupRequest itemGroupRequest) {
        GroupNameResponseList response;
        if (itemGroupRequest.getSite() != null && !itemGroupRequest.getSite().isEmpty()) {
            try {
                response = itemGroupService.getGroupNameList(itemGroupRequest.getSite(), itemGroupRequest.getItemGroup());
                return ResponseEntity.ok(response);
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemGroupException(1602, itemGroupRequest.getSite());
    }


    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ItemGroup> retrieveItemGroup(@RequestBody ItemGroupRequest itemGroupRequest) throws JsonProcessingException {
        ItemGroup retrieveResponse;
        if (itemGroupRequest.getSite() != null && !itemGroupRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(itemGroupRequest.getSite()).hookPoint("PRE").activity("itemgroup-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(itemGroupRequest)).build();
            String preExtensionResponse = itemGroupService.callExtension(preExtension);
            ItemGroupRequest preExtensionItemGroup = objectMapper.readValue(preExtensionResponse, ItemGroupRequest.class);

            try {
                retrieveResponse = itemGroupService.retrieveItemGroup(preExtensionItemGroup.getSite(), preExtensionItemGroup.getItemGroup());
                Extension postExtension = Extension.builder().site(itemGroupRequest.getSite()).hookPoint("POST").activity("activitygroup-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveResponse)).build();
                String postExtensionResponse = itemGroupService.callExtension(postExtension);
                ItemGroup postExtensionItemGroup = objectMapper.readValue(postExtensionResponse, ItemGroup.class);
                return ResponseEntity.ok(postExtensionItemGroup);
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemGroupException(1602, itemGroupRequest.getSite());
    }


    @PostMapping("/add")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ItemGroup> associateItemMemberToItemGroup(@RequestBody GroupMemberListRequest groupMemberListRequest) {
        ItemGroup response;
        if (groupMemberListRequest.getSite() != null && !groupMemberListRequest.getSite().isEmpty()) {
            try {
                response = itemGroupService.associateItemBOtoItemGroup(groupMemberListRequest.getSite(), groupMemberListRequest.getItemGroup(), groupMemberListRequest.getItem());
                return ResponseEntity.ok(response);
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemGroupException(1602, groupMemberListRequest.getSite());
    }


    @PostMapping("/remove")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ItemGroup> removeItemBOFromItemGroup(@RequestBody GroupMemberListRequest groupMemberListRequest) {
        ItemGroup response;

        if (groupMemberListRequest.getSite() != null && !groupMemberListRequest.getSite().isEmpty()) {
            try {
                response = itemGroupService.removeItemBOFromItemGroup(groupMemberListRequest.getSite(), groupMemberListRequest.getItemGroup(), groupMemberListRequest.getItem());
                return ResponseEntity.ok(response);
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemGroupException(1602, groupMemberListRequest.getSite());
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteItemGroup(@RequestBody ItemGroupRequest itemGroupRequest) throws JsonProcessingException {
        ItemMessageModel deleteResponse;
        if (itemGroupRequest.getSite() != null && !itemGroupRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(itemGroupRequest.getSite()).hookPoint("PRE").activity("itemgroup-service").hookableMethod("delete").request(objectMapper.writeValueAsString(itemGroupRequest)).build();
            String preExtensionResponse = itemGroupService.callExtension(preExtension);
            ItemGroupRequest preExtensionItemGroup = objectMapper.readValue(preExtensionResponse, ItemGroupRequest.class);

            try {
                deleteResponse = itemGroupService.deleteItemGroup(preExtensionItemGroup.getSite(), itemGroupRequest.getItemGroup(), itemGroupRequest.getUserId());
                AuditLogRequest activityLog = itemGroupService.deleteAuditLog(itemGroupRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(deleteResponse);
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemGroupException(1602, itemGroupRequest.getSite());
    }

    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isItemGroupExist(@RequestBody ItemGroupRequest itemGroupRequest) {
        Boolean isExist;
        if (itemGroupRequest.getSite() != null && !itemGroupRequest.getSite().isEmpty()) {
            try {
                isExist = itemGroupService.isItemGroupExist(itemGroupRequest.getSite(), itemGroupRequest.getItemGroup());
                return ResponseEntity.ok(isExist);
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemGroupException(1602, itemGroupRequest.getSite());
    }

    @PostMapping("/retrieveAvailableItems")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ItemList> getAvailableActivities(@RequestBody ItemGroupRequest itemGroupRequest) {
        ItemList response;
        if (itemGroupRequest.getSite() != null && !itemGroupRequest.getSite().isEmpty()) {
            try {
                response = itemGroupService.getAvailableItems(itemGroupRequest.getSite(), itemGroupRequest.getItemGroup());
                return ResponseEntity.ok(response);
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemGroupException(1602, itemGroupRequest.getSite());
    }


    @PostMapping("/retrieveBySite")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<?>> retrieveAllBySite(@RequestBody ItemGroupRequest itemGroupRequest) {
        List<ItemGroup> retrieveAllBySiteResponse = new ArrayList<>();
        if (itemGroupRequest.getSite() != null && !itemGroupRequest.getSite().isEmpty()) {
            try {
                retrieveAllBySiteResponse = itemGroupService.retrieveAllBySite(itemGroupRequest.getSite());
                return ResponseEntity.ok(retrieveAllBySiteResponse);
            } catch (ItemGroupException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemGroupException(1602, itemGroupRequest.getSite());
    }
}


