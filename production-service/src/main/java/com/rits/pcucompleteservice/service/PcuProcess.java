package com.rits.pcucompleteservice.service;

import com.rits.Utility.BOConverter;
import com.rits.pcucompleteservice.dto.*;
import com.rits.pcucompleteservice.exception.PcuCompleteException;
import com.rits.pcucompleteservice.model.MessageModel;
import com.rits.pcucompleteservice.model.PcuComplete;
import com.rits.pcucompleteservice.repository.PcuCompleteRepository;
import com.rits.pcuheaderservice.dto.PcuHeaderRequest;
import com.rits.pcuheaderservice.model.PcuHeader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;


@RequiredArgsConstructor
@Service
@Lazy
public class PcuProcess {

    private final WebClient.Builder webClientBuilder;
    @Value("${pcuinqueue-service.url}/create")
    private String insertInInQueueUrl;

    @Value("${pcuinqueue-service.url}/retrieve")
    private String retrieveInQueueUrl;
    @Value("${pcurouterheader-service.url}/getRoutingSubType")
    private String getRoutingSubTypeUrl;
    @Value("${start-service.url}/update")
    private String updateInInWorkUrl;
    @Value("${start-service.url}/delete")
    private String deleteInInWorkUrl;
    @Value("${pcudone-service.url}/insert")
    private String insertInPcuDoneUrl;
    @Value("${routing-service.url}/retrieve")
    private String routingRetrieveUrl;
    @Value("${start-service.url}/retrieve")
    private String retrieveInInWorkUrl;
    @Value("${pcurouterheader-service.url}/getOperationQueueList")
    private String getOperationQueueListUrl;
    @Value("${pcurouterheader-service.url}/getAllEntryStep")
    private String getAllEntryStepUrl;
    @Value("${pcurouterheader-service.url}/updateNeedsToBeCompleted")
    private String updateNeedsToBeCompletedUrl;

    @Value("${pcuheader-service.url}/readPcu")
    private String readPcuUrl;


    private final PcuCompleteRepository pcuCompleteRepository;

