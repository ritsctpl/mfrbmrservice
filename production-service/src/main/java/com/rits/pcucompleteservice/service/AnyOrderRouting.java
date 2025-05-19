package com.rits.pcucompleteservice.service;

import com.rits.Utility.BOConverter;
import com.rits.pcucompleteservice.dto.*;
import com.rits.pcucompleteservice.model.MessageDetails;
import com.rits.pcucompleteservice.model.MessageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Lazy
@RequiredArgsConstructor
public class AnyOrderRouting {

    private final WebClient.Builder webClientBuilder;
    @Value("${pcurouterheader-service.url}/getOperationNextStepID")
    private String getOperationNextStepIdUrl;
    @Value("${routing-service.url}/findStepDetailsByNextStepId")
    private String findStepDetailsByNextStepIdUrl;
    private final PcuProcess pcuProcess;
    public MessageModel executeAnyOrderLogic(PcuRequest pcuRequest, PcuCompleteRequestInfo pcuCompleteReqWithBO, List<RoutingStep> operationQueueList) throws Exception {
        PcuCompleteRequestInfo temporaryRequest = new PcuCompleteRequestInfo(pcuCompleteReqWithBO);
        StringBuilder operationList = new StringBuilder();
        for (RoutingStep routingStep : operationQueueList) {
            if (routingStep.getStepType().equalsIgnoreCase("operation")) {
                PcuCompleteRequestInfo pcuInQueueRequest = temporaryRequest;
                String operationBo="OperationBO:"+pcuInQueueRequest.getSite()+","+routingStep.getOperation()+","+routingStep.getOperationVersion();
                pcuInQueueRequest.setOperationBO(operationBo);
                pcuInQueueRequest.setQtyCompleted(pcuCompleteReqWithBO.getQtyCompleted());
                pcuInQueueRequest.setStepID(routingStep.getStepId());
                pcuProcess.insertOrUpdateInPcuInQueue(pcuInQueueRequest);

                operationList.append(routingStep.getOperation()).append(", ");
            } else {
                for (RoutingStep step : routingStep.getRouterDetails().get(0).getRoutingStepList()) {
                    if (step.isEntryStep() && step.getStepType().equalsIgnoreCase("operation")) {
                        PcuCompleteRequestInfo pcuInQueueRequest = temporaryRequest;
                        String operationBo="OperationBO:"+pcuInQueueRequest.getSite()+","+step.getOperation()+","+step.getOperationVersion();
                        pcuInQueueRequest.setOperationBO(operationBo);
                        pcuInQueueRequest.setQtyCompleted(pcuCompleteReqWithBO.getQtyCompleted());
                        pcuInQueueRequest.setStepID(step.getStepId());
                        pcuInQueueRequest.setChildRouterBO(routingStep.getRoutingBO());
                        pcuInQueueRequest.setParentStepID(routingStep.getStepId());
                        pcuProcess.insertOrUpdateInPcuInQueue(pcuInQueueRequest);

                        operationList.append(step.getOperation()).append(", ");
                    }
                }
            }
        }

        if (pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO)) {
            if (pcuProcess.complete(pcuCompleteReqWithBO)) {
                pcuProcess.updateNeedsToBeCompleted(pcuRequest);

                return MessageModel.builder()
                        .message_details(new MessageDetails("Operation "+ BOConverter.getOperation(pcuCompleteReqWithBO.getOperationBO())+" Completed Successfully and moved to operations: " + operationList.toString().trim(), "S"))
                        .build();
            }
        }

        return MessageModel.builder().message_details(new MessageDetails("Exception Occurred in Operation Complete", "E")).build();
    }



}
