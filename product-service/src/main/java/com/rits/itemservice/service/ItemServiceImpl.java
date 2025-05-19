package com.rits.itemservice.service;

import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.itemservice.dto.*;
import com.rits.itemservice.exception.ItemException;
import com.rits.itemservice.model.*;
import com.rits.itemservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageSource localMessageSource;


    @Value("${bom-service.url}/isExist")
    private String isBomExistUrl;
    @Value("${document-service.url}/retrieveBySite")
    private String documentUrl;
    @Value("${routing-service.url}/isExist")
    private String isRoutingExistUrl;
    @Value("${datatype-service.url}/isExist")
    private String isDataTypeExistUrl;
    @Value("${itemGroup-service.url}/isExist")
    private String isitemGroupExistUrl;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;


    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }


    @Override

    public ItemMessageModel createItem(ItemRequest requestItem) throws Exception {


        if (itemRepository.existsByActiveAndItemAndRevisionAndSite(1, requestItem.getItem(), requestItem.getRevision(), requestItem.getSite())) {
            throw new ItemException(101, requestItem.getItem(), requestItem.getRevision());

        } else {
            try {
                getValidated(requestItem);
                updateCurrentVersionToFalse(requestItem);
            } catch (ItemException itemException) {
                throw itemException;
            } catch (Exception e) {
                throw e;
            }


            if (requestItem.getDescription() == null || requestItem.getDescription().isEmpty()) {
                requestItem.setDescription(requestItem.getItem());
            }

            //saving new
            Item item = createItemBuilder(requestItem);
            String createdMessage = getFormattedMessage(1, requestItem.getItem(), requestItem.getRevision());


            return ItemMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(itemRepository.save(item)).build();

        }
    }

    private void updateCurrentVersionToFalse(ItemRequest requestItem) {
        List<Item> existing = itemRepository.findByItemAndActiveAndSite(requestItem.getItem(), 1, requestItem.getSite());


        existing.stream()
                .filter(Item::isCurrentVersion)
                .map(existingItem -> {
                    existingItem.setCurrentVersion(false);
                    existingItem.setModifiedDateTime(LocalDateTime.now());
                    return existingItem;
                })
                .forEach(itemRepository::save);
    }

    private Item createItemBuilder(ItemRequest requestItem) {
        String bomBO = null, routingBO = null;
        if (requestItem.getBom() != null && !requestItem.getBom().isEmpty()) {

            bomBO = "BomBO:" + requestItem.getSite() + "," + requestItem.getBom() + "," + requestItem.getBomVersion();
        }
        if (requestItem.getRouting() != null && !requestItem.getRouting().isEmpty()) {

            routingBO = "RoutingBO:" + requestItem.getSite() + "," + requestItem.getRouting() + "," + requestItem.getRoutingVersion();
        }
        Item item = Item.builder()
                .site(requestItem.getSite())
                .item(requestItem.getItem())
                .revision(requestItem.getRevision())
                .description(requestItem.getDescription())
                .itemGroup(requestItem.getItemGroup())
                .status(requestItem.getStatus())
                .procurementType(requestItem.getProcurementType())
                .currentVersion(requestItem.isCurrentVersion())
                .itemType(requestItem.getItemType())
//                .lotSize(Double.parseDouble(requestItem.getLotSize()))
                .lotSize(Integer.parseInt(requestItem.getLotSize()))
                .routing(requestItem.getRouting())
                .routingVersion(requestItem.getRoutingVersion())
                .bom(requestItem.getBom())
                .bomVersion(requestItem.getBomVersion())
                .assemblyDataType(requestItem.getAssemblyDataType())
                .removalDataType(requestItem.getRemovalDataType())
                .receiptDataType(requestItem.getReceiptDataType())
                .printDocuments(requestItem.getPrintDocuments())
                .customDataList(requestItem.getCustomDataList())
                .alternateComponentList(requestItem.getAlternateComponentList())
                .bomBO(bomBO)
                .routingBO(routingBO)
                .createdBy(requestItem.getUserId())
                .active(1)
                .inUse(requestItem.isInUse())
                .createdDateTime(LocalDateTime.now())
                .handle("ItemBO:" + requestItem.getSite() + "," + requestItem.getItem() + "," + requestItem.getRevision())
                .build();
        return item;
    }

    private void validateBom(ItemRequest requestItem) throws Exception {
        if (requestItem.getBom() != null && !requestItem.getBom().isEmpty()) {
            if (requestItem.getBomVersion() == null || requestItem.getBomVersion().isEmpty()) {
                throw new ItemException(208, requestItem.getBomVersion());
            }
            IsExistRequest isExistRequest = IsExistRequest.builder().site(requestItem.getSite()).bom(requestItem.getBom()).revision(requestItem.getBomVersion()).build();
            Boolean bomExist = isBomExist(isExistRequest);
            if (!bomExist) {
                throw new ItemException(200, requestItem.getBom());
            }
        }
    }

    private void validateRouting(ItemRequest requestItem) throws Exception {
        if (requestItem.getRouting() != null && !requestItem.getRouting().isEmpty()) {
            IsExistRequest isExistRequest = IsExistRequest.builder().site(requestItem.getSite()).routing(requestItem.getRouting()).version(requestItem.getRoutingVersion()).build();
            Boolean isRoutingExist = isRoutingExist(isExistRequest);
            if (!isRoutingExist) {
                throw new ItemException(110, requestItem.getRevision(), requestItem.getRoutingVersion());
            }
        }
    }

    private void validateDataType(ItemRequest requestItem, String dataType) throws Exception {
        if (dataType != null && !dataType.isEmpty()) {
            IsExistRequest isExistRequest = IsExistRequest.builder().site(requestItem.getSite()).category("Assembly").dataType(dataType).build();
            Boolean isDatatypeExist = isDataTypeExist(isExistRequest);
            if (!isDatatypeExist) {
                throw new ItemException(111, dataType);
            }
        }
    }

    private void validateItemGroup(ItemRequest requestItem) throws Exception {
        if (requestItem.getItemGroup() != null && !requestItem.getItemGroup().isEmpty()) {
            IsExistRequest isExistRequest = IsExistRequest.builder().site(requestItem.getSite()).itemGroup(requestItem.getItemGroup()).build();
            Boolean isItemGroupExist = isItemGroupExist(isExistRequest);
            if (!isItemGroupExist) {
                throw new ItemException(112, requestItem.getItemGroup());
            }
        }
    }

    private void validateAlternateComponents(ItemRequest requestItem) throws Exception {
        List<AlternateComponent> alternateComponentList = requestItem.getAlternateComponentList();

        for (AlternateComponent alternateComponent : alternateComponentList) {
            validateAlternateComponent(requestItem, alternateComponent);
        }
    }

    private void validateAlternateComponent(ItemRequest requestItem, AlternateComponent alternateComponent) throws Exception {
        if (alternateComponent.getAlternateComponent() != null &&
                !alternateComponent.getAlternateComponent().isEmpty() &&
                alternateComponent.getParentMaterial() != null &&
                !alternateComponent.getParentMaterial().isEmpty()) {

            // Check if alternateComponent and parentMaterial are the same
            if (alternateComponent.getAlternateComponent().equals(alternateComponent.getParentMaterial())) {
                // Throw an exception for the alternate component
                throw new ItemException(502, alternateComponent.getAlternateComponent(),alternateComponent.getParentMaterial());
            }
        }

        // Validate alternate component
        if (alternateComponent.getAlternateComponent() != null && !alternateComponent.getAlternateComponent().isEmpty()) {
            if (!isItemExist(alternateComponent.getAlternateComponent(), alternateComponent.getAlternateComponentVersion(), requestItem.getSite())) {
                throw new ItemException(103, alternateComponent.getAlternateComponent(), alternateComponent.getAlternateComponentVersion());
            }
        }

        // Validate parent material
        if (alternateComponent.getParentMaterial() != null && !alternateComponent.getParentMaterial().isEmpty()) {
            if (!isItemExist(alternateComponent.getParentMaterial(), alternateComponent.getParentMaterialVersion(), requestItem.getSite())) {
                throw new ItemException(104, alternateComponent.getParentMaterial(), alternateComponent.getParentMaterialVersion());
            }
        }

        // Validate date range
        validateDateTimeRange(alternateComponent);
    }



    private void validateDateTimeRange(AlternateComponent alternateComponent) throws Exception {
        if (alternateComponent.getValidFromDateTime() != null &&
                !alternateComponent.getValidFromDateTime().isEmpty() &&
                alternateComponent.getValidToDateTime() != null &&
                !alternateComponent.getValidToDateTime().isEmpty()) {

            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

            // Convert strings to OffsetDateTime
            LocalDateTime validFromDateTime = LocalDateTime.parse(alternateComponent.getValidFromDateTime());
            LocalDateTime validToDateTime = LocalDateTime.parse(alternateComponent.getValidToDateTime());

            if (validFromDateTime.isAfter(validToDateTime)) {
                throw new ItemException(109, alternateComponent.getValidFromDateTime(), alternateComponent.getValidToDateTime());
            }
        }
    }

    private void getValidated(ItemRequest requestItem) throws Exception {
        if (requestItem.getRevision() == null || requestItem.getRevision().isEmpty()) {
            throw new ItemException(107, requestItem.getItem());
        }
        if (requestItem.getUserId() == null || requestItem.getUserId().isEmpty()) {
            throw new ItemException(108, requestItem.getItem());
        }

        validateBom(requestItem);
        validateRouting(requestItem);
        validateDataType(requestItem, requestItem.getAssemblyDataType());
        validateDataType(requestItem, requestItem.getReceiptDataType());
        validateDataType(requestItem, requestItem.getRemovalDataType());
        validateItemGroup(requestItem);
        validateAlternateComponents(requestItem);
    }


    private Boolean isItemGroupExist(IsExistRequest isExistRequest) {
        return webClientBuilder.build()
                .post()
                .uri(isitemGroupExistUrl)
                .bodyValue(isExistRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    private Boolean isDataTypeExist(IsExistRequest isExistRequest) {
        return webClientBuilder.build()
                .post()
                .uri(isDataTypeExistUrl)
                .bodyValue(isExistRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    private Boolean isRoutingExist(IsExistRequest isExistRequest) {
        return webClientBuilder.build()
                .post()
                .uri(isRoutingExistUrl)
                .bodyValue(isExistRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }


    @Override
    public ItemMessageModel deleteItem(String item, String revision, String site, String userId) throws Exception {//needs to be changed after the bom ,order,sfc APIs are exposed.

        if (revision == null || revision.isEmpty()) {
            throw new ItemException(107, item);
        }
        if (userId == null || userId.isEmpty()) {
            throw new ItemException(108, item);
        }
        if (itemRepository.existsByActiveAndItemAndRevisionAndSite(1, item, revision, site)) {

            Item existingItem = itemRepository.findByItemAndRevisionAndActiveAndSite(item, revision, 1, site);
            if (existingItem.isInUse()) {
                throw new ItemException(106);
            } else {
                existingItem.setActive(0);
                existingItem.setModifiedBy(userId);
                itemRepository.save(existingItem);

                String deletedMessage = getFormattedMessage(3, item, revision);

                return ItemMessageModel.builder().message_details(new MessageDetails(deletedMessage, "S")).build();

            }

        } else {
            throw new ItemException(102, item, revision);
        }
    }


    @Override
    public ItemMessageModel updateItem(ItemRequest itemRequest) throws Exception {
        if (itemRepository.existsByActiveAndItemAndRevisionAndSite(1, itemRequest.getItem(), itemRequest.getRevision(), itemRequest.getSite())) {
            try {
                getValidated(itemRequest);
                updateCurrentVersionToFalse(itemRequest);
            } catch (ItemException itemException) {
                throw itemException;
            } catch (Exception e) {
                throw e;
            }


            if (itemRequest.getDescription() == null || itemRequest.getDescription().isEmpty()) {
                itemRequest.setDescription(itemRequest.getItem());
            }

            Item existingItem = itemRepository.findByItemAndRevisionAndActiveAndSite(itemRequest.getItem(), itemRequest.getRevision(), 1, itemRequest.getSite());
            if (!existingItem.isInUse()) {
                Item updatedItem = updateItemBuilder(itemRequest, existingItem);
                String updatedMessage = getFormattedMessage(2, itemRequest.getItem(), itemRequest.getRevision());


                return ItemMessageModel.builder().message_details(new MessageDetails(updatedMessage, "S")).response(itemRepository.save(updatedItem)).build();
            } else {
                throw new ItemException(106, itemRequest.getItem(), itemRequest.getRevision());
            }
        } else {
            throw new ItemException(102, itemRequest.getItem(), itemRequest.getRevision());
        }

    }

    private Item updateItemBuilder(ItemRequest itemRequest, Item existingItem) {
        String bomBO = null, routingBO = null;
        if (itemRequest.getBom() != null && !itemRequest.getBom().isEmpty()) {

            bomBO = "BomBO:" + itemRequest.getSite() + "," + itemRequest.getBom() + "," + itemRequest.getBomVersion();
        }
        if (itemRequest.getRouting() != null && !itemRequest.getRouting().isEmpty()) {

            routingBO = "RoutingBO:" + itemRequest.getSite() + "," + itemRequest.getRouting() + "," + itemRequest.getRoutingVersion();
        }
        return Item.builder().site(existingItem.getSite())
                .item(existingItem.getItem())
                .revision(itemRequest.getRevision())
                .description(itemRequest.getDescription())
                .itemGroup(itemRequest.getItemGroup())
                .status(itemRequest.getStatus())
                .procurementType(itemRequest.getProcurementType())
                .currentVersion(itemRequest.isCurrentVersion())
                .itemType(itemRequest.getItemType())
//                .lotSize(Double.parseDouble(itemRequest.getLotSize()))
                .lotSize(Integer.parseInt(itemRequest.getLotSize()))
                .routing(itemRequest.getRouting())
                .routingVersion(itemRequest.getRoutingVersion())
                .bom(itemRequest.getBom())
                .bomVersion(itemRequest.getBomVersion())
                .bomBO(bomBO)
                .routingBO(routingBO)
                .createdBy(existingItem.getCreatedBy())
                .modifiedBy(itemRequest.getUserId())
                .assemblyDataType(itemRequest.getAssemblyDataType())
                .removalDataType(itemRequest.getRemovalDataType())
                .receiptDataType(itemRequest.getReceiptDataType())
                .printDocuments(itemRequest.getPrintDocuments())
                .customDataList(itemRequest.getCustomDataList())
                .inUse(itemRequest.isInUse())
                .alternateComponentList(itemRequest.getAlternateComponentList())
                .active(existingItem.getActive())
                .createdDateTime(existingItem.getCreatedDateTime())
                .modifiedDateTime(LocalDateTime.now())
                .handle(existingItem.getHandle())
                .build();

    }

    @Override
    public ItemResponseList getItemListByCreationDate(String site) throws Exception {
        List<ItemResponse> existing = itemRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        //db.item.find({}).sort({createdDateTime: -1}).limit(50)
        return ItemResponseList.builder().itemList(existing).build();
    }

    @Override
    public ItemResponseList getItemList(String item, String site) throws Exception {

        List<ItemResponse> itemResponses;
        if (item != null && !item.isEmpty()) {
            itemResponses = itemRepository.findByActiveAndSiteAndItemContainingIgnoreCase(1, site, item);
            //db.Item.find({ item: { $regex: ".*" + material + ".*", $options: "i" } })
            if (itemResponses.isEmpty()) {
                throw new ItemException(102, item, "currentVersion");
            }
            return ItemResponseList.builder().itemList(itemResponses).build();
        } else {
            return getItemListByCreationDate(site);
        }

    }

    @Override
    public Boolean isItemExist(String item, String revision, String site) throws Exception {
        if (revision != null && !revision.isEmpty()) {
            return itemRepository.existsByActiveAndItemAndRevisionAndSite(1, item, revision, site);
        } else {
            return itemRepository.existsByActiveAndItemAndCurrentVersionAndSite(1, item, true, site);
        }
    }

    @Override
    public Item retrieveItem(String item, String revision, String site) throws Exception {
        Item existingItem;
        if (revision != null && !revision.isEmpty()) {
            existingItem = itemRepository.findByItemAndRevisionAndActiveAndSite(item, revision, 1, site);
            if (existingItem == null) {
                throw new ItemException(102, item, revision);
            }
        } else {
            existingItem = itemRepository.findByActiveAndSiteAndItemAndCurrentVersion(1, site, item, true);
            if (existingItem == null) {
                throw new ItemException(102, item, revision);
            }
        }
        return existingItem;
    }

    @Override
    public List<ItemResponse> retrieveAllItem(String site) throws Exception {
        List<ItemResponse> existingItem = itemRepository.findByActiveAndSite(1, site);
        return existingItem;
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
        if (extensionResponse == null || extensionResponse.isEmpty()) {
            throw new ItemException(800);
        }
        return extensionResponse;
    }

    @Override
    public PrintDocuments getAvailableDocument(String site, String item, String revision) throws Exception {

        IsExistRequest isExistRequest = IsExistRequest.builder().site(site).build();
        List<PrintDocument> documentResponse = webClientBuilder.build()
                .post()
                .uri(documentUrl)
                .bodyValue(isExistRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PrintDocument>>() {
                })
                .block();
        if (documentResponse == null) {
            throw new ItemException(900);
        }
        if (item != null && !item.isEmpty()) {
            Item exisitingItem = itemRepository.findByItemAndRevisionAndActiveAndSite(item, revision, 1, site);
            if (exisitingItem == null) {
                throw new ItemException(102, item, revision);
            }
            List<PrintDocument> printDocuments = exisitingItem.getPrintDocuments();
            documentResponse.removeIf(document -> printDocuments.stream().anyMatch(printDoc -> printDoc.getDocument().equals(document.getDocument())));

            return PrintDocuments.builder().printDocuments(documentResponse).build();

        }
        return PrintDocuments.builder().printDocuments(documentResponse).build();

    }


    @Override
    public PrintDocuments associateDocumentToPrintDocument(String site, String item, String revision, List<PrintDocument> printDocuments) throws Exception {
        Item existingItem = itemRepository.findByItemAndRevisionAndActiveAndSite(item, revision, 1, site);
        if (existingItem == null) {
            throw new ItemException(102, item, revision);
        }
        List<PrintDocument> existingPrintDocument = existingItem.getPrintDocuments();
        if (existingPrintDocument == null) {
            existingPrintDocument.addAll(printDocuments);
        } else {
            for (PrintDocument document : printDocuments) {
                boolean alreadyExists = existingPrintDocument.stream().anyMatch(printDocument -> Objects.equals(printDocument.getDocument(), document.getDocument()));
                if (!alreadyExists) {
                    existingPrintDocument.add(document);
                }
            }

        }
        existingItem.setPrintDocuments(existingPrintDocument);
        existingItem.setModifiedDateTime(LocalDateTime.now());
        itemRepository.save(existingItem);

        return PrintDocuments.builder().printDocuments(existingPrintDocument).build();
    }

    @Override
    public PrintDocuments removeDocumentFromPrintDocument(String site, String item, String revision, List<PrintDocument> printDocuments) throws Exception {
        Item existingItem = itemRepository.findByItemAndRevisionAndActiveAndSite(item, revision, 1, site);
        if (existingItem == null) {
            throw new ItemException(102, item, revision);
        }
        List<PrintDocument> existingPrintDocuments = existingItem.getPrintDocuments();
        if (existingPrintDocuments == null) {
            return null;
        }
        for (PrintDocument printDocument : printDocuments) {
            existingPrintDocuments.removeIf(existingPrintDocument -> existingPrintDocument.getDocument().equals(printDocument.getDocument()));
        }
        existingItem.setPrintDocuments(existingPrintDocuments);
        existingItem.setModifiedDateTime(LocalDateTime.now());
        itemRepository.save(existingItem);
        return PrintDocuments.builder().printDocuments(existingPrintDocuments).build();
    }

    @Override
    public ItemResponseListForFilter getItemListByCreationDateItem(String site) {
        List<Item> existing = itemRepository.findTop50BySiteAndActiveOrderByCreatedDateTimeDesc(site, 1);

        // Convert item & revision into "item/revision" format
        List<ItemResponseForFilter> formattedItems = existing.stream()
                .map(item -> new ItemResponseForFilter(item.getItem() + "/" + item.getRevision()))
                .collect(Collectors.toList());

        return ItemResponseListForFilter.builder().itemList(formattedItems).build();
    }

    public Boolean isBomExist(IsExistRequest isExistRequest) {
        return webClientBuilder.build()
                .post()
                .uri(isBomExistUrl)
                .bodyValue(isExistRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    @Override
    public AuditLogRequest createAuditLog(ItemRequest itemRequest) {
        return AuditLogRequest.builder()
                .site(itemRequest.getSite())
                .action_code("ITEM-CREATE")
                .action_detail("Item Created " + itemRequest.getItem())
                .action_detail_handle("ActionDetailBO:" + itemRequest.getSite() + "," + "ITEM-CREATE" + "," + itemRequest.getUserId() + ":" + "com.rits.itemservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(itemRequest.getUserId())
                .router(itemRequest.getRouting())
                .router_revision(itemRequest.getRoutingVersion())
                .txnId("ITEM-CREATE" + LocalDateTime.now() + itemRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Create")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest deleteAuditLog(ItemRequest itemRequest) {
        return AuditLogRequest.builder()
                .site(itemRequest.getSite())
                .action_code("ITEM-DELETE")
                .action_detail("Item Deleted " + itemRequest.getDescription())
                .action_detail_handle("ActionDetailBO:" + itemRequest.getSite() + "," + "ITEM-DELETE" + "," + itemRequest.getUserId() + ":" + "com.rits.itemservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(itemRequest.getUserId())
                .txnId("ITEM-DELETE" + LocalDateTime.now() + itemRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Delete")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest updateAuditLog(ItemRequest itemRequest) {
        return AuditLogRequest.builder()
                .site(itemRequest.getSite())
                .action_code("ITEM-UPDATE")
                .action_detail("Item Updated " + itemRequest.getDescription())
                .action_detail_handle("ActionDetailBO:" + itemRequest.getSite() + "," + "ITEM-UPDATE" + "," + itemRequest.getUserId() + ":" + "com.rits.itemservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(itemRequest.getUserId())
                .txnId("ITEM-UPDATE" + LocalDateTime.now() + itemRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Update")
                .topic("audit-log")
                .build();
    }
}



