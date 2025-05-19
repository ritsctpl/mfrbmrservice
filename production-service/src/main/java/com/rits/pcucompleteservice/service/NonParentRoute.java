package com.rits.pcucompleteservice.service;

import com.rits.Utility.BOConverter;
import com.rits.pcucompleteservice.dto.*;
import com.rits.pcucompleteservice.exception.PcuCompleteException;
import com.rits.pcucompleteservice.model.MessageDetails;
import com.rits.pcucompleteservice.model.MessageModel;
import com.rits.pcucompleteservice.repository.PcuCompleteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Lazy
@RequiredArgsConstructor
public class NonParentRoute {
    private final WebClient.Builder webClientBuilder;
    private final PcuCompleteRepository pcuCompleteRepository;
    private final PcuProcess pcuProcess;
    private final SequentialRouting sequentialRouting;
    @Value("${routing-service.url}/findNextStepIDDetails")
    private String findNextStepIDDetailsUrl;
    @Value("${routing-service.url}/findNextStepIDDetailsOfParentStepId")
    private String findNextStepIDDetailsOfParentStepIdUrl;
    public MessageModel executeNonParentLogic(String subType, PcuRequest pcuRequest, PcuCompleteRequestInfo pcuCompleteReqWithBO, RoutingRequest routingRequest ) throws Exception {
        PcuCompleteRequestInfo temporaryRequest = new PcuCompleteRequestInfo(pcuCompleteReqWithBO);
        MessageModel messageModel=null;
        String operation= BOConverter.getOperation(pcuCompleteReqWithBO.getOperationBO());
        String version=BOConverter.getOperationVersion(pcuCompleteReqWithBO.getOperationBO());
//
        String parentRouting = BOConverter.getRouting(pcuCompleteReqWithBO.getRouterBO());
        String parentVersion = BOConverter.getRouterVersion(pcuCompleteReqWithBO.getRouterBO());
        String output = "";
        Boolean execute = false;
        if (subType.equalsIgnoreCase("sequential")) {

            RoutingMessageModel nextStep = findNextStep(routingRequest);
            if (nextStep.getMessage_details().getMsg().equalsIgnoreCase("NextStepId")) {
                String nextSTepId=nextStep.getNextStepId();
                String[] stepIds =nextSTepId.split(",");
                if (stepIds.length > 1){
                    if (pcuCompleteReqWithBO.getNextStepId() == null) {
                        String nextSteps = pcuProcess.nextSTepId(routingRequest, nextSTepId);
                        messageModel= MessageModel.builder().message_details(new MessageDetails(nextSteps, "NextStepId")).build();
                    }else{
                        routingRequest.setStepId(pcuCompleteReqWithBO.getNextStepId());
                        RoutingStep routingResponses = sequentialRouting.findStepDetailsByNextStepId(routingRequest);
                        if(routingResponses.getStepType().equalsIgnoreCase("Operation")){
                            if(sequentialRouting.excuteSeqOprLogic(routingResponses, pcuCompleteReqWithBO)){
                                //splitIt
                                messageModel=MessageModel.builder().message_details(new MessageDetails("Operation "+operation+ " Completed Successfully and moved to operation :"+routingResponses.getOperation(), "S")).build();
                            }
                            else{
                                messageModel=MessageModel.builder().message_details(new MessageDetails("Exception occured in Operation Complete", "E")).build();
                            }
                        }else{
                            messageModel=sequentialRouting.executeROutingLogic(routingResponses, pcuCompleteReqWithBO,temporaryRequest,pcuRequest);
                        }
                    }

                }
            } else {
                RoutingStep routingResponse = nextStep.getRoutingStep();
                if (routingResponse == null || routingResponse.getStepType() == null) {

                    RoutingRequest nextStepIdByParentId = RoutingRequest.builder().site(pcuCompleteReqWithBO.getSite()).routing(parentRouting).version(parentVersion).operation(operation).operationVersion(version).stepId(pcuCompleteReqWithBO.getParentStepID()).pcuBo(pcuCompleteReqWithBO.getPcuBO()).build();

                    RoutingMessageModel nextStepByParent = findParentNextStep(nextStepIdByParentId);
                    pcuCompleteReqWithBO.setParentStepID(null);
                    pcuCompleteReqWithBO.setChildRouterBO(null);
                    temporaryRequest.setParentStepID(null);
                    temporaryRequest.setChildRouterBO(null);
                    if (nextStepByParent.getMessage_details().getMsg().equalsIgnoreCase("NextStepId")) {
                        String nextSTepId=nextStepByParent.getNextStepId();
                        String[] stepIds =nextSTepId.split(",");
                        if (stepIds.length > 1){
                            if (pcuCompleteReqWithBO.getNextStepId() == null) {
                                String nextSteps = pcuProcess.nextSTepId(nextStepIdByParentId, nextSTepId);
                                messageModel= MessageModel.builder().message_details(new MessageDetails(nextSteps, "NextStepId")).build();
                            }else{
                                nextStepIdByParentId.setStepId(pcuCompleteReqWithBO.getNextStepId());
                                RoutingStep routingResponses = sequentialRouting.findStepDetailsByNextStepId(nextStepIdByParentId);
                                if(routingResponses.getStepType().equalsIgnoreCase("Operation")){
                                    if(sequentialRouting.excuteSeqOprLogic(routingResponses, pcuCompleteReqWithBO)){
                                        messageModel=MessageModel.builder().message_details(new MessageDetails("Operation "+operation+" Completed Successfully and moved to operation: "+routingResponses.getOperation(), "S")).build();
                                    }
                                    else{
                                        messageModel=MessageModel.builder().message_details(new MessageDetails("Exception occured in Operation Complete", "E")).build();
                                    }
                                }else{
                                    messageModel=sequentialRouting.executeROutingLogic(routingResponses, pcuCompleteReqWithBO,temporaryRequest,pcuRequest);
                                }
                            }

                        }
                    } else {
                        if (nextStepByParent.getRoutingStep() != null && nextStepByParent.getRoutingStep().getStepType() != null) {
                            RoutingStep routingStep = nextStepByParent.getRoutingStep();
                            String processByOperationOrRouting = processByOperationOrRouting(routingStep, pcuCompleteReqWithBO, temporaryRequest);
                            if (processByOperationOrRouting!=null) {
                                pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO);
                                pcuProcess.complete(pcuCompleteReqWithBO);
                                messageModel=MessageModel.builder().message_details(new MessageDetails("Operation "+operation+" Completed Successfully and moved to operations: "+processByOperationOrRouting, "S")).build();
                            }
                        } else {
                            pcuProcess.complete(pcuCompleteReqWithBO);
                            pcuProcess.insertInPcuDone(pcuCompleteReqWithBO);
                            pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO);
                            messageModel=  MessageModel.builder().message_details(new MessageDetails("Pcu Done Successful", "S")).build();
                        }
                    }

                } else {
                    String processByOperationOrRouting = processByOperationOrRouting(routingResponse, pcuCompleteReqWithBO, temporaryRequest);//5
                    if (processByOperationOrRouting!=null) {
                        pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO);
                        pcuProcess.complete(pcuCompleteReqWithBO);
                        messageModel=  MessageModel.builder().message_details(new MessageDetails("Operation "+operation+"Completed Successfully and moved to operations: "+processByOperationOrRouting, "S")).build();
                    }
                }
            }
        } else if (subType.equalsIgnoreCase("anyOrder") || subType.equalsIgnoreCase("simultaneous")) {
            List<RoutingStep> getOperationQueueList = pcuProcess.getOperationQueueList(pcuRequest);
            if (getOperationQueueList == null || getOperationQueueList.isEmpty()) {
                RoutingRequest nextStepIdByParentId = RoutingRequest.builder().site(pcuCompleteReqWithBO.getSite()).routing(parentRouting).version(parentVersion).operation(operation).operationVersion(version).stepId(pcuCompleteReqWithBO.getParentStepID()).pcuBo(pcuCompleteReqWithBO.getPcuBO()).build();

                RoutingMessageModel nextStepByParent = findParentNextStep(nextStepIdByParentId);
                pcuCompleteReqWithBO.setParentStepID(null);
                pcuCompleteReqWithBO.setChildRouterBO(null);
                temporaryRequest.setParentStepID(null);
                temporaryRequest.setChildRouterBO(null);
                if (nextStepByParent.getMessage_details().getMsg().equalsIgnoreCase("NextStepId")) {
                    String nextSTepId=nextStepByParent.getNextStepId();
                    String[] stepIds =nextSTepId.split(",");
                    if (stepIds.length > 1){
                        if (pcuCompleteReqWithBO.getNextStepId() == null) {
                            String nextSteps = pcuProcess.nextSTepId(nextStepIdByParentId, nextSTepId);
                            messageModel= MessageModel.builder().message_details(new MessageDetails(nextSteps, "NextStepId")).build();
                        }else{
                            nextStepIdByParentId.setStepId(pcuCompleteReqWithBO.getNextStepId());

                            RoutingStep routingResponses = sequentialRouting.findStepDetailsByNextStepId(nextStepIdByParentId);
                            if(routingResponses.getStepType().equalsIgnoreCase("Operation")){
                                if(sequentialRouting.excuteSeqOprLogic(routingResponses, pcuCompleteReqWithBO)){
                                    //SplitIt
                                    messageModel=MessageModel.builder().message_details(new MessageDetails("Operation "+ operation+" Completed Successfully and moved to operation :"+routingResponses.getOperation(), "S")).build();
                                }
                                else{
                                    messageModel=MessageModel.builder().message_details(new MessageDetails("Exception occured in Operation Complete", "E")).build();
                                }
                            }else{
                                messageModel=sequentialRouting.executeROutingLogic(routingResponses, pcuCompleteReqWithBO,temporaryRequest,pcuRequest);
                            }
                        }

                    }
                } else {
                    if (nextStepByParent.getRoutingStep() != null && nextStepByParent.getRoutingStep().getStepType() != null) {
                        RoutingStep routingStep = nextStepByParent.getRoutingStep();
                        String processByOperationOrRouting = processByOperationOrRouting(routingStep, pcuCompleteReqWithBO, temporaryRequest);
                        if (processByOperationOrRouting!=null) {
                            pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO);
                            pcuProcess.complete(pcuCompleteReqWithBO);
                            pcuProcess.updateNeedsToBeCompleted(pcuRequest);
                            messageModel=  MessageModel.builder().message_details(new MessageDetails("Operation "+operation +" Completed Successfully and moved to operations: "+processByOperationOrRouting, "S")).build();

                        }
                    }else{
                        pcuProcess.complete(pcuCompleteReqWithBO);
                        pcuProcess.insertInPcuDone(pcuCompleteReqWithBO);
                        pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO);
                        messageModel=  MessageModel.builder().message_details(new MessageDetails("Pcu Done Successful", "S")).build();
                        }
                    }
                }else{
                    String processByOperationOrRouting = processListByOperationOrRouting(getOperationQueueList, pcuCompleteReqWithBO, temporaryRequest);
                    if (processByOperationOrRouting!=null) {
                        pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO);
                        pcuProcess.complete(pcuCompleteReqWithBO);
                        pcuProcess.updateNeedsToBeCompleted(pcuRequest);
                        messageModel=  MessageModel.builder().message_details(new MessageDetails("Operation "+operation +" Completed Successfully and moved to operations: "+processByOperationOrRouting, "S")).build();
                    }
                }
            }

        return  messageModel;
    }

    private String processListByOperationOrRouting(List<RoutingStep> getOperationQueueList, PcuCompleteRequestInfo pcuCompleteReqWithBO, PcuCompleteRequestInfo temporaryRequest) throws Exception {
        StringBuilder nextStepOperation = new StringBuilder();

        for (RoutingStep routingStep : getOperationQueueList) {
            if (routingStep.getStepType().equalsIgnoreCase("operation")) {
                PcuCompleteRequestInfo pcuInQueueRequest = temporaryRequest;
                //setIt
                String operationBo="OperationBO:"+pcuInQueueRequest.getSite()+","+routingStep.getOperation()+","+routingStep.getOperationVersion();
                pcuInQueueRequest.setOperationBO(operationBo);
                pcuInQueueRequest.setQtyCompleted(pcuCompleteReqWithBO.getQtyCompleted());
                pcuInQueueRequest.setStepID(routingStep.getStepId());
                pcuProcess.insertOrUpdateInPcuInQueue(pcuInQueueRequest);

                nextStepOperation.append(routingStep.getOperation()).append(", ");
            } else {
                for (RoutingStep step : routingStep.getRouterDetails().get(0).getRoutingStepList()) {
                    if (step.isEntryStep() && step.getStepType().equalsIgnoreCase("operation")) {
                        PcuCompleteRequestInfo pcuInQueueRequest = temporaryRequest;
                        String operationBo="OperationBO:"+pcuInQueueRequest.getSite()+","+step.getOperation()+","+step.getOperationVersion();
                        pcuInQueueRequest.setOperationBO(operationBo);
                        pcuInQueueRequest.setQtyCompleted(pcuCompleteReqWithBO.getQtyCompleted());
                        pcuInQueueRequest.setStepID(step.getStepId());
                        pcuInQueueRequest.setParentStepID(routingStep.getStepId());
                        pcuInQueueRequest.setChildRouterBO(routingStep.getRoutingBO());
                        pcuProcess.insertOrUpdateInPcuInQueue(pcuInQueueRequest);

                        nextStepOperation.append(step.getOperation()).append(", ");
                    }
                }
            }
        }

        return nextStepOperation.toString();
    }


    public RoutingMessageModel findNextStep(RoutingRequest routingRequest){
        RoutingMessageModel routingRes = webClientBuilder.build()
                .post()
                .uri(findNextStepIDDetailsUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(RoutingMessageModel.class)
                .block();
        if (routingRes == null ||routingRes.getMessage_details()==null|| routingRes.getMessage_details().getMsg_type()==null) {

            throw new PcuCompleteException(3807);
        }
        return routingRes;
    }
    public RoutingMessageModel findParentNextStep(RoutingRequest routingRequest){
        RoutingMessageModel routingMessageModel = webClientBuilder.build()
                .post()
                .uri(findNextStepIDDetailsOfParentStepIdUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(RoutingMessageModel.class)
                .block();
        if (routingMessageModel == null || routingMessageModel.getMessage_details().getMsg_type()==null) {
            throw new PcuCompleteException(500, routingRequest.getRouting(), routingRequest.getVersion());
        }
        return routingMessageModel;
    }

    public String processByOperationOrRouting(RoutingStep routingStep, PcuCompleteRequestInfo pcuCompleteReqWithBO, PcuCompleteRequestInfo temporaryRequest) throws Exception {
        StringBuilder nextStepOperation= new StringBuilder();
        if (routingStep.getStepType().equalsIgnoreCase("operation")) {
            PcuCompleteRequestInfo pcuInQueueRequest = temporaryRequest;
            String operationBo="OperationBO:"+pcuInQueueRequest.getSite()+","+routingStep.getOperation()+","+routingStep.getOperationVersion();
            pcuInQueueRequest.setOperationBO(operationBo);
            pcuInQueueRequest.setQtyCompleted(pcuCompleteReqWithBO.getQtyCompleted());
            pcuInQueueRequest.setStepID(routingStep.getStepId());
            pcuProcess.insertOrUpdateInPcuInQueue(pcuInQueueRequest);

            nextStepOperation.append(routingStep.getOperation()).append(", ");
        } else {
            for (RoutingStep step : routingStep.getRouterDetails().get(0).getRoutingStepList()) {
                if (step.isEntryStep() && step.getStepType().equalsIgnoreCase("operation")) {
                    PcuCompleteRequestInfo pcuInQueueRequest = temporaryRequest;
                    String operationBo="OperationBO:"+pcuInQueueRequest.getSite()+","+step.getOperation()+","+step.getOperationVersion();
                    pcuInQueueRequest.setOperationBO(operationBo);
                    pcuInQueueRequest.setQtyCompleted(pcuCompleteReqWithBO.getQtyCompleted());
                    pcuInQueueRequest.setStepID(step.getStepId());
                    pcuInQueueRequest.setParentStepID(routingStep.getStepId());
                    pcuInQueueRequest.setChildRouterBO(routingStep.getRoutingBO());
                    pcuProcess.insertOrUpdateInPcuInQueue(pcuInQueueRequest);

                    nextStepOperation.append(step.getOperation()).append(", ");
                }
            }
        }

        return nextStepOperation.toString();
    }

}