    public PcuCompleteReq boRemover(PcuCompleteRequestInfo pcuCompleteReqWithBO){

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
    public Boolean insertOrUpdateInPcuInQueue(PcuCompleteRequestInfo pcuCompleteReqWithBO) throws Exception {
        Boolean queue=false;
        PcuCompleteReq completeReq = boRemover(pcuCompleteReqWithBO);

        PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder()
                .site(pcuCompleteReqWithBO.getSite())
                .pcu(completeReq.getPcu())
                .router(completeReq.getRouter())
                .routerVersion(completeReq.getRouterVersion())
                .operation(completeReq.getOperation())
                .operationVersion(completeReq.getOperationVersion())
                .stepID(pcuCompleteReqWithBO.getStepID())
                .user(completeReq.getUser())
                .qtyToComplete(pcuCompleteReqWithBO.getQtyCompleted())
                .qtyInQueue(pcuCompleteReqWithBO.getQtyCompleted())
                .shopOrder(completeReq.getShopOrder())
                .workCenter(pcuCompleteReqWithBO.getWorkCenter())
                .childRouter(completeReq.getChildRouter())
                .childRouterVersion(completeReq.getChildRouterVersion())
                .parentStepID(pcuCompleteReqWithBO.getParentStepID())
                .build();

        PcuInQueueRequest retrievePcuInQueueResponse = webClientBuilder.build()
                .post()
                .uri(retrieveInQueueUrl)
                .bodyValue(pcuInQueueRequest)
                .retrieve()
                .bodyToMono(PcuInQueueRequest.class)
                .block();
        if(retrievePcuInQueueResponse != null && retrievePcuInQueueResponse.getPcu() != null && retrievePcuInQueueResponse.getQtyInQueue() != null)
        {
            pcuInQueueRequest.setQtyInQueue(String.valueOf(Double.parseDouble(retrievePcuInQueueResponse.getQtyInQueue()) +Double.parseDouble(pcuCompleteReqWithBO.getQtyCompleted())));
        }
        pcuInQueueRequest.setItem(completeReq.getItem());
        pcuInQueueRequest.setItemVersion(completeReq.getItemVersion());
        pcuInQueueRequest.setResource(completeReq.getResource());

        MessageModel pcuInQueueResponse = webClientBuilder.build()
                .post()
                .uri(insertInInQueueUrl)
                .bodyValue(pcuInQueueRequest)
                .retrieve()
                .bodyToMono(MessageModel.class)
                .block();
        if (pcuInQueueResponse == null || pcuInQueueResponse.getMessage_details() == null) {
            throw new PcuCompleteException(3600, pcuCompleteReqWithBO.getPcuBO());
        }else{
            queue=true;
        }
        return queue;
    }

    private Boolean deletePcuInWork(PcuCompleteReq pcuInWorkRequest){
        Boolean sucess=false;
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
        else{
            sucess=true;
        }
        return sucess;
    }

    private Boolean updatePcuInWork(PcuCompleteReq pcuInWorkRequest){
        Boolean sucess=false;
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
        else{
            sucess=true;
        }
        return sucess;
    }

    public Boolean insertInPcuDone(PcuCompleteRequestInfo pcuCompleteReqWithBO) throws Exception {
        List<PcuComplete> listofPCUsCompleted = pcuCompleteRepository.findByActiveAndSiteAndPcuBO(1, pcuCompleteReqWithBO.getSite(), pcuCompleteReqWithBO.getPcuBO());

        for (PcuComplete pcusObj : listofPCUsCompleted) {
            if(pcusObj.getQtyToComplete().equals("0")||(pcusObj.getPcuBO().equals(pcuCompleteReqWithBO.getPcuBO())&&pcusObj.getQtyToComplete().equals(pcuCompleteReqWithBO.getQtyCompleted()))){
                pcuCompleteRepository.delete(pcusObj);
            }else{
                throw new PcuCompleteException(3809);
            }
        }
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                .site(pcuCompleteReqWithBO.getSite())
                .pcuBO(pcuCompleteReqWithBO.getPcuBO())
                .build();
        PcuHeader pcuHeaderResponse= makeWebClientCall(readPcuUrl, pcuHeaderRequest, PcuHeader.class);
        if (pcuHeaderResponse == null || pcuHeaderResponse.getPcuBO()==null) {
            throw new PcuCompleteException(2701, pcuCompleteReqWithBO.getPcuBO());
        }

        Boolean DoneSuccess=false;
        PcuCompleteReq pcuCompleteReq = boRemover(pcuCompleteReqWithBO);

        PcuDoneDetails pcuDone = PcuDoneDetails.builder()
                .site(pcuCompleteReqWithBO.getSite())
                .pcu(pcuCompleteReq.getPcu())
                .qtyDone(String.valueOf(pcuHeaderResponse.getQtyInQueue()))
                .router(pcuCompleteReq.getRouter())
                .routerVersion(pcuCompleteReq.getRouterVersion())
                .user(pcuCompleteReq.getUser())
                .item(pcuCompleteReq.getItem())
                .itemVersion(pcuCompleteReq.getItemVersion())
                .workCenter(pcuCompleteReqWithBO.getWorkCenter())
                .shopOrder(pcuCompleteReq.getShopOrder())
                .operation(pcuCompleteReq.getOperation())
                .operationVersion(pcuCompleteReq.getOperationVersion())
                .resource(pcuCompleteReq.getResource())
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
            DoneSuccess =true;
        }

        return DoneSuccess;
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
    public Boolean updateOrDeleteInPcuInWork(PcuCompleteRequestInfo completeRequest) throws Exception {
        Boolean success=false;

        PcuCompleteRequestInfo completeRequestInfo = new PcuCompleteRequestInfo(completeRequest);
        PcuCompleteReq completeReq = boRemover(completeRequestInfo);
        completeReq.setSite(completeRequestInfo.getSite());

        PcuCompleteReq pcuInWOrkDetail = getPcuInWork(completeReq);

        if (pcuInWOrkDetail == null || pcuInWOrkDetail.getPcu() == null) {
            throw new PcuCompleteException(4000, completeRequestInfo.getPcuBO());
        }
        if(Double.parseDouble(pcuInWOrkDetail.getQtyInWork()) < Double.parseDouble(completeRequestInfo.getQtyCompleted()))
        {
            throw new PcuCompleteException(4001);
        }
//        String[] opArray = completeRequest.getOperationBO().split(",");
//        String operation = opArray[1] + "/" + opArray[2];
//
//        String[] resourceArray = completeRequest.getResourceBO().split(",");
//        String resource = resourceArray[1];

        completeRequestInfo.setQtyToComplete(String.valueOf(Double.parseDouble(completeRequestInfo.getQtyToComplete()) - Double.parseDouble((completeRequestInfo.getQtyCompleted()))));
        completeReq.setWorkCenter(completeRequestInfo.getWorkCenter());
        completeReq.setStepID(completeRequestInfo.getStepID());
        completeReq.setSite(completeRequestInfo.getSite());
        completeReq.setQtyToComplete(completeRequestInfo.getQtyToComplete());
        completeReq.setQtyInWork(String.valueOf(Double.parseDouble(pcuInWOrkDetail.getQtyInWork()) - Double.parseDouble(completeRequestInfo.getQtyCompleted())));
        completeReq.setParentStepID(completeRequestInfo.getParentStepID());

//        PcuInWorkRequestWithoutBO pcuInWorkRequest = PcuInWorkRequestWithoutBO.builder()
//                .site(completeRequest.getSite())
//                .pcu(pcuCompleteReqWithBO.getPcuBO())
//                .item(pcuCompleteReqWithBO.getItemBO())
//                .router(pcuCompleteReqWithBO.getRouterBO())
//                .resource(pcuCompleteReqWithBO.getResourceBO())
//                .operation(pcuCompleteReqWithBO.getOperationBO())
//                .workCenter(completeRequest.getWorkCenter())
//                .stepID(completeRequest.getStepID())
//                .user(pcuCompleteReqWithBO.getUserBO())
//                .qtyToComplete(completeRequest.getQtyToComplete())
//                .qtyInWork(String.valueOf(Double.parseDouble(pcuInWOrkDetail.getQtyInWork()) - Double.parseDouble(completeRequest.getQtyCompleted())))//ask shanmathi(its using nextId's qty.for current how to find the qtyInWOrk
//                .shopOrder(pcuCompleteReqWithBO.getShopOrderBO())
//                .childRouter(pcuCompleteReqWithBO.getChildRouterBO())
//                .parentStepID(completeRequest.getParentStepID())
//                .build();
//
////        if (Integer.parseInt(pcuInWorkRequest.getQtyToComplete()) == 0) {
////            success=deletePcuInWork(pcuInWorkRequest);
////        }
        if (Double.parseDouble(completeReq.getQtyInWork()) == 0) {
            success = deletePcuInWork(completeReq);
        }
        else{
            success = updatePcuInWork(completeReq);
        }
        return success;
    }
    private PcuCompleteReq getPcuInWork(PcuCompleteReq pcuCompleteReq){
        PcuCompleteReq completeReq = PcuCompleteReq.builder()
                .site(pcuCompleteReq.getSite())
                .pcu(pcuCompleteReq.getPcu())
                .operation(pcuCompleteReq.getOperation())
                .operationVersion(pcuCompleteReq.getOperationVersion())
                .build();

        PcuCompleteReq retrieveInWork = webClientBuilder.build()
                .post()
                .uri(retrieveInInWorkUrl)
                .bodyValue(completeReq)
                .retrieve()
                .bodyToMono(PcuCompleteReq.class)
                .block();

        return retrieveInWork;
    }

    public Boolean complete(PcuCompleteRequestInfo pcuCompleteReqWithBO) throws Exception {
        Boolean success=false;
//        String[] opArray = pcuCompleteReqWithBO.getOperationBO().split(",");
//        String[] resourceArray = pcuCompleteReqWithBO.getResourceBO().split(",");
//
        String operation = BOConverter.getOperation(pcuCompleteReqWithBO.getOperationBO());
        String resource = BOConverter.getResource(pcuCompleteReqWithBO.getResourceBO());

        if (!pcuCompleteRepository.existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(1, pcuCompleteReqWithBO.getSite(), pcuCompleteReqWithBO.getPcuBO(), operation, pcuCompleteReqWithBO.getShopOrderBO(), resource)) {
            return insert(pcuCompleteReqWithBO);
        }else {
            if (pcuCompleteReqWithBO.getQtyToComplete().equals(pcuCompleteReqWithBO.getQtyCompleted())) {

                PcuComplete existingPcuComplete = pcuCompleteRepository.findByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(1, pcuCompleteReqWithBO.getSite(), pcuCompleteReqWithBO.getPcuBO(), operation, pcuCompleteReqWithBO.getShopOrderBO(), resource);
                PcuComplete pcuComplete = PcuComplete.builder()
                        .site(existingPcuComplete.getSite())
                        .handle(existingPcuComplete.getHandle())
                        .pcuBO(existingPcuComplete.getPcuBO())
                        .operationBO(existingPcuComplete.getOperationBO())
                        .shopOrderBO(existingPcuComplete.getShopOrderBO())
                        .resourceBO(existingPcuComplete.getResourceBO())
                        .dateTime(LocalDateTime.now())
                        .itemBO(pcuCompleteReqWithBO.getItemBO())
                        .routerBO(pcuCompleteReqWithBO.getRouterBO())
                        .stepID(pcuCompleteReqWithBO.getStepID())
                        .userBO(pcuCompleteReqWithBO.getUserBO())
                        .workCenter(pcuCompleteReqWithBO.getWorkCenter())
                        .qtyToComplete("0")
                        .qtyCompleted(pcuCompleteReqWithBO.getQtyCompleted())
                        .childRouterBO(pcuCompleteReqWithBO.getChildRouterBO())
                        .parentStepID(pcuCompleteReqWithBO.getParentStepID())
                        .active(1)
                        .build();
                pcuCompleteRepository.save(pcuComplete);
                success = true;
            } else {
                pcuCompleteReqWithBO.setQtyToComplete(String.valueOf(Double.parseDouble(pcuCompleteReqWithBO.getQtyToComplete()) - Double.parseDouble((pcuCompleteReqWithBO.getQtyCompleted()))));
                success = true;

                PcuComplete existingPcuComplete = pcuCompleteRepository.findByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(1, pcuCompleteReqWithBO.getSite(), pcuCompleteReqWithBO.getPcuBO(), operation, pcuCompleteReqWithBO.getShopOrderBO(), resource);
                PcuComplete pcuComplete = PcuComplete.builder()
                        .site(existingPcuComplete.getSite())
                        .handle(existingPcuComplete.getHandle())
                        .pcuBO(existingPcuComplete.getPcuBO())
                        .operationBO(existingPcuComplete.getOperationBO())
                        .shopOrderBO(existingPcuComplete.getShopOrderBO())
                        .resourceBO(existingPcuComplete.getResourceBO())
                        .dateTime(LocalDateTime.now())
                        .itemBO(pcuCompleteReqWithBO.getItemBO())
                        .routerBO(pcuCompleteReqWithBO.getRouterBO())
                        .stepID(pcuCompleteReqWithBO.getStepID())
                        .userBO(pcuCompleteReqWithBO.getUserBO())
                        .workCenter(pcuCompleteReqWithBO.getWorkCenter())
                        .qtyToComplete(pcuCompleteReqWithBO.getQtyToComplete())
                        .qtyCompleted(pcuCompleteReqWithBO.getQtyCompleted())
                        .childRouterBO(pcuCompleteReqWithBO.getChildRouterBO())
                        .parentStepID(pcuCompleteReqWithBO.getParentStepID())
                        .active(1)
                        .build();
                pcuCompleteRepository.save(pcuComplete);
            }
        }
        return success;
    }

    public Boolean insert(PcuCompleteRequestInfo pcuCompleteReqWithBO) throws Exception {
        Boolean success=false;
        if (pcuCompleteRepository.existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(1, pcuCompleteReqWithBO.getSite(), pcuCompleteReqWithBO.getPcuBO(), pcuCompleteReqWithBO.getOperationBO(), pcuCompleteReqWithBO.getShopOrderBO(), pcuCompleteReqWithBO.getResourceBO())) {
            return complete(pcuCompleteReqWithBO);
        } else {
            if (pcuCompleteReqWithBO.getQtyToComplete().equals(pcuCompleteReqWithBO.getQtyCompleted())) {
                PcuComplete pcuComplete = PcuComplete.builder()
                        .site(pcuCompleteReqWithBO.getSite())
                        .handle("PCUCompleteBo:" + pcuCompleteReqWithBO.getSite() + "," + pcuCompleteReqWithBO.getPcuBO() + "," + pcuCompleteReqWithBO.getOperationBO() + "," + pcuCompleteReqWithBO.getShopOrderBO() + "," + pcuCompleteReqWithBO.getResourceBO())
                        .pcuBO(pcuCompleteReqWithBO.getPcuBO())
                        .dateTime(LocalDateTime.now())
                        .itemBO(pcuCompleteReqWithBO.getItemBO())
                        .routerBO(pcuCompleteReqWithBO.getRouterBO())
                        .operationBO(pcuCompleteReqWithBO.getOperationBO())
                        .resourceBO(pcuCompleteReqWithBO.getResourceBO())
                        .stepID(pcuCompleteReqWithBO.getStepID())
                        .userBO(pcuCompleteReqWithBO.getUserBO())
                        .workCenter(pcuCompleteReqWithBO.getWorkCenter())
                        .qtyCompleted(pcuCompleteReqWithBO.getQtyCompleted())
                        .qtyToComplete("0")
                        .shopOrderBO(pcuCompleteReqWithBO.getShopOrderBO())
                        .childRouterBO(pcuCompleteReqWithBO.getChildRouterBO())
                        .parentStepID(pcuCompleteReqWithBO.getParentStepID())
                        .active(1)
                        .build();
                pcuCompleteRepository.save(pcuComplete);
                success=true;
            } else {
                pcuCompleteReqWithBO.setQtyToComplete(String.valueOf(Double.parseDouble(pcuCompleteReqWithBO.getQtyToComplete()) - Double.parseDouble((pcuCompleteReqWithBO.getQtyCompleted()))));
                success = true;


                PcuComplete pcuComplete = PcuComplete.builder()
                        .site(pcuCompleteReqWithBO.getSite())
                        .handle("PCUCompleteBo:" + pcuCompleteReqWithBO.getSite() + "," + pcuCompleteReqWithBO.getPcuBO() + "," + pcuCompleteReqWithBO.getOperationBO() + "," + pcuCompleteReqWithBO.getShopOrderBO() + "," + pcuCompleteReqWithBO.getResourceBO())
                        .pcuBO(pcuCompleteReqWithBO.getPcuBO())
                        .dateTime(LocalDateTime.now())
                        .itemBO(pcuCompleteReqWithBO.getItemBO())
                        .routerBO(pcuCompleteReqWithBO.getRouterBO())
                        .operationBO(pcuCompleteReqWithBO.getOperationBO())
                        .resourceBO(pcuCompleteReqWithBO.getResourceBO())
                        .stepID(pcuCompleteReqWithBO.getStepID())
                        .workCenter(pcuCompleteReqWithBO.getWorkCenter())
                        .userBO(pcuCompleteReqWithBO.getUserBO())
                        .qtyCompleted(pcuCompleteReqWithBO.getQtyCompleted())
                        .qtyToComplete(pcuCompleteReqWithBO.getQtyToComplete())
                        .shopOrderBO(pcuCompleteReqWithBO.getShopOrderBO())
                        .childRouterBO(pcuCompleteReqWithBO.getChildRouterBO())
                        .parentStepID(pcuCompleteReqWithBO.getParentStepID())
                        .active(1)
                        .build();
                pcuCompleteRepository.save(pcuComplete);
            }
            return success;
        }
    }
    public boolean deleteComplete(PcuCompleteRequestInfo pcuCompleteReqWithBO) throws Exception {

        if (pcuCompleteRepository.existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(1, pcuCompleteReqWithBO.getSite(), pcuCompleteReqWithBO.getPcuBO(), pcuCompleteReqWithBO.getOperationBO(), pcuCompleteReqWithBO.getShopOrderBO(), pcuCompleteReqWithBO.getResourceBO())) {

            PcuComplete pcuComplete = pcuCompleteRepository.findByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(1, pcuCompleteReqWithBO.getSite(), pcuCompleteReqWithBO.getPcuBO(), pcuCompleteReqWithBO.getOperationBO(), pcuCompleteReqWithBO.getShopOrderBO(), pcuCompleteReqWithBO.getResourceBO());
            pcuCompleteRepository.delete(pcuComplete);
            return true;

        }
        return false;
    }
    public List<RoutingStep> getOperationQueueList(PcuRequest pcuRequest) throws Exception {
        PcuRouterHeaderMessageModel messageModel = webClientBuilder.build()
                .post()
                .uri(getOperationQueueListUrl)
                .bodyValue(pcuRequest)
                .retrieve()
                .bodyToMono(PcuRouterHeaderMessageModel.class)
                .block();
        if (messageModel == null || !messageModel.getMessagedetails().getMsg_type().equalsIgnoreCase("S")) {
            throw new PcuCompleteException(3807);
        }
        return messageModel.getRoutingStep();
    }
    public void updateNeedsToBeCompleted(PcuRequest routingRequest) throws Exception {
        PcuRouterHeaderMessageModel messageModel = webClientBuilder.build()
                .post()
                .uri(updateNeedsToBeCompletedUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(PcuRouterHeaderMessageModel.class)
                .block();
        if (messageModel == null || !messageModel.getMessagedetails().getMsg_type().equalsIgnoreCase("S")) {
            throw new PcuCompleteException(3809);
        }
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
    public EntryStep getAllEntryStep(PcuRequest pcuRequest) throws Exception {
        EntryStep getEntryStep = webClientBuilder.build()
                .post()
                .uri(getAllEntryStepUrl)
                .bodyValue(pcuRequest)
                .retrieve()
                .bodyToMono(EntryStep.class)
                .block();
        if (getEntryStep == null || getEntryStep.getRoutingStepList().isEmpty()) {
            throw new PcuCompleteException(3804, pcuRequest.getPcuBo());
        }
        return getEntryStep;
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
}
