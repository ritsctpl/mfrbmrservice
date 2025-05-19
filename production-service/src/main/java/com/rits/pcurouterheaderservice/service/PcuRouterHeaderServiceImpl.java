
package com.rits.pcurouterheaderservice.service;

import com.rits.Utility.BOConverter;
import com.rits.pcurouterheaderservice.dto.*;
import com.rits.pcurouterheaderservice.exception.PcuRouterHeaderException;
import com.rits.pcurouterheaderservice.model.*;
import com.rits.pcurouterheaderservice.repository.PcuRouterHeaderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@ RequiredArgsConstructor
public class PcuRouterHeaderServiceImpl implements PcuRouterHeaderService {
    private final PcuRouterHeaderRepository pcuRouterHeaderRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${routing-service.url}/retrieve")
    private String routingUrl;
    @Value("${shoporder-service.url}/retrieve")
    private String shopOrderUrl;
    @Value("${pcuinqueue-service.url}/create")
    private String pcuInQueueUrl;
    @Value("${operation-service.url}/retrieve")
    private String operationUrl;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    @Override
    public MessageModel createPcuRouterHeader(PcuROuterHeaderCreateRequest pcuROuterHeaderCreateRequest) throws Exception {
        String site = pcuROuterHeaderCreateRequest.getSite();
        String pcuRouterBo = pcuROuterHeaderCreateRequest.getPcuRouterBo();
        List<Pcu> pcuBos = new ArrayList<>();
        List<PcuBo> placePcuBosList = new ArrayList<>();
        if(!pcuROuterHeaderCreateRequest.getPcuNumberList().isEmpty()){
            for(Map.Entry<String, Integer> pcus : pcuROuterHeaderCreateRequest.getPcuNumberList().entrySet()) {
                Pcu pcuNumber = Pcu.builder().pcuBo(String.valueOf(pcus.getKey())).build();
                pcuBos.add(pcuNumber);
            }
        } else
            pcuBos = pcuROuterHeaderCreateRequest.getPcuBos();

        List<RouterList> routerLists=new ArrayList<>();
        routerLists.add(new RouterList(pcuROuterHeaderCreateRequest.getPcuRouterBo(),"new"));
        String[] shopOrder=pcuROuterHeaderCreateRequest.getShopOrderBo().split(",");
        ShopOrderRequest shopOrderRequest= ShopOrderRequest.builder().site(site)
                .shopOrder(shopOrder[1]).build();

        ShopOrder shopOrderResponse = webClientBuilder.build()
                .post()
                .uri(shopOrderUrl)
                .bodyValue(shopOrderRequest)
                .retrieve()
                .bodyToMono(ShopOrder.class)
                .block();

        if (shopOrderResponse == null||shopOrderResponse.getShopOrder()==null) {
            throw new PcuRouterHeaderException(500, pcuRouterBo);
        }

        List<MessageDetails> messageDetailsList = new ArrayList<>();
        List<PcuRouterHeader> pcuRouterHeaders = new ArrayList<>();
        for (Pcu pcu : pcuBos) {
            String pcuBo = pcu.getPcuBo();

            if (pcuRouterHeaderRepository.existsByActiveAndSiteAndPcuBo(1, site, pcuBo)) {
                PcuRouterHeader retrievedRecord = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo);
                retrievedRecord.setActive(0);
                pcuRouterHeaders.add(retrievedRecord);
            }
        }
        if(pcuRouterHeaders.size()>0)
            pcuRouterHeaderRepository.saveAll(pcuRouterHeaders);
        String[] routerBo = pcuRouterBo.split(",");
        String router = routerBo[1];
        String version = routerBo[2];
        RoutingRequest routingRequest = RoutingRequest.builder()
                .site(site)
                .routing(router)
                .version(version)
                .build();

        Routing routingResponse = webClientBuilder.build()
                .post()
                .uri(routingUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(Routing.class)
                .block();

        if (routingResponse == null) {
            throw new PcuRouterHeaderException(500, pcuRouterBo);
        }

        List<Routing> routerList = new ArrayList<>();
        routerList.add(routingResponse);
        Router route = Router.builder()
                .isParentRoute(true)
                .parentRouteBO(routingResponse.getHandle())
                .r_route(routerList)
                .build();

        List<Router> routers = new ArrayList<>();
        routers.add(route);
        List<PcuRouterHeader> pcuRouterHeaderList = new ArrayList<>();
        for (Pcu pcu : pcuBos) {
            String pcuBo = pcu.getPcuBo();

            if(!pcuROuterHeaderCreateRequest.getPcuNumberList().isEmpty()){
                pcuROuterHeaderCreateRequest.setQtyInQueue(String.valueOf(pcuROuterHeaderCreateRequest.getPcuNumberList().get(pcuBo)));
            }

            PcuRouterHeader pcuRouterHeader = PcuRouterHeader.builder()
                    .site(site)
                    .handle("PcuRouterHeaderBo:" + site + "," + pcuBo)
                    .pcuBo(pcuBo)
                    .pcuRouterBo(pcuRouterBo)
                    .router(routers)
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .build();
            pcuRouterHeaderList.add(pcuRouterHeader);



            PcuBo placePcuBO=PcuBo.builder()
                    .site(site)
                    .pcuBO(pcuBo)
                    .router(routerLists)
                    .itemBO("ItemBO:"+site+","+shopOrderResponse.getPlannedMaterial()+","+shopOrderResponse.getMaterialVersion())
                    .userBO(pcuROuterHeaderCreateRequest.getUserBO())
                    .qtyInQueue(pcuROuterHeaderCreateRequest.getQtyInQueue())
                    .shopOrderBO(pcuROuterHeaderCreateRequest.getShopOrderBo())
                    .build();
            placePcuBosList.add(placePcuBO);
        }
        pcuRouterHeaderRepository.saveAll(pcuRouterHeaderList);

        for(PcuBo pcuBo : placePcuBosList) {
            placePCUQueueAtFirstOperation(site, pcuBo, pcuROuterHeaderCreateRequest.getUserBO());
            messageDetailsList.add(new MessageDetails(pcuBo.getPcuBO() + " created successfully", "S"));//check
        }
        return MessageModel.builder()
                .message_details(messageDetailsList)
                .build();
    }



