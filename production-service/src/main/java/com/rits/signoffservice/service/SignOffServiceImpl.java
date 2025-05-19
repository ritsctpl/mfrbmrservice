package com.rits.signoffservice.service;

import com.rits.Utility.BOConverter;
import com.rits.pcucompleteservice.dto.PcuCompleteReq;
import com.rits.pcuinqueueservice.dto.PcuInQueueReq;
import com.rits.pcuinqueueservice.exception.PcuInQueueException;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import com.rits.productionlogservice.model.ProductionLog;
import com.rits.productionlogservice.model.ProductionLogMongo;
import com.rits.productionlogservice.producer.ProductionLogProducer;
import com.rits.signoffservice.dto.*;
import com.rits.signoffservice.exception.SignOffException;
import com.rits.signoffservice.model.MessageDetails;
import com.rits.signoffservice.model.MessageModel;
import com.rits.signoffservice.model.PcuInWorkMessageModel;
import com.rits.signoffservice.repository.SignOffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SignOffServiceImpl implements SignOffService {
    private final SignOffRepository signOffRepository;

    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${resource-service.url}/isExistByHandle")
    private String resourceUrl;

    @Value("${operation-service.url}/isExistByHandle")
    private String operationUrl;

//    @Value("${workcenter-service.url}/isExist")
//    private String workCenterUrl;

    @Value("${pcuinqueue-service.url}/create")
    private String pcuInQueueCreateUrl;

    @Value("${start-service.url}/retrieve")
    private String pcuInWorkRetrieveUrl;

    @Value("${start-service.url}/delete")
    private String pcuInWorkDeleteUrl;

    @Value("${start-service.url}/create")
    private String pcuInWorkCreateUrl;

    @Value("${pcuinqueue-service.url}/retrieve")
    private String pcuInQueueRetrieveUrl;

    @Value("${productionlog-service.url}/producer")
    private String productionLogUrl;
//    @Value("${shift-service.url}/getBreakHours")
//    private String getShiftBreakHoursUrl;

    @Value("${productionlog-service.url}/retrieveByPcuOperationShopOrderAndEventType")
    private String retrieveProductionLogUrl;

    @Value("${operation-service.url}/retrieveOperationByCurrentVersion")
    private String retrieveOperationByCurrentVersionUrl;

    @Override
    public SignOffRequestList convertToSignOffRequestList(SignOffRequestListDetails signOffRequestListNoBO) {
        List<SignOffRequest> requests = signOffRequestListNoBO.getRequestList().stream()
            .map(requestNoBO -> {
                SignOffRequest request = new SignOffRequest();

                request.setSite(requestNoBO.getSite());
                request.setHandle(requestNoBO.getHandle());
                request.setDateTime(requestNoBO.getDateTime());

                if (requestNoBO.getPcu() != null) {
                    request.setPcuBO(BOConverter.retrievePcuBO(requestNoBO.getSite(),requestNoBO.getPcu()));
                }
                if (requestNoBO.getItem() != null) {
                    request.setItemBO(BOConverter.retrieveItemBO(requestNoBO.getSite(),requestNoBO.getItem(),requestNoBO.getItemVersion()));
                }

                if (requestNoBO.getRouter() != null) {
                    request.setRouterBO(BOConverter.retrieveRouterBO(requestNoBO.getSite(),requestNoBO.getRouter(),requestNoBO.getRouterVersion()));
                }

                if (requestNoBO.getResource() != null) {
                    request.setResourceBO(BOConverter.retriveResourceBO(requestNoBO.getSite(),requestNoBO.getResource()));
                }

                if (requestNoBO.getOperation() != null) {
                    request.setOperationBO(BOConverter.retrieveOperationBO(requestNoBO.getSite(),requestNoBO.getOperation(),requestNoBO.getOperationVersion()));
                }

                if (requestNoBO.getStepID() != null) {
                    request.setStepID(requestNoBO.getStepID());
                }

                if (requestNoBO.getUser() != null) {
                    request.setUserBO(BOConverter.retrieveUserBO(requestNoBO.getSite(),requestNoBO.getUser()));
                }

                if (requestNoBO.getQuantity() != null) {
                    request.setQuantity(requestNoBO.getQuantity());
                }

                if (requestNoBO.getQtyToComplete() != null) {
                    request.setQtyToComplete(requestNoBO.getQtyToComplete());
                }

                if (requestNoBO.getWorkCenter() != null) {
                    request.setWorkCenter(requestNoBO.getWorkCenter());
                }

                if (requestNoBO.getQtyInQueue() != null) {
                    request.setQtyInQueue(requestNoBO.getQtyInQueue());
                }

                if (requestNoBO.getShopOrder() != null) {
                    request.setShopOrderBO(BOConverter.retrieveShopOrderBO(requestNoBO.getSite(),requestNoBO.getShopOrder()));
                }

                if (requestNoBO.getChildRouter() != null) {
                    request.setChildRouterBO(BOConverter.retrieveChildRouterBO(requestNoBO.getSite(),requestNoBO.getChildRouter(),requestNoBO.getChildRouterVersion()));
                }

                if (requestNoBO.getParentStepID() != null) {
                    request.setParentStepID(requestNoBO.getParentStepID());
                }

                if (requestNoBO.getStatus() != null) {
                    request.setStatus(requestNoBO.getStatus());
                }

                if (requestNoBO.getCreatedDateTime() != null) {
                    request.setCreatedDateTime(requestNoBO.getCreatedDateTime());
                }

                return request;
            })
            .collect(Collectors.toList());

    return SignOffRequestList.builder()
            .requestList(requests)
            .accessToken(signOffRequestListNoBO.getAccessToken())
            .build();
    }
    public SignOffRequestListDetails convertToSignOffRequestListNoBO(SignOffRequestList signOffRequestList) {
        List<SignOffRequestDetails> requestNoBOs = signOffRequestList.getRequestList().stream()
                .map(request -> {
                    SignOffRequestDetails requestNoBO = new SignOffRequestDetails();

                    if (request.getSite() != null && !request.getSite().isEmpty()) {
                        requestNoBO.setSite(request.getSite());
                    }

                    if (request.getHandle() != null && !request.getHandle().isEmpty()) {
                        requestNoBO.setHandle(request.getHandle());
                    }

                    if (request.getDateTime() != null) {
                        requestNoBO.setDateTime(request.getDateTime());
                    }

                    if (request.getPcuBO() != null && !request.getPcuBO().isEmpty()) {
                        requestNoBO.setPcu(BOConverter.getPcu(request.getPcuBO()));
                    }

                    if (request.getItemBO() != null && !request.getItemBO().isEmpty()) {
                        requestNoBO.setItem(BOConverter.getItem(request.getItemBO()));
                        requestNoBO.setItemVersion(BOConverter.getItemVersion(request.getItemBO()));
                    }

                    if (request.getRouterBO() != null && !request.getRouterBO().isEmpty()) {
                        requestNoBO.setRouter(BOConverter.getRouter(request.getRouterBO()));
                        requestNoBO.setRouterVersion(BOConverter.getRouterVersion(request.getRouterBO()));
                    }

                    if (request.getResourceBO() != null && !request.getResourceBO().isEmpty()) {
                        requestNoBO.setResource(BOConverter.getResource(request.getResourceBO()));
                    }

                    if (request.getOperationBO() != null && !request.getOperationBO().isEmpty()) {
                        requestNoBO.setOperation(BOConverter.getOperation(request.getOperationBO()));
                        requestNoBO.setOperationVersion(BOConverter.getOperationVersion(request.getOperationBO()));
                    }

                    if (request.getUserBO() != null && !request.getUserBO().isEmpty()) {
                        requestNoBO.setUser(BOConverter.getUser(request.getUserBO()));
                    }

                    if (request.getShopOrderBO() != null && !request.getShopOrderBO().isEmpty()) {
                        requestNoBO.setShopOrder(BOConverter.getShopOrder(request.getShopOrderBO()));
                    }

                    if (request.getChildRouterBO() != null && !request.getChildRouterBO().isEmpty()) {
                        requestNoBO.setChildRouter(BOConverter.getChildRouter(request.getChildRouterBO()));
                        requestNoBO.setChildRouterVersion(BOConverter.getChildRouterVersion(request.getChildRouterBO()));
                    }

                    if (request.getStepID() != null && !request.getStepID().isEmpty()) {
                        requestNoBO.setStepID(request.getStepID());
                    }

                    if (request.getQuantity() != null) {
                        requestNoBO.setQuantity(request.getQuantity());
                    }

                    if (request.getQtyToComplete() != null && !request.getQtyToComplete().isEmpty()) {
                        requestNoBO.setQtyToComplete(request.getQtyToComplete());
                    }

                    if (request.getWorkCenter() != null && !request.getWorkCenter().isEmpty()) {
                        requestNoBO.setWorkCenter(request.getWorkCenter());
                    }

                    if (request.getQtyInQueue() != null && !request.getQtyInQueue().isEmpty()) {
                        requestNoBO.setQtyInQueue(request.getQtyInQueue());
                    }

                    if (request.getParentStepID() != null && !request.getParentStepID().isEmpty()) {
                        requestNoBO.setParentStepID(request.getParentStepID());
                    }

                    if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                        requestNoBO.setStatus(request.getStatus());
                    }

                    if (request.getCreatedDateTime() != null) {
                        requestNoBO.setCreatedDateTime(request.getCreatedDateTime());
                    }

                    return requestNoBO;
                })
                .collect(Collectors.toList());

        return SignOffRequestListDetails.builder()
                .requestList(requestNoBOs)
                .accessToken(signOffRequestList.getAccessToken())
                .build();
    }

    @Override
    public MessageModel signOff(SignOffRequestList signOffRequestList) throws Exception {
        List<SignOffRequest> signOffRequests = signOffRequestList.getRequestList();
        String site = signOffRequests.get(0).getSite();
        String op = BOConverter.getOperation(signOffRequests.get(0).getOperationBO());
        String opVersion = getOperationCurrentVer(op, site);

        String OperationBO = BOConverter.retrieveOperationBO(site, op, opVersion);
        List<MessageDetails> messageDetailsList = new ArrayList<>();
        List<String> errorPcus = new ArrayList<>();
        List<String> successPcus = new ArrayList<>();
        MessageDetails messageDetails=new MessageDetails();
        String errorMessage = null;
        String successMessage = null;


        for (SignOffRequest signOffRequest : signOffRequests) {
            ResourceRequest resourceRequest = new ResourceRequest(signOffRequest.getSite(), signOffRequest.getResourceBO());
            Boolean isResourceExist = webClientBuilder.build()
                    .post()
                    .uri(resourceUrl)
                    .bodyValue(resourceRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();


            Operation operationRequest = new Operation(OperationBO, signOffRequest.getSite());
            Boolean isOperationExist = webClientBuilder.build()
                    .post()
                    .uri(operationUrl)
                    .bodyValue(operationRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
//
//            RetrieveRequest workCenterRequest = new RetrieveRequest(signOffRequest.getSite(), signOffRequest.getWorkCenter());
//            Boolean isWorkCenterExist = webClientBuilder.build()
//                    .post()
//                    .uri(workCenterUrl)
//                    .bodyValue(workCenterRequest)
//                    .retrieve()
//                    .bodyToMono(Boolean.class)
//                    .block();


            PcuInWorkRequest retrievePcuInWorkRequest = PcuInWorkRequest.builder().site(signOffRequest.getSite()).pcu(BOConverter.getPcu(signOffRequest.getPcuBO())).operation(op).operationVersion(opVersion).build();
            PcuInWork retrievePcuInWork = webClientBuilder.build()
                    .post()
                    .uri(pcuInWorkRetrieveUrl)
                    .bodyValue(retrievePcuInWorkRequest)
                    .retrieve()
                    .bodyToMono(PcuInWork.class)
                    .block();

            if(/*signOffRequest.getStatus().equalsIgnoreCase("in queue") ||*/ retrievePcuInWork == null || retrievePcuInWork.getPcu() == null || retrievePcuInWork.getPcu().isEmpty())
            {
                errorPcus.add(signOffRequest.getPcuBO());
                continue;
            }
            SignOffRequestListDetails signOffRequestListNoBO = convertToSignOffRequestListNoBO(signOffRequestList);
            SignOffRequestDetails firstRequestNoBO = signOffRequestListNoBO.getRequestList().get(0);
            PcuInQueueReq retrievePcuInQueueRequest = PcuInQueueReq.builder()
                    .pcu(firstRequestNoBO.getPcu())
                    .site(firstRequestNoBO.getSite())
                    .operation(firstRequestNoBO.getOperation())
                    .build();
            //PcuInQueueRequest retrievePcuInQueueRequest = PcuInQueueRequest.builder().pcuBO(signOffRequest.getPcuBO()).site(signOffRequest.getSite()).operationBO(signOffRequest.getOperationBO()).build();
            PcuInQueue retrievePcuInQueue = webClientBuilder.build()
                    .post()
                    .uri(pcuInQueueRetrieveUrl)
                    .bodyValue(retrievePcuInQueueRequest)
                    .retrieve()
                    .bodyToMono(PcuInQueue.class)
                    .block();
            String qtyInqueVal;

            if (retrievePcuInWork.getQtyInWork() != null && retrievePcuInQueue.getQtyInQueue() != null){
//                qtyInqueVal = String.valueOf(Integer.parseInt(retrievePcuInWork.getQtyInWork()) + Integer.parseInt(retrievePcuInQueue.getQtyInQueue()));
                qtyInqueVal = String.valueOf(Double.parseDouble(retrievePcuInWork.getQtyInWork()) + Double.parseDouble(retrievePcuInQueue.getQtyInQueue()));
            } else{
                qtyInqueVal=retrievePcuInWork.getQtyInWork();
            }

            if(signOffRequest.getQuantity() == null)
                signOffRequest.setQuantity("0");
            if (isOperationExist && isResourceExist /*&& isWorkCenterExist */) {
//                if (Integer.parseInt(signOffRequest.getQuantity()) > Integer.parseInt(retrievePcuInWork.getQtyInWork())) {
                if (signOffRequest.getQuantity() != null && Double.parseDouble(signOffRequest.getQuantity()) > Double.parseDouble(retrievePcuInWork.getQtyInWork())) {
                    throw new SignOffException(9900, signOffRequest.getQuantity());
//                } else if ((Integer.parseInt(signOffRequest.getQuantity()) == 0 && Integer.parseInt(retrievePcuInWork.getQtyInWork()) > 0) || signOffRequest.getQuantity().equals(retrievePcuInWork.getQtyInWork())) {
                } else if ((Double.parseDouble(signOffRequest.getQuantity()) == 0 && Double.parseDouble(retrievePcuInWork.getQtyInWork()) > 0) || signOffRequest.getQuantity().equals(retrievePcuInWork.getQtyInWork())) {
                    PcuInQueueRequest CreatePcuInQueueRequest = PcuInQueueRequest.builder()
                            .site(signOffRequest.getSite())
                            .handle(retrievePcuInQueue.getHandle())
                            .pcu(BOConverter.getPcu(signOffRequest.getPcuBO()))
                            .item(BOConverter.getItem(signOffRequest.getItemBO()))
                            .itemVersion(BOConverter.getItemVersion(signOffRequest.getItemBO()))
                            .resource(BOConverter.getResource(signOffRequest.getResourceBO()))
                            .router(BOConverter.getRouter(signOffRequest.getRouterBO()))
                            .routerVersion(BOConverter.getRouterVersion(signOffRequest.getRouterBO()))
                            .operation(op)
                            .operationVersion(opVersion)
                            .stepID(signOffRequest.getStepID())
                            .user(BOConverter.getUser(signOffRequest.getUserBO()))
                            .qtyInQueue(qtyInqueVal)
                            .qtyToComplete(signOffRequest.getQtyToComplete())
                            .shopOrder(BOConverter.getShopOrder(signOffRequest.getShopOrderBO()))
//                            .childRouter(BOConverter.getChildRouter(signOffRequest.getChildRouterBO()))
//                            .childRouterVersion(BOConverter.getChildRouterVersion(signOffRequest.getChildRouterBO()))
                            .parentStepID(signOffRequest.getParentStepID())
                            .build();
                    PcuInQueue createPcuInQueue = webClientBuilder.build()
                            .post()
                            .uri(pcuInQueueCreateUrl)
                            .bodyValue(CreatePcuInQueueRequest)
                            .retrieve()
                            .bodyToMono(PcuInQueue.class)
                            .block();
                    StartRequest deletePcuInWorkRequest = StartRequest.builder().site(signOffRequest.getSite()).pcu(BOConverter.getPcu(signOffRequest.getPcuBO())).operation(op).operationVersion(opVersion).build();
                    Response deletePcuInWork = webClientBuilder.build()
                            .post()
                            .uri(pcuInWorkDeleteUrl)
                            .bodyValue(deletePcuInWorkRequest)
                            .retrieve()
                            .bodyToMono(Response.class)
                            .block();
                    successPcus.add(signOffRequest.getPcuBO());
                    signOffRequest.setQuantity(qtyInqueVal);
                    Boolean productionLogged = productionLog(signOffRequest);
//                } else if (Integer.parseInt(retrievePcuInWork.getQtyInWork()) > Integer.parseInt(signOffRequest.getQuantity())) {
                } else if (Double.parseDouble(retrievePcuInWork.getQtyInWork()) > Double.parseDouble(signOffRequest.getQuantity())) {
                    if(retrievePcuInQueue.getQtyInQueue() == null)
                    {
                        retrievePcuInQueue.setQtyInQueue("0");
                    }
                    PcuInQueueRequest createPcuInQueueRequest = PcuInQueueRequest.builder()
                            .site(signOffRequest.getSite())
                            .handle(retrievePcuInQueue.getHandle())
                            .pcu(BOConverter.getPcu(signOffRequest.getPcuBO()))
                            .item(BOConverter.getItem(signOffRequest.getItemBO()))
                            .itemVersion(BOConverter.getItemVersion(signOffRequest.getItemBO()))
                            .resource(BOConverter.getResource(signOffRequest.getResourceBO()))
                            .router(BOConverter.getRouter(signOffRequest.getRouterBO()))
                            .routerVersion(BOConverter.getRouterVersion(signOffRequest.getRouterBO()))
                            .operation(op)
                            .operationVersion(opVersion)
                            .stepID(signOffRequest.getStepID())
                            .user(BOConverter.getUser(signOffRequest.getUserBO()))
//                            .qtyInQueue(String.valueOf(Integer.parseInt(retrievePcuInQueue.getQtyInQueue()) + Integer.parseInt(signOffRequest.getQuantity())))
                            .qtyInQueue(String.valueOf(Double.parseDouble(retrievePcuInQueue.getQtyInQueue()) + Double.parseDouble(signOffRequest.getQuantity())))
                            .qtyToComplete(signOffRequest.getQtyToComplete())
                            .shopOrder(BOConverter.getShopOrder(signOffRequest.getShopOrderBO()))
//                            .childRouter(BOConverter.getChildRouter(signOffRequest.getChildRouterBO()))
//                            .childRouterVersion(BOConverter.getChildRouterVersion(signOffRequest.getChildRouterBO()))
                            .parentStepID(signOffRequest.getParentStepID())
                            .build();
                    PcuInQueue createPcuInQueue = webClientBuilder.build()
                            .post()
                            .uri(pcuInQueueCreateUrl)
                            .bodyValue(createPcuInQueueRequest)
                            .retrieve()
                            .bodyToMono(PcuInQueue.class)
                            .block();

                    StartRequest createPcuInWorkRequest = StartRequest.builder()
                            .site(signOffRequest.getSite())
//                            .qtyInWork(String.valueOf(Integer.parseInt(retrievePcuInWork.getQtyInWork())-Integer.parseInt(signOffRequest.getQuantity())))
                            .qtyInWork(String.valueOf(Double.parseDouble(retrievePcuInWork.getQtyInWork())-Double.parseDouble(signOffRequest.getQuantity())))
                            .pcu(BOConverter.getPcu(signOffRequest.getPcuBO()))
                            .item(BOConverter.getItem(signOffRequest.getItemBO()))
                            .itemVersion(BOConverter.getItemVersion(signOffRequest.getItemBO()))
                            .resource(BOConverter.getResource(signOffRequest.getResourceBO()))
                            .router(BOConverter.getRouter(signOffRequest.getRouterBO()))
                            .routerVersion(BOConverter.getRouterVersion(signOffRequest.getRouterBO()))
                            .operation(op)
                            .operationVersion(opVersion)
                            .stepID(signOffRequest.getStepID())
                            .user(BOConverter.getUser(signOffRequest.getUserBO()))
                            .qtyToComplete(signOffRequest.getQtyToComplete())
                            .shopOrder(BOConverter.getShopOrder(signOffRequest.getShopOrderBO()))
//                            .childRouter(BOConverter.getChildRouter(signOffRequest.getChildRouterBO()))
//                            .childRouterVersion(BOConverter.getChildRouterVersion(signOffRequest.getChildRouterBO()))
                            .parentStepID(signOffRequest.getParentStepID())
                            .build();
                    PcuInWorkMessageModel createPcuInWork = webClientBuilder.build()
                            .post()
                            .uri(pcuInWorkCreateUrl)
                            .bodyValue(createPcuInWorkRequest)
                            .retrieve()
                            .bodyToMono(PcuInWorkMessageModel.class)
                            .block();
                    PcuInWorkMessageModel created = createPcuInWork;
                    successPcus.add(signOffRequest.getPcuBO());
//                    signOffRequest.setQuantity(String.valueOf(Integer.parseInt(retrievePcuInQueue.getQtyInQueue()) + Integer.parseInt(signOffRequest.getQuantity())));
                    signOffRequest.setQuantity(String.valueOf(Double.parseDouble(retrievePcuInQueue.getQtyInQueue()) + Double.parseDouble(signOffRequest.getQuantity())));
                    Boolean productionLogged = productionLog(signOffRequest);
                } else {
                    throw new SignOffException(9901, signOffRequest.getPcuBO());
                }
            }
//            messageDetailsList.add(MessageDetails.builder().msg(signOffRequest.getPcuBO()+" SignedOff Successfully").msg_type("s").build());

        }
        if(!errorPcus.isEmpty()) {
            List<String> splitErrorPcus = errorPcus.stream()
                    .filter(str -> str != null)
                    .flatMap(str -> Arrays.stream(str.split(",")).skip(1))
                    .collect(Collectors.toList());
            errorMessage = "Failed to SignOff Pcu :" + String.join(",", splitErrorPcus);
        }
        if(!successPcus.isEmpty()) {
            List<String> splitSuccessPcus = successPcus.stream()
                    .filter(str -> str != null)
                    .flatMap(str -> Arrays.stream(str.split(",")).skip(1))
                    .collect(Collectors.toList());
            successMessage = "SignedOff successfully Pcu :" + String.join(",", splitSuccessPcus);
        }
        if(errorMessage!=null && successMessage == null) {
            messageDetails = MessageDetails.builder().msg(errorMessage).msg_type("E").build();
        }
        if(successMessage != null)
        {
            messageDetails = MessageDetails.builder().msg(successMessage).msg_type("S").build();
        }
        return MessageModel.builder().message_details(messageDetails).build();
    }

    public Boolean productionLog(SignOffRequest signOffRequest)
    {
//        MinutesList shiftRecordList = getShiftBreakHours(signOffRequest.getSite());
//        Minutes minutesRecord = null;
//        for(Minutes shift : shiftRecordList.getMinutesList())
//        {
//            if(shift.getShiftType().equalsIgnoreCase("general"))
//            {
//                minutesRecord = shift;
//                break;
//            }
//        }

        ProductionLog productionLogRecord = retrieveProductionLog(signOffRequest.getPcuBO(),signOffRequest.getShopOrderBO(),signOffRequest.getOperationBO(), signOffRequest.getSite());
        long minutesDifference = 0;
        if(productionLogRecord != null)
        {
            minutesDifference = Duration.between(productionLogRecord.getCreated_datetime(),LocalDateTime.now()).toSeconds();
        }
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("BATCH_SIGNOFF")
                .userId(signOffRequest.getUserBO())
                .pcu(BOConverter.getPcu(signOffRequest.getPcuBO()))
                .shopOrderBO(BOConverter.getShopOrder(signOffRequest.getShopOrderBO()))
                .operation_bo(signOffRequest.getOperationBO())
                .routerBO(signOffRequest.getRouterBO())
                .workcenterId(signOffRequest.getWorkCenter())
                .resourceId(signOffRequest.getResourceBO())
                .itemBO(signOffRequest.getItemBO())
                .site(signOffRequest.getSite())
//                .shiftName(minutesRecord.getShiftName())
//                .shiftStartTime(minutesRecord.getStartTime().toString())
//                .shiftEndTime(minutesRecord.getEndTime().toString())
//                .totalBreakHours(String.valueOf(minutesRecord.getMinutes()))
                .actualCycleTime(Double.valueOf(minutesDifference))
                .qty(Integer.parseInt(signOffRequest.getQuantity()))
                .status("New")
                .topic("production-log")
                .eventData(signOffRequest.getPcuBO()+" SignedOff Successfully")
                .build();
        eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));
        return true;
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

    public ProductionLog retrieveProductionLog(String pcuBO, String shopOrderBO, String operationBO, String site)
    {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("BATCH_START")
                .site(site)
                .pcu(pcuBO)
                .shopOrderBO(shopOrderBO)
                .operation(BOConverter.getOperation(operationBO))
                .operationVersion(BOConverter.getOperationVersion(operationBO))
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
}
