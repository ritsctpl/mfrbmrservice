package com.rits.inventoryservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.inventoryservice.dto.*;
import com.rits.inventoryservice.exception.InventoryException;
import com.rits.inventoryservice.model.Inventory;
import com.rits.inventoryservice.model.InventoryMessageModel;
import com.rits.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/inventory-service")
public class InventoryController {
    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;
    private final ApplicationContext context;
    @PostMapping("/shutdown")
    public void shutdown() {
        System.out.println("Shutting down...");
        SpringApplication.exit(context, () -> 1);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createInventory(@RequestBody InventoryRequest inventoryRequest) throws JsonProcessingException {
       InventoryMessageModel createResponse;
        if(inventoryRequest.getSite()!=null && !inventoryRequest.getSite().isEmpty()){
            try {
                createResponse = inventoryService.createInventory(inventoryRequest);
                return ResponseEntity.ok(InventoryMessageModel.builder().message_details(createResponse.getMessage_details()).response(createResponse.getResponse()).build());
            }catch(InventoryException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InventoryException(1701, inventoryRequest.getSite());

    }

    @PostMapping("/update")
    public InventoryMessageModel updateInventory(@RequestBody InventoryRequest inventoryRequest) throws JsonProcessingException {
       InventoryMessageModel updateResponse;
        if(inventoryRequest.getSite()!=null && !inventoryRequest.getSite().isEmpty()){
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(inventoryRequest.getSite()).hookPoint("PRE").activity("inventory-service").hookableMethod("update").request(objectMapper.writeValueAsString(inventoryRequest)).build();
            String preExtensionResponse = inventoryService.callExtension(preExtension);
            InventoryRequest preExtensionInventoryRequest = objectMapper.readValue(preExtensionResponse, InventoryRequest.class);
            try {
                updateResponse = inventoryService.updateInventory(preExtensionInventoryRequest);
                Extension postExtension = Extension.builder().site(inventoryRequest.getSite()).hookPoint("POST").activity("inventory-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateResponse.getResponse())).build();
                String postExtensionResponse = inventoryService.callExtension(postExtension);
                Inventory postExtensionInventory = objectMapper.readValue(postExtensionResponse, Inventory.class);
                return InventoryMessageModel.builder().message_details(updateResponse.getMessage_details()).response(postExtensionInventory).build();
            }catch(InventoryException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InventoryException(1701, inventoryRequest.getSite());
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteInventory(@RequestBody InventoryRequest inventoryRequest) throws JsonProcessingException {
        InventoryMessageModel deleteResponse;
        if(inventoryRequest.getSite()!=null && !inventoryRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(inventoryRequest.getSite()).hookPoint("PRE").activity("inventory-service").hookableMethod("delete").request(objectMapper.writeValueAsString(inventoryRequest)).build();
            String preExtensionResponse = inventoryService.callExtension(preExtension);
            InventoryRequest preExtensionInventoryRequest = objectMapper.readValue(preExtensionResponse, InventoryRequest.class);
            try {
                deleteResponse = inventoryService.deleteInventory(preExtensionInventoryRequest);
                return ResponseEntity.ok(deleteResponse);
            }catch(InventoryException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InventoryException(1701, inventoryRequest.getSite());
    }
    @PostMapping("/isInUsage")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isInUsage(@RequestBody InventoryRequest inventoryRequest){
        Boolean isInUsageResponse;
        if(inventoryRequest.getSite()!=null && !inventoryRequest.getSite().isEmpty()) {
            try {
                isInUsageResponse= inventoryService.isInUsage(inventoryRequest);
                return  ResponseEntity.ok(isInUsageResponse);
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InventoryException(1701, inventoryRequest.getSite());
    }

    @PostMapping("/inventorySplit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> inventorySplit(@RequestBody JsonNode payload){
        InventorySplitRequest inventorySplitRequest = new ObjectMapper().convertValue(payload, InventorySplitRequest.class);
        List<Inventory> inventorySplitResponse;
        if(inventorySplitRequest.getSite()!=null && !inventorySplitRequest.getSite().isEmpty()) {
            try {
                inventorySplitResponse= inventoryService.inventorySplit(inventorySplitRequest);
                return  ResponseEntity.ok(inventorySplitResponse);
            }catch(InventoryException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InventoryException(1701, inventorySplitRequest.getSite());
    }

    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveInventory(@RequestBody InventoryRequest inventoryRequest) throws JsonProcessingException {
        Inventory retrieveResponse;
        if(inventoryRequest.getSite()!=null && !inventoryRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(inventoryRequest.getSite()).hookPoint("PRE").activity("inventory-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(inventoryRequest)).build();
            String preExtensionResponse = inventoryService.callExtension(preExtension);
            InventoryRequest preExtensionInventoryRequest = objectMapper.readValue(preExtensionResponse,InventoryRequest.class);
            try {
                retrieveResponse = inventoryService.retrieveInventory(preExtensionInventoryRequest);
                Extension postExtension = Extension.builder().site(inventoryRequest.getSite()).hookPoint("POST").activity("inventory-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveResponse)).build();
                String postExtensionResponse = inventoryService.callExtension(postExtension);
                Inventory postExtensionInventory = objectMapper.readValue(postExtensionResponse, Inventory.class);
                return ResponseEntity.ok(postExtensionInventory);
            }catch(InventoryException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InventoryException(1701, inventoryRequest.getSite());
    }

    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getTop1000InventoryListByCreationDate(@RequestBody InventoryRequest inventoryRequest){
        InventoryResponseList top50Response;
        if(inventoryRequest.getSite()!=null && !inventoryRequest.getSite().isEmpty()) {
            try {
                top50Response= inventoryService.getTop1000InventoryListByCreationDate(inventoryRequest);
                return  ResponseEntity.ok(top50Response);
            }catch(InventoryException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InventoryException(1701, inventoryRequest.getSite());
    }

//    {
//        "startDate":"",
//        "endDate":""
//    }
@PostMapping("/retrieveInventoryReturn")
@ResponseStatus(HttpStatus.OK)
public ResponseEntity<?> retrieveInventoryReturn(@RequestBody InventoryRequest inventoryRequest){
    Inventory response;
    try {
        response= inventoryService.retrieveInventoryReturn(inventoryRequest);
        return  ResponseEntity.ok(response);
    }catch(InventoryException e){
        throw e;
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
    @PostMapping("/retrieveByDate")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getInventoryListByCreationDate(@RequestBody InventoryRequest inventoryRequest){
        List<Inventory> response;
            try {
                response= inventoryService.getInventoryListByCreationDate(inventoryRequest);
                return  ResponseEntity.ok(response);
            }catch(InventoryException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getInventoryList(@RequestBody InventoryRequest inventoryRequest){
        InventoryResponseList getInventoryListResponse;
        if(inventoryRequest.getSite()!=null && !inventoryRequest.getSite().isEmpty()) {
            try {
                getInventoryListResponse= inventoryService.getInventoryList(inventoryRequest);
                return  ResponseEntity.ok(getInventoryListResponse);
            }catch(InventoryException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InventoryException(1701, inventoryRequest.getSite());
    }

    @PostMapping("/retrieveByItem")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveByItem(@RequestBody InventoryRequest inventoryRequest){
        InventoryResponseList retrieveByItem;
        if(inventoryRequest.getSite()!=null && !inventoryRequest.getSite().isEmpty()) {
            try {
                retrieveByItem= inventoryService.retrieveInventoryListByItem(inventoryRequest);
                return  ResponseEntity.ok(retrieveByItem);
            }catch(InventoryException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InventoryException(1701, inventoryRequest.getSite());
    }

    @PostMapping("/retrieveDataField")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveDataFieldsBYDataType(@RequestBody InventoryRequest inventoryRequest){
        List<DataField> retrieveByItem;
        if(inventoryRequest.getSite()!=null && !inventoryRequest.getSite().isEmpty()) {
            try {
                retrieveByItem= inventoryService.retrievedDataFieldByDataType(inventoryRequest.getSite(),inventoryRequest.getItem(),inventoryRequest.getVersion());
                return  ResponseEntity.ok(retrieveByItem);
            }catch(InventoryException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InventoryException(1701, inventoryRequest.getSite());
    }

    @PostMapping("/checkInventoryList")
    @ResponseStatus(HttpStatus.OK)
    public List<String> checkInventoryList(@RequestBody InventorySplitRequest inventoryListRequest){
        if(inventoryListRequest.getSite()!=null && !inventoryListRequest.getSite().isEmpty()) {
            try {
                return inventoryService.checkInventoryList(inventoryListRequest);
            }catch(InventoryException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new InventoryException(1701, inventoryListRequest.getSite());
    }

    @PostMapping("/getInventoryById")
    public ResponseEntity<InventoryRequest> getInventoryById(@RequestBody InventoryRequest inventoryRequest) {
        InventoryRequest inventory = inventoryService.findByInventoryId(inventoryRequest.getInventoryId());
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/checkBatchExist")
    public boolean checkBatchExists(@RequestBody InventoryRequest inventoryRequest) {
        return inventoryService.isBatchNumberExists(inventoryRequest.getInventoryId(), inventoryRequest.getSite());
    }
}
