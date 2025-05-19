package com.rits.scrapservice.service;

import com.rits.Utility.BOConverter;
import com.rits.assemblyservice.dto.AssemblyRequest;
import com.rits.assyservice.model.AssyData;
import com.rits.pcuheaderservice.dto.PcuHeaderRequest;
import com.rits.pcuheaderservice.model.PcuHeader;
import com.rits.pcuinqueueservice.model.PcuInQueue;
import com.rits.productionlogservice.dto.ActualCycleSum;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import com.rits.productionlogservice.model.ProductionLog;
import com.rits.productionlogservice.model.ProductionLogMongo;
import com.rits.productionlogservice.producer.ProductionLogProducer;
import com.rits.scrapservice.dto.*;
import com.rits.scrapservice.model.*;
import com.rits.scrapservice.repository.ScrapRepository;
import com.rits.shoporderservice.model.SerialPcu;
import com.rits.shoporderservice.model.ShopOrder;
import com.rits.startservice.model.PcuInWork;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScrapServiceImpl implements ScrapService{
    private final ScrapRepository scrapRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${pcuheader-service.url}/readPcu")
    private String retrievePcuHeaderUrl;

    @Value("${pcuheader-service.url}/delete")
    private String deletePcuHeaderUrl;

    @Value("${pcurouterheader-service.uri}/disableRecord")
    private String deletePcuRouterHeaderUrl;

    @Value("${start-service.url}/deletePcuInAllOperations")
    private String deletePcuInWorkUrl;

    @Value("${pcuinqueue-service.url}/deletePcuInAllOperations")
    private String deletePcuInQueueUrl;

    @Value("${pcudone-service.url}/deleteByPcu")
    private String deletePcuDoneUrl;

    @Value("${pcucomplete-service.url}/deleteByPcu")
    private String deletePcuCompleteUrl;

    @Value("${pcuheader-service.url}/unDelete")
    private String unDeletePcuHeaderUrl;

    @Value("${pcurouterheader-service.uri}/enableRecord")
    private String unDeletePcuRouterHeaderUrl;

    @Value("${start-service.url}/unDeletePcuInAllOperations")
    private String unDeletePcuInWorkUrl;

    @Value("${pcuinqueue-service.url}/unDeletePcuInAllOperations")
    private String unDeletePcuInQueueUrl;

    @Value("${pcudone-service.url}/unDeleteByPcu")
    private String unDeletePcuDoneUrl;

    @Value("${pcucomplete-service.url}/unDeleteByPcu")
    private String unDeletePcuCompleteUrl;

    @Value("${bomheader-service.url}/deleteByPcu")
    private String deleteBomHeaderUrl;

    @Value("${bomheader-service.url}/unDeleteByPcu")
    private String unDeleteBomHeaderUrl;
    @Value("${shoporder-service.url}/retrieve")
    private String retrieveShopOrder;

    @Value("${pcuinqueue-service.url}/retrieveByPcu")
    private String retrievePcuInQueueUrl;
    @Value("${start-service.url}/retrieveByPcuAndSite")
    private String retrievePcuInWorkUrl;

    @Value("${pcuinqueue-service.url}/retrieveDeletedPcu")
    private String retrieveDeletedPcuInQueueUrl;

    @Value("${start-service.url}/retrieveDeletedPcu")
    private String retrieveDeletedPcuInWorkUrl;
    @Value("${productionlog-service.url}/retrieveByPcuOperationShopOrderAndEventType")
    private String retrieveProductionLogUrl;

    @Value("${productionlog-service.url}/retrieveAllByEventTypeShopOrderOperationItem")
    private String retrieveAllSignOffLogUrl;

    @Value("${assy-service.url}/retrieve")
    private String retrieveAssembly;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public List<ScrapRequest> convertToScrapRequestList(List<ScrapRequestDetails> scrapRequestListNoBO) {
        List<ScrapRequest> requests = new ArrayList<>();

        for (ScrapRequestDetails requestNoBO : scrapRequestListNoBO) {
            ScrapRequest request = new ScrapRequest();

            request.setSite(requestNoBO.getSite());

            if (requestNoBO.getObject() != null && !requestNoBO.getObject().isEmpty()) {
                request.setObject(requestNoBO.getObject());
            }

            if (requestNoBO.getStatus() != null && !requestNoBO.getStatus().isEmpty()) {
                request.setStatus(requestNoBO.getStatus());
            }

            if (requestNoBO.getPcu() != null && !requestNoBO.getPcu().isEmpty()) {
                request.setPcu(BOConverter.retrievePcuBO(requestNoBO.getSite(), requestNoBO.getPcu()));
            }

            if (requestNoBO.getOperation() != null && !requestNoBO.getOperation().isEmpty()) {
                request.setOperation(BOConverter.retrieveOperationBO(requestNoBO.getSite(), requestNoBO.getOperation(), requestNoBO.getOperationVersion()));
            }

            if (requestNoBO.getResource() != null && !requestNoBO.getResource().isEmpty()) {
                request.setResource(BOConverter.retriveResourceBO(requestNoBO.getSite(), requestNoBO.getResource()));
            }

            if (requestNoBO.getShopOrder() != null && !requestNoBO.getShopOrder().isEmpty()) {
                request.setShopOrder(BOConverter.retrieveShopOrderBO(requestNoBO.getSite(), requestNoBO.getShopOrder()));
            }

            if (requestNoBO.getItem() != null && !requestNoBO.getItem().isEmpty()) {
                request.setItemBO(BOConverter.retrieveItemBO(requestNoBO.getSite(), requestNoBO.getItem(), requestNoBO.getItemVersion()));
            }

            if (requestNoBO.getRouting() != null && !requestNoBO.getRouting().isEmpty()) {
                request.setRoutingBO(BOConverter.retrieveRoutingBO(requestNoBO.getSite(), requestNoBO.getRouting(),requestNoBO.getRoutingVersion()));
            }

            if (requestNoBO.getUser() != null && !requestNoBO.getUser().isEmpty()) {
                request.setUserBO(BOConverter.retrieveUserBO(requestNoBO.getSite(), requestNoBO.getUser()));
            }

            if (requestNoBO.getBom() != null && !requestNoBO.getBom().isEmpty()) {
                request.setBomBO(BOConverter.retrieveBomBO(requestNoBO.getSite(), requestNoBO.getBom(), requestNoBO.getBomVersion()));
            }

            if (requestNoBO.getProcessLot() != null) {
                request.setProcessLot(requestNoBO.getProcessLot());
            }

            if (requestNoBO.getScrapQty() != null) {
                request.setScrapQty(requestNoBO.getScrapQty());
            }

            if (requestNoBO.getCreatedDateTime() != null) {
                request.setCreatedDateTime(requestNoBO.getCreatedDateTime());
            }

            if (requestNoBO.getPcuHeaderHandle() != null && !requestNoBO.getPcuHeaderHandle().isEmpty()) {
                request.setPcuHeaderHandle(requestNoBO.getPcuHeaderHandle());
            }

            if (requestNoBO.getRouterHeaderHandle() != null && !requestNoBO.getRouterHeaderHandle().isEmpty()) {
                request.setRouterHeaderHandle(requestNoBO.getRouterHeaderHandle());
            }

            if (requestNoBO.getUserId() != null && !requestNoBO.getUserId().isEmpty()) {
                request.setUserId(requestNoBO.getUserId());
            }

            if (requestNoBO.getEventType() != null && !requestNoBO.getEventType().isEmpty()) {
                request.setEventType(requestNoBO.getEventType());
            }

            if (requestNoBO.getEventData() != null && !requestNoBO.getEventData().isEmpty()) {
                request.setEventData(requestNoBO.getEventData());
            }
            requests.add(request);
        }
        return requests;
    }

    @Override
    public ScrapMessageModel scrap(List<ScrapRequest> scrapRequestList) throws Exception
    {
        StringBuilder scrapedPcus = new StringBuilder();
        List<String> assembledDataPcuId = new ArrayList<>();
        List<String> pcuList = null;
        String totalActiveShopOrder = "";

        for(ScrapRequest scrapRequest : scrapRequestList) {

            String site = scrapRequest.getSite();
            ShopOrder retrievedShopOrder = retrieveShopOrder(scrapRequest.getSite(), scrapRequest.getObject());

            if (retrievedShopOrder != null && retrievedShopOrder.getShopOrder() != null) {
                totalActiveShopOrder += scrapRequest.getObject()+ ",";
                continue;
            }

            String pcuBO = BOConverter.retrievePcuBO(scrapRequest.getSite(),scrapRequest.getObject());
            String pcu = scrapRequest.getObject();

            AssemblyRequest assemblyRequest = AssemblyRequest.builder().site(site).pcuBO(pcuBO).build();
            AssyData assemblyRec =webClientBuilder.build()
                    .post()
                    .uri(retrieveAssembly)
                    .bodyValue(assemblyRequest)
                    .retrieve()
                    .bodyToMono(AssyData.class)
                    .block();

            if(assemblyRec != null && assemblyRec.getPcuBO() != null)
                assembledDataPcuId.add(pcuBO);

            else{

                pcuList = new ArrayList<>();
                pcuList.add(scrapRequest.getObject());
                List<RetrieveResponse> getTheRetrieveResponse = getTheRetrieveResponse(site, pcuList);// record of InWork + InQueue
                PcuHeader retrivedPcuHeader = retrievePcuHeaderByPcuBO(site, pcuBO);

                if(retrivedPcuHeader == null)
                    throw new IllegalArgumentException("record not available");

                Response deletePcuHeader = deletePcuHeaderByPcuBO(site,pcuBO);
                Boolean deleteBomHeader = deleteBomHeader(site,pcuBO);
                MessageModel deletePcuRouterHeader = deletePcuRouterHeader(site, pcuBO);
                Boolean deletePcuInWork = deletePcuInWork(site, pcu);
                Boolean deletePcuInQueue = deletePcuInQueue(site, pcu);
                Boolean deletePcuDone = deletePcuDone(site, pcu);
                Boolean deletePcuComplete = deletePcuComplete(site, pcu);


                if (getTheRetrieveResponse != null && !getTheRetrieveResponse.isEmpty()) {
                    RetrieveResponse retrieveResponse = getTheRetrieveResponse.get(0);// InWork

                    if(retrieveResponse == null)
                        throw new IllegalArgumentException("record is not present");

                    if(StringUtils.isEmpty(retrieveResponse.getPcu()))
                        throw new IllegalArgumentException("pcu is empty");

                    Scrap scrap = Scrap.builder()
                            .site(site)
                            .scrapBO("ScrapBO:" + site + "," + (StringUtils.isEmpty(retrieveResponse.getPcu()) ? null: retrieveResponse.getPcu()))
                            .pcuBO(BOConverter.retrievePcuBO(site, (StringUtils.isEmpty(retrieveResponse.getPcu()) ? null: retrieveResponse.getPcu())))
                            .status(StringUtils.isEmpty(retrieveResponse.getStatus()) ? null : retrieveResponse.getStatus())
                            .operationBO(StringUtils.isEmpty(retrieveResponse.getOperation()) ? null : BOConverter.retrieveOperationBO(site, retrieveResponse.getOperation(), retrieveResponse.getOperationVersion()))
                            .resourceBO(StringUtils.isEmpty(retrieveResponse.getResource()) ? null : BOConverter.retriveResourceBO(site, retrieveResponse.getResource()))
                            .shopOrderBO(StringUtils.isEmpty(retrieveResponse.getShopOrder()) ? null : BOConverter.retrieveShopOrderBO(site, retrieveResponse.getShopOrder()))
                            .processLot(StringUtils.isEmpty(retrieveResponse.getProcessLot()) ? null : retrieveResponse.getProcessLot())
                            .itemBO(StringUtils.isEmpty(retrieveResponse.getItem()) ? null : BOConverter.retrieveItemBO(site, retrieveResponse.getItem(), retrieveResponse.getItemVersion()))
                            .routingBO(StringUtils.isEmpty(retrieveResponse.getRouter()) ? null : BOConverter.retrieveRoutingBO(site, retrieveResponse.getRouter(), retrieveResponse.getRouterVersion()))
                            .bomBO(StringUtils.isEmpty(retrieveResponse.getBom()) ? null : BOConverter.retrieveBomBO(site, retrieveResponse.getBom(), retrieveResponse.getBomVersion()))
                            .scrapQty(retrivedPcuHeader.getQtyInQueue())
                            .userBO(scrapRequest.getUserId())
                            .createdDateTime(LocalDateTime.now())
                            .pcuHeaderHandle(scrapRequest.getPcuHeaderHandle())
                            .routerHeaderHandle(scrapRequest.getRouterHeaderHandle())
                            .active(1)
                            .build();
                    scrapRepository.save(scrap);
//                pcuList.add(scrapRequest.getObject());// adding 2x
                    scrapRequest.setEventType("ScrapSFC");
                    scrapRequest.setEventData(" Scrapped successfully");
                    scrapRequest.setScrapQty(String.valueOf(retrivedPcuHeader.getQtyInQueue()));
                    scrapRequest.setPcu(BOConverter.retrievePcuBO(site, (StringUtils.isEmpty(retrieveResponse.getPcu()) ? "": retrieveResponse.getPcu())));
                    scrapRequest.setStatus(retrieveResponse.getStatus());
                    scrapRequest.setOperation(BOConverter.retrieveOperationBO(site, retrieveResponse.getOperation(), retrieveResponse.getOperationVersion()));
                    scrapRequest.setResource(BOConverter.retriveResourceBO(site, retrieveResponse.getResource()));
                    scrapRequest.setShopOrder(BOConverter.retrieveShopOrderBO(site, retrieveResponse.getShopOrder()));
                    scrapRequest.setProcessLot(retrieveResponse.getProcessLot());
                    scrapRequest.setItemBO(BOConverter.retrieveItemBO(site, retrieveResponse.getItem(), retrieveResponse.getItemVersion()));
                    scrapRequest.setRoutingBO(BOConverter.retrieveRoutingBO(site, retrieveResponse.getRouter(), retrieveResponse.getRouterVersion()));
                    scrapRequest.setBomBO(BOConverter.retrieveBomBO(site, retrieveResponse.getBom(), retrieveResponse.getBomVersion()));

                    Boolean productionLogged = productionLog(scrapRequest);
                    if (productionLogged) {
                        productionLog(scrapRequest, "scrapped successfully");
                    }
                    scrapedPcus.append(retrieveResponse.getPcu()).append(", ");
                }
//                return ScrapMessageModel.builder().message_details(new MessageDetails(pcuList+" "+"Scrapped" , "S")).build();
            }
        }
//        String msg = "Pcu: "+ (totalActiveShopOrder.isEmpty()?"0 ":totalActiveShopOrder) + " available in shopOrder and " + (assembledDataPcuId.size()>0? String.join(",",assembledDataPcuId):" 0")+" available in assembly, so can't be scrap";
        if(!scrapedPcus.toString().equals("")) {
            return ScrapMessageModel.builder().message_details(new MessageDetails(scrapedPcus + "Scrapped", "S")).build();
        }

        String msg = "Can not scrap pcu.";
        return ScrapMessageModel.builder().message_details(new MessageDetails(msg , "E")).build();
//        if(!assembledDataPcuId.isEmpty())
//            return ScrapMessageModel.builder().message_details(new MessageDetails(msg , "E")).build();

//        if (pcus != null)
//            pcus = String.join(",",pcuList);// outer pcu is blank : my know
//        if(!totalActiveShopOrder.isEmpty() && pcus.isEmpty())
//        {
//            return ScrapMessageModel.builder().message_details(new MessageDetails("Pcu "+totalActiveShopOrder+" "+"not found." , "E")).build();
//        }
//        return ScrapMessageModel.builder().message_details(new MessageDetails(totalActiveShopOrder+" "+"available in shopOrder" , "S")).build();

    }

    @Override
    public ScrapMessageModel unScrap(List<ScrapRequest> scrapRequestList) throws Exception
    {
        String pcus = "";
        List<String> pcusList = new ArrayList<>();
        String shopOrder = "";
        for(ScrapRequest scrapRequest : scrapRequestList) {
            String site = scrapRequest.getSite();
            ShopOrder retrievedShopOrder = retrieveShopOrder(scrapRequest.getSite(), scrapRequest.getObject());
            if (retrievedShopOrder != null && retrievedShopOrder.getShopOrder() != null) {
                shopOrder = shopOrder + "," + scrapRequest.getObject();
                continue;
            }
            String pcuBO = BOConverter.retrievePcuBO(scrapRequest.getSite(),scrapRequest.getObject());
            String pcu = scrapRequest.getObject();

            Response unDeletePcuHeader = unDeletePcuHeaderByPcuBO(site,pcuBO);
            Boolean unDeleteBomHeader = unDeleteBomHeader(site,pcuBO);
            MessageModel unDeletePcuRouterHeader = unDeletePcuRouterHeader(site,pcuBO);
            Boolean unDeletePcuInWork = unDeletePcuInWork(site,pcu);
            Boolean unDeletePcuInQueue = unDeletePcuInQueue(site,pcu);
            Boolean unDeletePcuDone = unDeletePcuDone(site,pcu);
            Boolean unDeletePcuComplete = unDeletePcuComplete(site,pcu);
            pcusList.add(scrapRequest.getObject());
                Scrap retrievedScrap = scrapRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);
                String eventType = getFormattedMessage(14);
                String eventData = getFormattedMessage(15);
                if (retrievedScrap != null) {
                    retrievedScrap.setActive(0);
                    scrapRepository.save(retrievedScrap);
                }
                scrapRequest.setEventType(eventType);
                scrapRequest.setEventData(" " + eventData);
                scrapRequest.setPcu(retrievedScrap.getPcuBO());
                scrapRequest.setStatus(retrievedScrap.getStatus());
                scrapRequest.setOperation(retrievedScrap.getOperationBO());
                scrapRequest.setResource(retrievedScrap.getResourceBO());
                scrapRequest.setShopOrder(retrievedScrap.getShopOrderBO());
                scrapRequest.setProcessLot(retrievedScrap.getProcessLot());
                scrapRequest.setItemBO(retrievedScrap.getItemBO());
                scrapRequest.setRoutingBO(retrievedScrap.getRoutingBO());
                scrapRequest.setBomBO(retrievedScrap.getBomBO());
                scrapRequest.setScrapQty(String.valueOf(retrievedScrap.getScrapQty()));
                Boolean productionLogged = productionLog(scrapRequest);
        }
        pcus = String.join(",",pcusList);
        if(!shopOrder.isEmpty() && pcus.isEmpty())
        {
            return ScrapMessageModel.builder().message_details(new MessageDetails("Pcu "+shopOrder+" "+"not found." , "E")).build();
        }
        String createdMessage = getFormattedMessage(13);
        return ScrapMessageModel.builder().message_details(new MessageDetails(pcus+" "+createdMessage , "S")).build();
    }



    public Boolean productionLog(ScrapRequest scrapRequest){
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType(scrapRequest.getEventType())
                .userId(scrapRequest.getUserBO())
                .pcu(scrapRequest.getPcu())
                .batchNo(scrapRequest.getPcu())
                .orderNumber(scrapRequest.getShopOrder())
                .shopOrderBO(scrapRequest.getShopOrder())
                .operation_bo(scrapRequest.getOperation())
                .routerBO(scrapRequest.getRoutingBO())
                .resourceId(scrapRequest.getResource())
                .itemBO(scrapRequest.getItemBO())
                //.qty(scrapRequest.getScrapQty())
                .site(scrapRequest.getSite())
                .topic("production-log")
                .status("Active")
                .eventData(scrapRequest.getPcu()+" "+scrapRequest.getEventData())
                .build();
        eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));
        return true;
    }
    public PcuHeader retrievePcuHeaderByPcuBO(String site, String pcuBO)
    {
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder().site(site).pcuBO(pcuBO).build();
        PcuHeader retrievePcuHeader = webClientBuilder.build()
                .post()
                .uri(retrievePcuHeaderUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();
        return retrievePcuHeader;
    }

    public Response deletePcuHeaderByPcuBO(String site, String pcuBO)
    {
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder().site(site).pcuBO(pcuBO).build();
        Response deletePcuHeader =  webClientBuilder.build()
                .post()
                .uri(deletePcuHeaderUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(Response.class)
                .block();
        return deletePcuHeader;
    }

    public MessageModel deletePcuRouterHeader(String site, String pcuBO )
    {
        PcuRouterHeaderRequest pcuRouterHeaderRequest = PcuRouterHeaderRequest.builder().site(site).pcuBo(pcuBO).build();
        MessageModel deletePcuRouterHeader =  webClientBuilder.build()
                .post()
                .uri(deletePcuRouterHeaderUrl)
                .bodyValue(pcuRouterHeaderRequest)
                .retrieve()
                .bodyToMono(MessageModel.class)
                .block();
        return deletePcuRouterHeader;
    }

    public Boolean deletePcuInWork(String site, String pcu )
    {
        StartRequest startRequest = StartRequest.builder().site(site).pcuBO(pcu).build();
        Boolean deleteStart =  webClientBuilder.build()
                .post()
                .uri(deletePcuInWorkUrl)
                .bodyValue(startRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return deleteStart;
    }

    public Boolean deletePcuInQueue(String site, String pcu )
    {
        PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder().site(site).pcu(pcu).build();
        Boolean deletePcuInQueue =  webClientBuilder.build()
                .post()
                .uri(deletePcuInQueueUrl)
                .bodyValue(pcuInQueueRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return deletePcuInQueue;
    }
    public Boolean deletePcuDone(String site, String pcu)
    {
        PcuDoneRequest pcuDoneRequest = PcuDoneRequest.builder().site(site).pcu(pcu).build();
        Boolean deletePcuDone =  webClientBuilder.build()
                .post()
                .uri(deletePcuDoneUrl)
                .bodyValue(pcuDoneRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return deletePcuDone;
    }
    public Boolean deletePcuComplete(String site, String pcu)
    {
        PcuCompleteRequest pcuCompleteRequest = PcuCompleteRequest.builder().site(site).pcu(pcu).build();
        Boolean deletePcuComplete =  webClientBuilder.build()
                .post()
                .uri(deletePcuCompleteUrl)
                .bodyValue(pcuCompleteRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return deletePcuComplete;
    }

    public Response unDeletePcuHeaderByPcuBO(String site, String pcuBO)
    {
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder().site(site).pcuBO(pcuBO).build();
        Response undeletePcuHeader =  webClientBuilder.build()
                .post()
                .uri(unDeletePcuHeaderUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(Response.class)
                .block();
        return undeletePcuHeader;
    }

    public MessageModel unDeletePcuRouterHeader(String site, String pcuBO )
    {
        PcuRouterHeaderRequest pcuRouterHeaderRequest = PcuRouterHeaderRequest.builder().site(site).pcuBo(pcuBO).build();
        return webClientBuilder.build()
                .post()
                .uri(unDeletePcuRouterHeaderUrl)
                .bodyValue(pcuRouterHeaderRequest)
                .retrieve()
                .bodyToMono(MessageModel.class)
                .block();
    }

    public Boolean unDeletePcuInWork(String site, String pcu)
    {
        InWorkDetails inWorkDetails = InWorkDetails.builder().site(site).pcu(pcu).build();
        return webClientBuilder.build()
                .post()
                .uri(unDeletePcuInWorkUrl)
                .bodyValue(inWorkDetails)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    public Boolean unDeletePcuInQueue(String site, String pcu)
    {
        PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder().site(site).pcu(pcu).build();
        return webClientBuilder.build()
                .post()
                .uri(unDeletePcuInQueueUrl)
                .bodyValue(pcuInQueueRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
    public Boolean unDeletePcuDone(String site, String pcu)
    {
        PcuDoneRequest pcuDoneRequest = PcuDoneRequest.builder().site(site).pcu(pcu).build();
        return webClientBuilder.build()
                .post()
                .uri(unDeletePcuDoneUrl)
                .bodyValue(pcuDoneRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
    public Boolean unDeletePcuComplete(String site, String pcu)
    {
        PcuCompleteRequest pcuCompleteRequest = PcuCompleteRequest.builder().site(site).pcu(pcu).build();
        return webClientBuilder.build()
                .post()
                .uri(unDeletePcuCompleteUrl)
                .bodyValue(pcuCompleteRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    public Boolean deleteBomHeader(String site, String pcuBO )
    {
        BomHeaderRequest bomHeaderRequest = BomHeaderRequest.builder().site(site).pcuBO(pcuBO).build();
        Boolean unDeletePcuComplete = webClientBuilder.build()
                .post()
                .uri(deleteBomHeaderUrl)
                .bodyValue(bomHeaderRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return unDeletePcuComplete;
    }

    public Boolean unDeleteBomHeader(String site, String pcuBO )
    {
        BomHeaderRequest bomHeaderRequest = BomHeaderRequest.builder().site(site).pcuBO(pcuBO).build();
        Boolean unDeletePcuComplete = webClientBuilder.build()
                .post()
                .uri(unDeleteBomHeaderUrl)
                .bodyValue(bomHeaderRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return unDeletePcuComplete;
    }
    @Override
    public List<RetrieveResponse> retrieveByPcuShopOrder(String site, List<String> objectList)
    {
        List<String> pcuList = new ArrayList<>();
        List<RetrieveResponse> retrieveResponseList= new ArrayList<>();
        for(String object : objectList) {
            List<String> pcuFromShopOrder = new ArrayList<>();
            ShopOrder retrievedShopOrder = retrieveShopOrder(site, object);
            if(retrievedShopOrder != null && retrievedShopOrder.getShopOrder()!=null)
            {
                pcuFromShopOrder = retrievedShopOrder.getSerialPcu().stream().map(SerialPcu::getPcuNumber).collect(Collectors.toList());
            }else{
                pcuList.add(object);
            }
            pcuList.addAll(pcuFromShopOrder);
        }
        retrieveResponseList = getTheRetrieveResponse(site,pcuList);
        return retrieveResponseList;
    }

    public List<RetrieveResponse> getTheRetrieveResponse(String site, List<String> pcuList)
    {
        List<RetrieveResponse> retrieveResponseList= new ArrayList<>();
        if(pcuList != null && !pcuList.isEmpty()) {
            for (String pcu : pcuList) {
                InWorkDetails pcuInWorkRecord = null;
                PcuInQueueDetails pcuInQueueRecord = null;
                List<PcuInQueueDetails> pcuInQueueList = retrievePcuInQueue(site, pcu);
                List<InWorkDetails> pcuInWorkList = retrievePcuInWork(site, pcu);
                if(pcuInQueueList!=null && !pcuInQueueList.isEmpty())
                {
                    pcuInQueueRecord = pcuInQueueList.get(0);
                }
                if(pcuInWorkList!=null && !pcuInWorkList.isEmpty())
                {
                    pcuInWorkRecord = pcuInWorkList.get(0);
                }
                if(pcuInWorkRecord != null && pcuInWorkRecord.getPcu()!=null) {
                    RetrieveResponse retrieveResponse = RetrieveResponse.builder()
                            .pcu(pcuInWorkRecord.getPcu())
                            .item(pcuInWorkRecord.getItem())
                            .itemVersion(pcuInWorkRecord.getItemVersion())
                            .router(pcuInWorkRecord.getRouter())
                            .routerVersion(pcuInWorkRecord.getRouterVersion())
                            .operation(pcuInWorkRecord.getOperation())
                            .operationVersion(pcuInWorkRecord.getOperationVersion())
                            .resource(pcuInWorkRecord.getResource())
                            .shopOrder(pcuInWorkRecord.getShopOrder())
                            .status("Active")
                            .build();
                    retrieveResponseList.add(retrieveResponse);
                }
                if(pcuInQueueRecord != null && pcuInQueueRecord.getPcu() != null && pcuInWorkRecord == null ) {
                    RetrieveResponse retrieveResponse = RetrieveResponse.builder()
                            .pcu(pcuInQueueRecord.getPcu())
                            .item(pcuInQueueRecord.getItem())
                            .itemVersion(pcuInQueueRecord.getItemVersion())
                            .router(pcuInQueueRecord.getRouter())
                            .routerVersion(pcuInQueueRecord.getRouterVersion())
                            .operation(pcuInQueueRecord.getOperation())
                            .operationVersion(pcuInQueueRecord.getOperationVersion())
                            .resource(pcuInQueueRecord.getResource())
                            .shopOrder(pcuInQueueRecord.getShopOrder())
                            .status("In Queue")
                            .build();
                    retrieveResponseList.add(retrieveResponse);
                }
            }
        }
        return retrieveResponseList;
    }


    public List<RetrieveResponse> getTheRetrieveResponseForUnscrap(String site, List<String> pcuList)
    {
        List<RetrieveResponse> retrieveResponseList= new ArrayList<>();
        if(pcuList != null && !pcuList.isEmpty()) {
            for (String pcu : pcuList) {
                InWorkDetails pcuInWorkRecord = null;
                PcuInQueueDetails pcuInQueueRecord = null;
                List<PcuInQueueDetails> pcuInQueueList = retrieveDeletedPcuInQueue(site, pcu);
                List<InWorkDetails> pcuInWorkList = retrieveDeletedPcuInWork(site, pcu);
                if(pcuInQueueList!=null && !pcuInQueueList.isEmpty())
                {
                    pcuInQueueRecord = pcuInQueueList.get(0);
                }
                if(pcuInWorkList!=null && !pcuInWorkList.isEmpty())
                {
                    pcuInWorkRecord = pcuInWorkList.get(0);
                }
                if(pcuInWorkRecord != null && pcuInWorkRecord.getPcu()!=null) {
                    RetrieveResponse retrieveResponse = RetrieveResponse.builder()
                            .pcu(pcuInWorkRecord.getPcu())
                            .item(pcuInWorkRecord.getItem())
                            .itemVersion(pcuInWorkRecord.getItemVersion())
                            .router(pcuInWorkRecord.getRouter())
                            .routerVersion(pcuInWorkRecord.getRouterVersion())
                            .operation(pcuInWorkRecord.getOperation())
                            .operationVersion(pcuInWorkRecord.getOperationVersion())
                            .resource(pcuInWorkRecord.getResource())
                            .shopOrder(pcuInWorkRecord.getShopOrder())
                            .status("Active")
                            .build();
                    retrieveResponseList.add(retrieveResponse);
                }
                if(pcuInQueueRecord != null && pcuInQueueRecord.getPcu() != null && pcuInWorkRecord == null ) {
                    RetrieveResponse retrieveResponse = RetrieveResponse.builder()
                            .pcu(pcuInQueueRecord.getPcu())
                            .item(pcuInQueueRecord.getItem())
                            .itemVersion(pcuInQueueRecord.getItemVersion())
                            .router(pcuInQueueRecord.getRouter())
                            .routerVersion(pcuInQueueRecord.getRouterVersion())
                            .operation(pcuInQueueRecord.getOperation())
                            .operationVersion(pcuInQueueRecord.getOperationVersion())
                            .resource(pcuInQueueRecord.getResource())
                            .shopOrder(pcuInQueueRecord.getShopOrder())
                            .status("In Queue")
                            .build();
                    retrieveResponseList.add(retrieveResponse);
                }
            }
        }
        return retrieveResponseList;
    }

    public List<InWorkDetails> retrievePcuInWork(String site,String pcu)
    {
        InWorkDetails inWorkDetails = InWorkDetails.builder().site(site).pcu(pcu).build();
        List<InWorkDetails> retrievedList = webClientBuilder.build()
                .post()
                .uri(retrievePcuInWorkUrl)
                .bodyValue(inWorkDetails)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<InWorkDetails>>() {
                })
                .block();
        return retrievedList;
    }

    public List<InWorkDetails> retrieveDeletedPcuInWork(String site,String pcu)
    {
        InWorkDetails startRequest = InWorkDetails.builder().site(site).pcu(pcu).build();
        return webClientBuilder.build()
                .post()
                .uri(retrieveDeletedPcuInWorkUrl)
                .bodyValue(startRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<InWorkDetails>>() {
                })
                .block();
    }

    @Override
    public List<RetrieveResponse> retrieveByPcuShopOrderForUnScrap(String site, List<String> objectList)
    {
        List<String> pcuList = new ArrayList<>();
        List<RetrieveResponse> retrieveResponseList= new ArrayList<>();
        for(String object : objectList) {
            List<String> pcuFromShopOrder = new ArrayList<>();
            ShopOrder retrievedShopOrder = retrieveShopOrder(site, object);
            if(retrievedShopOrder != null && retrievedShopOrder.getShopOrder()!=null)
            {
                pcuFromShopOrder = retrievedShopOrder.getSerialPcu().stream().map(SerialPcu::getPcuNumber).collect(Collectors.toList());
            }else{
                pcuList.add(object);
            }
            pcuList.addAll(pcuFromShopOrder);
        }
        retrieveResponseList = getTheRetrieveResponseForUnscrap(site,pcuList);
        return retrieveResponseList;
    }

    public List<PcuInQueueDetails> retrievePcuInQueue (String site, String pcu)
    {
        PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder().site(site).pcu(pcu).build();
        List<PcuInQueueDetails> retrievedList = webClientBuilder.build()
                .post()
                .uri(retrievePcuInQueueUrl)
                .bodyValue(pcuInQueueRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PcuInQueueDetails>>() {
                })
                .block();
        return retrievedList;
    }
    public List<PcuInQueueDetails> retrieveDeletedPcuInQueue (String site, String pcu)
    {
        PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder().site(site).pcu(pcu).build();
        return webClientBuilder.build()
                .post()
                .uri(retrieveDeletedPcuInQueueUrl)
                .bodyValue(pcuInQueueRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PcuInQueueDetails>>() {
                })
                .block();
    }

    public ShopOrder retrieveShopOrder(String site,String shopOrder)
    {
        ShopOrderRequest shopOrderRequest = ShopOrderRequest.builder().site(site).shopOrder(shopOrder).build();
        ShopOrder isShopOrder = webClientBuilder.build()
                .post()
                .uri(retrieveShopOrder)
                .bodyValue(shopOrderRequest)
                .retrieve()
                .bodyToMono(ShopOrder.class)
                .block();
        return isShopOrder;
    }
    @Override
    public List<Scrap> retrieveAllScrap(String site)
    {
       List<Scrap> scrapList = scrapRepository.findByActiveAndSite(1,site);
       return scrapList;
    }
    public ProductionLog retrieveProductionLog(String pcuBO, String shopOrderBO, String operationBO)
    {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("BATCH_START")
                .pcu(pcuBO)
                .shopOrderBO(shopOrderBO)
                .operation(BOConverter.getOperation(operationBO))
                .operationVersion(BOConverter.getOperationVersion(operationBO))
                .build();
        ProductionLog retrievedRecord = webClientBuilder.build()
                .post()
                .uri(retrieveProductionLogUrl)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(ProductionLog.class)
                .block();
        return retrievedRecord;
    }

    public Boolean productionLog(ScrapRequest pcuCompleteRequest, String message){

        ProductionLog productionLogRecord = retrieveProductionLog(pcuCompleteRequest.getPcu(),pcuCompleteRequest.getShopOrder(),pcuCompleteRequest.getOperation());
        long minutesDifference = 0;
        ActualCycleSum retrieveAllSignOffRecord = retrieveAllSignOffLog(pcuCompleteRequest.getPcu(),pcuCompleteRequest.getShopOrder(),pcuCompleteRequest.getOperation());
        if(retrieveAllSignOffRecord !=null)
        {

            minutesDifference =retrieveAllSignOffRecord.getTotalActualCycleTime();
        }
        if(productionLogRecord != null)
        {
            minutesDifference =minutesDifference + Duration.between(productionLogRecord.getCreated_datetime(),LocalDateTime.now()).toSeconds();
        }

        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("completeSfcBatch")
                .userId(pcuCompleteRequest.getUserBO())
                .pcu(pcuCompleteRequest.getPcu())
                .shopOrderBO(pcuCompleteRequest.getShopOrder())
                .operation_bo(pcuCompleteRequest.getOperation())
                .routerBO(pcuCompleteRequest.getRoutingBO())
                .itemBO(pcuCompleteRequest.getItemBO())
                .resourceId(pcuCompleteRequest.getResource())
                .site(pcuCompleteRequest.getSite())
                .qty(Integer.parseInt(pcuCompleteRequest.getScrapQty()))
                .actualCycleTime(Double.valueOf(minutesDifference))
                .topic("production-log")
                .status("Active")
                .eventData(message)
                .build();
        eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));
        return true;
    }

    public ActualCycleSum retrieveAllSignOffLog(String pcuBO,String shopOrderBO,String operationBO)
    {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("BATCH_SIGNOFF")
                .pcu(pcuBO)
                .shopOrderBO(shopOrderBO)
                .operation_bo(operationBO)
                .build();
        ActualCycleSum retrievedRecord = webClientBuilder.build()
                .post()
                .uri(retrieveAllSignOffLogUrl)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(ActualCycleSum.class)
                .block();
        return retrievedRecord;
    }

}
