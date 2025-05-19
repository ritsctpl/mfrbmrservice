package com.rits.changeproductionservice.service;

import com.rits.changeproductionservice.dto.ChangeProductionRequest;
import com.rits.changeproductionservice.dto.PcuCompleteRequest;
import com.rits.changeproductionservice.exception.ChangeProductionException;
import com.rits.changeproductionservice.model.OriginalRoutingDetails;
import com.rits.changeproductionservice.repository.ChangeProductionRepository;
import com.rits.dccollect.dto.OperationRequest;
import com.rits.pcucompleteservice.dto.PcuInWorkRequest;
import com.rits.pcucompleteservice.model.PcuComplete;
import com.rits.pcuheaderservice.dto.BomRequest;
import com.rits.pcuheaderservice.dto.PcuHeaderRequest;
import com.rits.pcuheaderservice.dto.ShopOrderRequest;
import com.rits.pcuheaderservice.model.PcuHeader;
import com.rits.pcurouterheaderservice.dto.Operation;
import com.rits.pcurouterheaderservice.dto.RoutingRequest;
import com.rits.pcurouterheaderservice.model.Routing;
import com.rits.pcurouterheaderservice.model.RoutingStep;
import com.rits.shoporderrelease.dto.*;
import com.rits.shoporderrelease.model.SOReleaseMessageModel;
import com.rits.shoporderrelease.dto.ShopOrder;
import com.rits.shoporderrelease.service.ShopOrderReleaseService;
import com.rits.startservice.dto.PcuInQueue;
import com.rits.startservice.dto.PcuInQueueRequest;
import com.rits.startservice.dto.ResourceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeProductionServiceImpl implements ChangeProductionService{
    private final ChangeProductionRepository changeProductionRepository;
    private final WebClient.Builder webClientBuilder;
    private final ShopOrderReleaseService shopOrderReleaseService;
    @Value("${routing-service.url}/retrieve")
    private String routingUrl;

    @Value("${pcuheader-service.url}/readPcu")
    private String retrievePcuHeader;

    @Value("${pcuinqueue-service.url}/retrieve")
    private String pcuInQueueRetrieveUrl;

    @Value("${start-service.url}/retrieve")
    private String retrievePcuInWorkUrl;

    @Value("${pcucomplete-service.url}/retrieve")
    private String pcuCompleteRetrieveUrl;

    @Value("${shoporder-service.url}/isExist")
    private String isShopOrderExistUrl;

    @Value("${resource-service.url}/isExist")
    private String isResourceExistUrl;
    @Value("${item-service.url}/isExist")
    private String isItemExistUrl;

    @Value("${routing-service.url}/isExist")
    private String isRoutingExistUrl;

    @Value("${bom-service.url}/isExist")
    private String isBomExistUrl;
    @Value("${operation-service.url}/isExist")
    private String isOperationExistUrl;

    @Value("${shoporder-service.url}/retrieve")
    private String retrieveShopOrderUrl;

    @Value("${nextnumbergenerator-service.url}/retrieve")
    private String retrieveNxtNumberUrl;

    @Value("${nextnumbergenerator-service.url}/generateNextNumber")
    private String generateNextNumberUrl;

    @Value("${shoporder-service.url}/create")
    private String createShopOrderUrl;

    @Value("${item-service.url}/retrieve")
    private String itemUrl;

    @Value("${bom-service.url}/retrieve")
    private String bomUrl;

    @Value("${routing-service.url}/retrieve")
    private String retrieveRoutingUrl;

    @Value("${pcuheader-service.url}/create")
    private String pcuHeaderCreateUrl;





    @Override
    public String getFirstOperation(String site, String routing, String routingVersion)throws Exception
    {
        List<RoutingStep> routingStepList = getRoutingStepList(site,routing,routingVersion);
        String newOperationBO = null;
        if(routingStepList!=null) {
            for (RoutingStep routingStep : routingStepList)
            {
                if(routingStep.getStepType().equalsIgnoreCase("Operation") && routingStep.isEntryStep())
                {
                    newOperationBO ="OperationBO:"+site+","+routingStep.getOperation()+","+routingStep.getOperationVersion();
                }
                else if(routingStep.getStepType().equalsIgnoreCase("Routing") && routingStep.isEntryStep()){
                    if(routingStep.getRoutingBO()!=null && !routingStep.getRoutingBO().isEmpty())
                    {
                        String [] routingBO  = routingStep.getRoutingBO().split(",");
                        if(routingBO.length==3)
                        {
                            getFirstOperation(site,routingBO[1],routingBO[2]);
                        }
                    }
                }
            }
        }
        return newOperationBO;
    }

    public List<Operation> getAllOperation(String site, String routing, String routingVersion)throws Exception
    {
        List<RoutingStep> routingStepList = getRoutingStepList(site,routing,routingVersion);
        List<Operation> operationList = new ArrayList<>();
        if(routingStepList!=null) {
            for (RoutingStep routingStep : routingStepList)
            {
                if(routingStep.getStepType().equalsIgnoreCase("Operation"))
                {
                    Operation operation = Operation.builder().operation(routingStep.getOperation()).revision(routingStep.getOperationVersion()).build();
                    operationList.add(operation);
                }
                else if(routingStep.getStepType().equalsIgnoreCase("Routing")){
                    if(routingStep.getRoutingBO()!=null && !routingStep.getRoutingBO().isEmpty())
                    {
                        String [] routingBO  = routingStep.getRoutingBO().split(",");
                        if(routingBO.length==3)
                        {
                            getAllOperation(site,routingBO[1],routingBO[2]);
                        }
                    }
                }
            }
        }
        return operationList;
    }

    public List<RoutingStep> getRoutingStepList(String site, String routing, String routingVersion){
        RoutingRequest routingRequest = RoutingRequest.builder()
                .site(site)
                .routing(routing)
                .version(routingVersion)
                .build();

        Routing routingResponse = webClientBuilder.build()
                .post()
                .uri(routingUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(Routing.class)
                .block();
        List<RoutingStep> routingStepList = null;
        if(routingResponse!=null && routingResponse.getRouting()!=null && !routingResponse.getRouting().isEmpty())
        {
            routingStepList = routingResponse.getRoutingStepList();
        }
        return routingStepList;
    }

    @Override
    public String getCurrentOperation(String site, String routing, String routingVersion, String pcu) throws Exception
    {
        String newOperationBO = null;
        String route = getPcuRoutingAndVersionForPcu(site,pcu);
        String [] routerBO = route.split(",");
//        boolean isPcuPresentInOperation = false;
        Operation currentOperation = null;
        if(routerBO.length==3) {
                if (routing != null && !routing.isEmpty() && routingVersion != null && !routingVersion.isEmpty()) {
                    if (routerBO[1].equalsIgnoreCase(routing) && routerBO[2].equalsIgnoreCase(routingVersion)) {
                    List<Operation> operationList = getAllOperation(site, routing, routingVersion);
                    if (operationList != null && !operationList.isEmpty()) {
                        for(Operation operation : operationList)
                        {
                            PcuInQueueRequest retrievePcuInQueueRequest = PcuInQueueRequest.builder().site(site).pcuBO(pcu).operationBO("OperationBO:"+site+","+operation.getOperation()+","+operation.getRevision()).build();
                            PcuInQueue retrievePcuInQueue = webClientBuilder.build()
                                    .post()
                                    .uri(pcuInQueueRetrieveUrl)
                                    .bodyValue(retrievePcuInQueueRequest)
                                    .retrieve()
                                    .bodyToMono(PcuInQueue.class)
                                    .block();
                            PcuInWorkRequest inWorkRequest = PcuInWorkRequest.builder().site(site)
                                    .pcuBO(pcu)
                                    .operationBO("OperationBO:"+site+","+operation.getOperation()+","+operation.getRevision()).build();
                            PcuInWorkRequest retrieveInWork = webClientBuilder.build()
                                    .post()
                                    .uri(retrievePcuInWorkUrl)
                                    .bodyValue(inWorkRequest)
                                    .retrieve()
                                    .bodyToMono(PcuInWorkRequest.class)
                                    .block();
                            if(retrievePcuInQueue!=null && retrievePcuInQueue.getPcu()!=null && !retrievePcuInQueue.getPcu().isEmpty())
                            {
                                currentOperation = operation;
                            }
                            if(retrieveInWork!=null && retrieveInWork.getPcuBO()!=null && !retrieveInWork.getPcuBO().isEmpty())
                            {
                                currentOperation = operation;
                            }
//                            if(retrievePcuInQueue==null || retrievePcuInQueue.getPcuBO()==null || retrievePcuInQueue.getPcuBO().isEmpty() || retrieveInWork!=null || retrieveInWork.getPcuBO()==null || retrieveInWork.getPcuBO().isEmpty())
//                            {
//                                break;
//                            }
                        }
                        if(currentOperation!=null)
                        {
                            newOperationBO="OperationBO:"+site+","+currentOperation.getOperation()+","+currentOperation.getRevision();

                        }
                    }
                }
                    else{
                        String firstOperationOfRouting = getFirstOperation(site,routing,routingVersion);
                       if(firstOperationOfRouting!=null && firstOperationOfRouting!=null&& !firstOperationOfRouting.isEmpty()) {
                           newOperationBO = firstOperationOfRouting;
                       }
                    }
            }
        }
        return newOperationBO;
    }

    @Override
    public String getFirstUncompletedOperation(String site, String routing, String routingVersion, String pcu)throws Exception
    {
        String newOperationBO = null;
        String route = getPcuRoutingAndVersionForPcu(site,pcu);
        String [] routerBO = route.split(",");

        Operation currentOperation = null;
        if(routerBO.length==3) {
            if (routing != null && !routing.isEmpty() && routingVersion != null && !routingVersion.isEmpty()) {
                if (routerBO[1].equalsIgnoreCase(routing) && routerBO[2].equalsIgnoreCase(routingVersion)) {
                    List<Operation> operationList = getAllOperation(site, routing, routingVersion);
                    if (operationList != null && !operationList.isEmpty()) {
                        for(Operation operation : operationList)
                        {
                            PcuCompleteRequest retrievePcuCompleteRequest = PcuCompleteRequest.builder().site(site).pcuBO(pcu).operationBO("OperationBO:"+site+","+operation.getOperation()+","+operation.getRevision()).build();
                            PcuComplete retrievePcuComplete = webClientBuilder.build()
                                    .post()
                                    .uri(pcuCompleteRetrieveUrl)
                                    .bodyValue(retrievePcuCompleteRequest)
                                    .retrieve()
                                    .bodyToMono(PcuComplete.class)
                                    .block();

//                            if(retrievePcuComplete!=null && retrievePcuComplete.getPcuBO()!=null && !retrievePcuComplete.getPcuBO().isEmpty())
//                            {
//                                currentOperation = operation;
//                            }

                            if(retrievePcuComplete==null || retrievePcuComplete.getPcuBO()==null || retrievePcuComplete.getPcuBO().isEmpty())
                            {
                                currentOperation = operation;
                            }

//                            if(retrievePcuComplete==null || retrievePcuComplete.getPcuBO()==null || retrievePcuComplete.getPcuBO().isEmpty())
//                            {
//                                break;
//                            }
                        }
                        if(currentOperation!=null)
                        {
                            newOperationBO = "OperationBO:"+site+","+currentOperation.getOperation()+","+currentOperation.getRevision();

                        }
                    }
                }
                else{
                    String firstOperationOfRouting = getFirstOperation(site,routing,routingVersion);
                    if(firstOperationOfRouting!=null && firstOperationOfRouting!=null&& !firstOperationOfRouting.isEmpty()) {
                        newOperationBO = firstOperationOfRouting;
                    }
                }
            }
        }
        return newOperationBO;
    }

    private String getPcuRoutingAndVersionForPcu(String site, String pcuBO)
    {
        String route = null;
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder().site(site).pcuBO(pcuBO).build();
        PcuHeader pcuHeaderResponse = webClientBuilder.build()
            .post()
            .uri(retrievePcuHeader)
            .bodyValue(pcuHeaderRequest)
            .retrieve()
            .bodyToMono(PcuHeader.class)
            .block();
        if(pcuHeaderResponse!=null && pcuHeaderResponse.getPcuBO()!=null && !pcuHeaderResponse.getPcuBO().isEmpty())
        {
            route = pcuHeaderResponse.getRouterList().get(0).getPcuRouterBO();
        }
        return route;
    }

    @Override
    public String  doChangeProduction(ChangeProductionRequest changeProductionRequest) throws  Exception
    {
        Boolean newRoutingPresent = false;
        Boolean newItemIsPresent = false;
        Boolean newBomIsPResent = false;
        Boolean newResourceIsPresent = false;
        Boolean newOperationIsPresent = false;
        int buildQty = 0;

        String newOperation=null;
//===============================================================Validating New Production Details===========================================================================================
//        VALIDATING RESOURCE
        if(changeProductionRequest.getNewResource()!=null && !changeProductionRequest.getNewResource().isEmpty()) {
            ResourceRequest resourceRequest = ResourceRequest.builder().site(changeProductionRequest.getSite()).resource(changeProductionRequest.getNewResource()).build();
            Boolean isResourceExistResponse = webClientBuilder.build()
                    .post()
                    .uri(isResourceExistUrl)
                    .bodyValue(resourceRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            if(!isResourceExistResponse)
            {
                throw new ChangeProductionException(6000,changeProductionRequest.getNewResource());
            }else{
                newResourceIsPresent = true;
            }
        }
//        VALIDATING ITEM
        if(changeProductionRequest.getNewItem()!=null && !changeProductionRequest.getNewItem().isEmpty() && changeProductionRequest.getNewItemVersion()!=null && !changeProductionRequest.getNewItemVersion().isEmpty()) {
            ItemRequest itemRequest = ItemRequest.builder().site(changeProductionRequest.getSite()).item(changeProductionRequest.getNewItem()).revision(changeProductionRequest.getNewItemVersion()).build();
            Boolean isItemExistResponse = webClientBuilder.build()
                    .post()
                    .uri(isItemExistUrl)
                    .bodyValue(itemRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            if(!isItemExistResponse)
            {
                throw new ChangeProductionException(6002,changeProductionRequest.getNewItem()+","+changeProductionRequest.getNewItemVersion());
            }else{
                newItemIsPresent = true;
            }
        }
//        VALIDATING ROUTING
        if(changeProductionRequest.getNewRouting()!=null && !changeProductionRequest.getNewRouting().isEmpty() && changeProductionRequest.getNewRoutingVersion()!=null && !changeProductionRequest.getNewRoutingVersion().isEmpty()) {
            RoutingRequest routingRequest = RoutingRequest.builder().site(changeProductionRequest.getSite()).routing(changeProductionRequest.getNewRouting()).version(changeProductionRequest.getNewRoutingVersion()).build();
            Boolean isRoutingExistResponse = webClientBuilder.build()
                    .post()
                    .uri(isRoutingExistUrl)
                    .bodyValue(routingRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            if(!isRoutingExistResponse)
            {
                throw new ChangeProductionException(6003,changeProductionRequest.getNewRouting()+","+changeProductionRequest.getNewRoutingVersion());
            }else{
                newRoutingPresent = true;
            }
        }
//       VALIDATING BOM
        if(changeProductionRequest.getNewBom()!=null && !changeProductionRequest.getNewBom().isEmpty() && changeProductionRequest.getNewBomVersion()!=null && !changeProductionRequest.getNewBomVersion().isEmpty()) {
            BomRequest bomRequest = BomRequest.builder().site(changeProductionRequest.getSite()).bom(changeProductionRequest.getNewBom()).revision(changeProductionRequest.getNewBomVersion()).build();
            Boolean isBomExistResponse = webClientBuilder.build()
                    .post()
                    .uri(isBomExistUrl)
                    .bodyValue(bomRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            if(!isBomExistResponse) {
                throw new ChangeProductionException(6004, changeProductionRequest.getNewBom()+","+changeProductionRequest.getNewBomVersion());
            }else{
                newBomIsPResent = true;
            }
        }
//        VALIDATING OPERATION
        if(changeProductionRequest.getNewOperation()!=null && !changeProductionRequest.getNewOperation().isEmpty()) {
            OperationRequest operationRequest = OperationRequest.builder().site(changeProductionRequest.getSite()).operation(changeProductionRequest.getNewOperation()).build();
            Boolean isOperationExistResponse = webClientBuilder.build()
                    .post()
                    .uri(isOperationExistUrl)
                    .bodyValue(operationRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            if(!isOperationExistResponse)
            {
                throw new ChangeProductionException(6005,changeProductionRequest.getNewOperation());
            }else{
                newOperationIsPresent = true;
            }
        }
//============================================================================Validating New Production Details Ends Here=======================================================================================
//        if(newRoutingPresent && changeProductionRequest.getNewRouting()!=null && !changeProductionRequest.getNewRouting().isEmpty())
//        {
//            if(changeProductionRequest.isSkipActiveSFCs()) {
//                buildQty = changeProductionRequest.getOriginalRoutingDetailsList().size();
//                for(OriginalRoutingDetails inactivePcus : changeProductionRequest.getOriginalRoutingDetailsList()) {
//
//                    if (!inactivePcus.getPcuStatus().equalsIgnoreCase("Active")) {
//                        if (changeProductionRequest.isPlaceInQueueAtOperation()) {
//                            newOperation = changeProductionRequest.getNewOperation();
//                        }
//                        if (changeProductionRequest.isPlaceInQueueAtCurrentOperation()) {
//                            newOperation = getCurrentOperation(changeProductionRequest.getSite(), changeProductionRequest.getNewRouting(), changeProductionRequest.getNewRoutingVersion(), inactivePcus.getPcu());
//                        }
//                        if(changeProductionRequest.isPlaceInQueueAtFirstOperation())
//                        {
//                            newOperation = getFirstOperation(changeProductionRequest.getSite(),changeProductionRequest.getNewRouting(),changeProductionRequest.getNewRoutingVersion());
//                        }
//                        if(changeProductionRequest.isPlaceInQueueAtFirstUncompletedOperation())
//                        {
//                            newOperation = getFirstUncompletedOperation(changeProductionRequest.getSite(),changeProductionRequest.getNewRouting(),changeProductionRequest.getNewRoutingVersion(),inactivePcus.getPcu());
//                        }
//                    }
//                }
//            }else {
//                buildQty = changeProductionRequest.getOriginalRoutingDetailsList().size();
//                for(OriginalRoutingDetails allPcu : changeProductionRequest.getOriginalRoutingDetailsList()) {
//                    if (changeProductionRequest.isPlaceInQueueAtOperation()) {
//                        newOperation = changeProductionRequest.getNewOperation();
//                    }
//                    if (changeProductionRequest.isPlaceInQueueAtCurrentOperation()) {
//                        newOperation = getCurrentOperation(changeProductionRequest.getSite(), changeProductionRequest.getNewRouting(), changeProductionRequest.getNewRoutingVersion(), allPcu.getPcu());
//                    }
//                    if(changeProductionRequest.isPlaceInQueueAtFirstOperation())
//                    {
//                        newOperation = getFirstOperation(changeProductionRequest.getSite(),changeProductionRequest.getNewRouting(),changeProductionRequest.getNewRoutingVersion());
//                    }
//                    if(changeProductionRequest.isPlaceInQueueAtFirstUncompletedOperation())
//                    {
//                        newOperation = getFirstUncompletedOperation(changeProductionRequest.getSite(),changeProductionRequest.getNewRouting(),changeProductionRequest.getNewRoutingVersion(),allPcu.getPcu());
//                    }
//                }
//            }
//        }

        Boolean isShopOrderExistResponse = false;
        if(changeProductionRequest.getNewShopOrder()!=null && !changeProductionRequest.getNewShopOrder().isEmpty())
        {
            ShopOrderRequest shopOrderRequest = ShopOrderRequest.builder().site(changeProductionRequest.getSite()).shopOrder(changeProductionRequest.getNewShopOrder()).build();
             isShopOrderExistResponse = webClientBuilder.build()
                    .post()
                    .uri(isShopOrderExistUrl)
                    .bodyValue(shopOrderRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        }
        for(OriginalRoutingDetails pcus : changeProductionRequest.getOriginalRoutingDetailsList()) {
            if (changeProductionRequest.isPlaceInQueueAtOperation()) {
                newOperation = changeProductionRequest.getNewOperation();
            }
            if (changeProductionRequest.isPlaceInQueueAtCurrentOperation()) {
                newOperation = getCurrentOperation(changeProductionRequest.getSite(), changeProductionRequest.getNewRouting(), changeProductionRequest.getNewRoutingVersion(), pcus.getPcu());
            }
            if (changeProductionRequest.isPlaceInQueueAtFirstOperation()) {
                newOperation = getFirstOperation(changeProductionRequest.getSite(), changeProductionRequest.getNewRouting(), changeProductionRequest.getNewRoutingVersion());
            }
            if (changeProductionRequest.isPlaceInQueueAtFirstUncompletedOperation()) {
                newOperation = getFirstUncompletedOperation(changeProductionRequest.getSite(), changeProductionRequest.getNewRouting(), changeProductionRequest.getNewRoutingVersion(), pcus.getPcu());
            }
            buildQty = changeProductionRequest.getOriginalRoutingDetailsList().size();
            Boolean changeProduction = false;
            if (changeProductionRequest.getNewShopOrder() != null && !changeProductionRequest.getNewShopOrder().isEmpty()) {
                if (!isShopOrderExistResponse) {
                    Boolean isShopOrderCreatedSuccessfully = createNewShopOrder(changeProductionRequest, buildQty);
                    if (pcus.getPcuStatus().equalsIgnoreCase("Active")) {
                    } else {
//                    here we need to release the pcus at the given operation
//                    call the method to put pcus at the specified operation
                        changeProduction = true;
                    }
                } else {
                    if (checkOrderHasEnoughQuantityToRelease(changeProductionRequest.getSite(), changeProductionRequest.getNewShopOrder(), buildQty)) {
//                    here we need to release the pcus at the given operation
//                    call the method to put pcus at the specified operation
                        changeProduction = true;
                    } else {
                        throw new ChangeProductionException(6006);
                    }
                }
            }else if(!changeProduction  && newRoutingPresent){
                com.rits.pcuheaderservice.dto.ShopOrder retrieveShopOrder = null;
                ShopOrderRequest shopOrderRetrieveRequest = ShopOrderRequest.builder().site(changeProductionRequest.getSite()).shopOrder(pcus.getShopOrder()).build();
                retrieveShopOrder = webClientBuilder
                        .build()
                        .post()
                        .uri(retrieveShopOrderUrl)
                        .body(BodyInserters.fromValue(shopOrderRetrieveRequest))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<com.rits.pcuheaderservice.dto.ShopOrder>() {})
                        .block();
                if(retrieveShopOrder!=null) {
                    retrieveShopOrder.setPlannedRouting(changeProductionRequest.getNewRouting());
                    retrieveShopOrder.setRoutingVersion(changeProductionRequest.getNewRoutingVersion());
                    List<com.rits.pcuheaderservice.dto.Pcu> pcuList = new ArrayList<>();
                    com.rits.pcuheaderservice.dto.Pcu pcu = com.rits.pcuheaderservice.dto.Pcu.builder().pcuBo(pcus.getPcu()).build();
                    pcuList.add(pcu);
                    PcuHeaderRequest createPcuHeaderRequest = PcuHeaderRequest.builder()
                            .shopOrder(retrieveShopOrder)
                            .qtyInQueue(pcus.getQty())
                            .userBO(changeProductionRequest.getUserBO())
                            .pcuBos(pcuList)
                            .build();
                    SOReleaseMessageModel createPCUHeader = webClientBuilder.build()
                            .post()
                            .uri(pcuHeaderCreateUrl)
                            .bodyValue(createPcuHeaderRequest)
                            .retrieve()
                            .bodyToMono(SOReleaseMessageModel.class)
                            .block();
//                    call the method to put pcus at the specified operation
                }
            }
//                Same as for routing, we have to do for item and bom
//             else if () {
//
//            }
        }
        return "";
    }

    public Boolean placePcuAtSpecifiedOperation(String site,List<Operation> operationList,String newOperation,String pcu,String shopOrder) throws Exception
    {
        for(Operation operations:operationList)
        {
            PcuInQueueRequest retrievePcuInQueueRequest = PcuInQueueRequest.builder().site(site).pcuBO(pcu).operationBO("OperationBO:"+site+","+operations.getOperation()+","+operations.getRevision()).build();
            PcuInQueue retrievePcuInQueue = webClientBuilder.build()
                    .post()
                    .uri(pcuInQueueRetrieveUrl)
                    .bodyValue(retrievePcuInQueueRequest)
                    .retrieve()
                    .bodyToMono(PcuInQueue.class)
                    .block();
            PcuInWorkRequest inWorkRequest = PcuInWorkRequest.builder().site(site)
                    .pcuBO(pcu)
                    .operationBO("OperationBO:"+site+","+operations.getOperation()+","+operations.getRevision()).build();
            PcuInWorkRequest retrieveInWork = webClientBuilder.build()
                    .post()
                    .uri(retrievePcuInWorkUrl)
                    .bodyValue(inWorkRequest)
                    .retrieve()
                    .bodyToMono(PcuInWorkRequest.class)
                    .block();
            String [] operationBO = newOperation.split(",");
            String op = "";
            String opVersion = "";
            if(operationBO.length==3)
            {
                op = operationBO[1];
                opVersion = operationBO[2];
            }
            if(operations.getOperation().equalsIgnoreCase(op) && operations.getOperation().equalsIgnoreCase(opVersion))
            {

            }
            if(retrievePcuInQueue!=null && retrievePcuInQueue.getPcu()!=null && !retrievePcuInQueue.getPcu().isEmpty())
            {

            }
            if(retrieveInWork!=null && retrieveInWork.getPcuBO()!=null && !retrieveInWork.getPcuBO().isEmpty())
            {

            }
        }
        return true;
    }

    public Boolean createNewShopOrder(ChangeProductionRequest changeProductionRequest, int buildQty) throws Exception
    {
        Boolean shopOrderCreated = false;
             String newBom = changeProductionRequest.getOriginalRoutingDetailsList().get(0).getBom();
             String newBomVersion = changeProductionRequest.getOriginalRoutingDetailsList().get(0).getBomVersion();
             String newItem = changeProductionRequest.getOriginalRoutingDetailsList().get(0).getItem();
             String newItemVersion = changeProductionRequest.getOriginalRoutingDetailsList().get(0).getItemVersion();
             String newRouting = changeProductionRequest.getOriginalRoutingDetailsList().get(0).getRouting();
             String newRoutingVersion = changeProductionRequest.getOriginalRoutingDetailsList().get(0).getRoutingVersion();
             if(changeProductionRequest.getNewRouting()!=null && !changeProductionRequest.getNewRouting().isEmpty() && changeProductionRequest.getNewRoutingVersion()!=null && !changeProductionRequest.getNewRoutingVersion().isEmpty())
             {
                 newRouting = changeProductionRequest.getNewRouting();
                 newRoutingVersion = changeProductionRequest.getNewRoutingVersion();
             }
            if(changeProductionRequest.getNewItem()!=null && !changeProductionRequest.getNewItem().isEmpty() && changeProductionRequest.getNewItemVersion()!=null && !changeProductionRequest.getNewItemVersion().isEmpty())
            {
                newItem = changeProductionRequest.getNewItem();
                newItemVersion = changeProductionRequest.getNewItemVersion();
            }
            if(changeProductionRequest.getNewBom()!=null && !changeProductionRequest.getNewBom().isEmpty() && changeProductionRequest.getNewBomVersion()!=null && !changeProductionRequest.getNewBomVersion().isEmpty())
            {
                newBom = changeProductionRequest.getNewBom();
                newBomVersion = changeProductionRequest.getNewBomVersion();
            }
//            NextNumberGeneratorRequest retrieveNextNumber = NextNumberGeneratorRequest.builder().site(changeProductionRequest.getSite()).numberType("Shop Order").build();
//            NextNumberGenerator existingNextNumber = webClientBuilder.build()
//                    .post()
//                    .uri(retrieveNxtNumberUrl)
//                    .bodyValue(retrieveNextNumber)
//                    .retrieve()
//                    .bodyToMono(new ParameterizedTypeReference<NextNumberGenerator>() {
//                    })
//                    .block();
//            if(existingNextNumber!=null &&  existingNextNumber.getObject()!=null &&  !existingNextNumber.getObject().isEmpty()) {
//                GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest = GeneratePrefixAndSuffixRequest.builder()
//                        .site(changeProductionRequest.getSite())
//                        .object(newItem)
//                        .itemBO("ItemBO:" + changeProductionRequest.getSite() + "," + newItem + "," + newItemVersion)
//                        .priority(500)
//                        .numberBase(existingNextNumber.getNumberBase())
//                        .sequenceLength(existingNextNumber.getSequenceLength())
//                        .minSequence(existingNextNumber.getMinSequence())
//                        .maxSequence(existingNextNumber.getMaxSequence())
//                        .incrementBy(existingNextNumber.getIncrementBy())
//                        .currentSequence(existingNextNumber.getCurrentSequence())
//                        .numberType("Shop Order")
//                        .orderType(existingNextNumber.getOrderType())
//                        .userBO(changeProductionRequest.getUserBO())
//                        .defineBy(existingNextNumber.getDefineBy())
//                        .objectVersion(newItemVersion)
//                        .suffix(existingNextNumber.getSuffix())
//                        .prefix(existingNextNumber.getPrefix())
//                        .build();
//                GeneratedNextNumber generatedNextNumber = webClientBuilder.build()
//                        .post()
//                        .uri(generateNextNumberUrl)
//                        .bodyValue(generatePrefixAndSuffixRequest)
//                        .retrieve()
//                        .bodyToMono(GeneratedNextNumber.class)
//                        .block();
                ShopOrderRequest cretaeShopOrderRequest = ShopOrderRequest.builder()
                        .site(changeProductionRequest.getSite())
                        .shopOrder(changeProductionRequest.getNewShopOrder())
                        .status("Releasable")
                        .orderType("Production")
                        .plannedMaterial(newItem)
                        .materialVersion(newItemVersion)
                        .bomType(changeProductionRequest.getNewBomType())
                        .plannedBom(newBom)
                        .bomVersion(newBomVersion)
                        .plannedRouting(newRouting)
                        .routingVersion(newRoutingVersion)
                        .priority(500)
                        .buildQty(String.valueOf(buildQty))
                        .inUse(false)
                        .build();
                SOReleaseMessageModel createShopOrder = webClientBuilder
                        .build()
                        .post()
                        .uri(createShopOrderUrl)
                        .body(BodyInserters.fromValue(cretaeShopOrderRequest))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<SOReleaseMessageModel>() {
                        })
                        .block();
                if(createShopOrder.getMessage_details().getMsg_type().equalsIgnoreCase("S"))
                {
                      shopOrderCreated = true;
                }
//                ReleaseRequest releaseRequest = ReleaseRequest.builder()
//
//                        .build();
//               MessageModel shopOrderReleaseMessage =  shopOrderReleaseService.pcuRelease(releaseRequest);
               return shopOrderCreated;
    }

   public Boolean checkOrderHasEnoughQuantityToRelease(String site,String shopOrder,int qty) throws Exception
   {
       boolean isQuantityEnough = false;
       ShopOrderRequest shopOrderRetrieveRequest = ShopOrderRequest.builder().site(site).shopOrder(shopOrder).build();
       ShopOrder retrieveShopOrder = webClientBuilder
               .build()
               .post()
               .uri(retrieveShopOrderUrl)
               .body(BodyInserters.fromValue(shopOrderRetrieveRequest))
               .retrieve()
               .bodyToMono(new ParameterizedTypeReference<ShopOrder>() {})
               .block();
       if(retrieveShopOrder!=null && retrieveShopOrder.getShopOrder()!=null && !retrieveShopOrder.getShopOrder().isEmpty())
       {
           if(qty<= Integer.parseInt(retrieveShopOrder.getAvailableQtyToRelease()))
           {
               isQuantityEnough = true;
           }
       }
       return isQuantityEnough;
   }

   private boolean isItemReleasableAndItemTypeManufactured(String site, String item , String itemVersion)throws Exception
   {
       boolean itemRecord = false;
       ItemRequest itemRequest = ItemRequest.builder().site(site).item(item).revision(itemVersion).build();
       Item existingItem = webClientBuilder
               .build()
               .post()
               .uri(itemUrl)
               .body(BodyInserters.fromValue(itemRequest))
               .retrieve()
               .bodyToMono(new ParameterizedTypeReference<Item>() {
               })
               .block();
       if(existingItem!=null && existingItem.getItem()!=null && !existingItem.getItem().isEmpty() && (existingItem.getProcurementType().equalsIgnoreCase("manufactured")||existingItem.getProcurementType().equalsIgnoreCase("purchased/manufactured"))&&existingItem.getStatus().equalsIgnoreCase("releasable"))
       {
           itemRecord = true;
       }
       return  itemRecord;
   }

    private boolean isBomReleasable(String site, String bom , String bomVersion)throws Exception
    {
        boolean bomRecord = false;
        BomRequest bomRequest = BomRequest.builder().site(site).bom(bom).revision(bomVersion).build();
        Bom existingBom = webClientBuilder
                .build()
                .post()
                .uri(bomUrl)
                .body(BodyInserters.fromValue(bomRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Bom>() {
                })
                .block();
        if(existingBom!=null && existingBom.getBom()!=null && !existingBom.getBom().isEmpty() && existingBom.getStatus().equalsIgnoreCase("releasable"))
        {
            bomRecord = true;
        }
        return  bomRecord;
    }

    private boolean isRoutingReleasable(String site, String routing , String routingVersion)throws Exception
    {
        boolean routingRecord = false;
        RoutingRequest routingRequest = RoutingRequest.builder().site(site).routing(routing).version(routingVersion).build();
        Routing existingRouting = webClientBuilder
                .build()
                .post()
                .uri(routingUrl)
                .body(BodyInserters.fromValue(routingRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Routing>() {
                })
                .block();
        if(existingRouting!=null && existingRouting.getRouting()!=null && !existingRouting.getRouting().isEmpty() && existingRouting.getStatus().equalsIgnoreCase("releasable"))
        {
            routingRecord = true;
        }
        return  routingRecord;
    }


}
