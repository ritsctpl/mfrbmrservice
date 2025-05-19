package com.rits.pcucompleteservice.service;

import com.rits.Utility.BOConverter;
import com.rits.dispositionlogservice.dto.DispositionLogRequest;
import com.rits.dispositionlogservice.model.DispositionLog;
import com.rits.nonconformanceservice.dto.DispositionRequest;
import com.rits.pcucompleteservice.dto.*;
import com.rits.pcucompleteservice.exception.PcuCompleteException;
import com.rits.pcucompleteservice.model.MessageDetails;
import com.rits.pcucompleteservice.model.MessageModel;
import com.rits.pcucompleteservice.model.PcuComplete;
import com.rits.pcucompleteservice.repository.PcuCompleteRepository;
import com.rits.pcuheaderservice.dto.PcuHeaderRequest;
import com.rits.pcuheaderservice.model.PcuHeader;
import com.rits.pcuinqueueservice.exception.PcuInQueueException;
import com.rits.productionlogservice.dto.ActualCycleSum;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import com.rits.productionlogservice.model.ProductionLog;
import com.rits.productionlogservice.model.ProductionLogMongo;
import com.rits.productionlogservice.producer.ProductionLogProducer;
import com.rits.startservice.dto.Operation;
import com.rits.startservice.dto.StartRequestDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class PcuCompleteServiceImpl implements PcuCompleteService {
    private final PcuCompleteRepository pcuCompleteRepository;
    private final WebClient.Builder webClientBuilder;
    private final ParentRoute parentroute;
    private final NonParentRoute nonParentRoute;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${pcurouterheader-service.url}/getStepDetails")
    private String getStepDetailsUrl;
    @Value("${pcurouterheader-service.url}/isParentRoute")
    private String isParentRouteUrl;
    @Value("${pcuheader-service.url}/readPcu")
    private String readPcuUrl;

    @Value("${pcurouterheader-service.url}/isLastReportingStep")
    private String isLastReportingStepUrl;
    @Value("${routing-service.url}/findNextStepIDDetails")
    private String findNextStepIDDetailsUrl;
    @Value("${routing-service.url}/findNextStepIDDetailsOfParentStepId")
    private String findNextStepIDDetailsOfParentStepIdUrl;

    @Value("${routing-service.url}/findStepDetailsByNextStepId")
    private String findStepDetailsByNextStepIdUrl;

    @Value("${routing-service.url}/getStepDetails")
    private String getStepDetailsUsingStepIdUrl;
    @Value("${pcurouterheader-service.url}/getOperationNextStepID")
    private String getOperationNextStepIdUrl;
    @Value("${pcuinqueue-service.url}/create")
    private String insertInInQueueUrl;
    @Value("${start-service.url}/update")
    private String updateInInWorkUrl;
    @Value("${start-service.url}/delete")
    private String deleteInInWorkUrl;
    @Value("${pcudone-service.url}/insert")
    private String insertInPcuDoneUrl;
    @Value("${routing-service.url}/retrieve")
    private String routingRetrieveUrl;
    @Value("${pcurouterheader-service.url}/getRoutingSubType")
    private String getRoutingSubTypeUrl;
    @Value("${start-service.url}/retrieve")
    private String retrieveInInWorkUrl;
    @Value("${pcurouterheader-service.url}/getEntryStep")
    private String getEntryStepUrl;
    @Value("${pcurouterheader-service.url}/getAllEntryStep")
    private String getAllEntryStepUrl;

    @Value("${productionlog-service.url}/producer")
    private String productionLogUrl;

    @Value("${dispositionlog-service.url}/findActiveRec")
    private String findDispotionLogUrl;
    @Value("${nonconformance-service.url}/Done")
    private String dispositionUrl;

//    @Value("${shift-service.url}/getBreakHours")
//    private String getShiftBreakHoursUrl;

    @Value("${productionlog-service.url}/retrieveByPcuOperationShopOrderAndEventType")
    private String retrieveProductionLogUrl;

    @Value("${productionlog-service.url}/retrieveFirstPcuRecord")
    private String retrieveFirstStartLogUrl;

    @Value("${productionlog-service.url}/retrieveAllByEventTypeShopOrderOperationItem")
    private String retrieveAllSignOffLogUrl;

    @Value("${operation-service.url}/retrieveOperationByCurrentVersion")
    private String retrieveOperationByCurrentVersionUrl;

    public PcuCompleteRequestInfo boBuilder(PcuCompleteReq pcuCompleteReq){

        PcuCompleteRequestInfo completeReqWithBO = new PcuCompleteRequestInfo();
        if(pcuCompleteReq.getPcu()!=null && !pcuCompleteReq.getPcu().isEmpty())
            completeReqWithBO.setPcuBO(BOConverter.retrievePcuBO(pcuCompleteReq.getSite(), pcuCompleteReq.getPcu()));

        if(pcuCompleteReq.getItem()!=null && !pcuCompleteReq.getItem().isEmpty()) {
            completeReqWithBO.setItemBO(BOConverter.retrieveItemBO(pcuCompleteReq.getSite(), pcuCompleteReq.getItem(), pcuCompleteReq.getItemVersion()));
        }

        if(pcuCompleteReq.getRouter()!=null && !pcuCompleteReq.getRouter().isEmpty()) {
            completeReqWithBO.setRouterBO(BOConverter.retrieveRouterBO(pcuCompleteReq.getSite(), pcuCompleteReq.getRouter(), pcuCompleteReq.getRouterVersion()));
        }

        if(pcuCompleteReq.getOperation()!=null && !pcuCompleteReq.getOperation().isEmpty()) {
            completeReqWithBO.setOperationBO(BOConverter.retrieveOperationBO(pcuCompleteReq.getSite(), pcuCompleteReq.getOperation(), pcuCompleteReq.getOperationVersion()));
        }

        if(pcuCompleteReq.getResource()!=null && !pcuCompleteReq.getResource().isEmpty())
            completeReqWithBO.setResourceBO(BOConverter.retriveResourceBO(pcuCompleteReq.getSite(), pcuCompleteReq.getResource()));

        if(pcuCompleteReq.getUser()!=null && !pcuCompleteReq.getUser().isEmpty())
            completeReqWithBO.setUserBO(BOConverter.retrieveUserBO(pcuCompleteReq.getSite(), pcuCompleteReq.getUser()));

        if(pcuCompleteReq.getShopOrder()!=null && !pcuCompleteReq.getShopOrder().isEmpty())
            completeReqWithBO.setShopOrderBO(BOConverter.retrieveShopOrderBO(pcuCompleteReq.getSite(), pcuCompleteReq.getShopOrder()));

        if(pcuCompleteReq.getChildRouter()!=null && !pcuCompleteReq.getChildRouter().isEmpty()) {
            completeReqWithBO.setChildRouterBO(BOConverter.retrieveChildRouterBO(pcuCompleteReq.getSite(), pcuCompleteReq.getChildRouter(), pcuCompleteReq.getChildRouterVersion()));
        }
        return completeReqWithBO;
    }

    public PcuCompleteReq boRemover(PcuComplete pcuCompleteReqWithBO){

        PcuCompleteReq completeReq = new PcuCompleteReq();

        if(pcuCompleteReqWithBO.getPcuBO()!=null && !pcuCompleteReqWithBO.getPcuBO().isEmpty()) {
            completeReq.setPcu(BOConverter.getPcu(pcuCompleteReqWithBO.getPcuBO()));
        }

        if(pcuCompleteReqWithBO.getItemBO()!=null && !pcuCompleteReqWithBO.getItemBO().isEmpty()) {
            completeReq.setItem(BOConverter.getItem(pcuCompleteReqWithBO.getItemBO()));
            completeReq.setItemVersion(BOConverter.getItemVersion(pcuCompleteReqWithBO.getItemBO()));
        }

        if(pcuCompleteReqWithBO.getRouterBO()!=null && !pcuCompleteReqWithBO.getRouterBO().isEmpty()) {
            completeReq.setRouter(BOConverter.getRouter(pcuCompleteReqWithBO.getRouterBO()));
            completeReq.setRouterVersion(BOConverter.getRouterVersion(pcuCompleteReqWithBO.getRouterBO()));
        }

        if(pcuCompleteReqWithBO.getOperationBO()!=null && !pcuCompleteReqWithBO.getOperationBO().isEmpty()) {
            completeReq.setOperation(BOConverter.getOperation(pcuCompleteReqWithBO.getOperationBO()));
            completeReq.setOperationVersion(BOConverter.getOperationVersion(pcuCompleteReqWithBO.getOperationBO()));
        }

        if(pcuCompleteReqWithBO.getResourceBO()!=null && !pcuCompleteReqWithBO.getResourceBO().isEmpty()) {
            completeReq.setResource(BOConverter.getResource(pcuCompleteReqWithBO.getResourceBO()));
        }

        if(pcuCompleteReqWithBO.getUserBO()!=null && !pcuCompleteReqWithBO.getUserBO().isEmpty()) {
            completeReq.setUser(BOConverter.getUser(pcuCompleteReqWithBO.getUserBO()));
        }

        if(pcuCompleteReqWithBO.getShopOrderBO()!=null && !pcuCompleteReqWithBO.getShopOrderBO().isEmpty()) {
            completeReq.setShopOrder(BOConverter.getShopOrder(pcuCompleteReqWithBO.getShopOrderBO()));
        }

        if(pcuCompleteReqWithBO.getChildRouterBO()!=null && !pcuCompleteReqWithBO.getChildRouterBO().isEmpty()) {
            completeReq.setChildRouter(BOConverter.getChildRouter(pcuCompleteReqWithBO.getChildRouterBO()));
            completeReq.setChildRouterVersion(BOConverter.getChildRouter(pcuCompleteReqWithBO.getChildRouterBO()));
        }
        return completeReq;
    }

    @Override
    public MessageModel insert(PcuCompleteReq pcuCompleteReq) throws Exception {
        PcuCompleteRequestInfo responseBOs = boBuilder(pcuCompleteReq);
        if (pcuCompleteRepository.existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(1, pcuCompleteReq.getSite(), responseBOs.getPcuBO(), responseBOs.getOperationBO(), responseBOs.getShopOrderBO(), pcuCompleteReq.getResource())) {
            return update(pcuCompleteReq);
        } else {
            if (pcuCompleteReq.getQtyToComplete().equals(pcuCompleteReq.getQtyCompleted())) {
                pcuCompleteReq.setQtyToComplete(String.valueOf(0));
            } else {
                pcuCompleteReq.setQtyToComplete(String.valueOf(Integer.parseInt(pcuCompleteReq.getQtyToComplete()) - Integer.parseInt((pcuCompleteReq.getQtyCompleted()))));

            }
            PcuComplete pcuComplete = PcuComplete.builder()
                    .site(pcuCompleteReq.getSite())
                    .handle("PCUCompleteBo:" + pcuCompleteReq.getSite() + "," + responseBOs.getPcuBO() + "," + pcuCompleteReq.getOperation() + "," + responseBOs.getShopOrderBO() + "," + pcuCompleteReq.getResource())
                    .pcuBO(responseBOs.getPcuBO())
                    .dateTime(LocalDateTime.now())
                    .itemBO(responseBOs.getItemBO())
                    .routerBO(responseBOs.getRouterBO())
                    .operationBO(pcuCompleteReq.getOperation())
                    .resourceBO(pcuCompleteReq.getResource())
                    .stepID(pcuCompleteReq.getStepID())
                    .userBO(responseBOs.getUserBO())
                    .qtyCompleted(pcuCompleteReq.getQtyCompleted())
                    .qtyToComplete(pcuCompleteReq.getQtyToComplete())
                    .shopOrderBO(responseBOs.getShopOrderBO())
                    .childRouterBO(responseBOs.getChildRouterBO())
                    .parentStepID(pcuCompleteReq.getParentStepID())
                    .active(1)
                    .build();
            pcuCompleteRepository.save(pcuComplete);

            pcuCompleteReq.setDateTime(pcuComplete.getDateTime());
            pcuCompleteReq.setHandle(pcuComplete.getHandle());
            pcuCompleteReq.setActive(pcuComplete.getActive());

            return MessageModel.builder().response(pcuCompleteReq).message_details(new MessageDetails(pcuComplete.getHandle() + " created SuccessFully", "S")).build();
        }
    }

    @Override
    public MessageModel update(PcuCompleteReq pcuCompleteReq) throws Exception {
        PcuCompleteRequestInfo responseBOs = boBuilder(pcuCompleteReq);
        if (!pcuCompleteRepository.existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(1, pcuCompleteReq.getSite(), responseBOs.getPcuBO(), pcuCompleteReq.getOperation(), responseBOs.getShopOrderBO(), pcuCompleteReq.getResource())) {
            return insert(pcuCompleteReq);
        }
        PcuComplete existingPcuComplete = pcuCompleteRepository.findByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(1, pcuCompleteReq.getSite(), responseBOs.getPcuBO(), pcuCompleteReq.getOperation(), responseBOs.getShopOrderBO(), pcuCompleteReq.getResource());
        PcuComplete pcuComplete = PcuComplete.builder()
                .site(existingPcuComplete.getSite())
                .handle(existingPcuComplete.getHandle())
                .pcuBO(existingPcuComplete.getPcuBO())
                .operationBO(existingPcuComplete.getOperationBO())
                .shopOrderBO(existingPcuComplete.getShopOrderBO())
                .resourceBO(existingPcuComplete.getResourceBO())
                .dateTime(LocalDateTime.now())
                .itemBO(responseBOs.getItemBO())
                .routerBO(responseBOs.getRouterBO())
                .stepID(pcuCompleteReq.getStepID())
                .userBO(responseBOs.getUserBO())
                .qtyToComplete(pcuCompleteReq.getQtyToComplete())
                .qtyCompleted(pcuCompleteReq.getQtyCompleted())
                .childRouterBO(responseBOs.getChildRouterBO())
                .parentStepID(pcuCompleteReq.getParentStepID())
                .active(1)
                .build();
        pcuCompleteRepository.save(pcuComplete);

        PcuCompleteReq completeReq = boRemover(pcuComplete);
        completeReq.setSite(pcuComplete.getSite());
        completeReq.setHandle(pcuComplete.getHandle());
        completeReq.setDateTime(pcuComplete.getDateTime());
        completeReq.setStepID(pcuComplete.getStepID());
        completeReq.setQtyToComplete(pcuComplete.getQtyToComplete());
        completeReq.setQtyCompleted(pcuComplete.getQtyCompleted());
        completeReq.setParentStepID(pcuComplete.getParentStepID());
        completeReq.setActive(pcuComplete.getActive());

        return MessageModel.builder().response(completeReq).message_details(new MessageDetails(pcuComplete.getHandle() + " updated SuccessFully", "S")).build();
    }

    @Override
    public boolean delete(PcuCompleteReq pcuCompleteReq) throws Exception {
        PcuCompleteRequestInfo responseBOs = boBuilder(pcuCompleteReq);

        if (pcuCompleteRepository.existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(1, pcuCompleteReq.getSite(), responseBOs.getPcuBO(), pcuCompleteReq.getOperation(), responseBOs.getShopOrderBO(), pcuCompleteReq.getResource())) {

            PcuComplete pcuComplete = pcuCompleteRepository.findByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(1, pcuCompleteReq.getSite(), responseBOs.getPcuBO(), pcuCompleteReq.getOperation(), responseBOs.getShopOrderBO(), pcuCompleteReq.getResource());
            pcuCompleteRepository.delete(pcuComplete);
            return true;
        }
        return false;
    }

    @Override
    public Boolean deleteByPcu(PcuCompleteReq pcuCompleteReq)
    {
        Boolean isDeleted = false;

        List<PcuComplete> pcuCompletes = pcuCompleteRepository.findByActiveAndSiteAndPcuBO(1, pcuCompleteReq.getSite(), BOConverter.retrievePcuBO(pcuCompleteReq.getSite(), pcuCompleteReq.getPcu()));
        if(pcuCompletes!=null && !pcuCompletes.isEmpty())
        {
            for(PcuComplete pcuComplete : pcuCompletes)
            {
                pcuComplete.setActive(0);
                pcuCompleteRepository.save(pcuComplete);
                isDeleted = true;
            }
        }
        return isDeleted;
    }
    @Override
    public Boolean unDeleteByPcu(PcuCompleteReq pcuCompleteReq)
    {
        Boolean isDeleted = false;
        List<PcuComplete> pcuCompletes = pcuCompleteRepository.findByActiveAndSiteAndPcuBO(0, pcuCompleteReq.getSite(), BOConverter.retrievePcuBO(pcuCompleteReq.getSite(), pcuCompleteReq.getPcu()));
        if(pcuCompletes!=null && !pcuCompletes.isEmpty())
        {
            for(PcuComplete pcuComplete : pcuCompletes)
            {
                pcuComplete.setActive(1);
                pcuCompleteRepository.save(pcuComplete);
                isDeleted = true;
            }
        }
        return isDeleted;
    }


    private RoutingStep getStepDetails(PcuRequest pcuRequest){
      RoutingStep getStepDetailsResponse = webClientBuilder.build()
                .post()
                .uri(getStepDetailsUrl)
                .bodyValue(pcuRequest)
                .retrieve()
                .bodyToMono(RoutingStep.class)
                .block();
        if (getStepDetailsResponse == null || getStepDetailsResponse.getOperation() == null) {
            getStepDetailsResponse=null;
        }
            return getStepDetailsResponse;
    }

    @Override
    public MessageModel complete(RequestList pcuCompleteRequests) throws Exception {
        Boolean isParentRouteResponse = false;
        StringBuilder concatenatedMessage = new StringBuilder();
        MessageDetails nextStepIdMessageDetails = null;

        String opVersion = "";
        if (pcuCompleteRequests.getRequestList() != null && !pcuCompleteRequests.getRequestList().isEmpty()) {
            var firstRequest = pcuCompleteRequests.getRequestList().get(0);
            if (firstRequest != null) {
                opVersion = getOperationCurrentVer(firstRequest.getOperation(), firstRequest.getSite());
            }
        }

        if(pcuCompleteRequests != null && pcuCompleteRequests.getRequestList()!= null){
            for (PcuCompleteReq pcuCompleteReq : pcuCompleteRequests.getRequestList()) {

                pcuCompleteReq.setOperationVersion(opVersion);

                PcuCompleteRequestInfo completeReqWithBOs = boBuilder(pcuCompleteReq);

                PcuInWorkRequestDetails pcuInWork = getPcuInWork(pcuCompleteReq);
                if (pcuCompleteReq.getQtyToComplete() == null || pcuCompleteReq.getQtyToComplete().isEmpty()) {

                    PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                            .site(pcuCompleteReq.getSite())
                            .pcuBO(completeReqWithBOs.getPcuBO())
                            .build();

                    PcuHeader pcuHeaderResponse = makeWebClientCall(readPcuUrl, pcuHeaderRequest, PcuHeader.class);
                    if (pcuHeaderResponse == null || pcuHeaderResponse.getPcuBO() == null) {
                        throw new PcuCompleteException(2701, pcuCompleteReq.getPcu());
                    }
                    pcuCompleteReq.setQtyToComplete(String.valueOf(pcuHeaderRequest.getQtyInQueue()));// ?

                    if(pcuInWork != null)
                        pcuCompleteReq.setQtyCompleted(pcuInWork.getQtyInWork());

                    PcuComplete pcuComplete = pcuCompleteRepository.findByActiveAndSiteAndPcuBOAndOperationBO(1, pcuCompleteReq.getSite(), completeReqWithBOs.getPcuBO(), pcuCompleteReq.getOperation());

                    if (pcuComplete != null && pcuComplete.getQtyCompleted() != null && !pcuComplete.getQtyCompleted().isEmpty()) {
                        pcuCompleteReq.setQtyToComplete(pcuComplete.getQtyToComplete());
                        pcuCompleteReq.setQtyCompleted(String.valueOf((Double.parseDouble(pcuCompleteReq.getQtyCompleted())) + (Double.parseDouble(pcuComplete.getQtyCompleted()))));
                    }

                }
    //            PcuCompleteReq temporaryRequest = new PcuCompleteReq(pcuCompleteReq);
                String qtyCompleted = pcuCompleteReq.getQtyCompleted();

    //            String[] pcu= pcuCompleteReqWithBO.getPcuBO().split(",");
    //            PcuInWorkRequestWithoutBO pcuInWork1 = getPcuInWork(pcuCompleteReq);
                if (pcuInWork == null || pcuInWork.getPcu() == null) {
                    throw new PcuCompleteException(4000, pcuCompleteReq.getPcu());
                }

                if (pcuCompleteReq.getQtyCompleted().equals("0")) {
                    pcuCompleteReq.setQtyCompleted(pcuInWork.getQtyInWork());
                }
                if (Double.parseDouble(pcuInWork.getQtyInWork()) >= Double.parseDouble(pcuCompleteReq.getQtyCompleted())) {

    //                String[] parentRoutingBo = pcuCompleteReq.getRouter().split("/");
                    String parentRouting = pcuCompleteReq.getRouter();
                    String parentVersion = pcuCompleteReq.getRouterVersion();

                    String[] routingBo;
                    String routing;
                    String version;
                    if (pcuCompleteReq.getChildRouter() != null && !pcuCompleteReq.getChildRouter().isEmpty()) {
    //                    routingBo = pcuCompleteReq.getChildRouter().split("/");
                        routing = pcuCompleteReq.getChildRouter();
                        version = pcuCompleteReq.getChildRouterVersion();

                    } else {
                        routing = parentRouting;
                        version = parentVersion;
                        isParentRouteResponse = true;

                    }
    //                String[] operationBO= pcuCompleteReq.getOperationBO().split(",");
    //                String operation=operationBO[1];
    //                String operationVersion=operationBO[2];
                    RoutingRequest routingRequest = RoutingRequest.builder().site(pcuCompleteReq.getSite()).pcuBo(completeReqWithBOs.getPcuBO()).routing(routing).version(version).operation(pcuCompleteReq.getOperation()).operationVersion(pcuCompleteReq.getOperationVersion()).stepId(pcuCompleteReq.getStepID()).build();

                    PcuRequest pcuRequest = PcuRequest.builder().site(pcuCompleteReq.getSite()).pcuBo(completeReqWithBOs.getPcuBO()).router(routing).version(version).operation(pcuCompleteReq.getOperation()).operationVersion(pcuCompleteReq.getOperationVersion()).stepId(pcuCompleteReq.getStepID()).build();
                    String subType = findSubType(pcuRequest);
                    MessageModel messageModel = null;
    //                String operation = "OperationBO:" + pcuCompleteReq.getSite() + "," + opArray[0] + "," + opArray[1];
    //                String resource = "ResourceBO:"+pcuCompleteReq.getSite()+","+pcuCompleteReq.getResource();

    //                PcuCompleteReqWithBO completeReqWithBO = PcuCompleteReqWithBO.builder()
    //                        .site(pcuCompleteReq.getSite())
    //                        .pcuBO(completeReqWithBOs.getPcuBO())
    //                        .operationBO(completeReqWithBOs.getOperationBO())
    //                        .qtyCompleted(pcuCompleteReq.getQtyCompleted())
    //                        .qtyToComplete(pcuCompleteReq.getQtyToComplete())
    //                        .routerBO(completeReqWithBOs.getRouterBO())
    //                        .userBO(completeReqWithBOs.getUserBO())
    //                        .itemBO(completeReqWithBOs.getItemBO())
    //                        .workCenter(pcuCompleteReq.getWorkCenter())
    //                        .shopOrderBO(completeReqWithBOs.getShopOrderBO())
    //                        .resourceBO(completeReqWithBOs.getResourceBO())
    //                        .stepID(pcuCompleteReq.getStepID())
    //                        .childRouterBO(completeReqWithBOs.getChildRouterBO())
    //                        .parentStepID(pcuCompleteReq.getParentStepID())
    //                        .nextStepId(pcuCompleteReq.getNextStepId())
    //                        .parallel(pcuCompleteReq.getParallel())
    //                        .build();

                    completeReqWithBOs.setSite(pcuCompleteReq.getSite());
                    completeReqWithBOs.setQtyCompleted(pcuCompleteReq.getQtyCompleted());
                    completeReqWithBOs.setQtyToComplete(pcuCompleteReq.getQtyToComplete());
                    completeReqWithBOs.setWorkCenter(pcuCompleteReq.getWorkCenter());
                    completeReqWithBOs.setStepID(pcuCompleteReq.getStepID());
                    completeReqWithBOs.setParentStepID(pcuCompleteReq.getParentStepID());
                    completeReqWithBOs.setNextStepId(pcuCompleteReq.getNextStepId());
                    completeReqWithBOs.setParallel(pcuCompleteReq.getParallel());

                    if(subType != null && !subType.isEmpty()) {
                        if (isParentRouteResponse) {
                            messageModel = parentroute.excuteParentLogic(subType, pcuRequest, completeReqWithBOs, routingRequest);
                        } else {
                            messageModel = nonParentRoute.executeNonParentLogic(subType, pcuRequest, completeReqWithBOs, routingRequest);
                        }
                    } else {
                        throw new PcuCompleteException(4002);
                    }

                    if (messageModel.getMessage_details().getMsg_type().equals("S")) {
    //                    if(temporaryRequest.getQtyCompleted().equals("0")){
                        if (qtyCompleted.equals("0")) {
                            concatenatedMessage.append("All of " + pcuCompleteReq.getPcu() + " PCU's " + messageModel.getMessage_details().getMsg()).append(",");
                        } else {
    //                        concatenatedMessage.append(temporaryRequest.getQtyCompleted()+" pieces of "+pcuCompleteReq.getPcu()+" PCU's " +messageModel.getMessage_details().getMsg()).append(",");
                            concatenatedMessage.append(qtyCompleted + " pieces of " + pcuCompleteReq.getPcu() + " PCU's " + messageModel.getMessage_details().getMsg()).append(",");

                        }
                    } else if (messageModel.getMessage_details().getMsg_type().equals("NextStepId")) {
                        nextStepIdMessageDetails = messageModel.getMessage_details();
                    }
                    if (messageModel.getMessage_details().getMsg_type().equalsIgnoreCase("S")) {
                        Boolean productionLogged = productionLog(pcuCompleteReq, messageModel.getMessage_details().getMsg());// why site in empty
                    }
                } else {
                    throw new PcuCompleteException(4001);
                }
            }
        } else
            throw new PcuCompleteException(2919);

        if (concatenatedMessage.length() > 0) {
            return MessageModel.builder().message_details(new MessageDetails(concatenatedMessage.toString().trim(), "S")).build();
        } else
        {
            return MessageModel.builder().message_details(nextStepIdMessageDetails).build();
        }

    }
    public <T, R> R makeWebClientCall(String url, T requestBody, Class<R> responseType) {
        return webClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }
    public String findSubType(PcuRequest pcuRequest) throws Exception {
        String getSubTypeResponse = webClientBuilder.build()
                .post()
                .uri(getRoutingSubTypeUrl)
                .bodyValue(pcuRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (getSubTypeResponse == null || getSubTypeResponse.isEmpty()) {
            throw new PcuCompleteException(3803, pcuRequest.getRouter(), pcuRequest.getVersion());
        }
        return getSubTypeResponse;
    }

    @Override
    public MessageModel insertOrUpdateInPcuInQueue(PcuCompleteReq pcuCompleteReq) throws Exception {
        pcuCompleteReq.setQtyInQueue(pcuCompleteReq.getQtyInWork());// what to pass inwork or inqueue

        MessageModel pcuInQueueResponse = webClientBuilder.build()
                .post()
                .uri(insertInInQueueUrl)
                .bodyValue(pcuCompleteReq)
                .retrieve()
                .bodyToMono(MessageModel.class)
                .block();
        if (pcuInQueueResponse == null || pcuInQueueResponse.getMessage_details() == null) {
            throw new PcuCompleteException(3600, pcuCompleteReq.getPcu());
        }
        return pcuInQueueResponse;
    }

    private PcuInWorkRequestDetails getPcuInWork(PcuCompleteReq pcuCompleteReq){

        PcuCompleteReq inWorkRequest = PcuCompleteReq.builder()
                .site(pcuCompleteReq.getSite())
                .pcu(pcuCompleteReq.getPcu())
                .operation(pcuCompleteReq.getOperation()).operationVersion(pcuCompleteReq.getOperationVersion())
                .build();

        PcuInWorkRequestDetails retrieveInWork = webClientBuilder.build()
                .post()
                .uri(retrieveInInWorkUrl)
                .bodyValue(inWorkRequest)
                .retrieve()
                .bodyToMono(PcuInWorkRequestDetails.class)
                .block();
            return retrieveInWork;
    }

    private MessageModel deletePcuInWork(PcuCompleteReq pcuInWorkRequest){
        MessageModel pcuInWorkResponse = webClientBuilder.build()
                .post()
                .uri(deleteInInWorkUrl)
                .bodyValue(pcuInWorkRequest)
                .retrieve()
                .bodyToMono(MessageModel.class)
                .block();
        if (pcuInWorkResponse == null || pcuInWorkResponse.getMessage_details() == null) {
            throw new PcuCompleteException(4000, pcuInWorkRequest.getPcu());
        }
        return pcuInWorkResponse;
    }

    private MessageModel updatePcuInWork(PcuCompleteReq pcuInWorkRequest){
        MessageModel pcuInWorkResponse = webClientBuilder.build()
                .post()
                .uri(updateInInWorkUrl)
                .bodyValue(pcuInWorkRequest)
                .retrieve()
                .bodyToMono(MessageModel.class)
                .block();
        if (pcuInWorkResponse == null || pcuInWorkResponse.getMessage_details() == null) {
            throw new PcuCompleteException(4000, pcuInWorkRequest.getPcu());
        }
        return pcuInWorkResponse;
    }

    @Override
    public MessageModel updateOrDeleteInPcuInWork(PcuCompleteReq pcuCompleteReq) throws Exception {
        MessageModel pcuInworkUpdated=null;

        PcuInWorkRequestDetails pcuInWOrkDetail = getPcuInWork(pcuCompleteReq);
        if (pcuInWOrkDetail == null || pcuInWOrkDetail.getPcu() == null) {
            throw new PcuCompleteException(4000, pcuCompleteReq.getPcu());
        }
        pcuCompleteReq.setQtyToComplete(String.valueOf(Integer.parseInt(pcuCompleteReq.getQtyToComplete()) - Integer.parseInt((pcuCompleteReq.getQtyCompleted()))));
//        PcuInWorkRequestWithoutBO pcuInWorkRequest = PcuInWorkRequestWithoutBO.builder()
//                .site(pcuCompleteReqWithBO.getSite())
//                .pcuBO(pcuCompleteReqWithBO.getPcuBO())
//                .itemBO(pcuCompleteReqWithBO.getItemBO())
//                .routerBO(pcuCompleteReqWithBO.getRouterBO())
//                .resourceBO(pcuCompleteReqWithBO.getResourceBO())
//                .operationBO(pcuCompleteReqWithBO.getOperationBO())
//                .stepID(pcuCompleteReqWithBO.getStepID())
//                .userBO(pcuCompleteReqWithBO.getUserBO())
//                .qtyToComplete(pcuCompleteReqWithBO.getQtyToComplete())
//                .qtyInWork(String.valueOf(Integer.parseInt(pcuInWOrkDetail.getQtyInWork()) - Integer.parseInt(pcuCompleteReqWithBO.getQtyCompleted())))//ask shanmathi(its using nextId's qty.for current how to find the qtyInWOrk
//                .shopOrderBO(pcuCompleteReqWithBO.getShopOrderBO())
//                .childRouterBO(pcuCompleteReqWithBO.getChildRouterBO())
//                .parentStepID(pcuCompleteReqWithBO.getParentStepID())
//                .build();
        pcuCompleteReq.setQtyInWork(String.valueOf(Integer.parseInt(pcuInWOrkDetail.getQtyInWork()) - Integer.parseInt(pcuCompleteReq.getQtyCompleted())));
        if (Integer.parseInt(pcuInWOrkDetail.getQtyToComplete()) == 0) {
            pcuInworkUpdated = deletePcuInWork(pcuCompleteReq);
        }
        else{
            pcuInworkUpdated = updatePcuInWork(pcuCompleteReq);
        }
            return pcuInworkUpdated;
    }

    @Override
    public Boolean insertInPcuDone(PcuCompleteRequestInfo pcuCompleteReqWithBO) throws Exception {
       Boolean DoneSuccess=false;
        PcuDone pcuDone = PcuDone.builder()
                .site(pcuCompleteReqWithBO.getSite())
                .pcuBO(pcuCompleteReqWithBO.getPcuBO())
                .qtyDone(pcuCompleteReqWithBO.getQtyCompleted())
                .routerBO(pcuCompleteReqWithBO.getRouterBO())
                .userBO(pcuCompleteReqWithBO.getUserBO())
                .itemBO(pcuCompleteReqWithBO.getItemBO())
                .shopOrderBO(pcuCompleteReqWithBO.getShopOrderBO())
                .build();
        PcuDoneMessageModel pcuDoneResponse = webClientBuilder.build()
                .post()
                .uri(insertInPcuDoneUrl)//change the URL
                .bodyValue(pcuDone)
                .retrieve()
                .bodyToMono(PcuDoneMessageModel.class)
                .block();
        if (pcuDoneResponse == null || pcuDoneResponse.getMessage_details() == null) {
            throw new PcuCompleteException(3900);
        }
        else{
            DoneSuccess=true;
            DispositionLogRequest dispositionLogRequest=new DispositionLogRequest();
            dispositionLogRequest.setPcuBO(pcuCompleteReqWithBO.getPcuBO());
            dispositionLogRequest.setToRoutingBo(pcuCompleteReqWithBO.getRouterBO());
            DispositionLog dispositionLog = webClientBuilder.build()
                    .post()
                    .uri(findDispotionLogUrl)//change the URL
                    .bodyValue(dispositionLogRequest)
                    .retrieve()
                    .bodyToMono(DispositionLog.class)
                    .block();
            if(dispositionLog!=null){
                DispositionRequest dispositionRequest=new DispositionRequest();
                dispositionRequest.setPcuBO(pcuCompleteReqWithBO.getPcuBO());
                dispositionRequest.setDispositionRoutingBo(dispositionLog.getFromRoutingBo());
                dispositionRequest.setQty(pcuCompleteReqWithBO.getQtyCompleted());
                dispositionRequest.setResourceBo(dispositionLog.getResourceBo());
                dispositionRequest.setItemBo(dispositionLog.getItemBo());
                dispositionRequest.setSite(pcuCompleteReqWithBO.getSite());
                dispositionRequest.setWorkCenterBo(dispositionLog.getWorkCenterBo());
                dispositionRequest.setRouterBo(dispositionLog.getFromRoutingBo());
                dispositionRequest.setStepID(pcuCompleteReqWithBO.getStepID());
                dispositionRequest.setUserBo(pcuCompleteReqWithBO.getUserBO());
                dispositionRequest.setShoporderBO(dispositionLog.getShopOrderBo());
                dispositionRequest.setOperationBO(pcuCompleteReqWithBO.getOperationBO());
                dispositionRequest.setToOperationBo(dispositionLog.getFromoperationBO());
                dispositionRequest.setActive("0");
                com.rits.nonconformanceservice.model.MessageModel messageModel = webClientBuilder.build()
                        .post()
                        .uri(dispositionUrl)//change the URL
                        .bodyValue(dispositionLogRequest)
                        .retrieve()
                        .bodyToMono(com.rits.nonconformanceservice.model.MessageModel.class)
                        .block();
            }
        }
        return DoneSuccess;
    }

    @Override
    public RoutingStep findStepDetailsByNextStepId(RoutingRequest routingRequest) throws Exception {
        RoutingMessageModel step = webClientBuilder.build()
                .post()
                .uri(findStepDetailsByNextStepIdUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(RoutingMessageModel.class)
                .block();
        if (step == null || !step.getMessage_details().getMsg_type().equalsIgnoreCase("S")) {
            throw new PcuCompleteException(3807);
        }
        return step.getRoutingStep();
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
            throw new PcuCompleteException(800);
        }
        return extensionResponse;
    }

    @Override
    public PcuCompleteReq retrieve(PcuCompleteReq pcuCompleteReq) throws Exception {
        String pcuBO = "PcuBO:"+pcuCompleteReq.getSite()+","+pcuCompleteReq.getPcu();

        PcuComplete pcuComplete=pcuCompleteRepository.findByActiveAndSiteAndPcuBOAndOperationBO(1,pcuCompleteReq.getSite(),pcuBO,pcuCompleteReq.getOperation());

        if(pcuComplete!=null&& pcuComplete.getPcuBO()!=null&& !pcuComplete.getPcuBO().isEmpty()){
            PcuCompleteReq completeReq = boRemover(pcuComplete);

            completeReq.setSite(pcuComplete.getSite());
            completeReq.setHandle(pcuComplete.getHandle());
            completeReq.setDateTime(pcuComplete.getDateTime());
            completeReq.setQtyCompleted(pcuComplete.getQtyCompleted());
            completeReq.setStepID(pcuComplete.getStepID());
            completeReq.setQtyToComplete(pcuComplete.getQtyToComplete());
            completeReq.setQtyCompleted(pcuComplete.getQtyCompleted());
            completeReq.setParentStepID(pcuComplete.getParentStepID());
            completeReq.setWorkCenter(pcuComplete.getWorkCenter());
            completeReq.setActive(pcuComplete.getActive());

            return completeReq;
        }
        return null;
    }
    @Override
    public List<PcuCompleteReq> retrieveByOperation(PcuCompleteReq pcuCompleteReq) throws Exception {
         List<PcuComplete> pcuCompleteList = pcuCompleteRepository.findByActiveAndSiteAndOperationBO(1,pcuCompleteReq.getSite(),pcuCompleteReq.getOperation());

         List<PcuCompleteReq> pcuCompleteReqList = null;
         for(PcuComplete pcuComplete : pcuCompleteList) {
             pcuCompleteReqList = new ArrayList<>();
             PcuCompleteReq completeReq = boRemover(pcuComplete);

             completeReq.setSite(pcuComplete.getSite());
             completeReq.setHandle(pcuComplete.getHandle());
             completeReq.setDateTime(pcuComplete.getDateTime());
             completeReq.setQtyCompleted(pcuComplete.getQtyCompleted());
             completeReq.setStepID(pcuComplete.getStepID());
             completeReq.setQtyToComplete(pcuComplete.getQtyToComplete());
             completeReq.setQtyCompleted(pcuComplete.getQtyCompleted());
             completeReq.setParentStepID(pcuComplete.getParentStepID());
             completeReq.setWorkCenter(pcuComplete.getWorkCenter());
             completeReq.setActive(pcuComplete.getActive());
             pcuCompleteReqList.add(completeReq);
         }

         return pcuCompleteReqList;
     }

    @Override
    public List<PcuCompleteReq> retrieveByOperationAndShopOrder(PcuCompleteReq pcuCompleteReq) throws Exception {

        String shopOrderBO = BOConverter.retrieveShopOrderBO(pcuCompleteReq.getSite(), pcuCompleteReq.getShopOrder());

        List<PcuComplete> pcuCompleteList = pcuCompleteRepository.findByActiveAndSiteAndOperationBOAndShopOrderBO(1,pcuCompleteReq.getSite(),pcuCompleteReq.getOperation(),shopOrderBO);

        List<PcuCompleteReq> pcuCompleteReqList = null;
        for(PcuComplete pcuComplete : pcuCompleteList) {
            pcuCompleteReqList = new ArrayList<>();
            PcuCompleteReq completeReq = boRemover(pcuComplete);

            completeReq.setSite(pcuComplete.getSite());
            completeReq.setHandle(pcuComplete.getHandle());
            completeReq.setDateTime(pcuComplete.getDateTime());
            completeReq.setQtyCompleted(pcuComplete.getQtyCompleted());
            completeReq.setStepID(pcuComplete.getStepID());
            completeReq.setQtyToComplete(pcuComplete.getQtyToComplete());
            completeReq.setQtyCompleted(pcuComplete.getQtyCompleted());
            completeReq.setParentStepID(pcuComplete.getParentStepID());
            completeReq.setWorkCenter(pcuComplete.getWorkCenter());
            completeReq.setActive(pcuComplete.getActive());
            pcuCompleteReqList.add(completeReq);
        }

        return pcuCompleteReqList;
    }


    public String nextSTepId(RoutingRequest routingRequest, String nextStepId) {
        Routing routing = webClientBuilder.build()
                .post()
                .uri(routingRetrieveUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(Routing.class)
                .block();

        if (routing == null || routing.getRoutingStepList().isEmpty()) {
            throw new PcuCompleteException(500, routingRequest.getRouting(), routingRequest.getVersion());
        }

        String nextStepIds[] = nextStepId.split(",");
        StringBuilder matchedStepIds = new StringBuilder();

        for (RoutingStep routingStep : routing.getRoutingStepList()) {
            for (String stepId : nextStepIds) {
                if (stepId.equals(routingStep.getStepId())) {
                    if (matchedStepIds.length() > 0) {
                        matchedStepIds.append(",");
                    }
                    matchedStepIds.append(stepId).append("-").append(routingStep.getOperation());
                }
            }
        }

        return matchedStepIds.toString();
    }

    public Boolean productionLog(PcuCompleteReq pcuCompleteReq, String message){

        PcuCompleteRequestInfo completeReqWithBO = boBuilder(pcuCompleteReq);
        ProductionLog productionLogRecord = retrieveProductionLog(completeReqWithBO.getPcuBO(), completeReqWithBO.getShopOrderBO(), completeReqWithBO.getOperationBO(), pcuCompleteReq.getSite());
        long minutesDifference = 0;
        ActualCycleSum retrieveAllSignOffRecord = retrieveAllSignOffLog(completeReqWithBO.getPcuBO(), completeReqWithBO.getShopOrderBO(), completeReqWithBO.getOperationBO());

       if(retrieveAllSignOffRecord !=null)
       {
//           for(ProductionLogMongo productionLog : retrieveAllSignOffRecord)
//           {
//               minutesDifference = minutesDifference + Long.parseLong(productionLog.getActualCycleTime().replace("s",""));
//           }
           minutesDifference = retrieveAllSignOffRecord.getTotalActualCycleTime() != null ? retrieveAllSignOffRecord.getTotalActualCycleTime() : 0;
       }
        if(productionLogRecord != null)
        {
            minutesDifference =minutesDifference + Duration.between(productionLogRecord.getCreated_datetime(),LocalDateTime.now()).toSeconds();
        }

        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("completeSfcBatch")
                .userId(completeReqWithBO.getUserBO())
                .pcu(completeReqWithBO.getPcuBO())
                .batchNo((completeReqWithBO.getPcuBO() != null) ? BOConverter.getPcu(completeReqWithBO.getPcuBO()) :"")
                .orderNumber((completeReqWithBO.getShopOrderBO() != null) ? BOConverter.getShopOrder(completeReqWithBO.getShopOrderBO()):"")
                .shopOrderBO(completeReqWithBO.getShopOrderBO())
                .operation_bo(completeReqWithBO.getOperationBO())
                .routerBO(completeReqWithBO.getRouterBO())
                .itemBO(completeReqWithBO.getItemBO())
                .resourceId(completeReqWithBO.getResourceBO())
//                .shiftName(minutesRecord.getShiftName())
//                .shiftStartTime(minutesRecord.getStartTime().toString())
//                .shiftEndTime(minutesRecord.getEndTime().toString())
                .site(pcuCompleteReq.getSite())
//                .totalBreakHours(String.valueOf(minutesRecord.getMinutes()))
                .qty(Integer.parseInt(pcuCompleteReq.getQtyCompleted()))
                .actualCycleTime(Double.valueOf(minutesDifference))
                .topic("production-log")
                .status("Active")
                .eventData(message)
                .build();
        eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));
        return true;
    }
    public ProductionLog retrieveProductionLog(String pcuBO, String shopOrderBO, String operationBO, String site)//1 call
    {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("BATCH_START")
                .site(site)
                .pcu(BOConverter.getPcu(pcuBO))
                .shopOrderBO(BOConverter.getShopOrder(shopOrderBO))
                .operation(BOConverter.getOperation(operationBO))
                .operationVersion(BOConverter.getOperationVersion(operationBO))//
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

    public ProductionLogMongo retrieveFirstStartLog(String pcuBO,String shopOrderBO)
    {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("BATCH_START")
                .pcu(pcuBO)
                .shopOrderBO(shopOrderBO)
                .build();
        ProductionLogMongo retrievedRecord = webClientBuilder.build()
                .post()
                .uri(retrieveFirstStartLogUrl)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(ProductionLogMongo.class)
                .block();
        return retrievedRecord;
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

    public String getOperationCurrentVer(String operation, String site) throws Exception{
        Operation oper = Operation.builder().site(site).operation(operation).build();

        try {
            Operation operVersion = webClientBuilder.build()
                    .post()
                    .uri(retrieveOperationByCurrentVersionUrl)
                    .bodyValue(oper)
                    .retrieve()
                    .bodyToMono(Operation.class)
                    .block();

            if(operVersion == null)
                throw new PcuInQueueException(1710);

            return operVersion.getRevision();

        } catch (Exception e){
            throw e;
        }
    }


//    public MinutesList getShiftBreakHours(String site)
//    {
//        ShiftRequest retrieveShiftRequest = ShiftRequest.builder().site(site).build();
//        MinutesList retrievedRecord = webClientBuilder.build()
//                .post()
//                .uri(getShiftBreakHoursUrl)
//                .bodyValue(retrieveShiftRequest)
//                .retrieve()
//                .bodyToMono(MinutesList.class)
//                .block();
//        return retrievedRecord;
//    }


}