package com.rits.inventoryservice.service;
import com.rits.inventoryservice.dto.*;
import com.rits.inventoryservice.exception.InventoryException;
import com.rits.inventoryservice.model.*;
import com.rits.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class InventoryServiceImpl implements  InventoryService{

    private final InventoryRepository inventoryRepository;
    private final WebClient.Builder webClientBuilder;
    private final MongoTemplate mongoTemplate;
    private final MessageSource localMessageSource;
    @Value("${item-service.url}/isExist")
    private String itemServiceUrl;

    @Value("${item-service.url}/retrieve")
    private String itemUrl;

    @Value("${datatype-service.url}/retrieve")
    private String dataType;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public InventoryMessageModel createInventory(InventoryRequest inventoryRequest) throws Exception{

            String inventoryId;
            int qty;
            InventoryLocation invLoc;
            List<InventoryLocation> invLocation = new ArrayList<>();
            Inventory responses = null;
            List<InventoryLocation> inventoryLocation = new ArrayList<>();

        if(inventoryRequest.getInventoryRequestList()!=null) {
            if(inventoryRequest.getInventoryRequestList().size() > 0){

                for (InventoryRequest invDetails : inventoryRequest.getInventoryRequestList()) {
                    long recordPresent= inventoryRepository.countByInventoryIdAndSiteAndActive(invDetails.getInventoryId(), inventoryRequest.getSite(),1);
                    if(recordPresent > 0){
                        throw new InventoryException(1700, invDetails.getInventoryId());
                    }
                }

                for (InventoryRequest invDetails : inventoryRequest.getInventoryRequestList()) {

                    Inventory inventory = Inventory.builder()
                            .site(invDetails.getSite())
                            .handle("InventoryBO:" + invDetails.getSite() + "," + invDetails.getInventoryId())
                            .status(invDetails.getStatus())
                            .inventoryId(invDetails.getInventoryId())
                            .batchNumber(invDetails.getBatchNumber())
                            .item(invDetails.getItem())
                            .version(invDetails.getVersion())
                            .receiveQty(invDetails.getReceiveQty())
                            .qty(invDetails.getQty())
                            .originalQty(invDetails.getOriginalQty())
                            .remainingQty(invDetails.getRemainingQty())
                            .receiveBy(invDetails.getReceiveBy())
                            .inventoryIdLocation(invDetails.getInventoryIdLocation())
                            .inventoryIdDataDetails(invDetails.getInventoryIdDataDetails())
                            .receiveDateOrTime(invDetails.getReceiveDateOrTime())
                            .parentInventoryId(invDetails.getParentInventoryId())
                            .splittedInventory(invDetails.isSplittedInventory())
                            .active(1)
                            .createdDateTime(LocalDateTime.now())
                            .modifiedDateTime(LocalDateTime.now())
                            .build();
                    responses = inventoryRepository.save(inventory);
                }
                return InventoryMessageModel.builder().message_details(new MessageDetails("successfully created", "S")).response(responses).build();

//            for (Map.Entry<String, Integer> entry : inventoryRequest.getInventoryMap().entrySet()) {
//                String inventoryValue = entry.getKey();
//                long recordPresent= inventoryRepository.countByInventoryIdAndSiteAndActive(inventoryValue, inventoryRequest.getSite(),1);
//                if(recordPresent > 0){
//                    throw new InventoryException(1700, inventoryValue);
//                }
//            }
//
//            for (Map.Entry<String, Integer> entry : inventoryRequest.getInventoryMap().entrySet()) {
//                inventoryId = entry.getKey();
//                qty = entry.getValue();
//                msg = createdInventory(inventoryId, qty, inventoryRequest);
//            }
            }
        }
    long recordPresent= inventoryRepository.countByInventoryIdAndSiteAndActive(inventoryRequest.getInventoryId(), inventoryRequest.getSite(),1);
    if(recordPresent>0){
        throw new InventoryException(1700, inventoryRequest.getInventoryId());
    }else {
        if (inventoryRequest.getItem() != null && !inventoryRequest.getItem().isEmpty()) {
            ItemRequest itemRequest = new ItemRequest();
            itemRequest.setItem(inventoryRequest.getItem());
            itemRequest.setSite(inventoryRequest.getSite());
            itemRequest.setRevision(inventoryRequest.getVersion());
            Boolean itemExist = isItemExist(itemRequest);
            if (!itemExist) {
                throw new InventoryException(1706, itemRequest.getItem());
            }
        }
        if(inventoryRequest.getDescription()==null || inventoryRequest.getDescription().isEmpty()){
            inventoryRequest.setDescription(inventoryRequest.getInventoryId());
        }
                Inventory inventory = Inventory.builder()
                .site(inventoryRequest.getSite())
                .handle("InventoryBO:" + inventoryRequest.getSite() + "," + inventoryRequest.getInventoryId())
                .inventoryId(inventoryRequest.getInventoryId())
                .batchNumber(inventoryRequest.getBatchNumber())
                .description(inventoryRequest.getDescription())
                .status(inventoryRequest.getStatus())
                .item(inventoryRequest.getItem())
                .version(inventoryRequest.getVersion())
                .receiveQty(inventoryRequest.getReceiveQty())
                .qty(inventoryRequest.getQty())
                .originalQty(inventoryRequest.getOriginalQty())
                .remainingQty(inventoryRequest.getRemainingQty())
                .receiveBy(inventoryRequest.getReceiveBy())
                .receiveDateOrTime(inventoryRequest.getReceiveDateOrTime())
                .inventoryIdLocation(inventoryRequest.getInventoryIdLocation())
                .inventoryIdDataDetails(inventoryRequest.getInventoryIdDataDetails())
                .usageCount(inventoryRequest.getUsageCount())
                .maximumUsageCount(inventoryRequest.getMaximumUsageCount())
                .inUsage(inventoryRequest.isInUsage())
                .splittedInventory(inventoryRequest.isSplittedInventory())
                .parentInventoryId(inventoryRequest.getParentInventoryId())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .modifiedDateTime(LocalDateTime.now())
                .build();
        String createdMessage = getFormattedMessage(1, inventoryRequest.getInventoryId());
        return InventoryMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(inventoryRepository.save(inventory)).build();
        }
    }

    public List<String> generateCombinations(List<InventoryDataDetails> dataDetailsList, List<String> trackableDataFields) {
        List<String> combinations = new ArrayList<>();
        for (InventoryDataDetails dataDetails : dataDetailsList) {
            String dataField = dataDetails.getDataField();
            String value = dataDetails.getValue();
            String combination = dataField + ":" + value;

            if (trackableDataFields.contains(dataField)) {
                combinations.add(combination);
            }
        }
        List<String> result = new ArrayList<>();
        generateCombinationsHelper(combinations, "", 0, result);
        return result;
    }

    public void generateCombinationsHelper(List<String> elements, String prefix, int startIndex, List<String> result) {
        if (!prefix.isEmpty()) {
            result.add(prefix);
        }
        for (int i = startIndex; i < elements.size(); i++) {
            generateCombinationsHelper(elements, prefix.isEmpty() ? elements.get(i) : prefix + "_" + elements.get(i), i + 1, result);
        }
    }

    @Override
    public InventoryMessageModel updateInventory(InventoryRequest inventoryRequest) throws Exception{
        Inventory inventory = inventoryRepository.findByInventoryIdAndSiteAndActive(inventoryRequest.getInventoryId(),  inventoryRequest.getSite(),1);
        if(inventoryRequest.getDescription()==null || inventoryRequest.getDescription().isEmpty()){
            inventoryRequest.setDescription(inventoryRequest.getInventoryId());
        }
        if (inventory !=null) {
            if (inventoryRequest.getItem() != null && !inventoryRequest.getItem().isEmpty()) {
                ItemRequest itemRequest = new ItemRequest();
                itemRequest.setItem(inventoryRequest.getItem());
                itemRequest.setSite(inventoryRequest.getSite());
                itemRequest.setRevision(inventoryRequest.getVersion());
                Boolean itemExist = isItemExist(itemRequest);
                if (!itemExist) {
                    throw new InventoryException(1706, itemRequest.getItem());
                }
            }
            inventory = Inventory.builder()
                    .handle("InventoryBO:"+ inventory.getSite()+","+ inventory.getInventoryId())
                    .site(inventory.getSite())
                    .inventoryId(inventory.getInventoryId())
                    .batchNumber(inventory.getBatchNumber())
                    .description(inventoryRequest.getDescription())
                    .status(inventoryRequest.getStatus())
                    .item(inventoryRequest.getItem())
                    .version(inventoryRequest.getVersion())
                    .receiveQty(inventoryRequest.getReceiveQty())
                    .qty(inventoryRequest.getQty())
                    .originalQty(inventoryRequest.getQty())
                    .remainingQty(inventoryRequest.getQty())
                    .receiveDateOrTime(inventoryRequest.getReceiveDateOrTime())
                    .receiveBy(inventoryRequest.getReceiveBy())
                    .inventoryIdDataDetails(inventoryRequest.getInventoryIdDataDetails())
                    .inventoryIdLocation(inventoryRequest.getInventoryIdLocation())
                    .usageCount(inventoryRequest.getUsageCount())
                    .maximumUsageCount(inventoryRequest.getMaximumUsageCount())
                    .inUsage(inventoryRequest.isInUsage())
                    .splittedInventory(inventoryRequest.isSplittedInventory())
                    .parentInventoryId(inventoryRequest.getParentInventoryId())
                    .active(inventory.getActive())
                    .createdDateTime(inventory.getCreatedDateTime())
                    .modifiedDateTime(LocalDateTime.now())
                    .build();
            String createdMessage = getFormattedMessage(2, inventoryRequest.getInventoryId());
            return InventoryMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(inventoryRepository.save(inventory)).build();
            }
            throw new InventoryException(1703, inventoryRequest.getInventoryId());

    }

    @Override
    public InventoryMessageModel deleteInventory(InventoryRequest inventoryRequest) throws Exception {
   Inventory inventory = inventoryRepository.findByInventoryIdAndSiteAndActive(inventoryRequest.getInventoryId(), inventoryRequest.getSite(),1);
        if (inventory !=null && inventory.isInUsage()) {
            throw new InventoryException(1702, inventoryRequest.getInventoryId());
        }
        inventory.setActive(0);
        inventory.setModifiedDateTime(LocalDateTime.now());
        inventoryRepository.save(inventory);
        String createdMessage = getFormattedMessage(3, inventory.getInventoryId());
        return InventoryMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).build();

    }

    @Override
    public Boolean isInUsage(InventoryRequest inventoryRequest) throws Exception {
    Inventory inventory = inventoryRepository.findByInventoryIdAndSiteAndActive(inventoryRequest.getInventoryId(), inventoryRequest.getSite(),1);
        if (inventory !=null && inventory.isInUsage()) {
            return inventory.isInUsage();
        }
        return false;
    }

    @Override
    public List<Inventory> inventorySplit(InventorySplitRequest inventorySplitRequest) throws Exception {
       Inventory parentInventory = inventoryRepository.findByInventoryIdAndSiteAndActive(inventorySplitRequest.getParentInventoryId(),inventorySplitRequest.getSite(),1);
        if (parentInventory==null) {
            throw new InventoryException(1703, inventorySplitRequest.getParentInventoryId());
        }
        double qtyToSplit = inventorySplitRequest.getQtyToSplit();
        double parentInventoryQty = parentInventory.getQty();
        if (qtyToSplit > parentInventoryQty) {
            throw new InventoryException(1704,inventorySplitRequest.getParentInventoryId());
        }
        int newInventoryQty = inventorySplitRequest.getInventoryIds().stream()
                .mapToInt(InventoryIds::getQty)
                .sum();
        if (newInventoryQty != qtyToSplit) {
            throw new InventoryException(1705);
        }
        List<Inventory> newInventories = new ArrayList<>();
        for (InventoryIds inventoryIdRequest : inventorySplitRequest.getInventoryIds()) {
            Inventory newInventory = new Inventory();
            newInventory.setInventoryId(inventoryIdRequest.getInventoryId());
            newInventory.setSite(inventorySplitRequest.getSite());
            newInventory.setHandle("InventoryBO:"+inventorySplitRequest.getSite()+","+inventoryIdRequest.getInventoryId());
            newInventory.setDescription(parentInventory.getDescription());
            newInventory.setStatus(parentInventory.getStatus());
            newInventory.setItem(inventorySplitRequest.getItem());
            newInventory.setVersion(inventorySplitRequest.getVersion());
            newInventory.setReceiveQty(inventoryIdRequest.getQty());
            newInventory.setQty(inventoryIdRequest.getQty());
            newInventory.setOriginalQty(inventoryIdRequest.getQty());
            newInventory.setRemainingQty(inventoryIdRequest.getQty());
            newInventory.setReceiveDateOrTime(parentInventory.getReceiveDateOrTime());
            newInventory.setReceiveBy(parentInventory.getReceiveBy());
            newInventory.setInventoryIdDataDetails(parentInventory.getInventoryIdDataDetails());
            newInventory.setInventoryIdLocation(inventorySplitRequest.getInventoryIdLocation());
            newInventory.setUsageCount(parentInventory.getUsageCount());
            newInventory.setMaximumUsageCount(parentInventory.getMaximumUsageCount());
            newInventory.setInUsage(false);
            newInventory.setSplittedInventory(true);
            newInventory.setActive(1);
            newInventory.setParentInventoryId(inventorySplitRequest.getParentInventoryId());
            newInventory.setCreatedDateTime(LocalDateTime.now());
            newInventory.setModifiedDateTime(LocalDateTime.now());
            newInventory.setPartitionDate(LocalDateTime.now());
            newInventory = inventoryRepository.save(newInventory);
            newInventories.add(newInventory);
        }
        parentInventory.setQty(parentInventoryQty - qtyToSplit);
        parentInventory.setRemainingQty(parentInventory.getRemainingQty() - qtyToSplit);
        parentInventory.setSplittedInventory(true);
        parentInventory.setParentInventoryId(parentInventory.getParentInventoryId());
        parentInventory.setInventoryIdDataDetails(parentInventory.getInventoryIdDataDetails());
        parentInventory.setInventoryIdLocation(inventorySplitRequest.getInventoryIdLocation());
        parentInventory.setCreatedDateTime(parentInventory.getCreatedDateTime());
        parentInventory.setPartitionDate(LocalDateTime.now());
        inventoryRepository.save(parentInventory);
        return newInventories;
    }

    @Override
    public Inventory retrieveInventory(InventoryRequest inventoryRequest) throws Exception {
        Inventory inventory = inventoryRepository.findByInventoryIdAndSiteAndActive(inventoryRequest.getInventoryId(), inventoryRequest.getSite(), 1);
        if (inventory != null && (inventory.getRemainingQty()>0)) {
            return inventory;
        } else {
            throw new InventoryException(1703, inventoryRequest.getInventoryId());
        }
    }
    @Override
    public Inventory retrieveInventoryReturn(InventoryRequest inventoryRequest) throws Exception {
        Inventory inventory = inventoryRepository.findByInventoryIdAndSiteAndActive(inventoryRequest.getInventoryId(), inventoryRequest.getSite(), 1);
        if (inventory != null) {
            return inventory;
        } else {
            throw new InventoryException(1703, inventoryRequest.getInventoryId());
        }
    }

    @Override
    public InventoryResponseList getTop1000InventoryListByCreationDate(InventoryRequest inventoryRequest) throws Exception {
        List<InventoryResponse> inventoryResponsesList = inventoryRepository.findTop1000ByActiveAndSiteOrderByCreatedDateTimeDesc(1, inventoryRequest.getSite());
        if (inventoryResponsesList != null && !inventoryResponsesList.isEmpty()) {
            return InventoryResponseList.builder().inventoryList(inventoryResponsesList).build();
        } else {
            throw new InventoryException(1703, inventoryRequest.getInventoryId());
        }
    }

    @Override
    public List<Inventory> getInventoryListByCreationDate(InventoryRequest inventoryRequest) throws Exception {
        List<Inventory> response = new ArrayList<>();
        Criteria criteria = Criteria.where("createdDateTime")
                .gte(inventoryRequest.getStartDate())
                .andOperator(Criteria.where("createdDateTime").lte(inventoryRequest.getEndDate()));
        Query query = new Query(criteria);
        List<Inventory> inventoryResponsesList = mongoTemplate.find(query, Inventory.class);
        if(inventoryResponsesList!=null || !inventoryResponsesList.isEmpty())
        {
            return inventoryResponsesList;
        }
        return response;
    }


    @Override
    public InventoryResponseList getInventoryList(InventoryRequest inventoryRequest) throws Exception {
        if (inventoryRequest.getInventoryId() == null || inventoryRequest.getInventoryId().isEmpty()) {
            return  getTop1000InventoryListByCreationDate(inventoryRequest);
        } else {
            List<InventoryResponse> inventoryResponsesList = inventoryRepository.findByInventoryIdContainingIgnoreCaseAndSiteAndActive(inventoryRequest.getInventoryId(), inventoryRequest.getSite(), 1);
            if (inventoryResponsesList != null && !inventoryResponsesList.isEmpty()) {
                return InventoryResponseList.builder().inventoryList(inventoryResponsesList).build();
            } else {
                throw new InventoryException(1703, inventoryRequest.getInventoryId());
            }
        }
    }

    @Override
    public InventoryResponseList retrieveInventoryListByItem(InventoryRequest inventoryRequest) throws Exception {
        if(inventoryRequest.getVersion()==null|| inventoryRequest.getVersion().isEmpty()){
            ItemRequest itemRequest= ItemRequest.builder().site(inventoryRequest.getSite()).item(inventoryRequest.getItem()).build();
          Item  item=  retrieveItem(itemRequest);
          if(item!=null && item.getItem()!=null) {
              inventoryRequest.setVersion(item.getRevision());
          }
        }
        List<InventoryResponse> inventoryResponses = inventoryRepository.findByItemAndVersionAndActiveAndSite(
                inventoryRequest.getItem(),
                inventoryRequest.getVersion(),
                1,
                inventoryRequest.getSite()
        );
        List<InventoryResponse> responseList= inventoryResponses.stream().filter(inventory-> Double.parseDouble(inventory.getRemainingQty())>0.1).collect(Collectors.toList());
        return InventoryResponseList.builder().inventoryList(responseList).build();
    }

    @Override
    public InventoryMessageModel updateRemainingQty(String site, String inventoryId, String remainingQty) throws Exception {
        InventoryMessageModel messageModel=null;
        Inventory inventory=inventoryRepository.findByInventoryIdAndSiteAndActive(inventoryId,site,1);
        if(inventory==null||inventory.getInventoryId()==null||inventory.getInventoryId().isEmpty()){
            throw new InventoryException(1703,inventoryId);
        }else{
            inventory.setRemainingQty(Integer.parseInt(remainingQty));
            inventory.setModifiedDateTime(LocalDateTime.now());
           messageModel= InventoryMessageModel.builder().response(inventoryRepository.save(inventory)).message_details(new MessageDetails("Updated Successfully","S")).build();
        }
        return messageModel;
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
        if (extensionResponse==null) {
            throw new InventoryException(800);
        }
        return extensionResponse;
    }

    @Override
    public List<DataField>  retrievedDataFieldByDataType(String site, String item, String itemVersion)
    {
        List<DataField> dataFieldList = new ArrayList<>();
        ItemRequest itemRequest = ItemRequest.builder().site(site).item(item).revision(itemVersion).build();
        Item retrievedItem = retrieveItem(itemRequest);

        if(retrievedItem != null && retrievedItem.getItem() != null && retrievedItem.getReceiptDataType() != null && !retrievedItem.getReceiptDataType().isEmpty())
        {
            DataTypeRequest dataTypeRequest = DataTypeRequest.builder().site(site).dataType(retrievedItem.getReceiptDataType()).category("Assembly").build();
            DataType dataType = retrieveDataType(dataTypeRequest);
            if(dataType != null && StringUtils.isNotBlank(dataType.getDataType()) && dataType.getDataFieldList() != null && !dataType.getDataFieldList().isEmpty())
            {
                dataFieldList.addAll(dataType.getDataFieldList());
            }
        }
        return dataFieldList;
    }

    public Item retrieveItem(ItemRequest itemRequest){
        Item retrievedItem = webClientBuilder.build()
                .post()
                .uri(itemUrl)
                .bodyValue(itemRequest)
                .retrieve()
                .bodyToMono(Item.class)
                .block();
        return retrievedItem;
    }

    public DataType retrieveDataType(DataTypeRequest dataTypeRequest){
        DataType retrievedDataType = webClientBuilder.build()
                .post()
                .uri(dataType)
                .bodyValue(dataTypeRequest)
                .retrieve()
                .bodyToMono(DataType.class)
                .block();
        return retrievedDataType;
    }
    public Boolean isItemExist(ItemRequest itemRequest){
        Boolean itemExist = webClientBuilder.build()
                .post()
                .uri(itemServiceUrl)
                .bodyValue(itemRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return itemExist;
    }

    public List<String> checkInventoryList(InventorySplitRequest inventoryListRequest) {
        List<String> invList = new ArrayList<>();
        for(String inventoryId : inventoryListRequest.getInventoryList()){
            boolean exists = inventoryRepository.existsByInventoryIdAndSite(inventoryId, inventoryListRequest.getSite());
            if (!exists) {
                invList.add(inventoryId);
            }
        }
        return invList;
    }

    @Override
    public InventoryRequest findByInventoryId(String inventoryId) {
        InventoryRequest inventory = inventoryRepository.findByInventoryId(inventoryId);
        return inventory;
    }



}
