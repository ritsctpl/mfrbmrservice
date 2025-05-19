package com.rits.pcucompleteservice.service;

import com.rits.Utility.BOConverter;
import com.rits.pcucompleteservice.dto.*;
import com.rits.pcucompleteservice.exception.PcuCompleteException;
import com.rits.pcucompleteservice.model.MessageDetails;
import com.rits.pcucompleteservice.model.MessageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@RequiredArgsConstructor
@Service
@Lazy
public class SequentialRouting {

    private final WebClient.Builder webClientBuilder;
    @Value("${pcurouterheader-service.url}/getOperationNextStepID")
    private String getOperationNextStepIdUrl;
    @Value("${routing-service.url}/findStepDetailsByNextStepId")
    private String findStepDetailsByNextStepIdUrl;
    private final PcuProcess pcuProcess;
    public MessageModel executeSequentialLogic(PcuRequest pcuRequest, PcuCompleteRequestInfo pcuCompleteReqWithBO, RoutingRequest routingRequest ) throws Exception {
        MessageModel messageModel = null;
        String operation= BOConverter.getOperation(pcuCompleteReqWithBO.getOperationBO());
        PcuCompleteRequestInfo temporaryRequest = new PcuCompleteRequestInfo(pcuCompleteReqWithBO);

        String nextStepID=getNextStepId(pcuRequest);
        String[] stepIds = nextStepID.split(",");

        if (stepIds.length > 1){
            if (pcuCompleteReqWithBO.getNextStepId() == null) {
                String nextStep = pcuProcess.nextSTepId(routingRequest, nextStepID);
                return MessageModel.builder().message_details(new MessageDetails(nextStep, "NextStepId")).build();
            }else{
                routingRequest.setStepId(pcuCompleteReqWithBO.getNextStepId());
                RoutingStep routingResponse = findStepDetailsByNextStepId(routingRequest);
                if(routingResponse.getStepType().equalsIgnoreCase("Operation")){
                    if(excuteSeqOprLogic(routingResponse, pcuCompleteReqWithBO)){//1
                          messageModel=MessageModel.builder().message_details(new MessageDetails("Operation "+operation+" Completed Successfully and moved to operation "+routingResponse.getOperation(), "S")).build();
                    }
                    else{
                        messageModel=MessageModel.builder().message_details(new MessageDetails("Exception occured in Operation Complete", "E")).build();
                    }
                }else{
                    messageModel=executeROutingLogicForSeq(routingResponse, pcuCompleteReqWithBO,temporaryRequest,pcuRequest);//2
                }
            }
        }
        else{
            //No parallel
            routingRequest.setStepId(nextStepID);
            RoutingStep routingResponse = findStepDetailsByNextStepId(routingRequest);
            if(routingResponse.getStepType().equalsIgnoreCase("Operation")){
                if(excuteSeqOprLogic(routingResponse, pcuCompleteReqWithBO)){
                    messageModel=MessageModel.builder().message_details(new MessageDetails("Operation "+operation+" Completed Successfully and moved to operation "+routingResponse.getOperation(), "S")).build();
                }
                else{
                    messageModel= MessageModel.builder().message_details(new MessageDetails("Exception occured in Operation Complete", "E")).build();
                }
            }
            else {
                messageModel=executeROutingLogicForSeq(routingResponse, pcuCompleteReqWithBO,temporaryRequest,pcuRequest);
            }
        }
            return messageModel;
    }

    public Boolean excuteSeqOprLogic(RoutingStep routingResponse, PcuCompleteRequestInfo pcuCompleteReqWithBO) throws Exception {
        Boolean success=false;

        PcuCompleteRequestInfo temporaryRequest = new PcuCompleteRequestInfo(pcuCompleteReqWithBO);
        PcuCompleteRequestInfo pcuInQueueRequest = temporaryRequest;


//        PcuCompleteRequestInfo pcuInQueueRequest = pcuCompleteReqWithBO;
//        PcuCompleteReqWithBO pcuInQueueRequest = temporaryRequest;
        String operationBo="OperationBO:"+pcuCompleteReqWithBO.getSite()+","+routingResponse.getOperation()+","+routingResponse.getOperationVersion();
        pcuInQueueRequest.setOperationBO(operationBo);
        pcuInQueueRequest.setQtyCompleted(pcuCompleteReqWithBO.getQtyCompleted());
        pcuInQueueRequest.setStepID(routingResponse.getStepId());

        if(pcuProcess.insertOrUpdateInPcuInQueue(pcuInQueueRequest)){
            if(pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO)){
                if(pcuProcess.complete(pcuCompleteReqWithBO)){
                    success=true;
                }

            }
        }

