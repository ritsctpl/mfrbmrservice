package com.rits.nextnumbergeneratorservice.service;

import com.rits.assemblyservice.dto.AuditLogRequest;
import com.rits.nextnumbergeneratorservice.dto.*;
import com.rits.nextnumbergeneratorservice.exception.NextNumberGeneratorException;
import com.rits.nextnumbergeneratorservice.model.MessageDetails;
import com.rits.nextnumbergeneratorservice.model.NextNumberMessageModel;
import com.rits.nextnumbergeneratorservice.model.NextNumberGenerator;
import com.rits.nextnumbergeneratorservice.repository.NextNumberGeneratorRepository;
import com.rits.pcuheaderservice.dto.Pcu;
import com.rits.pcuheaderservice.dto.PcuHeaderRequest;
import com.rits.shoporderrelease.dto.Item;
import com.rits.shoporderrelease.dto.ItemRequest;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.util.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class NextNumberGeneratorServiceImpl implements NextNumberGeneratorService {
    private final NextNumberGeneratorRepository nextNumberGeneratorRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;
    private static final List<String> MATERIAL_BASED_TYPES = Arrays.asList("PCU_RELEASE", "PCU_SERIALIZE", "FLOOR_STOCK","RMA_PCU_NUMBER","BATCH_NUMBER","Batch Number", "PCU release", "Process Order");
    private static final List<String> NON_MATERIAL_BASED_TYPES = Arrays.asList("SHOP_ORDER", "INCIDENT_NUMBER","PROCESS_LOT","RMA_NUMBER","MFR","BPR","BMR");

    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    @Value("${item-service.url}/retrieve")
    private String itemUrl;

    @Value("${item-service.url}/isExist")
    private String isExistItemUrl;

    @Value("${itemGroup-service.url}/isExist")
    private String isExistItemGroupUrl;

    @Value("${itemGroup-service.url}/retrieve")
    private String retrieveItemGroupUrl;

    @Value("${shoporder-service.url}/retrieve")
    private String retrieveShopOrderUrl;
    @Value("${inventory-service.url}/checkInventoryList")
    private String inventoryUrl;

    @Value("${mfrrecipes-service.url}/getMfrList")
    private String mfrrecipesUrl;

    @Value("${bmr-service.url}/getBmrList")
    private String bmrRecipesUrl;

    @Value("${pcuheader-service.url}/retrievePcuHeaderList")
    private String pcuHeaderUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }
    @Override
    public NextNumberMessageModel create(NextNumberGeneratorRequest nextNumberGeneratorRequest) throws Exception {
        if(nextNumberGeneratorRequest.getPrefix()==null)
            nextNumberGeneratorRequest.setPrefix("");

        if(nextNumberGeneratorRequest.getSuffix()==null)
            nextNumberGeneratorRequest.setSuffix("");

        if(nextNumberGeneratorRequest.getItem() != null && !nextNumberGeneratorRequest.getItem().equals("") || nextNumberGeneratorRequest.getItemGroup()!= null &&!nextNumberGeneratorRequest.getItemGroup().equals(""))
            validateItemOrItemGroupValue(nextNumberGeneratorRequest);

        NextNumberGenerator nextNumberGenerator = null;
//        NextNumberGenerator nextNumberGenerator = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndOrderType(1,nextNumberGeneratorRequest.getSite(),nextNumberGeneratorRequest.getNumberType(),nextNumberGeneratorRequest.getObject(), nextNumberGeneratorRequest.getOrderType());
        if(!nextNumberGeneratorRequest.getNumberType().equalsIgnoreCase("Process Order") && !nextNumberGeneratorRequest.getNumberType().equals("PCU release") && !nextNumberGeneratorRequest.getNumberType().equals("PCU serialize") && !nextNumberGeneratorRequest.getNumberType().equals("Batch Number") && !nextNumberGeneratorRequest.getNumberType().equals("Floor Stock Receipt") && !nextNumberGeneratorRequest.getNumberType().equals("RMA PCU Number")) {
            nextNumberGenerator = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberType(1, nextNumberGeneratorRequest.getSite(), nextNumberGeneratorRequest.getNumberType());
            nextNumberGeneratorRequest.setObject("");
            nextNumberGeneratorRequest.setObjectVersion("");
            nextNumberGeneratorRequest.setDefineBy("");
        }else
            nextNumberGenerator = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersionAndDefineBy(1,nextNumberGeneratorRequest.getSite(),nextNumberGeneratorRequest.getNumberType(),nextNumberGeneratorRequest.getObject(),nextNumberGeneratorRequest.getObjectVersion(), nextNumberGeneratorRequest.getDefineBy());
        if(nextNumberGenerator != null)
        {
            throw new NextNumberGeneratorException(5100);
        }
        if(nextNumberGeneratorRequest.getMaxSequence() != 0 && nextNumberGeneratorRequest.getMaxSequence() < nextNumberGeneratorRequest.getCurrentSequence())
        {
            throw new NextNumberGeneratorException(5105);
        }
        if(nextNumberGeneratorRequest.getMaxSequence() < nextNumberGeneratorRequest.getIncrementBy())
        {
            throw new NextNumberGeneratorException(5108);
        }
        if(nextNumberGeneratorRequest.getSequenceLength()!=0 && nextNumberGeneratorRequest.getSequenceLength()< String.valueOf(nextNumberGeneratorRequest.getCurrentSequence()).length())
        {
            throw new NextNumberGeneratorException(5106,nextNumberGeneratorRequest.getObject());
        }
        if(nextNumberGeneratorRequest.getSequenceLength()!=0 && nextNumberGeneratorRequest.getSequenceLength() < String.valueOf(nextNumberGeneratorRequest.getMaxSequence()).length())
        {
            throw new NextNumberGeneratorException(5110,nextNumberGeneratorRequest.getObject());
        }
        if(nextNumberGeneratorRequest.getSequenceLength()==0 && nextNumberGeneratorRequest.getSequenceLength()< String.valueOf(nextNumberGeneratorRequest.getCurrentSequence()).length())
        {
            throw new NextNumberGeneratorException(5106,nextNumberGeneratorRequest.getObject());
        }
        String sampleNextNumber = sampleNextNumberOnCreate(nextNumberGeneratorRequest);
        GeneratePrefixAndSuffixRequest generatePrefixRequest = buildPrefixAndSuffixRequest(nextNumberGeneratorRequest,"p");
        generatePrefixRequest.setPrefix(nextNumberGeneratorRequest.getPrefix());

        GeneratePrefixAndSuffixRequest generateSuffixRequest = buildPrefixAndSuffixRequest(nextNumberGeneratorRequest,"s");
        generateSuffixRequest.setSuffix(nextNumberGeneratorRequest.getSuffix());

        int nextNo = nextNumberGeneratorRequest.getCurrentSequence();
        String result = padZeros(nextNo, nextNumberGeneratorRequest.getSequenceLength());

        NextNumberGenerator createNextNumber = nextNumberBuilder(nextNumberGeneratorRequest);

        createNextNumber.setHandle("NextNumberBO:"+nextNumberGeneratorRequest.getSite()+","+nextNumberGeneratorRequest.getNumberType()+","+nextNumberGeneratorRequest.getOrderType()+","+nextNumberGeneratorRequest.getObject());
        createNextNumber.setSampleNextNumber(generatePrefixAndSuffix(generatePrefixRequest)+result+generatePrefixAndSuffix(generateSuffixRequest));
        createNextNumber.setCreatedBy(nextNumberGeneratorRequest.getUserId());
        createNextNumber.setCreatedDateTime(LocalDateTime.now());

        if(nextNumberGeneratorRequest.getDefineBy().equalsIgnoreCase("item"))
        {
            createNextNumber.setItemBO("ItemBO:"+nextNumberGeneratorRequest.getSite()+","+nextNumberGeneratorRequest.getObject()+","+nextNumberGeneratorRequest.getObjectVersion());
            createNextNumber.setItem(nextNumberGeneratorRequest.getObject());
        }
        else if(nextNumberGeneratorRequest.getDefineBy().equalsIgnoreCase("Item Group")){
            createNextNumber.setItemGroupBO("ItemGroupBO:"+nextNumberGeneratorRequest.getSite()+","+nextNumberGeneratorRequest.getObject());
            createNextNumber.setItemGroup(nextNumberGeneratorRequest.getObject());
        }

        AuditLogRequest activityLog = AuditLogRequest.builder()
                .site(nextNumberGeneratorRequest.getSite())
                .action_code("NEXTNUMBER-CREATE")
                .action_detail("NextNumber Created "+nextNumberGeneratorRequest.getObject())
                .action_detail_handle("ActionDetailBO:"+nextNumberGeneratorRequest.getSite()+","+"NEXTNUMBER-CREATE"+nextNumberGeneratorRequest.getUserBO()+":"+"com.rits.nextnumbergeneratorservice.service")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(nextNumberGeneratorRequest.getUserBO())
                .txnId("NEXTNUMBER-CREATE"+String.valueOf(LocalDateTime.now())+nextNumberGeneratorRequest.getUserBO())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("NEXT_NUMBER")
                .build();

        webClientBuilder.build()
                .post()
                .uri(auditlogUrl)
                .bodyValue(activityLog)
                .retrieve()
                .bodyToMono(AuditLogRequest.class)
                .block();

        nextNumberGeneratorRepository.save(createNextNumber);
        String createdMessage = getFormattedMessage(4);
        MessageDetails messageDetails = MessageDetails.builder().msg(createdMessage).msg_type("S").build();
//        createNextNumber.setSampleNextNumber(generatePrefixAndSuffix(generatePrefixRequest)+result+generatePrefixAndSuffix(generateSuffixRequest));
        return NextNumberMessageModel.builder().message_details(messageDetails).response(createNextNumber).build();
    }

    public void validateItemOrItemGroupValue(NextNumberGeneratorRequest nextNumberGeneratorRequest)throws Exception{
        String itemorItemGroup = "";
        boolean itemCheck = false, itemGroupCheck = false;
        if(!nextNumberGeneratorRequest.getItem().equals("*") && !nextNumberGeneratorRequest.getItemGroup().equals("*")){
            if (nextNumberGeneratorRequest.getDefineBy().equals("Item")) {
                itemorItemGroup = nextNumberGeneratorRequest.getItem();

                ItemRequest itemRequest = ItemRequest.builder().site(nextNumberGeneratorRequest.getSite()).item(nextNumberGeneratorRequest.getItem()).revision(nextNumberGeneratorRequest.getObjectVersion()).build();
                Boolean isExistItem = webClientBuilder
                        .build()
                        .post()
                        .uri(isExistItemUrl)
                        .body(BodyInserters.fromValue(itemRequest))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Boolean>() {
                        })
                        .block();
//                for (ItemResponse itemLists : itemList.getItemList()) {
//                    if (itemLists.getItem().equals(itemorItemGroup) && itemLists.getRevision().equals(nextNumberGeneratorRequest.getObjectVersion())) {
//                        itemCheck = true;
//                    }
//                }
                if(isExistItem)
                    itemCheck = true;

            } else {
                itemorItemGroup = nextNumberGeneratorRequest.getItemGroup();

                ItemGroupRequest itemGroupRequest = ItemGroupRequest.builder().site(nextNumberGeneratorRequest.getSite()).groupName(itemorItemGroup).build();
                Boolean isExistItemGroup = webClientBuilder
                        .build()
                        .post()
                        .uri(isExistItemGroupUrl)
                        .body(BodyInserters.fromValue(itemGroupRequest))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Boolean>() {
                        })
                        .block();

//                for (GroupNameResponse itemGroupLists : itemGroupList.getGroupNameList()) {
//                    if (itemGroupLists.getGroupName().equals(itemorItemGroup)) {
//                        itemGroupCheck = true;
//                    }
//                }
                if(isExistItemGroup)
                    itemGroupCheck = true;
            }

            if (!itemCheck && nextNumberGeneratorRequest.getDefineBy().equals("Item"))
                throw new NextNumberGeneratorException(4);

            if (!itemGroupCheck && nextNumberGeneratorRequest.getDefineBy().equals("Item Group"))
                throw new NextNumberGeneratorException(5);

            nextNumberGeneratorRequest.setObject(itemorItemGroup);

        }else{
            if(nextNumberGeneratorRequest.getDefineBy().equals("Item"))
                nextNumberGeneratorRequest.setObject(nextNumberGeneratorRequest.getItem());
            else
                nextNumberGeneratorRequest.setObject(nextNumberGeneratorRequest.getItemGroup());
        }

    }

    @Override
    public NextNumberMessageModel updateNextNumber(NextNumberGeneratorRequest nextNumberGeneratorRequest) throws Exception
    {
        if(nextNumberGeneratorRequest.getPrefix()==null)
            nextNumberGeneratorRequest.setPrefix("");

        if(nextNumberGeneratorRequest.getSuffix()==null)
            nextNumberGeneratorRequest.setSuffix("");
        if(nextNumberGeneratorRequest.getItem() != null && !nextNumberGeneratorRequest.getItem().equals("") || nextNumberGeneratorRequest.getItemGroup()!= null &&!nextNumberGeneratorRequest.getItemGroup().equals(""))
            validateItemOrItemGroupValue(nextNumberGeneratorRequest);
//        NextNumberGenerator nextNumberGenerator = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndOrderType(1,nextNumberGeneratorRequest.getSite(),nextNumberGeneratorRequest.getNumberType(),nextNumberGeneratorRequest.getObject(), nextNumberGeneratorRequest.getOrderType());
        NextNumberGenerator nextNumberGenerator = null;

        if(!nextNumberGeneratorRequest.getNumberType().equalsIgnoreCase("Process Order") && !nextNumberGeneratorRequest.getNumberType().equals("PCU release") && !nextNumberGeneratorRequest.getNumberType().equals("PCU serialize") && !nextNumberGeneratorRequest.getNumberType().equals("Batch Number") && !nextNumberGeneratorRequest.getNumberType().equals("Floor Stock Receipt") && !nextNumberGeneratorRequest.getNumberType().equals("RMA PCU Number")) {
            nextNumberGenerator = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberType(1, nextNumberGeneratorRequest.getSite(), nextNumberGeneratorRequest.getNumberType());
            nextNumberGeneratorRequest.setObject("");
            nextNumberGeneratorRequest.setObjectVersion("");
            nextNumberGeneratorRequest.setDefineBy("");
        }else
            nextNumberGenerator = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersionAndDefineBy(1,nextNumberGeneratorRequest.getSite(),nextNumberGeneratorRequest.getNumberType(),nextNumberGeneratorRequest.getObject(),nextNumberGeneratorRequest.getObjectVersion(), nextNumberGeneratorRequest.getDefineBy());

        if(nextNumberGenerator == null || nextNumberGenerator.getNumberType()==null)
        {
            throw new NextNumberGeneratorException(5101);
        }
        if(nextNumberGeneratorRequest.getMaxSequence() != 0 && nextNumberGeneratorRequest.getMaxSequence() < nextNumberGeneratorRequest.getCurrentSequence())
        {
            throw new NextNumberGeneratorException(5105);
        }
        if( nextNumberGeneratorRequest.getMaxSequence() < nextNumberGeneratorRequest.getIncrementBy())
        {
            throw new NextNumberGeneratorException(5108);
        }
        if(nextNumberGeneratorRequest.getSequenceLength()!=0 && nextNumberGeneratorRequest.getSequenceLength()< String.valueOf(nextNumberGeneratorRequest.getCurrentSequence()).length())
        {
            throw new NextNumberGeneratorException(5106);
        }
        if(nextNumberGeneratorRequest.getSequenceLength()!=0 && nextNumberGeneratorRequest.getSequenceLength() < String.valueOf(nextNumberGeneratorRequest.getMaxSequence()).length())
        {
            throw new NextNumberGeneratorException(5110,nextNumberGeneratorRequest.getObject());
        }
        if(nextNumberGeneratorRequest.getSequenceLength()==0 && nextNumberGeneratorRequest.getSequenceLength()< String.valueOf(nextNumberGeneratorRequest.getCurrentSequence()).length())
        {
            throw new NextNumberGeneratorException(5106,nextNumberGeneratorRequest.getObject());
        }
        String sampleNextNumber = sampleNextNumberOnCreate(nextNumberGeneratorRequest);
        GeneratePrefixAndSuffixRequest generatePrefixRequest = buildPrefixAndSuffixRequest(nextNumberGeneratorRequest,"p");

        generatePrefixRequest.setPrefix(nextNumberGeneratorRequest.getPrefix());

        GeneratePrefixAndSuffixRequest generateSuffixRequest = buildPrefixAndSuffixRequest(nextNumberGeneratorRequest,"s");
        generateSuffixRequest.setSuffix(nextNumberGeneratorRequest.getSuffix());

        NextNumberGenerator updateNextNumber = nextNumberBuilder(nextNumberGeneratorRequest);

        int nextNo = nextNumberGeneratorRequest.getCurrentSequence();
        String result = padZeros(nextNo, nextNumberGeneratorRequest.getSequenceLength());

        updateNextNumber.setHandle(nextNumberGenerator.getHandle());
        updateNextNumber.setSite(nextNumberGenerator.getSite());
        updateNextNumber.setSampleNextNumber(generatePrefixAndSuffix(generatePrefixRequest)+result+generatePrefixAndSuffix(generateSuffixRequest));
        updateNextNumber.setCreatedBy(nextNumberGenerator.getCreatedBy());
        updateNextNumber.setCreatedDateTime(nextNumberGenerator.getCreatedDateTime());
        updateNextNumber.setModifiedDateTime(LocalDateTime.now());
        updateNextNumber.setModifiedBy(nextNumberGeneratorRequest.getUserId());

        if(nextNumberGeneratorRequest.getDefineBy().equalsIgnoreCase("item"))
        {
            updateNextNumber.setItemBO("ItemBO:"+nextNumberGeneratorRequest.getSite()+","+nextNumberGeneratorRequest.getObject()+","+nextNumberGeneratorRequest.getObjectVersion());
            updateNextNumber.setItem(nextNumberGeneratorRequest.getObject());
        }
        else if(nextNumberGeneratorRequest.getDefineBy().equalsIgnoreCase("Item Group")){
            updateNextNumber.setItemGroupBO("ItemGroupBO:"+nextNumberGeneratorRequest.getSite()+","+nextNumberGeneratorRequest.getObject());
            updateNextNumber.setItemGroup(nextNumberGeneratorRequest.getObject());
        }

        AuditLogRequest activityLog = AuditLogRequest.builder()
                .site(nextNumberGeneratorRequest.getSite())
                .action_code("NEXTNUMBER-UPDATE")
                .action_detail("NextNumber Updated "+nextNumberGeneratorRequest.getObject())
                .action_detail_handle("ActionDetailBO:"+nextNumberGeneratorRequest.getSite()+","+"NEXTNUMBER-UPDATE"+nextNumberGeneratorRequest.getUserBO()+":"+"com.rits.nextnumbergeneratorservice.service")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(nextNumberGeneratorRequest.getUserBO())
                .txnId("NEXTNUMBER-UPDATE"+String.valueOf(LocalDateTime.now())+nextNumberGeneratorRequest.getUserBO())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("NEXT_NUMBER")
                .build();

        webClientBuilder.build()
                .post()
                .uri(auditlogUrl)
                .bodyValue(activityLog)
                .retrieve()
                .bodyToMono(AuditLogRequest.class)
                .block();

        nextNumberGeneratorRepository.save(updateNextNumber);
        String updatedMessage = getFormattedMessage(5);
        MessageDetails messageDetails = MessageDetails.builder().msg(updatedMessage).msg_type("S").build();
//        updateNextNumber.setSampleNextNumber(generatePrefixAndSuffix(generatePrefixRequest)+result+generatePrefixAndSuffix(generateSuffixRequest));
        return NextNumberMessageModel.builder().message_details(messageDetails).response(updateNextNumber).build();
    }

    public NextNumberGenerator nextNumberBuilder(NextNumberGeneratorRequest nextNumberGeneratorRequest) throws Exception
    {
        NextNumberGenerator nextNumber = NextNumberGenerator.builder()
                .handle(nextNumberGeneratorRequest.getHandle())
                .site(nextNumberGeneratorRequest.getSite())
                .numberType(nextNumberGeneratorRequest.getNumberType())
                .orderType(nextNumberGeneratorRequest.getOrderType())
                .defineBy(nextNumberGeneratorRequest.getDefineBy())
                .object(nextNumberGeneratorRequest.getObject())
                .objectVersion(nextNumberGeneratorRequest.getObjectVersion())
                .prefix(nextNumberGeneratorRequest.getPrefix())
                .suffix(nextNumberGeneratorRequest.getSuffix())
                .numberBase(nextNumberGeneratorRequest.getNumberBase())
                .sequenceLength(nextNumberGeneratorRequest.getSequenceLength())
                .minSequence(nextNumberGeneratorRequest.getMinSequence())
                .maxSequence(nextNumberGeneratorRequest.getMaxSequence())
                .warningThreshold(nextNumberGeneratorRequest.getWarningThreshold())
                .incrementBy(nextNumberGeneratorRequest.getIncrementBy())
                .currentSequence(nextNumberGeneratorRequest.getCurrentSequence())
                .resetSequenceNumber(nextNumberGeneratorRequest.getResetSequenceNumber())
                .nextNumberActivity(nextNumberGeneratorRequest.getNextNumberActivity())
                .createContinuousSfcOnImport(nextNumberGeneratorRequest.isCreateContinuousSfcOnImport())
                .commitNextNumberChangesImmediately(nextNumberGeneratorRequest.isCommitNextNumberChangesImmediately())
                .sampleNextNumber(nextNumberGeneratorRequest.getSampleNextNumber())
                .containerInput(nextNumberGeneratorRequest.getContainerInput())
                .active(1)
                .build();
        return nextNumber;
    }

    public GeneratePrefixAndSuffixRequest buildPrefixAndSuffixRequest(NextNumberGeneratorRequest nextNumberGeneratorRequest, String value)throws Exception
    {
        String itemGroup = "",itemGroupValue = "", item = "", shopOrderBo = "";
        boolean isPriorityPresent = false;
        String val = value.equals("p")? nextNumberGeneratorRequest.getPrefix(): nextNumberGeneratorRequest.getSuffix();

        if(value.equals("p")) {
            if(!nextNumberGeneratorRequest.getObject().equals("")) {
                if (nextNumberGeneratorRequest.getItem()!=null && !nextNumberGeneratorRequest.getItem().equals("*") && nextNumberGeneratorRequest.getItemGroup()!=null &&!nextNumberGeneratorRequest.getItemGroup().equals("*")) {

                    if (nextNumberGeneratorRequest.getPrefix().equals("%ITEM_GROUP_BO.(0)%") || nextNumberGeneratorRequest.getPrefix().equals("%ITEM_GROUP_BO.(1)%") || nextNumberGeneratorRequest.getPrefix().equals("%ITEM_GROUP%")) {
                        if (nextNumberGeneratorRequest.getItem() != null && !nextNumberGeneratorRequest.getItem().isEmpty()) {
                            ItemRequest itemRequest = ItemRequest.builder().site(nextNumberGeneratorRequest.getSite()).item(nextNumberGeneratorRequest.getItem()).revision(nextNumberGeneratorRequest.getObjectVersion()).build();
                            Item retrieveItem = webClientBuilder
                                    .build()
                                    .post()
                                    .uri(itemUrl)
                                    .body(BodyInserters.fromValue(itemRequest))
                                    .retrieve()
                                    .bodyToMono(new ParameterizedTypeReference<Item>() {
                                    })
                                    .block();

                            if (retrieveItem.getItemGroup() == null || retrieveItem.getItemGroup().isEmpty())
                                throw new NextNumberGeneratorException(7);
                            itemGroupValue = retrieveItem.getItemGroup();
                        } else
                            itemGroupValue = nextNumberGeneratorRequest.getItemGroup();

                        ItemGroupRequest itemGroupRequest = ItemGroupRequest.builder().site(nextNumberGeneratorRequest.getSite()).groupName(itemGroupValue).build();
                        ItemGroup retrieveItemGroup = webClientBuilder
                                .build()
                                .post()
                                .uri(retrieveItemGroupUrl)
                                .body(BodyInserters.fromValue(itemGroupRequest))
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<ItemGroup>() {
                                })
                                .block();
                        itemGroup = retrieveItemGroup.getHandle();
                    } else if (nextNumberGeneratorRequest.getPrefix().equals("%ITEM_BO.(1)%") || nextNumberGeneratorRequest.getPrefix().equals("%ITEM_BO.(2)%") || nextNumberGeneratorRequest.getPrefix().equals("%ITEM%")) {
                        if (nextNumberGeneratorRequest.getItem() != null && !nextNumberGeneratorRequest.getItem().isEmpty()) {
                            ItemRequest itemRequest = ItemRequest.builder().site(nextNumberGeneratorRequest.getSite()).item(nextNumberGeneratorRequest.getItem()).revision(nextNumberGeneratorRequest.getObjectVersion()).build();
                            Item retrieveItem = webClientBuilder
                                    .build()
                                    .post()
                                    .uri(itemUrl)
                                    .body(BodyInserters.fromValue(itemRequest))
                                    .retrieve()
                                    .bodyToMono(new ParameterizedTypeReference<Item>() {
                                    })
                                    .block();
                            item = retrieveItem.getHandle();
                        } else
                            item = "ITEM_BO";
                    }
                }
            }
            if (nextNumberGeneratorRequest.getPrefix().equals("%SHOP_ORDER_BO.(1)%")) {
                shopOrderBo = "SHOP_ORDER_BO";
            }
        } else {
            if(!nextNumberGeneratorRequest.getObject().equals("")) {
                if (nextNumberGeneratorRequest.getItem()!=null && !nextNumberGeneratorRequest.getItem().equals("*") && nextNumberGeneratorRequest.getItemGroup()!=null &&!nextNumberGeneratorRequest.getItemGroup().equals("*")) {

                    if (nextNumberGeneratorRequest.getSuffix().equals("%ITEM_GROUP_BO.(0)%") || nextNumberGeneratorRequest.getSuffix().equals("%ITEM_GROUP_BO.(1)%") || nextNumberGeneratorRequest.getSuffix().equals("%ITEM_GROUP%")) {
                        if (nextNumberGeneratorRequest.getItem() != null && !nextNumberGeneratorRequest.getItem().isEmpty()) {
                            ItemRequest itemRequest = ItemRequest.builder().site(nextNumberGeneratorRequest.getSite()).item(nextNumberGeneratorRequest.getItem()).revision(nextNumberGeneratorRequest.getObjectVersion()).build();
                            Item retrieveItem = webClientBuilder
                                    .build()
                                    .post()
                                    .uri(itemUrl)
                                    .body(BodyInserters.fromValue(itemRequest))
                                    .retrieve()
                                    .bodyToMono(new ParameterizedTypeReference<Item>() {
                                    })
                                    .block();

                            if (retrieveItem.getItemGroup() == null || retrieveItem.getItemGroup().isEmpty())
                                throw new NextNumberGeneratorException(7);
                            itemGroupValue = retrieveItem.getItemGroup();
                        } else
                            itemGroupValue = nextNumberGeneratorRequest.getItemGroup();

                        ItemGroupRequest itemGroupRequest = ItemGroupRequest.builder().site(nextNumberGeneratorRequest.getSite()).groupName(itemGroupValue).build();
                        ItemGroup retrieveItemGroup = webClientBuilder
                                .build()
                                .post()
                                .uri(retrieveItemGroupUrl)
                                .body(BodyInserters.fromValue(itemGroupRequest))
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<ItemGroup>() {
                                })
                                .block();
                        itemGroup = retrieveItemGroup.getHandle();
                    } else if (nextNumberGeneratorRequest.getSuffix().equals("%ITEM_BO.(1)%") || nextNumberGeneratorRequest.getSuffix().equals("%ITEM_BO.(2)%") || nextNumberGeneratorRequest.getSuffix().equals("%ITEM%")) {
                        if (nextNumberGeneratorRequest.getItem() != null && !nextNumberGeneratorRequest.getItem().isEmpty()) {
                            ItemRequest itemRequest = ItemRequest.builder().site(nextNumberGeneratorRequest.getSite()).item(nextNumberGeneratorRequest.getItem()).revision(nextNumberGeneratorRequest.getObjectVersion()).build();
                            Item retrieveItem = webClientBuilder
                                    .build()
                                    .post()
                                    .uri(itemUrl)
                                    .body(BodyInserters.fromValue(itemRequest))
                                    .retrieve()
                                    .bodyToMono(new ParameterizedTypeReference<Item>() {
                                    })
                                    .block();
                            item = retrieveItem.getHandle();
                        } else
                            item = "ITEM_BO";

                    }
                }
            }
            if (nextNumberGeneratorRequest.getSuffix().equals("%SHOP_ORDER_BO.(1)%")) {
                shopOrderBo = "SHOP_ORDER_BO";
            }
        }
        GeneratePrefixAndSuffixRequest.GeneratePrefixAndSuffixRequestBuilder builder = GeneratePrefixAndSuffixRequest.builder()
                .site(nextNumberGeneratorRequest.getSite())
                .nextNumberActivity(nextNumberGeneratorRequest.getNextNumberActivity())
                .object(nextNumberGeneratorRequest.getObject())
                .objectVersion(nextNumberGeneratorRequest.getObjectVersion())
                .userBO(nextNumberGeneratorRequest.getUserBO())
                .currentSequence(nextNumberGeneratorRequest.getCurrentSequence())
                .value(val)
                .nonStartObject(nextNumberGeneratorRequest.getObject())
                .nonStartVersion(nextNumberGeneratorRequest.getObjectVersion())
                .itemBO(item)
                .itemGroupBO(itemGroup)
                .shopOrderBO(shopOrderBo)
                .userBO(nextNumberGeneratorRequest.getUserBO());

        if(val.equals("p")) {
            if (nextNumberGeneratorRequest.getPrefix().equals("%PRIORITY%")) {
                builder.priority(0);
            }
        }else{
            if (nextNumberGeneratorRequest.getSuffix().equals("%PRIORITY%")) {
                builder.priority(0);
            }
        }
        GeneratePrefixAndSuffixRequest generateRequest = builder.build();

        return generateRequest;
    }

    @Override
    public NextNumberGenerator retrieveNextNumber(NextNumberGeneratorRequest nextNumberGeneratorRequest)throws Exception // make for itemgroup
    {
        NextNumberGenerator existingRecord = null;
        if (!nextNumberGeneratorRequest.getNumberType().equalsIgnoreCase("Process Order") && !nextNumberGeneratorRequest.getNumberType().equals("PCU release") && !nextNumberGeneratorRequest.getNumberType().equals("PCU serialize") && !nextNumberGeneratorRequest.getNumberType().equals("Batch Number") && !nextNumberGeneratorRequest.getNumberType().equals("Floor Stock Receipt") && !nextNumberGeneratorRequest.getNumberType().equals("RMA PCU Number") && !nextNumberGeneratorRequest.getNumberType().equals("Process Order")){
            if(!nextNumberGeneratorRequest.getNumberType().isEmpty() && (nextNumberGeneratorRequest.getObject() ==null || nextNumberGeneratorRequest.getObject().isEmpty()) ) {
                existingRecord = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberType(1, nextNumberGeneratorRequest.getSite(), nextNumberGeneratorRequest.getNumberType());
            }
        } else
        if(nextNumberGeneratorRequest.getObject() !=null && !nextNumberGeneratorRequest.getObject().isEmpty() && nextNumberGeneratorRequest.getObjectVersion()!=null && !nextNumberGeneratorRequest.getObjectVersion().isEmpty()) {
            existingRecord = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersion(1, nextNumberGeneratorRequest.getSite(), nextNumberGeneratorRequest.getNumberType(), nextNumberGeneratorRequest.getObject(),nextNumberGeneratorRequest.getObjectVersion());

//            if(existingRecord == null)
//                existingRecord = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersion(1, nextNumberGeneratorRequest.getSite(), nextNumberGeneratorRequest.getNumberType(), "*","*");
        }
        if(existingRecord == null)
        {
            throw new NextNumberGeneratorException(5101);
        }
        return existingRecord;
    }

    @Override
    public String sampleNextNumberOnCreate(NextNumberGeneratorRequest nextNumberGeneratorRequest)throws Exception
    {
        if(nextNumberGeneratorRequest.getItem()==null)
            nextNumberGeneratorRequest.setItem("");

        if(nextNumberGeneratorRequest.getItemGroup()==null)
            nextNumberGeneratorRequest.setItemGroup("");

        int nextNo = nextNumberGeneratorRequest.getCurrentSequence();
        String result = padZeros(nextNo, nextNumberGeneratorRequest.getSequenceLength());
        if(!nextNumberGeneratorRequest.getPrefix().isEmpty() && nextNumberGeneratorRequest.getPrefix().contains("%") && !nextNumberGeneratorRequest.getSuffix().isEmpty() && nextNumberGeneratorRequest.getSuffix().contains("%"))
        {
            String prefix="";
            String suffix="";
            String [] prefixArray =nextNumberGeneratorRequest.getPrefix().split("%");
            for(String iteratePrefix : prefixArray)
            {
                prefix=prefix+iteratePrefix;
            }
            String [] suffixArreay = nextNumberGeneratorRequest.getSuffix().split("%");
            for(String iterateSuffix : suffixArreay)
            {
                suffix = suffix+iterateSuffix;
            }
            String sampleNextNumber = prefix+result+suffix;
            return sampleNextNumber;
        }
        if(!nextNumberGeneratorRequest.getPrefix().isEmpty() && nextNumberGeneratorRequest.getPrefix().contains("%"))
        {
            String prefix="";
            String [] prefixArray =nextNumberGeneratorRequest.getPrefix().split("%");
            for(String iteratePrefix : prefixArray)
            {
                prefix=prefix+iteratePrefix;
            }
            String sampleNextNumber = prefix+result+nextNumberGeneratorRequest.getSuffix();
            return sampleNextNumber;
        }
        if(!nextNumberGeneratorRequest.getSuffix().isEmpty() && !nextNumberGeneratorRequest.getSuffix().contains("%"))
        {
            String suffix="";
            String [] suffixArreay = nextNumberGeneratorRequest.getSuffix().split("%");
            for(String iterateSuffix : suffixArreay)
            {
                suffix = suffix+iterateSuffix;
            }
            String sampleNextNumber = nextNumberGeneratorRequest.getPrefix()+result+suffix;
            return sampleNextNumber;
        }
        return nextNumberGeneratorRequest.getPrefix()+result+nextNumberGeneratorRequest.getSuffix();
    }

    @Override
    public NextNumberMessageModel delete(NextNumberGeneratorRequest nextNumberGeneratorRequest) throws Exception
    {
        NextNumberGenerator nextNumberGenerator = new NextNumberGenerator();
        if(nextNumberGeneratorRequest.getObject().equals("")&&nextNumberGeneratorRequest.getObjectVersion().equals("")&&nextNumberGeneratorRequest.getItem().equals("")&&nextNumberGeneratorRequest.getItemGroup().equals("")&&nextNumberGeneratorRequest.getDefineBy().equals("")&&nextNumberGeneratorRequest.getOrderType().equals("")){
            nextNumberGenerator = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberType(1,nextNumberGeneratorRequest.getSite(),nextNumberGeneratorRequest.getNumberType());
        }else
            nextNumberGenerator = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndDefineBy(1,nextNumberGeneratorRequest.getSite(),nextNumberGeneratorRequest.getNumberType(),nextNumberGeneratorRequest.getObject(), nextNumberGeneratorRequest.getDefineBy());
        if(nextNumberGenerator==null)
        {
            throw new NextNumberGeneratorException(5101);
        }
        nextNumberGenerator.setActive(0);
        nextNumberGenerator.setModifiedDateTime(LocalDateTime.now());
        nextNumberGenerator.setModifiedBy(nextNumberGeneratorRequest.getUserId());

        nextNumberGeneratorRepository.save(nextNumberGenerator);

        AuditLogRequest activityLog = AuditLogRequest.builder()
                .site(nextNumberGeneratorRequest.getSite())
                .action_code("NEXTNUMBER-DELETE")
                .action_detail("NextNumber Deleted "+nextNumberGeneratorRequest.getObject())
                .action_detail_handle("ActionDetailBO:"+nextNumberGeneratorRequest.getSite()+","+"NEXTNUMBER-DELETE"+nextNumberGeneratorRequest.getUserBO()+":"+"com.rits.nextnumbergeneratorservice.service")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(nextNumberGeneratorRequest.getUserBO())
                .txnId("NEXTNUMBER-DELETE"+String.valueOf(LocalDateTime.now())+nextNumberGeneratorRequest.getUserBO())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("NEXT_NUMBER")
                .build();

        webClientBuilder.build()
                .post()
                .uri(auditlogUrl)
                .bodyValue(activityLog)
                .retrieve()
                .bodyToMono(AuditLogRequest.class)
                .block();

        String deleteMessage = getFormattedMessage(6);
        MessageDetails messageDetails = MessageDetails.builder().msg(deleteMessage).msg_type("S").build();
        return NextNumberMessageModel.builder().message_details(messageDetails).build();
    }
    @Override
    public String generatePrefixAndSuffix(GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest) throws Exception
    {
        String item = "";
        String extractedValue = generatePrefixAndSuffixRequest.getValue();
        if(!extractedValue.isEmpty() || extractedValue!=null)
        {
            extractedValue = extractedValue.replaceAll("%ACTIVITY%",generatePrefixAndSuffixRequest.getNextNumberActivity());
            extractedValue = extractedValue.replaceAll("%SITE%",generatePrefixAndSuffixRequest.getSite());
            if(generatePrefixAndSuffixRequest.getItemBO()!=null && !generatePrefixAndSuffixRequest.getItemBO().isEmpty() && !generatePrefixAndSuffixRequest.getItemBO().equals("*")) {
                String[] itemBO = generatePrefixAndSuffixRequest.getItemBO().split(",");
                if(itemBO.length>=2) {
                    if(itemBO[1].equals("*"))
                        item = generatePrefixAndSuffixRequest.getNonStartObject();
                    else
                        item = itemBO[1];
                    extractedValue = extractedValue.replaceAll("%ITEM%", item);
                }
            }else{
                if(generatePrefixAndSuffixRequest.getObject().equals("*"))
                    extractedValue = extractedValue.replaceAll("%ITEM%", generatePrefixAndSuffixRequest.getNonStartObject());
                else
                    extractedValue = extractedValue.replaceAll("%ITEM%", generatePrefixAndSuffixRequest.getObject());

                extractedValue = extractedValue.replaceAll("%ITEM%", generatePrefixAndSuffixRequest.getItemBO());
            }
            if(generatePrefixAndSuffixRequest.getItemGroupBO()!=null && !generatePrefixAndSuffixRequest.getItemGroupBO().isEmpty()) {
                String[] itemGroupBO = generatePrefixAndSuffixRequest.getItemGroupBO().split(",");
                if(itemGroupBO.length==2) {
                    String itemGroup ="";
                    if(itemGroupBO[1].equals("*"))
                        itemGroup = generatePrefixAndSuffixRequest.getNonStartObject();
                    else
                        itemGroup = itemGroupBO[1];
                    extractedValue = extractedValue.replaceAll("%ITEM_GROUP%", itemGroup);
                }
            }else{
                if(generatePrefixAndSuffixRequest.getObject().equals("*"))
                    extractedValue = extractedValue.replaceAll("%ITEM_GROUP%", generatePrefixAndSuffixRequest.getNonStartObject());
                else
                    extractedValue = extractedValue.replaceAll("%ITEM_GROUP%", generatePrefixAndSuffixRequest.getObject());
            }
            extractedValue = extractedValue.replaceAll("%ONEDIGITYEAR%",String.valueOf(Calendar.getInstance().get(Calendar.YEAR) % 10));//CHECK IT
            extractedValue = extractedValue.replaceAll("%WEEKOFYEAR%", String.valueOf(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)));
            extractedValue = extractedValue.replaceAll("%ISO_WEEKOFYEAR%",getISOWeekOfYear(LocalDateTime.now()));
            extractedValue = extractedValue.replaceAll("%ISO_ONEDIGITYEAR%",getISOOneDigitYear(LocalDateTime.now()));
            if(generatePrefixAndSuffixRequest.getShopOrderBO() != null && !generatePrefixAndSuffixRequest.getShopOrderBO().isEmpty())
            {
                String[] shopOrder = generatePrefixAndSuffixRequest.getShopOrderBO().split(",");
                String shopOrderValue = "";
                if(shopOrder.length>1)
                    shopOrderValue = shopOrder[1];
                else
                    shopOrderValue = generatePrefixAndSuffixRequest.getShopOrderBO();

                extractedValue = extractedValue.replaceAll(Pattern.quote("%SHOP_ORDER_BO.(1)%"),shopOrderValue);
            }
            extractedValue = extractedValue.replaceAll("%PRIORITY%",Integer.toString(generatePrefixAndSuffixRequest.getPriority()));


            extractedValue = extractedValue.replaceAll(Pattern.quote("%PCU_BO.(1)%"),String.valueOf(generatePrefixAndSuffixRequest.getCurrentSequence()));

            if(generatePrefixAndSuffixRequest.getUserBO() != null && !generatePrefixAndSuffixRequest.getUserBO().isEmpty())
            {
                String[] userBO = generatePrefixAndSuffixRequest.getUserBO().split(",");
                extractedValue = extractedValue.replaceAll(Pattern.quote("%USER_BO.(0)%"),generatePrefixAndSuffixRequest.getSite());
                if(userBO.length==2) {
                    String user = userBO[1];
                    extractedValue = extractedValue.replaceAll(Pattern.quote("%USER_BO.(1)%"), user);
                }else {
                    extractedValue = extractedValue.replaceAll(Pattern.quote("%USER_BO.(1)%"), generatePrefixAndSuffixRequest.getUserBO());
                }
            }
            if(generatePrefixAndSuffixRequest.getItemBO() != null && !generatePrefixAndSuffixRequest.getItemBO().isEmpty())
            {
                String[] itemBO = generatePrefixAndSuffixRequest.getItemBO().split(",");
                if(itemBO.length>=2) {
                    if(itemBO[1].equals("*"))
                        item = generatePrefixAndSuffixRequest.getNonStartObject();
                    else
                        item = itemBO[1];
                    extractedValue = extractedValue.replaceAll(Pattern.quote("%ITEM_BO.(1)%"),item);
                }
                if(itemBO.length==3) {
                    String itemVersion = "";
                    if(itemBO[2].equals("*"))
                        itemVersion = generatePrefixAndSuffixRequest.getNonStartVersion();
                    else
                        itemVersion = itemBO[2];
                    extractedValue = extractedValue.replaceAll(Pattern.quote("%ITEM_BO.(2)%"), itemVersion);
                }
                else {
                    extractedValue = extractedValue.replaceAll(Pattern.quote("%ITEM_BO.(2)%"), generatePrefixAndSuffixRequest.getItemBO());
                    extractedValue = extractedValue.replaceAll(Pattern.quote("%ITEM_BO.(1)%"), generatePrefixAndSuffixRequest.getItemBO());
                }
            }
            if(generatePrefixAndSuffixRequest.getItemGroupBO() != null && !generatePrefixAndSuffixRequest.getItemGroupBO().isEmpty())
            {
                String[] itemGroupBO = generatePrefixAndSuffixRequest.getItemGroupBO().split(",");
                extractedValue = extractedValue.replaceAll(Pattern.quote("%ITEM_GROUP_BO.(0)%"),generatePrefixAndSuffixRequest.getSite());
                if(itemGroupBO.length==2) {
                    String itemGroup= "";
                    if(itemGroupBO[1].equals("*"))
                        itemGroup = generatePrefixAndSuffixRequest.getNonStartObject();
                    else
                        itemGroup = itemGroupBO[1];
//                    extractedValue = extractedValue.replaceAll(Pattern.quote("%ITEM_GROUP_BO.(1)%"), getItemGroupBO2(generatePrefixAndSuffixRequest.getItemGroupBO()));
                    extractedValue = extractedValue.replaceAll(Pattern.quote("%ITEM_GROUP_BO.(1)%"), itemGroup);
                }
            }
            return extractedValue;
        }
        return extractedValue;
    }

    @Override
    public NextNumberMessageModel generateNextNumber(GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest)throws Exception
    {
        String nextNumber = "";
        NextNumberGeneratorRequest numberGeneratorRequest = NextNumberGeneratorRequest.builder()
                .site(generatePrefixAndSuffixRequest.getSite())
                .numberType(generatePrefixAndSuffixRequest.getNumberType())
                .object(generatePrefixAndSuffixRequest.getObject())
                .objectVersion(generatePrefixAndSuffixRequest.getObjectVersion())
                .build();

        NextNumberGenerator existingRecord = retrieveNextNumber(numberGeneratorRequest);

        if(existingRecord.getSampleNextNumber().equals("not available")){
            String errorMessage = getFormattedMessage(8);
            String msgType = "E";

            MessageDetails messageDetails = MessageDetails.builder().msg_type(msgType).msg(errorMessage).build();
            NextNumberMessageModel nextNumberMessage = NextNumberMessageModel.builder().message_details(messageDetails).build();
            return nextNumberMessage;
        }
        int updateCurrentSequence = 0;
        int nextSequence = 0;
        if(existingRecord == null)
        {
            throw new NextNumberGeneratorException(5101);
        }

        generatePrefixAndSuffixRequest.setValue(existingRecord.getPrefix());
        GeneratePrefixAndSuffixRequest prefixRequest = generatePrefixAndSuffixRequest(generatePrefixAndSuffixRequest);

        generatePrefixAndSuffixRequest.setValue(existingRecord.getSuffix());
        GeneratePrefixAndSuffixRequest suffixRequest = generatePrefixAndSuffixRequest(generatePrefixAndSuffixRequest);

        String prefix;
        String suffix;

        if(existingRecord.getMaxSequence()>0)
        {
            if(existingRecord.getMaxSequence()<existingRecord.getMinSequence())
            {
                throw new NextNumberGeneratorException(5107);
            }
            if(existingRecord.getSequenceLength()!=0 && existingRecord.getSequenceLength() < String.valueOf(existingRecord.getMaxSequence()).length())
            {
                throw new NextNumberGeneratorException(5110,existingRecord.getObject());
            }
            if(existingRecord.getIncrementBy()>0)
            {
                updateCurrentSequence += existingRecord.getCurrentSequence()+existingRecord.getIncrementBy();

                NextNumberMessageModel validateCurrentSeq = validateCurrentSeqAndSeqLength(existingRecord);

                if(validateCurrentSeq.getMessage_details().getMsg_type().equals("E"))
                {
                    return validateCurrentSeq;
                }
                suffixRequest.setCurrentSequence(updateCurrentSequence);
                prefixRequest.setCurrentSequence(updateCurrentSequence);
                prefix = generatePrefixAndSuffix(prefixRequest);
                suffix = generatePrefixAndSuffix(suffixRequest);

                GeneratePrefixAndSuffixRequest prefixReq1 = GeneratePrefixAndSuffixRequest.builder()
                        .value(existingRecord.getPrefix())
                        .build();

                GeneratePrefixAndSuffixRequest prefixReq = existingPrefixAndSuffixRequest(prefixReq1, existingRecord);

                GeneratePrefixAndSuffixRequest suffixReq1 = GeneratePrefixAndSuffixRequest.builder()
                        .value(existingRecord.getSuffix())
                        .build();
                GeneratePrefixAndSuffixRequest suffixReq = existingPrefixAndSuffixRequest(suffixReq1, existingRecord);

                prefixReq.setCurrentSequence(updateCurrentSequence);
                suffixReq.setCurrentSequence(updateCurrentSequence);

                String prefixForNextNumber = generatePrefixAndSuffix(prefixReq);
                String suffixForNextNumber = generatePrefixAndSuffix(suffixReq);

                if(updateCurrentSequence <= existingRecord.getMaxSequence()){
                    String updatedNextNumber = padZeros(updateCurrentSequence, existingRecord.getSequenceLength());
                    nextNumber = prefixForNextNumber+updatedNextNumber+suffixForNextNumber;
                    existingRecord.setCurrentSequence(updateCurrentSequence);
                    existingRecord.setSampleNextNumber(nextNumber);
                }else {
                    existingRecord.setSampleNextNumber("not available");
                }
                nextNumberGeneratorRepository.save(existingRecord);
                GeneratedNextNumber generatedNextNumber = GeneratedNextNumber.builder().nextNum(prefix+padZeros(existingRecord.getCurrentSequence(), existingRecord.getSequenceLength())+suffix).build();
                NextNumberMessageModel nextNumberMessageModel = NextNumberMessageModel.builder().message_details(MessageDetails.builder().msg_type("S").build()).generatedNextNumberResponse(generatedNextNumber).build();
                return nextNumberMessageModel;

            }else{

                updateCurrentSequence = updateCurrentSequence+existingRecord.getCurrentSequence()+1;

                NextNumberMessageModel validateCurrentSeq = validateCurrentSeqAndSeqLength(existingRecord);

                if(validateCurrentSeq.getMessage_details().getMsg_type().equals("E"))
                {
                    return validateCurrentSeq;
                }
                suffixRequest.setCurrentSequence(updateCurrentSequence);
                prefixRequest.setCurrentSequence(updateCurrentSequence);
                prefix = generatePrefixAndSuffix(prefixRequest);
                suffix = generatePrefixAndSuffix(suffixRequest);

                GeneratePrefixAndSuffixRequest prefixReq1 = GeneratePrefixAndSuffixRequest.builder()
                        .value(existingRecord.getPrefix())
                        .build();

                GeneratePrefixAndSuffixRequest prefixReq = existingPrefixAndSuffixRequest(prefixReq1, existingRecord);

                GeneratePrefixAndSuffixRequest suffixReq1 = GeneratePrefixAndSuffixRequest.builder()
                        .value(existingRecord.getSuffix())
                        .build();
                GeneratePrefixAndSuffixRequest suffixReq = existingPrefixAndSuffixRequest(suffixReq1, existingRecord);

                prefixReq.setCurrentSequence(updateCurrentSequence);
                suffixReq.setCurrentSequence(updateCurrentSequence);

                String prefixForNextNumber = generatePrefixAndSuffix(prefixReq);
                String suffixForNextNumber = generatePrefixAndSuffix(suffixReq);

                if(updateCurrentSequence <= existingRecord.getMaxSequence()) {
                    String resUpdateNextNumber = padZeros(updateCurrentSequence, existingRecord.getSequenceLength());
                    nextNumber = prefixForNextNumber + resUpdateNextNumber + suffixForNextNumber;
                    existingRecord.setCurrentSequence(updateCurrentSequence);
                    existingRecord.setSampleNextNumber(nextNumber);
                }else {
                    existingRecord.setSampleNextNumber("not available");
                }
                nextNumberGeneratorRepository.save(existingRecord);
                GeneratedNextNumber generatedNextNumber = GeneratedNextNumber.builder().nextNum(prefix+padZeros(existingRecord.getCurrentSequence(), existingRecord.getSequenceLength())+suffix).build();
                NextNumberMessageModel nextNumberMessageModel = NextNumberMessageModel.builder().message_details(MessageDetails.builder().msg_type("S").build()).generatedNextNumberResponse(generatedNextNumber).build();
                return nextNumberMessageModel;
            }
        }

        updateCurrentSequence = existingRecord.getCurrentSequence() + 1;

        suffixRequest.setCurrentSequence(updateCurrentSequence);
        prefixRequest.setCurrentSequence(updateCurrentSequence);
        prefix = generatePrefixAndSuffix(prefixRequest);
        suffix = generatePrefixAndSuffix(suffixRequest);

        GeneratePrefixAndSuffixRequest prefixReq1 = GeneratePrefixAndSuffixRequest.builder()
                .value(existingRecord.getPrefix())
                .build();

        GeneratePrefixAndSuffixRequest prefixReq = existingPrefixAndSuffixRequest(prefixReq1, existingRecord);

        GeneratePrefixAndSuffixRequest suffixReq1 = GeneratePrefixAndSuffixRequest.builder()
                .value(existingRecord.getSuffix())
                .build();
        GeneratePrefixAndSuffixRequest suffixReq = existingPrefixAndSuffixRequest(suffixReq1, existingRecord);

        prefixReq.setCurrentSequence(updateCurrentSequence);
        suffixReq.setCurrentSequence(updateCurrentSequence);

        String prefixForNextNumber = generatePrefixAndSuffix(prefixReq);
        String suffixForNextNumber = generatePrefixAndSuffix(suffixReq);

        String resUpdateNextNumber = padZeros(updateCurrentSequence, existingRecord.getSequenceLength());
        nextNumber = prefixForNextNumber + resUpdateNextNumber + suffixForNextNumber;
        existingRecord.setCurrentSequence(updateCurrentSequence);
        existingRecord.setSampleNextNumber(nextNumber);

        nextNumberGeneratorRepository.save(existingRecord);
        GeneratedNextNumber generatedNextNumber = GeneratedNextNumber.builder().nextNum(prefix+padZeros(existingRecord.getCurrentSequence(), existingRecord.getSequenceLength())+suffix).build();
        NextNumberMessageModel nextNumberMessageModel = NextNumberMessageModel.builder().message_details(MessageDetails.builder().msg_type("S").build()).generatedNextNumberResponse(generatedNextNumber).build();
        return nextNumberMessageModel;
    }

    public static GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest(GeneratePrefixAndSuffixRequest prefixAndSuffixRequest){
        GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest = GeneratePrefixAndSuffixRequest.builder()
                .site(prefixAndSuffixRequest.getSite())
                .nextNumberActivity(prefixAndSuffixRequest.getNextNumberActivity())
                .object(prefixAndSuffixRequest.getObject())
                .value(prefixAndSuffixRequest.getValue())
                .shopOrderBO(prefixAndSuffixRequest.getShopOrderBO())
                .itemBO(prefixAndSuffixRequest.getItemBO())
                .itemGroupBO(prefixAndSuffixRequest.getItemGroupBO())
                .priority(prefixAndSuffixRequest.getPriority())
                .pcuBO(prefixAndSuffixRequest.getPcuBO())
                .userBO(prefixAndSuffixRequest.getUserBO())
                .nonStartObject(prefixAndSuffixRequest.getNonStartObject())
                .nonStartVersion(prefixAndSuffixRequest.getNonStartVersion())
                .build();
        return generatePrefixAndSuffixRequest;
    }

    public static GeneratePrefixAndSuffixRequest existingPrefixAndSuffixRequest(GeneratePrefixAndSuffixRequest prefixOrSuffixRequest, NextNumberGenerator existingRecord){
        GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest = GeneratePrefixAndSuffixRequest.builder()
                .value(prefixOrSuffixRequest.getValue())
                .nonStartObject(existingRecord.getObject())
                .nonStartVersion(existingRecord.getObjectVersion())
                .nextNumberActivity(existingRecord.getNextNumberActivity())
                .itemBO(existingRecord.getItemBO())
                .itemGroupBO(existingRecord.getItemGroupBO())
                .site(existingRecord.getSite())
                .object(existingRecord.getObject())
                .shopOrderBO("SHOP_ORDER_BO")
                .itemGroupBO(existingRecord.getItemGroupBO())
                .priority(0)
                .pcuBO("PCU_BO")
                .userBO(existingRecord.getCreatedBy())
                .build();

        return generatePrefixAndSuffixRequest;
    }
    public static String padZeros(int value, int totalDigitLength) {
        String stringValue = String.valueOf(value);
        int lengthDifference = totalDigitLength - stringValue.length();

        if (lengthDifference <= 0) {
            return stringValue;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < lengthDifference; i++) {
                stringBuilder.append("0");
            }
            stringBuilder.append(stringValue);
            return stringBuilder.toString();
        }
    }
    public NextNumberMessageModel validateCurrentSeqWithSeqLengthAndMaxSeq(NextNumberGenerator generatePrefixAndSuffixRequest,Integer nextSequence)
    {
        String errorMessage = null;
        String msgType = "S";
        if((generatePrefixAndSuffixRequest.getCurrentSequence()+generatePrefixAndSuffixRequest.getIncrementBy()) > generatePrefixAndSuffixRequest.getMaxSequence())
        {
            errorMessage = getFormattedMessage(7);
            msgType = "E";
        }
        if(String.valueOf(generatePrefixAndSuffixRequest.getCurrentSequence()+generatePrefixAndSuffixRequest.getIncrementBy()).length()>generatePrefixAndSuffixRequest.getSequenceLength() || String.valueOf(nextSequence).length()>generatePrefixAndSuffixRequest.getSequenceLength())
        {
            errorMessage = getFormattedMessage(8);
            msgType = "E";
        }
        MessageDetails messageDetails = MessageDetails.builder().msg_type(msgType).msg(errorMessage).build();
        NextNumberMessageModel nextNumberMessage = NextNumberMessageModel.builder().message_details(messageDetails).build();
        return nextNumberMessage;
    }

    public NextNumberMessageModel validateCurrentSeqAndSeqLength(NextNumberGenerator generatePrefixAndSuffixRequest){
        String errorMessage = null;
        String msgType = "S";
        if(generatePrefixAndSuffixRequest.getCurrentSequence() > generatePrefixAndSuffixRequest.getMaxSequence())
        {
            errorMessage = getFormattedMessage(7);
            msgType = "E";
        }
        if(String.valueOf(generatePrefixAndSuffixRequest.getCurrentSequence()).length()>generatePrefixAndSuffixRequest.getSequenceLength())
        {
            errorMessage = getFormattedMessage(8);
            msgType = "E";
        }
        MessageDetails messageDetails = MessageDetails.builder().msg_type(msgType).msg(errorMessage).build();
        NextNumberMessageModel nextNumberMessage = NextNumberMessageModel.builder().message_details(messageDetails).build();
        return nextNumberMessage;
    }
    @Override
    public GeneratedNextNumber updateCurrentSequence(GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest)throws Exception
    {
        NextNumberGeneratorRequest numberGeneratorRequest = NextNumberGeneratorRequest.builder()
                .site(generatePrefixAndSuffixRequest.getSite())
                .numberType(generatePrefixAndSuffixRequest.getNumberType())
                .object(generatePrefixAndSuffixRequest.getObject())
                .objectVersion(generatePrefixAndSuffixRequest.getObjectVersion())
                .build();
        NextNumberGenerator existingRecord = retrieveNextNumber(numberGeneratorRequest);

        int updateCurrentSequence = 0;
        if(existingRecord == null)
        {
            throw new NextNumberGeneratorException(5101);
        }
        if(generatePrefixAndSuffixRequest.getMaxSequence()>0)
        {
            if(generatePrefixAndSuffixRequest.getMaxSequence()<generatePrefixAndSuffixRequest.getMinSequence())
            {
                throw new NextNumberGeneratorException(5107);
            }
            if(generatePrefixAndSuffixRequest.getSequenceLength()!=0 && generatePrefixAndSuffixRequest.getSequenceLength() < String.valueOf(generatePrefixAndSuffixRequest.getMaxSequence()).length())
            {
                throw new NextNumberGeneratorException(5110,generatePrefixAndSuffixRequest.getObject());
            }
            if(generatePrefixAndSuffixRequest.getIncrementBy()>0)
            {
                updateCurrentSequence = updateCurrentSequence+generatePrefixAndSuffixRequest.getCurrentSequence()+generatePrefixAndSuffixRequest.getIncrementBy();
                if(updateCurrentSequence <= generatePrefixAndSuffixRequest.getMaxSequence()) {
                    existingRecord.setCurrentSequence(updateCurrentSequence);
                    nextNumberGeneratorRepository.save(existingRecord);
                }
                return GeneratedNextNumber.builder().currentSeq(generatePrefixAndSuffixRequest.getCurrentSequence()).build();
            }else{
                updateCurrentSequence = updateCurrentSequence+generatePrefixAndSuffixRequest.getCurrentSequence()+1;
                if(updateCurrentSequence <= generatePrefixAndSuffixRequest.getMaxSequence()) {
                    existingRecord.setCurrentSequence(updateCurrentSequence);
                    nextNumberGeneratorRepository.save(existingRecord);
                }
                return GeneratedNextNumber.builder().currentSeq(generatePrefixAndSuffixRequest.getCurrentSequence()).build();
            }
        }
        updateCurrentSequence = generatePrefixAndSuffixRequest.getCurrentSequence() + 1;
        existingRecord.setCurrentSequence(updateCurrentSequence);
        nextNumberGeneratorRepository.save(existingRecord);
        return GeneratedNextNumber.builder().currentSeq(generatePrefixAndSuffixRequest.getCurrentSequence()).build();
    }

    private static String getItemGroupBO2(String itemGroupBO)
    {
        String [] itemGroup = itemGroupBO.split(",");
        return itemGroup[0];
    }

    private static String getItemGroupBO1(String itemGroupBO)
    {
        String [] itemGroup = itemGroupBO.split(",");
        return itemGroup[1];
    }
    private static String getItemBO2(String itemBO)
    {
        String [] item = itemBO.split(",");
        return item[1];
    }

    private static String getItemBO1(String itemBO)
    {
        String [] item = itemBO.split(",");
        return item[2];
    }

    private static String getUserBO2(String userBO)
    {
        String [] user = userBO.split(",");
        String userValue = user[0];
        return userValue;
    }

    private static String getUserBO1(String userBO)
    {
        String [] user = userBO.split(",");
        String userValue = user[1];
        return userValue;
    }

    private static String getPcuBO(String pcuBO)
    {
        String [] pcu = pcuBO.split(",");
        String pcuValue = pcu[1];
        return pcuValue;
    }

    private static String getShopOrderBO(String shopOrderBO){
        String[] shopOrder = shopOrderBO.split(",");
        String shopOrderValue = shopOrder[1];
        return shopOrderValue;
    }

    private static String getISOWeekOfYear(LocalDateTime dateTime) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekOfYear = dateTime.get(weekFields.weekOfYear());
        return String.valueOf(weekOfYear);
    }

    private static String getISOOneDigitYear(LocalDateTime dateTime) {
        int isoWeekOfYear = Integer.parseInt(getISOWeekOfYear(dateTime));
        return String.valueOf(isoWeekOfYear % 10);
    }

    @Override
    public List<NextnumberList> getNewInventory(String site, String object, String objectVersion, double size, String userBO, String nextNumberActivity, String numberType)throws Exception {
        System.out.println("getNewInventory called with parameters: " + site + ", " + object + ", " + objectVersion);
        List<NextnumberList> createdInventoryList = new ArrayList<>();
        double lotSize = 0.0;
        int lotSizeCapacity = 0;
        List<Double> lotList = new ArrayList<>();
        NextNumberGenerator retrieveMaterialBasedRec = null;
        Item existingItem = null;

        if (!numberType.equals("PCU release") && !numberType.equals("PCU serialize") && !numberType.equals("Batch Number") && !numberType.equals("Floor Stock Receipt") && !numberType.equals("RMA PCU Number")) {
            if (!numberType.isEmpty() && (object == null || object.isEmpty())) {
                retrieveMaterialBasedRec = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberType(1, site, numberType);
                if(retrieveMaterialBasedRec == null)
                    return createdInventoryList;
                object = "";
                objectVersion = "";
                userBO="";

                existingItem = new Item();
                existingItem.setItemGroup("");
                lotList.add(0.0);

            }
        } else {
            retrieveMaterialBasedRec = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersion(1, site, numberType, object, objectVersion);

            if (retrieveMaterialBasedRec == null || retrieveMaterialBasedRec.getObject() == null || retrieveMaterialBasedRec.getObject().isEmpty()) {
                retrieveMaterialBasedRec = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersion(1, site, numberType, "*", "*");
            }

            if (retrieveMaterialBasedRec == null || retrieveMaterialBasedRec.getObject() == null || retrieveMaterialBasedRec.getObject().isEmpty()) {
                return createdInventoryList;
            }


            ItemRequest itemRequest = ItemRequest.builder().site(site).item(object).revision(objectVersion).build();
            existingItem = webClientBuilder
                    .build()
                    .post()
                    .uri(itemUrl)
                    .body(BodyInserters.fromValue(itemRequest))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Item>() {
                    })
                    .block();

            if (existingItem != null && existingItem.getItem() != null && !existingItem.getItem().isEmpty()) {
                lotSize = existingItem.getLotSize();
            }


            int numberOfInventoryIDCreate = (int) Math.ceil(size / lotSize);

            for (var i = 0; i < numberOfInventoryIDCreate; i++) {
                double qtyToAdd = Math.min(size, lotSize);
                lotList.add(qtyToAdd);
                size -= qtyToAdd;
            }
        }

        GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest = GeneratePrefixAndSuffixRequest.builder()
                .site(site)
                .itemGroupBO("ItemGroupBO:" + site + "," + existingItem.getItemGroup())
                .object(retrieveMaterialBasedRec.getObject())
                .itemBO("ItemBO:" +site + "," + retrieveMaterialBasedRec.getObject() + "," +  retrieveMaterialBasedRec.getObjectVersion())
                .priority(500)
                .numberBase(retrieveMaterialBasedRec.getNumberBase())
                .sequenceLength(retrieveMaterialBasedRec.getSequenceLength())
                .minSequence(retrieveMaterialBasedRec.getMinSequence())
                .maxSequence(retrieveMaterialBasedRec.getMaxSequence())
                .incrementBy(retrieveMaterialBasedRec.getIncrementBy())
                .currentSequence(retrieveMaterialBasedRec.getCurrentSequence())
                .numberType(numberType)
                .orderType(retrieveMaterialBasedRec.getOrderType())
                .userBO(userBO)
                .defineBy(retrieveMaterialBasedRec.getDefineBy())
                .objectVersion(retrieveMaterialBasedRec.getObjectVersion())
                .suffix(retrieveMaterialBasedRec.getSuffix())
                .prefix(retrieveMaterialBasedRec.getPrefix())
                .nonStartObject(object)
                .nonStartVersion(objectVersion)
                .nextNumberActivity(nextNumberActivity)
                .build();

        GeneratePrefixAndSuffixRequest generatePrefixRequest = GeneratePrefixAndSuffixRequest.builder()
                .site(generatePrefixAndSuffixRequest.getSite())
                .nextNumberActivity(generatePrefixAndSuffixRequest.getNextNumberActivity())
                .object(generatePrefixAndSuffixRequest.getObject())
                .value(generatePrefixAndSuffixRequest.getPrefix())
                .shopOrderBO(generatePrefixAndSuffixRequest.getShopOrderBO())
                .itemBO(generatePrefixAndSuffixRequest.getItemBO())
                .itemGroupBO(generatePrefixAndSuffixRequest.getItemGroupBO())
                .priority(generatePrefixAndSuffixRequest.getPriority())
                .pcuBO(generatePrefixAndSuffixRequest.getPcuBO())
                .userBO(generatePrefixAndSuffixRequest.getUserBO())
                .nonStartObject(generatePrefixAndSuffixRequest.getNonStartObject())
                .nonStartVersion(generatePrefixAndSuffixRequest.getNonStartVersion())
                .build();

        GeneratePrefixAndSuffixRequest generateSuffixRequest = GeneratePrefixAndSuffixRequest.builder()
                .site(generatePrefixAndSuffixRequest.getSite())
                .nextNumberActivity(generatePrefixAndSuffixRequest.getNextNumberActivity())
                .object(generatePrefixAndSuffixRequest.getObject())
                .value(generatePrefixAndSuffixRequest.getSuffix())
                .shopOrderBO(generatePrefixAndSuffixRequest.getShopOrderBO())
                .itemBO(generatePrefixAndSuffixRequest.getItemBO())
                .itemGroupBO(generatePrefixAndSuffixRequest.getItemGroupBO())
                .priority(generatePrefixAndSuffixRequest.getPriority())
                .pcuBO(generatePrefixAndSuffixRequest.getPcuBO())
                .userBO(generatePrefixAndSuffixRequest.getUserBO())
                .nonStartObject(generatePrefixAndSuffixRequest.getNonStartObject())
                .nonStartVersion(generatePrefixAndSuffixRequest.getNonStartVersion())
                .build();

        if(numberType.equals("MFR") || numberType.equals("BPR") || numberType.equals("BMR"))
            lotSizeCapacity = (int) size;
        else
            lotSizeCapacity = lotList.size();

        List<String> invList = new ArrayList<>();
        List<NextnumberList> getListOfInventories = new ArrayList<>();

        while(true) {
            getListOfInventories = listOfInventories(lotSizeCapacity, generatePrefixAndSuffixRequest, retrieveMaterialBasedRec, generatePrefixRequest, generateSuffixRequest);

            if(getListOfInventories.size()==0)
                return createdInventoryList;
            for (NextnumberList inv : getListOfInventories)
                invList.add(inv.getInventory());

            List<String> retrivedList = new ArrayList<>();

            if(numberType.equals("Floor Stock Receipt")) {
                InventoryListRequest inventoryListRequest = InventoryListRequest.builder().inventoryList(invList).site(site).build();
                retrivedList = webClientBuilder.build()
                        .post()
                        .uri(inventoryUrl)
                        .bodyValue(inventoryListRequest)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                        })
                        .block();
            }

            if(numberType.equals("MFR") || numberType.equals("BPR")) {

                MfrRecipesRequest mfrRecipesRequest = MfrRecipesRequest.builder().mfrNo(invList.get(0)).site(site).build();
                retrivedList = webClientBuilder.build()
                        .post()
                        .uri(mfrrecipesUrl)
                        .bodyValue(mfrRecipesRequest)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                        })
                        .block();
            }

            if(numberType.equals("BMR")) {

                BmrRequest mfrRecipesRequest = BmrRequest.builder().bmrNo(invList.get(0)).site(site).build();
                retrivedList = webClientBuilder.build()
                        .post()
                        .uri(bmrRecipesUrl)
                        .bodyValue(mfrRecipesRequest)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                        })
                        .block();
            }

            if(numberType.equals("PCU release")) {
                List<Pcu> pcus = new ArrayList<>();
                List<String> pcuList = new ArrayList<>();
                for(int i =0; i< invList.size(); i++){
                    pcuList.add("PcuBO:"+ site +","+invList.get(i));
                }
                for (String pcuString : pcuList) {
                    Pcu pcu = Pcu.builder().pcuBo(pcuString).build();
                    pcus.add(pcu);
                }
//                pcus.add((Pcu) pcuList);
                PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder().pcuBos(pcus).site(site).build();
                retrivedList = webClientBuilder.build()
                        .post()
                        .uri(pcuHeaderUrl)
                        .bodyValue(pcuHeaderRequest)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                        })
                        .block();
            }
            if (retrivedList.size() < lotSizeCapacity) {
                invList = new ArrayList<>();
                for(String val : retrivedList)
                    invList.add(val);
                lotSizeCapacity -= retrivedList.size();
                retrieveMaterialBasedRec.setCurrentSequence(getListOfInventories.get(getListOfInventories.size()-1).getCurrentSequence());
                int currentSeq = currentSequenceValue(generatePrefixAndSuffixRequest, retrieveMaterialBasedRec);
                if(currentSeq<0)
                    return createdInventoryList;
                retrieveMaterialBasedRec.setCurrentSequence(currentSeq);
            } else {
                break;
            }
        }

        for (int i = 0; i < invList.size(); i++){
            NextnumberList newInventory = new NextnumberList();
            if(numberType.equals("MFR") || numberType.equals("BMR") || numberType.equals("BPR")){
                newInventory = NextnumberList.builder().inventory(invList.get(i)).currentSequence(getListOfInventories.get(getListOfInventories.size()-1).getCurrentSequence()).build();
            }else
                newInventory = NextnumberList.builder().inventory(invList.get(i)).currentSequence(getListOfInventories.get(getListOfInventories.size()-1).getCurrentSequence()).qty(lotList.get(i)).build();
            createdInventoryList.add(newInventory);
        }

        return createdInventoryList;
    }

    public int currentSequenceValue(GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest, NextNumberGenerator retrieveMaterialBasedRec){
        int currentSequenceVal = 0;
        if(generatePrefixAndSuffixRequest.getMaxSequence()>0)
        {
            if(generatePrefixAndSuffixRequest.getMaxSequence()<generatePrefixAndSuffixRequest.getMinSequence())
            {
                throw new NextNumberGeneratorException(5107);
            }
            if(generatePrefixAndSuffixRequest.getSequenceLength()!=0 && generatePrefixAndSuffixRequest.getSequenceLength() < String.valueOf(generatePrefixAndSuffixRequest.getMaxSequence()).length())
            {
                throw new NextNumberGeneratorException(5110,generatePrefixAndSuffixRequest.getObject());
            }
            if(generatePrefixAndSuffixRequest.getIncrementBy()>0)
            {
                NextNumberMessageModel validateCurrentSeq = validateCurrentSeqWithSeqLengthAndMaxSeq(retrieveMaterialBasedRec,retrieveMaterialBasedRec.getCurrentSequence());
                if(validateCurrentSeq.getMessage_details().getMsg_type().equals("E"))
                {
//                    throw new NextNumberGeneratorException(5101);
                    return -1;
                }
                currentSequenceVal = retrieveMaterialBasedRec.getCurrentSequence() + retrieveMaterialBasedRec.getIncrementBy();

            }else{
                retrieveMaterialBasedRec.setIncrementBy(1);
                NextNumberMessageModel validateCurrentSeq = validateCurrentSeqWithSeqLengthAndMaxSeq(retrieveMaterialBasedRec,retrieveMaterialBasedRec.getCurrentSequence());
                if(validateCurrentSeq.getMessage_details().getMsg_type().equals("E"))
                {
//                    throw new NextNumberGeneratorException(5101);
                    return -1;
                }
                currentSequenceVal = retrieveMaterialBasedRec.getCurrentSequence() + 1;
            }
        }else {
            currentSequenceVal = retrieveMaterialBasedRec.getCurrentSequence() + 1;
        }
        return currentSequenceVal;
    }

    protected List<NextnumberList> listOfInventories(double lotSizeCapacity, GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest, NextNumberGenerator retrieveMaterialBasedRec,
                                                     GeneratePrefixAndSuffixRequest generatePrefixRequest, GeneratePrefixAndSuffixRequest generateSuffixRequest) throws Exception{
        int updateCurrentSequence = 0;
        String nextNumber = "";
        List<NextnumberList> createdInventoryList= new ArrayList<>();

        for(int i =0 ; i < lotSizeCapacity; i++)
        {
            String prefix;
            String suffix;
            if(generatePrefixAndSuffixRequest.getMaxSequence()>0)
            {
                if(generatePrefixAndSuffixRequest.getMaxSequence()<generatePrefixAndSuffixRequest.getMinSequence())
                {
                    throw new NextNumberGeneratorException(5107);
                }
                if(generatePrefixAndSuffixRequest.getSequenceLength()!=0 && generatePrefixAndSuffixRequest.getSequenceLength() < String.valueOf(generatePrefixAndSuffixRequest.getMaxSequence()).length())
                {
                    throw new NextNumberGeneratorException(5110,generatePrefixAndSuffixRequest.getObject());
                }
                if(generatePrefixAndSuffixRequest.getIncrementBy()>0)
                {
                    if(i > 0){
                        NextNumberMessageModel validateCurrentSeq = validateCurrentSeqWithSeqLengthAndMaxSeq(retrieveMaterialBasedRec,retrieveMaterialBasedRec.getCurrentSequence());
                        if(validateCurrentSeq.getMessage_details().getMsg_type().equals("E"))
                        {
//                            throw new NextNumberGeneratorException(5101);
                            return createdInventoryList;
                        }
                        retrieveMaterialBasedRec.setCurrentSequence(retrieveMaterialBasedRec.getCurrentSequence() + retrieveMaterialBasedRec.getIncrementBy());
                    }

                    generateSuffixRequest.setCurrentSequence(retrieveMaterialBasedRec.getCurrentSequence());
                    generatePrefixRequest.setCurrentSequence(retrieveMaterialBasedRec.getCurrentSequence());
                    prefix = generatePrefixAndSuffix(generatePrefixRequest);
                    suffix = generatePrefixAndSuffix(generateSuffixRequest);

                    if(retrieveMaterialBasedRec.getCurrentSequence() <= retrieveMaterialBasedRec.getMaxSequence()){
                        String updatedNextNumber = padZeros(retrieveMaterialBasedRec.getCurrentSequence(), retrieveMaterialBasedRec.getSequenceLength());
                        nextNumber = prefix+updatedNextNumber+suffix;
                    }
                    updateCurrentSequence = retrieveMaterialBasedRec.getCurrentSequence();

                }else{

                    if(i > 0){
                        retrieveMaterialBasedRec.setIncrementBy(1);
                        NextNumberMessageModel validateCurrentSeq = validateCurrentSeqWithSeqLengthAndMaxSeq(retrieveMaterialBasedRec,retrieveMaterialBasedRec.getCurrentSequence());
                        if(validateCurrentSeq.getMessage_details().getMsg_type().equals("E"))
                        {
//                            throw new NextNumberGeneratorException(5101);
                            return createdInventoryList;
                        }
                        retrieveMaterialBasedRec.setCurrentSequence(retrieveMaterialBasedRec.getCurrentSequence() + 1);
                    }
                    generateSuffixRequest.setCurrentSequence(retrieveMaterialBasedRec.getCurrentSequence());
                    generatePrefixRequest.setCurrentSequence(retrieveMaterialBasedRec.getCurrentSequence());
                    prefix = generatePrefixAndSuffix(generatePrefixRequest);
                    suffix = generatePrefixAndSuffix(generateSuffixRequest);

                    if(retrieveMaterialBasedRec.getCurrentSequence() <= retrieveMaterialBasedRec.getMaxSequence()) {
                        String resUpdateNextNumber = padZeros(retrieveMaterialBasedRec.getCurrentSequence(), retrieveMaterialBasedRec.getSequenceLength());
                        nextNumber = prefix + resUpdateNextNumber + suffix;
                    }
                    updateCurrentSequence = retrieveMaterialBasedRec.getCurrentSequence();
                }
            }else {

                generateSuffixRequest.setCurrentSequence(retrieveMaterialBasedRec.getCurrentSequence());
                generatePrefixRequest.setCurrentSequence(retrieveMaterialBasedRec.getCurrentSequence());
                prefix = generatePrefixAndSuffix(generatePrefixRequest);
                suffix = generatePrefixAndSuffix(generateSuffixRequest);

                String resUpdateNextNumber = padZeros(retrieveMaterialBasedRec.getCurrentSequence(), retrieveMaterialBasedRec.getSequenceLength());
                nextNumber = prefix + resUpdateNextNumber + suffix;

                updateCurrentSequence = retrieveMaterialBasedRec.getCurrentSequence();
                retrieveMaterialBasedRec.setCurrentSequence(retrieveMaterialBasedRec.getCurrentSequence() + 1);
            }
            NextnumberList newNumber = NextnumberList.builder().inventory(nextNumber).currentSequence(updateCurrentSequence).build();
            createdInventoryList.add(newNumber);
        }
        return createdInventoryList;
    }

    @Override
    public NextNumberMessageModel getAndUpdateCurrentSequence(GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest)throws Exception
    {
        NextNumberGenerator existingRecord = null;
        if (!generatePrefixAndSuffixRequest.getNumberType().equals("PCU release") && !generatePrefixAndSuffixRequest.getNumberType().equals("PCU serialize") && !generatePrefixAndSuffixRequest.getNumberType().equals("Batch Number") && !generatePrefixAndSuffixRequest.getNumberType().equals("Floor Stock Receipt") && !generatePrefixAndSuffixRequest.getNumberType().equals("RMA PCU Number")){
            if(!generatePrefixAndSuffixRequest.getNumberType().isEmpty() && (generatePrefixAndSuffixRequest.getObject() ==null || generatePrefixAndSuffixRequest.getObject().isEmpty()) ) {
                existingRecord = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberType(1, generatePrefixAndSuffixRequest.getSite(), generatePrefixAndSuffixRequest.getNumberType());
                existingRecord.setObjectVersion("");
                existingRecord.setObject("");
                existingRecord.setNextNumberActivity("");
            }
        } else {
            existingRecord = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersion(1, generatePrefixAndSuffixRequest.getSite(), generatePrefixAndSuffixRequest.getNumberType(), generatePrefixAndSuffixRequest.getObject(), generatePrefixAndSuffixRequest.getObjectVersion());

            if (existingRecord == null || existingRecord.getObject() == null || existingRecord.getObject().isEmpty()) {
                existingRecord = nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersion(1, generatePrefixAndSuffixRequest.getSite(), generatePrefixAndSuffixRequest.getNumberType(), "*", "*");
            }
        }

        if(existingRecord == null || existingRecord.getObject() == null || existingRecord.getObject().isEmpty())
        {
            throw new NextNumberGeneratorException(5101);
        }

        int updateCurrentSequence = 0;

        NextNumberGeneratorRequest nextNumberGeneratorRequest = NextNumberGeneratorRequest.builder()
                .site(generatePrefixAndSuffixRequest.getSite())
                .prefix(existingRecord.getPrefix())
                .suffix(existingRecord.getSuffix())
                .nextNumberActivity(existingRecord.getNextNumberActivity())
                .item(existingRecord.getObject())
                .itemGroup("")
                .object(existingRecord.getObject())
                .objectVersion(existingRecord.getObjectVersion())
                .userBO(generatePrefixAndSuffixRequest.getUserBO())
                .currentSequence(generatePrefixAndSuffixRequest.getCurrentSequence())
                .build();

        GeneratePrefixAndSuffixRequest generatePrefixRequest = buildPrefixAndSuffixRequest(nextNumberGeneratorRequest,"p");
        generatePrefixRequest.setPrefix(existingRecord.getPrefix());

        GeneratePrefixAndSuffixRequest generateSuffixRequest = buildPrefixAndSuffixRequest(nextNumberGeneratorRequest,"s");
        generateSuffixRequest.setSuffix(existingRecord.getSuffix());

        if(existingRecord.getMaxSequence() > 0)
        {
            if(existingRecord.getMaxSequence() < existingRecord.getMinSequence())
            {
                throw new NextNumberGeneratorException(5107);
            }
            if(generatePrefixAndSuffixRequest.getSequenceLength()!=0 && generatePrefixAndSuffixRequest.getSequenceLength() < String.valueOf(generatePrefixAndSuffixRequest.getMaxSequence()).length())
            {
                throw new NextNumberGeneratorException(5110,generatePrefixAndSuffixRequest.getObject());
            }
            if(existingRecord.getIncrementBy() > 0)
            {
                if((generatePrefixAndSuffixRequest.getCurrentSequence() + existingRecord.getIncrementBy()) > existingRecord.getMaxSequence())
                {
                    existingRecord.setCurrentSequence(generatePrefixAndSuffixRequest.getCurrentSequence());
                    int nextNo = generatePrefixAndSuffixRequest.getCurrentSequence();
                    String result = padZeros(nextNo, existingRecord.getSequenceLength());
                    existingRecord.setSampleNextNumber("not available");
                }else {
                    existingRecord.setCurrentSequence(generatePrefixAndSuffixRequest.getCurrentSequence() + existingRecord.getIncrementBy());
                    int nextNo = generatePrefixAndSuffixRequest.getCurrentSequence() + existingRecord.getIncrementBy();
                    String result = padZeros(nextNo, existingRecord.getSequenceLength());
                    existingRecord.setSampleNextNumber(generatePrefixAndSuffix(generatePrefixRequest) + result + generatePrefixAndSuffix(generateSuffixRequest));
                }

                nextNumberGeneratorRepository.save(existingRecord);
                String createdMessage = getFormattedMessage(5);
                MessageDetails messageDetails = MessageDetails.builder().msg(createdMessage).msg_type("S").build();
                return NextNumberMessageModel.builder().message_details(messageDetails).build();
            }else{
                updateCurrentSequence = updateCurrentSequence+existingRecord.getCurrentSequence()+1;
                if((generatePrefixAndSuffixRequest.getCurrentSequence()+1)>existingRecord.getMaxSequence())
                {
//                    throw new NextNumberGeneratorException(5103);
                    existingRecord.setCurrentSequence(generatePrefixAndSuffixRequest.getCurrentSequence());
                    int nextNo = generatePrefixAndSuffixRequest.getCurrentSequence();
                    String result = padZeros(nextNo, existingRecord.getSequenceLength());
                    existingRecord.setSampleNextNumber("not available");
                } else {
                    existingRecord.setCurrentSequence(generatePrefixAndSuffixRequest.getCurrentSequence() + 1);
                    int nextNo = generatePrefixAndSuffixRequest.getCurrentSequence() + 1;
                    String result = padZeros(nextNo, existingRecord.getSequenceLength());
                    existingRecord.setSampleNextNumber(generatePrefixAndSuffix(generatePrefixRequest) + result + generatePrefixAndSuffix(generateSuffixRequest));

                }
                nextNumberGeneratorRepository.save(existingRecord);
                String createdMessage = getFormattedMessage(5);
                MessageDetails messageDetails = MessageDetails.builder().msg(createdMessage).msg_type("S").build();
                return NextNumberMessageModel.builder().message_details(messageDetails).build();
            }
        }
//        updateCurrentSequence = existingRecord.getCurrentSequence() + 1;
        existingRecord.setCurrentSequence(generatePrefixAndSuffixRequest.getCurrentSequence() + 1);
        int nextNo = generatePrefixAndSuffixRequest.getCurrentSequence() + 1;
        String result = padZeros(nextNo, existingRecord.getSequenceLength());
        existingRecord.setSampleNextNumber(generatePrefixAndSuffix(generatePrefixRequest)+result+generatePrefixAndSuffix(generateSuffixRequest));

        nextNumberGeneratorRepository.save(existingRecord);

        String createdMessage = getFormattedMessage(5);
        MessageDetails messageDetails = MessageDetails.builder().msg(createdMessage).msg_type("S").build();
        return NextNumberMessageModel.builder().message_details(messageDetails).build();

    }

    public List<NextNumberResponse> createNextNumbers(String numberType, String site, String object, String objectVersion, String shopOrder, String pcu, String ncBo, String userBo, double batchQty ) {
        List<NextNumberResponse> nextNumbers = new ArrayList<>();

        boolean isMaterialBased = MATERIAL_BASED_TYPES.contains(numberType);
        boolean isNonMaterialBased = NON_MATERIAL_BASED_TYPES.contains(numberType);
        NextNumberGenerator generator=null;
        List<NextNumberResponse> nextNOs= new ArrayList<>();
        double lotSize = 1;

        if(isMaterialBased){
            generator = retrieveNextNumberGenerator(numberType, site, object, objectVersion);
            if (generator == null) {
                generator = retrieveNextNumberGenerator(numberType, site, "*", "*");
                if (generator == null) {
                    throw new IllegalArgumentException("No suitable NextNumberGenerator record found for type: " + numberType);
                }
            }
            lotSize = getItemLotSize(site, object, objectVersion);
            if(lotSize==0)
                lotSize = 1;
        }
        if(isNonMaterialBased)
            lotSize = (int) batchQty;

        int numberOfNextNumbers = calculateNextNumbers(batchQty, lotSize);

        nextNumbers = getNewNextNumber(numberOfNextNumbers, generator, site, object, objectVersion, shopOrder, pcu, ncBo, userBo, lotSize, batchQty, isMaterialBased);

        List<Double> lotList = new ArrayList<>();
        for (var i = 0; i < numberOfNextNumbers; i++) {
            double qtyToAdd = Math.min(batchQty, lotSize);
            nextNumbers.get(i).setQty(qtyToAdd);
            lotList.add(qtyToAdd);
            batchQty -= qtyToAdd;
        }


        nextNumberGeneratorRepository.save(generator);
        return nextNumbers;
    }

    /***
     *
     * @param numberType
     * @param site
     * @param object
     * @param objectVersion
     * @param shopOrder
     * @param pcu
     * @param ncBo
     * @param userBo
     * @param batchQty
     * @return
     */
    // Added by Shanmathi to performance managed next number number Maintenance. Floor stcok check and MFr, BMR to be added.

    public List<NextNumberResponse> createNextNumberList(String numberType, String site, String object, String objectVersion, String shopOrder, String pcu, String ncBo, String userBo, double batchQty) {
        List<NextNumberResponse> nextNumbers = new ArrayList<>();//

        boolean isMaterialBased = MATERIAL_BASED_TYPES.contains(numberType);
        boolean isNonMaterialBased = NON_MATERIAL_BASED_TYPES.contains(numberType);
        NextNumberGenerator generator = retrieveNextNumberGenerator(numberType, site, object, objectVersion);

        if (generator == null) {
            generator = retrieveNextNumberGenerator(numberType, site, "*", "*");
            if (generator == null) {
                throw new IllegalArgumentException("No suitable NextNumberGenerator record found for type: " + numberType);
            }
        }

        double lotSize = 1;
        List<NextNumberResponse> nextNOs= new ArrayList<>();

        if (isMaterialBased) {
            if (object == null || object.isEmpty()) {
                throw new IllegalArgumentException("Object cannot be empty for material-based number types");
            }
            lotSize = getItemLotSize(site, object, objectVersion);
        }
        if(lotSize==0){
        lotSize=1;
        }

        if(isNonMaterialBased)
            lotSize = (int) batchQty;

        int numberOfNextNumbers = calculateNextNumbers(batchQty, lotSize);

        List<Double> lotList = new ArrayList<>();
        for (var i = 0; i < numberOfNextNumbers; i++) {
            double qtyToAdd = Math.min(batchQty, lotSize);
            lotList.add(qtyToAdd);
            batchQty -= qtyToAdd;
        }

        List<String> nextNumberList = new ArrayList<>();
        while(true) {
            nextNumbers = getNewNextNumber(numberOfNextNumbers, generator, site, object, objectVersion, shopOrder, pcu, ncBo, userBo, lotSize, batchQty, isMaterialBased);

            if(nextNumbers.size()==0)
                return nextNumbers;

            for (NextNumberResponse nextNo : nextNumbers)
                nextNumberList.add(nextNo.getNextNumber());

            List<String> retrivedList = new ArrayList<>();

            if(numberType.equals("FLOOR_STOCK")) {
                NextNumberRequestHolder inventoryListRequest = NextNumberRequestHolder.builder().inventoryList(nextNumberList).site(site).build();
                retrivedList = webClientBuilder.build()
                        .post()
                        .uri(inventoryUrl)
                        .bodyValue(inventoryListRequest)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                        })
                        .block();
            }

            if(numberType.equals("MFR") || numberType.equals("BPR")) {

                NextNumberRequestHolder mfrRecipesRequest = NextNumberRequestHolder.builder().mfrNo(nextNumberList.get(0)).site(site).build();
                retrivedList = webClientBuilder.build()
                        .post()
                        .uri(mfrrecipesUrl)
                        .bodyValue(mfrRecipesRequest)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                        })
                        .block();
            }

            if(numberType.equals("BMR")) {

                NextNumberRequestHolder mfrRecipesRequest = NextNumberRequestHolder.builder().bmrNo(nextNumberList.get(0)).site(site).build();
                retrivedList = webClientBuilder.build()
                        .post()
                        .uri(bmrRecipesUrl)
                        .bodyValue(mfrRecipesRequest)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                        })
                        .block();
            }

            if(numberType.equals("PCU release")) {
                List<Pcu> pcus = new ArrayList<>();
                List<String> pcuList = new ArrayList<>();
                for(int i =0; i< nextNumberList.size(); i++){
                    pcuList.add("PcuBO:"+ site +","+nextNumberList.get(i));
                }
                for (String pcuString : pcuList) {
                    Pcu pcu1 = Pcu.builder().pcuBo(pcuString).build();
                    pcus.add(pcu1);
                }

                PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder().pcuBos(pcus).site(site).build();
                retrivedList = webClientBuilder.build()
                        .post()
                        .uri(pcuHeaderUrl)
                        .bodyValue(pcuHeaderRequest)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                        })
                        .block();
            }

            if (retrivedList.size() < numberOfNextNumbers) {

                nextNumberList = new ArrayList<>();

                for(String val : retrivedList)
                    nextNumberList.add(val);

                numberOfNextNumbers -= retrivedList.size();

                if(generator.getCurrentSequence() < 0)
                    return nextNOs;

            } else {
                break;
            }

            if(numberType.equals("Batch Number")) {
            break;
            }
        }

        for (int i = 0; i < nextNumberList.size(); i++){
            NextNumberResponse nextNumberResponse = new NextNumberResponse();
            if(numberType.equals("MFR") || numberType.equals("BMR") || numberType.equals("BPR")){
                nextNumberResponse = NextNumberResponse.builder().nextNumber(nextNumberList.get(i)).currentSequence(nextNumbers.get(nextNumbers.size()-1).getCurrentSequence()).build();
            }else
                nextNumberResponse = NextNumberResponse.builder().nextNumber(nextNumberList.get(i)).currentSequence(nextNumbers.get(nextNumbers.size()-1).getCurrentSequence()).qty(lotList.get(i)).build();
            nextNOs.add(nextNumberResponse);
        }

        nextNumberGeneratorRepository.save(generator);
        return nextNOs;
    }

    private List<NextNumberResponse> getNewNextNumber(int numberOfNextNumbers, NextNumberGenerator generator, String site, String object, String objectVersion, String shopOrder, String pcu,
                                                      String ncBo, String userBo, double lotSize, double batchQty, boolean isMaterialBased){

        List<NextNumberResponse> nextNumbers = new ArrayList<>();
        String nextNumber = "";
        for (int i = 0; i < numberOfNextNumbers; i++) {

            nextNumber = generateNextNumber(generator, site, object, objectVersion, shopOrder, pcu, ncBo, userBo);
            double responseBatchQty = isMaterialBased ? batchQty : lotSize;
            nextNumbers.add(new NextNumberResponse(nextNumber, generator.getCurrentSequence()));
            updateGeneratorSequence(generator);
        }
        generator.setSampleNextNumber(nextNumber);
        nextNumberGeneratorRepository.save(generator);
        return nextNumbers;
    }
    private NextNumberGenerator retrieveNextNumberGenerator(String numberType, String site, String object, String objectVersion) {
        if(object == null || object.isEmpty())
            return nextNumberGeneratorRepository.findByActiveAndSiteAndNumberType(1, site, numberType);

        return nextNumberGeneratorRepository.findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersion(1, site, numberType, object, objectVersion);
    }

    private double getItemLotSize(String site, String object, String objectVersion) throws WebClientResponseException {
        ItemRequest itemRequest = ItemRequest.builder().site(site).item(object).revision(objectVersion).build();
        Item itemDetail = webClientBuilder.build()
                .post()
                .uri(itemUrl)
                .bodyValue(itemRequest)
                .retrieve()
                .bodyToMono(Item.class)
                .block();
        return itemDetail != null ? itemDetail.getLotSize() : 1;
    }

    private int calculateNextNumbers(double batchQty, double lotSize) {
        return (int) Math.ceil(batchQty / lotSize);
    }

    private String generateNextNumber(NextNumberGenerator generator, String site, String object, String objectVersion, String shopOrder, String pcu, String ncBo, String userBo) {
        String prefix = generator.getPrefix();
        String suffix = generator.getSuffix();
        String nextSequence = getNextSequence(generator);

        Map<String, String> replacementMap = getReplacementMap(generator, site, object, objectVersion, shopOrder, pcu, userBo);
        prefix = replaceParameters(prefix, replacementMap);
        suffix = replaceParameters(suffix, replacementMap);

        return prefix + nextSequence + suffix;
    }

    private Map<String, String> getReplacementMap(NextNumberGenerator generator, String site, String object, String objectVersion, String shopOrder, String pcu, String userBo) {
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("%SITE%", site == null ? "": site);
        replacementMap.put("%ITEM%", object == null ? "": object);
        replacementMap.put("%ONEDIGITYEAR%", String.valueOf(Calendar.getInstance().get(Calendar.YEAR) % 10));
        replacementMap.put("%WEEKOFYEAR%", String.valueOf(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)));
        replacementMap.put("%ISO_WEEK_NUMBER%", String.valueOf(getISOWeekNumber(LocalDateTime.now(ZoneId.systemDefault()))));
        replacementMap.put("%ISO_ONEDIGITYEAR%", String.valueOf(getISOOneDigitYearNumber(LocalDateTime.now(ZoneId.systemDefault()))));
        replacementMap.put("%ISO_WEEKOFYEAR%", String.valueOf(getISOWeekNumber(LocalDateTime.now(ZoneId.systemDefault()))));
        replacementMap.put("%SHOP_ORDER_BO.(1)%", shopOrder == null ? "": shopOrder);
        replacementMap.put("%PCU_BO.(1)%", pcu == null ? "": pcu);
        replacementMap.put("%USER_BO.(0)%", userBo == null ? "": userBo);
        return replacementMap;
    }

    private String replaceParameters(String input, Map<String, String> replacementMap) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }

    private String getNextSequence(NextNumberGenerator generator) {
        int nextSequence = generator.getIncrementBy() == 0 ? generator.getCurrentSequence() + 1 : generator.getCurrentSequence() + generator.getIncrementBy();
        if (generator.getMinSequence() != 0 && nextSequence < generator.getMinSequence() || generator.getMaxSequence() != 0 && nextSequence > generator.getMaxSequence()) {
            throw new IllegalArgumentException("Next sequence is out of bounds");
        }
        return String.format("%0" + generator.getSequenceLength() + "d", nextSequence);
    }

    private void updateGeneratorSequence(NextNumberGenerator generator) {
        generator.setCurrentSequence(generator.getIncrementBy() == 0 ? generator.getCurrentSequence() + 1 : generator.getCurrentSequence() + generator.getIncrementBy());
    }

    private int getISOWeekNumber(LocalDateTime dateTime) {
        return dateTime.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    private int getISOOneDigitYearNumber(LocalDateTime dateTime) {
        return dateTime.getYear() % 10;
    }


}
