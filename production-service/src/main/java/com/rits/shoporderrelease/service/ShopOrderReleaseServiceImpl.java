package com.rits.shoporderrelease.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.nextnumbergeneratorservice.dto.NextNumberRequest;
import com.rits.nextnumbergeneratorservice.dto.NextNumberResponse;
import com.rits.nextnumbergeneratorservice.dto.NextnumberList;
import com.rits.nextnumbergeneratorservice.dto.InventoryNextNumberRequest;
import com.rits.pcuheaderservice.dto.PcuHeaderRequest;
import com.rits.shoporderrelease.dto.*;
import com.rits.shoporderrelease.exception.ShopOrderReleaseException;
import com.rits.shoporderrelease.model.*;
import com.rits.shoporderrelease.producer.PcuHeaderProducer;
import com.rits.shoporderrelease.producer.ShopOrderProducer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShopOrderReleaseServiceImpl implements ShopOrderReleaseService{
    private final WebClient.Builder webClientBuilder;
    private final MessageSource messageSource;
    private final MessageSource localMessageSource;
    private final ApplicationEventPublisher eventPublisher;


    @Value("${shoporder-service.url}/retrieve")
    private String shopOrderUrl;

    @Value("${shoporder-service.url}/create")
    private String createShopOrder;

    @Value("${item-service.url}/retrieve")
    private String itemUrl;

    @Value("${bom-service.url}/retrieve")
    private String bomUrl;

    @Value("${routing-service.url}/retrieve")
    private String routingUrl;

    @Value("${nextnumbergenerator-service.url}/retrieve")
    private String retrieveNxtNumberUrl;

    @Value("${nextnumbergenerator-service.url}/generateNextNumbers")
    private String newNxtNumberUrl;

    @Value("${nextnumbergenerator-service.url}/generateNextNumber")
    private String generateNextNumberUrl;

    @Value("${activity-service.url}/retrieveByActivityId")
    private String activityUrl;

    @Value("${nextnumbergenerator-service.url}/updateCurrentSequence")
    private String updateNextNumberCurrentSequenceUrl;

    @Value("${nextnumbergenerator-service.url}/getAndUpdateCurrentSequence")
    private String updateNextNumberUrl;
    @Value("${pcuheader-service.url}/create")
    private String pcuHeaderCreateUrl;

    @Value("${shoporder-service.url}/update")
    private String shopOrderUpdateUrl;

    @Value("${shoporder-service.url}/saveSerialPCu")
    private String serialPcuUpdate;

    @Value("${bom-service.url}/update")
    private String updateBomUrl;

    @Value("${routing-service.url}/inUse")
    private String updateRoutingUrl;

    private  Bom bomRecord;
    private Routing routingRecord;
    private String subOrderMessage="";

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public Boolean isItemAndBomAndRoutingReleasable(ShopOrder existingShopOrder,ReleaseRequest releaseRequest, Item existingItem)throws Exception
    {
        String bom = null;
        String bomVersion = null;
        String routing;
        String routingVersion;
        if(existingShopOrder!=null) {
            if (!existingShopOrder.getStatus().equalsIgnoreCase("releasable")) {
                throw new ShopOrderReleaseException(3215, existingShopOrder.getShopOrder());
            }
            validateDate(existingShopOrder);
            if (existingItem != null && existingItem.getItem() != null && !existingItem.getItem().isEmpty()) {
                if (!existingItem.getStatus().equalsIgnoreCase("releasable")) {
                    throw new ShopOrderReleaseException(3210, existingShopOrder.getPlannedMaterial());
                }
                if(!existingItem.getProcurementType().equalsIgnoreCase("manufactured")&&!existingItem.getProcurementType().equalsIgnoreCase("manufactured/purchased"))
                {
                    throw new ShopOrderReleaseException(3225);
                }
            } else {
                throw new ShopOrderReleaseException(3209, existingShopOrder.getPlannedMaterial());
            }

            //Routing checking in existingShopOrder and Item
            if (existingShopOrder.getPlannedRouting() != null && !existingShopOrder.getPlannedRouting().isEmpty() && existingShopOrder.getRoutingVersion() != null && !existingShopOrder.getRoutingVersion().isEmpty()) {
                routing = existingShopOrder.getPlannedRouting();
                routingVersion = existingShopOrder.getRoutingVersion();
            } else if (existingItem.getRouting() != null && !existingItem.getRouting().isEmpty() && existingItem.getRoutingVersion() != null && !existingItem.getRoutingVersion().isEmpty()) {
                routing = existingItem.getRouting();
                routingVersion = existingItem.getRoutingVersion();
            } else {
                throw new ShopOrderReleaseException(3213, existingShopOrder.getShopOrder());
            }

            //Bom checking in shopOrder and Item
            if (existingShopOrder.getPlannedBom() != null && !existingShopOrder.getPlannedBom().isEmpty() && existingShopOrder.getBomVersion() != null && !existingShopOrder.getBomVersion().isEmpty()) {
                bom = existingShopOrder.getPlannedBom();
                bomVersion = existingShopOrder.getBomVersion();
            } else if (existingItem.getBom() != null && !existingItem.getBom().isEmpty() && existingItem.getBomVersion() != null && !existingItem.getBomVersion().isEmpty()) {
                bom = existingItem.getBom();
                bomVersion = existingItem.getBomVersion();
            }

            if(bom != null && bomVersion != null && !bom.isEmpty() && !bomVersion.isEmpty())
            {
                Bom existingBom = retrieveBom(existingShopOrder.getSite(),bom,bomVersion);
                if(existingBom!=null)
                {
                    if(!existingBom.getStatus().equalsIgnoreCase("releasable"))
                    {
                        throw new ShopOrderReleaseException(3212,bom+"/"+bomVersion);
                    }
                    bomRecord = existingBom;
                }else{
                    throw new ShopOrderReleaseException(3229,bom+"/"+bomVersion);
                }
            }
            if (routing != null && routingVersion != null && !routing.isEmpty() && !routingVersion.isEmpty()) {
                Routing existingRouting = retrieveRouting(existingShopOrder.getSite(), routing, routingVersion);
                if (existingRouting != null) {
                    if (!existingRouting.getStatus().equalsIgnoreCase("releasable")) {
                        throw new ShopOrderReleaseException(3214, routing + "/" + routingVersion);
                    }
                    boolean isLastReportingStep=false;
                    for(RoutingStep routingStep: existingRouting.getRoutingStepList()){
                        isLastReportingStep= routingStep.isLastReportingStep();
                        if(isLastReportingStep){
                            break;
                        }
                    }
                    if(!isLastReportingStep){
                        throw new ShopOrderReleaseException(515);
                    }

                    routingRecord = existingRouting;
                }else {
                    throw new ShopOrderReleaseException(3228, routing + "/" + routingVersion);
                }
            } else {
                throw new ShopOrderReleaseException(3213, existingShopOrder.getShopOrder());
            }
            return true;
        }
        throw new ShopOrderReleaseException(3202,existingShopOrder.getShopOrder());
    }

    @KafkaListener(topics = "suborderRelease", groupId = "log-group", containerFactory = "kafkaListenerContainerFactory")
    public void multiReleaseShopOrder(String res) throws Exception {
        System.out.println("Shoporder release's Kafka call..");
        ObjectMapper objectMapper = new ObjectMapper();
        List<ReleaseRequest> message = objectMapper.readValue(res, new TypeReference<>() {});
        try {
            multiRelease(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public SOReleaseMessageModel multiRelease(List<ReleaseRequest> releaseRequests) {
        StringBuilder releasedNosBuilder = new StringBuilder();
        List<String> errorMessages = new ArrayList<>();

        for (ReleaseRequest releaseRequest : releaseRequests) {
            try {
                if(Double.parseDouble(releaseRequest.getQtyToRelease())<0)
                {
                    throw new ShopOrderReleaseException(3216,releaseRequest.getShopOrder());
                }
                if(Double.parseDouble(releaseRequest.getQtyToRelease())==0)
                {
                    throw new ShopOrderReleaseException(3221,releaseRequest.getShopOrder());
                }
                SOReleaseMessageModel releaseResult = serialRelease(releaseRequest);
                if(releaseResult.getMessage_details()!=null && releaseResult.getMessage_details().getMsg_type().equals("E") && releaseResult.getMessage_details().getMsg() !=null)
                {
                    errorMessages.add(releaseResult.getMessage_details().getMsg());
                }
                if(releaseResult.getMessage_details()!=null && releaseResult.getMessage_details().getMsg_type().equals("S") && releaseResult.getMessage_details().getMsg() !=null){
                    if (releasedNosBuilder.length() > 0) {
                        releasedNosBuilder.append("; ");
                    }
                    releasedNosBuilder.append(releaseResult.getMessage_details().getMsg()+": "+releaseRequest.getShopOrder());
                }
            } catch (ShopOrderReleaseException e) {
                String errorMessage = messageSource.getMessage(String.valueOf(e.getCode()), null, LocaleContextHolder.getLocale());
                errorMessages.add("E: " + errorMessage);
            } catch (Exception e) {
                errorMessages.add("E: " + e.getMessage());
            }
        }
        StringBuilder summaryMsgBuilder = new StringBuilder();
        if (releasedNosBuilder.length() > 0) {

            summaryMsgBuilder.append(releasedNosBuilder);
            summaryMsgBuilder.append(".");
        }
        if (!errorMessages.isEmpty()) {
            if (summaryMsgBuilder.length() > 0) {
                summaryMsgBuilder.append(" ");
            }
            summaryMsgBuilder.append("Error Messages: ");
            summaryMsgBuilder.append(String.join("; ", errorMessages));
        }
        MessageDetails message = MessageDetails.builder().msg(summaryMsgBuilder.toString()).msg_type("S").build();
        return SOReleaseMessageModel.builder().message_details(message).build();
    }

    @Override
    public Boolean subOrderCreator(Bom bom, String shopOrder,ReleaseRequest releaseRequest,int totalPcuQuantityCreated) throws Exception
    {
        Boolean subOrderReleased = false;
        if(bom!=null && bom.getBom()!=null)
        {
            NextNumberGenerator existingNextNumber = retrieveNextNumberForSubOrder(bom.getSite());
            if(existingNextNumber == null || existingNextNumber.getObject()==null)
            {
                throw new ShopOrderReleaseException(3222);
            }
            if(bom.getBomComponentList()!=null|| !bom.getBomComponentList().isEmpty()) {
                for (BomComponent bomItem : bom.getBomComponentList())
                {
                    Item existingItem = retrieveItem(bom.getSite(),bomItem.getComponent(),bomItem.getComponentVersion());
                    if(existingItem !=null || existingItem.getItem()!=null)
                    {
                        releaseRequest.setItemGroup(existingItem.getItemGroup());
                        if(existingItem.getProcurementType().equalsIgnoreCase("manufactured")||existingItem.getProcurementType().equalsIgnoreCase("manufactured/purchased"))
                        {
                            if(existingItem.getStatus().equalsIgnoreCase("releasable")) {

                                int requiredQty = Integer.parseInt(bomItem.getAssyQty());
                                releaseRequest.setItemGroup(existingItem.getItemGroup());
                                GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest = prefixAndSuffixBuilder(existingItem.getItem(),releaseRequest, existingNextNumber, "Shop Order", 500);
                                NextNumberMessageModel generatedNextNumber = generateNextNumber(generatePrefixAndSuffixRequest);
                                if (generatedNextNumber.getMessage_details() != null && generatedNextNumber.getMessage_details().getMsg() != null) {
                                    subOrderMessage = generatedNextNumber.getMessage_details().getMsg() + " for subOrder of order";
                                }
                                if (generatedNextNumber.getGeneratedNextNumberResponse() != null) {
                                    List<SerialPcu> serialPcu = new ArrayList<>();
                                    List<ProductionQuantity> productionQuantities = new ArrayList<>();
                                    productionQuantities.add(ProductionQuantity.builder().build());
                                    ShopOrderRequest shopOrderRequest = ShopOrderRequest.builder()
                                            .site(bom.getSite())
                                            .shopOrder(generatedNextNumber.getGeneratedNextNumberResponse().getNextNum())
                                            .status("Releasable")
                                            .orderType("Manufactured")
                                            .plannedMaterial(existingItem.getItem())
                                            .materialVersion(existingItem.getRevision())
                                            .priority(500)
                                            .buildQty(String.valueOf(requiredQty * totalPcuQuantityCreated))
                                            .serialPcu(serialPcu)
                                            .productionQuantities(productionQuantities)
                                            .parentOrderBO("ShopOrderBO:" + releaseRequest.getSite() + "," + shopOrder)
                                            .inUse(false)
                                            .build();

                                    if (existingItem.getRouting() != null && !existingItem.getRouting().isEmpty() && existingItem.getRoutingVersion() != null && !existingItem.getRoutingVersion().isEmpty()) {
                                        shopOrderRequest.setRoutingVersion(existingItem.getRoutingVersion());
                                        shopOrderRequest.setPlannedRouting(existingItem.getRouting());
                                    }
                                    if (existingItem.getBom() != null && !existingItem.getBom().isEmpty() && existingItem.getBomVersion() != null && !existingItem.getBomVersion().isEmpty()) {
                                        shopOrderRequest.setPlannedBom(existingItem.getBom());
                                        shopOrderRequest.setBomVersion(existingItem.getBomVersion());
                                    }
                                    if (StringUtils.isNotEmpty(shopOrderRequest.getPlannedRouting()) || StringUtils.isNotEmpty(shopOrderRequest.getPlannedRouting())) {
                                        SOReleaseMessageModel createShopOrderForSubOrder = createShopOrderForSubOrder(shopOrderRequest);

                                        GeneratePrefixAndSuffixRequest updateCurrentSequenceRequest = prefixAndSuffixBuilder(existingItem.getItem(),releaseRequest, existingNextNumber, "Shop Order", 500);
                                        GeneratedNextNumber updateCurrentSequence = updateCurrentSequence(updateCurrentSequenceRequest);

                                    if (createShopOrderForSubOrder.getMessage_details() != null && createShopOrderForSubOrder.getMessage_details().getMsg_type().equals("S")) {
                                        subOrderReleased = true;
                                    }
                                } else{
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return subOrderReleased;
    }


    public SOReleaseMessageModel serialRelease(ReleaseRequest releaseRequest) throws Exception {

        Map<String,Integer> pcuNumberList = new HashMap<>();
        List<String> newPcus = new ArrayList<>();

        ShopOrder existingShopOrder = retrieveShopOrder(releaseRequest.getSite(),releaseRequest.getShopOrder());
        List<String> newPcuNumbers = new ArrayList<>();
        if(existingShopOrder != null) {
            Item existingItem = retrieveItem(existingShopOrder.getSite(), existingShopOrder.getPlannedMaterial(), existingShopOrder.getMaterialVersion());

            if (isItemAndBomAndRoutingReleasable(existingShopOrder, releaseRequest, existingItem)) {

                if (Double.parseDouble(existingShopOrder.getAvailableQtyToRelease()) < Double.parseDouble(releaseRequest.getQtyToRelease())) {
                    throw new ShopOrderReleaseException(3216, releaseRequest.getShopOrder());
                }


                double size = Double.parseDouble(releaseRequest.getQtyToRelease());
                double lotSize = existingItem != null ? existingItem.getLotSize() : 0.0;
                List<Double> releasedQtys = new ArrayList<>();
                int noOfPcusToRelease = (int) Math.ceil(size / lotSize);

                for (var i = 0; i < noOfPcusToRelease; i++) {
                    double qtyToAdd = Math.min(size, lotSize);
                    releasedQtys.add(qtyToAdd);
                    size -= qtyToAdd;
                }

                int countNewPcus = 0;
                List<SerialPcu> existingSerialPcus = existingShopOrder.getSerialPcu();
                if (existingSerialPcus != null && !existingSerialPcus.isEmpty()) {
                    for (int i = 0; i < existingSerialPcus.size(); i++) {
                        if ("New".equals(existingSerialPcus.get(i).getState())) {

                            newPcuNumbers.add(existingSerialPcus.get(i).getPcuNumber());
                            Pcu pcuNumber = Pcu.builder().pcuBo("PcuBO:" + releaseRequest.getSite() + "," + existingSerialPcus.get(i).getPcuNumber()).build();
                            pcuNumberList.put(pcuNumber.getPcuBo(), releasedQtys.get(i).intValue());
                            countNewPcus++;
                            if (countNewPcus >= noOfPcusToRelease) break;

                        }
                    }
                }

                newPcus.addAll(newPcuNumbers);

                double remainingSize = releasedQtys.stream().mapToDouble(Double::doubleValue).sum() - pcuNumberList.size() * lotSize;
                List<NextNumberResponse> pcuList = new ArrayList<>();

                try {
                    if ((noOfPcusToRelease - countNewPcus) != 0) {
                        NextNumberRequest nextNumberRequest = NextNumberRequest.builder()
                                .site(existingShopOrder.getSite())
                                .object(existingShopOrder.getPlannedMaterial())
                                .objectVersion(existingShopOrder.getMaterialVersion())
                                .batchQty(remainingSize)
                                .userBo(releaseRequest.getUser())
                                .shopOrder(existingShopOrder.getShopOrder())
                                .numberType("PCU release")
                                .build();

                        pcuList = webClientBuilder
                                .build()
                                .post()
                                .uri(newNxtNumberUrl)
                                .body(BodyInserters.fromValue(nextNumberRequest))
                                .retrieve()
                                .onStatus(
                                        status -> status.isError(),  // Handle any error status
                                        clientResponse -> clientResponse.bodyToMono(String.class)
                                                .map(errorMessage -> new RuntimeException("Error: " + errorMessage))
                                )
                                .bodyToMono(new ParameterizedTypeReference<List<NextNumberResponse>>() {
                                })
                                .block();


                        if (pcuList != null && !pcuList.isEmpty()) {
                            for (int i = 0; i < pcuList.size(); i++) {
                                newPcuNumbers.add(pcuList.get(i).getNextNumber());
                                Pcu pcuNumber = Pcu.builder().pcuBo("PcuBO:" + releaseRequest.getSite() + "," + pcuList.get(i).getNextNumber()).build();
                                pcuNumberList.put(pcuNumber.getPcuBo(), (int) pcuList.get(i).getQty());
                            }
                        }
                    }
                } catch (WebClientResponseException e) {
                    String errorMessage = e.getResponseBodyAsString();
                    throw new RuntimeException("Error from service: " + errorMessage, e);
                }

                if (pcuNumberList.size() != 0) {
                    if (routingRecord != null) {
                        existingShopOrder.setPlannedRouting(routingRecord.getRouting());
                        existingShopOrder.setRoutingVersion(routingRecord.getVersion());
                    }
                    if (bomRecord != null) {
                        existingShopOrder.setPlannedBom(bomRecord.getBom());
                        existingShopOrder.setBomVersion(bomRecord.getRevision());
                    }
                    createPcuHeader(existingShopOrder, releaseRequest, pcuNumberList);

                    if(pcuList != null && !pcuList.isEmpty()) {
                        GeneratePrefixAndSuffixRequest nextNumberRequest = GeneratePrefixAndSuffixRequest.builder().site(releaseRequest.getSite())
                                .currentSequence(pcuList.get(pcuList.size() - 1).getCurrentSequence())
                                .object(releaseRequest.getPlannedMaterial())
                                .objectVersion(releaseRequest.getMaterialVersion())
                                .numberType("PCU release")
                                .userBO(releaseRequest.getUser())
                                .build();
                        NextNumberMessageModel updateCurrentSequence = updateNextNumberCurrentSequence(nextNumberRequest);
                    }

                    double releaseQty = 0.0;
                    if(existingShopOrder.getProductionQuantities() != null && !existingShopOrder.getProductionQuantities().isEmpty())
                        releaseQty = Double.parseDouble(existingShopOrder.getProductionQuantities().get(0).getReleasedQty());

                    if(existingShopOrder.getSerialPcu() == null){
                        List<SerialPcu> newSerialPcu = new ArrayList<>();
                        existingShopOrder.setSerialPcu(newSerialPcu);
                    }

                    for (Map.Entry<String, Integer> entry : pcuNumberList.entrySet()) {
                        String pcu = entry.getKey();
                        String[] pcuArray = pcu.split(",");
                        if (!newPcus.contains(pcuArray[1])) {
                            SerialPcu serialPcu = SerialPcu.builder().pcuQuantity(String.valueOf(entry.getValue())).serialNumber("").count("").pcuNumber(pcuArray[1]).state("Released").enabled(true).build();
                            existingShopOrder.getSerialPcu().add(serialPcu);
                        }
                        releaseQty += entry.getValue();
                    }

                    existingShopOrder.setNewPcus(newPcus);
                    existingShopOrder.setPcuNumberList(pcuNumberList);

                    if(existingShopOrder.getProductionQuantities() != null && !existingShopOrder.getProductionQuantities().isEmpty())
                        existingShopOrder.getProductionQuantities().get(0).setReleasedQty(String.valueOf(releaseQty));

                    SOReleaseMessageModel saveSerialPcu = saveSerialPcu(existingShopOrder);

                    ActivityRequest activityRequest = ActivityRequest.builder().site(releaseRequest.getSite()).activityId(releaseRequest.getNextNumberActivity()).build();
                    ActivityListResponseList activityResponseList = webClientBuilder
                            .build()
                            .post()
                            .uri(activityUrl)
                            .body(BodyInserters.fromValue(activityRequest))
                            .retrieve()
                            .bodyToMono(ActivityListResponseList.class)
                            .block();

                    List<ReleaseRequest> releaseRequestList = new ArrayList<>();
                    try {
//                        if (activityResponseList != null && activityResponseList.getActivityList().get(0).getActivityRules().size() > 0 && activityResponseList.getActivityList().get(0).getActivityRules().get(0).getRuleName().equals("auto-suborderrelease") && activityResponseList.getActivityList().get(0).getActivityRules().get(0).getSetting().equals("true")) {
                        if (activityResponseList != null ) {

                            if (bomRecord != null) {

                                ReleaseRequest request = ReleaseRequest.builder()
                                        .topic("suborderRelease")
                                        .build();

                                releaseRequestList.add(request);
                                eventPublisher.publishEvent(new ShopOrderProducer(releaseRequestList));
                                Boolean subOrderRelease = subOrderCreator(bomRecord, existingShopOrder.getShopOrder(), releaseRequest, noOfPcusToRelease);
                            }
                        }
                    } catch (Exception e) {
                        throw e;
                    }
                }
                existingShopOrder.setInUse(true);
                try {
                    updateShopOrder(existingShopOrder);
                } catch (Exception e) {
                    throw e;
                }
                if (routingRecord != null && routingRecord.getRouting() != null) {
                    routingRecord.setInUse(true);
                    Boolean updateRouting = updateRouting(routingRecord);
                }
                if (bomRecord != null && bomRecord.getBom() != null) {
                    bomRecord.setUsed(true);
                    SOReleaseMessageModel updateBom = updateBom(bomRecord);
                }
            }
        }
        bomRecord=null;
        routingRecord=null;
        String createdMessage = getFormattedMessage(9);
        if(newPcuNumbers.isEmpty())
        {
            MessageDetails message = MessageDetails.builder().msg("pcu list is empty").msg_type("E").build();
            return SOReleaseMessageModel.builder().message_details(message).build();
        }

        MessageDetails message = MessageDetails.builder().msg(String.join(",",newPcuNumbers)+" "+createdMessage+". "+subOrderMessage).msg_type("S").build();
        subOrderMessage="";
        return SOReleaseMessageModel.builder().message_details(message).build();

    }
    public ShopOrder retrieveShopOrder(String site,String shopOrder) throws Exception
    {
        ShopOrderRequest shopOrderRetrieveRequest = ShopOrderRequest.builder().site(site).shopOrder(shopOrder).build();
        ShopOrder existingShopOrder = webClientBuilder
                .build()
                .post()
                .uri(shopOrderUrl)
                .body(BodyInserters.fromValue(shopOrderRetrieveRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ShopOrder>() {})
                .block();
        return existingShopOrder;
    }
     public SOReleaseMessageModel createShopOrderForSubOrder(ShopOrderRequest shopOrderRequest) throws Exception
     {
         SOReleaseMessageModel createShopOrderForSubOrder = webClientBuilder
                 .build()
                 .post()
                 .uri(createShopOrder)
                 .body(BodyInserters.fromValue(shopOrderRequest))
                 .retrieve()
                 .bodyToMono(new ParameterizedTypeReference<SOReleaseMessageModel>() {
                 })
                 .block();
         return createShopOrderForSubOrder;
     }
     public Item retrieveItem(String site,String item,String version)throws Exception
     {
         ItemRequest itemRequest = ItemRequest.builder().site(site).item(item).revision(version).build();
         Item existingItem = webClientBuilder
                 .build()
                 .post()
                 .uri(itemUrl)
                 .body(BodyInserters.fromValue(itemRequest))
                 .retrieve()
                 .bodyToMono(new ParameterizedTypeReference<Item>() {
                 })
                 .block();
         return existingItem;
     }
     public Bom retrieveBom(String site,String bom,String version) throws Exception
     {
         BomRequest bomRequest = BomRequest.builder().site(site).bom(bom).build();
         Bom existingBom = webClientBuilder
                 .build()
                 .post()
                 .uri(bomUrl)
                 .body(BodyInserters.fromValue(bomRequest))
                 .retrieve()
                 .bodyToMono(new ParameterizedTypeReference<Bom>() {
                 })
                 .block();

         return  existingBom;
     }
     public Routing retrieveRouting(String site, String routing, String version)throws Exception
     {
         RoutingRequest routingRequest = RoutingRequest.builder().site(site).routing(routing).version(version).build();
         Routing existingRouting = webClientBuilder
                 .build()
                 .post()
                 .uri(routingUrl)
                 .body(BodyInserters.fromValue(routingRequest))
                 .retrieve()
                 .bodyToMono(new ParameterizedTypeReference<Routing>() {
                 })
                 .block();
         return existingRouting;
     }
    public NextNumberGenerator retrieveNextNumberForSubOrder(String site)throws Exception
    {
        NextNumberGeneratorRequest retrieveNextNumber = NextNumberGeneratorRequest.builder().site(site).numberType("Shop Order").build();
        NextNumberGenerator existingNextNumber = webClientBuilder.build()
                .post()
                .uri(retrieveNxtNumberUrl)
                .bodyValue(retrieveNextNumber)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<NextNumberGenerator>() {
                })
                .block();
        return existingNextNumber;
    }
    public NextNumberGenerator retrieveNextNumber(String site, String object, String objectVersion)throws Exception
    {
        NextNumberGeneratorRequest retrieveNextNumber = NextNumberGeneratorRequest.builder().site(site).numberType("PCU release").object(object).objectVersion(objectVersion).build();
        NextNumberGenerator existingNextNumber = webClientBuilder.build()
                .post()
                .uri(retrieveNxtNumberUrl)
                .bodyValue(retrieveNextNumber)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<NextNumberGenerator>() {
                })
                .block();
        return existingNextNumber;
    }
    public NextNumberMessageModel generateNextNumber(GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest)throws Exception
    {
        NextNumberMessageModel generatedNextNumber = webClientBuilder.build()
                .post()
                .uri(generateNextNumberUrl)
                .bodyValue(generatePrefixAndSuffixRequest)
                .retrieve()
                .bodyToMono(NextNumberMessageModel.class)
                .block();
        return generatedNextNumber;
    }

    public GeneratedNextNumber updateCurrentSequence(GeneratePrefixAndSuffixRequest updateCurrentSequenceRequest) throws Exception
    {
        GeneratedNextNumber updateCurrentSequence = null;
        try{
            updateCurrentSequence = webClientBuilder.build()
                    .post()
                    .uri(updateNextNumberCurrentSequenceUrl)
                    .bodyValue(updateCurrentSequenceRequest)
                    .retrieve()
                    .bodyToMono(GeneratedNextNumber.class)
                    .block();

        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        return updateCurrentSequence;
    }

    public NextNumberMessageModel updateNextNumberCurrentSequence(GeneratePrefixAndSuffixRequest updateCurrentSequenceRequest) throws Exception
    {
        NextNumberMessageModel updateCurrentSequence = null;
        try{
            updateCurrentSequence = webClientBuilder.build()
                    .post()
                    .uri(updateNextNumberUrl)
                    .bodyValue(updateCurrentSequenceRequest)
                    .retrieve()
                    .bodyToMono(NextNumberMessageModel.class)
                    .block();

        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        return updateCurrentSequence;
    }

//    public SOReleaseMessageModel createPcuHeader(ShopOrder existingShopOrder, ReleaseRequest releaseRequest, Map<String,Integer> pcuNumberList)throws Exception
//    {
//        PCUHeaderRequest pcuHeaderRequest = PCUHeaderRequest.builder()
//                .shopOrder(existingShopOrder)
//                .pcuNumberList(pcuNumberList)
//                .parentOrderBO(existingShopOrder.getParentOrderBO())
//                .parentPcuBO(existingShopOrder.getParentPcuBO())
//                .userBO("UserBO:"+releaseRequest.getSite()+","+releaseRequest.getUserBO())
//                .build();
//        SOReleaseMessageModel createPCUHeader = webClientBuilder.build()
//                .post()
//                .uri(pcuHeaderCreateUrl)
//                .bodyValue(pcuHeaderRequest)
//                .retrieve()
//                .bodyToMono(SOReleaseMessageModel.class)
//                .block();
//        return createPCUHeader;
//    }

    public void createPcuHeader(ShopOrder existingShopOrder, ReleaseRequest releaseRequest, Map<String, Integer> pcuNumberList)throws Exception {
        com.rits.pcuheaderservice.dto.ShopOrder shopOrder = com.rits.pcuheaderservice.dto.ShopOrder.builder()
                .plannedRouting(existingShopOrder.getPlannedRouting())
                .site(existingShopOrder.getSite())
                .routingVersion(existingShopOrder.getRoutingVersion())
                .plannedBom(existingShopOrder.getPlannedBom())
                .bomVersion(existingShopOrder.getBomVersion())
                .plannedMaterial(existingShopOrder.getPlannedMaterial())
                .materialVersion(existingShopOrder.getMaterialVersion())
                .shopOrder(existingShopOrder.getShopOrder())
                .build();

        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                .shopOrder(shopOrder)
                .pcuNumberList(pcuNumberList)
                .parentOrderBO(existingShopOrder.getParentOrderBO())
                .parentPcuBO(existingShopOrder.getParentPcuBO())
                .userBO("UserBO:" + releaseRequest.getSite() + "," + releaseRequest.getUser())
                .topic("pcu-header-create")
                .build();
        eventPublisher.publishEvent(new PcuHeaderProducer(pcuHeaderRequest));
    }
    public SOReleaseMessageModel updateShopOrder(ShopOrder existingShopOrder)throws Exception
    {
        SOReleaseMessageModel updateShopOrder = webClientBuilder
                .build()
                .post()
                .uri(shopOrderUpdateUrl)
                .body(BodyInserters.fromValue(existingShopOrder))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<SOReleaseMessageModel>() {
                })
                .block();
        return updateShopOrder;
    }
    public SOReleaseMessageModel saveSerialPcu(ShopOrder existingShopOrder)throws Exception
    {
        SOReleaseMessageModel saveSerialPcu = webClientBuilder
                .build()
                .post()
                .uri(serialPcuUpdate)
                .body(BodyInserters.fromValue(existingShopOrder))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<SOReleaseMessageModel>() {
                })
                .block();
        return saveSerialPcu;
    }
    public SOReleaseMessageModel updateBom(Bom bomRecord)throws Exception
    {
        SOReleaseMessageModel updateBom = webClientBuilder
                .build()
                .post()
                .uri(updateBomUrl)
                .body(BodyInserters.fromValue(bomRecord))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<SOReleaseMessageModel>() {
                })
                .block();
        return updateBom;
    }
    public Boolean updateRouting(Routing routingRecord)throws Exception
    {
        Boolean updateRouting = webClientBuilder
                .build()
                .post()
                .uri(updateRoutingUrl)
                .body(BodyInserters.fromValue(routingRecord))
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return updateRouting;
    }
    public GeneratePrefixAndSuffixRequest prefixAndSuffixBuilder(String BomItem,ReleaseRequest releaseRequest,NextNumberGenerator existingNextNumber,String numberType,int priority)throws Exception
    {
        String item="";

        if(BomItem!=null)
        {
            item=BomItem;
        }
        else{
            item=releaseRequest.getPlannedMaterial();
        }
        GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest = GeneratePrefixAndSuffixRequest.builder()
                .site(releaseRequest.getSite())
                .itemGroupBO("ItemGroupBO:" + releaseRequest.getSite() + "," + releaseRequest.getItemGroup())
                .object(existingNextNumber.getObject())
                .objectVersion(existingNextNumber.getObjectVersion())
                .itemBO("ItemBO:" +releaseRequest.getSite() + "," + existingNextNumber.getObject() + "," +  existingNextNumber.getObjectVersion())
                .shopOrderBO("ShopOrderBO:" + releaseRequest.getSite() + "," + releaseRequest.getShopOrder())
                .priority(priority)
                .numberBase(existingNextNumber.getNumberBase())
                .sequenceLength(existingNextNumber.getSequenceLength())
                .minSequence(existingNextNumber.getMinSequence())
                .maxSequence(existingNextNumber.getMaxSequence())
                .incrementBy(existingNextNumber.getIncrementBy())
                .currentSequence(existingNextNumber.getCurrentSequence())
                .numberType(numberType)
                .orderType(existingNextNumber.getOrderType())
                .userBO(releaseRequest.getUser())
                .defineBy(existingNextNumber.getDefineBy())
                .suffix(existingNextNumber.getSuffix())
                .prefix(existingNextNumber.getPrefix())
                .nonStartObject(item)
                .nextNumberActivity(releaseRequest.getNextNumberActivity())
                .build();
        return generatePrefixAndSuffixRequest;
    }
    public void validateDate(ShopOrder existingShopOrder) throws Exception
    {
        if(existingShopOrder.getPlannedStart()!=null)
        {
            if(LocalDateTime.now().isBefore(existingShopOrder.getPlannedStart()))
            {
                throw new ShopOrderReleaseException(3230);
            }
        }
        if(existingShopOrder.getScheduledStart()!=null)
        {
            if(LocalDateTime.now().isBefore(existingShopOrder.getScheduledStart()))
            {
                throw new ShopOrderReleaseException(3231);
            }
        }
        if(existingShopOrder.getPlannedCompletion()!=null)
        {
            if(LocalDateTime.now().isAfter(existingShopOrder.getPlannedCompletion()))
            {
                throw new ShopOrderReleaseException(3233);
            }
        }
        if(existingShopOrder.getScheduledEnd()!=null)
        {
            if(LocalDateTime.now().isAfter(existingShopOrder.getScheduledEnd()))
            {
                throw new ShopOrderReleaseException(3232);
            }
        }
    }
}