    @Override
    public MessageModel updatePcuRouterHeader(PcuROuterHeaderCreateRequest pcuROuterHeaderCreateRequest) throws Exception {
        String site = pcuROuterHeaderCreateRequest.getSite();
        String pcuRouterBo = pcuROuterHeaderCreateRequest.getPcuRouterBo();
        List<Pcu> pcuBos = pcuROuterHeaderCreateRequest.getPcuBos();

        List<MessageDetails> messageDetailsList = new ArrayList<>();
        for (Pcu pcu : pcuBos) {
            String pcuBo = pcu.getPcuBo();

            if (!pcuRouterHeaderRepository.existsByActiveAndSiteAndPcuBo(1, site, pcuBo)) {
                throw new PcuRouterHeaderException(3502, pcuBo);
            }
        }

        String[] routerBo = pcuRouterBo.split(",");
        String router = routerBo[1];
        String version = routerBo[2];
        RoutingRequest routingRequest = RoutingRequest.builder()
                .site(site)
                .routing(router)
                .version(version)
                .build();

        Routing routingResponse = webClientBuilder.build()
                .post()
                .uri(routingUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(Routing.class)
                .block();

        if (routingResponse == null) {
            throw new PcuRouterHeaderException(500, pcuRouterBo);
        }

        List<Routing> routerList = new ArrayList<>();
        routerList.add(routingResponse);
        Router route = Router.builder()
                .isParentRoute(true)
                .parentRouteBO(routingResponse.getHandle())
                .r_route(routerList)
                .build();

        List<Router> routers = new ArrayList<>();
        routers.add(route);

        for (Pcu pcu : pcuBos) {
            String pcuBo = pcu.getPcuBo();


            PcuRouterHeader pcuRouterHeader = PcuRouterHeader.builder()
                    .site(site)
                    .handle("PcuRouterHeaderBo:" + site + "," + pcuBo)
                    .pcuBo(pcuBo)
                    .pcuRouterBo(pcuRouterBo)
                    .router(routers)
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .build();

            pcuRouterHeaderRepository.save(pcuRouterHeader);

            messageDetailsList.add(new MessageDetails(pcuBo + " updated successfully", "S"));
        }

        return MessageModel.builder()
                .message_details(messageDetailsList)
                .build();
    }

    @Override
    public PcuRouterHeader retrievePcuRouterHeader(PcuRouterHeaderRequest pcuRouterHeaderRequest) throws Exception {
        PcuRouterHeader pcuRouterHeader = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, pcuRouterHeaderRequest.getSite(), pcuRouterHeaderRequest.getPcuBo());

        if (pcuRouterHeader == null) {
            throw new PcuRouterHeaderException(3502, pcuRouterHeaderRequest.getPcuBo());
        }
        return pcuRouterHeader;
    }

    @Override
    public boolean isParentRoute(String site, String routing, String version, String PCU) throws Exception {
        PcuRouterHeader pcuRouterHeader = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, PCU);
        if (pcuRouterHeader == null && pcuRouterHeader.getPcuBo().isEmpty()) {
            throw new PcuRouterHeaderException(3502, PCU);
        }
        List<Router> routers = pcuRouterHeader.getRouter();
        if (routers == null || routers.isEmpty()) {
            throw new PcuRouterHeaderException(3504);
        }
        for (Router router : routers) {
            if (router.getR_route() != null) {
                for (Routing r : router.getR_route()) {
                    if (r.getRouting().equals(routing) && r.getVersion().equals(version)) {
                        return router.isParentRoute();
                    }
                    return false;
                }
            }
        }
        throw new PcuRouterHeaderException(3505, routing, version);
    }



    @Override
    public List<RoutingStep> getCurrentStep(String site, String PCU) throws Exception {
        List<RoutingStep> currentSteps = new ArrayList<>();
        PcuRouterHeader header = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, PCU);
        if (header == null) {
            throw new PcuRouterHeaderException(3502, PCU);
        }
        List<Router> routers = header.getRouter();
        if (routers == null || routers.isEmpty()) {
            throw new PcuRouterHeaderException(3500,PCU);
        }
        for (Router router : routers) {
            List<Routing> routings = router.getR_route();
            if (routings != null) {
                for (Routing routing : routings) {
                    if (routing.getStatus().equalsIgnoreCase("Active")) {
                        List<RoutingStep> routingSteps = routing.getRoutingStepList();
                        if (routingSteps != null) {
                            currentSteps.addAll(routingSteps);
                        }
                        else {
                            throw new PcuRouterHeaderException(3500,PCU);
                        }
                    }
                }
            }
        }
        if (currentSteps.isEmpty()) {
            throw new PcuRouterHeaderException(3500, PCU);
        }
        return currentSteps;
    }



    @Override
    public boolean isEntryStep(String site, String pcu, String currentRouter, String version, String operation) throws PcuRouterHeaderException {
        PcuRouterHeader header = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcu);
        if (header == null) {
            throw new PcuRouterHeaderException(3502, pcu);
        }
        List<Router> routers = header.getRouter();
        for (Router router : routers) {
            List<Routing> routings = router.getR_route();
            if (routings != null) {
                for (Routing routing : routings) {
                    if (routing.getRouting().equals(currentRouter) && routing.getVersion().equals(version)) {
                        List<RoutingStep> routingSteps = routing.getRoutingStepList();
                        if (routingSteps != null) {
                            for (RoutingStep routingStep : routingSteps) {
                                if (routingStep.getOperation().equals(operation)) {
                                    return routingStep.isEntryStep();
                                }
                            }
                            throw new PcuRouterHeaderException(3506, operation);
                        } else {
                            throw new PcuRouterHeaderException(3505, currentRouter, version);
                        }
                    }
                }
            }
        }
        throw new PcuRouterHeaderException(500, currentRouter, version);
    }


    @Override
    public String getRoutingType(String site, String router,String version) throws Exception {
        RoutingRequest routingRequest = RoutingRequest.builder().site(site).routing(router).version(version).build();
        Routing routingResponse = webClientBuilder.build()
                .post()
                .uri(routingUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(Routing.class)
                .block();
        if (routingResponse == null || routingResponse.getRoutingType() == null) {
            throw new PcuRouterHeaderException(500, router, version);
        }
        return routingResponse.getRoutingType();
    }


    @Override
    public String getRoutingSubType(String site, String router,String version) throws Exception {
        RoutingRequest routingRequest = RoutingRequest.builder().site(site).routing(router).version(version).build();
        Routing routingResponse = webClientBuilder.build()
                .post()
                .uri(routingUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(Routing.class)
                .block();
        if (routingResponse == null || routingResponse.getSubType() == null) {
            throw new PcuRouterHeaderException(500, router, version);
        }
        return routingResponse.getSubType();
    }

    @Override
    public boolean isLastReportingStep(String site, String pcu, String currentRouter, String version, String operation,String operationVersion) throws PcuRouterHeaderException {
        PcuRouterHeader header = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcu);
        if (header == null) {
            throw new PcuRouterHeaderException(3502, pcu);
        }
        List<Router> routers = header.getRouter();
        for (Router router : routers) {
            List<Routing> routings = router.getR_route();
            if (routings != null) {
                for (Routing routing : routings) {
                    if (routing.getRouting().equals(currentRouter) && routing.getVersion().equals(version)) {
                        List<RoutingStep> routingSteps = routing.getRoutingStepList();
                        if (routingSteps != null) {
                            for (RoutingStep routingStep : routingSteps) {
                                if (routingStep.getOperation().equals(operation)&&routingStep.getOperationVersion().equals(operationVersion)) {
                                    return routingStep.isLastReportingStep();
                                }
                            }
                            throw new PcuRouterHeaderException(3506, operation);
                        } else {
                            throw new PcuRouterHeaderException(3505, currentRouter, version);
                        }
                    }
                }
            }
        }
        throw new PcuRouterHeaderException(500, currentRouter, version);
    }



    @Override
    public List<String> getCurrentRouter(String site, String PCU) throws PcuRouterHeaderException {
        PcuRouterHeader header = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, PCU);
        if (header == null) {
            throw new PcuRouterHeaderException(3502, PCU);
        }
        List<Router> routers = header.getRouter();
        List<String> activeRouters = new ArrayList<>();

        for (Router router : routers) {
            List<Routing> routings = router.getR_route();
            if (routings != null) {
                for (Routing routing : routings) {
                    List<RoutingStep> routingSteps = routing.getRoutingStepList();
                    if (routingSteps != null) {
                        for (RoutingStep step : routingSteps) {
                            String qtyInWork = step.getQtyInWork();
                            if (!qtyInWork.isEmpty()) {  // Check if qtyInWork is not empty in operation,routingBo or Routing,version
                                int qtyInWorkInt = Integer.parseInt(qtyInWork);
                                if (qtyInWorkInt > 0) {
                                    activeRouters.add(routing.getRouting() + "," + routing.getVersion());
                                    break;
                                }
                            }
                        }
                    } else {
                        throw new PcuRouterHeaderException(3505, routing.getRouting(), routing.getVersion());
                    }
                }
            }
        }

        if (!activeRouters.isEmpty()) {
            return activeRouters;
        } else {
            throw new PcuRouterHeaderException(3504);
        }
    }
