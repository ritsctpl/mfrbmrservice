package com.rits.checkhook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.Utility.BOConverter;
import com.rits.assemblyservice.service.AssemblyService;
import com.rits.checkhook.dto.DataCollectionRequest;
import com.rits.checkhook.dto.MessageModel;
import com.rits.checkhook.exception.CheckHookException;
import com.rits.dccollect.dto.*;
import com.rits.checkhook.dto.*;
import com.rits.dccollect.dto.DataCollection;
import com.rits.dccollect.dto.Operation;
import com.rits.dccollect.dto.ShopOrder;
import com.rits.logbuyoffservice.exception.LogBuyOffException;
import com.rits.logbuyoffservice.model.BuyoffLog;
import com.rits.nonconformanceservice.model.NcData;
import com.rits.nonconformanceservice.service.NonConformanceservice;
import com.rits.pcucompleteservice.dto.PcuCompleteReq;
import com.rits.pcucompleteservice.dto.PcuCompleteRequest;
import com.rits.pcucompleteservice.dto.PcuCompleteRequestInfo;
import com.rits.pcucompleteservice.dto.RequestList;
import com.rits.pcudoneservice.dto.Item;
import com.rits.pcuinqueueservice.dto.PcuInQueueReq;
import com.rits.pcuinqueueservice.model.PcuInQueue;
import com.rits.signoffservice.dto.SignOffRequest;
import com.rits.signoffservice.dto.SignOffRequestList;
import com.rits.startservice.dto.Routing;
import com.rits.startservice.dto.StartRequest;
import com.rits.startservice.dto.StartRequestList;
import com.rits.startservice.model.PcuInWork;
import com.rits.startservice.model.PcuInWorkReq;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckHookServiceImpl implements CheckHookService{
    private final WebClient.Builder webClientBuilder;
    private final AssemblyService assemblyService;
    private final NonConformanceservice nonConformanceservice;
    private final RestTemplate restTemplate;
    private final ObjectMapper  objectMapper;
    private final MessageSource localMessageSource;

    @Value("${datacollection-service.url}/retrieveByPcuAndOperationAndResource")
    private String getDataCollectionUrl;
    @Value("${start-service.url}/retrieve")
    private String retrieveInWorkUrl;
    @Value("${pcuinqueue-service.url}/retrieve")
    private String retrieveInQueueUrl;
    @Value("${operation-service.url}/retrieve")
    private String retrieveOperationUrl;
    @Value("${resource-service.url}/retrieveByResource")
    private String retrieveResourceUrl;
    @Value("${workcenter-service.url}/retrieve")
    private String retrieveWorkCenterUrl;
    @Value("${shoporder-service.url}/retrieve")
    private String retrieveShopOrderUrl;
    @Value("${routing-service.url}/retrieve")
    private String retrieveRoutingUrl;
    @Value("${item-service.url}/retrieve")
    private String retrieveItemUrl;
    @Value("${logbuyoff-service.url}/getListOfBuyoff")
    private String getBuyOffListUrl;
    @Value("${activity-service.url}/getActivityUrl")
    private String getActivityUrl;
    private String site;
    private String pcu;
    private List<ActivityHook> activityHookList=new ArrayList<>();
    @Value("${DOCKER_HOST_IP:localhost}")
    private String appHostIp;
    @Value("${APP_HOST_PORT:8080}")
    private String appHostPort;
    @Value("${APP_HOST_PORT:8585}")
    private String uiHostPort;

    @Value("${operation-service.url}/retrieveCertificateList")
    private String retriveCertificateListOperationUrl;
    @Value("${usercertificateassignment-service.url}/retrieveByUser")
    private String retriveUsercertificateassignmentUrl;
    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }



    @Override
    public MessageModel isMandatoryDataCollected(PcuCompleteReq completeReq) {
        try {
            site = completeReq.getSite();
            Attachment attachment = getAttachmentObject(completeReq);
            DataCollectionList dataCollectionList = getDataCollectionList(attachment);
            if (dataCollectionList != null && dataCollectionList.getDataCollectionList() != null && !dataCollectionList.getDataCollectionList().isEmpty()) {
                for (DataCollection dataCollection : dataCollectionList.getDataCollectionList()) {
                    if (dataCollection.isDataCollected() && !dataCollection.getCollectionMethod().equalsIgnoreCase("multiple")) {
                        return new MessageModel(false, getFormattedMessage(2102, pcu));
                    }
                }
            }
            return new MessageModel(true, null);
        } catch (Exception e) {
            return new MessageModel(false, e.getMessage());
        }
    }

    @Override
    public MessageModel isAllBuyOffApproved(PcuCompleteReq pcuCompleteReq) {
        Attachment attachment=getAttachmentObject(pcuCompleteReq);
        boolean buyOffs= getBuyOff(attachment);
        if(buyOffs){
            return new MessageModel(true,null);
        }else{
            return new MessageModel(false,getFormattedMessage(2103,pcu));

        }
    }

    @Override
    public boolean completeCheckHooks(RequestList requestList) throws CloneNotSupportedException {

        for(PcuCompleteReq completeReq :requestList.getRequestList()) {
            if (checkAllFieldsAreNotEmpty(completeReq).isSuccess()) {
                pcu = completeReq.getPcu();
                if (isPcuStarted(completeReq)) {
                    if (isAllComponentAssembled(completeReq).isSuccess()) {
                        if (isMandatoryDataCollected(completeReq).isSuccess()) {
                            if (isAllBuyOffApproved(completeReq).isSuccess()) {
                                if (isAllNcClosed(completeReq).isSuccess()) {
                                    return true;
                                } else {
                                    throw new CheckHookException(2104, pcu);
                                }

                            } else {
                                throw new CheckHookException(2103,pcu);
                            }
                        } else {
                            throw new CheckHookException(2102, pcu);
                        }

                    } else {
                        throw new CheckHookException(2101, pcu);
                    }
                } else {
                    throw new CheckHookException(2100, pcu);
                }
            }
        }
        return false;
    }

    @Override
    public boolean startCheckHook(StartRequestList pcuRequestList) throws Exception {
        if(!activityHookList.isEmpty()){
            activityHookList.removeAll(activityHookList);
        }
        if(pcuRequestList!=null && pcuRequestList.getRequestList()!=null && !pcuRequestList.getRequestList().isEmpty()) {
            for (StartRequest startRequest : pcuRequestList.getRequestList()) {
                String[] pcubo = startRequest.getPcuBO().split(",");
                pcu = pcubo[1];
                MessageModel isValid=getValidated(startRequest);
                if( isValid.isSuccess()){
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "Bearer " + pcuRequestList.getAccessToken());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    String body=objectMapper.writeValueAsString(startRequest);
                    HttpEntity<String> entity = new HttpEntity<>(body, headers);


                    if(activityHookList!=null && !activityHookList.isEmpty()){
                        Set<String> uniqueUrls=new HashSet<>();
                        List<ActivityHook> updatedActivityUrl =getUpdatedActivityUrl(activityHookList);
                        for(ActivityHook activityHook: updatedActivityUrl){
                            String url; //= "http://"+appHostIp+":"+appHostPort+"/"+activityHook.getUrl();

                            if (activityHook.getUrl().startsWith("http://")) {
                                url = activityHook.getUrl();
                            } else {
                                url = "http://" + appHostIp + ":" + appHostPort + "/" + activityHook.getUrl();
                            }
                            if (!uniqueUrls.add(url)) {
                                continue;
                            }
                            try {
                                ResponseEntity<MessageModel> responseEntity = restTemplate.exchange(
                                        url,
                                        HttpMethod.POST,
                                        entity,
                                        MessageModel.class
                                );
                                if(!responseEntity.getBody().isSuccess()) {
                                    throw new CheckHookException(1111,responseEntity.getBody().getErrorMsg());
                                }
                            }catch(CheckHookException checkHookException){
                                throw checkHookException;
                            }catch(Exception e){
                                throw e;
                            }
                        }
                    }
                }else{
                    throw new CheckHookException(1111,isValid.getErrorMsg());
                }

            }
        }
        return true;
    }

    private MessageModel getValidated(StartRequest startRequest) throws Exception {
        MessageModel message;

        message = isPcuValid(startRequest);
        if (!message.isSuccess()) {
            return message;
        }

        message = isRoutingValid(startRequest.getSite(), startRequest.getRouterBO());
        if (!message.isSuccess()) {
            return message;
        }

        message = isOperationValid(startRequest.getSite(), startRequest.getOperationBO(), "Pre_Start");
        if (!message.isSuccess()) {
            return message;
        }

        message = isResourceValid(startRequest.getSite(), startRequest.getResourceBO(), "Pre_Start");
        if (!message.isSuccess()) {
            return message;
        }

        message = isWorkCenterValid(startRequest.getSite(), startRequest.getWorkCenter(), "Pre_Start");
        if (!message.isSuccess()) {
            return message;
        }

        message = isOrderValid(startRequest.getSite(), startRequest.getShopOrderBO());
        if (!message.isSuccess()) {
            return message;
        }

        message = isItemValid(startRequest.getSite(), startRequest.getItemBO());
        if (!message.isSuccess()) {
            return message;
        }

        return new MessageModel(true, null);
    }

    public MessageModel isRoutingValid(String site, String routerBO) {
        if (routerBO != null && !routerBO.isEmpty()) {
            String[] routingBo = routerBO.split(",");
            IsExist isExist = IsExist.builder().site(site).routing(routingBo[1]).version(routingBo[2]).build();
            Routing routing = webClientBuilder.build()
                    .post()
                    .uri(retrieveRoutingUrl)
                    .bodyValue(isExist)
                    .retrieve()
                    .bodyToMono(Routing.class)
                    .block();

            if (routing == null || routing.getRouting() == null || routing.getRouting().isEmpty()) {
                return new MessageModel(false, getFormattedMessage(3228, routingBo[1], routingBo[2]));
            }
            if (!routing.getStatus().equalsIgnoreCase("Releasable")) {
                return new MessageModel(false, getFormattedMessage(3214, routingBo[1]));
            }
            return new MessageModel(true, null);
        } else {
            return new MessageModel(false, getFormattedMessage(2108, pcu));
        }
    }

    public MessageModel isItemValid(String site, String itemBO) {
        try {
            if (itemBO != null && !itemBO.isEmpty()) {
                String[] itembo = itemBO.split(",");
                IsExist isExist = IsExist.builder().site(site).item(itembo[1]).revision(itembo[2]).build();
                Item item = webClientBuilder.build()
                        .post()
                        .uri(retrieveItemUrl)
                        .bodyValue(isExist)
                        .retrieve()
                        .bodyToMono(Item.class)
                        .block();

                if (item == null || item.getItem() == null || item.getItem().isEmpty()) {
                    return new MessageModel(false, getFormattedMessage(100, itembo[1], itembo[2]));
                }
                if (!item.getStatus().equalsIgnoreCase("Releasable")) {
                    return new MessageModel(false, getFormattedMessage(3210, itembo[1]));
                } else {
                    return new MessageModel(true, null);
                }
            } else {
                return new MessageModel(false, getFormattedMessage(2113, pcu));
            }
        } catch (CheckHookException e) {
            return new MessageModel(false, e.getMessage());
        }
    }

    public MessageModel isOrderValid(String site, String shopOrderBO) {
        try {
            if (shopOrderBO != null && !shopOrderBO.isEmpty()) {
                String[] shopOrderBo = shopOrderBO.split(",");
                IsExist isExist = IsExist.builder().site(site).shopOrder(shopOrderBo[1]).build();
                ShopOrder shopOrder = webClientBuilder.build()
                        .post()
                        .uri(retrieveShopOrderUrl)
                        .bodyValue(isExist)
                        .retrieve()
                        .bodyToMono(ShopOrder.class)
                        .block();

                if (shopOrder == null || shopOrder.getShopOrder() == null || shopOrder.getShopOrder().isEmpty()) {
                    return new MessageModel(false, getFormattedMessage(3202, shopOrderBo[1]));
                }
                if (!shopOrder.getStatus().equalsIgnoreCase("Releasable")) {
                    return new MessageModel(false, getFormattedMessage(3215, shopOrderBo[1]));
                } else {
                    return new MessageModel(true, null);
                }
            } else {
                return new MessageModel(false, getFormattedMessage(2110, pcu));
            }
        } catch (CheckHookException e) {
            return new MessageModel(false, e.getMessage());
        }
    }

    public MessageModel isResourceValid(String site, String resourceBO, String hookPoint) {
        try {
            if (resourceBO != null && !resourceBO.isEmpty()) {
                String[] resourceBo = resourceBO.split(",");
                IsExist isExist = IsExist.builder().site(site).resource(resourceBo[1]).build();
                ResourceRequest resource = webClientBuilder.build()
                        .post()
                        .uri(retrieveResourceUrl)
                        .bodyValue(isExist)
                        .retrieve()
                        .bodyToMono(ResourceRequest.class)
                        .block();

                if (resource == null || resource.getResource() == null || resource.getResource().isEmpty()) {
                    return new MessageModel(false, getFormattedMessage(1112, resourceBo[1]));
                }
                if (!resource.getStatus().equalsIgnoreCase("Enabled") || !resource.getSetUpState().equalsIgnoreCase("Productive")) {
                    return new MessageModel(false, getFormattedMessage(2112, resourceBo[1]));
                }
                if (resource.getActivityHookList() != null && !resource.getActivityHookList().isEmpty()) {
                    activityHookList.addAll(resource.getActivityHookList().stream().filter(hook -> hook.isEnable() && hook.getHookPoint().equals(hookPoint))
                            .collect(Collectors.toList()));
                    return new MessageModel(true, null);
                } else {
                    return new MessageModel(true, null);
                }
            } else {
                return new MessageModel(false, getFormattedMessage(2107, pcu));
            }
        } catch (CheckHookException e) {
            return new MessageModel(false, e.getMessage());
        }
    }

    public MessageModel isPcuValid(StartRequest startRequest) {
        try {
            if (startRequest.getPcuBO() != null && !startRequest.getPcuBO().isEmpty()) {
                if (isInPcuInQueue(startRequest)) {
                    return new MessageModel(true, null);
                } else {
                    return new MessageModel(false, getFormattedMessage(2904, "Error message"));
                }
            } else {
                return new MessageModel(false, getFormattedMessage(2105, "Error message"));
            }
        } catch (Exception e) {
            return new MessageModel(false, e.getMessage());
        }
    }

    public MessageModel isOperationValid(String site, String operationBO, String hookPoint) {
        if (operationBO != null && !operationBO.isEmpty()) {
            String[] operationBo = operationBO.split(",");
            IsExist isExist = IsExist.builder().site(site).operation(operationBo[1]).revision(operationBo[2]).build();
            Operation operation = webClientBuilder.build()
                    .post()
                    .uri(retrieveOperationUrl)
                    .bodyValue(isExist)
                    .retrieve()
                    .bodyToMono(Operation.class)
                    .block();

            if (operation == null || operation.getOperation() == null) {
                return new MessageModel(false, getFormattedMessage(3506, operationBo[1]));
            }
            if (!operation.getStatus().equalsIgnoreCase("Releasable")) {
                return new MessageModel(false, getFormattedMessage(2111, operationBo[1]));
            }
            if (operation.getActivityHookList() != null && !operation.getActivityHookList().isEmpty()) {
                activityHookList.addAll(operation.getActivityHookList().stream().filter(hook -> hook.isEnable() && hook.getHookPoint().equals(hookPoint))
                        .collect(Collectors.toList()));
                return new MessageModel(true, null);
            } else {
                return new MessageModel(true, null);
            }
        } else {
            return new MessageModel(false, getFormattedMessage(2106, pcu));
        }
    }

    private boolean isInPcuInQueue(StartRequest startRequest) throws Exception {
        PcuInWorkReq pcu1 = PcuInWorkReq.builder()
                .site(startRequest.getSite())
                .item("").itemVersion("")
                .resource("")
//                .handle(signOffRequest.getHandle())
//                .dateTime(signOffRequest.getDateTime())
//                .router(BOConverter.getRouter(signOffRequest.getRouterBO()))
//                .routerVersion(BOConverter.getRouterVersion(signOffRequest.getRouterBO()))
                .operation(BOConverter.getOperation(startRequest.getOperationBO()))
                .operationVersion(BOConverter.getOperationVersion(startRequest.getOperationBO()))
                .pcu(BOConverter.getPcu(startRequest.getPcuBO()))
//                .workCenter(BOConverter.getWorkCenter(signOffRequest.getWorkCenter()))
//                .stepID(signOffRequest.getStepID())
//                .user(BOConverter.getUser(signOffRequest.getUserBO()))
//                .qtyToComplete(signOffRequest.getQtyToComplete())
//                .quantity(signOffRequest.getQuantity())
//                .qtyInWork(signOffRequest.getQtyInQueue())
//                .shopOrder(BOConverter.getShopOrder(signOffRequest.getShopOrderBO()))
//                .childRouter(BOConverter.getChildRouter(signOffRequest.getChildRouterBO()))
//                .childRouterVersion(BOConverter.getChildRouterVersion(signOffRequest.getChildRouterBO()))
//                .parentStepID(signOffRequest.getParentStepID())
//                .status(signOffRequest.getStatus())
//                .createdDateTime(signOffRequest.getCreatedDateTime())
//                .modifiedDateTime(startRequest.getModifiedDateTime())
//                .disable(startRequest.getDisable())
                .build();
//        PcuInQueueReq temp=(PcuInQueueReq) startRequest.clone();
//        temp.setItem("");
//        temp.setResource("");

        PcuInQueueReq pcuInQueue=webClientBuilder.build()
                .post()
                .uri(retrieveInQueueUrl)
                .bodyValue(pcu1)
                .retrieve()
                .bodyToMono(PcuInQueueReq.class)
                .block();

        if (pcuInQueue == null || pcuInQueue.getPcu()==null) {
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    public boolean signOffCheckHook(SignOffRequestList pcuRequestList) throws Exception {
        if(!activityHookList.isEmpty()){
            activityHookList.removeAll(activityHookList);
        }
        if(pcuRequestList!=null && pcuRequestList.getRequestList()!=null && !pcuRequestList.getRequestList().isEmpty()) {

            for (SignOffRequest signOffRequest : pcuRequestList.getRequestList()) {
                String[] pcubo = signOffRequest.getPcuBO().split(",");
                pcu = pcubo[1];
                MessageModel isValid=getValidatedSignOff(signOffRequest);
                if(isValid.isSuccess() ){
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "Bearer " + pcuRequestList.getAccessToken());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    String body=objectMapper.writeValueAsString(signOffRequest);
                    HttpEntity<String> entity = new HttpEntity<>(body, headers);


                    if(activityHookList!=null && !activityHookList.isEmpty()){
                        Set<String> uniqueUrls=new HashSet<>();
                        List<ActivityHook> updatedActivityUrl =getUpdatedActivityUrl(activityHookList);
                        for(ActivityHook activityHook: updatedActivityUrl){
                            String url=null;
                            if(!activityHook.getUrl().startsWith("http://")) {
                                url = "http://" + appHostIp + ":" + appHostPort + "/" + activityHook.getUrl();
                            }else {
                                url = activityHook.getUrl();
                            }                            if (!uniqueUrls.add(url)) {
                                continue;
                            }

                            try {
                                ResponseEntity<MessageModel> responseEntity = restTemplate.exchange(
                                        url,
                                        HttpMethod.POST,
                                        entity,
                                        MessageModel.class
                                );
                                if(!responseEntity.getBody().isSuccess()) {
                                    throw new CheckHookException(1111,responseEntity.getBody().getErrorMsg());
                                }
                            }catch(CheckHookException checkHookException){
                                throw checkHookException;
                            }catch(Exception e){
                                throw e;
                            }
                        }
                    }
                }else{
                    throw new CheckHookException(1111,isValid.getErrorMsg());
                }
            }
        }
        return false;
    }

    @Override
    public RequestList completeCheckHook(RequestList pcuRequestList) throws CloneNotSupportedException, JsonProcessingException {
        if(!activityHookList.isEmpty()){
            activityHookList.removeAll(activityHookList);
        }
        String router = "";
        String operation = "";
        String resource = "";
        String workcenter = "";
        String shopOrder = "";
        String item = "";
        if(pcuRequestList!=null && pcuRequestList.getRequestList()!=null && !pcuRequestList.getRequestList().isEmpty()){
            for(PcuCompleteReq pcuCompleteReq :pcuRequestList.getRequestList()) {
                if (checkAllFieldsAreNotEmpty(pcuCompleteReq).isSuccess()) {
//                    String[] pcuBO = pcuCompleteReqWithBO.getPcuBO().split(",");
                    pcu = pcuCompleteReq.getPcu();
                    if (isPcuStarted(pcuCompleteReq)) {
                        router = BOConverter.retrieveRouterBO(pcuCompleteReq.getSite(), pcuCompleteReq.getRouter(), pcuCompleteReq.getRouterVersion());

                        if(isRoutingValid(pcuCompleteReq.getSite(), router).isSuccess())
                            operation = BOConverter.retrieveOperationBO(pcuCompleteReq.getSite(), pcuCompleteReq.getOperation(), pcuCompleteReq.getOperationVersion());

                        if(isOperationValid(pcuCompleteReq.getSite(), operation,"Pre_Complete").isSuccess()){
                            resource = BOConverter.retriveResourceBO(pcuCompleteReq.getSite(), pcuCompleteReq.getResource());

                            if(isResourceValid(pcuCompleteReq.getSite(), resource,"Pre_Complete").isSuccess()){
                                if(pcuCompleteReq.getWorkCenter() != null)
                                    workcenter = BOConverter.retrieveWorkCenterBO(pcuCompleteReq.getSite(), pcuCompleteReq.getWorkCenter());

                                if(isWorkCenterValid(pcuCompleteReq.getSite(), workcenter,"Pre_Complete").isSuccess()) {
                                    shopOrder = BOConverter.retrieveWorkCenterBO(pcuCompleteReq.getSite(), pcuCompleteReq.getShopOrder());

                                    if (isOrderValid(pcuCompleteReq.getSite(), shopOrder).isSuccess()) {
                                        item = BOConverter.retrieveItemBO(pcuCompleteReq.getSite(), pcuCompleteReq.getItem(), pcuCompleteReq.getItemVersion());

                                        if (isItemValid(pcuCompleteReq.getSite(), item).isSuccess()) {
                                            HttpHeaders headers = new HttpHeaders();
                                            headers.set("Authorization", "Bearer " + pcuRequestList.getAccessToken());
                                            headers.setContentType(MediaType.APPLICATION_JSON);
                                            String body=objectMapper.writeValueAsString(pcuCompleteReq);
                                            HttpEntity<String> entity = new HttpEntity<>(body, headers);


                                            if(activityHookList!=null && !activityHookList.isEmpty()){
                                                Set<String> uniqueUrls = new HashSet<>();
                                                List<ActivityHook> updatedActivityUrl =getUpdatedActivityUrl(activityHookList);
                                                for(ActivityHook activityHook: updatedActivityUrl){
                                                    String url=null;
                                                    if(!activityHook.getUrl().startsWith("http://")) {
                                                        url = "http://" + appHostIp + ":" + appHostPort + "/" + activityHook.getUrl();
                                                    }else {
                                                        url = activityHook.getUrl();
                                                    }
                                                    if (!uniqueUrls.add(url)) {
                                                        continue;
                                                    }

                                                    try {
                                                        ResponseEntity<MessageModel> responseEntity = restTemplate.exchange(
                                                                url,
                                                                HttpMethod.POST,
                                                                entity,
                                                                MessageModel.class
                                                        );
                                                        if(!responseEntity.getBody().isSuccess()) {
                                                            throw new CheckHookException(1111,responseEntity.getBody().getErrorMsg());
                                                        }
                                                    }catch(CheckHookException checkHookException){
                                                        throw checkHookException;
                                                    }catch(Exception e){
                                                        throw e;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private List<ActivityHook> getUpdatedActivityUrl(List<ActivityHook> activityHookList) {
        if(activityHookList!=null && !activityHookList.isEmpty()) {
            for (ActivityHook activityHook : activityHookList) {
                String activityID = activityHook.getActivity();
                IsExist isExist= IsExist.builder().activityId(activityID).build();
                String url= webClientBuilder.build()
                        .post()
                        .uri(getActivityUrl)
                        .bodyValue(isExist)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                if(url!= null && !url.isEmpty()){
                    activityHook.setUrl(url);
                }else{
                    throw new CheckHookException(3,activityID);
                }

            }
        }
        return activityHookList;
    }

    public MessageModel isWorkCenterValid(String site, String workCenter, String hookPoint) {
        try {
            if (workCenter != null && !workCenter.isEmpty()) {
                IsExist isExist = IsExist.builder().site(site).workCenter(workCenter).build();
                WorkCenter workCenterResponse = webClientBuilder.build()
                        .post()
                        .uri(retrieveResourceUrl)
                        .bodyValue(isExist)
                        .retrieve()
                        .bodyToMono(WorkCenter.class)
                        .block();

                if (workCenterResponse == null || workCenterResponse.getWorkCenter() == null || workCenterResponse.getWorkCenter().isEmpty()) {
                    return new MessageModel(false, getFormattedMessage(602, workCenter));
                }
                if (!workCenterResponse.getStatus().equalsIgnoreCase("Available")) {
                    return new MessageModel(false, getFormattedMessage(2114, workCenter));
                } else {
                    if (workCenterResponse.getActivityHookList() != null && !workCenterResponse.getActivityHookList().isEmpty()) {
                        activityHookList.addAll(workCenterResponse.getActivityHookList().stream().filter(hook -> hook.isEnable() && hook.getHookPoint().equals(hookPoint))
                                .collect(Collectors.toList()));

                        return new MessageModel(true, null);
                    }
                    return new MessageModel(true, null);
                }
            } else {
                // for now true

                //        else{
//            throw new CheckHookException(2107,pcu);
//        }
                return new MessageModel(true, null);
            }
        } catch (CheckHookException e) {
            return new MessageModel(false, e.getMessage());
        }
    }


    private MessageModel getValidatedSignOff(SignOffRequest signOffRequest) throws CloneNotSupportedException {
        if (isPcuValidForSignOff(signOffRequest)) {
            MessageModel routingMessage = isRoutingValid(signOffRequest.getSite(), signOffRequest.getRouterBO());
            if (!routingMessage.isSuccess()) {
                return routingMessage;
            }

            MessageModel operationMessage = isOperationValid(signOffRequest.getSite(), signOffRequest.getOperationBO(), "Pre_SignOff");
            if (!operationMessage.isSuccess()) {
                return operationMessage;
            }

            MessageModel resourceMessage = isResourceValid(signOffRequest.getSite(), signOffRequest.getResourceBO(), "Pre_SignOff");
            if (!resourceMessage.isSuccess()) {
                return resourceMessage;
            }

            MessageModel workCenterMessage = isWorkCenterValid(signOffRequest.getSite(), signOffRequest.getWorkCenter(), "Pre_SignOff");
            if (!workCenterMessage.isSuccess()) {
                return workCenterMessage;
            }

            MessageModel orderMessage = isOrderValid(signOffRequest.getSite(), signOffRequest.getShopOrderBO());
            if (!orderMessage.isSuccess()) {
                return orderMessage;
            }

            MessageModel itemMessage = isItemValid(signOffRequest.getSite(), signOffRequest.getItemBO());
            if (!itemMessage.isSuccess()) {
                return itemMessage;
            }

            return new MessageModel(true, null);
        } else {
            return new MessageModel(false, "Pcu not started yet!");
        }
    }


    private boolean isPcuValidForSignOff(SignOffRequest signOffRequest) throws CloneNotSupportedException {
        PcuInWorkReq pcu1 = PcuInWorkReq.builder()
                .site(signOffRequest.getSite())
                .item("").itemVersion("")
                .handle(signOffRequest.getHandle())
                .dateTime(signOffRequest.getDateTime())
                .router(BOConverter.getRouter(signOffRequest.getRouterBO()))
                .routerVersion(BOConverter.getRouterVersion(signOffRequest.getRouterBO()))
                .operation(BOConverter.getOperation(signOffRequest.getOperationBO()))
                .operationVersion(BOConverter.getOperationVersion(signOffRequest.getOperationBO()))
                .pcu(BOConverter.getPcu(signOffRequest.getPcuBO()))
                .workCenter(BOConverter.getWorkCenter(signOffRequest.getWorkCenter()))
                .resource(BOConverter.getResource(signOffRequest.getResourceBO()))
                .stepID(signOffRequest.getStepID())
                .user(BOConverter.getUser(signOffRequest.getUserBO()))
                .qtyToComplete(signOffRequest.getQtyToComplete())
                .quantity(signOffRequest.getQuantity())
                .qtyInQueue(signOffRequest.getQtyInQueue())
                .shopOrder(BOConverter.getShopOrder(signOffRequest.getShopOrderBO()))
                .childRouter(BOConverter.getChildRouter(signOffRequest.getChildRouterBO()))
                .childRouterVersion(BOConverter.getChildRouterVersion(signOffRequest.getChildRouterBO()))
                .parentStepID(signOffRequest.getParentStepID())
                .status(signOffRequest.getStatus())
                .createdDateTime(signOffRequest.getCreatedDateTime())
                .build();
//        SignOffRequest temp = (SignOffRequest) signOffRequest.clone();
//        temp.setItemBO("");

        PcuInWorkReq pcuInWork=webClientBuilder.build()
                .post()
                .uri(retrieveInWorkUrl)
                .bodyValue(pcu1)
                .retrieve()
                .bodyToMono(PcuInWorkReq.class)
                .block();

        if (pcuInWork == null || pcuInWork.getPcu()==null) {
            return false;
        }
        else{
            return true;
        }
    }


    public MessageModel checkAllFieldsAreNotEmpty(PcuCompleteReq completeReq) {
        if (completeReq.getPcu() == null || completeReq.getPcu().isEmpty()) {
            return new MessageModel(false,getFormattedMessage(2105));
        }
        if (completeReq.getOperation() == null || completeReq.getOperation().isEmpty()) {
            return new MessageModel(false, getFormattedMessage(2106, completeReq.getPcu()));
        }
        if (completeReq.getResource() == null || completeReq.getResource().isEmpty()) {
            return new MessageModel(false, getFormattedMessage(2107, completeReq.getPcu()));
        }
        if (completeReq.getRouter() == null || completeReq.getRouter().isEmpty()) {
            return new MessageModel(false, getFormattedMessage(2108, completeReq.getPcu()));
        }
        if (completeReq.getUser() == null || completeReq.getUser().isEmpty()) {
            return new MessageModel(false, getFormattedMessage(2109, completeReq.getPcu()));
        }
        if (completeReq.getShopOrder() == null || completeReq.getShopOrder().isEmpty()) {
            return new MessageModel(false, getFormattedMessage(2110, completeReq.getPcu()));
        }
        return new MessageModel(true, null); // All fields are not empty

    }

    public MessageModel isAllNcClosed(PcuCompleteReq pcuCompleteReq) {
        String pcu = BOConverter.retrievePcuBO(pcuCompleteReq.getSite(), pcuCompleteReq.getPcu());
        List<NcData> ncDataList = nonConformanceservice.getAllNcByPCU(pcu);
        boolean hasNcOpen = ncDataList.stream().anyMatch(ncData -> ncData.getNcState().equalsIgnoreCase("o"));
        if (hasNcOpen) {
            return new MessageModel(false, getFormattedMessage(2104, pcu));
        } else {
            return new MessageModel(true, null);
        }

    }

    public MessageModel isAllComponentAssembled(PcuCompleteReq pcuCompleteReq) {
        boolean flag = assemblyService.isAllQuantityAssembled(pcuCompleteReq);
        if (flag) {
            return new MessageModel(true, null);
        } else {
            return new MessageModel(false, getFormattedMessage(2101, pcu));
        }
    }

    private boolean isPcuStarted(PcuCompleteReq completeReq) throws CloneNotSupportedException {
        PcuCompleteReq temp = (PcuCompleteReq) completeReq.clone();
        temp.setItem("");

        PcuInWorkReq pcuInWork=webClientBuilder.build()
                .post()
                .uri(retrieveInWorkUrl)
                .bodyValue(temp)
                .retrieve()
                .bodyToMono(PcuInWorkReq.class)
                .block();

        if (pcuInWork == null || pcuInWork.getPcu()==null) {
            return false;
        }
        else{
            return true;
        }
    }

    private boolean getBuyOff(Attachment attachment) {
        List<BuyoffLog> list = null;
        try {
            list= webClientBuilder.build()
                    .post()
                    .uri(getBuyOffListUrl)
                    .bodyValue(attachment)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<BuyoffLog>>() {
                    })
                    .block();
        }catch(LogBuyOffException e){
            if( e.getCode() == 2003){
                return true;
            }

        }catch(Exception e){
            if( e.getMessage() .contains("No buyOffs found for the selected PCU ")){
                return true;
            }
        }
        if(list!=null && !list.isEmpty()){
            boolean open= list.stream().map(BuyoffLog::getState).anyMatch(state -> state.equalsIgnoreCase("open"));
            if(open){
                return false;
            }
        }
        return true;
    }

    private DataCollectionList getDataCollectionList(Attachment attachment) {
        List<Attachment> attachmentList= new ArrayList<>();
        attachmentList.add(attachment);
        DataCollectionRequest dataCollectionRequest= DataCollectionRequest.builder().site(site).pcu(attachment.getPcu()).resource(attachment.getResource()).operation(attachment.getOperation()).attachmentList(attachmentList).build();
        DataCollectionList list= webClientBuilder.build()
                .post()
                .uri(getDataCollectionUrl)
                .bodyValue(dataCollectionRequest)
                .retrieve()
                .bodyToMono(DataCollectionList.class)
                .block();
        return list;
    }

    private Attachment getAttachmentObject(PcuCompleteReq completeReq) {
//        String[] itemBO = completeReq.getItemBO().split(",");
//        String[] operationBO = completeReq.getOperationBO().split(",");
//        String[] resourceBO= completeReq.getResourceBO().split(",");
//        String[] routingBO= completeReq.getRouterBO().split(",");
//        String[] shopOrderBO= completeReq.getShopOrderBO().split(",");
//        String[] pcu= completeReq.getPcuBO().split(",");

        Attachment attachment = Attachment.builder().pcu(completeReq.getPcu())
                .site(completeReq.getSite())
                .item(completeReq.getItem())
                .itemVersion(completeReq.getItemVersion())
                .routing(completeReq.getRouter())
                .routingVersion(completeReq.getRouterVersion())
                .operation(completeReq.getOperation())
                .operationVersion(completeReq.getOperationVersion())
                .resource(completeReq.getResource())
                .shopOrder(completeReq.getShopOrder())
                .workCenter(completeReq.getWorkCenter())
                .build();
        return attachment;
    }

    public String getAccessToken() {
        // Create the RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Make the API call to retrieve the token
        String url = "http://"+appHostIp+":"+uiHostPort+"/getToken"; // Update with your server URL
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class
        );

        // Get the response body
       String responseBody = responseEntity.getBody();

        // Extract the access token from the response
        if (responseBody != null && !responseBody.isEmpty()) {
            return responseBody;
        } else {
            throw new RuntimeException("Access token not found in response");
        }
    }


    public MessageModel userCertificationHook(String user, String operation, String site){
        boolean certificationListsMatch = false;
        boolean allMatch = true;
        // retrive certification using user
        UserCertificateAssignment userCertificateAssignment = new UserCertificateAssignment();
        userCertificateAssignment.setUser(user);
        userCertificateAssignment.setSite(site);
        UserCertificateAssignment userCertificateList = webClientBuilder.build()
                .post()
                .uri(retriveUsercertificateassignmentUrl)
                .bodyValue(userCertificateAssignment)
                .retrieve()
                .bodyToMono(UserCertificateAssignment.class)
                .block();

        // operation retrive operation list
        Operation op = new Operation();
        op.setOperation(operation);
        op.setSite(site);
        List<Operation> certificateList = webClientBuilder.build()
                .post()
                .uri(retriveCertificateListOperationUrl)
                .bodyValue(op)
                .retrieve()
                .bodyToFlux(Operation.class)
                .collectList()
                .block();

//        if(userCertificateList.getCertificationDetailsList().contains(certificateList.get(0).getCertificationList())){
        if (userCertificateList != null && userCertificateList.getCertificationDetailsList() != null
                && !userCertificateList.getCertificationDetailsList().isEmpty()
                && certificateList != null && !certificateList.isEmpty()) {

            // Extract certification lists
            List<CertificationDetails> userCertifications = userCertificateList.getCertificationDetailsList();
            List<Certification> operationCertifications = certificateList.get(0).getCertificationList();

            for (CertificationDetails userCertification : userCertifications) {
                boolean certificationMatch = false;
                for (Certification operationCertification : operationCertifications) {

                    if (userCertification.getCertification().equals(operationCertification.getCertification())) {
                        certificationMatch = true;
                        break;
                    }
                }

                if (!certificationMatch) {
                    allMatch = false;
                    break;
                }
            }

        }
        if(allMatch)
            return new MessageModel(true, null);
        else
            return new MessageModel(false, getFormattedMessage(3235, pcu));

    }

}
