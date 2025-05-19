package com.rits.itemservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.itemservice.dto.*;
import com.rits.itemservice.exception.ItemException;
import com.rits.itemservice.model.Item;
import com.rits.itemservice.model.ItemMessageModel;
import com.rits.itemservice.service.ItemServiceImpl;
import com.rits.kafkaservice.ProducerEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/item-service")
public class ItemController {

    private final ItemServiceImpl itemService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationContext context;
    @PostMapping("/shutdown")
    public void shutdown() {
        System.out.println("Shutting down...");
        SpringApplication.exit(context, () -> 1);
    }

    @PostMapping("create")
    public ResponseEntity<?> createItem(@RequestBody ItemRequest itemRequest) throws JsonProcessingException {


        ItemMessageModel createItem;

        try {
            createItem = itemService.createItem(itemRequest);

            return ResponseEntity.ok(createItem);

        } catch (ItemException itemException) {
            throw itemException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("delete")
    public ResponseEntity<ItemMessageModel> deleteItem(@RequestBody ItemRequest itemRequest) throws JsonProcessingException {
        ItemMessageModel deleteResponse;
        if (itemRequest.getSite() != null && !itemRequest.getSite().isEmpty()) {

            try {
                deleteResponse = itemService.deleteItem(itemRequest.getItem(), itemRequest.getRevision(), itemRequest.getSite(), itemRequest.getUserId());

                AuditLogRequest activityLog = itemService.deleteAuditLog(itemRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return ResponseEntity.ok(deleteResponse);

            } catch (ItemException itemException) {
                throw itemException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemException(1);
    }


    @PostMapping("retrieve")
    public ResponseEntity<Item> retrieveItem(@RequestBody ItemRequest itemRequest) throws JsonProcessingException {
        Item retrieveItem;

        if (itemRequest.getSite() != null && !itemRequest.getSite().isEmpty()) {

            try {
                retrieveItem = itemService.retrieveItem(itemRequest.getItem(), itemRequest.getRevision(), itemRequest.getSite());
                 return ResponseEntity.ok(retrieveItem);

            } catch (ItemException itemException) {
                throw itemException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemException(1);

    }

    @PostMapping("update")
    public ResponseEntity<ItemMessageModel> updateItem(@RequestBody ItemRequest itemRequest) throws JsonProcessingException {
        ItemMessageModel updateItem;


        try {
            updateItem = itemService.updateItem(itemRequest);

            AuditLogRequest activityLog = itemService.updateAuditLog(itemRequest);
            eventPublisher.publishEvent(new ProducerEvent(activityLog));

            return ResponseEntity.ok(ItemMessageModel.builder().message_details(updateItem.getMessage_details()).response(updateItem.getResponse()).build());

        } catch (ItemException itemException) {
            throw itemException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveTop50")
    public ResponseEntity<ItemResponseList> getItemListByCreatedDate(@RequestBody ItemRequest itemRequest) {
        ItemResponseList retrieveTop50Items;
        if (itemRequest.getSite() != null && !itemRequest.getSite().isEmpty()) {

            try {
                retrieveTop50Items = itemService.getItemListByCreationDate(itemRequest.getSite());
                return ResponseEntity.ok(retrieveTop50Items);
            } catch (ItemException itemException) {
                throw itemException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemException(1);
    }

    @PostMapping("retrieveTop50Item")
    public ResponseEntity<ItemResponseListForFilter> getItemListByCreatedDateItem(@RequestBody ItemRequest itemRequest) {
        ItemResponseListForFilter retrieveTop50Items;
        if (itemRequest.getSite() != null && !itemRequest.getSite().isEmpty()) {

            try {
                retrieveTop50Items = itemService.getItemListByCreationDateItem(itemRequest.getSite());
                return ResponseEntity.ok(retrieveTop50Items);
            } catch (ItemException itemException) {
                throw itemException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemException(1);
    }


    @PostMapping("retrieveAll")
    public ResponseEntity<ItemResponseList> getItemList(@RequestBody ItemRequest itemRequest) {
        ItemResponseList retrieveAllItems;
        if (itemRequest.getSite() != null && !itemRequest.getSite().isEmpty()) {
            try {
                retrieveAllItems = itemService.getItemList(itemRequest.getItem(), itemRequest.getSite());
                return ResponseEntity.ok(retrieveAllItems);
            } catch (ItemException itemException) {
                throw itemException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemException(1);
    }


    @PostMapping("isExist")
    public ResponseEntity<Boolean> isItemExist(@RequestBody ItemRequest itemRequest) {
        Boolean isItemExist;
        if (itemRequest.getSite() != null && !itemRequest.getSite().isEmpty()) {

            try {
                isItemExist = itemService.isItemExist(itemRequest.getItem(), itemRequest.getRevision(), itemRequest.getSite());
                return ResponseEntity.ok(isItemExist);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemException(1);
    }

    @PostMapping("retrieveBySite")
    public ResponseEntity<List<ItemResponse>> getAllItems(@RequestBody ItemRequest itemRequest) {
        List<ItemResponse> getAllItems;
        if (itemRequest.getSite() != null && !itemRequest.getSite().isEmpty()) {

            try {
                getAllItems = itemService.retrieveAllItem(itemRequest.getSite());
                return ResponseEntity.ok(getAllItems);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemException(1);
    }

    @PostMapping("getAvailableDocument")
    public ResponseEntity<PrintDocuments> getAvailableDocument(@RequestBody ItemRequest itemRequest) {
        PrintDocuments getAvailableDocument;
        if (itemRequest.getSite() != null && !itemRequest.getSite().isEmpty()) {

            try {
                getAvailableDocument = itemService.getAvailableDocument(itemRequest.getSite(), itemRequest.getItem(), itemRequest.getRevision());
                return ResponseEntity.ok(getAvailableDocument);
            } catch (ItemException itemException) {
                throw itemException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemException(1);
    }

    @PostMapping("add")
    public ResponseEntity<PrintDocuments> association(@RequestBody ItemRequest itemRequest) {
        PrintDocuments association;
        if (itemRequest.getSite() != null && !itemRequest.getSite().isEmpty()) {

            try {
                association = itemService.associateDocumentToPrintDocument(itemRequest.getSite(), itemRequest.getItem(), itemRequest.getRevision(), itemRequest.getPrintDocuments());
                return ResponseEntity.ok(association);
            } catch (ItemException itemException) {
                throw itemException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemException(1);
    }

    @PostMapping("remove")
    public ResponseEntity<PrintDocuments> remove(@RequestBody ItemRequest itemRequest) {
        PrintDocuments remove;
        if (itemRequest.getSite() != null && !itemRequest.getSite().isEmpty()) {

            try {
                remove = itemService.removeDocumentFromPrintDocument(itemRequest.getSite(), itemRequest.getItem(), itemRequest.getRevision(), itemRequest.getPrintDocuments());
                return ResponseEntity.ok(remove);
            } catch (ItemException itemException) {
                throw itemException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ItemException(1);
    }


}
