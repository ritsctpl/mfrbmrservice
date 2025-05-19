package com.rits.pcucompleteservice.service;

import com.rits.dispositionlogservice.dto.DispositionLogRequest;
import com.rits.dispositionlogservice.model.DispositionLog;
import com.rits.nonconformanceservice.dto.DispositionRequest;
import com.rits.pcucompleteservice.dto.*;
import com.rits.pcucompleteservice.model.MessageDetails;
import com.rits.pcucompleteservice.model.MessageModel;
import com.rits.pcucompleteservice.model.PcuComplete;
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
public class ParentRoute {


    private final WebClient.Builder webClientBuilder;

    private final PcuCompleteRepository pcuCompleteRepository;

    private final PcuProcess pcuProcess;

    private final SequentialRouting sequentialRouting;
    private final  SimultaneousRouting simultaneousRouting;
    private final AnyOrderRouting anyOrderRouting;
    @Value("${pcurouterheader-service.url}/isLastReportingStep")
    private String isLastReportingStepUrl;

    @Value("${dispositionlog-service.url}/findActiveRec")
    private String findDispotionLogUrl;
    @Value("${nonconformance-service.url}/Done")
    private String dispositionUrl;
private String Disposition(PcuCompleteRequestInfo pcuCompleteReqWithBO){

        String operation="";
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
                    .bodyValue(dispositionRequest)
                    .retrieve()
                    .bodyToMono(com.rits.nonconformanceservice.model.MessageModel.class)
                    .block();
            operation=dispositionLog.getFromoperationBO();
            String[] val = operation.split(",");
            operation=val[1];
            return operation;
        }
    return operation;
}

    public MessageModel excuteParentLogic(String subType, PcuRequest pcuRequest, PcuCompleteRequestInfo pcuCompleteReqWithBO, RoutingRequest routingRequest ) throws Exception {
        String output="";
        Boolean excute=false;

        if (subType.equalsIgnoreCase("sequential")){
            if(isLastReportingStep(pcuRequest)){
                if(checkAllQtyCompleted(pcuCompleteReqWithBO)){
                    if(pcuProcess.insertInPcuDone(pcuCompleteReqWithBO)) {
                        if(pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO)) {
                            String operation=Disposition(pcuCompleteReqWithBO);
                           if(!operation.equalsIgnoreCase("")) {
                               return MessageModel.builder().message_details(new MessageDetails("Pcu is Moved to the Orignal Route at the operation " + operation, "S")).build();
                           }
                           else{
                               return MessageModel.builder().message_details(new MessageDetails("Pcu Done Successfull" , "S")).build();
                           }
                        }
                        else{
                            return MessageModel.builder().message_details(new MessageDetails("Exception occurred while deleting in work", "E")).build();

                        }
                    }else{
                        return MessageModel.builder().message_details(new MessageDetails("Exception occurred while inserting in done", "E")).build();

                    }
                }
                else{
                    pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO);
                    pcuProcess.complete(pcuCompleteReqWithBO);

                    return MessageModel.builder().message_details(new MessageDetails("Pcu Completed Successful","S")).build();

                }
            }
            else{
                return sequentialRouting.executeSequentialLogic(pcuRequest, pcuCompleteReqWithBO,routingRequest);

            }
        } else if (subType.equalsIgnoreCase("anyOrder")) {
            List<RoutingStep> getOperationQueueList =pcuProcess.getOperationQueueList(pcuRequest);
            if (getOperationQueueList == null || getOperationQueueList.isEmpty()) {

                if (checkAllQtyCompleted(pcuCompleteReqWithBO)) {
                   if( pcuProcess.insertInPcuDone(pcuCompleteReqWithBO)){
                       if(pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO)){
                           return MessageModel.builder().message_details(new MessageDetails("Pcu Done Successful","S")).build();
                       }else {
                           return MessageModel.builder().message_details(new MessageDetails("Exception occurred while deleting in work", "E")).build();
                       }
                   }else{
                       return MessageModel.builder().message_details(new MessageDetails("Exception occurred while inserting in done", "E")).build();
                   }

                }
                else{
                    pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO);
                    pcuProcess.complete(pcuCompleteReqWithBO);
                    pcuProcess.updateNeedsToBeCompleted(pcuRequest);
                    return MessageModel.builder().message_details(new MessageDetails("Pcu Completed Successful","S")).build();
                }
            }else{
                return anyOrderRouting.executeAnyOrderLogic(pcuRequest, pcuCompleteReqWithBO,getOperationQueueList);
            }
        }else if(subType.equalsIgnoreCase("simultaneous")){
            List<RoutingStep> getOperationQueueList =pcuProcess.getOperationQueueList(pcuRequest);
            if (getOperationQueueList == null || getOperationQueueList.isEmpty()) {

                if (checkAllQtyCompleted(pcuCompleteReqWithBO)) {
                    if( pcuProcess.insertInPcuDone(pcuCompleteReqWithBO)){
                        if(pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO)){
                            return MessageModel.builder().message_details(new MessageDetails("Pcu Done Successful","S")).build();
                        }else {
                            return MessageModel.builder().message_details(new MessageDetails("Exception occurred while deleting in work", "E")).build();
                        }
                    }else{
                        return MessageModel.builder().message_details(new MessageDetails("Exception occurred while inserting in done", "E")).build();
                    }
                }
                else{
                    pcuProcess.updateOrDeleteInPcuInWork(pcuCompleteReqWithBO);
                    pcuProcess.complete(pcuCompleteReqWithBO);
                    pcuProcess.updateNeedsToBeCompleted(pcuRequest);
                    return MessageModel.builder().message_details(new MessageDetails("Pcu Completed Successful","S")).build();

                }
            }else{
                return simultaneousRouting.executeSimultaneousLogic(pcuRequest, pcuCompleteReqWithBO,routingRequest,getOperationQueueList);

            }
        }
        return null;
    }
    private Boolean isLastReportingStep(PcuRequest pcuRequest){

        Boolean isLastReportingStep = webClientBuilder.build()
                .post()
                .uri(isLastReportingStepUrl)
                .bodyValue(pcuRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return isLastReportingStep;
    }
    public boolean checkAllQtyCompleted(PcuCompleteRequestInfo pcuCompleteReqWithBO) throws Exception {
        List<PcuComplete> listofPCUsCompleted = pcuCompleteRepository.findByActiveAndSiteAndPcuBO(1, pcuCompleteReqWithBO.getSite(), pcuCompleteReqWithBO.getPcuBO());
        boolean completed = true;
        for (PcuComplete pcusObj : listofPCUsCompleted) {
            if(!pcusObj.getPcuBO().equals(pcuCompleteReqWithBO.getPcuBO())) {
                if (!pcusObj.getQtyToComplete().equals(String.valueOf(0))) {
                    completed = false;
                    break;
                }
            }
        }
        String[] op = pcuCompleteReqWithBO.getOperationBO().split(",");
        PcuComplete currentOperation= pcuCompleteRepository.findByActiveAndSiteAndPcuBOAndOperationBO(1, pcuCompleteReqWithBO.getSite(), pcuCompleteReqWithBO.getPcuBO(), op[1]);
        if(currentOperation!=null && currentOperation.getQtyToComplete()!=null && !currentOperation.getQtyToComplete().isEmpty()) {
            if (completed && (!pcuCompleteReqWithBO.getQtyCompleted().equals(currentOperation.getQtyToComplete()))) {
                return false;
            }
        }
        else{
            if(completed && (!(Double.parseDouble(pcuCompleteReqWithBO.getQtyCompleted()) == Double.parseDouble((pcuCompleteReqWithBO.getQtyToComplete()))))){
                return false;
            }
        }
        return completed;
    }


}