        return success;
    }

    public String getNextStepId(PcuRequest pcuRequest) throws Exception {
        PcuRouterHeaderMessageModel messageModel = webClientBuilder.build()
                .post()
                .uri(getOperationNextStepIdUrl)
                .bodyValue(pcuRequest)
                .retrieve()
                .bodyToMono(PcuRouterHeaderMessageModel.class)
                .block();
        if (messageModel == null || !messageModel.getMessagedetails().getMsg_type().equalsIgnoreCase("S")) {
            throw new PcuCompleteException(3808);
        }
        return messageModel.getNextStepId();
    }

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
    public MessageModel executeROutingLogic(RoutingStep routingResponse, PcuCompleteRequestInfo pcuCompleteReqWithBO, PcuCompleteRequestInfo temporaryRequest, PcuRequest pcuRequest) throws Exception {
        MessageModel messageModel = null;
        StringBuilder nextStepOperation = new StringBuilder();
//        String[] operationBO= pcuCompleteReqWithBO.getOperationBO().split(",");
        String operation=BOConverter.getOperation(pcuCompleteReqWithBO.getOperationBO());
//        String[] innerRoutingBo = routingResponse.getRoutingBO().split(",");

        String innerRouting = BOConverter.getRouting(routingResponse.getRoutingBO());
        String innerVersion = BOConverter.getRouterVersion(routingResponse.getRoutingBO());

        PcuRequest childRouterReq = PcuRequest.builder().site(pcuCompleteReqWithBO.getSite()).pcuBo(pcuCompleteReqWithBO.getPcuBO()).router(innerRouting).version(innerVersion).build();
        String childSubType = pcuProcess.findSubType(childRouterReq);

        if (childSubType.equalsIgnoreCase("sequential")) {
            EntryStep entryStepList = pcuProcess.getAllEntryStep(childRouterReq);
            for (RoutingStep routingStep : entryStepList.getRoutingStepList()) {
                PcuCompleteRequestInfo pcuInQueueRequest = new PcuCompleteRequestInfo(temporaryRequest);
                String operationBo="OperationBO:"+pcuInQueueRequest.getSite()+","+routingStep.getOperation()+","+routingStep.getOperationVersion();
                pcuInQueueRequest.setOperationBO(operationBo);
                pcuInQueueRequest.setQtyCompleted(pcuCompleteReqWithBO.getQtyCompleted());
                pcuInQueueRequest.setStepID(routingStep.getStepId());
                pcuInQueueRequest.setParentStepID(routingResponse.getStepId());
                pcuInQueueRequest.setChildRouterBO(routingResponse.getRoutingBO());
                pcuProcess.insertOrUpdateInPcuInQueue(pcuInQueueRequest);

                nextStepOperation.append(routingStep.getOperation()).append(", ");
            }

            if (pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO)) {
                if (pcuProcess.complete(pcuCompleteReqWithBO)) {
                    messageModel = MessageModel.builder()
                            .message_details(new MessageDetails("Operation " + operation + " Completed Successfully and moved to operations: " + nextStepOperation.toString().trim(), "S"))
                            .build();
                }
            }
        } else if (childSubType.equalsIgnoreCase("simultaneous") || childSubType.equalsIgnoreCase("anyOrder")) {
            EntryStep getAllEntryStep = pcuProcess.getAllEntryStep(childRouterReq);
            for (RoutingStep routingStep : getAllEntryStep.getRoutingStepList()) {
                PcuCompleteRequestInfo pcuInQueueRequest = new PcuCompleteRequestInfo(temporaryRequest);
                String operationBo="OperationBO:"+pcuInQueueRequest.getSite()+","+routingStep.getOperation()+","+routingStep.getOperationVersion();
                pcuInQueueRequest.setOperationBO(operationBo);
                pcuInQueueRequest.setQtyCompleted(pcuCompleteReqWithBO.getQtyCompleted());
                pcuInQueueRequest.setStepID(routingStep.getStepId());
                pcuInQueueRequest.setParentStepID(routingResponse.getStepId());
                pcuInQueueRequest.setChildRouterBO(routingResponse.getRoutingBO());
                pcuProcess.insertOrUpdateInPcuInQueue(pcuInQueueRequest);

                // Append the operation to the nextStepOperation StringBuilder
                nextStepOperation.append(routingStep.getOperation()).append(", ");
            }
            if (pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO)) {
                if (pcuProcess.complete(pcuCompleteReqWithBO)) {
                    pcuProcess.updateNeedsToBeCompleted(pcuRequest);
                    // Include nextStepOperation in the messageDetails
                    messageModel = MessageModel.builder()
                            .message_details(new MessageDetails("Operation " + operation + " Completed Successfully and moved to operations: " + nextStepOperation.toString().trim(), "S"))
                            .build();
                }
            }
        }

        return messageModel;
    }
    public MessageModel executeROutingLogicForSeq(RoutingStep routingResponse, PcuCompleteRequestInfo pcuCompleteReqWithBO, PcuCompleteRequestInfo temporaryRequest, PcuRequest pcuRequest) throws Exception {
        MessageModel messageModel = null;
        StringBuilder nextStepOperation = new StringBuilder();
        String[] operationBO= pcuCompleteReqWithBO.getOperationBO().split(",");
        String operation=operationBO[1];
        String[] innerRoutingBo = routingResponse.getRoutingBO().split(",");
        String innerRouting = innerRoutingBo[1];
        String innerVersion = innerRoutingBo[2];
        PcuRequest childRouterReq = PcuRequest.builder().site(pcuCompleteReqWithBO.getSite()).pcuBo(pcuCompleteReqWithBO.getPcuBO()).router(innerRouting).version(innerVersion).build();
        String childSubType = pcuProcess.findSubType(childRouterReq);

        if (childSubType.equalsIgnoreCase("sequential")) {
            EntryStep entryStepList = pcuProcess.getAllEntryStep(childRouterReq);
            for (RoutingStep routingStep : entryStepList.getRoutingStepList()) {
                PcuCompleteRequestInfo pcuInQueueRequest = new PcuCompleteRequestInfo(temporaryRequest);
                String operationBo="OperationBO:"+pcuInQueueRequest.getSite()+","+routingStep.getOperation()+","+routingStep.getOperationVersion();
                pcuInQueueRequest.setOperationBO(operationBo);
                pcuInQueueRequest.setQtyCompleted(pcuCompleteReqWithBO.getQtyCompleted());
                pcuInQueueRequest.setStepID(routingStep.getStepId());
                pcuInQueueRequest.setParentStepID(routingResponse.getStepId());
                pcuInQueueRequest.setChildRouterBO(routingResponse.getRoutingBO());
                pcuProcess.insertOrUpdateInPcuInQueue(pcuInQueueRequest);

                nextStepOperation.append(routingStep.getOperation()).append(", ");
            }
            if (pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO)) {
                if (pcuProcess.complete(pcuCompleteReqWithBO)) {
                    messageModel = MessageModel.builder()
                            .message_details(new MessageDetails("Operation " + operation + " Completed Successfully and moved to operations: " + nextStepOperation.toString().trim(), "S"))
                            .build();
                }
            }
        } else if (childSubType.equalsIgnoreCase("simultaneous") || childSubType.equalsIgnoreCase("anyOrder")) {
            EntryStep getAllEntryStep = pcuProcess.getAllEntryStep(childRouterReq);
            for (RoutingStep routingStep : getAllEntryStep.getRoutingStepList()) {
                PcuCompleteRequestInfo pcuInQueueRequest = new PcuCompleteRequestInfo(temporaryRequest);
                String operationBo="OperationBO:"+pcuInQueueRequest.getSite()+","+routingStep.getOperation()+","+routingStep.getOperationVersion();
                pcuInQueueRequest.setOperationBO(operationBo);
                pcuInQueueRequest.setQtyCompleted(pcuCompleteReqWithBO.getQtyCompleted());
                pcuInQueueRequest.setStepID(routingStep.getStepId());
                pcuInQueueRequest.setParentStepID(routingResponse.getStepId());
                pcuInQueueRequest.setChildRouterBO(routingResponse.getRoutingBO());
                pcuProcess.insertOrUpdateInPcuInQueue(pcuInQueueRequest);

                nextStepOperation.append(routingStep.getOperation()).append(", ");
            }
            if (pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO)) {
                if (pcuProcess.complete(pcuCompleteReqWithBO)) {

                    messageModel = MessageModel.builder()
                            .message_details(new MessageDetails("Operation " + operation + " Completed Successfully and moved to operations: " + nextStepOperation.toString().trim(), "S"))
                            .build();
                }
            }
        }

        return messageModel;
    }

}