//    public PcuBoWithoutBO pcuCompleteBORemover(PcuBo pcuBo){
//
//        PcuBoWithoutBO pcuBoWithoutBO = new PcuBoWithoutBO();
//
//        if(pcuBo.getPcuBO()!=null && !pcuBo.getPcuBO().isEmpty()) {
//            String[] pcuArray = pcuBo.getPcuBO().split(",");
//            pcuBoWithoutBO.setPcu(pcuArray[1]);
//        }
//
//        if(pcuBo.getItemBO()!=null && !pcuBo.getItemBO().isEmpty()) {
//            String[] itemArray = pcuBo.getItemBO().split(",");
//            pcuBoWithoutBO.setItem(itemArray[1] + "/" + itemArray[2]);
//        }
//
////        if(pcuCompleteReqWithBO.getRouterBO()!=null && !pcuCompleteReqWithBO.getRouterBO().isEmpty()) {
////            String[] routerArray = pcuCompleteReqWithBO.getRouterBO().split(",");
////            completeReq.setRouter(routerArray[1] + "/" + routerArray[2]);
////        }
//
////        if(pcuCompleteReqWithBO.getOperationBO()!=null && !pcuCompleteReqWithBO.getOperationBO().isEmpty()) {
////            String[] opArray = pcuCompleteReqWithBO.getOperationBO().split(",");
////            completeReq.setOperation(opArray[1] + "/" + opArray[2]);
////        }
////
////        if(pcuCompleteReqWithBO.getResourceBO()!=null && !pcuCompleteReqWithBO.getResourceBO().isEmpty()) {
////            String[] resourceArray = pcuCompleteReqWithBO.getResourceBO().split(",");
////            completeReq.setResource(resourceArray[1]);
////        }
//
//        if(pcuBo.getUserBO()!=null && !pcuBo.getUserBO().isEmpty()) {
//            String[] userArray = pcuBo.getUserBO().split(",");
//            pcuBoWithoutBO.setUser(userArray[1]);
//        }
//
//        if(pcuBo.getShopOrderBO()!=null && !pcuBo.getShopOrderBO().isEmpty()) {
//            String[] shopOrderArray = pcuBo.getShopOrderBO().split(",");
//            pcuBoWithoutBO.setShopOrder(shopOrderArray[1]);
//        }
//
////        if(pcuCompleteReqWithBO.getChildRouterBO()!=null && !pcuCompleteReqWithBO.getChildRouterBO().isEmpty()) {
////            String[] childRouterArray = pcuCompleteReqWithBO.getChildRouterBO().split(",");
////            completeReq.setChildRouter(childRouterArray[1] + "/" + childRouterArray[2]);
////        }
//
//        return pcuBoWithoutBO;
//    }
    @Override
    public PcuInQueue pcuReleaseAtEntryStep(String site, PcuBo pcuBo, String router, String version,String userBO) throws PcuRouterHeaderException {
        if (pcuRouterHeaderRepository.existsByActiveAndSiteAndPcuBo(1, site, pcuBo.getPcuBO())) {
            PcuRouterHeader pcuRouterHeader = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo.getPcuBO());
            List<Router> routers = pcuRouterHeader.getRouter();
            for (Router r : routers) {
                List<Routing> rRoutes = r.getR_route();
                for (Routing rRoute : rRoutes) {
                    List<RoutingStep> routingSteps = rRoute.getRoutingStepList();
                    if (rRoute.getRouting().equals(router) && rRoute.getVersion().equals(version)) {
                        for (RoutingStep routingStep : routingSteps) {
                            if (routingStep.isEntryStep()) {

                                String resource = getResourceBO(site,routingStep.getOperation(),routingStep.getOperationVersion());
                                String pcu = BOConverter.getPcu(pcuBo.getPcuBO());
                                String item = BOConverter.getItem(pcuBo.getItemBO());
                                String itemVersion = BOConverter.getItemVersion(pcuBo.getItemBO());
                                String shopOrder = BOConverter.getShopOrder(pcuBo.getShopOrderBO());
                                String routering = BOConverter.getRouter(pcuBo.getRouter().get(0).getPcuRouterBO());
                                String routeringVersion = BOConverter.getRouterVersion(pcuBo.getRouter().get(0).getPcuRouterBO());
                                String user = BOConverter.getUser(userBO);

                                PcuInQueueWithoutBO pcuInQueueRequest = PcuInQueueWithoutBO.builder()
                                        .site(pcuBo.getSite())
                                        .pcu(pcu)
                                        .item(item)
                                        .itemVersion(itemVersion)
                                        .shopOrder(shopOrder)
                                        .router(routering)
                                        .routerVersion(routeringVersion)
                                        .operation(routingStep.getOperation())
                                        .operationVersion(routingStep.getOperationVersion())
                                        .stepID(routingStep.getStepId())
                                        .user(user)
                                        .resource(resource)
                                        .qtyToComplete(pcuBo.getQtyInQueue())
                                        .qtyInQueue(pcuBo.getQtyInQueue())
                                        .build();

                                InQueueMessageModel pcuInQueue = webClientBuilder.build()
                                        .post()
                                        .uri(pcuInQueueUrl)
                                        .bodyValue(pcuInQueueRequest)
                                        .retrieve()
                                        .bodyToMono(InQueueMessageModel.class)
                                        .block();
                                if (pcuInQueue != null&&pcuInQueue.getResponse()!=null) {
                                    return pcuInQueue.getResponse();
                                } else {
                                    throw new PcuRouterHeaderException(3600, pcuBo);
                                }
                            }
                        }
                    }
                }
            }
            throw new PcuRouterHeaderException(3505, pcuBo);
        }
        throw new PcuRouterHeaderException(3502, pcuBo);
    }

    private String getResourceBO(String site, String operation, String operationVersion) {
        PcuRelease pcuRelease= PcuRelease.builder().site(site).operation(operation).revision(operationVersion).build();
        Operation operationResponse = webClientBuilder.build()
                .post()
                .uri(operationUrl)
                .bodyValue(pcuRelease)
                .retrieve()
                .bodyToMono(Operation.class)
                .block();
        if (operationResponse == null|| operationResponse.getOperation()==null) {
            throw new PcuRouterHeaderException(800);
        }
        return operationResponse.getDefaultResource();

    }

    @Override
    public String callExtension(Extension extension) throws Exception {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new PcuRouterHeaderException(800);
        }
        return extensionResponse;
    }

    @Override
    public EntryStep getAllEntryStep(String site, String pcuBo, String routing, String version) throws PcuRouterHeaderException {
//        PcuRouterHeader header = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo);
//        if (header == null) {
//            throw new PcuRouterHeaderException(3502, pcuBo);
//        }
//
//        List<RoutingStep> entrySteps = new ArrayList<>();
//
//        for (Router router : header.getRouter()) {
//            List<Routing> routings = router.getR_route();
//            if (routings != null) {
//                for (Routing r : routings) {
//                    if (r.getRouting().equals(routing) && r.getVersion().equals(version)) {
//                        RoutingRequest routingRequest = RoutingRequest.builder().site(site).routing(routing).version(version).build();
//                        Routing routingResponse = webClientBuilder.build()
//                                .post()
//                                .uri(routingUrl)
//                                .bodyValue(routingRequest)
//                                .retrieve()
//                                .bodyToMono(Routing.class)
//                                .block();
//                        if (routingResponse == null || routingResponse.getRouting() == null) {
//                            throw new PcuRouterHeaderException(500, router, version);
//                        }
//
//                        List<RoutingStep> routingSteps = routingResponse.getRoutingStepList();
//                        if (routingSteps != null) {
//                            for (RoutingStep step : routingSteps) {
//                                if (step.isEntryStep()) {
//                                    entrySteps.add(step);
//                                }
//                            }
//                        } else {
//                            throw new PcuRouterHeaderException(3505, routing, version);
//                        }
//                    }
//                }
//            } else {
//                throw new PcuRouterHeaderException(500, routing, version);
//            }
//        }
//
//        if (entrySteps.isEmpty()) {
//            throw new PcuRouterHeaderException(3504);
//        }
//
//        return EntryStep.builder()
//                .pcuBo(pcuBo)
//                .router(header.getHandle())
//                .version(version)
//                .routingStepList(entrySteps)
//                .build();
        PcuRouterHeader header = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo);
        if (header == null) {
            throw new PcuRouterHeaderException(3502, pcuBo);
        }

        List<RoutingStep> entrySteps = new ArrayList<>();


        for (Router routerObj : header.getRouter()) {
            List<Routing> routings = routerObj.getR_route();
            if (routings != null) {
                for (Routing route : routings) {
                    List<RoutingStep> routingSteps = route.getRoutingStepList();
                    if (routingSteps != null) {
                        for (RoutingStep step : routingSteps) {
                            List<Routing> innerRoutings =step.getRouterDetails();
                            if(innerRoutings!=null) {
                                for (Routing innerRouting : innerRoutings) {
                                    List<RoutingStep> innerRoutingSteps=innerRouting.getRoutingStepList();
                                    if (innerRouting.getRouting().equals(routing) && innerRouting.getVersion().equals(version)) {
                                        for (RoutingStep innerRoutingStep : innerRoutingSteps)
                                        {
                                            if(innerRoutingStep.isEntryStep()){
                                                entrySteps.add(innerRoutingStep);
                                            }
                                        }

                                    }
                                }
                            }else{  throw new PcuRouterHeaderException(3505, routing, version);}

                        }
                    }
                }
            }
        }

        if (entrySteps.isEmpty()) {
            throw new PcuRouterHeaderException(3504);
        }


        return EntryStep.builder()
                .pcuBo(pcuBo)
                .router(routing)
                .version(version)
                .routingStepList(entrySteps)
                .build();

    }




    @Override
    public EntryStep getEntryStep(String site, String pcuBo) throws Exception {
        PcuRouterHeader header = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo);
        if (header == null) {
            throw new PcuRouterHeaderException(3502, pcuBo);
        }

        List<RoutingStep> entrySteps = new ArrayList<>();
        String router = null;
        String version = null;

        for (Router routerObj : header.getRouter()) {
            List<Routing> routings = routerObj.getR_route();
            if (routings != null) {
                for (Routing routing : routings) {
                    List<RoutingStep> routingSteps = routing.getRoutingStepList();
                    if (routingSteps != null) {
                        for (RoutingStep step : routingSteps) {
                            if (step.isEntryStep()) {
                                entrySteps.add(step);
                                router = routing.getRouting();
                                version = routing.getVersion();
                            }
                        }
                    } else {
                        throw new PcuRouterHeaderException(3505, router, version);
                    }
                }
            } else {
                throw new PcuRouterHeaderException(500, router, version);
            }
        }

        if (entrySteps.isEmpty()) {
            throw new PcuRouterHeaderException(3504);
        }

        return EntryStep.builder()
                .pcuBo(pcuBo)
                .router(router)
                .version(version)
                .routingStepList(entrySteps)
                .build();

    }

    @Override
    public PcuInQueue placePCUQueueAtFirstOperation(String site, PcuBo pcuBo,String userBO) throws Exception {
        PcuRouterHeader header = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo.getPcuBO());
        if (header == null) {
            throw new PcuRouterHeaderException(3502, pcuBo.getPcuBO());
        }

        List<Router> routers = header.getRouter();
        for (Router router : routers) {
            List<Routing> routings = router.getR_route();
            if (routings != null) {
                for (Routing routing : routings) {
                    if(routing.getSubType().equalsIgnoreCase("simultaneous")|| routing.getSubType().equalsIgnoreCase("anyOrder")){

                        return placePCUQueueForAllStep(site,pcuBo,routing.getRouting(),routing.getVersion(),userBO);
                    }
                    List<RoutingStep> routingSteps = routing.getRoutingStepList();
                    for (RoutingStep step : routingSteps) {
                        if (step.isEntryStep() ){
                            if( step.getStepType().equalsIgnoreCase("operation")) {
                                String route = routing.getRouting();
                                String version = routing.getVersion();
                                return pcuReleaseAtEntryStep(site, pcuBo, route, version,userBO);
                            }else {
                                String route = routing.getRouting();
                                String version = routing.getVersion();
                                String childRouteBO = step.getRoutingBO();
                                return placePCUQueueForRoutingStep(site, pcuBo, route, version, childRouteBO,userBO);
                            }
                        }
                    }
                }
            } else {
                throw new PcuRouterHeaderException(3505, pcuBo);
            }
        }

        throw new PcuRouterHeaderException(3504);
    }

    @Override
    public PcuInQueue placePCUQueueForRoutingStep(String site, PcuBo pcuBo, String routing, String version, String childRouteBO,String userBO) throws Exception {
        if (pcuRouterHeaderRepository.existsByActiveAndSiteAndPcuBo(1, site, pcuBo.getPcuBO())) {
            PcuRouterHeader pcuRouterHeader = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo.getPcuBO());
            List<Router> routers = pcuRouterHeader.getRouter();
            String operation=null;
            String stepId=null;
            InQueueMessageModel pcuInQueue = null;
            for (Router r : routers) {
                List<Routing> rRoutes = r.getR_route();
                for (Routing rRoute : rRoutes) {
                    List<RoutingStep> routingSteps = rRoute.getRoutingStepList();
                    if (rRoute.getRouting().equals(routing) && rRoute.getVersion().equals(version)) {
                        for (RoutingStep routingStep : routingSteps) {
                            if (routingStep.isEntryStep()&& !routingStep.getRouterDetails().isEmpty()) {
                                String[] childRouteBOs = childRouteBO.split(",");
                                for(Routing innerRouting: routingStep.getRouterDetails()){
                                    if(innerRouting.getRouting().equalsIgnoreCase(childRouteBOs[1])&&innerRouting.getVersion().equalsIgnoreCase(childRouteBOs[2])){
                                        for(RoutingStep innerRoutingStep: innerRouting.getRoutingStepList()){
                                            if(innerRoutingStep.isEntryStep()){
//                                                operation = innerRoutingStep.getOperation()+"/"+innerRoutingStep.getOperationVersion();
//                                                stepId = innerRoutingStep.getStepId();
//                                                String resourceBO = getResourceBO(site,innerRoutingStep.getOperation(),innerRoutingStep.getOperationVersion());
//
//                                                String[] routerArray = pcuBo.getRouter().get(0).getPcuRouterBO().split(",");
//                                                pcuBo.setUserBO(userBO);
//
//                                                String[] childRouterArray = childRouteBO.split(",");


                                                String resource = getResourceBO(site,innerRoutingStep.getOperation(),innerRoutingStep.getOperationVersion());
                                                String pcu = BOConverter.getPcu(pcuBo.getPcuBO());
                                                String item = BOConverter.getItem(pcuBo.getItemBO());
                                                String itemVersion = BOConverter.getItemVersion(pcuBo.getItemBO());
                                                String shopOrder = BOConverter.getShopOrder(pcuBo.getShopOrderBO());
                                                String routering = BOConverter.getRouter(pcuBo.getRouter().get(0).getPcuRouterBO());
                                                String routeringVersion = BOConverter.getRouterVersion(pcuBo.getRouter().get(0).getPcuRouterBO());
                                                String user = BOConverter.getUser(userBO);
                                                String childRouter = BOConverter.getChildRouter(childRouteBO);
                                                String childRouterVersion = BOConverter.getChildRouterVersion(childRouteBO);

                                                PcuInQueueWithoutBO pcuInQueueRequest = PcuInQueueWithoutBO.builder()
                                                        .site(pcuBo.getSite())
                                                        .pcu(pcu)
                                                        .item(item)
                                                        .itemVersion(itemVersion)
                                                        .shopOrder(shopOrder)
                                                        .router(routering)
                                                        .routerVersion(routeringVersion)
                                                        .operation(innerRoutingStep.getOperation())
                                                        .operationVersion(innerRoutingStep.getOperationVersion())
                                                        .stepID(innerRoutingStep.getStepId())
                                                        .user(user)
                                                        .resource(resource)
                                                        .qtyToComplete(pcuBo.getQtyInQueue())
                                                        .qtyInQueue(pcuBo.getQtyInQueue())
                                                        .childRouter(childRouter)
                                                        .childRouterVersion(childRouterVersion)
                                                        .parentStepID(routingStep.getStepId())
                                                        .build();

                                                pcuInQueue = webClientBuilder.build()
                                                        .post()
                                                        .uri(pcuInQueueUrl)
                                                        .bodyValue(pcuInQueueRequest)
                                                        .retrieve()
                                                        .bodyToMono(InQueueMessageModel.class)
                                                        .block();
                                            }
                                        }
                                    }
                                }


                                if (pcuInQueue != null&&pcuInQueue.getResponse()!=null) {
                                    return pcuInQueue.getResponse();
                                } else {
                                    throw new PcuRouterHeaderException(3600, pcuBo);
                                }
                            }
                        }
                    }
                }
            }
            throw new PcuRouterHeaderException(3505, pcuBo);
        }
        throw new PcuRouterHeaderException(3502, pcuBo);
    }

    @Override
    public PcuInQueue placePCUQueueForAllStep(String site, PcuBo pcuBo, String routing, String version,String userBO) throws Exception {
        InQueueMessageModel pcuInQueue = null;
        if (pcuRouterHeaderRepository.existsByActiveAndSiteAndPcuBo(1, site, pcuBo.getPcuBO())) {
            PcuRouterHeader pcuRouterHeader = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo.getPcuBO());
            List<Router> routers = pcuRouterHeader.getRouter();
            String operation = null;
            String stepId = null;
            for (Router r : routers) {
                List<Routing> rRoutes = r.getR_route();
                for (Routing rRoute : rRoutes) {
                    List<RoutingStep> routingSteps = rRoute.getRoutingStepList();
                    if (rRoute.getRouting().equals(routing) && rRoute.getVersion().equals(version)) {
                        for (RoutingStep routingStep : routingSteps) {
                            if (routingStep.isEntryStep()&&routingStep.getStepType().equalsIgnoreCase("Operation")) {
//                                operation = routingStep.getOperation()+"/"+routingStep.getOperationVersion();
//                                stepId = routingStep.getStepId();
//                                String resourceBO = getResourceBO(site,routingStep.getOperation(),routingStep.getOperationVersion());
//
//                                String[] routerArray = pcuBo.getRouter().get(0).getPcuRouterBO().split(",");
//                                pcuBo.setUserBO(userBO);


                                String resource = getResourceBO(site,routingStep.getOperation(),routingStep.getOperationVersion());
                                String pcu = BOConverter.getPcu(pcuBo.getPcuBO());
                                String item = BOConverter.getItem(pcuBo.getItemBO());
                                String itemVersion = BOConverter.getItemVersion(pcuBo.getItemBO());
                                String shopOrder = BOConverter.getShopOrder(pcuBo.getShopOrderBO());
                                String routering = BOConverter.getRouter(pcuBo.getRouter().get(0).getPcuRouterBO());
                                String routeringVersion = BOConverter.getRouterVersion(pcuBo.getRouter().get(0).getPcuRouterBO());
                                String user = BOConverter.getUser(userBO);

                                PcuInQueueWithoutBO pcuInQueueRequest = PcuInQueueWithoutBO.builder()
                                        .site(pcuBo.getSite())
                                        .pcu(pcu)
                                        .item(item)
                                        .itemVersion(itemVersion)
                                        .shopOrder(shopOrder)
                                        .router(routering)
                                        .routerVersion(routeringVersion)
                                        .operation(routingStep.getOperation())
                                        .operationVersion(routingStep.getOperationVersion())
                                        .stepID(routingStep.getStepId())
                                        .user(user)
                                        .resource(resource)
                                        .qtyToComplete(pcuBo.getQtyInQueue())
                                        .qtyInQueue(pcuBo.getQtyInQueue())
                                        .parentStepID(routingStep.getStepId())
                                        .build();

                                pcuInQueue = webClientBuilder.build()
                                        .post()
                                        .uri(pcuInQueueUrl)
                                        .bodyValue(pcuInQueueRequest)
                                        .retrieve()
                                        .bodyToMono(InQueueMessageModel.class)
                                        .block();
                            }
                        }
                    }
                }
                return pcuInQueue.getResponse();
            }
            throw new PcuRouterHeaderException(3505, pcuBo);
        }
        throw new PcuRouterHeaderException(3502, pcuBo);
    }


    @Override
    public PcuInQueue placePCUQueueAtSpecificOperation(String site, PcuBo pcuBo, String routing, String version, String operation,String userBO) throws Exception {
        PcuRouterHeader header = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo.getPcuBO());
        if (header == null) {
            throw new PcuRouterHeaderException(3502, pcuBo.getPcuBO());
        }

        List<Router> routers = header.getRouter();
        for (Router router : routers) {
            List<Routing> routings = router.getR_route();
            if (routings != null) {
                for (Routing r : routings) {
                    if (r.getRouting().equals(routing) && r.getVersion().equals(version)) {
                        List<RoutingStep> routingSteps = r.getRoutingStepList();
                        for (RoutingStep step : routingSteps) {
                            if (step.isEntryStep() && step.getStepType().equalsIgnoreCase("operation") && step.getOperation().equals(operation)) {
                                return pcuReleaseAtEntryStep(site, pcuBo, routing, version,userBO);
                            }
                        }
                    }
                }
            } else {
                throw new PcuRouterHeaderException(3505, pcuBo);
            }
        }

        throw new PcuRouterHeaderException(3504, pcuBo);
    }

    @Override
    public RoutingStep getStepDetails(String site, String routing,String version, String operation, String pcuBo) throws Exception {
        PcuRouterHeader header = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo);
        if (header == null) {
            throw new PcuRouterHeaderException(3502, pcuBo);
        }
        RoutingRequest routingRequest = RoutingRequest.builder().site(site).routing(routing).version(version).build();
        Routing routingResponse = webClientBuilder.build()
                .post()
                .uri(routingUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(Routing.class)
                .block();
        if (routingResponse == null || routingResponse.getRoutingStepList() == null) {
            throw new PcuRouterHeaderException(500, routing, version);
        }
        List<RoutingStep> routingSteps=routingResponse.getRoutingStepList();
        for(RoutingStep step: routingSteps){
            if(step.getStepType().equalsIgnoreCase("operation") &&step.getOperation().equals(operation)){
                return step;
            }
        }
//        List<Router> routers = header.getRouter();
//        for (Router router : routers) {
//            List<Routing> routings = router.getR_route();
//            if (routings != null) {
//                for (Routing r : routings) {
//                    if (r.getRouting().equals(routing) && r.getVersion().equals(version)) {
//                        List<RoutingStep> routingSteps = r.getRoutingStepList();
//                        for (RoutingStep step : routingSteps) {
//                            if (step.getStepType().equalsIgnoreCase("operation") && step.getOperation().equals(operation)) {
//
//                                return step;
//                            }
//
//                        }
//                    }throw new PcuRouterHeaderException(500,routing,version);
//                }
//            }
//        }
        return null;
    }

    @Override
    public MessageModel getOperationQueueList(String site, String routing, String version, String operation, String pcuBo,String operationVersion) throws Exception {
        if (pcuRouterHeaderRepository.existsByActiveAndSiteAndPcuBo(1, site, pcuBo)) {
            PcuRouterHeader pcuRouterHeader = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo);
            List<Router> routers = pcuRouterHeader.getRouter();
            String childRouteBo="RoutingBo:"+site+","+routing+","+version;
            String[] needsToBeComplete=null;
            String[] nextStepId = null;
            List<RoutingStep> responseRoutingSteps=new ArrayList<>();
            for (Router r : routers) {
                List<Routing> rRoutes = r.getR_route();
                for (Routing rRoute : rRoutes) {
                    if(rRoute.getRouting().equalsIgnoreCase(routing)&& rRoute.getVersion().equalsIgnoreCase(version)){
                        List<RoutingStep> routingSteps = rRoute.getRoutingStepList();
                        for(RoutingStep  routingStep:routingSteps){
                            if(routingStep.getStepType().equalsIgnoreCase("operation")&&routingStep.getOperation().equalsIgnoreCase(operation)&&routingStep.getOperationVersion().equalsIgnoreCase(operationVersion)){
                                if(routingStep.getNextStepId().equals("00")||routingStep.getNextStepId().equals("")) {
                                    needsToBeComplete = routingStep.getNeedToBeCompleted().split(",");
                                }
                                else{
                                    nextStepId=routingStep.getNextStepId().split(",");

                                }
                            }
                        }
                        if(needsToBeComplete==null && nextStepId==null){
                            throw new PcuRouterHeaderException(510, operation);
                        }
                        if(nextStepId==null) {
                            for (RoutingStep routingStep : routingSteps) {
                                for (String nextStep : needsToBeComplete) {

                                    if (routingStep.getStepId().equals(nextStep)) {
                                        responseRoutingSteps.add(routingStep);
                                    }
                                }
                            }

                        }
                        else {
                            for (RoutingStep routingStep : routingSteps) {
                                for (String nextStep : nextStepId) {

                                    if (routingStep.getStepId().equals(nextStep)) {
                                        responseRoutingSteps.add(routingStep);
                                    }
                                }
                            }

                        }
                        return  MessageModel.builder().messagedetails(new MessageDetails("RoutingStep","S")).routingStep(responseRoutingSteps).build();
                    }else {
                        for (RoutingStep routingStep : rRoute.getRoutingStepList()) {
                            if (routingStep.getStepType().equalsIgnoreCase("routing") && routingStep.getRoutingBO().equalsIgnoreCase(childRouteBo)) {

                                for(Routing innerRouting: routingStep.getRouterDetails()){
                                    for(RoutingStep innerRoutingStep:innerRouting.getRoutingStepList()){
                                        if(innerRoutingStep.getStepType().equalsIgnoreCase("operation")&& innerRoutingStep.getOperation().equalsIgnoreCase(operation)&& innerRoutingStep.getOperationVersion().equalsIgnoreCase(operationVersion)){
                                            if(innerRoutingStep.getNextStepId().equals("00")||innerRoutingStep.getNextStepId().equals("")) {
                                                needsToBeComplete = innerRoutingStep.getNeedToBeCompleted().split(",");
                                            }
                                            else{
                                                nextStepId=innerRoutingStep.getNextStepId().split(",");

                                            }
                                        }
                                    }
                                    if(needsToBeComplete==null && nextStepId==null){
                                        throw new PcuRouterHeaderException(510, operation);
                                    }
                                    if(nextStepId==null) {
                                        for (RoutingStep innerRoutingStep : innerRouting.getRoutingStepList()) {
                                            for (String nextStep : needsToBeComplete) {

                                                if (innerRoutingStep.getStepId().equals(nextStep)) {
                                                    responseRoutingSteps.add(innerRoutingStep);
                                                }
                                            }
                                        }

                                    }
                                    else {
                                        for (RoutingStep innerRoutingStep : innerRouting.getRoutingStepList()) {
                                            for (String nextStep : nextStepId) {

                                                if (innerRoutingStep.getStepId().equals(nextStep)) {
                                                    responseRoutingSteps.add(innerRoutingStep);
                                                }
                                            }
                                        }

                                    }
                                    return  MessageModel.builder().messagedetails(new MessageDetails("RoutingStep","S")).routingStep(responseRoutingSteps).build();


                                }
                            }
                        }
                    }


                }
            }
            return null;
        }
        throw new PcuRouterHeaderException(3504, pcuBo);
    }

    @Override
    public MessageModel getOperationNextStepID(String site, String routing, String version, String operation, String pcuBo) throws Exception {
        if (pcuRouterHeaderRepository.existsByActiveAndSiteAndPcuBo(1, site, pcuBo)) {
            PcuRouterHeader pcuRouterHeader = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo);
            List<Router> routers = pcuRouterHeader.getRouter();
            String childRouteBo="RoutingBo:"+site+","+routing+","+version;

            String nextStepId = null;

            for (Router r : routers) {
                List<Routing> rRoutes = r.getR_route();
                for (Routing rRoute : rRoutes) {
                    if(rRoute.getRouting().equalsIgnoreCase(routing)&& rRoute.getVersion().equalsIgnoreCase(version)){
                        List<RoutingStep> routingSteps = rRoute.getRoutingStepList();
                        for(RoutingStep  routingStep:routingSteps){
                            if(routingStep.getStepType().equalsIgnoreCase("operation")&&routingStep.getOperation().equalsIgnoreCase(operation)){
                                if(routingStep.getNextStepId().equals("00")||routingStep.getNextStepId().equals("")) {
                                    nextStepId = routingStep.getNeedToBeCompleted();
                                }
                                else{
                                    nextStepId = routingStep.getNextStepId();

                                }
                            }
                        }
                        if( nextStepId==null){
                            throw new PcuRouterHeaderException(510, operation);
                        }
                        return MessageModel.builder().messagedetails(new MessageDetails("NextStepId","S")).nextStepId(nextStepId).build();

                    }else {
                        for (RoutingStep routingStep : rRoute.getRoutingStepList()) {
                            if (routingStep.getStepType().equalsIgnoreCase("routing") && routingStep.getRoutingBO().equalsIgnoreCase(childRouteBo)) {

                                for(Routing innerRouting: routingStep.getRouterDetails()){
                                    for(RoutingStep innerRoutingStep:innerRouting.getRoutingStepList()){
                                        if(innerRoutingStep.getStepType().equalsIgnoreCase("operation")&& innerRoutingStep.getOperation().equalsIgnoreCase(operation)){
                                            if(routingStep.getNextStepId().equals("00")||routingStep.getNextStepId().equals("")) {
                                                nextStepId = routingStep.getNeedToBeCompleted();
                                            }
                                            else{
                                                nextStepId = routingStep.getNextStepId();

                                            }
                                        }
                                    }
                                    if(nextStepId==null){
                                        throw new PcuRouterHeaderException(510, operation);
                                    }
                                    return MessageModel.builder().messagedetails(new MessageDetails("NextStepId","S")).nextStepId(nextStepId).build();

                                }
                            }
                        }
                    }


                }
            }
        }
        throw new PcuRouterHeaderException(3504, pcuBo);
    }

    @Override
    public List<RoutingStep> getStepDetailsList(String site, String routing,String version, String operation, String pcuBo) throws Exception {
        PcuRouterHeader header = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo);
        if (header == null) {
            throw new PcuRouterHeaderException(3502, pcuBo);
        }
        RoutingRequest routingRequest = RoutingRequest.builder().site(site).routing(routing).version(version).build();
        Routing routingResponse = webClientBuilder.build()
                .post()
                .uri(routingUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(Routing.class)
                .block();
        if (routingResponse == null || routingResponse.getRoutingStepList() == null) {
            throw new PcuRouterHeaderException(500, routing, version);
        }
        List<RoutingStep> routingSteps=routingResponse.getRoutingStepList();
        for(RoutingStep step: routingSteps){
            if(step.getStepType().equalsIgnoreCase("operation") &&step.getOperation().equals(operation)){
                return routingSteps;
            }
        }
        return null;
    }

    @Override
    public MessageModel updateNeedsToBeCompleted(String site, String pcuBo, String routing, String version, String operation, String stepID,String operationVersion) throws Exception {
        if (pcuRouterHeaderRepository.existsByActiveAndSiteAndPcuBo(1, site, pcuBo)) {
            PcuRouterHeader pcuRouterHeader = pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(1, site, pcuBo);
            List<Router> routers = pcuRouterHeader.getRouter();
            String childRouteBo = "RoutingBo:" + site + "," + routing + "," + version;

            for (Router r : routers) {
                List<Routing> rRoutes = r.getR_route();
                for (Routing rRoute : rRoutes) {
                    if (rRoute.getRouting().equalsIgnoreCase(routing) && rRoute.getVersion().equalsIgnoreCase(version)) {
                        List<RoutingStep> routingSteps = rRoute.getRoutingStepList();
                        for (RoutingStep routingStep : routingSteps) {
                            //check for version
                            if (routingStep.getStepType().equalsIgnoreCase("operation") && routingStep.getOperation().equalsIgnoreCase(operation)&& routingStep.getOperationVersion().equalsIgnoreCase(operationVersion)) {
                                if (routingStep.getStepId().equalsIgnoreCase(stepID)) {
                                    updateNeedsToBeCompletedInSteps(routingSteps, stepID);
                                }
                            }
                        }
                        rRoute.setRoutingStepList(routingSteps);
                        pcuRouterHeaderRepository.save(pcuRouterHeader);

                        return MessageModel.builder()
                                .messagedetails(new MessageDetails("Needs To Be Completed Updated Successfully", "S"))
                                .build();
                    } else {
                        for (RoutingStep routingStep : rRoute.getRoutingStepList()) {
                            if (routingStep.getStepType().equalsIgnoreCase("routing") && routingStep.getRoutingBO().equalsIgnoreCase(childRouteBo)) {
                                for (Routing innerRouting : routingStep.getRouterDetails()) {
                                    for (RoutingStep innerRoutingStep : innerRouting.getRoutingStepList()) {
                                        //check forn version too
                                        if (innerRoutingStep.getStepType().equalsIgnoreCase("operation") && innerRoutingStep.getOperation().equalsIgnoreCase(operation)&& innerRoutingStep.getOperationVersion().equalsIgnoreCase(operationVersion)) {
                                            if (innerRoutingStep.getStepId().equalsIgnoreCase(stepID)) {
                                                updateNeedsToBeCompletedInSteps(innerRouting.getRoutingStepList(), stepID);
                                            }
                                        }
                                    }
                                }
                                pcuRouterHeaderRepository.save(pcuRouterHeader);

                                return MessageModel.builder()
                                        .messagedetails(new MessageDetails("Needs To Be Completed Updated Successfully", "S"))
                                        .build();
                            }
                        }
                    }
                }
            }
        }
        throw new PcuRouterHeaderException(3504, pcuBo);
    }
    private void updateNeedsToBeCompletedInSteps(List<RoutingStep> routingSteps, String stepIDToRemove) {
        for (RoutingStep routingStep : routingSteps) {
            String[] needsToBeCompleted = routingStep.getNeedToBeCompleted().split(",");
            List<String> updatedNeedsToBeCompleted = new ArrayList<>();

            for (String step : needsToBeCompleted) {
                if (!step.equals(stepIDToRemove)) {
                    updatedNeedsToBeCompleted.add(step);
                }
            }

            routingStep.setNeedToBeCompleted(String.join(",", updatedNeedsToBeCompleted));
        }
    }
    @Override
    public Boolean disableRecord(PcuRouterHeaderRequest pcuRouterHeaderRequest){
        Boolean deleted=false;
        try {
          PcuRouterHeader responce= retrievePcuRouterHeader(pcuRouterHeaderRequest);
            String PCU=responce.getPcuBo();
            PcuRouterHeader pcuRouterHeader = PcuRouterHeader.builder()
                    .site(responce.getSite())
                    .handle("PcuRouterHeaderBo:" + responce.getSite() + "," + PCU)
                    .pcuBo(responce.getPcuBo())
                    .pcuRouterBo(responce.getPcuRouterBo())
                    .router(responce.getRouter())
                    .active(0)
                    .createdDateTime(LocalDateTime.now())
                    .build();

            pcuRouterHeaderRepository.save(pcuRouterHeader);
            deleted=true;
        } catch (Exception e) {
            deleted=false;
            throw new RuntimeException(e);
        }
        return deleted;
    }

    @Override
    public Boolean enableRecord(PcuRouterHeaderRequest pcuRouterHeaderRequest){
        Boolean deleted=false;
        try {
            PcuRouterHeader responce= pcuRouterHeaderRepository.findByActiveAndSiteAndPcuBo(0, pcuRouterHeaderRequest.getSite(), pcuRouterHeaderRequest.getPcuBo());
            String PCU=responce.getPcuBo();
            PcuRouterHeader pcuRouterHeader = PcuRouterHeader.builder()
                    .site(responce.getSite())
                    .handle("PcuRouterHeaderBo:" + responce.getSite() + "," + PCU)
                    .pcuBo(responce.getPcuBo())
                    .pcuRouterBo(responce.getPcuRouterBo())
                    .router(responce.getRouter())
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .build();

            pcuRouterHeaderRepository.save(pcuRouterHeader);
            deleted=true;
        } catch (Exception e) {
            deleted=false;
            throw new RuntimeException(e);
        }
        return deleted;
    }

}