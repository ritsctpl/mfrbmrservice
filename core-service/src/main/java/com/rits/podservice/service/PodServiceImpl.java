package com.rits.podservice.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.podservice.dto.*;
import com.rits.podservice.exception.PodException;
import com.rits.podservice.model.*;
import com.rits.podservice.repository.PodRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Data
@RequiredArgsConstructor
@Service
public class PodServiceImpl implements PodService{
    private final PodRepository podRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${resource-service.url}/isExist")
    private String resoureIsExistUrl;

    @Value("${operation-service.url}/isExist")
    private String operationIsExistUrl;

    @Value("${document-service.url}/retrieve")
    private String documentRetrieveUrl;

    @Value("${resource-service.url}/retrieveByResource")
    private String resourceDetailsUrl;

//    @Value("${auditlog-service.url}/producer")
//    private String auditlogUrl;


    @Override
    public MessageModel createPod(PodRequest podRequest) throws Exception{
        long recordPresent = podRepository.countByPodNameAndSiteAndActive(podRequest.getPodName(),podRequest.getSite(),1);
        if (recordPresent > 0) {
            throw new PodException(1800, podRequest.getPodName());
        }

        if(!podRequest.getDefaultResource().isEmpty() && podRequest.getDefaultResource()!=null) {
            ResourceRequest resourceRequest = ResourceRequest.builder().site(podRequest.getSite()).resource(podRequest.getDefaultResource()).build();
            Boolean resourceIsExist = webClientBuilder
                    .build()
                    .post()
                    .uri(resoureIsExistUrl)
                    .body(BodyInserters.fromValue(resourceRequest))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if (Boolean.FALSE.equals(resourceIsExist))
                throw new PodException(1806, podRequest.getDefaultResource());
        }

        if(!podRequest.getDefaultOperation().isEmpty() && podRequest.getDefaultOperation()!=null) {
            OperationRequest operationRequest = OperationRequest.builder().site(podRequest.getSite()).operation(podRequest.getDefaultOperation()).build();
            Boolean operationIsExist = webClientBuilder
                    .build()
                    .post()
                    .uri(operationIsExistUrl)
                    .body(BodyInserters.fromValue(operationRequest))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if (Boolean.FALSE.equals(operationIsExist))
                throw new PodException(1805, podRequest.getDefaultOperation());
        }

        if(!podRequest.getDocumentName().isEmpty() && podRequest.getDocumentName()!=null) {
            DocumentRequest documentRequest = DocumentRequest.builder().site(podRequest.getSite()).document(podRequest.getDocumentName()).build();
            Document documentIsExist = webClientBuilder
                    .build()
                    .post()
                    .uri(documentRetrieveUrl)
                    .body(BodyInserters.fromValue(documentRequest))
                    .retrieve()
                    .bodyToMono(Document.class)
                    .block();

            if (documentIsExist.getDocument() == null)
                throw new PodException(1804, podRequest.getDocumentName());
        }


        Pod pod = Pod.builder()
                .site(podRequest.getSite())
                .podName(podRequest.getPodName())
                .handle("PodBO:"+podRequest.getSite()+","+podRequest.getPodName())
                .description(podRequest.getDescription())
                .type(podRequest.getType())
                .resourceType(podRequest.getResourceType())
                .podCategory(podRequest.getPodCategory())
                .panelLayout(podRequest.getPanelLayout())
                .status(podRequest.getStatus())
                .displayDevice(podRequest.getDisplayDevice())
                .displaySize(podRequest.getDisplaySize())
                .ncClient(podRequest.getNcClient())
                .realTimeMessageDisplay(podRequest.getRealTimeMessageDisplay())
                .specialInstructionDisplay(podRequest.getSpecialInstructionDisplay())
                .kafkaIntegration(podRequest.isKafkaIntegration())
                .kafkaId(podRequest.getKafkaId())
                .sessionTimeout(podRequest.getSessionTimeout())
                .refreshRate(podRequest.getRefreshRate())
                .defaultOperation(podRequest.getDefaultOperation())
                .defaultPhaseId(podRequest.getDefaultPhaseId())
                .defaultResource(podRequest.getDefaultResource())
                .soundWithErrorMessage(podRequest.isSoundWithErrorMessage())
                .showHomeIcon(podRequest.isShowHomeIcon())
                .showHelpIcon(podRequest.isShowHelpIcon())
                .showLogoutIcon(podRequest.isShowLogoutIcon())
                .showOperation(podRequest.isShowOperation())
                .showResource(podRequest.isShowResource())
                .showPhase(podRequest.isShowPhase())
                .autoExpandMessageArea(podRequest.isAutoExpandMessageArea())
                .operationCanBeChanged(podRequest.isOperationCanBeChanged())
                .resourceCanBeChanged(podRequest.isResourceCanBeChanged())
                .phaseCanBeChanged(podRequest.isPhaseCanBeChanged())
                .showQuantity(podRequest.isShowQuantity())
                .pcuQueueButtonId(podRequest.getPcuQueueButtonId())
                .pcuInWorkButtonId(podRequest.getPcuInWorkButtonId())
                .documentName(podRequest.getDocumentName())
                .buttonList(podRequest.getButtonList())
                .listOptions(podRequest.getListOptions())
                .podSelection(podRequest.getPodSelection())
                .printers(podRequest.getPrinters())
                .customDataList(podRequest.getCustomDataList())
                .subPod(podRequest.getSubPod())
                .tabConfiguration(podRequest.getTabConfiguration())
                .createdBy(podRequest.getUserId())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .settings(podRequest.getSettings())
                .layout(podRequest.getLayout())
                .build();

        return MessageModel.builder().message_details(new MessageDetails("Created SuccessFull","S")).response(podRepository.save(pod)).build();
    }
    @Override
    public Pod retrievePod(PodRequest podRequest) throws Exception {
        Pod podResponse = podRepository.findByPodNameAndSiteAndActive(podRequest.getPodName(), podRequest.getSite(), 1);
        if (podResponse != null ) {
            ResourceRequest resourceStatusRequest = ResourceRequest.builder()
                    .site(podResponse.getSite())
                    .resource(podResponse.getDefaultResource())
                    .build();

            Map<String, Object> resourceStatusMap = webClientBuilder.build()
                    .post()
                    .uri(resourceDetailsUrl)
                    .bodyValue(resourceStatusRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (resourceStatusMap != null) {
               podResponse.setResourceStatus(resourceStatusMap.get("status").toString());
            }
            return podResponse;
        } else {
            throw new PodException(1801, podRequest.getPodName());
        }
    }
    @Override
    public MessageModel updatePod(PodRequest podRequest) throws Exception{
        Pod pod=podRepository.findByPodNameAndSiteAndActive(podRequest.getPodName(),podRequest.getSite(),1);

        if(!podRequest.getDefaultResource().isEmpty() && podRequest.getDefaultResource()!=null) {
            ResourceRequest resourceRequest = ResourceRequest.builder().site(podRequest.getSite()).resource(podRequest.getDefaultResource()).build();
            Boolean resourceIsExist = webClientBuilder
                    .build()
                    .post()
                    .uri(resoureIsExistUrl)
                    .body(BodyInserters.fromValue(resourceRequest))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if (Boolean.FALSE.equals(resourceIsExist))
                throw new PodException(1806, podRequest.getDefaultResource());
        }

        if(!podRequest.getDefaultOperation().isEmpty() && podRequest.getDefaultOperation()!=null) {
            OperationRequest operationRequest = OperationRequest.builder().site(podRequest.getSite()).operation(podRequest.getDefaultOperation()).build();
            Boolean operationIsExist = webClientBuilder
                    .build()
                    .post()
                    .uri(operationIsExistUrl)
                    .body(BodyInserters.fromValue(operationRequest))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if (Boolean.FALSE.equals(operationIsExist))
                throw new PodException(1805, podRequest.getDefaultOperation());
        }

        if(!podRequest.getDocumentName().isEmpty() && podRequest.getDocumentName()!=null) {
            DocumentRequest documentRequest = DocumentRequest.builder().site(podRequest.getSite()).document(podRequest.getDocumentName()).build();
            Document documentIsExist = webClientBuilder
                    .build()
                    .post()
                    .uri(documentRetrieveUrl)
                    .body(BodyInserters.fromValue(documentRequest))
                    .retrieve()
                    .bodyToMono(Document.class)
                    .block();

            if (documentIsExist.getDocument() == null)
                throw new PodException(1804, podRequest.getDocumentName());
        }

        if(pod!=null){
            pod=Pod.builder()
                    .id(pod.getId())
                    .site(pod.getSite())
                    .handle(pod.getHandle())
                    .podName(pod.getPodName())
                    .description(podRequest.getDescription())
                    .type(podRequest.getType())
                    .resourceType(podRequest.getResourceType())
                    .podCategory(podRequest.getPodCategory())
                    .panelLayout(podRequest.getPanelLayout())
                    .status(podRequest.getStatus())
                    .displayDevice(podRequest.getDisplayDevice())
                    .displaySize(podRequest.getDisplaySize())
                    .ncClient(podRequest.getNcClient())
                    .realTimeMessageDisplay(podRequest.getRealTimeMessageDisplay())
                    .specialInstructionDisplay(podRequest.getSpecialInstructionDisplay())
                    .kafkaId(podRequest.getKafkaId())
                    .kafkaIntegration(podRequest.isKafkaIntegration())
                    .sessionTimeout(podRequest.getSessionTimeout())
                    .refreshRate(podRequest.getRefreshRate())
                    .resourceCanBeChanged(podRequest.isResourceCanBeChanged())
                    .operationCanBeChanged(podRequest.isOperationCanBeChanged())
                    .defaultOperation(podRequest.getDefaultOperation())
                    .defaultPhaseId(podRequest.getDefaultPhaseId())
                    .defaultResource(podRequest.getDefaultResource())
                    .phaseCanBeChanged(podRequest.isPhaseCanBeChanged())
                    .soundWithErrorMessage(podRequest.isSoundWithErrorMessage())
                    .showHomeIcon(podRequest.isShowHomeIcon())
                    .showHelpIcon(podRequest.isShowHelpIcon())
                    .showLogoutIcon(podRequest.isShowLogoutIcon())
                    .showOperation(podRequest.isShowOperation())
                    .showResource(podRequest.isShowResource())
                    .showPhase(podRequest.isShowPhase())
                    .autoExpandMessageArea(podRequest.isAutoExpandMessageArea())
                    .showQuantity(podRequest.isShowQuantity())
                    .pcuInWorkButtonId(podRequest.getPcuInWorkButtonId())
                    .pcuQueueButtonId(podRequest.getPcuQueueButtonId())
                    .documentName(podRequest.getDocumentName())
                    .buttonList(podRequest.getButtonList())
                    .listOptions(podRequest.getListOptions())
                    .podSelection(podRequest.getPodSelection())
                    .printers(podRequest.getPrinters())
                    .customDataList(podRequest.getCustomDataList())
                    .subPod(podRequest.getSubPod())
                    .tabConfiguration(podRequest.getTabConfiguration())
                    .active(pod.getActive())
                    .createdDateTime(pod.getCreatedDateTime())
                    .modifiedBy(podRequest.getUserId())
                    .modifiedDateTime(LocalDateTime.now())

                    .settings(podRequest.getSettings())
                    .layout(podRequest.getLayout())
                    .build();
//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(podRequest.getSite())
//                    .change_stamp("Update")
//                    .action_code("POD-UPDATE")
//                    .action_detail("POD UPDATED "+podRequest.getPodName())
//                    .action_detail_handle("ActionDetailBO:"+podRequest.getSite()+","+"POD-UPDATE"+","+podRequest.getUserId()+":"+"com.rits.podservice.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(podRequest.getUserId())
//                    .txnId("POD-UPDATE"+String.valueOf(LocalDateTime.now())+podRequest.getUserId())
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();

            return MessageModel.builder().message_details(new MessageDetails("Updated SuccessFull","S")).response(podRepository.save(pod)).build();

        }else{
            throw new PodException(1801, podRequest.getPodName());
        }
    }
    @Override
    public MessageModel deletePod(DeleteRequest deleteRequest) throws Exception {
        if (podRepository.existsByPodNameAndSiteAndActive(deleteRequest.getPodName(), deleteRequest.getSite(), 1)) {
            Pod existingpod = podRepository.findByPodNameAndSiteAndActive(deleteRequest.getPodName(), deleteRequest.getSite(), 1);
            if (existingpod != null) {
                existingpod.setActive(0);
                existingpod.setModifiedDateTime(LocalDateTime.now());
                existingpod.setModifiedBy(deleteRequest.getUserId());
                podRepository.save(existingpod);

//                AuditLogRequest activityLog = AuditLogRequest.builder()
//                        .site(deleteRequest.getSite())
//                        .change_stamp("Delete")
//                        .action_code("POD-DELETE")
//                        .action_detail("POD DELETED "+deleteRequest.getPodName())
//                        .action_detail_handle("ActionDetailBO:"+deleteRequest.getSite()+","+"POD-DELETE"+","+deleteRequest.getUserId()+":"+"com.rits.podservice.service")
//                        .date_time(String.valueOf(LocalDateTime.now()))
//                        .userId(deleteRequest.getUserId())
//                        .txnId("POD-DELETE"+String.valueOf(LocalDateTime.now())+deleteRequest.getUserId())
//                        .created_date_time(String.valueOf(LocalDateTime.now()))
//                        .build();
//
//                webClientBuilder.build()
//                        .post()
//                        .uri(auditlogUrl)
//                        .bodyValue(activityLog)
//                        .retrieve()
//                        .bodyToMono(AuditLogRequest.class)
//                        .block();
                return MessageModel.builder().message_details(new MessageDetails(deleteRequest.getPodName() + " Deleted SuccessFull", "S")).build();
            }
            throw new PodException(1801, deleteRequest.getPodName());
        }
        throw new PodException(1801, deleteRequest.getPodName());
    }


    @Override
    public RButtonResponseList getButtonList(ButtonListRequest buttonListRequest) throws Exception{
        Pod pod = podRepository.findByPodNameAndSiteAndActive(buttonListRequest.getPodName(),buttonListRequest.getSite(),1);
        if (pod != null) {
            List<RButtonResponse> buttonList = pod.getButtonList().stream()
                    .map(button -> RButtonResponse.builder()
                            .sequence(button.getSequence())
                            .buttonType(button.getButtonType())
                            .buttonId(button.getButtonId())
                            .buttonLabel(button.getButtonLabel())
                            .buttonSize(button.getButtonSize())
                            .imageIcon(button.getImageIcon())
                            .hotKey(button.getHotKey())
                            .buttonLocation(button.getButtonLocation())
                            .startNewButtonRow(button.isStartNewButtonRow())
                            .activityList(button.getActivityList().stream()
                                    .map(activity -> Activity.builder()
                                            .activitySequence(activity.getActivitySequence())
                                            .activity(activity.getActivity())
                                            .type(activity.getType())
                                            .url(activity.getUrl())
                                            .pluginLocation(activity.getPluginLocation())
                                            .clearsPcu(activity.isClearsPcu())
                                            .fixed(activity.isFixed())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build())
                    .collect(Collectors.toList());

            return RButtonResponseList.builder()
                    .buttonList(buttonList)
                    .build();
        } else {
            throw new PodException(1801, buttonListRequest.getPodName());
        }
    }
    @Override
    public PodResponseList getPodListByCreationDate(PodListRequest podListRequest) throws Exception {
        List<PodResponse> podResponses = podRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, podListRequest.getSite());
            return PodResponseList.builder().podList(podResponses).build();
    }
    @Override
    public PodResponseList getPodList(PodListRequest podListRequest) throws Exception {
        if (podListRequest.getPodName() == null || podListRequest.getPodName().isEmpty()) {
            return  getPodListByCreationDate(podListRequest);
        } else {
            List<PodResponse> podResponses = podRepository.findByPodNameContainingIgnoreCaseAndSiteAndActive(podListRequest.getPodName(), podListRequest.getSite(), 1);
                return PodResponseList.builder().podList(podResponses).build();
        }
    }
    @Override
    public Boolean isPodExist(PodExistRequest podExistRequest) throws Exception{
        return podRepository.existsByPodNameAndSiteAndActive(podExistRequest.getPodName(),podExistRequest.getSite(),1);
    }


}




