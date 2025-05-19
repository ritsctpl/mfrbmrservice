package com.rits.pcustepstatus.service;

import com.rits.pcuheaderservice.model.RouterList;
import com.rits.pcustepstatus.dto.PcuHeader;
import com.rits.pcustepstatus.dto.*;
import com.rits.pcustepstatus.exception.PcuStepStatusException;
import com.rits.pcustepstatus.model.MessageDetails;
import com.rits.pcustepstatus.model.MessageModel;
import com.rits.pcustepstatus.model.PcuStepStatus;
import com.rits.pcustepstatus.model.PcuStepStatusDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PcuStepStatusServiceImpl implements PcuStepStatusService {
    private final WebClient.Builder webClientBuilder;

    @Value("${pcuheader-service.url}/readPcu")
    private String retrieveByPcu;
    @Value("${pcuheader-service.url}/retrieveByShopOrder")
    private String retrieveByShopOrder;
    @Value("${routing-service.url}/retrieve")
    private String retrieveRouting;
    @Value("${pcuinqueue-service.url}/retrieve")
    private String retrievePcuInQueue;
    @Value("${start-service.url}/retrieve")
    private String retrievePcuInWork;
    @Value("${pcucomplete-service.url}/retrieve")
    private String retrievePcuComplete;
    @Value("${pcucomplete-service.url}/retrieveByOperationAndShopOrder")
    private String retrievePcuCompleteByOperation;
    @Value("${start-service.url}/retrieveByOperation")
    private String retrievePcuInWorkByOperation;
    @Value("${pcuinqueue-service.url}/retrieveListOfPcuBOByOperationAndShopOrder")
    private String retrievePcuInQueueByOperation;
    @Value("${pcuinqueue-service.url}/delete")
    private String deletePcuInQueue;
    @Value("${start-service.url}/delete")
    private String deletePcuInWork;
    @Value("${pcucomplete-service.url}/delete")
    private String deletePcuComplete;
    @Value("${pcuinqueue-service.url}/create")
    private String createPcuInQueue;
    @Value("${start-service.url}/start")
    private String startPcu;
    @Value("${pcucomplete-service.url}/start")
    private String pcuComplete;


    private List<PcuStepStatus> pcuStepStatusLists;

    @Override
    public List<PcuStepStatus> retrieveByPcuShopOrderProcessLot(PcuStepStatusRequest pcuStepStatusRequest) throws Exception {
        String routing = "";
        String version = "";
        String item = "";
        String itemVersion = "";
        String shopOrder = "";
        boolean isShopOrderBO = false;
        String routingDescription = "";
        List<RouterList> routerLists = null;
        String shoporder="";
        String items="";
        List<PcuStepStatus> pcuStepStatusList = new ArrayList<>();

        for (String object : pcuStepStatusRequest.getObject()) {

            PcuHeader retrieveByPcu = retrievePcuHeaderByPcu(pcuStepStatusRequest.getSite(), "PcuBO:" + pcuStepStatusRequest.getSite() + "," + object);
            List<PcuHeader> retrieveByShopOrder = retrievePcuHeaderByShopOrder(pcuStepStatusRequest.getSite(), "ShopOrderBO:" + pcuStepStatusRequest.getSite() + "," + object);
//            if ((retrieveByPcu != null && retrieveByPcu.getPcuBO() != null && !retrieveByPcu.getPcuBO().isEmpty()) || (retrieveByShopOrder != null && !retrieveByShopOrder.isEmpty())) {
//                PcuRouterHeader retrievedPcuRouterHeader = retrievePcuRouterHeader(pcuStepStatusRequest.getSite(), object);
                if(retrieveByPcu != null && retrieveByPcu.getPcuBO() != null && !retrieveByPcu.getPcuBO().isEmpty())
                {
                    routerLists = retrieveByPcu.getRouterList();
                    shoporder=retrieveByPcu.getShopOrderBO();
                    items=retrieveByPcu.getItemBO();
                }
                if(retrieveByShopOrder != null && !retrieveByShopOrder.isEmpty())
                {
                    routerLists = retrieveByShopOrder.get(0).getRouterList();
                    shoporder =retrieveByShopOrder.get(0).getShopOrderBO();
                    items=retrieveByShopOrder.get(0).getItemBO();
                    isShopOrderBO=true;
                }
                if(pcuStepStatusRequest.getRoutingBO()!=null && !pcuStepStatusRequest.getRoutingBO().isEmpty()){
                    String[] routingBO = pcuStepStatusRequest.getRoutingBO().split(",");
                    routing = routingBO[1];
                    version = routingBO[2];
                }else if(routerLists!=null) {
                    String[] routingBO = routerLists.get(0).getPcuRouterBO().split(",");
                    if (routingBO.length == 3) {
                        routing = routingBO[1];
                        version = routingBO[2];
                    }
                }
                if(!items.isEmpty()) {
                    String[] itemBO = items.split(",");
                    if (itemBO.length == 2) {
                        item = itemBO[0];
                        itemVersion = itemBO[1];
                    }
                }
                if(!shoporder.isEmpty()) {
                    String[] shopOrderBO = shoporder.split(",");
                    if (shopOrderBO.length == 2) {
                        shopOrder = shopOrderBO[1];
                    }
                }
                Routing routingRecord = retrieveRouting(pcuStepStatusRequest.getSite(), version, routing);
                if (routingRecord != null && routingRecord.getRouting() != null && !routingRecord.getRouting().isEmpty()) {
                    routingDescription = routingRecord.getDescription();
                    SetPcuStepStatusRequest setPcuStepStatusRequest = SetPcuStepStatusRequest.builder()
                            .routingBO(routing + "/" + version)
                            .routingDescription(routingRecord.getDescription())
                            .pcu(object)
                            .site(pcuStepStatusRequest.getSite())
                            .pcuStepStatus("")
                            .shopOrder(shopOrder)
                            .itemBO(item + "/" + itemVersion)
                            .routingStepList(routingRecord.getRoutingStepList())
                            .resource("")
                            .user(pcuStepStatusRequest.getUser())
                            .holdId("")
                            .build();
                    pcuStepStatusList.addAll(getOperationDetailsByPcu(setPcuStepStatusRequest));
                }else{
                    throw new PcuStepStatusException(3228,routing);
                }
//            }
//            else if (retrieveByShopOrder!=null && !retrieveByShopOrder.isEmpty()) {
//
////                for(PcuHeader pcuHeader : retrieveByShopOrder)
//                PcuHeader pcuHeader = retrieveByShopOrder.get(0);
////                {
//                    String [] routingBO = pcuHeader.getRouterList().get(0).getPcuRouterBO().split(",");
//                    if(routingBO.length==3)
//                    {
//                        routing = routingBO[1];
//                        version = routingBO[2];
//                    }
//                    String [] itemBO = pcuHeader.getItemBO().split(",");
//                    if(itemBO.length==2)
//                    {
//                        item = itemBO[0];
//                        itemVersion = itemBO[1];
//                    }
//                    String [] shopOrderBO = pcuHeader.getShopOrderBO().split(",");
//                    if(shopOrderBO.length==2)
//                    {
//                        shopOrder = shopOrderBO[1];
//                    }
//                    Routing routingRecord = retrieveRouting(pcuStepStatusRequest.getSite(),version,routing);
//                    if(routingRecord != null && !routingRecord.getRouting().isEmpty() && routingRecord.getRouting() != null)
//                    {
//                        routingDescription = routingRecord.getDescription();
//                        SetPcuStepStatusRequest setPcuStepStatusRequest = SetPcuStepStatusRequest.builder()
//                                .routingBO(routing+"/"+version)
//                                .routingDescription(routingRecord.getDescription())
//                                .pcu(object)
//                                .site(pcuStepStatusRequest.getSite())
//                                .pcuStepStatus("active")
//                                .shopOrder(shopOrder)
//                                .itemBO(item+"/"+itemVersion)
//                                .routingStepList(routingRecord.getRoutingStepList())
//                                .resource("")
//                                .user(pcuStepStatusRequest.getUser())
//                                .holdId("")
//                                .build();
//                        if(retrieveByPcu == null && retrieveByShopOrder != null && !retrieveByShopOrder.isEmpty())
//                        {
//                            setPcuStepStatusRequest.setShopOrderBO(true);
//                            isShopOrderBO=true;
//                        }
//                        pcuStepStatusList.addAll(getOperationDetailsByPcu(setPcuStepStatusRequest));
//
//                    }
////                }
//            }


            if (pcuStepStatusList != null && !pcuStepStatusList.isEmpty()) {
                String operation = "";
                String operationVersion = "";
                for (PcuStepStatus pcuStepStatus : pcuStepStatusList) {
                    Integer qtyInQueue = 0;
                    Integer qtyInWork = 0;
                    Integer qtyCompletePending = 0;
                    String[] operationBO = pcuStepStatus.getOperation().split("/");
                    if (operationBO.length == 2) {
                        operation = operationBO[0];
                        operationVersion = operationBO[1];
                    }
                    SetPcuStepStatusRequest setPcuStepStatusRequest = SetPcuStepStatusRequest.builder()
                            .site(pcuStepStatusRequest.getSite())
                            .operation(operation)
                            .operationVersion(operationVersion)
                            .shopOrderBO(isShopOrderBO)
                            .routingBO(routing + "/" + version)
                            .routingDescription(routingDescription)
                            .pcu(object)
                            .site(pcuStepStatusRequest.getSite())
                            .pcuStepStatus("")
                            .shopOrder(shopOrder)
                            .itemBO(item + "/" + itemVersion)
                            .resource("")
                            .user(pcuStepStatusRequest.getUser())
                            .holdId("")
                            .build();

                    if (isShopOrderBO) {
                        List<PcuStepStatusDetails> detailsList = setPcuStepStatusByShopOrder(setPcuStepStatusRequest);
                        if (detailsList != null) {
                            pcuStepStatus.getPcuStepStatusDetailsList().addAll(detailsList);
                            if(pcuStepStatus.getPcuStepStatusDetailsList()!=null) {
                                for (PcuStepStatusDetails pcuStepStatusDetails : pcuStepStatus.getPcuStepStatusDetailsList()) {
                                    Integer qtyInQueueObject = pcuStepStatusDetails.getQtyInQueue();
                                    if (qtyInQueueObject != null) {
                                        qtyInQueue = qtyInQueue + qtyInQueueObject.intValue();
                                    }
                                    Integer qtyInWorkObject = pcuStepStatusDetails.getQtyInWork();
                                    if (qtyInWorkObject != null) {
                                        qtyInWork = qtyInWork + qtyInWorkObject.intValue();
                                    }
                                    Integer qtyCompletePendingObject = pcuStepStatusDetails.getQtyCompletePending();
                                    if (qtyCompletePendingObject != null) {
                                        qtyCompletePending = qtyCompletePending + qtyCompletePendingObject.intValue();
                                    }
                                }
                            }
                        }
                    } else {
                        PcuStepStatusDetails detailsList = setPcuStepStatusByPcu(setPcuStepStatusRequest);
                        if (detailsList != null) {
                            pcuStepStatus.getPcuStepStatusDetailsList().add(detailsList);
                            if (pcuStepStatus.getPcuStepStatusDetailsList() != null) {

                                for (PcuStepStatusDetails pcuStepStatusDetails : pcuStepStatus.getPcuStepStatusDetailsList()) {
                                    Integer qtyInQueueObject = pcuStepStatusDetails.getQtyInQueue();
                                    if (qtyInQueueObject != null) {
                                        qtyInQueue = qtyInQueue + qtyInQueueObject.intValue();
                                    }
                                    Integer qtyInWorkObject = pcuStepStatusDetails.getQtyInWork();
                                    if (qtyInWorkObject != null) {
                                        qtyInWork = qtyInWork + qtyInWorkObject.intValue();
                                    }
                                    Integer qtyCompletePendingObject = pcuStepStatusDetails.getQtyCompletePending();
                                    if (qtyCompletePendingObject != null) {
                                        qtyCompletePending = qtyCompletePending + qtyCompletePendingObject.intValue();
                                    }
                                }
                            }
                        }
                    }

                    pcuStepStatus.setQtyInQueue(qtyInQueue);
                    pcuStepStatus.setQtyInWork(qtyInWork);
                    pcuStepStatus.setQtyCompletePending(qtyCompletePending);
                    if(qtyCompletePending==0 && qtyInWork==0 && qtyInQueue==0)
                    {
                        pcuStepStatus.setStepStatus("Completed");
                    }
                    if(qtyInWork>0)
                    {
                        pcuStepStatus.setStepStatus("Active");
                    }
                    if(qtyInQueue>0 && qtyInWork==0)
                    {
                        pcuStepStatus.setStepStatus("InQueue");
                    }
                }
            }

        }
        pcuStepStatusLists = pcuStepStatusList;

        List<PcuStepStatus> filteredList = pcuStepStatusList.stream()
                .filter(pcuStepStatus -> pcuStepStatus.getPcuStepStatusDetailsList() != null && !pcuStepStatus.getPcuStepStatusDetailsList().isEmpty())
                .collect(Collectors.toList());
        pcuStepStatusList.clear();
        pcuStepStatusList.addAll(filteredList);
        return pcuStepStatusList;
    }

    @Override
    public List<PcuStepStatus> getOperationDetailsByPcu(SetPcuStepStatusRequest setPcuStepStatusRequest) throws Exception {
        List<PcuStepStatus> pcuStepStatusList = new ArrayList<>();

        for (RoutingStep routingStep : setPcuStepStatusRequest.getRoutingStepList()) {
            List<PcuStepStatusDetails> listPcu = new ArrayList<>();
//            PcuStepStatusDetails stepStatusDetails;
            if (routingStep.getStepType().equalsIgnoreCase("operation")) {
                setPcuStepStatusRequest.setOperation(routingStep.getOperation());
                setPcuStepStatusRequest.setOperationVersion(routingStep.getOperationVersion());
//                if(setPcuStepStatusRequest.isShopOrderBO())
//                {
//                    stepStatusDetails = setPcuStepStatusByShopOrder(setPcuStepStatusRequest);
//                }
//                else {
//                    stepStatusDetails = setPcuStepStatusByPcu(setPcuStepStatusRequest);
//                }
//             if(stepStatusDetails !=null && !stepStatusDetails.getPcu().isEmpty() && stepStatusDetails.getPcu() !=null) {
//
//                    for(PcuStepStatusDetails pcuStepStatusDetails : listPcu)
//                    {
//                        qtyInQueue = qtyInQueue + pcuStepStatusDetails.getQtyInQueue();
//                        qtyInWork = qtyInWork + pcuStepStatusDetails.getQtyInWork();
//                        qtyCompletePending = qtyCompletePending + pcuStepStatusDetails.getQtyCompletePending();
//                    }
                PcuStepStatus pcuStepStatus = PcuStepStatus.builder()
                        .stepId(Integer.parseInt(routingStep.getStepId()))
                        .operation(routingStep.getOperation() + "/" + routingStep.getOperationVersion())
                        .description(routingStep.getOperationDescription())
                        .stepStatus("Active")
                        .qtyInQueue(0)
                        .qtyInWork(0)
                        .qtyCompletePending(0)
                        .pcuStepStatusDetailsList(listPcu)
                        .build();
                pcuStepStatusList.add(pcuStepStatus);
//             }
            } else if (routingStep.getStepType().equalsIgnoreCase("routing")) {
                setPcuStepStatusRequest.setRoutingStepList(routingStep.getRouterDetails().get(0).getRoutingStepList());
                getOperationDetailsByPcu(setPcuStepStatusRequest);
            }
        }
        return pcuStepStatusList;
    }

    @Override
    public PcuStepStatusDetails setPcuStepStatusByPcu(SetPcuStepStatusRequest setPcuStepStatusRequest) throws Exception {
        PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder().site(setPcuStepStatusRequest.getSite()).operationBO("OperationBO:" + setPcuStepStatusRequest.getSite() + "," + setPcuStepStatusRequest.getOperation() + "," + setPcuStepStatusRequest.getOperationVersion()).pcuBO("PcuBO:" + setPcuStepStatusRequest.getSite() + "," + setPcuStepStatusRequest.getPcu()).build();
        PcuInQueue pcuInQueueResponse = webClientBuilder.build()
                .post()
                .uri(retrievePcuInQueue)
                .bodyValue(pcuInQueueRequest)
                .retrieve()
                .bodyToMono(PcuInQueue.class)
                .block();

        StartRequest pcuInWorkRequest = StartRequest.builder().site(setPcuStepStatusRequest.getSite()).operationBO("OperationBO:" + setPcuStepStatusRequest.getSite() + "," + setPcuStepStatusRequest.getOperation() + "," + setPcuStepStatusRequest.getOperationVersion()).pcuBO("PcuBO:" + setPcuStepStatusRequest.getSite() + "," + setPcuStepStatusRequest.getPcu()).build();
        PcuInWork pcuInWorkResponse = webClientBuilder.build()
                .post()
                .uri(retrievePcuInWork)
                .bodyValue(pcuInWorkRequest)
                .retrieve()
                .bodyToMono(PcuInWork.class)
                .block();

        PcuCompleteRequest pcuCompleteRequest = PcuCompleteRequest.builder().site(setPcuStepStatusRequest.getSite()).operationBO("OperationBO:" + setPcuStepStatusRequest.getSite() + "," + setPcuStepStatusRequest.getOperation() + "," + setPcuStepStatusRequest.getOperationVersion()).pcuBO("PcuBO:" + setPcuStepStatusRequest.getSite() + "," + setPcuStepStatusRequest.getPcu()).build();
        PcuComplete pcuCompleteResponse = webClientBuilder.build()
                .post()
                .uri(retrievePcuComplete)
                .bodyValue(pcuCompleteRequest)
                .retrieve()
                .bodyToMono(PcuComplete.class)
                .block();

        Integer qtyInQueue = 0;
        Integer qtyInWork = 0;
        Integer qtyCompletePending = 0;
        String resourceBO=null;
        String itemBO=null;

        if (pcuInQueueResponse != null && pcuInQueueResponse.getQtyInQueue() != null && !pcuInQueueResponse.getQtyInQueue().isEmpty()) {
            qtyInQueue = Integer.parseInt(pcuInQueueResponse.getQtyInQueue());
            resourceBO=pcuInQueueResponse.getResourceBO();
            itemBO=pcuInQueueResponse.getItemBO();
        }
        if (pcuInWorkResponse != null && pcuInWorkResponse.getQtyInWork() != null && !pcuInWorkResponse.getQtyInWork().isEmpty()) {
            qtyInWork = Integer.parseInt(pcuInWorkResponse.getQtyInWork());
            resourceBO=pcuInWorkResponse.getResourceBO();
            itemBO=pcuInWorkResponse.getItemBO();
        }
        if (pcuCompleteResponse != null && pcuCompleteResponse.getQtyToComplete() != null && !pcuCompleteResponse.getQtyToComplete().isEmpty()) {
            qtyCompletePending = Integer.parseInt(pcuCompleteResponse.getQtyToComplete());
            resourceBO=pcuCompleteResponse.getResourceBO();
            itemBO=pcuCompleteResponse.getItemBO();
        }

        PcuStepStatusDetails pcuStepStatusDetails = PcuStepStatusDetails.builder()
                .qtyInQueue(qtyInQueue)
                .qtyInWork(qtyInWork)
                .qtyCompletePending(qtyCompletePending)
                .routingBO(setPcuStepStatusRequest.getRoutingBO())
                .routingDescription(setPcuStepStatusRequest.getRoutingDescription())
                .pcu(setPcuStepStatusRequest.getPcu())
                .pcuStepStatus(setPcuStepStatusRequest.getPcuStepStatus())
                .shopOrder(setPcuStepStatusRequest.getShopOrder())
                .itemBO(itemBO)
                .resourceBO(resourceBO)
                .user(setPcuStepStatusRequest.getUser())
                .holdId(setPcuStepStatusRequest.getHoldId())
                .build();
        if(qtyCompletePending==0 && qtyInWork==0 && qtyInQueue==0)
        {
            pcuStepStatusDetails.setPcuStepStatus("Completed");
        }
        if(qtyInWork>0)
        {
            pcuStepStatusDetails.setPcuStepStatus("Completed");
        }
        if(qtyInQueue>0 && qtyInWork==0)
        {
            pcuStepStatusDetails.setPcuStepStatus("Completed");
        }
        return pcuStepStatusDetails;
    }

    @Override
    public List<PcuInQueue> getPcuInQueue(String site, String operation, String operationVersion,String shopOrder) throws Exception {
        PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder().site(site).operationBO("OperationBO:" + site + "," + operation + "," + operationVersion).shopOrderBO("ShopOrderBO:"+site+","+shopOrder).build();
        List<PcuInQueue> pcuInQueueResponse = webClientBuilder.build()
                .post()
                .uri(retrievePcuInQueueByOperation)
                .bodyValue(pcuInQueueRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PcuInQueue>>() {
                })
                .block();
        return pcuInQueueResponse;
    }

    @Override
    public List<PcuInWork> getPcuInWork(String site, String operation, String operationVersion,String shopOrder) throws Exception {
        StartRequest pcuInWorkRequest = StartRequest.builder().site(site).operationBO("OperationBO:" + site + "," + operation + "," + operationVersion).shopOrderBO("ShopOrderBO:"+site+","+shopOrder).build();
        List<PcuInWork> pcuInWorkResponse = webClientBuilder.build()
                .post()
                .uri(retrievePcuInWorkByOperation)
                .bodyValue(pcuInWorkRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PcuInWork>>() {
                })
                .block();
        return pcuInWorkResponse;
    }

    @Override
    public List<PcuComplete> getPcuComplete(String site, String operation, String operationVersion,String shopOrder) throws Exception {

        PcuCompleteReq pcuCompleteRequest = PcuCompleteReq.builder()
                .site(site)
                .operation(operation + "/" + operationVersion)
                .shopOrder("ShopOrderBO:"+site+","+shopOrder)
                .build();

        List<PcuCompleteReq> pcuCompleteResponse = webClientBuilder.build()
                .post()
                .uri(retrievePcuCompleteByOperation)
                .bodyValue(pcuCompleteRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PcuCompleteReq>>() {
                })
                .block();

        List<PcuComplete> pcuCompleteList = new ArrayList<>();
        PcuComplete pcuComplete1 = null;
        if(pcuCompleteResponse!=null) {
            for (PcuCompleteReq pcuCompleteReq : pcuCompleteResponse) {
                pcuComplete1 = new PcuComplete();
                PcuComplete completeReqWithBO = pcuCompleteBOBuilder(pcuCompleteReq);
                completeReqWithBO.setSite(pcuCompleteReq.getSite());
                completeReqWithBO.setStepID(pcuCompleteReq.getStepID());
                completeReqWithBO.setQtyToComplete(pcuCompleteReq.getQtyToComplete());
                completeReqWithBO.setQtyCompleted(pcuCompleteReq.getQtyCompleted());
                completeReqWithBO.setParentStepID(pcuCompleteReq.getParentStepID());
                completeReqWithBO.setHandle(pcuCompleteReq.getHandle());
                completeReqWithBO.setDateTime(pcuCompleteReq.getDateTime());

                pcuCompleteList.add(completeReqWithBO);
            }
        }

        return pcuCompleteList;
    }
    public PcuComplete pcuCompleteBOBuilder(PcuCompleteReq pcuCompleteReq){

        PcuComplete completeReqWithBO = new PcuComplete();
        if(pcuCompleteReq.getPcu()!=null && !pcuCompleteReq.getPcu().isEmpty())
            completeReqWithBO.setPcuBO("PcuBO:"+pcuCompleteReq.getSite()+","+pcuCompleteReq.getPcu());

        if(pcuCompleteReq.getItem()!=null && !pcuCompleteReq.getItem().isEmpty()) {
            String[] itemArray = pcuCompleteReq.getItem().split("/");
            completeReqWithBO.setItemBO("ItemBO:" + pcuCompleteReq.getSite() + "," + itemArray[0] + "," + itemArray[1]);
        }

        if(pcuCompleteReq.getRouter()!=null && !pcuCompleteReq.getRouter().isEmpty()) {
            String[] routerArray = pcuCompleteReq.getRouter().split("/");
            completeReqWithBO.setResourceBO("RoutingBO:" + pcuCompleteReq.getSite() + "," + routerArray[0] + "," + routerArray[1]);
        }

        if(pcuCompleteReq.getOperation()!=null && !pcuCompleteReq.getOperation().isEmpty()) {
            String[] opArray = pcuCompleteReq.getOperation().split("/");
            completeReqWithBO.setOperationBO("OperationBO:" + pcuCompleteReq.getSite() + "," + opArray[0] + "," + opArray[1]);
        }

        if(pcuCompleteReq.getResource()!=null && !pcuCompleteReq.getResource().isEmpty())
            completeReqWithBO.setResourceBO("ResourceBO:"+pcuCompleteReq.getSite()+","+pcuCompleteReq.getResource());

        if(pcuCompleteReq.getUser()!=null && !pcuCompleteReq.getUser().isEmpty())
            completeReqWithBO.setUserBO("UserBO:"+pcuCompleteReq.getSite()+","+pcuCompleteReq.getUser());

        if(pcuCompleteReq.getShopOrder()!=null && !pcuCompleteReq.getShopOrder().isEmpty())
            completeReqWithBO.setShopOrderBO("ShopOrderBO:"+pcuCompleteReq.getSite()+","+pcuCompleteReq.getShopOrder());

        if(pcuCompleteReq.getChildRouter()!=null && !pcuCompleteReq.getChildRouter().isEmpty()) {
            String[] childRouter = pcuCompleteReq.getChildRouter().split("/");
            completeReqWithBO.setChildRouterBO("ChildRouterBO:" + pcuCompleteReq.getSite() + "," + childRouter[0] + "," + childRouter[1]);
        }

        return completeReqWithBO;
    }

//    @Override
//    public PcuStepStatusDetails setPcuStepStatusByShopOrder(SetPcuStepStatusRequest setPcuStepStatusRequest) throws Exception {
//
//        List<PcuInQueue> pcuInQueueResponse = getPcuInQueue(setPcuStepStatusRequest.getSite(), setPcuStepStatusRequest.getOperation(), setPcuStepStatusRequest.getOperationVersion());
//
//        List<PcuInWork> pcuInWorkResponse = getPcuInWork(setPcuStepStatusRequest.getSite(), setPcuStepStatusRequest.getOperation(), setPcuStepStatusRequest.getOperationVersion());
//
//        List<PcuComplete> pcuCompleteResponse = getPcuComplete(setPcuStepStatusRequest.getSite(), setPcuStepStatusRequest.getOperation(), setPcuStepStatusRequest.getOperationVersion());
//
//
//        Integer qtyInQueue = 0;
//        Integer qtyInWork = 0;
//        Integer qtyCompletePending = 0;
//
//        if (pcuInQueueResponse != null && !pcuInQueueResponse.isEmpty()) {
//            for (PcuInQueue pcuInQueue : pcuInQueueResponse) {
//                if (pcuInQueue.getQtyInQueue() != null && !pcuInQueue.getQtyInQueue().isEmpty()) {
//                    qtyInQueue = qtyInQueue + Integer.parseInt(pcuInQueue.getQtyInQueue());
//                }
//            }
//        }
//        if (pcuInWorkResponse != null && !pcuInWorkResponse.isEmpty()) {
//            for (PcuInWork pcuInWork : pcuInWorkResponse) {
//                if (pcuInWork.getQtyInWork() != null && !pcuInWork.getQtyInWork().isEmpty()) {
//                    qtyInWork = qtyInWork + Integer.parseInt(pcuInWork.getQtyInWork());
//                }
//            }
//        }
//        if (pcuCompleteResponse != null && !pcuCompleteResponse.isEmpty()) {
//            for (PcuComplete pcuComplete : pcuCompleteResponse) {
//                if (pcuComplete.getQtyToComplete() != null && !pcuComplete.getQtyToComplete().isEmpty()) {
//                    qtyCompletePending = qtyCompletePending + Integer.parseInt(pcuComplete.getQtyToComplete());
//                }
//            }
//        }
//        PcuStepStatusDetails pcuStepStatusDetails = PcuStepStatusDetails.builder()
//                .qtyInQueue(qtyInQueue)
//                .qtyInWork(qtyInWork)
//                .qtyCompletePending(qtyCompletePending)
//                .routingBO(setPcuStepStatusRequest.getRoutingBO())
//                .routingDescription(setPcuStepStatusRequest.getRoutingDescription())
//                .pcu(setPcuStepStatusRequest.getPcu())
//                .pcuStepStatus(setPcuStepStatusRequest.getPcuStepStatus())
//                .shopOrder(setPcuStepStatusRequest.getShopOrder())
//                .itemBO(setPcuStepStatusRequest.getItemBO())
//                .resource(setPcuStepStatusRequest.getResource())
//                .user(setPcuStepStatusRequest.getUser())
//                .holdId(setPcuStepStatusRequest.getHoldId())
//                .build();
//        return pcuStepStatusDetails;
//    }
    @Override
    public List<PcuStepStatusDetails> setPcuStepStatusByShopOrder(SetPcuStepStatusRequest setPcuStepStatusRequest) throws Exception {
        List<PcuInQueue> pcuInQueueResponse = getPcuInQueue(setPcuStepStatusRequest.getSite(), setPcuStepStatusRequest.getOperation(), setPcuStepStatusRequest.getOperationVersion(),setPcuStepStatusRequest.getShopOrder());
        List<PcuInWork> pcuInWorkResponse = getPcuInWork(setPcuStepStatusRequest.getSite(), setPcuStepStatusRequest.getOperation(), setPcuStepStatusRequest.getOperationVersion(), setPcuStepStatusRequest.getShopOrder());
        List<PcuComplete> pcuCompleteResponse = getPcuComplete(setPcuStepStatusRequest.getSite(), setPcuStepStatusRequest.getOperation(), setPcuStepStatusRequest.getOperationVersion(), setPcuStepStatusRequest.getShopOrder());

        // Create a map to store PCU names and their corresponding quantities
        Map<String, PcuStepStatusDetails> pcuMap = new HashMap<>();

        // Process PCUs in queue
        for (PcuInQueue pcuInQueue : pcuInQueueResponse) {
            String pcuName = pcuInQueue.getPcuBO();
            PcuStepStatusDetails pcuDetails = pcuMap.getOrDefault(pcuName, newPcuStepStatusDetails(pcuName, setPcuStepStatusRequest));
            pcuDetails.setQtyInQueue(pcuDetails.getQtyInQueue() + Integer.parseInt(pcuInQueue.getQtyInQueue()));
            pcuDetails.setItemBO(pcuInQueue.getItemBO());
            pcuDetails.setResourceBO(setPcuStepStatusRequest.getResource());
            pcuMap.put(pcuName, pcuDetails);
        }

        // Process PCUs in work
        for (PcuInWork pcuInWork : pcuInWorkResponse) {
            String pcuName = pcuInWork.getPcuBO();
            PcuStepStatusDetails pcuDetails = pcuMap.getOrDefault(pcuName, newPcuStepStatusDetails(pcuName, setPcuStepStatusRequest));
            pcuDetails.setQtyInWork(pcuDetails.getQtyInWork() + Integer.parseInt(pcuInWork.getQtyInWork()));
            pcuDetails.setItemBO(pcuInWork.getItemBO());
            pcuDetails.setResourceBO(pcuInWork.getResourceBO());
            pcuMap.put(pcuName, pcuDetails);
        }

        // Process PCUs complete
        for (PcuComplete pcuComplete : pcuCompleteResponse) {
            String pcuName = pcuComplete.getPcuBO();
            PcuStepStatusDetails pcuDetails = pcuMap.getOrDefault(pcuName, newPcuStepStatusDetails(pcuName, setPcuStepStatusRequest));
            pcuDetails.setQtyCompletePending(pcuDetails.getQtyCompletePending() + Integer.parseInt(pcuComplete.getQtyToComplete()));
            pcuDetails.setItemBO(pcuComplete.getItemBO());
            pcuDetails.setResourceBO(pcuComplete.getResourceBO());
            pcuMap.put(pcuName, pcuDetails);
        }
        for (PcuStepStatusDetails pcuStepStatus : pcuMap.values()) {
            int qtyInQueue = pcuStepStatus.getQtyInQueue();
            int qtyInWork = pcuStepStatus.getQtyInWork();
            int qtyCompletePending = pcuStepStatus.getQtyCompletePending();

            // Set the stepStatus based on the quantities
            if (qtyCompletePending == 0 && qtyInWork == 0 && qtyInQueue == 0) {
                pcuStepStatus.setPcuStepStatus("Completed");
            } else if (qtyInWork > 0) {
                pcuStepStatus.setPcuStepStatus("Active");
            } else if (qtyInQueue > 0 && qtyInWork == 0) {
                pcuStepStatus.setPcuStepStatus("InQueue");
            }
        }

        // Convert the map values to a list
        return new ArrayList<>(pcuMap.values());
    }

    private PcuStepStatusDetails newPcuStepStatusDetails(String pcuName, SetPcuStepStatusRequest setPcuStepStatusRequest) {
        return PcuStepStatusDetails.builder()
                .pcu(pcuName)
                .qtyInQueue(0)  // Initialize to 0
                .qtyInWork(0)   // Initialize to 0
                .qtyCompletePending(0)  // Initialize to 0
                .routingBO(setPcuStepStatusRequest.getRoutingBO())
                .routingDescription(setPcuStepStatusRequest.getRoutingDescription())
                .shopOrder(setPcuStepStatusRequest.getShopOrder())
                .itemBO(setPcuStepStatusRequest.getItemBO())
                .resourceBO(setPcuStepStatusRequest.getResource())
                .user(setPcuStepStatusRequest.getUser())
                .holdId(setPcuStepStatusRequest.getHoldId())
                .build();
    }


    @Override
    public Routing retrieveRouting(String site, String version, String routing) throws Exception {
        RoutingRequest routingRequest = RoutingRequest.builder().site(site).routing(routing).version(version).build();
        Routing routingResponse = webClientBuilder.build()
                .post()
                .uri(retrieveRouting)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(Routing.class)
                .block();
        return routingResponse;
    }

    @Override
    public PcuHeader retrievePcuHeaderByPcu(String site, String pcuBO) throws Exception {
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder().site(site).pcuBO(pcuBO).build();
        PcuHeader pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(retrieveByPcu)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();
        return pcuHeaderResponse;
    }

    @Override
    public List<PcuHeader> retrievePcuHeaderByShopOrder(String site, String shopOrderBO) throws Exception {
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder().site(site).shopOrderBO(shopOrderBO).build();
        List<PcuHeader> pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(retrieveByShopOrder)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PcuHeader>>() {
                })
                .block();
        return pcuHeaderResponse;
    }

    @Override
    public Boolean deletePcuInQueue(String site, String pcuBo, String operationBO, String shopOrderBO) throws Exception {
        PcuInQueueRequest deletePcuInQueueRequest = PcuInQueueRequest.builder().site(site).operationBO(operationBO).pcuBO(shopOrderBO).build();
        Boolean pcuInQueueResponse = webClientBuilder.build()
                .post()
                .uri(deletePcuInQueue)
                .bodyValue(deletePcuInQueueRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return pcuInQueueResponse;
    }

    @Override
    public MessageModel deletePcuInWork(String site, String pcuBo, String operationBO) throws Exception {
        StartRequest pcuInWorkRequest = StartRequest.builder().site(site).operationBO(operationBO).pcuBO(pcuBo).build();
        MessageModel pcuInWorkResponse = webClientBuilder.build()
                .post()
                .uri(deletePcuInWork)
                .bodyValue(pcuInWorkRequest)
                .retrieve()
                .bodyToMono(MessageModel.class)
                .block();
        return pcuInWorkResponse;
    }

    @Override
    public Boolean deletePcuComplete(String site, String pcuBo, String operationBO, String shopOrderBO, String resourceBO) throws Exception {
        PcuCompleteRequest pcuCompleteRequest = PcuCompleteRequest.builder().site(site).pcuBO(pcuBo).operationBO(operationBO).shopOrderBO(shopOrderBO).resourceBO(resourceBO).build();
        Boolean pcuCompleteResponse = webClientBuilder.build()
                .post()
                .uri(deletePcuComplete)
                .bodyValue(pcuCompleteRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return pcuCompleteResponse;
    }

    public MessageModel createPcuInQueue(PcuInQueueRequest pcuInQueueRequest) throws Exception {
        MessageModel pcuInQueueResponse = webClientBuilder.build()
                .post()
                .uri(createPcuInQueue)
                .bodyValue(pcuInQueueRequest)
                .retrieve()
                .bodyToMono(MessageModel.class)
                .block();
        return pcuInQueueResponse;
    }

    @Override
    public MessageDetails clearPcu(String site, String operation, String operationVersion) throws Exception {
        String operationBO ="";
        String shopOrder ="";
        //changed the getPcuInQueue to getPcuInQueueByShopOrder
        List<PcuInQueue> pcuInQueueResponse = getPcuInQueue(site, operation, operationVersion,null);

        List<PcuInWork> pcuInWorkResponse = getPcuInWork(site, operation, operationVersion,null);

        List<PcuComplete> pcuCompleteResponse = getPcuComplete(site, operation, operationVersion,null);
        for(PcuStepStatus pcuStepStatus : pcuStepStatusLists)
        {

        }

        if (pcuInQueueResponse != null && !pcuInQueueResponse.isEmpty()) {
            for (PcuInQueue pcuInQueue : pcuInQueueResponse) {
                PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder()
                        .site(pcuInQueue.getSite())
                        .pcuBO(pcuInQueue.getPcuBO())
                        .itemBO(pcuInQueue.getItemBO())
                        .routerBO(pcuInQueue.getRouterBO())
                        .resourceBO(pcuInQueue.getResourceBO())
                        .operationBO(pcuInQueue.getOperationBO())
                        .stepID(pcuInQueue.getStepID())
                        .userBO(pcuInQueue.getUserBO())
                        .qtyToComplete(pcuInQueue.getQtyToComplete())
                        .qtyInQueue(pcuInQueue.getQtyInQueue())
                        .shopOrderBO(pcuInQueue.getShopOrderBO())
                        .childRouterBO(pcuInQueue.getChildRouterBO())
                        .parentStepID(pcuInQueue.getParentStepID())
                        .build();
                createPcuInQueue(pcuInQueueRequest);
                deletePcuInQueue(pcuInQueue.getSite(), pcuInQueue.getPcuBO(), pcuInQueue.getOperationBO(), pcuInQueue.getShopOrderBO());
            }
        }

        if (pcuInWorkResponse != null && !pcuInWorkResponse.isEmpty()) {
            for (PcuInWork pcuInWork : pcuInWorkResponse) {
                PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder()
                        .site(pcuInWork.getSite())
                        .pcuBO(pcuInWork.getPcuBO())
                        .itemBO(pcuInWork.getItemBO())
                        .routerBO(pcuInWork.getRouterBO())
                        .resourceBO(pcuInWork.getResourceBO())
                        .operationBO(pcuInWork.getOperationBO())
                        .stepID(pcuInWork.getStepID())
                        .userBO(pcuInWork.getUserBO())
                        .qtyToComplete(pcuInWork.getQtyToComplete())
                        .qtyInQueue(pcuInWork.getQtyInWork())
                        .shopOrderBO(pcuInWork.getShopOrderBO())
                        .childRouterBO(pcuInWork.getChildRouterBO())
                        .parentStepID(pcuInWork.getParentStepID())
                        .build();
                createPcuInQueue(pcuInQueueRequest);
                deletePcuInWork(pcuInWork.getSite(), pcuInWork.getPcuBO(), pcuInWork.getOperationBO());
            }
        }
//        if (pcuCompleteResponse != null && !pcuCompleteResponse.isEmpty()) {
//            for (PcuComplete pcuComplete : pcuCompleteResponse) {
//                deletePcuComplete(pcuComplete.getSite(), pcuComplete.getPcuBO(), pcuComplete.getOperationBO(), pcuComplete.getShopOrderBO(), pcuComplete.getResourceBO());
//            }
//        }

        return MessageDetails.builder().msg("Pcu Cleared Successfully for operation" + operation + "/" + operationVersion).msg_type("S").build();
    }


    @Override
    public MessageDetails placeEntireQuantityInQueue(String site, String operation, String operationVersion) throws Exception {
        //changed getPcuInwork,getpcuInw=Queue,getPcuComplete added one more field-shopOrder)
        List<PcuInWork> pcuInWorkResponse = getPcuInWork(site, operation, operationVersion,null);

        List<PcuComplete> pcuCompleteResponse = getPcuComplete(site, operation, operationVersion,null);
        if (pcuInWorkResponse != null && !pcuInWorkResponse.isEmpty()) {
            for (PcuInWork pcuInWork : pcuInWorkResponse) {
                PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder()
                        .site(pcuInWork.getSite())
                        .pcuBO(pcuInWork.getPcuBO())
                        .itemBO(pcuInWork.getItemBO())
                        .routerBO(pcuInWork.getRouterBO())
                        .resourceBO(pcuInWork.getResourceBO())
                        .operationBO(pcuInWork.getOperationBO())
                        .stepID(pcuInWork.getStepID())
                        .userBO(pcuInWork.getUserBO())
                        .qtyToComplete(pcuInWork.getQtyToComplete())
                        .qtyInQueue(pcuInWork.getQtyInWork())
                        .shopOrderBO(pcuInWork.getShopOrderBO())
                        .childRouterBO(pcuInWork.getChildRouterBO())
                        .parentStepID(pcuInWork.getParentStepID())
                        .build();
                createPcuInQueue(pcuInQueueRequest);
                deletePcuInWork(pcuInWork.getSite(), pcuInWork.getPcuBO(), pcuInWork.getOperationBO());
            }
        }
        if (pcuCompleteResponse != null && !pcuCompleteResponse.isEmpty()) {
            for (PcuComplete pcuComplete : pcuCompleteResponse) {
                PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder()
                        .site(pcuComplete.getSite())
                        .pcuBO(pcuComplete.getPcuBO())
                        .itemBO(pcuComplete.getItemBO())
                        .routerBO(pcuComplete.getRouterBO())
                        .resourceBO(pcuComplete.getResourceBO())
                        .operationBO(pcuComplete.getOperationBO())
                        .stepID(pcuComplete.getStepID())
                        .userBO(pcuComplete.getUserBO())
                        .qtyToComplete(pcuComplete.getQtyToComplete())
                        .qtyInQueue(pcuComplete.getQtyCompleted())
                        .shopOrderBO(pcuComplete.getShopOrderBO())
                        .childRouterBO(pcuComplete.getChildRouterBO())
                        .parentStepID(pcuComplete.getParentStepID())
                        .build();
                createPcuInQueue(pcuInQueueRequest);
                deletePcuComplete(pcuComplete.getSite(), pcuComplete.getPcuBO(), pcuComplete.getOperationBO(), pcuComplete.getShopOrderBO(), pcuComplete.getResourceBO());
            }
        }
        return MessageDetails.builder().msg("Pcu Cleared Successfully for operation" + operation + "/" + operationVersion).msg_type("S").build();
    }

    @Override
    public MessageDetails markStepAsComplete(String site, String operation, String operationVersion) throws Exception {
        List<PcuInWork> pcuInWorkResponse = getPcuInWork(site, operation, operationVersion,null);
        String startMessage = "";
        String completeMessage="";
        //changed the getPcuInQueue to getPcuInQueueByShopOrder
        List<PcuInQueue> pcuInQueueResponse = getPcuInQueue(site, operation, operationVersion,null);
        List<StartRequest> startRequests = new ArrayList<>();
        if (pcuInQueueResponse != null && !pcuInQueueResponse.isEmpty()) {
            for (PcuInQueue pcuInQueue : pcuInQueueResponse) {
                StartRequest startRequest = StartRequest.builder()
                        .site(pcuInQueue.getSite())
                        .pcuBO(pcuInQueue.getPcuBO())
                        .itemBO(pcuInQueue.getItemBO())
                        .routerBO(pcuInQueue.getRouterBO())
                        .operationBO(pcuInQueue.getOperationBO())
                        .resourceBO(pcuInQueue.getResourceBO())
                        .quantity("0")
                        .qtyToComplete(pcuInQueue.getQtyToComplete())
                        .stepID(pcuInQueue.getStepID())
                        .userBO(pcuInQueue.getUserBO())
                        .shopOrderBO(pcuInQueue.getShopOrderBO())
                        .childRouterBO(pcuInQueue.getChildRouterBO())
                        .parentStepID(pcuInQueue.getParentStepID())
                        .build();
                startRequests.add(startRequest);
            }
            StartRequestList startRequestList = StartRequestList.builder().requestList(startRequests).build();
            MessageModel start = webClientBuilder.build()
                    .post()
                    .uri(startPcu)
                    .bodyValue(startRequestList)
                    .retrieve()
                    .bodyToMono(MessageModel.class)
                    .block();
            startMessage = start.getMessage_details().getMsg_type();
        }

        if (pcuInWorkResponse != null && !pcuInWorkResponse.isEmpty()) {
            List<PcuCompleteRequest> pcuCompleteRequests = new ArrayList<>();
            for (PcuInWork pcuInWork : pcuInWorkResponse) {
                PcuCompleteRequest completeRequest = PcuCompleteRequest.builder()
                        .site(pcuInWork.getSite())
                        .pcuBO(pcuInWork.getPcuBO())
                        .itemBO(pcuInWork.getItemBO())
                        .routerBO(pcuInWork.getRouterBO())
                        .operationBO(pcuInWork.getOperationBO())
                        .resourceBO(pcuInWork.getResourceBO())
                        .qtyCompleted("0")
                        .qtyToComplete(pcuInWork.getQtyToComplete())
                        .stepID(pcuInWork.getStepID())
                        .userBO(pcuInWork.getUserBO())
                        .shopOrderBO(pcuInWork.getShopOrderBO())
                        .childRouterBO(pcuInWork.getChildRouterBO())
                        .parentStepID(pcuInWork.getParentStepID())
                        .build();
                pcuCompleteRequests.add(completeRequest);

            }
            RequestList requestList = RequestList.builder().requestList(pcuCompleteRequests).build();
            MessageModel complete = webClientBuilder.build()
                    .post()
                    .uri(pcuComplete)
                    .bodyValue(requestList)
                    .retrieve()
                    .bodyToMono(MessageModel.class)
                    .block();
            completeMessage = complete.getMessage_details().getMsg_type();
        }
        if(startMessage.equalsIgnoreCase("S")&&completeMessage.equalsIgnoreCase("S"))
        {
            return MessageDetails.builder().msg_type("S").msg("Operation Completed Successfully").build();
        }
            return MessageDetails.builder().msg_type("F").msg("Failed to complete the operation").build();
    }
//
//    @Override
//    public PcuStepStatus getPcuStepStatus(String site, String pcu, String shopOrder, String type,String user) throws Exception {
//        List<PcuStepStatus> pcuStepStatusList = new ArrayList<>();
//        if(type!=null&&type.equalsIgnoreCase("byShopOrder")){
//            List<PcuHeader>  pcuHeaders=retrievePcuHeaderByShopOrder(site,"ShopOrderBO:"+site+","+shopOrder);
//            String routingBO= pcuHeaders.get(0).getRouterList().get(0).getPcuRouterBO();
//            List<String> pcuBOList = pcuHeaders.stream()
//                    .map(pcuHeader -> pcuHeader.getPcuBO())
//                    .collect(Collectors.toList());
//            String[] routingBo=routingBO.split(",");
//            Routing routingResponse=retrieveRouting(site,routingBo[1],routingBo[2]);
//            if(routingResponse==null || routingResponse.getRouting()==null || routingResponse.getRouting().isEmpty()){
//                throw new PcuStepStatusException(3228,routingBo[1]);
//            }
//
//           List<RoutingStep> routingStepList=routingResponse.getRoutingStepList();
//            if(routingStepList!=null && !routingStepList.isEmpty()){
////                List<String> operationList=routingStepList.stream().map()
//            }
//        }else{
//            PcuHeader retrieveByPcu = retrievePcuHeaderByPcu(site, "PcuBO:" + site + "," + pcu);
//            String routingBO= retrieveByPcu.getRouterList().get(0).getPcuRouterBO();
//            String[] routingBo=routingBO.split(",");
//            Routing routingResponse=retrieveRouting(site,routingBo[1],routingBo[2]);
//            if(routingResponse==null || routingResponse.getRouting()==null || routingResponse.getRouting().isEmpty()){
//                throw new PcuStepStatusException(3228,routingBo[1]);
//            }
//
//            List<RoutingStep> routingStepList=routingResponse.getRoutingStepList();
//            if(routingStepList!=null && !routingStepList.isEmpty()){
//               String routingDescription = routingResponse.getDescription();
//               String itemBo[]=retrieveByPcu.getItemBO().split(",");
//               String shopOrderBo[]=retrieveByPcu.getShopOrderBO().split(",");
//                SetPcuStepStatusRequest setPcuStepStatusRequest = SetPcuStepStatusRequest.builder()
//                        .routingBO(routingBo[1] + "/" + routingBo[2])
//                        .routingDescription(routingDescription)
//                        .pcu(pcu)
//                        .site(site)
//                        .pcuStepStatus("")
//                        .shopOrder(shopOrderBo[1])
//                        .itemBO(itemBo[1] + "/" + itemBo[2])
//                        .routingStepList(routingStepList)
//                        .resource("")
//                        .user(user)
//                        .holdId("")
//                        .build();
//                pcuStepStatusList.addAll(getOperationDetailsByPcu(setPcuStepStatusRequest));
////                List<String> operationList=routingStepList.stream().map()
//            }
//
//
//
//        }
//        return null;
//    }


}
